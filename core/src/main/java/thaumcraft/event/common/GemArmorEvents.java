package thaumcraft.event.common;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import thaumcraft.item.armor.ichor_gem.ItemGemBoots;
import thaumcraft.item.armor.ichor_gem.ItemIchorArmorGem;

public class GemArmorEvents {
   /** Ключ в persistentData игрока: когда сет в последний раз спас владельца. */
   private static final String LAST_SAVE_KEY = "thaumcraft.gem_armor_last_save";
   /** Кулдаун между спасениями — 5 секунд, чтобы бездна не спамила эффектами. */
   private static final long SAVE_COOLDOWN = 100L;

   @SubscribeEvent
   public void onPlayerJump(LivingJumpEvent event) {
      if (event.getEntity() instanceof Player player) {
         ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
         if (boots.getItem() instanceof ItemGemBoots && ItemIchorArmorGem.isEnabled(boots)) {
            player.setDeltaMovement(player.getDeltaMovement().add(0.0, 0.3, 0.0));
         }
      }
   }

   @SubscribeEvent
   public void onFall(LivingFallEvent event) {
      if (event.getEntity() instanceof Player player) {
         ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
         if (boots.getItem() instanceof ItemGemBoots && ItemIchorArmorGem.isEnabled(boots)) {
            event.setDistance(0.0F);
         }
      }
   }

   /**
    * Бессмертие полного сета: собранная из четырёх частей пробуждённая ихоровая
    * броня не даёт владельцу умереть ни от чего — включая бездну и {@code /kill}.
    * Отмена {@link LivingDeathEvent} оставляет здоровье на нуле, поэтому здесь же
    * оно восстанавливается, иначе игрок застрял бы в мёртвом состоянии.
    *
    * <p>Спасение не может повторяться чаще, чем раз в {@link #SAVE_COOLDOWN}: иначе
    * в бездне игрок умирал бы несколько раз в секунду и заливал экран частицами.
    */
   @SubscribeEvent
   public void onLivingDeath(LivingDeathEvent event) {
      if (!(event.getEntity() instanceof Player player) || player.isCreative() || player.isSpectator()) {
         return;
      }

      if (!ItemIchorArmorGem.isFullSetWorn(player)) {
         return;
      }

      event.setCanceled(true);

      long now = player.level().getGameTime();
      // Проверять кулдаун можно только если ключ вообще записан: getLong на отсутствующем
      // ключе даёт 0, и в мире моложе SAVE_COOLDOWN тиков (now − 0 < 100) первое спасение
      // ушло бы в ветку кулдауна — без частиц, звука и сообщения, и ключ так и не записался бы.
      boolean onCooldown = player.getPersistentData().contains(LAST_SAVE_KEY)
         && now - player.getPersistentData().getLong(LAST_SAVE_KEY) < SAVE_COOLDOWN;
      if (onCooldown) {
         player.setHealth(Math.max(1.0F, player.getMaxHealth() * 0.5F));
         return;
      }

      player.getPersistentData().putLong(LAST_SAVE_KEY, now);
      player.setHealth(player.getMaxHealth());
      if (player.level() instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(
            ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY(0.5), player.getZ(),
            32, 0.5, 0.8, 0.5, 0.1
         );
      }

      player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
      player.displayClientMessage(Component.translatable("tc.awakened_armor.saved"), true);
   }
}