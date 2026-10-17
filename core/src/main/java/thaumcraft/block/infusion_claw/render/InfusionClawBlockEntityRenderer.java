package thaumcraft.block.infusion_claw.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.block.infusion_claw.InfusionClawBlockEntity;
import thaumcraft.entities.brainless.projectile.primal_orb.EntityPrimalOrb;

public class InfusionClawBlockEntityRenderer implements BlockEntityRenderer<InfusionClawBlockEntity> {
   public static final ResourceLocation RESOURCE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/infusion_claw.png");
   private static final InfusionClawModel MODEL = InfusionClawModel.create();
   private static final Random RANDOM = new Random();

   public InfusionClawBlockEntityRenderer(Context ctx) {
   }

   public void render(InfusionClawBlockEntity tile, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      r1(tile, partialTick, poseStack, buffer, packedLight, packedOverlay);
   }

   public static void r1(InfusionClawBlockEntity tile, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Level level = tile.getLevel();
      float ticks = (Minecraft.getInstance().getCameraEntity() != null ? Minecraft.getInstance().getCameraEntity().tickCount : 0) + partialTick;
      float elapsed = ticks - tile.lastRenderTick;
      tile.lastRenderTick = ticks;
      poseStack.pushPose();
      poseStack.translate(0.5F, 0.5F, 0.5F);
      renderSides(tile, level, tile.getBlockPos(), elapsed, poseStack, buffer, packedLight);
      renderCircle(tile, elapsed, poseStack, buffer, packedLight);
      renderWand(tile.getItem(0), ticks, poseStack, buffer, packedLight);
      renderPrimalOrb(tile, elapsed, partialTick, poseStack, buffer, packedLight);
      poseStack.popPose();
   }

   private static void renderCircle(InfusionClawBlockEntity tile, float elapsed, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      float speed = (float)Math.pow(1.1, Math.abs(tile.animationStates[9]));
      if (speed > 50.0F) {
         speed = 50.0F;
      }

      if (tile.animationStates[9] > 0.0F) {
         tile.animationStates[9] = (float)(tile.animationStates[9] + 0.5 * elapsed);
         if (tile.animationStates[9] > 100.0F) {
            tile.animationStates[9] = -tile.animationStates[9];
         }
      } else if (tile.animationStates[9] < 0.0F) {
         tile.animationStates[9] = (float)(tile.animationStates[9] + elapsed * 0.5);
         if (tile.animationStates[9] > 0.0F) {
            tile.animationStates[9] = 0.0F;
         }
      }

      for (int i = 0; i < 4; i++) {
         poseStack.pushPose();
         tile.animationStates[10] = (float)(tile.animationStates[10] + speed * elapsed * 0.001 * Math.PI);
         float angle = (float)(tile.animationStates[10] + i * 0.5 * Math.PI);
         float sx = Mth.sin(angle);
         float cz = Mth.cos(angle);
         float radius = 0.25F;
         poseStack.translate(0.0, 0.0625 * Math.sin(angle / 1.4) + 0.0625, 0.0);
         poseStack.translate(radius * sx, -0.27, radius * cz);
         poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
         poseStack.scale(0.75F, 0.75F, 0.75F);
         renderPart(i, poseStack, buffer, packedLight);
         poseStack.popPose();
      }

      poseStack.popPose();
   }

   private static void renderWand(ItemStack wandStack, float ticks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      float rotDeg = ticks / 3.0F / 20.0F * (180.0F / (float)Math.PI);
      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(rotDeg));
      renderTopPart(poseStack, buffer, packedLight);
      if (!wandStack.isEmpty()) {
         poseStack.pushPose();
         ItemStack visual = wandStack.copy();
         poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
         poseStack.scale(0.5F, 0.5F, 0.5F);
         poseStack.translate(0.0F, Mth.sin(ticks / 3.0F / 10.0F) * 0.08F + 0.08F, 0.0F);
         poseStack.translate(0.0F, -0.2924F, 0.0F);
         Minecraft.getInstance()
            .getItemRenderer()
            .renderStatic(visual, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, Minecraft.getInstance().level, 0);
         poseStack.popPose();
      }

