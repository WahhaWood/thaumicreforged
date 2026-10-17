package thaumcraft.block.deconstruction_table;

import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import thaumcraft.item.render.ItemBlockEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DeconstructionTableBlockItem extends BlockItem {
   public DeconstructionTableBlockItem(Block block, Properties props) {
      super(block, props);
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(ItemBlockEntityRenderer.getInstance());
   }
}
