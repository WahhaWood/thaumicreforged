package thaumcraft.particles.fx.bolt;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.particles.fx.WRVector3;

@OnlyIn(Dist.CLIENT)
public class FXLightningBolt extends Particle {
   public static final ResourceLocation TEX_LARGE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/p_large.png");
   public static final ResourceLocation TEX_SMALL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/p_small.png");
   private int type = 0;
   private float width = 0.03F;
   private final FXLightningBoltCommon main;

   public FXLightningBolt(ClientLevel level, WRVector3 start, WRVector3 end, long seed) {
      super(level, 0.0, 0.0, 0.0);
      this.main = new FXLightningBoltCommon(level, start, end, seed);
      this.setupFromMain();
   }

   public FXLightningBolt(ClientLevel level, Entity a, Entity b, long seed) {
      super(level, 0.0, 0.0, 0.0);
      this.main = new FXLightningBoltCommon(level, a, b, seed);
      this.setupFromMain();
   }

   public FXLightningBolt(ClientLevel level, Entity a, Entity b, long seed, int ticksPerSegment) {
      super(level, 0.0, 0.0, 0.0);
      this.main = new FXLightningBoltCommon(level, a, b, seed, ticksPerSegment);
      this.setupFromMain();
   }

   public FXLightningBolt(ClientLevel level, BlockEntity be, Entity e, long seed) {
      super(level, 0.0, 0.0, 0.0);
      this.main = new FXLightningBoltCommon(level, be, e, seed);
      this.setupFromMain();
   }

   public FXLightningBolt(ClientLevel level, double sx, double sy, double sz, double ex, double ey, double ez, long seed, int ticksPerSegment, float multiplier) {
      super(level, 0.0, 0.0, 0.0);
      this.main = new FXLightningBoltCommon(level, sx, sy, sz, ex, ey, ez, seed, ticksPerSegment, multiplier);
      this.setupFromMain();
   }

   public FXLightningBolt(
      ClientLevel level, double sx, double sy, double sz, double ex, double ey, double ez, long seed, int ticksPerSegment, float multiplier, int type
   ) {
      super(level, 0.0, 0.0, 0.0);
      this.main = new FXLightningBoltCommon(level, sx, sy, sz, ex, ey, ez, seed, ticksPerSegment, multiplier, type);
      this.setupFromMain();
   }

   public FXLightningBolt(ClientLevel level, double sx, double sy, double sz, double ex, double ey, double ez, long seed, int ticksPerSegment) {
      super(level, 0.0, 0.0, 0.0);
      this.main = new FXLightningBoltCommon(level, sx, sy, sz, ex, ey, ez, seed, ticksPerSegment, 1.0F);
      this.setupFromMain();
   }

   public FXLightningBolt(ClientLevel level, BlockEntity be, double ex, double ey, double ez, long seed) {
      super(level, 0.0, 0.0, 0.0);
      this.main = new FXLightningBoltCommon(level, be, ex, ey, ez, seed);
      this.setupFromMain();
   }

   private void setupFromMain() {
      this.lifetime = Math.max(1, this.main.particleMaxAge);
      this.setPos(this.main.start.x, this.main.start.y, this.main.start.z);
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.gravity = 0.0F;
      this.hasPhysics = false;
      this.main.type = this.type;
   }

   public void defaultFractal() {
      this.main.defaultFractal();
   }

   public void fractal(int splits, float splitFrac, float splitAngle, float splitLength, float splitWidth) {
      this.main.fractal(splits, splitFrac, splitAngle, splitLength, splitWidth);
   }

