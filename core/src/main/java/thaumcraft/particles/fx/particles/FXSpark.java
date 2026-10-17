package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FXSpark extends TextureSheetParticle {
   private final int particleBase;
   private final boolean flip;

   public FXSpark(ClientLevel level, double x, double y, double z, float particleScale) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.rCol = 1.0F;
      this.gCol = 1.0F;
      this.bCol = 1.0F;
      this.alpha = 1.0F;
      this.gravity = 0.0F;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.quadSize = particleScale;
      this.lifetime = 5 + this.random.nextInt(5);
      this.hasPhysics = false;
      this.setSize(0.01F, 0.01F);
      this.particleBase = this.random.nextInt(3) * 8;
      this.flip = this.random.nextBoolean();
   }

   @NotNull
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void render(@NotNull VertexConsumer consumer, @NotNull Camera camera, float pt) {
      if (this.sprite != null) {
         int frame = this.particleBase + (int)((this.age + pt) / this.lifetime * 7.0F);
         float u0 = frame % 8 / 8.0F;
         float u1 = u0 + 0.125F;
         float v0 = frame / 8 / 8.0F;
         float v1 = v0 + 0.125F;
         if (this.flip) {
            float t = u0;
            u0 = u1;
            u1 = t;
         }

         float size = this.quadSize;
         Vec3 camPos = camera.getPosition();
         float px = (float)(Mth.lerp(pt, this.xo, this.x) - camPos.x);
         float py = (float)(Mth.lerp(pt, this.yo, this.y) - camPos.y);
         float pz = (float)(Mth.lerp(pt, this.zo, this.z) - camPos.z);
         int light = this.getLightColor(pt);
         Quaternionf q = camera.rotation();
         Vector3f c0 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
         Vector3f c1 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
         Vector3f c2 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
         Vector3f c3 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
         consumer.addVertex(c0.x, c0.y, c0.z).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(light & 65535, light >> 16);
         consumer.addVertex(c1.x, c1.y, c1.z).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(light & 65535, light >> 16);
         consumer.addVertex(c2.x, c2.y, c2.z).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(light & 65535, light >> 16);
         consumer.addVertex(c3.x, c3.y, c3.z).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(light & 65535, light >> 16);
      }
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (++this.age >= this.lifetime) {
         this.remove();
      }
   }
}
