package thaumcraft.item.wands.foci;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusHeal extends ItemFocusBasic {
   private static final AspectList visUsage = new AspectList().add(Aspect.EARTH, 45).add(Aspect.WATER, 45);
   public static final Map<UUID, Integer> playerHealData = new HashMap<>();

   public ItemFocusHeal() {
      super("heal");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "HEAL" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getFocusColor(ItemStack stack) {
      return 16580822;
   }

   @Override
   public AspectList getVisCost(ItemStack stack) {
      return visUsage;
   }

   @Override
   public boolean isVisCostPerTick(ItemStack stack) {
      return false;
   }

   @Override
   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack stack) {
      return ItemFocusBasic.WandFocusAnimation.CHARGE;
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level level, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return wandStack;
   }

   @Override
   public void onUsingFocusTick(ItemStack wandStack, Player player, int time) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      Level level = player.level();
      if (wand.consumeAllVis(wandStack, player, visUsage, false, false) && !(player.getHealth() >= player.getMaxHealth())) {
         int potency = this.getUpgradeLevel(wand.getFocusItem(wandStack), FocusUpgradeType.potency);
         UUID playerId = player.getUUID();
         int progress = playerHealData.getOrDefault(playerId, 0) + 1;
         playerHealData.put(playerId, progress);
         if (level.isClientSide) {
            Thaumcraft.setup
               .sparkle(
                  (float)player.getX() + level.random.nextFloat() - 0.5F,
                  (float)player.getY() + level.random.nextFloat(),
                  (float)player.getZ() + level.random.nextFloat() - 0.5F,
                  0
               );
         }

         if (progress >= 30 - potency * 10 / 3) {
            playerHealData.put(playerId, 0);
            wand.consumeAllVis(wandStack, player, visUsage, true, false);
            player.heal(1.0F);
            ResourceLocation soundId = ResourceLocation.fromNamespaceAndPath("thaumcraft", "wand");
            SoundEvent se = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(soundId);
            if (se != null) {
               level.playSound(null, player.getX(), player.getY(), player.getZ(), se, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
         }
      }
   }

   @Override
   public void onPlayerStoppedUsingFocus(ItemStack wandStack, Level level, Player player, int count) {
      playerHealData.remove(player.getUUID());
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemStack, int rank) {
      return switch (rank) {
         case 1, 2, 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         default -> null;
      };
   }
}
