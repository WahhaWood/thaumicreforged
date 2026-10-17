package thaumcraft.api;

import net.minecraft.resources.ResourceLocation;

public interface IJarTextureProvider {
   default ResourceLocation getModel() {
      return ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/model/jar.png");
   }
}
