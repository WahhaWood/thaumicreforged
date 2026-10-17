package thaumcraft.item.equipment.ichor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ThaumcraftApi;

public class ItemIchorSword extends SwordItem {
   public ItemIchorSword() {
      super(
         ThaumcraftApi.TOOL_MATERIAL_ICHOR,
         new Properties()
            .rarity(Rarity.EPIC)
            .fireResistant()
            .attributes(SwordItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_ICHOR, 3, -2.4F))
            .component(DataComponents.UNBREAKABLE, new Unbreakable(false))
      );
   }

   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
   }
}
