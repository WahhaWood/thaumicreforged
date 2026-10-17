package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.client.config.BookFontMode;
import thaumcraft.client.config.ThaumcraftClientConfig;
import thaumcraft.util.CustomFont;

@OnlyIn(Dist.CLIENT)
public class ThaumonomiconFont {
   private static final float BASE_SCALE = 0.12F;
   private static final float ORIGINAL_FALLBACK_SCALE_X = 0.72F;
   private static final float ORIGINAL_FALLBACK_SCALE_Y = 0.68F;
   private final CustomFont realisticFont;
   private UnicodeBitmapFont originalFont;
   private boolean originalFontChecked;

   public ThaumonomiconFont(ResourceLocation location, boolean antialias) {
      this.realisticFont = CustomFont.create(location, antialias);
   }

   public float height() {
      if (this.getMode() == BookFontMode.REALISTIC) {
         return this.realisticFont.height();
      } else {
         return this.hasBitmapOriginal() ? this.getOriginalFont().height() : 9.0F * 0.68F;
      }
   }

   public float height(float scale) {
      if (this.getMode() == BookFontMode.REALISTIC) {
         return this.realisticFont.height(scale);
      } else {
         return this.hasBitmapOriginal() ? this.getOriginalFont().height(scale) : 9.0F * this.toVanillaScaleY(scale);
      }
   }

   public float width(Component text) {
      return this.width(text, 0.12F);
   }

   public float width(String text) {
      if (this.getMode() == BookFontMode.REALISTIC) {
         return this.realisticFont.width(text);
      } else {
         text = this.normalizeOriginalText(text);
         return this.hasBitmapOriginal() ? this.getOriginalFont().width(text) : this.vanilla().width(text) * 0.72F;
      }
   }

   public float width(Component text, float scale) {
      if (this.getMode() == BookFontMode.REALISTIC) {
         return this.realisticFont.width(text, scale);
      } else {
         String value = this.normalizeOriginalText(this.componentToString(text));
         return this.hasBitmapOriginal() ? this.getOriginalFont().width(value, scale) : this.vanilla().width(value) * this.toVanillaScaleX(scale);
      }
   }

   public float width(String text, float scale) {
      if (this.getMode() == BookFontMode.REALISTIC) {
         return this.realisticFont.width(text, scale);
      } else {
         text = this.normalizeOriginalText(text);
         return this.hasBitmapOriginal() ? this.getOriginalFont().width(text, scale) : this.vanilla().width(text) * this.toVanillaScaleX(scale);
      }
   }

   public void renderText(GuiGraphics graphics, Component text, float x, float y, int color) {
      if (this.getMode() == BookFontMode.REALISTIC) {
         this.realisticFont.renderText(graphics, text, x, y, color);
      } else {
         this.renderOriginal(graphics, this.componentToString(text), x, y, color);
      }
   }

   public void renderCenterText(GuiGraphics graphics, Component text, float x, float y, int color) {
      if (this.getMode() == BookFontMode.REALISTIC) {
         this.realisticFont.renderCenterText(graphics, text, x, y, color);
      } else {
         this.renderOriginalCenter(graphics, this.componentToString(text), x, y, color);
      }
   }

   public void renderText(GuiGraphics graphics, String text, float x, float y, int color) {
      if (this.getMode() == BookFontMode.REALISTIC) {
         this.realisticFont.renderText(graphics, text, x, y, color);
      } else {
         this.renderOriginal(graphics, text, x, y, color);
      }
   }

   public void renderCenterText(GuiGraphics graphics, String text, float x, float y, int color) {
      if (this.getMode() == BookFontMode.REALISTIC) {
         this.realisticFont.renderCenterText(graphics, text, x, y, color);
      } else {
         this.renderOriginalCenter(graphics, text, x, y, color);
      }
   }

   public float renderTextToWidth(GuiGraphics graphics, String text, float x, float y, float width, int color) {
      return this.getMode() == BookFontMode.REALISTIC
         ? this.realisticFont.renderTextToWidth(graphics, text, x, y, width, color)
         : this.renderOriginalToWidth(graphics, text, x, y, width, color);
   }

   public void renderText(GuiGraphics graphics, String text, float x, float y, float scale, int color) {
      if (this.getMode() == BookFontMode.REALISTIC) {
         this.realisticFont.renderText(graphics, text, x, y, scale, color);
      } else {
         this.renderOriginal(graphics, text, x, y, scale, color);
      }
   }

   public void renderCenterText(GuiGraphics graphics, String text, float x, float y, float scale, int color) {
      if (this.getMode() == BookFontMode.REALISTIC) {
         this.realisticFont.renderCenterText(graphics, text, x, y, scale, color);
      } else {
         this.renderOriginalCenter(graphics, text, x, y, scale, color);
      }
   }

