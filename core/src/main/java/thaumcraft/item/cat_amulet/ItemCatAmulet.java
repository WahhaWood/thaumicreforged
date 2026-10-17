package thaumcraft.item.cat_amulet;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.phys.AABB;
import thaumcraft.util.CuriosSlotUtils;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemCatAmulet extends Item implements ICurioItem {
   public ItemCatAmulet() {
      super(new Properties().stacksTo(1).setNoRepair().rarity(Rarity.RARE));
   }

   public boolean canEquip(SlotContext slotContext, ItemStack stack) {
      return stack.getItem() == this && CuriosSlotUtils.isAmuletSlot(slotContext.identifier());
   }

   public void curioTick(SlotContext slotContext, ItemStack stack) {
      if (!slotContext.entity().level().isClientSide && slotContext.entity().tickCount % 10 == 0) {
         if (slotContext.entity() instanceof Player player) {
            AABB var9 = player.getBoundingBox().inflate(10.0, 4.0, 10.0);

            for (Mob mob : player.level().getEntitiesOfClass(Mob.class, var9)) {
               if (mob instanceof Creeper creeper) {
                  boolean hasGoal = creeper.goalSelector
                     .getAvailableGoals()
                     .stream()
                     .anyMatch(wrapped -> wrapped.getGoal() instanceof ItemCatAmulet.AvoidAmuletBearerGoal);
                  if (!hasGoal) {
                     creeper.goalSelector.addGoal(3, new ItemCatAmulet.AvoidAmuletBearerGoal(creeper));
                  }

                  creeper.setSwellDir(-1);
               }

               if (mob.getTarget() == player) {
                  mob.setTarget(null);
               }
            }
         }
      }
   }

   public static class AvoidAmuletBearerGoal extends AvoidEntityGoal<Player> {
      public AvoidAmuletBearerGoal(Creeper creeper) {
         super(
            creeper,
            Player.class,
            6.0F,
            1.0,
            1.2,
            entity -> entity instanceof Player player
               ? CuriosApi.getCuriosInventory(player)
                  .map(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof ItemCatAmulet).isPresent())
                  .orElse(false)
               : false
         );
      }
   }
}
