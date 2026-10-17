package thaumcraft.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.armor.ichor_gem.ItemIchorArmorGem;

/**
 * Бессмертие полного сета пробуждённой ихоровой брони.
 *
 * <p>Проверяется сквозным путём: урон наносится через {@code hurt()}, то есть
 * событие {@code LivingDeathEvent} бросается тем же способом, что и в игре, а не
 * вручную в шину.
 *
 * <p>Игрок создаётся через {@code makeMockPlayer}, а не
 * {@code makeMockServerPlayerInLevel}: последний добавляет игрока в список игроков,
 * что вызывает синк датапака, а на нём megacells пытается отправить клиентский
 * пакет и роняет тест чужой ошибкой ещё до первой проверки.
 */
@GameTestHolder(Thaumcraft.MODID)
public class GemArmorImmortalityGameTest {

   @GameTest
   public static void fullSetSurvivesLethalDamage(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      equipFullSet(player);

      helper.assertTrue(ItemIchorArmorGem.isFullSetWorn(player), "полный комплект должен распознаваться");

      player.hurt(player.level().damageSources().genericKill(), 1000.0F);

      helper.assertTrue(player.isAlive(), "полный сет обязан пережить смертельный урон");
      helper.assertTrue(player.getHealth() > 0.0F, "после спасения здоровье должно быть восстановлено");

      helper.succeed();
   }

   @GameTest
   public static void incompleteSetDoesNotGrantImmortality(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.ICHOR_GEM_HELMET.get()));
      player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.ICHOR_GEM_CHEST.get()));
      player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.ICHOR_GEM_LEGGINGS.get()));
      // ботинки пусты — три части это ещё не комплект

      helper.assertFalse(ItemIchorArmorGem.isFullSetWorn(player), "три части не должны считаться комплектом");

      player.hurt(player.level().damageSources().genericKill(), 1000.0F);

      helper.assertFalse(player.isAlive(), "неполный сет не должен спасать владельца");

      helper.succeed();
   }

   @GameTest
   public static void disabledPieceRevokesImmortality(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      equipFullSet(player);
      // Shift+ПКМ по вещи гасит способности этой части
      ItemIchorArmorGem.toggleEnabled(player.getItemBySlot(EquipmentSlot.FEET));

      helper.assertFalse(ItemIchorArmorGem.isFullSetWorn(player), "выключенная часть должна ломать бонус сета");

      player.hurt(player.level().damageSources().genericKill(), 1000.0F);

      helper.assertFalse(player.isAlive(), "сет с выключенной частью не должен спасать владельца");

      helper.succeed();
   }

   /**
    * Первое спасение в свежем мире обязано быть полноценным: с частицами, звуком и
    * сообщением, а не тихим подниманием до половины здоровья.
    *
    * <p>Регрессия, которую ловит тест: {@code getLong} на незаписанном ключе даёт 0, и в
    * мире моложе {@code SAVE_COOLDOWN} тиков условие {@code now − 0 < 100} истинно — первое
    * спасение уходило в ветку кулдауна и не запускало кулдаун вовсе. Здоровье игрока заранее
    * сбито, чтобы отличить «подняли до полного» от «оставили как есть».
    */
   @GameTest
   public static void firstSaveInFreshWorldIsFull(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      equipFullSet(player);
      player.setHealth(1.0F);

      helper.assertFalse(
         player.getPersistentData().contains("thaumcraft.gem_armor_last_save"),
         "до первого спасения ключ кулдауна не должен существовать"
      );

      player.hurt(player.level().damageSources().genericKill(), 1000.0F);

      helper.assertTrue(player.isAlive(), "сет должен спасти владельца");
      helper.assertTrue(
         player.getHealth() == player.getMaxHealth(),
         "первое спасение обязано восстановить здоровье полностью, а не до половины"
      );
      helper.assertTrue(
         player.getPersistentData().contains("thaumcraft.gem_armor_last_save"),
         "первое спасение обязано записать ключ кулдауна"
      );

      helper.succeed();
   }

   private static void equipFullSet(Player player) {
      player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.ICHOR_GEM_HELMET.get()));
      player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.ICHOR_GEM_CHEST.get()));
      player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.ICHOR_GEM_LEGGINGS.get()));
      player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.ICHOR_GEM_BOOTS.get()));
   }
}