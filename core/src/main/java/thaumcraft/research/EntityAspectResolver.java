package thaumcraft.research;

import java.util.Locale;
import java.util.Map;
import java.util.Random;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

final class EntityAspectResolver {
   private static final Map<String, Aspect> SPECIAL_PLAYER_ASPECTS = Map.of("Brain", Aspect.MIND);

   private EntityAspectResolver() {
   }

   static AspectList resolve(Entity entity) {
      return entity instanceof Player player ? resolvePlayerAspects(player) : ThaumcraftApi.getObjectTags(entity);
   }

   static boolean isChildEntity(Entity entity) {
      if (entity instanceof AgeableMob ageableMob) {
         return ageableMob.isBaby();
      } else {
         return entity instanceof Zombie zombie ? zombie.isBaby() : false;
      }
   }

   private static AspectList resolvePlayerAspects(Player player) {
      AspectList aspects = new AspectList().add(Aspect.MAN, 4);
      Aspect specialAspect = SPECIAL_PLAYER_ASPECTS.get(player.getScoreboardName().toLowerCase(Locale.ROOT));
      if (specialAspect != null) {
         return aspects.add(specialAspect, 20);
      } else {
         Random random = new Random(("player_" + player.getScoreboardName()).hashCode());
         Aspect[] availableAspects = Aspect.aspects.values().toArray(new Aspect[0]);
         aspects.add(availableAspects[random.nextInt(availableAspects.length)], 4);
         aspects.add(availableAspects[random.nextInt(availableAspects.length)], 4);
         aspects.add(availableAspects[random.nextInt(availableAspects.length)], 4);
         return aspects;
      }
   }
}
