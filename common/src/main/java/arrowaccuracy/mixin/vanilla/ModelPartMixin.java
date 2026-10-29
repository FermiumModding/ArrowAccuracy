package arrowaccuracy.mixin.vanilla;

import arrowaccuracy.client.ModelCapture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin {

	@Shadow
	public boolean visible;

	@Shadow
	@Final
	private List<ModelPart.Cube> cubes;

	@Inject(
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
			at = @At("HEAD")
	)
	private void arrowaccuracy_vanillaModelPart_render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if(this.visible && !this.cubes.isEmpty() && ModelCapture.isCapturingModel()) {
			ModelCapture.onModelPartRender((ModelPart)(Object)this, this.cubes, poseStack);
		}
	}
}