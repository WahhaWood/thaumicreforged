package thaumcraft.particles.fx.particles;

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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FXEssentiaTrail extends Particle {
   public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles.png");
   public static final ParticleRenderType THAUMCRAFT_ADDITIVE_TEX = new ParticleRenderType() {
      public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
         RenderSystem.setShader(GameRenderer::getParticleShader);
         RenderSystem.setShaderTexture(0, FXEssentiaTrail.TEXTURE);
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.depthMask(false);
         return tesselator.begin(Mode.QUADS, DefaultVertexFormat.PARTICLE);
      }

      @Override
      public String toString() {
         return "THAUMCRAFT_ADDITIVE_TEX";
      }
   };
   private final double targetX;
   private final double targetY;
   private final double targetZ;
   private final int count;
   private float scale;

   public FXEssentiaTrail(
      ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, int count, int color, float baseScale
   ) {
      super(level, x, y, z);
      this.targetX = targetX;
      this.targetY = targetY;
      this.targetZ = targetZ;
      this.count = count;
      this.scale = (Mth.sin(count / 2.0F) * 0.1F + 1.0F) * baseScale;
      double dx = targetX - this.x;
      double dy = targetY - this.y;
      double dz = targetZ - this.z;
      int dist = (int)(Math.sqrt(dx * dx + dy * dy + dz * dz) * 30.0);
      if (dist < 1) {
         dist = 1;
      }

      this.lifetime = dist / 2 + this.random.nextInt(dist);
      this.xd = Mth.sin(count / 4.0F) * 0.015F + this.random.nextGaussian() * 0.002F;
      this.yd = 0.1F + Mth.sin(count / 3.0F) * 0.01F;
      this.zd = Mth.sin(count / 2.0F) * 0.015F + this.random.nextGaussian() * 0.002F;
      int rr = color >> 16 & 0xFF;
      int gg = color >> 8 & 0xFF;
      int bb = color & 0xFF;
      float rf = rr / 255.0F;
      float gf = gg / 255.0F;
      float bf = bb / 255.0F;
      float dr = rf * 0.2F;
      float dg = gf * 0.2F;
      float db = bf * 0.2F;
      this.rCol = rf - dr + this.random.nextFloat() * dr;
      this.gCol = gf - dg + this.random.nextFloat() * dg;
      this.bCol = bf - db + this.random.nextFloat() * db;
      this.alpha = 0.5F;
      this.gravity = 0.2F;
      this.hasPhysics = true;
      this.setSize(0.01F, 0.01F);
      this.applyDistanceCulling(64, 32);
   }

   public ParticleRenderType getRenderType() {
      return THAUMCRAFT_ADDITIVE_TEX;
   }

   public int getLightColor(float partialTick) {
      return 15728880;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (++this.age >= this.lifetime) {
         this.remove();
      } else {
         this.yd = this.yd + 0.01 * this.gravity;
         if (this.hasPhysics) {
            this.pushOutOfBlocks(new Vec3(this.x, this.y, this.z));
         }

         this.move(this.xd, this.yd, this.zd);
         this.xd *= 0.985;
         this.yd *= 0.985;
         this.zd *= 0.985;
         this.xd = Mth.clamp(this.xd, -0.05, 0.05);
         this.yd = Mth.clamp(this.yd, -0.05, 0.05);
         this.zd = Mth.clamp(this.zd, -0.05, 0.05);
         double dx = this.targetX - this.x;
         double dy = this.targetY - this.y;
         double dz = this.targetZ - this.z;
         double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (d < 2.0) {
            this.scale *= 0.98F;
         }

         if (this.scale < 0.2F) {
            this.remove();
         } else {
            if (d > 1.0E-6) {
               double nx = dx / d;
               double ny = dy / d;
               double nz = dz / d;
               double a = 0.01 / Math.min(1.0, d);
               this.xd += nx * a;
               this.yd += ny * a;
               this.zd += nz * a;
            }
         }
      }
   }

   public void setGravity(float g) {
      this.gravity = g;
   }

   public void render(VertexConsumer consumer, Camera camera, float pt) {
      Vec3 cam = camera.getPosition();
      float px = (float)(Mth.lerp(pt, this.xo, this.x) - cam.x);
      float py = (float)(Mth.lerp(pt, this.yo, this.y) - cam.y);
      float pz = (float)(Mth.lerp(pt, this.zo, this.z) - cam.z);
      float anim = Mth.sin((this.age + pt - this.count) / 5.0F) * 0.25F + 1.0F;
      float s = 0.1F * this.scale * anim;
      float u0 = 0.5625F;
      float u1 = 0.625F;
      float v0 = 0.0625F;
      float v1 = 0.125F;
      Quaternionf q = new Quaternionf(camera.rotation());
      Vector3f vA = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(s);
      Vector3f vB = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(s);
      Vector3f vC = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(s);
      Vector3f vD = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(s);
      int light = this.getLightColor(pt);
      float a = 0.5F;
      consumer.addVertex(px + vA.x(), py + vA.y(), pz + vA.z()).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
      consumer.addVertex(px + vB.x(), py + vB.y(), pz + vB.z()).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
      consumer.addVertex(px + vC.x(), py + vC.y(), pz + vC.z()).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
      consumer.addVertex(px + vD.x(), py + vD.y(), pz + vD.z()).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
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

   private boolean isBlocking(BlockPos pos) {
      if (this.level.isEmptyBlock(pos)) {
         return false;
      } else {
         return !this.level.getFluidState(pos).isEmpty() ? false : this.level.getBlockState(pos).isCollisionShapeFullBlock(this.level, pos);
      }
   }

   protected boolean pushOutOfBlocks(Vec3 pos) {
      BlockPos bp = new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z));
      if (!this.isBlocking(bp)) {
         return false;
      } else {
         double fx = pos.x - bp.getX();
         double fy = pos.y - bp.getY();
         double fz = pos.z - bp.getZ();
         boolean xm = !this.isBlocking(bp.west());
         boolean xp = !this.isBlocking(bp.east());
         boolean ym = !this.isBlocking(bp.below());
         boolean yp = !this.isBlocking(bp.above());
         boolean zm = !this.isBlocking(bp.north());
         boolean zp = !this.isBlocking(bp.south());
         int dir = -1;
         double best = 9999.0;
         if (xm && fx < best) {
            best = fx;
            dir = 0;
         }

         if (xp && 1.0 - fx < best) {
            best = 1.0 - fx;
            dir = 1;
         }

         if (ym && fy < best) {
            best = fy;
            dir = 2;
         }

         if (yp && 1.0 - fy < best) {
            best = 1.0 - fy;
            dir = 3;
         }

         if (zm && fz < best) {
            best = fz;
            dir = 4;
         }

         if (zp && 1.0 - fz < best) {
            dir = 5;
         }

         float s = this.random.nextFloat() * 0.05F + 0.025F;
         float r = (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;
         if (dir == 0) {
            this.xd = -s;
            this.yd = r;
            this.zd = r;
            return true;
         } else if (dir == 1) {
            this.xd = s;
            this.yd = r;
            this.zd = r;
            return true;
         } else if (dir == 2) {
            this.yd = -s;
            this.xd = r;
            this.zd = r;
            return true;
         } else if (dir == 3) {
            this.yd = s;
            this.xd = r;
            this.zd = r;
            return true;
         } else if (dir == 4) {
            this.zd = -s;
            this.xd = r;
            this.yd = r;
            return true;
         } else if (dir == 5) {
            this.zd = s;
            this.xd = r;
            this.yd = r;
            return true;
         } else {
            return false;
         }
      }
   }
}
