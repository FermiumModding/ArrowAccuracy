package arrowaccuracy.mixin.vanilla;

import arrowaccuracy.config.ConfigHandler;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArrowLayer.class)
public abstract class ArrowLayerMixin {

	@Inject(
			method = "numStuck",
			at = @At("HEAD"),
			cancellable = true
	)
	private void arrowaccuracy_vanillaArrowLayer_numStuck(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
		if(ConfigHandler.CONFIG.hideVanillaArrows) cir.setReturnValue(0);
	}
}