package thaumcraft.block.encharter;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.enchantment.EnchantmentData;
import thaumcraft.enchantment.EnchantmentManager;
import thaumcraft.util.ItemStackData;

@OnlyIn(Dist.CLIENT)
public class EnchanterScreen extends AbstractContainerScreen<EnchanterMenu> {
   private static final ResourceLocation GUI = Thaumcraft.location("textures/screen/enchanter.png");
   private static final int TEX_W = 256;
   private static final int TEX_H = 256;
   private ItemStack cachedStack = ItemStack.EMPTY;
   private List<ResourceLocation> cachedEnchants = new ArrayList<>();
   private List<Integer> cachedLevels = new ArrayList<>();
   private boolean cachedWorking = false;
   private final List<EnchanterScreen.EnchantChoice> cachedChoices = new ArrayList<>();
   private boolean cachedTwoRows = false;
   List<Component> tooltip = new ArrayList<>();

   public EnchanterScreen(EnchanterMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.imageHeight = 166;
   }

   protected void init() {
      super.init();
      this.rebuildAll();
   }

   protected void containerTick() {
      super.containerTick();
      ItemStack cur = ((EnchanterMenu)this.menu).enchanter.getItem(0);
      List<ResourceLocation> ench = ((EnchanterMenu)this.menu).enchanter.enchantments;
      List<Integer> lvl = ((EnchanterMenu)this.menu).enchanter.levels;
      boolean working = ((EnchanterMenu)this.menu).enchanter.working;
      if (!sameStack(this.cachedStack, cur) || !this.cachedEnchants.equals(ench) || !this.cachedLevels.equals(lvl) || this.cachedWorking != working) {
         this.cachedStack = cur.copy();
         this.cachedEnchants = new ArrayList<>(ench);
         this.cachedLevels = new ArrayList<>(lvl);
         this.cachedWorking = working;
         this.rebuildAll();
      }
   }

   private static boolean sameStack(ItemStack a, ItemStack b) {
      if (a.isEmpty() && b.isEmpty()) {
         return true;
      } else {
         return a.isEmpty() != b.isEmpty() ? false : ItemStackData.sameItemSameTags(a, b) && a.getCount() == b.getCount();
      }
   }

   private boolean isValidEnchantTarget(ItemStack stack) {
      return !stack.isEmpty() && !stack.isEnchanted() && stack.getCount() == 1 && stack.isEnchantable();
   }

