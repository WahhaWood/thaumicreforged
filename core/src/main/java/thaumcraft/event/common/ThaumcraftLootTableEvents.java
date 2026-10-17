package thaumcraft.event.common;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import thaumcraft.init.registry.ModItems;

/**
 * Добавление предметов Thaumcraft в ванильные сундуки — порт блока
 * {@code Config.manageLootAndChests} (1.7.10, строки 583-647), где 32 вызова
 * {@code ChestGenHooks.addItem} раскладывали списки по именам категорий сундуков.
 *
 * <p>Без этого половина добычи TC4 просто не существовала: мешки с лутом
 * наполнялись ({@code ModLoot}), а сами сундуки данжей, шахт, пирамид и крепостей
 * оставались чисто ванильными.
 *
 * <p>Оригинальные категории {@code ChestGenHooks} и таблицы 1.21.1 — разные
 * наборы, поэтому имена сопоставлены по смыслу:
 *
 * <table>
 *   <tr><td>1.7.10</td><td>1.21.1</td></tr>
 *   <tr><td>{@code dungeonChest}</td><td>{@code chests/spawn_bonus_chest}</td></tr>
 *   <tr><td>{@code pyramidJungleChest}</td><td>{@code chests/jungle_temple}</td></tr>
 *   <tr><td>{@code pyramidDesertyChest}</td><td>{@code chests/desert_pyramid}</td></tr>
 *   <tr><td>{@code mineshaftCorridor}</td><td>{@code chests/abandoned_mineshaft}</td></tr>
 *   <tr><td>{@code strongholdCorridor}</td><td>{@code chests/stronghold_corridor}</td></tr>
 *   <tr><td>{@code strongholdCrossing}</td><td>{@code chests/stronghold_crossing}</td></tr>
 *   <tr><td>{@code strongholdLibrary}</td><td>{@code chests/stronghold_library}</td></tr>
 *   <tr><td>{@code villageBlacksmith}</td><td>{@code village_armorer} +
 *       {@code village_weaponsmith}</td></tr>
 * </table>
 *
 * <p>Профессию кузнеца в 1.19.2–1.20.1 разделили на бронщика и оружейника, а
 * таблица {@code village_blacksmith} исчезла, поэтому запись идёт в обе новые.
 *
 * <p>Отличие в способе: в 1.7.10 предметы TC4 добавлялись в <em>тот же</em>
 * список, что и ванильный лут категории, и конкурировали с ним по весу. Здесь они
 * вынесены в отдельные пулы с теми же весами и диапазонами количества — состав и
 * частота совпадают, но броски независимы от ванильного пула.
 */
@EventBusSubscriber(modid = "thaumcraft")
public class ThaumcraftLootTableEvents {
   /** Записи одной группы: вес 1 у всех, как и в {@code WeightedRandomChestContent}. */
   private record Entry(ItemLike item, int weight) {
   }

   private static final ResourceLocation BONUS_CHEST = chest("spawn_bonus_chest");
   private static final ResourceLocation JUNGLE_TEMPLE = chest("jungle_temple");
   private static final ResourceLocation DESERT_PYRAMID = chest("desert_pyramid");
   private static final ResourceLocation MINESHAFT = chest("abandoned_mineshaft");
   private static final ResourceLocation STRONGHOLD_CORRIDOR = chest("stronghold_corridor");
   private static final ResourceLocation STRONGHOLD_CROSSING = chest("stronghold_crossing");
   private static final ResourceLocation STRONGHOLD_LIBRARY = chest("stronghold_library");
   /**
    * 1.7.10 {@code villageBlacksmith} — сундук кузнеца. В 1.20.1 профессию
    * разделили на две: кузнец-оружейник и кузнец-бронщик (появился в 1.19.2),
    * и таблица {@code village_blacksmith} исчезла. Поэтому запись идёт в обе
    * новые — иначе половина кузнец осталась бы без жира.
    */
   private static final List<ResourceLocation> VILLAGE_SMITHS = List.of(
      chest("village/village_armorer"),
      chest("village/village_weaponsmith")
   );

   /**
    * 1.7.10:583-592 — обычный лот. {@code itemLootbag} 0, {@code itemResource} 2 (жир)
    * и 6 (ткань).
    */
   private static List<Entry> commonLoot() {
      return List.of(
         entry(ModItems.LOOT_BAG.get()),
         entry(ModItems.MAGIC_TALLOW.get()),
         entry(ModItems.CLOTH.get())
      );
   }

