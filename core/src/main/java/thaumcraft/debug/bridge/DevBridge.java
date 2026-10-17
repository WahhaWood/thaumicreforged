package thaumcraft.debug.bridge;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.table.ResearchTableMenu;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchNoteData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Personal dev bridge: a tiny HTTP server inside the mod that lets an agent
 * drive the game autonomously.  Available in development builds only, bound to
 * localhost.
 *
 * Everything game related is executed on the server thread; HTTP requests wait
 * for the result, so a script can run command -> read state -> click slots
 * without guessing at timings.
 */
public final class DevBridge {
   private static final Logger LOGGER = LoggerFactory.getLogger("TC-Bridge");
   private static final Gson GSON = new Gson();
   private static final int DEFAULT_PORT = 9877;
   private static final long DEFAULT_TIMEOUT_MS = 10_000L;
   private static HttpServer server;
   private static long startedAt;
   private static int port;

   private DevBridge() {
   }

   public static void start() {
      // Обычно мост живёт только в dev-сборке. Но чтобы прогонять регрессию
      // против ИМЕННО выкладываемого jar'а (в нём ресурсы и классы упакованы
      // иначе, чем в build/classes), нужен явный opt-in: -Dtcbridge.force=true.
      // Флаг намеренно не включён по умолчанию — на боевом сервере без него
      // мост не поднимается, и отладочные эндпоинты наружу не торчат.
      boolean forced = Boolean.getBoolean("tcbridge.force");
      if (FMLEnvironment.production && !forced || server != null) {
         return;
      }

      try {
         port = Integer.getInteger("tcbridge.port", DEFAULT_PORT);
         server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
         server.createContext("/", DevBridge::handle);
         server.setExecutor(Executors.newCachedThreadPool());
         server.start();
         startedAt = System.currentTimeMillis();
         LOGGER.info("[TC-Bridge] listening on http://127.0.0.1:{} (dev only)", port);
      } catch (Exception e) {
         LOGGER.error("[TC-Bridge] could not start", e);
      }
   }

   // ------------------------------------------------------------------
   // routing
   // ------------------------------------------------------------------

