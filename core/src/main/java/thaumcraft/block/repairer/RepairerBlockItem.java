package thaumcraft.block.repairer;

import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import thaumcraft.item.render.ItemBlockEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RepairerBlockItem extends BlockItem {
   public RepairerBlockItem(Block block) {
      super(block, new Properties());
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(ItemBlockEntityRenderer.getInstance());
   }
}
