package thaumcraft.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IRepairableExtended extends IRepairable {
   boolean doRepair(ItemStack var1, Player var2, int var3);
}
