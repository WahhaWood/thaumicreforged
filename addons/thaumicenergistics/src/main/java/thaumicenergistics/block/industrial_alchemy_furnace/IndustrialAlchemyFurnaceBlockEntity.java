package thaumicenergistics.block.industrial_alchemy_furnace;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import thaumicenergistics.init.ModBlockEntities;
import thaumicenergistics.menu.IndustrialAlchemyFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXBlockSparkle;
import thaumcraft.recipe.ThaumcraftCraftingManager;

public class IndustrialAlchemyFurnaceBlockEntity extends BlockEntity implements IEssenceTransport, IAspectContainer, MenuProvider {
   private static final int ESSENTIA_CAPACITY = 5000;
   private static final int MAX_ENERGY = 80000;
   private static final int ENERGY_PER_ITEM = 100;
   private static final int ENERGY_PER_TICK_IN = 4000;
   private int syncTimer = 0;
   private final LinkedHashMap<Aspect, Integer> aspectsStorage = new LinkedHashMap<>();
   private int energy;
   private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
      protected void onContentsChanged(int slot) {
         IndustrialAlchemyFurnaceBlockEntity.this.setChanged();
      }

      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
         return IndustrialAlchemyFurnaceBlockEntity.this.getAspects(stack).visSize() > 0;
      }
   };
   protected final ContainerData dataAccess = new ContainerData() {
      public int get(int index) {
         return switch (index) {
            case 0 -> IndustrialAlchemyFurnaceBlockEntity.this.energy;
            case 1 -> IndustrialAlchemyFurnaceBlockEntity.this.getTotalEssentia();
            default -> 0;
         };
      }

      public void set(int index, int value) {
         if (index == 0) {
            IndustrialAlchemyFurnaceBlockEntity.this.energy = value;
         }
      }

      public int getCount() {
         return 2;
      }
   };
   private final IEnergyStorage energyStorage = new IEnergyStorage() {
      public int receiveEnergy(int maxReceive, boolean simulate) {
         if (maxReceive <= 0) {
            return 0;
         } else {
            int received = Math.min(Math.min(maxReceive, 4000), 80000 - IndustrialAlchemyFurnaceBlockEntity.this.energy);
            if (!simulate && received > 0) {
               IndustrialAlchemyFurnaceBlockEntity.this.energy += received;
               IndustrialAlchemyFurnaceBlockEntity.this.setChanged();
            }

            return received;
         }
      }

      public int extractEnergy(int maxExtract, boolean simulate) {
         return 0;
      }

      public int getEnergyStored() {
         return IndustrialAlchemyFurnaceBlockEntity.this.energy;
      }

      public int getMaxEnergyStored() {
         return 80000;
      }

      public boolean canExtract() {
         return false;
      }

      public boolean canReceive() {
         return true;
      }
   };

   public IndustrialAlchemyFurnaceBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.INDUSTRIAL_ALCHEMY_FURNACE.get(), pos, state);
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, IndustrialAlchemyFurnaceBlockEntity furnace) {
      if (level.isClientSide()) {
         return;
      }

      boolean changed = false;
      ItemStack input = furnace.itemHandler.getStackInSlot(0);
      if (!input.isEmpty() && furnace.energy >= ENERGY_PER_ITEM) {
         AspectList tags = ThaumcraftCraftingManager.getObjectTags(input);
         tags = ThaumcraftCraftingManager.getBonusTags(input, tags);
         if (tags != null && tags.visSize() > 0 && furnace.getTotalEssentia() + tags.visSize() <= ESSENTIA_CAPACITY) {
            furnace.energy -= ENERGY_PER_ITEM;
            Aspect firstAspect = null;
            for (java.util.Map.Entry<Aspect, Integer> entry : tags.aspects.entrySet()) {
               if (firstAspect == null) {
                  firstAspect = entry.getKey();
               }

               furnace.aspectsStorage.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }

            input.shrink(1);
            changed = true;
            if (firstAspect != null && level instanceof net.minecraft.server.level.ServerLevel) {
               PacketHandler.sendToAllAround(
                  new PacketFXBlockSparkle(pos, firstAspect.getColor()), level, pos, 24.0
               );
            }
         }
      }

      furnace.syncTimer++;
      if (changed || furnace.syncTimer >= 20) {
         furnace.syncTimer = 0;
         furnace.setChanged();
         furnace.syncToClient();
      }
   }

   private AspectList getAspects(ItemStack input) {
      AspectList al = ThaumcraftCraftingManager.getObjectTags(input);
      return ThaumcraftCraftingManager.getBonusTags(input, al);
   }

   public int getTotalEssentia() {
      return this.aspectsStorage.values().stream().mapToInt(Integer::intValue).sum();
   }

   public void syncToClient() {
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   public Component getDisplayName() {
      return Component.translatable("block.thaumicenergistics.industrial_alchemy_furnace");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
      return new IndustrialAlchemyFurnaceMenu(containerId, inventory, this, this.dataAccess);
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      tag.put("inventory", this.itemHandler.serializeNBT(registries));
      tag.putInt("energy", this.energy);
      this.saveAspectsToNBT(tag);
   }

   protected void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      this.itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
      this.aspectsStorage.clear();
      ListTag aspects = tag.getList("aspects", 10);

      for (int i = 0; i < aspects.size(); i++) {
         CompoundTag aspectData = aspects.getCompound(i);
         Aspect aspect = Aspect.getAspect(aspectData.getString("tag"));
         if (aspect != null) {
            int amount = aspectData.getInt("amount");
            if (amount > 0) {
               this.aspectsStorage.put(aspect, Integer.valueOf(amount));
            }
         }
      }

      this.energy = tag.getInt("energy");
   }

   public CompoundTag getUpdateTag(Provider registries) {
      CompoundTag tag = new CompoundTag();
      this.saveAspectsToNBT(tag);
      return tag;
   }

   private void saveAspectsToNBT(CompoundTag tag) {
      ListTag aspects = new ListTag();
      this.aspectsStorage.forEach((aspect, amount) -> {
         CompoundTag aspectData = new CompoundTag();
         aspectData.putInt("amount", amount);
         aspectData.putString("tag", aspect.getTag());
         aspects.add(aspectData);
      });
      tag.put("aspects", aspects);
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void handleUpdateTag(CompoundTag tag, Provider registries) {
      this.loadAdditional(tag, registries);
   }

   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider registries) {
      super.onDataPacket(net, pkt, registries);
      CompoundTag tag = pkt.getTag();
      if (tag != null) {
         this.loadAdditional(tag, registries);
      }
   }

   @Nullable
   public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
      return side == Direction.DOWN ? this.energyStorage : null;
   }

   @Nullable
   public IItemHandler getItemHandler(@Nullable Direction side) {
      Direction inputSide = ((Direction)this.getBlockState().getValue(IndustrialAlchemyFurnaceBlock.FACING)).getClockWise();
      return side == inputSide ? this.itemHandler : null;
   }

   public IItemHandler getInternalItemHandler() {
      return this.itemHandler;
   }

   public AspectList getAspects() {
      AspectList result = new AspectList();
      this.aspectsStorage.forEach(result::add);
      return result;
   }

   public void setAspects(AspectList aspectList) {
   }

   public boolean doesContainerAccept(Aspect aspect) {
      return false;
   }

   public int addToContainer(Aspect aspect, int amount) {
      return 0;
   }

   public boolean takeFromContainer(Aspect aspect, int amount) {
      Integer stored = this.aspectsStorage.get(aspect);
      if (stored != null && stored >= amount) {
         int remain = stored - amount;
         if (remain == 0) {
            this.aspectsStorage.remove(aspect);
         } else {
            this.aspectsStorage.put(aspect, Integer.valueOf(remain));
         }

         this.setChanged();
         this.syncToClient();
         return true;
      } else {
         return false;
      }
   }

   public boolean takeFromContainer(AspectList aspectList) {
      for (Entry<Aspect, Integer> entry : aspectList.aspects.entrySet()) {
         Integer stored = this.aspectsStorage.get(entry.getKey());
         if (stored == null || stored < entry.getValue()) {
            return false;
         }
      }

      for (Entry<Aspect, Integer> entryx : aspectList.aspects.entrySet()) {
         int remain = this.aspectsStorage.get(entryx.getKey()) - entryx.getValue();
         if (remain == 0) {
            this.aspectsStorage.remove(entryx.getKey());
         } else {
            this.aspectsStorage.put(entryx.getKey(), Integer.valueOf(remain));
         }
      }

      this.setChanged();
      this.syncToClient();
      return true;
   }

   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      Integer stored = this.aspectsStorage.get(aspect);
      return stored != null && stored >= amount;
   }

   public boolean doesContainerContain(AspectList aspectList) {
      for (Entry<Aspect, Integer> entry : aspectList.aspects.entrySet()) {
         Integer stored = this.aspectsStorage.get(entry.getKey());
         if (stored == null || stored < entry.getValue()) {
            return false;
         }
      }

      return true;
   }

   public int containerContains(Aspect aspect) {
      Integer stored = this.aspectsStorage.get(aspect);
      return stored == null ? 0 : stored;
   }

   public boolean isConnectable(Direction direction) {
      return direction != null && direction == ((Direction)this.getBlockState().getValue(IndustrialAlchemyFurnaceBlock.FACING)).getCounterClockWise();
   }

   public boolean canInputFrom(Direction direction) {
      return false;
   }

   public boolean canOutputTo(Direction direction) {
      return this.isConnectable(direction);
   }

   public void setSuction(Aspect aspect, int amount) {
   }

   @Nullable
   public Aspect getSuctionType(@Nullable Direction direction) {
      return null;
   }

   public int getSuctionAmount(@Nullable Direction direction) {
      return 0;
   }

   public int extractEssence(Aspect aspect, int amount, Direction direction) {
      if (!this.isConnectable(direction)) {
         return 0;
      } else {
         Integer stored = this.aspectsStorage.get(aspect);
         if (stored != null && stored > 0) {
            int available = Math.min(stored, amount);
            if (available == stored) {
               this.aspectsStorage.remove(aspect);
            } else {
               this.aspectsStorage.put(aspect, Integer.valueOf(stored - available));
            }

            this.setChanged();
            this.syncToClient();
            return available;
         } else {
            return 0;
         }
      }
   }

   public int injectEssence(Aspect aspect, int amount, Direction direction) {
      return 0;
   }

   @Nullable
   public Aspect getEssenceType(@Nullable Direction direction) {
      return this.isConnectable(direction) && !this.aspectsStorage.isEmpty() ? this.aspectsStorage.keySet().iterator().next() : null;
   }

   public int getEssenceAmount(Direction direction) {
      Aspect type = this.getEssenceType(direction);
      return type == null ? 0 : this.containerContains(type);
   }

   public int getMinimumSuction() {
      return 0;
   }

   public boolean renderExtendedTube() {
      return false;
   }
}
