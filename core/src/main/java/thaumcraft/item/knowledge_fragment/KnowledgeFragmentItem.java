package thaumcraft.item.knowledge_fragment;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.item.curio.CurioItem;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchManager;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.playerdata.PacketAspectPool;

/**
 * Фрагмент знаний — порт {@code ItemResource} с meta 9 (1.7.10).
 *
 * <p>В оригинале это не отдельный предмет, а девятый вариант «ресурса»
 * ({@code ConfigItems.itemResource}, текстура {@code knowledgefragment}).
 * Логика применения в {@code ItemResource.func_77659_a}: по ПКМ предмет
 * тратится и игрок получает по 1–2 единицы КАЖДОГО базового (primal) аспекта
 * в свой пул знаний, с сохранением исследования и рассылкой пакета, чтобы
 * клиент сразу показал новые значения.
 *
 * <p>Реализация пула и синхронизации — те же, что у команды {@code /thaum aspect
 * add} в порте, так что цифры на экране ведут себя одинаково.
 */
public class KnowledgeFragmentItem extends Item {
   public KnowledgeFragmentItem() {
      super(new Properties());
   }

   @Override
   public InteractionResultHolder<ItemStack> use(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand
   ) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
         List<Aspect> primals = Aspect.getPrimalAspects();
         PlayerKnowledge knowledge = PlayerKnowledge.of(player);
         for (Aspect aspect : primals) {
            // 1 или 2 единицы — как в оригинале: nextInt(2) + 1
            short amount = (short)(level.random.nextInt(2) + 1);
            knowledge.addAspectPool(aspect, amount);
            ResearchManager.scheduleSave(player);
            PacketHandler.sendTo(
               new PacketAspectPool(aspect.getTag(), amount, knowledge.getAspectPoolFor(aspect)), serverPlayer
            );
         }
      }

      // Порядок оригинала: сначала тратится предмет, потом начисляются аспекты.
      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }

      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
   }

   @Override
   public void appendHoverText(
      @NotNull ItemStack stack,
      @Nullable TooltipContext context,
      @NotNull List<Component> components,
      @NotNull TooltipFlag flag
   ) {
      components.add(
         Component.translatable("item.ItemEldritchObject.text.1").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE))
      );
   }

   public void verifyComponentsAfterLoad(@NotNull ItemStack stack) {
      super.verifyComponentsAfterLoad(stack);
      CurioItem.stripInvalidDamage(stack);
   }
}