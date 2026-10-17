package thaumcraft.event.common;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

@EventBusSubscriber(
   modid = "thaumcraft"
)
public class ThaumcraftVillagerEvents {
   /**
    * Сделки с жителями — заглушка, и это осознанно (решение E5 в
    * {@code notes/00-decisions.md}).
    *
    * <p>В 1.7.10 за торговлей тянулось три вещи: {@code VillageBankerManager} и
    * {@code VillageWizardManager} с 12 и 15 сделками, постройки дома банкира и
    * башни мага, и новая профессия жителя на {@code IVillageCreationHandler}.
    * Последнее в 1.21.1 не воспроизводится без POI, а постройки у нас удалены
    * пользователем: они не входят в TC4 и выглядели плохо. Новая профессия без
    * дома просто не появилась бы в мире.
    *
    * <p>Списки сделок приведены ниже как справка — если решение изменится,
    * переносить отсюда.
    */
   @SubscribeEvent
   public static void onVillagerTrades(VillagerTradesEvent event) {
      // Банкир (VillageBankerManager.manipulateTradesForVillager):
      //   20-22 монеты → 1 медь (itemResource 18)
      //   2-3 монеты   → 1 золотой слиток
      //   6-8 монет    → 1 золотой блок
      //   3-4 монеты   → 1 бумагу
      //   7-9 монет    → 1 сахар
      //   16-20 монет  → 1 золотую иглу
      //   9-12 монет   → 1 шерсть
      //   2-3 монеты   → 1 кожу
      //   22-24 монеты → 1 ствол
      //   6-8 монет    → 1 корень
      //   10-12 монет  → 2 жира (itemResource 2)
      //   25-32 монеты → 1 зелью крови
      //
      // Маг (VillageWizardManager.manipulateTradesForVillager):
      //   20-22 монеты → 1 книгу
      //   1 изумруд    → 1 фрагмент знания
      //   4-6 монет    → 1 золотой слиток
      //   1 изумруд    → 1 алюментум
      //   4-6 монет    → 1 нитор
      //   1 изумруд    → 1 уголь
      //   24-31 куриных ногтей → 1 изумруд
      //   4-6 сахара   → 1 фрагмент знания
      //   24-31 бычьих ногтей → 1 изумруд
      //   1 изумруд    → 2-3 осколка
      //   1 изумруд    → 1-2 магических боба
      //   5-7 монет    → 1 соль от помешательства
      //   5-7 монет    → 1 руническое кольцо
      //   5-7 монет    → 1 амулет вис
      //   5-7 монет    → пустой аксессуар (3-8)
   }
}
