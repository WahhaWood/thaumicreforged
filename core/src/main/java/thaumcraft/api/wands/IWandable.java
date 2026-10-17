package thaumcraft.api.wands;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface IWandable {
   int onWandRightClick(@NotNull Level var1, @NotNull Player var2, @NotNull InteractionHand var3, @NotNull BlockPos var4, @NotNull Direction var5);

   /** As above, knowing where the block was hit (TC4 retraced it to find which tube arm was clicked). */
   default int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos pos, @NotNull Direction side, @NotNull Vec3 hit
   ) {
      return this.onWandRightClick(level, player, hand, pos, side);
   }

   @NotNull
   ItemStack onWandRightClick(@NotNull Level var1, @NotNull Player var2, @NotNull InteractionHand var3);

   void onUsingWandTick(@NotNull ItemStack var1, @NotNull Player var2, int var3);

   void onWandStoppedUsing(@NotNull ItemStack var1, @NotNull Level var2, @NotNull Player var3, int var4);
}
