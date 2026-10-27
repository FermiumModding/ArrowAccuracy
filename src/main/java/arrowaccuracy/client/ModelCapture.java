package arrowaccuracy.client;

import arrowaccuracy.mixin.vanilla.ModelBoxAccessor;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public abstract class ModelCapture {
	private static final FloatBuffer BUFFER = BufferUtils.createFloatBuffer(16);
	private static final Map<Object,CapturedPart> CAPTURED = new IdentityHashMap<>();
	private static final Set<ModelRenderer> PARTS = Collections.newSetFromMap(new IdentityHashMap<>());
	private static final Matrix4f VIEW_INVERSE = new Matrix4f();
	private static EntityLivingBase entity = null;

	public static void begin(EntityLivingBase target, @Nullable ModelBase model) {
		CAPTURED.clear();
		PARTS.clear();
		entity = null;
		if(Matrix4f.invert(readModelView(new Matrix4f()), VIEW_INVERSE) == null) return;
		if(model != null) PARTS.addAll(model.boxList);
		entity = target;
	}

	public static void onPartRender(ModelRenderer part, float scale) {
		if(entity == null || !PARTS.contains(part)) return;
		CAPTURED.put(part, new CapturedPart(toWorld(null), () -> getBounds(part, scale)));
	}

	public static void onPoseRender(Object part, Matrix4f pose, Supplier<List<AxisAlignedBB>> bounds) {
		if(entity == null) return;
		CAPTURED.put(part, new CapturedPart(toWorld(pose), bounds));
	}

	public static boolean isCapturing(EntityLivingBase target) {
		return entity == target;
	}

	public static Map<Object,CapturedPart> end() {
		entity = null;
		PARTS.clear();
		return CAPTURED;
	}

	private static Matrix4f toWorld(@Nullable Matrix4f pose) {
		Matrix4f toWorld = readModelView(new Matrix4f());
		if(pose != null) Matrix4f.mul(toWorld, pose, toWorld);
		return Matrix4f.mul(VIEW_INVERSE, toWorld, toWorld);
	}

	private static Matrix4f readModelView(Matrix4f dest) {
		BUFFER.clear();
		GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, BUFFER);
		BUFFER.rewind();
		dest.load(BUFFER);
		return dest;
	}

	private static List<AxisAlignedBB> getBounds(ModelRenderer part, float scale) {
		List<AxisAlignedBB> bounds = new ArrayList<>(part.cubeList.size());
		for(ModelBox box : part.cubeList) bounds.add(getBounds(box, scale));
		return bounds;
	}

	private static AxisAlignedBB getBounds(ModelBox box, float scale) {
		PositionTextureVertex[] vertices = ((ModelBoxAccessor)box).arrowaccuracy$getVertexPositions();
		if(vertices == null || vertices.length == 0) {
			return new AxisAlignedBB(box.posX1 * scale, box.posY1 * scale, box.posZ1 * scale, box.posX2 * scale, box.posY2 * scale, box.posZ2 * scale);
		}
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
		for(PositionTextureVertex vertex : vertices) {
			minX = Math.min(minX, vertex.vector3D.x);
			minY = Math.min(minY, vertex.vector3D.y);
			minZ = Math.min(minZ, vertex.vector3D.z);
			maxX = Math.max(maxX, vertex.vector3D.x);
			maxY = Math.max(maxY, vertex.vector3D.y);
			maxZ = Math.max(maxZ, vertex.vector3D.z);
		}
		return new AxisAlignedBB(minX * scale, minY * scale, minZ * scale, maxX * scale, maxY * scale, maxZ * scale);
	}

	public static class CapturedPart {
		public final Matrix4f toWorld;
		private final Supplier<List<AxisAlignedBB>> boundsSupplier;
		private List<AxisAlignedBB> bounds = null;
		private Matrix4f fromWorld = null;

		private CapturedPart(Matrix4f toWorld, Supplier<List<AxisAlignedBB>> boundsSupplier) {
			this.toWorld = toWorld;
			this.boundsSupplier = boundsSupplier;
		}

		public List<AxisAlignedBB> getBounds() {
			if(this.bounds == null) this.bounds = this.boundsSupplier.get();
			return this.bounds;
		}

		@Nullable
		public Matrix4f getFromWorld() {
			if(this.fromWorld == null) this.fromWorld = Matrix4f.invert(this.toWorld, new Matrix4f());
			return this.fromWorld;
		}
	}
}