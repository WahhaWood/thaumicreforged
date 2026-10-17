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

public class FXWispArcing extends Particle {
   private final double startX;
   private final double startY;
   private final double startZ;
   float moteParticleScale;
   int moteHalfLife;
   public boolean tinkle = false;
   public int blendmode = 1;

   public FXWispArcing(ClientLevel level, double x, double y, double z, float scale, float red, float green, float blue) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      if (red == 0.0F) {
         red = 1.0F;
      }

      this.x = x;
      this.startX = x;
      this.y = y;
      this.startY = y;
      this.z = z;
      this.startZ = z;
      this.rCol = red;
      this.gCol = green;
      this.bCol = blue;
      this.gravity = 0.0F;
      this.moteParticleScale = scale;
      this.lifetime = (int)(36.0 / (Math.random() * 0.3 + 0.7));
      this.moteHalfLife = this.lifetime / 2;
      this.hasPhysics = true;
      this.setSize(0.01F, 0.01F);
      ParticleUtils.checkParticleDistance(this, 50);
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
   }

   public FXWispArcing(
      ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, float scale, float red, float green, float blue
   ) {
      this(level, x, y, z, scale, red, green, blue);
      this.xd = targetX - x;
      this.yd = targetY - y;
      this.zd = targetZ - z;
   }

   public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {
      float lifeFraction = (float)this.age / this.moteHalfLife;
      if (lifeFraction > 1.0F) {
         lifeFraction = 2.0F - lifeFraction;
      }

      float currentScale = this.moteParticleScale * lifeFraction;
      float scale = 0.5F * currentScale;
      float px = (float)(Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x);
      float py = (float)(Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y);
      float pz = (float)(Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z);
      float u0 = 0.0F;
      float u1 = 0.0F;
      float v0 = 0.875F;
      float v1 = 1.0F;
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
         .setUv(u1, v0)
         .setColor(this.rCol, this.gCol, this.bCol, 0.5F)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
         .setUv(u1, v1)
         .setColor(this.rCol, this.gCol, this.bCol, 0.5F)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
         .setUv(u0, v0)
         .setColor(this.rCol, this.gCol, this.bCol, 0.5F)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
         .setUv(u0, v1)
         .setColor(this.rCol, this.gCol, this.bCol, 0.5F)
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
      float progress = (float)this.age / this.lifetime;
      float halfProgress = this.age / (this.lifetime / 2.0F);
      float remaining = 1.0F - progress;
      float arcFactor = 1.0F - halfProgress;
      float arcSquared = arcFactor * arcFactor;
      this.x = this.startX + this.xd * remaining;
      this.y = this.startY + this.yd * remaining - arcSquared + 1.0;
      this.z = this.startZ + this.zd * remaining;
      if (this.age++ >= this.lifetime) {
         this.remove();
      }
   }

   public void setGravity(float gravity) {
      this.gravity = gravity;
   }
}
