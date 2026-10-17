package thaumcraft.block.advanced_alchemy_furnace;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModBlocks;

public class AdvAlchemyFurnaceTankBlock extends HorizontalDirectionalBlock {
   public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, 9);
   public static final MapCodec<AdvAlchemyFurnaceTankBlock> CODEC = simpleCodec(AdvAlchemyFurnaceTankBlock::new);

   public AdvAlchemyFurnaceTankBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(FILL, 0));
   }

   public AdvAlchemyFurnaceTankBlock() {
      this(Properties.of().forceSolidOn().noOcclusion().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.0F, 17.0F));
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FILL, FACING});
   }

   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return CODEC;
   }

   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState newState, boolean movedByPiston) {
      super.onRemove(state, level, blockPos, newState, movedByPiston);
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      return List.of(new ItemStack((ItemLike)ModBlocks.ARCANE_ALEMBIC.get()));
   }

   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
      return new ItemStack((ItemLike)ModBlocks.ARCANE_ALEMBIC.get());
   }
}
