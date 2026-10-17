package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FXDrop extends Particle {
   int bobTimer;
   protected float gravity;

   public FXDrop(ClientLevel level, double x, double y, double z, float red, float green, float blue) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.rCol = red;
      this.gCol = green;
      this.bCol = blue;
      this.gravity = 0.06F;
      this.bobTimer = 40;
      this.lifetime = (int)(64.0 / (Math.random() * 0.8 + 0.2));
      this.hasPhysics = true;
      this.setSize(0.01F, 0.01F);
   }

   protected int getLightColor(float partialTick) {
      return 15728880;
   }

   public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {
      float u0 = 0.0F;
      float u1 = 0.125F;
      float v0 = 0.875F;
      float v1 = 1.0F;
      float scale = 0.1F;
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
         .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
         .setUv(u1, v0)
         .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
         .setUv(u0, v0)
         .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
         .setUv2(light & 65535, light >> 16);
      buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
         .setUv(u0, v1)
         .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
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
      this.yd = this.yd - this.gravity;
      if (this.bobTimer-- > 0) {
         this.xd *= 0.02;
         this.yd *= 0.02;
         this.zd *= 0.02;
      }

      this.move(this.xd, this.yd, this.zd);
      this.xd *= 0.98;
      this.yd *= 0.98;
      this.zd *= 0.98;
      if (this.lifetime-- <= 0) {
         this.remove();
      } else if (this.onGround) {
         this.remove();
      } else {
         BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
         FluidState fluidState = this.level.getFluidState(pos);
         if (!fluidState.isEmpty()) {
            double fluidHeight = fluidState.getHeight(this.level, pos) + pos.getY();
            if (fluidHeight >= this.y) {
               this.remove();
            }
         }
      }
   }
}
