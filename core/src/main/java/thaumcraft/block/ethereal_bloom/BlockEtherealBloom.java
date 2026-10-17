package thaumcraft.block.ethereal_bloom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.plant.BlockCustomPlant;
import thaumcraft.init.registry.ModBlockEntities;

public final class BlockEtherealBloom extends BlockCustomPlant implements EntityBlock {
   public BlockEtherealBloom() {
      super(BlockCustomPlant.PlantKind.ETHEREAL_BLOOM);
   }

   @NotNull
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new EtherealBloomBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
      return type != ModBlockEntities.ETHEREAL_BLOOM.get()
         ? null
         : (lvl, pos, st, be) -> EtherealBloomBlockEntity.tick(lvl, pos, st, (EtherealBloomBlockEntity)be);
   }
}
