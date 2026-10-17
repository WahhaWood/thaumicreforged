package thaumcraft.dimension.outer_lands.maze;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Глобальное хранилище лабиринтов — порт {@code MazeHandler} (1.7.10), но
 * с сохранением в {@code labyrinth.dat} средствами 1.21.
 *
 * <p>Собственно лабиринт не привязан к сиду мира: он генерируется по запросу
 * (алтарь эльдричей / каменное кольцо) и хранится глобально, поэтому
 * одинаковые участки всегда строятся одинаково и переживают перезапуск.
 */
public final class MazeHandler {
   private static final ConcurrentHashMap<CellLoc, Short> LABYRINTH = new ConcurrentHashMap<>();
   /**
    * Ячейки, чьи блоки уже построены. Без этого любая достройка после рестарта
    * (или повторный вход) дублировала бы стражей, предметы на постаментах и
    * урны: генераторы добавляют сущности каждый раз, когда их вызывают.
    */
   private static final java.util.Set<CellLoc> BUILT = ConcurrentHashMap.newKeySet();

   private MazeHandler() {
   }

   public static synchronized void putToHashMap(CellLoc key, Cell cell) {
      LABYRINTH.put(key, cell.pack());
   }

   public static synchronized void putToHashMapRaw(CellLoc key, short cell) {
      LABYRINTH.put(key, cell);
   }

   public static synchronized Cell getFromHashMap(CellLoc key) {
      Short v = LABYRINTH.get(key);
      return v == null ? null : new Cell(v);
   }

   public static synchronized void removeFromHashMap(CellLoc key) {
      LABYRINTH.remove(key);
   }

   public static synchronized short getFromHashMapRaw(CellLoc key) {
      Short v = LABYRINTH.get(key);
      return v == null ? 0 : v;
   }

   public static synchronized void clearHashMap() {
      LABYRINTH.clear();
      BUILT.clear();
      BUILD_QUEUE.clear();
      buildLevel = null;
   }

   public static synchronized int size() {
      return LABYRINTH.size();
   }

   /** Снимок всех непустых ячеек — для статистики и проверок в тестах. */
   public static synchronized java.util.Collection<Cell> iterate() {
      java.util.List<Cell> out = new ArrayList<>(LABYRINTH.size());
      for (Short v : LABYRINTH.values()) {
         out.add(new Cell(v));
      }

      return out;
   }

   /** Есть ли лабиринт в радиусе w×h чанков вокруг точки — проверка «алтарь готов». */
   public static boolean mazesInRange(int chunkX, int chunkZ, int w, int h) {
      for (int x = -w; x <= w; x++) {
         for (int z = -h; z <= h; z++) {
            if (getFromHashMap(new CellLoc(chunkX + x, chunkZ + z)) != null) {
               return true;
            }
         }
      }

      return false;
   }

   private static void readNBT(CompoundTag nbt) {
      ListTag tagList = nbt.getList("cells", Tag.TAG_COMPOUND);

      for (int a = 0; a < tagList.size(); a++) {
         CompoundTag cell = tagList.getCompound(a);
         putToHashMapRaw(new CellLoc(cell.getInt("x"), cell.getInt("z")), (short)cell.getInt("cell"));
      }

      ListTag builtList = nbt.getList("built", Tag.TAG_COMPOUND);

      for (int a = 0; a < builtList.size(); a++) {
         CompoundTag cell = builtList.getCompound(a);
         BUILT.add(new CellLoc(cell.getInt("x"), cell.getInt("z")));
      }
   }

   private static CompoundTag writeNBT() {
      CompoundTag nbt = new CompoundTag();
      ListTag tagList = new ListTag();

      for (CellLoc loc : LABYRINTH.keySet()) {
         short v = getFromHashMapRaw(loc);
         if (v > 0) {
            CompoundTag cell = new CompoundTag();
            cell.putInt("x", loc.x);
            cell.putInt("z", loc.z);
            cell.putInt("cell", v);
            tagList.add(cell);
         }
      }

      nbt.put("cells", tagList);
      ListTag builtList = new ListTag();

      for (CellLoc loc : BUILT) {
         CompoundTag cell = new CompoundTag();
         cell.putInt("x", loc.x);
         cell.putInt("z", loc.z);
         builtList.add(cell);
      }

      nbt.put("built", builtList);
      return nbt;
   }

   private static Path mazeFile(ServerLevel level) {
      return level.getServer().getWorldPath(LevelResource.ROOT).resolve("labyrinth.dat");
   }

