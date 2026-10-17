package thaumcraft.block.eccence_reservoir;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import thaumcraft.block.eccence_reservoir.render.EssenceReservoirItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemEssenceReservoir extends BlockItem {
   public ItemEssenceReservoir(Block block, Properties props) {
      super(block, props);
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         private BlockEntityWithoutLevelRenderer r;

         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.r == null) {
               this.r = new EssenceReservoirItemRenderer();
            }

            return this.r;
         }
      });
   }
}
