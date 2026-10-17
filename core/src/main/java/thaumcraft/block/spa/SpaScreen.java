package thaumcraft.block.spa;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModSounds;

@OnlyIn(Dist.CLIENT)
public class SpaScreen extends AbstractContainerScreen<SpaMenu> {
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/spa.png");

   public SpaScreen(SpaMenu menu, Inventory inventory, Component component) {
      super(menu, inventory, component);
   }

   public void render(@NotNull GuiGraphics graphics, int mx, int my, float pt) {
      this.renderBackground(graphics, mx, my, pt);
      super.render(graphics, mx, my, pt);
      int x = this.leftPos;
      int y = this.topPos;
      int n4 = mx - (x + 104);
      int n5 = my - (y + 10);
      if (n4 >= 0 && n5 >= 0 && n4 < 10 && n5 < 55) {
         ArrayList<Component> list = new ArrayList<>();
         FluidStack fluid = ((SpaMenu)this.menu).spa.tank.getFluid();
         if (!fluid.isEmpty()) {
            list.add(fluid.getDisplayName());
            list.add(Component.literal(fluid.getAmount() + " mb"));
            graphics.renderComponentTooltip(this.font, list, mx, my);
         }
      }

      int n6 = mx - (x + 88);
      int n7 = my - (y + 34);
      if (n6 >= 0 && n7 >= 0 && n6 < 10 && n7 < 10) {
         graphics.renderTooltip(this.font, Component.translatable(((SpaMenu)this.menu).spa.getMix() ? "text.spa.mix.true" : "text.spa.mix.false"), mx, my);
      }

      this.renderTooltip(graphics, mx, my);
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mx, int my) {
      PoseStack pose = graphics.pose();
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int x = this.leftPos;
      int y = this.topPos;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      graphics.blit(this.texture, x, y, 0, 0, this.imageWidth, this.imageHeight);
      graphics.blit(this.texture, x + 89, y + 35, 208, ((SpaMenu)this.menu).spa.getMix() ? 16 : 32, 8, 8);
      FluidTank tank = ((SpaMenu)this.menu).spa.tank;
      if (tank.getFluidAmount() > 0) {
         FluidStack fluid = tank.getFluid();
         if (!fluid.isEmpty()) {
            TextureAtlasSprite sprite = (TextureAtlasSprite)this.minecraft
               .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
               .apply(IClientFluidTypeExtensions.of(fluid.getFluid()).getStillTexture(fluid));
            if (sprite != null) {
               float n6 = (float)tank.getFluidAmount() / tank.getCapacity();
               pose.pushPose();
               pose.translate(x + 107, y + 15, 0.0F);
               pose.scale(8.0F, 8.0F, 8.0F);

               for (int i = 0; i < 6; i++) {
                  graphics.blit(0, i, 0, 1, 1, sprite);
               }

               pose.popPose();
               graphics.blit(this.texture, x + 107, y + 15, 107, 15, 10, (int)(48.0F - 48.0F * n6));
            }
         }
      }

      graphics.blit(this.texture, x + 106, y + 11, 232, 0, 10, 55);
      RenderSystem.disableBlend();
   }

   public boolean mouseClicked(double mx, double my, int button) {
      double n6 = mx - (this.leftPos + 89);
      double n7 = my - (this.topPos + 35);
      if (n6 >= 0.0 && n7 >= 0.0 && n6 < 8.0 && n7 < 8.0) {
         if (this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(((SpaMenu)this.menu).containerId, 1);
         }

         this.playButtonClick();
         return true;
      } else {
         return super.mouseClicked(mx, my, button);
      }
   }

   private void playButtonClick() {
      if (this.minecraft.cameraEntity != null) {
         Entity entity = this.minecraft.cameraEntity;
         entity.level()
            .playLocalSound(entity.getX(), entity.getY(), entity.getZ(), (SoundEvent)ModSounds.CAMERA_CLACK.get(), SoundSource.BLOCKS, 0.4F, 1.0F, false);
      }
   }
}
