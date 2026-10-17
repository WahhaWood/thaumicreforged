package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import thaumcraft.particles.fx.ParticleUtils;

public class FXBlockRunes extends Particle {
   private final double ofx;
   private final double ofy;
   private final float rotation;
   private final int runeIndex;
   private final float scaleRand;
   public boolean noClip = true;

   public FXBlockRunes(ClientLevel level, double x, double y, double z, float r, float g, float b, int mult) {
      super(level, x, y, z);
      if (r == 0.0F) {
         r = 1.0F;
      }

      this.rCol = r;
      this.gCol = g;
      this.bCol = b;
      this.rotation = level.random.nextInt(4) * 90.0F;
      this.gravity = 0.0F;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.lifetime = 3 * Math.max(1, mult);
      this.setSize(0.01F, 0.01F);
      this.noClip = true;
      this.hasPhysics = false;
      this.runeIndex = 224 + this.random.nextInt(16);
      this.ofx = this.random.nextFloat() * 0.2;
      this.ofy = -0.3 + this.random.nextFloat() * 0.6;
      this.scaleRand = (float)(1.0 + this.random.nextGaussian() * 0.1);
      this.alpha = 0.0F;
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      float fadeIn = this.lifetime / 5.0F;
      if (this.age <= fadeIn) {
         this.alpha = this.age / fadeIn;
      } else {
         this.alpha = (float)(this.lifetime - this.age) / this.lifetime;
      }

      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.yd = this.yd - 0.04 * this.gravity;
         this.x = this.x + this.xd;
         this.y = this.y + this.yd;
         this.z = this.z + this.zd;
      }
   }

   public void setGravity(float g) {
      this.gravity = g;
   }

   public void render(VertexConsumer ignored, Camera camera, float partialTicks) {
      if (!this.removed) {
         Vec3 camPos = camera.getPosition();
         float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camPos.x);
         float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camPos.y);
         float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camPos.z);
         float u0 = this.runeIndex % 16 / 16.0F;
         float u1 = u0 + 0.0624375F;
         float v0 = 0.375F;
         float v1 = v0 + 0.0624375F;
         float s = 0.3F * this.scaleRand;
         float a = this.alpha * 0.5F;
         int light = 15728880;
         ResourceLocation tex = ParticleUtils.PARTICLE_TEXTURE;
         RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
         RenderSystem.setShaderTexture(0, tex);
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.depthMask(false);
         RenderSystem.defaultBlendFunc();
         Matrix4fStack mv = RenderSystem.getModelViewStack();
         mv.pushMatrix();
         mv.translate(px, py, pz);
         mv.rotate(Axis.YP.rotationDegrees(this.rotation));
         mv.rotate(Axis.ZP.rotationDegrees(90.0F));
         mv.translate((float)this.ofx, (float)this.ofy, -0.51F);
         RenderSystem.applyModelViewMatrix();
         Matrix4f mat = new Matrix4f(mv);
         Tesselator t = Tesselator.getInstance();
         BufferBuilder buf = t.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);
         float hx = 0.5F * s;
         buf.addVertex(mat, -hx, hx, 0.0F).setUv(u1, v1).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
         buf.addVertex(mat, hx, hx, 0.0F).setUv(u1, v0).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
         buf.addVertex(mat, hx, -hx, 0.0F).setUv(u0, v0).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
         buf.addVertex(mat, -hx, -hx, 0.0F).setUv(u0, v1).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
         BufferUploader.drawWithShader(buf.buildOrThrow());
         mv.popMatrix();
         RenderSystem.applyModelViewMatrix();
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
      }
   }
}
