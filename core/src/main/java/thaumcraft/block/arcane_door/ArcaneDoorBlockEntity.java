package thaumcraft.block.arcane_door;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.block.opaque.OwnedBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class ArcaneDoorBlockEntity extends OwnedBlockEntity {
   public ArcaneDoorBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.ARCANE_DOOR.get(), pos, state);
   }
}
