package arrowaccuracy.mixin.vanilla;

import arrowaccuracy.client.StuckArrowHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

	@Inject(
			method = "removeEntity",
			at = @At("HEAD")
	)
	private void arrowaccuracy_vanillaClientLevel_removeEntity(int entityId, Entity.RemovalReason reason, CallbackInfo ci) {
		if(((ClientLevel)(Object)this).getEntity(entityId) instanceof AbstractArrow arrow) {
			StuckArrowHandler.onArrowRemoved(arrow);
		}
	}
}