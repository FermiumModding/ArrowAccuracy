package arrowaccuracy.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public abstract class ModelCapture {
	private static final FloatBuffer BUFFER = BufferUtils.createFloatBuffer(16);
	private static final Map<ModelRenderer,CapturedPart> CAPTURED = new IdentityHashMap<>();
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
		Matrix4f toWorld = readModelView(new Matrix4f());
		Matrix4f.mul(VIEW_INVERSE, toWorld, toWorld);
		CAPTURED.put(part, new CapturedPart(toWorld, scale));
	}

	public static boolean isCapturing(EntityLivingBase target) {
		return entity == target;
	}

	public static Map<ModelRenderer,CapturedPart> end() {
		entity = null;
		PARTS.clear();
		return CAPTURED;
	}

	private static Matrix4f readModelView(Matrix4f dest) {
		BUFFER.clear();
		GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, BUFFER);
		BUFFER.rewind();
		dest.load(BUFFER);
		return dest;
	}

	public static class CapturedPart {
		public final Matrix4f toWorld;
		public final float scale;
		private Matrix4f fromWorld = null;

		private CapturedPart(Matrix4f toWorld, float scale) {
			this.toWorld = toWorld;
			this.scale = scale;
		}

		@Nullable
		public Matrix4f getFromWorld() {
			if(this.fromWorld == null) this.fromWorld = Matrix4f.invert(this.toWorld, new Matrix4f());
			return this.fromWorld;
		}
	}
}