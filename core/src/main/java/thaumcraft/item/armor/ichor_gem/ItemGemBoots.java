package thaumcraft.item.armor.ichor_gem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;

public class ItemGemBoots extends ItemIchorArmorGem {
   public ItemGemBoots() {
      super(Type.BOOTS);
   }

   /**
    * Сапоги горизонтального щита (порт {@code ItemGemBoots#tickPlayer}):
    * Haste II, шаг на блок, разгон, нет урона от падения, трава под ногами.
    */
   @Override
   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, level, entity, slot, selected);
      if (!(entity instanceof Player player) || player.getItemBySlot(EquipmentSlot.FEET) != stack) {
         return;
      }

      if (!isEnabled(stack)) {
         if (level.isClientSide && player.getAttribute(Attributes.STEP_HEIGHT) != null && player.maxUpStep() > 0.6F) {
            player.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6);
         }

         return;
      }

      if (!level.isClientSide) {
         player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 1, true, false));
      }

      if (level.isClientSide && player.getAttribute(Attributes.STEP_HEIGHT) != null) {
         player.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(player.isShiftKeyDown() ? 0.6F : 1.0F);
      }

      if ((player.onGround() || player.getAbilities().flying) && player.zza > 0.0F) {
         float bonus = player.getAbilities().flying ? 0.075F : 0.15F;
         player.moveRelative(bonus, new Vec3(0.0, 0.0, 1.0));
      }

      player.getAbilities().setFlyingSpeed(player.isSprinting() ? 0.05F : 0.04F);
      player.fallDistance = 0.0F;

      // Земля под ногами превращается в траву.
      if (!level.isClientSide) {
         BlockPos below = player.blockPosition().below();
         if (level.getBlockState(below).is(Blocks.DIRT)) {
            level.setBlock(below, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
         }
      }
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      return 5;
   }
}
