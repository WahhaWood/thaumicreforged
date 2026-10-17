package thaumcraft.item.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ScanResult;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ScanManager;
import thaumcraft.util.UtilsFX;

/**
 * Рендер таумометра — порт {@code ItemThaumometerRenderer} (1.7.10).
 *
 * <p>То, что видно игроку, — не «фиолетовая штучка поверх предмета», а
 * <b>экран самого прибора</b>: корпус рисуется моделью, а перед ним — текстура
 * {@code thaumometer_loop.png} (в 1.7.10 это был {@code scanscreen.png},
 * байт в байт та же картинка), подсвеченная фиолетовым через
 * {@code OpenGlHelper.glTextureParameters} с координатами светомапы
 * {@code 190 + sin(...)}. Когда цель уже изучена, на экране рисуются ярлыки
 * аспектов и название объекта.
 *
 * <p>Отличия от 1.7.10, вынужденные:
 * <ul>
 *   <li>отражения игрока в экране из первого лица нет — в 1.21 это отдельный
 *       рендер игрока внутри рендера предмета, он даёт артефакты и пропущен;</li>
 *   <li>шрифт рисуется батчем через {@code Font.drawInBatch}, а не
 *       {@code FontRenderer.drawString} со сдвигом GL-матрицы.</li>
 * </ul>
 *
 * <p>Логика {@code doScan} полностью перенесена: луч по игроку, сначала
 * сущность, потом блок, потом феномены через зарегистрированные обработчики.
 */
@OnlyIn(Dist.CLIENT)
public class ThaumometerItemRenderer extends BlockEntityWithoutLevelRenderer implements IClientItemExtensions {
   private static final ResourceLocation SCREEN = Thaumcraft.location("textures/item/model/thaumometer_loop.png");
   private static final ResourceLocation MODEL_TEX = Thaumcraft.location("item/model/thaumometer");
   private BakedModel model;

   private static ThaumometerItemRenderer instance;

   public static ThaumometerItemRenderer getInstance() {
      if (instance == null) {
         instance = new ThaumometerItemRenderer();
      }

      return instance;
   }

   private ThaumometerItemRenderer() {
      super(null, null);
   }

   @Override
   public void renderByItem(
      @NotNull ItemStack stack,
      @NotNull ItemDisplayContext context,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource source,
      int lightmap,
      int overlay
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      Player player = minecraft.player;
      if (this.model == null) {
         ModelManager manager = minecraft.getModelManager();
         this.model = manager.getModel(
            net.minecraft.client.resources.model.ModelResourceLocation.standalone(
               net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                  "thaumcraft", "item/model/thaumometer"
               )
            )
         );
      }

      float partial = minecraft.getTimer() == null ? 0.0F : minecraft.getTimer().getGameTimeDeltaPartialTick(false);
      pose.pushPose();

