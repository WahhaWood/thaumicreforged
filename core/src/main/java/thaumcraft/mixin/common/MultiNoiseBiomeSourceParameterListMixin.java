package thaumcraft.mixin.common;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.Climate.ParameterList;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList.Preset;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModBiomes;

@Mixin({MultiNoiseBiomeSourceParameterList.class})
public class MultiNoiseBiomeSourceParameterListMixin {
   @Shadow
   @Final
   @Mutable
   private ParameterList<Holder<Biome>> parameters;

   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")}
   )
   private void thaumcraft$injectOverworldBiomes(Preset preset, HolderGetter<Biome> biomes, CallbackInfo ci) {
      if (preset == Preset.OVERWORLD) {
         if (!biomes.getClass().getName().contains("RegistrySetBuilder$UniversalLookup")) {
            Holder<Biome> magicalForest = biomes.getOrThrow(ModBiomes.MAGICAL_FOREST);
            Holder<Biome> taint = biomes.getOrThrow(ModBiomes.TAINT);
            List<Pair<ParameterPoint, Holder<Biome>>> values = new ArrayList<>(this.parameters.values().size());
            int taintIndex = 0;

            for (Pair<ParameterPoint, Holder<Biome>> entry : this.parameters.values()) {
               Holder<Biome> biome = (Holder<Biome>)entry.getSecond();
               if (this.inMagicalSlab((ParameterPoint)entry.getFirst())
                  && (biome.is(Biomes.FOREST) || biome.is(Biomes.BIRCH_FOREST))) {
                  values.add(entry.mapSecond(ignored -> magicalForest));
               } else if (biome.is(Biomes.TAIGA) && shouldReplace(taintIndex++, Config.biomeTaintWeight)) {
                  values.add(entry.mapSecond(ignored -> taint));
               } else {
                  values.add(entry);
               }
            }

            this.parameters = new ParameterList(values);
         }
      }
   }

   /**
    * Климатический слэб волшебного леса: целиком покрывает умеренные лесные
    * ячейки (прохладный + умеренный ряды, средняя влажность), поэтому пятна
    * сливаются в крупные связные массивы, а не дробятся на speckles.
    * Границы — по сплитам OverworldBiomeBuilder: температуры [-0.45, 0.2],
    * влажности [-0.1, 0.3]. Редкость задаётся самим объёмом слэба в климате,
    * а не весом (см. Config: biomeMagicalForestWeight больше не используется).
    */
   private static boolean inMagicalSlab(ParameterPoint point) {
      long tMin = net.minecraft.world.level.biome.Climate.quantizeCoord(-0.45F);
      long tMax = net.minecraft.world.level.biome.Climate.quantizeCoord(0.2F);
      long hMin = net.minecraft.world.level.biome.Climate.quantizeCoord(-0.1F);
      long hMax = net.minecraft.world.level.biome.Climate.quantizeCoord(0.3F);
      return point.temperature().min() >= tMin
         && point.temperature().max() <= tMax
         && point.humidity().min() >= hMin
         && point.humidity().max() <= hMax;
   }

   private static boolean shouldReplace(int index, int weight) {
      if (weight <= 0) {
         return false;
      } else {
         return weight >= 10 ? true : Math.floorMod(index, 10) < weight;
      }
   }
}
