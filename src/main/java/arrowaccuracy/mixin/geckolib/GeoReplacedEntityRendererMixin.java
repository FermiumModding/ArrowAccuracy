package arrowaccuracy.mixin.geckolib;

import arrowaccuracy.compat.GeckoLibCompat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoReplacedEntityRenderer;

@Mixin(GeoReplacedEntityRenderer.class)
public abstract class GeoReplacedEntityRendererMixin {

	@WrapOperation(
			method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
			at = @At(value = "INVOKE", target = "Lsoftware/bernie/geckolib3/renderers/geo/GeoReplacedEntityRenderer;render(Lsoftware/bernie/geckolib3/geo/render/built/GeoModel;Ljava/lang/Object;FFFFF)V"),
			remap = false
	)
	private void arrowaccuracy_geckolibGeoReplacedEntityRenderer_doRender(GeoReplacedEntityRenderer<?> instance, GeoModel model, Object animatable, float partialTicks, float red, float green, float blue, float alpha, Operation<Void> original) {
		boolean capturing = GeckoLibCompat.beginModelRender(animatable);
		original.call(instance, model, animatable, partialTicks, red, green, blue, alpha);
		if(capturing) GeckoLibCompat.endModelRender();
	}
}