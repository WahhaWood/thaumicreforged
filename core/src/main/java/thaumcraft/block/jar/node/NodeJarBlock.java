package thaumcraft.block.jar.node;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.jar.standart.JarBlock;
import thaumcraft.init.registry.ModItems;

public class NodeJarBlock extends JarBlock {
   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new NodeJarBlockEntity(pos, state);
   }

   @NotNull
   @Override
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull Builder builder) {
      List<ItemStack> drops = new ArrayList<>();
      BlockEntity be = (BlockEntity)builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      // TC4 BlockJar.getDrops: the jar always drops with its node until a wand releases the node
      if (be instanceof NodeJarBlockEntity jar && jar.drop && ModItems.NODE_JAR.get() instanceof NodeJarBlockItem item) {
         ItemStack stack = new ItemStack(item);
         item.setAspects(stack, jar.getAspects());
         item.setNodeAttributes(stack, jar.getNodeType(), jar.getNodeModifier(), jar.getId());
         item.setAwakenPrimalVisAmount(stack, jar.getAwakenPrimalVisAmount());
         drops.add(stack);
         return drops;
      } else {
         return drops;
      }
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
      if (level.getBlockEntity(pos) instanceof NodeJarBlockEntity jar && ModItems.NODE_JAR.get() instanceof NodeJarBlockItem item) {
         ItemStack stack = new ItemStack(item);
         item.setAspects(stack, jar.getAspects());
         item.setNodeAttributes(stack, jar.getNodeType(), jar.getNodeModifier(), jar.getId());
         item.setAwakenPrimalVisAmount(stack, jar.getAwakenPrimalVisAmount());
         return stack;
      } else {
         return super.getCloneItemStack(state, target, level, pos, player);
      }
   }

   @Override
   public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
      return false;
   }
}
