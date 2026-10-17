package thaumicenergistics.ae2.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.EncodedPatternItem;
import java.util.ArrayList;
import java.util.Arrays;
import thaumicenergistics.util.InfusionRecipeHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.util.ItemStackData;

public class InfusionPatternItem extends EncodedPatternItem<TEInfusionPattern> {
   public InfusionPatternItem() {
      super(new Properties().stacksTo(1), InfusionPatternItem::decodePattern, null);
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
   public TEInfusionPattern decode(AEItemKey what, Level level) {
      return decodePattern(what, level);
   }

   @Nullable
   private static TEInfusionPattern decodePattern(AEItemKey what, Level level) {
      if (what != null && ItemStackData.hasTag(what.getReadOnlyStack())) {
         try {
            return new TEInfusionPattern(what, level);
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
      PatternEncoding.encodePattern(tag, in, out, allowSubstitutes, level.registryAccess());
      ItemStackData.setTag(stack, tag);
      return stack;
   }

   private boolean attemptRecovery(CompoundTag tag, Level level) {
      GenericStack[] ingredients = PatternEncoding.getCraftingInputs(tag, level.registryAccess());
      ItemStack product = PatternEncoding.getCraftingResult(tag, level.registryAccess());
      if (!product.isEmpty() && ingredients.length != 0) {
         AEItemKey centerKey = (AEItemKey)ingredients[0].what();
         ItemStack center = centerKey.toStack();
         ArrayList<ItemStack> inputs = new ArrayList<>();

         for (int i = 1; i < ingredients.length; i++) {
            AEItemKey itemKey = (AEItemKey)ingredients[i].what();
            inputs.add(itemKey.toStack());
         }

         InfusionRecipe potentialRecipe = InfusionRecipeHelper.INSTANCE.findMatchingRecipe(inputs, center, level);
         if (potentialRecipe != null) {
            ItemStack recipeOutput = potentialRecipe.getResultItem();
            if (!recipeOutput.isEmpty() && ItemStackData.sameItemSameTags(product, recipeOutput)) {
               ItemStack[] inStacks = Arrays.stream(ingredients)
                  .map(stack -> stack.what() instanceof AEItemKey key ? key.toStack() : ItemStack.EMPTY)
                  .toArray(ItemStack[]::new);
               PatternEncoding.encodePattern(tag, inStacks, product, PatternEncoding.canSubstitute(tag), level.registryAccess());
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
