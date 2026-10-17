package thaumcraft.item.equipment;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.SimpleTier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IWarpingGear;
import thaumcraft.init.registry.ModItems;

public class ItemCrimsonSword extends SwordItem implements IRepairable, IWarpingGear {
   private static final SimpleTier CRIMSON_TIER = new SimpleTier(
      BlockTags.SWORD_EFFICIENT, 200, 8.0F, 3.5F, 20, () -> Ingredient.of(new ItemLike[]{(ItemLike)ModItems.PRIMAL_CHARM.get()})
   );

   public ItemCrimsonSword() {
      super(CRIMSON_TIER, new Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(CRIMSON_TIER, 3, -2.4F)));
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(Component.translatable("enchantment.special.sapgreat").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
   }

   @Override
   public int getWarp(ItemStack itemstack, Player player) {
      return 2;
   }
}
