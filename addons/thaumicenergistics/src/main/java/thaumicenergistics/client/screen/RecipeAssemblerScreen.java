package thaumicenergistics.client.screen;

import thaumicenergistics.ae2.recipe.AssemblerRecipes;
import thaumicenergistics.menu.RecipeAssemblerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/** Base of the three assembler screens: the window plus the {@link RecipeGridPanel} wired into input and rendering. */
@OnlyIn(Dist.CLIENT)
abstract class RecipeAssemblerScreen<T extends AbstractContainerMenu & RecipeAssemblerMenu> extends AbstractContainerScreen<T> {
   private final RecipeGridPanel.Layout layout;
   protected RecipeGridPanel panel;

   protected RecipeAssemblerScreen(T menu, Inventory inventory, Component title, RecipeGridPanel.Layout layout) {
      super(menu, inventory, title);
      this.layout = layout;
   }

   @Override
   protected void setInitialFocus() {
      this.setFocused(null);
   }

   @Override
   protected void init() {
      super.init();
      AssemblerRecipes book = this.menu.recipeAssembler().recipeBook();
      String query = this.panel == null ? "" : this.panel.query();
      this.panel = new RecipeGridPanel(this.layout, this.font, this.menu.containerId, book, this.minecraft == null ? null : this.minecraft.level);
      this.panel.setQuery(query);
      this.panel.init(this.leftPos, this.topPos, widget -> this.addRenderableWidget(widget));
   }

   /** Call from renderBg after the window texture is drawn. */
   protected void renderRecipes(GuiGraphics graphics) {
      this.panel.renderRecipes(graphics);
   }

   @Override
   public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(graphics, mouseX, mouseY, partialTick);
      super.render(graphics, mouseX, mouseY, partialTick);
      this.panel.renderStatus(graphics);
      if (!this.panel.renderHover(graphics, mouseX, mouseY)) {
         this.renderTooltip(graphics, mouseX, mouseY);
         this.renderExtraTooltips(graphics, mouseX, mouseY);
      }
   }

   /** Tooltips of the screen's own regions (the vis bars), drawn when no recipe is hovered. */
   protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.panel.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      return this.panel.mouseScrolled(scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.panel.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
   }
}
