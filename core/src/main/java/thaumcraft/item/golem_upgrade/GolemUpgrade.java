package thaumcraft.item.golem_upgrade;


public enum GolemUpgrade {
   AIR("air", "Increased movement speed"),
   FIRE("fire", "Fire resistance and attack damage"),
   STRENGTH("strength", "Attack damage bonus"),
   WATER("water", "Can swim and breathe underwater"),
   EARTH("earth", "Increased health and knockback resistance"),
   ORDER("order", "More efficient task completion"),
   ENTROPY("entropy", "Area effect for tasks"),
   INVENTORY("inventory", "Increased inventory size"),
   PERCEPTION("perception", "Increased range"),
   VITALITY("vitality", "Health regeneration"),
   HASTE("haste", "Faster task execution");

   private final String name;
   private final String description;

   private GolemUpgrade(String name, String description) {
      this.name = name;
      this.description = description;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }
}
