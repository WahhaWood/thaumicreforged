package thaumcraft.block.infusion_matrix;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Random;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;

/**
 * Порт {@code TileRunicMatrixRenderer} из TC 4.2.3.5 (1.7.10).
 *
 * <p>В оригинале у блока матрицы нет статической модели: блок-рендер
 * ({@code BlockStoneDeviceRenderer}, meta 2) целиком делегируется в TESR, поэтому
 * этот рендерер работает всегда — и в простое (кластер стоит, startUp = 0), и в бою.
 * Свечение оболочки и гало в оригинале рисуются с аддитивным блендингом
 * ({@code glBlendFunc(SRC_ALPHA, ONE)}), поэтому здесь два кастомных RenderType
 * с тем же блендингом ({@code LIGHTNING_TRANSPARENCY}).
 */
@OnlyIn(Dist.CLIENT)
public class RunicMatrixRenderer implements BlockEntityRenderer<RunicMatrixBlockEntity> {
   /** Размер куба: 0.45 блока из оригинала (ModelCube: бокс 16 юнитов × glScalef(0.0625) × glScaled(0.45)); запечённая JSON-модель занимает 0..1 блока. */
   private static final float CUBE_SCALE = 0.45F;

   /** Аддитивная фиолетовая оболочка кубов (model_over в оригинале). */
   private static final RenderType MATRIX_GLOW = RenderType.create(
      "thaumcraft_runic_matrix_glow",
      DefaultVertexFormat.NEW_ENTITY,
      VertexFormat.Mode.QUADS,
      1536,
      false,
      true,
      RenderType.CompositeState.builder()
         .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
         .setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
         .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
         .setLightmapState(RenderStateShard.LIGHTMAP)
         .setOverlayState(RenderStateShard.OVERLAY)
         .createCompositeState(true)
   );

   /** Гало над крафтящей матрицей: залитые веера с градиентом белый → фиолетовый. */
   private static final RenderType MATRIX_HALO = RenderType.create(
      "thaumcraft_runic_matrix_halo",
      DefaultVertexFormat.POSITION_COLOR,
      VertexFormat.Mode.TRIANGLES,
      512,
      false,
      false,
      RenderType.CompositeState.builder()
         .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
         .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
         .setWriteMaskState(RenderStateShard.COLOR_WRITE)
         .createCompositeState(false)
   );

   private BakedModel model;
   private BakedModel over;

   public RunicMatrixRenderer(Context context) {
   }

   /**
    * Порт {@code drawHalo}. Веера {@code startDrawing(6)} (GL_TRIANGLE_FAN) переведены
    * в треугольники: центр + 3 точки обода = (c,r1,r2),(c,r2,r3),(c,r3,r1).
    * Порядок и количество вызовов {@code nextFloat} сохранены 1в1 с оригиналом,
    * иначе «дыхание» вееров (последний поворот = count/500 * 360°) поплывёт.
    */
   private void drawHalo(MultiBufferSource source, PoseStack pose, int count) {
      pose.pushPose();
      pose.translate(0.5F, 0.5F, 0.5F);
      boolean fancy = Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST;
      int fans = fancy ? 20 : 10;
      float rotationTick = count / 500.0F;
      Random random = new Random(245L);
      VertexConsumer consumer = source.getBuffer(MATRIX_HALO);

      for (int i = 0; i < fans; i++) {
         pose.pushPose();
         pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
         pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
         pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
         pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
         pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
         pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + rotationTick * 360.0F));

         float fa = random.nextFloat() * 20.0F + 5.0F;
         float f4 = random.nextFloat() * 2.0F + 1.0F;
         fa /= 20.0F / (Math.min(count, 50) / 50.0F);
         f4 /= 20.0F / (Math.min(count, 50) / 50.0F);