   private static void handle(HttpExchange exchange) throws IOException {
      try {
         String path = exchange.getRequestURI().getPath();
         JsonObject params = params(exchange);
         JsonElement result;

         switch (path) {
            case "/status" -> result = status();
            case "/help" -> result = help();
            case "/exec" -> result = exec(params);
            case "/wait" -> result = wait(params);
            case "/block" -> result = block(params);
            case "/energy" -> result = energy(params);
            case "/fluids" -> result = fluids(params);
            case "/fluids/set" -> result = fluidsSet(params);
            case "/items" -> result = items(params);
            case "/players" -> result = players();
            case "/player" -> result = movePlayer(params);
            case "/player/tick" -> result = playerTick(params);
            case "/break" -> result = breakBlock(params);
            case "/use" -> result = useItemOn(params);
            case "/use/entity" -> result = useOnEntity(params);
            case "/use/hand" -> result = useItemInHand(params);
            case "/use/first" -> result = useItemFirst(params);
            case "/hand" -> result = hand(params);
            case "/wand" -> result = wandState(params);
            case "/wand/vis" -> result = wandVis(params);
            case "/wand/info" -> result = wandInfo(params);
            case "/amulet/vis" -> result = amuletVis(params);
            case "/wand/trigger" -> result = wandTrigger(params);
            case "/wand/build" -> result = wandBuild(params);
            case "/wand/trigger/allowed" -> result = wandTriggerAllowed(params);
            case "/boss/map" -> result = bossMapState(params);
            case "/essentia" -> result = essentiaState(params);
            case "/grid" -> result = gridInfo(params);
            case "/essentia/insert" -> result = essentiaInsert(params);
            case "/container/set" -> result = containerSet(params);
            case "/advfurnace/set" -> result = advFurnaceSet(params);
            case "/altar/set" -> result = altarSet(params);
            case "/node/set" -> result = nodeSet(params);
            case "/node/info" -> result = nodeInfo(params);
            case "/node/roll" -> result = nodeRoll(params);
            case "/wand/oculus" -> result = wandOculusCheck(params);
            case "/warp" -> result = warpState(params);
            case "/warp/set" -> result = warpSet(params);
            case "/warp/trigger" -> result = warpTrigger(params);
            case "/thaumatorium" -> result = thaumatoriumState(params);
            case "/thaumatorium/select" -> result = thaumatoriumSelect(params);
            case "/centrifuge" -> result = centrifugeState(params);
            case "/crystallizer" -> result = crystallizerState(params);
            case "/hole" -> result = holeState(params);
            case "/portal/link" -> result = portalLink(params);
            case "/portal/use" -> result = portalUse(params);
            case "/tube" -> result = tubeState(params);
            case "/crucible" -> result = crucibleState(params);
            case "/crucible/set" -> result = crucibleSet(params);
            case "/alembic" -> result = alembicState(params);
            case "/alembic/set" -> result = alembicSet(params);
            case "/scan/now" -> result = scanNow(params);
            case "/biome" -> result = biomeAt(params);
            case "/biome/features" -> result = biomeFeatures(params);
            case "/biome/greatwood" -> result = biomeGreatwoodSupport(params);
            case "/biome/set" -> result = biomeSet(params);
            case "/lock/facing" -> result = lockFacing(params);
            case "/pedestal" -> result = pedestalState(params);
            case "/pedestal/set" -> result = pedestalSet(params);
            case "/matrix/instability" -> result = matrixInstability(params);
            case "/matrix/trigger" -> result = matrixTrigger(params);
            case "/matrix/start" -> result = matrixStart(params);
            case "/mirror/link" -> result = mirrorLink(params);
            case "/mirror/info" -> result = mirrorInfo(params);
            case "/mirror/drain" -> result = mirrorDrain(params);
            case "/mana/grow" -> result = manaGrow(params);
            case "/item/repair" -> result = itemRepair(params);
            case "/debug/hurt" -> result = debugHurt(params);
            case "/purge" -> result = purgeEntities(params);
            case "/trunk/info" -> result = trunkInfo(params);
            case "/charm/tick" -> result = charmTick(params);
            case "/loot/roll" -> result = lootRoll(params);
            case "/player/inventory-tick" -> result = playerInventoryTick(params);
            case "/curio" -> result = curioState(params);
            case "/curio/equip" -> result = curioEquip(params);
            case "/maze/generate" -> result = mazeGenerate(params);
            case "/maze/cell" -> result = mazeCell(params);
            case "/block/entityInside" -> result = blockEntityInside(params);
            case "/maze/place" -> result = mazePlace(params);
            case "/maze/passage" -> result = mazePassage(params);
            case "/maze/room" -> result = mazeRoom(params);
            case "/maze/cellbuild" -> result = mazeCellBuild(params);
            case "/golem/spawn" -> result = golemSpawn(params);
            case "/golem/info" -> result = golemInfo(params);
            case "/golem/give" -> result = golemGive(params);
            case "/golem/mark" -> result = golemMark(params);
            case "/feature" -> result = placeConfiguredFeature(params);
            case "/scan" -> result = scanBox(params);
            case "/research" -> result = researchState(params);
            case "/research/add" -> result = researchAdd(params);
            case "/research/remove" -> result = researchRemove(params);
            case "/research/available" -> result = researchAvailable(params);
            case "/research/clear" -> result = researchClear(params);
            case "/research/aspect" -> result = researchAspectPlace(params);
            case "/aspects" -> result = aspectPool(params);
            case "/aspects/add" -> result = aspectPoolAdd(params);
            case "/aspects/combine" -> result = aspectCombine(params);
            case "/warden/info" -> result = wardenInfo(params);
            case "/entities" -> result = entities(params);
            case "/recipe" -> result = recipes(params);
            case "/menu" -> result = menu(params);
            case "/menu/open" -> result = openMenu(params);
            case "/menu/click" -> result = clickMenu(params);
            case "/menu/button" -> result = clickMenuButton(params);
            case "/menu/close" -> result = closeMenu(params);
            case "/map" -> result = map(params);
            case "/biomescan" -> result = biomeScan(params);
            case "/screenshot" -> result = screenshot(params);
            case "/screen" -> result = clientBridge("screen", params);
            case "/connect" -> result = clientBridge("connect", params);
            case "/world" -> result = clientBridge("world", params);
            case "/quit" -> result = clientBridge("quit", params);
            case "/reload" -> result = clientBridge("reload", params);
            case "/screen/click" -> result = clientBridge("click", params);
            default -> {
               send(exchange, 404, error("unknown endpoint, see /help"));
               return;
            }
         }

         send(exchange, 200, result);
      } catch (Exception e) {
         Throwable cause = e;
         while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
         }

         if (cause instanceof IllegalArgumentException || cause instanceof IllegalStateException) {
            send(exchange, 400, error(String.valueOf(cause)));
         } else {
            send(exchange, 500, error(String.valueOf(cause)));
         }
      }
   }

   private static JsonElement help() {
      JsonObject object = new JsonObject();
      JsonArray endpoints = new JsonArray();
      endpoints.add("GET  /status");
      endpoints.add("GET  /help");
      endpoints.add("POST /exec    {\"command\":\"/time set day\"}  or {\"commands\":[...]}");
      endpoints.add("POST /wait    {\"ticks\":20}");
      endpoints.add("GET  /block   ?x=..&y=..&z=..");
      endpoints.add("GET  /energy  ?x=..&y=..&z=..[&side=north]");
      endpoints.add("GET  /fluids  ?x=..&y=..&z=..");
      endpoints.add("GET  /items   ?x=..&y=..&z=..   (or ?player)");
      endpoints.add("GET  /players");
      endpoints.add("GET  /menu");
      endpoints.add("POST /menu/open  {\"x\":..,\"y\":..,\"z\":..}");
      endpoints.add("POST /menu/click {\"slot\":3,\"button\":0,\"type\":\"PICKUP\"}");
      endpoints.add("POST /menu/button {\"id\":1}   (menu buttons: scanner save, replicator mode, ...)");
      endpoints.add("POST /menu/close");
      endpoints.add("POST /use     {\"x\":..,\"y\":..,\"z\":..[,\"side\":\"up\"]}   (ПКМ предметом из руки)");
      endpoints.add("POST /hand    {\"item\":\"thaumcraft:golem_bell\"|none}");
      endpoints.add("POST /use/entity {\"x\":..,\"y\":..,\"z\":..[,\"r\":4]}   (ПКМ по ближайшей сущности)");
      endpoints.add("POST /golem/spawn {\"x\":..,\"y\":..,\"z\":..,\"material\":\"iron\"}");
      endpoints.add("POST /golem       {\"x\":..,\"y\":..,\"z\":..}   (ближайший голем)");
      endpoints.add("POST /golem/task  {\"x\":..,\"y\":..,\"z\":..,\"task\":\"gather\"}");
      endpoints.add("POST /golem/carry {\"x\":..,\"y\":..,\"z\":..,\"item\":\"..\",\"count\":1}");
      endpoints.add("GET  /map     ?x1=..&z1=..&x2=..&z2=..&y=..");
      endpoints.add("POST /screenshot {\"path\":\"/tmp/shot.png\"}");
      object.add("endpoints", endpoints);
      return object;
   }

   private static JsonObject status() {
      JsonObject object = new JsonObject();
      object.addProperty("mod", "thaumcraft");
      object.addProperty("dist", FMLEnvironment.dist.name());
      object.addProperty("port", port);
      object.addProperty("uptimeMs", System.currentTimeMillis() - startedAt);

      MinecraftServer minecraftServer = gameServer();
      if (minecraftServer == null) {
         object.addProperty("server", false);
         return object;
      }

      object.addProperty("server", true);
      object.addProperty("tick", minecraftServer.getTickCount());
      object.addProperty("gameTime", minecraftServer.overworld().getGameTime());
      object.addProperty("dayTime", minecraftServer.overworld().dayTime());
      object.addProperty("frozen", minecraftServer.tickRateManager().isFrozen());
      object.addProperty("paused", minecraftServer.isPaused());
      object.addProperty("players", minecraftServer.getPlayerList().getPlayers().size());

      JsonArray players = new JsonArray();
      for (ServerPlayer player : minecraftServer.getPlayerList().getPlayers()) {
         players.add(playerJson(player));
      }

      object.add("playerList", players);
      return object;
   }

   private static JsonObject playerJson(ServerPlayer player) {
      JsonObject json = new JsonObject();
      json.addProperty("name", player.getName().getString());
      json.addProperty("dimension", player.level().dimension().location().toString());
      json.addProperty("x", player.getX());
      json.addProperty("y", player.getY());
      json.addProperty("z", player.getZ());
      json.addProperty("yaw", player.getYRot());
      json.addProperty("pitch", player.getXRot());
      json.addProperty("health", player.getHealth());
      json.addProperty("food", player.getFoodData().getFoodLevel());
      json.addProperty("gameMode", player.gameMode.getGameModeForPlayer().getName());
      json.addProperty("creative", player.isCreative());
      return json;
   }

   // ------------------------------------------------------------------
   // commands
   // ------------------------------------------------------------------

   private static JsonElement exec(JsonObject params) {
      List<String> commands = new ArrayList<>();
      if (params.has("commands")) {
         for (JsonElement element : params.getAsJsonArray("commands")) {
            commands.add(element.getAsString());
         }
      } else if (params.has("command")) {
         commands.add(params.get("command").getAsString());
      }

      if (commands.isEmpty()) {
         return error("missing command");
      }

      return onServer(() -> {
         MinecraftServer minecraftServer = gameServer();
         ServerPlayer player = firstPlayerOrNull();
         JsonObject result = new JsonObject();
         result.addProperty("ok", true);
         JsonArray output = new JsonArray();

         for (String command : commands) {
            try {
               CommandSourceStack source = commandSource(minecraftServer, player, output);
               String prefixed = command.startsWith("/") ? command.substring(1) : command;
               CommandDispatcher<CommandSourceStack> dispatcher = minecraftServer.getCommands().getDispatcher();
               dispatcher.execute(prefixed, source);
               output.add("> " + command);
            } catch (CommandSyntaxException e) {
               result.addProperty("ok", false);
               output.add("> " + command + "  !! " + e.getMessage());
            } catch (Exception e) {
               result.addProperty("ok", false);
               output.add("> " + command + "  !! " + e);
            }
         }

         result.add("output", output);
         return result;
      });
   }

   private static CommandSourceStack commandSource(MinecraftServer server, @Nullable ServerPlayer player, JsonArray output) {
      CommandSource collector = new CommandSource() {
         @Override
         public void sendSystemMessage(Component component) {
            output.add(component.getString());
         }

         @Override
         public boolean acceptsSuccess() {
            return true;
         }

         @Override
         public boolean acceptsFailure() {
            return true;
         }

         @Override
         public boolean shouldInformAdmins() {
            return false;
         }
      };

      ServerLevel level = player != null ? player.serverLevel() : server.overworld();
      Vec3 position = player != null ? player.position() : Vec3.atLowerCornerOf(level.getSharedSpawnPos());
      Vec2 rotation = player != null ? new Vec2(player.getXRot(), player.getYRot()) : Vec2.ZERO;
      return new CommandSourceStack(
         collector,
         position,
         rotation,
         level,
         4,
         player != null ? player.getName().getString() : "bridge",
         player != null ? player.getDisplayName() : Component.literal("bridge"),
         server,
         player
      );
   }

   private static JsonElement wait(JsonObject params) throws Exception {
      int ticks = params.has("ticks") ? params.get("ticks").getAsInt() : 20;
      long timeout = params.has("timeoutMs") ? params.get("timeoutMs").getAsLong() : Math.max(DEFAULT_TIMEOUT_MS, ticks * 500L);
      MinecraftServer server = gameServer();
      if (server == null) {
         return error("no server");
      }

      long target = onServer(() -> server.getTickCount() + ticks);
      long deadline = System.currentTimeMillis() + timeout;

      while (System.currentTimeMillis() < deadline) {
         if (onServer(server::getTickCount) >= target) {
            JsonObject result = new JsonObject();
            result.addProperty("ok", true);
            result.addProperty("tick", onServer(server::getTickCount));
            return result;
         }

         Thread.sleep(5L);
      }

      return error("timeout waiting " + ticks + " ticks");
   }

   // ------------------------------------------------------------------
   // world inspection
   // ------------------------------------------------------------------

   private static JsonElement block(JsonObject params) {
      BlockPos pos = pos(params);
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockState state = level.getBlockState(pos);
         JsonObject json = new JsonObject();
         json.addProperty("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
         json.addProperty("state", state.toString());
         json.addProperty("light", level.getMaxLocalRawBrightness(pos));
         json.addProperty("skyLight", level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos));
         json.addProperty("power", level.getBestNeighborSignal(pos));
         BlockEntity blockEntity = level.getBlockEntity(pos);
         if (blockEntity != null) {
            json.addProperty("beClass", blockEntity.getClass().getName());
            try {
               json.addProperty("be", blockEntity.saveWithFullMetadata(level.registryAccess()).toString());
            } catch (Exception e) {
               json.addProperty("beError", String.valueOf(e));
            }
         }

         return json;
      });
   }

   private static JsonElement energy(JsonObject params) {
      BlockPos pos = pos(params);
      return onServer(() -> {
         ServerLevel level = level(params);
         Direction side = side(params);
         IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
         JsonObject json = new JsonObject();

         if (storage == null) {
            // fall back to any public energy field of our own block entities
            BlockEntity blockEntity = level.getBlockEntity(pos);
            storage = findField(blockEntity, IEnergyStorage.class);
         }

         if (storage == null) {
            json.addProperty("found", false);
            return json;
         }

         json.addProperty("found", true);
         json.addProperty("stored", storage.getEnergyStored());
         json.addProperty("capacity", storage.getMaxEnergyStored());
         json.addProperty("canReceive", storage.canReceive());
         json.addProperty("canExtract", storage.canExtract());
         return json;
      });
   }

   /** Положить предмет в слот блочного контейнера: {"x","y","z","slot":0,"item":"minecraft:coal","count":64}. */
   private static JsonElement containerSet(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof net.minecraft.world.Container container)) {
            return error("no container");
         }

         int slot = params.get("slot").getAsInt();
         int count = params.has("count") ? params.get("count").getAsInt() : 1;
         ItemStack stack = ItemStack.EMPTY;
         if (count > 0) {
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(params.get("item").getAsString()));
            stack = new ItemStack(item, count);
         }

         container.setItem(slot, stack);
         container.setChanged();
         if (level.getBlockEntity(pos) != null) {
            level.getBlockEntity(pos).setChanged();
         }

         JsonObject json = new JsonObject();
         ItemStack now = container.getItem(slot);
         json.addProperty("ok", true);
         json.addProperty("now", now.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(now.getItem()).toString());
         json.addProperty("count", now.getCount());
         return json;
      });
   }

   /** Фичи биома по шагам генерации: {"x","y","z","dim"}. */
   private static JsonElement biomeFeatures(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         var biome = level.getBiome(pos).value();
         var steps = biome.getGenerationSettings().features();
         com.google.gson.JsonArray array = new com.google.gson.JsonArray();

         for (int step = 0; step < steps.size(); step++) {
            array.add(steps.get(step).toString());
         }

         json.add("steps", array);
         return json;
      });
   }

   /** Биом в точке: {"x","y","z","dim"}. */
   private static JsonElement biomeAt(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("biome", level.getBiome(pos).unwrapKey().map(key -> key.location().toString()).orElse("?"));
         return json;
      });
   }

   /**
    * Источник ориентации замка для рендера: {"x","y","z"} →
    * {"facing":..,"render_facing":"..","state_facing":".."}.
    *
    * <p>Нужен, чтобы отличать два разных источника. В 1.7.10 рендер читает
    * {@code TileEldritchLock.getFacing()} (байткод рендерера, строки 107-108), а
    * в порте рендер брал свойство блока {@code FACING} — эти две вещи нигде не
    * связаны, и свойство оставалось дефолтным NORTH. Замок всегда рисовался
    * лицом на север независимо от стороны выхода из ячейки.
    *
    * <p>Эндпоинт отдаёт оба значения, поэтому проверка видит расхождение, а не
    * только итог.
    */
   private static JsonElement lockFacing(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);

         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.eldritch.lock.EldritchLockBlockEntity lock)) {
            return error("no eldritch lock at " + pos.toShortString());
         }

         json.addProperty("facing", lock.getFacing());
         var render = lock.renderFacing();
         json.addProperty("render_facing", render == null ? "" : render.name());
         var state = level.getBlockState(pos);
         json.addProperty("state_facing",
            state.hasProperty(thaumcraft.block.eldritch.lock.BlockEldritchLock.FACING)
               ? state.getValue(thaumcraft.block.eldritch.lock.BlockEldritchLock.FACING).name()
               : "");
         json.addProperty("count", lock.count);
         return json;
      });
   }

   /**
    * Красит биом по площадке: {"x","z","radius","biome","y"} → {"set":N}.
    *
    * <p>Нужен сценариям, которые зависят от биома: зал тайнта по TC4 красит
    * биом на 25x25 вокруг замка ({@code Utils.setBiomeAt}), из-за чего площадка
    * великого дерева в общей стендовой зоне становилась тайновой и дерево
    * переставало расти. Без явной установки биома сценарий зависит от того,
    * что сгенерировалось вокруг стенда.
    *
    * <p>Красится целиком столбец (все секции чанка), как это делает
    * {@link thaumcraft.util.Utils#setBiomeAt} — иначе биом не держится.
    */
   private static JsonElement biomeSet(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         int x = params.has("x") ? params.get("x").getAsInt() : 0;
         int z = params.has("z") ? params.get("z").getAsInt() : 0;
         int radius = params.has("radius") ? params.get("radius").getAsInt() : 0;
         int y = params.has("y") ? params.get("y").getAsInt() : level.getMinBuildHeight() + 1;
         String biomeId = params.has("biome") ? params.get("biome").getAsString() : "";

         if (biomeId.isBlank()) {
            return error("no biome");
         }

         net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key =
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
               net.minecraft.resources.ResourceLocation.parse(biomeId));
         int set = 0;

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               if (thaumcraft.util.Utils.setBiomeAt(level, new BlockPos(x + dx, y, z + dz), key)) {
                  set++;
               }
            }
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("biome", biomeId);
         json.addProperty("set", set);
         return json;
      });
   }

   /**
    * Состояние AE2-узла по координатам: {"x","y","z"[,"dim"]}.
    * Всё через рефлексию — у ядра нет зависимости от AE2 (он только в аддоне),
    * классы резолвятся в рантайме, если AE2 стоит в mods.
    */
   private static JsonElement gridInfo(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         BlockEntity be = level.getBlockEntity(pos);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("be", be == null ? "null" : be.getClass().getName());
         if (be == null) {
            return json;
         }

         try {
            Object managed;
            try {
               managed = be.getClass().getMethod("getMainNode").invoke(be);
            } catch (NoSuchMethodException e) {
               json.addProperty("hasNode", false);
               json.addProperty("note", "no getMainNode (not a grid host)");
               return json;
            }

            json.addProperty("hasNode", managed != null);
            if (managed == null) {
               return json;
            }

            Object node = managed.getClass().getMethod("getNode").invoke(managed);
            json.addProperty("nodePresent", node != null);
            if (node == null) {
               return json;
            }

            boolean active = (Boolean)node.getClass().getMethod("isActive").invoke(node);
            json.addProperty("nodeActive", active);
            Object grid = node.getClass().getMethod("getGrid").invoke(node);
            json.addProperty("hasGrid", grid != null);
            if (grid == null) {
               return json;
            }

            try {
               Object energy = grid.getClass().getMethod("getEnergyService").invoke(grid);
               Object stored = energy.getClass().getMethod("getStoredPower").invoke(energy);
               Object max = energy.getClass().getMethod("getMaxStoredPower").invoke(energy);
               json.addProperty("powerStored", String.valueOf(stored));
               json.addProperty("powerMax", String.valueOf(max));
            } catch (Exception e) {
               json.addProperty("powerError", e.toString());
            }

            try {
               Object pathing = grid.getClass().getMethod("getPathingService").invoke(grid);
               Object state = pathing.getClass().getMethod("getControllerState").invoke(pathing);
               json.addProperty("controllerState", String.valueOf(state));
            } catch (Exception e) {
               json.addProperty("controllerError", e.toString());
            }

            // Смонтированные хранилища сети (для тестов TE: видно ли банку
            // через шину, ячейки в драйве и т.п.). Всё рефлексией — зависимости
            // ядра от AE2 нет.
            try {
               Object storageService = grid.getClass().getMethod("getStorageService").invoke(grid);
               Object inventory = storageService.getClass().getMethod("getInventory").invoke(storageService);
               java.lang.reflect.Field mountsField = null;
               for (Class<?> c = inventory.getClass(); c != null; c = c.getSuperclass()) {
                  try {
                     mountsField = c.getDeclaredField("priorityInventory");
                     break;
                  } catch (NoSuchFieldException ignored) {
                  }
               }

               com.google.gson.JsonArray mounts = new com.google.gson.JsonArray();
               if (mountsField != null) {
                  mountsField.setAccessible(true);
                  Object map = mountsField.get(inventory);
                  if (map instanceof java.util.Map<?, ?> mm) {
                     for (Object v : mm.values()) {
                        if (v instanceof java.util.Collection<?> col) {
                           for (Object storage : col) {
                              mounts.add(storage == null ? "null" : storage.getClass().getName());
                           }
                        } else if (v != null) {
                           mounts.add(v.getClass().getName());
                        }
                     }
                  }
               }

               json.add("storages", mounts);
            } catch (Exception e) {
               json.addProperty("storagesError", e.toString());
            }
         } catch (Exception e) {
            json.addProperty("error", e.toString());
         }

         return json;
      });
   }

   /**
    * Скан биомов по сетке: {"x1","z1","x2","z2","step"[, "dim"], "y"}.
    * Возвращает легенду (индекс->ID биома) и строки сетки для связного анализа
    * (размер/частота пятен). Лимит 16384 сэмпла.
    */
   private static JsonElement biomeScan(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         int x1 = Math.min(params.get("x1").getAsInt(), params.get("x2").getAsInt());
         int x2 = Math.max(params.get("x1").getAsInt(), params.get("x2").getAsInt());
         int z1 = Math.min(params.get("z1").getAsInt(), params.get("z2").getAsInt());
         int z2 = Math.max(params.get("z1").getAsInt(), params.get("z2").getAsInt());
         int step = params.has("step") ? Math.max(4, params.get("step").getAsInt()) : 32;
         int y = params.has("y") ? params.get("y").getAsInt() : 64;
         int nx = (x2 - x1) / step + 1;
         int nz = (z2 - z1) / step + 1;
         if ((long)nx * (long)nz > 16384L) {
            throw new IllegalArgumentException("biomescan too large, max 16384 samples");
         }

         java.util.Map<String, Integer> legend = new java.util.LinkedHashMap<>();
         java.util.List<String> rows = new java.util.ArrayList<>();
         for (int iz = 0; iz < nz; iz++) {
            StringBuilder row = new StringBuilder();
            for (int ix = 0; ix < nx; ix++) {
               BlockPos pos = new BlockPos(x1 + ix * step, y, z1 + iz * step);
               String id = level.getBiome(pos).unwrapKey()
                  .map(k -> k.location().toString()).orElse("?");
               int idx = legend.computeIfAbsent(id, k -> legend.size());
               row.append(idx < 10 ? (char)('0' + idx) : (char)('a' + idx - 10));
            }

            rows.add(row.toString());
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("step", step);
         JsonObject leg = new JsonObject();
         legend.forEach(leg::addProperty);
         json.add("legend", leg);
         com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
         rows.forEach(arr::add);
         json.add("rows", arr);
         return json;
      });
   }

   /** Прямой скан таумометром: {"player":"fake","look":{"x":..,"y":..,"z":..}}. */
   private static JsonElement scanNow(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         if (!(player.getMainHandItem().getItem() instanceof thaumcraft.item.relics.ItemThaumometer meter)) {
            return error("no thaumometer in hand");
         }

         if (params.has("look")) {
            com.google.gson.JsonObject look = params.getAsJsonObject("look");
            double dx = look.get("x").getAsDouble() - player.getX();
            double dy = look.get("y").getAsDouble() - player.getEyeY();
            double dz = look.get("z").getAsDouble() - player.getZ();
            float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            float pitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
            player.setYRot(yaw);
            player.setXRot(pitch);
            player.setYHeadRot(yaw);
            player.yBodyRot = yaw;
            player.yRotO = yaw;
            player.xRotO = pitch;
         }

         thaumcraft.api.research.ScanResult scan = meter.doScan(player.getMainHandItem(), player, false);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         if (scan == null) {
            json.addProperty("scan", "null");
            return json;
         }

         json.addProperty("type", scan.type.name());
         if (scan.phenomena != null) {
            json.addProperty("phenomena", scan.phenomena);
         }
         thaumcraft.research.ScanManager.completeScan(player, scan, "@");
         json.addProperty("completed", true);
         return json;
      });
   }

   /** Состояние тигля: {"x","y","z"}. */
   private static JsonElement crucibleState(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.crucible.CrucibleBlockEntity crucible)) {
            return error("no crucible");
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("heat", crucible.heat);
         json.addProperty("water", crucible.tank.getFluidAmount());
         json.addProperty("aspects", crucible.getAspects().visSize());
         json.addProperty("tags", crucible.tagAmount());
         JsonObject al = new JsonObject();
         if (crucible.aspects != null) {
            for (thaumcraft.api.aspects.Aspect a : crucible.aspects.getAspects()) {
               al.addProperty(a.getTag(), crucible.aspects.getAmount(a));
            }
         }
         json.add("aspectList", al);
         return json;
      });
   }

   /** Установка параметров тигля: {"x","y","z","heat"?, "water"?, "aspect"?, "amount"?}. */
   private static JsonElement crucibleSet(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.crucible.CrucibleBlockEntity crucible)) {
            return error("no crucible");
         }
         if (params.has("heat")) {
            crucible.heat = (short)params.get("heat").getAsInt();
         }
         if (params.has("water")) {
            crucible.tank.setFluid(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, params.get("water").getAsInt()));
         }
         if (params.has("aspect") && params.has("amount")) {
            thaumcraft.api.aspects.Aspect a = thaumcraft.api.aspects.Aspect.getAspect(params.get("aspect").getAsString());
            if (a != null) {
               crucible.aspects.add(a, params.get("amount").getAsInt());
            }
         }
         crucible.setChanged();
         crucible.markForUpdate();

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("heat", crucible.heat);
         json.addProperty("water", crucible.tank.getFluidAmount());
         json.addProperty("tags", crucible.tagAmount());
         return json;
      });
   }

   /** Состояние алембика: {"x","y","z"}. */
   private static JsonElement alembicState(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.arcane_alembic.ArcaneAlembicBlockEntity alembic)) {
            return error("no alembic");
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("aspect", alembic.aspect != null ? alembic.aspect.getTag() : "");
         json.addProperty("filter", alembic.aspectFilter != null ? alembic.aspectFilter.getTag() : "");
         json.addProperty("amount", alembic.amount);
         json.addProperty("maxAmount", alembic.maxAmount);
         return json;
      });
   }

   /** Установка параметров алембика: {"x","y","z","aspect"?, "filter"?, "amount"?}. */
   private static JsonElement alembicSet(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.arcane_alembic.ArcaneAlembicBlockEntity alembic)) {
            return error("no alembic");
         }

         if (params.has("aspect")) {
            String tag = params.get("aspect").getAsString();
            alembic.aspect = tag.isEmpty() ? null : thaumcraft.api.aspects.Aspect.getAspect(tag);
         }
         if (params.has("filter")) {
            String filter = params.get("filter").getAsString();
            alembic.aspectFilter = filter.isEmpty() ? null : thaumcraft.api.aspects.Aspect.getAspect(filter);
         }
         if (params.has("amount")) {
            alembic.amount = params.get("amount").getAsInt();
         }
         alembic.setChanged();
         alembic.markForUpdate();

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("aspect", alembic.aspect != null ? alembic.aspect.getTag() : "");
         json.addProperty("filter", alembic.aspectFilter != null ? alembic.aspectFilter.getTag() : "");
         json.addProperty("amount", alembic.amount);
         return json;
      });
   }

   /** Состояние трубы: {"x","y","z"}. */
   private static JsonElement tubeState(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.tube.TubeBlockEntity tube)) {
            return error("no tube");
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("suction", tube.getSuctionAmount(null));
         json.addProperty("suctionType", tube.getSuctionType(null) == null ? "" : tube.getSuctionType(null).getTag());
         json.addProperty("essenceType", tube.getEssenceType(null) == null ? "" : tube.getEssenceType(null).getTag());
         json.addProperty("essenceAmount", tube.getEssenceAmount(null));
         StringBuilder open = new StringBuilder();
         boolean[] sides = tube.getOpenSides();

         for (int i = 0; i < sides.length; i++) {
            open.append(sides[i] ? "1" : "0");
         }

         json.addProperty("open", open.toString());
         if (tube instanceof thaumcraft.block.tube.valve.TubeValveBlockEntity valve) {
            json.addProperty("allowFlow", valve.allowFlow);
         }

         return json;
      });
   }

   /** Прямой прогон телепорта портала (для тестов): {"x","y","z","player":"fake"}. */
   private static JsonElement portalUse(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.eldritch.portal.EldritchPortalBlockEntity portal)) {
            return error("no eldritch portal");
         }

         net.minecraft.server.level.ServerPlayer player = playerFor(params);
         portal.teleport(level, pos, player);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("dim", player.level().dimension().location().toString());
         json.addProperty("x", player.getX());
         json.addProperty("y", player.getY());
         json.addProperty("z", player.getZ());
         return json;
      });
   }

   /** Инфо о линке портала: {"x","y","z"} (позиция портала) или {"olChunkX","olChunkZ"}. */
   private static JsonElement portalLink(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         var links = thaumcraft.dimension.outer_lands.data.PortalLinksData.get(level);
         if (links == null) {
            return error("no link data");
         }

         // reset=true — забыть связь, чтобы тест прошёл полный цикл заново.
         if (params.has("reset") && params.get("reset").getAsBoolean() && params.has("x")) {
            JsonObject droppedJson = new JsonObject();
            droppedJson.addProperty("ok", true);
            droppedJson.addProperty("dropped", links.removeByPortal(level, pos(params)));
            return droppedJson;
         }

         thaumcraft.dimension.outer_lands.data.PortalLinksData.Link link = null;
         if (params.has("olChunkX")) {
            link = links.getByOuterChunk(params.get("olChunkX").getAsInt(), params.get("olChunkZ").getAsInt());
         } else if (params.has("x")) {
            link = links.getByPortal(level, pos(params));
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         if (link == null) {
            json.addProperty("found", false);
            return json;
         }

         json.addProperty("found", true);
         json.addProperty("sourceDim", link.sourceDimension.location().toString());
         json.addProperty("portal", link.portalPos.getX() + " " + link.portalPos.getY() + " " + link.portalPos.getZ());
         json.addProperty("olChunkX", link.olChunkX);
         json.addProperty("olChunkZ", link.olChunkZ);
         json.addProperty("mazeHalfWidth", link.mazeHalfWidth);
         json.addProperty("mazeHalfHeight", link.mazeHalfHeight);
         if (link.sourceExit != null) {
            json.addProperty("sourceExit", link.sourceExit.getX() + " " + link.sourceExit.getY() + " " + link.sourceExit.getZ());
         }

         if (link.olExit != null) {
            json.addProperty("olExit", link.olExit.getX() + " " + link.olExit.getY() + " " + link.olExit.getZ());
         }

         return json;
      });
   }

   /** Настройка алтаря эльдрича: {"x","y","z","eyes":4,"open":true}. */
   private static JsonElement altarSet(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.eldritch.altar.EldritchAltarBlockEntity altar)) {
            return error("no eldritch altar");
         }

         if (params.has("eyes")) {
            altar.setEyes((byte)params.get("eyes").getAsInt());
         }

         if (params.has("open")) {
            altar.setOpen(params.get("open").getAsBoolean());
         }

         altar.setChanged();
         altar.markForUpdate();
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("eyes", altar.getEyes());
         json.addProperty("open", altar.isOpen());
         return json;
      });
   }

   /** Тип и параметры узла ауры: {"x","y","z","type":"DARK","modifier":"BRIGHT","aspect":"ignis","amount":10,"base":10}. */
   private static JsonElement nodeSet(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.airy.aura_node.AuraNodeBlockEntity node)) {
            return error("no aura node");
         }

         if (params.has("type")) {
            thaumcraft.api.nodes.NodeType type = thaumcraft.api.nodes.NodeType
               .valueOf(params.get("type").getAsString().toUpperCase());
            node.setNodeType(type);
         }
         if (params.has("modifier")) {
            String modStr = params.get("modifier").getAsString().toUpperCase();
            if ("NONE".equals(modStr) || "NULL".equals(modStr) || "NORMAL".equals(modStr)) {
               node.setNodeModifier(null);
            } else {
               node.setNodeModifier(thaumcraft.api.nodes.NodeModifier.valueOf(modStr));
            }
         }
         if (params.has("aspect")) {
            thaumcraft.api.aspects.Aspect a = thaumcraft.api.aspects.Aspect.getAspect(params.get("aspect").getAsString().toLowerCase());
            if (a != null) {
               int amt = params.has("amount") ? params.get("amount").getAsInt() : 0;
               node.getAspects().setAmount(a, amt);
               if (params.has("base")) {
                  node.setNodeVisBase(a, (short)params.get("base").getAsInt());
               }
            }
         }

         node.setChanged();
         node.markForUpdate();
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("type", node.getNodeType().name());
         json.addProperty("modifier", node.getNodeModifier() != null ? node.getNodeModifier().name() : "NORMAL");
         json.addProperty("lock", node.getLock());
         json.addProperty("id", node.getId());
         json.addProperty("locations", thaumcraft.block.airy.aura_node.AuraNodeBlockEntity.locations.size());
         return json;
      });
   }

   /** Информация об узле ауры: {"x","y","z"}. */
   private static JsonElement nodeInfo(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.airy.aura_node.AuraNodeBlockEntity node)) {
            return error("no aura node");
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("type", node.getNodeType().name());
         json.addProperty("modifier", node.getNodeModifier() != null ? node.getNodeModifier().name() : "NORMAL");
         json.addProperty("lock", node.getLock());
         json.addProperty("id", node.getId());

         JsonObject aspects = new JsonObject();
         if (node.getAspects() != null) {
            for (thaumcraft.api.aspects.Aspect a : node.getAspects().getAspects()) {
               if (a != null) {
                  aspects.addProperty(a.getTag(), node.getAspects().getAmount(a));
               }
            }
         }
         json.add("aspects", aspects);

         JsonObject base = new JsonObject();
         if (node.getAspectsBase() != null) {
            for (thaumcraft.api.aspects.Aspect a : node.getAspectsBase().getAspects()) {
               if (a != null) {
                  base.addProperty(a.getTag(), node.getAspectsBase().getAmount(a));
               }
            }
         }
         json.add("base", base);
         return json;
      });
   }

   /**
    * Прогоняет бросок содержимого узла (порт createRandomNodeAt) много раз и
    * возвращает статистику: {"x","y","z","count":400,"silverwood":false,"eerie":false}.
    *
    * <p>Нужен, чтобы проверить правило наполнения узла (D18), а не разовое
    * поведение: составные аспекты, HUNGRY/PURE/TAINTED/FADING и запас по биому
    * видны только на статистике тысяч бросков.
    */
   private static JsonElement nodeRoll(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         int count = params.has("count") ? params.get("count").getAsInt() : 200;
         boolean silverwood = params.has("silverwood") && params.get("silverwood").getAsBoolean();
         boolean eerie = params.has("eerie") && params.get("eerie").getAsBoolean();

         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.airy.aura_node.AuraNodeBlockEntity)) {
            return error("no aura node");
         }

         Map<String, Integer> typeCount = new LinkedHashMap<>();
         Map<String, Integer> modCount = new LinkedHashMap<>();
         Map<String, Integer> aspectCount = new LinkedHashMap<>();
         Map<String, Integer> aspectVis = new LinkedHashMap<>();
         int compoundNodes = 0;
         int emptyNodes = 0;
         int minVis = Integer.MAX_VALUE;
         int maxVis = Integer.MIN_VALUE;
         long visSum = 0L;

         Random rng = new Random(level.getSeed());

         for (int i = 0; i < count; i++) {
            thaumcraft.world.features.ThaumcraftWorldgen.initNode(
               level, pos, net.minecraft.util.RandomSource.create(rng.nextLong()), silverwood, eerie
            );
            if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.airy.aura_node.AuraNodeBlockEntity node)) {
               continue;
            }

            typeCount.merge(node.getNodeType().name(), 1, Integer::sum);
            modCount.merge(node.getNodeModifier() != null ? node.getNodeModifier().name() : "NORMAL", 1, Integer::sum);

            int vis = 0;
            boolean hasCompound = false;

            for (thaumcraft.api.aspects.Aspect a : node.getAspects().getAspects()) {
               if (a == null) {
                  continue;
               }

               int amt = node.getAspects().getAmount(a);
               aspectCount.merge(a.getTag(), 1, Integer::sum);
               aspectVis.merge(a.getTag(), amt, Integer::sum);
               vis += amt;

               if (!a.isPrimal()) {
                  hasCompound = true;
               }
            }

            if (hasCompound) {
               compoundNodes++;
            }

            if (vis == 0) {
               emptyNodes++;
            } else {
               minVis = Math.min(minVis, vis);
               maxVis = Math.max(maxVis, vis);
               visSum += vis;
            }
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("count", count);
         json.addProperty("compound_nodes", compoundNodes);
         json.addProperty("empty_nodes", emptyNodes);
         json.addProperty("min_vis", emptyNodes == count ? 0 : minVis);
         json.addProperty("max_vis", emptyNodes == count ? 0 : maxVis);
         json.addProperty("avg_vis", emptyNodes == count ? 0.0 : (double)visSum / (count - emptyNodes));

         JsonObject types = new JsonObject();
         typeCount.forEach(types::addProperty);
         json.add("types", types);

         JsonObject mods = new JsonObject();
         modCount.forEach(mods::addProperty);
         json.add("modifiers", mods);

         // Аспекты, встретившиеся хотя бы в 5% узлов, — иначе ответ нечитаем.
         JsonObject aspects = new JsonObject();
         aspectCount.entrySet().stream()
            .filter(e -> e.getValue() * 20 >= count)
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> aspects.addProperty(
               e.getKey(), e.getValue() + "/" + aspectVis.getOrDefault(e.getKey(), 0)
            ));
         json.add("aspects", aspects);
         json.addProperty("distinct_aspects", aspectCount.size());
         return json;
      });
   }

   /**
    * Проверка условий триггера окулуса (порт WandManager.createOculus): четыре глаза,
    * закрытый алтарь, тёмный узел над алтарём и готовый лабиринт.
    * {"x","y","z"}.
    */
   private static JsonElement wandOculusCheck(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);

         thaumcraft.block.eldritch.altar.EldritchAltarBlockEntity altar = null;
         BlockPos altarPos = pos;

         if (level.getBlockEntity(pos) instanceof thaumcraft.block.eldritch.altar.EldritchAltarBlockEntity found) {
            altar = found;
         } else {
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
               if (level.getBlockEntity(pos.relative(dir)) instanceof thaumcraft.block.eldritch.altar.EldritchAltarBlockEntity found2) {
                  altar = found2;
                  altarPos = pos.relative(dir);
                  break;
               }
            }
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("altar_found", altar != null);

         if (altar == null) {
            json.addProperty("eyes", 0);
            json.addProperty("open", false);
            json.addProperty("dark_node_above", false);
            json.addProperty("maze", false);
            json.addProperty("would_pass", false);
            return json;
         }

         BlockPos portalPos = altarPos.above();
         boolean darkNode = level.getBlockEntity(portalPos) instanceof thaumcraft.api.nodes.INode n
            && n.getNodeType() == thaumcraft.api.nodes.NodeType.DARK;
         int eyes = altar.getEyes();
         boolean open = altar.isOpen();
         boolean maze = altar.checkForMaze();

         json.addProperty("eyes", eyes);
         json.addProperty("open", open);
         json.addProperty("dark_node_above", darkNode);
         json.addProperty("node_above_type",
            level.getBlockEntity(portalPos) instanceof thaumcraft.api.nodes.INode n2 ? n2.getNodeType().name() : "NONE");
         json.addProperty("maze", maze);
         json.addProperty("would_pass", eyes == 4 && !open && darkNode && maze);
         return json;
      });
   }

   /** Состояние варпа: {"player":"fake"}. */
   private static JsonElement warpState(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         thaumcraft.research.PlayerKnowledge knowledge = thaumcraft.research.PlayerKnowledge.of(player);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("perm", knowledge.getWarpPerm());
         json.addProperty("sticky", knowledge.getWarpSticky());
         json.addProperty("temp", knowledge.getWarpTemp());
         json.addProperty("total", knowledge.getWarpTotal());
         json.addProperty("counter", knowledge.getWarpCounter());
         return json;
      });
   }

   /** Установка варпа: {"player":"fake","perm":10,"sticky":5,"temp":2,"counter":100}. */
   private static JsonElement warpSet(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         thaumcraft.research.PlayerKnowledge knowledge = thaumcraft.research.PlayerKnowledge.of(player);
         if (params.has("perm")) {
            knowledge.setWarpPerm(params.get("perm").getAsInt());
         }
         if (params.has("sticky")) {
            knowledge.setWarpSticky(params.get("sticky").getAsInt());
         }
         if (params.has("temp")) {
            knowledge.setWarpTemp(params.get("temp").getAsInt());
         }
         if (params.has("counter")) {
            knowledge.setWarpCounter(params.get("counter").getAsInt());
         }
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("perm", knowledge.getWarpPerm());
         json.addProperty("sticky", knowledge.getWarpSticky());
         json.addProperty("temp", knowledge.getWarpTemp());
         json.addProperty("total", knowledge.getWarpTotal());
         return json;
      });
   }

   /** Принудительный триггер варп-события: {"player":"fake"}. */
   private static JsonElement warpTrigger(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         thaumcraft.event.common.WarpEvents.checkWarpEvent(player);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         return json;
      });
   }

   /** Состояние тауматория: {"x","y","z"}. */
   private static JsonElement thaumatoriumState(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.thaumatorium.ThaumatoriumBlockEntity th)) {
            return error("no thaumatorium");
         }
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("hasRecipes", th.hasSelectedRecipes());
         json.addProperty("recipeCount", th.getSelectedRecipeCount());
         JsonObject aspects = new JsonObject();
         if (th.getAspects() != null) {
            for (thaumcraft.api.aspects.Aspect a : th.getAspects().getAspects()) {
               if (a != null) {
                  aspects.addProperty(a.getTag(), th.getAspects().getAmount(a));
               }
            }
         }
         json.add("aspects", aspects);
         return json;
      });
   }

   /** Выбор рецепта в тауматории: {"x","y","z","recipe":"thaumcraft:nugget_iron"}. */
   private static JsonElement thaumatoriumSelect(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.thaumatorium.ThaumatoriumBlockEntity th)) {
            return error("no thaumatorium");
         }
         ResourceLocation recipeId = ResourceLocation.parse(params.get("recipe").getAsString());
         thaumcraft.api.crafting.CrucibleRecipe recipe = thaumcraft.api.ThaumcraftApi.getCrucibleRecipeFromId(recipeId);
         if (recipe == null && !recipeId.getPath().startsWith("crucible/")) {
            ResourceLocation prefixed = ResourceLocation.fromNamespaceAndPath(recipeId.getNamespace(), "crucible/" + recipeId.getPath());
            recipe = thaumcraft.api.ThaumcraftApi.getCrucibleRecipeFromId(prefixed);
            if (recipe != null) {
               recipeId = prefixed;
            }
         }
         if (recipe != null) {
            th.toggleRecipeSelection(new net.minecraft.world.item.crafting.RecipeHolder<>(recipeId, recipe));
            th.setChanged();
            th.markForUpdate();
         }
         JsonObject json = new JsonObject();
         json.addProperty("ok", recipe != null);
         json.addProperty("selected", th.isRecipeSelected(recipeId));
         return json;
      });
   }

   /** Состояние центрифуги: {"x","y","z"}. */
   private static JsonElement centrifugeState(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.tube.centrifuge.CentrifugeBlockEntity cf)) {
            return error("no centrifuge");
         }
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("aspectIn", cf.aspectIn != null ? cf.aspectIn.getTag() : "");
         json.addProperty("aspectOut", cf.aspectOut != null ? cf.aspectOut.getTag() : "");
         return json;
      });
   }

   /** Состояние кристаллизатора: {"x","y","z"}. */
   private static JsonElement crystallizerState(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.essence_crystallizer.EssenceCrystalizerBlockEntity cr)) {
            return error("no crystallizer");
         }
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("aspect", cr.aspect != null ? cr.aspect.getTag() : "");
         return json;
      });
   }

   /** Состояние портативной дыры: {"x","y","z"}. */
   private static JsonElement holeState(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.hole.HoleBlockEntity hole)) {
            return error("no hole block");
         }
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("countdown", hole.countdown);
         json.addProperty("countdownMax", hole.countdownMax);
         json.addProperty("stored", hole.getStoredState() != null ? hole.getStoredState().toString() : "");
         return json;
      });
   }

   /** Нагреть/зарядить продвинутую печь для тестов: {"x","y","z","heat":500,"power1":500,"power2":500}. */
   private static JsonElement advFurnaceSet(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBlockEntity furnace)) {
            return error("no advanced furnace");
         }

         if (params.has("heat")) {
            furnace.heat = params.get("heat").getAsInt();
         }

         if (params.has("power1")) {
            furnace.power1 = params.get("power1").getAsInt();
         }

         if (params.has("power2")) {
            furnace.power2 = params.get("power2").getAsInt();
         }

         furnace.setChanged();
         furnace.markForUpdate();
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("heat", furnace.heat);
         json.addProperty("power1", furnace.power1);
         json.addProperty("power2", furnace.power2);
         json.addProperty("vis", furnace.vis);
         return json;
      });
   }

   /** Установить жидкость в резервуаре: {"x","y","z","fluid":"minecraft:water","amount":1000}. */
   private static JsonElement fluidsSet(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side(params));
         if (handler == null && level.getBlockEntity(pos) instanceof IFluidHandler fluid) {
            handler = fluid;
         }

         JsonObject json = new JsonObject();
         if (handler == null) {
            json.addProperty("found", false);
            return json;
         }

         for (int i = 0; i < handler.getTanks(); i++) {
            net.neoforged.neoforge.fluids.FluidStack current = handler.getFluidInTank(i);
            if (!current.isEmpty()) {
               handler.drain(current.copyWithAmount(current.getAmount()), IFluidHandler.FluidAction.EXECUTE);
            }
         }

         int filled = 0;
         if (params.has("fluid") && params.get("amount").getAsInt() > 0) {
            net.minecraft.world.level.material.Fluid fluid = BuiltInRegistries.FLUID
               .get(ResourceLocation.parse(params.get("fluid").getAsString()));
            if (fluid != null) {
               filled = handler.fill(
                  new net.neoforged.neoforge.fluids.FluidStack(fluid, params.get("amount").getAsInt()),
                  IFluidHandler.FluidAction.EXECUTE
               );
            }
         }

         json.addProperty("found", true);
         json.addProperty("filled", filled);
         return json;
      });
   }

   private static JsonElement fluids(JsonObject params) {
      BlockPos pos = pos(params);
      return onServer(() -> {
         ServerLevel level = level(params);
         IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side(params));
         if (handler == null) {
            handler = findField(level.getBlockEntity(pos), IFluidHandler.class);
         }

         JsonObject json = new JsonObject();
         if (handler == null) {
            json.addProperty("found", false);
            return json;
         }

         json.addProperty("found", true);
         JsonArray tanks = new JsonArray();
         for (int i = 0; i < handler.getTanks(); i++) {
            JsonObject tank = new JsonObject();
            var fluid = handler.getFluidInTank(i);
            tank.addProperty("fluid", fluid.isEmpty() ? "empty" : BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
            tank.addProperty("amount", fluid.getAmount());
            tank.addProperty("capacity", handler.getTankCapacity(i));
            tanks.add(tank);
         }

         json.add("tanks", tanks);
         return json;
      });
   }

   private static JsonElement items(JsonObject params) {
      // Полная очистка инвентаря: нужно, чтобы проверять прохождение «с нуля» —
      // иначе в фейк-игроке остаются предметы от прошлых прогонов регрессии,
      // и прохождение проходится наполовину обманутым способом.
      if (params.has("clear") && params.get("clear").getAsBoolean()) {
         return onServer(() -> {
            ServerPlayer player = playerFor(params);
            player.getInventory().clearContent();
            player.inventoryMenu.broadcastChanges();
            JsonObject json = new JsonObject();
            json.addProperty("ok", true);
            json.addProperty("cleared", true);
            return json;
         });
      }

      if (params.has("slot")) {
         // write an item into a player slot (real or fake player)
         return onServer(() -> {
            ServerPlayer player = playerFor(params);
            int slot = params.get("slot").getAsInt();
            int count = params.has("count") ? params.get("count").getAsInt() : 1;
            ItemStack stack = ItemStack.EMPTY;
            if (params.has("item") && !params.get("item").getAsString().isBlank()) {
               net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(
                  net.minecraft.resources.ResourceLocation.parse(params.get("item").getAsString())
               );
               stack = new ItemStack(item, Math.max(1, count));
            }

            player.getInventory().setItem(slot, stack);
            player.inventoryMenu.broadcastChanges();
            JsonObject json = new JsonObject();
            json.addProperty("ok", true);
            json.addProperty("slot", slot);
            json.addProperty("count", stack.getCount());
            return json;
         });
      }

      if (params.has("player")) {
         return onServer(() -> {
            ServerPlayer player = playerFor(params);
            JsonObject json = new JsonObject();
            JsonArray slots = new JsonArray();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
               ItemStack stack = player.getInventory().getItem(i);
               if (!stack.isEmpty()) {
                  JsonObject entry = itemJson(i, stack);
                  slots.add(entry);
               }
            }

            json.add("slots", slots);
            return json;
         });
      }

      BlockPos pos = pos(params);
      return onServer(() -> {
         ServerLevel level = level(params);
         IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side(params));
         if (handler == null) {
            handler = findField(level.getBlockEntity(pos), IItemHandler.class);
         }

         JsonObject json = new JsonObject();
         if (handler == null) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof Container container) {
               JsonArray slots = new JsonArray();
               for (int i = 0; i < container.getContainerSize(); i++) {
                  ItemStack stack = container.getItem(i);
                  if (!stack.isEmpty()) {
                     slots.add(itemJson(i, stack));
                  }
               }

               json.add("slots", slots);
               return json;
            }

            json.addProperty("found", false);
            return json;
         }

         JsonArray slots = new JsonArray();
         for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
               slots.add(itemJson(i, stack));
            }
         }

         json.add("slots", slots);
         json.add("containers", containerFields(level.getBlockEntity(pos)));
         return json;
      });
   }

   /**
    * Walks the block entity fields for our own containers and reports them by
    * field name, so tests no longer depend on the capability layout.
    */
   private static JsonObject containerFields(@Nullable BlockEntity blockEntity) {
      JsonObject containers = new JsonObject();
      if (blockEntity == null) {
         return containers;
      }

      for (Class<?> clazz = blockEntity.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
         for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (!Container.class.isAssignableFrom(field.getType())) {
               continue;
            }

            try {
               field.setAccessible(true);
               if (!(field.get(blockEntity) instanceof Container container)) {
                  continue;
               }

               JsonArray slots = new JsonArray();
               for (int i = 0; i < container.getContainerSize(); i++) {
                  ItemStack stack = container.getItem(i);
                  if (!stack.isEmpty()) {
                     slots.add(itemJson(i, stack));
                  }
               }

               containers.add(field.getName(), slots);
            } catch (Exception ignored) {
               // try the next field
            }
         }
      }

      return containers;
   }

   private static JsonObject itemJson(int slot, ItemStack stack) {
      JsonObject entry = new JsonObject();
      entry.addProperty("slot", slot);
      entry.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
      entry.addProperty("count", stack.getCount());
      return entry;
   }

   /** Loaded recipes producing the given item (verifies the datapack in game). */
   private static JsonElement recipes(JsonObject params) {
      String itemId = params.get("item").getAsString();
      return onServer(() -> {
         var level = level(params);
         var item = BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(itemId));
         JsonObject json = new JsonObject();
         JsonArray array = new JsonArray();

         for (var holder : level.getRecipeManager().getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING)) {
            var recipe = holder.value();
            var result = recipe.getResultItem(level.registryAccess());
            if (result.isEmpty() || result.getItem() != item) {
               continue;
            }

            JsonObject entry = new JsonObject();
            entry.addProperty("id", holder.id().toString());
            entry.addProperty("shaped", recipe instanceof net.minecraft.world.item.crafting.ShapedRecipe);
            JsonArray ingredients = new JsonArray();
            for (var ingredient : recipe.getIngredients()) {
               if (ingredient.isEmpty()) {
                  ingredients.add(" ");
                  continue;
               }

               var stacks = ingredient.getItems();
               ingredients.add(stacks.length == 0 ? "?" : BuiltInRegistries.ITEM.getKey(stacks[0].getItem()).toString());
            }

            entry.add("ingredients", ingredients);
            entry.addProperty("result", BuiltInRegistries.ITEM.getKey(result.getItem()) + " x" + result.getCount());
            array.add(entry);
         }

         json.add("recipes", array);
         return json;
      });
   }

   /** Breaks a block as the player, so loot/wrench behaviour can be tested. */
   private static JsonElement breakBlock(JsonObject params) {
      BlockPos pos = pos(params);
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         boolean destroyed = player.gameMode.destroyBlock(pos);
         JsonObject json = new JsonObject();
         json.addProperty("ok", destroyed);
         json.addProperty("player", playerKey(params));
         return json;
      });
   }

   /** ПКМ предметом из главной руки по блоку (колокол, пьедесталы, верстаки…). */
   private static JsonElement useItemOn(JsonObject params) {
      BlockPos pos = pos(params);
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         ServerLevel level = level(params);
         Direction side = side(params);
         if (side == null) {
            side = Direction.UP;
         }

         boolean prevShift = player.isShiftKeyDown();
         if (params.has("sneak")) {
            player.setShiftKeyDown(params.get("sneak").getAsBoolean());
         }

         try {
            net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
               net.minecraft.world.phys.Vec3.atCenterOf(pos), side, pos, false
            );
            ItemStack stack = player.getMainHandItem();
            net.minecraft.world.InteractionResult result = player.gameMode.useItemOn(
               player, level, stack, InteractionHand.MAIN_HAND, hit
            );
            JsonObject json = new JsonObject();
            json.addProperty("ok", result.consumesAction());
            json.addProperty("result", result.toString());
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            boolean opened = player.containerMenu != player.inventoryMenu;
            json.addProperty("menuOpened", opened);
            if (opened) {
               json.addProperty("containerId", player.containerMenu.containerId);
               json.addProperty("slotCount", player.containerMenu.slots.size());
            }
            return json;
         } finally {
            player.setShiftKeyDown(prevShift);
         }
      });
   }

   /** ПКМ предметом из главной руки по ближайшей сущности (ядра големов и т.п.). */
   private static JsonElement useOnEntity(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         ServerLevel level = level(params);
         double x = params.get("x").getAsDouble();
         double y = params.get("y").getAsDouble();
         double z = params.get("z").getAsDouble();
         double radius = params.has("r") ? params.get("r").getAsDouble() : 4.0;
         var box = new net.minecraft.world.phys.AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
         java.util.List<net.minecraft.world.entity.Entity> entities = level.getEntitiesOfClass(
            net.minecraft.world.entity.Entity.class, box
         );
         if (entities.isEmpty()) {
            return error("no entity near " + x + "," + y + "," + z);
         }

         entities.sort(java.util.Comparator.comparingDouble(entity -> entity.distanceToSqr(x, y, z)));
         net.minecraft.world.entity.Entity target = entities.get(0);
         ItemStack stack = player.getMainHandItem();
         net.minecraft.world.InteractionResult result = player.interactOn(target, InteractionHand.MAIN_HAND);
         JsonObject json = new JsonObject();
         json.addProperty("ok", result.consumesAction());
         json.addProperty("result", result.toString());
         json.addProperty("entity", BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
         json.addProperty("uuid", target.getUUID().toString());
         json.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
         boolean opened = player.containerMenu != player.inventoryMenu;
         json.addProperty("menuOpened", opened);
         if (opened) {
            json.addProperty("containerId", player.containerMenu.containerId);
            json.addProperty("slotCount", player.containerMenu.slots.size());
         }
         return json;
      });
   }

   /** Использовать предмет в руке (правый клик в воздух): {\"player\":\"fake\"}. */
   private static JsonElement useItemInHand(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         boolean prevShift = player.isShiftKeyDown();
         if (params.has("sneak")) {
            player.setShiftKeyDown(params.get("sneak").getAsBoolean());
         }

         try {
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty()) {
               return error("empty hand");
            }

            net.minecraft.world.InteractionResult result = player.gameMode.useItem(
               player, player.serverLevel(), stack, InteractionHand.MAIN_HAND
            );
            JsonObject json = new JsonObject();
            json.addProperty("ok", result.consumesAction());
            json.addProperty("result", result.toString());
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString());
            json.addProperty("count", player.getMainHandItem().getCount());
            boolean opened = player.containerMenu != player.inventoryMenu;
            json.addProperty("menuOpened", opened);
            if (opened) {
               json.addProperty("containerId", player.containerMenu.containerId);
               json.addProperty("slotCount", player.containerMenu.slots.size());
            }
            return json;
         } finally {
            player.setShiftKeyDown(prevShift);
         }
      });
   }

   /** Положить предмет в главную руку игрока (или очистить). */
   private static JsonElement hand(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         // Без "item" эндпоинт только ЧИТАЕТ состояние главной руки. Раньше он
         // молча очищал руку, из-за чего нельзя было посмотреть предмет после
         // действия, не списав его.
         if (!params.has("item")) {
            JsonObject read = new JsonObject();
            read.addProperty("ok", true);
            read.addProperty("player", playerKey(params));
            read.addProperty("name", player.getScoreboardName());
            read.addProperty("item",
               BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString());
            read.addProperty("count", player.getMainHandItem().getCount());
            read.addProperty("damage", player.getMainHandItem().getDamageValue());
            return read;
         }

         String item = params.get("item").getAsString();
         if (item.isBlank() || item.equalsIgnoreCase("none") || item.equals("minecraft:air")) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
         } else {
            var registered = BuiltInRegistries.ITEM.get(ResourceLocation.parse(item));
            if (registered == net.minecraft.world.item.Items.AIR) {
               return error("unknown item: " + item);
            }

            ItemStack stack = new ItemStack(registered, params.has("count") ? params.get("count").getAsInt() : 1);
            if (params.has("enchant")) {
               var ench = player.registryAccess()
                  .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                  .get(net.minecraft.resources.ResourceKey.create(
                     net.minecraft.core.registries.Registries.ENCHANTMENT,
                     net.minecraft.resources.ResourceLocation.parse(params.get("enchant").getAsString())
                  ));
               if (ench.isPresent()) {
                  stack.enchant(ench.get(), params.has("level") ? params.get("level").getAsInt() : 1);
               }
            }

            if (params.has("damage")) {
               stack.setDamageValue(params.get("damage").getAsInt());
            }

            if (params.has("slot")) {
               com.google.gson.JsonElement slotParam = params.get("slot");
               if (slotParam.isJsonPrimitive() && slotParam.getAsJsonPrimitive().isString()) {
                  String slotName = slotParam.getAsString().toLowerCase();
                  net.minecraft.world.entity.EquipmentSlot armorSlot = switch (slotName) {
                     case "feet", "boots" -> net.minecraft.world.entity.EquipmentSlot.FEET;
                     case "legs", "leggings" -> net.minecraft.world.entity.EquipmentSlot.LEGS;
                     case "chest", "chestplate" -> net.minecraft.world.entity.EquipmentSlot.CHEST;
                     case "head", "helmet" -> net.minecraft.world.entity.EquipmentSlot.HEAD;
                     default -> null;
                  };
                  if (armorSlot != null) {
                     player.setItemSlot(armorSlot, stack);
                  }
               } else {
                  player.getInventory().setItem(slotParam.getAsInt(), stack);
               }
            } else {
               player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            }
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("player", playerKey(params));
         json.addProperty("name", player.getScoreboardName());
         json.addProperty("item", BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString());
         json.addProperty("count", player.getMainHandItem().getCount());
         json.addProperty("damage", player.getMainHandItem().getDamageValue());
         return json;
      });
   }


   /** Состояние жезла в руке. */
   private static JsonElement wandState(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         ItemStack stack = player.getMainHandItem();
         return wandJsonInternal(stack);
      });
   }

   /** Установить вис на жезл в руке: {"aspect":"ignis","amount":100}. */
   private static JsonElement wandVis(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         ItemStack stack = player.getMainHandItem();
         if (!(stack.getItem() instanceof thaumcraft.item.wands.ItemWandCasting wand)) {
            return error("no wand in main hand");
         }

         // Без aspect/amount раньше падало NPE и мост отвечал 500. Это пишет
         // заряд, а читает его /wand и /wand/info — путать их легко, поэтому
         // отвечаем понятной ошибкой, а не стектрейсом.
         if (!params.has("aspect") || !params.has("amount")) {
            return error("/wand/vis sets vis: needs aspect and amount; "
                         + "read charge with /wand or /wand/info");
         }

         thaumcraft.api.aspects.Aspect aspect = thaumcraft.api.aspects.Aspect.getAspect(params.get("aspect").getAsString());
         if (aspect == null) {
            return error("unknown aspect: " + params.get("aspect").getAsString());
         }

         wand.storeVis(stack, aspect, params.get("amount").getAsInt());
         return wandJsonInternal(stack);
      });
   }

   /**
    * Заряд жезла по всем аспектам: {"player":..} → {"vis":{"ignis":..},"max":..}.
    *
    * <p>Нужен, чтобы увидеть перелив из амулета: {@code /wand/vis} только пишет
    * по одному аспекту, а читать заряд было нечем.
    */
   private static JsonElement wandInfo(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         ItemStack stack = player.getMainHandItem();

         if (!(stack.getItem() instanceof thaumcraft.item.wands.ItemWandCasting wand)) {
            return error("no wand in main hand");
         }

         JsonObject vis = new JsonObject();
         thaumcraft.api.aspects.AspectList all = wand.getAllVis(stack);
         for (var a : all.getAspects()) {
            if (a != null) {
               vis.addProperty(a.getTag(), all.getAmount(a));
            }
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.add("vis", vis);
         json.addProperty("max", wand.getMaxVis(stack));
         json.addProperty("hand_vis", player.getOffhandItem() == stack ? -1 : 0);
         return json;
      });
   }

   /**
    * Заряд амулета виса в слоте Curios: {"player":..,"item":"thaumcraft:vis_amulet"}.
    * С amount — записать заряд по всем аспектам, без него — прочитать.
    *
    * <p>Амулет в обычном инвентаре проверять нечем: он живёт в capability Curios.
    */
   private static JsonElement amuletVis(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         String itemId = params.has("item") ? params.get("item").getAsString()
            : "thaumcraft:vis_amulet";
         var handler = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player);

         if (handler.isEmpty()) {
            return error("у игрока нет слотов Curios");
         }

         thaumcraft.item.curios.ItemAmuletVis amulet = null;
         ItemStack slot = ItemStack.EMPTY;

         for (var entry : handler.get().getCurios().entrySet()) {
            ItemStack candidate = entry.getValue().getStacks().getStackInSlot(0);

            if (candidate.getItem() instanceof thaumcraft.item.curios.ItemAmuletVis found) {
               amulet = found;
               slot = candidate;
               break;
            }
         }

         if (amulet == null) {
            return error("амулет виса не надет: " + itemId);
         }

         if (params.has("amount")) {
            int amount = params.get("amount").getAsInt();

            for (var a : thaumcraft.api.aspects.Aspect.getPrimalAspects()) {
               amulet.storeVis(slot, a, amount);
            }
         }

         JsonObject vis = new JsonObject();
         for (var a : thaumcraft.api.aspects.Aspect.getPrimalAspects()) {
            vis.addProperty(a.getTag(), amulet.getVis(slot, a));
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("item", BuiltInRegistries.ITEM.getKey(slot.getItem()).toString());
         json.add("vis", vis);
         json.addProperty("max", amulet.getMaxVis(slot));
         return json;
      });
   }

   /** Прямой вызов жезл-триггера: {"event":4,"x":..,"y":..,"z":..[,"side":"up"]}. */
   private static JsonElement wandTrigger(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         Direction side = side(params);
         if (side == null) {
            side = Direction.UP;
         }

         var block = level.getBlockState(pos).getBlock();
         net.minecraft.world.InteractionResult result = thaumcraft.api.wands.WandTriggerRegistry.performTrigger(
            level, player.getMainHandItem(), player, pos, side, block
         );
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("result", result.toString());
         json.addProperty("block", BuiltInRegistries.BLOCK.getKey(block).toString());
         json.addProperty("hasTrigger", thaumcraft.api.wands.WandTriggerRegistry.hasTrigger(block));
         int event = params.get("event").getAsInt();
         boolean direct = thaumcraft.Thaumcraft.setup.wandManager.performTrigger(
            level, player.getMainHandItem(), player, pos, side, event
         );
         json.addProperty("direct", direct);
         return json;
      });
   }

   /** Собрать полноценный жезл (по умолчанию серебряное древко + таумиевое навершие). */
   private static JsonElement wandBuild(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         String rodId = params.has("rod") ? params.get("rod").getAsString() : "thaumcraft:wand_rod_silverwood";
         String capId = params.has("cap") ? params.get("cap").getAsString() : "thaumcraft:wand_cap_thaumium";
         var rodItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(rodId));
         var capItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(capId));
         if (!(rodItem instanceof thaumcraft.item.wands.ItemWandRod rod)
            || !(capItem instanceof thaumcraft.item.wands.ItemWandCap cap)) {
            return error("bad rod or cap: " + rodId + " / " + capId);
         }

         ItemStack stack = thaumcraft.item.wands.ItemWandCasting.createWandFill(rod, cap);
         if (params.has("focus")) {
            var focusItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(params.get("focus").getAsString()));
            if (focusItem instanceof thaumcraft.api.wands.ItemFocusBasic) {
               ((thaumcraft.item.wands.ItemWandCasting)stack.getItem())
                  .setFocus(stack, new ItemStack(focusItem));
            }
         }

         player.setItemInHand(InteractionHand.MAIN_HAND, stack);
         return wandJsonInternal(stack);
      });
   }

   /** Состояние контейнера эссенции (банка/алембик/узел...). */
   private static JsonElement essentiaState(JsonObject params) {
      return onServer(() -> {
         net.minecraft.world.level.Level level = level(params);
         net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos(params));
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("beClass", be == null ? "none" : be.getClass().getName());
         if (be instanceof thaumcraft.api.aspects.IAspectContainer container) {
            JsonObject aspects = new JsonObject();
            thaumcraft.api.aspects.AspectList list = container.getAspects();
            if (list != null) {
               for (thaumcraft.api.aspects.Aspect aspect : list.getAspects()) {
                  aspects.addProperty(aspect.getTag(), list.getAmount(aspect));
               }
            }

            json.add("aspects", aspects);
         }
         if (be instanceof thaumcraft.block.jar.standart.JarBlockEntity jar && jar.aspectFilter != null) {
            json.addProperty("aspectFilter", jar.aspectFilter.getTag());
         }
         if (be instanceof thaumcraft.block.arcane_alembic.ArcaneAlembicBlockEntity alembic && alembic.aspectFilter != null) {
            json.addProperty("aspectFilter", alembic.aspectFilter.getTag());
         }

         return json;
      });
   }

   /** Влить эссенцию в контейнер: {"x":..,"y":..,"z":..,"aspect":"potentia","amount":8}. */
   private static JsonElement essentiaInsert(JsonObject params) {
      return onServer(() -> {
         net.minecraft.world.level.Level level = level(params);
         net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos(params));
         if (!(be instanceof thaumcraft.api.aspects.IAspectContainer container)) {
            return error("no essentia container at " + pos(params));
         }

         thaumcraft.api.aspects.Aspect aspect = thaumcraft.api.aspects.Aspect.getAspect(params.get("aspect").getAsString());
         if (aspect == null) {
            return error("unknown aspect");
         }

         int leftover = container.addToContainer(aspect, params.get("amount").getAsInt());
         if (be != null) {
            be.setChanged();
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("leftover", leftover);
         return json;
      });
   }

   /** Что лежит на пьедестале/в контейнере-блоке (слот 0). */
   private static JsonElement pedestalState(JsonObject params) {
      return onServer(() -> {
         net.minecraft.world.level.Level level = level(params);
         net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos(params));
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         if (be instanceof net.minecraft.world.Container container) {
            ItemStack stack = container.getItem(0);
            json.addProperty("item", stack.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            json.addProperty("count", stack.getCount());
            com.google.gson.JsonObject enchants = new com.google.gson.JsonObject();
            stack.getEnchantments()
               .entrySet()
               .forEach(entry -> enchants.addProperty(
                  ((net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>)entry.getKey())
                     .getRegisteredName(),
                  entry.getIntValue()
               ));
            json.add("enchantments", enchants);
         } else {
            json.addProperty("item", "");
            json.addProperty("count", 0);
         }

         return json;
      });
   }

   /** Положить предмет в слот 0 контейнера (пьедестал и пр.). */
   private static JsonElement pedestalSet(JsonObject params) {
      return onServer(() -> {
         net.minecraft.world.level.Level level = level(params);
         net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos(params));
         if (!(be instanceof net.minecraft.world.Container container)) {
            return error("no container at " + pos(params));
         }

         String item = params.has("item") ? params.get("item").getAsString() : "";
         ItemStack stack = ItemStack.EMPTY;
         if (!item.isBlank() && !item.equalsIgnoreCase("none")) {
            var registered = BuiltInRegistries.ITEM.get(ResourceLocation.parse(item));
            if (registered == net.minecraft.world.item.Items.AIR) {
               return error("unknown item: " + item);
            }

            stack = new ItemStack(registered, params.has("count") ? params.get("count").getAsInt() : 1);
            if (params.has("enchant")) {
               var ench = level.registryAccess()
                  .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                  .get(net.minecraft.resources.ResourceKey.create(
                     net.minecraft.core.registries.Registries.ENCHANTMENT,
                     net.minecraft.resources.ResourceLocation.parse(params.get("enchant").getAsString())
                  ));
               if (ench.isPresent()) {
                  stack.enchant(ench.get(), params.has("level") ? params.get("level").getAsInt() : 1);
               }
            }
         }

         container.setItem(0, stack);
         if (be != null) {
            be.setChanged();
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("item", stack.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
         return json;
      });
   }

   /** Проверка нестабильности рунической матрицы и симметрии алтаря. */
   private static JsonElement matrixInstability(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos(params));
         if (!(be instanceof thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity matrix)) {
            return error("no runic matrix");
         }
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("crafting", matrix.isCrafting());
         json.addProperty("instability", matrix.getInstability());
         thaumcraft.block.infusion_matrix.handlers.InfusionInstabilityHandler.StructureScan scan =
            matrix.getInstabilityHandler().scanStructureDetail();
         json.addProperty("stability", scan.stability());
         json.addProperty("stabilityRequired", thaumcraft.block.infusion_matrix.handlers.InfusionInstabilityHandler.requiredStability(matrix.getRecipeInstability()));
         json.addProperty("recipeInstability", matrix.getRecipeInstability());
         json.addProperty("recipeType", matrix.getRecipeType());
         json.addProperty("recipeXP", matrix.getRecipeXP());
         json.addProperty("recipeEssentia", matrix.getAspects().toString());
         json.addProperty("recipeIngredients", matrix.getRecipeIngredientsCount());
         return json;
      });
   }

   /** Запуск события нестабильности рунической матрицы. */
   private static JsonElement matrixTrigger(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos(params));
         if (!(be instanceof thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity matrix)) {
            return error("no runic matrix");
         }
         matrix.getInstabilityHandler().runInstabilityEvent(level);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         return json;
      });
   }

   /** Клик жезлом по матрице = старт инфузии (тот же путь, что ItemWandCasting#onItemUseFirst). */
   private static JsonElement matrixStart(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
         if (!(be instanceof thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity matrix)) {
            return error("no runic matrix");
         }
         int ret = matrix.onWandRightClick(
            level, player, InteractionHand.MAIN_HAND, pos, side(params) == null ? Direction.UP : side(params)
         );
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("ret", ret);
         json.addProperty("crafting", matrix.isCrafting());
         json.addProperty("instability", matrix.getInstability());
         return json;
      });
   }

   /** Связать два зеркала (оба направления) — то же состояние, что после связки предметом. */
   private static JsonElement mirrorLink(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos a = new BlockPos(params.get("x1").getAsInt(), params.get("y1").getAsInt(), params.get("z1").getAsInt());
         BlockPos b = new BlockPos(params.get("x2").getAsInt(), params.get("y2").getAsInt(), params.get("z2").getAsInt());
         net.minecraft.world.level.block.entity.BlockEntity beA = level.getBlockEntity(a);
         net.minecraft.world.level.block.entity.BlockEntity beB = level.getBlockEntity(b);
         JsonObject json = new JsonObject();
         if (beA instanceof thaumcraft.block.mirror.MirrorBlockEntity ma
            && beB instanceof thaumcraft.block.mirror.MirrorBlockEntity mb) {
            ma.linked = true;
            ma.link = new thaumcraft.api.LevelBlockPos(b, level.dimension());
            mb.linked = true;
            mb.link = new thaumcraft.api.LevelBlockPos(a, level.dimension());
            ma.setChanged();
            mb.setChanged();
            json.addProperty("ok", true);
         } else if (beA instanceof thaumcraft.block.mirror.MirrorEssenceBlockEntity ea
            && beB instanceof thaumcraft.block.mirror.MirrorEssenceBlockEntity eb) {
            ea.linked = true;
            ea.link = new thaumcraft.api.LevelBlockPos(b, level.dimension());
            eb.linked = true;
            eb.link = new thaumcraft.api.LevelBlockPos(a, level.dimension());
            ea.setChanged();
            eb.setChanged();
            json.addProperty("ok", true);
         } else {
            return error("not a mirror pair at given positions");
         }

         return json;
      });
   }

   /** Взять эссенцию через эссенция-зеркало (удащённый кран у парного зеркала). */
   private static JsonElement mirrorDrain(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos(params));
         if (!(be instanceof thaumcraft.block.mirror.MirrorEssenceBlockEntity mirror)) {
            return error("no essentia mirror at " + pos(params));
         }
         thaumcraft.api.aspects.Aspect aspect = thaumcraft.api.aspects.Aspect.getAspect(params.get("aspect").getAsString());
         if (aspect == null) {
            return error("unknown aspect");
         }
         boolean taken = mirror.takeFromContainer(aspect, params.has("amount") ? params.get("amount").getAsInt() : 1);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("taken", taken);
         return json;
      });
   }

   /** Прямой урон игроку (диагностика цепочки hurt/событий). */
   /**
    * Принудительно убрать сущности в радиусе: {@code {"x":..,"y":..,"z":..,"r":8,
    * "type":"minecraft:zombie"}}. Нужно для чистки тестов — команда {@code /kill}
    * не удаляет уже мёртвые сущности, а их трупы остаются pickable и перехватывают
    * попадания фокусов (шок бил в труп вместо живой цели).
    */
