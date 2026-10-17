package thaumicenergistics.ae2.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.EncodedPatternItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.util.ItemStackData;

public class CruciblePatternItem extends EncodedPatternItem<TECruciblePattern> {
   public CruciblePatternItem() {
      super(new Properties().stacksTo(1), CruciblePatternItem::decodePattern, null);
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
   public TECruciblePattern decode(AEItemKey what, Level level) {
      return decodePattern(what, level);
   }

   @Nullable
   private static TECruciblePattern decodePattern(AEItemKey what, Level level) {
      if (what != null && ItemStackData.hasTag(what.getReadOnlyStack())) {
         try {
            return new TECruciblePattern(what, level);
         } catch (Exception var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   public ItemStack encode(ItemStack catalyst, ItemStack output, boolean allowSubstitutes, Level level) {
      ItemStack stack = new ItemStack(this);
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      PatternEncoding.encodePattern(tag, new ItemStack[]{catalyst}, output, allowSubstitutes, level.registryAccess());
      ItemStackData.setTag(stack, tag);
      return stack;
   }

   private boolean attemptRecovery(CompoundTag tag, Level level) {
      GenericStack[] ingredients = PatternEncoding.getCraftingInputs(tag, level.registryAccess());
      ItemStack product = PatternEncoding.getCraftingResult(tag, level.registryAccess());
      if (!product.isEmpty() && ingredients.length != 0) {
         AEItemKey catalystKey = (AEItemKey)ingredients[0].what();
         ItemStack catalyst = catalystKey.toStack();

         for (RecipeHolder<CrucibleRecipe> holder : level.getRecipeManager().getAllRecipesFor(CrucibleRecipe.TYPE)) {
            CrucibleRecipe recipe = (CrucibleRecipe)holder.value();
            if (recipe.catalystMatches(catalyst) && ItemStackData.sameItemSameTags(product, recipe.getResultItem())) {
               PatternEncoding.encodePattern(tag, new ItemStack[]{catalyst}, product, PatternEncoding.canSubstitute(tag), level.registryAccess());
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
