package thaumicenergistics.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import thaumicenergistics.ae2.recipe.AssemblerRecipe;
import thaumicenergistics.ae2.recipe.AssemblerRecipes;
import thaumicenergistics.network.AssemblerTogglePacket;
import thaumicenergistics.network.ThEPacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * The recipe list of an assembler GUI: every recipe as an item icon in the grid the window already has
 * (pages, search above the window), a click switches the recipe on or off for the ME network and
 * switched-off recipes are dimmed. "All on/off" and the page buttons are tabs stuck to the right edge.
 */
@OnlyIn(Dist.CLIENT)
final class RecipeGridPanel {
   /** Where the grid sits inside the window and where the tabs stand (x of the window's right edge). */
   record Layout(int gridX, int gridY, int cols, int rows, int tabX) {
   }

   private final Layout layout;
   private final Font font;
   private final int containerId;
   private final AssemblerRecipes book;
   @Nullable
   private final Level level;
   private int leftPos;
   private int topPos;
   private EditBox search;
   private int page;
   private List<AssemblerRecipe> filtered = List.of();
   @Nullable
   private Map<ResourceLocation, AssemblerRecipe> filteredFrom;
   private String filteredQuery = "";

   RecipeGridPanel(Layout layout, Font font, int containerId, AssemblerRecipes book, @Nullable Level level) {
      this.layout = layout;
      this.font = font;
      this.containerId = containerId;
      this.book = book;
      this.level = level;
   }

   private int pageSize() {
      return this.layout.cols() * this.layout.rows();
   }

   void init(int leftPos, int topPos, Consumer<AbstractWidget> add) {
      this.leftPos = leftPos;
      this.topPos = topPos;
      int width = Math.max(46, this.layout.cols() * 18 - 44);
      this.search = new EditBox(this.font, leftPos + this.layout.gridX(), topPos - 16, width, 12, Component.translatable("gui.thaumicenergistics.recipes.search"));
      this.search.setHint(Component.translatable("gui.thaumicenergistics.recipes.search").withStyle(ChatFormatting.DARK_GRAY));
      this.search.setMaxLength(64);
      this.search.setValue(this.filteredQuery);
      this.search.setResponder(value -> this.page = 0);
      add.accept(this.search);
      this.addTab(add, 0, "+", "gui.thaumicenergistics.recipes.enable_all", () -> this.send(AssemblerRecipes.Action.ENABLE_ALL, null));
      this.addTab(add, 1, "-", "gui.thaumicenergistics.recipes.disable_all", () -> this.send(AssemblerRecipes.Action.DISABLE_ALL, null));
      this.addTab(add, 2, "<", "gui.thaumicenergistics.recipes.previous", () -> this.turnPage(-1));
      this.addTab(add, 3, ">", "gui.thaumicenergistics.recipes.next", () -> this.turnPage(1));
   }

   private void addTab(Consumer<AbstractWidget> add, int index, String label, String tooltip, Runnable action) {
      add.accept(
         Button.builder(Component.literal(label), button -> action.run())
            .bounds(this.leftPos + this.layout.tabX(), this.topPos + 17 + index * 22, 20, 20)
            .tooltip(Tooltip.create(Component.translatable(tooltip)))
            .build()
      );
   }

   /** The search text, kept by the screen across a window resize (init builds a new panel). */
   String query() {
      return this.search == null ? this.filteredQuery : this.search.getValue();
   }

   void setQuery(String query) {
      this.filteredQuery = query;
   }

   boolean isSearchFocused() {
      return this.search != null && this.search.isFocused();
   }

   private List<AssemblerRecipe> visibleRecipes() {
      Map<ResourceLocation, AssemblerRecipe> all = this.book.recipes(this.level);
      String query = this.search == null ? "" : this.search.getValue().trim().toLowerCase(Locale.ROOT);
      if (all != this.filteredFrom || !query.equals(this.filteredQuery)) {
         List<AssemblerRecipe> list = new ArrayList<>();
         for (AssemblerRecipe recipe : all.values()) {
            if (matches(recipe, query)) {
               list.add(recipe);
            }
         }

         this.filtered = list;
         this.filteredFrom = all;
         this.filteredQuery = query;
      }

      this.page = Math.min(this.page, this.pageCount() - 1);
      return this.filtered;
   }

   /** Search over the output name, the input names and the recipe id. */
   private static boolean matches(AssemblerRecipe recipe, String query) {
      if (query.isEmpty() || recipe.id().toString().contains(query) || recipe.output().getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) {
         return true;
      }

      for (AssemblerRecipe.Entry entry : recipe.items()) {
         for (ItemStack input : entry.ingredient().getItems()) {
            if (input.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) {
               return true;
            }
         }
      }

      return false;
   }

   private int pageCount() {
      return Math.max(1, (this.filtered.size() + this.pageSize() - 1) / this.pageSize());
   }

   private void turnPage(int delta) {
      this.visibleRecipes();
      this.page = Math.floorMod(this.page + delta, this.pageCount());
   }

   private void send(AssemblerRecipes.Action action, @Nullable ResourceLocation recipe) {
      ThEPacketHandler.sendToServer(new AssemblerTogglePacket(this.containerId, action, recipe));
   }

