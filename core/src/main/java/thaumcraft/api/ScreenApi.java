package thaumcraft.api;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.net.URI;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;
import thaumcraft.client.ThaumonomiconFont;
import thaumcraft.util.CustomFont;

@OnlyIn(Dist.CLIENT)
public class ScreenApi {
   public static final ThaumonomiconFont MAGIC = new ThaumonomiconFont(Thaumcraft.location("font/magic.otf"), true);
   public static final CustomFont RUNIC = CustomFont.create(Thaumcraft.location("font/runic.ttf"), true);

   public static void setShaderTexture(ResourceLocation location) {
      RenderSystem.setShaderTexture(0, location);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderTextureQuad(@NotNull GuiGraphics graphics, @NotNull ResourceLocation loc, double x, double y, double width, double height) {
      setShaderTexture(loc);
      renderTextureQuad(graphics, x, y, width, height);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderTextureQuadBlend(@NotNull GuiGraphics graphics, @NotNull ResourceLocation loc, double x, double y, double width, double height) {
      setShaderTexture(loc);
      RenderSystem.enableBlend();
      renderTextureQuad(graphics, x, y, width, height);
      RenderSystem.disableBlend();
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderTextureQuad(@NotNull GuiGraphics graphics, double x, double y, double width, double height) {
      Matrix4f matrix4f = graphics.pose().last().pose();
      BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      builder.addVertex(matrix4f, (float)(x + width), (float)(y + height), 0.0F).setUv(1.0F, 1.0F);
      builder.addVertex(matrix4f, (float)(x + width), (float)y, 0.0F).setUv(1.0F, 0.0F);
      builder.addVertex(matrix4f, (float)x, (float)y, 0.0F).setUv(0.0F, 0.0F);
      builder.addVertex(matrix4f, (float)x, (float)(y + height), 0.0F).setUv(0.0F, 1.0F);
      BufferUploader.drawWithShader(builder.buildOrThrow());
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderTextureQuadBlend(@NotNull GuiGraphics graphics, double x, double y, double width, double height) {
      RenderSystem.enableBlend();
      renderTextureQuad(graphics, x, y, width, height);
      RenderSystem.disableBlend();
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderTextureRect(@NotNull GuiGraphics graphics, @NotNull ResourceLocation loc, float x, float y, int u, int v, float width, float height) {
      renderTextureRect(graphics, loc, x, y, u, v, width, height, width, height, 256.0F, 256.0F);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderTextureRect(
      @NotNull GuiGraphics graphics,
      @NotNull ResourceLocation loc,
      float x,
      float y,
      int u,
      int v,
      float width,
      float height,
      float imageWidth,
      float imageHeight
   ) {
      renderTextureRect(graphics, loc, x, y, u, v, width, height, width, height, imageWidth, imageHeight);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderTextureRect(
      @NotNull GuiGraphics graphics,
      @NotNull ResourceLocation loc,
      float x,
      float y,
      float tx,
      float ty,
      float tw,
      float th,
      float width,
      float height,
      float imageWidth,
      float imageHeight
   ) {
      setShaderTexture(loc);
      renderTextureRect(graphics, x, y, tx, ty, tw, th, width, height, imageWidth, imageHeight);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderTextureRect(@NotNull GuiGraphics graphics, float x, float y, int u, int v, float width, float height) {
      renderTextureRect(graphics, x, y, u, v, width, height, width, height, 256.0F, 256.0F);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderTextureRect(
      @NotNull GuiGraphics graphics, float x, float y, int u, int v, float width, float height, float imageWidth, float imageHeight
   ) {
      renderTextureRect(graphics, x, y, u, v, width, height, width, height, imageWidth, imageHeight);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderTextureRect(
      @NotNull GuiGraphics graphics, float x, float y, float tx, float ty, float tw, float th, float width, float height, float imageWidth, float imageHeight
   ) {
      float f4 = 1.0F / imageWidth;
      float f5 = 1.0F / imageHeight;
      Matrix4f matrix4f = graphics.pose().last().pose();
      BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      builder.addVertex(matrix4f, x + 0.0F, y + height, 0.0F).setUv(tx * f4, (ty + th) * f5);
      builder.addVertex(matrix4f, x + width, y + height, 0.0F).setUv((tx + tw) * f4, (ty + th) * f5);
      builder.addVertex(matrix4f, x + width, y + 0.0F, 0.0F).setUv((tx + tw) * f4, ty * f5);
      builder.addVertex(matrix4f, x + 0.0F, y + 0.0F, 0.0F).setUv(tx * f4, ty * f5);
      BufferUploader.drawWithShader(builder.buildOrThrow());
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderBlock(GuiGraphics graphics, BlockState blockState, int x, int y) {
      if (!blockState.isAir()) {
         PoseStack pose = graphics.pose();
         Minecraft minecraft = Minecraft.getInstance();
         BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
         BakedModel bakedmodel = dispatcher.getBlockModel(blockState);
         pose.pushPose();
         pose.translate(x + 15, y + 12, 150.0F);

         try {
            pose.mulPose(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
            pose.scale(10.0F, 10.0F, 1.0F);
            pose.mulPose(Axis.XP.rotationDegrees(30.0F));
            pose.mulPose(Axis.YP.rotationDegrees(225.0F));
            boolean flag = !bakedmodel.usesBlockLight();
            if (flag) {
               Lighting.setupForFlatItems();
            }

            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
               IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluidState);
               TextureAtlasSprite sprite = (TextureAtlasSprite)minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                  .apply(props.getStillTexture(fluidState, null, null));
               int i = props.getTintColor(fluidState, null, null);
               float alpha = (i >> 24 & 0xFF) / 255.0F;
               float red = (i >> 16 & 0xFF) / 255.0F;
               float green = (i >> 8 & 0xFF) / 255.0F;
               float blue = (i & 0xFF) / 255.0F;
               pose.pushPose();
               graphics.blit(0, 0, 0, 1, 1, sprite, red, green, blue, alpha);
               pose.mulPose(Axis.YN.rotationDegrees(90.0F));
               pose.translate(0.0F, 0.0F, -1.0F);
               graphics.blit(0, 0, 0, 1, 1, sprite, red, green, blue, alpha);
               pose.mulPose(Axis.XP.rotationDegrees(90.0F));
               pose.translate(0.0F, 0.0F, -1.0F);
               graphics.blit(0, 0, 0, 1, 1, sprite, red, green, blue, alpha);
               pose.popPose();
            } else {
               dispatcher.renderSingleBlock(blockState, pose, graphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            }

            graphics.flush();
            if (flag) {
               Lighting.setupFor3DItems();
            }
         } catch (Throwable var17) {
            CrashReport crashreport = CrashReport.forThrowable(var17, "Rendering block");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being rendered");
            crashreportcategory.setDetail("Block", () -> String.valueOf(blockState));
            crashreportcategory.setDetail("Registry Name", () -> String.valueOf(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())));
            throw new ReportedException(crashreport);
         }

         pose.popPose();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void enableScissor(GuiGraphics graphics, float posX, float posY, float width, float height) {
      graphics.enableScissor((int)posX, (int)posY, (int)(posX + width), (int)(posY + height));
   }

   public static void browse(String url) {
      try {
         browse(new URI(url));
      } catch (Throwable var2) {
         var2.printStackTrace();
      }
   }

   public static void browse(URI url) {
      try {
         Util.getPlatform().openUri(url);
      } catch (Throwable var2) {
         var2.printStackTrace();
      }
   }
}
