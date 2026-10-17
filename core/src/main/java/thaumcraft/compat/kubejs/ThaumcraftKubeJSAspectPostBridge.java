package thaumcraft.compat.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.script.ScriptType;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public final class ThaumcraftKubeJSAspectPostBridge {
   private ThaumcraftKubeJSAspectPostBridge() {
   }

   public static void collect(LinkedHashMap<String, JsonObject> aspectDefinitions, ArrayList<JsonObject> objectTags, ArrayList<JsonObject> entityTags) {
      ThaumcraftKubeJSEvents.ASPECTS.post(ScriptType.SERVER, new ThaumcraftAspectEventJS(aspectDefinitions, objectTags, entityTags));
   }
}
