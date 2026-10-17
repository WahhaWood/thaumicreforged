package thaumicenergistics.block.gearbox;

import com.mojang.serialization.MapCodec;
import thaumicenergistics.block.gearbox.entity.GearBoxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class GearBoxBlock extends AbstractGearBoxBlock {
   public static final MapCodec<GearBoxBlock> CODEC = simpleCodec(properties -> new GearBoxBlock());

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public GearBoxBlock() {
      super(false);
   }

   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new GearBoxBlockEntity(pos, state);
   }
}
