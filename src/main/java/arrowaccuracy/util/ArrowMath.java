package arrowaccuracy.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Matrix4f;

public abstract class ArrowMath {

	private static final double T_MIN = -1.0D;
	private static final double T_MAX = 2.0D;

	public static Vec3d transform(Matrix4f m, Vec3d v, double w) {
		return new Vec3d(
				m.m00 * v.x + m.m10 * v.y + m.m20 * v.z + m.m30 * w,
				m.m01 * v.x + m.m11 * v.y + m.m21 * v.z + m.m31 * w,
				m.m02 * v.x + m.m12 * v.y + m.m22 * v.z + m.m32 * w);
	}

	public static Vec3d rotateYaw(Vec3d v, float degrees) {
		return v.rotateYaw((float)Math.toRadians(-degrees));
	}

	public static Vec3d clamp(Vec3d p, AxisAlignedBB box) {
		return new Vec3d(
				Math.max(box.minX, Math.min(box.maxX, p.x)),
				Math.max(box.minY, Math.min(box.maxY, p.y)),
				Math.max(box.minZ, Math.min(box.maxZ, p.z)));
	}

	public static Impact impact(AxisAlignedBB box, Vec3d origin, Vec3d dir) {
		Vec3d start = origin.add(dir.scale(T_MIN));
		Vec3d end = origin.add(dir.scale(T_MAX));
		if(box.contains(start)) return new Impact(start, start, true, T_MIN);

		RayTraceResult result = box.calculateIntercept(start, end);
		if(result != null) {
			double t = T_MIN + (T_MAX - T_MIN) * start.distanceTo(result.hitVec) / start.distanceTo(end);
			return new Impact(result.hitVec, result.hitVec, true, t);
		}

		Vec3d onRay = origin.add(dir.scale(closestApproach(box, origin, dir)));
		return new Impact(clamp(onRay, box), onRay, false, Double.NaN);
	}

	private static double closestApproach(AxisAlignedBB box, Vec3d origin, Vec3d dir) {
		double lo = T_MIN;
		double hi = T_MAX;
		for(int i = 0; i < 48; i++) {
			double m1 = lo + (hi - lo) / 3.0D;
			double m2 = hi - (hi - lo) / 3.0D;
			Vec3d p1 = origin.add(dir.scale(m1));
			Vec3d p2 = origin.add(dir.scale(m2));
			if(p1.squareDistanceTo(clamp(p1, box)) < p2.squareDistanceTo(clamp(p2, box))) hi = m2;
			else lo = m1;
		}
		return (lo + hi) * 0.5D;
	}

	public static class Impact {
		public final Vec3d point;
		public final Vec3d onRay;
		public final boolean hit;
		public final double t;

		private Impact(Vec3d point, Vec3d onRay, boolean hit, double t) {
			this.point = point;
			this.onRay = onRay;
			this.hit = hit;
			this.t = t;
		}
	}
}