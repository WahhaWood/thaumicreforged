package thaumcraft.world.biomes;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.biome.Biome.TemperatureModifier;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;

public final class BiomeHandler {
   private static final Map<TagKey<Biome>, BiomeHandler.BiomeInfo> TAG_INFO = new LinkedHashMap<>();
   private static final EnumMap<TemperatureModifier, BiomeHandler.BiomeInfo> TEMP_INFO = new EnumMap<>(TemperatureModifier.class);
   private static final EnumMap<Precipitation, BiomeHandler.BiomeInfo> PRECIP_INFO = new EnumMap<>(Precipitation.class);

   private BiomeHandler() {
   }

   public static TagKey<Biome> biomeTag(String namespace, String path) {
      return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(namespace, path));
   }

   public static void registerTagInfo(TagKey<Biome> tag, int aura, @Nullable Aspect aspect, boolean greatwood, float chance) {
      TAG_INFO.put(tag, new BiomeHandler.BiomeInfo(aura, aspect, greatwood, chance));
   }

   public static void registerTempInfo(TemperatureModifier mod, int aura, @Nullable Aspect aspect, boolean greatwood, float chance) {
      TEMP_INFO.put(mod, new BiomeHandler.BiomeInfo(aura, aspect, greatwood, chance));
   }

   public static void registerPrecipInfo(Precipitation pr, int aura, @Nullable Aspect aspect, boolean greatwood, float chance) {
      PRECIP_INFO.put(pr, new BiomeHandler.BiomeInfo(aura, aspect, greatwood, chance));
   }

   private static Precipitation getPrecipitationNormalized(Biome b, @Nullable BlockPos pos) {
      if (pos == null) {
         return Precipitation.NONE;
      } else {
         BlockPos p = new BlockPos(pos.getX(), 64, pos.getZ());
         return b.getPrecipitationAt(p);
      }
   }

   public static float getBiomeSupportsGreatwood(Holder<Biome> biome, @Nullable BlockPos pos) {
      try {
         {
            // 1.7.10 (BiomeHandler#getBiomeSupportsGreatwood) просто берёт ПЕРВУЮ
            // подошедшую категорию из biomeInfo. Раньше здесь стояла отдельная
            // ветка «лес/тайга/джунгль -> 0.2», которая перебивала таблицу, где
            // для леса 1.0, тайги 0.8, джунглей 1.2 — великое дерево выпадало
            // втрое реже оригинала. Теперь таблица единственный источник.
            float best = 0.0F;

            for (Entry<TagKey<Biome>, BiomeHandler.BiomeInfo> e : TAG_INFO.entrySet()) {
               if (biome.is(e.getKey())) {
                  BiomeHandler.BiomeInfo info = e.getValue();
                  if (info.greatwood()) {
                     best = Math.max(best, info.greatwoodChance());
                  }
               }
            }

            if (best > 0.0F) {
               return best;
            } else {
               Biome b = (Biome)biome.value();
               BiomeHandler.BiomeInfo t = TEMP_INFO.get(b.getModifiedClimateSettings().temperatureModifier());
               if (t != null && t.greatwood()) {
                  best = Math.max(best, t.greatwoodChance());
               }

               BiomeHandler.BiomeInfo p = PRECIP_INFO.get(getPrecipitationNormalized(b, pos));
               if (p != null && p.greatwood()) {
                  best = Math.max(best, p.greatwoodChance());
               }

               return best;
            }
         }
      } catch (Exception var6) {
         Thaumcraft.log.error("[Thaumcraft] getBiomeSupportsGreatwood failed", var6);
         return 0.0F;
      }
   }

   public static boolean getBiomeSupportsSilverwood(Holder<Biome> biome, @Nullable BlockPos pos) {
      try {
         Biome b = (Biome)biome.value();
         float temp = b.getModifiedClimateSettings().temperature();
         if (temp < 0.3F || temp > 0.9F) {
            return false;
         } else if (!b.hasPrecipitation()) {
            return false;
         } else {
            Precipitation pr = getPrecipitationNormalized(b, pos);
            if (pr != Precipitation.RAIN) {
               return false;
            } else {
               float downfall = b.getModifiedClimateSettings().downfall();
               return downfall >= 0.2F;
            }
         }
      } catch (Exception var6) {
         Thaumcraft.log.error("[Thaumcraft] getBiomeSupportsSilverwood failed", var6);
         return false;
      }
   }

   public static int getBiomeAura(Holder<Biome> biome, @Nullable BlockPos pos) {
      try {
         int sum = 0;
         int cnt = 0;

         for (Entry<TagKey<Biome>, BiomeHandler.BiomeInfo> e : TAG_INFO.entrySet()) {
            if (biome.is(e.getKey())) {
               sum += e.getValue().aura();
               cnt++;
            }
         }

         Biome b = (Biome)biome.value();
         BiomeHandler.BiomeInfo t = TEMP_INFO.get(b.getModifiedClimateSettings().temperatureModifier());
         if (t != null) {
            sum += t.aura();
            cnt++;
         }

         BiomeHandler.BiomeInfo p = PRECIP_INFO.get(getPrecipitationNormalized(b, pos));
         if (p != null) {
            sum += p.aura();
            cnt++;
         }

         return cnt == 0 ? 100 : sum / cnt;
      } catch (Exception var7) {
         Thaumcraft.log.error("[Thaumcraft] getBiomeAura failed", var7);
         return 100;
      }
   }

   @Nullable
   public static Aspect getRandomBiomeTag(Holder<Biome> biome, @Nullable BlockPos pos, RandomSource random) {
      try {
         for (Entry<TagKey<Biome>, BiomeHandler.BiomeInfo> e : TAG_INFO.entrySet()) {
            if (biome.is(e.getKey())) {
               Aspect a = e.getValue().aspect();
               if (a != null && random.nextBoolean()) {
                  return a;
               }
            }
         }

         Biome b = (Biome)biome.value();
         BiomeHandler.BiomeInfo t = TEMP_INFO.get(b.getModifiedClimateSettings().temperatureModifier());
         Aspect out = t != null ? t.aspect() : null;
         BiomeHandler.BiomeInfo p = PRECIP_INFO.get(getPrecipitationNormalized(b, pos));
         if (p != null && random.nextBoolean()) {
            out = p.aspect();
         }

         if (out == null) {
            float temp = b.getBaseTemperature();
            float downfall = b.getModifiedClimateSettings().downfall();
            if (temp > 0.9F) {
               out = Aspect.FIRE;
            } else if (temp < 0.2F) {
               out = Aspect.ORDER;
            } else if (downfall > 0.8F) {
               out = Aspect.WATER;
            } else if (downfall < 0.2F) {
               out = Aspect.ENTROPY;
            } else {
               out = random.nextBoolean() ? Aspect.EARTH : Aspect.AIR;
            }
         }

         return out;
      } catch (Exception var9) {
         Thaumcraft.log.error("[Thaumcraft] getRandomBiomeTag failed", var9);
         return null;
      }
   }

   public record BiomeInfo(int aura, @Nullable Aspect aspect, boolean greatwood, float greatwoodChance) {
   }
}
