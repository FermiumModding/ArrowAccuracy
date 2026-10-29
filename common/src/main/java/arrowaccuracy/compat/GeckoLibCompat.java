package arrowaccuracy.compat;

import arrowaccuracy.client.ModelCapture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

import java.util.Collections;
import java.util.List;

public abstract class GeckoLibCompat {

	public static void onCubeRender(PoseStack poseStack, GeoCube cube) {
		if(!ModelCapture.isCapturingBones()) return;
		Vec3 pivot = cube.pivot();
		Matrix4f pose = new Matrix4f(poseStack.last().pose()).translate((float)(-pivot.x() / 16.0D), (float)(-pivot.y() / 16.0D), (float)(-pivot.z() / 16.0D));
		ModelCapture.onPoseRender(cube, pose, () -> getBounds(cube));
	}

	private static List<AABB> getBounds(GeoCube cube) {
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
		boolean any = false;
		for(GeoQuad quad : cube.quads()) {
			if(quad == null) continue;
			for(GeoVertex vertex : quad.vertices()) {
				Vector3f position = vertex.position();
				minX = Math.min(minX, position.x());
				minY = Math.min(minY, position.y());
				minZ = Math.min(minZ, position.z());
				maxX = Math.max(maxX, position.x());
				maxY = Math.max(maxY, position.y());
				maxZ = Math.max(maxZ, position.z());
				any = true;
			}
		}
		if(!any) return Collections.emptyList();
		return Collections.singletonList(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
	}
}