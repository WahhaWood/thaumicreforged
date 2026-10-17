package thaumicenergistics.ae2.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.EncodedPatternItem;
import java.util.Arrays;
import thaumicenergistics.util.ArcaneRecipeHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.util.ItemStackData;

public class ArcanePatternItem extends EncodedPatternItem<TEArcanePattern> {
   public ArcanePatternItem() {
      super(new Properties().stacksTo(1), ArcanePatternItem::decodePattern, null);
   }

   @Nullable
   public IPatternDetails decode(ItemStack stack, Level level, boolean tryRecovery) {
      if (stack.getItem() == this && ItemStackData.hasTag(stack) && level != null) {
         IPatternDetails result = this.decode(AEItemKey.of(stack), level);
         if (tryRecovery && result == null) {
            CompoundTag tag = ItemStackData.getOrCreateTag(stack);
            if (this.attemptRecovery(tag, level)) {
               ItemStackData.setTag(stack, tag);
               result = this.decode(AEItemKey.of(stack), level);
            }
         }

         return result;
      } else {
         return null;
      }
   }

   @Nullable
   public TEArcanePattern decode(AEItemKey what, Level level) {
      return decodePattern(what, level);
   }

   @Nullable
   private static TEArcanePattern decodePattern(AEItemKey what, Level level) {
      if (what != null && ItemStackData.hasTag(what.getReadOnlyStack())) {
         try {
            return new TEArcanePattern(what, level);
         } catch (Exception var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   public ItemStack encode(ItemStack[] in, ItemStack out, boolean allowSubstitutes, Level level) {
      ItemStack stack = new ItemStack(this);
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      PatternEncoding.encodeGridPattern(tag, in, out, allowSubstitutes, level.registryAccess());
      ItemStackData.setTag(stack, tag);
      return stack;
   }

   private boolean attemptRecovery(CompoundTag tag, Level level) {
      GenericStack[] ingredients = PatternEncoding.getCraftingInputs(tag, level.registryAccess());
      ItemStack product = PatternEncoding.getCraftingResult(tag, level.registryAccess());
      if (!product.isEmpty() && ingredients.length != 0) {
         ItemStack[] testInputs = new ItemStack[ingredients.length];

         for (int i = 0; i < ingredients.length; i++) {
            if (ingredients[i] != null && ingredients[i].what() instanceof AEItemKey key) {
               testInputs[i] = key.toStack();
            } else {
               testInputs[i] = ItemStack.EMPTY;
            }
         }

         IArcaneRecipe potentialRecipe = ArcaneRecipeHelper.INSTANCE.findMatchingRecipe(testInputs, level);
         if (potentialRecipe != null) {
            ItemStack recipeOutput = ArcaneRecipeHelper.INSTANCE.getRecipeOutput(potentialRecipe, testInputs, level);
            if (!recipeOutput.isEmpty() && ItemStackData.sameItemSameTags(product, recipeOutput)) {
               ItemStack[] inStacks = Arrays.stream(ingredients)
                  .map(s -> s != null && s.what() instanceof AEItemKey key ? key.toStack() : ItemStack.EMPTY)
                  .toArray(ItemStack[]::new);
               PatternEncoding.encodeGridPattern(tag, inStacks, product, PatternEncoding.canSubstitute(tag), level.registryAccess());
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
