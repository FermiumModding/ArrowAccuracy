package arrowaccuracy.mixin.mobends;

import arrowaccuracy.client.ModelCapture;
import goblinbob.mobends.core.client.model.ModelPart;
import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin {

	@Inject(
			method = "renderPart",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;callList(I)V", remap = true),
			remap = false
	)
	private void arrowaccuracy_moBendsModelPart_renderPart(float scale, CallbackInfo ci) {
		ModelCapture.onPartRender((ModelRenderer)(Object)this, scale);
	}

	@Inject(
			method = "renderJustPart",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;callList(I)V", remap = true),
			remap = false
	)
	private void arrowaccuracy_moBendsModelPart_renderJustPart(float scale, CallbackInfo ci) {
		ModelCapture.onPartRender((ModelRenderer)(Object)this, scale);
	}
}