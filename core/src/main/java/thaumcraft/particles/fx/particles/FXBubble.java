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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXBubble extends Particle {
   public int particle = 16;
   public double bubblespeed = 0.002;
   private final float quadSize;
   public boolean noClip = true;
   private static final float TILE = 0.0625F;
   private static final float TILE_INNER = 0.0624375F;

   public FXBubble(ClientLevel level, double x, double y, double z, double mx, double my, double mz, int lifeParam) {
      super(level, x, y, z);
      this.particle = 16;
      this.bubblespeed = 0.002;
      this.rCol = 1.0F;
      this.gCol = 0.0F;
      this.bCol = 0.5F;
      this.alpha = 1.0F;
      this.setSize(0.02F, 0.02F);
      this.noClip = true;
      this.hasPhysics = false;
      this.quadSize = 1.0F;
      this.scale(this.random.nextFloat() * 0.3F + 0.2F);
      this.xd = mx * 0.2F + (Math.random() * 2.0 - 1.0) * 0.02;
      this.yd = my * 0.2F + Math.random() * 0.02;
      this.zd = mz * 0.2F + (Math.random() * 2.0 - 1.0) * 0.02;
      this.lifetime = (int)(lifeParam + 2 + 8.0 / (Math.random() * 0.8 + 0.2));
      Entity cam = Minecraft.getInstance().getCameraEntity();
      int range = Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FAST ? 25 : 50;
      if (cam != null && cam.distanceToSqr(x, y, z) > range * range) {
         this.lifetime = 0;
      }

      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
   }

   public void setFroth() {
      this.scale(0.75F);
      this.lifetime = 4 + this.random.nextInt(3);
      this.bubblespeed = -0.001;
      this.xd /= 5.0;
      this.yd /= 10.0;
      this.zd /= 5.0;
   }

   public void setFroth2() {
      this.scale(0.75F);
      this.lifetime = 12 + this.random.nextInt(12);
      this.bubblespeed = -0.005;
      this.xd /= 5.0;
      this.yd /= 10.0;
      this.zd /= 5.0;
   }

   public void setRGB(float r, float g, float b) {
      this.rCol = r;
      this.gCol = g;
      this.bCol = b;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.hasPhysics = !this.noClip;
      this.yd = this.yd + this.bubblespeed;
      if (this.bubblespeed > 0.0) {
         this.xd = this.xd + (this.random.nextFloat() - this.random.nextFloat()) * 0.01;
         this.zd = this.zd + (this.random.nextFloat() - this.random.nextFloat()) * 0.01;
      }

      this.move(this.xd, this.yd, this.zd);
      this.xd *= 0.85F;
      this.yd *= 0.85F;
      this.zd *= 0.85F;
      if (this.lifetime-- <= 0) {
         this.remove();
      } else {
         if (this.lifetime <= 2) {
            this.particle++;
         }

         this.age++;
      }
   }

   public void render(VertexConsumer ignored, Camera camera, float partialTicks) {
      if (!this.removed) {
         float u0 = this.particle % 16 * 0.0625F;
         float v0 = this.particle / 16 * 0.0625F;
         float u1 = u0 + 0.0624375F;
         float v1 = v0 + 0.0624375F;
         float size = 0.1F * this.quadSize;
         Vec3 camPos = camera.getPosition();
         float px = (float)(this.xo + (this.x - this.xo) * partialTicks - camPos.x);
         float py = (float)(this.yo + (this.y - this.yo) * partialTicks - camPos.y);
         float pz = (float)(this.zo + (this.z - this.zo) * partialTicks - camPos.z);
         Quaternionf q = new Quaternionf(camera.rotation());
         Vector3f c1 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(size);
         Vector3f c2 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(size);
         Vector3f c3 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(size);
         Vector3f c4 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(size);
         int light = 15728880;
         float a = this.alpha;
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
