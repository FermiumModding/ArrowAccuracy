package arrowaccuracy.mixin.vanilla;

import arrowaccuracy.client.StuckArrowHandler;
import arrowaccuracy.util.IAbstractArrow;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

	@Shadow
	public abstract <T extends Entity> EntityRenderer<? super T> getRenderer(T entity);

	@Inject(
			method = "shouldRender",
			at = @At("HEAD"),
			cancellable = true
	)
	private void arrowaccuracy_vanillaEntityRenderDispatcher_shouldRender(Entity entity, Frustum frustum, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
		if(entity instanceof IAbstractArrow arrow && arrow.arrowaccuracy$getPending() != null) cir.setReturnValue(false);
	}

	@Inject(
			method = "render",
			at = @At("HEAD")
	)
	private void arrowaccuracy_vanillaEntityRenderDispatcher_render_head(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
		StuckArrowHandler.beginRender(entity, this.getRenderer(entity), poseStack);
	}

	@Inject(
			method = "render",
			at = @At("RETURN")
	)
	private void arrowaccuracy_vanillaEntityRenderDispatcher_render_return(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
		StuckArrowHandler.endRender(entity, x, y, z, partialTicks, poseStack, buffer, packedLight);
	}
}