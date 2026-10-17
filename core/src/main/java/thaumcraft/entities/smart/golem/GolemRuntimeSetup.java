package thaumcraft.entities.smart.golem;

import java.util.Objects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.pathfinder.PathType;
import thaumcraft.item.golem_decoration.GolemDecorationType;
import thaumcraft.item.golem_upgrade.GolemUpgrade;

public final class GolemRuntimeSetup {
   private GolemRuntimeSetup() {
   }

   public static void applyMovementAndAttributes(EntityGolemBase golem) {
      EnumGolemType golemType = golem.level().isClientSide ? golem.getGolemType() : golem.golemType;
      boolean heavy = golemType == EnumGolemType.STONE || golemType == EnumGolemType.IRON || golemType == EnumGolemType.THAUMIUM;
      golem.setPathfindingMalus(PathType.WATER, heavy ? 0.0F : 8.0F);
      int bonusHealth = golem.hasDecoration(GolemDecorationType.TOP_HAT) ? 5 : 0;
      if (golem.getAttribute(Attributes.MAX_HEALTH) != null) {
         Objects.requireNonNull(golem.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(golemType.health + bonusHealth);
      }

      int damage = 2 + golem.getGolemStrength() + golem.getUpgradeAmount(GolemUpgrade.EARTH.ordinal());
      if (golem.hasDecoration(GolemDecorationType.MACE)) {
         damage += 2;
      }

      if (golem.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
         Objects.requireNonNull(golem.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(damage);
      }

      int armor = golem.getTotalArmorValue();
      if (golem.getAttribute(Attributes.ARMOR) != null) {
         Objects.requireNonNull(golem.getAttribute(Attributes.ARMOR)).setBaseValue(armor);
      }
   }
}
