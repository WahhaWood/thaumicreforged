package thaumcraft.item.wands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.init.registry.ModItems;

public class ItemWandCap extends Item {
   public static final Map<String, ItemWandCap> TYPES = new HashMap<>();
   private final String tag;
   private final int craftCost;
   private final float basicDiscount;
   private final List<Aspect> aspectsSpecialDiscount;
   private final float specialDiscount;
   private final ResourceLocation model;

   public ItemWandCap(String tag, float basicDiscount, int craftCost) {
      this(tag, basicDiscount, null, 0.0F, craftCost, Thaumcraft.location("item/wand/cap/" + tag));
   }

   public ItemWandCap(String tag, float basicDiscount, int craftCost, ResourceLocation model) {
      this(tag, basicDiscount, null, 0.0F, craftCost, model);
   }

   public ItemWandCap(String tag, float basicDiscount, List<Aspect> aspectsSpecialDiscount, float specialDiscount, int craftCost) {
      this(tag, basicDiscount, aspectsSpecialDiscount, specialDiscount, craftCost, Thaumcraft.location("item/wand/cap/" + tag));
   }

   public ItemWandCap(String tag, float basicDiscount, List<Aspect> aspectsSpecialDiscount, float specialDiscount, int craftCost, ResourceLocation model) {
      super(new Properties());
      this.tag = tag;
      this.basicDiscount = basicDiscount;
      this.aspectsSpecialDiscount = aspectsSpecialDiscount;
      this.specialDiscount = specialDiscount;
      this.model = model;
      this.craftCost = craftCost;
      synchronized (TYPES) {
         TYPES.put(tag, this);
      }
   }

   public float getBasicDiscount() {
      return this.basicDiscount;
   }

   public List<Aspect> getAspectsSpecialDiscount() {
      return this.aspectsSpecialDiscount;
   }

   public float getSpecialDiscount() {
      return this.specialDiscount;
   }

   public ResourceLocation getModel() {
      return this.model;
   }

   public String getTag() {
      return this.tag;
   }

   public int getCraftCost() {
      return this.craftCost;
   }

   public String getResearch() {
      return "CAP_" + this.getTag();
   }

   public static ItemWandCap get(String s) {
      return TYPES.getOrDefault(s, (ItemWandCap)ModItems.IRON_WAND_CAP.get());
   }
}