      poseStack.popPose();
   }

   private static void renderTopPart(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      poseStack.translate(0.0F, 0.453125F, 0.0F);
      poseStack.scale(0.75F, 0.75F, 0.75F);
      renderPart(4, poseStack, buffer, packedLight);
      poseStack.popPose();
   }

   private static void renderPrimalOrb(
      InfusionClawBlockEntity tile, float elapsed, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight
   ) {
      if (tile.animationStates[11] > 0.0F) {
         tile.animationStates[11] = tile.animationStates[11] + elapsed / 70.0F;
         poseStack.pushPose();
         poseStack.translate(0.0F, 0.75F - tile.animationStates[11], 0.0F);
         poseStack.scale(0.35F, 0.35F, 0.35F);
         if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null) {
            EntityPrimalOrb orb = new EntityPrimalOrb(Minecraft.getInstance().level, Minecraft.getInstance().player, true);
            orb.tickCount = Minecraft.getInstance().getCameraEntity() != null ? Minecraft.getInstance().getCameraEntity().tickCount : 0;
            Minecraft.getInstance().getEntityRenderDispatcher().render(orb, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, buffer, packedLight);
         }

         poseStack.popPose();
         if (0.75F - tile.animationStates[11] < -0.5F) {
            tile.animationStates[11] = 0.0F;
         }
      }
   }

   private static void renderSides(
      InfusionClawBlockEntity tile, Level level, BlockPos pos, float elapsed, PoseStack poseStack, MultiBufferSource buffer, int packedLight
   ) {
      poseStack.pushPose();
      float speed = (float)Math.pow(1.1, Math.abs(tile.animationStates[8]));
      if (tile.animationStates[8] > 0.0F) {
         tile.animationStates[8] = tile.animationStates[8] + elapsed;
         if (tile.animationStates[8] > 50.0F) {
            tile.animationStates[8] = -tile.animationStates[8];
            tile.animationStates[9] = 1.0F;
            createSideZap(tile, pos);
         }
      } else if (tile.animationStates[8] < 0.0F) {
         tile.animationStates[8] = (float)(tile.animationStates[8] + elapsed * 0.5);
         if (tile.animationStates[8] > 0.0F) {
            tile.animationStates[8] = 0.0F;
         }
      }

      for (int i = 0; i < 4; i++) {
         float heightMove = getNextMoveOffset(tile, i, speed, elapsed);
         float widthMove = getNextMoveOffset(tile, i + 4, speed, elapsed);

         Direction dir = switch (i) {
            case 0 -> Direction.NORTH;
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            default -> Direction.EAST;
         };
         boolean powered = level != null ? level.getSignal(pos, dir) > 0 : false;
         poseStack.pushPose();
         poseStack.translate(-dir.getStepZ() * widthMove, heightMove, -dir.getStepX() * widthMove);
         poseStack.translate(0.453125 * dir.getStepX(), 0.0, 0.453125 * dir.getStepZ());
         poseStack.mulPose(
            Axis.YP.rotationDegrees(dir == Direction.NORTH ? 0.0F : (dir == Direction.SOUTH ? 180.0F : (dir == Direction.WEST ? 90.0F : -90.0F)))
         );
         poseStack.scale(0.75F, 0.75F, 0.75F);
         renderPart(powered ? 6 : 5, poseStack, buffer, packedLight);
         poseStack.popPose();
      }

      poseStack.popPose();
   }

   private static float getNextMoveOffset(InfusionClawBlockEntity tile, int id, float speed, float elapsed) {
      tile.animationStates[id] = (float)(tile.animationStates[id] + RANDOM.nextDouble() * 2.0 * elapsed * speed);
      return (float)(0.0625 * Math.sin(tile.animationStates[id] / 30.0F));
   }

   private static void renderPart(int num, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(RESOURCE));
      MODEL.parts[num].render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
   }

   private static void createSideZap(InfusionClawBlockEntity tile, BlockPos pos) {
      Level level = tile.getLevel();
      if (level != null) {
         for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            float sx = 0.5F + 0.5F * dir.getStepX();
            float sz = 0.5F + 0.5F * dir.getStepZ();
            float ex = 0.5F + dir.getStepX() * 0.2F;
            float ez = 0.5F + dir.getStepZ() * 0.2F;
            Thaumcraft.setup
               .nodeBolt(
                  Thaumcraft.setup.getClientWorld(), pos.getX() + sx, pos.getY() + 0.6F, pos.getZ() + sz, pos.getX() + ex, pos.getY() + 0.2F, pos.getZ() + ez
               );
            playZapSound(level, pos);
         }
      }
   }

   private static void playZapSound(Level level, BlockPos pos) {
      SoundEvent zap = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "zap"));
      if (zap != null) {
         level.playLocalSound(
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, zap, SoundSource.BLOCKS, 0.1F, 1.0F + level.random.nextFloat() * 0.2F, false
         );
      }
   }
}
