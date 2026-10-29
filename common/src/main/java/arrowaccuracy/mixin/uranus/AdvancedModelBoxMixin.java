package arrowaccuracy.mixin.uranus;

import arrowaccuracy.client.ModelCapture;
import arrowaccuracy.compat.UranusCompat;
import com.iafenvoy.uranus.client.model.AdvancedModelBox;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AdvancedModelBox.class, remap = false)
public abstract class AdvancedModelBoxMixin {

	@Inject(
			method = "doRender",
			at = @At("HEAD")
	)
	private void arrowaccuracy_uranusAdvancedModelBox_doRender(PoseStack.Pose pose, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if(ModelCapture.isCapturingModel()) {
			UranusCompat.onBoxRender((AdvancedModelBox)(Object)this, pose.pose());
		}
	}
}