package thaumcraft.item.wands;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.item.focus_pouch.FocusPouchMenu;
import thaumcraft.util.ItemStackData;

public class ItemFocusPouch extends Item implements MenuProvider {
   public ItemFocusPouch() {
      super(new Properties().stacksTo(1).rarity(Rarity.RARE));
   }

   protected ItemFocusPouch(Properties properties) {
      super(properties);
   }

   public boolean isFoil(@NotNull ItemStack stack) {
      return false;
   }

   public boolean onDroppedByPlayer(ItemStack item, Player player) {
      if (player.containerMenu instanceof FocusPouchMenu) {
         player.closeContainer();
      }

      return super.onDroppedByPlayer(item, player);
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      if (player instanceof ServerPlayer serverPlayer && !player.isShiftKeyDown()) {
         serverPlayer.openMenu(this, buf -> buf.writeEnum(hand));
      }

      return super.use(level, player, hand);
   }

   @NotNull
   public Component getDisplayName() {
      return Component.empty();
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowsId, @NotNull Inventory inventory, @NotNull Player player) {
      return new FocusPouchMenu(windowsId, inventory, null);
   }

   public NonNullList<ItemStack> getInventory(ItemStack item) {
      NonNullList<ItemStack> stackList = NonNullList.withSize(18, ItemStack.EMPTY);
      if (ItemStackData.hasTag(item)) {
         ListTag var2 = ItemStackData.getTag(item).getList("Inventory", 10);

         for (int var3 = 0; var3 < var2.size(); var3++) {
            CompoundTag var4 = var2.getCompound(var3);
            int slot = var4.getByte("Slot") & 255;
            if (slot < stackList.size()) {
               stackList.set(slot, ItemStackData.loadStack(var4));
            }
         }
      }

      return stackList;
   }

   public void setInventory(ItemStack item, NonNullList<ItemStack> stackList) {
      ListTag var2x = new ListTag();

      for (int i = 0; i < stackList.size(); i++) {
         ItemStack stack = (ItemStack)stackList.get(i);
         if (!stack.isEmpty()) {
            CompoundTag var4 = new CompoundTag();
            var4.putByte("Slot", (byte)i);
            CompoundTag stackTag = ItemStackData.saveStack(stack);
            stackTag.putByte("Slot", (byte)i);
            var2x.add(stackTag);
         }
      }

      ItemStackData.addTagElement(item, "Inventory", var2x);
   }
}
