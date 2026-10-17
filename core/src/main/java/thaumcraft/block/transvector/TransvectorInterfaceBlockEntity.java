package thaumcraft.block.transvector;

import java.util.ArrayList;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class TransvectorInterfaceBlockEntity extends TransvectorBlockEntity implements ITickable, WorldlyContainer, IAspectContainer, IEssenceTransport {
   private static final int DEFAULT_MAX_DISTANCE = 16;
   private boolean propagatingChanged;

   public TransvectorInterfaceBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.TRANSVECTOR_INTERFACE.get(), pos, state);
   }

   @Override
   protected int getMaxDistance() {
      return 16;
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (level.getGameTime() % 100L == 0L) {
         level.updateNeighborsAt(pos, state.getBlock());
      }
   }

   public void setChanged() {
      super.setChanged();
      if (!this.propagatingChanged) {
         BlockEntity linked = this.getDelegatedBlockEntity();
         if (linked != null && linked != this) {
            this.propagatingChanged = true;

            try {
               linked.setChanged();
            } finally {
               this.propagatingChanged = false;
            }
         }
      }
   }

   @NotNull
   public IItemHandler getItemHandler(@Nullable Direction side) {
      BlockEntity linked = this.getDelegatedBlockEntity();
      if (linked == null) {
         return (IItemHandler)(side == null ? new InvWrapper(this) : new SidedInvWrapper(this, side));
      } else if (linked instanceof ThaumcraftSideInvBlockEntity inv) {
         return inv.getItemHandler(side);
      } else if (linked instanceof WorldlyContainer container) {
         return new SidedInvWrapper(container, side);
      } else if (linked instanceof Container container) {
         return new InvWrapper(container);
      } else {
         return (IItemHandler)(side == null ? new InvWrapper(this) : new SidedInvWrapper(this, side));
      }
   }

   public int getContainerSize() {
      Container container = this.getDelegatedContainer();
      return container != null ? container.getContainerSize() : 0;
   }

   public boolean isEmpty() {
      Container container = this.getDelegatedContainer();
      return container == null || container.isEmpty();
   }

   @NotNull
   public ItemStack getItem(int slot) {
      Container container = this.getDelegatedContainer();
      return container != null ? container.getItem(slot) : ItemStack.EMPTY;
   }

   @NotNull
   public ItemStack removeItem(int slot, int amount) {
      Container container = this.getDelegatedContainer();
      return container != null ? container.removeItem(slot, amount) : ItemStack.EMPTY;
   }

   @NotNull
   public ItemStack removeItemNoUpdate(int slot) {
      Container container = this.getDelegatedContainer();
      return container != null ? container.removeItemNoUpdate(slot) : ItemStack.EMPTY;
   }

   public void setItem(int slot, @NotNull ItemStack stack) {
      Container container = this.getDelegatedContainer();
      if (container != null) {
         container.setItem(slot, stack);
      }
   }

   public boolean stillValid(@NotNull Player player) {
      Container container = this.getDelegatedContainer();
      return container != null && container.stillValid(player);
   }

   public void clearContent() {
      Container container = this.getDelegatedContainer();
      if (container != null) {
         container.clearContent();
      }
   }

   @NotNull
   public int[] getSlotsForFace(@NotNull Direction direction) {
      WorldlyContainer worldlyContainer = this.getDelegatedWorldlyContainer();
      if (worldlyContainer != null) {
         return worldlyContainer.getSlotsForFace(direction);
      } else {
         Container container = this.getDelegatedContainer();
         if (container == null) {
            return new int[0];
         } else {
            int[] slots = new int[container.getContainerSize()];
            int i = 0;

            while (i < slots.length) {
               slots[i] = i++;
            }

            return slots;
         }
      }
   }

   public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, @Nullable Direction direction) {
      WorldlyContainer worldlyContainer = this.getDelegatedWorldlyContainer();
      return worldlyContainer != null ? worldlyContainer.canPlaceItemThroughFace(slot, stack, direction) : this.getDelegatedContainer() != null;
   }

   public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, Direction direction) {
      WorldlyContainer worldlyContainer = this.getDelegatedWorldlyContainer();
      return worldlyContainer != null ? worldlyContainer.canTakeItemThroughFace(slot, stack, direction) : this.getDelegatedContainer() != null;
   }

   public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
      Container container = this.getDelegatedContainer();
      return container != null && container.canPlaceItem(slot, stack);
   }

   @Nullable
   @Override
   public AspectList getAspects() {
      IAspectContainer aspectContainer = this.getDelegatedAspectContainer();
      return aspectContainer != null ? aspectContainer.getAspects() : null;
   }

   @Override
   public void setAspects(AspectList aspectList) {
      IAspectContainer aspectContainer = this.getDelegatedAspectContainer();
      if (aspectContainer != null) {
         aspectContainer.setAspects(aspectList);
      }
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      IAspectContainer aspectContainer = this.getDelegatedAspectContainer();
      return aspectContainer != null && aspectContainer.doesContainerAccept(aspect);
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      IAspectContainer aspectContainer = this.getDelegatedAspectContainer();
      return aspectContainer != null ? aspectContainer.addToContainer(aspect, amount) : 0;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      IAspectContainer aspectContainer = this.getDelegatedAspectContainer();
      return aspectContainer != null && aspectContainer.takeFromContainer(aspect, amount);
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      IAspectContainer aspectContainer = this.getDelegatedAspectContainer();
      return aspectContainer != null && aspectContainer.takeFromContainer(aspectList);
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      IAspectContainer aspectContainer = this.getDelegatedAspectContainer();
      return aspectContainer != null && aspectContainer.doesContainerContainAmount(aspect, amount);
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      IAspectContainer aspectContainer = this.getDelegatedAspectContainer();
      return aspectContainer != null && aspectContainer.doesContainerContain(aspectList);
   }

   @Override
   public int containerContains(Aspect aspect) {
      IAspectContainer aspectContainer = this.getDelegatedAspectContainer();
      return aspectContainer != null ? aspectContainer.containerContains(aspect) : 0;
   }

   @Override
   public boolean isConnectable(Direction direction) {
      return true;
   }

   @Override
   public boolean canInputFrom(Direction direction) {
      IEssenceTransport transport = this.getDelegatedEssenceTransport();
      return transport != null && transport.canInputFrom(direction);
   }

   @Override
   public boolean canOutputTo(Direction direction) {
      IEssenceTransport transport = this.getDelegatedEssenceTransport();
      return transport != null && transport.canOutputTo(direction);
   }

   @Override
   public void setSuction(Aspect aspect, int amount) {
      IEssenceTransport transport = this.getDelegatedEssenceTransport();
      if (transport != null) {
         transport.setSuction(aspect, amount);
      }
   }

   @Override
   public Aspect getSuctionType(@Nullable Direction direction) {
      IEssenceTransport transport = this.getDelegatedEssenceTransport();
      return transport != null ? transport.getSuctionType(direction) : null;
   }

   @Override
   public int getSuctionAmount(@Nullable Direction direction) {
      IEssenceTransport transport = this.getDelegatedEssenceTransport();
      return transport != null ? transport.getSuctionAmount(direction) : 0;
   }

   @Override
   public int extractEssence(Aspect aspect, int amount, Direction direction) {
      IEssenceTransport transport = this.getDelegatedEssenceTransport();
      return transport != null ? transport.extractEssence(aspect, amount, direction) : 0;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction direction) {
      IEssenceTransport transport = this.getDelegatedEssenceTransport();
      return transport != null ? transport.injectEssence(aspect, amount, direction) : 0;
   }

   @Override
   public Aspect getEssenceType(@Nullable Direction direction) {
      IEssenceTransport transport = this.getDelegatedEssenceTransport();
      return transport != null ? transport.getEssenceType(direction) : null;
   }

   @Override
   public int getEssenceAmount(Direction direction) {
      IEssenceTransport transport = this.getDelegatedEssenceTransport();
      return transport != null ? transport.getEssenceAmount(direction) : 0;
   }

   @Override
   public int getMinimumSuction() {
      IEssenceTransport transport = this.getDelegatedEssenceTransport();
      return transport != null ? transport.getMinimumSuction() : 0;
   }

   @Override
   public boolean renderExtendedTube() {
      IEssenceTransport transport = this.getDelegatedEssenceTransport();
      return transport != null && transport.renderExtendedTube();
   }

   @Nullable
   private Container getDelegatedContainer() {
      return this.getDelegatedBlockEntity() instanceof Container container ? container : null;
   }

   @Nullable
   private WorldlyContainer getDelegatedWorldlyContainer() {
      return this.getDelegatedBlockEntity() instanceof WorldlyContainer container ? container : null;
   }

   @Nullable
   private IAspectContainer getDelegatedAspectContainer() {
      return this.getDelegatedBlockEntity() instanceof IAspectContainer container ? container : null;
   }

   @Nullable
   private IEssenceTransport getDelegatedEssenceTransport() {
      return this.getDelegatedBlockEntity() instanceof IEssenceTransport transport ? transport : null;
   }

   @Nullable
   private BlockEntity getDelegatedBlockEntity() {
      ArrayList<TransvectorBlockEntity> traversed = new ArrayList<>();
      HashSet<BlockPos> visited = new HashSet<>();
      TransvectorInterfaceBlockEntity current = this;

      while (true) {
         traversed.add(current);
         if (!visited.add(current.worldPosition)) {
            this.clearTargetChain(traversed);
            return null;
         }

         LinkTargetResult result = current.lookupDirectTarget();
         if (result.state().shouldClearTarget()) {
            this.clearTargetChain(traversed);
            return null;
         }

         if (result.state() != LinkTargetState.RESOLVED) {
            return null;
         }

         BlockEntity linked = result.blockEntity();
         if (!(linked instanceof TransvectorInterfaceBlockEntity next)) {
            return linked;
         }

         if (visited.contains(next.worldPosition) || traversed.size() >= 32) {
            this.clearTargetChain(traversed);
            return null;
         }

         current = next;
      }
   }
}
