package thaumicenergistics.ae2.pattern;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.util.ItemStackData;

public final class PatternEncoding {
   private static final String NBT_INPUTS = "in";
   private static final String NBT_OUTPUT = "out";
   private static final String NBT_SUBSTITUTE = "substitute";

   private PatternEncoding() {
   }

   public static void encodePattern(CompoundTag tag, ItemStack[] in, ItemStack out, boolean allowSubstitutes, Provider registries) {
      ListTag inputList = new ListTag();

      for (ItemStack stack : in) {
         if (stack != null && !stack.isEmpty()) {
            inputList.add(saveStack(stack, registries));
         }
      }

      tag.put("in", inputList);
      tag.put("out", saveStack(out, registries));
      tag.putBoolean("substitute", allowSubstitutes);
   }

   public static void encodeGridPattern(CompoundTag tag, ItemStack[] in, ItemStack out, boolean allowSubstitutes, Provider registries) {
      ListTag inputList = new ListTag();

      for (ItemStack stack : in) {
         CompoundTag itemTag = new CompoundTag();
         if (stack != null && !stack.isEmpty()) {
            itemTag = saveStack(stack, registries);
         }

         inputList.add(itemTag);
      }

      tag.put("in", inputList);
      tag.put("out", saveStack(out, registries));
      tag.putBoolean("substitute", allowSubstitutes);
   }

   public static GenericStack[] getCraftingInputs(CompoundTag tag, Provider registries) {
      ListTag inputList = tag.getList("in", 10);
      GenericStack[] inputs = new GenericStack[inputList.size()];

      for (int i = 0; i < inputList.size(); i++) {
         ItemStack stack = loadStack(inputList.getCompound(i), registries);
         if (!stack.isEmpty()) {
            inputs[i] = new GenericStack(AEItemKey.of(stack), (long)stack.getCount());
         }
      }

      return inputs;
   }

   public static ItemStack getCraftingResult(CompoundTag tag, Provider registries) {
      return tag.contains("out", 10) ? loadStack(tag.getCompound("out"), registries) : ItemStack.EMPTY;
   }

   public static boolean canSubstitute(CompoundTag tag) {
      return tag.getBoolean("substitute");
   }

   public static AEItemKey definitionKey(Item item, CompoundTag tag) {
      ItemStack stack = new ItemStack(item);
      ItemStackData.setTag(stack, tag);
      return AEItemKey.of(stack);
   }

   public static CompoundTag saveStack(ItemStack stack, Provider registries) {
      return stack.isEmpty() ? new CompoundTag() : (CompoundTag)stack.save(registries);
   }

   public static ItemStack loadStack(CompoundTag tag, Provider registries) {
      if (tag == null || tag.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         return isLegacyStackTag(tag) ? ItemStackData.loadStack(tag) : ItemStack.parseOptional(registries, tag);
      }
   }

   private static boolean isLegacyStackTag(CompoundTag tag) {
      return tag.contains("Count") && !tag.contains("count");
   }
}
