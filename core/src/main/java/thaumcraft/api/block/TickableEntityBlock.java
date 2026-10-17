package thaumcraft.api.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;

public class TickableEntityBlock extends ThaumcraftEntityBlock {
   public static final MapCodec<TickableEntityBlock> CODEC = simpleCodec(TickableEntityBlock::new);

   public TickableEntityBlock(Properties properties) {
      super(properties);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return null;
   }

   @Override
   protected MapCodec<? extends ThaumcraftEntityBlock> codec() {
      return CODEC;
   }

   @javax.annotation.Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? (lvl, pos, st, be) -> {
         if (be instanceof ITickable t) {
            t.clientTick(lvl, pos, st);
         }
      } : (lvl, pos, st, be) -> {
         if (be instanceof ITickable t) {
            t.serverTick(lvl, pos, st);
         }
      };
   }
}