   private void rebuildAll() {
      this.clearWidgets();
      this.cachedChoices.clear();
      ItemStack stack = ((EnchanterMenu)this.menu).enchanter.getItem(0);
      if (this.isValidEnchantTarget(stack) && stack.getEnchantmentValue() != 0) {
         for (ResourceLocation enchantId : EnchantmentManager.getSortedEnchantmentIds()) {
            Holder<Enchantment> ench = this.getEnchantment(enchantId);
            if (ench != null
               && EnchantmentManager.canApply(stack, ench, ((EnchanterMenu)this.menu).enchanter.enchantments, this.minecraft.player.registryAccess())
               && EnchantmentManager.canEnchantmentBeUsed(this.minecraft.player, ench)) {
               int addButtonIndex = this.cachedChoices.size();
               this.cachedChoices.add(new EnchanterScreen.EnchantChoice(addButtonIndex, enchantId, (Enchantment)ench.value()));
               if (this.cachedChoices.size() >= 16) {
                  break;
               }
            }
         }
      }

      this.cachedTwoRows = this.cachedChoices.size() > 8;
      int x = this.leftPos;
      int y = this.topPos;
      EnchanterScreen.StartButton startBtn = new EnchanterScreen.StartButton(this, x + 151, y + 33);
      startBtn.active = !((EnchanterMenu)this.menu).enchanter.enchantments.isEmpty();
      this.addRenderableWidget(startBtn);

      for (int i = 0; i < 16; i++) {
         int z = 0;
         if (i <= 7 && this.cachedTwoRows) {
            z = -24;
         }

         int bx = x + 34 + i % 8 * 16;
         int by = y + 54 + z;
         EnchanterScreen.EnchantChoice ch = i < this.cachedChoices.size() ? this.cachedChoices.get(i) : null;
         EnchanterScreen.EnchantPickButton pickBtn = new EnchanterScreen.EnchantPickButton(this, bx, by, ch);
         pickBtn.active = ch != null;
         this.addRenderableWidget(pickBtn);
      }

      int selectedCount = Math.min(((EnchanterMenu)this.menu).enchanter.enchantments.size(), 36);

      for (int i = 0; i < selectedCount; i++) {
         int yOff = y + i * 26;
         ResourceLocation enchantIdx = ((EnchanterMenu)this.menu).enchanter.enchantments.get(i);
         Holder<Enchantment> ench = this.getEnchantment(enchantIdx);
         if (ench != null) {
            int idx = i;
            EnchanterScreen.FramedEnchantButton framedBtn = new EnchanterScreen.FramedEnchantButton(
               this, x + this.imageWidth + 4, yOff, enchantIdx, (Enchantment)ench.value(), () -> this.clickMenuButton(EnchanterMenu.removeEnchantButtonId(idx))
            );
            framedBtn.active = true;
            this.addRenderableWidget(framedBtn);
            EnchanterScreen.LevelButton minus = new EnchanterScreen.LevelButton(
               x + this.imageWidth + 24, yOff - 4, false, () -> this.clickMenuButton(EnchanterMenu.lowerLevelButtonId(idx))
            );
            minus.active = true;
            this.addRenderableWidget(minus);
            EnchanterScreen.LevelButton plus = new EnchanterScreen.LevelButton(
               x + this.imageWidth + 31, yOff - 4, true, () -> this.clickMenuButton(EnchanterMenu.raiseLevelButtonId(idx))
            );
            plus.active = true;
            this.addRenderableWidget(plus);
         }
      }
   }

