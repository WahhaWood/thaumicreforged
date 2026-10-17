package thaumcraft.event.common;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import thaumcraft.util.ItemStackData;

public class ServerTickEventsFML {
   public static final ConcurrentHashMap<Level, ServerTickEventsFML.SwapQueue> swapList = new ConcurrentHashMap<>();
   public static HashMap<ResourceKey<Level>, ArrayList<ChunkPos>> chunksToGenerate = new HashMap<>();

   public static int findCarriedSlot(ServerPlayer player, ItemStack target) {
      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         ItemStack s = player.getInventory().getItem(i);
         if (ItemStackData.sameItemSameTags(s, target) && s.getCount() > 0) {
            return i;
         }
      }

      return -1;
   }

   public static void addSwapper(Level level, BlockPos pos, BlockState sourceState, ItemStack target, int life, Player player, int wandSlot) {
      if (level.isClientSide) {
         return;
      }

      BlockState current = level.getBlockState(pos);
      if (current.isAir() || current.getDestroySpeed(level, pos) < 0.0F) {
         return;
      }

      if (target.getItem() instanceof net.minecraft.world.item.BlockItem targetBlock
         && current.is(targetBlock.getBlock())) {
         return;
      }

      SwapQueue queue = swapList.computeIfAbsent(level, key -> new SwapQueue());
      if (queue.add(pos, new VirtualSwapper(pos, current, target.copy(), life, player.getUUID(), wandSlot))) {
         level.playSound(
            null, pos, thaumcraft.init.registry.ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.25F, 1.0F
         );
      }
   }

   /**
    * Раскладка и прогресс стройки лабиринта переживают рестарт: без этого
    * {@code labyrinth.dat} никто не читал (loadMaze/saveMaze были мёртвым
    * кодом), очередь достройки жила только в RAM, и любой рестарт в первые
    * минуты после входа в портал навсегда оставлял дальние ячейки (включая
    * боссов зал) непостроенными — флаг mazeBuilt при этом уже стоял.
    */
   @net.neoforged.bus.api.SubscribeEvent
   public void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
      net.minecraft.server.level.ServerLevel outer =
         event.getServer().getLevel(thaumcraft.init.registry.ModDimensions.OUTER_LANDS);
      if (outer != null) {
         thaumcraft.dimension.outer_lands.maze.MazeHandler.loadMaze(outer);
         thaumcraft.dimension.outer_lands.maze.MazeHandler.resumeUnbuilt(outer);
      }
   }

   @net.neoforged.bus.api.SubscribeEvent
   public void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
      net.minecraft.server.level.ServerLevel outer =
         event.getServer().getLevel(thaumcraft.init.registry.ModDimensions.OUTER_LANDS);
      if (outer != null) {
         thaumcraft.dimension.outer_lands.maze.MazeHandler.saveMaze(outer);
      }
   }

   /** Обработка очереди обменов: по одному за тик на измерение (как в оригинале). */
   /** TC4 serverWorldTick: notes heard this tick are forgotten at its end. */
   @net.neoforged.bus.api.SubscribeEvent
   public void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
      if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
         java.util.ArrayList<Integer[]> heard = thaumcraft.block.sensor.SensorBlockEntity.noteBlockEvents.get(level);
         if (heard != null) {
            heard.clear();
         }
      }
   }

   @net.neoforged.bus.api.SubscribeEvent
   public void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
      // Достройка лабиринта Внешних земель порциями: блоки пишутся только в
      // серверном потоке, поэтому фоновая генерация идёт по несколько ячеек
      // за тик. Раньше лабиринт строили целиком в один тик (фриз), а потом
      // строили только 7x7 и он обрывался.
      thaumcraft.dimension.outer_lands.maze.MazeHandler.buildQueued(3);

      var it = swapList.entrySet().iterator();
      while (it.hasNext()) {
         var entry = it.next();
         Level level = entry.getKey();
         SwapQueue queue = entry.getValue();
         if (!(level instanceof net.minecraft.server.level.ServerLevel)) {
            continue;
         }

         VirtualSwapper swapper = queue.poll();
         if (swapper == null) {
            it.remove();
            continue;
         }

         processSwap((net.minecraft.server.level.ServerLevel)level, swapper, queue);
      }
   }

   /**
    * Выполнение одного обмена (порт тела {@code tickBlockSwap}):
    * проверка жезла/виса/наличия блока, сбор дропа, установка цели, разлёт по соседям.
    */
   private static void processSwap(
      net.minecraft.server.level.ServerLevel level, VirtualSwapper swapper, SwapQueue queue
   ) {
      Player player = level.getPlayerByUUID(swapper.playerId);
      if (!(player instanceof ServerPlayer serverPlayer)) {
         return;
      }

      player = serverPlayer;

      ItemStack wandStack = player.getInventory().getItem(swapper.wandSlot);
      if (!(wandStack.getItem() instanceof thaumcraft.item.wands.ItemWandCasting wand)) {
         return;
      }

      ItemStack focusStack = wand.getFocusItem(wandStack);
      thaumcraft.api.wands.ItemFocusBasic focus = wand.getFocus(wandStack);
      if (focus == null) {
         return;
      }

      BlockPos pos = swapper.pos;
      BlockState current = level.getBlockState(pos);
      if (current.isAir() || !current.is(swapper.sourceState.getBlock())) {
         return;
      }

      if (!(swapper.target.getItem() instanceof net.minecraft.world.item.BlockItem targetBlockItem)) {
         return;
      }

      BlockState targetState = stateFromStack(targetBlockItem.getBlock(), swapper.target);
      if (current.is(targetState.getBlock())) {
         return;
      }

      thaumcraft.api.aspects.AspectList cost = focus.getVisCost(focusStack);
      if (!wand.consumeAllVis(wandStack, player, cost, false, false)) {
         return;
      }

      boolean creative = player.getAbilities().instabuild;
      int slot = creative ? 1 : findCarriedSlot(serverPlayer, swapper.target);
      if (slot < 0) {
         return;
      }

      if (!creative) {
         player.getInventory().removeItem(slot, 1);
      }

      boolean silk = focus.isUpgradedWith(focusStack, thaumcraft.api.wands.FocusUpgradeType.silktouch);
      int fortune = focus.getUpgradeLevel(focusStack, thaumcraft.api.wands.FocusUpgradeType.treasure);
      java.util.List<ItemStack> drops;
      if (silk) {
         drops = java.util.List.of(current.getBlock().getCloneItemStack(level, pos, current));
      } else {
         drops = current.getDrops(
            new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
               .withParameter(
                  net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                  net.minecraft.world.phys.Vec3.atCenterOf(pos)
               )
               .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, ItemStack.EMPTY)
               .withLuck(fortune)
         );
      }

      level.destroyBlock(pos, false);
      for (ItemStack drop : drops) {
         if (!drop.isEmpty()) {
            net.minecraft.world.level.block.Block.popResource(level, pos, drop);
         }
      }

      level.setBlock(pos, targetState, 3);
      wand.consumeAllVis(wandStack, player, cost, true, false);
      thaumcraft.network.PacketHandler.sendToAllAround(
         new thaumcraft.network.fx.PacketFXBlockSparkle(pos, 12632319), level, pos, 32.0
      );
      level.levelEvent(2001, pos, Block.getId(current));

      // Разлёт на открытых соседей того же блока (глубина — lifespan).
      if (swapper.lifespan > 0) {
         for (int xx = -1; xx <= 1; xx++) {
            for (int yy = -1; yy <= 1; yy++) {
               for (int zz = -1; zz <= 1; zz++) {
                  if (xx == 0 && yy == 0 && zz == 0) {
                     continue;
                  }

                  BlockPos neighbor = pos.offset(xx, yy, zz);
                  if (level.getBlockState(neighbor).is(swapper.sourceState.getBlock())
                     && thaumcraft.util.BlockUtils.isBlockExposed(level, neighbor)) {
                     queue.add(
                        neighbor,
                        new VirtualSwapper(
                           neighbor, level.getBlockState(neighbor), swapper.target, swapper.lifespan - 1, swapper.playerId, swapper.wandSlot
                        )
                     );
                  }
               }
            }
         }
      }
   }

   public static BlockState stateFromStack(Block block, ItemStack stack) {
      BlockState state = block.defaultBlockState();
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null && tag.contains("BlockStateTag", 10)) {
         CompoundTag propsTag = tag.getCompound("BlockStateTag");
         StateDefinition<Block, BlockState> def = block.getStateDefinition();

         for (String key : propsTag.getAllKeys()) {
            Property<?> prop = def.getProperty(key);
            if (prop != null) {
               String val = propsTag.getString(key);
               state = applyProperty(state, prop, val);
            }
         }

         return state;
      } else {
         return state;
      }
   }

   public static <T extends Comparable<T>> BlockState applyProperty(BlockState state, Property<T> prop, String value) {
      Optional<T> v = prop.getValue(value);
      return v.<BlockState>map(t -> (BlockState)state.setValue(prop, t)).orElse(state);
   }

   public static final class SwapQueue {
      public final ArrayDeque<ServerTickEventsFML.VirtualSwapper> queue = new ArrayDeque<>();
      public final LongOpenHashSet dedup = new LongOpenHashSet();

      boolean add(BlockPos pos, ServerTickEventsFML.VirtualSwapper vs) {
         long k = pos.asLong();
         if (!this.dedup.add(k)) {
            return false;
         } else {
            this.queue.add(vs);
            return true;
         }
      }

      ServerTickEventsFML.VirtualSwapper poll() {
         ServerTickEventsFML.VirtualSwapper vs = this.queue.poll();
         if (vs == null) {
            return null;
         } else {
            this.dedup.remove(vs.pos.asLong());
            return vs;
         }
      }
   }

   public static final class VirtualSwapper {
      final int lifespan;
      final BlockPos pos;
      final BlockState sourceState;
      final ItemStack target;
      final int wandSlot;
      final UUID playerId;

      VirtualSwapper(BlockPos pos, BlockState sourceState, ItemStack target, int life, UUID playerId, int wandSlot) {
         this.pos = pos;
         this.sourceState = sourceState;
         this.target = target;
         this.lifespan = life;
         this.playerId = playerId;
         this.wandSlot = wandSlot;
      }
   }
}
