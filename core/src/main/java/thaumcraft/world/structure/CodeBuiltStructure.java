package thaumcraft.world.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

/**
 * Структура, построенная кодовой фичей ({@code Feature}), а не jigsaw-шаблоном.
 *
 * <p>Зачем: постройки TC4 (курган, холм, тотем, кольцо) исторически строятся кодом,
 * а ванильный {@code /locate structure} видит только настоящие структуры.
 * Этот тип оборачивает существующую {@code ConfiguredFeature}: генерация идёт тем
 * же кодом, что и раньше, но структура попадает в наборы структур и находится
 * командой {@code /locate structure thaumcraft:...}.
 */
public class CodeBuiltStructure extends Structure {
   public static final MapCodec<CodeBuiltStructure> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
         settingsCodec(instance),
         ResourceKey.codec(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE)
            .fieldOf("feature").forGetter(structure -> structure.featureKey)
      ).apply(instance, CodeBuiltStructure::new)
   );

   private final ResourceKey<ConfiguredFeature<?, ?>> featureKey;

   public CodeBuiltStructure(StructureSettings settings, ResourceKey<ConfiguredFeature<?, ?>> featureKey) {
      super(settings);
      this.featureKey = featureKey;
   }

   @Override
   protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
      ChunkPos chunkPos = context.chunkPos();
      int x = chunkPos.getMiddleBlockX();
      int z = chunkPos.getMiddleBlockZ();
      int y = context.chunkGenerator().getFirstOccupiedHeight(
         x, z, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState());

      // ВАЖНО: /locate считает позицию математически и не смотрит на рельеф.
      // Раньше он охотно отправлял игрока в океан или на обрыв, а постройка там
      // молча отменялась (её собственные проверки грунта) — «локейт врёт».
      // Проверяем рельеф здесь, на этапе выбора точки: тогда /locate находит
      // только те места, где постройка действительно встанет.
      if (!this.isBuildableTerrain(context, x, y, z)) {
         return Optional.empty();
      }

      return Optional.of(new GenerationStub(new BlockPos(x, y, z),
         builder -> builder.addPiece(new CodeBuiltPiece(this.featureKey, new BlockPos(x, y, z)))));
   }

   /**
    * Годится ли рельеф: площадка достаточно ровная и не под водой.
    *
    * <p>Высоты берутся через heightmap генератора, то есть доступны и при
    * поиске (/locate), когда блоки читать ещё нельзя. Разброс по площадке
    * 3x3 не больше двух блоков, и сама точка выше уровня моря — океаны и
    * крутые склоны отсекаются.
    *
    * <p>Heightmap именно MOTION_BLOCKING_NO_LEAVES: с WORLD_SURFACE высота
    * считалась по кроне дерева, и центр постройки оказывался на листве —
    * проверка грунта у фичи не проходила, хотя /locate точку показывал.
    */
   private boolean isBuildableTerrain(GenerationContext context, int x, int y, int z) {
      int seaLevel = 63;
      if (y <= seaLevel) {
         return false;
      }

      int min = y;
      int max = y;
      for (int dx = -3; dx <= 3; dx += 3) {
         for (int dz = -3; dz <= 3; dz += 3) {
            if (dx == 0 && dz == 0) {
               continue;
            }

            int h = context.chunkGenerator().getFirstOccupiedHeight(
               x + dx, z + dz, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState());
            min = Math.min(min, h);
            max = Math.max(max, h);
         }
      }

      return max - min <= 2;
   }

   @Override
   public StructureType<?> type() {
      return ModStructures.CODE_BUILT.get();
   }
}
