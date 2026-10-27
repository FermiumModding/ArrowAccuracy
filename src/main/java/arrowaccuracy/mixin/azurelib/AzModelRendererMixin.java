package arrowaccuracy.mixin.azurelib;

import arrowaccuracy.compat.AzureLibCompat;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AzModelRenderer.class)
public abstract class AzModelRendererMixin {

	@Inject(
			method = "renderCube",
			at = @At("HEAD"),
			remap = false
	)
	private void arrowaccuracy_azurelibAzModelRenderer_renderCube(AzRendererPipelineContext<?,?> context, GeoCube cube, CallbackInfo ci) {
		AzureLibCompat.onCubeRender(context, cube);
	}
}