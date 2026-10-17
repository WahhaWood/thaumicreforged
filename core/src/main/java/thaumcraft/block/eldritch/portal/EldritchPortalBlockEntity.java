package thaumcraft.block.eldritch.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.init.registry.ModBlockEntities;

public class EldritchPortalBlockEntity extends BlockEntity implements ITickable {
   public int opencount = -1;
   private int count = 0;

   public EldritchPortalBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.ELDRITCH_PORTAL.get(), pos, state);
   }


   private static final String COOLDOWN_KEY = "thaumcraft:eldritch_portal_cooldown";

   /** Телепорт игроков между миром и Outer Lands (порт TileEldritchPortal из 1.7.10). */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      this.count++;
      if (this.count % 5 != 0 || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
         return;
      }

      net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(pos).inflate(0.5, 1.0, 0.5);
      for (net.minecraft.server.level.ServerPlayer player : serverLevel.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class, area)) {
         this.teleport(serverLevel, pos, player);
      }
   }

   public void teleport(net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.server.level.ServerPlayer player) {
      long now = level.getGameTime();
      if (now - player.getPersistentData().getLong(COOLDOWN_KEY) < 100L) {
         return;
      }

      var links = thaumcraft.dimension.outer_lands.data.PortalLinksData.get(level);
      net.minecraft.server.MinecraftServer server = level.getServer();
      if (links == null || server == null) {
         return;
      }

      if (level.dimension().equals(thaumcraft.init.registry.ModDimensions.OUTER_LANDS)) {
         var link = links.findNearestByOuterChunk(pos.getX() >> 4, pos.getZ() >> 4, 32);
         if (link == null) {
            return;
         }

         net.minecraft.server.level.ServerLevel overworld = server.overworld();
         BlockPos target = link.sourceExit != null ? link.sourceExit : link.portalPos.above();
         this.sendTo(player, overworld, target);
         player.getPersistentData().putLong(COOLDOWN_KEY, now);
         links.touch(link.sourceDimension, link.portalPos, now);
      } else {
         net.minecraft.server.level.ServerLevel outer = server.getLevel(thaumcraft.init.registry.ModDimensions.OUTER_LANDS);
         if (outer == null) {
            return;
         }

         var link = links.getByPortal(level, pos);
         if (link == null) {
            link = links.getOrCreate(level, pos, level.random.nextLong(), 8, 8);
         }

         links.updateSourceExit(level, pos, pos.above(), now);
         links.setDirty();

         // Строим НАСТОЯЩИЙ лабиринт из семи генераторов TC4, а не упрощённые
         // коробки MazeBuilder. Комната входа (feature 1) уже содержит портал
         // обратно, поэтому отдельная «платформа прибытия» больше не нужна.
         if (!link.mazeBuilt) {
            // Сид выводим из чанка связи: раскладка лабиринта тогда зависит
            // только от позиции портала и переживает перезапуск, а поле в Link
            // менять не нужно.
            long mazeSeed = ((long)link.olChunkX << 32) ^ (link.olChunkZ & 0xFFFFFFFFL);
            thaumcraft.dimension.outer_lands.maze.CellLoc portalCell =
               thaumcraft.dimension.outer_lands.maze.MazeHandler.buildRealMaze(
                  outer, link.olChunkX, link.olChunkZ,
                  Math.max(11, link.mazeHalfWidth * 2), Math.max(11, link.mazeHalfHeight * 2),
                  3, mazeSeed);
            link.mazeBuilt = true;
            links.setDirty();

            if (portalCell != null) {
               // Комната входа: пол — древний камень на y+1 (51), ходить надо по
               // y+2 (52), а портал стоит на постаменте на y+3 (53). GenPortal
               // ставит портал в центр комнаты (x+8, z+8) и обелиск прямо над ним,
               // поэтому игрока выводим на четыре блока в сторону и ставим на
               // уровень ходьбы. Если там почему-то занято — поднимаем на блок.
               int walkY = 52;
               BlockPos arrival = new BlockPos(portalCell.x * 16 + 8, walkY, portalCell.z * 16 + 4);
               if (!outer.getBlockState(arrival).isAir() || !outer.getBlockState(arrival.above()).isAir()) {
                  arrival = arrival.above();
               }

               links.updateOlExit(level, pos, arrival, now);
            }
         }

         BlockPos arrival = link.olExit;
         if (arrival == null) {
            // Запасной путь: лабиринта нет (например, старая связь) — ставим
            // простую платформу, чтобы игрок не оказался в пустоте.
            arrival = this.buildArrival(outer, link);
            links.updateOlExit(level, pos, arrival, now);
         }

         this.sendTo(player, outer, arrival);
         this.grantEnterOuter(player);
         player.getPersistentData().putLong(COOLDOWN_KEY, now);
      }
   }

   /**
    * Исследование {@code ender_outer} выдаётся при первом переходе во Внешние земли —
    * порт {@code TileEldritchPortal.teleport} (1.7.10, строки 87-90).
    *
    * <p>В оригинале оно идёт подряд с телепортом, без каких-либо условий. Иначе
    * ветка элдричей непроходима навсегда: {@code ender_outer} — {@code hidden},
    * в Таумономиконе не кликается, других путей выдачи нет, а он единственный
    * родитель {@code outerev} и всего, что дальше по ветке.
    */
   private void grantEnterOuter(net.minecraft.server.level.ServerPlayer player) {
      if (thaumcraft.research.ResearchManager.isResearchComplete(player, "ender_outer")) {
         return;
      }

      thaumcraft.network.PacketHandler.sendTo(
         new thaumcraft.network.playerdata.PacketResearchComplete("ender_outer"), player
      );
      Thaumcraft.setup.getResearchManager().completeResearch(player, "ender_outer");
      thaumcraft.research.ResearchManager.scheduleSave(player);
   }

   /** Телепорт игрока; fake-игрок (без сети) просто перемещается. */
   private void sendTo(net.minecraft.server.level.ServerPlayer player, net.minecraft.server.level.ServerLevel target, BlockPos pos) {
      if (player instanceof net.neoforged.neoforge.common.util.FakePlayer) {
         player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
         return;
      }

      player.teleportTo(target, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
   }

   /** Платформа прибытия в Outer Lands: камень древних, портал в центре, спавн рядом. */
   private BlockPos buildArrival(net.minecraft.server.level.ServerLevel outer, thaumcraft.dimension.outer_lands.data.PortalLinksData.Link link) {
      int cx = link.olChunkX * 16 + 8;
      int cz = link.olChunkZ * 16 + 8;
      // Уровень лабиринта портального пути (пол y+1=51, ходьба y+2=52):
      // платформа на 64 оставляла игрока в войде на 12 блоков выше.
      int y = 52;
      net.minecraft.world.level.block.Block stone = thaumcraft.init.registry.ModBlocks.ANCIENT_STONE.get();
      net.minecraft.world.level.block.Block portal = thaumcraft.init.registry.ModBlocks.ELDRITCH_PORTAL.get();

      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            outer.setBlock(new BlockPos(cx + dx, y - 1, cz + dz), stone.defaultBlockState(), 3);
            if (dx == 0 && dz == 0) {
               outer.setBlock(new BlockPos(cx, y, cz), portal.defaultBlockState(), 3);
            } else if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
               outer.setBlock(new BlockPos(cx + dx, y, cz + dz), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
               outer.setBlock(new BlockPos(cx + dx, y + 1, cz + dz), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            }
         }
      }

      outer.setBlock(new BlockPos(cx, y + 3, cz), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
      return new BlockPos(cx + 2, y, cz);
   }

   public AABB getRenderBoundingBox() {
      BlockPos p = this.getBlockPos();
      return new AABB(p.getX() - 1, p.getY() - 1, p.getZ() - 1, p.getX() + 2, p.getY() + 2, p.getZ() + 2);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.count++;
      if (this.count % 250 == 0 || this.count == 0) {
         SoundEvent snd = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "evilportal"));
         if (snd != null) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, snd, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }
      }

      if (this.opencount < 30) {
         this.opencount++;
      }
   }
}
