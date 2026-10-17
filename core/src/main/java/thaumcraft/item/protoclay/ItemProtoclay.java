package thaumcraft.item.protoclay;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import thaumcraft.item.equipment.ichor.ItemIchorAxeAdv;
import thaumcraft.item.equipment.ichor.ItemIchorPickaxeAdv;
import thaumcraft.item.equipment.ichor.ItemIchorShovelAdv;
import thaumcraft.item.equipment.ichor.ItemIchorSwordAdv;

public class ItemProtoclay extends Item {
   public ItemProtoclay() {
      super(new Properties().stacksTo(1).setNoRepair().rarity(Rarity.RARE));
      NeoForge.EVENT_BUS.register(this);
   }

   @SubscribeEvent
   public void onLeftClickBlock(LeftClickBlock event) {
      Player player = event.getEntity();
      if (player != null && !player.level().isClientSide) {
         if (hasProtoclay(player)) {
            Inventory inventory = player.getInventory();
            ItemStack current = inventory.getSelected();
            ItemProtoclay.ToolType currentType = getToolType(current);
            if (currentType != ItemProtoclay.ToolType.NONE && currentType != ItemProtoclay.ToolType.SWORD) {
               BlockState state = player.level().getBlockState(event.getPos());
               ItemProtoclay.ToolType requiredType = getRequiredType(state);
               if (requiredType != ItemProtoclay.ToolType.NONE && requiredType != currentType) {
                  int selected = inventory.selected;

                  for (int i = 0; i < inventory.items.size(); i++) {
                     if (i != selected) {
                        ItemStack inSlot = (ItemStack)inventory.items.get(i);
                        if (getToolType(inSlot) == requiredType) {
                           inventory.items.set(selected, inSlot);
                           inventory.items.set(i, current);
                           inventory.setChanged();
                           event.setCanceled(true);
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean hasProtoclay(Player player) {
      for (ItemStack stack : player.getInventory().items) {
         if (stack.getItem() instanceof ItemProtoclay) {
            return true;
         }
      }

      return false;
   }

   private static ItemProtoclay.ToolType getRequiredType(BlockState state) {
      if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
         return ItemProtoclay.ToolType.PICK;
      } else if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
         return ItemProtoclay.ToolType.SHOVEL;
      } else {
         return state.is(BlockTags.MINEABLE_WITH_AXE) ? ItemProtoclay.ToolType.AXE : ItemProtoclay.ToolType.NONE;
      }
   }

   private static ItemProtoclay.ToolType getToolType(ItemStack stack) {
      if (stack.getItem() instanceof ItemIchorPickaxeAdv) {
         return ItemProtoclay.ToolType.PICK;
      } else if (stack.getItem() instanceof ItemIchorShovelAdv) {
         return ItemProtoclay.ToolType.SHOVEL;
      } else if (stack.getItem() instanceof ItemIchorAxeAdv) {
         return ItemProtoclay.ToolType.AXE;
      } else {
         return stack.getItem() instanceof ItemIchorSwordAdv ? ItemProtoclay.ToolType.SWORD : ItemProtoclay.ToolType.NONE;
      }
   }

   private static enum ToolType {
      PICK,
      SHOVEL,
      AXE,
      SWORD,
      NONE;
   }
}
