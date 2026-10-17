package thaumcraft.enchantment;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.aspects.AspectList;

public class EnchantmentData {
   public final ResourceLocation texture;
   public final AspectList aspects;
   public final boolean vanilla;
   public final String research;

   public EnchantmentData(ResourceLocation texture, boolean vanilla, AspectList aspects) {
      this(texture, vanilla, aspects, "");
   }

   public EnchantmentData(ResourceLocation texture, boolean vanilla, AspectList aspects, String research) {
      this.texture = texture;
      this.vanilla = vanilla;
      this.aspects = aspects;
      this.research = research;
   }
}
