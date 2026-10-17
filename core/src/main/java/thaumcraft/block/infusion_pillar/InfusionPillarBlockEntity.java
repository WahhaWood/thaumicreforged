package thaumcraft.block.infusion_pillar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class InfusionPillarBlockEntity extends ThaumcraftSideInvBlockEntity {
   public boolean isUpgraded = false;
   public byte upgradeLevel = 0;

   public InfusionPillarBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.INFUSION_PILLAR.get(), blockPos, blockState, 1);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.savePillarData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.savePillarData(compound);
   }

   private void savePillarData(@NotNull CompoundTag compound) {
      compound.putBoolean("isUpgraded", this.isUpgraded);
      compound.putByte("upgradeLevel", this.upgradeLevel);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.loadPillarData(compound);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.loadPillarData(compound);
   }

   private void loadPillarData(@NotNull CompoundTag compound) {
      this.isUpgraded = compound.getBoolean("isUpgraded");
      this.upgradeLevel = compound.getByte("upgradeLevel");
   }

   public void setChanged() {
      super.setChanged();
   }

   public int getMaxStackSize() {
      return 1;
   }

   /** Block events must return true on the server as well: only then does the server send them to clients. */
   @Override
   public boolean triggerEvent(int id, int data) {
      if (id == 1) {
         // TC4 BlockStoneDevice: the altar has just been built with the wand.
         if (this.level != null && this.level.isClientSide) {
            Thaumcraft.setup.blockSparkle(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), 11960575, 2);
            this.level.levelEvent(2001, this.worldPosition, Block.getId(Blocks.STONE_BRICKS.defaultBlockState()));
         }

         return true;
      } else if (id == 11) {
         if (this.level != null && this.level.isClientSide) {
            for (int a = 0; a < Thaumcraft.setup.particleCount(5); a++) {
               Thaumcraft.setup.blockSparkle(this.level, this.worldPosition.getX(), this.worldPosition.getY() + 1, this.worldPosition.getZ(), 12632319, 2);
            }
         }

         return true;
      } else if (id == 12) {
         if (this.level != null && this.level.isClientSide) {
            for (int a = 0; a < Thaumcraft.setup.particleCount(10); a++) {
               Thaumcraft.setup.blockSparkle(this.level, this.worldPosition.getX(), this.worldPosition.getY() + 1, this.worldPosition.getZ(), -9999, 2);
            }
         }

         return true;
      } else if (id != 13) {
         return super.triggerEvent(id, data);
      } else {
         if (this.level != null && this.level.isClientSide) {
            for (int a = 0; a < Thaumcraft.setup.particleCount(8); a++) {
               Thaumcraft.setup.blockSparkle(this.level, this.worldPosition.getX(), this.worldPosition.getY() + 1, this.worldPosition.getZ(), 16711935, 3);
            }
         }

         return true;
      }
   }
}
