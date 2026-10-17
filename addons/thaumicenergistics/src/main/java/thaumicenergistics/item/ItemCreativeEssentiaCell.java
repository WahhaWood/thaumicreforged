package thaumicenergistics.item;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.items.contents.CellConfig;
import appeng.items.storage.BasicStorageCell;
import appeng.util.ConfigInventory;
import java.util.Set;
import thaumicenergistics.ae2.storage.AEAspectKeyType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class ItemCreativeEssentiaCell extends BasicStorageCell {
   private static final int CREATIVE_TYPES = 63;

   public ItemCreativeEssentiaCell(ItemLike housingItem) {
      super(new Properties().stacksTo(1), 0.0, 1, 8, 63, AEAspectKeyType.INSTANCE);
   }

   public int getBytes(ItemStack cellItem) {
      return Integer.MAX_VALUE;
   }

   public int getBytesPerType(ItemStack cellItem) {
      return 1;
   }

   public int getTotalTypes(ItemStack cellItem) {
      return 63;
   }

   public ConfigInventory getConfigInventory(ItemStack is) {
      return CellConfig.create(Set.of(AEAspectKeyType.INSTANCE), is);
   }

   public double getIdleDrain() {
      return 0.0;
   }

   public IUpgradeInventory getUpgrades(ItemStack is) {
      return UpgradeInventories.empty();
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      return InteractionResultHolder.pass(player.getItemInHand(hand));
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      return InteractionResult.PASS;
   }

   public boolean isStorageCell(ItemStack i) {
      return false;
   }
}
