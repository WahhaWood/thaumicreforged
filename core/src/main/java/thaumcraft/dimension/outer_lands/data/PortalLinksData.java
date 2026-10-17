package thaumcraft.dimension.outer_lands.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;

public class PortalLinksData extends SavedData {
   private static final String DATA_NAME = "thaumcraft_portal_links";
   private static final int MAX_ALLOCATION_ATTEMPTS = 2048;
   private static final int PROBE_RANGE = 512;
   private static final long PROBE_INCREMENT = -7046029254386353131L;
   private final Map<PortalLinksData.PortalKey, PortalLinksData.Link> sourcePortalToLink = new HashMap<>();
   private final Long2ObjectOpenHashMap<PortalLinksData.PortalKey> olChunkToPortal = new Long2ObjectOpenHashMap();

   public PortalLinksData() {
   }

   public PortalLinksData(CompoundTag tag) {
      this();
      this.load(tag);
   }

   public static PortalLinksData get(MinecraftServer server) {
      ServerLevel overworld = server.overworld();
      DimensionDataStorage storage = overworld.getDataStorage();
      return (PortalLinksData)storage.computeIfAbsent(new SavedData.Factory<>(PortalLinksData::new, PortalLinksData::load, null), "thaumcraft_portal_links");
   }

   @Nullable
   public static PortalLinksData get(ServerLevel level) {
      MinecraftServer server = level.getServer();
      return server == null ? null : get(server);
   }

   /**
    * Забыть связь: тестовая обвязка dev-моста. Сброс нужен, чтобы проверка
    * «портал обратно остался в лабиринте» каждый раз проходила полный цикл
    * (buildArea + buildArrival), а не цеплялась за уже построенный лабиринт
    * прошлого прогона, где флаг mazeBuilt снял бы постройку заново.
    */
   public boolean removeByPortal(ServerLevel sourceLevel, BlockPos portalPos) {
      PortalLinksData.Link link = this.getByPortal(sourceLevel, portalPos);
      if (link == null) {
         return false;
      }

      this.sourcePortalToLink.remove(new PortalLinksData.PortalKey(sourceLevel.dimension(), portalPos));
      // Ключ обязан совпадать с get/put (ChunkPos.asLong): иначе резерв чанка
      // не снимается и новые порталы отъезжают всё дальше.
      this.olChunkToPortal.remove(ChunkPos.asLong(link.olChunkX, link.olChunkZ));
      this.setDirty();
      return true;
   }

   @Nullable
   public PortalLinksData.Link getByOverworldPortal(BlockPos portalPos) {
      return this.getByPortal(Level.OVERWORLD, portalPos);
   }

   @Nullable
   public PortalLinksData.Link getByPortal(ServerLevel sourceLevel, BlockPos portalPos) {
      return this.getByPortal(sourceLevel.dimension(), portalPos);
   }

   @Nullable
   public PortalLinksData.Link getByPortal(ResourceKey<Level> sourceDimension, BlockPos portalPos) {
      return this.sourcePortalToLink.get(new PortalLinksData.PortalKey(sourceDimension, portalPos));
   }

   @Nullable
   public PortalLinksData.Link getByOuterChunk(int chunkX, int chunkZ) {
      PortalLinksData.PortalKey portalKey = (PortalLinksData.PortalKey)this.olChunkToPortal.get(ChunkPos.asLong(chunkX, chunkZ));
      return portalKey == null ? null : this.sourcePortalToLink.get(portalKey);
   }

   @Nullable
   public PortalLinksData.Link findNearestByOuterChunk(int chunkX, int chunkZ, int maxRadius) {
      PortalLinksData.Link best = null;
      long bestDist = Long.MAX_VALUE;

      for (PortalLinksData.Link link : this.sourcePortalToLink.values()) {
         int dx = link.olChunkX - chunkX;
         int dz = link.olChunkZ - chunkZ;
         long d = (long)dx * dx + (long)dz * dz;
         if (d < bestDist) {
            bestDist = d;
            best = link;
         }
      }

      if (best == null) {
         return null;
      } else {
         return bestDist > (long)maxRadius * maxRadius ? null : best;
      }
   }

