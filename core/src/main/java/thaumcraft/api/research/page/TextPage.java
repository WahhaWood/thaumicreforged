package thaumcraft.api.research.page;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.block.table.ResearchRecipeScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TextPage implements ResearchPage {
   private static final Pattern IMG = Pattern.compile("<IMG>(.+?)</IMG>", 32);
   private static final int REALISTIC_TEXT_OFFSET_X = 10;
   private static final int ORIGINAL_TEXT_OFFSET_X = 15;
   private static final int REALISTIC_TEXT_WIDTH = 120;
   private static final int ORIGINAL_TEXT_WIDTH = 139;
   private final String key;
   private FormattedText text;

   public TextPage(String id) {
      this.key = id;
   }

   public TextPage(JsonObject object) {
      this.key = GsonHelper.getAsString(object, "key");
   }

   public FormattedText getText() {
      if (this.text == null) {
         if (this.key != null) {
            this.text = Component.literal(Language.getInstance().getOrDefault(this.key));
         } else {
            this.text = Component.EMPTY;
         }
      }

      return this.text;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
      PoseStack pose = graphics.pose();
      boolean originalMode = ScreenApi.MAGIC.isOriginalMode();
      int width = originalMode ? 139 : 120;
      pose.pushPose();
      pose.translate(x - (originalMode ? 15 : 10), y - 10, 0.0F);
      String text = Language.getInstance().getOrDefault(this.key).replace("<BR>", "\n");
      float lineHeight = ScreenApi.MAGIC.height();
      float yY = 0.0F;
      String[] strings = text.split("<LINE>");
      int end = strings.length - 1;

      for (int i = 0; i <= end; i++) {
         String s = strings[i];
         Matcher matcher = IMG.matcher(s);

         int lastIndex;
         for (lastIndex = 0; matcher.find(); lastIndex = matcher.end()) {
            String first = s.substring(lastIndex, matcher.start());
            if (!first.isEmpty()) {
               yY = ScreenApi.MAGIC.renderTextToWidth(graphics, first, 0.0F, yY, width, 0);
            }

            String[] split = matcher.group(1).split(":");
            // Кривой IMG-тег в lang (меньше 7 частей или не числа) ронял всю
            // книгу при листании: пропускаем тег как текст, а не крашимся.
            if (split.length < 7) {
               yY = ScreenApi.MAGIC.renderTextToWidth(graphics, matcher.group(0), 0.0F, yY, width, 0);
               lastIndex = matcher.end();
               continue;
            }

            float scale;
            int w;
            int h;
            int tx;
            int ty;
            try {
               scale = Float.parseFloat(split[6]);
               w = Integer.parseInt(split[4]);
               h = Integer.parseInt(split[5]);
               tx = Integer.parseInt(split[2]);
               ty = Integer.parseInt(split[3]);
            } catch (NumberFormatException ex) {
               yY = ScreenApi.MAGIC.renderTextToWidth(graphics, matcher.group(0), 0.0F, yY, width, 0);
               lastIndex = matcher.end();
               continue;
            }

            pose.pushPose();
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            pose.translate(-3.0F + width / 2.0F - w / 2.0F * scale, yY, 0.0F);
            pose.scale(scale, scale, scale);
            graphics.blit(ResourceLocation.fromNamespaceAndPath(split[0], split[1]), 0, 0, tx, ty, w, h);
            pose.popPose();
            yY += h * scale;
         }

         if (lastIndex > 0) {
            s = s.substring(lastIndex);
         }

         yY = ScreenApi.MAGIC.renderTextToWidth(graphics, s, 0.0F, yY, width, 0);
         if (i != end) {
            ScreenApi.renderTextureRect(graphics, ResearchRecipeScreen.RESEARCH_BOOK, width / 2.0F - 48.0F, yY + 6.0F, 24, 184, 96.0F, 4.0F);
            yY += lineHeight + 3.0F;
         }
      }

      pose.popPose();
   }
}
