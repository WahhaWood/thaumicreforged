package thaumcraft.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@OnlyIn(Dist.CLIENT)
public class CustomFont {
   private static final float DEFAULT_SCALE = 0.12F;
   private final int textureId;
   private final Char2ObjectMap<Rectangle> charMap = new Char2ObjectOpenHashMap();
   private final int textureWidth;
   private final int textureHeight;
   private final BufferedImage textureImage;
   private final Graphics2D g;
   private final int lineHeight;
   private final boolean antialias;
   private final Font mainFont;
   private final Font fallbackFont;
   private int currentX;
   private int currentY;

   private CustomFont(Font font, boolean antialias, int textureSize) {
      this.textureWidth = textureSize;
      this.textureHeight = textureSize;
      this.textureImage = new BufferedImage(this.textureWidth, this.textureHeight, 2);
      this.g = this.textureImage.createGraphics();
      this.antialias = antialias;
      this.mainFont = font;
      this.fallbackFont = new Font("SansSerif", font.getStyle(), font.getSize());
      this.g.setFont(font);
      this.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
      this.g
         .setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antialias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
      this.g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      this.g.setComposite(AlphaComposite.Src);
      this.g.setColor(new Color(0, 0, 0, 0));
      this.g.fillRect(0, 0, this.textureWidth, this.textureHeight);
      this.lineHeight = this.g.getFontMetrics().getHeight();
      ByteBuffer buffer = BufferUtils.createByteBuffer(this.textureWidth * this.textureHeight * 4);

      for (int i = 0; i < this.textureWidth * this.textureHeight; i++) {
         buffer.putInt(0);
      }

      buffer.flip();
      this.textureId = GL11.glGenTextures();
      GlStateManager._bindTexture(this.textureId);
      GlStateManager._texImage2D(3553, 0, 6408, this.textureWidth, this.textureHeight, 0, 6408, 5121, buffer.asIntBuffer());
      GlStateManager._texParameter(3553, 10241, 9987);
      GlStateManager._texParameter(3553, 10240, 9729);
      GL30.glGenerateMipmap(3553);
   }

   public float height() {
      return this.height(0.12F);
   }

   public float height(float scale) {
      return this.lineHeight * scale * 0.9F;
   }

   private void addCharacterToTexture(char c) {
      Font fontToUse = this.mainFont.canDisplay(c) ? this.mainFont : this.fallbackFont;
      boolean switched = fontToUse != this.g.getFont();
      if (switched) {
         this.g.setFont(fontToUse);
      }

      int charWidth = this.g.getFontMetrics().charWidth(c);
      if (charWidth <= 0) {
         charWidth = this.g.getFontMetrics().charWidth('?');
      }

      if (this.currentX + charWidth >= this.textureWidth) {
         this.currentX = 0;
         this.currentY = this.currentY + this.lineHeight;
      }

      if (this.currentY + this.lineHeight > this.textureHeight) {
         if (switched) {
            this.g.setFont(this.mainFont);
         }
      } else {
         this.g.setColor(Color.WHITE);
         this.g.drawString(String.valueOf(c), this.currentX, this.currentY + this.g.getFontMetrics().getAscent());
         this.charMap.put(c, new Rectangle(this.currentX, this.currentY, charWidth, this.lineHeight));
         this.updateTextureRegion(this.currentX, this.currentY, charWidth, this.lineHeight);
         this.currentX += charWidth;
         if (switched) {
            this.g.setFont(this.mainFont);
         }
      }
   }

   private void updateTextureRegion(int x, int y, int width, int height) {
      int[] pixels = new int[width * height];
      this.textureImage.getRGB(x, y, width, height, pixels, 0, width);
      ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

      for (int pixel : pixels) {
         buffer.put((byte)(pixel >> 16 & 0xFF));
         buffer.put((byte)(pixel >> 8 & 0xFF));
         buffer.put((byte)(pixel & 0xFF));
         buffer.put((byte)(pixel >> 24 & 0xFF));
      }

      buffer.flip();
      GL11.glBindTexture(3553, this.textureId);
      GL11.glTexSubImage2D(3553, 0, x, y, width, height, 6408, 5121, buffer);
      GL30.glGenerateMipmap(3553);
   }

