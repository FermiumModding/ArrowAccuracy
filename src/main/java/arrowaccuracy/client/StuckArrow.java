package arrowaccuracy.client;

import arrowaccuracy.handlers.ForgeConfigHandler;
import arrowaccuracy.util.ArrowMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL14;

import java.util.Map;

public class StuckArrow {
	private static final int GL_CONSTANT_ALPHA = 0x8003;
	private static final int GL_ONE_MINUS_CONSTANT_ALPHA = 0x8004;

	public final EntityArrow arrow;
	private final Vec3d rayOrigin;
	private final Vec3d rayDir;
	private final float hitYaw;

	public int age = 0;
	public boolean confirmed;
	private boolean resolved = false;
	private Object part = null;
	private Vec3d localPos;
	private Vec3d localDir;

	public StuckArrow(EntityArrow arrow, EntityLivingBase target, boolean confirmed) {
		this.arrow = arrow;
		this.rayOrigin = new Vec3d(arrow.posX - target.posX, arrow.posY - target.posY, arrow.posZ - target.posZ);
		this.rayDir = new Vec3d(arrow.motionX, arrow.motionY, arrow.motionZ);
		this.hitYaw = target.renderYawOffset;
		this.confirmed = confirmed;
	}

	public boolean isResolved() {
		return this.resolved;
	}

	public void resolve(EntityLivingBase entity, Map<Object,ModelCapture.CapturedPart> parts, Vec3d renderPos, float yaw) {
		float yawDelta = yaw - this.hitYaw;
		Vec3d relOrigin = ArrowMath.rotateYaw(this.rayOrigin, yawDelta);
		Vec3d dir = ArrowMath.rotateYaw(this.rayDir, yawDelta);

		this.resolved = true;
		if(!this.resolveModel(parts, relOrigin.add(renderPos), dir)) this.resolveBB(entity, relOrigin, dir, yaw);
	}

	private boolean resolveModel(Map<Object,ModelCapture.CapturedPart> parts, Vec3d origin, Vec3d dir) {
		Candidate best = null;
		for(Map.Entry<Object,ModelCapture.CapturedPart> entry : parts.entrySet()) {
			Object part = entry.getKey();
			ModelCapture.CapturedPart captured = entry.getValue();
			if(captured.getBounds().isEmpty() || captured.getFromWorld() == null) continue;

			Vec3d localOrigin = ArrowMath.transform(captured.getFromWorld(), origin, 1.0D);
			Vec3d localDir = ArrowMath.transform(captured.getFromWorld(), dir, 0.0D);

			for(AxisAlignedBB bounds : captured.getBounds()) {
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
	private void resolveBB(EntityLivingBase entity, Vec3d relOrigin, Vec3d dir, float yaw) {
		AxisAlignedBB bb = entity.getEntityBoundingBox().offset(-entity.posX, -entity.posY, -entity.posZ);
		ArrowMath.Impact impact = ArrowMath.impact(bb, relOrigin, dir);
		if(!impact.hit) {
			ArrowMath.Impact grown = ArrowMath.impact(bb.grow(0.3D), relOrigin, dir);
			if(grown.hit) impact = grown;
		}
		this.part = null;
		this.localPos = ArrowMath.rotateYaw(impact.point, -yaw);
		this.localDir = ArrowMath.rotateYaw(dir, -yaw);
	}

	public void render(Map<Object,ModelCapture.CapturedPart> parts, Vec3d renderPos, float yaw, float partialTicks) {
		int duration = ForgeConfigHandler.CLIENT.stuckArrowDuration;
		int fade = Math.min(ForgeConfigHandler.CLIENT.fadeOutDuration, duration);
		float remaining = duration - (this.age + partialTicks);
		float alpha = fade > 0 ? MathHelper.clamp(remaining / fade, 0.0F, 1.0F) : 1.0F;
		if(alpha <= 0.0F) return;

		Vec3d pos;
		Vec3d dir;
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
		float arrowYaw = (float)(MathHelper.atan2(dir.x, dir.z) * (180D / Math.PI));
		float arrowPitch = (float)(MathHelper.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)) * (180D / Math.PI));
		float[] previous = { this.arrow.rotationYaw, this.arrow.prevRotationYaw, this.arrow.rotationPitch, this.arrow.prevRotationPitch };
		int previousShake = this.arrow.arrowShake;
		this.setArrowPose(arrowYaw, arrowYaw, arrowPitch, arrowPitch, 0);

		boolean fading = alpha < 1.0F;
		if(fading) beginFade(alpha);
		Minecraft.getMinecraft().getRenderManager().renderEntity(this.arrow, pos.x, pos.y, pos.z, arrowYaw, partialTicks, false);
		if(fading) endFade();

		this.setArrowPose(previous[0], previous[1], previous[2], previous[3], previousShake);
	}

	private void setArrowPose(float yaw, float prevYaw, float pitch, float prevPitch, int shake) {
		this.arrow.rotationYaw = yaw;
		this.arrow.prevRotationYaw = prevYaw;
		this.arrow.rotationPitch = pitch;
		this.arrow.prevRotationPitch = prevPitch;
		this.arrow.arrowShake = shake;
	}

	private static void beginFade(float alpha) {
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL_CONSTANT_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA, GlStateManager.SourceFactor.ONE.factor, GlStateManager.DestFactor.ZERO.factor);
		GL14.glBlendColor(1.0F, 1.0F, 1.0F, alpha);
	}

	private static void endFade() {
		GL14.glBlendColor(0.0F, 0.0F, 0.0F, 0.0F);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableBlend();
	}

	private static class Candidate {
		private final Object part;
		private final ArrowMath.Impact impact;
		private final Vec3d localDir;
		private final double score;

		private Candidate(Object part, ModelCapture.CapturedPart captured, ArrowMath.Impact impact, Vec3d localDir) {
			this.part = part;
			this.impact = impact;
			this.localDir = localDir;
			this.score = impact.hit ? impact.t : ArrowMath.transform(captured.toWorld, impact.onRay, 1.0D).squareDistanceTo(ArrowMath.transform(captured.toWorld, impact.point, 1.0D));
		}

		private boolean beats(Candidate other) {
			if(this.impact.hit != other.impact.hit) return this.impact.hit;
			return this.score < other.score;
		}
	}
}