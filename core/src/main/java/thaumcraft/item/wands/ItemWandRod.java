package thaumcraft.item.wands;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import thaumcraft.Thaumcraft;
import thaumcraft.api.wands.IWandRodOnUpdate;
import thaumcraft.init.registry.ModItems;

public class ItemWandRod extends Item {
   public static final Map<String, ItemWandRod> TYPES = new HashMap<>();
   private final String tag;
   private final int craftCost;
   private final int capacity;
   private final IWandRodOnUpdate onUpdate;
   private final ResourceLocation model;
   private boolean glow;
   private boolean staff;
   private boolean runes;

   public ItemWandRod(String tag, int capacity, int craftCost) {
      this(tag, capacity, craftCost, null, Thaumcraft.location("item/wand/rod/" + tag));
   }

   public ItemWandRod(String tag, int capacity, int craftCost, ResourceLocation model) {
      this(tag, capacity, craftCost, null, model);
   }

   public ItemWandRod(String tag, int capacity, int craftCost, IWandRodOnUpdate onUpdate) {
      this(tag, capacity, craftCost, onUpdate, Thaumcraft.location("item/wand/rod/" + tag));
   }

   public ItemWandRod(String tag, int capacity, int craftCost, IWandRodOnUpdate onUpdate, ResourceLocation model) {
      super(new Properties());
      this.tag = tag;
      this.capacity = capacity;
      this.model = model;
      this.craftCost = craftCost;
      this.onUpdate = onUpdate;
      synchronized (TYPES) {
         TYPES.put(this.tag, this);
      }
   }

   public String getTag() {
      return this.tag;
   }

   public int getCapacity() {
      return this.capacity;
   }

   public ResourceLocation getModel() {
      return this.model;
   }

   public int getCraftCost() {
      return this.craftCost;
   }

   public IWandRodOnUpdate getOnUpdate() {
      return this.onUpdate;
   }

   public boolean isGlowing() {
      return this.glow;
   }

   public ItemWandRod glowing() {
      this.glow = true;
      return this;
   }

   public boolean isStaff() {
      return this.staff;
   }

   public ItemWandRod staff() {
      this.staff = true;
      return this;
   }

   public boolean isRunes() {
      return this.runes;
   }

   public ItemWandRod runes() {
      this.runes = true;
      return this;
   }

   public String getResearch() {
      return "ROD_" + this.getTag();
   }

   public static ItemWandRod get(String s) {
      return TYPES.getOrDefault(s, (ItemWandRod)ModItems.WOOD_WAND_ROD.get());
   }
}
