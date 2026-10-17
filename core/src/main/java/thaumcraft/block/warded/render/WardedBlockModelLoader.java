package thaumcraft.block.warded.render;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import thaumcraft.block.opaque.render.WardedBlockUnbakedGeometry;

public class WardedBlockModelLoader implements IGeometryLoader<WardedBlockUnbakedGeometry> {
   public static final WardedBlockModelLoader INSTANCE = new WardedBlockModelLoader();

   public WardedBlockUnbakedGeometry read(JsonObject jsonObject, JsonDeserializationContext context) {
      return new WardedBlockUnbakedGeometry(jsonObject);
   }
}
