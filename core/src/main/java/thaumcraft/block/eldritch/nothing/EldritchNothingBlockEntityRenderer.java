package thaumcraft.block.eldritch.nothing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import thaumcraft.util.ThaumcraftRenderTypes;

public class EldritchNothingBlockEntityRenderer implements BlockEntityRenderer<EldritchNothingBlockEntity> {
   public EldritchNothingBlockEntityRenderer(Context ctx) {
   }

   public void render(EldritchNothingBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Level level = be.getLevel();
      if (level != null) {
         BlockPos pos = be.getBlockPos();
         Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
         double cx = pos.getX() + 0.5;
         double cy = pos.getY() + 0.5;
         double cz = pos.getZ() + 0.5;
         // Дорогой шейдер энд-портала — только вблизи (12 м): дальние грани
         // рисуются дешёвой статичной текстурой. Раньше радиус был ~22 м и в
         // большом зале под шейдером оказывались сотни блоков — 30 FPS.
         boolean inRange = cam.distanceToSqr(cx, cy, cz) < 144.0;
         Matrix4f mat = poseStack.last().pose();
         if (inRange) {
            VertexConsumer vc = buffer.getBuffer(ThaumcraftRenderTypes.portal());
            renderFacesPortal(level, pos, vc, mat);
         } else {
            VertexConsumer vc = buffer.getBuffer(ThaumcraftRenderTypes.far());
            renderFacesFar(level, pos, vc, mat);
         }
      }
   }

   private static boolean faceVisible(Level level, BlockPos pos, Direction dir) {
      BlockPos np = pos.relative(dir);
      BlockState ns = level.getBlockState(np);
      // Общая грань двух блоков пустоты не видна ни с одной стороны (NO_CULL
      // рисовал бы её дважды вглубь геометрии) — пропускаем. В смежных ячейках
      // лабиринта такие интерфейсы повсюду: проходы стыкуются void-к-void.
      if (ns.is(thaumcraft.init.registry.ModBlocks.ELDRITCH_NOTHING.get())) {
         return false;
      }

      return !ns.isSolidRender(level, np);
   }

   private static void v(VertexConsumer vc, Matrix4f mat, float x, float y, float z) {
      vc.addVertex(mat, x, y, z);
   }

   private static void vuv(VertexConsumer vc, Matrix4f mat, float x, float y, float z, float u, float v) {
      vc.addVertex(mat, x, y, z).setUv(u, v);
   }

   private static void quadBoth(
      VertexConsumer vc, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4
   ) {
      v(vc, mat, x1, y1, z1);
      v(vc, mat, x2, y2, z2);
      v(vc, mat, x3, y3, z3);
      v(vc, mat, x4, y4, z4);
      v(vc, mat, x4, y4, z4);
      v(vc, mat, x3, y3, z3);
      v(vc, mat, x2, y2, z2);
      v(vc, mat, x1, y1, z1);
   }

   private static void quadBothUV(
      VertexConsumer vc,
      Matrix4f mat,
      float x1,
      float y1,
      float z1,
      float u1,
      float v1,
      float x2,
      float y2,
      float z2,
      float u2,
      float v2,
      float x3,
      float y3,
      float z3,
      float u3,
      float v3,
      float x4,
      float y4,
      float z4,
      float u4,
      float v4
   ) {
      vuv(vc, mat, x1, y1, z1, u1, v1);
      vuv(vc, mat, x2, y2, z2, u2, v2);
      vuv(vc, mat, x3, y3, z3, u3, v3);
      vuv(vc, mat, x4, y4, z4, u4, v4);
      vuv(vc, mat, x4, y4, z4, u4, v4);
      vuv(vc, mat, x3, y3, z3, u3, v3);
      vuv(vc, mat, x2, y2, z2, u2, v2);
      vuv(vc, mat, x1, y1, z1, u1, v1);
   }

   private static void renderFacesPortal(Level level, BlockPos pos, VertexConsumer vc, Matrix4f mat) {
      if (faceVisible(level, pos, Direction.UP)) {
         quadBoth(vc, mat, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      }

      if (faceVisible(level, pos, Direction.DOWN)) {
         quadBoth(vc, mat, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
      }

      if (faceVisible(level, pos, Direction.NORTH)) {
         quadBoth(vc, mat, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F);
      }

      if (faceVisible(level, pos, Direction.SOUTH)) {
         quadBoth(vc, mat, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F);
      }

      if (faceVisible(level, pos, Direction.WEST)) {
         quadBoth(vc, mat, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F);
      }

      if (faceVisible(level, pos, Direction.EAST)) {
         quadBoth(vc, mat, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
      }
   }

   private static void renderFacesFar(Level level, BlockPos pos, VertexConsumer vc, Matrix4f mat) {
      if (faceVisible(level, pos, Direction.UP)) {
         quadBothUV(vc, mat, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F);
      }

      if (faceVisible(level, pos, Direction.DOWN)) {
         quadBothUV(vc, mat, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F);
      }

      if (faceVisible(level, pos, Direction.NORTH)) {
         quadBothUV(vc, mat, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F);
      }

      if (faceVisible(level, pos, Direction.SOUTH)) {
         quadBothUV(vc, mat, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F);
      }

      if (faceVisible(level, pos, Direction.WEST)) {
         quadBothUV(vc, mat, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F);
      }

      if (faceVisible(level, pos, Direction.EAST)) {
         quadBothUV(vc, mat, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F);
      }
   }

   /** The block entity's own box: NeoForge 21.1 asks the renderer, not the block entity. */
   @Override
   public AABB getRenderBoundingBox(EldritchNothingBlockEntity blockEntity) {
      return blockEntity.getRenderBoundingBox();
   }
}
