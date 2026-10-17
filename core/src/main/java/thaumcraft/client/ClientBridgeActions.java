package thaumcraft.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

/**
 * Клиентские действия dev-моста: базовые операции для автотестов и скриншотов.
 * Полноценные экраны мода регистрирует ClientSetup.
 */
public final class ClientBridgeActions {
   private ClientBridgeActions() {
   }

   public static JsonElement handle(String action, JsonObject params) {
      Minecraft minecraft = Minecraft.getInstance();
      JsonObject json = new JsonObject();

      switch (action) {
         case "screen" -> {
            String name = params.get("name").getAsString();
            minecraft.execute(() -> {
               if (name.equals("inventory") && minecraft.player != null) {
                  minecraft.setScreen(new InventoryScreen(minecraft.player));
               } else if (name.equals("close")) {
                  minecraft.setScreen(null);
               } else if (name.equals("title")) {
                  minecraft.setScreen(new TitleScreen());
               }
            });
            json.addProperty("ok", true);
         }
         case "click" -> {
            double cx = params.get("x").getAsDouble();
            double cy = params.get("y").getAsDouble();
            minecraft.execute(() -> {
               if (minecraft.screen != null) {
                  minecraft.screen.mouseClicked(cx, cy, 0);
               }
            });
            json.addProperty("ok", true);
         }
         case "reload" -> {
            minecraft.execute(() -> minecraft.reloadResourcePacks());
            json.addProperty("ok", true);
         }
         case "quit" -> {
            minecraft.execute(() -> minecraft.disconnect(new TitleScreen()));
            json.addProperty("ok", true);
         }
         case "world" -> {
            String name = params.has("name") ? params.get("name").getAsString() : "New World";
            if (minecraft.level != null || minecraft.getOverlay() != null) {
               json.addProperty("ok", true);
               json.addProperty("skipped", "a world is already loaded or loading");
            } else {
               minecraft.execute(() -> minecraft.createWorldOpenFlows().openWorld(
                  name,
                  () -> minecraft.setScreen(new TitleScreen())
               ));
               json.addProperty("ok", true);
            }
         }
         case "connect" -> {
            String host = params.get("host").getAsString();
            int port = params.has("port") ? params.get("port").getAsInt() : 25565;
            String address = host + ":" + port;
            minecraft.execute(() -> ConnectScreen.startConnecting(
               minecraft.screen,
               minecraft,
               ServerAddress.parseString(address),
               new ServerData("dev", address, ServerData.Type.OTHER),
               false,
               null
            ));
            json.addProperty("ok", true);
         }
         default -> {
            json.addProperty("ok", false);
            json.addProperty("error", "unknown action " + action);
         }
      }

      return json;
   }
}
