package thaumcraft.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class CropUtils {
   public static final Map<Block, Integer> clickableCrops = new HashMap<>();
   public static final Map<Block, Integer> standardCrops = new HashMap<>();
   public static final Map<Block, Integer> stackedCrops = new HashMap<>();
   public static final Set<Block> lampBlacklist = new HashSet<>();

   public static void addStandardCrop(ItemStack itemStack, int age) {
      Block b = Block.byItem(itemStack.getItem());
      if (b != Blocks.AIR) {
         standardCrops.put(b, age);
      }
   }

   public static void addClickableCrop(ItemStack itemStack, int age) {
      Block b = Block.byItem(itemStack.getItem());
      if (b != Blocks.AIR) {
         clickableCrops.put(b, age);
      }
   }

   public static void addStackedCrop(ItemStack itemStack, int age) {
      Block b = Block.byItem(itemStack.getItem());
      if (b != Blocks.AIR) {
         stackedCrops.put(b, age);
      }
   }

   public static void addStackedCrop(Block block, int age) {
      if (block != Blocks.AIR) {
         stackedCrops.put(block, age);
      }
   }

   @Deprecated
   public static boolean isGrownCrop(Level world, int x, int y, int z) {
      return isGrownCrop(world, new BlockPos(x, y, z));
   }

   public static boolean isGrownCrop(Level world, BlockPos pos) {
      if (world.isEmptyBlock(pos)) {
         return false;
      } else {
         BlockState state = world.getBlockState(pos);
         Block block = state.getBlock();
         if (!isGrownCustomCrop(state, standardCrops) && !isGrownCustomCrop(state, clickableCrops)) {
            if (isGrownCustomCrop(state, stackedCrops) && world.getBlockState(pos.below()).getBlock() == block) {
               return true;
            } else if (block instanceof CropBlock cropBlock) {
               return cropBlock.isMaxAge(state);
            } else if (block instanceof NetherWartBlock) {
               return (Integer)state.getValue(NetherWartBlock.AGE) >= 3;
            } else {
               return block instanceof CocoaBlock ? (Integer)state.getValue(CocoaBlock.AGE) >= 2 : false;
            }
         } else {
            return true;
         }
      }
   }

   private static boolean isGrownCustomCrop(BlockState state, Map<Block, Integer> map) {
      Block block = state.getBlock();
      if (!map.containsKey(block)) {
         return false;
      } else {
         int targetAge = map.get(block);
         return targetAge == 32767 ? true : getAge(state) >= targetAge;
      }
   }

   public static void blacklistLamp(ItemStack itemStack, int age) {
      Block b = Block.byItem(itemStack.getItem());
      if (b != Blocks.AIR) {
         lampBlacklist.add(b);
      }
   }

   @Deprecated
   public static boolean doesLampGrow(Level world, int x, int y, int z) {
      return doesLampGrow(world, new BlockPos(x, y, z));
   }

   public static boolean doesLampGrow(Level world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      return !lampBlacklist.contains(state.getBlock());
   }

   public static int getAge(BlockState state) {
      Block block = state.getBlock();
      if (block instanceof CropBlock) {
         return (Integer)state.getValue(CropBlock.AGE);
      } else if (block instanceof NetherWartBlock) {
         return (Integer)state.getValue(NetherWartBlock.AGE);
      } else {
         for (Property<?> prop : state.getProperties()) {
            if (prop instanceof IntegerProperty ip && prop.getName().toLowerCase().contains("age")) {
               return (Integer)state.getValue(ip);
            }
         }

         return 0;
      }
   }
}