   private BookFontMode getMode() {
      return ThaumcraftClientConfig.getBookFontMode();
   }

   public boolean isOriginalMode() {
      return this.getMode() == BookFontMode.ORIGINAL;
   }

   public boolean isRealisticMode() {
      return this.getMode() == BookFontMode.REALISTIC;
   }

   public boolean isBitmapOriginalAvailable() {
      return this.hasBitmapOriginal();
   }

   private UnicodeBitmapFont getOriginalFont() {
      if (!this.originalFontChecked) {
         this.originalFont = new UnicodeBitmapFont();
         this.originalFontChecked = true;
      }

      return this.originalFont;
   }

   private boolean hasBitmapOriginal() {
      return this.getOriginalFont().isAvailable();
   }

   private float renderOriginalToWidth(GuiGraphics graphics, String text, float x, float y, float width, int color) {
      text = this.normalizeOriginalText(text);
      if (this.hasBitmapOriginal()) {
         return this.getOriginalFont().renderTextToWidth(graphics, text, x, y, width, color);
      } else {
         float lineHeight = this.height();
         int wrapWidth = Math.max(1, Math.round(width / this.toVanillaScaleX(0.12F)));

         for (FormattedText line : this.vanilla().getSplitter().splitLines(text, wrapWidth, Style.EMPTY)) {
            this.renderOriginal(graphics, line.getString(), x, y, color);
            y += lineHeight;
         }

         return y;
      }
   }

   private void renderOriginal(GuiGraphics graphics, String text, float x, float y, int color) {
      this.renderOriginal(graphics, text, x, y, 0.12F, color);
   }

   private void renderOriginalCenter(GuiGraphics graphics, String text, float x, float y, int color) {
      this.renderOriginalCenter(graphics, text, x, y, 0.12F, color);
   }

   private void renderOriginal(GuiGraphics graphics, String text, float x, float y, float scale, int color) {
      text = this.normalizeOriginalText(text);
      if (this.hasBitmapOriginal()) {
         this.getOriginalFont().renderText(graphics, text, x, y, scale, color);
      } else {
         this.renderVanillaFallback(graphics, text, x, y, scale, color);
      }
   }

   private void renderOriginalCenter(GuiGraphics graphics, String text, float x, float y, float scale, int color) {
      if (this.hasBitmapOriginal()) {
         this.getOriginalFont().renderCenterText(graphics, text, x, y, scale, color);
      } else {
         this.renderVanillaFallback(graphics, text, x - this.width(text, scale) / 2.0F, y, scale, color);
      }
   }

   private void renderVanillaFallback(GuiGraphics graphics, String text, float x, float y, float scale, int color) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(x, y, 0.0F);
      pose.scale(this.toVanillaScaleX(scale), this.toVanillaScaleY(scale), 1.0F);
      graphics.drawString(this.vanilla(), text, 0, 0, this.normalizeColor(color), false);
      pose.popPose();
   }

   private float toVanillaScaleX(float scale) {
      return Math.max(scale / 0.12F * 0.72F, 0.01F);
   }

   private float toVanillaScaleY(float scale) {
      return Math.max(scale / 0.12F * 0.68F, 0.01F);
   }

   private int normalizeColor(int color) {
      return ARGB32.alpha(color) == 0 ? color | 0xFF000000 : color;
   }

   private String normalizeOriginalText(String text) {
      return text != null && !text.isEmpty() ? text.replace('–', '-').replace('—', '-').replace('−', '-') : text;
   }

   private Font vanilla() {
      return Minecraft.getInstance().font;
   }

   private String componentToString(Component component) {
      StringBuilder builder = new StringBuilder();
      component.visit((style, text) -> {
         if (style != Style.EMPTY) {
            if (style.isBold()) {
               text = ChatFormatting.BOLD + text;
            }

            if (style.isItalic()) {
               text = ChatFormatting.ITALIC + text;
            }

            if (style.isUnderlined()) {
               text = ChatFormatting.UNDERLINE + text;
            }

            if (style.isStrikethrough()) {
               text = ChatFormatting.STRIKETHROUGH + text;
            }

            TextColor textColor = style.getColor();
            if (textColor != null) {
               for (ChatFormatting value : ChatFormatting.values()) {
                  if (value.isColor() && value.getColor() == textColor.getValue()) {
                     text = value + text;
                     break;
                  }
               }
            }
         }

         builder.append(text);
         return Optional.empty();
      }, Style.EMPTY);
      return builder.toString();
   }
}
