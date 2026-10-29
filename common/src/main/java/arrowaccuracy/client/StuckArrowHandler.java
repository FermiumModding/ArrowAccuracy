package arrowaccuracy.client;

import arrowaccuracy.config.ConfigHandler;
import arrowaccuracy.mixin.vanilla.ShulkerInvoker;
import arrowaccuracy.platform.Services;
import arrowaccuracy.util.IAbstractArrow;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class StuckArrowHandler {

	private static final int SERVER_UPDATE_GRACE = 3;
	private static final Map<LivingEntity,List<StuckArrow>> STUCK = new WeakHashMap<>();
	private static ClientLevel lastLevel = null;

	@Nullable
	public static LivingEntity getTarget(@Nullable Entity entity) {
		if(entity instanceof LivingEntity living) return living;
		if(entity instanceof EnderDragonPart part) return part.parentMob;
		return Services.PLATFORM.getMultipartParent(entity);
	}

	public static void onClientHit(AbstractArrow arrow, LivingEntity target) {
		if(arrow instanceof ThrownTrident) return;
		IAbstractArrow holder = (IAbstractArrow)arrow;
		cancelPending(holder);
		if(willServerReject(arrow, target)) return;

		StuckArrow stuck = new StuckArrow(arrow, target, false);
		holder.arrowaccuracy$setPending(stuck);
		add(target, stuck);
	}

	//Janky but helps with visual desync for common arrow blocking
	private static boolean willServerReject(AbstractArrow arrow, LivingEntity target) {
		Entity shooter = arrow.getOwner();
		DamageSource source = arrow.damageSources().arrow(arrow, shooter == null ? arrow : shooter);
		if(target.isInvulnerableTo(source)) return true;
		if(target instanceof Player player && (player.isCreative() || player.isSpectator())) return true;
		if(target instanceof EnderMan) return true;
		if(target instanceof Shulker shulker && ((ShulkerInvoker)shulker).arrowaccuracy$invokeIsClosed()) return true;
		if(target instanceof WitherBoss wither) {
			if(wither.getInvulnerableTicks() > 0 || wither.isPowered()) return true;
			if(shooter instanceof WitherBoss) return true;
		}
		return target.isDamageSourceBlocked(source);
	}

	public static void onServerPositionUpdate(AbstractArrow arrow) {
		IAbstractArrow holder = (IAbstractArrow)arrow;
		StuckArrow pending = holder.arrowaccuracy$getPending();
		if(pending == null || pending.confirmed || pending.age < SERVER_UPDATE_GRACE) return;
		cancelPending(holder);
	}

	public static void onArrowRemoved(AbstractArrow arrow) {
		if(arrow instanceof ThrownTrident) return;
		IAbstractArrow holder = (IAbstractArrow)arrow;
		StuckArrow pending = holder.arrowaccuracy$getPending();
		if(pending != null) {
			holder.arrowaccuracy$setPending(null);
			if(!pending.confirmed && pending.age <= ConfigHandler.CONFIG.hitConfirmationTimeout) {
				pending.confirmed = true;
			}
			return;
		}
		if(holder.arrowaccuracy$hasHitSomething() || holder.arrowaccuracy$isInGround()) return;
		traceMissedHit(arrow);
	}

	private static void traceMissedHit(AbstractArrow arrow) {
		Vec3 motion = arrow.getDeltaMovement();
		if(motion.lengthSqr() < 1.0E-4D) return;
		Vec3 start = arrow.position();
		Vec3 end = start.add(motion.scale(2.0D));

		LivingEntity hit = null;
		double best = Double.MAX_VALUE;
		AABB search = arrow.getBoundingBox().expandTowards(motion.x * 2.0D, motion.y * 2.0D, motion.z * 2.0D).inflate(1.0D);
		for(Entity entity : arrow.level().getEntities(arrow, search)) {
			LivingEntity target = getTarget(entity);
			if(target == null || target == arrow.getOwner() || target.isRemoved() || target.isSpectator()) continue;
			AABB box = entity.getBoundingBox().inflate(0.3D);
			double dist;
			if(box.contains(start)) dist = 0.0D;
			else {
				Optional<Vec3> result = box.clip(start, end);
				if(result.isEmpty()) continue;
				dist = start.distanceToSqr(result.get());
			}
			if(dist < best) {
				best = dist;
				hit = target;
			}
		}
		if(hit == null) return;

		add(hit, new StuckArrow(arrow, hit, true));
	}

	private static void add(LivingEntity target, StuckArrow stuck) {
		List<StuckArrow> list = STUCK.computeIfAbsent(target, k -> new ArrayList<>());
		list.add(stuck);
		while(list.size() > ConfigHandler.CONFIG.maxArrowsPerEntity) list.remove(0);
	}

	private static void cancelPending(IAbstractArrow holder) {
		StuckArrow pending = holder.arrowaccuracy$getPending();
		holder.arrowaccuracy$setPending(null);
		if(pending == null || pending.confirmed) return;
		for(List<StuckArrow> list : STUCK.values()) {
			if(list.remove(pending)) return;
		}
	}

	public static void onClientTickEnd() {
		Minecraft mc = Minecraft.getInstance();
		if(mc.level != lastLevel) {
			STUCK.clear();
			lastLevel = mc.level;
		}
		if(mc.level == null || mc.isPaused()) return;

		int duration = ConfigHandler.CONFIG.stuckArrowDuration;
		int timeout = ConfigHandler.CONFIG.hitConfirmationTimeout;
		Iterator<Map.Entry<LivingEntity,List<StuckArrow>>> iterator = STUCK.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<LivingEntity,List<StuckArrow>> entry = iterator.next();
			LivingEntity target = entry.getKey();
			if(target == null || target.isRemoved()) {
				iterator.remove();
				continue;
			}
			List<StuckArrow> list = entry.getValue();
			list.removeIf(stuck -> {
				stuck.age++;
				if(stuck.confirmed) return stuck.age >= duration;
				if(stuck.age <= timeout) return false;
				IAbstractArrow holder = (IAbstractArrow)stuck.arrow;
				if(holder.arrowaccuracy$getPending() == stuck) holder.arrowaccuracy$setPending(null);
				return true;
			});
			if(list.isEmpty()) iterator.remove();
		}
	}

	public static void beginRender(Entity entity, EntityRenderer<?> renderer, PoseStack poseStack) {
		if(!(entity instanceof LivingEntity living) || ModelCapture.isActive()) return;
		List<StuckArrow> list = STUCK.get(living);
		if(list == null || list.isEmpty()) return;
		ModelCapture.begin(living, renderer instanceof LivingEntityRenderer, poseStack.last().pose());
	}

	public static void endRender(Entity entity, double x, double y, double z, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if(!(entity instanceof LivingEntity living) || !ModelCapture.isCapturing(living)) return;
		Map<Object,ModelCapture.CapturedPart> parts = ModelCapture.end();
		List<StuckArrow> list = STUCK.get(living);
		if(list == null || list.isEmpty()) return;

		Vec3 renderPos = new Vec3(x, y, z);
		float yaw = Mth.rotLerp(partialTicks, living.yBodyRotO, living.yBodyRot);
		boolean worldPass = isWorldPass(living, renderPos, partialTicks);

		for(StuckArrow stuck : list) {
			if(!stuck.isResolved() && worldPass) stuck.resolve(living, parts, renderPos, yaw);
			if(stuck.isResolved()) stuck.render(parts, renderPos, yaw, partialTicks, poseStack, buffer, packedLight);
		}
	}

	private static boolean isWorldPass(LivingEntity entity, Vec3 renderPos, float partialTicks) {
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		Vec3 expected = new Vec3(
				Mth.lerp(partialTicks, entity.xOld, entity.getX()) - camera.x,
				Mth.lerp(partialTicks, entity.yOld, entity.getY()) - camera.y,
				Mth.lerp(partialTicks, entity.zOld, entity.getZ()) - camera.z);
		return expected.distanceToSqr(renderPos) < 1.0E-6D;
	}
}