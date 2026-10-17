package thaumcraft.block.tube;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum TubeSide implements StringRepresentable {
   NONE("none"),
   NORMAL("normal"),
   EXTENDED("extended");

   private final String name;

   private TubeSide(String name) {
      this.name = name;
   }

   @NotNull
   public String getSerializedName() {
      return this.name;
   }

   public boolean isConnected() {
      return this != NONE;
   }
}
