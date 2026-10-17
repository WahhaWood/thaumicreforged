package thaumcraft.item.wands.foci;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.entities.smart.pech_blast.EntityPechBlast;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusPech extends ItemFocusBasic {
   private static final AspectList cost = new AspectList().add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 10).add(Aspect.WATER, 10);
   private static final AspectList costAll = new AspectList()
      .add(Aspect.AIR, 10)
      .add(Aspect.FIRE, 10)
      .add(Aspect.EARTH, 10)
      .add(Aspect.ORDER, 10)
      .add(Aspect.ENTROPY, 10)
      .add(Aspect.WATER, 10);
   public static final FocusUpgradeType nightshade = new FocusUpgradeType(
      15,
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/foci/nightshade.png"),
      "focus.upgrade.nightshade.name",
      "focus.upgrade.nightshade.text",
      new AspectList().add(Aspect.LIFE, 1).add(Aspect.POISON, 1).add(Aspect.MAGIC, 1)
   );

   public ItemFocusPech() {
      super("pech");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "PP" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getActivationCooldown(ItemStack focusstack) {
      return 250;
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level world, Player player, InteractionHand hand) {
      // Определяем, в какой руке жезл: сравниваем по предмету, а не по ссылке.
      if (ItemStack.isSameItem(wandStack, player.getMainHandItem())) {
         hand = InteractionHand.MAIN_HAND;
      } else if (ItemStack.isSameItem(wandStack, player.getOffhandItem())) {
         hand = InteractionHand.OFF_HAND;
      }

      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      EntityPechBlast blast = new EntityPechBlast(
         world, player, wand.getFocusPotency(wandStack), wand.getFocusExtend(wandStack), this.isUpgradedWith(wand.getFocusItem(wandStack), nightshade)
      );
      // Порт 1.7.10: снаряд появляется в мир только после успешной оплаты виса.
      // Цена берётся со стека фокуса, а не жезла: getVisCost проверяет апгрейд через
      // тег самого фокуса, а в CUSTOM_DATA жезла списка upgrade нет — с wandStack
      // проверка всегда была ложной и каждый выстрел стоил базовую цену.
      if (!world.isClientSide
         && wand.consumeAllVis(wandStack, player, this.getVisCost(wand.getFocusItem(wandStack)), true, false)) {
         if (world.addFreshEntity(blast)) {
            world.playSound(
               null, blast.getX(), blast.getY(), blast.getZ(),
               thaumcraft.init.registry.ModSounds.ICE.get(), net.minecraft.sounds.SoundSource.PLAYERS,
               0.4F, 1.0F + world.random.nextFloat() * 0.1F
            );
         }
      }

      player.swing(hand);
      return wandStack;
   }

   @Override
   public int getFocusColor(ItemStack itemstack) {
      return 2267460;
   }

   @Override
   public AspectList getVisCost(ItemStack itemstack) {
      return this.isUpgradedWith(itemstack, nightshade) ? costAll : cost;
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      return switch (rank) {
         case 1 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 2 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.extend};
         case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.extend};
         case 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, nightshade};
         default -> null;
      };
   }
}
