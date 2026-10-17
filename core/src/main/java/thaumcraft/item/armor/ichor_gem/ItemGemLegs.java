package thaumcraft.item.armor.ichor_gem;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.item.bright_nitor.ItemBrightNitor;

public class ItemGemLegs extends ItemIchorArmorGem {
   public ItemGemLegs() {
      super(Type.LEGGINGS);
   }

   /**
    * Поножи пылающей мантии (порт {@code ItemGemLegs#tickPlayer}):
    * сопротивление огню + светящийся след нитора по взгляду.
    */
   @Override
   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, level, entity, slot, selected);
      if (!(entity instanceof Player player) || player.getItemBySlot(EquipmentSlot.LEGS) != stack || !isEnabled(stack)) {
         return;
      }

      if (!level.isClientSide) {
         MobEffectInstance effect = player.getEffect(MobEffects.FIRE_RESISTANCE);
         if (effect == null || effect.getDuration() <= 202) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 202, 0, true, false));
         }
      }

      // След из яркого нитора на 5 блоков по направлению взгляда.
      if (!level.isClientSide) {
         double yawRad = Math.toRadians(Mth.wrapDegrees(player.getYRot()) + 90.0F);
         double lx = Math.cos(yawRad);
         double lz = Math.sin(yawRad);
         int x = Mth.floor(player.getX());
         int y = (int)player.getY() + 1;
         int z = Mth.floor(player.getZ());
         double nx = 0.0;
         double nz = 0.0;

         for (int i = 0; i < 5; i++) {
            nx += lx;
            nz += lz;
            ItemBrightNitor.setBlock(x + (int)nx, y, z + (int)nz, level);
         }
      }
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      return 5;
   }

   /**
    * Урон огнём превращается в лечение (порт {@code ItemGemLegs#onDamageTaken}).
    */
   @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
   public void onDamageTaken(LivingIncomingDamageEvent event) {
      LivingEntity living = event.getEntity();
      if (living instanceof Player player
         && player.getItemBySlot(EquipmentSlot.LEGS).getItem() == this
         && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
         && isEnabled(player.getItemBySlot(EquipmentSlot.LEGS))) {
         event.setCanceled(true);
         player.heal(event.getAmount());
      }
   }
}
