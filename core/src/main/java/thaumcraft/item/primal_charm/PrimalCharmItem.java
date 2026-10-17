package thaumcraft.item.primal_charm;

import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.entities.brainless.aspect_orb.AspectOrbEntity;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.playerdata.PacketResearchComplete;
import thaumcraft.research.ResearchManager;

/**
 * Амулет примальных аспектов.
 *
 * <p>Порт {@code ItemResource.func_77663_a} (1.7.10, строки 122-163). В оригинале
 * амулет — это {@code itemResource} с метаданными 15, поэтому условие там
 * {@code stack.getItemDamage() == 15}; в порте это отдельный предмет, условие
 * выполняется всегда, а стак делится как обычный предмет.
 *
 * <p>Амулет пассивный: своего действия по ПКМ в оригинале нет вообще
 * ({@code func_77648_a} обрабатывает только метаданные 1, то есть нитор), а всё
 * происходит, пока стак лежит в инвентаре. Шелест в подсказке — отдельная
 * мистификация, к игровой механике отношения не имеет.
 */
public class PrimalCharmItem extends Item {
   public PrimalCharmItem() {
      super(new Properties());
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      Random rand = new Random(stack.hashCode() + (level == null ? 0L : level.getGameTime() / 120L));
      int r = rand.nextInt(200);
      if (r < 25) {
         components.add(Component.translatable("tc.primalcharm." + rand.nextInt(5)).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
      }
   }

   /**
    * Пассивная выдача, порт {@code ItemResource.func_77663_a}.
    *
    * <p>Раз в 20000 тиков на стак: 20 шансов сферы одного из шести основных
    * аспектов (вис 1), и отдельно ровно одно значение — 42 — шепот и выдача
    * скрытого исследования {@code focus_primal}. Ветки взаимоисключающие,
    * как в оригинале (там {@code if / else if}).
    */
   @Override
   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
      super.inventoryTick(stack, level, entity, slotId, isSelected);
      tickCharm(level, entity);
   }

   /**
    * Сам бросок, вынесенный отдельно от {@link #inventoryTick}.
    *
    * <p>Отдельно нужно потому, что шанс выдачи — 20/20000 и 1/20000 за стак за
    * тик: чтобы увидеть шепот {@code focus_primal} естественным путём, нужны
    * десятки тысяч тиков. Тесты крутят этот метод напрямую (мост
    * {@code /charm/tick}), а связь с {@code inventoryTick} проверяют отдельно — по
    * тому, что сферы действительно падают из инвентаря.
    */
   public static void tickCharm(@NotNull Level level, @NotNull Entity entity) {
      if (level.isClientSide()) {
         return;
      }

      int r = level.random.nextInt(20000);

      if (r < 20) {
         Aspect aspect = switch (level.random.nextInt(6)) {
            case 0 -> Aspect.AIR;
            case 1 -> Aspect.EARTH;
            case 2 -> Aspect.FIRE;
            case 3 -> Aspect.WATER;
            case 4 -> Aspect.ORDER;
            default -> Aspect.ENTROPY;
         };

         AspectOrbEntity orb = new AspectOrbEntity(level, entity.getX(), entity.getY(), entity.getZ(), aspect, 1);
         level.addFreshEntity(orb);
      } else if (r == 42 && entity instanceof ServerPlayer player
         && !ResearchManager.isResearchComplete(player, "focus_primal")
         && !ResearchManager.isResearchComplete(player, "@focus_primal")) {
         MutableComponent whisper = Component.literal("§5§o").append(Component.translatable("tc.primalcharm.trigger"));
         player.sendSystemMessage(whisper);
         PacketHandler.sendTo(new PacketResearchComplete("@focus_primal"), player);
         thaumcraft.Thaumcraft.setup.getResearchManager().completeResearch(player, "@focus_primal");
         ResearchManager.scheduleSave(player);
      }
   }

   /**
    * В оригинале ПКМ по амулету ничего не делает — предмет пассивный. Раньше в
    * порте здесь был вызов, который просто съедал стак, ничего не выдавая.
    */
   @NotNull
   @Override
   public InteractionResultHolder<ItemStack> use(
      @NotNull Level level, @NotNull Player player, @NotNull net.minecraft.world.InteractionHand hand
   ) {
      ItemStack stack = player.getItemInHand(hand);
      if (!player.isCreative()) {
         level.playSound(player, player, SoundEvents.CROSSBOW_SHOOT, SoundSource.AMBIENT, 0.3F,
            0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
      }

      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
   }
}
