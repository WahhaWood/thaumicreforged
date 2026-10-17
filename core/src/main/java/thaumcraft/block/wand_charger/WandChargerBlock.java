package thaumcraft.block.wand_charger;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class WandChargerBlock extends TickableEntityBlock {
   public WandChargerBlock() {
      super(Properties.of().forceSolidOn().noOcclusion().sound(SoundType.STONE).strength(3.0F, 25.0F));
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(ACTIVE, false));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ACTIVE});
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new WandChargerBlockEntity(pos, state);
   }
}