   /**
    * 1.7.10:593-599 — необычный лот. Мешок 1 уровня, пустые аксессуары 0..2 и
    * фрагмент знания ({@code itemResource} 9).
    *
    * <p>Аксессуаров в TC4 двенадцать мета-вариантов, в порте зарегистрировано
    * четыре (см. {@code ModItems.CURIOS_*}), поэтому в список идут только они.
    */
   private static List<Entry> uncommonLoot() {
      return List.of(
         entry(ModItems.LOOT_BAG_UNCOMMON.get()),
         entry(ModItems.CURIOS_AMULET.get()),
         entry(ModItems.CURIOS_RING.get()),
         entry(ModItems.CURIOS_BELT.get()),
         entry(ModItems.KNOWLEDGE_FRAGMENT.get())
      );
   }

   /**
    * 1.7.10:600-612 — редкий лот. Мешок 2 уровня, Таумономикон, комплект
    * таумиевого снаряжения, руническое кольцо, пустые аксессуары 3..8 и амулет
    * вис с аспектами.
    */
   private static List<Entry> rareLoot() {
      List<Entry> list = new ArrayList<>();
      list.add(entry(ModItems.LOOT_BAG_RARE.get()));
      list.add(entry(ModItems.THAUMONOMICON.get()));
      list.add(entry(ModItems.THAUMIUM_SWORD.get()));
      list.add(entry(ModItems.THAUMIUM_PICKAXE.get()));
      list.add(entry(ModItems.THAUMIUM_AXE.get()));
      list.add(entry(ModItems.THAUMIUM_HOE.get()));
      list.add(entry(ModItems.RUNIC_RING.get()));
      // Аксессуары 3..8 в оригинале — кольца с заданным основным аспектом.
      // В порте вариант «кольцо из железа» хранит аспект в NBT, поэтому в список
      // попадает он.
      for (int i = 0; i < 6; i++) {
         list.add(entry(ModItems.CURIOS_RING_IRON.get()));
      }

      // Амулет вис. В 1.7.10 он попадал в сундук уже наполненным случайными
      // аспектами (Config.java:519-523), но в 1.21.1 из таблиц лута убрали
      // установку NBT целиком (нет SetNbtFunction), поэтому здесь он пустой.
      list.add(entry(ModItems.VIS_AMULET.get()));
      return list;
   }

   private static Entry entry(ItemLike item) {
      return new Entry(item, 1);
   }

   @SubscribeEvent
   public static void onLootTableLoad(LootTableLoadEvent event) {
      ResourceLocation name = event.getName();
      LootTable table = event.getTable();

      // dungeonChest / pyramidJungleChest / pyramidDesertyChest:
      //   обычный лот 3-5, необычный 2-4, редкий 1-1 (1.7.10:615-646).
      if (isOneOf(name, BONUS_CHEST, JUNGLE_TEMPLE, DESERT_PYRAMID)) {
         addPool(table, commonLoot(), 3, 5);
         addPool(table, uncommonLoot(), 2, 4);
         addPool(table, rareLoot(), 1, 1);
         return;
      }

      // mineshaftCorridor / strongholdCorridor / strongholdCrossing /
      // strongholdLibrary: обычный лот 3-4, необычный 2-3, редкий 1-1.
      if (isOneOf(name, MINESHAFT, STRONGHOLD_CORRIDOR, STRONGHOLD_CROSSING, STRONGHOLD_LIBRARY)) {
         addPool(table, commonLoot(), 3, 4);
         addPool(table, uncommonLoot(), 2, 3);
         addPool(table, rareLoot(), 1, 1);
      }

      // 1.7.10:648 — дополнительно фрагменты знаний в библиотеке крепости.
      if (STRONGHOLD_LIBRARY.equals(name)) {
         addPool(table, List.of(new Entry(ModItems.KNOWLEDGE_FRAGMENT.get(), 3)), 6, 20);
      }

      // 1.7.10:650 — кузнец в деревне держит жир.
      if (isOneOf(name, VILLAGE_SMITHS.toArray(new ResourceLocation[0]))) {
         addPool(table, List.of(new Entry(ModItems.MAGIC_TALLOW.get(), 1)), 3, 10);
      }
   }

   private static boolean isOneOf(ResourceLocation name, ResourceLocation... candidates) {
      for (ResourceLocation candidate : candidates) {
         if (candidate.equals(name)) {
            return true;
         }
      }

      return false;
   }

   private static ResourceLocation chest(String path) {
      return ResourceLocation.fromNamespaceAndPath("minecraft", "chests/" + path);
   }

   private static void addPool(LootTable table, List<Entry> entries, int min, int max) {
      if (entries.isEmpty()) {
         return;
      }

      LootPool.Builder pool = LootPool.lootPool().setRolls(UniformGenerator.between(min, max));
      for (Entry e : entries) {
         pool.add(LootItem.lootTableItem(e.item()).setWeight(e.weight()));
      }

      // addPool — патч NeoForge: сохраняет уже загруженные ванильные пулы,
      // в отличие от пересборки таблицы через LootTable.Builder.
      table.addPool(pool.build());
   }
}
