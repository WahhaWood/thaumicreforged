package thaumcraft.item.wands.foci;

import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.entities.brainless.projectile.primal_orb.EntityPrimalOrb;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusPrimal extends ItemFocusBasic {
   public static FocusUpgradeType seeker = new FocusUpgradeType(
      16,
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/foci/seeker.png"),
      "focus.upgrade.seeker.name",
      "focus.upgrade.seeker.text",
      new AspectList().add(Aspect.SENSES, 1).add(Aspect.MIND, 1)
   );

   public ItemFocusPrimal() {
      super("primal");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "FP" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getActivationCooldown(ItemStack focusstack) {
      return 500;
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack itemstack, Level level, Player p, InteractionHand hit) {
      ItemWandCasting wand = (ItemWandCasting)itemstack.getItem();
      ItemStack focusStack = wand.getFocusItem(itemstack);
      // Цена роллится заново при каждом вызове getVisCost, поэтому считаем её
      // один раз на каст: проверка доступности и списание видят одну цену.
      AspectList cost = this.getVisCost(focusStack);
      if (wand.consumeAllVis(itemstack, p, cost, false, false)) {
         EntityPrimalOrb shard = new EntityPrimalOrb(level, p, this.isUpgradedWith(focusStack, seeker));
         // Порт 1.7.10: осколок появляется в мир только после успешной оплаты виса.
         if (!level.isClientSide && wand.consumeAllVis(itemstack, p, cost, true, false)) {
            if (level.addFreshEntity(shard)) {
               level.playSound(
                  null, shard.getX(), shard.getY(), shard.getZ(),
                  thaumcraft.init.registry.ModSounds.ICE.get(), net.minecraft.sounds.SoundSource.PLAYERS,
                  0.3F, 0.8F + level.random.nextFloat() * 0.1F
               );
            }
         }
      }

      p.swing(p.getUsedItemHand());
      return itemstack;
   }

   @Override
   public int getFocusColor(ItemStack itemstack) {
      return 10854849;
   }

   @Override
   public AspectList getVisCost(ItemStack itemstack) {
      Random rand = new Random(System.currentTimeMillis() / 200L);
      return new AspectList()
         .add(Aspect.WATER, 50 + rand.nextInt(5) * 50)
         .add(Aspect.AIR, 50 + rand.nextInt(5) * 50)
         .add(Aspect.EARTH, 50 + rand.nextInt(5) * 50)
         .add(Aspect.FIRE, 50 + rand.nextInt(5) * 50)
         .add(Aspect.ORDER, 50 + rand.nextInt(5) * 50)
         .add(Aspect.ENTROPY, 50 + rand.nextInt(5) * 50);
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      return switch (rank) {
         case 1 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
         case 2 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
         case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, seeker};
         case 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
         case 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
         default -> null;
      };
   }
}
