package thaumcraft.recipe;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.INoRemoveEnchant;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModRecipes;
import thaumcraft.util.ItemStackData;

public class SpellClothRecipe extends CustomRecipe {
   public SpellClothRecipe(ResourceLocation id, CraftingBookCategory category) {
      super(category);
   }

   public SpellClothRecipe(CraftingBookCategory category) {
      super(category);
   }

   public boolean matches(@NotNull CraftingInput inv, @NotNull Level level) {
      boolean foundCloth = false;
      boolean foundTarget = false;

      for (int i = 0; i < inv.size(); i++) {
         ItemStack stack = inv.getItem(i);
         if (!stack.isEmpty()) {
            if (stack.is((Item)ModItems.SPELL_CLOTH.get())) {
               if (foundCloth) {
                  return false;
               }

               foundCloth = true;
            } else {
               boolean isEnchantedItem = stack.isEnchanted() || stack.is(Items.ENCHANTED_BOOK);
               boolean isForbidden = stack.getItem() instanceof INoRemoveEnchant;
               if (!isEnchantedItem || isForbidden) {
                  return false;
               }

               if (foundTarget) {
                  return false;
               }

               foundTarget = true;
            }
         }
      }

      return foundCloth && foundTarget;
   }

   @NotNull
   public ItemStack assemble(@NotNull CraftingInput inv, @NotNull Provider access) {
      ItemStack targetStack = ItemStack.EMPTY;

      for (int i = 0; i < inv.size(); i++) {
         ItemStack stack = inv.getItem(i);
         if (!stack.isEmpty() && !stack.is((Item)ModItems.SPELL_CLOTH.get())) {
            targetStack = stack;
            break;
         }
      }

      if (targetStack.isEmpty()) {
         return ItemStack.EMPTY;
      } else if (targetStack.is(Items.ENCHANTED_BOOK)) {
         return new ItemStack(Items.BOOK);
      } else {
         ItemStack result = targetStack.copy();
         result.setCount(1);
         if (ItemStackData.hasTag(result)) {
            CompoundTag tag = ItemStackData.getOrCreateTag(result);
            tag.remove("Enchantments");
            tag.remove("StoredEnchantments");
            tag.remove("RepairCost");
            if (tag.isEmpty()) {
               ItemStackData.setTag(result, null);
            }
         }

         return result;
      }
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= 2;
   }

   @NotNull
   public RecipeSerializer<?> getSerializer() {
      return (RecipeSerializer<?>)ModRecipes.SPELL_CLOTH_SERIALIZER.get();
   }
}
