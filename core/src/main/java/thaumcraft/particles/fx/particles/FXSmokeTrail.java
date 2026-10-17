package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXSmokeTrail extends Particle {
   private final Entity target;
   public int particle = 24;
   float particleScale;

   public FXSmokeTrail(ClientLevel level, double x, double y, double z, Entity target, float red, float green, float blue) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.rCol = red;
      this.gCol = green;
      this.bCol = blue;
      this.particleScale = this.random.nextFloat() * 0.5F + 0.5F;
      this.target = target;
      double dx = target.getX() - this.x;
      double dy = target.getY() - this.y;
      double dz = target.getZ() - this.z;
      int dist = (int)(Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0);
      if (dist < 1) {
         dist = 1;
      }

      this.lifetime = dist / 2 + this.random.nextInt(dist);
      float spread = 0.1F;
      this.xd = (this.random.nextFloat() - this.random.nextFloat()) * spread;
      this.yd = (this.random.nextFloat() - this.random.nextFloat()) * spread;
      this.zd = (this.random.nextFloat() - this.random.nextFloat()) * spread;
      this.gravity = 0.2F;
      this.hasPhysics = true;
      ParticleUtils.checkParticleDistance(this, 64);
   }

   public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {
      float pulsate = Mth.sin(this.age / 3.0F) * 0.33F + 0.66F;
      int frame = (int)(1.0F + (float)this.age / this.lifetime * 4.0F);
      float u0 = frame % 16 / 16.0F;
      float u1 = u0 + 0.0624375F;
      float v0 = frame / 16 / 16.0F;
      float v1 = v0 + 0.0624375F;
      float scale = 0.3F * this.particleScale * pulsate;
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

      int light = this.getLightColor(partialTick);
      buffer.addVertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
         .setUv(u1, v1)
         .setColor(this.rCol, this.gCol, this.bCol, 0.33F)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
         .setUv(u1, v0)
         .setColor(this.rCol, this.gCol, this.bCol, 0.33F)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
         .setUv(u0, v0)
         .setColor(this.rCol, this.gCol, this.bCol, 0.33F)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
         .setUv(u0, v1)
         .setColor(this.rCol, this.gCol, this.bCol, 0.33F)
         .setUv2(light & 65535, light >> 16);
   }

   @NotNull
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.target != null && this.target.isAlive()) {
         double distSq = this.distanceToSqr(this.target);
         if (this.age++ < this.lifetime && !(distSq < 1.0)) {
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.985;
            this.yd *= 0.985;
            this.zd *= 0.985;
            double dx = this.target.getX() - this.x;
            double dy = this.target.getY() - this.y;
            double dz = this.target.getZ() - this.z;
            double accel = 0.3;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 4.0) {
               this.particleScale *= 0.9F;
               accel = 0.6;
            }

            double nx = dx / dist;
            double ny = dy / dist;
            double nz = dz / dist;
            this.xd += nx * accel;
            this.yd += ny * accel;
            this.zd += nz * accel;
            this.xd = Mth.clamp(this.xd, -0.35, 0.35);
            this.yd = Mth.clamp(this.yd, -0.35, 0.35);
            this.zd = Mth.clamp(this.zd, -0.35, 0.35);
         } else {
            this.remove();
         }
      } else {
         this.remove();
      }
   }

   private double distanceToSqr(Entity entity) {
      double dx = this.x - entity.getX();
      double dy = this.y - entity.getY();
      double dz = this.z - entity.getZ();
      return dx * dx + dy * dy + dz * dz;
   }

   public void setGravity(float gravity) {
      this.gravity = gravity;
   }
}
