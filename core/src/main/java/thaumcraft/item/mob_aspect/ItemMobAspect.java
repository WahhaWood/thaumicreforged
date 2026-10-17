package thaumcraft.item.mob_aspect;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.init.registry.ModItems;
import thaumcraft.util.ItemStackData;

public class ItemMobAspect extends Item {
   public static final int aspectCount = NumericAspectHelper.getCount();
   public static final String TAG_ASPECT_NUM = "AspectNum";
   public static final String TAG_TYPE = "Type";
   public static final byte TYPE_NORMAL = 0;
   public static final byte TYPE_CONDENSED = 1;
   public static final byte TYPE_INFUSED = 2;
   private static final String TAG_LAST_X = "LastX";
   private static final String TAG_LAST_Y = "LastY";
   private static final String TAG_LAST_Z = "LastZ";
   private static final String TAG_LAST_DIM = "LastDim";

   public ItemMobAspect() {
      super(new Properties().stacksTo(16));
   }

   @Nullable
   public static Aspect getAspect(ItemStack item) {
      if (item != null && !item.isEmpty()) {
         int aspectCount = NumericAspectHelper.getCount();
         if (aspectCount <= 0) {
            return null;
         } else {
            CompoundTag tag = ItemStackData.getTag(item);
            int num = (tag == null ? 0 : tag.getInt("AspectNum")) % aspectCount;
            if (num < 0) {
               num += aspectCount;
            }

            return NumericAspectHelper.getAspect(num);
         }
      } else {
         return null;
      }
   }

   public static ItemStack getStackFromAspect(Aspect a) {
      return createStack(a, (byte)0);
   }

   public static ItemStack getCondensedStackFromAspect(Aspect a) {
      return createStack(a, (byte)1);
   }

   public static ItemStack getInfusedStackFromAspect(Aspect a) {
      return createStack(a, (byte)2);
   }

   private static ItemStack createStack(Aspect aspect, byte type) {
      ItemStack result = new ItemStack((ItemLike)ModItems.MOB_ASPECT.get());
      CompoundTag tag = ItemStackData.getOrCreateTag(result);
      tag.putInt("AspectNum", NumericAspectHelper.getNumber(aspect));
      tag.putByte("Type", type);
      ItemStackData.setTag(result, tag);
      return result;
   }

   public static boolean isCondensed(ItemStack item) {
      if (item != null && !item.isEmpty()) {
         CompoundTag tag = ItemStackData.getTag(item);
         return tag != null && tag.getByte("Type") == 1;
      } else {
         return false;
      }
   }

   public static boolean isInfused(ItemStack item) {
      if (item != null && !item.isEmpty()) {
         CompoundTag tag = ItemStackData.getTag(item);
         return tag != null && tag.getByte("Type") == 2;
      } else {
         return false;
      }
   }

   @NotNull
   public String getDescriptionId(@NotNull ItemStack stack) {
      String base = super.getDescriptionId(stack);
      if (isCondensed(stack)) {
         return base + ".condensed";
      } else {
         return isInfused(stack) ? base + ".infused" : base;
      }
   }

   public static void markLastUsedTablet(ItemStack stack, BlockEntity tablet) {
      if (!stack.isEmpty() && tablet != null && tablet.getLevel() != null) {
         CompoundTag tag = ItemStackData.getOrCreateTag(stack);
         tag.putInt("LastX", tablet.getBlockPos().getX());
         tag.putInt("LastY", tablet.getBlockPos().getY());
         tag.putInt("LastZ", tablet.getBlockPos().getZ());
         ResourceKey<Level> dimension = tablet.getLevel().dimension();
         tag.putString("LastDim", dimension.location().toString());
         ItemStackData.setTag(stack, tag);
      }
   }

   public static boolean lastUsedTabletMatches(ItemStack stack, BlockEntity tablet) {
      if (!stack.isEmpty() && tablet != null && tablet.getLevel() != null && ItemStackData.hasTag(stack)) {
         CompoundTag tag = ItemStackData.getTag(stack);
         if (tag == null) {
            return true;
         } else {
            String dim = tag.getString("LastDim");
            return !dim.isEmpty() && !tablet.getLevel().dimension().location().toString().equals(dim)
               ? false
               : tag.getInt("LastX") == tablet.getBlockPos().getX()
                  && tag.getInt("LastY") == tablet.getBlockPos().getY()
                  && tag.getInt("LastZ") == tablet.getBlockPos().getZ();
         }
      } else {
         return true;
      }
   }

   public boolean isFoil(@NotNull ItemStack stack) {
      return isInfused(stack);
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      Aspect a = getAspect(stack);
      if (a != null) {
         tooltip.add(a.getName());
      }
   }
}
