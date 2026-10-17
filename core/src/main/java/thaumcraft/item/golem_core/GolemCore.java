package thaumcraft.item.golem_core;


public enum GolemCore {
   FILL(0, "fill", true, true, true),
   EMPTY(1, "empty", true, true, true),
   GATHER(2, "gather", true, true, true),
   HARVEST(3, "harvest", false, false, false),
   GUARD(4, "guard", true, false, false),
   LIQUID(5, "liquid", true, false, true),
   ESSENCE(6, "essence", false, false, false),
   LUMBER(7, "lumber", false, false, false),
   USE(8, "use", true, true, true),
   BUTCHER(9, "butcher", false, false, false),
   SORTING(10, "sorting", true, true, false),
   FISHING(11, "fishing", false, false, false),
   BLANK(100, "blank", false, false, false);

   private final int id;
   private final String name;
   private final boolean hasGUI;
   private final boolean canSort;
   private final boolean hasInventory;

   private GolemCore(int id, String name, boolean hasGUI, boolean canSort, boolean hasInventory) {
      this.id = id;
      this.name = name;
      this.hasGUI = hasGUI;
      this.canSort = canSort;
      this.hasInventory = hasInventory;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public boolean hasGUI() {
      return this.hasGUI;
   }

   public boolean canSort() {
      return this.canSort;
   }

   public boolean hasInventory() {
      return this.hasInventory;
   }
}