   public List<String> split(Component text, float width) {
      return this.split(text, width, 0.12F);
   }

   public List<String> split(String text, float width) {
      return this.split(text, width, 0.12F);
   }

   public List<String> split(Component text, float width, float scale) {
      return this.split(this.componentToString(text), width, scale);
   }

   public List<String> split(String text, float width, float scale) {
      List<String> strings = new ArrayList<>();
      float totalWidth = 0.0F;
      float ww = this.width(" ", scale);
      String[] strs = text.split(" ");
      int length = strs.length;
      StringBuilder str = new StringBuilder();

      for (int i = 0; i < length; i++) {
         String s = strs[i];
         float w = this.width(s, scale);
         if (totalWidth + w > width) {
            strings.add(str.toString().trim());
            totalWidth = 0.0F;
            str = new StringBuilder();
         }

         str.append(s);
         if (i != length - 1) {
            str.append(" ");
            totalWidth += ww;
         }

         totalWidth += w;
      }

      strings.add(str.toString().trim());
      return strings;
   }

   public float width(Component text) {
      return this.width(text, 0.12F);
   }

   public float width(String text) {
      return this.width(text, 0.12F);
   }

   public float width(Component text, float scale) {
      return this.width(this.componentToString(text), scale);
   }

   public float width(String text, float scale) {
      float totalWidth = 0.0F;
      char[] chars = text.toCharArray();

      for (int i = 0; i < chars.length; i++) {
         char c = chars[i];
         if (c != '\n') {
            if (c == 167 && i + 1 < chars.length) {
               char code = chars[i + 1];
               ChatFormatting formatting = ChatFormatting.getByCode(code);
               if (formatting != null) {
                  i++;
                  continue;
               }
            }

            if (!this.charMap.containsKey(c)) {
               this.addCharacterToTexture(c);
            }

            Rectangle charRect = (Rectangle)this.charMap.get(c);
            if (charRect != null) {
               totalWidth += charRect.width * scale;
            }
         }
      }

      return totalWidth;
   }

