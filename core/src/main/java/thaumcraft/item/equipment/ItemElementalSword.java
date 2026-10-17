package thaumcraft.item.equipment;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IRepairable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.util.Utils;

public class ItemElementalSword extends SwordItem implements IRepairable {
   private boolean sweepInProgress = false;

   public ItemElementalSword() {
      super(
         ThaumcraftApi.TOOL_MATERIAL_ELEMENTAL,
         new Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_ELEMENTAL, 3, -2.4F))
      );
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      player.startUsingItem(hand);
      return InteractionResultHolder.consume(stack);
   }

   public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
      return 72000;
   }

   @NotNull
   public UseAnim getUseAnimation(@NotNull ItemStack stack) {
      return UseAnim.BLOCK;
   }

   public void onUseTick(@NotNull Level level, @NotNull LivingEntity user, @NotNull ItemStack stack, int count) {
      if (user instanceof Player player) {
         int ticks = this.getUseDuration(stack, user) - count;
         Vec3 movement = player.getDeltaMovement();
         if (movement.y < 0.0) {
            movement = new Vec3(movement.x, movement.y / 1.2F, movement.z);
            player.fallDistance /= 1.2F;
         }

         movement = movement.add(0.0, 0.08F, 0.0);
         if (movement.y > 0.5) {
            movement = new Vec3(movement.x, 0.2F, movement.z);
         }

         player.setDeltaMovement(movement);

         // Ветряной щит: расталкивание всех сущностей вокруг (кроме игроков).
         // Порт цикла из onUsingTick оригинала.
         if (!level.isClientSide) {
            for (Entity target : level.getEntities(player,
               player.getBoundingBox().inflate(2.5, 2.5, 2.5),
               entity -> entity.isAlive() && !(entity instanceof Player))) {
               if (player.getVehicle() == target) {
                  continue;
               }

               Vec3 push = target.position().subtract(player.position());
               double distance = push.length() + 0.1;
               Vec3 motion = target.getDeltaMovement()
                  .add(push.x / 2.5 / distance, push.y / 2.5 / distance, push.z / 2.5 / distance);
               target.setDeltaMovement(motion);
               target.hurtMarked = true;
            }
         }

         if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            // Полный сброс счётчика падения для сетевого игрока (иначе урон
            // при приземлении после парения). Порт Utils.resetFloatCounter.
            Utils.resetFloatCounter(sp);
            if (ticks == 0 || ticks % 20 == 0) {
               level.playSound(null, player.getX(), player.getY(), player.getZ(),
                  (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.WIND.get(),
                  net.minecraft.sounds.SoundSource.PLAYERS, 0.5F,
                  0.9F + level.random.nextFloat() * 0.2F);
            }
         }

         if (ticks % 20 == 0) {
            stack.hurtAndBreak(1, player,
               player.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                  ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                  : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
         }

         if (level.isClientSide) {
            int unused = (int)(player.getBoundingBox().minY - 2.0);
            if (player.onGround()) {
               unused = Mth.floor(player.getBoundingBox().minY);
            }

            for (int _x = 0; _x < 5; _x++) {
               Thaumcraft.setup
                  .smokeSpiral(
                     level,
                     player.getX(),
                     player.getBoundingBox().minY + player.getBbHeight() / 2.0F,
                     player.getZ(),
                     1.5F,
                     level.random.nextInt(360),
                     unused,
                     14540253
                  );
            }

            if (player.onGround()) {
               int var15 = (int)(level.random.nextFloat() * 360.0F);
               float _x = -Mth.sin(var15 / 180.0F * (float) Math.PI) / 5.0F;
               float _xx = Mth.cos(var15 / 180.0F * (float) Math.PI) / 5.0F;
               level.addParticle(ParticleTypes.SMOKE, player.getX(), player.getBoundingBox().minY + 0.1, player.getZ(), _x, 0.0, _xx);
            }
         }

         super.onUseTick(level, user, stack, count);
      }
   }

   public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
      boolean result = super.hurtEnemy(stack, target, attacker);
      if (attacker instanceof Player player && !player.level().isClientSide && !this.sweepInProgress) {
         // Порт onLeftClickEntity: задеваем всех мобов вокруг цели (кроме игроков и самой цели).
         List<LivingEntity> targets = player.level()
            .getEntitiesOfClass(
               LivingEntity.class,
               target.getBoundingBox().inflate(1.2, 1.1, 1.2),
               entity -> entity.isAlive()
                  && entity != target
                  && entity != player
                  && !(entity instanceof Player)
            );
         if (!targets.isEmpty()) {
            this.sweepInProgress = true;

            try {
               for (LivingEntity entity : targets) {
                  player.attack(entity);
               }
            } finally {
               this.sweepInProgress = false;
            }

            player.level()
               .playSound(
                  null, target.blockPosition(), thaumcraft.init.registry.ModSounds.SWING.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.9F + player.level().random.nextFloat() * 0.2F
               );
         }
      }

      return result;
   }

   private static int getFireAspect(ItemStack stack) {
      return stack.getEnchantments()
         .entrySet()
         .stream()
         .filter(entry -> ((Holder)entry.getKey()).is(Enchantments.FIRE_ASPECT))
         .mapToInt(Entry::getIntValue)
         .findFirst()
         .orElse(0);
   }
}
