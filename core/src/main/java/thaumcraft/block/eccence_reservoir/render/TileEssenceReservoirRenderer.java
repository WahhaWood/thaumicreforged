package thaumcraft.block.eccence_reservoir.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.block.eccence_reservoir.EssenceReservoirBlockEntity;

public class TileEssenceReservoirRenderer implements BlockEntityRenderer<EssenceReservoirBlockEntity> {
   public static final ResourceLocation LIQUID_SPRITE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/animatedglow");
   public static final ResourceLocation GLASS_SPRITE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/glass");
   private static final ResourceLocation GLASS_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/glass.png");
   private static final int GLASS_UV_PAD = 2;
   private static final float GLASS_EPS = 0.00125F;
   private static final float GLASS_ALPHA_MUL = 0.9F;
   private static final boolean GLASS_FULLBRIGHT = true;

   public TileEssenceReservoirRenderer(Context ctx) {
   }

   public void render(EssenceReservoirBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      this.renderLiquid(be, pose, buffer);
      renderGlass(pose, buffer, packedLight);
   }

   private void renderLiquid(EssenceReservoirBlockEntity be, PoseStack pose, MultiBufferSource buffer) {
      if (be.displayAspect != null) {
         int amount = be.essence.visSize();
         if (amount > 0 && be.maxAmount > 0) {
            float fill = Mth.clamp((float)amount / be.maxAmount, 0.0F, 1.0F);
            float x1 = 0.1875F;
            float y1 = 0.1875F;
            float z1 = 0.1875F;
            float x2 = 0.8125F;
            float y2 = y1 + 0.625F * fill;
            float z2 = 0.8125F;
            TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(LIQUID_SPRITE);
            VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
            pose.pushPose();
            renderBox(pose, vc, sprite, x1, y1, z1, x2, y2, z2, be.cr, be.cg, be.cb, 0.9F, 15728880);
            pose.popPose();
         }
      }
   }

   static void renderGlass(PoseStack pose, MultiBufferSource buffer, int packedLight) {
      float x1 = 0.12375F;
      float y1 = 0.12375F;
      float z1 = 0.12375F;
      float x2 = 0.87625F;
      float y2 = 0.87625F;
      float z2 = 0.87625F;
      VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(GLASS_TEXTURE));
      int light = 15728880;
      float u0 = 0.125F;
      float u1 = 1.0F - u0;
      float v0 = 0.125F;
      float v1 = 1.0F - v0;
      pose.pushPose();
      renderBoxUV(pose, vc, x1, y1, z1, x2, y2, z2, u0, u1, v0, v1, 1.0F, 1.0F, 1.0F, 0.9F, light);
      pose.popPose();
   }

   private static void renderBox(
      PoseStack pose,
      VertexConsumer vc,
      TextureAtlasSprite sprite,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float r,
      float g,
      float b,
      float a,
      int light
   ) {
      float u0 = sprite.getU0();
      float u1 = sprite.getU1();
      float v0 = sprite.getV0();
      float v1 = sprite.getV1();
      renderBoxUV(pose, vc, x1, y1, z1, x2, y2, z2, u0, u1, v0, v1, r, g, b, a, light);
   }

   private static void renderBoxCroppedUV(
      PoseStack pose,
      VertexConsumer vc,
      TextureAtlasSprite sprite,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float r,
      float g,
      float b,
      float aMul,
      int light,
      int pad
   ) {
      float u0 = sprite.getU(pad);
      float u1 = sprite.getU(16 - pad);
      float v0 = sprite.getV(pad);
      float v1 = sprite.getV(16 - pad);
      renderBoxUV(pose, vc, x1, y1, z1, x2, y2, z2, u0, u1, v0, v1, r, g, b, aMul, light);
   }

   private static void renderBoxUV(
      PoseStack pose,
      VertexConsumer vc,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float u0,
      float u1,
      float v0,
      float v1,
      float r,
      float g,
      float b,
      float a,
      int light
   ) {
      Pose last = pose.last();
      Matrix4f m = last.pose();
      Matrix3f n = last.normal();
      quad(vc, m, n, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, light, 0.0F, -1.0F, 0.0F);
      quad(vc, m, n, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, u0, v0, u1, v1, r, g, b, a, light, 0.0F, 1.0F, 0.0F);
      quad(vc, m, n, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, u0, v0, u1, v1, r, g, b, a, light, 0.0F, 0.0F, -1.0F);
      quad(vc, m, n, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, u0, v0, u1, v1, r, g, b, a, light, 0.0F, 0.0F, 1.0F);
      quad(vc, m, n, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, u0, v0, u1, v1, r, g, b, a, light, -1.0F, 0.0F, 0.0F);
      quad(vc, m, n, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, u0, v0, u1, v1, r, g, b, a, light, 1.0F, 0.0F, 0.0F);
   }

   private static void quad(
      VertexConsumer vc,
      Matrix4f m,
      Matrix3f n,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      float u0,
      float v0,
      float u1,
      float v1,
      float r,
      float g,
      float b,
      float a,
      int light,
      float nx,
      float ny,
      float nz
   ) {
      vert(vc, m, n, x1, y1, z1, u0, v1, r, g, b, a, light, nx, ny, nz);
      vert(vc, m, n, x2, y2, z2, u1, v1, r, g, b, a, light, nx, ny, nz);
      vert(vc, m, n, x3, y3, z3, u1, v0, r, g, b, a, light, nx, ny, nz);
      vert(vc, m, n, x4, y4, z4, u0, v0, r, g, b, a, light, nx, ny, nz);
   }

   private static void vert(
      VertexConsumer vc,
      Matrix4f m,
      Matrix3f n,
      float x,
      float y,
      float z,
      float u,
      float v,
      float r,
      float g,
      float b,
      float a,
      int light,
      float nx,
      float ny,
      float nz
   ) {
      vc.addVertex(m, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(light & 65535, light >> 16).setNormal(nx, ny, nz);
   }
}
