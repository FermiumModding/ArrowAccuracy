package arrowaccuracy.mixin.vanilla;

import arrowaccuracy.handlers.ForgeConfigHandler;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArrow.class)
public abstract class LayerArrowMixin {

	@Inject(
			method = "doRenderLayer",
			at = @At("HEAD"),
			cancellable = true
	)
	private void arrowaccuracy_vanillaLayerArrow_doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
		if(ForgeConfigHandler.CLIENT.hideVanillaArrows) ci.cancel();
	}
}
