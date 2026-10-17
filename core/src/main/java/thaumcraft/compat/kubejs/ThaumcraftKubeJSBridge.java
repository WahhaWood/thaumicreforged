package thaumcraft.compat.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.script.ScriptType;
import java.util.LinkedHashMap;
import net.minecraft.resources.ResourceLocation;

public final class ThaumcraftKubeJSBridge {
   private ThaumcraftKubeJSBridge() {
   }

   public static void applyResearchMutations(LinkedHashMap<String, JsonObject> categoryData, LinkedHashMap<ResourceLocation, JsonElement> researchData) {
      ThaumcraftKubeJSEvents.RESEARCH.post(ScriptType.SERVER, new ThaumcraftResearchEventJS(categoryData, researchData));
   }
}
