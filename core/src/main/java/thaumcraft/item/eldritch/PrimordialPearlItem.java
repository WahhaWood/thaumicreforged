package thaumcraft.item.eldritch;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.research.ResearchManager;

/**
 * Первородная жемчужина: разрушает узел ауры — порт
 * {@code ItemEldritchObject.func_77648_a} (1.7.10, строки 132-188).
 *
 * <p>Раньше метод только взмахивал рукой и возвращал результат, ничего не
 * делая: жемчужина была ингредиентом, но не имела эффекта, а исследование
 * {@code prim_node}, которое в 1.7.10 выдаёт ей дополнительную силу, вообще
 * нигде не читалось.
 *
 * <p>Исследование {@code prim_node} (в оригинале {@code PRIMNODE}) расширяет
 * разброс: база основных аспектов падает сильнее, шанс заполнения выше, и
 * громче звук.
 */
public class PrimordialPearlItem extends EldritchObjectItem {
   public PrimordialPearlItem() {
      super(Rarity.EPIC);
   }

   public boolean hasCraftingRemainingItem(ItemStack stack) {
      return true;
   }

   @NotNull
   public ItemStack getCraftingRemainingItem(@NotNull ItemStack stack) {
      return stack.copyWithCount(1);
   }

   @Override
   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(Component.translatable("item.ItemEldritchObject.text.5").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)));
      components.add(Component.translatable("item.ItemEldritchObject.text.6").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)));
   }

   @Override
   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Player player = context.getPlayer();
      Level level = context.getLevel();
      BlockPos blockPos = context.getClickedPos();

      if (!(level.getBlockEntity(blockPos) instanceof AuraNodeBlockEntity node)) {
         return InteractionResult.PASS;
      }

      if (level.isClientSide()) {
         player.swing(context.getHand());
         return InteractionResult.SUCCESS;
      }

      if (player != null) {
         player.swing(context.getHand());
      }

      boolean research = player != null && ResearchManager.isResearchComplete(player, "prim_node");

      // 1.7.10:145-155 — обрушиваем базу. Основные аспекты теряют 2..7 (или 2..10
      // с исследованием), составные — иногда 1.
      for (Aspect ax : node.getAspects().getAspects()) {
         if (ax == null) {
            continue;
         }

         int m = node.getNodeVisBase(ax);
         if (!ax.isPrimal()) {
            if (level.random.nextBoolean()) {
               node.setNodeVisBase(ax, (short)(m - 1));
            }
         } else {
            node.setNodeVisBase(ax, (short)(m - 2 + level.random.nextInt(research ? 9 : 6)));
         }
      }

      // 1.7.10:157-165 — шанс, что основной аспект окажется выше базы; тогда
      // подкладываем единицу в контейнер. С исследованием шанс выше (до 3).
      for (Aspect axx : Aspect.getPrimalAspects()) {
         int m = node.getNodeVisBase(axx);
         int r = level.random.nextInt(research ? 4 : 3);
         if (r > 0 && r > m) {
            node.setNodeVisBase(axx, (short)r);
            node.addToContainer(axx, 1);
         }
      }

      // 1.7.10:167-173 — откат повреждения: FADING → PALE, PALE → норма,
      // норма → BRIGHT (1 из 5).
      if (node.getNodeModifier() == NodeModifier.FADING && level.random.nextBoolean()) {
         node.setNodeModifier(NodeModifier.PALE);
      } else if (node.getNodeModifier() == NodeModifier.PALE && level.random.nextBoolean()) {
         node.setNodeModifier(null);
      } else if (node.getNodeModifier() == null && level.random.nextInt(5) == 0) {
         node.setNodeModifier(NodeModifier.BRIGHT);
      }

      node.setChanged();
      node.markForUpdate();

      // 1.7.10:175 — звук, громче без исследования.
      // 1.7.10 звал playSoundAtEntity(null, x, y, z, ..., 3.0F + rand*(3|5), true):
      // последний аргумент был громкостью 1.0 по смыслу, громкость — предыдущая.
      // 1.7.10 звал playSoundAtEntity(null, x, y+1.5, z, "random.explode",
      // 3.0F + rand*(3|5), true): громкость в предпоследнем аргументе, шаг в 1.0.
      // 1.7.10 звал playSoundAtEntity(null, x, y+1.5, z, "random.explode",
      // 3.0F + rand*(3|5), true): громкость в предпоследнем аргументе, шаг 1.0.
      // В 1.21.1 GENERIC_EXPLODE — Holder, поэтому берём перегрузку с координатами.
      level.playSound(null,
         blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5,
         net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
         net.minecraft.sounds.SoundSource.BLOCKS,
         3.0F + level.random.nextFloat() * (research ? 3.0F : 5.0F), 1.0F);

      // 1.7.10:177-189 — 33 порции слизи по 11×11×11 вокруг узла: ниже узла
      // густая слизь, выше и на его уровне — газ.
      for (int i = 0; i < 33; i++) {
         int xx = blockPos.getX() + level.random.nextInt(6) - level.random.nextInt(6);
         int yy = blockPos.getY() + level.random.nextInt(6) - level.random.nextInt(6);
         int zz = blockPos.getZ() + level.random.nextInt(6) - level.random.nextInt(6);
         BlockPos target = new BlockPos(xx, yy, zz);

         if (level.isEmptyBlock(target)) {
            level.setBlock(target, (yy < blockPos.getY() ? ModBlocks.FLUX_GOO.get() : ModBlocks.FLUX_GAS.get())
               .defaultBlockState(), 3);
         }
      }

      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }

      return InteractionResult.CONSUME;
   }
}
