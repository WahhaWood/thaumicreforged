package thaumcraft.block.jar.render.util;

import net.minecraft.resources.ResourceLocation;

public final class AtlasSpriteUtil {
   private AtlasSpriteUtil() {
   }

   public static ResourceLocation spriteFromTexture(ResourceLocation texture) {
      String p = texture.getPath();
      if (p.endsWith(".png")) {
         p = p.substring(0, p.length() - 4);
      }

      if (p.startsWith("textures/")) {
         p = p.substring("textures/".length());
      }

      return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), p);
   }
}