private static JsonElement purgeEntities(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         double x = params.get("x").getAsDouble();
         double y = params.get("y").getAsDouble();
         double z = params.get("z").getAsDouble();
         double r = params.has("r") ? params.get("r").getAsDouble() : 8.0;
         var box = new net.minecraft.world.phys.AABB(
            x - r, y - r, z - r, x + r, y + r, z + r
         );
         String wanted = params.has("type") ? params.get("type").getAsString() : null;
         List<net.minecraft.world.entity.Entity> found = new java.util.ArrayList<>(
            level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, box)
         );
         int removed = 0;
         for (net.minecraft.world.entity.Entity e : found) {
            if (wanted != null && !BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals(wanted)) {
               continue;
            }

            if (e instanceof ServerPlayer) {
               continue;
            }

            e.setRemoved(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            removed++;
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("found", found.size());
         json.addProperty("removed", removed);
         return json;
      });
   }

   /**
    * Сгенерировать лабиринт (синхронно) и вернуть статистику:
    * {@code {"x":0,"z":0,"w":11,"h":11,"seed":123}}.
    *
    * <p>Главная проверка — {@code connected}: в оригинале генератор обязан
    * соединить угловую 2×2 зону босса с центральной ячейкой входа, иначе
    * лабиринт непроходим.
    */
