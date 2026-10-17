package thaumcraft.item.eldritch;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EldritchObjectItem extends Item {
   public EldritchObjectItem(Rarity rarity) {
      super(new Properties().stacksTo(1).rarity(rarity));
   }

   public EldritchObjectItem() {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(Component.translatable("item.ItemEldritchObject.text.1").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)));
   }
}
