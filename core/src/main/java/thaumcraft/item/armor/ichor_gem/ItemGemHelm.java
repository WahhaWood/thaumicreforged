package thaumcraft.item.armor.ichor_gem;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.IGoggles;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.IRevealer;

public class ItemGemHelm extends ItemIchorArmorGem implements IGoggles, IRevealer {
   public ItemGemHelm() {
      super(Type.HELMET);
   }

   /**
    * Капюшон бездонных глубин (порт {@code ItemGemHelm#tickPlayer}):
    * дыхание под водой, зрение в лаве, подкормка при голоде.
    */
   @Override
   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, level, entity, slot, selected);
      if (!(entity instanceof Player player) || player.getItemBySlot(EquipmentSlot.HEAD) != stack || !isEnabled(stack)) {
         return;
      }

      if (!level.isClientSide) {
         // Оригинал проверяет погружение тела (isInsideOfMaterial), а не глаз.
         if (player.isInWater()) {
            player.setAirSupply(player.getMaxAirSupply());
            refreshEffect(player, MobEffects.WATER_BREATHING, 0);
         }

         if (player.isInLava()) {
            player.setAirSupply(player.getMaxAirSupply());
            refreshEffect(player, MobEffects.NIGHT_VISION, 0);
         }

         // Регенерация игнорирует голод: подкормка при сытости ниже 18.
         if (player.getFoodData().getFoodLevel() > 0
            && player.getFoodData().getFoodLevel() < 18
            && player.tickCount % 80 == 0) {
            player.heal(1.0F);
         }
      }
   }

   private static void refreshEffect(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
      MobEffectInstance current = player.getEffect(effect);
      if (current == null || current.getDuration() <= 202) {
         player.addEffect(new MobEffectInstance(effect, 202, amplifier, true, false));
      }
   }

   @Override
   public boolean showNodes(ItemStack stack, LivingEntity entity) {
      return true;
   }

   @Override
   public boolean showIngamePopups(ItemStack stack, LivingEntity entity) {
      return true;
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      return 5;
   }
}
