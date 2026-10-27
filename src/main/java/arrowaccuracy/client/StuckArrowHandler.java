package arrowaccuracy.client;

import arrowaccuracy.ArrowAccuracy;
import arrowaccuracy.handlers.ForgeConfigHandler;
import arrowaccuracy.mixin.vanilla.RenderAccessor;
import arrowaccuracy.util.IEntityArrow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = ArrowAccuracy.MODID, value = Side.CLIENT)
public class StuckArrowHandler {

	private static final int SERVER_UPDATE_GRACE = 3;
	private static final Map<EntityLivingBase,List<StuckArrow>> STUCK = new WeakHashMap<>();

	public static void onClientHit(EntityArrow arrow, EntityLivingBase target) {
		IEntityArrow holder = (IEntityArrow)arrow;
		cancelPending(holder);
		if(willServerReject(arrow, target)) return;

		StuckArrow stuck = new StuckArrow(arrow, target, false);
		holder.arrowaccuracy$setPending(stuck);
		add(target, stuck);
	}
    
    //Janky but helps with visual desync for common arrow blocking
    private static boolean willServerReject(EntityArrow arrow, EntityLivingBase target) {
        Entity shooter = arrow.shootingEntity;
        DamageSource source = DamageSource.causeArrowDamage(arrow, shooter == null ? arrow : shooter);
        if(target.isEntityInvulnerable(source)) return true;
        if(target instanceof EntityPlayer && (((EntityPlayer)target).isCreative() || ((EntityPlayer)target).isSpectator())) return true;
        if(target instanceof EntityEnderman) return true;
        if(target instanceof EntityShulker && ((EntityShulker)target).getPeekTick() == 0) return true;
        if(target instanceof EntityWither) {
            EntityWither wither = (EntityWither)target;
            if(wither.getInvulTime() > 0 || wither.isArmored()) return true;
            if(shooter instanceof EntityLivingBase && !(shooter instanceof EntityPlayer) && ((EntityLivingBase)shooter).getCreatureAttribute() == wither.getCreatureAttribute()) return true;
        }
        if(target.isActiveItemStackBlocking()) {
            Vec3d toArrow = new Vec3d(arrow.posX, arrow.posY, arrow.posZ).subtractReverse(new Vec3d(target.posX, target.posY, target.posZ)).normalize();
            return new Vec3d(toArrow.x, 0.0D, toArrow.z).dotProduct(target.getLook(1.0F)) < 0.0D;
        }
        return false;
    }

	public static void onServerPositionUpdate(EntityArrow arrow) {
		IEntityArrow holder = (IEntityArrow)arrow;
		StuckArrow pending = holder.arrowaccuracy$getPending();
		if(pending == null || pending.confirmed || pending.age < SERVER_UPDATE_GRACE) return;
		cancelPending(holder);
	}

	public static void onArrowRemoved(EntityArrow arrow) {
		IEntityArrow holder = (IEntityArrow)arrow;
		StuckArrow pending = holder.arrowaccuracy$getPending();
		if(pending != null) {
			holder.arrowaccuracy$setPending(null);
			if(!pending.confirmed && pending.age <= ForgeConfigHandler.CLIENT.hitConfirmationTimeout) {
				pending.confirmed = true;
				arrow.extinguish();
			}
			return;
		}
		if(holder.arrowaccuracy$hasHitSomething() || holder.arrowaccuracy$isInGround()) return;
		traceMissedHit(arrow);
	}

	private static void traceMissedHit(EntityArrow arrow) {
		Vec3d motion = new Vec3d(arrow.motionX, arrow.motionY, arrow.motionZ);
		if(motion.lengthSquared() < 1.0E-4D) return;
		Vec3d start = new Vec3d(arrow.posX, arrow.posY, arrow.posZ);
		Vec3d end = start.add(motion.scale(2.0D));

		EntityLivingBase hit = null;
		double best = Double.MAX_VALUE;
		AxisAlignedBB search = arrow.getEntityBoundingBox().expand(motion.x * 2.0D, motion.y * 2.0D, motion.z * 2.0D).grow(1.0D);
		for(EntityLivingBase entity : arrow.world.getEntitiesWithinAABB(EntityLivingBase.class, search)) {
			if(entity == arrow.shootingEntity || entity.isDead || (entity instanceof EntityPlayer && ((EntityPlayer)entity).isSpectator())) continue;
			AxisAlignedBB box = entity.getEntityBoundingBox().grow(0.3D);
			double dist;
			if(box.contains(start)) dist = 0.0D;
			else {
				RayTraceResult result = box.calculateIntercept(start, end);
				if(result == null) continue;
				dist = start.squareDistanceTo(result.hitVec);
			}
			if(dist < best) {
				best = dist;
				hit = entity;
			}
		}
		if(hit == null) return;

		arrow.extinguish();
		add(hit, new StuckArrow(arrow, hit, true));
	}

