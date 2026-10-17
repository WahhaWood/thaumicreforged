package thaumcraft.api.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public interface IThaumcraftChunk {
   void thaumcraft$setBiome(BlockPos var1, Holder<Biome> var2);
}
