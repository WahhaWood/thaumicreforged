package thaumcraft.item.golem_decoration;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class ItemGolemDecoration extends Item {
   private final GolemDecorationType type;

   public ItemGolemDecoration(GolemDecorationType type) {
      super(new Properties().stacksTo(64));
      this.type = type;
   }

   public String getDecoChar() {
      return this.type.getCharKey();
   }

   public GolemDecorationType getDecoType() {
      return this.type;
   }
}
