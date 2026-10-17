package thaumcraft.block.arcane_workbench;

import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.item.wands.ItemWandCasting;

public class MagicWorkbenchBlockEntity extends ThaumcraftSideInvBlockEntity implements CraftingContainer, MenuProvider {
   private final WeakHashMap<Player, ArcaneWorkbenchMenu> menus = new WeakHashMap<>();
   private int craftingMutationDepth;

   public MagicWorkbenchBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ARCANE_WORKBENCH.get(), blockPos, blockState, 10);
   }

   public void bindMenu(ArcaneWorkbenchMenu menu) {
      this.menus.put(menu.inventory.player, menu);
   }

   public void unbindMenu(ArcaneWorkbenchMenu menu) {
      this.menus.remove(menu.inventory.player, menu);
   }

   public void setItemNoUpdate(int index, ItemStack stack) {
      if (index >= 0 && index < this.contents.size()) {
         this.contents.set(index, stack);
      }
   }

   public void beginCraftingMutation() {
      this.craftingMutationDepth++;
   }

   public void endCraftingMutation() {
      if (this.craftingMutationDepth > 0) {
         this.craftingMutationDepth--;
      }

      if (this.craftingMutationDepth == 0) {
         this.setChanged();
      }
   }

   public void setChanged() {
      super.setChanged();
      if (this.craftingMutationDepth <= 0) {
         Iterator<Entry<Player, ArcaneWorkbenchMenu>> iterator = this.menus.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<Player, ArcaneWorkbenchMenu> entry = iterator.next();
            Player player = entry.getKey();
            ArcaneWorkbenchMenu menu = entry.getValue();
            if (player.containerMenu == menu && menu.stillValid(player)) {
               menu.slotsChanged(this);
            } else {
               iterator.remove();
            }
         }
      }
   }

   @NotNull
   public ItemStack getStackInRowAndColumn(int x, int y) {
      return x >= 0 && x < 3 && y >= 0 && y < 3 ? this.getItem(x + y * 3) : ItemStack.EMPTY;
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      return index != 9 ? true : stack.getItem() instanceof ItemWandCasting wand && !wand.isStaff(stack);
   }

   @NotNull
   @Override
   public int[] getSlotsForFace(@NotNull Direction direction) {
      return new int[]{9};
   }

   @Override
   public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
      return index == 9 && this.canPlaceItem(index, stack);
   }

   @Override
   public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
      return index == 9;
   }

   public int getWidth() {
      return 3;
   }

   public int getHeight() {
      return 3;
   }

   @NotNull
   public List<ItemStack> getItems() {
      return this.contents;
   }

   public void fillStackedContents(@NotNull StackedContents contents) {
      for (ItemStack stack : this.contents) {
         contents.accountSimpleStack(stack);
      }
   }

   @NotNull
   public Component getDisplayName() {
      return Component.translatable("block.thaumcraft.arcane_workbench");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inventory, @NotNull Player player) {
      return new ArcaneWorkbenchMenu(windowId, inventory, this);
   }
}
