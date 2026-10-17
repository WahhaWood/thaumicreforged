package thaumcraft.enchantment;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.IEnchantmentRule;

public class BasicCompatibilityRule implements IEnchantmentRule {
   private final ResourceLocation illegalEnchantmentId;

   public BasicCompatibilityRule(ResourceLocation illegalEnchantmentId) {
      this.illegalEnchantmentId = illegalEnchantmentId;
   }

   @Override
   public boolean cantApplyAlongside(List<ResourceLocation> enchantments) {
      return this.illegalEnchantmentId != null && enchantments.contains(this.illegalEnchantmentId);
   }
}
