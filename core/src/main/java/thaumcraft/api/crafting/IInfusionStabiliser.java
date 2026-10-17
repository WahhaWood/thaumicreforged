package thaumcraft.api.crafting;

/**
 * Блок-стабилизатор инфузии. Возвращает вес блока в зеркальной паре:
 * пара из двух таких блоков добавит стабильности вдвое больше.
 *
 * <p>Дефолт — свеча (2.5). Кластеры кристалла и черепа переопределяют вес
 * в {@code InfusionInstabilityHandler#getStabilityValue} (10 и 20).
 */
public interface IInfusionStabiliser {
   default double getStabilityValue() {
      return 2.5;
   }
}
