package thaumcraft.item.wands.foci;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.entities.smart.fire_bat.EntityFireBat;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusHellbat extends ItemFocusBasic {
   private static final AspectList COST_BASE = new AspectList().add(Aspect.FIRE, 200).add(Aspect.ENTROPY, 100).add(Aspect.AIR, 100);
   private static final AspectList COST_BOMB = new AspectList().add(Aspect.FIRE, 100).add(Aspect.ENTROPY, 200).add(Aspect.AIR, 100);
   private static final AspectList COST_DEVIL = new AspectList().add(Aspect.FIRE, 100).add(Aspect.ENTROPY, 100).add(Aspect.AIR, 100).add(Aspect.EARTH, 100);
   public static final FocusUpgradeType BATBOMBS = new FocusUpgradeType(
      13,
      Thaumcraft.location("textures/foci/batbombs.png"),
      "focus.upgrade.batbombs.name",
      "focus.upgrade.batbombs.text",
      new AspectList().add(Aspect.ENERGY, 1).add(Aspect.TRAP, 1)
   );
   public static final FocusUpgradeType DEVILBATS = new FocusUpgradeType(
      14,
      Thaumcraft.location("textures/foci/devilbats.png"),
      "focus.upgrade.devilbats.name",
      "focus.upgrade.devilbats.text",
      new AspectList().add(Aspect.ARMOR, 1)
   );
   public static final FocusUpgradeType VAMPIREBATS = new FocusUpgradeType(
      19,
      Thaumcraft.location("textures/foci/vampirebats.png"),
      "focus.upgrade.vampirebats.name",
      "focus.upgrade.vampirebats.text",
      new AspectList().add(Aspect.HUNGER, 1).add(Aspect.LIFE, 1)
   );

   public ItemFocusHellbat() {
      super("hell_bat");
   }

   @Override
   public int getFocusColor(ItemStack itemstack) {
      return 14431746;
   }

   @Override
   public AspectList getVisCost(ItemStack focusStack) {
      if (this.isUpgradedWith(focusStack, BATBOMBS)) {
         return COST_BOMB;
      } else {
         return this.isUpgradedWith(focusStack, DEVILBATS) ? COST_DEVIL : COST_BASE;
      }
   }

   @Override
   public int getActivationCooldown(ItemStack focusStack) {
      return 1000;
   }

   @Override
   public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
      if (type == VAMPIREBATS) {
         return thaumcraft.research.PlayerKnowledge.of(player).getResearchCompleted().contains("vampire_bat");
      }

      return super.canApplyUpgrade(focusstack, player, type, rank);
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      return switch (rank) {
         case 1, 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, BATBOMBS, DEVILBATS};
         case 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, VAMPIREBATS};
         default -> null;
      };
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level world, Player player, InteractionHand hand) {
      return this.doCast(wandStack, world, player);
   }

   /**
    * Призыв огненной летучей мыши на указанную цель (порт {@code ItemFocusHellbat#onFocusRightClick}).
    */
   private ItemStack doCast(ItemStack wandStack, Level level, Player player) {
      if (!(wandStack.getItem() instanceof ItemWandCasting wand)) {
         return wandStack;
      }

      if (!(getPointedEntity(level, player, 32.0) instanceof LivingEntity living)) {
         return wandStack;
      }

      if (living instanceof Player targetPlayer && level.getServer() != null && !level.getServer().isPvpAllowed()) {
         return wandStack;
      }

      if (level.isClientSide) {
         player.swing(player.getUsedItemHand());
         return wandStack;
      }

      EntityFireBat firebat = new EntityFireBat(
         thaumcraft.init.registry.ModEntities.FIRE_BAT.get(), level
      );
      if (firebat == null) {
         return wandStack;
      }

      net.minecraft.world.phys.Vec3 look = player.getLookAngle();
      double px = player.getX() - Math.sin(Math.toRadians(player.getYRot())) * 0.16 + look.x * 0.5;
      double py = player.getEyeY() + 0.25 - 0.05 + look.y * 0.5;
      double pz = player.getZ() + Math.cos(Math.toRadians(player.getYRot())) * 0.16 + look.z * 0.5;
      firebat.moveTo(px, py + firebat.getBbHeight(), pz, player.getYRot(), 0.0F);
      firebat.setTarget(living);
      firebat.setDamBonus(wand.getFocusPotency(wandStack));
      firebat.setIsSummoned(true);
      firebat.setIsBatHanging(false);
      ItemStack focusStack = wand.getFocusItem(wandStack);
      if (this.isUpgradedWith(focusStack, DEVILBATS)) {
         firebat.setIsDevil(true);
      }

      if (this.isUpgradedWith(focusStack, BATBOMBS)) {
         firebat.setIsExplosive(true);
      }

      if (this.isUpgradedWith(focusStack, VAMPIREBATS)) {
         firebat.setOwner(player);
         firebat.setIsVampire(true);
      }

      if (wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), true, false)
         && level.addFreshEntity(firebat)) {
         level.levelEvent(2004, firebat.blockPosition(), 0);
         level.playSound(
            null, firebat.getX(), firebat.getY(), firebat.getZ(),
            net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.PLAYERS, 0.2F, 0.95F + level.random.nextFloat() * 0.1F
         );
      } else {
         level.playSound(
            null, player.getX(), player.getY(), player.getZ(),
            thaumcraft.init.registry.ModSounds.WAND_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.1F, 0.8F + level.random.nextFloat() * 0.2F
         );
      }

      player.swing(player.getUsedItemHand());
      return wandStack;
   }

   private static Entity getPointedEntity(Level level, Player player, double range) {
      Vec3 eye = player.getEyePosition(1.0F);
      Vec3 look = player.getLookAngle();
      Vec3 end = eye.add(look.x * range, look.y * range, look.z * range);
      AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
      EntityHitResult hit = ProjectileUtil.getEntityHitResult(
         level, player, eye, end, box, e -> !e.isSpectator() && e.isPickable() && !(e instanceof EntityFireBat)
      );
      return hit != null ? hit.getEntity() : null;
   }
}
