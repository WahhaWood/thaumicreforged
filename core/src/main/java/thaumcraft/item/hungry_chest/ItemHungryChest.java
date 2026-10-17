package thaumcraft.item.hungry_chest;

import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import thaumcraft.item.render.ItemBlockEntityRenderer;

public class ItemHungryChest extends BlockItem {
   public ItemHungryChest(Block block, Properties properties) {
      super(block, properties);
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(ItemBlockEntityRenderer.getInstance());
   }
}
