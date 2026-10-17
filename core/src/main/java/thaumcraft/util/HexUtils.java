package thaumcraft.util;

import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.util.RandomSource;

public class HexUtils {
   public static final int[][] NEIGHBOURS = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};

   public static int getDistance(HexUtils.Hex hex, HexUtils.Hex hex2) {
      return (Math.abs(hex.q - hex2.q) + Math.abs(hex.r - hex2.r) + Math.abs(hex.q + hex.r - hex2.q - hex2.r)) / 2;
   }

   public static HexUtils.Hex getRoundedHex(double n, double n2) {
      return getRoundedCubicHex(n, n2, -n - n2).toHex();
   }

   public static HexUtils.CubicHex getRoundedCubicHex(double n, double n2, double n3) {
      int n4 = (int)Math.round(n);
      int n5 = (int)Math.round(n2);
      int n6 = (int)Math.round(n3);
      double abs = Math.abs(n4 - n);
      double abs2 = Math.abs(n5 - n2);
      double abs3 = Math.abs(n6 - n3);
      if (abs > abs2 && abs > abs3) {
         n4 = -n5 - n6;
      } else if (abs2 > abs3) {
         n5 = -n4 - n6;
      } else {
         n6 = -n4 - n5;
      }

      return new HexUtils.CubicHex(n4, n5, n6);
   }

   public static ArrayList<HexUtils.Hex> getRing(int n) {
      HexUtils.Hex hex = new HexUtils.Hex(0, 0);

      for (int i = 0; i < n; i++) {
         hex = hex.getNeighbour(4);
      }

      ArrayList<HexUtils.Hex> list = new ArrayList<>();

      for (int j = 0; j < 6; j++) {
         for (int k = 0; k < n; k++) {
            list.add(hex);
            hex = hex.getNeighbour(j);
         }
      }

      return list;
   }

   public static ArrayList<HexUtils.Hex> distributeRingRandomly(int n, int n2, RandomSource random) {
      ArrayList<HexUtils.Hex> ring = getRing(n);
      ArrayList<HexUtils.Hex> list = new ArrayList<>();
      float n3 = (float)ring.size() / n2;
      random.nextInt(ring.size());
      float n4 = 0.0F;

      for (int i = 0; i < n2; i++) {
         list.add(ring.get(Math.round(n4)));
         n4 += n3;
      }

      return list;
   }

   public static HashMap<String, HexUtils.Hex> generateHexes(int n) {
      HashMap<String, HexUtils.Hex> hashMap = new HashMap<>();
      HexUtils.Hex neighbour = new HexUtils.Hex(0, 0);
      hashMap.put(neighbour.toString(), neighbour);

      for (int i = 0; i < n; i++) {
         neighbour = neighbour.getNeighbour(4);
         HexUtils.Hex neighbour2 = new HexUtils.Hex(neighbour.q, neighbour.r);

         for (int j = 0; j < 6; j++) {
            for (int k = 0; k <= i; k++) {
               hashMap.put(neighbour2.toString(), neighbour2);
               neighbour2 = neighbour2.getNeighbour(j);
            }
         }
      }

      return hashMap;
   }

   public static class CubicHex {
      public int x = 0;
      public int y = 0;
      public int z = 0;

      public CubicHex(int x, int y, int z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public HexUtils.Hex toHex() {
         return new HexUtils.Hex(this.x, this.z);
      }
   }

   public static class Hex {
      public int q = 0;
      public int r = 0;

      public Hex(int q, int r) {
         this.q = q;
         this.r = r;
      }

      public HexUtils.CubicHex toCubicHex() {
         return new HexUtils.CubicHex(this.q, this.r, -this.q - this.r);
      }

      public HexUtils.Pixel toPixel(int n) {
         return new HexUtils.Pixel(n * 1.5 * this.q, n * Math.sqrt(3.0) * (this.r + this.q / 2.0));
      }

      public HexUtils.Hex getNeighbour(int n) {
         int[] array = HexUtils.NEIGHBOURS[n];
         return new HexUtils.Hex(this.q + array[0], this.r + array[1]);
      }

      public boolean equals(HexUtils.Hex hex) {
         return hex.q == this.q && hex.r == this.r;
      }

      @Override
      public String toString() {
         return this.q + ":" + this.r;
      }
   }

   public static class Pixel {
      public double x = 0.0;
      public double y = 0.0;

      public Pixel(double x, double y) {
         this.x = x;
         this.y = y;
      }

      public HexUtils.Hex toHex(int n) {
         return HexUtils.getRoundedHex(0.6666666666666666 * this.x / n, (0.3333333333333333 * Math.sqrt(3.0) * -this.y - 0.3333333333333333 * this.x) / n);
      }
   }
}
