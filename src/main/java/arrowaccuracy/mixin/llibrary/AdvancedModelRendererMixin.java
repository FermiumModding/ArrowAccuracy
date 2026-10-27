package arrowaccuracy.mixin.llibrary;

import arrowaccuracy.client.ModelCapture;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancedModelRenderer.class)
public abstract class AdvancedModelRendererMixin {

	@Inject(
			method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;callList(I)V")
	)
	private void arrowaccuracy_llibraryAdvancedModelRenderer_render(float scale, CallbackInfo ci) {
		ModelCapture.onPartRender((ModelRenderer)(Object)this, scale);
	}
}