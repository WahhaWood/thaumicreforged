package thaumcraft.api.aspects.obj;

import java.util.Arrays;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import thaumcraft.util.ItemStackData;

public final class ObjectAspectUtils {
   private ObjectAspectUtils() {
   }

   public static int getLegacyMeta(ItemStack stack) {
      if (stack.isEmpty()) {
         return 32767;
      } else {
         return stack.isDamageableItem() ? 32767 : stack.getDamageValue();
      }
   }

   public static boolean matchesBase(ItemStack stack, Item item, int meta) {
      return !stack.isEmpty() && stack.getItem() == item && (meta == 32767 || getLegacyMeta(stack) == meta);
   }

   public static String getItemKey(Item item) {
      ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
      return key == null ? item.toString() : key.toString();
   }

   public static String getBlockKey(Block block) {
      ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
      return key == null ? block.toString() : key.toString();
   }

   @Nullable
   public static CompoundTag getScanTag(ItemStack stack) {
      if (!ItemStackData.hasTag(stack)) {
         return null;
      } else {
         CompoundTag tag = ItemStackData.getTag(stack).copy();
         tag.remove("Damage");
         tag.remove("RepairCost");
         if (tag.contains("display", 10)) {
            CompoundTag display = tag.getCompound("display");
            display.remove("Name");
            if (display.isEmpty()) {
               tag.remove("display");
            }
         }

         return tag.isEmpty() ? null : tag;
      }
   }

   public static String buildExactScanKey(ItemStack stack, int meta, boolean strictNbt) {
      String key = getItemKey(stack.getItem()) + ":" + meta;
      if (strictNbt) {
         CompoundTag tag = getScanTag(stack);
         if (tag != null) {
            key = key + "|" + tag;
         }
      }

      return key;
   }

   public static String buildGroupScanKey(Item item, int[] metas) {
      StringBuilder builder = new StringBuilder(getItemKey(item)).append(":group");

      for (int meta : metas) {
         builder.append(':').append(meta);
      }

      return builder.toString();
   }

   public static int[] normalizeMetaGroup(int[] metas) {
      return Arrays.stream(metas).distinct().sorted().toArray();
   }
}
