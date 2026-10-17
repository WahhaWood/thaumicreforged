package thaumicenergistics.item;

import java.util.List;
import thaumicenergistics.ae2.pattern.PatternEncoding;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import org.jetbrains.annotations.Nullable;
import thaumcraft.util.ItemStackData;

public class ItemCrucibleCore extends Item {
   public ItemCrucibleCore() {
      super(new Properties().stacksTo(1));
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tips, TooltipFlag flag) {
      super.appendHoverText(stack, context, tips, flag);
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null) {
         ListTag patterns = tag.getList("Patterns", 10);
         if (!patterns.isEmpty()) {
            boolean first = true;

            for (int i = 0; i < patterns.size(); i++) {
               CompoundTag patternEntry = patterns.getCompound(i);
               ItemStack out = readOutput(patternEntry, context.registries());
               if (!out.isEmpty()) {
                  if (first) {
                     first = false;
                     tips.add(Component.translatable("tooltip.thaumicenergistics.pattern_core"));
                  }

                  tips.add(out.getHoverName());
               }
            }
         }
      }
   }

   private static ItemStack readOutput(CompoundTag patternEntry, @Nullable Provider registries) {
      if (patternEntry.contains("out", 10)) {
         CompoundTag outTag = patternEntry.getCompound("out");
         return registries != null ? PatternEncoding.loadStack(outTag, registries) : ItemStackData.loadStack(outTag);
      } else {
         return ItemStack.EMPTY;
      }
   }
}
