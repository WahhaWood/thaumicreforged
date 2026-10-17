package thaumcraft.item.wands.foci;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.entities.brainless.projectile.frost_shard.EntityFrostShard;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusFrost extends ItemFocusBasic {
   private static final AspectList costBase = new AspectList().add(Aspect.WATER, 5).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2);
   private static final AspectList costScatter = new AspectList().add(Aspect.WATER, 20).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2).add(Aspect.AIR, 5);
   private static final AspectList costBoulder = new AspectList().add(Aspect.WATER, 20).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2).add(Aspect.EARTH, 5);
   public static final FocusUpgradeType scattershot = new FocusUpgradeType(
      11,
      Thaumcraft.location("textures/foci/scattershot.png"),
      "focus.upgrade.scattershot.name",
      "focus.upgrade.scattershot.text",
      new AspectList().add(Aspect.COLD, 1).add(Aspect.WEAPON, 1)
   );
   public static final FocusUpgradeType iceboulder = new FocusUpgradeType(
      12,
      Thaumcraft.location("textures/foci/iceboulder.png"),
      "focus.upgrade.iceboulder.name",
      "focus.upgrade.iceboulder.text",
      new AspectList().add(Aspect.COLD, 1).add(Aspect.CRYSTAL, 1)
   );

   public ItemFocusFrost() {
      super("frost");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "BF" + super.getSortingHelper(itemstack);
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack itemstack, Level level, Player p, InteractionHand hand) {
      ItemWandCasting wand = (ItemWandCasting)itemstack.getItem();
      // Порт 1.7.10: три режима по апгрейдам — scattershot (россыпь),
      // iceboulder (отскакивающий валун) и обычный осколок.
      // Цену читаем из стака фокуса: там лежат апгрейды, в стаке жезла их не видно.
      ItemStack focusStack = wand.getFocusItem(itemstack);
      if (!level.isClientSide && wand.consumeAllVis(itemstack, p, this.getVisCost(focusStack), true, false)) {
         int frosty = this.getUpgradeLevel(focusStack, FocusUpgradeType.alchemistsfrost);
         EntityFrostShard shard = null;
         if (this.isUpgradedWith(focusStack, scattershot)) {
            for (int a = 0; a < 5 + wand.getFocusPotency(itemstack) * 2; a++) {
               shard = new EntityFrostShard(
                  (net.minecraft.world.entity.EntityType)ModEntities.FROST_SHARD.get(), level, p, 8.0F
               );
               shard.setDamage(1.0F);
               shard.fragile = true;
               shard.setFrosty(frosty);
               level.addFreshEntity(shard);
            }
         } else if (this.isUpgradedWith(focusStack, iceboulder)) {
            shard = new EntityFrostShard(
               (net.minecraft.world.entity.EntityType)ModEntities.FROST_SHARD.get(), level, p, 1.0F
            );
            shard.setDamage(4 + wand.getFocusPotency(itemstack) * 2);
            shard.bounce = 0.8;
            shard.bounceLimit = 6;
            shard.setFrosty(frosty);
            level.addFreshEntity(shard);
         } else {
            shard = new EntityFrostShard(
               (net.minecraft.world.entity.EntityType)ModEntities.FROST_SHARD.get(), level, p, 1.0F
            );
            shard.setDamage((float)(3.0D + (double)wand.getFocusPotency(itemstack) * 1.5D));
            shard.setFrosty(frosty);
            level.addFreshEntity(shard);
         }

         level.playSound(
            null, shard.getX(), shard.getY(), shard.getZ(),
            thaumcraft.init.registry.ModSounds.ICE.get(), net.minecraft.sounds.SoundSource.PLAYERS,
            0.4F, 1.0F + level.random.nextFloat() * 0.1F
         );
      }

      p.swing(p.getUsedItemHand());
      return itemstack;
   }

   @Override
   public int getFocusColor(ItemStack itemstack) {
      return 5204428;
   }

   @Override
   public AspectList getVisCost(ItemStack itemstack) {
      return this.isUpgradedWith(itemstack, scattershot) ? costScatter : (this.isUpgradedWith(itemstack, iceboulder) ? costBoulder : costBase);
   }

   @Override
   public int getActivationCooldown(ItemStack focusstack) {
      return this.getUpgradeLevel(focusstack, scattershot) <= 0 && this.getUpgradeLevel(focusstack, iceboulder) <= 0 ? 200 : 500;
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      switch (rank) {
         case 1:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.alchemistsfrost};
         case 2:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 3:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, scattershot, iceboulder, FocusUpgradeType.alchemistsfrost};
         case 4:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 5:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.alchemistsfrost};
         default:
            return null;
      }
   }
}
