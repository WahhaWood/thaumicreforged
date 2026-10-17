package thaumicenergistics.part;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import java.util.List;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.IDigiVisSource;
import thaumicenergistics.init.ModMenuTypes;
import thaumicenergistics.integration.tc.DigiVisSourceData;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.item.wands.ItemWandCasting;

public class PartArcaneTerminal extends AbstractTerminalPart implements IGridTickable {
   public static final ResourceLocation INV_CRAFTING = ThaumicEnergistics.location("arcane_terminal_crafting");
   public static final ResourceLocation INV_WAND = ThaumicEnergistics.location("arcane_terminal_wand");
   public static final ResourceLocation MODEL_OFF = ThaumicEnergistics.location("part/arcane_terminal_off");
   public static final ResourceLocation MODEL_ON = ThaumicEnergistics.location("part/arcane_terminal_on");
   public static final IPartModel MODELS_OFF = new PartModel(new ResourceLocation[]{MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF});
   public static final IPartModel MODELS_ON = new PartModel(new ResourceLocation[]{MODEL_BASE, MODEL_ON, MODEL_STATUS_ON});
   public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(new ResourceLocation[]{MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL});
   private final AppEngInternalInventory wand = new AppEngInternalInventory(this, 1);
   private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory(this, 9);
   private final DigiVisSourceData visSourceInfo = new DigiVisSourceData();

   public PartArcaneTerminal(IPartItem<?> partItem) {
      super(partItem);
      this.getMainNode().setIdlePowerUsage(0.5).addService(IGridTickable.class, this);
   }

   public MenuType<?> getMenuType(Player player) {
      return (MenuType<?>)ModMenuTypes.ARCANE_CRAFTING_TERM.get();
   }

   public IPartModel getStaticModels() {
      return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
   }

   public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
      super.addAdditionalDrops(drops, wrenched);

      for (ItemStack is : this.wand) {
         if (!is.isEmpty()) {
            drops.add(is);
         }
      }

      for (ItemStack isx : this.craftingGrid) {
         if (!isx.isEmpty()) {
            drops.add(isx);
         }
      }
   }

   public void clearContent() {
      super.clearContent();
      this.wand.clear();
      this.craftingGrid.clear();
   }

   public InternalInventory getSubInventory(ResourceLocation id) {
      if (id.equals(INV_WAND)) {
         return this.wand;
      } else {
         return (InternalInventory)(id.equals(INV_CRAFTING) ? this.craftingGrid : super.getSubInventory(id));
      }
   }

   public boolean useStandardMemoryCard() {
      return false;
   }

   public boolean onUseItemOn(ItemStack playerHolding, Player player, InteractionHand hand, Vec3 position) {
      if (playerHolding.isEmpty() || !(playerHolding.getItem() instanceof IMemoryCard memoryCard)) {
         return super.onUseItemOn(playerHolding, player, hand, position);
      } else if (this.getLevel() != null && !this.getLevel().isClientSide()) {
         if (this.visSourceInfo.readFromMemoryCard(playerHolding)) {
            memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
            this.saveChanges();
         } else if (DigiVisSourceData.isMemoryCardBlank(playerHolding)) {
            this.visSourceInfo.clearData();
            memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
            this.saveChanges();
         } else {
            memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
         }

         return true;
      } else {
         return true;
      }
   }

   public void writeToNBT(CompoundTag data, Provider registries) {
      super.writeToNBT(data, registries);
      this.wand.writeToNBT(data, "wand", registries);
      this.craftingGrid.writeToNBT(data, "craftingGrid", registries);
      this.visSourceInfo.writeToNBT(data, "VisInterface");
   }

   public void readFromNBT(CompoundTag data, Provider registries) {
      super.readFromNBT(data, registries);
      this.wand.readFromNBT(data, "wand", registries);
      this.craftingGrid.readFromNBT(data, "craftingGrid", registries);
      this.visSourceInfo.readFromNBT(data, "VisInterface");
   }

   public TickingRequest getTickingRequest(IGridNode node) {
      return new TickingRequest(2, 20, false);
   }

   public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
      if (!this.visSourceInfo.hasSourceData()) {
         return TickRateModulation.IDLE;
      } else {
         ItemStack stack = this.wand.getStackInSlot(0);
         if (!stack.isEmpty() && stack.getItem() instanceof ItemWandCasting wandItem) {
            AspectList neededVis = wandItem.getAspectsWithRoom(stack);
            if (neededVis.size() <= 0) {
               return TickRateModulation.IDLE;
            } else {
               IGrid grid = this.getMainNode().getGrid();
               if (grid == null) {
                  return TickRateModulation.IDLE;
               } else {
                  IDigiVisSource visSource = this.visSourceInfo.tryGetSource(grid);
                  if (visSource == null) {
                     return TickRateModulation.IDLE;
                  } else {
                     for (Aspect vis : neededVis.getAspects()) {
                        int amountToDrain = wandItem.getMaxVis(stack) - wandItem.getVis(stack, vis);
                        int amountDrained = visSource.consumeVis(vis, amountToDrain);
                        if (amountDrained > 0) {
                           wandItem.addRealVis(stack, vis, amountDrained, true);
                        }
                     }

                     return TickRateModulation.URGENT;
                  }
               }
            }
         } else {
            return TickRateModulation.IDLE;
         }
      }
   }
}
