package thaumcraft.api.wands;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IWandTriggerManager {
   boolean performTrigger(Level var1, ItemStack var2, Player var3, BlockPos var4, Direction var5, int var6);
}
