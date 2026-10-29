package arrowaccuracy.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Optional;

public abstract class ArrowMath {

	private static final double T_MIN = -1.0D;
	private static final double T_MAX = 2.0D;

	public static Vec3 transform(Matrix4f m, Vec3 v, double w) {
		return new Vec3(
				m.m00() * v.x + m.m10() * v.y + m.m20() * v.z + m.m30() * w,
				m.m01() * v.x + m.m11() * v.y + m.m21() * v.z + m.m31() * w,
				m.m02() * v.x + m.m12() * v.y + m.m22() * v.z + m.m32() * w);
	}

	public static Vec3 rotateYaw(Vec3 v, float degrees) {
		return v.yRot((float)Math.toRadians(-degrees));
	}

	public static Vec3 clamp(Vec3 p, AABB box) {
		return new Vec3(
				Math.max(box.minX, Math.min(box.maxX, p.x)),
				Math.max(box.minY, Math.min(box.maxY, p.y)),
				Math.max(box.minZ, Math.min(box.maxZ, p.z)));
	}

	public static Impact impact(AABB box, Vec3 origin, Vec3 dir) {
		Vec3 start = origin.add(dir.scale(T_MIN));
		Vec3 end = origin.add(dir.scale(T_MAX));
		if(box.contains(start)) return new Impact(start, start, true, T_MIN);

		Optional<Vec3> result = box.clip(start, end);
		if(result.isPresent()) {
			Vec3 hitVec = result.get();
			double t = T_MIN + (T_MAX - T_MIN) * start.distanceTo(hitVec) / start.distanceTo(end);
			return new Impact(hitVec, hitVec, true, t);
		}

		Vec3 onRay = origin.add(dir.scale(closestApproach(box, origin, dir)));
		return new Impact(clamp(onRay, box), onRay, false, Double.NaN);
	}

	private static double closestApproach(AABB box, Vec3 origin, Vec3 dir) {
		double lo = T_MIN;
		double hi = T_MAX;
		for(int i = 0; i < 48; i++) {
			double m1 = lo + (hi - lo) / 3.0D;
			double m2 = hi - (hi - lo) / 3.0D;
			Vec3 p1 = origin.add(dir.scale(m1));
			Vec3 p2 = origin.add(dir.scale(m2));
			if(p1.distanceToSqr(clamp(p1, box)) < p2.distanceToSqr(clamp(p2, box))) hi = m2;
			else lo = m1;
		}
		return (lo + hi) * 0.5D;
	}

	public static class Impact {
		public final Vec3 point;
		public final Vec3 onRay;
		public final boolean hit;
		public final double t;

		private Impact(Vec3 point, Vec3 onRay, boolean hit, double t) {
			this.point = point;
			this.onRay = onRay;
			this.hit = hit;
			this.t = t;
		}
	}
}