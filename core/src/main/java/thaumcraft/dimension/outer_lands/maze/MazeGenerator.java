package thaumcraft.dimension.outer_lands.maze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Генератор сетки лабиринта — дословный порт
 * {@code thaumcraft.common.lib.world.dim.MazeGenerator} (1.7.10).
 *
 * <p>Алгоритм: случайный бэктрекер по клеткам с запрещёнными «островами»,
 * затем гарантированное соединение двух стартовых 2×2-зон (центральной с
 * порталом и угловой с алтарём босса) и расстановка фич по тупикам.
 *
 * <p>Биты: N=1, S=2, E=4, W=8, A=16, B=32; стартовые зоны помечаются
 * 512/768/1024/1280, центральная — 256, а «соединительная» метка при
 * достраивании пути равна 25344 (0x6300).
 */
public class MazeGenerator {
   public static final int N = 1;
   public static final int S = 2;
   public static final int E = 4;
   public static final int W = 8;
   public static final int A = 16;
   public static final int B = 32;

   int width = 0;
   int height = 0;
   long seed = 0L;
   Random rand = null;
   public int[][] grid;

   public MazeGenerator(int width, int height, long seed) {
      this.width = width;
      this.height = height;
      this.seed = seed;
      this.rand = new Random(seed);
      this.grid = new int[height][width];

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            this.grid[y][x] = 0;
         }
      }
   }

   public static int getOPP(int in) {
      return switch (in) {
         case 1 -> 2;
         case 2 -> 1;
         case 4 -> 8;
         case 8 -> 4;
         default -> -99;
      };
   }

   public static int getDX(int in) {
      return switch (in) {
         case 1, 2 -> 0;
         case 4 -> 1;
         case 8 -> -1;
         default -> -99;
      };
   }

   public static int getDY(int in) {
      return switch (in) {
         case 1 -> -1;
         case 2 -> 1;
         case 4, 8 -> 0;
         default -> -99;
      };
   }

   public boolean generate() {
      int bx = 0;
      int by = 0;
      switch (this.rand.nextInt(4)) {
         case 0 -> {
            bx = 0;
            by = 0;
         }
         case 1 -> {
            bx = this.width - 2;
            by = this.height - 2;
         }
         case 2 -> {
            bx = this.width - 2;
            by = 0;
         }
         case 3 -> {
            bx = 0;
            by = this.height - 2;
         }
         default -> {
         }
      }

      // Угловая 2x2 зона босса и центральная ячейка входа.
      this.grid[by][bx] = 512;
      this.grid[by][bx + 1] = 768;
      this.grid[by + 1][bx] = 1024;
      this.grid[by + 1][bx + 1] = 1280;
      int px = 1 + this.width / 2;
      int py = 1 + this.height / 2;
      this.grid[py][px] = 256;
      ArrayList<MazeGenerator.Loc> cells = new ArrayList<>();
      int l = (this.width + this.height) / 4;

      // Раскидываем запрещённые «острова» — они станут непроходимыми блоками.
      for (int z = 0; z < l; z++) {
         int w = 1 + this.rand.nextInt(3);
         if (w > 2) {
            l--;
         }

         int qq = this.rand.nextInt(this.width - w);
         int ww = this.rand.nextInt(this.height - w);

         for (int a = qq; a < qq + w; a++) {
            for (int b = ww; b < ww + w; b++) {
               if (this.grid[b][a] == 0) {
                  this.grid[b][a] = -1;
               }
            }
         }
      }

      List<Integer> directions = new ArrayList<>(List.of(1, 2, 4, 8));
      Collections.shuffle(directions, this.rand);
      int xx = px + getDX(directions.get(0));
      int yy = py + getDY(directions.get(0));
      this.grid[py][px] = this.grid[py][px] | directions.get(0);
      if (this.grid[yy][xx] < 0) {
         this.grid[yy][xx] = 0;
      }

      this.grid[yy][xx] = this.grid[yy][xx] | getOPP(directions.get(0));
      cells.add(new MazeGenerator.Loc(xx, yy));
      boolean success = false;

      // Основной проход случайного бэктрекера.
      while (!cells.isEmpty()) {
         int index = this.getNextIndex(cells.size());
         int x = cells.get(index).x;
         int y = cells.get(index).y;
         Collections.shuffle(directions, this.rand);
         boolean carved = false;

         for (int dir : directions) {
            int nx = x + getDX(dir);
            int ny = y + getDY(dir);
            if (0 < nx && nx < this.width - 1 && 0 < ny && ny < this.height - 1) {
               if (this.grid[ny][nx] == 0) {
                  this.grid[y][x] = this.grid[y][x] | dir;
                  this.grid[ny][nx] = this.grid[ny][nx] | getOPP(dir);
                  cells.add(new MazeGenerator.Loc(nx, ny));
                  carved = true;
               }

               if (carved) {
                  success = true;
                  break;
               }
            }
         }

         if (!carved) {
            cells.remove(index);
         }
      }

      if (!success) {
         return false;
      }

      // Чистим метки островов — они остаются непроходимыми стенами.
      for (int aa = 0; aa < this.height; aa++) {
         for (int bb = 0; bb < this.width; bb++) {
            if (this.grid[aa][bb] < 0) {
               this.grid[aa][bb] = 0;
            }
         }
      }

      // Случайные дополнительные выходы из центральной ячейки.
      Collections.shuffle(directions, this.rand);

      for (int dirx : directions) {
         int nx = px + getDX(dirx);
         int ny = py + getDY(dirx);
         if (0 < nx && nx < this.width - 1 && 0 < ny && ny < this.height - 1
            && this.grid[ny][nx] > 0 && this.rand.nextBoolean()) {
            this.grid[ny][nx] = this.grid[ny][nx] | getOPP(dirx);
            this.grid[py][px] = this.grid[py][px] | dirx;
         }
      }

      Collections.shuffle(directions, this.rand);
      boolean connected = false;

      // Первая попытка: соединяем угловую 2x2 зону с уже проложенным проходом.
      label336:
      for (int ax = 0; ax < 2; ax++) {
         for (int ay = 0; ay < 2; ay++) {
            for (int dirxx : directions) {
               int nx = bx + ax + getDX(dirxx);
               int ny = by + ay + getDY(dirxx);
               if (0 < nx && nx < this.width - 1 && 0 < ny && ny < this.height - 1
                  && this.grid[ny][nx] > 0 && (new Cell((short)this.grid[ny][nx])).feature == 0) {
                  this.grid[ny][nx] = this.grid[ny][nx] | getOPP(dirxx);
                  this.grid[by + ay][bx + ax] = this.grid[by + ay][bx + ax] | dirxx;
                  connected = true;
                  break label336;
               }
            }
         }
      }

      if (!connected) {
         // Вторая попытка: достраиваем путь от угловой зоны до любой
         // существующей клетки, помечая новые клетки признаком 25344.
         List<Integer> directions2 = new ArrayList<>(List.of(1, 2, 4, 8));
         Collections.shuffle(directions2, this.rand);
         success = false;

         label312:
         for (int ax = 0; ax < 2; ax++) {
            for (int ay = 0; ay < 2; ay++) {
               for (int dir2 : directions2) {
                  int qx = bx + ax + getDX(dir2);
                  int qy = by + ay + getDY(dir2);
                  if (0 < qx && qx < this.width - 1 && 0 < qy && qy < this.height - 1
                     && this.grid[qy][qx] == 0) {
                     cells.add(new MazeGenerator.Loc(qx, qy));

                     while (!cells.isEmpty()) {
                        int index = this.getNextIndex(cells.size());
                        int x = cells.get(index).x;
                        int y = cells.get(index).y;
                        Collections.shuffle(directions, this.rand);
                        boolean carved = false;

                        for (int dirxxx : directions) {
                           int nx = x + getDX(dirxxx);
                           int ny = y + getDY(dirxxx);
                           if (0 < nx && nx < this.width - 1 && 0 < ny && ny < this.height - 1) {
                              if (this.grid[ny][nx] == 0) {
                                 this.grid[y][x] = this.grid[y][x] | dirxxx;
                                 this.grid[y][x] = this.grid[y][x] | 25344;
                                 this.grid[ny][nx] = this.grid[ny][nx] | getOPP(dirxxx);
                                 this.grid[ny][nx] = this.grid[ny][nx] | 25344;
                                 cells.add(new MazeGenerator.Loc(nx, ny));
                                 carved = true;
                              } else if ((new Cell((short)this.grid[ny][nx])).feature == 0) {
                                 this.grid[y][x] = this.grid[y][x] | dirxxx;
                                 this.grid[ny][nx] = this.grid[ny][nx] | getOPP(dirxxx);
                                 this.grid[qy][qx] = this.grid[qy][qx] | getOPP(dir2);
                                 this.grid[by + ay][bx + ax] = this.grid[by + ay][bx + ax] | dir2;
                                 success = true;
                                 break label312;
                              }

                              if (carved) {
                                 break;
                              }
                           }
                        }

                        if (!carved) {
                           cells.remove(index);
                        }
                     }
                  }
               }
            }
         }

         if (!success) {
            return false;
         }
      }

      // Снимаем временные метки «пути дострайки» (feature == 99 → 0).
      for (int aa = 0; aa < this.height; aa++) {
         for (int bbx = 0; bbx < this.width; bbx++) {
            Cell c = new Cell((short)this.grid[aa][bbx]);
            if (c.feature == 99) {
               c.feature = 0;
               this.grid[aa][bbx] = c.pack();
            }
         }
      }

      // Тупики: один ключевой зал (6), часть — гнёзда/библиотеки (7..9).
      ArrayList<CellLoc> deadEndsloc = new ArrayList<>();

      for (int aa = 0; aa < this.height; aa++) {
         for (int bbxx = 0; bbxx < this.width; bbxx++) {
            Cell c = new Cell((short)this.grid[aa][bbxx]);
            int exits = (c.north ? 1 : 0) + (c.south ? 1 : 0) + (c.east ? 1 : 0) + (c.west ? 1 : 0);
            if (exits == 1 && c.feature == 0) {
               deadEndsloc.add(new CellLoc(aa, bbxx));
            }
         }
      }

      if (deadEndsloc.size() == 0) {
         return false;
      }

      int r = this.rand.nextInt(deadEndsloc.size());
      CellLoc ll = deadEndsloc.get(r);
      Cell c = new Cell((short)this.grid[ll.x][ll.z]);
      c.feature = 6;
      this.grid[ll.x][ll.z] = c.pack();
      deadEndsloc.remove(r);
      if (deadEndsloc.size() > 0) {
         r = 0;

         while (r < deadEndsloc.size() / 2) {
            int rx = this.rand.nextInt(deadEndsloc.size());
            CellLoc llx = deadEndsloc.get(rx);
            Cell cx = new Cell((short)this.grid[llx.x][llx.z]);
            if (cx.feature == 0) {
               cx.feature = (byte)(7 + this.rand.nextInt(3));
               this.grid[llx.x][llx.z] = cx.pack();
               deadEndsloc.remove(rx);
               r++;
            }
         }
      }

      // Редкие комнаты по всему лабиринту.
      for (int aa = 0; aa < this.height; aa++) {
         for (int bbxxx = 0; bbxxx < this.width; bbxxx++) {
            c = new Cell((short)this.grid[aa][bbxxx]);
            // В TC4 здесь nextInt(25), а паучья комната (фича 14) выпадает ещё и с
      // шансом 1/8 внутри — суммарно 1/200 на подходящую ячейку, и пауков в
      // лабиринте почти не встретить. По просьбе игрока особые проходы стали
      // вдвое чаще, а паучьих среди них вдвое больше (см. switch ниже).
      // Это отступление от оригинала, а не паритет.
      if (c.feature == 0 && (c.north || c.south || c.west || c.east) && this.rand.nextInt(12) == 0) {
               switch (this.rand.nextInt(14)) {
                  case 0 -> c.feature = 8;
                  case 1 -> c.feature = 10;
                  case 2, 3 -> c.feature = 11;
                  case 4, 5 -> c.feature = 12;
                  case 6 -> c.feature = 13;
                  case 7, 8, 9, 10, 11, 12, 13 -> c.feature = 14;
                  default -> {
                  }
               }

               this.grid[aa][bbxxx] = c.pack();
            }
         }
      }

      return true;
   }

   private int getNextIndex(int ceil) {
      float r = this.rand.nextFloat();
      if (r <= 0.45F) {
         return ceil - 1;
      }

      return r <= 0.9F ? this.rand.nextInt(ceil) : 0;
   }

   private static class Loc {
      int x;
      int y;

      Loc(int x, int y) {
         this.x = x;
         this.y = y;
      }
   }
}