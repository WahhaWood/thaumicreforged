package thaumcraft.block.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.init.registry.ModBlockEntities;

public class PlatformBlockEntity extends BlockEntity {
   private BlockState mimic = Blocks.AIR.defaultBlockState();

   public PlatformBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.PLATFORM.get(), pos, state);
   }

   public BlockState getMimic() {
      return this.mimic;
   }

   public void setMimic(BlockState mimic) {
      this.mimic = sanitizeMimic(mimic);
      this.setChanged();
      if (this.level != null) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
      }
   }

   private static BlockState sanitizeMimic(BlockState state) {
      return state != null && !state.isAir() && !(state.getBlock() instanceof PlatformBlock) ? state : Blocks.AIR.defaultBlockState();
   }

   public void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      if (tag.contains("mimic", 10)) {
         this.mimic = sanitizeMimic(NbtUtils.readBlockState(provider.lookupOrThrow(Registries.BLOCK), tag.getCompound("mimic")));
      } else {
         this.mimic = Blocks.AIR.defaultBlockState();
      }
   }

   protected void saveAdditional(CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      if (!this.mimic.isAir()) {
         tag.put("mimic", NbtUtils.writeBlockState(this.mimic));
      }
   }

   public CompoundTag getUpdateTag(Provider provider) {
      CompoundTag tag = super.getUpdateTag(provider);
      this.saveAdditional(tag, provider);
      return tag;
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider provider) {
      super.onDataPacket(net, pkt, provider);
      this.refreshModel();
   }

   public void handleUpdateTag(CompoundTag tag, Provider provider) {
      this.loadAdditional(tag, provider);
      this.refreshModel();
   }

   private void refreshModel() {
      this.requestModelDataUpdate();
      if (this.level != null) {
         BlockState state = this.getBlockState();
         this.level.sendBlockUpdated(this.worldPosition, state, state, 2);
      }
   }
}
