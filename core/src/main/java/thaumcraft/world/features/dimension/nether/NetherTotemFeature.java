package thaumcraft.world.features.dimension.nether;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.world.features.StructureGen;
import thaumcraft.world.features.ThaumcraftWorldgen;

/** Тотем в Нижнем мире: опора из обсидиана/тотема с узлом (порт 1.7.10). */
public class NetherTotemFeature extends Feature<NoneFeatureConfiguration> {
   public NetherTotemFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      int x = context.origin().getX();
      int z = context.origin().getZ();
      int groundY = StructureGen.groundY(level, x, z, context.origin().getY());
      if (groundY == Integer.MIN_VALUE) {
         return false;
      }

      BlockPos ground = new BlockPos(x, groundY, z);
      BlockState soil = level.getBlockState(ground);
      if (!soil.is(net.minecraft.world.level.block.Blocks.NETHERRACK)
         && !soil.is(net.minecraft.world.level.block.Blocks.BLACKSTONE)
         && !soil.is(net.minecraft.world.level.block.Blocks.BASALT)) {
         return false;
      }

      int height = 2 + random.nextInt(4);
      level.setBlock(ground, ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState(), 3);

      for (int i = 1; i <= height; i++) {
         BlockPos pos = ground.above(i);
         if (i == 1) {
            level.setBlock(pos, ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState(), 3);
         } else {
            level.setBlock(pos, ModBlocks.CHARGED_OBSIDIAN_TOTEM.get().defaultBlockState(), 3);
            ThaumcraftWorldgen.initNode(level, pos, random);
            break;
         }
      }

      return true;
   }
}
