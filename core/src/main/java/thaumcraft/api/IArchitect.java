package thaumcraft.api;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IArchitect {
   ArrayList<BlockPos> getArchitectBlocks(ItemStack var1, Level var2, BlockPos var3, Direction var4, Player var5);

   boolean showAxis(ItemStack var1, Level var2, Player var3, Direction var4, IArchitect.EnumAxis var5);

   public static enum EnumAxis {
      X,
      Y,
      Z;
   }
}
