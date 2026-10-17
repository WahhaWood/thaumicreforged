package thaumcraft.world.biomes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.init.registry.ModBiomes;
import thaumcraft.init.registry.ModBlockTags;
import thaumcraft.util.Utils;

public final class TaintHelper {
   private TaintHelper() {
   }

   public static boolean isTaintZone(Level level, BlockPos pos) {
      return level.getBiome(pos).is(ModBiomes.TAINT) ? true : countAdjacentTaint(level, pos) >= 2;
   }

   public static int countAdjacentTaint(LevelAccessor level, BlockPos pos) {
      int c = 0;

      for (Direction d : Direction.values()) {
         if (level.getBlockState(pos.relative(d)).is(ModBlockTags.TAINT_MATERIAL)) {
            c++;
         }
      }

      return c;
   }

   public static boolean isOnlyAdjacentToTaint(LevelAccessor level, BlockPos pos) {
      for (Direction d : Direction.values()) {
         BlockState s = level.getBlockState(pos.relative(d));
         if (!s.isAir() && !s.is(ModBlockTags.TAINT_MATERIAL)) {
            return false;
         }
      }

      return true;
   }

   public static boolean trySetBiomeColumn(ServerLevel level, BlockPos anyYPosInColumn, ResourceKey<Biome> biomeKey) {
      return Utils.setBiomeAt(level, anyYPosInColumn, biomeKey);
   }
}
