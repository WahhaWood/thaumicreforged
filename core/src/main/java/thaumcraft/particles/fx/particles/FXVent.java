package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.awt.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXVent extends Particle {
   private float psm = 1.0F;
   private float quadSize;

   public FXVent(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ, int color) {
      super(level, x, y, z);
      this.setSize(0.02F, 0.02F);
      this.quadSize = this.random.nextFloat() * 0.1F + 0.05F;
      this.xd = motionX;
      this.yd = motionY;
      this.zd = motionZ;
      this.hasPhysics = false;
      Color c = new Color(color);
      this.rCol = c.getRed() / 255.0F;
      this.gCol = c.getGreen() / 255.0F;
      this.bCol = c.getBlue() / 255.0F;
      this.setHeading(this.xd, this.yd, this.zd, 0.125F, 5.0F);
      Entity cam = Minecraft.getInstance().getCameraEntity();
      int range = 50;
      if (Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FAST) {
         range = 25;
      }

      if (cam != null && cam.distanceToSqr(x, y, z) > range * range) {
         this.lifetime = 0;
      } else {
         this.lifetime = 1000000;
      }

      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.alpha = 1.0F;
   }

   public void setScale(float s) {
      this.quadSize *= s;
      this.psm *= s;
   }

   public void setAlpha(float alpha) {
      this.alpha = alpha;
   }

   public void setHeading(double motionX, double motionY, double motionZ, float speed, float jitterMul) {
      float len = (float)Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
      if (len < 1.0E-6F) {
         this.xd = 0.0;
         this.yd = 0.0;
         this.zd = 0.0;
      } else {
         motionX /= len;
         motionY /= len;
         motionZ /= len;
         double j = 0.0075F * jitterMul;
         motionX += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * j;
         motionY += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * j;
         motionZ += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * j;
         motionX *= speed;
         motionY *= speed;
         motionZ *= speed;
         this.xd = motionX;
         this.yd = motionY;
         this.zd = motionZ;
      }
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.age++;
      if (this.quadSize > this.psm) {
         this.remove();
      } else {
         this.yd += 0.0025;
         this.x = this.x + this.xd;
         this.y = this.y + this.yd;
         this.z = this.z + this.zd;
         this.xd *= 0.8500000190734863;
         this.yd *= 0.8500000190734863;
         this.zd *= 0.8500000190734863;
         if (this.quadSize < this.psm) {
            this.quadSize *= 1.15F;
         }
      }
   }

   public void setRGB(float r, float g, float b) {
      this.rCol = r;
      this.gCol = g;
      this.bCol = b;
   }

   public void render(VertexConsumer ignored, Camera camera, float partialTicks) {
      if (!this.removed) {
         float baseAlpha = 0.33F * this.alpha;
         float fade = (this.psm - this.quadSize) / this.psm;
         if (fade < 0.0F) {
            fade = 0.0F;
         }

         float a = baseAlpha * fade;
         int frame = (int)(1.0F + this.quadSize / this.psm * 4.0F);
         int idx = frame & 0xFF;
         float u0 = idx % 16 / 16.0F;
         float u1 = u0 + 0.0624375F;
         float v0 = idx / 16 / 16.0F;
         float v1 = v0 + 0.0624375F;
         float s = 0.3F * this.quadSize;
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
