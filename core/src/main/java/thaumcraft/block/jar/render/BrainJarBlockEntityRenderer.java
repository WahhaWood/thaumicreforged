package thaumcraft.block.jar.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import thaumcraft.block.jar.brain.BrainJarBlockEntity;
import thaumcraft.block.jar.render.model.TCBrainModel;
import thaumcraft.block.jar.render.model.TCJarModel;
import thaumcraft.block.jar.render.util.AtlasSpriteUtil;
import thaumcraft.block.jar.render.util.SpriteUVConsumer;

public class BrainJarBlockEntityRenderer implements BlockEntityRenderer<BrainJarBlockEntity> {
   private final TCJarModel jarModel = TCJarModel.create();
   private final TCBrainModel brainModel = TCBrainModel.create();

   public BrainJarBlockEntityRenderer(Context ctx) {
   }

   public void render(
      @NotNull BrainJarBlockEntity jar,
      float partialTicks,
      @NotNull PoseStack poseStack,
      @NotNull MultiBufferSource buffers,
      int packedLight,
      int packedOverlay
   ) {
      poseStack.pushPose();
      poseStack.translate(0.5, 0.01, 0.5);
      poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
      RenderType rtCutout = RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS);
      VertexConsumer vcCutout = buffers.getBuffer(rtCutout);
      float bob = getBrainBob();
      float yawDeg = jar.getRenderYaw(partialTicks);
      poseStack.pushPose();
      poseStack.translate(0.0, -0.8 + bob, 0.0);
      poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg));
      poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
      poseStack.scale(0.4F, 0.4F, 0.4F);
      TextureAtlasSprite brainSprite = (TextureAtlasSprite)Minecraft.getInstance()
         .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
         .apply(AtlasSpriteUtil.spriteFromTexture(TCBrainModel.TEX_BRAIN));
      this.brainModel.render(poseStack, new SpriteUVConsumer(vcCutout, brainSprite), packedLight, packedOverlay);
      poseStack.popPose();
      flush(buffers, rtCutout);
      RenderType rtTrans = RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);
      VertexConsumer vcTrans = buffers.getBuffer(rtTrans);
      TextureAtlasSprite brineSprite = (TextureAtlasSprite)Minecraft.getInstance()
         .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
         .apply(AtlasSpriteUtil.spriteFromTexture(TCJarModel.TEX_BRINE));
      this.jarModel.renderBrine(poseStack, new SpriteUVConsumer(vcTrans, brineSprite), packedLight, packedOverlay);
      poseStack.popPose();
   }

   private static void flush(MultiBufferSource buffers, RenderType rt) {
      if (buffers instanceof BufferSource bs) {
         bs.endBatch(rt);
      }
   }

   private static float getBrainBob() {
      LocalPlayer p = Minecraft.getInstance().player;
      return p == null ? 0.03F : Mth.sin(p.tickCount / 14.0F) * 0.03F + 0.03F;
   }

   public boolean shouldRenderOffScreen(BrainJarBlockEntity be) {
      return false;
   }
}
