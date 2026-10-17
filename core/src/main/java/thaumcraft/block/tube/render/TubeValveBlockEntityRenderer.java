package thaumcraft.block.tube.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.block.tube.render.model.ModelTubeValve;
import thaumcraft.block.tube.render.util.TubeRenderUtil;
import thaumcraft.block.tube.valve.TubeValveBlockEntity;

public class TubeValveBlockEntityRenderer implements BlockEntityRenderer<TubeValveBlockEntity> {
   public static final ResourceLocation VALVE_PNG = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/valve.png");
   public static final ResourceLocation PIPE_VALVE_SPRITE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/pipe_valve");
   private final ModelTubeValve model;
   private static final float HANDLE_THICKNESS = 0.1F;
   private static final float HANDLE_Z_EPS = 0.15F;

   public TubeValveBlockEntityRenderer(Context ctx) {
      ModelPart root = ctx.bakeLayer(ModelTubeValve.TUBE_VALVE);
      this.model = new ModelTubeValve(root);
   }

   public void render(
      @NotNull TubeValveBlockEntity be, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay
   ) {
      pose.pushPose();
      pose.translate(0.5, 0.5, 0.5);
      Direction facing = Direction.NORTH;
      Direction f = be.getFacing();
      if (f != null) {
         facing = f;
      }

      TubeRenderUtil.applyFacingOpposite(pose, facing);
      float rot = 0.0F;
      rot = be.getRotation(partialTick);
      pose.mulPose(Axis.YP.rotationDegrees(-rot * 1.5F));
      pose.translate(0.0, -(rot / 360.0F) * 0.12F, 0.0);
      VertexConsumer rod = buffers.getBuffer(RenderType.entityCutout(VALVE_PNG));
      this.model.render(pose, rod, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
      renderHandle(pose, buffers, packedLight);
      pose.popPose();
   }

   private static void renderHandle(PoseStack pose, MultiBufferSource buffers, int light) {
      TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(PIPE_VALVE_SPRITE);
      pose.pushPose();
      pose.mulPose(Axis.XP.rotationDegrees(90.0F));
      pose.translate(-0.25F, -0.25F, -0.25F);
      pose.scale(0.5F, 0.5F, 0.5F);
      pose.translate(0.0F, 0.0F, 0.15F);
      VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
      renderExtrudedSprite(pose, vc, sprite, light, 0.1F);
      pose.popPose();
   }

   private static void renderExtrudedSprite(PoseStack pose, VertexConsumer vc, TextureAtlasSprite sp, int light, float thickness) {
      if (!(thickness <= 0.0F)) {
         Pose p = pose.last();
         Matrix4f mat = p.pose();
         Matrix3f nrm = p.normal();
         float u0 = sp.getU0();
         float u1 = sp.getU1();
         float v0 = sp.getV0();
         float v1 = sp.getV1();
         int w = 16;
         int h = 16;
         float z0 = 0.0F;
         float z1 = -thickness;
         quad(vc, mat, nrm, 0.0F, 0.0F, z0, u0, v1, 1.0F, 0.0F, z0, u1, v1, 1.0F, 1.0F, z0, u1, v0, 0.0F, 1.0F, z0, u0, v0, light, 0.0F, 0.0F, 1.0F);
         quad(vc, mat, nrm, 0.0F, 1.0F, z1, u0, v0, 1.0F, 1.0F, z1, u1, v0, 1.0F, 0.0F, z1, u1, v1, 0.0F, 0.0F, z1, u0, v1, light, 0.0F, 0.0F, -1.0F);
         float f5 = 0.5F * (u0 - u1) / w;
         float f6 = 0.5F * (v1 - v0) / h;

         for (int k = 0; k < w; k++) {
            float f7 = (float)k / w;
            float f8 = u0 + (u1 - u0) * f7 - f5;
            float f9 = f7 + 1.0F / w;
            quad(vc, mat, nrm, f7, 0.0F, z1, f8, v1, f7, 0.0F, z0, f8, v1, f7, 1.0F, z0, f8, v0, f7, 1.0F, z1, f8, v0, light, -1.0F, 0.0F, 0.0F);
            quad(vc, mat, nrm, f9, 1.0F, z1, f8, v0, f9, 1.0F, z0, f8, v0, f9, 0.0F, z0, f8, v1, f9, 0.0F, z1, f8, v1, light, 1.0F, 0.0F, 0.0F);
         }

         for (int k = 0; k < h; k++) {
            float f7 = (float)k / h;
            float f8 = v1 + (v0 - v1) * f7 - f6;
            float f9 = f7 + 1.0F / h;
            quad(vc, mat, nrm, 0.0F, f9, z0, u0, f8, 1.0F, f9, z0, u1, f8, 1.0F, f9, z1, u1, f8, 0.0F, f9, z1, u0, f8, light, 0.0F, 1.0F, 0.0F);
            quad(vc, mat, nrm, 1.0F, f7, z0, u1, f8, 0.0F, f7, z0, u0, f8, 0.0F, f7, z1, u0, f8, 1.0F, f7, z1, u1, f8, light, 0.0F, -1.0F, 0.0F);
         }
      }
   }

   private static void v(VertexConsumer vc, Matrix4f mat, Matrix3f nrm, float x, float y, float z, float u, float v, int light, float nx, float ny, float nz) {
      vc.addVertex(mat, x, y, z)
         .setColor(1.0F, 1.0F, 1.0F, 1.0F)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(light & 65535, light >> 16)
         .setNormal(nx, ny, nz);
   }

   private static void quad(
      VertexConsumer vc,
      Matrix4f mat,
      Matrix3f nrm,
      float x0,
      float y0,
      float z0,
      float u0,
      float v0,
      float x1,
      float y1,
      float z1,
      float u1,
      float v1,
      float x2,
      float y2,
      float z2,
      float u2,
      float v2,
      float x3,
      float y3,
      float z3,
      float u3,
      float v3,
      int light,
      float nx,
      float ny,
      float nz
   ) {
      v(vc, mat, nrm, x0, y0, z0, u0, v0, light, nx, ny, nz);
      v(vc, mat, nrm, x1, y1, z1, u1, v1, light, nx, ny, nz);
      v(vc, mat, nrm, x2, y2, z2, u2, v2, light, nx, ny, nz);
      v(vc, mat, nrm, x3, y3, z3, u3, v3, light, nx, ny, nz);
   }
}
