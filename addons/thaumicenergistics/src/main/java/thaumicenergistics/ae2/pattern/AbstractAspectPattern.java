package thaumicenergistics.ae2.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.util.ItemStackData;

public abstract class AbstractAspectPattern implements IPatternDetails {
   private final AEItemKey definition;
   private final boolean canSubstitute;
   protected IInput[] inputs;
   protected AspectList aspects;
   protected GenericStack[] sparseInputs;
   protected GenericStack[] sparseOutputs;

   public AbstractAspectPattern(AEItemKey definition) {
      this.definition = definition;
      this.canSubstitute = PatternEncoding.canSubstitute(Objects.requireNonNull(ItemStackData.getTag(definition.getReadOnlyStack())));
   }

   public AspectList getAspects() {
      return this.aspects;
   }

   public AEItemKey getDefinition() {
      return this.definition;
   }

   public IInput[] getInputs() {
      return this.inputs;
   }

   public GenericStack[] getSparseInputs() {
      return this.sparseInputs;
   }

   public List<GenericStack> getOutputs() {
      return Arrays.asList(this.sparseOutputs);
   }

   @Override
   public int hashCode() {
      return this.definition.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return obj != null && obj.getClass() == this.getClass() && ((AbstractAspectPattern)obj).definition.equals(this.definition);
   }

   @Nullable
   protected Object getRecipeIngredient(int slot) {
      return null;
   }

   public boolean canSubstitute() {
      return this.canSubstitute;
   }

   public boolean isItemValid(int slot, AEItemKey key, Level level) {
      if (this.canSubstitute) {
         return key == null ? this.sparseInputs[slot] == null : false;
      } else {
         return this.sparseInputs[slot] == null && key == null || this.sparseInputs[slot] != null && this.sparseInputs[slot].what().equals(key);
      }
   }

   public static class Input implements IInput {
      private final AbstractAspectPattern pattern;
      private final int slot;
      private final GenericStack[] possibleInputs;
      private final long multiplier;

      public Input(AbstractAspectPattern pattern, int slot, GenericStack condensedInput) {
         this.pattern = pattern;
         this.slot = slot;
         this.multiplier = condensedInput.amount();
         GenericStack itemInput = pattern.sparseInputs[slot];
         if (!pattern.canSubstitute()) {
            this.possibleInputs = new GenericStack[]{itemInput};
         } else {
            Object ingredient = pattern.getRecipeIngredient(slot);
            ItemStack[] matchingStacks = null;
            if (ingredient instanceof Ingredient ing) {
               matchingStacks = ing.getItems();
            } else if (ingredient instanceof ItemStack stack) {
               matchingStacks = new ItemStack[]{stack};
            } else if (ingredient instanceof ItemStack[] stacks) {
               matchingStacks = stacks;
            }

            if (matchingStacks != null && matchingStacks.length > 0) {
               this.possibleInputs = new GenericStack[matchingStacks.length + 1];
               this.possibleInputs[0] = itemInput;

               for (int i = 0; i < matchingStacks.length; i++) {
                  this.possibleInputs[i + 1] = GenericStack.fromItemStack(matchingStacks[i]);
               }
            } else {
               this.possibleInputs = new GenericStack[]{itemInput};
            }
         }
      }

      public Input(AbstractAspectPattern pattern, GenericStack condensedInput) {
         this.pattern = pattern;
         this.slot = -1;
         this.multiplier = condensedInput.amount();
         this.possibleInputs = new GenericStack[]{new GenericStack(condensedInput.what(), 1L)};
      }

      public GenericStack[] getPossibleInputs() {
         return this.possibleInputs;
      }

      public long getMultiplier() {
         return this.multiplier;
      }

      public boolean isValid(AEKey input, Level level) {
         if (input.matches(this.possibleInputs[0])) {
            return true;
         } else {
            if (this.slot != -1 && this.pattern.canSubstitute() && input instanceof AEItemKey itemKey && this.pattern.isItemValid(this.slot, itemKey, level)) {
               return true;
            }

            return false;
         }
      }

      @Nullable
      public AEKey getRemainingKey(AEKey template) {
         return null;
      }
   }
}
