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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.api.nodes.IRevealer;
import thaumcraft.particles.fx.particles.FXEssentiaTrail;

public class FXBeamPower extends Particle {
   public static final ResourceLocation TEX_BEAM = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam1.png");
   public static final ParticleRenderType THAUMCRAFT_BEAM = new ParticleRenderType() {
      public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
         RenderSystem.setShader(GameRenderer::getParticleShader);
         RenderSystem.setShaderTexture(0, FXBeamPower.TEX_BEAM);
         // setShaderTexture does not bind; without this the wrap mode lands on whatever texture was bound last.
         textureManager.getTexture(FXBeamPower.TEX_BEAM).bind();
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
         return "THAUMCRAFT_BEAM";
      }
   };
   public int particle = 16;
   public double tX;
   public double tY;
   public double tZ;
   public double ptX;
   public double ptY;
   public double ptZ;
   public float opacity = 0.3F;
   public final float size = 0.7F;

   public FXBeamPower(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, float r, float g, float b, int maxAge) {
      super(level, x, y, z);
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
      return THAUMCRAFT_BEAM;
   }

   public int getLightColor(float partialTick) {
      return 15728880;
   }

   public void updateBeam(double x, double y, double z, double tx, double ty, double tz) {
      this.setPos(x, y, z);
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

   public void setPulse(boolean pulse, float r, float g, float b) {
      this.setRGB(r, g, b);
      if (pulse) {
         this.opacity = 0.8F;
      }
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.ptX = this.tX;
      this.ptY = this.tY;
      this.ptZ = this.tZ;
      if (this.opacity > 0.3F) {
         this.opacity -= 0.025F;
      }

      if (this.opacity < 0.3F) {
         this.opacity = 0.3F;
      }

      if (++this.age >= this.lifetime) {
         this.remove();
      }
   }

   public void render(VertexConsumer consumer, Camera camera, float pt) {
      Vec3 cam = camera.getPosition();
      float sx = (float)(Mth.lerp(pt, this.xo, this.x) - cam.x);
      float sy = (float)(Mth.lerp(pt, this.yo, this.y) - cam.y);
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
         Vector3f r1 = new Vector3f(dir).cross(camUp);
         if (r1.lengthSquared() < 1.0E-6F) {
            r1 = new Vector3f(dir).cross(camRight);
         }

         r1.normalize();
         Vector3f r2 = new Vector3f(dir).cross(r1).normalize();
         float halfW = 0.15F * 0.7F;
         float t = (float)(Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L) + pt;
         float v0 = -t * 0.2F - Mth.floor(-t * 0.1F);
         float reveal = this.getRevealerFactor();
         float a = this.opacity * reveal;
         int light = 15728880;
         this.drawPlane(consumer, sx, sy, sz, ex, ey, ez, r1, halfW, len, v0, a, light);
         this.drawPlane(consumer, sx, sy, sz, ex, ey, ez, r2, halfW, len, v0 + 0.33333334F, a, light);
      }
   }

   public void drawPlane(
      VertexConsumer vc, float sx, float sy, float sz, float ex, float ey, float ez, Vector3f r, float halfW, float len, float vStart, float a, int light
   ) {
      float rx = r.x() * halfW;
      float ry = r.y() * halfW;
      float rz = r.z() * halfW;
      float u0 = 0.0F;
      float u1 = 1.0F;
      float v0 = -1.0F + vStart;
      float v1 = len + v0;
      vc.addVertex(ex - rx, ey - ry, ez - rz).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
      vc.addVertex(sx - rx, sy - ry, sz - rz).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
      vc.addVertex(sx + rx, sy + ry, sz + rz).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
      vc.addVertex(ex + rx, ey + ry, ez + rz).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
   }

   public float getRevealerFactor() {
      return wearsRevealer() ? 1.0F : 0.1F;
   }

   /** TC4: the beam is barely visible unless the viewer wears goggles of revealing (or other IRevealer headgear). */
   public static boolean wearsRevealer() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getCameraEntity() instanceof Player p) {
         ItemStack st = p.getItemBySlot(EquipmentSlot.HEAD);
         return !st.isEmpty() && st.getItem() instanceof IRevealer;
      }

      return false;
   }

   public boolean shouldCull() {
      return false;
   }

   /** NeoForge culls particles by this box alone; the default 0.02 box at the start lost the beam whenever its parent end was off screen. */
   @Override
   public AABB getRenderBoundingBox(float partialTicks) {
      return new AABB(this.x, this.y, this.z, this.tX, this.tY, this.tZ).inflate(1.0);
   }

   public void applyDistanceCulling(int fancy, int fast) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         int max = mc.options.graphicsMode().get() == GraphicsStatus.FANCY ? fancy : fast;
         if (mc.player.distanceToSqr(this.x, this.y, this.z) > max * max) {
            this.lifetime = 0;
         }
      }
   }

   /**
    * TC4 FXBeamPower.renderFlare: a pulsing glow where the beam meets the relay. It is drawn from the particle sheet, so in
    * 1.21.1 it is a particle of its own that follows the beam and dies with it.
    */
   public static class Flare extends Particle {
      private final FXBeamPower beam;

      public Flare(ClientLevel level, FXBeamPower beam) {
         super(level, beam.tX, beam.tY, beam.tZ);
         this.beam = beam;
         this.hasPhysics = false;
         this.lifetime = Integer.MAX_VALUE;
         this.setSize(0.02F, 0.02F);
      }

      public ParticleRenderType getRenderType() {
         return FXEssentiaTrail.THAUMCRAFT_ADDITIVE_TEX;
      }

      public int getLightColor(float partialTick) {
         return 15728880;
      }

      public void tick() {
         if (!this.beam.isAlive()) {
            this.remove();
         } else {
            this.setPos(this.beam.tX, this.beam.tY, this.beam.tZ);
         }
      }

      public void render(VertexConsumer consumer, Camera camera, float pt) {
         Vec3 cam = camera.getPosition();
         float px = (float)(Mth.lerp(pt, this.beam.ptX, this.beam.tX) - cam.x);
         float py = (float)(Mth.lerp(pt, this.beam.ptY, this.beam.tY) - cam.y);
         float pz = (float)(Mth.lerp(pt, this.beam.ptZ, this.beam.tZ) - cam.z);
         float s = 0.66F * this.beam.opacity;
         float u0 = this.beam.age % 16 / 16.0F;
         float u1 = u0 + 0.0624375F;
         float v0 = 0.3125F;
         float v1 = v0 + 0.0624375F;
         float a = this.beam.opacity * (wearsRevealer() ? 1.0F : 0.2F);
         Quaternionf q = camera.rotation();
         Vector3f vA = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(s);
         Vector3f vB = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(s);
         Vector3f vC = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(s);
         Vector3f vD = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(s);
         int light = 15728880;
         float r = this.beam.rCol;
         float g = this.beam.gCol;
         float b = this.beam.bCol;
         consumer.addVertex(px + vA.x(), py + vA.y(), pz + vA.z()).setUv(u1, v1).setColor(r, g, b, a).setUv2(light & 65535, light >> 16);
         consumer.addVertex(px + vB.x(), py + vB.y(), pz + vB.z()).setUv(u1, v0).setColor(r, g, b, a).setUv2(light & 65535, light >> 16);
         consumer.addVertex(px + vC.x(), py + vC.y(), pz + vC.z()).setUv(u0, v0).setColor(r, g, b, a).setUv2(light & 65535, light >> 16);
         consumer.addVertex(px + vD.x(), py + vD.y(), pz + vD.z()).setUv(u0, v1).setColor(r, g, b, a).setUv2(light & 65535, light >> 16);
      }
   }
}
