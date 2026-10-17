package thaumicenergistics.ae2.pattern;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import java.util.ArrayList;
import java.util.Arrays;
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
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.util.ItemStackData;

public class TEInfusionPattern extends AbstractAspectPattern {
   private final InfusionRecipe recipe;

   public TEInfusionPattern(CompoundTag tag, Level level) {
      this(PatternEncoding.definitionKey((Item)ModItems.INFUSION_PATTERN.get(), tag), level);
   }

   public TEInfusionPattern(AEItemKey definition, Level level) {
      super(definition);
      CompoundTag tag = Objects.requireNonNull(ItemStackData.getTag(definition.getReadOnlyStack()));
      this.sparseInputs = PatternEncoding.getCraftingInputs(tag, level.registryAccess());
      AEItemKey centerKey = (AEItemKey)this.sparseInputs[0].what();
      ItemStack testCenter = centerKey.toStack();
      ArrayList<ItemStack> testInputs = new ArrayList<>();

      for (int i = 1; i < this.sparseInputs.length; i++) {
         AEItemKey itemKey = (AEItemKey)this.sparseInputs[i].what();
         testInputs.add(itemKey.toStack());
      }

      InfusionRecipe found = null;

      for (RecipeHolder<InfusionRecipe> holder : level.getRecipeManager().getAllRecipesFor(InfusionRecipe.TYPE)) {
         InfusionRecipe r = (InfusionRecipe)holder.value();
         if (r.matches(testInputs, testCenter)) {
            found = r;
            break;
         }
      }

      this.recipe = found;
      if (this.recipe == null) {
         throw new IllegalStateException("The recipe " + Arrays.toString((Object[])this.sparseInputs) + " no longer matches the encoded input.");
      } else {
         ItemStack output = this.recipe.getResultItem();
         if (output.isEmpty()) {
            throw new IllegalStateException("The recipe " + Arrays.toString((Object[])this.sparseInputs) + " produced an empty item stack result.");
         } else {
            this.sparseOutputs = new GenericStack[]{Objects.requireNonNull(GenericStack.fromItemStack(output))};
            this.aspects = this.recipe.aspects;
            int aspectCount = this.aspects != null ? this.aspects.size() : 0;
            this.inputs = new AbstractAspectPattern.Input[this.sparseInputs.length + aspectCount];

            int j;
            for (j = 0; j < this.sparseInputs.length; j++) {
               this.inputs[j] = new AbstractAspectPattern.Input(this, j, this.sparseInputs[j]);
            }

            if (this.aspects != null) {
               for (Entry<Aspect, Integer> entry : this.aspects.aspects.entrySet()) {
                  this.inputs[j++] = new AbstractAspectPattern.Input(this, new GenericStack(AEAspectKey.of(entry.getKey()), (long)entry.getValue().intValue()));
               }
            }
         }
      }
   }

   @Override
   protected Object getRecipeIngredient(int slot) {
      if (this.recipe != null && slot < this.sparseInputs.length) {
         if (slot == 0) {
            return this.recipe.central;
         }

         int componentIndex = slot - 1;
         if (componentIndex < this.recipe.components.size()) {
            return this.recipe.components.get(componentIndex);
         }
      }

      return null;
   }
}
