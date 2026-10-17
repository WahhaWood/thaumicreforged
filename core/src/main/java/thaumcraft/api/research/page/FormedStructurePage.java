package thaumcraft.api.research.page;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.block.table.ResearchRecipeScreen;
import thaumcraft.util.DataGenCompat;
import thaumcraft.util.InventoryUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FormedStructurePage implements ResearchPage {
   private static final int TITLE_OFFSET_X = 56;
   private static final int TITLE_OFFSET_Y = -10;
   private static final float TITLE_SCALE = 0.15F;
   private static final int TITLE_COLOR = 5263440;
   private static final int ASPECT_SLOT_WIDTH = 18;
   private static final int ASPECT_BASE_X = 14;
   private static final int ASPECT_BASE_Y = 175;
   private static final int ASPECT_MAX_COUNT = 5;
   private static final int ASPECT_ICON_SIZE = 12;
   private static final float ASPECT_OVERLAY_ALPHA = 0.4F;
   private static final int BLOCK_SIZE = 16;
   private static final int BLOCK_HEIGHT_OFFSET = 8;
   private static final int LAYER_HEIGHT = 50;
   private static final int BASE_X_OFFSET = 64;
   private static final int BASE_Y_OFFSET = 103;
   private static final int BASE_Z_DEPTH = 60;
   private static final int LAYER_Z_STEP = 10;
   private static final float RENDER_SCALE = 2.0F;
   private static final int SCALE_THRESHOLD_Y = 3;
   private static final float SCALE_REDUCTION_PER_LAYER = 0.2F;
   private static final float BACKGROUND_ALPHA = 0.5F;
   private static final int BACKGROUND_OFFSET_X = -8;
   private static final int BACKGROUND_BASE_LAYER_OFFSET = 25;
   private final int dx;
   private final int dy;
   private final int dz;
   @Nullable
   private final AspectList aspects;
   private final List<Object> objects;

   public FormedStructurePage(JsonObject object) {
      this.dx = GsonHelper.getAsInt(object, "dx", 1);
      this.dy = GsonHelper.getAsInt(object, "dy", 2);
      this.dz = GsonHelper.getAsInt(object, "dz", 1);
      this.aspects = parseAspects(object);
      this.objects = parseIngredients(object);
   }

   @Nullable
   private static AspectList parseAspects(JsonObject object) {
      return GsonHelper.isArrayNode(object, "aspects") ? AspectList.fromJson(GsonHelper.getAsJsonArray(object, "aspects")) : null;
   }

   private static List<Object> parseIngredients(JsonObject object) {
      List<Object> result = new ArrayList<>();

      for (JsonElement element : GsonHelper.getAsJsonArray(object, "ingredients")) {
         result.add(parseIngredient(element));
      }

      return result;
   }

   private static Object parseIngredient(JsonElement element) {
      if (GsonHelper.isStringValue(element)) {
         return Ingredient.EMPTY;
      } else if (!element.isJsonObject()) {
         return DataGenCompat.ingredientFromJson(element);
      } else {
         JsonObject jsonObject = element.getAsJsonObject();
         if (jsonObject.entrySet().isEmpty()) {
            return Ingredient.EMPTY;
         } else {
            return GsonHelper.isStringValue(jsonObject, "block") ? parseBlockIngredient(jsonObject) : DataGenCompat.ingredientFromJson(element);
         }
      }
   }

   private static Object parseBlockIngredient(JsonObject jsonObject) {
      ResourceLocation blockId = ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "block"));
      Block block = (Block)BuiltInRegistries.BLOCK.get(blockId);
      return block != null ? block : Ingredient.EMPTY;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
      this.renderTitle(graphics, x, y);
      this.renderAspects(screen, graphics, x, y, mx, my);
      this.renderStructure(screen, graphics, x, y, mx, my);
   }

   @OnlyIn(Dist.CLIENT)
   private void renderTitle(GuiGraphics graphics, int x, int y) {
      String title = I18n.get("recipe.type.construct", new Object[0]);
      ScreenApi.MAGIC.renderCenterText(graphics, title, x + 56, y + -10, 0.15F, 5263440);
   }

   @OnlyIn(Dist.CLIENT)
   private void renderAspects(ResearchRecipeScreen screen, GuiGraphics graphics, int x, int y, int mx, int my) {
      if (this.aspects != null && this.aspects.size() != 0) {
         PoseStack pose = graphics.pose();
         int count = 0;
         int aspectOffsetX = (5 - this.aspects.size()) * 8;

         for (Aspect aspect : this.aspects.getAspectsSortedAmount()) {
            int aspectX = x + 14 + 18 * count + aspectOffsetX;
            int aspectY = y + 175;
            screen.renderAspectAndTooltips(graphics, mx, my, aspect, this.aspects.getAmount(aspect), aspectX, aspectY);
            count++;
         }

         this.renderAspectOverlay(graphics, pose, x, y);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderAspectOverlay(GuiGraphics graphics, PoseStack pose, int x, int y) {
      pose.pushPose();
      graphics.setColor(1.0F, 1.0F, 1.0F, 0.4F);
      RenderSystem.enableBlend();
      pose.translate(x, y + 175 - 5, 0.0F);
      pose.scale(2.0F, 2.0F, 1.0F);
      ScreenApi.renderTextureRect(graphics, ResearchRecipeScreen.RESEARCH_BOOK_OVERLAY, 0.0F, 0.0F, 68, 76, 12.0F, 12.0F);
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      pose.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   private void renderStructure(ResearchRecipeScreen screen, GuiGraphics graphics, int x, int y, int mx, int my) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      float scale = this.calculateScale();
      if (scale < 1.0F) {
         pose.translate(x * (1.02F - scale), y * (1.06F - scale), 0.0F);
         pose.scale(scale, scale, scale);
      }

      this.renderStructureBackground(graphics, pose, x, y);
      int centeredX = x + 64 - (this.dx * 16 + this.dz * 16) / 2;
      int centeredY = y + 103 - this.dy * 25;
      this.renderBlocks(screen, graphics, pose, centeredX, centeredY, mx, my);
      pose.popPose();
   }

   private float calculateScale() {
      return this.dy > 3 ? 1.0F - (this.dy - 3) * 0.2F : 1.0F;
   }

   @OnlyIn(Dist.CLIENT)
   private void renderStructureBackground(GuiGraphics graphics, PoseStack pose, int x, int y) {
      pose.pushPose();
      graphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
      RenderSystem.enableBlend();
      int backgroundY = y - 16 - this.dy * 25 + Math.max(3 - this.dx, 3 - this.dz) * 8 + this.dx * 4 + this.dz * 4 + this.dy * 50;
      pose.translate(x + -8, backgroundY, 0.0F);
      pose.scale(2.0F, 2.0F, 1.0F);
      ScreenApi.renderTextureRect(graphics, ResearchRecipeScreen.RESEARCH_BOOK_OVERLAY, 0.0F, 0.0F, 0, 72, 64.0F, 44.0F);
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      pose.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   private void renderBlocks(ResearchRecipeScreen screen, GuiGraphics graphics, PoseStack pose, int baseX, int baseY, int mx, int my) {
      int blockIndex = 0;

      for (int layerY = 0; layerY < this.dy; layerY++) {
         for (int posZ = this.dz - 1; posZ >= 0; posZ--) {
            for (int posX = this.dx - 1; posX >= 0; posX--) {
               if (blockIndex >= this.objects.size()) {
                  return;
               }

               int pixelX = baseX + posX * 16 + posZ * 16;
               int pixelY = baseY - posX * 8 + posZ * 8 + layerY * 50;
               float zLevel = 60 - layerY * 10;
               this.renderBlockAt(screen, graphics, pose, blockIndex, pixelX, pixelY, zLevel, mx, my);
               blockIndex++;
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderBlockAt(
      ResearchRecipeScreen screen, GuiGraphics graphics, PoseStack pose, int blockIndex, int pixelX, int pixelY, float zLevel, int mx, int my
   ) {
      pose.pushPose();
      pose.translate(0.0F, 0.0F, zLevel);
      Object obj = this.objects.get(blockIndex);
      if (obj instanceof Ingredient ingredient && !ingredient.isEmpty()) {
         screen.renderItemAndTooltips(graphics, mx, my, InventoryUtils.cycleItemStack(ingredient), pixelX, pixelY);
      } else if (obj instanceof Block block) {
         this.renderBlock(screen, graphics, block, pixelX, pixelY, mx, my);
      }

      pose.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   private void renderBlock(ResearchRecipeScreen screen, GuiGraphics graphics, Block block, int pixelX, int pixelY, int mx, int my) {
      ScreenApi.renderBlock(graphics, block.defaultBlockState(), pixelX, pixelY);
      if (this.isMouseOver(mx, my, pixelX, pixelY)) {
         this.showBlockTooltip(screen, block, mx, my);
      }
   }

   private boolean isMouseOver(int mx, int my, int x, int y) {
      return mx >= x && my >= y && mx < x + 16 && my < y + 16;
   }

   private void showBlockTooltip(ResearchRecipeScreen screen, Block block, int mx, int my) {
      List<Component> components = new ArrayList<>();
      components.add(block.getName());
      if (screen.getMinecraft().options.advancedItemTooltips) {
         ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
         if (blockId != null) {
            components.add(Component.literal(blockId.toString()).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
         }
      }

      screen.setTooltipNextRender(components, mx, my);
   }
}
