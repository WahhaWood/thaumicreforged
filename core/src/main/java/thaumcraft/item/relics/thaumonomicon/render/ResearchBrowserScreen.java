package thaumcraft.item.relics.thaumonomicon.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.block.table.ResearchRecipeScreen;
import thaumcraft.client.config.BookFontMode;
import thaumcraft.client.config.ThaumcraftClientConfig;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.playerdata.PacketPlayerCompleteToServer;
import thaumcraft.particles.fx.ParticleUtils;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchManager;
import thaumcraft.util.InventoryUtils;
import thaumcraft.util.ThaumcraftRenderTypes;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class ResearchBrowserScreen extends Screen {
   private static final int TEX_W = 256;
   private static final int TEX_H = 230;
   private static final int BORDER_W = 16;
   private static final int BORDER_H = 17;
   private static final int TAB_W = 24;
   private static final int TAB_STEP = 24;
   private static final int NAV_BTN_W = 24;
   private static final int NAV_BTN_H = 8;
   private static final int SEARCH_W = 140;
   private static final int SEARCH_H = 180;
   private static final int FONT_BUTTON_W = 17;
   private static final int FONT_BUTTON_H = 16;
   public static final ResourceLocation TEX_RESEARCH = Thaumcraft.location("textures/screen/research.png");
   public static final ResourceLocation NODES_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/nodes.png");
   private static final int[][] PARAMS = new int[][]{{0, 0, 0, 1}, {0, 1, 1, -1}, {1, -1, 1, 0}};
   public static double lastMapX = -202.5;
   public static double lastMapY = -214.5;
   public static ArrayList<String> highlightedItem = new ArrayList<>();
   private static String selectedCategory = null;
   protected int paneWidth = 256;
   protected int paneHeight = 230;
   private int paddingX = 16;
   private int paddingY = 17;
   private int mapViewW = 224;
   private int mapViewH = 196;
   private int guiLeft;
   private int guiTop;
   private int minY;
   private int maxY;
   private int maxX;
   private int minX;
   protected double mapX;
   protected double mapY;
   private final LinkedList<ResearchItem> research = new LinkedList<>();
   private ResearchItem currentHighlight;
   long popuptime;
   Component popupmessage = Component.empty();
   public boolean hasScribestuff;
   @NotNull
   public final PlayerKnowledge knowledge;
   @NotNull
   public final HashSet<String> completed;
   private int currentPageIndex = 0;
   private int maxPageIndex = 0;
   private boolean recalcTabs = true;
   private final LinkedHashMap<String, ResearchCategoryList> currentPageTabs = new LinkedHashMap<>();
   private ResearchBrowserScreen.PageButton btnPrev;
   private ResearchBrowserScreen.PageButton btnNext;
   private EditBox searchBox;
   private int searchScroll = 0;
   private List<ResearchItem> searchResults = new ArrayList<>();
   private boolean progressHover = false;
   private String progressHoverText = null;

   public ResearchBrowserScreen(Player player) {
      this(player, lastMapX, lastMapY);
   }

   public ResearchBrowserScreen(Player player, double mapX, double mapY) {
      super(Component.empty());
      this.knowledge = PlayerKnowledge.of(player);
      this.completed = this.knowledge.getResearchCompleted();
      this.currentHighlight = null;
      this.popuptime = 0L;
      this.hasScribestuff = false;
      this.mapX = mapX;
      this.mapY = mapY;
      this.updateResearch();
   }

   public static void resetSelectedCategory() {
      selectedCategory = null;
   }

   protected void init() {
      super.init();
      this.recalcPanelSize();
      this.updateResearch();
      this.updateMaxPageIndex();
      this.rebuildTabCache();
      this.clearWidgets();
      int x1 = this.guiLeft + 16 + 5;
      int x2 = this.guiLeft + this.paneWidth - 16 - 5 - 1 - 24;
      int y = this.guiTop + this.paneHeight - 8 - 4;
      this.btnPrev = new ResearchBrowserScreen.PageButton(x1, y, true);
      this.btnNext = new ResearchBrowserScreen.PageButton(x2, y, false);
      this.addRenderableWidget(this.btnPrev);
      this.addRenderableWidget(this.btnNext);
      this.initSearchIfFits();
      this.tick();
   }

   public void resize(Minecraft mc, int w, int h) {
      super.resize(mc, w, h);
   }

   private void recalcPanelSize() {
      int maxW = Math.max(256, this.width - 48);
      int maxH = Math.max(230, this.height - 34);
      this.paneWidth = Mth.clamp(maxW, 256, this.width - 48);
      this.paneHeight = Mth.clamp(maxH, 230, this.height - 10);
      this.mapViewW = this.paneWidth - this.paddingX * 2;
      this.mapViewH = this.paneHeight - this.paddingY * 2;
      this.guiLeft = (this.width - this.paneWidth) / 2;
      this.guiTop = (this.height - this.paneHeight) / 2;
   }

   private void initSearchIfFits() {
      int x = this.getSearchAreaX();
      int y = this.getSearchAreaY();
      int w = this.getSearchAreaW();
      if (this.mapViewW >= 190 && this.mapViewH >= 80) {
         this.searchBox = new EditBox(this.font, x + 4, y + 4, w - 8, 12, Component.translatable("thaumcraft.gui.search"));
         this.searchBox.setBordered(false);
         this.searchBox.setTextColor(16777215);
         this.searchBox.setMaxLength(40);
         this.searchBox.setResponder(s -> {
            this.searchScroll = 0;
            this.searchResults = this.buildSearchResults(s);
         });
         this.addRenderableWidget(this.searchBox);
         this.searchResults = this.buildSearchResults(this.searchBox.getValue());
      } else {
         this.searchBox = null;
         this.searchResults = List.of();
         this.searchScroll = 0;
      }
   }

   private int getSearchAreaX() {
      return this.guiLeft + this.paneWidth - 16 - 140 - 4;
   }

   private int getSearchAreaW() {
      return 140;
   }

   private int getSearchAreaY() {
      return this.guiTop + 17 + 2;
   }

   private int getSearchAreaH() {
      if (this.searchBox != null && !this.searchBox.getValue().isEmpty() && !this.searchResults.isEmpty()) {
         int maxAvailableH = Math.min(this.mapViewH - 4, 180);
         int visibleRows = Math.min(this.searchResults.size(), (maxAvailableH - 22) / 11);
         return 22 + visibleRows * 11 + 2;
      } else {
         return 20;
      }
   }

   private int getSearchRows() {
      int maxAvailableH = Math.min(this.mapViewH - 4, 180);
      int usable = maxAvailableH - 22;
      return Math.max(0, usable / 11);
   }

   private List<ResearchItem> buildSearchResults(String query) {
      if (query == null) {
         return List.of();
      } else {
         final String q = query.trim().toLowerCase(Locale.ROOT);
         return q.isEmpty()
            ? List.of()
            : this.research.stream().filter(Objects::nonNull).filter(ri -> !ri.isVirtual()).filter(this::isResearchVisibleForPlayer).filter(ri -> {
               String name = safeLower(ri.getName().getString());
               String key = safeLower(ri.key);
               return name.contains(q) || key.contains(q);
            }).sorted(Comparator.comparing(ri -> ri.getName().getString(), String.CASE_INSENSITIVE_ORDER)).limit(200L).collect(Collectors.toList());
      }
   }

   private static String safeLower(String s) {
      return s == null ? "" : s.toLowerCase(Locale.ROOT);
   }

   public void updateResearch() {
      if (this.minecraft == null) {
         this.minecraft = Minecraft.getInstance();
      }

      this.research.clear();
      this.hasScribestuff = false;
      if (selectedCategory == null) {
         selectedCategory = ResearchApi.getResearchLists().keySet().iterator().next();
      }

      ResearchCategoryList categoryList = ResearchApi.getResearchList(selectedCategory);
      this.research.addAll(categoryList.research.values());
      if (ResearchManager.consumeInkFromPlayer(this.minecraft.player, false)
         && InventoryUtils.isPlayerCarrying(this.minecraft.player, new ItemStack(Items.PAPER)) >= 0) {
         this.hasScribestuff = true;
      }

      this.updateMapBounds(categoryList);
      this.clampMapPosition();
      this.recalcTabs = true;
      this.updateMaxPageIndex();
      if (this.searchBox != null) {
         this.searchScroll = 0;
         this.searchResults = this.buildSearchResults(this.searchBox.getValue());
      }
   }

   private void updateMapBounds(ResearchCategoryList categoryList) {
      if (categoryList != null && !categoryList.research.isEmpty()) {
         int left = categoryList.minColumn * 24;
         int right = categoryList.maxColumn * 24 + 22;
         int top = categoryList.minRow * 24;
         int bottom = categoryList.maxRow * 24 + 22;
         if (right - left <= this.mapViewW) {
            int centeredX = Math.round((left + right - this.mapViewW) / 2.0F);
            this.minX = centeredX;
            this.maxX = centeredX;
         } else {
            float fx = this.mapViewW / 224.0F;
            this.minX = categoryList.minColumn * 24 - Math.round(85.0F * fx);
            this.maxX = categoryList.maxColumn * 24 - Math.round(112.0F * fx);
            if (this.minX > this.maxX) {
               int centeredX = Math.round((left + right - this.mapViewW) / 2.0F);
               this.minX = centeredX;
               this.maxX = centeredX;
            }
         }

         if (bottom - top <= this.mapViewH) {
            int centeredY = Math.round((top + bottom - this.mapViewH) / 2.0F);
            this.minY = centeredY;
            this.maxY = centeredY;
         } else {
            float fy = this.mapViewH / 196.0F;
            this.minY = categoryList.minRow * 24 - Math.round(112.0F * fy);
            this.maxY = categoryList.maxRow * 24 - Math.round(61.0F * fy);
            if (this.minY > this.maxY) {
               int centeredY = Math.round((top + bottom - this.mapViewH) / 2.0F);
               this.minY = centeredY;
               this.maxY = centeredY;
            }
         }
      } else {
         int centeredX = Math.round((22 - this.mapViewW) / 2.0F);
         int centeredY = Math.round((22 - this.mapViewH) / 2.0F);
         this.minX = centeredX;
         this.maxX = centeredX;
         this.minY = centeredY;
         this.maxY = centeredY;
      }
   }

   private void clampMapPosition() {
      this.mapX = clampAxis(this.mapX, this.minX, this.maxX);
      this.mapY = clampAxis(this.mapY, this.minY, this.maxY);
   }

   private static double clampAxis(double value, int min, int max) {
      return min >= max ? min : Mth.clamp(value, min, max);
   }

   private boolean isCategorySelectable(String categoryKey, ResearchCategoryList categoryList) {
      if (categoryKey == null || categoryList == null) {
         return false;
      } else {
         return categoryKey.equals("eldritch") && !this.isEldritchUnlocked() ? false : !categoryList.research.isEmpty();
      }
   }

   private String findFallbackCategory() {
      String firstVisible = null;

      for (Entry<String, ResearchCategoryList> entry : ResearchApi.getResearchLists().entrySet()) {
         String key = entry.getKey();
         ResearchCategoryList value = entry.getValue();
         if (!key.equals("eldritch") || this.isEldritchUnlocked()) {
            if (firstVisible == null) {
               firstVisible = key;
            }

            if (value != null && !value.research.isEmpty()) {
               return key;
            }
         }
      }

      return firstVisible;
   }

   public void removed() {
      super.removed();
      lastMapX = this.mapX;
      lastMapY = this.mapY;
   }

   public boolean isPauseScreen() {
      return false;
   }

   protected void renderBlurredBackground(float partialTick) {
   }

   public boolean keyPressed(int key, int scanCode, int modifiers) {
      if (this.searchBox != null && this.searchBox.isFocused()) {
         if (key == 256) {
            this.searchBox.setFocused(false);
            return true;
         } else {
            return this.searchBox.keyPressed(key, scanCode, modifiers);
         }
      } else if (this.minecraft != null && key == this.minecraft.options.keyInventory.getKey().getValue()) {
         highlightedItem.clear();
         this.minecraft.setScreen(null);
         return true;
      } else {
         if (key == 1) {
            highlightedItem.clear();
         }

         return super.keyPressed(key, scanCode, modifiers);
      }
   }

   public void tick() {
      this.clampMapPosition();
      if (this.minecraft != null && this.minecraft.player != null && this.minecraft.player.tickCount % 10 == 0) {
         this.updateMaxPageIndex();
      }

      if (this.btnPrev != null) {
         this.btnPrev.setVisible(this.currentPageIndex > 0);
      }

      if (this.btnNext != null) {
         this.btnNext.setVisible(this.currentPageIndex < this.maxPageIndex);
      }
   }

   public boolean mouseDragged(double mx, double my, int button, double moveX, double moveY) {
      if (button == 0) {
         int browseX = this.guiLeft + this.paddingX;
         int browseY = this.guiTop + this.paddingY;
         if (mx >= browseX && mx < browseX + this.mapViewW && my >= browseY && my < browseY + this.mapViewH && !this.isMouseInSearchPanel(mx, my)) {
            this.mapX -= moveX;
            this.mapY -= moveY;
            this.tick();
            return true;
         }
      }

      return super.mouseDragged(mx, my, button, moveX, moveY);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.searchBox != null && this.isMouseInSearchList(mouseX, mouseY) && !this.searchResults.isEmpty()) {
         int rows = this.getSearchRows();
         int maxScroll = Math.max(0, this.searchResults.size() - rows);
         if (scrollY < 0.0) {
            this.searchScroll = Math.min(maxScroll, this.searchScroll + 1);
         } else {
            this.searchScroll = Math.max(0, this.searchScroll - 1);
         }

         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }
   }

   private boolean isMouseInSearchPanel(double mx, double my) {
      if (this.searchBox == null) {
         return false;
      } else {
         int x = this.getSearchAreaX();
         int y = this.getSearchAreaY();
         int w = this.getSearchAreaW();
         int h = this.getSearchAreaH();
         return mx >= x && mx < x + w && my >= y && my < y + h;
      }
   }

   private boolean isMouseInSearchList(double mx, double my) {
      if (this.searchBox != null && !this.searchBox.getValue().isEmpty()) {
         int x = this.getSearchAreaX();
         int y = this.getSearchAreaY();
         int w = this.getSearchAreaW();
         int h = this.getSearchAreaH();
         return mx >= x && mx < x + w && my >= y + 20 && my < y + h;
      } else {
         return false;
      }
   }

   private boolean isResearchDebugEnabled() {
      return this.minecraft != null && this.minecraft.player != null && this.minecraft.player.hasPermissions(2);
   }

   private void copyDebugKey(String type, String key) {
      if (this.minecraft != null && key != null && !key.isEmpty()) {
         this.minecraft.keyboardHandler.setClipboard(key);
         if (this.minecraft.player != null) {
            this.minecraft.player.displayClientMessage(Component.literal("Copied " + type + " key: " + key), true);
         }

         this.playButtonClick();
      }
   }

   private void debugCompleteResearch(ResearchItem researchItem) {
      if (researchItem != null) {
         PacketHandler.sendToServer(new PacketPlayerCompleteToServer(researchItem.key, (byte)2));
         this.playButtonClick();
      }
   }

   private String getTabKeyAt(double mouseX, double mouseY) {
      int mx = (int)mouseX;
      int my = (int)mouseY;
      int tabsPerSide = this.getTabPerSide();
      List<String> keys = new ArrayList<>(this.getTabsOnCurrentPage());

      for (int i = 0; i < keys.size(); i++) {
         int row = i % tabsPerSide;
         boolean right = i >= tabsPerSide;
         int tx = right ? this.guiLeft + this.paneWidth : this.guiLeft - 24;
         int ty = this.guiTop + row * 24;
         if (mx >= tx && mx < tx + 24 && my >= ty && my < ty + 24) {
            return keys.get(i);
         }
      }

      return null;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         this.popuptime = System.currentTimeMillis() - 1L;
         if (button == 0 && this.isOverFontButton(mouseX, mouseY)) {
            ThaumcraftClientConfig.toggleBookFontMode();
            this.playButtonClick();
            return true;
         } else {
            if (this.isResearchDebugEnabled() && hasShiftDown()) {
               ResearchItem searchHit = this.getClickedSearchResult(mouseX, mouseY);
               if (searchHit != null) {
                  if (button == 0) {
                     this.copyDebugKey("research", searchHit.key);
                     return true;
                  }

                  if (button == 1) {
                     this.debugCompleteResearch(searchHit);
                     return true;
                  }
               }

               if (this.currentHighlight != null && !this.isMouseInSearchPanel(mouseX, mouseY)) {
                  if (button == 0) {
                     this.copyDebugKey("research", this.currentHighlight.key);
                     return true;
                  }

                  if (button == 1) {
                     this.debugCompleteResearch(this.currentHighlight);
                     return true;
                  }
               }

               String tabKey = this.getTabKeyAt(mouseX, mouseY);
               if (tabKey != null && button == 0) {
                  this.copyDebugKey("category", tabKey);
                  return true;
               }
            }

            if (this.searchBox != null && button == 0) {
               ResearchItem hit = this.getClickedSearchResult(mouseX, mouseY);
               if (hit != null) {
                  this.minecraft.setScreen(new ResearchRecipeScreen(hit, 0, this.mapX, this.mapY));
                  return true;
               }

               if (this.isMouseInSearchPanel(mouseX, mouseY)) {
                  return true;
               }
            }

            if (this.currentHighlight != null && !this.completed.contains(this.currentHighlight.key) && this.canUnlockResearch(this.currentHighlight)) {
               this.updateResearch();
               if (this.currentHighlight.tags != null
                  && this.currentHighlight.tags.size() > 0
                  && (Config.researchDifficulty == -1 || Config.researchDifficulty == 0 && this.currentHighlight.isSecondary())) {
                  boolean ok = true;

                  for (Aspect aspect : this.currentHighlight.tags.getAspects()) {
                     if (this.knowledge.getAspectPoolFor(aspect) < this.currentHighlight.tags.getAmount(aspect)) {
                        ok = false;
                        break;
                     }
                  }

                  if (ok) {
                     PacketHandler.sendToServer(new PacketPlayerCompleteToServer(this.currentHighlight.key, (byte)0));
                     return true;
                  }
               } else if (this.hasScribestuff && ResearchManager.getResearchSlot(this.minecraft.player, this.currentHighlight.key) == -1) {
                  PacketHandler.sendToServer(new PacketPlayerCompleteToServer(this.currentHighlight.key, (byte)1));
                  this.popuptime = System.currentTimeMillis() + 3000L;
                  this.popupmessage = Component.translatable("tc.research.popup", new Object[]{this.currentHighlight.getName()});
                  return true;
               }
            } else if (this.currentHighlight != null && this.completed.contains(this.currentHighlight.key)) {
               this.minecraft.setScreen(new ResearchRecipeScreen(this.currentHighlight, 0, this.mapX, this.mapY));
               return true;
            }

            int mx = (int)mouseX;
            int my = (int)mouseY;
            int tabsPerSide = this.getTabPerSide();
            List<String> keys = new ArrayList<>(this.getTabsOnCurrentPage());

            for (int i = 0; i < keys.size(); i++) {
               int row = i % tabsPerSide;
               boolean right = i >= tabsPerSide;
               int tx = right ? this.guiLeft + this.paneWidth : this.guiLeft - 24;
               int ty = this.guiTop + row * 24;
               if (mx >= tx && mx < tx + 24 && my >= ty && my < ty + 24) {
                  selectedCategory = keys.get(i);
                  this.updateResearch();
                  this.playButtonClick();
                  return true;
               }
            }

            return false;
         }
      }
   }

   private ResearchItem getClickedSearchResult(double mouseX, double mouseY) {
      if (!this.isMouseInSearchList(mouseX, mouseY)) {
         return null;
      } else {
         int x = this.getSearchAreaX();
         int y = this.getSearchAreaY();
         int w = this.getSearchAreaW();
         int rows = this.getSearchRows();
         if (rows <= 0) {
            return null;
         } else {
            int relY = (int)mouseY - (y + 20);
            int row = relY / 11;
            if (row >= 0 && row < rows) {
               int idx = this.searchScroll + row;
               if (idx >= 0 && idx < this.searchResults.size()) {
                  return !(mouseX < x + 2) && !(mouseX > x + w - 2) ? this.searchResults.get(idx) : null;
               } else {
                  return null;
               }
            } else {
               return null;
            }
         }
      }
   }

   public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float pt) {
      this.renderBackground(graphics, mouseX, mouseY, pt);
      this.genResearchBackground(graphics, mouseX, mouseY, pt);
      if (this.popuptime > System.currentTimeMillis()) {
         int cx = this.guiLeft + this.paneWidth / 2;
         int cy = this.guiTop + this.paneHeight / 2;
         List<FormattedCharSequence> list = this.font.split(this.popupmessage, 150);
         int lineHeight = 9;
         int textHeight = list.size() * lineHeight;
         int yStart = cy - textHeight / 2;
         graphics.fill(cx - 78, yStart - 3, cx + 78, yStart + textHeight + 3, -1073741824);

         for (int i = 0; i < list.size(); i++) {
            graphics.drawString(this.font, list.get(i), cx - 75, yStart + lineHeight * i, -7303024);
         }
      }

      this.drawTabTooltips(graphics, mouseX, mouseY);
      this.drawCompletionCounter(graphics, mouseX, mouseY);
      this.drawSearchPanel(graphics, mouseX, mouseY);
      this.drawFontButton(graphics, mouseX, mouseY);
      super.render(graphics, mouseX, mouseY, pt);
      if (this.currentHighlight != null) {
         this.renderNodeTooltip(graphics, mouseX, mouseY);
      }

      if (this.progressHover && this.progressHoverText != null) {
         graphics.renderComponentTooltip(this.font, List.of(Component.literal(this.progressHoverText)), mouseX, mouseY);
      }

      RenderSystem.enableDepthTest();
   }

   private void drawFontButton(GuiGraphics graphics, int mouseX, int mouseY) {
      int x = this.getFontButtonX();
      int y = this.getFontButtonY();
      boolean hovered = this.isOverFontButton(mouseX, mouseY);
      int borderColor = hovered ? -10534364 : -11848672;
      int fillColor = hovered ? -387525451 : -657145442;
      String label = Component.translatable(ThaumcraftClientConfig.getBookFontMode().getShortTranslationKey()).getString();
      int textColor = hovered ? 4007447 : 5914666;
      graphics.fill(x, y, x + 17, y + 16, fillColor);
      graphics.fill(x, y, x + 17, y + 1, borderColor);
      graphics.fill(x, y + 16 - 1, x + 17, y + 16, borderColor);
      graphics.fill(x, y, x + 1, y + 16, borderColor);
      graphics.fill(x + 17 - 1, y, x + 17, y + 16, borderColor);
      graphics.drawString(this.font, label, x + (17 - this.font.width(label)) / 2, y + 3, textColor, false);
      if (hovered) {
         graphics.renderComponentTooltip(this.font, this.getFontButtonTooltip(), mouseX, mouseY);
      }
   }

   private int getFontButtonX() {
      return this.guiLeft + this.paneWidth - 17;
   }

   private int getFontButtonY() {
      return this.guiTop + this.paneHeight - 16;
   }

   private boolean isOverFontButton(double mouseX, double mouseY) {
      int x = this.getFontButtonX();
      int y = this.getFontButtonY();
      return mouseX >= x && mouseY >= y && mouseX < x + 17 && mouseY < y + 16;
   }

   private List<Component> getFontButtonTooltip() {
      BookFontMode currentMode = ThaumcraftClientConfig.getBookFontMode();
      BookFontMode nextMode = currentMode.next();
      return List.of(
         Component.translatable("gui.thaumcraft.thaumonomicon_font.current", new Object[]{Component.translatable(currentMode.getTranslationKey())}),
         Component.translatable("gui.thaumcraft.thaumonomicon_font.toggle", new Object[]{Component.translatable(nextMode.getTranslationKey())})
      );
   }

   private void drawTabTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
      List<String> keys = new ArrayList<>(this.getTabsOnCurrentPage());
      int tabsPerSide = this.getTabPerSide();

      for (int i = 0; i < keys.size(); i++) {
         int row = i % tabsPerSide;
         boolean right = i >= tabsPerSide;
         int tx = right ? this.guiLeft + this.paneWidth : this.guiLeft - 24;
         int ty = this.guiTop + row * 24;
         if (mouseX >= tx && mouseX < tx + 24 && mouseY >= ty && mouseY < ty + 24) {
            String categoryName = ResearchApi.getCategoryName(keys.get(i));
            int textX;
            if (right) {
               int textWidth = (int)ScreenApi.MAGIC.width(categoryName, 0.15F);
               textX = mouseX - textWidth - 8;
            } else {
               textX = mouseX + 8;
            }

            ScreenApi.MAGIC.renderText(graphics, categoryName, textX, mouseY - 10, 0.15F, 16777215);
            break;
         }
      }
   }

   private void drawCompletionCounter(GuiGraphics graphics, int mouseX, int mouseY) {
      this.progressHover = false;
      this.progressHoverText = null;
      ResearchCategoryList category = ResearchApi.getResearchList(selectedCategory);
      if (category != null) {
         Map<String, ResearchItem> all = new HashMap<>(category.research);
         all.values().removeIf(ri -> ri == null || ri.isAutoUnlock() && ri.isVirtual());
         int total = all.size();
         long completedCount = all.keySet().stream().filter(this.completed::contains).count();
         long revealedCount = all.entrySet().stream().filter(e -> {
            ResearchItem ri = e.getValue();
            return ri != null && (this.completed.contains(e.getKey()) || this.completed.contains("@" + e.getKey()) || this.isResearchVisibleForPlayer(ri));
         }).count();
      }
   }

   private void drawSearchPanel(GuiGraphics graphics, int mouseX, int mouseY) {
      if (this.searchBox != null) {
         PoseStack pose = graphics.pose();
         pose.pushPose();
         pose.translate(0.0F, 0.0F, 100.0F);
         int x = this.getSearchAreaX();
         int y = this.getSearchAreaY();
         int w = this.getSearchAreaW();
         boolean hasQuery = !this.searchBox.getValue().isEmpty();
         boolean hasResults = !this.searchResults.isEmpty();
         boolean expanded = hasQuery && hasResults;
         graphics.fill(x, y, x + w, y + 20, -535291880);
         if (expanded) {
            int h = this.getSearchAreaH();
            graphics.fill(x, y + 20, x + w, y + h, -1072689136);
         }

         if (this.searchBox.getValue().isEmpty() && !this.searchBox.isFocused()) {
            graphics.drawString(this.font, I18n.get("thaumcraft.screen.search", new Object[0]), this.searchBox.getX(), this.searchBox.getY() + 1, 7829367);
         }

         this.searchBox.render(graphics, mouseX, mouseY, 0.0F);
         if (!expanded) {
            pose.popPose();
         } else {
            int maxRows = this.getSearchRows();
            if (maxRows <= 0) {
               pose.popPose();
            } else {
               int listX = x + 4;
               int listY = y + 22;
               int maxTextW = Math.max(20, w - 8);
               int visibleHeight = this.getSearchAreaH() - 22;
               int visibleRows = visibleHeight / 11;
               this.searchScroll = Mth.clamp(this.searchScroll, 0, Math.max(0, this.searchResults.size() - visibleRows));

               for (int i = 0; i < visibleRows; i++) {
                  int idx = this.searchScroll + i;
                  if (idx >= this.searchResults.size()) {
                     break;
                  }

                  ResearchItem ri = this.searchResults.get(idx);
                  int ry1 = listY + i * 11;
                  int ry2 = ry1 + 11;
                  if (mouseX >= x && mouseX < x + w && mouseY >= ry1 && mouseY < ry2) {
                     graphics.fill(x + 2, ry1, x + w - 2, ry2, 1090519039);
                  }

                  int color = this.completed.contains(ri.key) ? 11184810 : 16777215;
                  graphics.drawString(this.font, this.font.plainSubstrByWidth(ri.getName().getString(), maxTextW), listX, ry1 + 1, color, false);
               }

               if (this.searchResults.size() > visibleRows) {
                  int scrollbarX = x + w - 4;
                  int scrollbarY = y + 22;
                  int scrollbarH = visibleHeight - 2;
                  int thumbH = Math.max(10, scrollbarH * visibleRows / this.searchResults.size());
                  int maxScroll = this.searchResults.size() - visibleRows;
                  int thumbY = scrollbarY + (scrollbarH - thumbH) * this.searchScroll / Math.max(1, maxScroll);
                  graphics.fill(scrollbarX, scrollbarY, scrollbarX + 2, scrollbarY + scrollbarH, 1090519039);
                  graphics.fill(scrollbarX, thumbY, scrollbarX + 2, thumbY + thumbH, -1593835521);
               }

               pose.popPose();
            }
         }
      }
   }

   private void genResearchBackground(GuiGraphics graphics, int mouseX, int mouseY, float pt) {
      Player player = this.minecraft.player;
      if (player != null) {
         PoseStack pose = graphics.pose();
         long time = System.nanoTime() / 50000000L;
         int camX = Mth.floor(this.mapX);
         int camY = Mth.floor(this.mapY);
         int borderX = this.guiLeft;
         int borderY = this.guiTop;
         int browseX = borderX + this.paddingX;
         int browseY = borderY + this.paddingY;
         RenderSystem.depthFunc(518);
         pose.pushPose();
         pose.translate(0.0F, 0.0F, -200.0F);
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         ResearchCategoryList category = ResearchApi.getResearchList(selectedCategory);
         ResourceLocation background = category == null ? Thaumcraft.location("textures/screen/research_background.png") : category.background;
         float[] baseLineColor = category == null ? new float[]{0.16F, 0.16F, 0.16F} : toRgb(category.lineColor);
         float[] completedLineColor = scale(baseLineColor, 0.9F);
         float[] siblingLineColor = scale(baseLineColor, 0.75F);
         float[] unlockedParentLineColor = mix(baseLineColor, 0.0F, 1.0F, 0.0F, 0.6F);
         float[] availableParentLineColor = mix(baseLineColor, 0.0F, 0.0F, 1.0F, 0.6F);
         graphics.blit(background, browseX, browseY, this.mapViewW, this.mapViewH, 0.0F, 0.0F, 256, 256, 256, 256);
         pose.popPose();
         RenderSystem.enableDepthTest();
         RenderSystem.depthFunc(515);

         for (ResearchItem ri : this.research) {
            int startX = ri.displayColumn * 24 - camX + 11 + browseX;
            int startY = ri.displayRow * 24 - camY + 11 + browseY;
            if (ri.parents != null) {
               for (String pk : ri.parents) {
                  ResearchItem parent = ResearchApi.getResearch(pk);
                  if (parent != null && parent.category.equals(selectedCategory) && !parent.isVirtual()) {
                     int endX = parent.displayColumn * 24 - camX + 11 + browseX;
                     int endY = parent.displayRow * 24 - camY + 11 + browseY;
                     if (this.completed.contains(ri.key)) {
                        this.drawLine(graphics, startX, startY, endX, endY, completedLineColor[0], completedLineColor[1], completedLineColor[2], pt, false);
                     } else if (!ri.isLost() && (!ri.isHidden() || this.completed.contains("@" + ri.key)) && (!ri.isConcealed() || this.canUnlockResearch(ri))) {
                        if (this.completed.contains(parent.key)) {
                           this.drawLine(
                              graphics,
                              startX,
                              startY,
                              endX,
                              endY,
                              unlockedParentLineColor[0],
                              unlockedParentLineColor[1],
                              unlockedParentLineColor[2],
                              pt,
                              true
                           );
                        } else if ((!parent.isHidden() && !ri.isLost() || this.completed.contains("@" + parent.key))
                           && (!parent.isConcealed() || this.canUnlockResearch(parent))) {
                           this.drawLine(
                              graphics,
                              startX,
                              startY,
                              endX,
                              endY,
                              availableParentLineColor[0],
                              availableParentLineColor[1],
                              availableParentLineColor[2],
                              pt,
                              true
                           );
                        }
                     }
                  }
               }
            }

            if (ri.siblings != null) {
               for (String sk : ri.siblings) {
                  ResearchItem sib = ResearchApi.getResearch(sk);
                  if (sib != null
                     && sib.category.equals(selectedCategory)
                     && !sib.isVirtual()
                     && (sib.parents == null || !Arrays.asList(sib.parents).contains(ri.key))) {
                     int endX = sib.displayColumn * 24 - camX + 11 + browseX;
                     int endY = sib.displayRow * 24 - camY + 11 + browseY;
                     if (this.completed.contains(ri.key)) {
                        this.drawLine(graphics, startX, startY, endX, endY, siblingLineColor[0], siblingLineColor[1], siblingLineColor[2], pt, false);
                     } else if (!ri.isLost() && (!ri.isHidden() || this.completed.contains("@" + ri.key)) && (!ri.isConcealed() || this.canUnlockResearch(ri))) {
                        if (this.completed.contains(sib.key)) {
                           this.drawLine(
                              graphics,
                              startX,
                              startY,
                              endX,
                              endY,
                              unlockedParentLineColor[0],
                              unlockedParentLineColor[1],
                              unlockedParentLineColor[2],
                              pt,
                              true
                           );
                        } else if ((!sib.isHidden() || this.completed.contains("@" + sib.key)) && (!sib.isConcealed() || this.canUnlockResearch(sib))) {
                           this.drawLine(
                              graphics,
                              startX,
                              startY,
                              endX,
                              endY,
                              availableParentLineColor[0],
                              availableParentLineColor[1],
                              availableParentLineColor[2],
                              pt,
                              true
                           );
                        }
                     }
                  }
               }
            }
         }

         this.currentHighlight = null;
         ScreenApi.enableScissor(graphics, browseX, browseY, this.mapViewW, this.mapViewH);

         for (ResearchItem ri : this.research) {
            int posX = ri.displayColumn * 24 - camX;
            int posY = ri.displayRow * 24 - camY;
            if (!ri.isVirtual() && posX >= -24 && posY >= -24 && posX <= this.mapViewW && posY <= this.mapViewH && this.isResearchVisibleForPlayer(ri)) {
               int x = browseX + posX;
               int y = browseY + posY;
               if (ThaumcraftApi.getWarp(ri.key) > 0) {
                  this.drawForbidden(graphics, (float)time, x + 11, y + 11);
               }

               graphics.flush();
               if (this.completed.contains(ri.key)) {
                  graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               } else {
                  float pulse = this.canUnlockResearch(ri) ? (float)Math.sin(System.currentTimeMillis() % 600L / 600.0 * Math.PI * 2.0) * 0.25F + 0.75F : 0.3F;
                  graphics.setColor(pulse, pulse, pulse, 1.0F);
               }

               RenderSystem.enableBlend();
               RenderSystem.blendFunc(770, 771);
               if (ri.isRound()) {
                  graphics.blit(TEX_RESEARCH, x - 2, y - 2, 54, 230, 26, 26);
               } else if (ri.isHidden()) {
                  graphics.blit(
                     TEX_RESEARCH,
                     x - 2,
                     y - 2,
                     Config.researchDifficulty != -1 && (Config.researchDifficulty != 0 || !ri.isSecondary()) ? 86 : 230,
                     230,
                     26,
                     26
                  );
               } else {
                  graphics.blit(
                     TEX_RESEARCH,
                     x - 2,
                     y - 2,
                     Config.researchDifficulty != -1 && (Config.researchDifficulty != 0 || !ri.isSecondary()) ? 0 : 110,
                     230,
                     26,
                     26
                  );
               }

               if (ri.isSpecial()) {
                  graphics.blit(TEX_RESEARCH, x - 2, y - 2, 26, 230, 26, 26);
               }

               if (highlightedItem.contains(ri.key)) {
                  pose.pushPose();
                  graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                  pose.translate(x - 5, y - 5, 0.0F);
                  graphics.blit(ParticleUtils.PARTICLE_TEXTURE, 0, 0, (int)(time % 16L) * 16, 80, 16, 16);
                  pose.popPose();
               }

               pose.pushPose();
               pose.translate(0.0F, 0.0F, -140.0F);
               if (ri.icon instanceof Ingredient ingredient) {
                  ItemStack stack = InventoryUtils.cycleItemStack(ingredient);
                  graphics.renderItem(stack, x + 3, y + 3);
                  graphics.renderItemDecorations(this.font, stack, x + 3, y + 3);
               } else if (ri.icon instanceof Block block) {
                  ScreenApi.renderBlock(graphics, block.defaultBlockState(), x, y + 2);
               } else if (ri.icon instanceof ResourceLocation rl) {
                  UtilsFX.drawTexturedQuadFull(graphics, rl, x + 3, y + 3, 1000);
               }

               pose.popPose();
               RenderSystem.disableBlend();
               if (mouseX >= browseX
                  && mouseY >= browseY
                  && mouseX < browseX + this.mapViewW
                  && mouseY < browseY + this.mapViewH
                  && mouseX >= x
                  && mouseX <= x + 22
                  && mouseY >= y
                  && mouseY <= y + 22
                  && !this.isMouseInSearchPanel(mouseX, mouseY)) {
                  this.currentHighlight = ri;
               }

               graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
         }

         graphics.flush();
         graphics.disableScissor();
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
         List<String> tabKeys = new ArrayList<>(this.getTabsOnCurrentPage());
         int tabsPerSide = this.getTabPerSide();

         for (int i = 0; i < tabKeys.size(); i++) {
            String key = tabKeys.get(i);
            ResearchCategoryList list = ResearchApi.getResearchList(key);
            int row = i % tabsPerSide;
            boolean right = i >= tabsPerSide;
            int baseX = right ? borderX + this.paneWidth : borderX - 24;
            int baseY = borderY + row * 24;
            int uOff = selectedCategory.equals(key) ? 0 : 24;
            int iconShift = right ? (selectedCategory.equals(key) ? -2 : -10) : (selectedCategory.equals(key) ? 0 : 8);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (right) {
               this.drawTexturedModalRectReversed(graphics, TEX_RESEARCH, baseX, baseY, 176 + uOff, 232, 24, 24);
            } else {
               graphics.blit(TEX_RESEARCH, baseX, baseY, 152 + uOff, 232, 24, 24);
            }

            if (highlightedItem.contains(key)) {
               UtilsFX.drawTexturedQuad(graphics, ParticleUtils.PARTICLE_TEXTURE, baseX - 3 + iconShift, baseY - 4, (int)(16L * (time % 16L)), 80, 16, 16, -90);
            }

            UtilsFX.drawTexturedQuadFull(graphics, list.icon, baseX + 5 + iconShift, baseY + 4, -80);
            if (!selectedCategory.equals(key)) {
               if (right) {
                  this.drawTexturedModalRectReversed(graphics, TEX_RESEARCH, baseX, baseY, 224, 232, 24, 24);
               } else {
                  graphics.blit(TEX_RESEARCH, baseX, baseY, 200, 232, 24, 24);
               }
            }
         }

         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         this.drawResearchBrowserBorders(graphics, borderX, borderY, this.paneWidth, this.paneHeight);
         RenderSystem.depthFunc(515);
         RenderSystem.disableDepthTest();
      }
   }

   private boolean isResearchVisibleForPlayer(ResearchItem ri) {
      return !this.completed.contains(ri.key) && !this.completed.contains("@" + ri.key)
         ? !ri.isLost() && (!ri.isHidden() || this.completed.contains("@" + ri.key)) && (!ri.isConcealed() || this.canUnlockResearch(ri))
         : true;
   }

   private void drawResearchBrowserBorders(GuiGraphics graphics, int x, int y, int w, int h) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(0.0F, 0.0F, -100.0F);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, TEX_RESEARCH);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      Matrix4f m = pose.last().pose();
      BufferBuilder bb = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

      for (int[] px : PARAMS) {
         for (int[] py : PARAMS) {
            double x1 = x + px[0] * w + px[1] * 16;
            double x2 = x + px[2] * w + px[3] * 16;
            double y1 = y + py[0] * h + py[1] * 17;
            double y2 = y + py[2] * h + py[3] * 17;
            float tu1 = (float)((px[0] * 256 + px[1] * 16) / 256.0);
            float tu2 = (float)((px[2] * 256 + px[3] * 16) / 256.0);
            float tv1 = (float)((py[0] * 230 + py[1] * 17) / 256.0);
            float tv2 = (float)((py[2] * 230 + py[3] * 17) / 256.0);
            bb.addVertex(m, (float)x1, (float)y2, 0.0F).setUv(tu1, tv2);
            bb.addVertex(m, (float)x2, (float)y2, 0.0F).setUv(tu2, tv2);
            bb.addVertex(m, (float)x2, (float)y1, 0.0F).setUv(tu2, tv1);
            bb.addVertex(m, (float)x1, (float)y1, 0.0F).setUv(tu1, tv1);
         }
      }

      BufferUploader.drawWithShader(bb.buildOrThrow());
      RenderSystem.disableBlend();
      pose.popPose();
   }

   private int getTabPerSide() {
      return Math.max(1, this.paneHeight / 24);
   }

   private boolean isEldritchUnlocked() {
      return this.completed.contains("eldritch_minor");
   }

   private int countVisibleCategories() {
      int count = 0;

      for (String k : ResearchApi.getResearchLists().keySet()) {
         if (!k.equals("eldritch") || this.isEldritchUnlocked()) {
            count++;
         }
      }

      return count;
   }

   private void updateMaxPageIndex() {
      int tabsPerPage = this.getTabPerSide() * 2;
      int newMax = Math.max(0, (this.countVisibleCategories() + tabsPerPage - 1) / tabsPerPage - 1);
      if (newMax != this.maxPageIndex) {
         this.maxPageIndex = newMax;
         this.currentPageIndex = Math.min(this.currentPageIndex, this.maxPageIndex);
         this.recalcTabs = true;
      }
   }

   private Collection<String> getTabsOnCurrentPage() {
      if (this.recalcTabs) {
         this.rebuildTabCache();
      }

      return this.currentPageTabs.keySet();
   }

   private void rebuildTabCache() {
      this.currentPageTabs.clear();
      int tabsPerPage = this.getTabPerSide() * 2;
      int toSkip = tabsPerPage * Mth.clamp(this.currentPageIndex, 0, this.maxPageIndex);

      for (Entry<String, ResearchCategoryList> e : ResearchApi.getResearchLists().entrySet()) {
         if (!e.getKey().equals("eldritch") || this.isEldritchUnlocked()) {
            if (toSkip > 0) {
               toSkip--;
            } else {
               this.currentPageTabs.put(e.getKey(), e.getValue());
               if (this.currentPageTabs.size() >= tabsPerPage) {
                  break;
               }
            }
         }
      }

      this.recalcTabs = false;
   }

   private void nextPage() {
      this.setPage(this.currentPageIndex + 1);
   }

   private void prevPage() {
      this.setPage(this.currentPageIndex - 1);
   }

   private void setPage(int next) {
      this.currentPageIndex = Mth.clamp(next, 0, this.maxPageIndex);
      this.recalcTabs = true;
      this.rebuildTabCache();
   }

   private void renderNodeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
      Player player = this.minecraft.player;
      if (player != null) {
         Component name = this.currentHighlight.getName();
         boolean nonComplete = !this.completed.contains(this.currentHighlight.key);
         boolean canUnlock = this.canUnlockResearch(this.currentHighlight);
         boolean useRunic = nonComplete && !canUnlock;
         float lineHeightTitle = this.bookFontHeight(useRunic, 0.15F);
         PoseStack pose = graphics.pose();
         pose.pushPose();
         pose.translate(0.0F, 0.0F, 1100.0F);
         int x = mouseX + 6;
         int y = mouseY - 4;
         if (canUnlock) {
            Component desc = this.currentHighlight.getText();
            boolean aspectToResearch = nonComplete
               && this.currentHighlight.tags != null
               && this.currentHighlight.tags.size() > 0
               && (Config.researchDifficulty == -1 || Config.researchDifficulty == 0 && this.currentHighlight.isSecondary());
            boolean scribeToResearch = !aspectToResearch && nonComplete;
            int maxWidth = (int)Math.max(this.bookFontWidth(useRunic, name, 0.15F), this.bookFontWidth(useRunic, desc));
            int offY = (int)(lineHeightTitle + 5.0F);
            int height = 0;
            if (scribeToResearch) {
               height += 12;
               maxWidth = (int)Math.max((float)maxWidth, this.bookFontWidth(useRunic, I18n.get("tc.research.shortprim", new Object[0])));
            }

            if (aspectToResearch) {
               height += 29;
               maxWidth = (int)Math.max((float)maxWidth, this.bookFontWidth(useRunic, I18n.get("tc.research.short", new Object[0])));
            }

            int warp = Math.min(ThaumcraftApi.getWarp(this.currentHighlight.key), 5);
            String forbidden = I18n.get("tc.forbidden", new Object[]{I18n.get("tc.forbidden.level." + warp, new Object[0])});
            if (warp > 0) {
               height += 9;
               maxWidth = (int)Math.max((float)maxWidth, this.bookFontWidth(useRunic, forbidden));
            }

            graphics.fillGradient(x - 3, y - 1, x + maxWidth + 3, y + offY + 6 + height, -1073741824, -1073741824);
            this.renderBookText(useRunic, graphics, desc, (float)x, y + offY - 3.5F, -7303024);
            if (warp > 0) {
               this.renderBookText(useRunic, graphics, forbidden, (float)x, y + offY + 4.5F, 16777215);
               offY += 9;
            }

            if (scribeToResearch) {
               if (ResearchManager.getResearchSlot(player, this.currentHighlight.key) >= 0) {
                  this.renderBookText(useRunic, graphics, Component.translatable("tc.research.hasnote"), (float)x, (float)(y + offY + 6), -22016);
               } else {
                  this.renderBookText(
                     useRunic,
                     graphics,
                     Component.translatable(this.hasScribestuff ? "tc.research.getprim" : "tc.research.shortprim"),
                     (float)x,
                     (float)(y + offY + 6),
                     this.hasScribestuff ? -7875669 : -2356724
                  );
               }
            } else if (aspectToResearch) {
               boolean enough = true;
               int i = 0;

               for (Aspect aspect : this.currentHighlight.tags.getAspectsSortedAmount()) {
                  if (this.knowledge.hasDiscoveredAspect(aspect)) {
                     float alpha = this.knowledge.getAspectPoolFor(aspect) < this.currentHighlight.tags.getAmount(aspect)
                        ? (float)Math.sin(System.currentTimeMillis() % 600L / 600.0 * Math.PI * 2.0) * 0.25F + 0.75F
                        : 1.0F;
                     if (alpha < 1.0F) {
                        enough = false;
                     }

                     UtilsFX.drawTag(
                        graphics, x + i * 16, y + offY + 10, aspect, (float)this.currentHighlight.tags.getAmount(aspect), 0, 0.0F, 771, alpha, false
                     );
                  } else {
                     enough = false;
                     graphics.pose().pushPose();
                     graphics.setColor(0.5F, 0.5F, 0.5F, 0.5F);
                     graphics.pose().translate(x + i * 16, y + offY + 8, 0.0F);
                     UtilsFX.drawTexturedQuadFull(graphics, Thaumcraft.location("textures/aspects/_unknown.png"), 0, 0, 0);
                     graphics.pose().popPose();
                  }

                  i++;
               }

               this.renderBookText(
                  useRunic,
                  graphics,
                  Component.translatable(enough ? "tc.research.purchase" : "tc.research.short"),
                  (float)x,
                  (float)(y + offY + 23),
                  enough ? -7875669 : -2356724
               );
            }
         } else {
            Component missing = Component.translatable("tc.researchmissing");
            graphics.fillGradient(
               x - 3,
               y - 3,
               x + (int)Math.max(this.bookFontWidth(useRunic, name, 0.15F), ScreenApi.MAGIC.width(missing)) + 3,
               (int)(y + lineHeightTitle + 13.0F),
               -1073741824,
               -1073741824
            );
            ScreenApi.MAGIC.renderText(graphics, missing, (float)x, (float)(y + 12), -9396048);
         }

         this.renderBookText(
            useRunic,
            graphics,
            name.getString(),
            x,
            y,
            0.15F,
            canUnlock ? (this.currentHighlight.isSpecial() ? -128 : -1) : (this.currentHighlight.isSpecial() ? -8355776 : -8355712)
         );
         pose.popPose();
      }
   }

   private float bookFontHeight(boolean runic, float scale) {
      return runic ? ScreenApi.RUNIC.height(scale) : ScreenApi.MAGIC.height(scale);
   }

   private float bookFontWidth(boolean runic, Component text, float scale) {
      return runic ? ScreenApi.RUNIC.width(text, scale) : ScreenApi.MAGIC.width(text, scale);
   }

   private float bookFontWidth(boolean runic, Component text) {
      return runic ? ScreenApi.RUNIC.width(text) : ScreenApi.MAGIC.width(text);
   }

   private float bookFontWidth(boolean runic, String text) {
      return runic ? ScreenApi.RUNIC.width(text) : ScreenApi.MAGIC.width(text);
   }

   private void renderBookText(boolean runic, GuiGraphics graphics, Component text, float x, float y, int color) {
      if (runic) {
         ScreenApi.RUNIC.renderText(graphics, text, x, y, color);
      } else {
         ScreenApi.MAGIC.renderText(graphics, text, x, y, color);
      }
   }

   private void renderBookText(boolean runic, GuiGraphics graphics, String text, float x, float y, int color) {
      if (runic) {
         ScreenApi.RUNIC.renderText(graphics, text, x, y, color);
      } else {
         ScreenApi.MAGIC.renderText(graphics, text, x, y, color);
      }
   }

   private void renderBookText(boolean runic, GuiGraphics graphics, String text, float x, float y, float scale, int color) {
      if (runic) {
         ScreenApi.RUNIC.renderText(graphics, text, x, y, scale, color);
      } else {
         ScreenApi.MAGIC.renderText(graphics, text, x, y, scale, color);
      }
   }

   public void drawTexturedModalRectReversed(GuiGraphics graphics, ResourceLocation location, int x, int y, int u, int v, int width, int height) {
      Matrix4f matrix4f = graphics.pose().last().pose();
      RenderSystem.setShaderTexture(0, location);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      float tu = 0.00390625F;
      float tv = 0.00390625F;
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder builder = tesselator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      builder.addVertex(matrix4f, x, y + height, 0.0F).setUv(u * tu, (v + height) * tv);
      builder.addVertex(matrix4f, x + width, y + height, 0.0F).setUv((u - width) * tu, (v + height) * tv);
      builder.addVertex(matrix4f, x + width, y, 0.0F).setUv((u - width) * tu, v * tv);
      builder.addVertex(matrix4f, x, y, 0.0F).setUv(u * tu, v * tv);
      BufferUploader.drawWithShader(builder.buildOrThrow());
   }

   private void playButtonClick() {
      this.minecraft
         .cameraEntity
         .level()
         .playLocalSound(
            this.minecraft.cameraEntity.getX(),
            this.minecraft.cameraEntity.getY(),
            this.minecraft.cameraEntity.getZ(),
            (SoundEvent)ModSounds.CAMERA_CLACK.get(),
            SoundSource.MASTER,
            0.4F,
            1.0F,
            false
         );
   }

   private boolean canUnlockResearch(ResearchItem researchItem) {
      if (researchItem.parents != null) {
         for (String p : researchItem.parents) {
            if (!this.completed.contains(p)) {
               return false;
            }
         }
      }

      if (researchItem.parentsHidden != null) {
         for (String px : researchItem.parentsHidden) {
            if (!this.completed.contains(px)) {
               return false;
            }
         }
      }

      return true;
   }

   private static float[] toRgb(int color) {
      return new float[]{(color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F};
   }

   private static float[] scale(float[] color, float factor) {
      return new float[]{Mth.clamp(color[0] * factor, 0.0F, 1.0F), Mth.clamp(color[1] * factor, 0.0F, 1.0F), Mth.clamp(color[2] * factor, 0.0F, 1.0F)};
   }

   private static float[] mix(float[] base, float r, float g, float b, float ratio) {
      float inv = 1.0F - ratio;
      return new float[]{
         Mth.clamp(base[0] * inv + r * ratio, 0.0F, 1.0F), Mth.clamp(base[1] * inv + g * ratio, 0.0F, 1.0F), Mth.clamp(base[2] * inv + b * ratio, 0.0F, 1.0F)
      };
   }

   private void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, float r, float g, float b, float pt, boolean animated) {
      PoseStack pose = graphics.pose();
      float tick = this.minecraft.player.tickCount + pt;
      pose.pushPose();
      pose.translate(0.0F, 0.0F, -50.0F);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 771);
      double dx = x1 - x2;
      double dy = y1 - y2;
      int steps = Math.max(1, (int)(Math.sqrt(dx * dx + dy * dy) / 2.0));
      float stepX = (float)(dx / steps);
      float stepY = (float)(dy / steps);
      boolean mostlyX = Math.abs(dx) > Math.abs(dy);
      if (mostlyX) {
         stepX *= 2.0F;
      } else {
         stepY *= 2.0F;
      }

      float[] points = new float[(steps + 1) * 2];

      for (int i = 0; i <= steps; i++) {
         float offX = 0.0F;
         float offY = 0.0F;
         if (animated) {
            float t = (float)i / steps;
            float wobble = Mth.sin(t * (float) Math.PI);
            offX = Mth.sin((tick + i) / 7.0F) * 5.0F * wobble;
            offY = Mth.sin((tick + i) / 5.0F) * 5.0F * wobble;
            float consumer = r * (1.0F - t);
            float ix = g * (1.0F - t);
            float rr = b * (1.0F - t);
         }

         points[i * 2] = x1 - stepX * i + offX;
         points[i * 2 + 1] = y1 - stepY * i + offY;
         if (mostlyX) {
            stepX *= 1.0F - 1.0F / (steps * 1.5F);
         } else {
            stepY *= 1.0F - 1.0F / (steps * 1.5F);
         }
      }

      Matrix4f matrix = pose.last().pose();
      VertexConsumer consumer = graphics.bufferSource().getBuffer(RenderType.lineStrip());
      RenderSystem.lineWidth(5.0F);

      for (int i = 0; i <= steps; i++) {
         float rr = r;
         float gg = g;
         float bb = b;
         float a = 0.6F;
         if (animated) {
            float t = (float)i / steps;
            rr = r * (1.0F - t);
            gg = g * (1.0F - t);
            bb = b * (1.0F - t);
            a *= t;
         }

         int prevIndex = Math.max(0, i - 1) * 2;
         int nextIndex = Math.min(steps, i + 1) * 2;
         float dirX = points[nextIndex] - points[prevIndex];
         float dirY = points[nextIndex + 1] - points[prevIndex + 1];
         float dirLength = Mth.sqrt(dirX * dirX + dirY * dirY);
         if (dirLength < 0.001F) {
            dirX = 0.0F;
            dirY = 1.0F;
            dirLength = 1.0F;
         }

         consumer.addVertex(matrix, points[i * 2], points[i * 2 + 1], 0.0F).setColor(rr, gg, bb, a).setNormal(dirX / dirLength, dirY / dirLength, 0.0F);
      }

      graphics.flush();
      RenderSystem.disableBlend();
      pose.popPose();
   }

   private void drawForbidden(GuiGraphics graphics, float partialTick, int x, int y) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         float smoothTicks = mc.player.tickCount + partialTick;
         float speed = 0.5F;
         float animProgress = smoothTicks * speed;
         int gridSize = 16;
         float maxFrameIndex = gridSize - 1;
         float cycleDuration = maxFrameIndex * 2.0F;
         float cyclePos = animProgress % cycleDuration;
         float floatFrame;
         if (cyclePos <= maxFrameIndex) {
            floatFrame = cyclePos;
         } else {
            floatFrame = cycleDuration - cyclePos;
         }

         int frame = (int)floatFrame;
         PoseStack poseStack = graphics.pose();
         poseStack.pushPose();
         poseStack.translate(x, y, 0.0);
         float size = 80.0F;
         float alpha = 0.66F;
         int row = 5;
         int color = 4456533;
         float cellSize = 1.0F / gridSize;
         float u0 = frame * cellSize;
         float u1 = (frame + 1) * cellSize;
         float v0 = row * cellSize;
         float v1 = (row + 1) * cellSize;
         int r = color >> 16 & 0xFF;
         int g = color >> 8 & 0xFF;
         int b = color & 0xFF;
         int a = (int)(alpha * 255.0F);
         float hs = size * 0.5F;
         Matrix4f pose = poseStack.last().pose();
         VertexConsumer vc = graphics.bufferSource().getBuffer(ThaumcraftRenderTypes.forbiddenGui(NODES_TEXTURE));
         vc.addVertex(pose, -hs, hs, 0.0F).setColor(r, g, b, a).setUv(u0, v1);
         vc.addVertex(pose, hs, hs, 0.0F).setColor(r, g, b, a).setUv(u1, v1);
         vc.addVertex(pose, hs, -hs, 0.0F).setColor(r, g, b, a).setUv(u1, v0);
         vc.addVertex(pose, -hs, -hs, 0.0F).setColor(r, g, b, a).setUv(u0, v0);
         poseStack.popPose();
      }
   }

   private final class PageButton extends AbstractWidget {
      private final boolean prev;

      PageButton(int x, int y, boolean prev) {
         super(x, y, 24, 8, Component.empty());
         this.prev = prev;
      }

      void setVisible(boolean v) {
         this.visible = v;
         this.active = v;
      }

      protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float pt) {
         if (this.visible) {
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.isHovered ? -1442840576 : -2013265920);
            String s = this.prev ? "<" : ">";
            graphics.drawString(
               Minecraft.getInstance().font,
               s,
               this.getX() + (this.width - Minecraft.getInstance().font.width(s)) / 2,
               this.getY() + (this.height - 9) / 2,
               16777215
            );
         }
      }

      public void onClick(double mx, double my) {
         if (this.prev) {
            ResearchBrowserScreen.this.prevPage();
         } else {
            ResearchBrowserScreen.this.nextPage();
         }
      }

      protected void updateWidgetNarration(@NotNull NarrationElementOutput n) {
      }
   }
}
