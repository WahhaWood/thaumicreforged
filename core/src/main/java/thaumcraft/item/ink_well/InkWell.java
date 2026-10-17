package thaumcraft.item.ink_well;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import thaumcraft.api.IScribeTools;

public class InkWell extends Item implements IScribeTools {
   public InkWell() {
      super(new Properties().stacksTo(1).durability(100));
   }
}
