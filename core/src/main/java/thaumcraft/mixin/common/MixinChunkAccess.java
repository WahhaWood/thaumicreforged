package thaumcraft.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import thaumcraft.api.world.IThaumcraftChunk;

@Mixin({ChunkAccess.class})
public abstract class MixinChunkAccess implements IThaumcraftChunk {
   @Shadow
   @Final
   protected LevelChunkSection[] sections;

   @Shadow
   public abstract void setUnsaved(boolean var1);

   @Override
   public void thaumcraft$setBiome(BlockPos pos, Holder<Biome> biome) {
      int sectionIndex = ((ChunkAccess)(Object)this).getSectionIndex(pos.getY());
      if (sectionIndex >= 0 && sectionIndex < this.sections.length) {
         LevelChunkSection section = this.sections[sectionIndex];
         PalettedContainerRO<Holder<Biome>> currentBiomes = section.getBiomes();
         PalettedContainer<Holder<Biome>> newBiomes = currentBiomes.recreate();
         int x = (pos.getX() & 15) >> 2;
         int y = (pos.getY() & 15) >> 2;
         int z = (pos.getZ() & 15) >> 2;

         for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
               for (int k = 0; k < 4; k++) {
                  if (i != x || j != y || k != z) {
                     Holder<Biome> oldBiome = (Holder<Biome>)currentBiomes.get(i, j, k);
                     newBiomes.getAndSetUnchecked(i, j, k, oldBiome);
                  }
               }
            }
         }

         newBiomes.getAndSetUnchecked(x, y, z, biome);
         ((LevelChunkSectionAccessor)section).setBiomes(newBiomes);
         this.setUnsaved(true);
      }
   }
}
