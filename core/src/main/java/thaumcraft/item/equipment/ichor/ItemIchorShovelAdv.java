package thaumcraft.item.equipment.ichor;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Пробуждённая ихориевая лопата (Awakened Ichorium Shovel, KAMI/TT2).
 * Режимы: 0 — Block, 1 — Square (5×5), 2 — Column (столб однотипных блоков 8 вверх/вниз).
 */
public class ItemIchorShovelAdv extends ItemIchorShovel {
   @Override
   public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity) {
      if (!level.isClientSide && miningEntity instanceof Player player) {
         int mode = IchorToolHelper.getMode(stack);
         if (mode == 1) {
            Direction face = IchorToolHelper.rayTraceFace(player, pos);
            int[] r = IchorToolHelper.squareRanges(face);
            IchorToolHelper.removeBlocksInIteration(level, player, stack, pos, r[0], r[1], r[2], r[3], r[4], r[5], null, IchorToolHelper.SHOVEL_MATERIALS);
         } else if (mode == 2) {
            IchorToolHelper.breakColumn(level, player, stack, pos, state);
         }
      }

      return super.mineBlock(stack, level, state, pos, miningEntity);
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown()) {
         if (!level.isClientSide) {
            IchorToolHelper.cycleMode(stack);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
            player.displayClientMessage(IchorToolHelper.getModeText("shovel", IchorToolHelper.getMode(stack)), true);
         }

         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
      } else {
         return super.use(level, player, hand);
      }
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      tooltip.add(IchorToolHelper.getModeText("shovel", IchorToolHelper.getMode(stack)));
   }
}
