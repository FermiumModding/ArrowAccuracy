package arrowaccuracy.mixin.vanilla;

import arrowaccuracy.client.StuckArrowHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderManager.class)
public abstract class RenderManagerMixin {

	@WrapOperation(
			method = "renderEntity",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V")
	)
	private void arrowaccuracy_vanillaRenderManager_renderEntity(Render<Entity> instance, Entity entity, double x, double y, double z, float entityYaw, float partialTicks, Operation<Void> original) {
		boolean capturing = StuckArrowHandler.beginRender(entity, instance);
		original.call(instance, entity, x, y, z, entityYaw, partialTicks);
		if(capturing) StuckArrowHandler.endRender((EntityLivingBase)entity, x, y, z, partialTicks);
	}
}