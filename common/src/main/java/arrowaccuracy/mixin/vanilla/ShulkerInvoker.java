package arrowaccuracy.mixin.vanilla;

import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Shulker.class)
public interface ShulkerInvoker {

	@Invoker("isClosed")
	boolean arrowaccuracy$invokeIsClosed();
}