package thaumcraft.item.mob_aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import thaumcraft.api.aspects.Aspect;

public class NumericAspectHelper {
   private static final List<Aspect> aspects = new ArrayList<>();
   private static final Map<Aspect, Integer> aspectToIdMap = new HashMap<>();
   private static boolean initialized;

   public static synchronized void init() {
      if (!initialized) {
         register(Aspect.WATER);
         register(Aspect.MAN);
         register(Aspect.AIR);
         register(Aspect.FLIGHT);
         register(Aspect.FIRE);
         register(Aspect.MAGIC);
         register(Aspect.UNDEAD);
         register(Aspect.FLESH);
         register(Aspect.BEAST);
         register(Aspect.POISON);
         register(Aspect.EARTH);
         register(Aspect.ELDRITCH);
         register(Aspect.TRAVEL);
         register(Aspect.METAL);
         register(Aspect.SLIME);
         register(Aspect.CLOTH);
         register(Aspect.CROP);
         initialized = true;
      }
   }

   private static void ensureInitialized() {
      if (!initialized) {
         init();
      }
   }

   private static void register(Aspect aspect) {
      if (aspect != null && !aspectToIdMap.containsKey(aspect)) {
         int id = aspects.size();
         aspects.add(aspect);
         aspectToIdMap.put(aspect, id);
      }
   }

   public static int getNumber(Aspect aspect) {
      ensureInitialized();
      return aspectToIdMap.getOrDefault(aspect, -1);
   }

   public static Aspect getAspect(int id) {
      ensureInitialized();
      return id >= 0 && id < aspects.size() ? aspects.get(id) : null;
   }

   public static int getCount() {
      ensureInitialized();
      return aspects.size();
   }

   static {
      init();
   }
}
