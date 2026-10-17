package thaumcraft.api;

import java.awt.Color;

public class ColorHelper {
   public static int toHex(int red, int green, int blue) {
      return red << 16 | green << 8 | blue;
   }

   public static int toHex(Color color) {
      return toHex(color.getRed(), color.getGreen(), color.getBlue());
   }
}
