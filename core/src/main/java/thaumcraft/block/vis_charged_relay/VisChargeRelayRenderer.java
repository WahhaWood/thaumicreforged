package thaumcraft.block.vis_charged_relay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.block.vis_relay.VisRelayBlockEntity;

@OnlyIn(Dist.CLIENT)
public class VisChargeRelayRenderer implements BlockEntityRenderer<VisChargeRelayBlockEntity> {
   public static final ResourceLocation CRYSTAL_MODEL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/vis_charge_relay_crystal");
   private final ModelBlockRenderer modelRenderer;

   public VisChargeRelayRenderer(Context ctx) {
      BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
      this.modelRenderer = brd.getModelRenderer();
   }

   public void render(VisChargeRelayBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      if (be.getLevel() != null) {
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

         int n5 = (VisNetHandler.isNodeValid(be.getParent()) ? 50 : 0) + (int)(150.0F * (Mth.sin((ticks + partialTick) / 2.0F) * 0.05F + 0.95F));
         int blockLight = Mth.clamp(n5 / 16, 0, 15);
         int light = LightTexture.pack(blockLight, 0);
         BakedModel model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(CRYSTAL_MODEL));
         pose.pushPose();
         VertexConsumer vc = buffer.getBuffer(RenderType.translucent());
         this.modelRenderer.renderModel(pose.last(), vc, be.getBlockState(), model, r, g, b, light, packedOverlay);
         pose.popPose();
      }
   }
}
