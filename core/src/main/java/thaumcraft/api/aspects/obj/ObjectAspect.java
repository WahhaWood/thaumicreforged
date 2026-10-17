package thaumcraft.api.aspects.obj;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;

public class ObjectAspect {
   public final AspectList aspects;

   public ObjectAspect(AspectList aspects) {
      this.aspects = aspects;
   }

   public boolean matches(ItemStack stack) {
      return false;
   }

   public boolean matches(ItemLike stack) {
      return false;
   }

   public int matchPriority(ItemStack stack) {
      return this.matches(stack) ? 0 : Integer.MIN_VALUE;
   }

   public ObjectAspectLookupStage lookupStage() {
      return ObjectAspectLookupStage.GENERIC;
   }

   public AspectList getAspects(ItemStack stack) {
      return this.copyAspects();
   }

   @Nullable
   public String getScanKey(ItemStack stack) {
      return null;
   }

   protected final AspectList copyAspects() {
      return this.aspects.copy();
   }
}
