package thaumcraft.particles.fx.beams;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public final class FXBeamBoreImpact extends Particle {
   private static final ParticleRenderType IMPACT_TYPE = new ParticleRenderType() {
      public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
         RenderSystem.setShader(GameRenderer::getParticleShader);
         RenderSystem.setShaderTexture(0, ParticleUtils.PARTICLE_TEXTURE);
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.depthMask(false);
         return tesselator.begin(Mode.QUADS, DefaultVertexFormat.PARTICLE);
      }

      @Override
      public String toString() {
         return "THAUMCRAFT_BEAM_BORE_IMPACT";
      }
   };
   private final FXBeamBore beam;
   private int impact = 5;

   public FXBeamBoreImpact(ClientLevel level, FXBeamBore beam) {
      super(level, beam.tX, beam.tY, beam.tZ);
      this.beam = beam;
      this.impact = Math.max(1, beam.impact + 1);
      this.lifetime = this.impact + 1;
      this.hasPhysics = false;
      this.alpha = 1.0F;
      this.rCol = beam.getRed();
      this.gCol = beam.getGreen();
      this.bCol = beam.getBlue();
      this.setSize(0.02F, 0.02F);
   }

   public ParticleRenderType getRenderType() {
      return IMPACT_TYPE;
   }

   public int getLightColor(float pt) {
      return 15728880;
   }

   public void tick() {
      if (!this.beam.isAlive()) {
         this.remove();
      } else {
         this.xo = this.x;
         this.yo = this.y;
         this.zo = this.z;
         this.setPos(this.beam.tX, this.beam.tY, this.beam.tZ);
         if (this.impact > 0) {
            this.impact--;
         }

         if (++this.age >= this.lifetime || this.impact <= 0) {
            this.remove();
         }
      }
   }

   public void render(VertexConsumer consumer, Camera camera, float pt) {
      if (this.beam.isAlive()) {
         Vec3 cam = camera.getPosition();
         float x = (float)(Mth.lerp(pt, this.xo, this.x) - cam.x);
         float y = (float)(Mth.lerp(pt, this.yo, this.y) - cam.y);
         float z = (float)(Mth.lerp(pt, this.zo, this.z) - cam.z);
         int denom = Math.max(1, 6 - Math.max(0, this.impact));
         float s = this.beam.endMod / 2.0F / denom;
         float u0 = this.age % 16 / 16.0F;
         float u1 = u0 + 0.0624375F;
         float v0 = 0.3125F;
         float v1 = v0 + 0.0624375F;
         Quaternionf q = new Quaternionf(camera.rotation());
         Vector3f a = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(s);
         Vector3f b = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(s);
         Vector3f c = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(s);
         Vector3f d = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(s);
         int light = 15728880;
         consumer.addVertex(x + a.x(), y + a.y(), z + a.z()).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, 0.66F).setUv2(light & 65535, light >> 16);
         consumer.addVertex(x + b.x(), y + b.y(), z + b.z()).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, 0.66F).setUv2(light & 65535, light >> 16);
         consumer.addVertex(x + c.x(), y + c.y(), z + c.z()).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, 0.66F).setUv2(light & 65535, light >> 16);
         consumer.addVertex(x + d.x(), y + d.y(), z + d.z()).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, 0.66F).setUv2(light & 65535, light >> 16);
      }
   }
}
