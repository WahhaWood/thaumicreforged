package thaumcraft.dimension.outer_lands.maze;

/**
 * Ячейка лабиринта — порт {@code thaumcraft.common.lib.world.dim.Cell} (1.7.10).
 *
 * <p>Битовая упаковка полностью совпадает с оригиналом, потому что она же
 * пишется в {@code labyrinth.dat}: младшие биты — открытые проходы,
 * бит 4 — «потолок открыт», бит 5 — «пол открыт», биты 8+ — тип фичи.
 */
public class Cell {
   /** Открыт проход на север (сетка ориентации: N = 1). */
   public boolean north;
   /** Открыт проход на юг (S = 2). */
   public boolean south;
   /** Открыт проход на восток (E = 4). */
   public boolean east;
   /** Открыт проход на запад (W = 8). */
   public boolean west;
   /** Открыт верхний проход (бит 4). */
   public boolean above;
   /** Открыт нижний проход (бит 5). */
   public boolean below = false;
   /** Тип содержимого ячейки: 0 — проход, 1 — портал, 6 — ключевая, 7 — гнездо, 8 — библиотека, 2..5 — боссовые залы. */
   public byte feature = 0;

   public Cell() {
   }

   public Cell(short data) {
      this.unpack(data);
   }

   public void unpack(short data) {
      this.north = thaumcraft.util.Utils.getBit(data, 0);
      this.south = thaumcraft.util.Utils.getBit(data, 1);
      this.east = thaumcraft.util.Utils.getBit(data, 2);
      this.west = thaumcraft.util.Utils.getBit(data, 3);
      this.above = thaumcraft.util.Utils.getBit(data, 4);
      this.below = thaumcraft.util.Utils.getBit(data, 5);
      this.feature = (byte)(data >> 8);
   }

   public short pack() {
      int out = 0;
      if (this.north) {
         out = thaumcraft.util.Utils.setBit(out, 0);
      }

      if (this.south) {
         out = thaumcraft.util.Utils.setBit(out, 1);
      }

      if (this.east) {
         out = thaumcraft.util.Utils.setBit(out, 2);
      }

      if (this.west) {
         out = thaumcraft.util.Utils.setBit(out, 3);
      }

      if (this.above) {
         out = thaumcraft.util.Utils.setBit(out, 4);
      }

      if (this.below) {
         out = thaumcraft.util.Utils.setBit(out, 5);
      }

      out |= this.feature << 8;
      return (short)out;
   }

   /** Количество открытых проходов по четырём направлениям (без верхнего/нижнего). */
   public int exits() {
      return (this.north ? 1 : 0) + (this.south ? 1 : 0) + (this.east ? 1 : 0) + (this.west ? 1 : 0);
   }

   @Override
   public String toString() {
      return "Cell[n=" + this.north + ",s=" + this.south + ",e=" + this.east + ",w=" + this.west
         + ",above=" + this.above + ",below=" + this.below + ",feature=" + this.feature + "]";
   }
}