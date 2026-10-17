package thaumcraft.block.infusion_claw;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import thaumcraft.block.infusion_claw.render.InfusionClawItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemInfusionClaw extends BlockItem {
   public ItemInfusionClaw(Block block, Properties props) {
      super(block, props);
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         private final InfusionClawItemRenderer renderer = new InfusionClawItemRenderer();

         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return this.renderer;
         }
      });
   }
}
