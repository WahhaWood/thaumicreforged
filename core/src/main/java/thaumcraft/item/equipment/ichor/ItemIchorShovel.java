package thaumcraft.item.equipment.ichor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ThaumcraftApi;

public class ItemIchorShovel extends ShovelItem {
   public ItemIchorShovel() {
      super(
         ThaumcraftApi.TOOL_MATERIAL_ICHOR,
         new Properties()
            .rarity(Rarity.EPIC)
            .fireResistant()
            .attributes(DiggerItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_ICHOR, 1.5F, -3.0F))
            .component(DataComponents.UNBREAKABLE, new Unbreakable(false))
      );
   }

   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
   }
}
