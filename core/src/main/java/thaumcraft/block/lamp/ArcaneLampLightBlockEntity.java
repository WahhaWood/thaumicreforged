package thaumcraft.block.lamp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.init.registry.ModBlockEntities;

public class ArcaneLampLightBlockEntity extends BlockEntity {
   private BlockPos sourcePos = null;
   private int count = 0;

   public ArcaneLampLightBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.ARCANE_LAMP_LIGHT.get(), pos, state);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, ArcaneLampLightBlockEntity blockEntity) {
      if (!level.isClientSide) {
         if (blockEntity.count == 0) {
            blockEntity.count = level.random.nextInt(100);
         }

         if (++blockEntity.count % 100 == 0) {
            if (blockEntity.sourcePos == null) {
               level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
               return;
            }

            BlockEntity sourceTile = level.getBlockEntity(blockEntity.sourcePos);
            if (!(sourceTile instanceof ArcaneLampBlockEntity)) {
               level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
         }
      }
   }

   public void setSourcePos(BlockPos pos) {
      this.sourcePos = pos;
      this.setChanged();
   }

   public void loadAdditional(CompoundTag compound, Provider provider) {
      super.loadAdditional(compound, provider);
      if (compound.contains("sourceX")) {
         int x = compound.getInt("sourceX");
         int y = compound.getInt("sourceY");
         int z = compound.getInt("sourceZ");
         if (x != Integer.MAX_VALUE) {
            this.sourcePos = new BlockPos(x, y, z);
         }
      }
   }

   protected void saveAdditional(CompoundTag compound, Provider provider) {
      super.saveAdditional(compound, provider);
      if (this.sourcePos != null) {
         compound.putInt("sourceX", this.sourcePos.getX());
         compound.putInt("sourceY", this.sourcePos.getY());
         compound.putInt("sourceZ", this.sourcePos.getZ());
      } else {
         compound.putInt("sourceX", Integer.MAX_VALUE);
         compound.putInt("sourceY", Integer.MAX_VALUE);
         compound.putInt("sourceZ", Integer.MAX_VALUE);
      }
   }
}
