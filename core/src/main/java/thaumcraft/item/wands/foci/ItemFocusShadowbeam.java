package thaumcraft.item.wands.foci;

import java.util.HashMap;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.entities.brainless.projectile.shadow_beam.EntityShadowbeam;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusShadowbeam extends ItemFocusBasic {
   private static final AspectList visUsage = new AspectList().add(Aspect.ORDER, 25).add(Aspect.ENTROPY, 25).add(Aspect.AIR, 15);
   static HashMap<String, Long> soundDelay = new HashMap<>();
   /** Счётчик тиков между чистками карты — см. {@link ItemFocusExcavation#purgeCounter}. */
   private static int purgeCounter = 0;
   /** Записи старше этого срока считаются протухшими и удаляются целиком. */
   private static final long STALE_TIMEOUT_MS = 5000L;

   public ItemFocusShadowbeam() {
      super("shadow_beam", Rarity.EPIC);
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "SHADOWBEAM" + super.getSortingHelper(itemstack);
   }

   @Override
   public boolean isVisCostPerTick(ItemStack stack) {
      return true;
   }

   @Override
   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack stack) {
      return ItemFocusBasic.WandFocusAnimation.CHARGE;
   }

   @Override
   public int getFocusColor(ItemStack stack) {
      return 4915283;
   }

   @Override
   public AspectList getVisCost(ItemStack stack) {
      return visUsage;
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level level, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return wandStack;
   }

   @Override
   public void onUsingFocusTick(ItemStack wandStack, Player player, int count) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      // Порт TT2: каждый тик оплачиваем вис и выпускаем луч, живущий 300 тиков.
      if (!player.level().isClientSide && wand.consumeAllVis(wandStack, player, this.getVisCost(wandStack), true, false)) {
         // Антиспам звука: не чаще раза в полсекунды на игрока.
         String pp = keyFor(player);
         long now = System.currentTimeMillis();
         // Чистка не на каждом тике: карта статическая и иначе растёт вечно — запись
         // удаляется только в onPlayerStoppedUsingFocus, которого не бывает, если игрок
         // умер или вышел с лучом в руке.
         if (++purgeCounter >= 100) {
            purgeCounter = 0;
            soundDelay.entrySet().removeIf(entry -> now - entry.getValue() > STALE_TIMEOUT_MS);
         }

         long lastSound = soundDelay.getOrDefault(pp, 0L);
         if (lastSound < now && player.level().random.nextInt(10) == 0) {
            player.level().playSound(
               null, player.getX(), player.getY(), player.getZ(),
               thaumcraft.init.registry.ModSounds.BRAIN.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F
            );
            soundDelay.put(pp, now + 500L);
         }

         int potency = wand.getFocusPotency(wandStack);
         EntityShadowbeam beam = new EntityShadowbeam(player.level(), player, potency);
         if (player.level().addFreshEntity(beam)) {
            // Луч живёт 300 тиков; тикаем сразу, чтобы он появился в том же кадре.
            for (int t = 0; t < 2; t++) {
               beam.tick();
            }
         }
      }
   }

   @Override
   public void onPlayerStoppedUsingFocus(ItemStack wandStack, Level level, Player player, int count) {
      String pp = keyFor(player);
      soundDelay.remove(pp);
   }

   /**
    * Ключ по UUID, а не по нику: смена ника оставляла бы старую запись в карте мёртвым
    * грузом до конца процесса. Префикс стороны — как у соседних фокусов, клиент и сервер
    * ведут счётчики раздельно.
    */
   private static String keyFor(Player player) {
      return player.level().isClientSide ? "C" + player.getUUID() : "S" + player.getUUID();
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemStack, int rank) {
      return switch (rank) {
         case 1, 2, 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         default -> null;
      };
   }
}
