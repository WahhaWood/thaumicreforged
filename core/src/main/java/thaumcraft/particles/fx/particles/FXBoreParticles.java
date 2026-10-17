package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.List;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FXBoreParticles extends TextureSheetParticle {
   public static final ParticleRenderType THAUMCRAFT_BLOCK_SHEET_OPAQUE = new ParticleRenderType() {
      public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
         RenderSystem.setShader(GameRenderer::getParticleShader);
         RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
         return tesselator.begin(Mode.QUADS, DefaultVertexFormat.PARTICLE);
      }

      @Override
      public String toString() {
         return "THAUMCRAFT_BLOCK_SHEET_OPAQUE";
      }
   };
   private final BlockState blockState;
   private final ItemStack itemStack;
   private final Direction side;
   private final double targetX;
   private final double targetY;
   private final double targetZ;

   public FXBoreParticles(
      ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, BlockState blockState, Direction side
   ) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.blockState = blockState;
      this.itemStack = ItemStack.EMPTY;
      this.side = side;
      this.targetX = targetX;
      this.targetY = targetY;
      this.targetZ = targetZ;
      this.setSprite(getSpriteForBlockFace(level, blockState, side));
      this.gravity = 0.2F;
      this.hasPhysics = true;
      this.rCol = 0.6F;
      this.gCol = 0.6F;
      this.bCol = 0.6F;
      this.quadSize = this.random.nextFloat() * 0.3F + 0.4F;
      this.alpha = 1.0F;
      double dx = targetX - this.x;
      double dy = targetY - this.y;
      double dz = targetZ - this.z;
      int dist = (int)(Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0);
      if (dist < 1) {
         dist = 1;
      }

      this.lifetime = dist / 2 + this.random.nextInt(dist);
      float s = 0.01F;
      this.xd = this.random.nextGaussian() * s;
      this.yd = this.random.nextGaussian() * s;
      this.zd = this.random.nextGaussian() * s;
      this.applyDistanceCulling(64, 32);
   }

   public FXBoreParticles(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, ItemStack stack, Direction side) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.blockState = null;
      this.itemStack = stack.copy();
      this.side = side;
      this.targetX = targetX;
      this.targetY = targetY;
      this.targetZ = targetZ;
      this.setSprite(getSpriteForItem(level, this.itemStack));
      this.gravity = 0.2F;
      this.hasPhysics = true;
      this.rCol = 0.6F;
      this.gCol = 0.6F;
      this.bCol = 0.6F;
      this.quadSize = this.random.nextFloat() * 0.3F + 0.4F;
      this.alpha = 1.0F;
      double dx = targetX - this.x;
      double dy = targetY - this.y;
      double dz = targetZ - this.z;
      int dist = (int)(Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0);
      if (dist < 1) {
         dist = 1;
      }

      this.lifetime = dist / 2 + this.random.nextInt(dist);
      float s = 0.01F;
      this.xd = this.random.nextGaussian() * s;
      this.yd = this.random.nextGaussian() * s;
      this.zd = this.random.nextGaussian() * s;
      this.applyDistanceCulling(64, 32);
   }

   public FXBoreParticles(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, Item item, Direction side) {
      this(level, x, y, z, targetX, targetY, targetZ, new ItemStack(item), side);
   }

   public ParticleRenderType getRenderType() {
      return THAUMCRAFT_BLOCK_SHEET_OPAQUE;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (++this.age < this.lifetime
         && (Mth.floor(this.x) != Mth.floor(this.targetX) || Mth.floor(this.y) != Mth.floor(this.targetY) || Mth.floor(this.z) != Mth.floor(this.targetZ))) {
         this.pushOutOfBlocks(new Vec3(this.x, this.y, this.z));
         this.move(this.xd, this.yd, this.zd);
         this.xd *= 0.985;
         this.yd *= 0.985;
         this.zd *= 0.985;
         double dx = this.targetX - this.x;
         double dy = this.targetY - this.y;
         double dz = this.targetZ - this.z;
         double accel = 0.3;
         double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (d < 4.0) {
            this.quadSize *= 0.9F;
            accel = 0.6;
         }

         if (d > 1.0E-6) {
            double nx = dx / d;
            double ny = dy / d;
            double nz = dz / d;
            this.xd += nx * accel;
            this.yd += ny * accel;
            this.zd += nz * accel;
         }

         this.xd = Mth.clamp(this.xd, -0.35, 0.35);
         this.yd = Mth.clamp(this.yd, -0.35, 0.35);
         this.zd = Mth.clamp(this.zd, -0.35, 0.35);
      } else {
         this.remove();
      }
   }

   public FXBoreParticles setColor(int x, int y, int z) {
      if (this.blockState == null) {
         if (!this.itemStack.isEmpty()) {
            int c = Minecraft.getInstance().getItemColors().getColor(this.itemStack, 0);
            this.rCol *= (c >> 16 & 0xFF) / 255.0F;
            this.gCol *= (c >> 8 & 0xFF) / 255.0F;
            this.bCol *= (c & 0xFF) / 255.0F;
         }

         return this;
      } else if (this.blockState.is(Blocks.GRASS_BLOCK) && this.side != Direction.UP) {
         return this;
      } else {
         BlockPos pos = new BlockPos(x, y, z);

         try {
            int c = Minecraft.getInstance().getBlockColors().getColor(this.blockState, this.level, pos, 0);
            if (c != -1) {
               this.rCol *= (c >> 16 & 0xFF) / 255.0F;
               this.gCol *= (c >> 8 & 0xFF) / 255.0F;
               this.bCol *= (c & 0xFF) / 255.0F;
            }
         } catch (Throwable var6) {
         }

         return this;
      }
   }

   public void setAlpha(float alpha) {
      this.alpha = alpha;
   }

   public FXBoreParticles applyRenderColor(int tintIndex) {
      if (this.blockState == null) {
         if (!this.itemStack.isEmpty()) {
            int c = Minecraft.getInstance().getItemColors().getColor(this.itemStack, tintIndex);
            this.rCol *= (c >> 16 & 0xFF) / 255.0F;
            this.gCol *= (c >> 8 & 0xFF) / 255.0F;
            this.bCol *= (c & 0xFF) / 255.0F;
         }

         return this;
      } else if (this.blockState.is(Blocks.GRASS_BLOCK)) {
         return this;
      } else {
         BlockPos pos = BlockPos.containing(this.x, this.y, this.z);

         try {
            int c = Minecraft.getInstance().getBlockColors().getColor(this.blockState, this.level, pos, tintIndex);
            if (c != -1) {
               this.rCol *= (c >> 16 & 0xFF) / 255.0F;
               this.gCol *= (c >> 8 & 0xFF) / 255.0F;
               this.bCol *= (c & 0xFF) / 255.0F;
            }
         } catch (Throwable var4) {
         }

         return this;
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

   private static TextureAtlasSprite getSpriteForItem(ClientLevel level, ItemStack stack) {
      Minecraft mc = Minecraft.getInstance();
      BakedModel model = mc.getItemRenderer().getModel(stack, level, null, 0);
      return model.getParticleIcon();
   }

   private static TextureAtlasSprite getSpriteForBlockFace(ClientLevel level, BlockState state, Direction side) {
      Minecraft mc = Minecraft.getInstance();
      BakedModel model = mc.getBlockRenderer().getBlockModel(state);
      RandomSource rs = RandomSource.create(12648430L);
      List<BakedQuad> quads = model.getQuads(state, side, rs);
      if (!quads.isEmpty()) {
         return quads.get(0).getSprite();
      } else {
         quads = model.getQuads(state, null, rs);
         return !quads.isEmpty() ? quads.get(0).getSprite() : model.getParticleIcon();
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