      switch (context) {
         case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
            pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
            pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90.0F));
            pose.translate(-0.8F, 15.0F, -20.0F);
         }
         case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
            pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-90.0F));
            pose.translate(1.0F, -1.5F, 0.0F);
            pose.scale(0.15F, 0.15F, 0.15F);
         }
         case GUI -> {
            pose.translate(2.5F, 2.5F, 0.0F);
            pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
            pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90.0F));
            pose.scale(0.3F, 0.3F, 0.3F);
         }
         case GROUND, FIXED -> {
            pose.translate(3.0F, 3.0F, 3.0F);
            pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0F));
            pose.scale(0.4F, 0.4F, 0.4F);
         }
         default -> {
         }
      }

      // Корпус рисуется той же запечённой моделью, что и у ванильного рендера
      // предметов, только напрямую — иначе вызов ушёл бы в этот же рендерер.
      ModelBlockRenderer modelRenderer = minecraft.getBlockRenderer().getModelRenderer();
      // Модель прибора рисуется полупрозрачным листом — у OBJ есть альфа-канал.
      VertexConsumer buffer = source.getBuffer(net.minecraft.client.renderer.Sheets.translucentItemSheet());
      modelRenderer.renderModel(
         pose.last(), buffer, null, this.model, 1.0F, 1.0F, 1.0F,
         lightmap, overlay, ModelData.EMPTY, null
      );

      // Экран прибора: тот же фиолетовый отсвет, что в 1.7.10
      if (context != ItemDisplayContext.GROUND && context != ItemDisplayContext.FIXED) {
         pose.pushPose();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
         pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
         pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90.0F));
         RenderSystem.setShaderTexture(0, SCREEN);
         int lv = screenLightmap(player);
         UtilsFX.renderQuadCenteredFromTexture(
            pose, 2.5F, 1.0F, 1.0F, 1.0F, lv, 771, 1.0F
         );
         pose.popPose();

         if (player != null && context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            drawScanReadout(minecraft, player, stack, pose, source, partial);
         }
      }

      pose.popPose();
   }

   /** Фиолетовый отсвет экрана: 190 + sin(время + шум) * 10 + 10, как в оригинале. */
   private static int screenLightmap(Player player) {
      int t = player == null ? 0 : player.tickCount;
      return (int)(190.0F + (float)Math.sin(t + (player == null ? 0.0F : player.getRandom().nextFloat() * 2.0F)) * 10.0F + 10.0F);
   }

   /** Аспекты изученной цели и её название — содержимое экрана. */
   private static void drawScanReadout(
      Minecraft minecraft, Player player, ItemStack stack, PoseStack pose, MultiBufferSource source, float partial
   ) {
      Level level = player.level();
      // Скан считает сам предмет — единственный источник правды (тот же doScan,
      // что и на сервере), а не копия логики здесь.
      ScanResult scan = stack.getItem() instanceof thaumcraft.item.relics.ItemThaumometer thaumometer
         ? thaumometer.doScan(player.getMainHandItem(), player, true) : null;
      if (scan == null) {
         return;
      }

      AspectList aspects = ScanManager.hasBeenScanned(player, scan)
         ? ScanManager.getScanAspects(scan, level) : null;

      // Ярлыки аспектов
      if (aspects != null && aspects.size() > 0) {
         int posX = 0;
         int posY = 0;
         int left = aspects.size();
         int baseX = Math.min(5, left) * 8;
         for (Aspect aspect : aspects.getAspectsSorted()) {
            pose.pushPose();
            RenderSystem.setShaderTexture(0, aspect.getImage());
            Color c = new Color(aspect.getColor());
            pose.scale(0.0075F, 0.0075F, 0.0075F);
            UtilsFX.renderQuadCenteredFromTexture(
               pose, 1.0F, c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, 220, 771, 1.0F
            );
            pose.popPose();
            if (++posX >= 5 - posY) {
               posX = 0;
               posY++;
               left -= 5 - posY;
               baseX = Math.min(5 - posY, left) * 8;
            }
         }
      }

      // Название объекта под экраном
      String text = "?";
      if (scan.type == ScanResult.Type.ENTITY && scan.entity != null) {
         text = scan.entity.getName().getString();
      } else if (scan.stack != null && !scan.stack.isEmpty()) {
         text = scan.stack.getHoverName().getString();
      }

      if (!text.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
         pose.pushPose();
         pose.translate(0.0F, -0.25F, 0.0F);
         float scale = 0.005F;
         int width = minecraft.font.width(text);
         if (width > 90) {
            scale -= 2.5E-5F * (width - 90);
         }
         pose.scale(scale, scale, scale);
         minecraft.font.drawInBatch(
            text, -width / 2, 0.0F, 0xFFFFFF, false,
            pose.last().pose(), source, DisplayMode.NORMAL, 0, 15728880
         );
         pose.popPose();
      }
   }

   @Override
   public BlockEntityWithoutLevelRenderer getCustomRenderer() {
      return this;
   }
}