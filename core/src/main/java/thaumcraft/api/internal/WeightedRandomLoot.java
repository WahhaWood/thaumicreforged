package thaumcraft.api.internal;

import java.util.ArrayList;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WeightedRandomLoot implements WeightedEntry {
   public static ArrayList<WeightedRandomLoot> lootBagCommon = new ArrayList<>();
   public static ArrayList<WeightedRandomLoot> lootBagUncommon = new ArrayList<>();
   public static ArrayList<WeightedRandomLoot> lootBagRare = new ArrayList<>();
   public ItemStack item;
   public Weight weight;

   public WeightedRandomLoot(ItemStack item, int weight) {
      this.item = item;
      this.weight = Weight.of(weight);
   }

   @NotNull
   public Weight getWeight() {
      return this.weight;
   }
}
