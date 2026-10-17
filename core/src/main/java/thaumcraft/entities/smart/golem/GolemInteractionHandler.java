package thaumcraft.entities.smart.golem;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.entities.smart.golem.hook.GolemHooks;
import thaumcraft.item.golem_bell.ItemGolemBell;
import thaumcraft.item.golem_core.ItemGolemCore;
import thaumcraft.item.golem_decoration.ItemGolemDecoration;
import thaumcraft.item.golem_upgrade.ItemGolemUpgrade;

public final class GolemInteractionHandler {
   private GolemInteractionHandler() {
   }

   public static InteractionResult mobInteract(EntityGolemBase golem, Player player, InteractionHand hand) {
      if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      } else {
         ItemStack held = player.getItemInHand(hand);
         if (golem.hasValidCore() && !held.isEmpty() && held.getItem() instanceof ItemGolemBell) {
            return InteractionResult.PASS;
         } else if (!golem.hasValidCore() && !held.isEmpty() && held.getItem() instanceof ItemGolemCore coreItem) {
            // Установка ядра.
            if (!golem.level().isClientSide) {
               thaumcraft.item.golem_core.GolemCore old = golem.getCore();
               golem.setCore(coreItem.getCore());
               golem.setupGolemInventory();
               if (!player.getAbilities().instabuild) {
                  held.shrink(1);
               }

               ItemStack oldStack = ItemGolemCore.getStackByCore(old);
               if (!oldStack.isEmpty()) {
                  golem.spawnAtLocation(oldStack);
               }
            }

            player.swing(hand);
            return InteractionResult.SUCCESS;
         } else if (!held.isEmpty() && held.getItem() instanceof ItemGolemUpgrade upgradeItem) {
            golem.ensureUpgradeCapacity(false);
            int upgradeOrdinal = upgradeItem.getUpgrade().ordinal();

            for (int slot = 0; slot < golem.getUpgradeSlotCount(); slot++) {
               if (golem.getUpgrade(slot) == -1 && golem.getUpgradeAmount(upgradeOrdinal) < 2) {
                  player.swing(hand);
                  return InteractionResult.SUCCESS;
               }
            }

            return InteractionResult.PASS;
         } else {
            return customInteraction(golem, player, hand);
         }
      }
   }

   public static InteractionResult customInteraction(EntityGolemBase golem, Player player, InteractionHand hand) {
      ItemStack held = player.getItemInHand(hand);
      if (!held.isEmpty() && held.getItem() instanceof ItemGolemBell) {
         return InteractionResult.PASS;
      } else if (GolemHooks.hook_CustomInteraction(golem, player, golem.hookHandlerData)) {
         return InteractionResult.CONSUME;
      } else if (!held.isEmpty() && held.getItem() instanceof ItemGolemDecoration decorationItem) {
         player.swing(hand);
         return InteractionResult.CONSUME;
      } else if (!held.isEmpty() && held.getItem() == Items.COOKIE) {
         golem.handleCookieFeeding(player, hand, held);
         return InteractionResult.CONSUME;
      } else if (golem.hasValidCore() && golem.getCoreItem(golem.getCore()).hasGUI()) {
         if (golem.level().isClientSide) {
            return InteractionResult.SUCCESS;
         } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.openMenu(golem, buf -> buf.writeInt(golem.getId()));
            return InteractionResult.CONSUME;
         }
         return InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }
}
