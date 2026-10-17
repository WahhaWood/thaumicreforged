package thaumicenergistics.ae2.recipe;

import net.minecraft.world.item.Item;

/** Definition of an {@link AssemblerPattern}: never in a tab, no recipe, only ever lives inside AE2's crafting data. */
public class RecipePatternItem extends Item {
   public RecipePatternItem() {
      super(new Item.Properties().stacksTo(1));
   }
}
