package thaumcraft.item.crystal_cluster;

import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import thaumcraft.item.render.ItemBlockEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemCrystalCluster extends BlockItem {
   public ItemCrystalCluster(Block block) {
      super(block, new Properties());
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(ItemBlockEntityRenderer.getInstance());
   }
}
