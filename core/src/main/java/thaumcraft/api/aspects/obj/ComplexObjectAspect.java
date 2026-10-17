package thaumcraft.api.aspects.obj;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;

public class ComplexObjectAspect extends DelegatingObjectAspect {
   private final ItemStack referenceStack;
   @Nullable
   private AspectList cachedAspects;

   public ComplexObjectAspect(ObjectAspect delegate, ItemStack referenceStack, AspectList aspects) {
      super(delegate, aspects == null ? new AspectList() : aspects.copy());
      this.referenceStack = referenceStack.copy();
      this.referenceStack.setCount(1);
   }

   @Override
   public AspectList getAspects(ItemStack stack) {
      if (this.cachedAspects != null) {
         return this.cachedAspects.copy();
      } else {
         AspectList finalAspects = this.copyAspects();
         AspectList registeredBaseAspects = ThaumcraftApiHelper.getRegisteredObjectTags(this.referenceStack, this);
         if (registeredBaseAspects.size() > 0) {
            finalAspects.add(registeredBaseAspects);
            return this.cacheIfReady(finalAspects);
         } else {
            AspectList generatedBaseAspects = ThaumcraftApiHelper.generateTags(this.referenceStack, this);
            if (generatedBaseAspects != null && generatedBaseAspects.size() > 0) {
               finalAspects.add(generatedBaseAspects);
            }

            return this.cacheIfReady(finalAspects);
         }
      }
   }

   private AspectList cacheIfReady(AspectList finalAspects) {
      if (ThaumcraftApi.getSafeRecipeManager() != null) {
         this.cachedAspects = finalAspects.copy();
      }

      return finalAspects;
   }
}
