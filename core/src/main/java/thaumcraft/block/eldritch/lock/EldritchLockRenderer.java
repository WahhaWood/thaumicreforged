package thaumcraft.block.eldritch.lock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import thaumcraft.init.registry.ModItems;
import thaumcraft.util.ThaumcraftRenderTypes;

public class EldritchLockRenderer implements BlockEntityRenderer<EldritchLockBlockEntity> {
   private final ModelCube model = new ModelCube(0);

   public EldritchLockRenderer(Context ctx) {
   }

   public void render(EldritchLockBlockEntity lock, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Direction dir = lock.renderFacing();

      if (dir != null) {
         double dx = lock.getBlockPos().getX() + 0.5;
         double dy = lock.getBlockPos().getY() + 0.5;
         double dz = lock.getBlockPos().getZ() + 0.5;
         Entity cam = Minecraft.getInstance().getCameraEntity();
         boolean inRange = cam != null && cam.distanceToSqr(dx, dy, dz) < 512.0;
         float t = (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0) + partialTick;
         this.renderCubeColumns(lock, dir, t, poseStack, buffer, packedLight);
         if (lock.count >= 0) {
            this.renderItem(lock, dir, poseStack, buffer, packedLight);
         }

         this.renderPortalPlane(lock, dir, poseStack, buffer, inRange);
      }
   }

   private void renderCubeColumns(EldritchLockBlockEntity lock, Direction dir, float t, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);
      VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(ThaumcraftRenderTypes.ELD_CUBE));
      float ax = dir.getStepX();
      float ay = dir.getStepY();
      float az = dir.getStepZ();

      for (int i = 0; i < 4; i++) {
         poseStack.pushPose();
         poseStack.mulPose(new Quaternionf().rotateAxis((float)Math.toRadians(90.0F * i), ax, ay, az));
         int limit = 5 - (lock.count + i * 5) / 20;

         for (int j = 1; j < limit; j++) {
            poseStack.pushPose();
            poseStack.translate(0.0, 0.25 + 0.5 * j, 0.0);
            float wobble = Mth.sin((t + j * 10.0F + i * 20.0F) / 20.0F) * 0.1F;
            if (j == 1 || j == 4) {
               wobble = wobble / 2.0F + 0.2F;
            }

            poseStack.scale(0.5F + wobble, 0.5F, 0.5F + wobble);
            this.model.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
         }

         poseStack.popPose();
      }

      poseStack.popPose();
   }

   private void renderItem(EldritchLockBlockEntity lock, Direction dir, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      poseStack.translate(0.5 + dir.getStepX() * 0.525, 0.5, 0.5 + dir.getStepZ() * 0.525);
      float rotY = 0.0F;
      switch (dir) {
         case EAST:
            rotY = 90.0F;
            break;
         case WEST:
            rotY = 270.0F;
            break;
         case NORTH:
            rotY = 180.0F;
      }

      poseStack.mulPose(new Quaternionf().rotateY((float)Math.toRadians(rotY)));
      ItemStack stack = new ItemStack((ItemLike)ModItems.RUNE_TABLET.get());
      float scale = 0.5F;
      poseStack.scale(scale, scale, scale);
      Minecraft.getInstance()
         .getItemRenderer()
         .renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, lock.getLevel(), 0);
      poseStack.popPose();
   }

   private void renderPortalPlane(EldritchLockBlockEntity lock, Direction dir, PoseStack poseStack, MultiBufferSource buffer, boolean inRange) {
      poseStack.pushPose();
      if (inRange) {
         VertexConsumer vc = buffer.getBuffer(ThaumcraftRenderTypes.eldritchLockPortal());
         this.emitPortalQuad(dir, poseStack, vc);
      } else {
         VertexConsumer vc = buffer.getBuffer(ThaumcraftRenderTypes.eldritchLockPortalFar());
         this.emitPortalQuadFar(dir, poseStack, vc, 15728880);
      }

      poseStack.popPose();
   }

   private void emitPortalQuad(Direction dir, PoseStack poseStack, VertexConsumer vc) {
      Matrix4f m = poseStack.last().pose();
      if (dir != Direction.NORTH && dir != Direction.SOUTH) {
         float x = 0.5F;
         vc.addVertex(m, x, 3.0F, -2.0F);
         vc.addVertex(m, x, 3.0F, 3.0F);
         vc.addVertex(m, x, -2.0F, 3.0F);
         vc.addVertex(m, x, -2.0F, -2.0F);
      } else {
         float z = 0.5F;
         vc.addVertex(m, -2.0F, 3.0F, z);
         vc.addVertex(m, -2.0F, -2.0F, z);
         vc.addVertex(m, 3.0F, -2.0F, z);
         vc.addVertex(m, 3.0F, 3.0F, z);
      }
   }

   private void emitPortalQuadFar(Direction dir, PoseStack poseStack, VertexConsumer vc, int light) {
      Matrix4f m = poseStack.last().pose();
      float r = 0.5F;
      float g = 0.5F;
      float b = 0.5F;
      float a = 1.0F;
      if (dir != Direction.NORTH && dir != Direction.SOUTH) {
         float x = 0.5F;
         vc.addVertex(m, x, 3.0F, -2.0F).setColor(r, g, b, a).setUv(0.0F, 1.0F).setUv2(light & 65535, light >> 16);
         vc.addVertex(m, x, 3.0F, 3.0F).setColor(r, g, b, a).setUv(0.0F, 0.0F).setUv2(light & 65535, light >> 16);
         vc.addVertex(m, x, -2.0F, 3.0F).setColor(r, g, b, a).setUv(1.0F, 0.0F).setUv2(light & 65535, light >> 16);
         vc.addVertex(m, x, -2.0F, -2.0F).setColor(r, g, b, a).setUv(1.0F, 1.0F).setUv2(light & 65535, light >> 16);
      } else {
         float z = 0.5F;
         vc.addVertex(m, -2.0F, 3.0F, z).setColor(r, g, b, a).setUv(0.0F, 1.0F).setUv2(light & 65535, light >> 16);
         vc.addVertex(m, -2.0F, -2.0F, z).setColor(r, g, b, a).setUv(0.0F, 0.0F).setUv2(light & 65535, light >> 16);
         vc.addVertex(m, 3.0F, -2.0F, z).setColor(r, g, b, a).setUv(1.0F, 0.0F).setUv2(light & 65535, light >> 16);
         vc.addVertex(m, 3.0F, 3.0F, z).setColor(r, g, b, a).setUv(1.0F, 1.0F).setUv2(light & 65535, light >> 16);
      }
   }

   /** The block entity's own box: NeoForge 21.1 asks the renderer, not the block entity. */
   @Override
   public AABB getRenderBoundingBox(EldritchLockBlockEntity blockEntity) {
      return blockEntity.getRenderBoundingBox();
   }
}
