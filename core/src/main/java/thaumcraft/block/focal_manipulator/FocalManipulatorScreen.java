package thaumcraft.block.focal_manipulator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.particles.fx.ParticleUtils;
import thaumcraft.research.ResearchManager;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class FocalManipulatorScreen extends AbstractContainerScreen<FocalManipulatorMenu> {
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/focal_manipulator.png");
   int selected = -1;
   int rank = 0;
   long time;
   long nextSparkle = 0L;
   DecimalFormat myFormatter = new DecimalFormat("#######.#");
   ArrayList<FocusUpgradeType> possibleUpgrades = new ArrayList<>();
   ArrayList<FocusUpgradeType> upgrades = new ArrayList<>();
   AspectList aspects = new AspectList();
   HashMap<Long, FocalManipulatorScreen.Sparkle> sparkles = new HashMap<>();

   public FocalManipulatorScreen(FocalManipulatorMenu menu, Inventory inventory, Component component) {
      super(menu, inventory, component);
      this.imageWidth = 192;
      this.imageHeight = 233;
   }

   public void render(@NotNull GuiGraphics graphics, int mx, int my, float pt) {
      super.render(graphics, mx, my, pt);
      int x = this.leftPos;
      int y = this.topPos;
      if (this.rank > 0) {
         for (int i = 0; i < this.possibleUpgrades.size(); i++) {
            int n4 = mx - (x + 48 + i * 16);
            int n5 = my - (y + 104);
            if (n4 >= 0 && n5 >= 0 && n4 < 16 && n5 < 16) {
               FocusUpgradeType focusUpgradeType = this.possibleUpgrades.get(i);
               ArrayList<String> list = new ArrayList<>();
               list.add("" + ChatFormatting.DARK_PURPLE + ChatFormatting.UNDERLINE + focusUpgradeType.getLocalizedName());
               list.add(focusUpgradeType.getLocalizedText());
               this.drawHoveringTextFixed(graphics, list, x + this.imageWidth - 36, y + 24, this.width - (x + this.imageWidth - 16));
            }
         }
      }

      if (this.selected >= 0) {
         int n6 = mx - (x + 48);
         int n7 = my - (y + 48);
         if (n6 >= 0 && n7 >= 0 && n6 < 36 && n7 < 36) {
            graphics.renderTooltip(this.font, Component.translatable("wandtable.text1"), mx, my);
         }

         int n8 = mx - (x + 108);
         int n9 = my - (y + 58);
         if (n8 >= 0 && n9 >= 0 && n8 < 36 && n9 < 16) {
            graphics.renderTooltip(this.font, Component.translatable("wandtable.text2"), mx, my);
         }

         if (((FocalManipulatorMenu)this.menu).manipulator.size == 0 && this.rank * 8 <= this.minecraft.player.experienceLevel) {
            int n10 = mx - (x + 48);
            int n11 = my - (y + 88);
            if (n10 >= 0 && n11 >= 0 && n10 < 96 && n11 < 8) {
               graphics.renderTooltip(this.font, Component.translatable("wandtable.text3"), mx, my);
            }
         }
      }

      for (int j = 0; j < this.upgrades.size(); j++) {
         int n12 = mx - (x + 56 + j * 16);
         int n13 = my - (y + 32);
         if (n12 >= 0 && n13 >= 0 && n12 < 16 && n13 < 16) {
            FocusUpgradeType focusUpgradeType2 = this.upgrades.get(j);
            ArrayList<String> list5 = new ArrayList<>();
            list5.add("" + ChatFormatting.DARK_PURPLE + ChatFormatting.UNDERLINE + focusUpgradeType2.getLocalizedName());
            list5.add(focusUpgradeType2.getLocalizedText());
            this.drawHoveringTextFixed(graphics, list5, x + this.imageWidth - 36, y + 24, this.width - (x + this.imageWidth - 16));
         }
      }

      this.renderTooltip(graphics, mx, my);
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mx, int my) {
      FocalManipulatorBlockEntity manipulator = ((FocalManipulatorMenu)this.menu).manipulator;
      ItemStack focus = manipulator.getItem(0);
      Player player = this.minecraft.player;
      Level level = manipulator.getLevel();
      this.time = System.currentTimeMillis();
      int x = this.leftPos;
      int y = this.topPos;
      PoseStack pose = graphics.pose();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 771);
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(this.texture, x, y, 0, 0, this.imageWidth, this.imageHeight);
      if (focus.isEmpty() || manipulator.rank < 0 || manipulator.reset) {
         this.rank = 0;
         this.selected = -1;
         this.possibleUpgrades.clear();
         this.upgrades.clear();
         this.aspects = new AspectList();
         manipulator.reset = false;
         manipulator.rank = 0;
      }

      if (this.rank > 0) {
         for (int i = 0; i < this.possibleUpgrades.size(); i++) {
            if (this.selected == this.possibleUpgrades.get(i).id) {
               graphics.blit(this.texture, x + 48 + i * 16, y + 104, 200, 0, 16, 16);
            }
         }
      }

      if (this.rank > 0 && this.selected >= 0 && !focus.isEmpty()) {
         int n6 = this.rank * 8;
         if (manipulator.size == 0 && n6 <= player.experienceLevel) {
            graphics.blit(this.texture, x + 48, y + 88, 8, 240, 96, 8);
         }

         graphics.blit(this.texture, x + 108, y + 59, 200, 16, 16, 16);
         int n7 = 0;
         if (manipulator.aspects.size() > 0) {
            for (Aspect aspect : manipulator.aspects.getAspectsSorted()) {
               if (aspect != null && manipulator.aspects.getAmount(aspect) != 0) {
                  int n8 = (int)((float)manipulator.aspects.getAmount(aspect) / manipulator.size * 96.0F);
                  Color color = new Color(aspect.getColor());
                  graphics.setColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 0.9F);
                  graphics.blit(this.texture, x + 48 + n7, y + 88, 112 + n7, 240, n8, 8);
                  n7 += n8;
                  if (level.random.nextInt(66) == 0) {
                     float n9 = 48 + n7;
                     float n10 = 92.0F;
                     this.sparkles
                        .put(
                           this.time,
                           new FocalManipulatorScreen.Sparkle(
                              n9, 92.0F, (46 + this.rank * 16 - n9) / 9.0F, -6.0F, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F
                           )
                        );
                  }
               }
            }
         }

         graphics.drawString(this.font, n6 + "", x + 125, y + 64, n6 > player.experienceLevel ? 16151160 : 10092429);
         AspectList list = this.aspects;
         if (manipulator.size > 0) {
            list = manipulator.aspects;
         }

         int n11 = 0;

         for (Aspect aspect2 : list.getAspectsSorted()) {
            if (aspect2 != null) {
               pose.pushPose();
               pose.translate(x + 49, y + 68 - list.size() * 2.5, 0.0);
               pose.scale(0.5F, 0.5F, 0.5F);
               graphics.drawString(this.font, aspect2.getName(), 0, n11 * 10, aspect2.getColor());
               graphics.drawString(this.font, this.myFormatter.format(list.getAmount(aspect2) / 100.0F), 48, n11 * 10, aspect2.getColor());
               pose.popPose();
               n11++;
            }
         }
      }

      if (this.rank > 0) {
         if (this.nextSparkle < this.time) {
            this.nextSparkle = this.time + (manipulator.size > 0 ? 10 : 500) + level.random.nextInt(200);
            this.sparkles
               .put(
                  this.time,
                  new FocalManipulatorScreen.Sparkle(
                     42 + this.rank * 16 + level.random.nextInt(12),
                     34 + level.random.nextInt(12),
                     0.0F,
                     0.0F,
                     0.5F + level.random.nextFloat() * 0.4F,
                     1.0F - level.random.nextFloat() * 0.4F,
                     1.0F - level.random.nextFloat() * 0.4F
                  )
               );
         }

         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

         for (int l = 0; l < this.possibleUpgrades.size(); l++) {
            FocusUpgradeType focusUpgradeType = this.possibleUpgrades.get(l);
            pose.pushPose();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            UtilsFX.drawTexturedQuadFull(graphics, focusUpgradeType.icon, x + 48 + l * 16, y + 104, 10);
            pose.popPose();
         }
      } else if (this.rank == 0 && !focus.isEmpty()) {
         try {
            this.gatherInfo();
         } catch (Exception var22) {
         }
      }

      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

      for (int n12 = 0; n12 < this.upgrades.size(); n12++) {
         FocusUpgradeType focusUpgradeType2 = this.upgrades.get(n12);
         pose.pushPose();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
         UtilsFX.drawTexturedQuadFull(graphics, focusUpgradeType2.icon, x + 56 + n12 * 16, y + 32, 0);
         pose.popPose();
      }

      RenderSystem.disableBlend();
   }

   private void gatherInfo() {
      FocalManipulatorBlockEntity manipulator = ((FocalManipulatorMenu)this.menu).manipulator;
      ItemStack focus = manipulator.getItem(0);
      this.possibleUpgrades.clear();
      this.upgrades.clear();
      this.aspects = new AspectList();
      ItemFocusBasic itemFocusBasic = (ItemFocusBasic)focus.getItem();
      short[] appliedUpgrades = itemFocusBasic.getAppliedUpgrades(focus);
      this.rank = 1;

      int n;
      for (n = 0; this.rank <= 5 && appliedUpgrades[this.rank - 1] != -1; this.rank++) {
         this.upgrades.add(FocusUpgradeType.types[appliedUpgrades[this.rank - 1]]);
         n++;
      }

      if (n == 5) {
         this.rank = -1;
      } else {
         FocusUpgradeType[] possibleUpgradesByRank = itemFocusBasic.getPossibleUpgradesByRank(focus, this.rank);
         if (possibleUpgradesByRank == null) {
            return;
         }

         for (FocusUpgradeType type : possibleUpgradesByRank) {
            if (itemFocusBasic.canApplyUpgrade(focus, this.minecraft.player, type, this.rank)) {
               this.possibleUpgrades.add(type);
            }
         }
      }

      if (manipulator.size > 0) {
         this.selected = manipulator.upgrade;
      }
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      RenderSystem.setShaderTexture(0, ParticleUtils.PARTICLE_TEXTURE);
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      Iterator<Entry<Long, FocalManipulatorScreen.Sparkle>> iterator = this.sparkles.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<Long, FocalManipulatorScreen.Sparkle> entry = iterator.next();
         FocalManipulatorScreen.Sparkle sparkle = entry.getValue();
         this.drawSparkle(graphics, sparkle.x, sparkle.y, sparkle.frame, sparkle.r, sparkle.g, sparkle.b);
         if (sparkle.nextframe < this.time) {
            sparkle.frame++;
            sparkle.nextframe = this.time + 50L;
            sparkle.x = sparkle.x + sparkle.mx;
            sparkle.y = sparkle.y + sparkle.my;
         }

         if (sparkle.frame >= 9) {
            iterator.remove();
         }
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      double n6 = mouseX - (this.leftPos + 48);
      double n7 = mouseY - (this.topPos + 88);
      if (((FocalManipulatorMenu)this.menu).manipulator.size == 0
         && this.selected >= 0
         && this.rank * 8 <= this.minecraft.player.experienceLevel
         && n6 >= 0.0
         && n7 >= 0.0
         && n6 < 96.0
         && n7 < 8.0) {
         this.minecraft.gameMode.handleInventoryButtonClick(((FocalManipulatorMenu)this.menu).containerId, this.selected);
         this.playButtonClick();
         return true;
      } else {
         if (((FocalManipulatorMenu)this.menu).manipulator.size == 0) {
            for (int i = 0; i < this.possibleUpgrades.size(); i++) {
               FocusUpgradeType focusUpgradeType = this.possibleUpgrades.get(i);
               double n8 = mouseX - (this.leftPos + 48 + i * 16);
               double n9 = mouseY - (this.topPos + 104);
               if (n8 >= 0.0 && n9 >= 0.0 && n8 < 16.0 && n9 < 16.0) {
                  this.aspects = new AspectList();
                  if (this.selected == focusUpgradeType.id) {
                     this.selected = -1;
                  } else {
                     this.selected = focusUpgradeType.id;
                     int n10 = 200;

                     for (int j = 1; j < this.rank; j++) {
                        n10 *= 2;
                     }

                     AspectList list = new AspectList();
                     Aspect[] aspects = FocusUpgradeType.types[this.selected].aspects.getAspects();
                     int length = aspects.length;

                     for (int k = 0; k < length; k++) {
                        list.add(aspects[k], n10);
                     }

                     this.aspects = ResearchManager.reduceToPrimals(list);
                  }

                  this.playButtonClick();
                  return true;
               }
            }
         }

         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   private void playButtonClick() {
      Entity entity = this.minecraft.cameraEntity;
      if (entity != null) {
         entity.level()
            .playLocalSound(entity.getX(), entity.getY(), entity.getZ(), (SoundEvent)ModSounds.CAMERA_CLACK.get(), SoundSource.BLOCKS, 0.4F, 1.0F, false);
      }
   }

   private void drawSparkle(GuiGraphics graphics, double x, double y, int frame, float r, float g, float b) {
      float uMin = frame / 16.0F;
      float uMax = uMin + 0.0624375F;
      float vMin = 0.4375F;
      float vMax = vMin + 0.0624375F;
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(x, y, 200.0);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder builder = tesselator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      Matrix4f matrix = pose.last().pose();
      builder.addVertex(matrix, -4.0F, 4.0F, 0.0F).setUv(uMax, vMax).setColor(r, g, b, 0.9F);
      builder.addVertex(matrix, 4.0F, 4.0F, 0.0F).setUv(uMax, vMin).setColor(r, g, b, 0.9F);
      builder.addVertex(matrix, 4.0F, -4.0F, 0.0F).setUv(uMin, vMin).setColor(r, g, b, 0.9F);
      builder.addVertex(matrix, -4.0F, -4.0F, 0.0F).setUv(uMin, vMax).setColor(r, g, b, 0.9F);
      BufferUploader.drawWithShader(builder.buildOrThrow());
      RenderSystem.disableBlend();
      pose.popPose();
   }

   protected void drawHoveringTextFixed(GuiGraphics graphics, List<String> list, int x, int y, int limitWidth) {
      if (!list.isEmpty()) {
         ArrayList<FormattedCharSequence> sequences = new ArrayList<>();

         for (String s : list) {
            sequences.addAll(this.font.split(this.trimStringNewline(s), limitWidth));
         }

         graphics.renderTooltip(this.font, sequences, x, y);
      }
   }

   private Component trimStringNewline(@NotNull String substring) {
      while (substring.endsWith("\n")) {
         substring = substring.substring(0, substring.length() - 1);
      }

      return Component.literal(substring);
   }

   public static class Sparkle {
      float x;
      float y;
      float mx;
      float my;
      float r;
      float g;
      float b;
      long nextframe;
      int frame;

      public Sparkle(float x, float y, float mx, float my, float r, float g, float b) {
         this.x = x;
         this.y = y;
         this.mx = mx;
         this.my = my;
         this.frame = 0;
         this.r = r;
         this.g = g;
         this.b = b;
         this.nextframe = System.currentTimeMillis() + 50L;
      }
   }
}
