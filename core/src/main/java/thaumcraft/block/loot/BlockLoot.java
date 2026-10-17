package thaumcraft.block.loot;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import thaumcraft.util.Utils;

public class BlockLoot extends Block {
   private static final VoxelShape SHAPE = box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
   private static final RandomSource LOOT_RANDOM = RandomSource.create();
   private final int level;

   public BlockLoot(int level, Properties properties) {
      super(properties);
      this.level = level;
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return SHAPE;
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull Builder builder) {
      ArrayList<ItemStack> list = new ArrayList<>();
      int i = 0;

      for (int amount = 1 + this.level + LOOT_RANDOM.nextInt(3); i < amount; i++) {
         ItemStack loot = Utils.generateLoot(this.level, LOOT_RANDOM, builder.getLevel().registryAccess());
         if (!loot.isEmpty()) {
            list.add(loot);
         }
      }

      return list;
   }
}
