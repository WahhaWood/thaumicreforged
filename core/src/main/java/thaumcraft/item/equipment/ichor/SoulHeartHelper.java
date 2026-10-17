package thaumcraft.item.equipment.ichor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.playerdata.PacketSyncSoulHearts;

/**
 * «Душевные сердца» пробуждённого ихориевого меча (порт {@code SoulHeartHandler} из TT2).
 * Каждое сердце поглощает 1 единицу урона, максимум 20.
 */
public final class SoulHeartHelper {
   private static final String ROOT = "ThaumicTinkerer";
   private static final String TAG_HP = "soulHearts";
   public static final int MAX_HP = 20;

   private SoulHeartHelper() {
   }

   private static CompoundTag compound(Player player) {
      CompoundTag root = player.getPersistentData();
      if (!root.contains(ROOT, 10)) {
         root.put(ROOT, new CompoundTag());
      }

      return root.getCompound(ROOT);
   }

   public static int getHP(Player player) {
      return compound(player).getInt(TAG_HP);
   }

   public static void setHP(Player player, int hp) {
      compound(player).putInt(TAG_HP, Math.max(0, Math.min(MAX_HP, hp)));
   }

   public static boolean addHearts(Player player) {
      return addHP(player, 1);
   }

   public static boolean addHP(Player player, int hp) {
      int current = getHP(player);
      if (current >= MAX_HP) {
         return false;
      }

      setHP(player, current + hp);
      updateClient(player);
      return true;
   }

   public static int removeHP(Player player, int hp) {
      int current = getHP(player);
      int newHp = current - hp;
      setHP(player, newHp);
      updateClient(player);
      return Math.max(0, -newHp);
   }

   public static void updateClient(Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         PacketHandler.sendTo(new PacketSyncSoulHearts(getHP(player)), serverPlayer);
      }
   }
}
