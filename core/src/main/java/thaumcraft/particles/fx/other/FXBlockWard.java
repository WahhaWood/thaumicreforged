package thaumcraft.particles.fx.other;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class FXBlockWard extends Particle {
   private final Direction side;
   private final int rotation;
   private float sx;
   private float sy;
   private float sz;
   private float particleScale;
   private float particleGravity;

   public FXBlockWard(ClientLevel level, double x, double y, double z, Direction side, float n4, float n5, float n6) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.side = side;
      this.particleGravity = 0.0F;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.lifetime = 12 + this.random.nextInt(5);
      this.hasPhysics = false;
      this.setSize(0.01F, 0.01F);
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.particleScale = (float)(1.4 + this.random.nextGaussian() * 0.3F);
      this.rotation = this.random.nextInt(360);
      this.sx = Mth.clamp(n4 - 0.6F + this.random.nextFloat() * 0.2F, -0.4F, 0.4F);
      this.sy = Mth.clamp(n5 - 0.6F + this.random.nextFloat() * 0.2F, -0.4F, 0.4F);
      this.sz = Mth.clamp(n6 - 0.6F + this.random.nextFloat() * 0.2F, -0.4F, 0.4F);
      if (side.getStepX() != 0) {
         this.sx = 0.0F;
      }

      if (side.getStepY() != 0) {
         this.sy = 0.0F;
      }

      if (side.getStepZ() != 0) {
         this.sz = 0.0F;
      }
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void render(VertexConsumer ignored, Camera camera, float partialTicks) {
      PoseStack pose = new PoseStack();
      double ix = Mth.lerp(partialTicks, this.xo, this.x);
      double iy = Mth.lerp(partialTicks, this.yo, this.y);
      double iz = Mth.lerp(partialTicks, this.zo, this.z);
      Vec3 camPos = camera.getPosition();
      pose.translate(ix - camPos.x + this.sx, iy - camPos.y + this.sy, iz - camPos.z + this.sz);
      float ax = this.side.getStepY();
      float ay = -this.side.getStepX();
      float az = this.side.getStepZ();
      pose.mulPose(new Quaternionf().rotateAxis((float)Math.toRadians(90.0), ax, ay, az));
      pose.mulPose(Axis.ZP.rotationDegrees(this.rotation));
      if (this.side.getStepZ() > 0) {
         pose.translate(0.0, 0.0, 0.505F);
         pose.mulPose(Axis.YN.rotationDegrees(180.0F));
      } else {
         pose.translate(0.0, 0.0, -0.505F);
      }

      float a = this.alpha / 2.0F;
      UtilsFX.bindTexture("textures/model/hemis" + Math.min(15, (int)(15.0F * ((this.age + partialTicks) / this.lifetime))) + ".png");
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, a);
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder buffer = tesselator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      Matrix4f mat = pose.last().pose();
      float s = this.particleScale;
      int cr = Mth.clamp((int)(this.rCol * 255.0F), 0, 255);
      int cg = Mth.clamp((int)(this.gCol * 255.0F), 0, 255);
      int cb = Mth.clamp((int)(this.bCol * 255.0F), 0, 255);
      int ca = Mth.clamp((int)(a * 255.0F), 0, 255);
      buffer.addVertex(mat, -0.5F * s, 0.5F * s, 0.0F).setUv(0.0F, 1.0F).setColor(cr, cg, cb, ca);
      buffer.addVertex(mat, 0.5F * s, 0.5F * s, 0.0F).setUv(1.0F, 1.0F).setColor(cr, cg, cb, ca);
      buffer.addVertex(mat, 0.5F * s, -0.5F * s, 0.0F).setUv(1.0F, 0.0F).setColor(cr, cg, cb, ca);
      buffer.addVertex(mat, -0.5F * s, -0.5F * s, 0.0F).setUv(0.0F, 0.0F).setColor(cr, cg, cb, ca);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      float n = this.lifetime / 5.0F;
      if (this.age <= n) {
         this.alpha = this.age / n;
      } else {
         this.alpha = (float)(this.lifetime - this.age) / this.lifetime;
      }

      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.yd = this.yd - 0.04 * this.particleGravity;
         this.x = this.x + this.xd;
         this.y = this.y + this.yd;
         this.z = this.z + this.zd;
      }
   }

   public void setGravity(float particleGravity) {
      this.particleGravity = particleGravity;
   }
}
