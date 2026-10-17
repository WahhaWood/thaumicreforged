package thaumcraft.api.aspects;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.util.ItemStackData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IEssenceContainerItem {
   @Nullable
   default AspectList getAspects(ItemStack itemstack) {
      if (ItemStackData.hasTag(itemstack)) {
         AspectList aspects = new AspectList();
         aspects.load(ItemStackData.getTag(itemstack));
         return aspects.size() > 0 ? aspects : null;
      } else {
         return null;
      }
   }

   default void setAspects(ItemStack stack, @Nullable AspectList aspects) {
      if (aspects == null) {
         ItemStackData.removeTagKey(stack, "aspects");
      } else {
         CompoundTag tag = ItemStackData.getOrCreateTag(stack);
         aspects.save(tag);
         ItemStackData.setTag(stack, tag);
      }
   }

   @OnlyIn(Dist.CLIENT)
   default void appendHoverAspects(ItemStack stack, List<Component> components) {
      AspectList aspects = this.getAspects(stack);
      if (aspects != null && aspects.size() > 0) {
         Player player = Minecraft.getInstance().player;
         if (player == null) {
            return;
         }

         PlayerKnowledge knowledge = PlayerKnowledge.of(player);

         for (Aspect tag : aspects.getAspectsSorted()) {
            if (knowledge.hasDiscoveredAspect(tag)) {
               components.add(tag.getName().append(" x" + aspects.getAmount(tag)));
            } else {
               components.add(Component.translatable("tc.aspect.unknown"));
            }
         }
      }
   }
}
