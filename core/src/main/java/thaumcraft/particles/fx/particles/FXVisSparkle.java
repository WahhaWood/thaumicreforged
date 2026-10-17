package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXVisSparkle extends Particle {
   private final double targetX;
   private final double targetY;
   private final double targetZ;
   private float quadSize;
   private float sizeMod;

   public FXVisSparkle(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ) {
      super(level, x, y, z);
      this.targetX = targetX;
      this.targetY = targetY;
      this.targetZ = targetZ;
      this.rCol = 0.2F;
      this.gCol = 0.6F + this.random.nextFloat() * 0.3F;
      this.bCol = 0.2F;
      this.quadSize = 0.0F;
      this.lifetime = 1000;
      float s = 0.01F;
      this.xd = this.random.nextGaussian() * s;
      this.yd = this.random.nextGaussian() * s;
      this.zd = this.random.nextGaussian() * s;
      this.sizeMod = 45.0F + this.random.nextInt(15);
      this.hasPhysics = false;
      Entity cam = Minecraft.getInstance().getCameraEntity();
      int range = 64;
      if (Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FAST) {
         range = 32;
      }

      if (cam != null && cam.distanceToSqr(x, y, z) > range * range) {
         this.lifetime = 0;
      }
   }

   public void setGravity(float g) {
      this.gravity = g;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.move(this.xd, this.yd, this.zd);
         this.xd *= 0.985;
         this.yd *= 0.985;
         this.zd *= 0.985;
         double dx = this.targetX - this.x;
         double dy = this.targetY - this.y;
         double dz = this.targetZ - this.z;
         double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (dist < 2.0) {
            this.scale(0.95F);
         }

         if (dist < 0.2) {
            this.lifetime = this.age;
         }

         if (this.age < 10) {
            this.quadSize = this.age / this.sizeMod;
         }

         if (dist > 1.0E-6) {
            double pull = 0.1F;
            dx /= dist;
            dy /= dist;
            dz /= dist;
            this.xd += dx * pull;
            this.yd += dy * pull;
            this.zd += dz * pull;
         }

         this.xd = Mth.clamp(this.xd, -0.1, 0.1);
         this.yd = Mth.clamp(this.yd, -0.1, 0.1);
         this.zd = Mth.clamp(this.zd, -0.1, 0.1);
      }
   }

   public void render(VertexConsumer ignored, Camera camera, float partialTicks) {
      if (!this.removed) {
         float wobble = Mth.sin(this.age / 3.0F) * 0.3F + 6.0F;
         float half = 0.1F * this.quadSize * wobble;
         if (!(half <= 1.0E-5F)) {
            float u0 = this.age % 16 / 16.0F;
            float u1 = u0 + 0.0624375F;
            float v0 = 0.5F;
            float v1 = v0 + 0.0624375F;
            Vec3 camPos = camera.getPosition();
            float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camPos.x);
            float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camPos.y);
            float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camPos.z);
            Quaternionf q = new Quaternionf(camera.rotation());
            Vector3f c1 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(half);
            Vector3f c2 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(half);
            Vector3f c3 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(half);
            Vector3f c4 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(half);
            int light = 15728880;
            float a = 0.5F;
            ResourceLocation tex = ParticleUtils.PARTICLE_TEXTURE;
            RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
            RenderSystem.setShaderTexture(0, tex);
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
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
         }
      }
   }
}
