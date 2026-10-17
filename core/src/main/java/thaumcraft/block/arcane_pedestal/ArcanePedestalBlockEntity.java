package thaumcraft.block.arcane_pedestal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class ArcanePedestalBlockEntity extends ThaumcraftSideInvBlockEntity {
   public ArcanePedestalBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ARCANE_PEDESTAL.get(), blockPos, blockState, 1);
   }

   public void setChanged() {
      super.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public int getMaxStackSize() {
      return 1;
   }

   public void setInfusionOutput(ItemStack stack) {
      ItemStack output = stack.copy();
      if (!output.isEmpty() && output.getCount() > output.getMaxStackSize()) {
         output.setCount(output.getMaxStackSize());
      }

      this.contents.set(0, output);
      this.setChanged();
   }

   /** Block events must return true on the server as well: only then does the server send them to clients. */
   @Override
   public boolean triggerEvent(int i, int j) {
      if (i == 11) {
         if (this.level.isClientSide) {
            for (int a = 0; a < Thaumcraft.setup.particleCount(5); a++) {
               Thaumcraft.setup.blockSparkle(this.level, this.worldPosition.getX(), this.worldPosition.getY() + 1, this.worldPosition.getZ(), 12583104, 2);
            }
         }

         return true;
      } else if (i != 12) {
         return super.triggerEvent(i, j);
      } else {
         if (this.level.isClientSide) {
            // Завершение крафта — фиолетовые искры (в тон вспышке матрицы).
            for (int a = 0; a < Thaumcraft.setup.particleCount(10); a++) {
               Thaumcraft.setup.blockSparkle(this.level, this.worldPosition.getX(), this.worldPosition.getY() + 1, this.worldPosition.getZ(), 13369599, 2);
            }
         }

         return true;
      }
   }
}
