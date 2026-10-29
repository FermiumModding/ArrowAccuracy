package arrowaccuracy.compat;

import arrowaccuracy.client.ModelCapture;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;

public abstract class AzureLibCompat {

	public static void onCubeRender(AzRendererPipelineContext<?,?> context, GeoCube cube) {
		if(!ModelCapture.isCapturingBones()) return;
		if(!(context.animatable() instanceof LivingEntity living) || !ModelCapture.isCapturing(living)) return;

		Matrix4f pose = new Matrix4f(context.poseStack().last().pose());
		if(!cube.transform().identity()) pose.mul(cube.transform().pose());

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