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

public class FXSparkleTrail extends Particle {
   private final Entity target;
   public int particle = 24;
   protected float particleScale;
   protected float gravity;

   public FXSparkleTrail(ClientLevel level, double x, double y, double z, Entity target, float red, float green, float blue) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.rCol = red;
      this.gCol = green;
      this.bCol = blue;
      this.particleScale = level.random.nextFloat() * 0.5F + 0.5F;
      this.target = target;
      double dx = target.getX() - this.x;
      double dy = target.getY() - this.y;
      double dz = target.getZ() - this.z;
      int dist = (int)(Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0);
      if (dist < 1) {
         dist = 1;
      }

      this.lifetime = dist / 2 + level.random.nextInt(dist);
      float spread = 0.1F;
      this.xd = (level.random.nextFloat() - level.random.nextFloat()) * spread;
      this.yd = (level.random.nextFloat() - level.random.nextFloat()) * spread;
      this.zd = (level.random.nextFloat() - level.random.nextFloat()) * spread;
      this.gravity = 0.2F;
      this.hasPhysics = true;
      ParticleUtils.checkParticleDistance(this, 64);
   }

   public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {
      float sizeFactor = Mth.sin(this.age / 3.0F) * 0.5F + 1.0F;
      int frame = this.particle + this.age % 16;
      float u0 = frame % 8 / 8.0F;
      float u1 = u0 + 0.124875F;
      float v0 = frame / 8 / 8.0F;
      float v1 = v0 + 0.124875F;
      float scale = 0.1F * this.particleScale * sizeFactor;
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
         .setColor(this.rCol, this.gCol, this.bCol, 1.0F)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
         .setUv(u1, v0)
         .setColor(this.rCol, this.gCol, this.bCol, 1.0F)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
         .setUv(u0, v0)
         .setColor(this.rCol, this.gCol, this.bCol, 1.0F)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
         .setUv(u0, v1)
         .setColor(this.rCol, this.gCol, this.bCol, 1.0F)
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
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else if (this.target != null && this.target.isAlive()) {
         double dx = this.target.getX() - this.x;
         double dy = this.target.getY() + this.target.getBbHeight() / 2.0 - this.y;
         double dz = this.target.getZ() - this.z;
         double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (dist < 0.3) {
            this.remove();
         } else {
            this.xd += dx / dist * 0.03;
            this.yd += dy / dist * 0.03;
            this.zd += dz / dist * 0.03;
            this.yd = this.yd - 0.04 * this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.96;
            this.yd *= 0.96;
            this.zd *= 0.96;
         }
      } else {
         this.remove();
      }
   }
}
