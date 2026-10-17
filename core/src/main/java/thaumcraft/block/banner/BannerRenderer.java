package thaumcraft.block.banner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;

@OnlyIn(Dist.CLIENT)
public class BannerRenderer implements BlockEntityRenderer<BannerBlockEntity> {
   public static final ModelLayerLocation BANNER = new ModelLayerLocation(Thaumcraft.location("banner"), "main");
   public static final RenderType CULTIST_TYPE = RenderType.entityCutoutNoCull(Thaumcraft.location("textures/block/model/banner_cultist.png"));
   public static final RenderType BLANK_TYPE = RenderType.entityCutoutNoCull(Thaumcraft.location("textures/block/model/banner_blank.png"));
   private final ModelPart flag;
   private final ModelPart pole;
   private final ModelPart beam;
   private final ModelPart tabs;

   public BannerRenderer(Context context) {
      ModelPart modelpart = context.bakeLayer(BANNER);
      this.flag = modelpart.getChild("flag");
      this.pole = modelpart.getChild("pole");
      this.beam = modelpart.getChild("beam");
      this.tabs = modelpart.getChild("tabs");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition part = mesh.getRoot();
      part.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, 1.0F, -0.5F, 14.0F, 28.0F, 1.0F), PartPose.offset(0.0F, 30.0F, 0.0F));
      part.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(62, 0).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 31.0F, 2.0F), PartPose.ZERO);
      part.addOrReplaceChild("beam", CubeListBuilder.create().texOffs(30, 0).addBox(-7.0F, 29.0F, -1.0F, 14.0F, 2.0F, 2.0F), PartPose.ZERO);
      part.addOrReplaceChild(
         "tabs",
         CubeListBuilder.create().texOffs(0, 29).addBox(3.0F, 28.5F, -1.5F, 2.0F, 3.0F, 3.0F).addBox(-5.0F, 28.5F, -1.5F, 2.0F, 3.0F, 3.0F),
         PartPose.ZERO
      );
      return LayerDefinition.create(mesh, 128, 64);
   }

   public void render(
      @NotNull BannerBlockEntity entity, float pt, @NotNull PoseStack pose, @NotNull MultiBufferSource source, int brightnessOverride, int overlayTexture
   ) {
      boolean isCultist = entity.getAspect() == null && entity.getColor() == null;
      VertexConsumer consumer = source.getBuffer(isCultist ? CULTIST_TYPE : BLANK_TYPE);
      pose.pushPose();
      long i;
      if (entity.getLevel() == null) {
         i = 0L;
         pose.translate(0.5F, 0.0F, 0.5F);
         this.pole.visible = true;
      } else {
         i = entity.getLevel().getGameTime();
         BlockState blockstate = entity.getBlockState();
         if (blockstate.getBlock() instanceof BannerBlock) {
            pose.translate(0.5F, 0.0F, 0.5F);
            float f1 = -RotationSegment.convertToDegrees((Integer)blockstate.getValue(BannerBlock.ROTATION));
            pose.mulPose(Axis.YP.rotationDegrees(f1));
            this.pole.visible = true;
         } else {
            pose.translate(0.5F, -0.6666667F, 0.5F);
            float f3 = -((Direction)blockstate.getValue(net.minecraft.world.level.block.WallBannerBlock.FACING)).toYRot();
            pose.mulPose(Axis.YP.rotationDegrees(f3));
            pose.translate(0.0F, -0.3125F, -0.4375F);
            this.pole.visible = false;
         }
      }

      pose.pushPose();
      this.pole.render(pose, consumer, brightnessOverride, overlayTexture);
      this.beam.render(pose, consumer, brightnessOverride, overlayTexture);
      DyeColor color = entity.getColor();
      int tint;
      if (color != null) {
         int rgb = color.getTextureDiffuseColor();
         tint = 0xFF000000 | rgb;
      } else {
         tint = -1;
      }

      this.tabs.render(pose, consumer, brightnessOverride, overlayTexture);
      BlockPos blockpos = entity.getBlockPos();
      float f2 = ((float)Math.floorMod(blockpos.getX() * 7L + blockpos.getY() * 9L + blockpos.getZ() * 13L + i, 100L) + pt) / 100.0F;
      this.flag.xRot = -(-0.0125F + 0.01F * Mth.cos((float) (Math.PI * 2) * f2)) * (float) Math.PI;
      this.flag.y = -30.0F;
      pose.scale(1.0F, -1.0F, 1.0F);
      this.flag.render(pose, consumer, brightnessOverride, overlayTexture, tint);
      pose.popPose();
      pose.popPose();
      if (source instanceof BufferSource bufferSource) {
         bufferSource.endBatch(isCultist ? CULTIST_TYPE : BLANK_TYPE);
      }
   }

   /** The block entity's own box: NeoForge 21.1 asks the renderer, not the block entity. */
   @Override
   public AABB getRenderBoundingBox(BannerBlockEntity blockEntity) {
      return blockEntity.getRenderBoundingBox();
   }
}
