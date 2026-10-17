package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FXBreaking extends TextureSheetParticle {
   private final int jitterX;
   private final int jitterY;

   public FXBreaking(ClientLevel level, double x, double y, double z, Item item) {
      this(level, x, y, z, new ItemStack(item));
   }

   public FXBreaking(ClientLevel level, double x, double y, double z, ItemStack stack) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.rCol = 1.0F;
      this.gCol = 1.0F;
      this.bCol = 1.0F;
      this.alpha = 1.0F;
      this.gravity = 1.0F;
      this.quadSize *= 0.5F;
      this.hasPhysics = true;
      this.setSize(0.02F, 0.02F);
      this.jitterX = this.random.nextInt(4);
      this.jitterY = this.random.nextInt(4);
      TextureAtlasSprite spr = Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, 0).getParticleIcon();
      this.setSprite(spr);
   }

   public FXBreaking(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, Item item) {
      this(level, x, y, z, item);
      this.xd *= 0.1F;
      this.yd *= 0.1F;
      this.zd *= 0.1F;
      this.xd += vx;
      this.yd += vy;
      this.zd += vz;
   }

   public void setParticleMaxAge(int particleMaxAge) {
      this.lifetime = particleMaxAge;
   }

   public void setAlpha(float alpha) {
      this.alpha = alpha;
   }

   @NotNull
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.TERRAIN_SHEET;
   }

   protected int getLightColor(float pt) {
      return super.getLightColor(pt);
   }

   public void render(@NotNull VertexConsumer consumer, @NotNull Camera camera, float pt) {
      if (this.sprite != null) {
         float fade = 1.0F - (this.age + pt) / this.lifetime;
         if (!(fade <= 0.0F)) {
            float size = 0.1F * this.quadSize * fade;
            Vec3 camPos = camera.getPosition();
            float px = (float)(Mth.lerp(pt, this.xo, this.x) - camPos.x);
            float py = (float)(Mth.lerp(pt, this.yo, this.y) - camPos.y);
            float pz = (float)(Mth.lerp(pt, this.zo, this.z) - camPos.z);
            float u0 = this.sprite.getU(this.jitterX * 4.0F);
            float u1 = this.sprite.getU((this.jitterX + 1) * 4.0F);
            float v0 = this.sprite.getV(this.jitterY * 4.0F);
            float v1 = this.sprite.getV((this.jitterY + 1) * 4.0F);
            int light = this.getLightColor(pt);
            Quaternionf q = camera.rotation();
            Vector3f c0 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
            Vector3f c1 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
            Vector3f c2 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
            Vector3f c3 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
            float a = this.alpha * fade;
            consumer.addVertex(c0.x, c0.y, c0.z).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
            consumer.addVertex(c1.x, c1.y, c1.z).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
            consumer.addVertex(c2.x, c2.y, c2.z).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
            consumer.addVertex(c3.x, c3.y, c3.z).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
         }
      }
   }
}
