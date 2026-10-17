package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXBubbleAlt extends Particle {
   public int particle = 25;
   public double bubblespeed = 1.0E-4;
   float particleScale;
   float particleAlpha;

   public FXBubbleAlt(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ, int duration) {
      super(level, x, y, z, motionX, motionY, motionZ);
      this.rCol = 1.0F;
      this.gCol = 0.0F;
      this.bCol = 0.5F;
      this.setSize(0.02F, 0.02F);
      this.hasPhysics = false;
      this.particleScale = this.random.nextFloat() * 0.3F + 0.2F;
      this.xd = motionX * 0.2 + (Math.random() * 2.0 - 1.0) * 0.02F;
      this.yd = motionY * 0.2 + Math.random() * 0.02F;
      this.zd = motionZ * 0.2 + (Math.random() * 2.0 - 1.0) * 0.02F;
      this.lifetime = (int)(duration + 2 + 8.0 / (Math.random() * 0.8 + 0.2));
      this.age = 0;
      this.particleAlpha = 1.0F;
      ParticleUtils.checkParticleDistance(this, 50);
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.xd = this.xd + (this.random.nextFloat() - this.random.nextFloat()) * 0.001F;
      this.zd = this.zd + (this.random.nextFloat() - this.random.nextFloat()) * 0.001F;
      this.x = this.x + this.xd;
      this.y = this.y + this.yd;
      this.z = this.z + this.zd;
      this.xd *= 0.85;
      this.yd *= 0.85;
      this.zd *= 0.85;
      if (this.age++ >= this.lifetime) {
         this.remove();
      }

      if (this.age == this.lifetime - 2) {
         this.particle = 17;
      } else if (this.age == this.lifetime - 1) {
         this.particle = 18;
      }
   }

   public void setRGB(float red, float green, float blue) {
      this.rCol = red;
      this.gCol = green;
      this.bCol = blue;
   }

   public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {
      float u0 = this.particle % 16 / 16.0F;
      float u1 = u0 + 0.0624375F;
      float v0 = this.particle / 16 / 16.0F;
      float v1 = v0 + 0.0624375F;
      float scale = 0.2F * (this.particleScale * ((float)this.age / this.lifetime));
      float px = (float)(Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x);
      float py = (float)(Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y);
      float pz = (float)(Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z);
      Quaternionf rotation = camera.rotation();
      Vector3f[] vertices = new Vector3f[]{
         new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
      };

      for (Vector3f vertex : vertices) {
         vertex.rotate(rotation);
         vertex.mul(scale);
         vertex.add(px, py, pz);
      }

      int light = 15728880;
      buffer.addVertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
         .setUv(u1, v1)
         .setColor(this.rCol, this.gCol, this.bCol, this.particleAlpha)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
         .setUv(u1, v0)
         .setColor(this.rCol, this.gCol, this.bCol, this.particleAlpha)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
         .setUv(u0, v0)
         .setColor(this.rCol, this.gCol, this.bCol, this.particleAlpha)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
         .setUv(u0, v1)
         .setColor(this.rCol, this.gCol, this.bCol, this.particleAlpha)
         .setUv2(light & 65535, light >> 16);
   }

   @NotNull
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }
}
