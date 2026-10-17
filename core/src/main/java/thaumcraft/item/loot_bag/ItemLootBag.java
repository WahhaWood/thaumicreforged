package thaumcraft.item.loot_bag;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.util.Utils;

public class ItemLootBag extends Item {
   private final int level;

   public ItemLootBag(Rarity rarity, int level) {
      super(new Properties().stacksTo(16).rarity(rarity));
      this.level = level;
   }

   @NotNull
   @Override
   public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!world.isClientSide) {
         int q = 8 + world.random.nextInt(5);

         for (int a = 0; a < q; a++) {
            ItemStack is = Utils.generateLoot(this.level, world.random, world.registryAccess());
            if (is != null && !is.isEmpty()) {
               ItemEntity entity = new ItemEntity(world, player.getX(), player.getY() + 0.5, player.getZ(), is.copy());
               entity.setDefaultPickUpDelay();
               world.addFreshEntity(entity);
            }
         }

         world.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.COINS.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
      }

      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }

      return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      components.add(Component.translatable("tc.lootbag"));
   }
}
