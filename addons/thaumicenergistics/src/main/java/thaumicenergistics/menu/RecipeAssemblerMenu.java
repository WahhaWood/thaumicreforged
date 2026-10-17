package thaumicenergistics.menu;

import thaumicenergistics.ae2.recipe.RecipeAssembler;

/** Menu of an assembler whose recipes the player switches on and off (see AssemblerTogglePacket). */
public interface RecipeAssemblerMenu {
   RecipeAssembler recipeAssembler();
}
