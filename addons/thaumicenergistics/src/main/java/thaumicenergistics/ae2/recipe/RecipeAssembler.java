package thaumicenergistics.ae2.recipe;

import appeng.api.networking.IManagedGridNode;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** A block entity that offers Thaumcraft recipes to the ME network through an {@link AssemblerRecipes}. */
public interface RecipeAssembler {
   AssemblerRecipes recipeBook();

   IManagedGridNode getMainNode();

   @Nullable
   Level getLevel();

   void markForUpdate();

   void setChanged();
}