   private int indexAt(double mouseX, double mouseY) {
      // the cell includes the 1-pixel slot border on its left and top
      int col = (int)Math.floor((mouseX - this.leftPos - this.layout.gridX() + 1) / 18.0);
      int row = (int)Math.floor((mouseY - this.topPos - this.layout.gridY() + 1) / 18.0);
      if (col < 0 || row < 0 || col >= this.layout.cols() || row >= this.layout.rows()) {
         return -1;
      }

      int index = this.page * this.pageSize() + row * this.layout.cols() + col;
      return index < this.visibleRecipes().size() ? index : -1;
   }

   /** The icons of the current page; dimmed when the recipe is off. Called from renderBg. */
   void renderRecipes(GuiGraphics graphics) {
      List<AssemblerRecipe> recipes = this.visibleRecipes();
      int start = this.page * this.pageSize();
      for (int i = 0; i < this.pageSize() && start + i < recipes.size(); i++) {
         AssemblerRecipe recipe = recipes.get(start + i);
         int x = this.leftPos + this.layout.gridX() + (i % this.layout.cols()) * 18;
         int y = this.topPos + this.layout.gridY() + (i / this.layout.cols()) * 18;
         graphics.renderItem(recipe.icon(), x, y);
         graphics.renderItemDecorations(this.font, recipe.icon(), x, y);
         if (!this.book.isEnabled(recipe.id())) {
            // above the stack size (drawn at z 200), so the digits are dimmed too
            graphics.fill(x, y, x + 16, y + 16, 250, 0xB0303030);
         }
      }
   }

   /** Page and enabled count next to the search box (outside the window, so no text has to fit inside it). */
   void renderStatus(GuiGraphics graphics) {
      this.visibleRecipes();
      Map<ResourceLocation, AssemblerRecipe> all = this.book.recipes(this.level);
      int enabled = 0;
      for (ResourceLocation id : all.keySet()) {
         if (this.book.isEnabled(id)) {
            enabled++;
         }
      }

      String text = (this.page + 1) + "/" + this.pageCount() + "  " + Component.translatable("gui.thaumicenergistics.recipes.count", enabled, all.size()).getString();
      graphics.drawString(this.font, text, this.search.getX() + this.search.getWidth() + 6, this.search.getY() + 2, 0xFFFFFF, true);
   }

   /** Highlight and tooltip of the hovered recipe; false when the mouse is not over one. */
   boolean renderHover(GuiGraphics graphics, int mouseX, int mouseY) {
      int index = this.indexAt(mouseX, mouseY);
      if (index < 0) {
         return false;
      }

      AssemblerRecipe recipe = this.filtered.get(index);
      int slot = index - this.page * this.pageSize();
      int x = this.leftPos + this.layout.gridX() + (slot % this.layout.cols()) * 18;
      int y = this.topPos + this.layout.gridY() + (slot / this.layout.cols()) * 18;
      graphics.fill(x, y, x + 16, y + 16, 300, 0x80FFFFFF);
      graphics.renderComponentTooltip(this.font, this.tooltip(recipe), mouseX, mouseY);
      return true;
   }

   private List<Component> tooltip(AssemblerRecipe recipe) {
      List<Component> lines = new ArrayList<>();
      ItemStack output = recipe.output();
      lines.add(Component.literal(output.getCount() + "× ").append(output.getHoverName()));
      for (AssemblerRecipe.Entry entry : recipe.items()) {
         ItemStack[] options = entry.ingredient().getItems();
         Component input = Component.literal(entry.count() + "× ").append(options[0].getHoverName());
         if (options.length > 1) {
            input = Component.translatable("gui.thaumicenergistics.recipes.variants", input, options.length - 1);
         }

         lines.add(Component.translatable("gui.thaumicenergistics.recipes.input", input).withStyle(ChatFormatting.GRAY));
      }

      if (!recipe.aspects().aspects.isEmpty()) {
         StringBuilder aspects = new StringBuilder();
         recipe.aspects().aspects.forEach((aspect, amount) -> {
            if (aspect != null && amount > 0) {
               aspects.append(aspects.length() == 0 ? "" : ", ").append(aspect.getName().getString()).append(" ").append(amount);
            }
         });
         lines.add(Component.translatable("gui.thaumicenergistics.recipes.aspects." + recipe.kind().name().toLowerCase(Locale.ROOT), aspects.toString()).withStyle(ChatFormatting.GRAY));
      }

      lines.add(
         this.book.isEnabled(recipe.id())
            ? Component.translatable("gui.thaumicenergistics.recipes.enabled").withStyle(ChatFormatting.GREEN)
            : Component.translatable("gui.thaumicenergistics.recipes.disabled").withStyle(ChatFormatting.RED)
      );
      return lines;
   }

   boolean mouseClicked(double mouseX, double mouseY, int button) {
      int index = this.indexAt(mouseX, mouseY);
      if (index >= 0 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
         this.send(AssemblerRecipes.Action.TOGGLE, this.filtered.get(index).id());
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         return true;
      }

      return false;
   }

   boolean mouseScrolled(double scrollY) {
      if (scrollY != 0) {
         this.turnPage(scrollY > 0 ? -1 : 1);
         return true;
      }

      return false;
   }

   /** Typing in the search box must not close the screen on the inventory key. */
   boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.isSearchFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
         return this.search.keyPressed(keyCode, scanCode, modifiers) || this.search.canConsumeInput();
      }

      return false;
   }
}