   public void finalizeBolt() {
      this.main.finalizeBolt();
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         mc.particleEngine.add(this);
      }
   }

   public void setType(int type) {
      this.type = type;
      this.main.type = type;
   }

   public void setMultiplier(float multiplier) {
      this.main.multiplier = multiplier;
   }

   public void setWidth(float width) {
      this.width = width;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void tick() {
      this.main.onUpdate();
      this.setPos(this.main.start.x, this.main.start.y, this.main.start.z);
      if (this.main.particleAge >= this.main.particleMaxAge) {
         this.remove();
      }
   }

   private static WRVector3 getRelativeViewVector(WRVector3 point) {
      Entity cam = Minecraft.getInstance().getCameraEntity();
      return cam == null ? new WRVector3(0.0, 0.0, 1.0) : new WRVector3((float)cam.getX() - point.x, (float)cam.getY() - point.y, (float)cam.getZ() - point.z);
   }

   private void applyTypePass0BlendAndColor() {
      switch (this.type) {
         case 1:
            this.rCol = 0.6F;
            this.gCol = 0.6F;
            this.bCol = 0.1F;
            RenderSystem.blendFunc(770, 1);
            break;
         case 2:
            this.rCol = 0.1F;
            this.gCol = 0.1F;
            this.bCol = 0.6F;
            RenderSystem.blendFunc(770, 1);
            break;
         case 3:
            this.rCol = 0.1F;
            this.gCol = 1.0F;
            this.bCol = 0.1F;
            RenderSystem.blendFunc(770, 1);
            break;
         case 4:
            this.rCol = 0.6F;
            this.gCol = 0.1F;
            this.bCol = 0.1F;
            RenderSystem.blendFunc(770, 1);
            break;
         case 5:
            this.rCol = 0.6F;
            this.gCol = 0.2F;
            this.bCol = 0.6F;
            RenderSystem.blendFunc(770, 771);
            break;
         case 6:
            this.rCol = 0.75F;
            this.gCol = 1.0F;
            this.bCol = 1.0F;
            RenderSystem.blendFunc(770, 771);
            break;
         default:
            this.rCol = 0.6F;
            this.gCol = 0.3F;
            this.bCol = 0.6F;
            RenderSystem.blendFunc(770, 1);
      }
   }

   private void applyTypePass1BlendAndColor() {
      switch (this.type) {
         case 1:
            this.rCol = 1.0F;
            this.gCol = 1.0F;
            this.bCol = 0.1F;
            break;
         case 2:
            this.rCol = 0.1F;
            this.gCol = 0.1F;
            this.bCol = 1.0F;
            break;
         case 3:
            this.rCol = 0.1F;
            this.gCol = 0.6F;
            this.bCol = 0.1F;
            break;
         case 4:
            this.rCol = 1.0F;
            this.gCol = 0.1F;
            this.bCol = 0.1F;
            break;
         case 5:
            this.rCol = 0.0F;
            this.gCol = 0.0F;
            this.bCol = 0.0F;
            RenderSystem.blendFunc(770, 771);
            return;
         case 6:
            this.rCol = 0.75F;
            this.gCol = 1.0F;
            this.bCol = 1.0F;
            RenderSystem.blendFunc(770, 771);
            return;
         default:
            this.rCol = 1.0F;
            this.gCol = 0.6F;
            this.bCol = 1.0F;
      }

      if (this.type >= 0 && this.type <= 4) {
         RenderSystem.blendFunc(770, 1);
      }
   }

   private static void v(BufferBuilder buf, float x, float y, float z, float u, float v, int light, float r, float g, float b, float a) {
      buf.addVertex(x, y, z).setUv(u, v).setUv2(light & 65535, light >> 16).setColor(r, g, b, a);
   }

   private void renderBolt(BufferBuilder buf, Vec3 camPos, float partialTicks, int pass, float baseAlpha) {
      float progress = this.main.particleAge >= 0 && this.main.particleMaxAge > 0 ? (float)this.main.particleAge / this.main.particleMaxAge : 0.0F;
      float alpha;
      if (pass == 0) {
         alpha = (1.0F - progress) * 0.4F;
      } else {
         alpha = 1.0F - progress * 0.5F;
      }

      alpha *= baseAlpha;
      int light = 15728880;
      int denom = Math.max(1, (int)(this.main.length * 3.0F));
      int n9 = (int)((this.main.particleAge + partialTicks + denom) / denom * this.main.numsegments0);

      for (FXLightningBoltCommon.Segment seg : this.main.segments) {
         if (seg.segmentno <= n9) {
            float dist = getRelativeViewVector(seg.startpoint.point).length();
            float n10 = this.width * (dist / 5.0F + 1.0F) * (1.0F + seg.light) * 0.5F;
            WRVector3 view = getRelativeViewVector(seg.startpoint.point);
            float vlen = view.length();
            if (vlen < 1.0E-6F) {
               vlen = 1.0E-6F;
            }

            view = view.scale(1.0F / vlen);
            float sinPrev = Math.abs(seg.sinprev) < 1.0E-4F ? 1.0E-4F : seg.sinprev;
            float sinNext = Math.abs(seg.sinnext) < 1.0E-4F ? 1.0E-4F : seg.sinnext;
            WRVector3 offA = WRVector3.crossProduct(view, seg.prevdiff).scale(n10 / sinPrev);
            WRVector3 offB = WRVector3.crossProduct(view, seg.nextdiff).scale(n10 / sinNext);
            WRVector3 p0 = seg.startpoint.point;
            WRVector3 p1 = seg.endpoint.point;
            float x0 = (float)(p0.x - camPos.x);
            float y0 = (float)(p0.y - camPos.y);
            float z0 = (float)(p0.z - camPos.z);
            float x1 = (float)(p1.x - camPos.x);
            float y1 = (float)(p1.y - camPos.y);
            float z1 = (float)(p1.z - camPos.z);
            float a = alpha * seg.light;
            v(buf, x1 - offB.x, y1 - offB.y, z1 - offB.z, 0.5F, 0.0F, light, this.rCol, this.gCol, this.bCol, a);
            v(buf, x0 - offA.x, y0 - offA.y, z0 - offA.z, 0.5F, 0.0F, light, this.rCol, this.gCol, this.bCol, a);
            v(buf, x0 + offA.x, y0 + offA.y, z0 + offA.z, 0.5F, 1.0F, light, this.rCol, this.gCol, this.bCol, a);
            v(buf, x1 + offB.x, y1 + offB.y, z1 + offB.z, 0.5F, 1.0F, light, this.rCol, this.gCol, this.bCol, a);
            if (seg.next == null) {
               WRVector3 cap = seg.endpoint.point.copy().add(seg.diff.copy().normalize().scale(n10));
               float xc = (float)(cap.x - camPos.x);
               float yc = (float)(cap.y - camPos.y);
               float zc = (float)(cap.z - camPos.z);
               v(buf, xc - offB.x, yc - offB.y, zc - offB.z, 0.0F, 0.0F, light, this.rCol, this.gCol, this.bCol, a);
               v(buf, x1 - offB.x, y1 - offB.y, z1 - offB.z, 0.5F, 0.0F, light, this.rCol, this.gCol, this.bCol, a);
               v(buf, x1 + offB.x, y1 + offB.y, z1 + offB.z, 0.5F, 1.0F, light, this.rCol, this.gCol, this.bCol, a);
               v(buf, xc + offB.x, yc + offB.y, zc + offB.z, 0.0F, 1.0F, light, this.rCol, this.gCol, this.bCol, a);
            }

            if (seg.prev == null) {
               WRVector3 cap = seg.startpoint.point.copy().sub(seg.diff.copy().normalize().scale(n10));
               float xc = (float)(cap.x - camPos.x);
               float yc = (float)(cap.y - camPos.y);
               float zc = (float)(cap.z - camPos.z);
               v(buf, x0 - offA.x, y0 - offA.y, z0 - offA.z, 0.5F, 0.0F, light, this.rCol, this.gCol, this.bCol, a);
               v(buf, xc - offA.x, yc - offA.y, zc - offA.z, 0.0F, 0.0F, light, this.rCol, this.gCol, this.bCol, a);
               v(buf, xc + offA.x, yc + offA.y, zc + offA.z, 0.0F, 1.0F, light, this.rCol, this.gCol, this.bCol, a);
               v(buf, x0 + offA.x, y0 + offA.y, z0 + offA.z, 0.5F, 1.0F, light, this.rCol, this.gCol, this.bCol, a);
            }
         }
      }
   }

   public void render(VertexConsumer ignored, Camera camera, float partialTicks) {
      Entity camEnt = Minecraft.getInstance().getCameraEntity();
      if (camEnt != null) {
         int range = 100;
         if (Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FAST) {
            range = 50;
         }

         if (!(camEnt.distanceToSqr(this.x, this.y, this.z) > range * range)) {
            Vec3 camPos = camera.getPosition();
            RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, TEX_LARGE);
            this.applyTypePass0BlendAndColor();
            Tesselator t = Tesselator.getInstance();
            BufferBuilder buf = t.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);
            this.renderBolt(buf, camPos, partialTicks, 0, 1.0F);
            BufferUploader.drawWithShader(buf.buildOrThrow());
            RenderSystem.setShaderTexture(0, TEX_SMALL);
            this.applyTypePass1BlendAndColor();
            buf = t.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);
            this.renderBolt(buf, camPos, partialTicks, 1, 1.0F);
            BufferUploader.drawWithShader(buf.buildOrThrow());
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.blendFunc(770, 771);
         }
      }
   }
}