private static JsonElement mazeGenerate(JsonObject params) {
      return onServer(() -> {
         int x = params.has("x") ? params.get("x").getAsInt() : 0;
         int z = params.has("z") ? params.get("z").getAsInt() : 0;
         int w = params.has("w") ? params.get("w").getAsInt() : 11;
         int h = params.has("h") ? params.get("h").getAsInt() : 11;
         long seed = params.has("seed") ? params.get("seed").getAsLong() : 12345L;
         boolean clear = params.has("clear") && params.get("clear").getAsBoolean();
         if (clear) {
            thaumcraft.dimension.outer_lands.maze.MazeHandler.clearHashMap();
         }

         long t0 = System.currentTimeMillis();
         thaumcraft.dimension.outer_lands.maze.MazeThread.runNow(x, z, w, h, seed);
         long took = System.currentTimeMillis() - t0;

         // Обход в ширину от центральной ячейки входа.
         int px = 1 + w / 2;
         int py = 1 + h / 2;
         int col = x - (1 + w / 2);
         int row = z - (1 + h / 2);
         java.util.Set<thaumcraft.dimension.outer_lands.maze.CellLoc> seen = new java.util.HashSet<>();
         java.util.ArrayDeque<thaumcraft.dimension.outer_lands.maze.CellLoc> queue =
            new java.util.ArrayDeque<>();
         thaumcraft.dimension.outer_lands.maze.Cell start =
            thaumcraft.dimension.outer_lands.maze.MazeHandler.getFromHashMap(
               new thaumcraft.dimension.outer_lands.maze.CellLoc(px + col, py + row)
            );
         if (start != null) {
            queue.add(new thaumcraft.dimension.outer_lands.maze.CellLoc(px + col, py + row));
            seen.add(new thaumcraft.dimension.outer_lands.maze.CellLoc(px + col, py + row));
         }

         int bossReached = -1;
         while (!queue.isEmpty()) {
            thaumcraft.dimension.outer_lands.maze.CellLoc cur = queue.poll();
            thaumcraft.dimension.outer_lands.maze.Cell c =
               thaumcraft.dimension.outer_lands.maze.MazeHandler.getFromHashMap(cur);
            if (c == null) {
               continue;
            }

            if (c.feature >= 2 && c.feature <= 5 && bossReached < 0) {
               bossReached = c.feature;
            }

            // индекс 2 — флаг прохода: 1=N, 2=S, 3=E, 4=W
            int[][] steps = {{0, -1, 1, 1}, {0, 1, 2, 1}, {1, 0, 3, 4}, {-1, 0, 4, 8}};
            for (int[] st : steps) {
               boolean open = switch (st[2]) {
                  case 1 -> c.north;
                  case 2 -> c.south;
                  case 3 -> c.east;
                  default -> c.west;
               };
               if (!open) {
                  continue;
               }

               thaumcraft.dimension.outer_lands.maze.CellLoc n =
                  new thaumcraft.dimension.outer_lands.maze.CellLoc(cur.x + st[0], cur.z + st[1]);
               if (seen.add(n)) {
                  queue.add(n);
               }
            }
         }

         // Гистограмма типов ячеек.
         java.util.Map<Integer, Integer> features = new java.util.TreeMap<>();
         for (thaumcraft.dimension.outer_lands.maze.Cell c :
            thaumcraft.dimension.outer_lands.maze.MazeHandler.iterate()) {
            features.merge((int)c.feature, 1, Integer::sum);
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("tookMs", took);
         json.addProperty("cells", thaumcraft.dimension.outer_lands.maze.MazeHandler.size());
         json.addProperty("reachableFromCenter", seen.size());
         json.addProperty("bossRoomReachable", bossReached >= 0);
         json.addProperty("bossFeature", bossReached);
         JsonObject feat = new JsonObject();
         features.forEach((k, v) -> feat.addProperty(String.valueOf(k), v));
         json.add("features", feat);
         return json;
      });
   }

   /** Прочитать одну ячейку лабиринта по координатам чанка. */
   /**
    * Поставить блок лабиринта по ID палитры и вернуть, что получилось:
    * {@code {"x":0,"y":70,"z":0,"palette":2,"side":"north"}}.
    */
private static JsonElement mazePlace(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         int x = params.get("x").getAsInt();
         int y = params.get("y").getAsInt();
         int z = params.get("z").getAsInt();
         int palette = params.get("palette").getAsInt();
         Direction dir = side(params) == null ? Direction.NORTH : side(params);
         thaumcraft.dimension.outer_lands.maze.Cell cell = new thaumcraft.dimension.outer_lands.maze.Cell();
         if (params.has("feature")) {
            cell.feature = (byte)params.get("feature").getAsInt();
         }

         thaumcraft.dimension.outer_lands.maze.GenCommon.placeBlock(level, x, y, z, palette, dir, cell);
         net.minecraft.world.level.block.state.BlockState st = level.getBlockState(new BlockPos(x, y, z));
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("block", BuiltInRegistries.BLOCK.getKey(st.getBlock()).toString());
         json.addProperty("facing", st.hasProperty(net.minecraft.world.level.block.StairBlock.FACING)
            ? st.getValue(net.minecraft.world.level.block.StairBlock.FACING).getName() : "");
         json.addProperty("half", st.hasProperty(net.minecraft.world.level.block.StairBlock.HALF)
            ? st.getValue(net.minecraft.world.level.block.StairBlock.HALF).name() : "");
         return json;
      });
   }

   /**
    * Построить именованную комнату лабиринта по её фиче:
    * {@code {"cx":25,"cz":-25,"y":50,"room":"portal|nest|key|library|boss",
    * "north":false,"south":false,"west":false,"east":false}}.
    * Возвращает гистограмму блоков и время генерации.
    */
   private static JsonElement mazeRoom(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         int cx = params.get("cx").getAsInt();
         int cz = params.get("cz").getAsInt();
         int y = params.has("y") ? params.get("y").getAsInt() : 50;
         String room = params.has("room") ? params.get("room").getAsString() : "portal";
         thaumcraft.dimension.outer_lands.maze.Cell cell = new thaumcraft.dimension.outer_lands.maze.Cell();
         cell.north = params.has("north") && params.get("north").getAsBoolean();
         cell.south = params.has("south") && params.get("south").getAsBoolean();
         cell.west = params.has("west") && params.get("west").getAsBoolean();
         cell.east = params.has("east") && params.get("east").getAsBoolean();

         long t0 = System.currentTimeMillis();
         switch (room) {
            case "portal" -> {
               cell.feature = (byte)0;
               thaumcraft.dimension.outer_lands.maze.GenPortal.generatePortal(level, level.getRandom(), cx, cz, y, cell);
            }
            case "nest" -> {
               cell.feature = (byte)7;
               thaumcraft.dimension.outer_lands.maze.GenNestRoom.generateRoom(level, level.getRandom(), cx, cz, y, cell);
            }
            case "key" -> {
               cell.feature = (byte)6;
               thaumcraft.dimension.outer_lands.maze.GenKeyRoom.generateRoom(level, level.getRandom(), cx, cz, y, cell);
            }
            case "library" -> {
               cell.feature = (byte)8;
               thaumcraft.dimension.outer_lands.maze.GenLibraryRoom.generateRoom(level, level.getRandom(), cx, cz, y, cell);
            }
            case "boss" -> {
               cell.feature = params.has("feature") ? (byte)params.get("feature").getAsInt() : (byte)2;
               thaumcraft.dimension.outer_lands.maze.GenBossRoom.generateRoom(level, level.getRandom(), cx, cz, y, cell);
            }
            default -> throw new IllegalArgumentException("неизвестная комната: " + room);
         }
         long took = System.currentTimeMillis() - t0;

         int bx = cx * 16;
         int bz = cz * 16;
         java.util.Map<String, Integer> histogram = new java.util.TreeMap<>();
         for (int dx = 0; dx < 16; dx++) {
            for (int dy = -1; dy <= 13; dy++) {
               for (int dz = 0; dz < 16; dz++) {
                  var st = level.getBlockState(new BlockPos(bx + dx, y + dy, bz + dz));
                  histogram.merge(
                     BuiltInRegistries.BLOCK.getKey(st.getBlock()).toString(), 1, Integer::sum
                  );
               }
            }
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("room", room);
         json.addProperty("tookMs", took);
         JsonObject hist = new JsonObject();
         histogram.forEach((k, v) -> hist.addProperty(k, v));
         json.add("blocks", hist);
         return json;
      });
   }

   /**
    * Проверить, что диспетчер {@code MazeHandler.generateEldritch} строит ячейку
    * по её координате: {@code {"cx":25,"cz":-25}}. Возвращает гистограмму блоков.
    */
   private static JsonElement mazeCellBuild(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         int cx = params.get("cx").getAsInt();
         int cz = params.get("cz").getAsInt();
         long t0 = System.currentTimeMillis();
         thaumcraft.dimension.outer_lands.maze.MazeHandler.generateEldritch(
            level, level.getRandom(), cx, cz
         );
         long took = System.currentTimeMillis() - t0;

         thaumcraft.dimension.outer_lands.maze.Cell cell = thaumcraft.dimension.outer_lands.maze.MazeHandler.getFromHashMap(
            new thaumcraft.dimension.outer_lands.maze.CellLoc(cx, cz)
         );
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("tookMs", took);
         json.addProperty("hasCell", cell != null);
         if (cell != null) {
            json.addProperty("feature", cell.feature);
            json.addProperty("packed", cell.pack());
         }

         int bx = cx * 16;
         int bz = cz * 16;
         java.util.Map<String, Integer> histogram = new java.util.TreeMap<>();
         for (int dx = 0; dx < 16; dx++) {
            for (int dy = -1; dy <= 13; dy++) {
               for (int dz = 0; dz < 16; dz++) {
                  var st = level.getBlockState(new BlockPos(bx + dx, 50 + dy, bz + dz));
                  histogram.merge(
                     BuiltInRegistries.BLOCK.getKey(st.getBlock()).toString(), 1, Integer::sum
                  );
               }
            }
         }

         JsonObject hist = new JsonObject();
         histogram.forEach((k, v) -> hist.addProperty(k, v));
         json.add("blocks", hist);
         return json;
      });
   }

   /**
    * Прямой прогон {@code BlockState.entityInside} по указанной позиции на ближайшей
    * живой сущности заданного типа. Нужен потому, что «Portal to Nothing» в 1.21
    * срабатывает только из {@code Entity.move()}, а для этого сущность должна
    * двигаться в живом мире. Здесь проверяется сама логика блока: 8 единиц
    * magic-урона при {@code tickCount > 20} и пустая коллизия.
    *
    * <p>{@code {"x":404,"y":102,"z":-386,"type":"minecraft:cow","radius":5}}
    */
   private static JsonElement blockEntityInside(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = new BlockPos(params.get("x").getAsInt(), params.get("y").getAsInt(),
            params.get("z").getAsInt());
         String type = params.has("type") ? params.get("type").getAsString() : "minecraft:cow";
         double radius = params.has("radius") ? params.get("radius").getAsDouble() : 5.0;

         net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
         jsonBlockEntityInsideHeader(state, level, pos);

         java.util.List<net.minecraft.world.entity.LivingEntity> found =
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
               new net.minecraft.world.phys.AABB(pos).inflate(radius),
               e -> BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals(type));

         if (found.isEmpty()) {
            return jsonBlockEntityInsideEmpty(type, state, level, pos);
         }

         net.minecraft.world.entity.LivingEntity target = found.get(0);
         // поднимаем потолок здоровья, чтобы вынести одно срабатывание
         net.minecraft.world.entity.ai.attributes.AttributeInstance maxHp =
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
         if (maxHp != null) {
            maxHp.setBaseValue(400.0);
         }
         target.setHealth(target.getMaxHealth());

         float before = target.getHealth();
         state.entityInside(level, pos, target);
         float after = target.getHealth();

         JsonObject json = jsonBlockEntityInsideHeader(state, level, pos);
         json.addProperty("found", found.size());
         json.addProperty("type", BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
         json.addProperty("tickCount", target.tickCount);
         json.addProperty("before", before);
         json.addProperty("after", after);
         json.addProperty("dropped", before - after);
         return json;
      });
   }

   private static JsonObject jsonBlockEntityInsideHeader(
      net.minecraft.world.level.block.state.BlockState state, ServerLevel level, BlockPos pos
   ) {
      JsonObject json = new JsonObject();
      json.addProperty("ok", true);
      json.addProperty("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
      json.addProperty("collisionEmpty", state.getCollisionShape(level, pos).isEmpty());
      return json;
   }

   private static JsonElement jsonBlockEntityInsideEmpty(
      String type, net.minecraft.world.level.block.state.BlockState state, ServerLevel level, BlockPos pos
   ) {
      JsonObject json = jsonBlockEntityInsideHeader(state, level, pos);
      json.addProperty("type", type);
      json.addProperty("found", 0);
      json.addProperty("dropped", 0);
      json.addProperty("note", "цель не найдена");
      return json;
   }

   /**
    * Построить целый проход по чанку: {@code {"cx":25,"cz":-25,"y":50,
    * "north":true,"south":false,"west":true,"east":false,"feature":0}}.
    * Возвращает счётчики поставленных блоков по видам.
    */
private static JsonElement mazePassage(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         int cx = params.get("cx").getAsInt();
         int cz = params.get("cz").getAsInt();
         int y = params.has("y") ? params.get("y").getAsInt() : 50;
         thaumcraft.dimension.outer_lands.maze.Cell cell = new thaumcraft.dimension.outer_lands.maze.Cell();
         cell.north = !params.has("north") || params.get("north").getAsBoolean();
         cell.south = params.has("south") && params.get("south").getAsBoolean();
         cell.west = params.has("west") && params.get("west").getAsBoolean();
         cell.east = params.has("east") && params.get("east").getAsBoolean();
         if (params.has("feature")) {
            cell.feature = (byte)params.get("feature").getAsInt();
         }

         thaumcraft.dimension.outer_lands.maze.GenCommon.DECO_COMMON.clear();
         thaumcraft.dimension.outer_lands.maze.GenCommon.DECO_URN.clear();
         thaumcraft.dimension.outer_lands.maze.GenCommon.CRAB_SPAWNER.clear();

         long t0 = System.currentTimeMillis();
         thaumcraft.dimension.outer_lands.maze.GenPassage.generateDefaultPassage(
            level, level.getRandom(), cx, cz, y, cell
         );
         long took = System.currentTimeMillis() - t0;

         // Считаем, что получилось: пол/потолок/стены должны быть на месте
         int bx = cx * 16;
         int bz = cz * 16;
         java.util.Map<String, Integer> histogram = new java.util.TreeMap<>();
         for (int dx = 0; dx < 16; dx++) {
            for (int dy = 0; dy <= 10; dy++) {
               for (int dz = 0; dz < 16; dz++) {
                  var st = level.getBlockState(new BlockPos(bx + dx, y + dy, bz + dz));
                  histogram.merge(
                     BuiltInRegistries.BLOCK.getKey(st.getBlock()).toString(), 1, Integer::sum
                  );
               }
            }
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("tookMs", took);
         json.addProperty("decoCommon", thaumcraft.dimension.outer_lands.maze.GenCommon.DECO_COMMON.size());
         json.addProperty("decoUrn", thaumcraft.dimension.outer_lands.maze.GenCommon.DECO_URN.size());
         json.addProperty("crabSpawner", thaumcraft.dimension.outer_lands.maze.GenCommon.CRAB_SPAWNER.size());
         // пол y и потолок y+10 в центре прохода (7x7)
         json.addProperty("floorCenter", BuiltInRegistries.BLOCK.getKey(
            level.getBlockState(new BlockPos(bx + 8, y, bz + 8)).getBlock()).toString());
         json.addProperty("ceilingCenter", BuiltInRegistries.BLOCK.getKey(
            level.getBlockState(new BlockPos(bx + 8, y + 10, bz + 8)).getBlock()).toString());
         JsonObject hist = new JsonObject();
         histogram.forEach((k, v) -> hist.addProperty(k, v));
         json.add("blocks", hist);
         return json;
      });
   }

   /** Прочитать одну ячейку лабиринта по координатам чанка. */
   private static JsonElement mazeCell(JsonObject params) {
      return onServer(() -> {
         int cx = params.get("x").getAsInt();
         int cz = params.get("z").getAsInt();
         thaumcraft.dimension.outer_lands.maze.Cell c =
            thaumcraft.dimension.outer_lands.maze.MazeHandler.getFromHashMap(
               new thaumcraft.dimension.outer_lands.maze.CellLoc(cx, cz)
            );
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("present", c != null);
         if (c != null) {
            json.addProperty("north", c.north);
            json.addProperty("south", c.south);
            json.addProperty("east", c.east);
            json.addProperty("west", c.west);
            json.addProperty("feature", c.feature);
            json.addProperty("packed", c.pack());
         }

         return json;
      });
   }

   private static JsonElement debugHurt(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         float before = player.getHealth();
         boolean result = player.hurt(player.level().damageSources().magic(), 5.0F);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("result", result);
         json.addProperty("before", before);
         json.addProperty("after", player.getHealth());
         return json;
      });
   }

   /** Прямой прогон авторемонта Repair на предмете в руке (тест логики без тика игрока). */
   private static JsonElement itemRepair(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         ItemStack stack = params.has("slot")
            ? player.getInventory().getItem(params.get("slot").getAsInt())
            : player.getMainHandItem();
         int before = stack.getDamageValue();
         thaumcraft.event.common.EventHandlerEntity.doRepair(stack, player);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("damageBefore", before);
         json.addProperty("damageAfter", stack.getDamageValue());
         return json;
      });
   }

   /** Пошаговый рост мана-боба (тест checkGrowth без ожидания randomTick). */
   private static JsonElement manaGrow(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos(params));
         if (!(be instanceof thaumcraft.block.mana_pod.ManaPodBlockEntity pod)) {
            return error("no mana pod at " + pos(params));
         }
         int steps = params.has("steps") ? params.get("steps").getAsInt() : 1;
         for (int i = 0; i < steps; i++) {
            pod.checkGrowth();
         }
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("age", pod.getBlockState().getValue(thaumcraft.block.mana_pod.BlockManaPod.AGE));
         json.addProperty("aspect", pod.aspect == null ? "" : pod.aspect.getTag());
         return json;
      });
   }

   /** Состояние зеркала: связь, нестабильность, очередь выдачи. */
   private static JsonElement mirrorInfo(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos(params));
         JsonObject json = new JsonObject();
         if (be instanceof thaumcraft.block.mirror.MirrorBlockEntity mirror) {
            json.addProperty("ok", true);
            json.addProperty("linked", mirror.linked && mirror.isLinkValidSimple());
            json.addProperty("instability", mirror.instability);
            json.addProperty("ticks", mirror.tickCount());
            json.addProperty("queued", mirror.queuedStacks());
         } else if (be instanceof thaumcraft.block.mirror.MirrorEssenceBlockEntity essence) {
            json.addProperty("ok", true);
            json.addProperty("linked", essence.linked && essence.isLinkValidSimple());
            json.addProperty("instability", essence.instability);
         } else {
            return error("no mirror at " + pos(params));
         }

         return json;
      });
   }

   /** Список завершённых исследований. */
   private static JsonElement researchState(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         JsonArray array = new JsonArray();
         for (String key : thaumcraft.research.PlayerKnowledge.of(player).getResearchCompleted()) {
            array.add(key);
         }

         json.add("completed", array);
         return json;
      });
   }

   /**
    * Снять исследование у игрока: {"key":"ender_outer"}.
    *
    * <p>Нужно, чтобы проверить выдачу скрытых исследований: перед тестом снимаем
    * ключ, после прохода ждём, что он вернётся сам.
    */
   private static JsonElement researchRemove(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         String key = params.get("key").getAsString();
         boolean removed = thaumcraft.research.PlayerKnowledge.of(player).getResearchCompleted().remove(key);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("key", key);
         json.addProperty("removed", removed);
         return json;
      });
   }

   /**
    * Исследования, доступные игроку прямо сейчас: {"player":"fake"}.
    *
    * <p>Показывает дерево вслед за пройденными предками — тем же путём, каким
    * рисуется Таумономикон. Нужен, чтобы доказать, что ветка элдричей
    * разблокировалась, а не только один ключ появился в списке.
    */
   private static JsonElement researchAvailable(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         var completed = thaumcraft.research.PlayerKnowledge.of(player).getResearchCompleted();
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         JsonArray array = new JsonArray();

         for (var category : thaumcraft.api.research.ResearchApi.getResearchLists().values()) {
            for (var entry : category.research.values()) {
               if (completed.contains(entry.key)) {
                  continue;
               }

               if (entry.doesPlayerHaveRequisites(player, completed)) {
                  array.add(entry.key);
               }
            }
         }

         json.add("available", array);
         json.addProperty("available_count", array.size());
         return json;
      });
   }

   /** Добавить исследование игроку: {"key":"silver_cap"}. */
   private static JsonElement researchAdd(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         String key = params.get("key").getAsString();
         thaumcraft.research.PlayerKnowledge.of(player).getResearchCompleted().add(key);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("key", key);
         return json;
      });
   }

   /**
    * Разместить/стереть аспект на столе исследований — сквозной вызов настоящего
    * пакета {@link thaumcraft.network.playerdata.PacketAspectPlaceToServer},
    * чтобы тесты проверяли ровно тот код, что крутится в игре.
    *
    * <p>Опционально {@code bonus}: засеять бонусные аспекты стола (в игре их копит
    * {@code recalculateBonus}), чтобы проверить приоритет списания пула игрока.
    */
   private static JsonElement researchAspectPlace(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         ResearchTableMenu menu = player.containerMenu instanceof ResearchTableMenu m ? m : null;
         if (menu == null || menu.table == null) {
            return error("fake player must have a research table menu open");
         }

         if (params.has("bonus")) {
            for (String entry : params.get("bonus").getAsString().split(",")) {
               String[] parts = entry.trim().split(":");
               if (parts.length == 2) {
                  Aspect a = Aspect.getAspect(parts[0].trim());
                  if (a != null) {
                     menu.table.bonusAspects.merge(a, Integer.parseInt(parts[1].trim()));
                  }
               }
            }

            menu.table.setChanged();
            menu.table.markForUpdate();
         }

         // Настоящая записка: у свежего research_notes нет NBT, и пакет вышел бы
         // сразу. createNote раскладывает гексы по кругу — как в игре.
         if (params.has("note")) {
            ItemStack made = ResearchManager.createNote(
               new ItemStack(thaumcraft.init.registry.ModItems.RESEARCH_NOTES.get()),
               params.get("note").getAsString(), menu.table.getLevel());
            if (made.isEmpty()) {
               return error("unknown research: " + params.get("note").getAsString());
            }

            menu.getSlot(1).set(made);
            menu.broadcastChanges();
         }

         if (params.has("q") && params.has("r")) {
            String tag = params.has("aspect") ? params.get("aspect").getAsString() : null;
            Aspect aspect = tag == null || tag.isBlank() ? null : Aspect.getAspect(tag);
            new thaumcraft.network.playerdata.PacketAspectPlaceToServer(
               params.get("q").getAsByte(), params.get("r").getAsByte(), aspect).executeServer(player);
            menu.broadcastChanges();
         }

         PlayerKnowledge knowledge = PlayerKnowledge.of(player);
         ItemStack note = menu.getSlot(1).getItem();
         ResearchNoteData data = note.isEmpty() ? null : ResearchManager.getData(note);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         JsonObject pool = new JsonObject();
         for (Aspect a : Aspect.aspects.values()) {
            short amt = knowledge.getAspectPoolFor(a);
            if (amt > 0) {
               pool.addProperty(a.getTag(), amt);
            }
         }

         json.add("pool", pool);
         JsonObject bonus = new JsonObject();
         for (Aspect a : Aspect.aspects.values()) {
            int amt = menu.table.bonusAspects.getAmount(a);
            if (amt > 0) {
               bonus.addProperty(a.getTag(), amt);
            }
         }

         json.add("bonus", bonus);
         json.addProperty("copies", data == null ? 0 : data.copies);
         json.addProperty("noteComplete", data != null && data.complete);
         json.addProperty("noteDiscovery",
            !note.isEmpty() && note.getItem() instanceof thaumcraft.item.research_notes.ItemResearchNotes notes
               && notes.isDiscovery(note));
         json.addProperty("inkDamage", menu.getSlot(0).getItem().getDamageValue());

         // Гексы заметки: тестам нужно знать, куда класть аспекты (круг случайный).
         com.google.gson.JsonArray hexes = new com.google.gson.JsonArray();
         if (data != null) {
            for (java.util.Map.Entry<String, thaumcraft.util.HexUtils.Hex> pair : data.hexes.entrySet()) {
               ResearchManager.HexEntry entry = data.hexEntries.get(pair.getKey());
               if (entry == null) {
                  continue;
               }

               thaumcraft.util.HexUtils.Hex hex = pair.getValue();
               com.google.gson.JsonObject h = new com.google.gson.JsonObject();
               h.addProperty("q", hex.q);
               h.addProperty("r", hex.r);
               h.addProperty("type", entry.type);
               h.addProperty("aspect", entry.aspect == null ? "" : entry.aspect.getTag());

               // Для гекса кольца — какие аспекты с ним соединятся (то же правило,
               // что в checkConnections): компонент одного = другой. Тесты
               // используют это, чтобы решить головоломку без угадывания.
               com.google.gson.JsonArray options = new com.google.gson.JsonArray();
               if (entry.type == 1 && entry.aspect != null) {
                  for (Aspect other : Aspect.aspects.values()) {
                     if (other == entry.aspect || other.getComponents() == null) {
                        continue;
                     }

                     Aspect[] comp = other.getComponents();
                     if (comp != null && (comp[0] == entry.aspect || comp[1] == entry.aspect)) {
                        options.add(other.getTag());
                     }

                     comp = entry.aspect.getComponents();
                     if (comp != null && (comp[0] == other || comp[1] == other)) {
                        options.add(other.getTag());
                     }
                  }
               }

               h.add("options", options);

               // Соседи по гекс-сетке: тесту нужно знать, в какой пустой гекс
               // класть соединяющий аспект для конкретного гекса кольца.
               com.google.gson.JsonArray neighbours = new com.google.gson.JsonArray();
               for (int n = 0; n < 6; n++) {
                  thaumcraft.util.HexUtils.Hex nb = hex.getNeighbour(n);
                  com.google.gson.JsonObject o = new com.google.gson.JsonObject();
                  o.addProperty("q", nb.q);
                  o.addProperty("r", nb.r);
                  o.addProperty("present", data.hexes.containsKey(nb.toString()));
                  neighbours.add(o);
               }

               h.add("neighbours", neighbours);
               hexes.add(h);
            }
         }

         json.add("hexes", hexes);

         // Теги исследования: комплексные аспекты, ради которых заметка и затевалась.
         com.google.gson.JsonArray tags = new com.google.gson.JsonArray();
         if (data != null && data.key != null) {
            thaumcraft.api.research.ResearchItem item = ResearchApi.getResearch(data.key);
            if (item != null && item.tags != null) {
               for (Aspect a : item.tags.getAspects()) {
                  com.google.gson.JsonObject t = new com.google.gson.JsonObject();
                  t.addProperty("aspect", a.getTag());
                  t.addProperty("amount", item.tags.getAmount(a));
                  t.addProperty("primal", a.isPrimal());
                  tags.add(t);
               }
            }
         }

         json.add("tags", tags);

         // «Мосты» для головоломки: пустой гекс + аспект, который соединяет сразу
         // ДВА гекса кольца. Без перемычки между кольцами DFS от первого гекса
         // не дойдёт до остальных (checkResearchCompletion требует связности).
         com.google.gson.JsonArray bridges = new com.google.gson.JsonArray();
         if (data != null) {
            java.util.Map<String, Aspect> ringAspects = new java.util.HashMap<>();
            for (java.util.Map.Entry<String, thaumcraft.util.HexUtils.Hex> pair : data.hexes.entrySet()) {
               ResearchManager.HexEntry e = data.hexEntries.get(pair.getKey());
               if (e != null && e.type == 1 && e.aspect != null) {
                  ringAspects.put(pair.getKey(), e.aspect);
               }
            }

            for (java.util.Map.Entry<String, thaumcraft.util.HexUtils.Hex> pair : data.hexes.entrySet()) {
               ResearchManager.HexEntry e = data.hexEntries.get(pair.getKey());
               if (e == null || e.type != 0) {
                  continue;
               }

               thaumcraft.util.HexUtils.Hex blank = pair.getValue();
               java.util.List<Aspect> touching = new java.util.ArrayList<>();
               for (int n = 0; n < 6; n++) {
                  thaumcraft.util.HexUtils.Hex nb = blank.getNeighbour(n);
                  Aspect a = ringAspects.get(nb.toString());
                  if (a != null && !touching.contains(a)) {
                     touching.add(a);
                  }
               }

               if (touching.size() < 2) {
                  continue;
               }

               com.google.gson.JsonObject bridge = new com.google.gson.JsonObject();
               bridge.addProperty("q", blank.q);
               bridge.addProperty("r", blank.r);
               com.google.gson.JsonArray opts = new com.google.gson.JsonArray();
               for (Aspect cand : Aspect.aspects.values()) {
                  Aspect[] comp = cand.getComponents();
                  if (comp == null) {
                     continue;
                  }

                  int hits = 0;
                  for (Aspect t : touching) {
                     if (comp[0] == t || comp[1] == t) {
                        hits++;
                     }
                  }

                  if (hits >= 2) {
                     opts.add(cand.getTag());
                  }
               }

               if (opts.size() > 0) {
                  bridge.add("options", opts);
                  bridges.add(bridge);
               }
            }
         }

         json.add("bridges", bridges);

         // Для каждого пустого гекса — аспекты соседних заполненных гексов.
         // Тест растит связную цепочку: мост между уже достижимым гексом и
         // недостижимым, иначе кольцо остаётся несвязным.
         com.google.gson.JsonArray touch = new com.google.gson.JsonArray();
         if (data != null) {
            for (java.util.Map.Entry<String, thaumcraft.util.HexUtils.Hex> pair : data.hexes.entrySet()) {
               ResearchManager.HexEntry e = data.hexEntries.get(pair.getKey());
               if (e == null || e.type != 0) {
                  continue;
               }

               thaumcraft.util.HexUtils.Hex blank = pair.getValue();
               com.google.gson.JsonObject t = new com.google.gson.JsonObject();
               t.addProperty("q", blank.q);
               t.addProperty("r", blank.r);
               com.google.gson.JsonArray around = new com.google.gson.JsonArray();
               for (int n = 0; n < 6; n++) {
                  thaumcraft.util.HexUtils.Hex nb = blank.getNeighbour(n);
                  ResearchManager.HexEntry ne = data.hexEntries.get(nb.toString());
                  if (ne != null && ne.type >= 1 && ne.aspect != null) {
                     around.add(ne.aspect.getTag());
                  }
               }

               t.add("touch", around);
               touch.add(t);
            }
         }

         json.add("touch", touch);
         return json;
      });
   }

   /** Сброс знаний игрока (чистые сканы/исследования для детерминированных тестов). */
   private static JsonElement researchClear(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         thaumcraft.research.PlayerKnowledge.of(player).clear();
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         return json;
      });
   }

   /** Пул аспектов игрока: GET/POST /aspects {"player":"fake"} */
   private static JsonElement aspectPool(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         thaumcraft.research.PlayerKnowledge knowledge = thaumcraft.research.PlayerKnowledge.of(player);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         JsonObject pool = new JsonObject();
         for (thaumcraft.api.aspects.Aspect a : thaumcraft.api.aspects.Aspect.aspects.values()) {
            short amt = knowledge.getAspectPoolFor(a);
            if (amt > 0) {
               pool.addProperty(a.getTag(), amt);
            }
         }
         json.add("aspects", pool);
         return json;
      });
   }

   /** Добавить аспекты в пул игрока: {"aspect":"ignis","amount":10,"player":"fake"} */
   private static JsonElement aspectPoolAdd(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         thaumcraft.research.PlayerKnowledge knowledge = thaumcraft.research.PlayerKnowledge.of(player);
         String tag = params.get("aspect").getAsString();
         int amt = params.has("amount") ? params.get("amount").getAsInt() : 1;
         thaumcraft.api.aspects.Aspect a = thaumcraft.api.aspects.Aspect.getAspect(tag);
         if (a == null) return error("unknown aspect: " + tag);
         knowledge.addAspectPool(a, (short)amt);

         // discover=true — «изучить» аспект, как это делает скан таумометром.
         // Нужно для стола исследований: связи гейтятся hasDiscoveredAspect.
         if (params.has("discover") && params.get("discover").getAsBoolean()) {
            knowledge.addDiscoveredAspect(a);
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("aspect", tag);
         json.addProperty("total", knowledge.getAspectPoolFor(a));
         return json;
      });
   }

   /** Комбинировать аспекты: {"aspect1":"aer","aspect2":"aqua","player":"fake"} */
   private static JsonElement aspectCombine(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         String t1 = params.get("aspect1").getAsString();
         String t2 = params.get("aspect2").getAsString();
         thaumcraft.api.aspects.Aspect a1 = thaumcraft.api.aspects.Aspect.getAspect(t1);
         thaumcraft.api.aspects.Aspect a2 = thaumcraft.api.aspects.Aspect.getAspect(t2);
         if (a1 == null) return error("unknown aspect: " + t1);
         if (a2 == null) return error("unknown aspect: " + t2);
         new thaumcraft.network.playerdata.PacketAspectCombinationToServer(a1, a2, false, false).executeServer(player);
         thaumcraft.api.aspects.Aspect combo = thaumcraft.research.ResearchManager.getCombinationResult(a1, a2);
         thaumcraft.research.PlayerKnowledge k = thaumcraft.research.PlayerKnowledge.of(player);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("aspect1", t1);
         json.addProperty("aspect2", t2);
         json.addProperty("combo", combo != null ? combo.getTag() : null);
         json.addProperty("comboAmount", combo != null ? k.getAspectPoolFor(combo) : 0);
         json.addProperty("discovered", combo != null && k.hasDiscoveredAspect(combo));
         return json;
      });
   }

   /** Инфо о Смотрителе Древних: {"x":..,"y":..,"z":..,"r":16.0} */
   private static JsonElement wardenInfo(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         double x = params.get("x").getAsDouble();
         double y = params.get("y").getAsDouble();
         double z = params.get("z").getAsDouble();
         double r = params.has("r") ? params.get("r").getAsDouble() : 16.0;
         var box = new net.minecraft.world.phys.AABB(x - r, y - r, z - r, x + r, y + r, z + r);
         var list = level.getEntitiesOfClass(thaumcraft.entities.smart.eldrich_warden.EntityEldritchWarden.class, box);
         if (list.isEmpty()) {
            JsonObject none = new JsonObject();
            none.addProperty("ok", true);
            none.addProperty("found", false);
            return none;
         }
         var warden = list.get(0);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("found", true);
         json.addProperty("uuid", warden.getUUID().toString());
         json.addProperty("title", warden.getTitle());
         json.addProperty("hp", warden.getHealth());
         json.addProperty("maxHp", warden.getMaxHealth());
         json.addProperty("shield", warden.getRunicShield());
         return json;
      });
   }

   /** Заспавнить голема: {"x":..,"y":..,"z":..,"type":6,"core":2}. */
   private static JsonElement golemSpawn(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         double x = params.get("x").getAsDouble();
         double y = params.get("y").getAsDouble();
         double z = params.get("z").getAsDouble();
         thaumcraft.entities.smart.golem.EntityGolemBase golem = thaumcraft.init.registry.ModEntities.GOLEM
            .get()
            .create(level);
         if (golem == null) {
            return error("cannot create golem");
         }

         golem.moveTo(x, y, z, 0.0F, 0.0F);
         int type = params.has("type") ? params.get("type").getAsInt() : 1;
         int core = params.has("core") ? params.get("core").getAsInt() : 2;
         golem.setGolemType(
            thaumcraft.entities.smart.golem.EnumGolemType.values()[
               Math.max(0, Math.min(thaumcraft.entities.smart.golem.EnumGolemType.values().length - 1, type))
            ]
         );
         golem.setCore(
            thaumcraft.item.golem_core.GolemCore.values()[
               Math.max(0, Math.min(thaumcraft.item.golem_core.GolemCore.values().length - 1, core))
            ]
         );
         golem.setupGolemInventory();
         level.addFreshEntity(golem);
         return golemJson(golem);
      });
   }

   /** Информация о ближайшем големе: тип/ядро/метки/инвентарь. */
   private static JsonElement golemInfo(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         thaumcraft.entities.smart.golem.EntityGolemBase golem = findGolem(level, params);
         return golem == null ? error("no golem near") : golemJson(golem);
      });
   }

   /** Поставить метку для ближайшего голема: {"x":..,"y":..,"z":..} и позиция голема gx/gy/gz. */
   private static JsonElement golemMark(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         thaumcraft.entities.smart.golem.EntityGolemBase golem = findGolem(level, params);
         if (golem == null) {
            return error("no golem near");
         }

         BlockPos pos = pos(params);
         Direction side = side(params);
         byte color = (byte)(params.has("color") ? params.get("color").getAsInt() : 0);
         golem.getMarkers()
            .add(
               new thaumcraft.entities.smart.golem.Marker(
                  pos, level.dimension(), side == null ? Direction.UP : side, color
               )
            );
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("markers", golem.getMarkers().size());
         return json;
      });
   }

   private static thaumcraft.entities.smart.golem.EntityGolemBase findGolem(ServerLevel level, JsonObject params) {
      double x = params.has("gx") ? params.get("gx").getAsDouble() : params.get("x").getAsDouble();
      double y = params.has("gy") ? params.get("gy").getAsDouble() : params.get("y").getAsDouble();
      double z = params.has("gz") ? params.get("gz").getAsDouble() : params.get("z").getAsDouble();
      double radius = params.has("r") ? params.get("r").getAsDouble() : 8.0;
      var box = new net.minecraft.world.phys.AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
      return level.getEntitiesOfClass(thaumcraft.entities.smart.golem.EntityGolemBase.class, box)
         .stream()
         .min(java.util.Comparator.comparingDouble(golem -> golem.distanceToSqr(x, y, z)))
         .orElse(null);
   }

   private static JsonObject golemJson(thaumcraft.entities.smart.golem.EntityGolemBase golem) {
      JsonObject json = new JsonObject();
      json.addProperty("ok", true);
      json.addProperty("id", golem.getId());
      json.addProperty("uuid", golem.getUUID().toString());
      json.addProperty("type", golem.getGolemType().name());
      json.addProperty("core", golem.getCore().name());
      json.addProperty("markers", golem.getMarkers().size());
      json.addProperty("hasItems", golem.hasSomething());
      json.addProperty("owner", golem.getOwnerName());
      json.addProperty("essentia", golem.essentia == null ? "" : golem.essentia.getTag());
      json.addProperty("essentiaAmount", golem.essentiaAmount);
      json.addProperty("goals", golem.debugGoals());
      json.addProperty("carried", golem.getCarried().isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(golem.getCarried().getItem()).toString());
      json.addProperty("x", golem.getX());
      json.addProperty("y", golem.getY());
      json.addProperty("z", golem.getZ());
      return json;
   }

   /** Сканирование объёма: {"x1","y1","z1","x2","y2","z2","blocks":"id,id,*_suffix"} — счётчики и первая позиция. */
   private static JsonElement scanBox(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         int x1 = Math.min(params.get("x1").getAsInt(), params.get("x2").getAsInt());
         int x2 = Math.max(params.get("x1").getAsInt(), params.get("x2").getAsInt());
         int y1 = Math.min(params.get("y1").getAsInt(), params.get("y2").getAsInt());
         int y2 = Math.max(params.get("y1").getAsInt(), params.get("y2").getAsInt());
         int z1 = Math.min(params.get("z1").getAsInt(), params.get("z2").getAsInt());
         int z2 = Math.max(params.get("z1").getAsInt(), params.get("z2").getAsInt());
         java.util.List<String> wanted = new java.util.ArrayList<>();
         for (String id : params.get("blocks").getAsString().split(",")) {
            if (!id.isBlank()) {
               wanted.add(id.trim());
            }
         }

         JsonObject counts = new JsonObject();
         JsonObject first = new JsonObject();
         net.minecraft.core.BlockPos.MutableBlockPos cursor = new net.minecraft.core.BlockPos.MutableBlockPos();
         int lastCx = Integer.MIN_VALUE;
         int lastCz = Integer.MIN_VALUE;

         for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
               if ((x >> 4) != lastCx || (z >> 4) != lastCz) {
                  level.getChunk(x >> 4, z >> 4);
                  lastCx = x >> 4;
                  lastCz = z >> 4;
               }

               for (int y = y1; y <= y2; y++) {
                  var state = level.getBlockState(cursor.set(x, y, z));
                  if (state.isAir()) {
                     continue;
                  }

                  String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                  boolean hit = false;

                  for (String w : wanted) {
                     if (w.endsWith("*") ? id.startsWith(w.substring(0, w.length() - 1)) : id.equals(w)) {
                        hit = true;
                        break;
                     }
                  }

                  if (hit) {
                     counts.addProperty(id, counts.has(id) ? counts.get(id).getAsInt() + 1 : 1);
                     if (!first.has(id)) {
                        first.addProperty(id, x + " " + y + " " + z);
                     }
                  }
               }
            }
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.add("counts", counts);
         json.add("first", first);
         return json;
      });
   }

   /** Прямой вызов сконфигурированной фичи (отладка worldgen): {"id":"thaumcraft:mana_pods","x":..,"y":..,"z":..}. */
   private static JsonElement placeConfiguredFeature(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE);
         var key = net.minecraft.resources.ResourceKey.create(
            net.minecraft.core.registries.Registries.CONFIGURED_FEATURE,
            ResourceLocation.parse(params.get("id").getAsString())
         );
         var holder = registry.getHolder(key).orElse(null);
         if (holder == null) {
            return error("unknown configured feature: " + key.location());
         }

         net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?> configured = holder.value();
         net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create(level.random.nextLong());
         boolean placed = configured.place(level, level.getChunkSource().getGenerator(), random, pos);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("placed", placed);
         json.addProperty("id", key.location().toString());
         return json;
      });
   }

   /** Положить предмет в инвентарь голема: {"x":..,"y":..,"z":..,"item":"..","count":1}. */
   private static JsonElement golemGive(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         thaumcraft.entities.smart.golem.EntityGolemBase golem = findGolem(level, params);
         if (golem == null) {
            return error("no golem near");
         }

         var registered = BuiltInRegistries.ITEM.get(ResourceLocation.parse(params.get("item").getAsString()));
         ItemStack stack = new ItemStack(registered, params.has("count") ? params.get("count").getAsInt() : 1);
         boolean added = golem.getInventory().addItem(stack);
         JsonObject json = new JsonObject();
         json.addProperty("ok", added);
         json.addProperty("hasItems", golem.hasSomething());
      json.addProperty("essentia", golem.essentia == null ? "" : golem.essentia.getTag());
      json.addProperty("essentiaAmount", golem.essentiaAmount);
      json.addProperty("goals", golem.debugGoals());
         return json;
      });
   }

   private static JsonObject wandJsonInternal(ItemStack stack) {
      JsonObject json = new JsonObject();
      json.addProperty("ok", true);
      json.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
      if (stack.getItem() instanceof thaumcraft.item.wands.ItemWandCasting wand) {
         json.addProperty("maxVis", wand.getMaxVis(stack));
         JsonObject vis = new JsonObject();
         thaumcraft.api.aspects.AspectList all = wand.getAllVis(stack);
         for (thaumcraft.api.aspects.Aspect aspect : all.getAspects()) {
            vis.addProperty(aspect.getTag(), all.getAmount(aspect));
         }

         json.add("vis", vis);
         ItemStack focusStack = wand.getFocusItem(stack);
         json.addProperty(
            "focus",
            focusStack.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(focusStack.getItem()).toString()
         );
         if (focusStack.getItem() instanceof thaumcraft.item.wands.foci.ItemFocusTrade trade) {
            ItemStack picked = trade.getPickedBlock(stack);
            json.addProperty(
               "picked",
               picked == null || picked.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(picked.getItem()).toString()
            );
         }

         if (focusStack.getItem() instanceof thaumcraft.item.wands.foci.ItemFocusRecall) {
            json.addProperty(
               "recallAttuned", thaumcraft.item.wands.foci.ItemFocusRecall.isAttuned(focusStack)
            );
         }
      }

      return json;
   }

   /** Entities around a point (item entities carry their stack). */
   private static JsonElement entities(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         double x = params.get("x").getAsDouble();
         double y = params.get("y").getAsDouble();
         double z = params.get("z").getAsDouble();
         double radius = params.has("r") ? params.get("r").getAsDouble() : 8.0;
         var box = new net.minecraft.world.phys.AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);

         JsonObject json = new JsonObject();
         JsonArray array = new JsonArray();
         for (net.minecraft.world.entity.Entity entity : level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, box)) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", entity.getId());
            entry.addProperty("type", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
            entry.addProperty("uuid", entity.getUUID().toString());
            entry.addProperty("x", entity.getX());
            entry.addProperty("y", entity.getY());
            entry.addProperty("z", entity.getZ());
            if (entity instanceof net.minecraft.world.entity.item.ItemEntity item) {
               entry.addProperty("item", BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString());
               entry.addProperty("count", item.getItem().getCount());
            }

            if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
               entry.addProperty("health", living.getHealth());

               // Активные эффекты: нужны, чтобы проверить воздействие обелиска на
               // элдрич-мобов (сила и сопротивление урону, 1.7.10:
               // TileEldritchObelisk.func_145845_h, строки 52-58) — без них
               // видно только, что моб появился.
               if (params.has("effects") && params.get("effects").getAsBoolean()) {
                  JsonArray fx = new JsonArray();

                  for (var inst : living.getActiveEffects()) {
                     JsonObject one = new JsonObject();
                     var holder = inst.getEffect();
                     one.addProperty("effect", BuiltInRegistries.MOB_EFFECT.getKey(holder.value()).toString());
                     one.addProperty("ticks", inst.getDuration());
                     one.addProperty("amplifier", inst.getAmplifier());
                     fx.add(one);
                  }

                  entry.add("effects", fx);
               }
            }

            array.add(entry);
         }

         json.add("entities", array);
         return json;
      });
   }

   /**
    * Состояние путешествующего сундука: {"x","y","z","r":16}.
    *
    * <p>Отдельный эндпоинт, а не общий /entities, потому что проверять надо не
    * факт появления сущности, а перенесённые с предмета поля: владельца, апгрейд,
    * размер инвентаря и содержимое.
    */
   private static JsonElement trunkInfo(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         double x = params.get("x").getAsDouble();
         double y = params.get("y").getAsDouble();
         double z = params.get("z").getAsDouble();
         double radius = params.has("r") ? params.get("r").getAsDouble() : 16.0;
         var box = new net.minecraft.world.phys.AABB(
            x - radius, y - radius, z - radius, x + radius, y + radius, z + radius
         );

         var list = level.getEntitiesOfClass(
            thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk.class, box
         );

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("count", list.size());
         JsonArray array = new JsonArray();

         for (var trunk : list) {
            JsonObject entry = new JsonObject();
            entry.addProperty("x", trunk.getX());
            entry.addProperty("y", trunk.getY());
            entry.addProperty("z", trunk.getZ());
            entry.addProperty("owner", trunk.func_152113_b());
            entry.addProperty("upgrade", trunk.getUpgrade());
            entry.addProperty("rows", trunk.getRows());
            entry.addProperty("slots", trunk.getInventory().getSlotCount());
            entry.addProperty("has_items", trunk.getInventory().hasItems());
            entry.addProperty("health", trunk.getHealth());
            JsonObject items = new JsonObject();

            for (int i = 0; i < trunk.getInventory().getSlotCount(); i++) {
               var stack = trunk.getInventory().getStackInSlot(i);
               if (!stack.isEmpty()) {
                  items.addProperty(
                     BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount()
                  );
               }
            }

            entry.add("items", items);
            array.add(entry);
         }

         json.add("trunks", array);
         return json;
      });
   }

   /**
    * Прогон броска амулета примальных аспектов N раз: {"player":"fake","count":200000}.
    *
    * <p>Шанс в оригинале 20/20000 на сферу и ровно 1/20000 на шепот, поэтому
    * проверять естественным тиком пришлось бы десятки тысяч тиков. Здесь крутится
    * ровно тот же метод, который зовёт inventoryTick.
    */
   private static JsonElement charmTick(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         int count = params.has("count") ? params.get("count").getAsInt() : 1000;
         ServerLevel level = player.serverLevel();
         int before = 0;

         for (var entity : level.getEntitiesOfClass(
            thaumcraft.entities.brainless.aspect_orb.AspectOrbEntity.class,
            player.getBoundingBox().inflate(24.0)
         )) {
            if (entity.isAlive()) {
               before++;
            }
         }

         boolean hadFocusPrimal = thaumcraft.research.ResearchManager.isResearchComplete(player, "focus_primal")
            || thaumcraft.research.ResearchManager.isResearchComplete(player, "@focus_primal");

         for (int i = 0; i < count; i++) {
            thaumcraft.item.primal_charm.PrimalCharmItem.tickCharm(level, player);
         }

         int after = 0;

         for (var entity : level.getEntitiesOfClass(
            thaumcraft.entities.brainless.aspect_orb.AspectOrbEntity.class,
            player.getBoundingBox().inflate(24.0)
         )) {
            if (entity.isAlive()) {
               after++;
            }
         }

         boolean hasFocusPrimal = thaumcraft.research.ResearchManager.isResearchComplete(player, "focus_primal")
            || thaumcraft.research.ResearchManager.isResearchComplete(player, "@focus_primal");

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("ticks", count);
         json.addProperty("orbs_before", before);
         json.addProperty("orbs_after", after);
         json.addProperty("orbs_spawned", after - before);
         json.addProperty("focus_primal_before", hadFocusPrimal);
         json.addProperty("focus_primal_after", hasFocusPrimal);
         return json;
      });
   }

   /**
    * Прокрутка ванильной таблицы лута N раз: {"table":"minecraft:chests/stronghold_library",
    * "count":400}. Возвращает счётчик предметов Thaumcraft среди выпавшего.
    *
    * <p>Нужен, чтобы проверить инъекцию лута в сундуки: предмет TC4 появляется с
    * вероятностью порядка десятков процентов, на одном открытом сундуке это не
    * видно. Крутится ровно та таблица, что и в игре, из реестра.
    */
   private static JsonElement lootRoll(JsonObject params) {
      return onServer(() -> {
         MinecraftServer server = gameServer();
         if (server == null) {
            return error("no server");
         }

         String id = params.has("table") ? params.get("table").getAsString() : "minecraft:chests/stronghold_library";
         int count = params.has("count") ? params.get("count").getAsInt() : 200;
         ResourceLocation key = ResourceLocation.parse(id);

         var table = server.reloadableRegistries().getLootTable(
            net.minecraft.resources.ResourceKey.create(
               net.minecraft.core.registries.Registries.LOOT_TABLE, key
            )
         );
         if (table == null) {
            return error("unknown loot table: " + id);
         }

         ServerLevel level = server.overworld();
         Map<String, Integer> counts = new LinkedHashMap<>();
         int total = 0;
         int tcItems = 0;

         for (int i = 0; i < count; i++) {
            net.minecraft.world.level.storage.loot.LootParams lootParams =
               new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                  .withParameter(
                     net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                     net.minecraft.world.phys.Vec3.atCenterOf(level.getSharedSpawnPos())
                  )
                  .withParameter(
                     net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, null
                  )
                  .withLuck(0.0F)
                  .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
            var stacks = table.getRandomItems(lootParams, level.random.nextLong());
            if (stacks == null) {
               continue;
            }

            for (var stack : stacks) {
               if (stack.isEmpty()) {
                  continue;
               }

               String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
               counts.merge(name, 1, Integer::sum);
               total++;

               if (name.startsWith("thaumcraft:")) {
                  tcItems++;
               }
            }
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("table", id);
         json.addProperty("rolls", count);
         json.addProperty("total_items", total);
         json.addProperty("thaumcraft_items", tcItems);
         json.addProperty("has_thaumcraft", tcItems > 0);

         int topLimit = params.has("top") ? params.get("top").getAsInt() : 30;
         JsonObject sorted = new JsonObject();
         counts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(topLimit)
            .forEach(e -> sorted.addProperty(e.getKey(), e.getValue()));
         json.add("top", sorted);

         // Точные счётчики по запрошенным предметам. Топ-30 для проверки
         // недостаточно: за 250 прокруток выпадает около 3500 предметов, и редкие
         // (thaumium_hoe, runic_ring) набирают 20-40 штук — этого хватало бы для
         // топа при удаче, но обычно они оказывались за его пределами, и
         // сценарий проверял лут, которого не видел. Параметр items: ["a","b"].
         if (params.has("items") && params.get("items").isJsonArray()) {
            JsonObject exact = new JsonObject();
            for (var e : params.getAsJsonArray("items")) {
               String want = e.getAsString();
               exact.addProperty(want, counts.getOrDefault(want, 0));
            }

            json.add("counts", exact);
         }

         return json;
      });
   }

   /** Фейки, уже добавленные в мир (повторный add роняет сервер). */
   /**
    * Тик инвентаря фейка: {"player":"fake","ticks":600}.
    *
    * <p>Ручной {@code fake.tick()} не каскадирует тики инвентаря — в игре их крутит
    * PlayerList. Поэтому предметы, работающие через {@code inventoryTick}, у фейка
    * молчат. Это ограничение стенда, а не поведение мода, поэтому отдельный
    * эндпоинт вместо правки общего /player/tick: он тикает только инвентарь и не
    * задевает уже настроенные сценарии с надетым снаряжением.
    */
   private static JsonElement playerInventoryTick(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         int ticks = params.has("ticks") ? params.get("ticks").getAsInt() : 20;
         int stacks = 0;

         for (var stack : player.getInventory().items) {
            if (!stack.isEmpty()) {
               stacks++;
            }
         }

         int done = 0;

         for (int i = 0; i < ticks; i++) {
            int slot = 0;

            for (net.minecraft.world.item.ItemStack stack : player.getInventory().items) {
               if (!stack.isEmpty()) {
                  stack.inventoryTick(player.level(), player, slot, false);
               }

               slot++;
            }

            player.getInventory().tick();
            done++;
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("ticked", done);
         json.addProperty("stacks", stacks);
         return json;
      });
   }

   /**
    * Слоты Curios игрока: {"player":..} → {"slots":{"amulet":"thaumcraft:vis_amulet",..}}.
    *
    * <p>Аксессуары живут в capability Curios, а не в инвентаре, поэтому обычный
    * {@code /hand} их не видит. Нужно аксессуарам, работающим на.curioTick:
    * амулет виса переливает заряд в жезл и тянет вис из ретрансляторов.
    */
   private static JsonElement curioState(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         JsonObject slots = new JsonObject();
         var handler = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player);

         if (handler.isPresent()) {
            for (var entry : handler.get().getCurios().entrySet()) {
               var stack = entry.getValue().getStacks().getStackInSlot(0);
               slots.addProperty(entry.getKey(),
                  stack.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            }
         }

         json.add("slots", slots);
         return json;
      });
   }

   /**
    * Кладёт предмет в первый слот Curios, который его принимает:
    * {"player":..,"item":..} → {"slot":"amulet","equipped":true}.
    *
    * <p>В оригинале аксессуары надеваются через Baubles командой, здесь — через
    * Curios. Слот подбирается тем же правилом, что и в игре: первый, для
    * которого предмет возвращает {@code canEquip}.
    */
   private static JsonElement curioEquip(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         String itemId = params.has("item") ? params.get("item").getAsString() : "";
         var item = BuiltInRegistries.ITEM.get(
            net.minecraft.resources.ResourceLocation.parse(itemId));

         if (!(item instanceof top.theillusivec4.curios.api.type.capability.ICurioItem curio)) {
            return error("не аксессуар: " + itemId);
         }

         var handler = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player);

         if (handler.isEmpty()) {
            return error("у игрока нет слотов Curios");
         }

         ItemStack stack = new ItemStack(item);

         for (var entry : handler.get().getCurios().entrySet()) {
            var context = new top.theillusivec4.curios.api.SlotContext(
               entry.getKey(), player, 0, false, true);

            if (curio.canEquip(context, stack)) {
               handler.get().setEquippedCurio(entry.getKey(), 0, stack);
               JsonObject json = new JsonObject();
               json.addProperty("ok", true);
               json.addProperty("slot", entry.getKey());
               json.addProperty("equipped", true);
               return json;
            }
         }

         return error("предмет не подходит ни к одному слоту: " + itemId);
      });
   }

   /** Фейки, уже добавленные в мир (повторный add роняет сервер). */
   /**
    * Шанс великого дерева в биоме: {"x","y","z"} → {"biome":..,"support":0.8}.
    *
    * <p>Нужен тесту великого дерева: посадка идёт через configured feature, а та
    * внутри отсекается гейтом биома. Если площадка попадает в биом без поддержки
    * (например eerie), дерево не растёт, и число посадок гуляет от прогона к
    * прогону. Здесь видно, какие точки вообще годятся.
    */
   private static JsonElement biomeGreatwoodSupport(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         var biome = level.getBiome(pos);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("biome", biome.unwrapKey().map(k -> k.location().toString()).orElse("unknown"));
         json.addProperty("support",
            thaumcraft.world.biomes.BiomeHandler.getBiomeSupportsGreatwood(biome, pos));
         return json;
      });
   }

   /** Фейки, уже добавленные в мир (повторный add роняет сервер). */
   /**
    * Гейт исследования для триггеров жезла: {"player":"fake"}.
    *
    * <p>Возвращает, какие из триггеров 2..7 открыты игроку. В 1.7.10
    * {@code WandManager.performTrigger} (строки 874-912) проверяет исследование
    * перед каждым из них, и без этой проверки можно было собрать инфузионный
    * алтарь или окулус стартовым жезлом.
    */
   private static JsonElement wandTriggerAllowed(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);

         // Триггеры 0 и 1 в оригинале не гейтятся — так и здесь.
         json.addProperty("thaumonomicon", true);
         json.addProperty("crucible", true);

         json.addProperty("infernal_furnace",
            thaumcraft.research.ResearchManager.isResearchComplete(player, "infernal_furnace"));
         json.addProperty("infusion",
            thaumcraft.research.ResearchManager.isResearchComplete(player, "infusion"));
         json.addProperty("node_jar",
            thaumcraft.research.ResearchManager.isResearchComplete(player, "node_jar"));
         json.addProperty("thaumatorium",
            thaumcraft.research.ResearchManager.isResearchComplete(player, "thaumatorium"));
         json.addProperty("oculus",
            thaumcraft.research.ResearchManager.isResearchComplete(player, "oculus"));
         json.addProperty("adv_alchemy_furnace",
            thaumcraft.research.ResearchManager.isResearchComplete(player, "adv_alchemy_furnace"));
         return json;
      });
   }

   /** Фейки, уже добавленные в мир (повторный add роняет сервер). */
   /**
    * Использовать предмет по блоку: {"player":"fake","item":"thaumcraft:primordial_pearl",
    * "x","y","z","side":"up"}. Возвращает, что вернул предмет.
    *
    * <p>Нужен для жемчужины: `onItemUseFirst` не вызывается из обычного
    * `gameMode.useItem`, а проверять надо именно его — он разрушает узел.
    */
   private static JsonElement useItemFirst(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         if (params.has("item")) {
            var reg = BuiltInRegistries.ITEM.get(ResourceLocation.parse(params.get("item").getAsString()));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(reg));
         }

         ServerLevel level = level(params);
         BlockPos pos = pos(params);
         String sideName = params.has("side") ? params.get("side").getAsString() : "up";
         net.minecraft.core.Direction side = net.minecraft.core.Direction.valueOf(sideName.toUpperCase(java.util.Locale.ROOT));
         ItemStack stack = player.getMainHandItem();
         var hit = new net.minecraft.world.phys.BlockHitResult(
            net.minecraft.world.phys.Vec3.atCenterOf(pos), side, pos, false);
         var ctx = new net.minecraft.world.item.context.UseOnContext(
            player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);

         var item = stack.getItem();
         if (!(item instanceof thaumcraft.item.eldritch.PrimordialPearlItem pearl)) {
            return error("not a primordial pearl: " + BuiltInRegistries.ITEM.getKey(item).toString());
         }

         var result = pearl.onItemUseFirst(stack, ctx);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("result", result.toString());
         json.addProperty("consumes", result.consumesAction());
         json.addProperty("remaining", stack.getCount());
         return json;
      });
   }

   /**
    * Счётчик пробуждённых боссов (MapBossData из TC4): {"dim":"thaumcraft:outer_lands"}.
    *
    * <p>Вариант боссового зала выбирается как bossCount % 4, поэтому проверять
    * надо сам счётчик: тест, открывающий замок наугад, зависит от того, попадёт
    * ли случайный бросок в нужный остаток, и флакует.
    */
   private static JsonElement bossMapState(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         var bossData = thaumcraft.dimension.outer_lands.data.BossMapData.get(level);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("dim", level.dimension().location().toString());
         json.addProperty("boss_count", bossData == null ? -1 : bossData.bossCount);
         json.addProperty("variant", bossData == null ? -1 : Math.floorMod(bossData.bossCount, 4));
         return json;
      });
   }

   private static final java.util.Set<java.util.UUID> JOINED_FAKES = java.util.Collections
      .synchronizedSet(new java.util.HashSet<>());

   private static JsonElement movePlayer(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         // NB: координаты опциональны — без них только чтение состояния (раньше каждый
         // опрос молча телепортировал фейка и портил измерения).
         if (params.has("x") && params.has("y") && params.has("z")) {
            double x = params.get("x").getAsDouble();
            double y = params.get("y").getAsDouble();
            double z = params.get("z").getAsDouble();
            float yaw = params.has("yaw") ? params.get("yaw").getAsFloat() : player.getYRot();
            float pitch = params.has("pitch") ? params.get("pitch").getAsFloat() : player.getXRot();
            // FakePlayer переопределяет teleportTo() как no-op, поэтому фейка
            // переставляем напрямую; настоящим игрокам нужен корректный пакет
            // телепорта, иначе клиент откатывает позицию назад.
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
               && !(player instanceof net.neoforged.neoforge.common.util.FakePlayer)) {
               serverPlayer.teleportTo(serverPlayer.serverLevel(), x, y, z, yaw, pitch);
            } else {
               player.setPos(x, y, z);
            }

            player.setYRot(yaw);
            player.setXRot(pitch);
            player.setYHeadRot(yaw);
            player.yBodyRot = yaw;
            player.yRotO = yaw;
            player.xRotO = pitch;
         }
         if (params.has("join") && params.get("join").getAsBoolean()
            && player instanceof net.neoforged.neoforge.common.util.FakePlayer fake
            && fake.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // NB: static-флаг недостаточен — выпавший из мира фейк уже не вернуть,
            // проверяем живое присутствие по id.
            if (fake.isRemoved()) {
               return error("fake player was discarded (fell out of world?) — restart server");
            }

            if (serverLevel.getEntity(fake.getId()) != fake) {
               JOINED_FAKES.add(fake.getUUID());
               serverLevel.addFreshEntityWithPassengers(fake);
            }
         }
         if (params.has("levels")) {
            player.giveExperienceLevels(params.get("levels").getAsInt());
         }
         if (params.has("xp")) {
            // Суммарный опыт фейка задаётся через giveExperiencePoints: иначе
            // уровень, прогресс и totalExperience разъезжаются, и отрицательная
            // дельта в фокусе XPDrain ломает состояние игрока.
            player.experienceLevel = 0;
            player.experienceProgress = 0.0F;
            player.totalExperience = 0;
            player.giveExperiencePoints(params.get("xp").getAsInt());
         }
         if (params.has("clearEffects") && params.get("clearEffects").getAsBoolean()) {
            for (net.minecraft.world.effect.MobEffectInstance effect : new java.util.ArrayList<>(player.getActiveEffects())) {
               player.removeEffect(effect.getEffect());
            }
         }
         if (params.has("health")) {
            player.setHealth(params.get("health").getAsFloat());
         }
         if (params.has("sneakSet")) {
            player.setShiftKeyDown(params.get("sneakSet").getAsBoolean());
         }
         if (params.has("stopUsing") && params.get("stopUsing").getAsBoolean()) {
            player.stopUsingItem();
         }
         if (params.has("clearCooldowns") && params.get("clearCooldowns").getAsBoolean()) {
            // Кулдауны фокусов живут 250-1000 тиков и переживают прогон регрессии,
            // из-за чего следующий тест того же фокуса молча не срабатывает.
            for (var item : BuiltInRegistries.ITEM) {
               player.getCooldowns().removeCooldown(item);
            }
         }
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("player", playerKey(params));
         json.addProperty("x", player.getX());
         json.addProperty("y", player.getY());
         json.addProperty("z", player.getZ());
         json.addProperty("health", player.getHealth());
         json.addProperty("maxHealth", player.getMaxHealth());
         json.addProperty("usingItem", player.isUsingItem());
         json.addProperty("usingTicks", player.getTicksUsingItem());
         json.addProperty("sneaking", player.isShiftKeyDown());
         json.addProperty("creative", player.isCreative());
         json.addProperty("spectator", player.isSpectator());
         json.addProperty("instabuild", player.getAbilities().instabuild);
         json.addProperty("invulnerable", player.isInvulnerable());
         com.google.gson.JsonArray effects = new com.google.gson.JsonArray();
         for (net.minecraft.world.effect.MobEffectInstance effect : player.getActiveEffects()) {
            com.google.gson.JsonObject entry = new com.google.gson.JsonObject();
            entry.addProperty("id", effect.getEffect().getRegisteredName());
            entry.addProperty("duration", effect.getDuration());
            entry.addProperty("amplifier", effect.getAmplifier());
            effects.add(entry);
         }

         json.add("effects", effects);
         json.addProperty("yaw", player.getYRot());
         json.addProperty("pitch", player.getXRot());
         json.addProperty("headYaw", player.getYHeadRot());
         json.addProperty("totalExperience", player.totalExperience);
         json.addProperty("experienceLevel", player.experienceLevel);
         com.google.gson.JsonObject armor = new com.google.gson.JsonObject();
         armor.addProperty("feet", BuiltInRegistries.ITEM.getKey(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).getItem()).toString());
         armor.addProperty("legs", BuiltInRegistries.ITEM.getKey(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).getItem()).toString());
         armor.addProperty("chest", BuiltInRegistries.ITEM.getKey(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem()).toString());
         armor.addProperty("head", BuiltInRegistries.ITEM.getKey(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem()).toString());
         json.add("armor", armor);
         return json;
      });
   }

   /** Тикнуть фейка N раз (armor ticks, livingTick и т.п. без клиента). Ошибки тика возвращаются, не роняют мост. */
   private static JsonElement playerTick(JsonObject params) {
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         int ticks = params.has("ticks") ? params.get("ticks").getAsInt() : 20;
         int ok = 0;
         String lastError = "";
         for (int i = 0; i < ticks; i++) {
            try {
               if (player instanceof net.neoforged.neoforge.common.util.FakePlayer fake) {
                  fake.tick();
                  // baseTick обновляет воду/лаву/гравитацию (wasTouchingWater и т.п.),
                  // без него фейк физически мёртв.
                  try {
                     fake.baseTick();
                  } catch (Throwable ignored) {
                  }

                  try {
                     fake.aiStep();
                  } catch (Throwable ignored) {
                  }
                  // Ручной tick() фейка не каскадирует тики инвентаря (в игре их крутит
                  // PlayerList/чанки), поэтому тикаем надетое/содержимое рук напрямую.
                  tickEquipment(fake);
               } else {
                  break;
               }

               ok++;
            } catch (Throwable t) {
               lastError = String.valueOf(t);
               break;
            }
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("ticked", ok);
         json.addProperty("lastError", lastError);
         json.addProperty("tickCount", player.tickCount);
         json.addProperty("x", player.getX());
         json.addProperty("y", player.getY());
         json.addProperty("z", player.getZ());
         return json;
      });
   }

   /** Тик экипировки и рук фейка (эмуляция части PlayerList.tick для тестов). */
   private static final java.util.Map<java.util.UUID, Integer> USE_TICKS = new java.util.HashMap<>();

   private static void tickEquipment(ServerPlayer player) {
      net.minecraft.world.level.Level level = player.level();
      int slot = 0;
      for (net.minecraft.world.entity.EquipmentSlot equipSlot : new net.minecraft.world.entity.EquipmentSlot[]{
         net.minecraft.world.entity.EquipmentSlot.FEET, net.minecraft.world.entity.EquipmentSlot.LEGS,
         net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.HEAD,
         net.minecraft.world.entity.EquipmentSlot.MAINHAND, net.minecraft.world.entity.EquipmentSlot.OFFHAND
      }) {
         ItemStack stack = player.getItemBySlot(equipSlot);
         if (!stack.isEmpty()) {
            stack.inventoryTick(level, player, slot, equipSlot == net.minecraft.world.entity.EquipmentSlot.MAINHAND);
         }

         slot++;
      }

      // Эмуляция ченнелинга использования предмета: у фейка каунтдаун useTicks
      // не тикает сам (нет PlayerList.tick), поэтому ведём счётчик здесь и зовём
      // onUseTick с убывающим остатком — как ванилла для настоящих игроков.
      if (player.isUsingItem() && player.getUseItem().getItem() instanceof thaumcraft.item.wands.ItemWandCasting wand) {
         ItemStack using = player.getUseItem();
         int elapsed = USE_TICKS.merge(player.getUUID(), 1, Integer::sum);
         int remaining = Math.max(0, using.getUseDuration(player) - elapsed);
         wand.onUseTick(player.level(), player, using, remaining);
      } else {
         USE_TICKS.remove(player.getUUID());
      }
   }

   private static JsonElement players() {
      return onServer(() -> {
         JsonArray array = new JsonArray();
         for (ServerPlayer player : gameServer().getPlayerList().getPlayers()) {
            array.add(playerJson(player));
         }

         return array;
      });
   }

   // ------------------------------------------------------------------
   // menus
   // ------------------------------------------------------------------

   private static JsonElement menu(JsonObject params) {
      return onServer(() -> {
         AbstractContainerMenu menu = activeMenu(params);
         JsonObject json = new JsonObject();
         json.addProperty("containerId", menu.containerId);
         json.addProperty("player", playerKey(params));
         JsonArray slots = new JsonArray();
         for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            JsonObject entry = itemJson(i, slot.getItem());
            entry.addProperty("x", slot.x);
            entry.addProperty("y", slot.y);
            slots.add(entry);
         }

         json.add("slots", slots);
         json.addProperty("playerInventoryStart", menu.slots.size() - 36);
         return json;
      });
   }

   private static JsonElement openMenu(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         net.minecraft.world.MenuProvider provider = null;
         BlockPos p = null;
         if (params.has("x") && params.has("y") && params.has("z")) {
            p = pos(params);
            BlockEntity blockEntity = level.getBlockEntity(p);
            provider = blockEntity instanceof net.minecraft.world.MenuProvider beMenu
               ? beMenu
               : level.getBlockState(p).getMenuProvider(level, p);
            if (provider == null) {
               var box = new net.minecraft.world.phys.AABB(p).inflate(1.5);
               for (net.minecraft.world.entity.Entity e : level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, box)) {
                  if (e instanceof net.minecraft.world.MenuProvider mp) {
                     provider = mp;
                     break;
                  }
               }
            }
         } else if (params.has("entity")) {
            net.minecraft.world.entity.Entity e = null;
            if (params.get("entity").isJsonPrimitive() && params.get("entity").getAsJsonPrimitive().isNumber()) {
               e = level.getEntity(params.get("entity").getAsInt());
            } else if (params.get("entity").isJsonPrimitive()) {
               try {
                  e = level.getEntity(java.util.UUID.fromString(params.get("entity").getAsString()));
               } catch (Exception ignored) {}
            }
            if (e instanceof net.minecraft.world.MenuProvider mp) {
               provider = mp;
            }
         } else if (params.has("item")) {
            String itemId = params.get("item").getAsString();
            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
            if (item instanceof net.minecraft.world.MenuProvider mp) {
               provider = mp;
            }
         }

         JsonObject json = new JsonObject();
         if (provider != null) {
            if (playerKey(params).equals("fake")) {
               ServerPlayer fake = fakePlayer();
               AbstractContainerMenu menu = provider.createMenu(nextContainerId++, fake.getInventory(), fake);
               if (menu != null) {
                  FAKE_MENUS.put("fake", menu);
                  fake.containerMenu = menu;
                  json.addProperty("ok", true);
                  json.addProperty("player", "fake");
                  json.addProperty("containerId", menu.containerId);
                  json.addProperty("slotCount", menu.slots.size());
                  json.addProperty("title", provider.getDisplayName().getString());
               } else {
                  json.addProperty("ok", false);
                  json.addProperty("error", "provider.createMenu returned null");
               }
            } else {
               ServerPlayer player = firstPlayer();
               player.openMenu(provider, p != null ? p : player.blockPosition());
               json.addProperty("ok", true);
               json.addProperty("player", player.getName().getString());
            }
         } else {
            json.addProperty("ok", false);
            json.addProperty("error", "no menu provider found");
         }

         return json;
      });
   }

   private static JsonElement clickMenu(JsonObject params) {
      int slot = params.get("slot").getAsInt();
      int button = params.has("button") ? params.get("button").getAsInt() : 0;
      String typeName = params.has("type") ? params.get("type").getAsString() : "PICKUP";

      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         AbstractContainerMenu menu = activeMenu(params);
         ClickType type = ClickType.valueOf(typeName);
         menu.clicked(slot, button, type, player);
         menu.broadcastChanges();

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("slot", slot);
         json.addProperty("type", typeName);
         ItemStack carried = menu.getCarried();
         json.addProperty("carried", carried.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(carried.getItem()) + " x" + carried.getCount());

         // report the slot contents afterwards so a script can follow along
         JsonArray after = new JsonArray();
         for (int i = 0; i < menu.slots.size(); i++) {
            ItemStack stack = menu.slots.get(i).getItem();
            if (!stack.isEmpty()) {
               after.add(itemJson(i, stack));
            }
         }

         json.add("slots", after);
         return json;
      });
   }

   private static JsonElement clickMenuButton(JsonObject params) {
      int id = params.get("id").getAsInt();
      return onServer(() -> {
         ServerPlayer player = playerFor(params);
         AbstractContainerMenu menu = activeMenu(params);
         boolean handled = menu.clickMenuButton(player, id);
         menu.broadcastChanges();
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("id", id);
         json.addProperty("handled", handled);
         return json;
      });
   }

   private static JsonElement closeMenu(JsonObject params) {
      return onServer(() -> {
         if (playerKey(params).equals("fake")) {
            FAKE_MENUS.remove("fake");
            ServerPlayer fake = fakePlayer();
            fake.containerMenu = fake.inventoryMenu;
         } else {
            firstPlayer().closeContainer();
         }

         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         return json;
      });
   }

   private static final Map<String, AbstractContainerMenu> FAKE_MENUS = new java.util.concurrent.ConcurrentHashMap<>();
   private static int nextContainerId = 200;

   private static String playerKey(JsonObject params) {
      return params.has("player") && params.get("player").getAsString().equalsIgnoreCase("fake") ? "fake" : "real";
   }

   private static ServerPlayer playerFor(JsonObject params) {
      if (playerKey(params).equals("fake")) {
         return fakePlayer();
      }
      List<ServerPlayer> players = gameServer().getPlayerList().getPlayers();
      return players.isEmpty() ? fakePlayer() : players.get(0);
   }

   private static ServerPlayer fakePlayer() {
      return net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(gameServer().overworld());
   }

   private static AbstractContainerMenu activeMenu(JsonObject params) {
      if (playerKey(params).equals("fake")) {
         ServerPlayer fake = fakePlayer();
         // реально открытый контейнер важнее записи /menu/open (её тоже видно через containerMenu)
         AbstractContainerMenu menu = fake.containerMenu;
         if (menu == fake.inventoryMenu && FAKE_MENUS.containsKey("fake")) {
            menu = FAKE_MENUS.get("fake");
         }

         return menu;
      }

      return firstPlayer().containerMenu;
   }

   // ------------------------------------------------------------------
   // map, screenshots
   // ------------------------------------------------------------------

   private static JsonElement map(JsonObject params) {
      return onServer(() -> {
         ServerLevel level = level(params);
         int x1 = params.get("x1").getAsInt();
         int z1 = params.get("z1").getAsInt();
         int x2 = params.get("x2").getAsInt();
         int z2 = params.get("z2").getAsInt();
         int y = params.get("y").getAsInt();
         int fromX = Math.min(x1, x2);
         int toX = Math.max(x1, x2);
         int fromZ = Math.min(z1, z2);
         int toZ = Math.max(z1, z2);
         if (toX - fromX > 127 || toZ - fromZ > 127) {
            throw new IllegalArgumentException("map too large, max 128x128 blocks");
         }

         Map<String, Character> legend = new LinkedHashMap<>();
         List<String> rows = new ArrayList<>();
         StringBuilder header = new StringBuilder("     ");
         for (int x = fromX; x <= toX; x++) {
            header.append(x % 10 == 0 ? String.valueOf(Math.floorMod(x, 100) / 10) : ' ');
         }

         rows.add(header.toString());

         for (int z = fromZ; z <= toZ; z++) {
            StringBuilder row = new StringBuilder(String.format("%4d ", z));
            for (int x = fromX; x <= toX; x++) {
               BlockPos pos = new BlockPos(x, y, z);
               BlockState state = level.getBlockState(pos);
               boolean hasEntity = level.getBlockEntity(pos) != null;
               String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
               if (state.isAir()) {
                  row.append('.');
                  continue;
               }

               Character symbol = legend.get(name);
               if (symbol == null) {
                  symbol = symbolFor(legend.size());
                  legend.put(name, symbol);
               }

               row.append(hasEntity ? Character.toUpperCase(symbol) : symbol);
            }

            rows.add(row.toString());
         }

         JsonObject json = new JsonObject();
         json.addProperty("y", y);
         JsonArray lines = new JsonArray();
         rows.forEach(lines::add);
         json.add("rows", lines);
         JsonObject legendJson = new JsonObject();
         legend.forEach((name, symbol) -> legendJson.addProperty(String.valueOf(symbol), name));
         json.add("legend", legendJson);
         json.addProperty("note", "upper case = block entity present");
         return json;
      });
   }

   private static char symbolFor(int index) {
      String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
      return alphabet.charAt(index % alphabet.length());
   }

   /** Client-only bridge actions (screens, connecting); no-op on a dedicated server. */
   private static JsonElement clientBridge(String action, JsonObject params) {
      if (FMLEnvironment.dist.isDedicatedServer()) {
         return error("client only");
      }

      return thaumcraft.client.ClientBridgeActions.handle(action, params);
   }

   private static JsonElement screenshot(JsonObject params) {
      if (FMLEnvironment.dist.isDedicatedServer()) {
         throw new IllegalStateException("screenshots need the client");
      }

      String path = params.has("path") ? params.get("path").getAsString() : "bridge-shot.png";
      try {
         String absolute = DevBridgeClient.screenshot(path);
         JsonObject json = new JsonObject();
         json.addProperty("ok", true);
         json.addProperty("path", absolute);
         return json;
      } catch (Exception e) {
         return error(String.valueOf(e));
      }
   }

   // ------------------------------------------------------------------
   // helpers
   // ------------------------------------------------------------------

   private static JsonObject params(HttpExchange exchange) throws IOException {
      JsonObject params = new JsonObject();

      // query string
      String query = exchange.getRequestURI().getQuery();
      if (query != null) {
         for (String pair : query.split("&")) {
            int index = pair.indexOf('=');
            if (index > 0) {
               params.addProperty(
                  java.net.URLDecoder.decode(pair.substring(0, index), StandardCharsets.UTF_8),
                  java.net.URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8)
               );
            }
         }
      }

      // json body
      if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
         InputStream stream = exchange.getRequestBody();
         String body = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
         if (!body.isBlank()) {
            JsonElement parsed = com.google.gson.JsonParser.parseString(body);
            if (parsed.isJsonObject()) {
               for (Map.Entry<String, JsonElement> entry : parsed.getAsJsonObject().entrySet()) {
                  params.add(entry.getKey(), entry.getValue());
               }
            }
         }
      }

      return params;
   }

   private static void send(HttpExchange exchange, int code, JsonElement body) throws IOException {
      byte[] bytes = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
      exchange.sendResponseHeaders(code, bytes.length);
      try (OutputStream stream = exchange.getResponseBody()) {
         stream.write(bytes);
      }
   }

   private static JsonObject error(String message) {
      JsonObject json = new JsonObject();
      json.addProperty("ok", false);
      json.addProperty("error", message);
      return json;
   }

   @Nullable
   private static MinecraftServer gameServer() {
      return ServerLifecycleHooks.getCurrentServer();
   }

   @Nullable
   private static ServerPlayer firstPlayerOrNull() {
      List<ServerPlayer> players = gameServer().getPlayerList().getPlayers();
      return players.isEmpty() ? null : players.get(0);
   }

   private static ServerPlayer firstPlayer() {
      List<ServerPlayer> players = gameServer().getPlayerList().getPlayers();
      if (players.isEmpty()) {
         throw new IllegalStateException("no player online");
      }

      return players.get(0);
   }

   private static ServerLevel level(JsonObject params) {
      if (params.has("dim") && !params.get("dim").getAsString().isBlank()) {
         net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> key = net.minecraft.resources.ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION, net.minecraft.resources.ResourceLocation.parse(params.get("dim").getAsString())
         );
         ServerLevel level = gameServer().getLevel(key);
         if (level != null) {
            return level;
         }
      }

      ServerPlayer player = firstPlayerOrNull();
      return player != null ? player.serverLevel() : gameServer().overworld();
   }

   private static BlockPos pos(JsonObject params) {
      return new BlockPos(params.get("x").getAsInt(), params.get("y").getAsInt(), params.get("z").getAsInt());
   }

   @Nullable
   private static Direction side(JsonObject params) {
      if (!params.has("side")) {
         return null;
      }

      String side = params.get("side").getAsString();
      return side.isBlank() || side.equalsIgnoreCase("null") ? null : Direction.byName(side);
   }

   @Nullable
   @SuppressWarnings("unchecked")
   private static <T> T findField(@Nullable Object target, Class<T> type) {
      return findField(target, type, null);
   }

   /** Finds a public field of the given type (optionally by name) for capability-less block entities. */
   @Nullable
   @SuppressWarnings("unchecked")
   private static <T> T findField(@Nullable Object target, Class<T> type, @Nullable String name) {
      if (target == null) {
         return null;
      }

      for (Class<?> clazz = target.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
         for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (name != null && !field.getName().equals(name)) {
               continue;
            }

            if (!type.isAssignableFrom(field.getType())) {
               continue;
            }

            try {
               field.setAccessible(true);
               return (T)field.get(target);
            } catch (Exception ignored) {
               // try the next one
            }
         }
      }

      return null;
   }

   private static <T> T onServer(java.util.concurrent.Callable<T> action) {
      MinecraftServer minecraftServer = gameServer();
      if (minecraftServer == null) {
         throw new IllegalStateException("no server running");
      }

      if (minecraftServer.isSameThread()) {
         try {
            return action.call();
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }

      CompletableFuture<T> future = new CompletableFuture<>();
      minecraftServer.execute(() -> {
         try {
            future.complete(action.call());
         } catch (Throwable t) {
            future.completeExceptionally(t);
         }
      });

      try {
         return future.get(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
      } catch (Exception e) {
         throw new RuntimeException("server task failed: " + e, e);
      }
   }
}
