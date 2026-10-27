package arrowaccuracy.mixin.vanilla;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.PositionTextureVertex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelBox.class)
public interface ModelBoxAccessor {

	@Accessor("vertexPositions")
	PositionTextureVertex[] arrowaccuracy$getVertexPositions();
}