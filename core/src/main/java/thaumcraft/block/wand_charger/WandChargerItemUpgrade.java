package thaumcraft.block.wand_charger;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WandChargerItemUpgrade extends Item {
   private final WandChargerItemUpgrade.Type type;

   public WandChargerItemUpgrade(WandChargerItemUpgrade.Type type) {
      super(new Properties().stacksTo(1));
      this.type = type;
   }

   public WandChargerItemUpgrade.Type getType() {
      return this.type;
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(Component.translatable(this.type.getDescriptionKey()).withStyle(ChatFormatting.GOLD));
   }

   public static enum Type {
      VIS_BUFFER(1, "item.thaumcraft.wand_charger_vis_buffer.desc"),
      CAP_CHARGE(2, "item.thaumcraft.wand_charger_cap_charge.desc"),
      SPEED_CHARGE(3, "item.thaumcraft.wand_charger_speed_charge.desc"),
      ALLOW_STAFF(4, "item.thaumcraft.wand_charger_allow_staff.desc");

      private final int slotIndex;
      private final String descriptionKey;

      private Type(int slotIndex, String descriptionKey) {
         this.slotIndex = slotIndex;
         this.descriptionKey = descriptionKey;
      }

      public int getSlotIndex() {
         return this.slotIndex;
      }

      public String getDescriptionKey() {
         return this.descriptionKey;
      }
   }
}
