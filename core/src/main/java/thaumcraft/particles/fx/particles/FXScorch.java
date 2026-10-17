package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FXScorch extends Particle {
   public boolean pvp = true;
   public boolean mobs = true;
   public boolean animals = true;
   private double px;
   private double py;
   private double pz;
   private float transferParticleScale;
   Entity partDestEnt;
   public boolean lance = false;
   protected float particleScale;

   public FXScorch(ClientLevel level, double posX, double posY, double posZ, Vec3 vec3, float spread, boolean lance) {
      super(level, posX, posY, posZ, 0.0, 0.0, 0.0);
      this.x = posX;
      this.y = posY;
      this.z = posZ;
      this.lance = lance;
      this.px = posX + vec3.x * 100.0;
      this.py = posY + vec3.y * 100.0;
      this.pz = posZ + vec3.z * 100.0;
      if (!lance) {
         this.px = this.px + (level.random.nextFloat() - level.random.nextFloat()) * spread;
         this.py = this.py + (level.random.nextFloat() - level.random.nextFloat()) * spread;
         this.pz = this.pz + (level.random.nextFloat() - level.random.nextFloat()) * spread;
      } else {
         this.px = this.px + (level.random.nextFloat() - level.random.nextFloat()) * 0.5;
         this.py = this.py + (level.random.nextFloat() - level.random.nextFloat()) * 0.5;
         this.pz = this.pz + (level.random.nextFloat() - level.random.nextFloat()) * 0.5;
      }

      float scale = level.random.nextFloat() * 0.5F + 2.0F;
      this.particleScale = scale;
      this.transferParticleScale = scale;
      if (!lance) {
         float scale2 = level.random.nextFloat() + 3.0F;
         this.particleScale = scale2;
         this.transferParticleScale = scale2;
      }

      this.lifetime = 50;
      this.setSize(0.1F, 0.1F);
      this.hasPhysics = true;
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.alpha = 0.66F;
   }

   public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {
      float ageRatio = (this.age + partialTick) / this.lifetime;
      float scale = this.particleScale * (1.0F - ageRatio * 0.5F);
      float u0 = 0.875F;
      float u1 = 1.0F;
      float v0 = 0.875F;
      float v1 = 1.0F;
      float px = (float)(Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x);
      float py = (float)(Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y);
      float pz = (float)(Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z);
      Quaternionf rotation = camera.rotation();
      Vector3f[] vertices = new Vector3f[]{
         new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
      };

      for (Vector3f vertex : vertices) {
         vertex.rotate(rotation);
         vertex.mul(scale * 0.1F);
         vertex.add(px, py, pz);
      }

      float alphaFade = this.alpha * (1.0F - ageRatio);
      int light = this.getLightColor(partialTick);
      buffer.addVertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
         .setUv(u1, v1)
         .setColor(1.0F, 0.5F + ageRatio * 0.5F, ageRatio, alphaFade)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
         .setUv(u1, v0)
         .setColor(1.0F, 0.5F + ageRatio * 0.5F, ageRatio, alphaFade)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
         .setUv(u0, v0)
         .setColor(1.0F, 0.5F + ageRatio * 0.5F, ageRatio, alphaFade)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
         .setUv(u0, v1)
         .setColor(1.0F, 0.5F + ageRatio * 0.5F, ageRatio, alphaFade)
         .setUv2(light & 65535, light >> 16);
   }

   @NotNull
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   protected int getLightColor(float partialTick) {
      return 15728880;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         double dx = this.px - this.x;
         double dy = this.py - this.y;
         double dz = this.pz - this.z;
         double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (dist > 0.1) {
            double speed = this.lance ? 1.5 : 1.0;
            this.xd = dx / dist * speed;
            this.yd = dy / dist * speed;
            this.zd = dz / dist * speed;
         }

         this.move(this.xd, this.yd, this.zd);
      }
   }
}
