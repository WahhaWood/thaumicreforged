package thaumcraft.item.wands.foci;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusEnderChest extends ItemFocusBasic {
   private static final AspectList visUsage = new AspectList().add(Aspect.ENTROPY, 100).add(Aspect.ORDER, 100);
   private static final Component CONTAINER_TITLE = Component.translatable("container.enderchest");

   public ItemFocusEnderChest() {
      super("ender_chest");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "ENDERCHEST" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getFocusColor(ItemStack stack) {
      return 1253923;
   }

   @Override
   public AspectList getVisCost(ItemStack stack) {
      return visUsage;
   }

   @Override
   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack stack) {
      return ItemFocusBasic.WandFocusAnimation.WAVE;
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level level, Player player, InteractionHand hand) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      ItemStack focusStack = wand.getFocusItem(wandStack);
      if (wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), !level.isClientSide, false)) {
         // Контейнер открываем только на сервере.
         if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider((id, inv, p) -> ChestMenu.threeRows(id, inv, p.getEnderChestInventory()), CONTAINER_TITLE));
         }

         level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
      }

      return wandStack;
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemStack, int rank) {
      return switch (rank) {
         case 1, 2, 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
         default -> null;
      };
   }
}
