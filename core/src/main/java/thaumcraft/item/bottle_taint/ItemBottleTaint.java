package thaumcraft.item.bottle_taint;

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
import thaumcraft.api.IScribeTools;
import thaumcraft.entities.brainless.taint_bottle.EntityBottleTaint;

public class ItemBottleTaint extends Item implements IScribeTools {
   public ItemBottleTaint() {
      super(new Properties().stacksTo(8));
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!player.isCreative()) {
         stack.shrink(1);
      }

      level.playSound(player, player, SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
      if (!level.isClientSide) {
         level.addFreshEntity(new EntityBottleTaint(level, player));
      }

      return super.use(level, player, hand);
   }
}
