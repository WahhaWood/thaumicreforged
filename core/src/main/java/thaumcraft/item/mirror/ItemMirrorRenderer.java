package thaumcraft.item.mirror;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.block.mirror.MirrorBlockEntity;
import thaumcraft.block.mirror.MirrorEssenceBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;

public class ItemMirrorRenderer extends BlockEntityWithoutLevelRenderer {
   private final MirrorEssenceBlockEntity dummyEssentiaTile;
   private final BlockState essentiaState = ((Block)ModBlocks.MIRROR_ESSENCE.get()).defaultBlockState();
   private final MirrorBlockEntity dummyMagicTile;
   private final BlockState magicState;

   public ItemMirrorRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
      super(dispatcher, modelSet);
      this.dummyEssentiaTile = new MirrorEssenceBlockEntity(BlockPos.ZERO, this.essentiaState);
      this.magicState = ((Block)ModBlocks.MIRROR.get()).defaultBlockState();
      this.dummyMagicTile = new MirrorBlockEntity(BlockPos.ZERO, this.magicState);
   }

   public void renderByItem(
      ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay
   ) {
      BlockState stateToRender = null;
      BlockEntity tileToRender = null;
      if (stack.getItem() == ModItems.MIRROR_ESSENCE.get()) {
         stateToRender = this.essentiaState;
         this.dummyEssentiaTile.linked = false;
         this.dummyEssentiaTile.instability = 0;
         tileToRender = this.dummyEssentiaTile;
      } else if (stack.getItem() == ModItems.MIRROR.get()) {
         stateToRender = this.magicState;
         this.dummyMagicTile.linked = false;
         this.dummyMagicTile.instability = 0;
         tileToRender = this.dummyMagicTile;
      }

      if (stateToRender != null && tileToRender != null) {
         Minecraft mc = Minecraft.getInstance();
         poseStack.pushPose();
         mc.getBlockRenderer().renderSingleBlock(stateToRender, poseStack, buffer, packedLight, packedOverlay);
         poseStack.popPose();
         poseStack.pushPose();
         mc.getBlockEntityRenderDispatcher().renderItem(tileToRender, poseStack, buffer, packedLight, packedOverlay);
         poseStack.popPose();
      }
   }
}
