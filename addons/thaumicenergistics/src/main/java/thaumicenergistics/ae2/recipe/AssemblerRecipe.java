package thaumicenergistics.ae2.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;

/**
 * One Thaumcraft recipe as an assembler sees it: item inputs (identical ingredients condensed into a
 * count), the aspect cost and one output. For the crucible and infusion the aspects are essentia taken
 * from the network, for the arcane assembler they are the vis the assembler itself spends.
 */
public record AssemblerRecipe(ResourceLocation id, AssemblerKind kind, List<Entry> items, AspectList aspects, ItemStack output) {
   public record Entry(Ingredient ingredient, int count) {
   }

   @Nullable
   static AssemblerRecipe of(
      ResourceLocation id, AssemblerKind kind, List<Ingredient> ingredients, boolean skipEmpty, @Nullable AspectList aspects, ItemStack result
   ) {
      if (result.isEmpty()) {
         return null;
      }

      List<Entry> items = new ArrayList<>();
      for (Ingredient ingredient : ingredients) {
         if (ingredient.isEmpty()) {
            if (skipEmpty) {
               continue;
            }

            return null;
         }

         int same = -1;
         for (int i = 0; i < items.size() && same < 0; i++) {
            if (items.get(i).ingredient().equals(ingredient)) {
               same = i;
            }
         }

         if (same >= 0) {
            items.set(same, new Entry(ingredient, items.get(same).count() + 1));
         } else {
            items.add(new Entry(ingredient, 1));
         }
      }

      if (items.isEmpty()) {
         return null;
      }

      return new AssemblerRecipe(id, kind, List.copyOf(items), AssemblerKind.copyOf(aspects), result.copy());
   }

   /** The item the GUI shows for this recipe. */
   public ItemStack icon() {
      return this.output;
   }
}