   @OnlyIn(Dist.CLIENT)
   public void renderText(GuiGraphics graphics, Component text, float x, float y, int color) {
      this.renderText(graphics, this.componentToString(text), x, y, 0.12F, -1.0F, color);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderCenterText(GuiGraphics graphics, Component text, float x, float y, int color) {
      this.renderCenterText(graphics, this.componentToString(text), x, y, color);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderText(GuiGraphics graphics, String text, float x, float y, int color) {
      this.renderText(graphics, text, x, y, 0.12F, -1.0F, color);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderCenterText(GuiGraphics graphics, String text, float x, float y, int color) {
      this.renderText(graphics, text, x - this.width(text) / 2.0F, y, 0.12F, -1.0F, color);
   }

   @OnlyIn(Dist.CLIENT)
   public float renderTextToWidth(GuiGraphics graphics, String text, float x, float y, float width, int color) {
      return this.renderText(graphics, text, x, y, 0.12F, width, color);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderText(GuiGraphics graphics, String text, float x, float y, float scale, int color) {
      this.renderText(graphics, text, x, y, scale, -1.0F, color);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderCenterText(GuiGraphics graphics, String text, float x, float y, float scale, int color) {
      this.renderText(graphics, text, x - this.width(text, scale) / 2.0F, y, scale, -1.0F, color);
   }

   @OnlyIn(Dist.CLIENT)
   public float renderText(GuiGraphics graphics, String text, float x, float y, float scale, float width, int color) {
      int alpha = this.getAlphaI(color);
      int red = ARGB32.red(color);
      int green = ARGB32.green(color);
      int blue = ARGB32.blue(color);
      float height = this.height(scale);
      if (!(width > 0.0F)) {
         this.renderText(graphics, text, x, y, scale, -1.0F, red, green, blue, alpha);
         return y + height;
      } else {
         for (String split : text.split("\n")) {
            List<String> stringList = this.split(split, width);
            int lastSize = stringList.size() - 1;

            for (int i = 0; i <= lastSize; i++) {
               String s = stringList.get(i);
               this.renderText(graphics, s, x, y, scale, i == lastSize ? -1.0F : width, red, green, blue, alpha);
               y += height;
            }
         }

         return y;
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderText(GuiGraphics graphics, String text, float x, float y, float scale, float width, int red, int green, int blue, int alpha) {
      Matrix4f matrix4f = graphics.pose().last().pose();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderTexture(0, this.textureId);
      if (this.antialias) {
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
      }

      Tesselator tessellator = Tesselator.getInstance();
      BufferBuilder buffer = tessellator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      float textureWidthF = this.textureWidth;
      float textureHeightF = this.textureHeight;
      int r = red;
      int g = green;
      int b = blue;
      boolean bold = false;
      boolean underline = false;
      boolean strikethrough = false;
      float stepX = 0.0F;
      if (width > 0.0F) {
         int count = text.split(" ", -1).length - 1;
         if (count > 1) {
            stepX = Math.max(0.0F, (width - this.width(text, scale)) / count);
         }
      }

      float lineHeight = this.height(scale);
      Rectangle lineRect = null;
      char[] chars = text.toCharArray();

      for (int i = 0; i < chars.length; i++) {
         char c = chars[i];
         if (c == 167 && i + 1 < chars.length) {
            ChatFormatting formatting = ChatFormatting.getByCode(chars[i + 1]);
            if (formatting != null) {
               if (formatting == ChatFormatting.RESET) {
                  r = red;
                  g = green;
                  b = blue;
                  bold = false;
                  underline = false;
                  strikethrough = false;
               } else if (formatting == ChatFormatting.BOLD) {
                  bold = true;
               } else if (formatting == ChatFormatting.UNDERLINE) {
                  underline = true;
               } else if (formatting == ChatFormatting.STRIKETHROUGH) {
                  strikethrough = true;
               } else if (formatting.isColor()) {
                  int color = formatting.getColor();
                  r = color >> 16 & 0xFF;
                  g = color >> 8 & 0xFF;
                  b = color & 0xFF;
               }

               i++;
               continue;
            }
         }

         if (!this.charMap.containsKey(c)) {
            this.addCharacterToTexture(c);
         }

         Rectangle charRect = (Rectangle)this.charMap.get(c);
         if (charRect != null) {
            if (c == ' ') {
               x += charRect.width * scale + stepX;
            } else {
               float u1 = charRect.x / textureWidthF;
               float v1 = charRect.y / textureHeightF;
               float u2 = (charRect.x + charRect.width) / textureWidthF;
               float v2 = (charRect.y + charRect.height) / textureHeightF;
               float charWidth = charRect.width * scale;
               float charHeight = charRect.height * scale;
               buffer.addVertex(matrix4f, x, y, 0.0F).setUv(u1, v1).setColor(r, g, b, alpha);
               buffer.addVertex(matrix4f, x, y + charHeight, 0.0F).setUv(u1, v2).setColor(r, g, b, alpha);
               buffer.addVertex(matrix4f, x + charWidth, y + charHeight, 0.0F).setUv(u2, v2).setColor(r, g, b, alpha);
               buffer.addVertex(matrix4f, x + charWidth, y, 0.0F).setUv(u2, v1).setColor(r, g, b, alpha);
               if (bold) {
                  buffer.addVertex(matrix4f, x + 0.05F, y, 0.0F).setUv(u1, v1).setColor(r, g, b, alpha);
                  buffer.addVertex(matrix4f, x + 0.05F, y + charHeight, 0.0F).setUv(u1, v2).setColor(r, g, b, alpha);
                  buffer.addVertex(matrix4f, x + 0.05F + charWidth, y + charHeight, 0.0F).setUv(u2, v2).setColor(r, g, b, alpha);
                  buffer.addVertex(matrix4f, x + 0.05F + charWidth, y, 0.0F).setUv(u2, v1).setColor(r, g, b, alpha);
               }

               if (underline || strikethrough) {
                  if (lineRect == null) {
                     if (!this.charMap.containsKey('_')) {
                        this.addCharacterToTexture('_');
                     }

                     lineRect = (Rectangle)this.charMap.get('_');
                  }

                  if (lineRect != null) {
                     u1 = lineRect.x / textureWidthF;
                     v1 = lineRect.y / textureHeightF;
                     u2 = (lineRect.x + lineRect.width) / textureWidthF;
                     v2 = (lineRect.y + lineRect.height) / textureHeightF;
                     float cw = lineRect.width * scale;
                     float ch = lineRect.height * scale;
                     float w = charWidth / 2.0F;
                     if (underline) {
                        buffer.addVertex(matrix4f, x - w, y, 0.0F).setUv(u1, v1).setColor(r, g, b, alpha);
                        buffer.addVertex(matrix4f, x - w, y + ch, 0.0F).setUv(u1, v2).setColor(r, g, b, alpha);
                        buffer.addVertex(matrix4f, x + cw + 2.0F * w, y + ch, 0.0F).setUv(u2, v2).setColor(r, g, b, alpha);
                        buffer.addVertex(matrix4f, x + cw + 2.0F * w, y, 0.0F).setUv(u2, v1).setColor(r, g, b, alpha);
                     }

                     if (strikethrough) {
                        float yy = y - lineHeight / 2.5F;
                        buffer.addVertex(matrix4f, x - w, yy, 0.0F).setUv(u1, v1).setColor(r, g, b, alpha);
                        buffer.addVertex(matrix4f, x - w, yy + ch, 0.0F).setUv(u1, v2).setColor(r, g, b, alpha);
                        buffer.addVertex(matrix4f, x + cw + 2.0F * w, yy + ch, 0.0F).setUv(u2, v2).setColor(r, g, b, alpha);
                        buffer.addVertex(matrix4f, x + cw + 2.0F * w, yy, 0.0F).setUv(u2, v1).setColor(r, g, b, alpha);
                     }
                  }
               }

               x += charWidth;
            }
         }
      }

      BufferUploader.drawWithShader(buffer.buildOrThrow());
   }

   public void cleanup() {
      RenderSystem.deleteTexture(this.textureId);
      this.g.dispose();
   }

   public String componentToString(Component component) {
      StringBuilder builder = new StringBuilder();
      component.visit((style, s) -> {
         if (style != Style.EMPTY) {
            if (style.isBold()) {
               s = ChatFormatting.BOLD + s;
            }

            if (style.isUnderlined()) {
               s = ChatFormatting.UNDERLINE + s;
            }

            if (style.isStrikethrough()) {
               s = ChatFormatting.STRIKETHROUGH + s;
            }

            TextColor textColor = style.getColor();
            if (textColor != null) {
               for (ChatFormatting value : ChatFormatting.values()) {
                  if (value.isColor() && value.getColor() == textColor.getValue()) {
                     s = value + s;
                     break;
                  }
               }
            }
         }

         builder.append(s);
         return Optional.empty();
      }, Style.EMPTY);
      return builder.toString();
   }

   private int getAlphaI(int color) {
      int alpha = ARGB32.alpha(color);
      return alpha == 0 ? 255 : alpha;
   }

   @OnlyIn(Dist.CLIENT)
   public static CustomFont create(ResourceLocation location, boolean antialias) {
      try {
         InputStream stream = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location).open();
         Font font = Font.createFont(0, stream);
         return new CustomFont(font.deriveFont(72.0F), antialias, 2048);
      } catch (Throwable var4) {
         throw new RuntimeException(var4);
      }
   }
}
