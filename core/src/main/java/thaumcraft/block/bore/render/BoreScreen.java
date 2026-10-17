package thaumcraft.block.bore.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.block.bore.BoreBlockEntity;
import thaumcraft.block.bore.BoreMenu;
import thaumcraft.item.equipment.ItemElementalPickaxe;
import thaumcraft.item.wands.foci.ItemFocusExcavation;

@OnlyIn(Dist.CLIENT)
public class BoreScreen extends AbstractContainerScreen<BoreMenu> {
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/arcane_bore.png");

   public BoreScreen(BoreMenu menu, Inventory inventory, Component component) {
      super(menu, inventory, component);
      this.imageWidth = 176;
      this.imageHeight = 141;
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mx, int my) {
      BoreBlockEntity bore = ((BoreMenu)this.menu).bore;
      ItemStack focus = bore.getItem(0);
      ItemStack pickaxe = bore.getItem(1);
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      if (!pickaxe.isEmpty() && pickaxe.getDamageValue() + 1 >= pickaxe.getMaxDamage()) {
         graphics.blit(this.texture, this.leftPos + 74, this.topPos + 18, 184, 0, 16, 16);
      }

      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(this.leftPos + 112, this.topPos + 8, 505.0F);
      pose.scale(0.5F, 0.5F, 0.0F);
      graphics.drawString(this.font, "Width: " + (1 + (bore.area + bore.maxRadius) * 2), 0, 0, 16777215);
      graphics.drawString(this.font, "Speed: +" + bore.speed, 0, 10, 16777215);
      graphics.drawString(this.font, "Other properties:", 0, 24, 16777215);
      int n6 = 0;
      if (!pickaxe.isEmpty() && pickaxe.getItem() instanceof ItemElementalPickaxe
         || focus.getItem() instanceof ItemFocusBasic basic && basic.isUpgradedWith(focus, ItemFocusExcavation.dowsing)) {
         graphics.drawString(this.font, "Native Clusters", 4, 34 + n6, 12632256);
         n6 += 9;
      }

      if (bore.fortune > 0) {
         graphics.drawString(this.font, "Fortune " + bore.fortune, 4, 34 + n6, 15648330);
         n6 += 9;
      }

      if (!pickaxe.isEmpty() && EnchantmentHelper.getTagEnchantmentLevel(this.silkTouchHolder(), pickaxe) > 0
         || focus.getItem() instanceof ItemFocusBasic basic && basic.isUpgradedWith(focus, FocusUpgradeType.silktouch)) {
         graphics.drawString(this.font, "Silk Touch", 4, 34 + n6, 8421631);
      }

      pose.popPose();
   }

   private Holder<Enchantment> silkTouchHolder() {
      return this.minecraft.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mx, int my) {
   }
}
