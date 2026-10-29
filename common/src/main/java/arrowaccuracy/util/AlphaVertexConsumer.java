package arrowaccuracy.util;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class AlphaVertexConsumer implements VertexConsumer {
	private final VertexConsumer delegate;
	private final float alpha;

	public AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
		this.delegate = delegate;
		this.alpha = alpha;
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		this.delegate.vertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		this.delegate.color(red, green, blue, (int)(alpha * this.alpha));
		return this;
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		this.delegate.uv(u, v);
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		this.delegate.overlayCoords(u, v);
		return this;
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		this.delegate.uv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		this.delegate.normal(x, y, z);
		return this;
	}

	@Override
	public void endVertex() {
		this.delegate.endVertex();
	}

	@Override
	public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
		this.delegate.vertex(x, y, z, red, green, blue, alpha * this.alpha, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
	}

	@Override
	public void defaultColor(int defaultR, int defaultG, int defaultB, int defaultA) {
		this.delegate.defaultColor(defaultR, defaultG, defaultB, (int)(defaultA * this.alpha));
	}

	@Override
	public void unsetDefaultColor() {
		this.delegate.unsetDefaultColor();
	}
}