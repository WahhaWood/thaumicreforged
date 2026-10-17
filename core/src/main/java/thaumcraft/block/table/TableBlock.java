package thaumcraft.block.table;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IScribeTools;
import thaumcraft.api.wands.IWandable;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;

public class TableBlock extends Block implements IWandable {
   public static final BooleanProperty STATE = BooleanProperty.create("state");

   public TableBlock() {
      super(Properties.of().mapColor(MapColor.WOOD).strength(1.0F, 5.0F).noOcclusion().forceSolidOn());
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(STATE, true));
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{STATE});
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(STATE, context.getHorizontalDirection().getAxis() == Axis.Z);
   }

   @NotNull
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState blockState,
      @NotNull Level level,
      @NotNull BlockPos blockPos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult result
   ) {
      if (stack.is((Item)ModItems.INKWELL.get()) || stack.getItem() instanceof IScribeTools) {
         Block blockTable = (Block)ModBlocks.TABLE.get();

         for (Direction value : Direction.values()) {
            if (!value.getAxis().isVertical()) {
               BlockPos pos = blockPos.relative(value);
               BlockState state = level.getBlockState(pos);
               if (state.is(blockTable)) {
                  if (!level.isClientSide) {
                     BlockState mainState = ((Block)ModBlocks.RESEARCH_TABLE.get()).defaultBlockState()
                        .setValue(ResearchTableBlock.MAIN, true)
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, value)
                        .setValue(ResearchTableBlock.HAS_TOOLS, true)
                        .setValue(ResearchTableBlock.HAS_NOTES, false);
                     level.setBlock(blockPos, mainState, 3);

                     BlockState partnerState = ((Block)ModBlocks.RESEARCH_TABLE.get()).defaultBlockState()
                        .setValue(ResearchTableBlock.MAIN, false)
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, value.getOpposite())
                        .setValue(ResearchTableBlock.HAS_TOOLS, false)
                        .setValue(ResearchTableBlock.HAS_NOTES, false);
                     level.setBlock(pos, partnerState, 3);

                     if (level.getBlockEntity(blockPos) instanceof ResearchTableBlockEntity tableBE) {
                        ItemStack toolStack = stack.copy();
                        toolStack.setCount(1);
                        tableBE.setItem(0, toolStack);
                        tableBE.setChanged();
                     }

                     if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                     }
                  }

                  level.playSound(player, blockPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                  return ItemInteractionResult.sidedSuccess(level.isClientSide);
               }
            }
         }
      }

      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @Override
   public int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos blockPos, @NotNull Direction direction
   ) {
      // Passthrough к WandTriggerRegistry: стол превращается в магический
      // верстак событием 8 (WandManager.createArcaneWorkbench). Возврат 0 здесь
      // блокировал бы триггер, ничего не делая.
      return -1;
   }

   @NotNull
   @Override
   public ItemStack onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      return ItemStack.EMPTY;
   }

   @Override
   public void onUsingWandTick(@NotNull ItemStack stack, @NotNull Player player, int count) {
   }

   @Override
   public void onWandStoppedUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player, int count) {
   }
}
