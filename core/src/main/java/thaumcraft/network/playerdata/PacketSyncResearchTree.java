package thaumcraft.network.playerdata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.item.relics.thaumonomicon.render.ResearchBrowserScreen;
import thaumcraft.network.BasePacket;
import thaumcraft.research.ResearchManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketSyncResearchTree extends BasePacket {
   private final Map<String, JsonObject> categories;
   private final Map<ResourceLocation, JsonElement> data;

   public PacketSyncResearchTree(Map<String, JsonObject> categories, Map<ResourceLocation, JsonElement> data) {
      this.categories = categories;
      this.data = data;
   }

   public PacketSyncResearchTree(FriendlyByteBuf buf) {
      int categorySize = buf.readVarInt();
      this.categories = new LinkedHashMap<>(categorySize);

      for (int i = 0; i < categorySize; i++) {
         String key = buf.readUtf(32767);
         String json = buf.readUtf(32767);
         this.categories.put(key, JsonParser.parseString(json).getAsJsonObject());
      }

      int researchSize = buf.readVarInt();
      this.data = new LinkedHashMap<>(researchSize);

      for (int i = 0; i < researchSize; i++) {
         ResourceLocation key = buf.readResourceLocation();
         String json = buf.readUtf(262144);
         this.data.put(key, JsonParser.parseString(json));
      }
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeVarInt(this.categories.size());

      for (Entry<String, JsonObject> entry : this.categories.entrySet()) {
         buf.writeUtf(entry.getKey());
         buf.writeUtf(entry.getValue().toString(), 32767);
      }

      buf.writeVarInt(this.data.size());

      for (Entry<ResourceLocation, JsonElement> entry : this.data.entrySet()) {
         buf.writeResourceLocation(entry.getKey());
         buf.writeUtf(entry.getValue().toString(), 262144);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      ResearchBrowserScreen.resetSelectedCategory();
      ResearchManager.LOADER.applyFromNetwork(this.categories, this.data);
   }
}
