package arrowaccuracy.mixin.geckolib;

import arrowaccuracy.compat.GeckoLibCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.util.MatrixStack;

@Mixin(MatrixStack.class)
public abstract class MatrixStackMixin {

	@Inject(
			method = "moveBackFromPivot(Lsoftware/bernie/geckolib3/geo/render/built/GeoCube;)V",
			at = @At("RETURN"),
			remap = false
	)
	private void arrowaccuracy_geckolibMatrixStack_moveBackFromPivot(GeoCube cube, CallbackInfo ci) {
		GeckoLibCompat.onCubeRender(cube, ((MatrixStack)(Object)this).getModelMatrix());
	}
}