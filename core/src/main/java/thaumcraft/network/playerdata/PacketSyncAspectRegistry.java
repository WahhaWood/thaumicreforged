package thaumcraft.network.playerdata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.compat.kubejs.ThaumcraftKubeJSAspectBridge;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketSyncAspectRegistry extends BasePacket {
   private final Map<String, JsonObject> aspects;
   private final List<JsonObject> objectTags;
   private final List<JsonObject> entityTags;

   public PacketSyncAspectRegistry(Map<String, JsonObject> aspects, List<JsonObject> objectTags, List<JsonObject> entityTags) {
      this.aspects = aspects;
      this.objectTags = objectTags;
      this.entityTags = entityTags;
   }

   public PacketSyncAspectRegistry(FriendlyByteBuf buf) {
      int aspectSize = buf.readVarInt();
      this.aspects = new LinkedHashMap<>(aspectSize);

      for (int i = 0; i < aspectSize; i++) {
         String key = buf.readUtf(32767);
         String json = buf.readUtf(32767);
         this.aspects.put(key, JsonParser.parseString(json).getAsJsonObject());
      }

      this.objectTags = readJsonList(buf);
      this.entityTags = readJsonList(buf);
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeVarInt(this.aspects.size());

      for (Entry<String, JsonObject> entry : this.aspects.entrySet()) {
         buf.writeUtf(entry.getKey());
         buf.writeUtf(entry.getValue().toString(), 32767);
      }

      writeJsonList(buf, this.objectTags);
      writeJsonList(buf, this.entityTags);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      ThaumcraftKubeJSAspectBridge.applyClientPayload(this.aspects, this.objectTags, this.entityTags);
   }

   private static List<JsonObject> readJsonList(FriendlyByteBuf buf) {
      int size = buf.readVarInt();
      ArrayList<JsonObject> out = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
         out.add(JsonParser.parseString(buf.readUtf(32767)).getAsJsonObject());
      }

      return out;
   }

   private static void writeJsonList(FriendlyByteBuf buf, List<JsonObject> list) {
      buf.writeVarInt(list.size());

      for (JsonObject object : list) {
         buf.writeUtf(object.toString(), 32767);
      }
   }
}
