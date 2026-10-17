package thaumcraft.block.crystal;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModBlocks;

@OnlyIn(Dist.CLIENT)
public class CrystalClusterRenderer implements BlockEntityRenderer<CrystalClusterBlock.CrystalClusterBlockEntity> {
   public static final ModelLayerLocation MODEL = new ModelLayerLocation(Thaumcraft.location("crystal_cluster"), "main");
   public static final RenderType TYPE = RenderType.entityNoOutline(Thaumcraft.location("textures/block/model/crystal_cluster.png"));
   private final ModelPart model;

   public CrystalClusterRenderer(Context context) {
      this.model = context.bakeLayer(MODEL).getChild("main");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition part = mesh.getRoot();
      part.addOrReplaceChild(
         "main",
         CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-16.0F, -16.0F, 0.0F, 16.0F, 16.0F, 16.0F),
         PartPose.offsetAndRotation(0.0F, 32.0F, 0.0F, 0.7071F, 0.0F, 0.7071F)
      );
      return LayerDefinition.create(mesh, 64, 32);
   }

   public void render(
      @NotNull CrystalClusterBlock.CrystalClusterBlockEntity entity,
      float pt,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource source,
      int brightnessOverride,
      int overlayTexture
   ) {
      pose.pushPose();
      CrystalClusterBlock crystal = (CrystalClusterBlock)entity.getBlockState().getBlock();
      int color = crystal == ModBlocks.MIXED_CRYSTAL_CLUSTER.get() ? crystal.getColor(4) : crystal.getColor();
      BlockPos pos = entity.getBlockPos();
      RandomSource random = RandomSource.create(pos.getX() + (long)pos.getY() * pos.getZ());
      Direction facing = entity.getFacing();
      long gameTime = entity.getLevel() == null ? 0L : entity.getLevel().getGameTime();
      this.drawCrystal(
         pose,
         source,
         brightnessOverride,
         overlayTexture,
         facing,
         (random.nextFloat() - random.nextFloat()) * 5.0F,
         (random.nextFloat() - random.nextFloat()) * 5.0F,
         random,
         color,
         1.1F,
         gameTime
      );

      for (int i = 0; i < 5; i++) {
         if (crystal == ModBlocks.MIXED_CRYSTAL_CLUSTER.get()) {
            // Набор мелких шипов 1:1 с TC4 (colors[1..5]): воздух, огонь, вода,
            // земля, порядок. Было (0,1,2,3,5) — порядок терялся, энтропия дважды.
            color = crystal.getColor(i);
         }

         this.drawCrystal(pose, source, brightnessOverride, overlayTexture, facing, random.nextInt(36) + 72 * i, 15 + random.nextInt(15), random, color, 0.8F, gameTime);
      }

      pose.popPose();
   }

   private void drawCrystal(
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource source,
      int brightnessOverride,
      int overlayTexture,
      Direction facing,
      float yRot,
      float xRot,
      RandomSource random,
      int color,
      float scaled,
      long gameTime
   ) {
      pose.pushPose();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 771);
      this.translateFromOrientation(pose, facing);
      pose.mulPose(Axis.YP.rotationDegrees(yRot));
      pose.mulPose(Axis.XP.rotationDegrees(xRot));
      // Масштаб 1:1 с TC4 (TileCrystalRenderer.drawCrystal, строка 84): у нас
      // здесь стоял минус по Y — весь шип был отзеркален относительно оригинала
      // (рос в опору, а не из неё).
      pose.scale((0.15F + random.nextFloat() * 0.075F) * scaled, (0.5F + random.nextFloat() * 0.1F) * scaled, (0.15F + random.nextFloat() * 0.05F) * scaled);
      // Мерцание яркости 1:1 с TC4 (строка 67): синус от времени + shade.
      float shade = net.minecraft.util.Mth.sin((gameTime + random.nextInt(10)) / (5.0F + random.nextFloat())) * 0.075F + 0.925F;
      // Цвет 1:1 с TC4 (строки 69-72, 86-90): каналы /220 (ярче сырого цвета),
      // лайтмап 210*shade (~12) вместо мирового освещения — кристаллы светятся.
      int lit = (int)(210.0F * shade);
      int packed = Math.min(15, lit * 15 / 255);
      this.model.render(pose, source.getBuffer(TYPE), net.minecraft.client.renderer.LightTexture.pack(packed, packed), overlayTexture, shadeColor(color, shade));
      RenderSystem.disableBlend();
      pose.popPose();
   }

   /** Каналы цвета как в TC4: {@code c/220}, clamp, умножить на shade. */
   private static int shadeColor(int color, float shade) {
      int r = Math.min(255, (int)(((color >> 16) & 0xFF) / 220.0F * 255.0F * shade));
      int g = Math.min(255, (int)(((color >> 8) & 0xFF) / 220.0F * 255.0F * shade));
      int b = Math.min(255, (int)((color & 0xFF) / 220.0F * 255.0F * shade));
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   private void translateFromOrientation(PoseStack pose, Direction facing) {
      // Смещения — ровно как в оригинальном TileCrystalRenderer: FACING означает
      // НАПРАВЛЕНИЕ РОСТА кристалла (он крепится к блоку с противоположной
      // стороны), поэтому основание ставится у той стены, откуда он растёт.
      // У нас все шесть смещений были взяты от ПРОТИВОПОЛОЖНОЙ стороны
      // (повороты при этом совпадали) — кристалл прилипал не к той стене и
      // выглядел торчащим в воздух или смотрящим вверх.
      switch (facing) {
         case DOWN:
            // растёт вниз: основание у потолка
            pose.translate(0.5F, 1.3F, 0.5F);
            pose.mulPose(Axis.XP.rotationDegrees(180.0F));
            break;
         case UP:
            // растёт вверх: основание у пола
            pose.translate(0.5F, -0.3F, 0.5F);
            break;
         case NORTH:
            pose.translate(0.5F, 0.5F, 1.3F);
            pose.mulPose(Axis.XN.rotationDegrees(90.0F));
            break;
         case SOUTH:
            pose.translate(0.5F, 0.5F, -0.3F);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            break;
         case WEST:
            pose.translate(1.3F, 0.5F, 0.5F);
            pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
            break;
         case EAST:
            pose.translate(-0.3F, 0.5F, 0.5F);
            pose.mulPose(Axis.ZN.rotationDegrees(90.0F));
      }
   }
}
