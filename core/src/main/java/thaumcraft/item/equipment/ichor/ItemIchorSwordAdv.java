package thaumcraft.item.equipment.ichor;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Пробуждённый ихориевый меч (Awakened Ichorium Sword, KAMI/TT2).
 * Режимы: 0 — Single, 1 — Area (все мобы того же типа в радиусе 3), 2 — Soul (даёт «душевное сердце»,
 * цель получает Resistance на 1 тик — как {@code Potion.field_76429_m} в оригинале).
 */
public class ItemIchorSwordAdv extends ItemIchorSword {
   private boolean areaAttackInProgress = false;

   @Override
   public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
      if (!attacker.level().isClientSide && attacker instanceof Player player) {
         int mode = IchorToolHelper.getMode(stack);
         if (mode == 1 && !this.areaAttackInProgress) {
            this.areaAttackInProgress = true;

            try {
               List<LivingEntity> entities = attacker.level()
                  .getEntitiesOfClass(
                     LivingEntity.class,
                     new AABB(target.blockPosition()).inflate(3.0),
                     entity -> entity != target
                        && entity != player
                        && entity.isAlive()
                        && entity.getClass() == target.getClass()
                  );

               for (LivingEntity entity : entities) {
                  player.attack(entity);
               }

               if (!entities.isEmpty()) {
                  attacker.level()
                     .playSound(
                        null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.9F + attacker.level().random.nextFloat() * 0.2F
                     );
               }
            } finally {
               this.areaAttackInProgress = false;
            }
         } else if (mode == 2) {
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1, 1, false, false));
            SoulHeartHelper.addHearts(player);
         }
      }

      return super.hurtEnemy(stack, target, attacker);
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown()) {
         if (!level.isClientSide) {
            IchorToolHelper.cycleMode(stack);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
            player.displayClientMessage(IchorToolHelper.getModeText("sword", IchorToolHelper.getMode(stack)), true);
         }

         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
      } else {
         return super.use(level, player, hand);
      }
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      tooltip.add(IchorToolHelper.getModeText("sword", IchorToolHelper.getMode(stack)));
   }
}
