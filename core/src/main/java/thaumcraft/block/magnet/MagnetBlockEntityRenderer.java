package thaumcraft.block.magnet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.item.armor.hover_harness.HoverHarnessLayer;
import thaumcraft.util.UtilsFX;

public class MagnetBlockEntityRenderer implements BlockEntityRenderer<MagnetBlockEntity> {
   public static boolean mobRenderFallback = false;
   private static final ResourceLocation TEX_MAGNET_S = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/model/magnet_s.png");
   private static final ResourceLocation TEX_MAGNET_N = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/model/magnet_n.png");
   private static final ResourceLocation TEX_MOB_MAGNET_S = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/model/mob_magnet_s.png");
   private static final ResourceLocation TEX_MOB_MAGNET_N = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/model/mob_magnet_n.png");
   private static final ResourceLocation RING_SPRITE = HoverHarnessLayer.RING_SPRITE;
   private final ModelMagnet model;

   public MagnetBlockEntityRenderer(Context ctx) {
      this.model = new ModelMagnet(ctx.bakeLayer(ModelMagnet.LAYER_LOCATION));
   }

   public void render(MagnetBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      BlockState state = be.getBlockState();
      boolean pulling = state.hasProperty(MagnetBlock.PULLING) && (Boolean)state.getValue(MagnetBlock.PULLING);
      Level level = be.getLevel();
      int redstone;
      if (level != null) {
         redstone = 0;

         for (Direction dir : Direction.values()) {
            int s = level.getSignal(be.getBlockPos().relative(dir), dir);
            if (s > redstone) {
               redstone = s;
            }
         }
      } else {
         redstone = 15;
      }

      boolean mob = ((MagnetBlock)state.getBlock()).isMobMagnet();
      ResourceLocation tex = mob ? (pulling ? TEX_MOB_MAGNET_S : TEX_MOB_MAGNET_N) : (pulling ? TEX_MAGNET_S : TEX_MAGNET_N);
      float scale = redstone == 0 ? 0.0F : redstone / 15.0F * 0.7F + 0.4F;
      float r = pulling ? 0.0F : 1.0F;
      float g = 0.0F;
      float b = pulling ? 1.0F : 0.0F;
      TextureAtlasSprite ringSprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(RING_SPRITE);
      int fullBright = 15728880;
      poseStack.pushPose();
      poseStack.translate(0.5F, 1.5F, 0.5F);
      poseStack.scale(1.0F, -1.0F, -1.0F);
      VertexConsumer modelConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
      this.model.renderToBuffer(poseStack, modelConsumer, packedLight, packedOverlay, -1);
      poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
      poseStack.translate(0.0F, 0.0F, -0.6F);
      VertexConsumer ringConsumer = buffer.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
      long now = System.currentTimeMillis();

      for (int i = 0; i < 3; i++) {
         poseStack.pushPose();
         UtilsFX.drawQuadSpriteIconStyle(poseStack, ringConsumer, fullBright, ringSprite, scale, r, g, b, 0.9F);
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
         UtilsFX.drawQuadSpriteIconStyle(poseStack, ringConsumer, fullBright, ringSprite, scale, r, g, b, 0.9F);
         poseStack.popPose();
         float wave = (Mth.cos((float)now / 500.0F) + 1.0F) * 0.09F + 0.1F;
         poseStack.translate(0.0F, 0.0F, -wave);
      }

      poseStack.popPose();
   }
}
