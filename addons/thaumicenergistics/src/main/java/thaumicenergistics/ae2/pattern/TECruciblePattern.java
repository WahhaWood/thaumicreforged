package thaumicenergistics.ae2.pattern;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import java.util.Objects;
import java.util.Map.Entry;
import thaumicenergistics.ae2.storage.AEAspectKey;
import thaumicenergistics.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.util.ItemStackData;

public class TECruciblePattern extends AbstractAspectPattern {
   private final CrucibleRecipe recipe;

   public TECruciblePattern(CompoundTag tag, Level level) {
      this(PatternEncoding.definitionKey((Item)ModItems.CRUCIBLE_PATTERN.get(), tag), level);
   }

   public TECruciblePattern(AEItemKey definition, Level level) {
      super(definition);
      CompoundTag tag = Objects.requireNonNull(ItemStackData.getTag(definition.getReadOnlyStack()));
      this.sparseInputs = PatternEncoding.getCraftingInputs(tag, level.registryAccess());
      AEItemKey catalystKey = (AEItemKey)this.sparseInputs[0].what();
      ItemStack testCatalyst = catalystKey.toStack();
      ItemStack encodedOutput = PatternEncoding.getCraftingResult(tag, level.registryAccess());
      CrucibleRecipe found = null;

      for (RecipeHolder<CrucibleRecipe> holder : level.getRecipeManager().getAllRecipesFor(CrucibleRecipe.TYPE)) {
         CrucibleRecipe r = (CrucibleRecipe)holder.value();
         if (r.catalystMatches(testCatalyst) && ItemStackData.sameItemSameTags(encodedOutput, r.getResultItem())) {
            found = r;
            break;
         }
      }

      this.recipe = found;
      if (this.recipe == null) {
         throw new IllegalStateException("The crucible recipe for catalyst " + testCatalyst + " no longer matches the encoded input.");
      } else {
         ItemStack output = this.recipe.getResultItem();
         if (output.isEmpty()) {
            throw new IllegalStateException("The crucible recipe produced an empty item stack result.");
         } else {
            this.sparseOutputs = new GenericStack[]{Objects.requireNonNull(GenericStack.fromItemStack(output))};
            this.aspects = this.recipe.aspects;
            int aspectCount = this.aspects != null ? this.aspects.size() : 0;
            this.inputs = new AbstractAspectPattern.Input[1 + aspectCount];
            this.inputs[0] = new AbstractAspectPattern.Input(this, 0, this.sparseInputs[0]);
            if (this.aspects != null) {
               int j = 1;

               for (Entry<Aspect, Integer> entry : this.aspects.aspects.entrySet()) {
                  this.inputs[j++] = new AbstractAspectPattern.Input(this, new GenericStack(AEAspectKey.of(entry.getKey()), (long)entry.getValue().intValue()));
               }
            }
         }
      }
   }

   public CrucibleRecipe getRecipe() {
      return this.recipe;
   }

   public boolean isSimilar(CrucibleRecipe other) {
      return this.recipe == other;
   }

   @Override
   protected Object getRecipeIngredient(int slot) {
      return this.recipe != null && slot == 0 ? this.recipe.catalyst : null;
   }

   public static CrucibleRecipe findCrucibleRecipe(Level level, ItemStack catalyst, AspectList aspects, ItemStack output) {
      for (RecipeHolder<CrucibleRecipe> holder : level.getRecipeManager().getAllRecipesFor(CrucibleRecipe.TYPE)) {
         CrucibleRecipe recipe = (CrucibleRecipe)holder.value();
         if (recipe.matches(aspects, catalyst) && ItemStackData.sameItemSameTags(output, recipe.getResultItem())) {
            return recipe;
         }
      }

      return null;
   }
}
