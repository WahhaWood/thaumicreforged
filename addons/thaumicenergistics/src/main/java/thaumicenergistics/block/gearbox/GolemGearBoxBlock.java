package thaumicenergistics.block.gearbox;

import com.mojang.serialization.MapCodec;
import thaumicenergistics.block.gearbox.entity.GearBoxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class GolemGearBoxBlock extends AbstractGearBoxBlock {
   public static final MapCodec<GolemGearBoxBlock> CODEC = simpleCodec(properties -> new GolemGearBoxBlock());

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public GolemGearBoxBlock() {
      super(true);
   }

   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new GearBoxBlockEntity(pos, state);
   }
}
