package thaumcraft.api.aspects.obj;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;

abstract class DelegatingObjectAspect extends ObjectAspect {
   private static final int COMPATIBILITY_PRIORITY_BONUS = 25;
   protected final ObjectAspect delegate;

   protected DelegatingObjectAspect(ObjectAspect delegate, AspectList aspects) {
      super(aspects);
      this.delegate = delegate;
   }

   @Override
   public boolean matches(ItemStack stack) {
      return this.delegate.matches(stack);
   }

   @Override
   public int matchPriority(ItemStack stack) {
      int delegatePriority = this.delegate.matchPriority(stack);
      return delegatePriority == Integer.MIN_VALUE ? Integer.MIN_VALUE : delegatePriority + 25;
   }

   @Override
   public ObjectAspectLookupStage lookupStage() {
      return this.delegate.lookupStage();
   }

   @Nullable
   @Override
   public String getScanKey(ItemStack stack) {
      return this.delegate.getScanKey(stack);
   }
}
