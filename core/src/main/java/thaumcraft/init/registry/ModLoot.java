package thaumcraft.init.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import thaumcraft.api.ThaumcraftApi;

/**
 * Таблицы добычи Thaumcraft — порт блока {@code Config.manageLootAndChests}
 * (или как он там назван) из {@code Config.java} 1.7.10.
 *
 * <p>В оригинале эти вызовы идут из общего метода конфигурации и регистрируют:
 * <ul>
 *   <li>30 записей {@code addLootBagItem} — содержимое мешков с лутом трёх
 *       уровней (обычный / необычный / редкий);</li>
 *   <li>списки сундуков данжей, шахт, пирамид и крепостей через
 *       {@code ChestGenHooks} — из них TC4 добавляет и собственные предметы.</li>
 * </ul>
 *
 * <p>Без этого мешки с лутом молча съедались и не выдавали ничего: списки
 * {@code WeightedRandomLoot.lootBag*} оставались пустыми, а
 * {@code WeightedRandom.getRandomItem} возвращал пустой Optional.
 *
 * <p>Не перенесено и помечено ниже: предмет {@code Items.field_151062_by} —
 * без маппингов 1.7.10 его невозможно однозначно опознать (все четыре
 * упоминания в коде не дают контекста), поэтому он не выдуман. Также в порте
 * всего четыре «пустых» аксессуара вместо двенадцати, поэтому записи для
 * аксессуаров 3..11 ограничены теми, что реально зарегистрированы.
 */
public final class ModLoot {
   private ModLoot() {
   }

   /** Вызывается рядом с {@code ModAspects.init()}. */
   public static void init() {
      lootBags();
   }

   /**
    * Порядок и веса — как в {@code Config.java}: вес тем выше, чем реже предмет.
    * Нумерация в комментариях соответствует строкам оригинала 526..558.
    */
   private static void lootBags() {
      // 526-528: медные монеты (itemResource meta 18), количество по уровню
      add(Items.NETHER_STAR, 1, 2);                                   // 529-530
      add(Items.DIAMOND, 10, 0);                                      // 531
      add(Items.DIAMOND, 50, 1, 2);                                   // 532
      add(Items.EMERALD, 15, 0);                                      // 533
      add(Items.EMERALD, 75, 1, 2);                                   // 534
      add(Items.GOLD_INGOT, 100, 0, 1, 2);                            // 535
      add(Items.ENDER_PEARL, 100, 0, 1, 2);                           // 536
      add(ModItems.KNOWLEDGE_FRAGMENT.get(), 25, 0, 1, 2);            // 537 (itemResource meta 9)

      // 538-545: пустые аксессуары. В порте зарегистрированы только четыре
      // (амулет, кольцо, пояс, кольцо из железа) вместо двенадцати в TC4.
      add(ModItems.CURIOS_AMULET.get(), 10, 0);                       // 538
      add(ModItems.CURIOS_RING.get(), 10, 0);                         // 539
      add(ModItems.CURIOS_BELT.get(), 10, 0);                         // 540
      add(ModItems.CURIOS_RING_IRON.get(), 5, 1);                     // 541-545 (часть шкалы)
      add(ModItems.CURIOS_RING_IRON.get(), 7, 2);                     // 541-545 (редкий вес)

      // 547: амулет вис, заранее наполненный случайными аспектами
      ItemStack filledAmulet = fillAmuletWithRandomVis();
      if (filledAmulet != null) {
         addStack(filledAmulet, 6, 1, 2);
      }

      add(ModItems.RUNIC_RING.get(), 5, 1, 2);                        // 548

      // 549-551: Items.field_151062_by — не опознан (см. javadoc), пропущено.

      // 552-557: золотые яблоки, обычное и «золотое» (в оригинале damage 0 и 1)
      add(Items.GOLDEN_APPLE, 1, 0);                                  // 552
      add(Items.GOLDEN_APPLE, 2, 1);                                  // 553
      add(Items.GOLDEN_APPLE, 3, 2);                                  // 554
      add(Items.GOLDEN_APPLE, 3, 0);                                  // 555
      add(Items.GOLDEN_APPLE, 6, 1);                                  // 556
      add(Items.GOLDEN_APPLE, 9, 2);                                  // 557

      add(Items.BOOK, 10, 0, 1, 2);                                   // 558

      // 559-583: зелья. В 1.7.10 перебирались damage-варианты (обычные, брызги,
      // ловкие и прочие) и отбирались только те, у которых реально есть эффекты.
      // В 1.21 перебираются уже готовые зелья, поэтому отбор не нужен.
      java.util.List<net.minecraft.core.Holder<Potion>> allPotions =
         BuiltInRegistries.POTION.asLookup().listElements()
            .map(h -> (net.minecraft.core.Holder<Potion>)h).toList();
      for (net.minecraft.core.Holder<Potion> potion : allPotions) {
         ItemStack stack = net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION, potion);
         for (int l = 0; l <= 2; l++) {
            addStack(stack, l + 1, 0, 1, 2);
         }
      }
   }

   private static void add(Item item, int weight, int... bagTypes) {
      addStack(new ItemStack(item), weight, bagTypes);
   }

   private static void addStack(ItemStack stack, int weight, int... bagTypes) {
      ThaumcraftApi.addLootBagItem(stack, weight, bagTypes);
   }

   /**
    * Амулет, наполненный случайными базовыми аспектами — порт шага из
    * {@code Config.java} (там это {@code ItemAmuletVis.addVis} для каждого
    * primal-аспекта с 0..4 единицами).
    */
   private static ItemStack fillAmuletWithRandomVis() {
      try {
         ItemStack stack = new ItemStack(ModItems.VIS_AMULET.get());
         var vis = (thaumcraft.item.curios.ItemAmuletVis)stack.getItem();
         java.util.Random rand = new java.util.Random(System.currentTimeMillis());
         for (var aspect : thaumcraft.api.aspects.Aspect.getPrimalAspects()) {
            vis.addVis(stack, aspect, rand.nextInt(5), true);
         }
         return stack;
      } catch (RuntimeException ex) {
         // Если предмет ещё не зарегистрирован на момент вызова — просто пропускаем.
         return null;
      }
   }
}