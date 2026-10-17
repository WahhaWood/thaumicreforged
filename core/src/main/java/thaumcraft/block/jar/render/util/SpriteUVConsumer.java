package thaumcraft.block.jar.render.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public final class SpriteUVConsumer implements VertexConsumer {
   private final VertexConsumer delegate;
   private final TextureAtlasSprite sprite;

   @OnlyIn(Dist.CLIENT)
   public SpriteUVConsumer(VertexConsumer delegate, TextureAtlasSprite sprite) {
      this.delegate = delegate;
      this.sprite = sprite;
   }

   @OnlyIn(Dist.CLIENT)
   public VertexConsumer addVertex(float x, float y, float z) {
      return this.delegate.addVertex(x, y, z);
   }

   @OnlyIn(Dist.CLIENT)
   public VertexConsumer setColor(int red, int green, int blue, int alpha) {
      return this.delegate.setColor(red, green, blue, alpha);
   }

   @OnlyIn(Dist.CLIENT)
   public VertexConsumer setUv(float u, float v) {
      float atlasU = this.sprite.getU0() + (this.sprite.getU1() - this.sprite.getU0()) * u;
      float atlasV = this.sprite.getV0() + (this.sprite.getV1() - this.sprite.getV0()) * v;
      return this.delegate.setUv(atlasU, atlasV);
   }

   @OnlyIn(Dist.CLIENT)
   public VertexConsumer setUv1(int u, int v) {
      return this.delegate.setUv1(u, v);
   }

   @OnlyIn(Dist.CLIENT)
   public VertexConsumer setUv2(int u, int v) {
      return this.delegate.setUv2(u, v);
   }

   @OnlyIn(Dist.CLIENT)
   public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
      return this.delegate.setNormal(normalX, normalY, normalZ);
   }
}
