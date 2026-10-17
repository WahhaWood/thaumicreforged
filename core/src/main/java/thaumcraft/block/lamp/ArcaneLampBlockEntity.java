package thaumcraft.block.lamp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class ArcaneLampBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   public ArcaneLampBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ARCANE_LAMP.get(), blockPos, blockState);
   }

   private int randomOffset() {
      return this.level.random.nextInt(16) - this.level.random.nextInt(16);
   }
}
