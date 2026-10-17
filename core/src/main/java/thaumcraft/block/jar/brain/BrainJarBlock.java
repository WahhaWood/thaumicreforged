package thaumcraft.block.jar.brain;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.jar.standart.JarBlock;

public class BrainJarBlock extends JarBlock {
   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new BrainJarBlockEntity(pos, state);
   }

   @NotNull
   @Override
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull Builder builder) {
      return List.of(new ItemStack(this));
   }

   @NotNull
   @Override
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      return level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar
         ? ItemInteractionResult.sidedSuccess(level.isClientSide)
         : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
      return 2.0F;
   }

   @Override
   public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
      return true;
   }

   @Override
   public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
      return level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar ? Mth.floor((float)jar.xp / jar.xpMax * 14.0F) + (jar.xp > 0 ? 1 : 0) : 0;
   }

   @Override
   public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar && jar.xp > 0) {
            level.updateNeighbourForOutputSignal(pos, this);
         }

         super.onRemove(state, level, pos, newState, isMoving);
      }
   }

   public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
      if (level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar && jar.xp >= jar.xpMax) {
         double x = pos.getX() + 0.3 + random.nextFloat() * 0.4;
         double y = pos.getY() + 0.9;
         double z = pos.getZ() + 0.3 + random.nextFloat() * 0.4;
         level.addParticle(ParticleTypes.EFFECT, x, y, z, 0.0, 0.4 + random.nextFloat() * 0.1, 0.3 + random.nextFloat() * 0.2);
      }
   }
}
