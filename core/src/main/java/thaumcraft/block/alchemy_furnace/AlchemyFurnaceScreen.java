package thaumcraft.block.alchemy_furnace;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;

@OnlyIn(Dist.CLIENT)
public class AlchemyFurnaceScreen extends AbstractContainerScreen<AlchemyFurnaceMenu> {
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/alchemy_furnace.png");

   public AlchemyFurnaceScreen(AlchemyFurnaceMenu menu, Inventory inventory, Component component) {
      super(menu, inventory, component);
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mx, int my) {
      RenderSystem.enableBlend();
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      if (((AlchemyFurnaceMenu)this.menu).furnace.isBurning()) {
         int burnTimeRemainingScaled = ((AlchemyFurnaceMenu)this.menu).furnace.getBurnTimeRemainingScaled(20);
         graphics.blit(
            this.texture, this.leftPos + 80, this.topPos + 26 + 20 - burnTimeRemainingScaled, 176, 20 - burnTimeRemainingScaled, 16, burnTimeRemainingScaled
         );
      }

      int cookProgressScaled = ((AlchemyFurnaceMenu)this.menu).furnace.getCookProgressScaled(46);
      graphics.blit(this.texture, this.leftPos + 106, this.topPos + 13 + 46 - cookProgressScaled, 216, 46 - cookProgressScaled, 9, cookProgressScaled);
      int contentsScaled = ((AlchemyFurnaceMenu)this.menu).furnace.getContentsScaled(48);
      graphics.blit(this.texture, this.leftPos + 61, this.topPos + 12 + 48 - contentsScaled, 200, 48 - contentsScaled, 8, contentsScaled);
      graphics.blit(this.texture, this.leftPos + 60, this.topPos + 8, 232, 0, 10, 55);
      RenderSystem.disableBlend();
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mx, int my) {
   }
}
