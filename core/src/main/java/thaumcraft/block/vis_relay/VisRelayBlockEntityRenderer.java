package thaumcraft.block.vis_relay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.api.visnet.VisNetHandler;

@OnlyIn(Dist.CLIENT)
public class VisRelayBlockEntityRenderer implements BlockEntityRenderer<VisRelayBlockEntity> {
   public static final ResourceLocation CRYSTAL_MODEL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/vis_relay_crystal");
   private final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
   private final ModelBlockRenderer modelRenderer = this.blockRenderer.getModelRenderer();

   public VisRelayBlockEntityRenderer(Context ctx) {
   }

   public void render(VisRelayBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      if (be.getLevel() != null) {
         BlockState state = be.getBlockState();
         float r = 1.0F;
         float g = 1.0F;
         float b = 1.0F;
         if (be.color >= 0 && be.color < VisRelayBlockEntity.colors.length) {
            Color c = new Color(VisRelayBlockEntity.colors[be.color]);
            r = c.getRed() / 200.0F;
            g = c.getGreen() / 200.0F;
            b = c.getBlue() / 200.0F;
         }

         int ticks = 0;
         Entity cam = Minecraft.getInstance().getCameraEntity();
         if (cam != null) {
            ticks = cam.tickCount;
         }

         boolean valid = VisNetHandler.isNodeValid(be.getParent());
         int n5 = (valid ? 50 : 0) + (int)(150.0F * (Mth.sin((ticks + partialTick) / 2.0F) * 0.05F + 0.95F));
         int blockLight = Mth.clamp(n5 / 16, 0, 15);
         int light = LightTexture.pack(blockLight, 0);
         BakedModel model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(CRYSTAL_MODEL));
         pose.pushPose();
         applyFacingOrOrientation(pose, state, be.getFacing());
         VertexConsumer vc = buffer.getBuffer(RenderType.translucent());
         this.modelRenderer.renderModel(pose.last(), vc, state, model, r, g, b, light, packedOverlay);
         pose.popPose();
      }
   }

   private static void applyFacingOrOrientation(PoseStack pose, BlockState state, Direction direction) {
      pose.translate(0.5, 0.5, 0.5);
      switch (direction) {
         case DOWN:
            pose.mulPose(Axis.XP.rotationDegrees(180.0F));
         case UP:
         default:
            break;
         case NORTH:
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            break;
         case SOUTH:
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            break;
         case WEST:
            pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
            break;
         case EAST:
            pose.mulPose(Axis.ZP.rotationDegrees(-90.0F));
      }

      pose.translate(-0.5, -0.5, -0.5);
   }
}
