package thaumcraft.item.wands.foci;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.util.ItemStackData;

public class ItemFocusXPDrain extends ItemFocusBasic {
   private static final AspectList EMPTY_COST = new AspectList();
   // Счётчик ротации аспектов храним в NBT стака фокуса (внутри жезла), а не в поле:
   // поле одно на весь сервер и гонит состояние между игроками.
   private static final String NBT_LAST_GIVEN = "xpdrain_last";

   public ItemFocusXPDrain() {
      super("xp_drain", Rarity.EPIC);
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "XPDRAIN" + super.getSortingHelper(itemstack);
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
   public ItemStack onFocusRightClick(ItemStack wandStack, Level level, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return wandStack;
   }

   @Override
   public int getFocusColor(ItemStack stack) {
      return 16777215;
   }

   /**
    * Порт {@code ItemFocusXPDrain#onUsingFocusTick} (TT2): по тику подбирает первичный
    * аспект с запасом места в жезле и обменивает 15 XP игрока на 500 вис этого аспекта
    * (не выше {@code maxVis}). Перебор идёт по кругу, максимум 7 попыток за тик.
    * Позиция ротации своя у каждого стака фокуса (NBT), общего состояния нет.
    */
   @Override
   public void onUsingFocusTick(ItemStack wandStack, Player player, int count) {
      if (player.level().isClientSide) {
         return;
      }

      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      AspectList all = wand.getAllVis(wandStack);
      int maxVis = wand.getMaxVis(wandStack);
      Aspect aspectToAdd = null;
      ArrayList<Aspect> primals = Aspect.getPrimalAspects();
      int lastGiven = readLastGiven(wandStack);
      int startLastGiven = lastGiven;

      for (int takes = 0; aspectToAdd == null && takes < 7; takes++) {
         lastGiven = lastGiven == 5 ? 0 : lastGiven + 1;
         if (lastGiven < primals.size()) {
            Aspect aspect = primals.get(lastGiven);
            if (all.getAmount(aspect) < maxVis) {
               aspectToAdd = aspect;
            }
         }
      }

      if (aspectToAdd == null) {
         return;
      }

      int xpUse = this.getXpUse(wandStack);
      // В 1.7.10 сравнивалось с полем experience (суммарный опыт); в 1.21
      // эквивалент — totalExperience, а giveExperiencePoints корректно
      // раскладывает отрицательную дельту по уровням.
      if (player.totalExperience < xpUse) {
         return;
      }

      player.giveExperiencePoints(-xpUse);
      int amount = Math.min(maxVis, wand.getVis(wandStack, aspectToAdd) + 500);
      wand.storeVis(wandStack, aspectToAdd, amount);

      // Позиция ротации пишется только здесь и только если она реально сдвинулась.
      // ItemStackData.getTag возвращает глубокую копию всего CUSTOM_DATA жезла
      // (все аспекты, фокус, стержень, навершие), а setTag заменяет компонент целиком,
      // поэтому безусловная запись 20 раз в секунду на игрока постоянно дишла слот
      // инвентаря без изменения поведения.
      if (lastGiven != startLastGiven) {
         writeLastGiven(wandStack, lastGiven);
      }
   }

   int getXpUse(ItemStack stack) {
      return 15;
   }

   // getFocusItem returns a detached copy, so the edited focus is written back with setFocus.
   private static int readLastGiven(ItemStack wandStack) {
      return ItemStackData.getInt(((ItemWandCasting)wandStack.getItem()).getFocusItem(wandStack), NBT_LAST_GIVEN);
   }

   private static void writeLastGiven(ItemStack wandStack, int value) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      ItemStack focus = wand.getFocusItem(wandStack);
      if (!focus.isEmpty()) {
         ItemStackData.putInt(focus, NBT_LAST_GIVEN, value);
         wand.setFocus(wandStack, focus);
      }
   }

   @Override
   public void addFocusInformation(ItemStack stack, Level level, List<Component> list, TooltipFlag flag) {
      super.addFocusInformation(stack, level, list, flag);
      list.add(
         Component.literal(" ")
            .append(Component.translatable("ttmisc.experience").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" x " + this.getXpUse(stack)).withStyle(ChatFormatting.WHITE))
      );
   }

   @Override
   public AspectList getVisCost(ItemStack stack) {
      return EMPTY_COST;
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemStack, int rank) {
      return switch (rank) {
         case 1, 2, 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
         default -> null;
      };
   }
}
