package thaumcraft.api.research;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface IResearchItemFactory {
   ResearchItem create(JsonObject var1, String var2, String var3, ResearchPage[] var4);
}
