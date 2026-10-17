package thaumcraft.item.golem_upgrade;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemGolemUpgrade extends Item {
   private static final Map<GolemUpgrade, ItemGolemUpgrade> BY_UPGRADE = new EnumMap<>(GolemUpgrade.class);
   private final GolemUpgrade upgrade;

   public ItemGolemUpgrade(GolemUpgrade upgrade) {
      super(new Properties().rarity(Rarity.UNCOMMON).stacksTo(16));
      this.upgrade = upgrade;
      BY_UPGRADE.put(upgrade, this);
   }

   public GolemUpgrade getUpgrade() {
      return this.upgrade;
   }

   @Nullable
   public static ItemGolemUpgrade getItemByUpgrade(GolemUpgrade upgrade) {
      return BY_UPGRADE.get(upgrade);
   }

   public static ItemStack getStackByUpgrade(GolemUpgrade upgrade) {
      ItemGolemUpgrade item = getItemByUpgrade(upgrade);
      return item == null ? ItemStack.EMPTY : new ItemStack(item);
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(Component.translatable(this.getDescriptionId() + ".desc"));
   }
}
