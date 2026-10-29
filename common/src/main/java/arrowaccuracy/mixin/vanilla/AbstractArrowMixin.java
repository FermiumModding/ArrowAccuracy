package arrowaccuracy.mixin.vanilla;

import arrowaccuracy.client.StuckArrow;
import arrowaccuracy.client.StuckArrowHandler;
import arrowaccuracy.util.IAbstractArrow;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Entity implements IAbstractArrow {

	@Shadow
	protected boolean inGround;

	@Unique
	private StuckArrow arrowaccuracy$pending = null;
	@Unique
	private boolean arrowaccuracy$hitSomething = false;

	public AbstractArrowMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	@Inject(
			method = "onHitEntity",
			at = @At("HEAD")
	)
	private void arrowaccuracy_vanillaAbstractArrow_onHitEntity(EntityHitResult result, CallbackInfo ci) {
		if(!this.level().isClientSide()) return;
		this.arrowaccuracy$hitSomething = true;
		LivingEntity target = StuckArrowHandler.getTarget(result.getEntity());
		if(target != null) {
			StuckArrowHandler.onClientHit((AbstractArrow)(Object)this, target);
		}
	}

	@Inject(
			method = "onHitBlock",
			at = @At("HEAD")
	)
	private void arrowaccuracy_vanillaAbstractArrow_onHitBlock(BlockHitResult result, CallbackInfo ci) {
		if(this.level().isClientSide()) this.arrowaccuracy$hitSomething = true;
	}

	@Inject(
			method = "lerpTo",
			at = @At("HEAD")
	)
	private void arrowaccuracy_vanillaAbstractArrow_lerpTo(double x, double y, double z, float yRot, float xRot, int steps, CallbackInfo ci) {
		StuckArrowHandler.onServerPositionUpdate((AbstractArrow)(Object)this);
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