   public PortalLinksData.Link getOrCreate(BlockPos overworldPortalPos, long seed) {
      return this.getOrCreate(Level.OVERWORLD, overworldPortalPos, seed, 0, 0);
   }

   public PortalLinksData.Link getOrCreate(BlockPos overworldPortalPos, long seed, int mazeHalfWidth, int mazeHalfHeight) {
      return this.getOrCreate(Level.OVERWORLD, overworldPortalPos, seed, mazeHalfWidth, mazeHalfHeight, null);
   }

   public PortalLinksData.Link getOrCreate(ServerLevel sourceLevel, BlockPos sourcePortalPos, long seed) {
      return this.getOrCreate(sourceLevel.dimension(), sourcePortalPos, seed, 0, 0);
   }

   public PortalLinksData.Link getOrCreate(ServerLevel sourceLevel, BlockPos sourcePortalPos, long seed, int mazeHalfWidth, int mazeHalfHeight) {
      return this.getOrCreate(sourceLevel.dimension(), sourcePortalPos, seed, mazeHalfWidth, mazeHalfHeight, null);
   }

   public PortalLinksData.Link getOrCreate(
      ServerLevel sourceLevel,
      BlockPos sourcePortalPos,
      long seed,
      int mazeHalfWidth,
      int mazeHalfHeight,
      PortalLinksData.AreaOccupationTest areaOccupationTest
   ) {
      return this.getOrCreate(sourceLevel.dimension(), sourcePortalPos, seed, mazeHalfWidth, mazeHalfHeight, areaOccupationTest);
   }

   public synchronized PortalLinksData.Link getOrCreate(
      ResourceKey<Level> sourceDimension, BlockPos sourcePortalPos, long seed, int mazeHalfWidth, int mazeHalfHeight
   ) {
      return this.getOrCreate(sourceDimension, sourcePortalPos, seed, mazeHalfWidth, mazeHalfHeight, null);
   }

   public synchronized PortalLinksData.Link getOrCreate(
      ResourceKey<Level> sourceDimension,
      BlockPos sourcePortalPos,
      long seed,
      int mazeHalfWidth,
      int mazeHalfHeight,
      PortalLinksData.AreaOccupationTest areaOccupationTest
   ) {
      PortalLinksData.PortalKey portalKey = new PortalLinksData.PortalKey(sourceDimension, sourcePortalPos);
      PortalLinksData.Link link = this.sourcePortalToLink.get(portalKey);
      int requestedHalfWidth = normalizeHalfExtent(mazeHalfWidth);
      int requestedHalfHeight = normalizeHalfExtent(mazeHalfHeight);
      if (link != null) {
         if (this.expandReservation(portalKey, link, requestedHalfWidth, requestedHalfHeight, areaOccupationTest)) {
            this.setDirty();
         }

         return link;
      } else {
         int baseCx = sourcePortalPos.getX() >> 4;
         int baseCz = sourcePortalPos.getZ() >> 4;
         int[] allocated = this.allocateCenter(baseCx, baseCz, seed, portalKey, requestedHalfWidth, requestedHalfHeight, areaOccupationTest);
         int cx = allocated[0];
         int cz = allocated[1];
         PortalLinksData.Link created = new PortalLinksData.Link(sourceDimension, sourcePortalPos, cx, cz, requestedHalfWidth, requestedHalfHeight);
         this.sourcePortalToLink.put(portalKey, created);
         this.reserveChunks(portalKey, created);
         this.setDirty();
         return created;
      }
   }

   public void updateOwExit(BlockPos portalPos, BlockPos sourceExit, long now) {
      this.updateSourceExit(Level.OVERWORLD, portalPos, sourceExit, now);
   }

   public void updateSourceExit(ServerLevel sourceLevel, BlockPos sourcePortalPos, BlockPos sourceExit, long now) {
      this.updateSourceExit(sourceLevel.dimension(), sourcePortalPos, sourceExit, now);
   }

