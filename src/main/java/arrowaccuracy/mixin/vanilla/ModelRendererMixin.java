package arrowaccuracy.mixin.vanilla;

import arrowaccuracy.client.ModelCapture;
import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelRenderer.class)
public abstract class ModelRendererMixin {
	
	@Inject(
			method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;callList(I)V")
	)
	private void arrowaccuracy_vanillaModelRenderer_render(float scale, CallbackInfo ci) {
		ModelCapture.onPartRender((ModelRenderer)(Object)this, scale);
	}

	@Inject(
			method = "renderWithRotation",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;callList(I)V")
	)
	private void arrowaccuracy_vanillaModelRenderer_renderWithRotation(float scale, CallbackInfo ci) {
		ModelCapture.onPartRender((ModelRenderer)(Object)this, scale);
	}
}