         Matrix4f matrix = pose.last().pose();
         // Центр — белый непрозрачный, обод — фиолетовый (RGB 0xCC00FF) прозрачный.
         // Формат setColor(int) — ARGB: 0x00CC00FF = альфа 0, RGB (204, 0, 255).
         int center = 0xFFFFFFFF;
         int edge = 0x00CC00FF;
         vertex(consumer, matrix, 0.0F, 0.0F, 0.0F, center);
         vertex(consumer, matrix, -0.866F * f4, fa, -0.5F * f4, edge);
         vertex(consumer, matrix, 0.866F * f4, fa, -0.5F * f4, edge);
         vertex(consumer, matrix, 0.0F, fa, f4, edge);
         vertex(consumer, matrix, -0.866F * f4, fa, -0.5F * f4, edge);
         pose.popPose();
      }

      pose.popPose();
   }

   private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int argb) {
      consumer.addVertex(matrix, x, y, z).setColor(argb);
   }

   public void render(
      @NotNull RunicMatrixBlockEntity matrix, float pt, @NotNull PoseStack pose, @NotNull MultiBufferSource source, int brightnessOverride, int overlayTexture
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      if (this.model == null) {
         ModelManager manager = minecraft.getModelManager();
         this.model = manager.getModel(Thaumcraft.staticModel("runic_matrix"));
         this.over = manager.getModel(Thaumcraft.staticModel("runic_matrix_over"));
      }

      ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
      VertexConsumer solid = source.getBuffer(Sheets.cutoutBlockSheet());
      VertexConsumer glow = source.getBuffer(MATRIX_GLOW);
      float time = (minecraft.cameraEntity != null ? minecraft.cameraEntity.tickCount : 0) + pt;
      float startUp = matrix.startUp;
      boolean active = matrix.isActive();
      float instability = Math.min(
         6.0F,
         1.0F + matrix.getInstability() * 0.66F * (Math.min(matrix.getCraftTickCount(), 50) / 50.0F)
      );

      pose.pushPose();

      // Пивот вращений — ЦЕНТР блока (в оригинале glTranslatef(x+0.5, y+0.5, z+0.5)
      // перед поворотами); без этого кластер при вращении уходит за угол блока.
      pose.translate(0.5F, 0.5F, 0.5F);

      // В оригинале разворот делается безусловно (проверка world != null), а в простое
      // матрицу держит на месте сам startUp = 0. Так же и здесь: гейт по крафту запрещён.
      pose.mulPose(Axis.YP.rotationDegrees(time % 360.0F * startUp));
      pose.mulPose(Axis.XP.rotationDegrees(35.0F * startUp));
      pose.mulPose(Axis.ZP.rotationDegrees(45.0F * startUp));

      for (int a = 0; a < 2; a++) {
         for (int b = 0; b < 2; b++) {
            for (int c = 0; c < 2; c++) {
               // Пока матрица спит, дрожь равна нулю — как в оригинале,
               // где b1..b3 заполняются только при is.active.
               float n6 = active
                  ? Mth.sin((time + a * 10) / (15.0F - instability / 2.0F)) * 0.01F * startUp * instability : 0.0F;
               float n7 = active
                  ? Mth.sin((time + b * 10) / (14.0F - instability / 2.0F)) * 0.01F * startUp * instability : 0.0F;
               float n8 = active
                  ? Mth.sin((time + c * 10) / (13.0F - instability / 2.0F)) * 0.01F * startUp * instability : 0.0F;
               int n9 = a == 0 ? -1 : 1;
               int n10 = b == 0 ? -1 : 1;
               int n11 = c == 0 ? -1 : 1;

               // Порядок трансформаций как в оригинале: смещение -> повороты ->
               // масштаб. Масштаб обязан идти ПОСЛЕ поворотов, иначе куб
               // увеличивается вместе с орбитой и уезжает за пределы блока.
               pose.pushPose();
               pose.translate(n6 + n9 * 0.25F, n7 + n10 * 0.25F, n8 + n11 * 0.25F);
               if (a > 0) {
                  pose.mulPose(Axis.XP.rotationDegrees(90.0F));
               }

               if (b > 0) {
                  pose.mulPose(Axis.YP.rotationDegrees(90.0F));
               }

               if (c > 0) {
                  pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
               }

               // 0.45 из оригинала, умноженное на 1/16: ModelCube.render()
               // в 1.7.10 вызывал glScalef(0.0625F), то есть куб — это
               // сотая доля блока. Наша запечённая модель занимает 0..1,
               // поэтому деление на 16 делается здесь.
               pose.scale(CUBE_SCALE, CUBE_SCALE, CUBE_SCALE);
               // Модель не центрирована (0..1), а оригинальный ModelCube был
               // центрирован — сдвигаем к центру.
               pose.translate(-0.5F, -0.5F, -0.5F);
               renderer.renderModel(
                  pose.last(), solid, null, this.model, 1.0F, 1.0F, 1.0F,
                  brightnessOverride, overlayTexture, ModelData.EMPTY, null
               );
               pose.popPose();
            }
         }
      }

      // Фиолетовая подсветка поверх — только у активной матрицы (как в оригинале).
      if (active) {
         for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
               for (int c = 0; c < 2; c++) {
                  float n6 = Mth.sin((time + a * 10) / (15.0F - instability / 2.0F)) * 0.01F * startUp * instability;
                  float n7 = Mth.sin((time + b * 10) / (14.0F - instability / 2.0F)) * 0.01F * startUp * instability;
                  float n8 = Mth.sin((time + c * 10) / (13.0F - instability / 2.0F)) * 0.01F * startUp * instability;
                  int n9 = a == 0 ? -1 : 1;
                  int n10 = b == 0 ? -1 : 1;
                  int n11 = c == 0 ? -1 : 1;

                  pose.pushPose();
                  pose.translate(n6 + n9 * 0.25F, n7 + n10 * 0.25F, n8 + n11 * 0.25F);
                  if (a > 0) {
                     pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                  }

                  if (b > 0) {
                     pose.mulPose(Axis.YP.rotationDegrees(90.0F));
                  }

                  if (c > 0) {
                     pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
                  }

                  pose.scale(CUBE_SCALE, CUBE_SCALE, CUBE_SCALE);
                  pose.translate(-0.5F, -0.5F, -0.5F);
                  this.renderOver(
                     pose.last(), glow, overlayTexture,
                     (Mth.sin((time + a * 2 + b * 3 + c * 4) / 4.0F) * 0.1F + 0.2F) * startUp,
                     0.8F, 0.1F, 1.0F
                  );
                  pose.popPose();
               }
            }
         }
      }

      pose.popPose();
      if (matrix.isCrafting()) {
         this.drawHalo(source, pose, matrix.getCraftTickCount());
      }

      // Вспышка завершения: гало быстро раскрывается от нуля до полного радиуса
      // за 20 тиков и гаснет — фиолетовый «выдох» матрицы при готовом предмете.
      int finishFlash = matrix.getFinishFlashTicks();
      if (finishFlash > 0) {
         this.drawHalo(source, pose, 50 - finishFlash * 50 / 20);
      }
   }

   /**
    * Фиолетовая оболочка кубов. В оригинале она рисуется с цветом
    * {@code glColor4f(0.8F, 0.1F, 1.0F, alpha)} — тот же фиолетовый, что и
    * отсвет экрана таумометра.
    */
   private void renderOver(Pose pose, VertexConsumer consumer, int overlayTexture, float alpha, float r, float g, float b) {
      RandomSource randomsource = RandomSource.create();

      for (Direction direction : Direction.values()) {
         randomsource.setSeed(42L);
         this.renderQuadList(pose, consumer, this.over.getQuads(null, direction, randomsource, ModelData.EMPTY, null), overlayTexture, alpha, r, g, b);
      }

      randomsource.setSeed(42L);
      this.renderQuadList(pose, consumer, this.over.getQuads(null, null, randomsource, ModelData.EMPTY, null), overlayTexture, alpha, r, g, b);
   }

   private void renderQuadList(
      Pose pose, VertexConsumer consumer, List<BakedQuad> quads, int overlayTexture, float alpha, float r, float g, float b
   ) {
      float[] floats = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
      int[] brightness = new int[]{15728880, 15728880, 15728880, 15728880};

      for (BakedQuad bakedquad : quads) {
         consumer.putBulkData(pose, bakedquad, floats, r, g, b, alpha, brightness, overlayTexture, false);
      }
   }
}
