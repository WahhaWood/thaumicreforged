package thaumcraft.api.nodes;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wisp_essence.ItemWispEssence;

public interface INode extends IAspectContainer {
   int MAX_NODE_ASPECT_AMOUNT = 256;

   String getId();

   AspectList getAspectsBase();

   NodeType getNodeType();

   void setNodeType(NodeType var1);

   void setNodeModifier(NodeModifier var1);

   NodeModifier getNodeModifier();

   int getNodeVisBase(Aspect var1);

   void setNodeVisBase(Aspect var1, short var2);

   @NotNull
   default List<ItemStack> getDrops() {
      List<ItemStack> stacks = new ArrayList<>();
      AspectList aspectList = this.getAspects();
      if (aspectList == null || aspectList.size() == 0) {
         aspectList = this.getAspectsBase();
      }

      if (aspectList != null && aspectList.size() != 0) {
         for (Aspect aspect : aspectList.getAspects()) {
            for (int j = 0; j <= aspectList.getAmount(aspect) / 10; j++) {
               if (aspectList.getAmount(aspect) >= 5) {
                  ItemWispEssence essence = (ItemWispEssence)ModItems.WISP_ESSENCE.get();
                  ItemStack itemStack = new ItemStack(essence);
                  essence.setAspects(itemStack, new AspectList().add(aspect, 2));
                  stacks.add(itemStack);
               }
            }
         }

         return stacks;
      } else {
         return stacks;
      }
   }
}
