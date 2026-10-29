package arrowaccuracy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class ModelCapture {
	public static final float PIXEL_SCALE = 0.0625F;

	private static final Map<Object,CapturedPart> CAPTURED = new IdentityHashMap<>();
	private static final Matrix4f VIEW_INVERSE = new Matrix4f();
	private static LivingEntity entity = null;
	private static boolean livingRenderer = false;
	private static int modelDepth = 0;

	public static void begin(LivingEntity target, boolean isLivingRenderer, Matrix4f view) {
		CAPTURED.clear();
		entity = null;
		modelDepth = 0;
		if(view.determinant() == 0.0F) return;
		view.invert(VIEW_INVERSE);
		livingRenderer = isLivingRenderer;
		entity = target;
	}

	public static void beginModel(LivingEntity target) {
		if(entity == target) modelDepth++;
	}

	public static void endModel(LivingEntity target) {
		if(entity == target && modelDepth > 0) modelDepth--;
	}

	public static boolean isCapturingModel() {
		return entity != null && modelDepth > 0;
	}

	public static boolean isCapturingBones() {
		return entity != null && (modelDepth > 0 || !livingRenderer);
	}

	public static boolean isCapturing(LivingEntity target) {
		return entity == target;
	}

	public static boolean isActive() {
		return entity != null;
	}

	public static void onModelPartRender(ModelPart part, List<ModelPart.Cube> cubes, PoseStack poseStack) {
		poseStack.pushPose();
		part.translateAndRotate(poseStack);
		onPoseRender(part, poseStack.last().pose(), () -> getBounds(cubes));
		poseStack.popPose();
	}

	public static void onPoseRender(Object part, Matrix4f pose, Supplier<List<AABB>> bounds) {
		if(entity == null) return;
		CAPTURED.put(part, new CapturedPart(new Matrix4f(VIEW_INVERSE).mul(pose), bounds));
	}

	public static Map<Object,CapturedPart> end() {
		entity = null;
		modelDepth = 0;
		return CAPTURED;
	}

	private static List<AABB> getBounds(List<ModelPart.Cube> cubes) {
		List<AABB> bounds = new ArrayList<>(cubes.size());
		for(ModelPart.Cube cube : cubes) {
			bounds.add(new AABB(cube.minX * PIXEL_SCALE, cube.minY * PIXEL_SCALE, cube.minZ * PIXEL_SCALE, cube.maxX * PIXEL_SCALE, cube.maxY * PIXEL_SCALE, cube.maxZ * PIXEL_SCALE));
		}
		return bounds;
	}

	public static class CapturedPart {
		public final Matrix4f toWorld;
		private final Supplier<List<AABB>> boundsSupplier;
		private List<AABB> bounds = null;
		private Matrix4f fromWorld = null;

		private CapturedPart(Matrix4f toWorld, Supplier<List<AABB>> boundsSupplier) {
			this.toWorld = toWorld;
			this.boundsSupplier = boundsSupplier;
		}

		public List<AABB> getBounds() {
			if(this.bounds == null) this.bounds = this.boundsSupplier.get();
			return this.bounds;
		}

		@Nullable
		public Matrix4f getFromWorld() {
			if(this.fromWorld == null && this.toWorld.determinant() != 0.0F) this.fromWorld = this.toWorld.invert(new Matrix4f());
			return this.fromWorld;
		}
	}
}