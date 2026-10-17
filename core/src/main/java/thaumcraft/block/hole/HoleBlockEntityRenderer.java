package thaumcraft.block.hole;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.init.registry.ModBlocks;

public class HoleBlockEntityRenderer implements BlockEntityRenderer<HoleBlockEntity> {
   public static final ResourceLocation TUNNEL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/tunnel.png");
   public static final ResourceLocation FIELD = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particlefield.png");
   public static final ResourceLocation FIELD_32 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particlefield32.png");

   public HoleBlockEntityRenderer(Context ctx) {
   }

   public void render(HoleBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Level level = be.getLevel();
      if (level != null) {
         BlockPos pos = be.getBlockPos();
         double cx = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().x;
         double cy = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().y;
         double cz = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().z;
         double dx = pos.getX() + 0.5 - cx;
         double dy = pos.getY() + 0.5 - cy;
         double dz = pos.getZ() + 0.5 - cz;
         boolean inrange = dx * dx + dy * dy + dz * dz < 512.0;
         poseStack.pushPose();
         poseStack.translate(0.0, 0.0, 0.0);
         Matrix4f mat = poseStack.last().pose();
         Matrix3f nmat = poseStack.last().normal();

         for (Direction dir : Direction.values()) {
            if (shouldRenderFace(level, pos, dir)) {
               if (inrange) {
                  float t = (float)(System.currentTimeMillis() % 700000L) / 250000.0F;
                  VertexConsumer vc0 = buffer.getBuffer(RenderType.entityTranslucent(TUNNEL));
                  emitFace(vc0, mat, nmat, dir, 1.0F, 1.0F, 1.0F, 1.0F, t, 15728880, packedOverlay);
                  VertexConsumer vc1 = buffer.getBuffer(RenderType.entityTranslucent(FIELD));
                  emitFace(vc1, mat, nmat, dir, 0.6F, 0.6F, 0.6F, 1.0F, t * 1.5F, 15728880, packedOverlay);
               } else {
                  VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(FIELD_32));
                  emitFace(vc, mat, nmat, dir, 0.5F, 0.5F, 0.5F, 1.0F, 0.0F, 15728880, packedOverlay);
               }
            }
         }

         poseStack.popPose();
      }
   }

   private static boolean shouldRenderFace(Level level, BlockPos pos, Direction face) {
      BlockPos npos = pos.relative(face);
      BlockState ns = level.getBlockState(npos);
      if (ns.isAir()) {
         return false;
      } else {
         return ns.is((Block)ModBlocks.HOLE.get()) ? false : ns.isSolidRender(level, npos);
      }
   }

   private static void emitFace(
      VertexConsumer vc, Matrix4f mat, Matrix3f nmat, Direction dir, float r, float g, float b, float a, float timeV, int light, int overlay
   ) {
      float epsN = 0.001F;
      float epsP = 0.999F;
      float x0 = 0.0F;
      float x1 = 1.0F;
      float y0 = 0.0F;
      float y1 = 1.0F;
      float z0 = 0.0F;
      float z1 = 1.0F;
      float px = 0.0F;
      float py = 0.0F;
      float pz = 0.0F;
      switch (dir) {
         case UP:
            quadY(vc, mat, nmat, epsP, x0, x1, z0, z1, r, g, b, a, timeV, light, overlay, 0, 1, 0);
            break;
         case DOWN:
            quadY(vc, mat, nmat, epsN, x0, x1, z0, z1, r, g, b, a, timeV, light, overlay, 0, -1, 0);
            break;
         case SOUTH:
            quadZ(vc, mat, nmat, epsP, x0, x1, y0, y1, r, g, b, a, timeV, light, overlay, 0, 0, 1);
            break;
         case NORTH:
            quadZ(vc, mat, nmat, epsN, x0, x1, y0, y1, r, g, b, a, timeV, light, overlay, 0, 0, -1);
            break;
         case EAST:
            quadX(vc, mat, nmat, epsP, z0, z1, y0, y1, r, g, b, a, timeV, light, overlay, 1, 0, 0);
            break;
         case WEST:
            quadX(vc, mat, nmat, epsN, z0, z1, y0, y1, r, g, b, a, timeV, light, overlay, -1, 0, 0);
      }
   }

   private static void v(
      VertexConsumer vc,
      Matrix4f mat,
      Matrix3f nmat,
      float x,
      float y,
      float z,
      float u,
      float v,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay,
      int nx,
      int ny,
      int nz
   ) {
      vc.addVertex(mat, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(overlay).setUv2(light & 65535, light >> 16).setNormal(nx, ny, nz);
   }

   private static void quadY(
      VertexConsumer vc,
      Matrix4f mat,
      Matrix3f nmat,
      float y,
      float x0,
      float x1,
      float z0,
      float z1,
      float r,
      float g,
      float b,
      float a,
      float t,
      int light,
      int overlay,
      int nx,
      int ny,
      int nz
   ) {
      v(vc, mat, nmat, x0, y, z1, 1.0F, 1.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
      v(vc, mat, nmat, x0, y, z0, 1.0F, 0.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
      v(vc, mat, nmat, x1, y, z0, 0.0F, 0.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
      v(vc, mat, nmat, x1, y, z1, 0.0F, 1.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
   }

   private static void quadZ(
      VertexConsumer vc,
      Matrix4f mat,
      Matrix3f nmat,
      float z,
      float x0,
      float x1,
      float y0,
      float y1,
      float r,
      float g,
      float b,
      float a,
      float t,
      int light,
      int overlay,
      int nx,
      int ny,
      int nz
   ) {
      v(vc, mat, nmat, x0, y1, z, 1.0F, 1.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
      v(vc, mat, nmat, x0, y0, z, 1.0F, 0.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
      v(vc, mat, nmat, x1, y0, z, 0.0F, 0.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
      v(vc, mat, nmat, x1, y1, z, 0.0F, 1.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
   }

   private static void quadX(
      VertexConsumer vc,
      Matrix4f mat,
      Matrix3f nmat,
      float x,
      float z0,
      float z1,
      float y0,
      float y1,
      float r,
      float g,
      float b,
      float a,
      float t,
      int light,
      int overlay,
      int nx,
      int ny,
      int nz
   ) {
      v(vc, mat, nmat, x, y1, z0, 1.0F, 1.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
      v(vc, mat, nmat, x, y1, z1, 1.0F, 0.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
      v(vc, mat, nmat, x, y0, z1, 0.0F, 0.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
      v(vc, mat, nmat, x, y0, z0, 0.0F, 1.0F + t, r, g, b, a, light, OverlayTexture.NO_OVERLAY, nx, ny, nz);
   }
}
