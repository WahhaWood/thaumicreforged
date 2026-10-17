package thaumcraft.block.infusion_matrix;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.util.ItemStackData;

public class RunicMatrixBlockItem extends BlockItem {
   public RunicMatrixBlockItem(Block block) {
      super(block, new Properties());
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null && tag.getBoolean("isInfo") && tag.contains("metaUpdate")) {
         int meta = tag.getByte("metaUpdate");
         String key = "item.info." + meta;

         ChatFormatting color = switch (meta) {
            case 0 -> ChatFormatting.BLUE;
            case 1 -> ChatFormatting.YELLOW;
            case 2 -> ChatFormatting.WHITE;
            case 3 -> ChatFormatting.GREEN;
            case 4 -> ChatFormatting.AQUA;
            case 5 -> ChatFormatting.DARK_PURPLE;
            default -> ChatFormatting.GRAY;
         };
         components.add(Component.translatable(key).withStyle(color));
      }
   }
}
