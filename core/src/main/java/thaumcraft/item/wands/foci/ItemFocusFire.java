package thaumcraft.item.wands.foci;

import java.util.HashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.entities.brainless.projectile.ember.EntityEmber;
import thaumcraft.entities.brainless.projectile.explosive_orb.EntityExplosiveOrb;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusFire extends ItemFocusBasic {
   private static final AspectList costBase = new AspectList().add(Aspect.FIRE, 10);
   private static final AspectList costBeam = new AspectList().add(Aspect.FIRE, 10).add(Aspect.ORDER, 3);
   private static final AspectList costBall = new AspectList().add(Aspect.FIRE, 66).add(Aspect.ENTROPY, 33);
   static HashMap<String, Long> soundDelay = new HashMap<>();
   public static final FocusUpgradeType fireball = new FocusUpgradeType(
      9,
      Thaumcraft.location("textures/foci/fireball.png"),
      "focus.upgrade.fireball.name",
      "focus.upgrade.fireball.text",
      new AspectList().add(Aspect.DARKNESS, 1)
   );
   public static final FocusUpgradeType firebeam = new FocusUpgradeType(
      10,
      Thaumcraft.location("textures/foci/firebeam.png"),
      "focus.upgrade.firebeam.name",
      "focus.upgrade.firebeam.text",
      new AspectList().add(Aspect.ENERGY, 1).add(Aspect.AIR, 1)
   );

   public ItemFocusFire() {
      super("fire");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "AF" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getFocusColor(ItemStack itemstack) {
      return 15028484;
   }

   @Override
   public AspectList getVisCost(ItemStack itemstack) {
      return this.isUpgradedWith(itemstack, firebeam) ? costBeam : (this.isUpgradedWith(itemstack, fireball) ? costBall : costBase);
   }

   @Override
   public int getActivationCooldown(ItemStack focusstack) {
      return this.isUpgradedWith(focusstack, fireball) ? 1000 : 0;
   }

   @Override
   public boolean isVisCostPerTick(ItemStack itemstack) {
      return true;
   }

   @Override
   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack itemstack) {
      return this.isUpgradedWith(itemstack, fireball) ? ItemFocusBasic.WandFocusAnimation.WAVE : ItemFocusBasic.WandFocusAnimation.CHARGE;
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level level, Player player, InteractionHand hand) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      ItemStack focusStack = wand.getFocusItem(wandStack);
      if (this.isUpgradedWith(focusStack, fireball)) {
         if (wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), !level.isClientSide, false)) {
            if (!level.isClientSide) {
               // Порт 1.7.10: апгрейд fireball стреляет взрывным шаром по взгляду игрока.
               // Цена costBall уже списана строкой выше — повторно не списываем.
               int potency = wand.getFocusPotency(wandStack);
               EntityExplosiveOrb orb = new EntityExplosiveOrb(level, player);
               orb.strength = 1.0F + potency * 0.5F;
               orb.onFire = true;
               level.addFreshEntity(orb);
            }

            player.swing(hand);
         }
      } else {
         player.startUsingItem(hand);
      }

      return wandStack;
   }

   @Override
   public void onUsingFocusTick(ItemStack wandStack, Player player, int count) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      Level level = player.level();
      ItemStack focusStack = wand.getFocusItem(wandStack);
      if (!wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), false, false)) {
         player.stopUsingItem();
      } else {
         String pp = "R" + player.getScoreboardName();
         if (!level.isClientSide) {
            long lastSound = soundDelay.getOrDefault(pp, 0L);
            if (lastSound < System.currentTimeMillis()) {
               ResourceLocation id = ResourceLocation.fromNamespaceAndPath("thaumcraft", "fireloop");
               SoundEvent se = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(id);
               if (se != null) {
                  level.playSound(null, player.getX(), player.getY(), player.getZ(), se, SoundSource.PLAYERS, 0.33F, 2.0F);
               }

               soundDelay.put(pp, System.currentTimeMillis() + 500L);
            }
         }

         if (!level.isClientSide && wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), true, false)) {
            float scatter = this.isUpgradedWith(focusStack, firebeam) ? 0.25F : 15.0F;
            int potency = wand.getFocusPotency(wandStack);

            for (int a = 0; a < 2 + potency; a++) {
               EntityEmber orb = new EntityEmber(level, player, scatter);
               orb.damage = 2 + potency;
               if (this.isUpgradedWith(focusStack, firebeam)) {
                  orb.damage += 0.5F;
                  orb.damage *= 1.5F;
                  orb.duration = 30;
               }

               orb.firey = this.getUpgradeLevel(focusStack, FocusUpgradeType.alchemistsfire);
               Vec3 motion = orb.getDeltaMovement();
               orb.setPos(orb.getX() + motion.x, orb.getY() + motion.y, orb.getZ() + motion.z);
               level.addFreshEntity(orb);
            }
         }
      }
   }

   @Override
   public void onPlayerStoppedUsingFocus(ItemStack wandStack, Level level, Player player, int count) {
      String pp = "R" + player.getScoreboardName();
      soundDelay.remove(pp);
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      return switch (rank) {
         case 1 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 2 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.alchemistsfire};
         case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, fireball, firebeam};
         case 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.alchemistsfire};
         case 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         default -> null;
      };
   }

   @Override
   public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
      return type.equals(FocusUpgradeType.alchemistsfire) && this.isUpgradedWith(focusstack, fireball)
         ? true
         : !type.equals(FocusUpgradeType.alchemistsfire)
            || !this.isUpgradedWith(focusstack, fireball)
            || !this.isUpgradedWith(focusstack, FocusUpgradeType.alchemistsfire);
   }
}
