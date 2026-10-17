package thaumcraft.block.eccence_reservoir.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import thaumcraft.init.registry.ModBlocks;

public class EssenceReservoirItemRenderer extends BlockEntityWithoutLevelRenderer {
   public EssenceReservoirItemRenderer() {
      super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
   }

   public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      BlockState state = (BlockState)((Block)ModBlocks.ESSENCE_RESERVOIR.get()).defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP);
      Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pose, buffer, packedLight, packedOverlay);
      TileEssenceReservoirRenderer.renderGlass(pose, buffer, 15728880);
   }
}
