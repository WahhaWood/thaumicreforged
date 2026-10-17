package thaumcraft.block.eldritch.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.eldritch.BlockEldritchStructureCoreBase;
import thaumcraft.init.registry.ModDimensions;
import thaumcraft.init.registry.ModItems;

public class BlockEldritchAltar extends BlockEldritchStructureCoreBase {
   public static final BooleanProperty INERT = BooleanProperty.create("inert");
   public static final BooleanProperty OUTER = BooleanProperty.create("outer");

   public BlockEldritchAltar() {
      super(
         Properties.of()
            .strength(50.0F, 20000.0F)
            .sound(SoundType.STONE)
            .noOcclusion()
            .isSuffocating((s, g, p) -> false)
            .isViewBlocking((s, g, p) -> false)
            .isValidSpawn((s, g, p, e) -> false)
            .lightLevel(s -> 8)
      );
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(INERT, false)).setValue(OUTER, false));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{INERT, OUTER});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = super.getStateForPlacement(context);
      if (state == null) {
         state = this.defaultBlockState();
      }

      boolean isOuterLands = context.getLevel().dimension().equals(ModDimensions.OUTER_LANDS);
      return (BlockState)state.setValue(OUTER, isOuterLands);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new EldritchAltarBlockEntity(pos, state);
   }

   @NotNull
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      if (!player.isShiftKeyDown()
         && !stack.isEmpty()
         && stack.getItem().equals(ModItems.ELDRITCH_EYE.get())
         && level.getBlockEntity(pos) instanceof EldritchAltarBlockEntity altar
         && altar.getEyes() < 4
         && !(Boolean)state.getValue(INERT)) {
         if (!level.isClientSide) {
            // Порядок как в BlockEldritch (1.7.10): сначала решается, спавнить ли
            // культистов, потом увеличивается счётчик глаз, потом заказывается
            // лабиринт. Условие >= 2 проверяется до увеличения.
            if (altar.getEyes() >= 2) {
               altar.setSpawner(true);
               altar.setSpawnType((byte)1);
            }

            altar.setEyes((byte)(altar.getEyes() + 1));
            // Каждый установленный глаз запускает сборку лабиринта вокруг алтаря.
            altar.checkForMaze();
            altar.setChanged();
            altar.markForUpdate();
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }

            level.playSound(
               null, pos, net.minecraft.sounds.SoundEvents.END_PORTAL_FRAME_FILL,
               net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F
            );
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }
}
