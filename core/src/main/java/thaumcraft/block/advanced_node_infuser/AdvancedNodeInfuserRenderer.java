package thaumcraft.block.advanced_node_infuser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.IRevealer;
import thaumcraft.block.airy.aura_node.AuraNodeRenderer;
import thaumcraft.item.relics.ItemThaumometer;
import thaumcraft.util.ItemStackData;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class AdvancedNodeInfuserRenderer implements BlockEntityRenderer<AdvancedNodeInfuserBlockEntity> {
   private static final ResourceLocation NODE_BUBBLE = Thaumcraft.location("textures/misc/node_bubble.png");
   private BakedModel model;
   private BakedModel over;

   public AdvancedNodeInfuserRenderer(Context context) {
   }

   public void render(
      @NotNull AdvancedNodeInfuserBlockEntity tile,
      float partialTick,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource source,
      int brightnessOverride,
      int overlayTexture
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      if (this.model == null) {
         ModelManager manager = minecraft.getModelManager();
         this.model = manager.getModel(Thaumcraft.staticModel("advanced_node_infuser_piston"));
         this.over = manager.getModel(Thaumcraft.staticModel("advanced_node_infuser_piston_over"));
      }

      Entity viewer = minecraft.getCameraEntity();
      int viewerTicks = viewer != null ? viewer.tickCount : 0;
      ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
      VertexConsumer buffer = source.getBuffer(Sheets.translucentCullBlockSheet());
      pose.pushPose();
      pose.translate(0.5F, 0.0F, 0.5F);
      pose.mulPose(Axis.XN.rotationDegrees(90.0F));

      for (int i = 0; i < 4; i++) {
         pose.pushPose();
         pose.mulPose(Axis.ZP.rotationDegrees(90 * i));
         pose.mulPose(Axis.XP.rotationDegrees(90.0F));
         pose.translate(-0.33F, 0.37F, -0.5F);
         pose.mulPose(Axis.ZN.rotationDegrees(45.0F));
         pose.translate(0.0F, tile.count / 100.0F, 0.0F);
         renderer.renderModel(pose.last(), buffer, null, this.model, 1.0F, 1.0F, 1.0F, brightnessOverride, overlayTexture, ModelData.EMPTY, null);
         int glow = 50 + (int)(170.0F * (tile.count / 37.0F * (Mth.sin((viewerTicks + i * 5) / 3.0F) * 0.1F + 0.9F)));
         pose.pushPose();
         pose.scale(1.002F, 1.002F, 1.002F);
         pose.translate(-0.001F, -0.001F, -0.001F);
         this.renderOver(pose.last(), buffer, glow, overlayTexture);
         pose.popPose();
         pose.popPose();
      }

      pose.popPose();
      if (tile.count > 0) {
         pose.pushPose();
         float alpha = Mth.sin(viewerTicks / 8.0F) * 0.1F + 0.5F;
         pose.translate(0.5F, 1.5F, 0.5F);
         pose.scale(1.5F, 1.5F, 1.5F);
         UtilsFX.renderFacingQuad(
            source, pose, NODE_BUBBLE, tile.getX() + 0.5, tile.getY() + 1.5, tile.getZ() + 0.5, 0.9F, tile.count / 37.0F * alpha, 1, 0, partialTick, 16777215
         );
         pose.popPose();
      }

      if (!tile.getJarStack().isEmpty() && ItemStackData.hasTag(tile.getJarStack())) {
         AspectList aspects = tile.getNodeAspects();
         if (aspects == null) {
            aspects = new AspectList();
         }

         if (viewer != null) {
            double viewDistance = 64.0;
            boolean viewNode = false;
            boolean disableDepth = false;
            if (viewer instanceof Player player) {
               ItemStack headStack = player.getInventory().getArmor(3);
               if (headStack.getItem() instanceof IRevealer revealer && revealer.showNodes(headStack, player)) {
                  viewNode = true;
                  disableDepth = true;
               } else if (player.getInventory().getSelected().getItem() instanceof ItemThaumometer
                  && UtilsFX.isVisibleTo(0.44F, viewer, tile.getBlockPos().above())) {
                  viewNode = true;
                  disableDepth = true;
                  viewDistance = 48.0;
               }
            }

            pose.pushPose();
            pose.translate(0.0, 1.0, 0.0);
            AuraNodeRenderer.renderNode(
               viewer,
               pose,
               source,
               viewDistance,
               viewNode,
               disableDepth,
               partialTick,
               tile.getBlockPos().above(),
               aspects,
               tile.getNodeType(),
               tile.getNodeModifier()
            );
            pose.popPose();
         }
      }
   }

   private void renderOver(Pose pose, VertexConsumer consumer, int brightnessOverride, int overlayTexture) {
      RandomSource randomSource = RandomSource.create();

      for (Direction direction : Direction.values()) {
         randomSource.setSeed(42L);
         this.renderQuadList(pose, consumer, this.over.getQuads(null, direction, randomSource, ModelData.EMPTY, null), brightnessOverride, overlayTexture);
      }

      randomSource.setSeed(42L);
      this.renderQuadList(pose, consumer, this.over.getQuads(null, null, randomSource, ModelData.EMPTY, null), brightnessOverride, overlayTexture);
   }

   private void renderQuadList(Pose pose, VertexConsumer consumer, List<BakedQuad> quads, int brightnessOverride, int overlayTexture) {
      float[] diffuse = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
      int[] brightness = new int[]{brightnessOverride, brightnessOverride, brightnessOverride, brightnessOverride};

      for (BakedQuad quad : quads) {
         consumer.putBulkData(pose, quad, diffuse, 1.0F, 1.0F, 1.0F, 1.0F, brightness, overlayTexture, false);
      }
   }

   /** The block entity's own box: NeoForge 21.1 asks the renderer, not the block entity. */
   @Override
   public AABB getRenderBoundingBox(AdvancedNodeInfuserBlockEntity blockEntity) {
      return blockEntity.getRenderBoundingBox();
   }
}
