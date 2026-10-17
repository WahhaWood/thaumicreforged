package thaumcraft.block.warded.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import thaumcraft.block.warded.WardedBlock;
import thaumcraft.block.warded.WardedBlockEntity;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.foci.ItemFocusWarding;

public class WardedBlockEntityRenderer implements BlockEntityRenderer<WardedBlockEntity> {
   public static final ResourceLocation MODEL_LOC = ResourceLocation.fromNamespaceAndPath("thaumcraft", "warded_overlay");
   public static final ResourceLocation TEXTURE_PREFIX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/warded_glass_");
   private static final int[] CONNECTED = new int[]{
      0,
      0,
      6,
      6,
      0,
      0,
      6,
      6,
      3,
      3,
      19,
      15,
      3,
      3,
      19,
      15,
      1,
      1,
      18,
      18,
      1,
      1,
      13,
      13,
      2,
      2,
      23,
      31,
      2,
      2,
      27,
      14,
      0,
      0,
      6,
      6,
      0,
      0,
      6,
      6,
      3,
      3,
      19,
      15,
      3,
      3,
      19,
      15,
      1,
      1,
      18,
      18,
      1,
      1,
      13,
      13,
      2,
      2,
      23,
      31,
      2,
      2,
      27,
      14,
      4,
      4,
      5,
      5,
      4,
      4,
      5,
      5,
      17,
      17,
      22,
      26,
      17,
      17,
      22,
      26,
      16,
      16,
      20,
      20,
      16,
      16,
      28,
      28,
      21,
      21,
      46,
      42,
      21,
      21,
      43,
      38,
      4,
      4,
      5,
      5,
      4,
      4,
      5,
      5,
      9,
      9,
      30,
      12,
      9,
      9,
      30,
      12,
      16,
      16,
      20,
      20,
      16,
      16,
      28,
      28,
      25,
      25,
      45,
      37,
      25,
      25,
      40,
      32,
      0,
      0,
      6,
      6,
      0,
      0,
      6,
      6,
      3,
      3,
      19,
      15,
      3,
      3,
      19,
      15,
      1,
      1,
      18,
      18,
      1,
      1,
      13,
      13,
      2,
      2,
      23,
      31,
      2,
      2,
      27,
      14,
      4,
      4,
      5,
      5,
      4,
      4,
      5,
      5,
      17,
      17,
      22,
      26,
      17,
      17,
      22,
      26,
      16,
      16,
      20,
      20,
      16,
      16,
      28,
      28,
      21,
      21,
      46,
      42,
      21,
      21,
      43,
      38,
      4,
      4,
      5,
      5,
      4,
      4,
      5,
      5,
      9,
      9,
      30,
      12,
      9,
      9,
      30,
      12,
      16,
      16,
      20,
      20,
      16,
      16,
      28,
      28,
      25,
      25,
      45,
      37,
      25,
      25,
      40,
      32,
      8,
      8,
      10,
      10,
      8,
      8,
      10,
      10,
      36,
      36,
      44,
      24,
      36,
      36,
      44,
      24,
      35,
      35,
      39,
      39,
      35,
      35,
      34,
      34,
      41,
      41,
      33,
      29,
      41,
      41,
      11,
      7,
      8,
      8,
      10,
      10,
      8,
      8,
      10,
      10,
      36,
      36,
      44,
      24,
      36,
      36,
      44,
      24,
      35,
      35,
      39,
      39,
      35,
      35,
      34,
      34,
      41,
      41,
      33,
      29,
      41,
      41,
      11,
      7,
      0,
      0,
      6,
      6,
      0,
      0,
      6,
      6,
      3,
      3,
      19,
      15,
      3,
      3,
      19,
      15,
      1,
      1,
      18,
      18,
      1,
      1,
      13,
      13,
      2,
      2,
      23,
      31,
      2,
      2,
      27,
      14
   };
   private static List<BakedQuad>[][] QUADS = null;

