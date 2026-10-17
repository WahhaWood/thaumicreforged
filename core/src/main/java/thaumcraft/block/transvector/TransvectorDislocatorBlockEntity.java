package thaumcraft.block.transvector;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ITickable;
import thaumcraft.init.registry.ModBlockEntities;

public class TransvectorDislocatorBlockEntity extends TransvectorBlockEntity implements ITickable {
   private static final String TAG_PULSE_STORED = "pulseStored";
   private static final String TAG_COOLDOWN = "cooldown";
   private static final int DEFAULT_MAX_DISTANCE = 16;
   private static final Set<Block> BLOCKS_BLACKLIST = Set.of(Blocks.REDSTONE_WIRE, Blocks.COMPARATOR, Blocks.REPEATER);
   private int cooldown;
   private boolean pulseStored;

   public TransvectorDislocatorBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.TRANSVECTOR_DISLOCATOR.get(), pos, state);
   }

   @Override
   protected int getMaxDistance() {
      return 16;
   }

   @Override
   protected boolean tileRequiredAtLink() {
      return false;
   }

   @Override
   public void loadCustom(CompoundTag compound) {
      super.loadCustom(compound);
      this.cooldown = compound.getInt("cooldown");
      this.pulseStored = compound.getBoolean("pulseStored");
   }

   @Override
   public void saveCustom(CompoundTag compound) {
      super.saveCustom(compound);
      compound.putInt("cooldown", this.cooldown);
      compound.putBoolean("pulseStored", this.pulseStored);
   }
}
