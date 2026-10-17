package thaumcraft.dimension.outer_lands.maze;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Фоновая генерация лабиринта — порт {@code MazeThread} (1.7.10).
 *
 * <p>В оригинале на каждый запрос создавался новый {@code Thread}. Здесь
 * используется один пул на один поток: алтарь опрашивает готовность через
 * {@link MazeHandler#mazesInRange}, так что асинхронность сохранена, но
 * спам потоков (в оригинале он был возможен) исключён.
 */
public final class MazeThread {
   private static final ExecutorService POOL = Executors.newSingleThreadExecutor(new ThreadFactory() {
      private final AtomicInteger counter = new AtomicInteger();

      @Override
      public Thread newThread(Runnable r) {
         Thread t = new Thread(r, "TC-MazeGen-" + this.counter.incrementAndGet());
         t.setDaemon(true);
         return t;
      }
   });

   private MazeThread() {
   }

   public static void schedule(int x, int z, int w, int h, long seed) {
      POOL.execute(() -> run(x, z, w, h, seed));
   }

   /** Синхронный вариант для тестов и вызовов, где фон не нужен. */
   public static void runNow(int x, int z, int w, int h, long seed) {
      run(x, z, w, h, seed);
   }

   private static void run(int x, int z, int w, int h, long seedBase) {
      // Резервируем 2x2 угловую зону босса и центральную ячейку входа.
      MazeHandler.putToHashMapRaw(new CellLoc(x, z), (short)0);
      MazeHandler.putToHashMapRaw(new CellLoc(x - w, z - h), (short)0);
      MazeHandler.putToHashMapRaw(new CellLoc(x + w, z + h), (short)0);
      MazeHandler.putToHashMapRaw(new CellLoc(x - w, z + h), (short)0);
      MazeHandler.putToHashMapRaw(new CellLoc(x + w, z - h), (short)0);

      long seed = seedBase;
      MazeGenerator gen = new MazeGenerator(w, h, seed);

      // Генератор может отвергнуть сетку (например, не вышло соединить
      // угол с проходом) — тогда берём следующий сид, как в оригинале.
      while (!gen.generate()) {
         seed++;
         gen = new MazeGenerator(w, h, seed);
      }

      int col = x - (1 + w / 2);
      int row = z - (1 + h / 2);

      for (int a = 0; a < w; a++) {
         for (int b = 0; b < h; b++) {
            if (gen.grid[b][a] > 0) {
               MazeHandler.putToHashMapRaw(new CellLoc(a + col, b + row), (short)gen.grid[b][a]);
            }
         }
      }

      // Пустые ячейки в границах не должны висеть в карте.
      int[][] markers = {{x, z}, {x - w, z - h}, {x + w, z + h}, {x - w, z + h}, {x + w, z - h}};
      List<CellLoc> keys = new ArrayList<>();
      for (int[] m : markers) {
         keys.add(new CellLoc(m[0], m[1]));
      }

      for (CellLoc key : keys) {
         if (MazeHandler.getFromHashMapRaw(key) == 0) {
            MazeHandler.removeFromHashMap(key);
         }
      }
   }
}