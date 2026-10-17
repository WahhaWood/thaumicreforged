package thaumcraft.block.glimmer_of_light;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class LightSourceTile extends ThaumcraftBlockEntity implements ITickable {
   private int ticks = 0;

   public LightSourceTile(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.GLIMMER_OF_LIGHT.get(), blockPos, blockState);
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveAdditional(compound, provider);
      compound.putInt("ticks", this.ticks);
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadAdditional(compound, provider);
      this.ticks = compound.getInt("ticks");
   }
}
