package thaumcraft.item.aura_node;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemAuraNode extends BlockItem {
   public ItemAuraNode(Block b) {
      super(b, new Properties());
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(Component.translatable("item.thaumcraft.aura_node.tooltip.place_randomly_generated_node"));
   }

   protected boolean placeBlock(@NotNull BlockPlaceContext context, @NotNull BlockState blockState) {
      return super.placeBlock(context, blockState);
   }
}
