package thaumcraft.entities.smart.golem;

import thaumcraft.item.golem_core.GolemCore;
import thaumcraft.item.golem_upgrade.GolemUpgrade;

public final class GolemInventoryBehaviors {
   private GolemInventoryBehaviors() {
   }

   public static void configureInventory(EntityGolemBase golem) {
      if (golem.getCore() == GolemCore.LIQUID) {
         golem.fluidHandler.setCapacity(golem.getFluidCarryLimit());
      }
   }

   public static int getInventorySize(EntityGolemBase golem) {
      return switch (golem.getCore()) {
         case FILL, EMPTY, GATHER, USE, SORTING -> 6 + golem.getUpgradeAmount(GolemUpgrade.FIRE.ordinal()) * 6;
         case LIQUID -> 1 + golem.getUpgradeAmount(GolemUpgrade.FIRE.ordinal());
         default -> 0;
      };
   }
}
