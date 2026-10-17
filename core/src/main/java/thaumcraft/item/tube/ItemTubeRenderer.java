package thaumcraft.item.tube;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import thaumcraft.block.tube.TubeSide;
import thaumcraft.block.tube.buffer.TubeBufferBlockEntity;
import thaumcraft.block.tube.oneway.TubeOnewayBlockEntity;
import thaumcraft.block.tube.valve.TubeValveBlockEntity;
import thaumcraft.init.registry.ModBlocks;

public class ItemTubeRenderer extends BlockEntityWithoutLevelRenderer {
   private static final float INV_SCALE = 0.85F;
   private static final Map<Block, BlockState> STATE_CACHE = new HashMap<>();
   private TubeValveBlockEntity dummyValve;
   private TubeOnewayBlockEntity dummyOneway;
   private TubeBufferBlockEntity dummyBuffer;

   public ItemTubeRenderer() {
      super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
   }

   public void renderByItem(
      @NotNull ItemStack stack,
      @NotNull ItemDisplayContext ctx,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource buffers,
      int packedLight,
      int packedOverlay
   ) {
      if (stack.getItem() instanceof BlockItem bi) {
         Block var11 = bi.getBlock();
         BlockState state = STATE_CACHE.computeIfAbsent(var11, ItemTubeRenderer::buildInventoryTubeState);
         pose.pushPose();
         applyInventoryPose(ctx, pose);
         Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pose, buffers, packedLight, packedOverlay);
         Object beToRender = null;
         if (var11 == ModBlocks.ESSENCE_VALVE.get()) {
            if (this.dummyValve == null) {
               this.dummyValve = new TubeValveBlockEntity(BlockPos.ZERO, state);
            }

            if (this.dummyValve.getBlockState() != state) {
               this.dummyValve.setBlockState(state);
            }

            beToRender = this.dummyValve;
         } else if (var11 == ModBlocks.DIRECTIONAL_ESSENCE_TUBE.get()) {
            if (this.dummyOneway == null) {
               this.dummyOneway = new TubeOnewayBlockEntity(BlockPos.ZERO, state);
            }

            if (this.dummyOneway.getBlockState() != state) {
               this.dummyOneway.setBlockState(state);
            }

            beToRender = this.dummyOneway;
         } else if (var11 == ModBlocks.ESSENCE_BUFFER.get()) {
            if (this.dummyBuffer == null) {
               this.dummyBuffer = new TubeBufferBlockEntity(BlockPos.ZERO, state);
            }

            if (this.dummyBuffer.getBlockState() != state) {
               this.dummyBuffer.setBlockState(state);
            }

            beToRender = this.dummyBuffer;
         }

         if (beToRender != null) {
            Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem((BlockEntity)beToRender, pose, buffers, packedLight, packedOverlay);
         }

         pose.popPose();
      }
   }

   private static void applyInventoryPose(ItemDisplayContext ctx, PoseStack pose) {
      if (ctx == ItemDisplayContext.GUI || ctx == ItemDisplayContext.FIXED) {
         pose.translate(0.5F, 0.5F, 0.5F);
         pose.mulPose(Axis.XP.rotationDegrees(30.0F));
         pose.mulPose(Axis.YP.rotationDegrees(225.0F));
         pose.scale(0.85F, 0.85F, 0.85F);
         pose.translate(-0.5F, -0.5F, -0.5F);
      }
   }

   private static BlockState buildInventoryTubeState(Block block) {
      BlockState s = block.defaultBlockState();
      s = applyFacingIfPresent(s, Direction.EAST);
      s = setBoolByName(s, "has_any", true);
      s = setTubeSide(s, "up", TubeSide.NORMAL);
      s = setTubeSide(s, "down", TubeSide.NORMAL);
      s = setTubeSide(s, "north", TubeSide.NONE);
      s = setTubeSide(s, "south", TubeSide.NONE);
      s = setTubeSide(s, "west", TubeSide.NONE);
      return setTubeSide(s, "east", TubeSide.NONE);
   }

   private static BlockState applyFacingIfPresent(BlockState state, Direction dir) {
      if (state.hasProperty(BlockStateProperties.FACING)) {
         return (BlockState)state.setValue(BlockStateProperties.FACING, dir);
      } else {
         return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? (BlockState)state.setValue(BlockStateProperties.HORIZONTAL_FACING, dir) : state;
      }
   }

   private static BlockState setTubeSide(BlockState state, String name, TubeSide value) {
      for (Property<?> p : state.getProperties()) {
         if (p.getName().equals(name) && p instanceof EnumProperty<?> ep && ep.getValueClass() == TubeSide.class) {
            return (BlockState)state.setValue((net.minecraft.world.level.block.state.properties.Property) p, value);
         }
      }

      return state;
   }

   private static BlockState setBoolByName(BlockState state, String name, boolean value) {
      for (Property<?> p : state.getProperties()) {
         if (p.getName().equals(name) && p instanceof BooleanProperty bp) {
            return (BlockState)state.setValue(bp, value);
         }
      }

      return state;
   }
}
