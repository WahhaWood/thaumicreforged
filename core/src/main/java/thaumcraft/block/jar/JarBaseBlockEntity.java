package thaumcraft.block.jar;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IJarTextureProvider;

public abstract class JarBaseBlockEntity extends BlockEntity implements IJarTextureProvider {
   protected static final Random rand = new Random();
   protected float rotYaw = 0.0F;
   protected float rotYawPrev = 0.0F;
   protected float rotYawTarget = 0.0F;
   protected float bob = 0.0F;

   protected JarBaseBlockEntity(@NotNull BlockEntityType<?> type, @NotNull BlockPos pos, @NotNull BlockState state) {
      super(type, pos, state);
   }

   protected void loadCustom(@NotNull CompoundTag tag) {
   }

   protected void saveCustom(@NotNull CompoundTag tag) {
   }

   protected void loadAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.loadAdditional(tag, provider);
      this.loadCustom(tag);
   }

   protected void saveAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.saveAdditional(tag, provider);
      this.saveCustom(tag);
   }

   @NotNull
   public CompoundTag getUpdateTag(@NotNull Provider provider) {
      CompoundTag tag = super.getUpdateTag(provider);
      this.saveCustom(tag);
      return tag;
   }

   public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.handleUpdateTag(tag, provider);
      this.loadCustom(tag);
   }

   @Override
   public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, Provider provider) {
      CompoundTag tag = pkt.getTag();
      if (tag != null) {
         this.loadCustom(tag);
      }
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void markForUpdate() {
      Level lvl = this.level;
      if (lvl != null) {
         this.setChanged();
         BlockState state = this.getBlockState();
         lvl.sendBlockUpdated(this.worldPosition, state, state, 3);
      }
   }

   @NotNull
   public Direction getHorizontalFacing() {
      BlockState state = this.getBlockState();
      return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? (Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
   }

   @NotNull
   public AABB getRenderBoundingBox() {
      return new AABB(this.worldPosition);
   }

   public float getRenderYaw(float partialTicks) {
      return Mth.lerp(partialTicks, this.rotYawPrev, this.rotYaw);
   }

   public float getRenderBob(float partialTicks) {
      return this.bob;
   }
}
