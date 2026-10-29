package arrowaccuracy.platform.services;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public interface IPlatformHelper {

	@Nullable
	LivingEntity getMultipartParent(Entity entity);
}