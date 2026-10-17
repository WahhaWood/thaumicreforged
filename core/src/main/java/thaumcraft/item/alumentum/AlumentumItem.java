package thaumcraft.item.alumentum;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AlumentumItem extends Item {
   public AlumentumItem() {
      super(new Properties());
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      level.playSound(
         null,
         player.getX(),
         player.getY(),
         player.getZ(),
         SoundEvents.CROSSBOW_SHOOT,
         SoundSource.PLAYERS,
         0.5F,
         0.4F / (level.random.nextFloat() * 0.4F + 0.8F)
      );
      if (!player.isCreative()) {
         stack.shrink(1);
      }

      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
   }
}
