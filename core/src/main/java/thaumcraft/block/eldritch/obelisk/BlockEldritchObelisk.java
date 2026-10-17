package thaumcraft.block.eldritch.obelisk;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.eldritch.BlockEldritchStructureCoreBase;

public class BlockEldritchObelisk extends BlockEldritchStructureCoreBase {
   public BlockEldritchObelisk() {
      super(Properties.of().strength(50.0F, 20000.0F).sound(SoundType.STONE).noOcclusion().lightLevel(s -> 8));
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new EldritchObeliskBlockEntity(pos, state);
   }

   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock()) {
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }
}
