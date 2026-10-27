package arrowaccuracy.mixin.vanilla;

import arrowaccuracy.client.StuckArrow;
import arrowaccuracy.client.StuckArrowHandler;
import arrowaccuracy.util.IEntityArrow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityArrow.class)
public abstract class EntityArrowMixin extends Entity implements IEntityArrow {

	@Shadow
	protected boolean inGround;

	@Unique
	private StuckArrow arrowaccuracy$pending = null;
	@Unique
	private boolean arrowaccuracy$hitSomething = false;

	public EntityArrowMixin(World world) {
		super(world);
	}

	@Inject(
			method = "onHit",
			at = @At("HEAD")
	)
	private void arrowaccuracy_vanillaEntityArrow_onHit(RayTraceResult result, CallbackInfo ci) {
		if(!this.world.isRemote) return;
		this.arrowaccuracy$hitSomething = true;
		if(result.entityHit instanceof EntityLivingBase) {
			StuckArrowHandler.onClientHit((EntityArrow)(Object)this, (EntityLivingBase)result.entityHit);
		}
	}

	@Inject(
			method = "setPositionAndRotationDirect",
			at = @At("HEAD")
	)
	private void arrowaccuracy_vanillaEntityArrow_setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport, CallbackInfo ci) {
		StuckArrowHandler.onServerPositionUpdate((EntityArrow)(Object)this);
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		if(this.isDead || this.arrowaccuracy$pending != null) return false;
		return super.shouldRenderInPass(pass);
	}

	@Unique
	@Override
	public StuckArrow arrowaccuracy$getPending() {
		return this.arrowaccuracy$pending;
	}

	@Unique
	@Override
	public void arrowaccuracy$setPending(StuckArrow pending) {
		this.arrowaccuracy$pending = pending;
	}

	@Unique
	@Override
	public boolean arrowaccuracy$hasHitSomething() {
		return this.arrowaccuracy$hitSomething;
	}

	@Unique
	@Override
	public boolean arrowaccuracy$isInGround() {
		return this.inGround;
	}
}