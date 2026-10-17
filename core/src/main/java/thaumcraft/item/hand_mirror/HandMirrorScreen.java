package thaumcraft.item.hand_mirror;

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
public class HandMirrorScreen extends AbstractContainerScreen<HandMirrorMenu> {
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/guihandmirror.png");
   private final int blockSlot;

   public HandMirrorScreen(HandMirrorMenu menu, Inventory inventory, Component component) {
      super(menu, inventory, component);
      this.blockSlot = inventory.selected;
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mouseX, int mouseY) {
      if (this.minecraft.player.getInventory().getItem(this.blockSlot).isEmpty()) {
         this.minecraft.player.closeContainer();
      }

      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(this.texture, (this.width - this.imageWidth) / 2, (this.height - this.imageHeight) / 2, 0, 0, this.imageWidth, this.imageHeight);
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
   }

   protected boolean checkHotbarKeyPressed(int mouseX, int mouseY) {
      return false;
   }
}
