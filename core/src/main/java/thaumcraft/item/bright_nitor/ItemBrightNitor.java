package thaumcraft.item.bright_nitor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.block.airy.gazy.BlockNitorGas;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModBlocks;

public class ItemBrightNitor extends Item {
   public ItemBrightNitor() {
      super(new Properties().stacksTo(1));
   }

   public static void setBlock(int x, int y, int z, Level level) {
      if (!level.isClientSide) {
         BlockPos pos = new BlockPos(x, y, z);
         Block current = level.getBlockState(pos).getBlock();
         Block gasBlock = (Block)ModBlocks.NITOR_GAS.get();
         if (current == Blocks.AIR || current == gasBlock) {
            if (gasBlock instanceof BlockNitorGas nitorGas) {
               level.setBlock(pos, nitorGas.stateForBrightNitor(), 2);
            } else {
               level.setBlock(pos, gasBlock.defaultBlockState(), 2);
            }
         }
      }
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
      if (!level.isClientSide) {
         int x = (int)Math.floor(entity.getX());
         int y = (int)entity.getY() + 1;
         int z = (int)Math.floor(entity.getZ());
         if (Config.allowSetLightInRegions || entity instanceof Player player && false) {
            setBlock(x, y, z, level);
         }
      }
   }
}
