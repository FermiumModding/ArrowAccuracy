package arrowaccuracy.platform;

import arrowaccuracy.platform.services.IPlatformHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class FabricPlatformHelper implements IPlatformHelper {

	@Override
	public LivingEntity getMultipartParent(Entity entity) {
		return null;
	}
}