   public void updateSourceExit(ResourceKey<Level> sourceDimension, BlockPos sourcePortalPos, BlockPos sourceExit, long now) {
      PortalLinksData.Link link = this.getByPortal(sourceDimension, sourcePortalPos);
      if (link != null) {
         link.sourceExit = new BlockPos(sourceExit);
         link.lastUsed = now;
         this.setDirty();
      }
   }

   public void updateOlExit(BlockPos portalPos, BlockPos olExit, long now) {
      this.updateOlExit(Level.OVERWORLD, portalPos, olExit, now);
   }

   public void updateOlExit(ServerLevel sourceLevel, BlockPos sourcePortalPos, BlockPos olExit, long now) {
      this.updateOlExit(sourceLevel.dimension(), sourcePortalPos, olExit, now);
   }

   public void updateOlExit(ResourceKey<Level> sourceDimension, BlockPos sourcePortalPos, BlockPos olExit, long now) {
      PortalLinksData.Link link = this.getByPortal(sourceDimension, sourcePortalPos);
      if (link != null) {
         link.olExit = new BlockPos(olExit);
         link.lastUsed = now;
         this.setDirty();
      }
   }

   public void touch(BlockPos portalPos, long now) {
      this.touch(Level.OVERWORLD, portalPos, now);
   }

   public void touch(ServerLevel sourceLevel, BlockPos sourcePortalPos, long now) {
      this.touch(sourceLevel.dimension(), sourcePortalPos, now);
   }

   public void touch(ResourceKey<Level> sourceDimension, BlockPos sourcePortalPos, long now) {
      PortalLinksData.Link link = this.getByPortal(sourceDimension, sourcePortalPos);
      if (link != null) {
         link.lastUsed = now;
         this.setDirty();
      }
   }

   public static PortalLinksData load(CompoundTag tag, Provider provider) {
      PortalLinksData data = new PortalLinksData();
      data.load(tag);
      return data;
   }

