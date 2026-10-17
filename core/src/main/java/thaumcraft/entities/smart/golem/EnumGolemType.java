package thaumcraft.entities.smart.golem;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum EnumGolemType {
   STRAW(0, 10, 0, 0.38F, false, 1, 1, 75, 0),
   WOOD(1, 20, 6, 0.35F, false, 1, 4, 75, 1),
   TALLOW(2, 20, 9, 0.33F, false, 2, 8, 75, 2),
   CLAY(3, 25, 9, 0.33F, true, 1, 8, 100, 2),
   FLESH(4, 15, 6, 0.35F, false, 2, 4, 40, 1),
   STONE(5, 30, 12, 0.32F, true, 1, 16, 100, 3),
   IRON(6, 35, 15, 0.31F, true, 1, 32, 125, 4),
   THAUMIUM(7, 40, 15, 0.32F, true, 2, 32, 100, 4);

   public final int health;
   public final int armor;
   public final int upgrades;
   public final int carry;
   public final int regenDelay;
   public final int strength;
   public final float speed;
   public final boolean fireResist;
   private final int legacyId;
   private static final Int2ObjectMap<EnumGolemType> ID_TO_TYPE = new Int2ObjectOpenHashMap();

   public static EnumGolemType getType(int i) {
      return (EnumGolemType)ID_TO_TYPE.get(i);
   }

   public static EnumGolemType byId(int id) {
      return (EnumGolemType)ID_TO_TYPE.get(id);
   }

   public int getLegacyId() {
      return this.legacyId;
   }

   private EnumGolemType(int legacyId, int health, int armor, float speed, boolean fireResist, int upgrades, int carry, int regenDelay, int strength) {
      this.legacyId = legacyId;
      this.health = health;
      this.armor = armor;
      this.speed = speed;
      this.fireResist = fireResist;
      this.upgrades = upgrades;
      this.carry = carry;
      this.regenDelay = regenDelay;
      this.strength = strength;
   }

   static {
      for (EnumGolemType t : values()) {
         ID_TO_TYPE.put(t.legacyId, t);
      }
   }
}
