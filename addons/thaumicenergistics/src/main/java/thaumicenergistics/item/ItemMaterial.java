package thaumicenergistics.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class ItemMaterial extends Item {
   private final ItemMaterial.MaterialType type;

   public ItemMaterial(ItemMaterial.MaterialType type) {
      super(new Properties());
      this.type = type;
   }

   public ItemMaterial.MaterialType getType() {
      return this.type;
   }

   public static enum MaterialType {
      DIFFUSION_CORE,
      COALESCENCE_CORE,
      IRON_GEAR;
   }
}
