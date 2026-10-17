package thaumcraft.block.ethereal_bloom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModSounds;

public class EtherealBloomBlockEntity extends BlockEntity {
   public int counter = 0;
   public int growthCounter = 0;

   public EtherealBloomBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.ETHEREAL_BLOOM.get(), pos, state);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, EtherealBloomBlockEntity be) {
      if (be.counter == 0) {
         be.counter = level.random.nextInt(100);
      }

      be.counter++;
      if (level.isClientSide) {
         be.clientTick(level, pos);
      } else {
         be.serverTick(level, pos);
      }
   }

   private void serverTick(Level level, BlockPos pos) {
      if (this.counter % 20 == 0) {
         int dx = level.random.nextInt(8) - level.random.nextInt(8);
         int dz = level.random.nextInt(8) - level.random.nextInt(8);
         if (dx * dx + dz * dz <= 81) {
            BlockPos targetPos = pos.offset(dx, 0, dz);
            var biomeHolder = level.getBiome(targetPos);
            if (biomeHolder.is(thaumcraft.init.registry.ModBiomes.TAINT) || biomeHolder.is(thaumcraft.init.registry.ModBiomes.EERIE)) {
               thaumcraft.util.Utils.setBiomeAt(level, targetPos, net.minecraft.world.level.biome.Biomes.PLAINS);
            }

            for (int dy = -4; dy <= 4; dy++) {
               BlockPos checkPos = targetPos.offset(0, dy, 0);
               BlockState checkState = level.getBlockState(checkPos);
               if (checkState.is((net.minecraft.world.level.block.Block)thaumcraft.init.registry.ModBlocks.FIBROUS_TAINT.get())
                     || checkState.is((net.minecraft.world.level.block.Block)thaumcraft.init.registry.ModBlocks.TAINTED_PLANT.get())
                     || checkState.is((net.minecraft.world.level.block.Block)thaumcraft.init.registry.ModBlocks.TAINTED_GRASS.get())
                     || checkState.is((net.minecraft.world.level.block.Block)thaumcraft.init.registry.ModBlocks.SPORE_STALK.get())
                     || checkState.is((net.minecraft.world.level.block.Block)thaumcraft.init.registry.ModBlocks.MATURE_SPORE_STALK.get())
                     || checkState.is((net.minecraft.world.level.block.Block)thaumcraft.init.registry.ModBlocks.FLUX_GOO.get())) {
                  level.removeBlock(checkPos, false);
               } else if (checkState.is((net.minecraft.world.level.block.Block)thaumcraft.init.registry.ModBlocks.TAINT_SOIL.get())) {
                  level.setBlock(checkPos, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState(), 3);
               } else if (checkState.is((net.minecraft.world.level.block.Block)thaumcraft.init.registry.ModBlocks.TAINT_CRUST.get())) {
                  level.removeBlock(checkPos, false);
               }
            }
         }
      }
   }

   private void clientTick(Level level, BlockPos pos) {
      if (this.growthCounter == 0) {
         playRoots(level, pos);
      }

      this.growthCounter++;
   }

   private static void playRoots(Level level, BlockPos pos) {
      SoundEvent ev = (SoundEvent)ModSounds.ROOTS.get();
      level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ev, SoundSource.BLOCKS, 1.0F, 0.6F, false);
   }
}
