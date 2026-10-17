package thaumcraft.item.golem_decoration;


public enum GolemDecorationType {
   TOP_HAT("H"),
   GOGGLES("G"),
   BOWTIE("B"),
   FEZ("F"),
   DART("R"),
   VISOR("V"),
   ARMOR("P"),
   MACE("M");

   private final String charKey;

   private GolemDecorationType(String charKey) {
      this.charKey = charKey;
   }

   public String getCharKey() {
      return this.charKey;
   }
}
