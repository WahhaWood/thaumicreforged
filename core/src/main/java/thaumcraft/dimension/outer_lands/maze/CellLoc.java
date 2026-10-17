package thaumcraft.dimension.outer_lands.maze;

/** Позиция ячейки лабиринта в координатах чанков — порт {@code CellLoc} (1.7.10). */
public class CellLoc implements Comparable<CellLoc> {
   public int x;
   public int z;

   public CellLoc() {
   }

   public CellLoc(int x, int z) {
      this.x = x;
      this.z = z;
   }

   public CellLoc(CellLoc c) {
      this.x = c.x;
      this.z = c.z;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof CellLoc other)) {
         return false;
      }

      return this.x == other.x && this.z == other.z;
   }

   @Override
   public int hashCode() {
      // Совпадает с остальным кодом (ChunkPos): 31*x + z
      return 31 * this.x + this.z;
   }

   @Override
   public int compareTo(CellLoc o) {
      if (this.x != o.x) {
         return this.x < o.x ? -1 : 1;
      }

      return this.z == o.z ? 0 : (this.z < o.z ? -1 : 1);
   }

   @Override
   public String toString() {
      return "(" + this.x + "," + this.z + ")";
   }
}