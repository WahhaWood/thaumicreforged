package thaumcraft.item.shard;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class ItemShard extends Item {
   private final int color;

   public ItemShard(int color) {
      super(new Properties());
      this.color = color;
   }

   public int getColor() {
      return this.color;
   }
}
