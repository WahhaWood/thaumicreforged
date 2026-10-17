package thaumcraft.block.platform.render;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public class PlatformBlockModelLoader implements IGeometryLoader<PlatformBlockUnbakedGeometry> {
   public PlatformBlockUnbakedGeometry read(JsonObject jsonObject, JsonDeserializationContext context) {
      return new PlatformBlockUnbakedGeometry(jsonObject);
   }
}
