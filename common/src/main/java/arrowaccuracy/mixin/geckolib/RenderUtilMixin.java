package arrowaccuracy.mixin.geckolib;

import arrowaccuracy.compat.GeckoLibCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.util.RenderUtil;

@Mixin(value = RenderUtil.class, remap = false)
public abstract class RenderUtilMixin {

	@Inject(
			method = "rotateMatrixAroundCube",
			at = @At("RETURN")
	)
	private static void arrowaccuracy_geckolibRenderUtil_rotateMatrixAroundCube(PoseStack poseStack, GeoCube cube, CallbackInfo ci) {
		GeckoLibCompat.onCubeRender(poseStack, cube);
	}
}