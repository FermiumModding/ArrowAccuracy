package arrowaccuracy.compat;

import arrowaccuracy.client.ModelCapture;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.List;

public abstract class GeckoLibCompat {
	private static final FloatBuffer BUFFER = BufferUtils.createFloatBuffer(16);
	private static boolean capturing = false;

	public static boolean beginModelRender(Object animatable) {
		if(!(animatable instanceof EntityLivingBase) || !ModelCapture.isCapturing((EntityLivingBase)animatable)) return false;
		capturing = true;
		return true;
	}

	public static void endModelRender() {
		capturing = false;
	}

	public static void onCubeRender(GeoCube cube, javax.vecmath.Matrix4f model) {
		if(!capturing) return;

		BUFFER.clear();
		BUFFER.put(model.m00).put(model.m10).put(model.m20).put(model.m30);
		BUFFER.put(model.m01).put(model.m11).put(model.m21).put(model.m31);
		BUFFER.put(model.m02).put(model.m12).put(model.m22).put(model.m32);
		BUFFER.put(model.m03).put(model.m13).put(model.m23).put(model.m33);
		BUFFER.rewind();
		Matrix4f pose = new Matrix4f();
		pose.load(BUFFER);

		ModelCapture.onPoseRender(cube, pose, () -> getBounds(cube));
	}

	private static List<AxisAlignedBB> getBounds(GeoCube cube) {
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
		boolean any = false;
		for(GeoQuad quad : cube.quads) {
			if(quad == null || quad.vertices == null) continue;
			for(GeoVertex vertex : quad.vertices) {
				minX = Math.min(minX, vertex.position.x);
				minY = Math.min(minY, vertex.position.y);
				minZ = Math.min(minZ, vertex.position.z);
				maxX = Math.max(maxX, vertex.position.x);
				maxY = Math.max(maxY, vertex.position.y);
				maxZ = Math.max(maxZ, vertex.position.z);
				any = true;
			}
		}
		if(!any) return Collections.emptyList();
		return Collections.singletonList(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
	}
}