package thaumcraft.api.aspects.obj;

import java.util.Objects;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.AspectList;

public class TagAspect extends ObjectAspect {
   public final TagKey<Item> tagKey;

   public TagAspect(TagKey<Item> tagKey, AspectList aspects) {
      super(aspects);
      this.tagKey = tagKey;
   }

   @Override
   public boolean matches(ItemStack stack) {
      return stack.is(this.tagKey);
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof TagAspect that ? Objects.equals(this.tagKey, that.tagKey) : super.equals(o);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.tagKey);
   }
}
