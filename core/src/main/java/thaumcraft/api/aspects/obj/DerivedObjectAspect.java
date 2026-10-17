package thaumcraft.api.aspects.obj;

import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.util.ItemStackData;

public class DerivedObjectAspect extends DelegatingObjectAspect {
   private final ItemStack referenceStack;
   private final UnaryOperator<AspectList> transformer;
   @Nullable
   private AspectList cachedAspects;

   public DerivedObjectAspect(ObjectAspect delegate, ItemStack referenceStack, UnaryOperator<AspectList> transformer) {
      super(delegate, new AspectList());
      this.referenceStack = referenceStack.copy();
      this.referenceStack.setCount(1);
      this.transformer = transformer;
   }

   @Override
   public AspectList getAspects(ItemStack stack) {
      if (this.cachedAspects != null) {
         return this.cachedAspects.copy();
      } else {
         AspectList sourceAspects = ThaumcraftApiHelper.getObjectAspects(this.referenceStack);
         AspectList workingAspects = sourceAspects == null ? new AspectList() : sourceAspects.copy();
         AspectList transformedAspects = this.transformer.apply(workingAspects);
         if (transformedAspects != null) {
            workingAspects = transformedAspects;
         }

         if (ThaumcraftApi.getSafeRecipeManager() != null) {
            this.cachedAspects = workingAspects.copy();
         }

         return workingAspects;
      }
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof DerivedObjectAspect that)
         ? super.equals(o)
         : Objects.equals(this.delegate, that.delegate) && ItemStackData.sameItemSameTags(this.referenceStack, that.referenceStack);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.delegate, this.referenceStack.getItem(), this.referenceStack.getDamageValue(), ItemStackData.getTag(this.referenceStack));
   }
}
