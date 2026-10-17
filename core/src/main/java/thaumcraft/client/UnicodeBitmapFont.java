package thaumcraft.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class UnicodeBitmapFont {
   private static final float BASE_SCALE = 0.12F;
   private static final float RAW_GLYPH_HEIGHT = 8.0F;
   private static final float RAW_LINE_HEIGHT = 9.0F;
   private static final float WRAP_WIDTH_CORRECTION = 1.14F;
   private static final float GLYPH_UV_EPSILON = 0.0F;
   private static final float GLYPH_HEIGHT_EPSILON = 0.0F;
   private static final String FORMAT_CODES = "0123456789abcdefklmnor";
   private final byte[] glyphWidth = new byte[65536];
   private final int[] colorCode = new int[32];
   private final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
   private final Random fontRandom = new Random();
   private boolean available = true;

   public UnicodeBitmapFont() {
      this.initColorCodes();
      this.loadGlyphSizes();
   }

   public boolean isAvailable() {
      return this.available;
   }

   public float height() {
      return this.height(0.12F);
   }

   public float height(float scale) {
      return 9.0F * this.toRawScale(scale);
   }

   public float width(String text) {
      return this.width(text, 0.12F);
   }

   public float width(Component text) {
      return this.width(text.getString(), 0.12F);
   }

   public float width(String text, float scale) {
      return this.getStringWidth(text) * this.toRawScale(scale);
   }

   public float width(Component text, float scale) {
      return this.width(text.getString(), scale);
   }

   public void renderText(GuiGraphics graphics, String text, float x, float y, int color) {
      this.renderText(graphics, text, x, y, 0.12F, color);
   }

   public void renderText(GuiGraphics graphics, Component text, float x, float y, int color) {
      this.renderText(graphics, text.getString(), x, y, 0.12F, color);
   }

   public void renderText(GuiGraphics graphics, String text, float x, float y, float scale, int color) {
      if (this.available && text != null && !text.isEmpty()) {
         PoseStack pose = graphics.pose();
         pose.pushPose();
         pose.translate(x, y, 0.0F);
         float rawScale = this.toRawScale(scale);
         pose.scale(rawScale, rawScale, 1.0F);
         this.drawString(graphics, text, 0.0F, 0.0F, color);
         pose.popPose();
      }
   }

   public void renderCenterText(GuiGraphics graphics, String text, float x, float y, int color) {
      this.renderText(graphics, text, x - this.width(text) / 2.0F, y, color);
   }

   public void renderCenterText(GuiGraphics graphics, String text, float x, float y, float scale, int color) {
      this.renderText(graphics, text, x - this.width(text, scale) / 2.0F, y, scale, color);
   }

   public float renderTextToWidth(GuiGraphics graphics, String text, float x, float y, float width, int color) {
      if (!this.available) {
         return y;
      } else {
         float lineHeight = this.height();
         int rawWidth = Math.max(1, Math.round(width / (this.toRawScale(0.12F) * 1.14F)));

         for (String line : this.listFormattedStringToWidth(text, rawWidth)) {
            this.renderText(graphics, line, x, y, color);
            y += lineHeight;
         }

         return y;
      }
   }

   public List<String> listFormattedStringToWidth(String text, int width) {
      text = this.trimStringNewline(text).replace("<BR>", "\n").replace("<BR/>", "\n");
      return Arrays.asList(this.wrapFormattedStringToWidth(text, width).split("\n", -1));
   }

   private void drawString(GuiGraphics graphics, String text, float x, float y, int color) {
      int normalized = this.normalizeColor(color);
      int baseColor = normalized;
      int currentColor = normalized;
      float posX = x;
      boolean randomStyle = false;
      boolean boldStyle = false;
      boolean underlineStyle = false;
      boolean strikethroughStyle = false;
      boolean italicStyle = false;

      for (int i = 0; i < text.length(); i++) {
         char chr = text.charAt(i);
         if (chr == 167 && i + 1 < text.length()) {
            int index = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(text.charAt(i + 1)));
            if (index >= 16) {
               if (index == 16) {
                  randomStyle = true;
               } else if (index == 17) {
                  boldStyle = true;
               } else if (index == 18) {
                  strikethroughStyle = true;
               } else if (index == 19) {
                  underlineStyle = true;
               } else if (index == 20) {
                  italicStyle = true;
               } else if (index == 21) {
                  randomStyle = false;
                  boldStyle = false;
                  strikethroughStyle = false;
                  underlineStyle = false;
                  italicStyle = false;
                  currentColor = baseColor;
               }
            } else {
               randomStyle = false;
               boldStyle = false;
               strikethroughStyle = false;
               underlineStyle = false;
               italicStyle = false;
               if (index < 0 || index > 15) {
                  index = 15;
               }

               currentColor = this.normalizeColor(this.colorCode[index]);
            }

            i++;
         } else {
            char renderChar = chr;
            int rawWidth = this.getCharWidth(chr);
            if (randomStyle && rawWidth > 0) {
               renderChar = this.getRandomCharOfWidth(rawWidth);
               rawWidth = this.getCharWidth(renderChar);
            }

            if (rawWidth > 0) {
               float renderedWidth = renderChar == ' ' ? 4.0F : this.drawGlyph(graphics, renderChar, posX, y, currentColor, italicStyle);
               int advance = rawWidth;
               if (boldStyle) {
                  if (renderChar != ' ') {
                     this.drawGlyph(graphics, renderChar, posX + 0.5F, y, currentColor, italicStyle);
                  }

                  renderedWidth++;
                  advance = rawWidth + 1;
               }

               int lineColor = this.normalizeColor(currentColor);
               if (strikethroughStyle) {
                  this.fillRaw(graphics, posX, y + 4.5F, posX + renderedWidth, y + 4.5F + 1.0F, lineColor);
               }

               if (underlineStyle) {
                  this.fillRaw(graphics, posX - 1.0F, y + 9.0F - 1.0F, posX + renderedWidth, y + 9.0F, lineColor);
               }

               posX += advance;
            }
         }
      }
   }

   private float drawGlyph(GuiGraphics graphics, char chr, float x, float y, int color, boolean italic) {
      if (chr == ' ') {
         return 4.0F;
      } else if (chr < this.glyphWidth.length && this.glyphWidth[chr] != 0) {
         int start = this.glyphWidth[chr] >>> 4;
         int end = this.glyphWidth[chr] & 15;
         if (end > 7) {
            end = 15;
            start = 0;
         }

         float sourceWidth = end + 1 - start;
         float drawWidth = sourceWidth / 2.0F;
         float u1 = (chr % 16 * 16 + start) / 256.0F;
         float v1 = (chr & 255) / 16 * 16 / 256.0F;
         float skew = italic ? 1.0F : 0.0F;
         ResourceLocation page = this.getUnicodePageLocation(chr / 256);
         Matrix4f matrix = graphics.pose().last().pose();
         RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
         RenderSystem.setShaderTexture(0, page);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         GlStateManager._texParameter(3553, 10241, 9729);
         GlStateManager._texParameter(3553, 10240, 9729);
         int alpha = ARGB32.alpha(color);
         int red = ARGB32.red(color);
         int green = ARGB32.green(color);
         int blue = ARGB32.blue(color);
         float glyphWidth = sourceWidth - 0.0F;
         float glyphHeight = 16.0F;
         float drawHeight = 8.0F;
         float maxU = u1 + glyphWidth / 256.0F;
         float maxV = v1 + glyphHeight / 256.0F;
         BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         builder.addVertex(matrix, x + skew, y, 0.0F).setUv(u1, v1).setColor(red, green, blue, alpha);
         builder.addVertex(matrix, x - skew, y + drawHeight, 0.0F).setUv(u1, maxV).setColor(red, green, blue, alpha);
         builder.addVertex(matrix, x + glyphWidth / 2.0F - skew, y + drawHeight, 0.0F).setUv(maxU, maxV).setColor(red, green, blue, alpha);
         builder.addVertex(matrix, x + glyphWidth / 2.0F + skew, y, 0.0F).setUv(maxU, v1).setColor(red, green, blue, alpha);
         BufferUploader.drawWithShader(builder.buildOrThrow());
         return drawWidth + 1.0F;
      } else {
         return 0.0F;
      }
   }

   private void fillRaw(GuiGraphics graphics, float minX, float minY, float maxX, float maxY, int color) {
      graphics.fill((int)Math.floor(minX), (int)Math.floor(minY), (int)Math.ceil(maxX), (int)Math.ceil(maxY), color);
   }

   private char getRandomCharOfWidth(int width) {
      List<Character> candidates = new ArrayList<>();

      for (int i = 0; i < 256; i++) {
         char candidate = (char)i;
         if (this.getCharWidth(candidate) == width) {
            candidates.add(candidate);
         }
      }

      return candidates.isEmpty() ? '?' : candidates.get(this.fontRandom.nextInt(candidates.size()));
   }

   private int getStringWidth(String text) {
      if (text == null) {
         return 0;
      } else {
         int total = 0;
         boolean bold = false;

         for (int i = 0; i < text.length(); i++) {
            int charWidth = this.getCharWidth(text.charAt(i));
            if (charWidth < 0 && i < text.length() - 1) {
               char format = text.charAt(++i);
               if (format == 'l' || format == 'L') {
                  bold = true;
               } else if (format == 'r' || format == 'R') {
                  bold = false;
               }

               charWidth = 0;
            }

            total += charWidth;
            if (bold) {
               total++;
            }
         }

         return total;
      }
   }

   private int getCharWidth(char chr) {
      if (chr == 167) {
         return -1;
      } else if (chr == ' ') {
         return 4;
      } else if (chr >= this.glyphWidth.length) {
         return 0;
      } else if (this.glyphWidth[chr] != 0) {
         int start = this.glyphWidth[chr] >>> 4;
         int end = this.glyphWidth[chr] & 15;
         if (end > 7) {
            end = 15;
            start = 0;
         }

         end++;
         return (end - start) / 2 + 1;
      } else {
         return 0;
      }
   }

   private String trimStringNewline(String text) {
      while (text != null && text.endsWith("\n")) {
         text = text.substring(0, text.length() - 1);
      }

      return text == null ? "" : text;
   }

   private String wrapFormattedStringToWidth(String text, int width) {
      int size = this.sizeStringToWidth(text, width);
      if (text.length() <= size) {
         return text;
      } else {
         String part = text.substring(0, size);
         char splitChar = text.charAt(size);
         return part
            + "\n"
            + this.wrapFormattedStringToWidth(this.getFormatFromString(part) + text.substring(size + (splitChar != ' ' && splitChar != '\n' ? 0 : 1)), width);
      }
   }

   private int sizeStringToWidth(String text, int width) {
      int length = text.length();
      int currentWidth = 0;
      int index = 0;
      int lastSpace = -1;

      for (boolean bold = false; index < length; index++) {
         char chr = text.charAt(index);
         switch (chr) {
            case '\n':
               return index + 1;
            case ' ':
               lastSpace = index;
               break;
            case '§':
               if (index < length - 1) {
                  char format = text.charAt(++index);
                  if (format == 'l' || format == 'L') {
                     bold = true;
                  } else if (format == 'r' || format == 'R' || this.isFormatColor(format)) {
                     bold = false;
                  }
               }
               break;
            default:
               currentWidth += this.getCharWidth(chr);
               if (bold) {
                  currentWidth++;
               }
         }

         if (currentWidth > width) {
            break;
         }
      }

      return index != length && lastSpace != -1 && lastSpace < index ? lastSpace : index;
   }

   private boolean isFormatColor(char chr) {
      return chr >= '0' && chr <= '9' || chr >= 'a' && chr <= 'f' || chr >= 'A' && chr <= 'F';
   }

   private boolean isFormatSpecial(char chr) {
      return chr >= 'k' && chr <= 'o' || chr >= 'K' && chr <= 'O' || chr == 'r' || chr == 'R';
   }

   private String getFormatFromString(String text) {
      String format = "";
      int index = -1;

      while ((index = text.indexOf(167, index + 1)) != -1) {
         if (index < text.length() - 1) {
            char chr = text.charAt(index + 1);
            if (this.isFormatColor(chr)) {
               format = "§" + chr;
            } else if (this.isFormatSpecial(chr)) {
               format = format + "§" + chr;
            }
         }
      }

      return format;
   }

   private float toRawScale(float scale) {
      return Math.max(scale / 0.12F, 0.01F);
   }

   private int normalizeColor(int color) {
      return ARGB32.alpha(color) == 0 ? color | 0xFF000000 : color;
   }

   private void initColorCodes() {
      for (int i = 0; i < 32; i++) {
         int j = (i >> 3 & 1) * 85;
         int red = (i >> 2 & 1) * 170 + j;
         int green = (i >> 1 & 1) * 170 + j;
         int blue = (i & 1) * 170 + j;
         if (i == 6) {
            red += 85;
         }

         if (i >= 16) {
            red /= 4;
            green /= 4;
            blue /= 4;
         }

         this.colorCode[i] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
      }
   }

   private void loadGlyphSizes() {
      ResourceLocation glyphSizes = ResourceLocation.fromNamespaceAndPath("minecraft", "font/glyph_sizes.bin");

      try (InputStream stream = Minecraft.getInstance().getResourceManager().getResourceOrThrow(glyphSizes).open()) {
         int offset = 0;

         while (offset < this.glyphWidth.length) {
            int read = stream.read(this.glyphWidth, offset, this.glyphWidth.length - offset);
            if (read < 0) {
               throw new IOException("Unexpected end of glyph_sizes.bin");
            }

            offset += read;
         }
      } catch (Exception var7) {
         this.available = false;
      }
   }

   private ResourceLocation getUnicodePageLocation(int page) {
      ResourceLocation location = this.unicodePageLocations[page];
      if (location == null) {
         location = ResourceLocation.fromNamespaceAndPath("minecraft", String.format(Locale.ROOT, "textures/font/unicode_page_%02x.png", page));
         this.unicodePageLocations[page] = location;
      }

      return location;
   }
}
