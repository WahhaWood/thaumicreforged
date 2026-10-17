package thaumcraft.block.eldritch.trap;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.Thaumcraft;
import thaumcraft.block.eldritch.BlockEldritchEntityBase;

public class BlockEldritchTrap extends BlockEldritchEntityBase {
   public BlockEldritchTrap() {
      super(
         Properties.of()
            .strength(15.0F, 30.0F)
            .sound(SoundType.STONE)
            .noOcclusion()
            .isSuffocating((s, g, p) -> false)
            .isViewBlocking((s, g, p) -> false)
            .isValidSpawn((s, g, p, e) -> false)
            .lightLevel(s -> 0)
      );
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new EldritchTrapBlockEntity(pos, state);
   }

   public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
      super.spawnAfterBreak(state, level, pos, tool, dropExperience);
      if (dropExperience) {
         this.popExperience(level, pos, Mth.nextInt(level.random, 1, 4));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (level instanceof ClientLevel clientLevel) {
         int x = pos.getX();
         int y = pos.getY();
         int z = pos.getZ();
         int n4 = x + random.nextInt(2) - random.nextInt(2);
         int n5 = y + random.nextInt(2) - random.nextInt(2);
         int n6 = z + random.nextInt(2) - random.nextInt(2);
         BlockPos p = new BlockPos(n4, n5, n6);
         if (clientLevel.isEmptyBlock(p)) {
            Thaumcraft.setup
               .blockRunes(
                  clientLevel,
                  n4 + random.nextFloat(),
                  n5 + random.nextFloat(),
                  n6 + random.nextFloat(),
                  0.5F + random.nextFloat() * 0.5F,
                  random.nextFloat() * 0.3F,
                  0.9F + random.nextFloat() * 0.1F,
                  16 + random.nextInt(4),
                  0.0F
               );
         }
      }
   }
}
