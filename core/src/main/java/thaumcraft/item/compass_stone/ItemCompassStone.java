package thaumcraft.item.compass_stone;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.LevelBlockPos;
import thaumcraft.util.EntityUtils;

public class ItemCompassStone extends Item {
   public static final long TTL_MS = 10000L;
   public static final HashMap<LevelBlockPos, Long> sinisterNodes = new HashMap<>();

   public ItemCompassStone() {
      super(new Properties().stacksTo(1).rarity(Rarity.RARE));
   }

   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean selected) {
      if (level.isClientSide) {
         pruneExpired();
      }
   }

   public static boolean isActiveFor(@NotNull Level level, Entity entity) {
      if (!level.isClientSide) {
         return false;
      } else if (entity == null) {
         return false;
      } else {
         long cutoff = System.currentTimeMillis() - 10000L;
         synchronized (sinisterNodes) {
            Iterator<Entry<LevelBlockPos, Long>> it = sinisterNodes.entrySet().iterator();

            while (it.hasNext()) {
               Entry<LevelBlockPos, Long> e = it.next();
               Long ts = e.getValue();
               if (ts != null && ts >= cutoff) {
                  LevelBlockPos wc = e.getKey();
                  if (wc != null
                     && level.dimension().equals(wc.dim)
                     && EntityUtils.isVisibleTo(0.66F, entity, wc.getX() + 0.5, wc.getY() + 0.5, wc.getZ() + 0.5, 256.0F)) {
                     return true;
                  }
               } else {
                  it.remove();
               }
            }

            return false;
         }
      }
   }

   public static void pruneExpired() {
      long cutoff = System.currentTimeMillis() - 10000L;
      synchronized (sinisterNodes) {
         Iterator<Entry<LevelBlockPos, Long>> it = sinisterNodes.entrySet().iterator();

         while (it.hasNext()) {
            Entry<LevelBlockPos, Long> e = it.next();
            Long ts = e.getValue();
            if (ts == null || ts < cutoff) {
               it.remove();
            }
         }
      }
   }
}
