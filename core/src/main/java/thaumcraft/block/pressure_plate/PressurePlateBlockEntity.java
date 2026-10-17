package thaumcraft.block.pressure_plate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.block.opaque.OwnedBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class PressurePlateBlockEntity extends OwnedBlockEntity {
   public PressurePlateBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType)ModBlockEntities.ARCANE_PRESSURE_PLATE.get(), blockPos, blockState);
   }
}
