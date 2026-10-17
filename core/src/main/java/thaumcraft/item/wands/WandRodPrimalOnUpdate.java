package thaumcraft.item.wands;

import java.util.ArrayList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.wands.IWandRodOnUpdate;

public class WandRodPrimalOnUpdate implements IWandRodOnUpdate {
   Aspect aspect;
   ArrayList<Aspect> primals;

   public WandRodPrimalOnUpdate(Aspect aspect) {
      this.aspect = aspect;
   }

   public WandRodPrimalOnUpdate() {
      this.aspect = null;
      this.primals = Aspect.getPrimalAspects();
   }

   @Override
   public void onUpdate(ItemStack itemstack, Player player) {
      if (itemstack.getItem() instanceof ItemWandCasting casting) {
         if (this.aspect != null) {
            if (player.tickCount % 200 == 0 && casting.getVis(itemstack, this.aspect) < casting.getMaxVis(itemstack) / 10) {
               casting.addVis(itemstack, this.aspect, 1, true);
            }
         } else if (player.tickCount % 50 == 0) {
            ArrayList<Aspect> q = new ArrayList<>();

            for (Aspect as : this.primals) {
               if (casting.getVis(itemstack, as) < casting.getMaxVis(itemstack) / 10) {
                  q.add(as);
               }
            }

            if (q.size() > 0) {
               casting.addVis(itemstack, q.get(player.level().random.nextInt(q.size())), 1, true);
            }
         }
      }
   }
}
