package arrowaccuracy.mixin.llibrary;

import arrowaccuracy.util.IPartEntity;
import net.ilexiconn.llibrary.server.entity.multipart.PartEntity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PartEntity.class)
public abstract class PartEntityMixin implements IPartEntity {

	@Shadow(remap = false)
	protected EntityLiving parent;

	@Unique
	@Override
	public EntityLivingBase arrowaccuracy$getParent() {
		return this.parent;
	}
}