   private void clickMenuButton(int id) {
      if (id >= 0) {
         if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(((EnchanterMenu)this.menu).containerId, id);
         }

         this.applyLocalPrediction(id);
         this.rebuildAll();
      }
   }

   private void applyLocalPrediction(int id) {
      if (this.minecraft != null && this.minecraft.player != null) {
         EnchanterBlockEntity enchanter = ((EnchanterMenu)this.menu).enchanter;
         if (id == EnchanterMenu.startButtonId()) {
            if (!enchanter.working && !enchanter.enchantments.isEmpty() && !enchanter.levels.isEmpty()) {
               enchanter.working = true;
            }
         } else if (!enchanter.working) {
            int addIndex = EnchanterMenu.getAddEnchantIndex(id);
            if (addIndex >= 0) {
               if (enchanter.enchantments.size() < 36) {
                  if (addIndex < this.cachedChoices.size()) {
                     ResourceLocation enchantmentId = this.cachedChoices.get(addIndex).enchantId();
                     if (!enchanter.enchantments.contains(enchantmentId)) {
                        Holder<Enchantment> enchantment = this.getEnchantment(enchantmentId);
                        ItemStack stack = enchanter.getItem(0);
                        if (enchantment != null
                           && EnchantmentManager.canApply(stack, enchantment, enchanter.enchantments, this.minecraft.player.registryAccess())
                           && EnchantmentManager.canEnchantmentBeUsed(this.minecraft.player, enchantment)) {
                           enchanter.enchantments.add(enchantmentId);
                           enchanter.levels.add(1);
                           enchanter.recalculateAspectList();
                        }
                     }
                  }
               }
            } else {
               int removeIndex = EnchanterMenu.getRemoveEnchantIndex(id);
               if (removeIndex < 0) {
                  int lowerLevelIndex = EnchanterMenu.getLowerLevelIndex(id);
                  if (lowerLevelIndex >= 0) {
                     this.applyLevelPrediction(lowerLevelIndex, -1, true);
                  } else {
                     int raiseLevelIndex = EnchanterMenu.getRaiseLevelIndex(id);
                     if (raiseLevelIndex >= 0) {
                        this.applyLevelPrediction(raiseLevelIndex, 1, false);
                     }
                  }
               } else if (removeIndex < enchanter.enchantments.size() && removeIndex < enchanter.levels.size()) {
                  enchanter.enchantments.remove(removeIndex);
                  enchanter.levels.remove(removeIndex);
                  enchanter.recalculateAspectList();
               }
            }
         }
      }
   }

   private void applyLevelPrediction(int index, int delta, boolean removeWhenOne) {
      EnchanterBlockEntity enchanter = ((EnchanterMenu)this.menu).enchanter;
      if (index >= 0 && index < enchanter.enchantments.size() && index < enchanter.levels.size()) {
         ResourceLocation enchantmentId = enchanter.enchantments.get(index);
         Holder<Enchantment> enchantment = this.getEnchantment(enchantmentId);
         if (enchantment != null) {
            int nextLevel = enchanter.levels.get(index) + delta;
            if (removeWhenOne && nextLevel < 1) {
               enchanter.enchantments.remove(index);
               enchanter.levels.remove(index);
            } else {
               enchanter.levels.set(index, Math.max(1, Math.min(((Enchantment)enchantment.value()).getMaxLevel(), nextLevel)));
            }

            enchanter.recalculateAspectList();
         }
      }
   }

   private Holder<Enchantment> getEnchantment(ResourceLocation id) {
      if (this.minecraft.level == null) {
         return null;
      } else {
         ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
         return (Holder<Enchantment>)this.minecraft.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
      }
   }

   public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.tooltip.clear();
      this.renderBackground(graphics, mouseX, mouseY, partialTick);
      super.render(graphics, mouseX, mouseY, partialTick);
      this.renderAspectTooltips(mouseX, mouseY);
      if (!this.tooltip.isEmpty()) {
         graphics.renderTooltip(this.font, this.tooltip, Optional.empty(), mouseX, mouseY);
      }

      this.renderTooltip(graphics, mouseX, mouseY);
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(GUI, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
      ItemStack item = ((EnchanterMenu)this.menu).enchanter.getItem(0);
      if (this.isValidEnchantTarget(item)) {
         if (this.cachedTwoRows) {
            graphics.blit(GUI, this.leftPos + 30, this.topPos + 26, 0.0F, this.imageHeight, 147, 24, 256, 256);
         }

         graphics.blit(GUI, this.leftPos + 30, this.topPos + 50, 0.0F, this.imageHeight, 147, 24, 256, 256);
      }

      if (!((EnchanterMenu)this.menu).enchanter.enchantments.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         int baseX = this.leftPos + 40;
         int xo = 15;
         int z = this.cachedTwoRows ? 26 : 50;

         for (Aspect aspect : Aspect.getPrimalAspects()) {
            this.drawAspectBar(graphics, aspect, baseX + xo, this.topPos + z);
            xo += 15;
         }

         RenderSystem.disableBlend();
      }
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
   }

   private void renderAspectTooltips(int mouseX, int mouseY) {
      if (!((EnchanterMenu)this.menu).enchanter.enchantments.isEmpty()) {
         int baseX = this.leftPos + 40;
         int xo = 15;
         int z = this.cachedTwoRows ? 26 : 50;

         for (Aspect aspect : Aspect.getPrimalAspects()) {
            int bx = baseX + xo;
            int by = this.topPos + z;
            int total = ((EnchanterMenu)this.menu).enchanter.totalAspects.getAmount(aspect);
            int size = total == 0 ? 11 : 59;
            if (mouseX > bx && mouseX <= bx + 10 && mouseY > by - size && mouseY <= by) {
               int cur = ((EnchanterMenu)this.menu).enchanter.currentAspects.getAmount(aspect);
               this.tooltip.add(aspect.getName().withStyle(s -> s.withColor(aspect.getColor())));
               this.tooltip.add(Component.literal(cur + "/" + total));
               return;
            }

            xo += 15;
         }
      }
   }

   private void drawAspectBar(GuiGraphics graphics, Aspect aspect, int x, int y) {
      int total = ((EnchanterMenu)this.menu).enchanter.totalAspects.getAmount(aspect);
      int current = ((EnchanterMenu)this.menu).enchanter.currentAspects.getAmount(aspect);
      int size = total == 0 ? 11 : 59;
      if (total == 0) {
         graphics.blit(GUI, x, y - size, 200.0F, 0.0F, 10, 4, 256, 256);
         graphics.blit(GUI, x, y - size + 4, 200.0F, 52.0F, 10, 10, 256, 256);
      } else {
         int pixels = Mth.clamp((int)(48.0 * ((double)current / total)), 0, 48);
         float r = (aspect.getColor() >> 16 & 0xFF) / 255.0F;
         float g = (aspect.getColor() >> 8 & 0xFF) / 255.0F;
         float b = (aspect.getColor() & 0xFF) / 255.0F;
         RenderSystem.setShaderColor(r, g, b, 1.0F);
         if (pixels > 0) {
            graphics.blit(GUI, x + 1, y - size + 4 + 48 - pixels, 210.0F, 48 - pixels, 8, pixels, 256, 256);
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         graphics.blit(GUI, x, y - size, 200.0F, 0.0F, 10, size, 256, 256);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private record EnchantChoice(int addButtonIndex, ResourceLocation enchantId, Enchantment enchantment) {
   }

   @OnlyIn(Dist.CLIENT)
   private static final class EnchantPickButton extends AbstractWidget {
      private final EnchanterScreen parent;
      private final EnchanterScreen.EnchantChoice choice;

      EnchantPickButton(EnchanterScreen parent, int x, int y, EnchanterScreen.EnchantChoice choice) {
         super(x, y, 16, 16, Component.empty());
         this.parent = parent;
         this.choice = choice;
      }

      public void onClick(double mouseX, double mouseY) {
         if (this.active && this.choice != null) {
            this.parent.clickMenuButton(EnchanterMenu.addEnchantButtonId(this.choice.addButtonIndex));
         }
      }

      protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
         if (this.choice != null && this.active) {
            if (EnchantmentManager.enchantmentData.containsKey(this.choice.enchantId)) {
               EnchantmentData data = EnchantmentManager.enchantmentData.get(this.choice.enchantId).get(1);
               if (data != null && data.texture != null) {
                  RenderSystem.setShader(GameRenderer::getPositionTexShader);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  graphics.blit(data.texture, this.getX(), this.getY(), 0.0F, 0.0F, 16, 16, 16, 16);
                  RenderSystem.disableBlend();
                  if (this.isHovered) {
                     List<Component> tip = new ArrayList<>();
                     tip.add(this.choice.enchantment.description().copy().withStyle(ChatFormatting.AQUA));

                     for (Aspect aspect : data.aspects.getAspectsSorted()) {
                        tip.add(
                           Component.literal(" ")
                              .append(aspect.getName().withStyle(s -> s.withColor(aspect.getColor())))
                              .append(Component.literal(" x " + data.aspects.getAmount(aspect) + " ").withStyle(ChatFormatting.RESET))
                              .append(Component.translatable("ttmisc.baseCost"))
                        );
                     }

                     this.parent.tooltip = tip;
                  }
               }
            }
         }
      }

      protected void updateWidgetNarration(@NotNull NarrationElementOutput n) {
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static final class FramedEnchantButton extends AbstractWidget {
      private final EnchanterScreen parent;
      private final ResourceLocation enchantId;
      private final Enchantment enchantment;
      private final Runnable onPress;

      FramedEnchantButton(EnchanterScreen parent, int x, int y, ResourceLocation enchantId, Enchantment enchantment, Runnable onPress) {
         super(x, y, 16, 16, Component.empty());
         this.parent = parent;
         this.enchantId = enchantId;
         this.enchantment = enchantment;
         this.onPress = onPress;
      }

      public void onClick(double mouseX, double mouseY) {
         if (this.active) {
            this.onPress.run();
         }
      }

      protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
         if (!((EnchanterMenu)this.parent.menu).enchanter.enchantments.isEmpty() && !((EnchanterMenu)this.parent.menu).enchanter.levels.isEmpty()) {
            if (EnchantmentManager.enchantmentData.containsKey(this.enchantId)) {
               RenderSystem.setShader(GameRenderer::getPositionTexShader);
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               graphics.blit(EnchanterScreen.GUI, this.getX() - 4, this.getY() - 4, 176.0F, 0.0F, 24, 24, 256, 256);
               EnchantmentData data = EnchantmentManager.enchantmentData.get(this.enchantId).get(1);
               if (data != null && data.texture != null) {
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  graphics.blit(data.texture, this.getX(), this.getY(), 0.0F, 0.0F, 16, 16, 16, 16);
                  RenderSystem.disableBlend();
               }

               int index = ((EnchanterMenu)this.parent.menu).enchanter.enchantments.indexOf(this.enchantId);
               if (index != -1 && index < ((EnchanterMenu)this.parent.menu).enchanter.levels.size()) {
                  int level = ((EnchanterMenu)this.parent.menu).enchanter.levels.get(index);
                  graphics.drawString(this.parent.font, Component.translatable("enchantment.level." + level), this.getX() + 26, this.getY() + 8, 16777215, true);
               }

               if (this.isHovered && data != null) {
                  List<Component> tip = new ArrayList<>();
                  tip.add(this.enchantment.description().copy().withStyle(ChatFormatting.AQUA));

                  for (Aspect aspect : data.aspects.getAspectsSorted()) {
                     tip.add(
                        Component.literal(" ")
                           .append(aspect.getName().withStyle(s -> s.withColor(aspect.getColor())))
                           .append(Component.literal(" x " + data.aspects.getAmount(aspect) + " ").withStyle(ChatFormatting.RESET))
                           .append(Component.translatable("ttmisc.baseCost"))
                     );
                  }

                  if (!((EnchanterMenu)this.parent.menu).enchanter.working) {
                     tip.add(Component.translatable("ttmisc.clickToRemove").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
                  }

                  this.parent.tooltip = tip;
               }
            }
         }
      }

      protected void updateWidgetNarration(@NotNull NarrationElementOutput n) {
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static final class LevelButton extends AbstractWidget {
      private static final int U = 218;
      private static final int W = 7;
      private static final int H = 7;
      private final boolean isPlus;
      private final Runnable onPress;

      LevelButton(int x, int y, boolean isPlus, Runnable onPress) {
         super(x, y, 7, 7, Component.empty());
         this.isPlus = isPlus;
         this.onPress = onPress;
      }

      public void onClick(double mouseX, double mouseY) {
         if (this.active) {
            this.onPress.run();
         }
      }

      protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
         if (this.active) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int uu = 218 + (this.isPlus ? 7 : 0);
            graphics.blit(EnchanterScreen.GUI, this.getX(), this.getY(), uu, 0.0F, 7, 7, 256, 256);
         }
      }

      protected void updateWidgetNarration(@NotNull NarrationElementOutput n) {
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static final class StartButton extends AbstractWidget {
      private final EnchanterScreen parent;

      StartButton(EnchanterScreen parent, int x, int y) {
         super(x, y, 15, 15, Component.empty());
         this.parent = parent;
      }

      public void onClick(double mouseX, double mouseY) {
         if (this.active) {
            this.parent.clickMenuButton(EnchanterMenu.startButtonId());
         }
      }

      protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
         if (this.active) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int v = ((EnchanterMenu)this.parent.menu).enchanter.working ? 39 : 24;
            graphics.blit(EnchanterScreen.GUI, this.getX(), this.getY(), 176.0F, v, 15, 15, 256, 256);
            if (this.isHovered && !((EnchanterMenu)this.parent.menu).enchanter.working) {
               this.parent.tooltip.add(Component.translatable("ttmisc.startEnchant").withStyle(ChatFormatting.AQUA));
            }
         }
      }

      protected void updateWidgetNarration(@NotNull NarrationElementOutput n) {
      }
   }
}
