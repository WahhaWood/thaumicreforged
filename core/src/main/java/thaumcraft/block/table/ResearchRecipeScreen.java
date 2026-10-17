package thaumcraft.block.table;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.api.research.page.AspectPage;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.relics.thaumonomicon.render.ResearchBrowserScreen;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ScanManager;
import thaumcraft.util.InventoryUtils;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class ResearchRecipeScreen extends Screen {
   public static final ResourceLocation RESEARCH_BOOK = Thaumcraft.location("textures/screen/research_book.png");
   public static final ResourceLocation RESEARCH_BOOK_OVERLAY = Thaumcraft.location("textures/screen/research_book_overlay.png");
   public static LinkedList<Object[]> history = new LinkedList<>();
   public static ConcurrentHashMap<Integer, ItemStack> cache = new ConcurrentHashMap<>();
   private static boolean cacheInitialized;
   protected int paneWidth = 256;
   protected int paneHeight = 181;
   protected double guiMapX;
   protected double guiMapY;
   private ResearchItem research;
   private ResearchPage[] pages;
   private int page;
   private int maxPages;
   public HashMap<Aspect, ArrayList<ItemStack>> aspectItems = new HashMap<>();
   ArrayList<List> reference = new ArrayList<>();
   private List<Component> tooltips;
   private int tooltipX;
   private int tooltipY;

   public static synchronized void putToCache(int n, ItemStack itemStack) {
      cache.put(n, itemStack);
   }

   public static synchronized ItemStack getFromCache(int n) {
      return cache.get(n);
   }

   public static synchronized void cacheScannedStack(ItemStack stack) {
      if (!stack.isEmpty()) {
         ItemStack copy = stack.copy();
         copy.setCount(1);
         putToCache(ScanManager.generateItemHash(copy), copy);
      }
   }

   private static synchronized void ensureCacheInitialized(Player player) {
      if (!cacheInitialized && player != null) {
         CreativeModeTabs.tryRebuildTabContents(player.level().enabledFeatures(), player.canUseGameMasterBlocks(), player.level().registryAccess());

         for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            cacheStacks(tab.getDisplayItems());
            cacheStacks(tab.getSearchTabDisplayItems());
         }

         cacheInitialized = true;
      }
   }

   private static void cacheStacks(Iterable<ItemStack> stacks) {
      for (ItemStack stack : stacks) {
         cacheScannedStack(stack);
      }
   }

   public ResearchRecipeScreen(ResearchItem research, int page, double guiMapX, double guiMapY) {
      super(Component.empty());
      if (research == null) {
         // Ссылка в никуда (история/кликтухроу на удалённое исследование):
         // вместо краша показываем пустую книгу.
         research = ResearchApi.getResearch("aspects");
         if (research == null) {
            this.pages = new ResearchPage[0];
            this.maxPages = 0;
            this.page = 0;
            this.research = null;
            return;
         }
      }

      this.research = research;
      this.guiMapX = guiMapX;
      this.guiMapY = guiMapY;
      this.minecraft = Minecraft.getInstance();
      this.pages = research.getPages();
      if (research.key.equalsIgnoreCase("aspects")) {
         ensureCacheInitialized(this.minecraft.player);
         PlayerKnowledge knowledge = PlayerKnowledge.of(this.minecraft.player);
         AspectList aspectsDiscovered = knowledge.getAspectsDiscovered();
         HashSet<String> list3 = knowledge.getObjectsScanned();
         if (list3.size() > 0) {
            for (String s : list3) {
               try {
                  ItemStack fromCache = getFromCache(Integer.parseInt(s.substring(1)));
                  if (fromCache != null) {
                     AspectList bonusTags = ThaumcraftCraftingManager.getBonusTags(fromCache, ThaumcraftCraftingManager.getObjectTags(fromCache));
                     if (bonusTags.size() > 0) {
                        for (Aspect aspect : bonusTags.getAspects()) {
                           ArrayList<ItemStack> list4 = this.aspectItems.get(aspect);
                           if (list4 == null) {
                              list4 = new ArrayList<>();
                           }

                           ItemStack copy = fromCache.copy();
                           copy.setCount(bonusTags.getAmount(aspect));
                           list4.add(copy);
                           this.aspectItems.put(aspect, list4);
                        }
                     }
                  }
               } catch (NumberFormatException var20) {
               }
            }
         }

         ArrayList<ResearchPage> list5 = new ArrayList<>();
         ResearchPage[] pages = research.getPages();
         int length2 = pages.length;

         for (int j = 0; j < length2; j++) {
            list5.add(pages[j]);
         }

         AspectList list6 = new AspectList();
         int n = 0;

         for (Aspect aspect2 : aspectsDiscovered.getAspectsSorted()) {
            if (n <= 4) {
               n++;
               list6.add(aspect2, aspectsDiscovered.getAmount(aspect2));
            }

            if (n == 4) {
               n = 0;
               list5.add(new AspectPage(list6.copy()));
               list6 = new AspectList();
            }
         }

         if (n > 0) {
            list5.add(new AspectPage(list6));
         }

         this.pages = list5.toArray(this.pages);
      }

      this.maxPages = this.pages.length;
      if (page % 2 == 1) {
         page--;
      }

      this.page = page;
   }

   public boolean keyPressed(int key, int scanCode, int modifiers) {
      if (key != this.minecraft.options.keyInventory.getKey().getValue() && key != 256) {
         return super.keyPressed(key, scanCode, modifiers);
      } else {
         history.clear();
         this.minecraft.setScreen(new ResearchBrowserScreen(this.minecraft.player, this.guiMapX, this.guiMapY));
         return true;
      }
   }

   public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float pt) {
      super.renderBackground(graphics, mouseX, mouseY, pt);
      this.genResearchBackground(graphics, mouseX, mouseY, pt);
      int n4 = (this.width - this.paneWidth) / 2;
      int n5 = (this.height - this.paneHeight) / 2;
      if (!history.isEmpty()) {
         int n6 = mouseX - (n4 + 118);
         int n7 = mouseY - (n5 + 189);
         if (n6 >= 0 && n7 >= 0 && n6 < 20 && n7 < 12) {
            graphics.drawString(this.font, Component.translatable("recipe.return"), mouseX, mouseY, 16777215);
         }
      }
   }

   protected void genResearchBackground(GuiGraphics graphics, int mouseX, int mouseY, float pt) {
      PoseStack pose = graphics.pose();
      int x = (this.width - this.paneWidth) / 2;
      int y = (this.height - this.paneHeight) / 2;
      float borderX = (this.width - this.paneWidth * 1.3F) / 2.0F;
      float borderY = (this.height - this.paneHeight * 1.3F) / 2.0F;
      RenderSystem.enableBlend();
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      pose.pushPose();
      pose.translate(borderX, borderY, 0.0F);
      pose.scale(1.3F, 1.3F, 1.0F);
      graphics.blit(RESEARCH_BOOK, 0, 0, 0, 0, this.paneWidth, this.paneHeight);
      pose.popPose();
      this.reference.clear();
      this.tooltips = null;
      int pageIndex = 0;

      for (ResearchPage researchPage : this.pages) {
         if ((pageIndex == this.page || pageIndex == this.page + 1) && pageIndex < this.maxPages) {
            this.drawPage(graphics, researchPage, pageIndex % 2, x, y, mouseX, mouseY);
         }

         if (++pageIndex > this.page + 1) {
            break;
         }
      }

      if (this.tooltips != null && !this.tooltips.isEmpty()) {
         graphics.renderComponentTooltip(this.font, this.tooltips, this.tooltipX, this.tooltipY);
      }

      float n9 = Mth.sin(this.minecraft.player.tickCount / 3.0F) * 0.2F + 0.1F;
      if (!history.isEmpty()) {
         RenderSystem.enableBlend();
         this.drawTexturedModalRectScaled(graphics, RESEARCH_BOOK, x + 118, y + 189, 38, 202, 20, 12, n9);
      }

      if (this.page > 0) {
         RenderSystem.enableBlend();
         this.drawTexturedModalRectScaled(graphics, RESEARCH_BOOK, x - 16, y + 190, 0, 184, 12, 8, n9);
      }

      if (this.page < this.maxPages - 2) {
         RenderSystem.enableBlend();
         this.drawTexturedModalRectScaled(graphics, RESEARCH_BOOK, x + 262, y + 190, 12, 184, 12, 8, n9);
      }
   }

   private void drawPage(GuiGraphics graphics, ResearchPage researchPage, int page, int x, int y, int mouseX, int mouseY) {
      if (this.page == 0 && page == 0) {
         graphics.blit(RESEARCH_BOOK, x + 4, y - 13, 24, 184, 96, 4);
         graphics.blit(RESEARCH_BOOK, x + 4, y + 4, 24, 184, 96, 4);
         String title = this.research.getName().getString();
         if (ScreenApi.MAGIC.isOriginalMode() && !ScreenApi.MAGIC.isBitmapOriginalAvailable()) {
            int stringWidth = this.font.width(title);
            if (stringWidth <= 130) {
               graphics.drawString(this.font, title, x + 52 - stringWidth / 2, y - 6, 3158064, false);
            } else {
               float scale = 130.0F / stringWidth;
               PoseStack pose = graphics.pose();
               pose.pushPose();
               pose.translate(x + 52 - stringWidth / 2.0F * scale, y - 6.0F * scale, 0.0F);
               pose.scale(scale, scale, 1.0F);
               graphics.drawString(this.font, title, 0, 0, 3158064, false);
               pose.popPose();
            }
         } else {
            float stringWidth = ScreenApi.MAGIC.width(title, 0.15F);
            float scale = 0.15F * (stringWidth <= 130.0F ? 1.0F : 130.0F / stringWidth);
            ScreenApi.MAGIC.renderCenterText(graphics, title, x + 52, y - 9.5F, scale, 3158064);
         }

         y += 25;
      }

      try {
         researchPage.render(this, graphics, this.font, page, x + page * 152, y, mouseX, mouseY);
      } catch (Exception var12) {
         // Листание книги не должно ронять игру: битые страницы (нет рецепта,
         // кривой IMG-тег, ссылка в никуда) рисуются пустыми, а не крашат.
         // NPE здесь ловили и раньше; расширяем на всё остальное.
         var12.fillInStackTrace();
         thaumcraft.Thaumcraft.log.error("[Thaumcraft] Failed to render research page", var12);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      Player player = this.minecraft.player;
      Vec3 pos = player.position();
      Level level = player.level();
      int n4 = (this.width - this.paneWidth) / 2;
      int n5 = (this.height - this.paneHeight) / 2;
      double n6 = mouseX - (n4 + 261);
      double n7 = mouseY - (n5 + 189);
      if (this.page < this.maxPages - 2 && n6 >= 0.0 && n7 >= 0.0 && n6 < 14.0 && n7 < 10.0) {
         this.page += 2;
         level.playLocalSound(pos.x, pos.y, pos.z, (SoundEvent)ModSounds.PAGE.get(), SoundSource.MASTER, 0.66F, 1.0F, false);
         return true;
      } else {
         double n8 = mouseX - (n4 - 17);
         double n9 = mouseY - (n5 + 189);
         if (this.page >= 2 && n8 >= 0.0 && n9 >= 0.0 && n8 < 14.0 && n9 < 10.0) {
            this.page -= 2;
            level.playLocalSound(pos.x, pos.y, pos.z, (SoundEvent)ModSounds.PAGE.get(), SoundSource.MASTER, 0.66F, 1.0F, false);
            return true;
         } else {
            if (!history.isEmpty()) {
               double n10 = mouseX - (n4 + 118);
               double n11 = mouseY - (n5 + 189);
               if (n10 >= 0.0 && n11 >= 0.0 && n10 < 20.0 && n11 < 12.0) {
                  level.playLocalSound(pos.x, pos.y, pos.z, (SoundEvent)ModSounds.PAGE.get(), SoundSource.MASTER, 0.66F, 1.0F, false);
                  Object[] array = history.pop();
                  ResearchItem back = ResearchApi.getResearch((String)array[0]);
                  // История могла вести на исследование, которого больше нет
                  // (удалено датапаком/KubeJS): молча остаёмся на месте.
                  if (back == null) {
                     return true;
                  }

                  this.minecraft.setScreen(new ResearchRecipeScreen(back, (Integer)array[1], this.guiMapX, this.guiMapY));
                  return true;
               }
            }

            if (this.reference.size() > 0) {
               for (List list : this.reference) {
                  if (mouseX >= ((Integer)list.get(0)).intValue()
                     && mouseY >= ((Integer)list.get(1)).intValue()
                     && mouseX < (Integer)list.get(0) + 16
                     && mouseY < (Integer)list.get(1) + 16) {
                     level.playLocalSound(pos.x, pos.y, pos.z, (SoundEvent)ModSounds.PAGE.get(), SoundSource.MASTER, 0.66F, 1.0F, false);
                     ResearchItem target = ResearchApi.getResearch((String)list.get(2));
                     if (target == null || this.research == null) {
                        return true;
                     }

                     history.push(new Object[]{this.research.key, this.page});
                     this.minecraft.setScreen(new ResearchRecipeScreen(target, (Integer)list.get(3), this.guiMapX, this.guiMapY));
                     return true;
                  }
               }
            }

            return super.mouseClicked(mouseX, mouseY, button);
         }
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void drawTexturedModalRectScaled(GuiGraphics graphics, ResourceLocation location, int x, int y, int u, int v, int width, int height, float scale) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(x + width / 2.0F, y + height / 2.0F, 0.0F);
      pose.scale(1.0F + scale, 1.0F + scale, 1.0F);
      ScreenApi.renderTextureRect(graphics, location, -width / 2.0F, -height / 2.0F, u, v, width, height);
      pose.popPose();
   }

   public void renderItemAndTooltips(GuiGraphics graphics, int mouseX, int mouseY, Ingredient ingredient, int x, int y) {
      this.renderItemAndTooltips(graphics, mouseX, mouseY, InventoryUtils.cycleItemStack(ingredient), x, y);
   }

   public void renderItemAndTooltips(GuiGraphics graphics, int mouseX, int mouseY, Ingredient ingredient, int x, int y, boolean reference) {
      this.renderItemAndTooltips(graphics, mouseX, mouseY, InventoryUtils.cycleItemStack(ingredient), x, y, reference);
   }

   public void renderItemAndTooltips(GuiGraphics graphics, int mouseX, int mouseY, ItemStack stack, int x, int y) {
      this.renderItemAndTooltips(graphics, mouseX, mouseY, stack, x, y, true);
   }

   public void renderItemAndTooltips(GuiGraphics graphics, int mouseX, int mouseY, ItemStack stack, int x, int y, boolean reference) {
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.renderItem(stack, x, y);
      graphics.renderItemDecorations(this.font, stack, x, y);
      if (mouseX >= x && mouseY >= y && mouseX < x + 16 && mouseY < y + 16) {
         List<Component> tooltip = stack.getTooltipLines(
            TooltipContext.of(this.minecraft.level),
            this.minecraft.player,
            this.minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL
         );
         if (reference) {
            Object[] recipeReference = ThaumcraftApi.getCraftingRecipeKey(this.minecraft.player, stack);
            if (recipeReference != null && !recipeReference[0].equals(this.research.key)) {
               tooltip.add(Component.translatable("recipe.clickthrough").setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_GRAY)));
               this.reference.add(Arrays.asList(mouseX, mouseY, (String)recipeReference[0], (Integer)recipeReference[1]));
            }
         }

         this.setTooltipNextRender(tooltip, mouseX, mouseY);
      }
   }

   public void renderAspectAndTooltips(GuiGraphics graphics, int mouseX, int mouseY, Aspect aspect, int amount, int x, int y) {
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      UtilsFX.drawTag(graphics, x, y, aspect, amount, 0, 0.0F);
      if (mouseX >= x && mouseY >= y && mouseX < x + 16 && mouseY < y + 16) {
         this.setTooltipNextRender(Arrays.asList(aspect.getName(), aspect.getLocalizedDescription()), mouseX, mouseY - 8);
      }
   }

   public void setTooltipNextRender(List<Component> tooltips, int x, int y) {
      this.tooltips = tooltips;
      this.tooltipX = x;
      this.tooltipY = y;
   }
}
