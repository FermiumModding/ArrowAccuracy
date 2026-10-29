package arrowaccuracy.client;

import arrowaccuracy.config.ConfigHandler;
import arrowaccuracy.util.AlphaVertexConsumer;
import arrowaccuracy.util.ArrowMath;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class StuckArrow {

	public final AbstractArrow arrow;
	private final Vec3 rayOrigin;
	private final Vec3 rayDir;
	private final float hitYaw;

	public int age = 0;
	public boolean confirmed;
	private boolean resolved = false;
	private Object part = null;
	private Vec3 localPos;
	private Vec3 localDir;

	public StuckArrow(AbstractArrow arrow, LivingEntity target, boolean confirmed) {
		this.arrow = arrow;
		this.rayOrigin = arrow.position().subtract(target.position());
		this.rayDir = arrow.getDeltaMovement();
		this.hitYaw = target.yBodyRot;
		this.confirmed = confirmed;
	}

	public boolean isResolved() {
		return this.resolved;
	}

	public void resolve(LivingEntity entity, Map<Object,ModelCapture.CapturedPart> parts, Vec3 renderPos, float yaw) {
		float yawDelta = yaw - this.hitYaw;
		Vec3 relOrigin = ArrowMath.rotateYaw(this.rayOrigin, yawDelta);
		Vec3 dir = ArrowMath.rotateYaw(this.rayDir, yawDelta);

		this.resolved = true;
		if(!this.resolveModel(parts, relOrigin.add(renderPos), dir)) this.resolveBB(entity, relOrigin, dir, yaw);
	}

	private boolean resolveModel(Map<Object,ModelCapture.CapturedPart> parts, Vec3 origin, Vec3 dir) {
		Candidate best = null;
		for(Map.Entry<Object,ModelCapture.CapturedPart> entry : parts.entrySet()) {
			Object part = entry.getKey();
			ModelCapture.CapturedPart captured = entry.getValue();
			if(captured.getBounds().isEmpty() || captured.getFromWorld() == null) continue;

			Vec3 localOrigin = ArrowMath.transform(captured.getFromWorld(), origin, 1.0D);
			Vec3 localDir = ArrowMath.transform(captured.getFromWorld(), dir, 0.0D);

			for(AABB bounds : captured.getBounds()) {
				Candidate candidate = new Candidate(part, captured, ArrowMath.impact(bounds, localOrigin, localDir), localDir);
				if(best == null || candidate.beats(best)) best = candidate;
			}
		}
		if(best == null) return false;

		this.part = best.part;
		this.localPos = best.impact.point;
		this.localDir = best.localDir;
		return true;
	}

	//fallback around bounding box
	private void resolveBB(LivingEntity entity, Vec3 relOrigin, Vec3 dir, float yaw) {
		AABB bb = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
		ArrowMath.Impact impact = ArrowMath.impact(bb, relOrigin, dir);
		if(!impact.hit) {
			ArrowMath.Impact grown = ArrowMath.impact(bb.inflate(0.3D), relOrigin, dir);
			if(grown.hit) impact = grown;
		}
		this.part = null;
		this.localPos = ArrowMath.rotateYaw(impact.point, -yaw);
		this.localDir = ArrowMath.rotateYaw(dir, -yaw);
	}

	public void render(Map<Object,ModelCapture.CapturedPart> parts, Vec3 renderPos, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		int duration = ConfigHandler.CONFIG.stuckArrowDuration;
		int fade = Math.min(ConfigHandler.CONFIG.fadeOutDuration, duration);
		float remaining = duration - (this.age + partialTicks);
		float alpha = fade > 0 ? Mth.clamp(remaining / fade, 0.0F, 1.0F) : 1.0F;
		if(alpha <= 0.0F) return;

		Vec3 pos;
		Vec3 dir;
		if(this.part != null) {
			ModelCapture.CapturedPart captured = parts.get(this.part);
			if(captured == null) return;
			pos = ArrowMath.transform(captured.toWorld, this.localPos, 1.0D);
			dir = ArrowMath.transform(captured.toWorld, this.localDir, 0.0D).normalize();
		}
		else {
			pos = ArrowMath.rotateYaw(this.localPos, yaw).add(renderPos);
			dir = ArrowMath.rotateYaw(this.localDir, yaw).normalize();
		}
		float arrowYaw = (float)(Mth.atan2(dir.x, dir.z) * (180D / Math.PI));
		float arrowPitch = (float)(Mth.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)) * (180D / Math.PI));
		float[] previous = { this.arrow.getYRot(), this.arrow.yRotO, this.arrow.getXRot(), this.arrow.xRotO };
		int previousShake = this.arrow.shakeTime;
		this.setArrowPose(arrowYaw, arrowYaw, arrowPitch, arrowPitch, 0);

		EntityRenderer<? super AbstractArrow> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this.arrow);
		poseStack.pushPose();
		poseStack.translate(pos.x, pos.y, pos.z);
		renderer.render(this.arrow, arrowYaw, partialTicks, poseStack, alpha < 1.0F ? fadeBuffer(buffer, renderer.getTextureLocation(this.arrow), alpha) : buffer, packedLight);
		poseStack.popPose();

		this.setArrowPose(previous[0], previous[1], previous[2], previous[3], previousShake);
	}

	private void setArrowPose(float yaw, float prevYaw, float pitch, float prevPitch, int shake) {
		this.arrow.setYRot(yaw);
		this.arrow.yRotO = prevYaw;
		this.arrow.setXRot(pitch);
		this.arrow.xRotO = prevPitch;
		this.arrow.shakeTime = shake;
	}

	private static MultiBufferSource fadeBuffer(MultiBufferSource buffer, ResourceLocation texture, float alpha) {
		RenderType cutout = RenderType.entityCutout(texture);
		return type -> new AlphaVertexConsumer(buffer.getBuffer(type == cutout ? RenderType.entityTranslucent(texture) : type), alpha);
	}

	private static class Candidate {
		private final Object part;
		private final ArrowMath.Impact impact;
		private final Vec3 localDir;
		private final double score;

		private Candidate(Object part, ModelCapture.CapturedPart captured, ArrowMath.Impact impact, Vec3 localDir) {
			this.part = part;
			this.impact = impact;
			this.localDir = localDir;
			this.score = impact.hit ? impact.t : ArrowMath.transform(captured.toWorld, impact.onRay, 1.0D).distanceToSqr(ArrowMath.transform(captured.toWorld, impact.point, 1.0D));
		}

		private boolean beats(Candidate other) {
			if(this.impact.hit != other.impact.hit) return this.impact.hit;
			return this.score < other.score;
		}
	}
}