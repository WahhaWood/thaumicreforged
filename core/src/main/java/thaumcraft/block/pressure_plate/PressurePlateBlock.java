package thaumcraft.block.pressure_plate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.init.registry.Config;

public class PressurePlateBlock extends net.minecraft.world.level.block.PressurePlateBlock implements EntityBlock {
   private static final EnumProperty<PressurePlateBlock.TargetType> TYPE = EnumProperty.create("type", PressurePlateBlock.TargetType.class);

   public PressurePlateBlock() {
      super(BlockSetType.IRON, Properties.of().randomTicks().strength(Config.wardedStone ? -1.0F : 2.0F, 999.0F).pushReaction(PushReaction.IGNORE));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(TYPE, PressurePlateBlock.TargetType.ALL));
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{TYPE});
   }

   public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
   }

   @NotNull
   public InteractionResult useWithoutItem(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull BlockHitResult hitResult
   ) {
      if (level.getBlockEntity(blockPos) instanceof PressurePlateBlockEntity plate) {
         ;
      }

      return InteractionResult.PASS;
   }

   public void setPlacedBy(
      @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @Nullable LivingEntity entity, @NotNull ItemStack stack
   ) {
      if (entity instanceof ServerPlayer player && level.getBlockEntity(blockPos) instanceof PressurePlateBlockEntity plate) {
         Object var10000 = null;
      }

      super.setPlacedBy(level, blockPos, blockState, entity, stack);
   }

   public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
      return true;
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new PressurePlateBlockEntity(blockPos, blockState);
   }

   public static enum TargetType implements StringRepresentable {
      ALL,
      EXCEPT,
      YOU;

      public PressurePlateBlock.TargetType next() {
         return switch (this) {
            case ALL -> EXCEPT;
            case EXCEPT -> YOU;
            case YOU -> ALL;
         };
      }

      @NotNull
      public String getSerializedName() {
         return this.name().toLowerCase();
      }
   }
}
