package thaumcraft.item.zombie_brain;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class ItemZombieBrain extends Item {
   public ItemZombieBrain() {
      super(
         new Properties().food(new Builder().nutrition(4).saturationModifier(0.2F).effect(() -> new MobEffectInstance(MobEffects.HUNGER, 30, 0), 0.8F).build())
      );
   }

   @Override
   public net.minecraft.world.item.ItemStack finishUsingItem(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level level, net.minecraft.world.entity.LivingEntity entity) {
      if (!level.isClientSide && entity instanceof net.minecraft.world.entity.player.Player player) {
         if (level.random.nextFloat() < 0.1F) {
            thaumcraft.Thaumcraft.addStickyWarpToPlayer(player, 1);
         } else {
            thaumcraft.Thaumcraft.addWarpToPlayer(player, 1 + level.random.nextInt(3), true);
         }
      }
      return super.finishUsingItem(stack, level, entity);
   }
}