   private void load(CompoundTag tag) {
      this.sourcePortalToLink.clear();
      this.olChunkToPortal.clear();
      if (tag.contains("links", 9)) {
         ListTag list = tag.getList("links", 10);

         for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            ResourceKey<Level> sourceDimension = readDimension(e, "owd");
            BlockPos sourcePortal = new BlockPos(e.getInt("owx"), e.getInt("owy"), e.getInt("owz"));
            int olCx = e.getInt("olcx");
            int olCz = e.getInt("olcz");
            int mazeHalfWidth = normalizeHalfExtent(e.getInt("mhw"));
            int mazeHalfHeight = normalizeHalfExtent(e.getInt("mhh"));
            PortalLinksData.PortalKey portalKey = new PortalLinksData.PortalKey(sourceDimension, sourcePortal);
            PortalLinksData.Link link = new PortalLinksData.Link(sourceDimension, sourcePortal, olCx, olCz, mazeHalfWidth, mazeHalfHeight);
            if (e.getBoolean("hasOwExit")) {
               link.sourceExit = new BlockPos(e.getInt("owex"), e.getInt("owey"), e.getInt("owez"));
            }

            if (e.getBoolean("hasOlExit")) {
               link.olExit = new BlockPos(e.getInt("olex"), e.getInt("oley"), e.getInt("olez"));
            }

            link.lastUsed = e.getLong("lu");
            link.mazeBuilt = e.getBoolean("mb");
            this.sourcePortalToLink.put(portalKey, link);
            this.reserveChunks(portalKey, link);
         }
      }
   }

   public CompoundTag save(CompoundTag tag, Provider provider) {
      ListTag list = new ListTag();

      for (PortalLinksData.Link link : this.sourcePortalToLink.values()) {
         CompoundTag e = new CompoundTag();
         e.putString("owd", link.sourceDimension.location().toString());
         e.putInt("owx", link.portalPos.getX());
         e.putInt("owy", link.portalPos.getY());
         e.putInt("owz", link.portalPos.getZ());
         e.putInt("olcx", link.olChunkX);
         e.putInt("olcz", link.olChunkZ);
         e.putInt("mhw", link.mazeHalfWidth);
         e.putInt("mhh", link.mazeHalfHeight);
         e.putLong("lu", link.lastUsed);
         e.putBoolean("mb", link.mazeBuilt);
         e.putBoolean("hasOwExit", link.sourceExit != null);
         if (link.sourceExit != null) {
            e.putInt("owex", link.sourceExit.getX());
            e.putInt("owey", link.sourceExit.getY());
            e.putInt("owez", link.sourceExit.getZ());
         }

         e.putBoolean("hasOlExit", link.olExit != null);
         if (link.olExit != null) {
            e.putInt("olex", link.olExit.getX());
            e.putInt("oley", link.olExit.getY());
            e.putInt("olez", link.olExit.getZ());
         }

         list.add(e);
      }

      tag.put("links", list);
      return tag;
   }

   private static ResourceKey<Level> readDimension(CompoundTag tag, String key) {
      if (!tag.contains(key, 8)) {
         return Level.OVERWORLD;
      } else {
         ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString(key));
         return dimensionId == null ? Level.OVERWORLD : ResourceKey.create(Registries.DIMENSION, dimensionId);
      }
   }

   private static long mix64(long z) {
      z = (z ^ z >>> 33) * -49064778989728563L;
      z = (z ^ z >>> 33) * -4265267296055464877L;
      return z ^ z >>> 33;
   }

   private static int normalizeHalfExtent(int halfExtent) {
      return Math.max(0, halfExtent);
   }

   private boolean expandReservation(
      PortalLinksData.PortalKey portalKey,
      PortalLinksData.Link link,
      int requestedHalfWidth,
      int requestedHalfHeight,
      @Nullable PortalLinksData.AreaOccupationTest areaOccupationTest
   ) {
      int nextHalfWidth = Math.max(link.mazeHalfWidth, requestedHalfWidth);
      int nextHalfHeight = Math.max(link.mazeHalfHeight, requestedHalfHeight);
      if (nextHalfWidth == link.mazeHalfWidth && nextHalfHeight == link.mazeHalfHeight) {
         return false;
      } else if (!this.isCandidateAreaFree(link.olChunkX, link.olChunkZ, nextHalfWidth, nextHalfHeight, portalKey, areaOccupationTest)) {
         return false;
      } else {
         link.mazeHalfWidth = nextHalfWidth;
         link.mazeHalfHeight = nextHalfHeight;
         this.reserveChunks(portalKey, link);
         return true;
      }
   }

   private int[] allocateCenter(
      int baseCx,
      int baseCz,
      long seed,
      PortalLinksData.PortalKey portalKey,
      int mazeHalfWidth,
      int mazeHalfHeight,
      @Nullable PortalLinksData.AreaOccupationTest areaOccupationTest
   ) {
      if (this.isCandidateAreaFree(baseCx, baseCz, mazeHalfWidth, mazeHalfHeight, portalKey, areaOccupationTest)) {
         return new int[]{baseCx, baseCz};
      } else {
         long probeSeed = mix64(seed ^ portalKey.hashCode());

         for (int tries = 0; tries < 2048; tries++) {
            probeSeed = mix64(probeSeed + -7046029254386353131L);
            int dx = (int)(probeSeed & 1023L) - 512;
            int dz = (int)(probeSeed >>> 10 & 1023L) - 512;
            int cx = baseCx + dx;
            int cz = baseCz + dz;
            if (this.isCandidateAreaFree(cx, cz, mazeHalfWidth, mazeHalfHeight, portalKey, areaOccupationTest)) {
               return new int[]{cx, cz};
            }
         }

         for (int radius = 513; radius < 536870911; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
               int cx = baseCx + dx;
               int northCz = baseCz - radius;
               int southCz = baseCz + radius;
               if (this.isCandidateAreaFree(cx, northCz, mazeHalfWidth, mazeHalfHeight, portalKey, areaOccupationTest)) {
                  return new int[]{cx, northCz};
               }

               if (this.isCandidateAreaFree(cx, southCz, mazeHalfWidth, mazeHalfHeight, portalKey, areaOccupationTest)) {
                  return new int[]{cx, southCz};
               }
            }

            for (int dz = -radius + 1; dz <= radius - 1; dz++) {
               int cz = baseCz + dz;
               int westCx = baseCx - radius;
               int eastCx = baseCx + radius;
               if (this.isCandidateAreaFree(westCx, cz, mazeHalfWidth, mazeHalfHeight, portalKey, areaOccupationTest)) {
                  return new int[]{westCx, cz};
               }

               if (this.isCandidateAreaFree(eastCx, cz, mazeHalfWidth, mazeHalfHeight, portalKey, areaOccupationTest)) {
                  return new int[]{eastCx, cz};
               }
            }
         }

         throw new IllegalStateException("Unable to allocate a free Outer Lands maze area");
      }
   }

   private boolean isCandidateAreaFree(
      int centerChunkX,
      int centerChunkZ,
      int mazeHalfWidth,
      int mazeHalfHeight,
      PortalLinksData.PortalKey portalKey,
      @Nullable PortalLinksData.AreaOccupationTest areaOccupationTest
   ) {
      return !this.isAreaAvailable(centerChunkX, centerChunkZ, mazeHalfWidth, mazeHalfHeight, portalKey)
         ? false
         : areaOccupationTest == null || !areaOccupationTest.isOccupied(centerChunkX, centerChunkZ, mazeHalfWidth, mazeHalfHeight);
   }

   private boolean isAreaAvailable(int centerChunkX, int centerChunkZ, int mazeHalfWidth, int mazeHalfHeight, PortalLinksData.PortalKey portalKey) {
      for (int cx = centerChunkX - mazeHalfWidth; cx <= centerChunkX + mazeHalfWidth; cx++) {
         for (int cz = centerChunkZ - mazeHalfHeight; cz <= centerChunkZ + mazeHalfHeight; cz++) {
            PortalLinksData.PortalKey existing = (PortalLinksData.PortalKey)this.olChunkToPortal.get(ChunkPos.asLong(cx, cz));
            if (existing != null && !existing.equals(portalKey)) {
               return false;
            }
         }
      }

      return true;
   }

   private void reserveChunks(PortalLinksData.PortalKey portalKey, PortalLinksData.Link link) {
      for (int cx = link.olChunkX - link.mazeHalfWidth; cx <= link.olChunkX + link.mazeHalfWidth; cx++) {
         for (int cz = link.olChunkZ - link.mazeHalfHeight; cz <= link.olChunkZ + link.mazeHalfHeight; cz++) {
            this.olChunkToPortal.put(ChunkPos.asLong(cx, cz), portalKey);
         }
      }
   }

   @FunctionalInterface
   public interface AreaOccupationTest {
      boolean isOccupied(int var1, int var2, int var3, int var4);
   }

   public static final class Link {
      public final ResourceKey<Level> sourceDimension;
      public final BlockPos portalPos;
      public final int olChunkX;
      public final int olChunkZ;
      public int mazeHalfWidth;
      public int mazeHalfHeight;
      public BlockPos sourceExit;
      public BlockPos olExit;
      public long lastUsed;
      public boolean mazeBuilt;

      private Link(ResourceKey<Level> sourceDimension, BlockPos portalPos, int olChunkX, int olChunkZ, int mazeHalfWidth, int mazeHalfHeight) {
         this.sourceDimension = sourceDimension;
         this.portalPos = new BlockPos(portalPos);
         this.olChunkX = olChunkX;
         this.olChunkZ = olChunkZ;
         this.mazeHalfWidth = mazeHalfWidth;
         this.mazeHalfHeight = mazeHalfHeight;
      }
   }

   private static final class PortalKey {
      private final ResourceKey<Level> sourceDimension;
      private final BlockPos portalPos;

      private PortalKey(ResourceKey<Level> sourceDimension, BlockPos portalPos) {
         this.sourceDimension = sourceDimension;
         this.portalPos = new BlockPos(portalPos);
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else {
            return !(obj instanceof PortalLinksData.PortalKey other)
               ? false
               : this.sourceDimension.equals(other.sourceDimension) && this.portalPos.equals(other.portalPos);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.sourceDimension, this.portalPos);
      }
   }
}
