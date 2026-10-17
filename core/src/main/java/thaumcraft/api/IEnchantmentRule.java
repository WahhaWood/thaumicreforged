package thaumcraft.api;

import java.util.List;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface IEnchantmentRule {
   boolean cantApplyAlongside(List<ResourceLocation> var1);
}
