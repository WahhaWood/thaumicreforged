package thaumcraft.dimension.outer_lands.data;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

/**
 * Счётчик пробуждённых боссов лабиринта — порт {@code MapBossData} из 1.7.10.
 *
 * <p>Оригинал по этому счётчику выбирает вариант боссова зала по кругу:
 * голем, Смотритель, культисты, таинт. Без него все залы были бы одинаковыми.
 */
public class BossMapData extends SavedData {
   private static final String FILE_ID = "thaumcraft_boss_map";

   public int bossCount = 0;

   public BossMapData() {
   }

   public static BossMapData load(CompoundTag tag, Provider provider) {
      BossMapData data = new BossMapData();
      data.bossCount = tag.getInt("bossCount");
      return data;
   }

   @Override
   public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull Provider provider) {
      tag.putInt("bossCount", this.bossCount);
      return tag;
   }

   public static BossMapData get(ServerLevel level) {
      MinecraftServer server = level.getServer();
      ServerLevel overworld = server.overworld();
      return overworld.getDataStorage().computeIfAbsent(
         new SavedData.Factory<BossMapData>(BossMapData::new, BossMapData::load, null), FILE_ID
      );
   }
}
