package thaumcraft.block.opaque.render;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public class WardedGlassModelLoader implements IGeometryLoader<WardedGlassUnbakedGeometry> {
   public WardedGlassUnbakedGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
      ResourceLocation prefix = ResourceLocation.tryParse(GsonHelper.getAsString(jsonObject, "texture_prefix"));
      ResourceLocation particle = ResourceLocation.tryParse(GsonHelper.getAsString(jsonObject, "particle"));
      if (prefix != null && particle != null) {
         return new WardedGlassUnbakedGeometry(prefix, particle);
      } else {
         throw new IllegalArgumentException("Invalid warded glass model resource location");
      }
   }
}
