package thaumcraft.block.magnet;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.soul_mould.ItemSoulMould;

public class MobMagnetScreen extends AbstractContainerScreen<MobMagnetMenu> {
   private static final ResourceLocation TEXTURE = Thaumcraft.location("textures/screen/mob_magnet.png");

   public MobMagnetScreen(MobMagnetMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
   }

   public void render(@NotNull GuiGraphics graphics, int mx, int my, float pt) {
      this.renderBackground(graphics, mx, my, pt);
      super.render(graphics, mx, my, pt);
      this.renderTooltip(graphics, mx, my);
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mx, int my) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      int x = this.leftPos;
      int y = this.topPos;
      graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
      boolean adult = ((MobMagnetMenu)this.menu).magnet.isAdultMode();
      graphics.blit(TEXTURE, x + 100, y + 28, 176, adult ? 0 : 8, 8, 8);
      graphics.blit(TEXTURE, x + 100, y + 48, 176, adult ? 8 : 0, 8, 8);
      ItemStack stack = ((MobMagnetMenu)this.menu).magnet.getItem(0);
      Component filter;
      if (stack.isEmpty()) {
         filter = Component.translatable("ttmisc.all");
      } else {
         String name = ItemSoulMould.getPatternName(stack);
         if (name.isEmpty()) {
            filter = Component.translatable("ttmisc.none");
         } else {
            ResourceLocation id = ResourceLocation.tryParse(name);
            EntityType<?> type = id != null ? (EntityType)BuiltInRegistries.ENTITY_TYPE.get(id) : null;
            filter = (Component)(type == null ? Component.translatable("ttmisc.none") : type.getDescription());
         }
      }

      graphics.drawString(this.font, filter, x + this.imageWidth / 2 - this.font.width(filter) / 2 - 26, y + 16, 10066329, false);
      graphics.drawString(this.font, Component.translatable("ttmisc.adult"), x + 120, y + 30, 10066329, false);
      graphics.drawString(this.font, Component.translatable("ttmisc.child"), x + 120, y + 50, 10066329, false);
      RenderSystem.disableBlend();
   }

   public boolean mouseClicked(double mx, double my, int button) {
      int x = this.leftPos;
      int y = this.topPos;
      if (isOver(mx, my, x + 100, y + 28, 8, 8)) {
         this.clickMode(0);
         return true;
      } else if (isOver(mx, my, x + 100, y + 48, 8, 8)) {
         this.clickMode(1);
         return true;
      } else {
         return super.mouseClicked(mx, my, button);
      }
   }

   private static boolean isOver(double mx, double my, int x, int y, int w, int h) {
      return mx >= x && my >= y && mx < x + w && my < y + h;
   }

   private void clickMode(int id) {
      if (this.minecraft.gameMode != null) {
         this.minecraft.gameMode.handleInventoryButtonClick(((MobMagnetMenu)this.menu).containerId, id);
      }

      if (this.minecraft.cameraEntity != null) {
         Entity entity = this.minecraft.cameraEntity;
         entity.level()
            .playLocalSound(entity.getX(), entity.getY(), entity.getZ(), (SoundEvent)ModSounds.CAMERA_CLACK.get(), SoundSource.BLOCKS, 0.4F, 1.0F, false);
      }
   }
}
