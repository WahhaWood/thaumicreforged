package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

@OnlyIn(Dist.CLIENT)
public class FXWispEG extends Particle {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles.png");
   private static final float CELL = 0.0625F;
   private static final float CELL_SAFE = 0.0624375F;
   private float quadSize;
   public static final ParticleRenderType THAUMCRAFT_SHEET_TRANSLUCENT = new ParticleRenderType() {
      public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
         RenderSystem.setShader(GameRenderer::getParticleShader);
         RenderSystem.setShaderTexture(0, FXWispEG.TEX);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         RenderSystem.depthMask(false);
         return tesselator.begin(Mode.QUADS, DefaultVertexFormat.PARTICLE);
      }

      @Override
      public String toString() {
         return "THAUMCRAFT_SHEET_TRANSLUCENT";
      }
   };
   private final Entity target;
   public int blendmode;

   public FXWispEG(ClientLevel level, double x, double y, double z, Entity target) {
      super(level, x, y, z);
      this.target = target;
      this.xd = this.random.nextGaussian() * 0.03;
      this.yd = -0.05;
      this.zd = this.random.nextGaussian() * 0.03;
      this.rCol = this.random.nextFloat() * 0.05F;
      this.gCol = this.random.nextFloat() * 0.05F;
      this.bCol = this.random.nextFloat() * 0.05F;
      this.alpha = 0.0F;
      this.quadSize = 0.4F;
      this.lifetime = (int)(40.0 / (this.random.nextDouble() * 0.3 + 0.7));
      this.hasPhysics = true;
      this.setSize(0.01F, 0.01F);
      this.blendmode = 771;
      ParticleUtils.checkParticleDistance(this, 25);
   }

   @NotNull
   public ParticleRenderType getRenderType() {
      return THAUMCRAFT_SHEET_TRANSLUCENT;
   }

   protected int getLightColor(float pt) {
      return 15728880;
   }

   public void render(@NotNull VertexConsumer consumer, @NotNull Camera camera, float pt) {
      float life = 1.0F - (this.age + pt) / this.lifetime;
      if (!(life <= 0.0F)) {
         Vec3 camPos = camera.getPosition();
         double pxw = Mth.lerp(pt, this.xo, this.x);
         double pyw = Mth.lerp(pt, this.yo, this.y);
         double pzw = Mth.lerp(pt, this.zo, this.z);
         double d2 = camPos.distanceToSqr(pxw, pyw, pzw);
         float dist = (float)(1.0 - Math.min(1024.0, d2) / 1024.0);
         if (!(dist <= 0.0F)) {
            float a = 0.15F * life * dist * dist;
            if (!(a <= 0.0F)) {
               int frame = this.age % 13;
               float u0 = frame * 0.0625F;
               float u1 = u0 + 0.0624375F;
               float v0 = 0.1875F;
               float v1 = v0 + 0.0624375F;
               float size = 0.5F * this.quadSize;
               float px = (float)(pxw - camPos.x);
               float py = (float)(pyw - camPos.y);
               float pz = (float)(pzw - camPos.z);
               Quaternionf q = camera.rotation();
               Vector3f c0 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
               Vector3f c1 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
               Vector3f c2 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
               Vector3f c3 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
               int light = this.getLightColor(pt);
               consumer.addVertex(c0.x, c0.y, c0.z).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
               consumer.addVertex(c1.x, c1.y, c1.z).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
               consumer.addVertex(c2.x, c2.y, c2.z).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
               consumer.addVertex(c3.x, c3.y, c3.z).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, a).setUv2(light & 65535, light >> 16);
            }
         }
      }
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.target != null && !this.onGround) {
         Vec3 m = this.target.getDeltaMovement();
         this.x = this.x + m.x;
         this.z = this.z + m.z;
      }

      this.pushOutOfBlocks(new Vec3(this.x, this.y, this.z));
      this.move(this.xd, this.yd, this.zd);
      this.xd *= 0.98F;
      this.yd *= 0.98F;
      this.zd *= 0.98F;
      if (this.onGround) {
         this.xd *= 0.8500000190734863;
         this.zd *= 0.8500000190734863;
      }

      if (++this.age >= this.lifetime) {
         this.remove();
      }
   }

   private boolean isSolid(BlockPos pos) {
      BlockState st = this.level.getBlockState(pos);
      if (!this.level.getFluidState(pos).isEmpty()) {
         return false;
      } else {
         VoxelShape shape = st.getCollisionShape(this.level, pos);
         return !shape.isEmpty();
      }
   }

   protected boolean pushOutOfBlocks(Vec3 pos) {
      BlockPos bp = new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z));
      double fx = pos.x - bp.getX();
      double fy = pos.y - bp.getY();
      double fz = pos.z - bp.getZ();
      if (this.isSolid(bp) && !this.level.containsAnyLiquid(this.getBoundingBox())) {
         boolean xm = !this.isSolid(bp.west());
         boolean xp = !this.isSolid(bp.east());
         boolean ym = !this.isSolid(bp.below());
         boolean yp = !this.isSolid(bp.above());
         boolean zm = !this.isSolid(bp.north());
         boolean zp = !this.isSolid(bp.south());
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
      } else {
         return false;
      }
   }
}