   public WardedBlockEntityRenderer(Context ctx) {
   }

   private static void ensureQuads() {
      if (QUADS == null) {
         QUADS = new List[6][47];
         Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
         TextureAtlasSprite[] sprites = new TextureAtlasSprite[47];

         for (int i = 0; i < 47; i++) {
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(TEXTURE_PREFIX.getNamespace(), TEXTURE_PREFIX.getPath() + (i + 1));
            sprites[i] = atlas.apply(rl);
         }

         FaceBakery bakery = new FaceBakery();
         BlockFaceUV uv = new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0);
         Vector3f from = new Vector3f(0.0F, 0.0F, 0.0F);
         Vector3f to = new Vector3f(16.0F, 16.0F, 16.0F);

         for (Direction dir : Direction.values()) {
            int di = dir.get3DDataValue();

            for (int si = 0; si < 47; si++) {
               BlockElementFace face = new BlockElementFace(dir, -1, "", uv);
               BakedQuad quad = bakery.bakeQuad(from, to, face, sprites[si], dir, BlockModelRotation.X0_Y0, null, true);
               QUADS[di][si] = Collections.singletonList(quad);
            }
         }
      }
   }

   public void render(WardedBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.getCameraEntity() instanceof Player player) {
         ItemStack held = player.getMainHandItem();
         if (held.getItem() instanceof ItemWandCasting wand) {
            if (wand.getFocus(held) instanceof ItemFocusWarding) {
               ensureQuads();
               Level level = be.getLevel();
               if (level != null) {
                  float t = player.tickCount + partialTick;
                  BlockPos p = be.getBlockPos();
                  boolean owned = be.getOwnerHash() != 0 && player.getGameProfile().getName().hashCode() == be.getOwnerHash();
                  float r = Mth.sin(t / 2.0F + p.getX()) * 0.2F + 0.8F;
                  float g = Mth.sin(t / 3.0F + p.getY()) * 0.2F + (owned ? 0.7F : 0.28F);
                  float b = Mth.sin(t / 4.0F + p.getZ()) * 0.2F + 0.28F;
                  float a = owned ? 0.5F : 0.25F;
                  RenderSystem.enableBlend();
                  RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
                  RenderSystem.setShaderColor(r, g, b, a);
                  poseStack.pushPose();
                  poseStack.translate(0.5F, 0.5F, 0.5F);
                  poseStack.scale(1.002F, 1.002F, 1.002F);
                  poseStack.translate(-0.5F, -0.5F, -0.5F);
                  VertexConsumer vc = buffer.getBuffer(RenderType.translucent());
                  Pose pose = poseStack.last();
                  int full = 15728880;

                  for (Direction dir : Direction.values()) {
                     if (shouldRenderFace(level, p, dir)) {
                        int idx = computeFaceIndex(level, p, dir, be.getOwnerHash());
                        if (idx < 0 || idx >= 47) {
                           idx = 0;
                        }

                        for (BakedQuad q : QUADS[dir.get3DDataValue()][idx]) {
                           vc.putBulkData(pose, q, 1.0F, 1.0F, 1.0F, 1.0F, full, OverlayTexture.NO_OVERLAY, true);
                        }
                     }
                  }

                  poseStack.popPose();
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.disableBlend();
               }
            }
         }
      }
   }

   private static boolean shouldRenderFace(Level level, BlockPos pos, Direction dir) {
      BlockPos np = pos.relative(dir);
      BlockState ns = level.getBlockState(np);
      if (!(ns.getBlock() instanceof WardedBlock)) {
         return true;
      } else {
         BlockEntity be0 = level.getBlockEntity(pos);
         BlockEntity be1 = level.getBlockEntity(np);
         if (!(be0 instanceof WardedBlockEntity w0 && be1 instanceof WardedBlockEntity w1)) {
            return true;
         } else {
            return w0.getMimic() != null && w1.getMimic() != null ? !w0.getMimic().equals(w1.getMimic()) : true;
         }
      }
   }

   private static boolean isConnected(Level level, BlockPos pos, int ownerHash) {
      BlockState st = level.getBlockState(pos);
      if (!(st.getBlock() instanceof WardedBlock)) {
         return false;
      } else {
         return level.getBlockEntity(pos) instanceof WardedBlockEntity wbe ? wbe.getOwnerHash() == ownerHash : false;
      }
   }

   private static int computeFaceIndex(Level level, BlockPos pos, Direction dir, int ownerHash) {
      boolean[] a = new boolean[8];
      switch (dir) {
         case DOWN:
         case UP:
            a[0] = isConnected(level, pos.offset(-1, 0, -1), ownerHash);
            a[1] = isConnected(level, pos.offset(0, 0, -1), ownerHash);
            a[2] = isConnected(level, pos.offset(1, 0, -1), ownerHash);
            a[3] = isConnected(level, pos.offset(-1, 0, 0), ownerHash);
            a[4] = isConnected(level, pos.offset(1, 0, 0), ownerHash);
            a[5] = isConnected(level, pos.offset(-1, 0, 1), ownerHash);
            a[6] = isConnected(level, pos.offset(0, 0, 1), ownerHash);
            a[7] = isConnected(level, pos.offset(1, 0, 1), ownerHash);
            break;
         case NORTH:
         case SOUTH:
            int x0 = dir == Direction.NORTH ? 1 : -1;
            int x2 = dir == Direction.NORTH ? -1 : 1;
            a[0] = isConnected(level, pos.offset(x0, 1, 0), ownerHash);
            a[1] = isConnected(level, pos.offset(0, 1, 0), ownerHash);
            a[2] = isConnected(level, pos.offset(x2, 1, 0), ownerHash);
            a[3] = isConnected(level, pos.offset(x0, 0, 0), ownerHash);
            a[4] = isConnected(level, pos.offset(x2, 0, 0), ownerHash);
            a[5] = isConnected(level, pos.offset(x0, -1, 0), ownerHash);
            a[6] = isConnected(level, pos.offset(0, -1, 0), ownerHash);
            a[7] = isConnected(level, pos.offset(x2, -1, 0), ownerHash);
            break;
         case WEST:
         case EAST:
            int z0 = dir == Direction.EAST ? 1 : -1;
            int z2 = dir == Direction.EAST ? -1 : 1;
            a[0] = isConnected(level, pos.offset(0, 1, z0), ownerHash);
            a[1] = isConnected(level, pos.offset(0, 1, 0), ownerHash);
            a[2] = isConnected(level, pos.offset(0, 1, z2), ownerHash);
            a[3] = isConnected(level, pos.offset(0, 0, z0), ownerHash);
            a[4] = isConnected(level, pos.offset(0, 0, z2), ownerHash);
            a[5] = isConnected(level, pos.offset(0, -1, z0), ownerHash);
            a[6] = isConnected(level, pos.offset(0, -1, 0), ownerHash);
            a[7] = isConnected(level, pos.offset(0, -1, z2), ownerHash);
      }

      if (dir == Direction.DOWN) {
         boolean t0 = a[0];
         boolean t1 = a[1];
         boolean t2 = a[2];
         a[0] = a[5];
         a[1] = a[6];
         a[2] = a[7];
         a[5] = t0;
         a[6] = t1;
         a[7] = t2;
      }

      if (!a[1] || !a[3]) {
         a[0] = false;
      }

      if (!a[1] || !a[4]) {
         a[2] = false;
      }

      if (!a[6] || !a[3]) {
         a[5] = false;
      }

      if (!a[6] || !a[4]) {
         a[7] = false;
      }

      int mask = 0;

      for (int i = 0; i < 8; i++) {
         if (a[i]) {
            mask |= 1 << i;
         }
      }

      return mask >= 0 && mask < CONNECTED.length ? CONNECTED[mask] : 0;
   }
}
