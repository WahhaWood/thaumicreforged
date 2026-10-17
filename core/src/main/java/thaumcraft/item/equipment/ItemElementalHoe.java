package thaumcraft.item.equipment;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.IRepairable;
import thaumcraft.api.ThaumcraftApi;

public class ItemElementalHoe extends HoeItem implements IRepairable {
   public ItemElementalHoe() {
      super(
         ThaumcraftApi.TOOL_MATERIAL_ELEMENTAL,
         new Properties().rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_ELEMENTAL, -3.0F, 0.0F))
      );
   }

   public int getEnchantmentValue() {
      return 5;
   }

   @NotNull
   public InteractionResult useOn(@NotNull UseOnContext context) {
      Player player = context.getPlayer();
      if (player == null || player.isShiftKeyDown()) {
         return super.useOn(context);
      } else {
         return context.getLevel().isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
      }
   }
}
