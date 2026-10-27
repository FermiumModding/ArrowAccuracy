package arrowaccuracy.mixin.vanilla;

import net.minecraft.client.renderer.entity.Render;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Render.class)
public interface RenderAccessor {

	@Accessor("renderOutlines")
	boolean arrowaccuracy$getRenderOutlines();
}