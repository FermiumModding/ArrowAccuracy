package arrowaccuracy.compat;

import arrowaccuracy.client.ModelCapture;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.TabulaModelRenderUtils;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public abstract class CitadelCompat {

	public static void onBoxRender(AdvancedModelBox box, Matrix4f pose) {
		if(box.cubeList.isEmpty()) return;
		ModelCapture.onPoseRender(box, pose, () -> getBounds(box));
	}

	private static List<AABB> getBounds(AdvancedModelBox box) {
		float scale = ModelCapture.PIXEL_SCALE;
		List<AABB> bounds = new ArrayList<>(box.cubeList.size());
		for(TabulaModelRenderUtils.ModelBox cube : box.cubeList) {
			bounds.add(new AABB(cube.posX1 * scale, cube.posY1 * scale, cube.posZ1 * scale, cube.posX2 * scale, cube.posY2 * scale, cube.posZ2 * scale));
		}
		return bounds;
	}
}