   public static void loadMaze(ServerLevel level) {
      clearHashMap();
      Path main = mazeFile(level);
      List<Path> candidates = new ArrayList<>();
      candidates.add(main);
      candidates.add(main.resolveSibling("labyrinth.dat_old"));

      for (Path p : candidates) {
         if (!Files.exists(p)) {
            continue;
         }

         try {
            CompoundTag root = net.minecraft.nbt.NbtIo.readCompressed(
               p, net.minecraft.nbt.NbtAccounter.unlimitedHeap()
            );
            readNBT(root.getCompound("Data"));
            return;
         } catch (Exception e) {
            // Повреждённый файл не должен ломать загрузку мира: пробуем следующий.
            e.printStackTrace();
         }
      }
   }

   /** Атомарная запись через .dat_new → .dat (как в оригинале). */
   public static void saveMaze(ServerLevel level) {
      CompoundTag root = new CompoundTag();
      root.put("Data", writeNBT());

      Path main = mazeFile(level);
      Path tmp = main.resolveSibling("labyrinth.dat_new");
      Path old = main.resolveSibling("labyrinth.dat_old");
      try {
         Files.deleteIfExists(tmp);
         try (var out = Files.newOutputStream(tmp)) {
            net.minecraft.nbt.NbtIo.writeCompressed(root, out);
         }

         Files.deleteIfExists(old);
         if (Files.exists(main)) {
            Files.move(main, old, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
         }

         Files.move(tmp, main, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   /**
    * Диспетчер ячейки лабиринта — порт {@code MazeHandler.generateEldritch}.
    *
    * <p>Высота зала в оригинале зашита константой 50. Раскладка по feature:
    * 1 — портал, 2..5 — углы боссовой комнаты, 6 — комната ключа, 7 — гнездо,
    * 8 — библиотека, всё остальное (9..14 и по умолчанию) — обычный проход.
    * После генерации в любом случае вызывается {@code processDecorations}.
    *
    * @param cx координата чанка по X
    * @param cz координата чанка по Z
    */
   /**
    * Ячейка входа (feature 1) ИМЕННО ЭТОГО лабиринта — комната с порталом.
    *
    * <p>Искать по всей карте нельзя: в ней живут лабиринты всех порталов, и
    * легко вернуть чужую комнату входа — тогда точка прибытия уедет к другому
    * лабиринту. Поэтому обходим только сетку построенного лабиринта.
    */
   public static synchronized CellLoc findPortalCell(int cx, int cz, int mazeW, int mazeH) {
      int col = cx - (1 + mazeW / 2);
      int row = cz - (1 + mazeH / 2);
      for (int a = 0; a < mazeW; a++) {
         for (int b = 0; b < mazeH; b++) {
            CellLoc loc = new CellLoc(col + a, row + b);
            Cell cell = getFromHashMap(loc);
            if (cell != null && cell.feature == 1) {
               return loc;
            }
         }
      }

      return null;
   }

   /**
    * Построить НАСТОЯЩИЙ лабиринт вокруг чанка — пайплайн TC4, а не упрощённый
    * {@code MazeBuilder}.
    *
    * <p>Сначала раскладывается сетка ({@link MazeGenerator} через
    * {@link MazeThread}), затем каждая ячейка окрестности строится своим
    * генератором из {@code Gen*} — с проходами, комнатой входа, ключевой,
    * гнездом, библиотекой и боссовым залом. Раньше вход в портал звал
    * {@code MazeBuilder}, который писал простые коробки: комнаты не были
    * соединены и не походили на оригинальные, хотя все семь генераторов в
    * порте уже были написаны.
    *
    * @return чанковая координата комнаты входа (с порталом) или {@code null}
    */
   /** Очередь достройки: лабиринтов много, а блоки пишутся только в серверном потоке. */
   private static final java.util.ArrayDeque<CellLoc> BUILD_QUEUE = new java.util.ArrayDeque<>();
   private static ServerLevel buildLevel;

   /**
    * Начать постройку лабиринта: комнату входа строим сразу, остальное уходит
    * в очередь и достраивается порциями по тикам ({@link #buildQueued}).
    *
    * <p>Так решаются сразу две беды: вход не стопорит сервер на десятки секунд
    * (раньше всё делалось в одном тике), и лабиринт больше НЕ ОБРЫВАЕТСЯ через
    * семь ячеек — достраивается целиком, включая боссовые залы.
    *
    * @return чанковая координата комнаты входа (с порталом) или {@code null}
    */
   public static synchronized CellLoc buildRealMaze(
      ServerLevel level, int cx, int cz, int mazeW, int mazeH, int patchRadius, long seed
   ) {
      if (getFromHashMap(new CellLoc(cx, cz)) == null) {
         // Раскладке нужен ПОЛНЫЙ размер сетки: на 4x4 генератор выдаёт пусто.
         MazeThread.runNow(cx, cz, mazeW, mazeH, seed);
      }

      // Комната входа и её окрестность — сразу, игрок туда телепортируется.
      for (int dx = -patchRadius; dx <= patchRadius; dx++) {
         for (int dz = -patchRadius; dz <= patchRadius; dz++) {
            CellLoc loc = new CellLoc(cx + dx, cz + dz);
            if (getFromHashMap(loc) != null) {
               generateEldritch(level, level.random, loc.x, loc.z);
            }
         }
      }

      // Остальной лабиринт — в очередь, по тикам. Очередь НЕ очищается:
      // второй портал, открытый пока достраивается первый лабиринт, иначе
      // выкидывал бы чужие недостроенные ячейки навсегда (они нигде не
      // persistились, а флаг mazeBuilt уже стоял).
      buildLevel = level;
      int col = cx - (1 + mazeW / 2);
      int row = cz - (1 + mazeH / 2);
      for (int a = 0; a < mazeW; a++) {
         for (int b = 0; b < mazeH; b++) {
            int x = col + a;
            int z = row + b;
            if (Math.abs(x - cx) <= patchRadius && Math.abs(z - cz) <= patchRadius) {
               continue;
            }

            CellLoc loc = new CellLoc(x, z);
            if (getFromHashMap(loc) != null && !BUILT.contains(loc) && !BUILD_QUEUE.contains(loc)) {
               BUILD_QUEUE.add(loc);
            }
         }
      }

      return findPortalCell(cx, cz, mazeW, mazeH);
   }

   /** Достроить несколько ячеек. Вызывается каждый серверный тик. */
   public static synchronized int buildQueued(int maxCells) {
      if (buildLevel == null || BUILD_QUEUE.isEmpty()) {
         if (buildLevel != null && BUILD_QUEUE.isEmpty()) {
            // Очередь drained: фиксируем раскладку и прогресс на диск, чтобы
            // рестарт не застал лабиринт в полупостроенном состоянии.
            saveMaze(buildLevel);
            buildLevel = null;
         } else {
            buildLevel = null;
         }

         return 0;
      }

      int done = 0;
      while (done < maxCells && !BUILD_QUEUE.isEmpty()) {
         CellLoc loc = BUILD_QUEUE.poll();
         if (getFromHashMap(loc) != null) {
            generateEldritch(buildLevel, buildLevel.random, loc.x, loc.z);
            done++;
         }
      }

      if (BUILD_QUEUE.isEmpty()) {
         saveMaze(buildLevel);
         buildLevel = null;
      }

      return done;
   }

   /**
    * Продолжить недостроенное после рестарта: раскладка и прогресс уже
    * загружены из {@code labyrinth.dat}, осталось поставить в очередь всё,
    * что размечено, но не построено.
    */
   public static synchronized void resumeUnbuilt(ServerLevel level) {
      for (CellLoc loc : LABYRINTH.keySet()) {
         if (getFromHashMapRaw(loc) > 0 && !BUILT.contains(loc) && !BUILD_QUEUE.contains(loc)) {
            BUILD_QUEUE.add(loc);
         }
      }

      if (!BUILD_QUEUE.isEmpty()) {
         buildLevel = level;
      }
   }

   /** Сколько ячеек ещё ждёт постройки (для тестов и отладки). */
   public static synchronized int queuedCount() {
      return BUILD_QUEUE.size();
   }

   public static void generateEldritch(ServerLevel level, RandomSource random, int cx, int cz) {
      Cell cell = getFromHashMap(new CellLoc(cx, cz));
      if (cell == null) {
         return;
      }

      CellLoc loc = new CellLoc(cx, cz);
      if (!BUILT.add(loc)) {
         // Ячейка уже построена (повторный вход, докачка после рестарта,
         // перекрывающиеся лабиринты): генераторы добавляют сущности при
         // каждом вызове, поэтому строить дважды нельзя.
         return;
      }

      int y = 50;
      switch (cell.feature) {
         case 1 -> GenPortal.generatePortal(level, random, cx, cz, y, cell);
         case 2, 3, 4, 5 -> GenBossRoom.generateRoom(level, random, cx, cz, y, cell);
         case 6 -> GenKeyRoom.generateRoom(level, random, cx, cz, y, cell);
         case 7 -> GenNestRoom.generateRoom(level, random, cx, cz, y, cell);
         case 8 -> GenLibraryRoom.generateRoom(level, random, cx, cz, y, cell);
         // В оригинале здесь generatePassage с единственной веткой
         // switch (random.nextInt(1)) — она всегда даёт 0, но лишний вызов
         // Random остаётся, чтобы не сдвинуть последовательность.
         default -> {
            random.nextInt(1);
            GenPassage.generateDefaultPassage(level, random, cx, cz, y, cell);
         }
      }

      GenCommon.processDecorations(level);
   }
}