	private static void add(EntityLivingBase target, StuckArrow stuck) {
		List<StuckArrow> list = STUCK.computeIfAbsent(target, k -> new ArrayList<>());
		list.add(stuck);
		while(list.size() > ForgeConfigHandler.CLIENT.maxArrowsPerEntity) list.remove(0);
	}

	private static void cancelPending(IEntityArrow holder) {
		StuckArrow pending = holder.arrowaccuracy$getPending();
		holder.arrowaccuracy$setPending(null);
		if(pending == null || pending.confirmed) return;
		for(List<StuckArrow> list : STUCK.values()) {
			if(list.remove(pending)) return;
		}
	}

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		if(event.getWorld().isRemote) STUCK.clear();
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		if(event.getWorld().isRemote) STUCK.clear();
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase != TickEvent.Phase.END) return;
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.world == null || mc.isGamePaused()) return;

		int duration = ForgeConfigHandler.CLIENT.stuckArrowDuration;
		int timeout = ForgeConfigHandler.CLIENT.hitConfirmationTimeout;
		Iterator<Map.Entry<EntityLivingBase,List<StuckArrow>>> iterator = STUCK.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<EntityLivingBase,List<StuckArrow>> entry = iterator.next();
			EntityLivingBase target = entry.getKey();
			if(target == null || target.isDead) {
				iterator.remove();
				continue;
			}
			List<StuckArrow> list = entry.getValue();
			list.removeIf(stuck -> {
				stuck.age++;
				if(stuck.confirmed) return stuck.age >= duration;
				if(stuck.age <= timeout) return false;
				IEntityArrow holder = (IEntityArrow)stuck.arrow;
				if(holder.arrowaccuracy$getPending() == stuck) holder.arrowaccuracy$setPending(null);
				return true;
			});
			if(list.isEmpty()) iterator.remove();
		}
	}

	public static boolean beginRender(Entity entity, Render<?> render) {
		if(!(entity instanceof EntityLivingBase)) return false;
		List<StuckArrow> list = STUCK.get(entity);
		if(list == null || list.isEmpty()) return false;
		if(((RenderAccessor)render).arrowaccuracy$getRenderOutlines()) return false;
		ModelCapture.begin((EntityLivingBase)entity, render instanceof RenderLivingBase ? ((RenderLivingBase<?>)render).getMainModel() : null);
		return true;
	}
 
	public static void endRender(EntityLivingBase entity, double x, double y, double z, float partialTicks) {
		if(!ModelCapture.isCapturing(entity)) return;
		Map<Object,ModelCapture.CapturedPart> parts = ModelCapture.end();
		List<StuckArrow> list = STUCK.get(entity);
		if(list == null || list.isEmpty()) return;

		Vec3d renderPos = new Vec3d(x, y, z);
		float yaw = entity.prevRenderYawOffset + MathHelper.wrapDegrees(entity.renderYawOffset - entity.prevRenderYawOffset) * partialTicks;
		boolean worldPass = isWorldPass(entity, renderPos, partialTicks);

		for(StuckArrow stuck : list) {
			if(!stuck.isResolved() && worldPass) stuck.resolve(entity, parts, renderPos, yaw);
			if(stuck.isResolved()) stuck.render(parts, renderPos, yaw, partialTicks);
		}
	}

	private static boolean isWorldPass(EntityLivingBase entity, Vec3d renderPos, float partialTicks) {
		RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		Vec3d expected = new Vec3d(
				entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - manager.viewerPosX,
				entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - manager.viewerPosY,
				entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - manager.viewerPosZ);
		return expected.squareDistanceTo(renderPos) < 1.0E-6D;
	}
}