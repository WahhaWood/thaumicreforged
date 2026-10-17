package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXGeneric extends Particle {
   private static final float STEP = 0.0624375F;
   private boolean loop = false;
   private float quadSize;
   private int delay = 0;
   private int startParticle = 0;
   private int numParticles = 1;
   private int particleInc = 1;
   private int particleIndex = 0;

   public FXGeneric(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ) {
      super(level, x, y, z);
      this.setSize(0.1F, 0.1F);
      this.hasPhysics = false;
      this.xd = motionX;
      this.yd = motionY;
      this.zd = motionZ;
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.quadSize = 1.0F;
      this.alpha = 1.0F;
      this.rCol = 1.0F;
      this.gCol = 1.0F;
      this.bCol = 1.0F;
      this.lifetime = 20;
      this.particleIndex = this.startParticle;
   }

   public void setLoop(boolean loop) {
      this.loop = loop;
   }

   public void setScale(float particleScale) {
      this.quadSize = particleScale;
   }

   public void setAlpha(float alpha) {
      this.alpha = alpha;
   }

   public void setMaxAge(int particleMaxAge, int delay) {
      this.delay = Math.max(0, delay);
      this.lifetime = Math.max(1, particleMaxAge + this.delay);
   }

   public void setParticles(int start, int numParticles, int particleInc) {
      this.startParticle = start;
      this.numParticles = Math.max(1, numParticles);
      this.particleInc = Math.max(1, particleInc);
      this.particleIndex = start;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void tick() {
      super.tick();
      if (!this.removed) {
         if (this.loop) {
            int frame = this.age / this.particleInc % this.numParticles;
            this.particleIndex = this.startParticle + frame;
         } else {
            float t = (float)this.age / this.lifetime;
            float f = Math.min(this.numParticles * t, (float)(this.numParticles - 1));
            this.particleIndex = this.startParticle + (int)f;
         }
      }
   }

   public void render(VertexConsumer ignored, Camera camera, float partialTicks) {
      if (!this.removed) {
         if (this.age >= this.delay) {
            float a = this.alpha;
            if (this.age <= 1 || this.age >= this.lifetime - 1) {
               a *= 0.5F;
            }

            int idx = this.particleIndex & 0xFF;
            float u0 = idx % 16 / 16.0F;
            float u1 = u0 + 0.0624375F;
            float v0 = idx / 16 / 16.0F;
            float v1 = v0 + 0.0624375F;
            float s = 0.1F * this.quadSize;
            Vec3 camPos = camera.getPosition();
            float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camPos.x);
            float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camPos.y);
            float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camPos.z);
            Quaternionf q = new Quaternionf(camera.rotation());
            Vector3f c1 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(s);
            Vector3f c2 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(s);
            Vector3f c3 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(s);
            Vector3f c4 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(s);
            int light = this.getLightColor(partialTicks);
            RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
            RenderSystem.setShaderTexture(0, ParticleUtils.PARTICLE_TEXTURE);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(770, 771);
            Tesselator t = Tesselator.getInstance();
            BufferBuilder buf = t.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);
            buf.addVertex(px + c1.x(), py + c1.y(), pz + c1.z()).setUv(u1, v1).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
            buf.addVertex(px + c2.x(), py + c2.y(), pz + c2.z()).setUv(u1, v0).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
            buf.addVertex(px + c3.x(), py + c3.y(), pz + c3.z()).setUv(u0, v0).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
            buf.addVertex(px + c4.x(), py + c4.y(), pz + c4.z()).setUv(u0, v1).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
            BufferUploader.drawWithShader(buf.buildOrThrow());
            RenderSystem.blendFunc(770, 771);
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
         }
      }
   }
}
