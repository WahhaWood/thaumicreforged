package thaumcraft.block.infusion_claw.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import thaumcraft.block.infusion_claw.InfusionClawBlockEntity;
import thaumcraft.init.registry.ModBlocks;

public class InfusionClawItemRenderer extends BlockEntityWithoutLevelRenderer {
   private InfusionClawBlockEntity dummyTile;

   public InfusionClawItemRenderer() {
      super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
   }

   public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      if (this.dummyTile == null) {
         this.dummyTile = new InfusionClawBlockEntity(BlockPos.ZERO, ((Block)ModBlocks.INFUSION_CLAW.get()).defaultBlockState());
      }

      InfusionClawBlockEntityRenderer.r1(this.dummyTile, 0.0F, poseStack, buffer, packedLight, packedOverlay);
   }
}
