package thaumcraft.item.ichor_pouch;

import java.util.UUID;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.item.wands.ItemFocusPouch;
import thaumcraft.util.ItemStackData;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemIchorPouch extends ItemFocusPouch implements MenuProvider, ICurioItem {
   public static final String NBT_INV = "Inventory";
   public static final String NBT_UID = "UID";

   public ItemIchorPouch() {
      super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
   }

   @Override
   public boolean isFoil(@NotNull ItemStack stack) {
      return true;
   }

   @Override
   public boolean onDroppedByPlayer(ItemStack item, Player player) {
      if (player.containerMenu instanceof IchorPouchMenu) {
         player.closeContainer();
      }

      return super.onDroppedByPlayer(item, player);
   }

   @NotNull
   @Override
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!(stack.getItem() instanceof ItemIchorPouch)) {
         return InteractionResultHolder.pass(stack);
      } else if (!level.isClientSide && player instanceof ServerPlayer sp && !player.isShiftKeyDown()) {
         ensureUid(stack);
         MenuProvider provider = new SimpleMenuProvider(
            (windowId, inv, p) -> new IchorPouchMenu(windowId, inv, hand), Component.translatable("container.thaumcraft.ichor_pouch")
         );
         sp.openMenu(provider, buf -> buf.writeEnum(hand));
         return InteractionResultHolder.success(stack);
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   @NotNull
   @Override
   public Component getDisplayName() {
      return Component.translatable("container.thaumcraft.ichor_pouch");
   }

   @Nullable
   @Override
   public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inventory, @NotNull Player player) {
      return new IchorPouchMenu(windowId, inventory, InteractionHand.MAIN_HAND);
   }

   public static void ensureUid(ItemStack stack) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      if (!tag.contains("UID", 8)) {
         tag.putString("UID", UUID.randomUUID().toString());
         ItemStackData.setTag(stack, tag);
      }
   }

   public static String getUid(ItemStack stack) {
      return !ItemStackData.hasTag(stack) ? "" : ItemStackData.getTag(stack).getString("UID");
   }

   @Override
   public NonNullList<ItemStack> getInventory(ItemStack item) {
      NonNullList<ItemStack> stackList = NonNullList.withSize(117, ItemStack.EMPTY);
      if (ItemStackData.hasTag(item)) {
         CompoundTag root = ItemStackData.getTag(item);
         if (root != null && root.contains("Inventory", 9)) {
            ListTag list = root.getList("Inventory", 10);

            for (int i = 0; i < list.size(); i++) {
               CompoundTag tag = list.getCompound(i);
               int slot = tag.getShort("Slot") & '\uffff';
               if (slot >= 0 && slot < stackList.size()) {
                  stackList.set(slot, ItemStackData.loadStack(tag));
               }
            }
         }
      }

      return stackList;
   }

   @Override
   public void setInventory(ItemStack item, NonNullList<ItemStack> stackList) {
      ListTag list = new ListTag();

      for (int i = 0; i < stackList.size(); i++) {
         ItemStack stack = (ItemStack)stackList.get(i);
         if (!stack.isEmpty()) {
            CompoundTag tag = ItemStackData.saveStack(stack);
            tag.putShort("Slot", (short)i);
            list.add(tag);
         }
      }

      CompoundTag tag = ItemStackData.getOrCreateTag(item);
      tag.put("Inventory", list);
      ItemStackData.setTag(item, tag);
   }

   public boolean canEquip(SlotContext slotContext, ItemStack stack) {
      return stack.getItem() == this && "belt".equals(slotContext.identifier());
   }
}
