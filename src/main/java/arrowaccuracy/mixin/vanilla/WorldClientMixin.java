package arrowaccuracy.mixin.vanilla;

import arrowaccuracy.client.StuckArrowHandler;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldClient.class)
public abstract class WorldClientMixin {

	@Inject(
			method = "removeEntityFromWorld",
			at = @At("RETURN")
	)
	private void arrowaccuracy_vanillaWorldClient_removeEntityFromWorld(int entityID, CallbackInfoReturnable<Entity> cir) {
		Entity entity = cir.getReturnValue();
		if(entity instanceof EntityArrow) {
			StuckArrowHandler.onArrowRemoved((EntityArrow)entity);
		}
	}
}