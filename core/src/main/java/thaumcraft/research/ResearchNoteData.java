package thaumcraft.research;

import java.util.HashMap;
import thaumcraft.util.HexUtils;

public class ResearchNoteData {
   public String key;
   public int color;
   public HashMap<String, ResearchManager.HexEntry> hexEntries = new HashMap<>();
   public HashMap<String, HexUtils.Hex> hexes = new HashMap<>();
   public boolean complete;
   public int copies;

   public boolean isComplete() {
      return this.complete;
   }
}
