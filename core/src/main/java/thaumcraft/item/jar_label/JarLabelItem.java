package thaumcraft.item.jar_label;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.util.ItemStackData;

public class JarLabelItem extends Item implements IEssenceContainerItem {
   public JarLabelItem() {
      super(new Properties());
   }

   public JarLabelItem(Properties props) {
      super(props);
   }

   public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      Level level = context.level();
      AspectList aspects = this.getAspects(stack);
      if (aspects != null && aspects.size() > 0) {
         for (Aspect a : aspects.getAspectsSorted()) {
            tooltip.add(a.getName());
         }
      }
   }

   @Nullable
   @Override
   public AspectList getAspects(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag == null) {
         return null;
      } else {
         AspectList aspects = new AspectList();
         aspects.load(tag);
         return aspects.size() > 0 ? aspects : null;
      }
   }

   @Override
   public void setAspects(ItemStack stack, AspectList aspects) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      aspects.save(tag);
      ItemStackData.setTag(stack, tag);
   }
}
