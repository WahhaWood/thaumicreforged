package thaumcraft.block.opaque;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class OwnedBlockEntity extends ThaumcraftBlockEntity {
   public String owner = "";
   public ArrayList<String> accessList = new ArrayList<>();
   public boolean safeToRemove = false;

   public OwnedBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.OWNED.get(), blockPos, blockState);
   }

   public OwnedBlockEntity(BlockEntityType type, BlockPos blockPos, BlockState blockState) {
      super(type, blockPos, blockState);
   }

   public void onLoad() {
      super.onLoad();
      if (this.level != null && this.level.isClientSide) {
         BlockState st = this.getBlockState();
         if (st.getBlock() instanceof WardedGlassBlock) {
            WardedGlassBlock.markCtmDirty(this.level, this.worldPosition);
         }
      }
   }

   @Nullable
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.loadCustom(tag, provider);
      this.owner = tag.getString("owner");
      this.safeToRemove = tag.getBoolean("safeToRemove");
      ListTag list = tag.getList("access", 8);
      this.accessList = new ArrayList<>();

      for (int i = 0; i < list.size(); i++) {
         this.accessList.add(list.getString(i));
      }
   }

   @Override
   public void saveCustom(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.saveCustom(tag, provider);
      tag.putString("owner", this.owner == null ? "" : this.owner);
      tag.putBoolean("safeToRemove", this.safeToRemove);
      ListTag list = new ListTag();

      for (String s : this.accessList) {
         list.add(StringTag.valueOf(s));
      }

      tag.put("access", list);
   }
}
