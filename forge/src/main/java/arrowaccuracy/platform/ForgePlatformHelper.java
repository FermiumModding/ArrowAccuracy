package arrowaccuracy.platform;

import arrowaccuracy.platform.services.IPlatformHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.entity.PartEntity;

public class ForgePlatformHelper implements IPlatformHelper {

	@Override
	public LivingEntity getMultipartParent(Entity entity) {
		if(entity instanceof PartEntity<?> part && part.getParent() instanceof LivingEntity living) return living;
		return null;
	}
}