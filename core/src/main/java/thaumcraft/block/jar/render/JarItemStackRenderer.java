package thaumcraft.block.jar.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.block.airy.aura_node.AuraNodeRenderer;
import thaumcraft.block.jar.render.model.TCBrainModel;
import thaumcraft.block.jar.render.model.TCJarModel;
import thaumcraft.block.jar.render.util.AtlasSpriteUtil;
import thaumcraft.block.jar.render.util.JarRenderUtil;
import thaumcraft.block.jar.render.util.SpriteUVConsumer;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.util.ItemStackData;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public final class JarItemStackRenderer extends BlockEntityWithoutLevelRenderer {
   public static final ResourceLocation TEX_LABEL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/label.png");
   private static final float ITEM_SCALE = 0.65F;
   private static final float LIQUID_EPS = 0.00125F;
   private final TCJarModel jarModel = TCJarModel.create();
   private final TCBrainModel brainModel = TCBrainModel.create();

   public JarItemStackRenderer() {
      super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
   }

   public void renderByItem(
      @NotNull ItemStack stack,
      @NotNull ItemDisplayContext ctx,
      @NotNull PoseStack poseStack,
      @NotNull MultiBufferSource buffers,
      int packedLight,
      int packedOverlay
   ) {
      if (stack.getItem() instanceof BlockItem bi) {
         Block var9 = bi.getBlock();
         poseStack.pushPose();
         poseStack.translate(0.5, 0.5, 0.5);
         if (ctx != ItemDisplayContext.GUI && ctx != ItemDisplayContext.FIXED && ctx != ItemDisplayContext.GROUND) {
            poseStack.scale(0.65F, 0.65F, 0.65F);
         } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
            poseStack.scale(0.65F, 0.65F, 0.65F);
         }

         poseStack.translate(-0.5, -0.5, -0.5);
         if (var9 == ModBlocks.BRAIN_JAR.get()) {
            this.renderBrainJar(stack, poseStack, buffers, packedLight, packedOverlay);
         } else if (var9 == ModBlocks.NODE_JAR.get()) {
            this.renderNodeJar(stack, var9, poseStack, buffers, packedLight, packedOverlay);
         } else {
            this.renderStandardJar(stack, var9, poseStack, buffers, packedLight, packedOverlay);
         }

         poseStack.popPose();
      }
   }

   private void renderStandardJar(ItemStack stack, Block block, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
      JarItemStackRenderer.JarFill fillData = getJarFillData(stack);
      if (fillData != null) {
         float fillLevel = Mth.clamp(fillData.amount / 64.0F, 0.0F, 1.0F);
         float y1 = 0.0625F + 0.625F * fillLevel;
         float x1i = 0.25125F;
         float z1i = 0.25125F;
         float x2i = 0.74875F;
         float z2i = 0.74875F;
         JarRenderUtil.renderFluidBox(
            poseStack, buffers, x1i, 0.0625F, z1i, x2i, y1, z2i, fillData.aspect.getColor(), 0.75F, 15728880, OverlayTexture.NO_OVERLAY
         );
      }

      poseStack.pushPose();
      poseStack.translate(0.5, 0.01, 0.5);
      poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      Aspect filter = getFilter(stack);
      if (filter != null) {
         this.renderLabel(filter, poseStack, buffers, packedLight, packedOverlay);
      }

      poseStack.popPose();
      this.renderJarShell(block, stack, poseStack, buffers, packedLight, packedOverlay);
   }

   private void renderBrainJar(ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
      RenderType rtAtlas = RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);
      VertexConsumer vcAtlas = buffers.getBuffer(rtAtlas);
      poseStack.pushPose();
      poseStack.translate(0.5, 0.01, 0.5);
      poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      TextureAtlasSprite brineSprite = (TextureAtlasSprite)Minecraft.getInstance()
         .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
         .apply(AtlasSpriteUtil.spriteFromTexture(TCJarModel.TEX_BRINE));
      this.jarModel.renderBrine(poseStack, new SpriteUVConsumer(vcAtlas, brineSprite), packedLight, packedOverlay);
      float bob = 0.03F;
      if (Minecraft.getInstance().player != null) {
         bob += Mth.sin(Minecraft.getInstance().player.tickCount / 14.0F) * 0.03F;
      }

      poseStack.pushPose();
      poseStack.translate(0.0, -0.8 + bob, 0.0);
      poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
      poseStack.scale(0.4F, 0.4F, 0.4F);
      TextureAtlasSprite brainSprite = (TextureAtlasSprite)Minecraft.getInstance()
         .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
         .apply(AtlasSpriteUtil.spriteFromTexture(TCBrainModel.TEX_BRAIN));
      this.brainModel.render(poseStack, new SpriteUVConsumer(vcAtlas, brainSprite), packedLight, packedOverlay);
      poseStack.popPose();
      poseStack.popPose();
      this.renderJarShell((Block)ModBlocks.BRAIN_JAR.get(), stack, poseStack, buffers, packedLight, packedOverlay);
   }

   private void renderNodeJar(ItemStack stack, Block block, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
      AspectList aspects = getAspects(stack);
      NodeType type = NodeType.NORMAL;
      NodeModifier mod = null;
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null) {
         if (tag.contains("type")) {
            int t = tag.getByte("type");
            if (t >= 0 && t < NodeType.values().length) {
               type = NodeType.values()[t];
            }
         }

         if (tag.contains("modifier")) {
            int m = tag.getByte("modifier");
            if (m >= 0 && m < NodeModifier.values().length) {
               mod = NodeModifier.values()[m];
            }
         }
      }

      if (aspects != null && aspects.size() > 0) {
         AuraNodeRenderer.renderNode(Minecraft.getInstance().player, poseStack, buffers, 64.0, true, false, 0.7F, BlockPos.ZERO, aspects, type, mod);
      }

      this.renderJarShell(block, stack, poseStack, buffers, packedLight, packedOverlay);
   }

   private void renderJarShell(Block block, ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
      BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState());
      ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
      boolean rendered = false;

      for (RenderType renderType : model.getRenderTypes(stack, false)) {
         VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffers, renderType, true, stack.hasFoil());
         itemRenderer.renderModelLists(model, stack, packedLight, packedOverlay, poseStack, vertexConsumer);
         rendered = true;
      }

      if (!rendered) {
         VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffers, RenderType.translucent(), true, stack.hasFoil());
         itemRenderer.renderModelLists(model, stack, packedLight, packedOverlay, poseStack, vertexConsumer);
      }
   }

   private void renderLabel(Aspect filter, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
      RenderType rtLabel = RenderType.entityTranslucent(TEX_LABEL);
      VertexConsumer vcLabel = buffers.getBuffer(rtLabel);
      poseStack.pushPose();
      poseStack.translate(0.0, -0.4, 0.315);
      JarRenderUtil.quad(
         poseStack,
         vcLabel,
         -0.25F,
         -0.25F,
         0.0F,
         0.25F,
         0.25F,
         0.0F,
         0.0F,
         0.0F,
         1.0F,
         1.0F,
         packedLight,
         packedOverlay,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         0.0F,
         0.0F,
         1.0F
      );
      poseStack.popPose();
      poseStack.pushPose();
      poseStack.translate(0.0, -0.4, 0.316);
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      UtilsFX.renderQuadCenteredFromTextureLevelFlat(buffers, poseStack, filter.getImage(), 0.336F, 0.1F, 0.1F, 0.1F, DestFactor.ONE_MINUS_SRC_ALPHA, 0.8F);
      poseStack.popPose();
   }

   @Nullable
   private static JarItemStackRenderer.JarFill getJarFillData(ItemStack stack) {
      AspectList aspects = getAspects(stack);
      if (aspects != null && aspects.size() > 0) {
         Aspect aspect = aspects.getAspects()[0];
         return new JarItemStackRenderer.JarFill(aspect, aspects.getAmount(aspect));
      } else {
         return null;
      }
   }

   @Nullable
   private static Aspect getFilter(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag != null && tag.contains("AspectFilter") ? Aspect.getAspect(tag.getString("AspectFilter")) : null;
   }

   @Nullable
   private static AspectList getAspects(ItemStack stack) {
      return stack.getItem() instanceof IEssenceContainerItem container ? container.getAspects(stack) : null;
   }

   private record JarFill(Aspect aspect, int amount) {
   }
}
