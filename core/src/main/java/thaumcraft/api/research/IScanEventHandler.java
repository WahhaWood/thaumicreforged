package thaumcraft.api.research;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IScanEventHandler {
   ScanResult scanPhenomena(ItemStack var1, Level var2, Player var3);
}
