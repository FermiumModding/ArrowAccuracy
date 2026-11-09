package arrowaccuracy.mixin.fastentityrender;

import arrowaccuracy.client.ModelCapture;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import meldexun.fastentityrender.api.IModelRenderer;
import meldexun.fastentityrender.api.IVertexConsumer;
import meldexun.fastentityrender.renderer.FastModelRenderer;
import meldexun.matrixutil.MatrixStack;
import net.minecraft.client.model.ModelRenderer;
import org.lwjgl.util.vector.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FastModelRenderer.class)
public abstract class FastModelRendererMixin {

	@WrapOperation(
			method = "render(Lmeldexun/fastentityrender/api/IModelRenderer;FZ)V",
			at = @At(value = "INVOKE", target = "Lmeldexun/fastentityrender/api/IModelRenderer;render(Lmeldexun/matrixutil/MatrixStack;FLmeldexun/fastentityrender/api/IVertexConsumer;)V"),
			remap = false
	)
	private void arrowaccuracy_fastEntityRenderFastModelRenderer_render(IModelRenderer bone, MatrixStack matrixStack, float scale, IVertexConsumer vertexConsumer, Operation<Void> original) {
		if((Object)bone instanceof ModelRenderer && ModelCapture.isCapturingPart(bone)) {
			ModelCapture.onPartRender((ModelRenderer)(Object)bone, scale, toLwjgl(matrixStack.last().modelMatrix()));
		}
		original.call(bone, matrixStack, scale, vertexConsumer);
	}

	private static Matrix4f toLwjgl(meldexun.matrixutil.Matrix4f m) {
		Matrix4f dest = new Matrix4f();
		dest.m00 = m.m00; dest.m01 = m.m10; dest.m02 = m.m20; dest.m03 = m.m30;
		dest.m10 = m.m01; dest.m11 = m.m11; dest.m12 = m.m21; dest.m13 = m.m31;
		dest.m20 = m.m02; dest.m21 = m.m12; dest.m22 = m.m22; dest.m23 = m.m32;
		dest.m30 = m.m03; dest.m31 = m.m13; dest.m32 = m.m23; dest.m33 = m.m33;
		return dest;
	}
}