package thaumcraft.item.matrix_upgrade;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MatrixUpgradeItem extends Item {
   public final int upgradeIndex;
   private final String descriptionId;

   public MatrixUpgradeItem(String descriptionId, int upgradeIndex) {
      super(new Properties());
      this.descriptionId = descriptionId;
      this.upgradeIndex = upgradeIndex;
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(Component.translatable(this.descriptionId));
   }
}
