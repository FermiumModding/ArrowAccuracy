package arrowaccuracy.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor;

public class AlphaVertexConsumer implements VertexConsumer {
	private final VertexConsumer delegate;
	private final float alpha;

	public AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
		this.delegate = delegate;
		this.alpha = alpha;
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		this.delegate.addVertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		this.delegate.setColor(red, green, blue, (int)(alpha * this.alpha));
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		this.delegate.setUv(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		this.delegate.setUv1(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		this.delegate.setUv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		this.delegate.setNormal(x, y, z);
		return this;
	}

	@Override
	public void addVertex(float x, float y, float z, int color, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
		this.delegate.addVertex(x, y, z, FastColor.ARGB32.color((int)(FastColor.ARGB32.alpha(color) * this.alpha), color), texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
	}
}
