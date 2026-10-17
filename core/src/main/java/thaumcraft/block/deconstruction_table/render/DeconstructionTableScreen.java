package thaumcraft.block.deconstruction_table.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.block.deconstruction_table.DeconstructionTableMenu;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class DeconstructionTableScreen extends AbstractContainerScreen<DeconstructionTableMenu> {
   private static final int ASPECT_X = 64;
   private static final int ASPECT_Y = 48;
   private static final int ASPECT_W = 16;
   private static final int ASPECT_H = 16;
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/deconstruction_table.png");

   public DeconstructionTableScreen(DeconstructionTableMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
   }

   public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(graphics, mouseX, mouseY, partialTick);
      super.render(graphics, mouseX, mouseY, partialTick);
      this.renderTooltip(graphics, mouseX, mouseY);
      if (((DeconstructionTableMenu)this.menu).table != null
         && ((DeconstructionTableMenu)this.menu).table.aspect != null
         && this.isHovering(64, 48, 16, 16, mouseX, mouseY)) {
         graphics.renderTooltip(
            this.font,
            Arrays.asList(
               ((DeconstructionTableMenu)this.menu).table.aspect.getName(), ((DeconstructionTableMenu)this.menu).table.aspect.getLocalizedDescription()
            ),
            Optional.empty(),
            mouseX,
            mouseY
         );
      }
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      if (((DeconstructionTableMenu)this.menu).table != null && ((DeconstructionTableMenu)this.menu).table.breaktime > 0) {
         int scaled = ((DeconstructionTableMenu)this.menu).table.getBreakTimeScaled(46);
         graphics.blit(this.texture, this.leftPos + 93, this.topPos + 15 + 46 - scaled, 176, 46 - scaled, 9, scaled);
      }

      if (((DeconstructionTableMenu)this.menu).table != null && ((DeconstructionTableMenu)this.menu).table.aspect != null) {
         UtilsFX.drawTag(graphics, this.leftPos + 64, this.topPos + 48, ((DeconstructionTableMenu)this.menu).table.aspect, 0.0F, 0, 0.0F);
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      RenderSystem.disableBlend();
   }

   protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      double x = mouseX - (this.leftPos + 64);
      double y = mouseY - (this.topPos + 48);
      if (!(x >= 0.0)
         || !(y >= 0.0)
         || !(x < 16.0)
         || !(y < 16.0)
         || ((DeconstructionTableMenu)this.menu).table == null
         || ((DeconstructionTableMenu)this.menu).table.aspect == null) {
         return super.mouseClicked(mouseX, mouseY, button);
      } else {
         assert this.minecraft != null;

         assert this.minecraft.gameMode != null;

         this.minecraft.gameMode.handleInventoryButtonClick(((DeconstructionTableMenu)this.menu).containerId, 1);
         this.playButtonAspect();
         return true;
      }
   }

   private void playButtonAspect() {
      assert this.minecraft != null;

      Entity e = this.minecraft.cameraEntity;
      if (e != null) {
         e.level()
            .playLocalSound(
               e.getX(), e.getY(), e.getZ(), (SoundEvent)ModSounds.HHOFF.get(), SoundSource.BLOCKS, 0.2F, 1.0F + e.level().random.nextFloat() * 0.1F, false
            );
      }
   }
}
