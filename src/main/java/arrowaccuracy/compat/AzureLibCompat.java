package arrowaccuracy.compat;

import arrowaccuracy.client.ModelCapture;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.math.Vector3f;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.List;

public abstract class AzureLibCompat {
	private static final FloatBuffer BUFFER = BufferUtils.createFloatBuffer(16);
	private static final mod.azure.azurelib.util.math.Matrix4f POSE = new mod.azure.azurelib.util.math.Matrix4f();

	public static void onCubeRender(AzRendererPipelineContext<?,?> context, GeoCube cube) {
		Object animatable = context.animatable();
		if(!(animatable instanceof EntityLivingBase) || !ModelCapture.isCapturing((EntityLivingBase)animatable)) return;

		POSE.load(context.poseStack().last().pose());
		if(!cube.transform().identity()) POSE.multiply(cube.transform().pose());
		BUFFER.clear();
		POSE.store(BUFFER);
		BUFFER.rewind();
		Matrix4f pose = new Matrix4f();
		pose.load(BUFFER);

		ModelCapture.onPoseRender(cube, pose, () -> getBounds(cube));
	}

	private static List<AxisAlignedBB> getBounds(GeoCube cube) {
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
		return Collections.singletonList(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
	}
}