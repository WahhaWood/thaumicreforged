package thaumcraft.item.curios;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.mob_aspect.NumericAspectHelper;
import thaumcraft.util.ItemStackData;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemBaubleBlanks extends Item implements ICurioItem, IVisDiscountGear, IRunicArmor {
   private static final String TAG_ASPECT = "aspect";
   private static final String TAG_ASPECT_ALT = "Aspect";
   private static final String TAG_ASPECT_NUM = "AspectNum";

   public ItemBaubleBlanks() {
      super(new Properties().stacksTo(1).setNoRepair());
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      Aspect storedAspect = getStoredAspect(stack);
      return storedAspect != null && storedAspect == aspect ? 1 : 0;
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   public static ItemStack createAspectRing(Aspect aspect) {
      ItemStack stack = new ItemStack((ItemLike)ModItems.CURIOS_RING_IRON.get());
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.putString("aspect", aspect.getTag());
      tag.putInt("AspectNum", NumericAspectHelper.getNumber(aspect));
      ItemStackData.setTag(stack, tag);
      return stack;
   }

   @NotNull
   public Component getName(@NotNull ItemStack stack) {
      if (stack.is((Item)ModItems.CURIOS_RING_IRON.get())) {
         Aspect aspect = getStoredAspect(stack);
         Component aspectName = (Component)(aspect == null ? Component.translatable("tc.aspect.unknown") : getAspectDisplayName(aspect));
         return Component.translatable(this.getDescriptionId(stack), new Object[]{aspectName});
      } else {
         return super.getName(stack);
      }
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      Aspect aspect = getStoredAspect(stack);
      if (aspect != null) {
         tooltip.add(
            Component.empty()
               .append(getAspectDisplayName(aspect))
               .append(Component.literal(" "))
               .append(Component.translatable("tc.discount"))
               .append(Component.literal(": 1%"))
               .withStyle(ChatFormatting.DARK_PURPLE)
         );
      }
   }

   @Nullable
   public static Aspect getStoredAspect(ItemStack stack) {
      if (stack.is((Item)ModItems.CURIOS_RING_IRON.get()) && ItemStackData.hasTag(stack)) {
         CompoundTag tag = ItemStackData.getTag(stack);
         if (tag == null) {
            return null;
         } else if (tag.contains("aspect", 8)) {
            return Aspect.getAspect(tag.getString("aspect"));
         } else if (tag.contains("Aspect", 8)) {
            return Aspect.getAspect(tag.getString("Aspect"));
         } else {
            return tag.contains("AspectNum", 3) ? NumericAspectHelper.getAspect(tag.getInt("AspectNum")) : null;
         }
      } else {
         return null;
      }
   }

   private static Component getAspectDisplayName(Aspect aspect) {
      return Component.translatable("tc.aspect." + aspect.getTag());
   }
}
