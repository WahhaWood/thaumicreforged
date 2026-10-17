package thaumcraft.particles.fx.beams;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
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
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class FXBeamBore extends Particle {
   public static final ResourceLocation TEX_BEAM0 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam.png");
   public static final ResourceLocation TEX_BEAM1 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam1.png");
   public static final ResourceLocation TEX_BEAM2 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam2.png");
   public static final ResourceLocation TEX_BEAM3 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam3.png");
   private static final ParticleRenderType[] RENDER_TYPES = new ParticleRenderType[]{
      new FXBeamBore.BeamTypeRenderType(TEX_BEAM0),
      new FXBeamBore.BeamTypeRenderType(TEX_BEAM1),
      new FXBeamBore.BeamTypeRenderType(TEX_BEAM2),
      new FXBeamBore.BeamTypeRenderType(TEX_BEAM3)
   };
   public int particle = 16;
   public int impact = 0;
   final double offset;
   double tX;
   double tY;
   double tZ;
   double ptX;
   double ptY;
   double ptZ;
   int type = 0;
   float endMod = 1.0F;
   boolean reverse = false;
   boolean pulse = true;
   int rotationspeed = 5;
   float prevSize = 0.0F;
   private FXBeamBoreImpact impactParticle;

   public FXBeamBore(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, float r, float g, float b, int maxAge) {
      super(level, x, y, z);
      this.offset = 0.0;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.tX = tx;
      this.tY = ty;
      this.tZ = tz;
      this.ptX = tx;
      this.ptY = ty;
      this.ptZ = tz;
      this.rCol = r;
      this.gCol = g;
      this.bCol = b;
      this.alpha = 1.0F;
      this.hasPhysics = false;
      this.lifetime = maxAge;
      this.setSize(0.02F, 0.02F);
      this.applyDistanceCulling(50, 25);
   }

   public ParticleRenderType getRenderType() {
      int idx = this.type;
      if (idx < 0) {
         idx = 0;
      }

      if (idx > 3) {
         idx = 3;
      }

      return RENDER_TYPES[idx];
   }

   public int getLightColor(float pt) {
      return 15728880;
   }

   public void updateBeam(double tx, double ty, double tz) {
      this.tX = tx;
      this.tY = ty;
      this.tZ = tz;

      while (this.lifetime - this.age < 4) {
         this.lifetime++;
      }
   }

   public void setRGB(float r, float g, float b) {
      this.rCol = r;
      this.gCol = g;
      this.bCol = b;
   }

   public float getRed() {
      return this.rCol;
   }

   public float getGreen() {
      return this.gCol;
   }

   public float getBlue() {
      return this.bCol;
   }

   public void setType(int type) {
      this.type = type;
   }

   public void setEndMod(float endMod) {
      this.endMod = endMod;
   }

   public void setReverse(boolean reverse) {
      this.reverse = reverse;
   }

   public void setPulse(boolean pulse) {
      this.pulse = pulse;
   }

   public void setRotationspeed(int rotationspeed) {
      this.rotationspeed = Math.max(1, rotationspeed);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y + this.offset;
      this.zo = this.z;
      this.ptX = this.tX;
      this.ptY = this.tY;
      this.ptZ = this.tZ;
      if (this.impact > 0) {
         if (this.impactParticle == null || !this.impactParticle.isAlive()) {
            this.impactParticle = new FXBeamBoreImpact(this.level, this);
            Minecraft.getInstance().particleEngine.add(this.impactParticle);
         }

         this.impact--;
      }

      if (++this.age >= this.lifetime) {
         this.remove();
      }
   }

   public void render(VertexConsumer consumer, Camera camera, float pt) {
      Vec3 cam = camera.getPosition();
      float sx = (float)(Mth.lerp(pt, this.xo, this.x) - cam.x);
      float sy = (float)(Mth.lerp(pt, this.yo, this.y + this.offset) - cam.y);
      float sz = (float)(Mth.lerp(pt, this.zo, this.z) - cam.z);
      float ex = (float)(Mth.lerp(pt, this.ptX, this.tX) - cam.x);
      float ey = (float)(Mth.lerp(pt, this.ptY, this.tY) - cam.y);
      float ez = (float)(Mth.lerp(pt, this.ptZ, this.tZ) - cam.z);
      float dx = ex - sx;
      float dy = ey - sy;
      float dz = ez - sz;
      float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
      if (!(len < 1.0E-6F)) {
         float invLen = 1.0F / len;
         Vector3f dir = new Vector3f(dx * invLen, dy * invLen, dz * invLen);
         Vector3f camUp = new Vector3f(0.0F, 1.0F, 0.0F).rotate(camera.rotation());
         Vector3f camRight = new Vector3f(1.0F, 0.0F, 0.0F).rotate(camera.rotation());
         Vector3f r = new Vector3f(dir).cross(camUp);
         if (r.lengthSquared() < 1.0E-6F) {
            r = new Vector3f(dir).cross(camRight);
         }

         r.normalize();
         Vector3f u = new Vector3f(dir).cross(r).normalize();
         float sizeNow;
         if (this.pulse) {
            float target = Math.min(this.age / 4.0F, 1.0F);
            sizeNow = this.prevSize + (target - this.prevSize) * pt;
         } else {
            sizeNow = 1.0F;
         }

         float alpha = 0.4F;
         int left = this.lifetime - this.age;
         if (this.pulse && left <= 4) {
            alpha = 0.4F - (4 - left) * 0.1F;
         }

         if (alpha < 0.0F) {
            alpha = 0.0F;
         }

         float time = (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0) + pt;
         if (this.reverse) {
            time *= -1.0F;
         }

         float scroll = -time * 0.2F - Mth.floor(-time * 0.1F);
         int div = Math.max(1, 360 / Math.max(1, this.rotationspeed));
         float rot = ((float)(this.level.getGameTime() % div) + pt) * this.rotationspeed;
         float startW = 0.15F * sizeNow;
         float endW = 0.15F * sizeNow * this.endMod;
         int light = 15728880;
         float lenScaled = len * sizeNow;

         for (int i = 0; i < 3; i++) {
            float ang = (rot + i * 60.0F) * (float) (Math.PI / 180.0);
            float ca = Mth.cos(ang);
            float sa = Mth.sin(ang);
            Vector3f perp = new Vector3f(r.x() * ca + u.x() * sa, r.y() * ca + u.y() * sa, r.z() * ca + u.z() * sa).normalize();
            float v0 = -1.0F + scroll + i / 3.0F;
            float v1 = lenScaled + v0;
            float sxm = sx - perp.x() * startW;
            float sym = sy - perp.y() * startW;
            float szm = sz - perp.z() * startW;
            float sxp = sx + perp.x() * startW;
            float syp = sy + perp.y() * startW;
            float szp = sz + perp.z() * startW;
            float exm = ex - perp.x() * endW;
            float eym = ey - perp.y() * endW;
            float ezm = ez - perp.z() * endW;
            float exp = ex + perp.x() * endW;
            float eyp = ey + perp.y() * endW;
            float ezp = ez + perp.z() * endW;
            consumer.addVertex(exm, eym, ezm).setUv(1.0F, v1).setColor(this.rCol, this.gCol, this.bCol, alpha).setUv2(light & 65535, light >> 16);
            consumer.addVertex(sxm, sym, szm).setUv(1.0F, v0).setColor(this.rCol, this.gCol, this.bCol, alpha).setUv2(light & 65535, light >> 16);
            consumer.addVertex(sxp, syp, szp).setUv(0.0F, v0).setColor(this.rCol, this.gCol, this.bCol, alpha).setUv2(light & 65535, light >> 16);
            consumer.addVertex(exp, eyp, ezp).setUv(0.0F, v1).setColor(this.rCol, this.gCol, this.bCol, alpha).setUv2(light & 65535, light >> 16);
         }

         this.prevSize = sizeNow;
      }
   }

   private void applyDistanceCulling(int fancy, int fast) {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player == null) {
            return;
         }

         int max = mc.options.graphicsMode().get() == GraphicsStatus.FANCY ? fancy : fast;
         if (mc.player.distanceToSqr(this.x, this.y, this.z) > max * max) {
            this.lifetime = 0;
         }
      } catch (Throwable var5) {
      }
   }

   public static final class BeamTypeRenderType implements ParticleRenderType {
      private final ResourceLocation tex;

      private BeamTypeRenderType(ResourceLocation tex) {
         this.tex = tex;
      }

      public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
         RenderSystem.setShader(GameRenderer::getParticleShader);
         RenderSystem.setShaderTexture(0, this.tex);
         GlStateManager._texParameter(3553, 10242, 10497);
         GlStateManager._texParameter(3553, 10243, 10497);
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.depthMask(false);
         return tesselator.begin(Mode.QUADS, DefaultVertexFormat.PARTICLE);
      }

      @Override
      public String toString() {
         return "THAUMCRAFT_BEAM_BORE(" + this.tex + ")";
      }
   }

   /** NeoForge culls particles by this box alone; it must span the whole beam, not the 0.02 box at its start. */
   @Override
   public AABB getRenderBoundingBox(float partialTicks) {
      return new AABB(this.x, this.y, this.z, this.tX, this.tY, this.tZ).inflate(1.0);
   }
}
