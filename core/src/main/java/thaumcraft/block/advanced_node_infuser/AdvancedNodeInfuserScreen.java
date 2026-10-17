package thaumcraft.block.advanced_node_infuser;

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
public class AdvancedNodeInfuserScreen extends AbstractContainerScreen<AdvancedNodeInfuserMenu> {
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/adv_node_infuser.png");

   public AdvancedNodeInfuserScreen(AdvancedNodeInfuserMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
   }

   public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(graphics, mouseX, mouseY, partialTick);
      super.render(graphics, mouseX, mouseY, partialTick);
      this.renderTooltip(graphics, mouseX, mouseY);
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(this.texture, this.leftPos, this.topPos, 0.0F, 0.0F, 256, 256, 256, 256);
      RenderSystem.disableBlend();
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.drawString(this.font, Component.translatable("block.thaumcraft.advanced_node_infuser"), 28, 4, 15658734, false);
   }
}
