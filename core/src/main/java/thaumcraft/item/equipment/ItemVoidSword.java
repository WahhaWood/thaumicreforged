package thaumcraft.item.equipment;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IWarpingGear;
import thaumcraft.api.ThaumcraftApi;

public class ItemVoidSword extends SwordItem implements IRepairable, IWarpingGear {
   public ItemVoidSword() {
      super(
         ThaumcraftApi.TOOL_MATERIAL_VOID,
         new Properties().rarity(Rarity.UNCOMMON).attributes(SwordItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_VOID, 3, -2.4F))
      );
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(Component.translatable("enchantment.special.sapless").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
   }

   @Override
   public int getWarp(ItemStack itemstack, Player player) {
      return 1;
   }
}
