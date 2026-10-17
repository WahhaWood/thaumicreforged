package thaumcraft.item.consumables;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Съедобный мясной наггетс (порт {@code ItemNuggetEdible} из TC4 1.7.10).
 *
 * <p>В 1.7.10 наггетс съедался за 10 тиков вместо 32 (быстрая еда, 0.5 с).
 * Предмет не имеет прочности и стакается до 64.
 */
public class ItemNuggetEdible extends Item {
   public ItemNuggetEdible(Properties properties) {
      super(properties);
   }

   @Override
   public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
      return 10;
   }
}
