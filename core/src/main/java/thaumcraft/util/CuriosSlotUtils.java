package thaumcraft.util;

public final class CuriosSlotUtils {
   private CuriosSlotUtils() {
   }

   public static boolean isRingSlot(String identifier) {
      return "ring".equals(identifier) || identifier.endsWith(":ring");
   }

   public static boolean isBeltSlot(String identifier) {
      return "belt".equals(identifier) || identifier.endsWith(":belt");
   }

   public static boolean isAmuletSlot(String identifier) {
      return "amulet".equals(identifier) || identifier.endsWith(":amulet");
   }
}
