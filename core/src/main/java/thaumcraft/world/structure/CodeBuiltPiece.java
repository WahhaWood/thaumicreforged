package thaumcraft.world.structure;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Кусок структуры, который просто вызывает кодовую фичу в своей точке.
 * Точка и id фичи сохраняются в NBT куска.
 */
public class CodeBuiltPiece extends StructurePiece {
   private final ResourceKey<ConfiguredFeature<?, ?>> featureKey;
   private final BlockPos origin;

   public CodeBuiltPiece(ResourceKey<ConfiguredFeature<?, ?>> featureKey, BlockPos origin) {
      super(ModStructurePieces.CODE_BUILT.get(), 0,
         BoundingBox.fromCorners(origin.offset(-16, -10, -16), origin.offset(16, 10, 16)));
      this.featureKey = featureKey;
      this.origin = origin;
   }

   public CodeBuiltPiece(StructurePieceSerializationContext context, CompoundTag tag) {
      super(ModStructurePieces.CODE_BUILT.get(), tag);
      this.featureKey = ResourceKey.create(Registries.CONFIGURED_FEATURE,
         ResourceLocation.parse(tag.getString("Feature")));
      this.origin = new BlockPos(tag.getInt("OX"), tag.getInt("OY"), tag.getInt("OZ"));
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      tag.putString("Feature", this.featureKey.location().toString());
      tag.putInt("OX", this.origin.getX());
      tag.putInt("OY", this.origin.getY());
      tag.putInt("OZ", this.origin.getZ());
   }

   @Override
   public void postProcess(WorldGenLevel level, StructureManager structureManager,
                           ChunkGenerator chunkGenerator, RandomSource random,
                           BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
      Optional<Holder.Reference<ConfiguredFeature<?, ?>>> holder =
         level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).get(this.featureKey);
      if (holder.isEmpty()) {
         return;
      }

      ConfiguredFeature<?, ?> configured = holder.get().value();
      this.placeFeature(level, chunkGenerator, random, configured);
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private void placeFeature(WorldGenLevel level, ChunkGenerator chunkGenerator,
                             RandomSource random, ConfiguredFeature configured) {
      configured.feature().place(new FeaturePlaceContext(Optional.empty(), level,
         chunkGenerator, random, this.origin, configured.config()));
   }

   public static StructurePieceType type(StructurePieceSerializationContext context, CompoundTag tag) {
      return ModStructurePieces.CODE_BUILT.get();
   }
}
