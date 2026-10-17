package thaumcraft.block.table;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.playerdata.PacketAspectCombinationToServer;
import thaumcraft.network.playerdata.PacketAspectPlaceToServer;
import thaumcraft.particles.fx.ParticleUtils;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchNoteData;
import thaumcraft.util.HexUtils;
import thaumcraft.util.PlayerNotifications;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {
   private final HashMap<String, ResearchTableScreen.Rune> runes = new HashMap<>();
   private final HashMap<String, HexUtils.Hex[]> lines = new HashMap<>();
   private final ArrayList<String> checked = new ArrayList<>();
   private final ArrayList<String> highlight = new ArrayList<>();
   private final long butcount1 = 0L;
   private long butcount2 = 0L;
   private int page = 0;
   private int lastPage = 0;
   private int isMouseButtonDown = 0;
   public Aspect select1 = null;
   public Aspect select2 = null;
   private Aspect draggedAspect;
   public ResearchNoteData note = null;
   long lastRuneCheck = 0L;
   private final boolean visibleAspectComponents;
   private final boolean enableAspectCombiner;
   private final boolean enableDuplicateNote;
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/research_table.png");
   private final PlayerKnowledge knowledge;

   public ResearchTableScreen(ResearchTableMenu menu, Inventory inventory, Component component) {
      super(menu, inventory, component);
      this.imageWidth = 255;
      this.imageHeight = 255;
      this.knowledge = PlayerKnowledge.of(inventory.player);
      this.visibleAspectComponents = this.knowledge.getResearchCompleted().contains("researcher1");
      this.enableAspectCombiner = this.knowledge.getResearchCompleted().contains("researcher2");
      this.enableDuplicateNote = this.knowledge.getResearchCompleted().contains("researchdupe");
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      long time = System.nanoTime() / 1000000L;
      if (PlayerNotifications.getListAndUpdate(time).size() > 0) {
         Thaumcraft.instance.renderEventHandler.renderNotifyHUD(graphics, this.width, this.height, time);
      }
   }

   public void render(@NotNull GuiGraphics graphics, int mx, int my, float pt) {
      super.render(graphics, mx, my, pt);
      int x = this.leftPos;
      int y = this.topPos;
      if (this.note != null && this.enableDuplicateNote && this.note.isComplete()) {
         int var7 = mx - (x + 37);
         int var8 = my - (y + 5);
         if (var7 >= 0 && var8 >= 0 && var7 < 24 && var8 < 24) {
            RenderSystem.enableBlend();
            graphics.blit(this.texture, x + 100, y + 21, 184, 224, 48, 16);
            ResearchItem research = ResearchApi.getResearch(this.note.key);
            if (research != null) {
               AspectList al = research.tags.copy();

               for (Aspect aspect : al.getAspects()) {
                  al.add(aspect, this.note.copies);
               }

               int count = 0;

               for (Aspect aspect : al.getAspectsSorted()) {
                  UtilsFX.drawTag(graphics, x + 100 + 48 + count * 16, y + 21, aspect, al.getAmount(aspect), 0, 0.0F);
                  count++;
               }
            }

            graphics.drawString(this.font, Component.translatable("tc.research.copy"), x + 100, y + 12, -1);
         }
      }

      if (this.isMouseButtonDown == 1 && this.draggedAspect != null) {
         // Перетаскиваемый аспект рисуется увеличенным и с ореолом — иначе его
         // легко не отличить от уже лежащего на гексе (жалоба: «непонятно, что
         // перетаскиваешь»). Плюс подсвечиваем гекс-приёмник.
         RenderSystem.enableBlend();
         PoseStack dragPose = graphics.pose();
         dragPose.pushPose();
         dragPose.translate((float)mx, (float)my, 0.0F);
         dragPose.scale(0.8F, 0.8F, 1.0F);
         dragPose.translate(-8.0F, -8.0F, 0.0F);
         // Подсветка-ореол, чтобы аспект читался на любом фоне...
         this.drawOrbScaled(graphics, 0.0F, 0.0F, this.draggedAspect.getColor(), 1.0F);
         // ...плюс сама ИКОНКА аспекта: drawTag рисуется тем же путём, что и
         // теги на пергаменте (они видны всегда), поэтому тащимый аспект
         // гарантированно виден и сразу понятно, какой именно он.
         UtilsFX.drawTag(graphics, 0, 0, this.draggedAspect, 0.0F, 0, 0.0F, 771, 1.0F);
         dragPose.popPose();
         RenderSystem.disableBlend();
      }

      this.drawAspectText(graphics, x + 10, y + 40, mx, my);
      ItemStack ink = ((ResearchTableMenu)this.menu).table.getItem(0);
      if (this.note != null && (ink.isEmpty() || ink.getDamageValue() == ink.getMaxDamage())) {
         Component component0 = Component.translatable("tip.thaumcraft.research.noink.0");
         Component component1 = Component.translatable("tip.thaumcraft.research.noink.1");
         int sx = Math.max(this.font.width(component0), this.font.width(component1)) / 2;
         graphics.renderComponentTooltip(this.font, List.of(component0, component1), x + 157 - sx, y + 84);
      }

      this.renderTooltip(graphics, mx, my);
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      RenderSystem.enableBlend();
      graphics.blit(this.texture, x, y, 0, 0, 255, 167);
      graphics.blit(this.texture, x + 40, y + 167, 0, 166, 184, 88);
      if (this.page < this.lastPage) {
         graphics.blit(this.texture, x + 51, y + 121, 208, 208, 24, 8);
      }

      if (this.page > 0) {
         graphics.blit(this.texture, x + 27, y + 121, 184, 208, 24, 8);
      }

      if (this.butcount2 < System.nanoTime() && this.select1 != null && this.select2 != null) {
         graphics.blit(this.texture, x + 35, y + 139, 184, 184, 32, 16);
         this.drawOrb(graphics, x + 43, y + 139);
      } else if (this.butcount2 >= System.nanoTime() && this.select1 != null && this.select2 != null) {
         graphics.blit(this.texture, x + 35, y + 139, 184, 184, 32, 16);
         graphics.blit(this.texture, x + 35, y + 139, 184, 168, 32, 16);
      }

      if (this.enableDuplicateNote && this.note != null && this.note.isComplete()) {
         graphics.blit(this.texture, x + 37, y + 5, 232, 200, 24, 24);
      }

      this.drawAspects(graphics, x + 10, y + 40);
      this.drawResearchData(graphics, x, y, mouseX, mouseY);
   }

   public boolean mouseReleased(double mx, double my, int button) {
      if (this.isMouseButtonDown == 1 && this.draggedAspect != null) {
         if (this.note != null) {
            double mouseX = mx - (this.leftPos + 169);
            double mouseY = my - (this.topPos + 83);
            HexUtils.Hex hp = new HexUtils.Pixel(mouseX, mouseY).toHex(9);
            if (!this.note.isComplete()
               && this.note.hexEntries.containsKey(hp.toString())
               && this.note.hexEntries.get(hp.toString()).type == 0) {
               this.playButtonCombine();
               this.playButtonWrite();
               PacketHandler.sendToServer(new PacketAspectPlaceToServer((byte)hp.q, (byte)hp.r, this.draggedAspect));
               this.draggedAspect = null;
            }
         }

         if (this.draggedAspect != null) {
            boolean skip = false;
            double mouseX = mx - (this.leftPos + 20);
            double mouseY = my - (this.topPos + 146);
            if (mouseX >= -16.0 && mouseY >= -16.0 && mouseX < 16.0 && mouseY < 16.0) {
               this.playButtonAspect();
               this.select1 = this.draggedAspect;
               skip = true;
            }

            mouseX = mx - (this.leftPos + 79);
            mouseY = my - (this.topPos + 146);
            if (!skip && mouseX >= -16.0 && mouseY >= -16.0 && mouseX < 16.0 && mouseY < 16.0) {
               this.playButtonAspect();
               this.select2 = this.draggedAspect;
               skip = true;
            }

            if (!skip) {
               Aspect aspect = this.getClickedAspect(mx, my, this.leftPos, this.topPos, false);
               if (aspect == this.draggedAspect) {
                  if (this.select1 == null) {
                     this.select1 = this.draggedAspect;
                  } else if (this.select2 == null) {
                     this.select2 = this.draggedAspect;
                  }
               }
            }
         }
      }

      this.isMouseButtonDown = 0;
      this.draggedAspect = null;
      return super.mouseReleased(mx, my, button);
   }

   private void drawAspects(GuiGraphics graphics, int x, int y) {
      AspectList aspects = this.knowledge.getAspectsDiscovered();
      int count = aspects.size();
      this.lastPage = (count - 20) / 5;
      count = 0;
      int drawn = 0;

      for (Aspect aspect : aspects.getAspectsSorted()) {
         if (aspect == null) {
            return;
         }

         if (((ResearchTableMenu)this.menu).table == null || ((ResearchTableMenu)this.menu).table.bonusAspects == null) {
            return;
         }

         if (++count - 1 >= this.page * 5 && drawn < 25) {
            boolean faded = aspects.getAmount(aspect) <= 0 && ((ResearchTableMenu)this.menu).table.bonusAspects.getAmount(aspect) <= 0;
            int xx = drawn / 5 * 16;
            int yy = drawn % 5 * 16;
            UtilsFX.drawTag(
               graphics,
               x + xx,
               y + yy,
               aspect,
               aspects.getAmount(aspect),
               ((ResearchTableMenu)this.menu).table.bonusAspects.getAmount(aspect),
               0.0F,
               771,
               faded ? 0.33F : 1.0F
            );
            drawn++;
         }
      }

      if (((ResearchTableMenu)this.menu).table != null && ((ResearchTableMenu)this.menu).table.bonusAspects != null) {
         if (this.select1 != null
            && this.knowledge.getAspectPoolFor(this.select1) <= 0
            && ((ResearchTableMenu)this.menu).table.bonusAspects.getAmount(this.select1) <= 0) {
            this.select1 = null;
         }

         if (this.select2 != null
            && this.knowledge.getAspectPoolFor(this.select2) <= 0
            && ((ResearchTableMenu)this.menu).table.bonusAspects.getAmount(this.select2) <= 0) {
            this.select2 = null;
         }

         if (this.select1 != null) {
            UtilsFX.drawTag(graphics, x + 3, y + 99, this.select1, 0.0F, 0, 0.0F);
         }

         if (this.select2 != null) {
            UtilsFX.drawTag(graphics, x + 61, y + 99, this.select2, 0.0F, 0, 0.0F);
         }
      }
   }

   private void drawAspectText(GuiGraphics graphics, int x, int y, int mx, int my) {
      AspectList aspects = this.knowledge.getAspectsDiscovered();
      int count = 0;
      int drawn = 0;
      PoseStack pose = graphics.pose();

      for (Aspect aspect : aspects.getAspectsSorted()) {
         if (++count - 1 >= this.page * 5 && drawn < 25) {
            int xx = drawn / 5 * 16;
            int yy = drawn % 5 * 16;
            int var7 = mx - (x + xx);
            int var8 = my - (y + yy);
            if (var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16) {
               graphics.renderTooltip(this.font, Arrays.asList(aspect.getName(), aspect.getLocalizedDescription()), Optional.empty(), mx, my - 8);
               if (this.visibleAspectComponents && !aspect.isPrimal()) {
                  ResourceLocation location = Thaumcraft.location("textures/aspects/_back.png");
                  pose.pushPose();
                  RenderSystem.enableBlend();
                  RenderSystem.blendFunc(770, 771);
                  pose.translate(0.0F, 0.0F, 200.0F);
                  ScreenApi.renderTextureQuad(graphics, location, mx + 6, my + 6, 20.0, 20.0);
                  ScreenApi.renderTextureQuad(graphics, location, mx + 24, my + 6, 20.0, 20.0);
                  UtilsFX.drawTag(graphics, mx + 26, my + 8, aspect.getComponents()[1], 0.0F, 0, 0.0F);
                  UtilsFX.drawTag(graphics, mx + 8, my + 8, aspect.getComponents()[0], 0.0F, 0, 0.0F);
                  RenderSystem.disableBlend();
                  pose.popPose();
               }

               return;
            }

            drawn++;
         }
      }

      if (this.select1 != null) {
         int var7 = mx - (x + 3);
         int var8 = my - (y + 99);
         if (var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16) {
            graphics.renderTooltip(this.font, Arrays.asList(this.select1.getName(), this.select1.getLocalizedDescription()), Optional.empty(), mx, my - 8);
            return;
         }
      }

      if (this.select2 != null) {
         int var7 = mx - (x + 61);
         int var8 = my - (y + 99);
         if (var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16) {
            graphics.renderTooltip(this.font, Arrays.asList(this.select2.getName(), this.select2.getLocalizedDescription()), Optional.empty(), mx, my - 8);
         }
      }
   }

   private void drawResearchData(GuiGraphics graphics, int x, int y, int mx, int my) {
      graphics.pose().pushPose();
      RenderSystem.enableBlend();
      this.drawSheet(graphics, x, y, mx, my);
      graphics.pose().popPose();
   }

   private void drawHex(GuiGraphics graphics, HexUtils.Hex hex, int x, int y) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, Thaumcraft.location("textures/screen/hex1.png"));
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      HexUtils.Pixel pix = hex.toPixel(9);
      pose.translate(x + pix.x, y + pix.y, 0.0);
      BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      Matrix4f matrix4f = pose.last().pose();
      builder.addVertex(matrix4f, -8.0F, 8.0F, 0.0F).setUv(0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 0.25F);
      builder.addVertex(matrix4f, 8.0F, 8.0F, 0.0F).setUv(1.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 0.25F);
      builder.addVertex(matrix4f, 8.0F, -8.0F, 0.0F).setUv(1.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 0.25F);
      builder.addVertex(matrix4f, -8.0F, -8.0F, 0.0F).setUv(0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 0.25F);
      BufferUploader.drawWithShader(builder.buildOrThrow());
      pose.popPose();
   }

   private void drawHexHighlight(GuiGraphics graphics, HexUtils.Hex hex, int x, int y) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShaderTexture(0, Thaumcraft.location("textures/screen/hex2.png"));
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      HexUtils.Pixel pix = hex.toPixel(9);
      pose.translate(x + pix.x, y + pix.y, 0.0);
      BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      Matrix4f matrix4f = pose.last().pose();
      builder.addVertex(matrix4f, -8.0F, 8.0F, 0.0F).setUv(0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 0.25F);
      builder.addVertex(matrix4f, 8.0F, 8.0F, 0.0F).setUv(1.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 0.25F);
      builder.addVertex(matrix4f, 8.0F, -8.0F, 0.0F).setUv(1.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 0.25F);
      builder.addVertex(matrix4f, -8.0F, -8.0F, 0.0F).setUv(0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 0.25F);
      BufferUploader.drawWithShader(builder.buildOrThrow());
      RenderSystem.blendFunc(770, 771);
      pose.popPose();
   }

   /**
    * Линии связей между аспектами — нарисованы заново.
    *
    * <p>Раньше здесь были GL-линии (`RenderType.lineStrip` + `lineWidth(3)`).
    * В современном OpenGL толщину линий ядро не поддерживает (значения больше
    * 1 либо игнорируются, либо клампятся), поэтому линии выглядели рваными,
    * «плавали» по толщине и не читались. Теперь каждая связь — это
    * прямоугольник (две треугольника) постоянной толщины, построенный по
    * нормали к отрезку. Толщина, цвет и свечение полностью под нашим контролем.
    */
   private void drawConnectionLines(GuiGraphics graphics, int x, int y) {
      if (this.lines.isEmpty()) {
         return;
      }

      final float thickness = 2.0F;
      int tick = this.minecraft.player.tickCount;
      float alpha = 0.55F + Mth.sin(tick / 8.0F) * 0.15F;

      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      RenderSystem.disableCull();
      BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      Matrix4f mat = graphics.pose().last().pose();

      for (HexUtils.Hex[] con : this.lines.values()) {
         HexUtils.Pixel p1 = con[0].toPixel(9);
         HexUtils.Pixel p2 = con[1].toPixel(9);
         float x1 = (float)(x + 169 + p1.x);
         float y1 = (float)(y + 83 + p1.y);
         float x2 = (float)(x + 169 + p2.x);
         float y2 = (float)(y + 83 + p2.y);
         float dx = x2 - x1;
         float dy = y2 - y1;
         float len = Mth.sqrt(dx * dx + dy * dy);
         if (len < 0.001F) {
            continue;
         }

         // Нормаль к отрезку, длиной в половину толщины.
         float nx = -dy / len * thickness * 0.5F;
         float ny = dx / len * thickness * 0.5F;

         builder.addVertex(mat, x1 + nx, y1 + ny, 0.0F).setColor(0.35F, 0.85F, 1.0F, alpha);
         builder.addVertex(mat, x2 + nx, y2 + ny, 0.0F).setColor(0.35F, 0.85F, 1.0F, alpha);
         builder.addVertex(mat, x2 - nx, y2 - ny, 0.0F).setColor(0.35F, 0.85F, 1.0F, alpha);
         builder.addVertex(mat, x1 - nx, y1 - ny, 0.0F).setColor(0.35F, 0.85F, 1.0F, alpha);
      }

      BufferUploader.drawWithShader(builder.buildOrThrow());
      RenderSystem.enableCull();
      RenderSystem.blendFunc(770, 771);
      RenderSystem.disableBlend();
   }

   private void drawSheet(GuiGraphics graphics, int x, int y, int mx, int my) {
      // У пустышки (чанк не догружен) уровня нет — рисуем пустой пергамент,
      // иначе level.random ниже бросит NPE.
      if (((ResearchTableMenu)this.menu).table != null
         && ((ResearchTableMenu)this.menu).table.getLevel() != null) {
         PoseStack pose = graphics.pose();
         Level level = ((ResearchTableMenu)this.menu).table.getLevel();
         this.note = ResearchManager.getData(((ResearchTableMenu)this.menu).table.getItem(1));
         if (this.note != null && this.note.key != null && this.note.key.length() != 0) {
            graphics.blit(Thaumcraft.location("textures/misc/parchment3.png"), x + 94, y + 8, 0, 0, 150, 150);
            long time = System.currentTimeMillis();
            if (this.lastRuneCheck < time) {
               this.lastRuneCheck = time + 250L;
               int k = level.random.nextInt(120) - 60;
               int l = level.random.nextInt(120) - 60;
               HexUtils.Hex hp = new HexUtils.Pixel(k, l).toHex(9);
               if (!this.runes.containsKey(hp.toString()) && !this.note.hexes.containsKey(hp.toString())) {
                  this.runes
                     .put(
                        hp.toString(),
                        new ResearchTableScreen.Rune(hp.q, hp.r, time, this.lastRuneCheck + 15000L + level.random.nextInt(10000), level.random.nextInt(16))
                     );
               }
            }

            if (this.runes.size() > 0) {
               Iterator<Entry<String, ResearchTableScreen.Rune>> iterator = this.runes.entrySet().iterator();

               while (iterator.hasNext()) {
                  Entry<String, ResearchTableScreen.Rune> entry = iterator.next();
                  ResearchTableScreen.Rune rune = entry.getValue();
                  if (rune.decay >= time) {
                     HexUtils.Pixel pix = new HexUtils.Hex(rune.q, rune.r).toPixel(9);
                     float progress = (float)(time - rune.start) / (float)(rune.decay - rune.start);
                     float alpha = 0.5F;
                     if (progress < 0.25F) {
                        alpha = progress * 2.0F;
                     } else if (progress > 0.5F) {
                        alpha = 1.0F - progress;
                     }

                     this.drawRune(graphics, x + 169 + pix.x, y + 83 + pix.y, rune.rune, alpha * 0.66F);
                  } else {
                     iterator.remove();
                  }
               }
            }

            int mouseX = mx - (x + 169);
            int mouseY = my - (y + 83);
            HexUtils.Hex hp = new HexUtils.Pixel(mouseX, mouseY).toHex(9);
            this.lines.clear();
            this.checked.clear();
            this.highlight.clear();

            for (HexUtils.Hex hex : this.note.hexes.values()) {
               if (this.note.hexEntries.get(hex.toString()).type == 1 && this.knowledge.hasDiscoveredAspect(this.note.hexEntries.get(hex.toString()).aspect)) {
                  this.checkConnections(hex);
               }
            }

            this.drawConnectionLines(graphics, x, y);

            UtilsFX.bindTexture("textures/screen/hex1.png");
            pose.pushPose();
            if (!this.note.isComplete()) {
               for (HexUtils.Hex hexx : this.note.hexes.values()) {
                  if (this.note.hexEntries.get(hexx.toString()).type != 1) {
                     if (!this.note.isComplete()) {
                        if (hexx.equals(hp)) {
                           this.drawHexHighlight(graphics, hexx, x + 169, y + 83);
                        }

                        this.drawHex(graphics, hexx, x + 169, y + 83);
                     }
                  } else {
                     this.drawOrb(graphics, x + 161 + hexx.toPixel(9).x, y + 75 + hexx.toPixel(9).y);
                  }
               }
            }

            pose.translate(0.0F, 0.0F, 40.0F);

            for (HexUtils.Hex hexxx : this.note.hexes.values()) {
               if (this.note.hexEntries.get(hexxx.toString()).aspect != null
                  && !this.knowledge.hasDiscoveredAspect(this.note.hexEntries.get(hexxx.toString()).aspect)) {
                  HexUtils.Pixel pix = hexxx.toPixel(9);
                  pose.pushPose();
                  graphics.setColor(0.0F, 0.0F, 0.0F, 0.5F);
                  RenderSystem.enableBlend();
                  RenderSystem.blendFunc(770, 771);
                  pose.translate(x + 161 + pix.x, y + 75 + pix.y, 0.0);
                  UtilsFX.drawTexturedQuadFull(graphics, Thaumcraft.location("textures/aspects/_unknown.png"), 0, 0, 0);
                  graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                  RenderSystem.disableBlend();
                  pose.popPose();
               } else if (this.note.hexEntries.get(hexxx.toString()).type == 1 || this.highlight.contains(hexxx.toString())) {
                  HexUtils.Pixel pix = hexxx.toPixel(9);
                  UtilsFX.drawTag(
                     graphics,
                     (float)(x + 161 + pix.x),
                     (float)(y + 75 + pix.y),
                     this.note.hexEntries.get(hexxx.toString()).aspect,
                     0.0F,
                     0,
                     0.0F,
                     771,
                     1.0F,
                     false
                  );
               } else if (this.note.hexEntries.get(hexxx.toString()).type == 2) {
                  HexUtils.Pixel pix = hexxx.toPixel(9);
                  UtilsFX.drawTag(
                     graphics,
                     (float)(x + 161 + pix.x),
                     (float)(y + 75 + pix.y),
                     this.note.hexEntries.get(hexxx.toString()).aspect,
                     0.0F,
                     0,
                     0.0F,
                     771,
                     0.66F,
                     true
                  );
               }
            }

            pose.popPose();
         } else {
            this.runes.clear();
         }
      }
   }

   private void checkConnections(HexUtils.Hex hex) {
      this.checked.add(hex.toString());

      for (int a = 0; a < 6; a++) {
         HexUtils.Hex target = hex.getNeighbour(a);
         if (!this.checked.contains(target.toString())
            && this.note.hexEntries.containsKey(target.toString())
            && this.note.hexEntries.get(target.toString()).type >= 1) {
            Aspect aspect1 = this.note.hexEntries.get(hex.toString()).aspect;
            Aspect aspect2 = this.note.hexEntries.get(target.toString()).aspect;
            if (this.knowledge.hasDiscoveredAspect(aspect1)
               && this.knowledge.hasDiscoveredAspect(aspect2)
               && (
                  !aspect1.isPrimal() && (aspect1.getComponents()[0] == aspect2 || aspect1.getComponents()[1] == aspect2)
                     || !aspect2.isPrimal() && (aspect2.getComponents()[0] == aspect1 || aspect2.getComponents()[1] == aspect1)
               )) {
               String k1 = hex + ":" + target;
               String k2 = target + ":" + hex;
               if (!this.lines.containsKey(k1) && !this.lines.containsKey(k2)) {
                  this.lines.put(k1, new HexUtils.Hex[]{hex, target});
                  this.highlight.add(target.toString());
               }

               this.checkConnections(target);
            }
         }
      }
   }

   private void drawRune(GuiGraphics graphics, double x, double y, int rune, float alpha) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      RenderSystem.setShaderTexture(0, Thaumcraft.location("textures/item/model/script.png"));
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      pose.translate(x, y, 0.0);
      if (rune < 16) {
         pose.mulPose(Axis.ZN.rotationDegrees(90.0F));
      }

      float var8 = 0.0625F * rune;
      float var9 = var8 + 0.0625F;
      float var10 = 0.0F;
      float var11 = 1.0F;
      BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      Matrix4f matrix4f = pose.last().pose();
      builder.addVertex(matrix4f, -5.0F, 5.0F, 0.0F).setUv(var9, var11).setColor(0.0F, 0.0F, 0.0F, alpha);
      builder.addVertex(matrix4f, 5.0F, 5.0F, 0.0F).setUv(var9, var10).setColor(0.0F, 0.0F, 0.0F, alpha);
      builder.addVertex(matrix4f, 5.0F, -5.0F, 0.0F).setUv(var8, var10).setColor(0.0F, 0.0F, 0.0F, alpha);
      builder.addVertex(matrix4f, -5.0F, -5.0F, 0.0F).setUv(var8, var11).setColor(0.0F, 0.0F, 0.0F, alpha);
      BufferUploader.drawWithShader(builder.buildOrThrow());
      pose.popPose();
   }

   public boolean mouseClicked(double mx, double my, int button) {
      if (0L <= System.nanoTime() && this.butcount2 <= System.nanoTime()) {
         int gx = this.leftPos;
         int gy = this.topPos;
         double var7 = mx - (gx + 35);
         double var8 = my - (gy + 139);
         if (var7 >= 0.0 && var8 >= 0.0 && var7 < 32.0 && var8 < 16.0 && this.butcount2 < System.nanoTime() && this.select1 != null && this.select2 != null) {
            this.butcount2 = System.nanoTime() + 200000000L;
            this.playButtonClick();
            this.playButtonCombine();
            PacketHandler.sendToServer(
               new PacketAspectCombinationToServer(
                  this.select1,
                  this.select2,
                  ((ResearchTableMenu)this.menu).table.bonusAspects.getAmount(this.select1) > 0,
                  ((ResearchTableMenu)this.menu).table.bonusAspects.getAmount(this.select2) > 0
               )
            );
         } else {
            var7 = mx - (gx + 27);
            var8 = my - (gy + 121);
            if (this.page > 0 && var7 >= 0.0 && var8 >= 0.0 && var7 < 24.0 && var8 < 8.0) {
               this.page--;
               this.playButtonScroll();
            } else {
               var7 = mx - (gx + 51);
               var8 = my - (gy + 121);
               if (this.page < this.lastPage && var7 >= 0.0 && var8 >= 0.0 && var7 < 24.0 && var8 < 8.0) {
                  this.page++;
                  this.playButtonScroll();
               } else {
                  if (this.select1 != null) {
                     var7 = mx - (gx + 11);
                     var8 = my - (gy + 137);
                     if (var7 >= 0.0 && var8 >= 0.0 && var7 < 16.0 && var8 < 16.0) {
                        this.select1 = null;
                        this.playButtonAspect();
                        return true;
                     }
                  }

                  if (this.select2 != null) {
                     var7 = mx - (gx + 71);
                     var8 = my - (gy + 137);
                     if (var7 >= 0.0 && var8 >= 0.0 && var7 < 16.0 && var8 < 16.0) {
                        this.select2 = null;
                        this.playButtonAspect();
                        return true;
                     }
                  }

                  if (this.note != null) {
                     this.checkClickedHex((int)mx, (int)my, gx, gy);
                     if (this.enableDuplicateNote && this.note.isComplete()) {
                        var7 = mx - (gx + 37);
                        var8 = my - (gy + 5);
                        if (var7 >= 0.0 && var8 >= 0.0 && var7 < 24.0 && var8 < 24.0) {
                           this.minecraft.gameMode.handleInventoryButtonClick(((ResearchTableMenu)this.menu).containerId, 5);
                           this.playButtonClick();
                           return true;
                        }
                     }
                  }

                  if (hasShiftDown() && this.enableAspectCombiner) {
                     Aspect aspect = this.getClickedAspect((int)mx, (int)my, gx, gy, true);
                     if (aspect != null && !aspect.isPrimal()) {
                        AspectList aspects = this.knowledge.getAspectsDiscovered();
                        if ((
                              aspects.getAmount(aspect.getComponents()[0]) > 0
                                 || ((ResearchTableMenu)this.menu).table.bonusAspects.getAmount(aspect.getComponents()[0]) > 0
                           )
                           && (
                              aspects.getAmount(aspect.getComponents()[1]) > 0
                                 || ((ResearchTableMenu)this.menu).table.bonusAspects.getAmount(aspect.getComponents()[1]) > 0
                           )) {
                           this.draggedAspect = null;
                           this.playButtonCombine();
                           PacketHandler.sendToServer(
                              new PacketAspectCombinationToServer(
                                 aspect.getComponents()[0],
                                 aspect.getComponents()[1],
                                 ((ResearchTableMenu)this.menu).table.bonusAspects.getAmount(aspect.getComponents()[0]) > 0,
                                 ((ResearchTableMenu)this.menu).table.bonusAspects.getAmount(aspect.getComponents()[1]) > 0
                              )
                           );
                        }
                     }
                  }
               }
            }
         }
      }

      if (button == 0) {
         int sx = this.leftPos + 10;
         int sy = this.topPos + 40;
         if (this.isMouseButtonDown == 0 && mx >= sx && mx < sx + 80 && my >= sy && my < sy + 80) {
            Aspect aspect = this.getClickedAspect(mx, my, this.leftPos, this.topPos, false);
            if (aspect != null) {
               this.playButtonAspect();
               this.isMouseButtonDown = 1;
               this.draggedAspect = aspect;
            }
         }
      }

      return super.mouseClicked(mx, my, button);
   }

   private void checkClickedHex(int mx, int my, int gx, int gy) {
      int mouseX = mx - (gx + 169);
      int mouseY = my - (gy + 83);
      HexUtils.Hex hp = new HexUtils.Pixel(mouseX, mouseY).toHex(9);
      if (this.note.isComplete()) {
         return;
      }

      if (this.note.hexes.containsKey(hp.toString()) && this.note.hexEntries.get(hp.toString()).type == 2) {
         this.playButtonCombine();
         this.playButtonErase();
         PacketHandler.sendToServer(new PacketAspectPlaceToServer((byte)hp.q, (byte)hp.r, null));
      }
   }

   private Aspect getClickedAspect(double mx, double my, int gx, int gy, boolean ignoreZero) {
      AspectList aspects = this.knowledge.getAspectsDiscovered();
      if (aspects != null) {
         int count = 0;
         int drawn = 0;

         for (Aspect aspect : aspects.getAspectsSorted()) {
            if (++count - 1 >= this.page * 5 && drawn < 25) {
               int xx = drawn / 5 * 16;
               int yy = drawn % 5 * 16;
               double var7x = mx - (gx + xx + 10);
               double var8 = my - (gy + yy + 40);
               if ((ignoreZero || aspects.getAmount(aspect) > 0 || ((ResearchTableMenu)this.menu).table.bonusAspects.getAmount(aspect) > 0)
                  && var7x >= 0.0
                  && var8 >= 0.0
                  && var7x < 16.0
                  && var8 < 16.0) {
                  return aspect;
               }

               drawn++;
            }
         }
      }

      return null;
   }

   private void playButtonClick() {
      this.playSound((SoundEvent)ModSounds.CAMERA_CLACK.get(), 0.4F, 1.0F);
   }

   private void playButtonAspect() {
      this.playSound((SoundEvent)ModSounds.HHOFF.get(), 0.2F, 1.0F + this.minecraft.cameraEntity.level().random.nextFloat() * 0.1F);
   }

   private void playButtonCombine() {
      this.playSound((SoundEvent)ModSounds.HHON.get(), 0.3F, 1.0F);
   }

   private void playButtonWrite() {
      this.playSound((SoundEvent)ModSounds.WRITE.get(), 0.2F, 1.0F);
   }

   private void playButtonErase() {
      this.playSound((SoundEvent)ModSounds.ERASE.get(), 0.2F, 1.0F + this.minecraft.cameraEntity.level().random.nextFloat() * 0.1F);
   }

   private void playButtonScroll() {
      this.playSound((SoundEvent)ModSounds.KEY.get(), 0.3F, 1.0F);
   }

   private void playSound(SoundEvent event, float f, float f1) {
      Entity entity = this.minecraft.cameraEntity;
      entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), event, SoundSource.BLOCKS, f, f1, false);
   }

   private void drawOrb(GuiGraphics graphics, double x, double y) {
      int count = this.minecraft.player.tickCount;
      float red = 0.7F + Mth.sin((float)((count + x) / 10.0)) * 0.15F + 0.15F;
      float green = 0.7F + Mth.sin((float)((count + x + y) / 11.0)) * 0.15F + 0.15F;
      float blue = 0.7F + Mth.sin((float)((count + y) / 12.0)) * 0.15F + 0.15F;
      Color c = new Color(red, green, blue);
      this.drawOrb(graphics, x, y, c.getRGB());
   }

   /** Орб с увеличением — для перетаскиваемого аспекта, чтобы его было видно. */
   private void drawOrbScaled(GuiGraphics graphics, double x, double y, int color, float scale) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(x + 8.0, y + 8.0, 0.0);
      pose.scale(scale, scale, 1.0F);
      pose.translate(-8.0, -8.0, 0.0);
      this.drawOrb(graphics, 0.0F, 0.0F, color);
      pose.popPose();
   }

   private void drawOrb(GuiGraphics graphics, double x, double y, int color) {
      int count = this.minecraft.player.tickCount;
      Color c = new Color(color);
      float red = c.getRed() / 255.0F;
      float green = c.getGreen() / 255.0F;
      float blue = c.getBlue() / 255.0F;
      if (Config.colorBlind) {
         red /= 1.8F;
         green /= 1.8F;
         blue /= 1.8F;
      }

      // Орб рисуется процедурным свечением, а НЕ спрайтом из particles.png.
      // В 1.7.10 UV 0.5..0.5625 попадали ровно в один спрайт листа 128x128
      // (16x16 спрайтов). В порте лист 256x256 (32x32 спрайтов), и та же доля
      // накрывала четыре спрайта сразу — вместо мягкого шарика получалась каша,
      // а центр выглядел прозрачным. Зависимость от атласа убрана совсем.
      PoseStack pose = graphics.pose();
      pose.pushPose();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      pose.translate(x, y, 0.0);

      BufferBuilder builder = Tesselator.getInstance().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
      Matrix4f matrix4f = pose.last().pose();
      builder.addVertex(matrix4f, 8.0F, 8.0F, 0.0F).setColor(red, green, blue, 1.0F);
      int segments = 24;

      for (int i = 0; i <= segments; i++) {
         double angle = i * (Math.PI * 2.0) / segments;
         float vx = 8.0F + (float)Math.cos(angle) * 8.0F;
         float vy = 8.0F + (float)Math.sin(angle) * 8.0F;
         builder.addVertex(matrix4f, vx, vy, 0.0F).setColor(red, green, blue, 0.0F);
      }

      BufferUploader.drawWithShader(builder.buildOrThrow());
      RenderSystem.disableBlend();
      pose.popPose();
   }

   public static class Rune {
      int q;
      int r;
      long start;
      long decay;
      int rune;

      public Rune(int q, int r, long start, long decay, int rune) {
         this.q = q;
         this.r = r;
         this.start = start;
         this.decay = decay;
         this.rune = rune;
      }
   }
}
