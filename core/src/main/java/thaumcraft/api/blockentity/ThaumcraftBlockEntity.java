package thaumcraft.api.blockentity;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.block.ThaumcraftEntityBlock;

public class ThaumcraftBlockEntity extends BlockEntity {
   public ThaumcraftBlockEntity(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
      super(type, blockPos, blockState);
   }

   public void loadAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadAdditional(compound, provider);
      this.loadCustom(compound, provider);
   }

   public void loadCustom(@NotNull CompoundTag compound) {
   }

   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      this.loadCustom(compound);
   }

   protected void saveAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveAdditional(compound, provider);
      this.saveCustom(compound, provider);
   }

   public void saveCustom(@NotNull CompoundTag compound) {
   }

   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      this.saveCustom(compound);
   }

   @OnlyIn(Dist.CLIENT)
   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider provider) {
      CompoundTag compound = pkt.getTag();
      if (compound != null) {
         this.loadCustom(compound, provider);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull Provider provider) {
      this.loadCustom(tag, provider);
   }

   @NotNull
   @Override
   public CompoundTag getUpdateTag(@NotNull Provider provider) {
      CompoundTag tag = super.getUpdateTag(provider);
      this.saveCustom(tag, provider);
      return tag;
   }

   @org.jetbrains.annotations.Nullable
   @Override
   public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public Direction getHorizontalFacing() {
      return (Direction)this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
   }

   public Direction getFacing() {
      return (Direction)this.getBlockState().getValue(BlockStateProperties.FACING);
   }

   public void setFacing(Direction direction) {
      this.getBlockState().setValue(BlockStateProperties.FACING, direction);
   }

   public boolean iaActive() {
      return (Boolean)this.getBlockState().getValue(ThaumcraftEntityBlock.ACTIVE);
   }

   public void markForUpdate() {
      this.setChanged();
      if (this.level != null) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   public int getX() {
      return this.worldPosition.getX();
   }

   public int getY() {
      return this.worldPosition.getY();
   }

   public int getZ() {
      return this.worldPosition.getZ();
   }

   public boolean cantBreak(@Nonnull BlockPos blockPos) {
      return false;
   }

   public final boolean cantAttack(@Nonnull Entity target) {
      return false;
   }
}
