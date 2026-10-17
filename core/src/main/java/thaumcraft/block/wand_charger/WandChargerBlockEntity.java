package thaumcraft.block.wand_charger;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.block.ThaumcraftEntityBlock;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.item.wands.ItemWandCasting;

public class WandChargerBlockEntity extends ThaumcraftSideInvBlockEntity implements ITickable, MenuProvider {
   private static final List<Aspect> CHARGE_ASPECTS = List.of(Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH, Aspect.ORDER, Aspect.ENTROPY);
   private static final int[] ACCESSIBLE_SLOTS = new int[]{0};
   private static final int DEFAULT_BUFFER = 100;
   private static final int BUFFER_UPGRADE = 50;
   private static final int DEFAULT_TRANSFER = 10;
   private static final int TRANSFER_UPGRADE = 5;
   private static final int DEFAULT_SPEED = 250;
   private static final int SPEED_UPGRADE = 500;
   private static final int CHARGE_PASSES_PER_TICK = 6;
   private static final boolean PIPES_ENABLED = false;
   private final int[] aspectStorage = new int[CHARGE_ASPECTS.size()];

   public WandChargerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.WAND_CHARGER.get(), pos, state, 5);
      this.slotsForFace = ACCESSIBLE_SLOTS;
   }

   public static List<Aspect> getChargeAspects() {
      return CHARGE_ASPECTS;
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.loadChargerData(compound);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.loadChargerData(compound);
   }

   private void loadChargerData(@NotNull CompoundTag compound) {
      for (int index = 0; index < CHARGE_ASPECTS.size(); index++) {
         this.aspectStorage[index] = compound.getInt(getStorageKey(index));
      }
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.saveChargerData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.saveChargerData(compound);
   }

   private void saveChargerData(@NotNull CompoundTag compound) {
      for (int index = 0; index < CHARGE_ASPECTS.size(); index++) {
         compound.putInt(getStorageKey(index), this.aspectStorage[index]);
      }
   }

   @Override
   public void setItem(int index, @NotNull ItemStack stack) {
      super.setItem(index, stack);
      this.markForUpdate();
   }

   @NotNull
   @Override
   public ItemStack removeItem(int index, int amount) {
      ItemStack removed = super.removeItem(index, amount);
      if (!removed.isEmpty()) {
         this.markForUpdate();
      }

      return removed;
   }

   @NotNull
   @Override
   public ItemStack removeItemNoUpdate(int index) {
      ItemStack removed = super.removeItemNoUpdate(index);
      if (!removed.isEmpty()) {
         this.markForUpdate();
         this.setChanged();
      }

      return removed;
   }

   public int getMaxStackSize() {
      return 1;
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else if (index == 0) {
         return this.canChargeWand(stack);
      } else {
         return stack.getItem() instanceof WandChargerItemUpgrade upgrade ? upgrade.getType().getSlotIndex() == index : false;
      }
   }

   @Override
   public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
      return false;
   }

   @Override
   public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
      return false;
   }

   public int getAspectStorage(Aspect aspect) {
      return this.aspectStorage[this.getAspectIndex(aspect)];
   }

   public int getAspectStorage(int index) {
      return this.aspectStorage[index];
   }

   public void setAspectStorage(Aspect aspect, int amount) {
      this.aspectStorage[this.getAspectIndex(aspect)] = amount;
   }

   public void setAspectStorage(int index, int amount) {
      this.aspectStorage[index] = amount;
   }

   public boolean canChargeWand(ItemStack stack) {
      if (!(!stack.isEmpty() && stack.getItem() instanceof ItemWandCasting wandItem)) {
         return false;
      } else if (wandItem.isStaff(stack) && !this.hasUpgrade(WandChargerItemUpgrade.Type.ALLOW_STAFF)) {
         return false;
      } else {
         int maxVis = wandItem.getMaxVis(stack);

         for (Aspect aspect : CHARGE_ASPECTS) {
            if (wandItem.getVis(stack, aspect) < maxVis) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isEnabled() {
      ItemStack stack = this.getItem(0);
      if (this.canChargeWand(stack) && stack.getItem() instanceof ItemWandCasting wandItem) {
         int var6 = wandItem.getMaxVis(stack);

         for (Aspect aspect : CHARGE_ASPECTS) {
            if (wandItem.getVis(stack, aspect) < var6 && this.getAspectStorage(aspect) > 0) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public int getMaxVis() {
      return (100 + (this.hasUpgrade(WandChargerItemUpgrade.Type.VIS_BUFFER) ? 50 : 0)) * 100;
   }

   public ContainerData createAspectStorageData() {
      return new ContainerData() {
         public int get(int index) {
            return WandChargerBlockEntity.this.getAspectStorage(index);
         }

         public void set(int index, int value) {
            WandChargerBlockEntity.this.setAspectStorage(index, value);
         }

         public int getCount() {
            return WandChargerBlockEntity.CHARGE_ASPECTS.size();
         }
      };
   }

   @NotNull
   public Component getDisplayName() {
      return Component.translatable("container.thaumcraft.wand_charger");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inventory, @NotNull Player player) {
      return new WandChargerMenu(windowId, inventory, this);
   }

   public AABB getRenderBoundingBox() {
      return new AABB(
         this.worldPosition.getX(),
         this.worldPosition.getY(),
         this.worldPosition.getZ(),
         this.worldPosition.getX() + 1,
         this.worldPosition.getY() + 2,
         this.worldPosition.getZ() + 1
      );
   }

   private int drainVis(Aspect aspect) {
      return VisNetHandler.drainVis(this.level, this.worldPosition, aspect, 10 + (this.hasUpgrade(WandChargerItemUpgrade.Type.CAP_CHARGE) ? 5 : 0));
   }

   private boolean addVisToWand(ItemStack wandStack, Aspect aspect) {
      if (wandStack.getItem() instanceof ItemWandCasting wandItem) {
         int transferLimit = 250 + (this.hasUpgrade(WandChargerItemUpgrade.Type.SPEED_CHARGE) ? 500 : 0);
         int neededVis = wandItem.getMaxVis(wandStack) - wandItem.getVis(wandStack, aspect);
         int bufferedVis = this.getAspectStorage(aspect);
         int transferredVis = Math.min(Math.min(neededVis, transferLimit), bufferedVis);
         if (transferredVis <= 0) {
            return false;
         } else {
            wandItem.addRealVis(wandStack, aspect, transferredVis, true);
            this.setAspectStorage(aspect, bufferedVis - transferredVis);
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean hasUpgrade(WandChargerItemUpgrade.Type type) {
      ItemStack stack = this.getItem(type.getSlotIndex());
      return stack.getItem() instanceof WandChargerItemUpgrade upgrade && upgrade.getType() == type;
   }

   private void updateActiveState(BlockState state) {
      boolean active = this.isEnabled();
      if ((Boolean)state.getValue(ThaumcraftEntityBlock.ACTIVE) != active) {
         this.level.setBlock(this.worldPosition, (BlockState)state.setValue(ThaumcraftEntityBlock.ACTIVE, active), 2);
      }
   }

   private int getAspectIndex(Aspect aspect) {
      int index = CHARGE_ASPECTS.indexOf(aspect);
      if (index < 0) {
         throw new IllegalArgumentException("Unsupported aspect: " + aspect.getTag());
      } else {
         return index;
      }
   }

   private static String getStorageKey(int index) {
      return CHARGE_ASPECTS.get(index).getTag() + "Vis";
   }
}
