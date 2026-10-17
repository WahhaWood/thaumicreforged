package thaumcraft.block.eldritch.obelisk;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.init.registry.ModDimensions;

public class TileEldritchObeliskRenderer implements BlockEntityRenderer<EldritchObeliskBlockEntity> {
   public static final ResourceLocation T1 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/tunnel.png");
   public static final ResourceLocation T2 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particlefield.png");
   public static final ResourceLocation T3 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particlefield32.png");
   public static final ResourceLocation SIDE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/obelisk_side.png");
   public static final ResourceLocation SIDE_2 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/obelisk_side_2.png");
   public static final ResourceLocation CAP_MODEL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/static/eldritch_altar");
   public static final ResourceLocation CAP_MODEL_OUTER = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/static/eldritch_altar_outer");
   private static final Map<String, RenderType> RENDER_TYPE_CACHE = new HashMap<>();

   public TileEldritchObeliskRenderer(Context ctx) {
   }

   public boolean shouldRenderOffScreen(EldritchObeliskBlockEntity be) {
      return true;
   }

   public int getViewDistance() {
      return 96;
   }

   public void render(EldritchObeliskBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Level level = be.getLevel();
      if (level != null) {
         BlockPos pos = be.getBlockPos();
         Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
         boolean inrange = cam.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 512.0;
         float ticks = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : (float)level.getGameTime();
         float bob = Mth.sin((ticks + partialTick) / 10.0F) * 0.1F + 0.1F;
         int sideLight = LevelRenderer.getLightColor(level, pos);
         float eyeX = (float)(cam.x - pos.getX() - 0.5);
         float eyeY = (float)(cam.y - pos.getY() - 1.0 - bob);
         float eyeZ = (float)(cam.z - pos.getZ() - 0.5);
         pose.pushPose();
         pose.translate(0.5, 1.0 + bob, 0.5);

         for (TileEldritchObeliskRenderer.Plane plane : TileEldritchObeliskRenderer.Plane.values()) {
            drawPlane(pose, buffer, inrange, 3, plane, eyeX, eyeY, eyeZ);
         }

         renderSides(pose, buffer, isOuter(level) ? SIDE_2 : SIDE, sideLight, packedOverlay, 3);
         pose.popPose();
         this.renderCaps(pose, buffer, level, bob, sideLight);
      }
   }

   private void renderCaps(PoseStack pose, MultiBufferSource buffer, Level level, float bob, int light) {
      ResourceLocation modelLoc = isOuter(level) ? CAP_MODEL_OUTER : CAP_MODEL;
      BakedModel capModel = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(modelLoc));
      if (capModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
         VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
         float[][] transforms = new float[][]{{1.0F + bob, 90.0F, 0.0F}, {4.0F + bob, 270.0F, 90.0F}};

         for (float[] t : transforms) {
            pose.pushPose();
            pose.translate(0.5, t[0], 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(t[2]));
            pose.mulPose(Axis.XP.rotationDegrees(t[1]));
            pose.translate(-0.5, 0.0, -0.5);
            renderBakedModel(capModel, pose, vc, light);
            pose.popPose();
         }
      }
   }

   private static boolean isOuter(Level level) {
      return level.dimension().equals(ModDimensions.OUTER_LANDS);
   }

   private static RenderType getPortalRenderType(TileEldritchObeliskRenderer.PortalMode mode, ResourceLocation tex) {
      String key = mode.name() + "_" + tex.toString();
      return RENDER_TYPE_CACHE.computeIfAbsent(key, k -> createPortalRenderType(mode, tex));
   }

   private static RenderType createPortalRenderType(TileEldritchObeliskRenderer.PortalMode mode, ResourceLocation tex) {
      return RenderType.entityTranslucent(tex);
   }

   private static void drawPlane(
      PoseStack pose, MultiBufferSource buffer, boolean inrange, int height, TileEldritchObeliskRenderer.Plane plane, float eyeX, float eyeY, float eyeZ
   ) {
      if (!inrange) {
         drawSimplePlane(pose, buffer.getBuffer(getPortalRenderType(TileEldritchObeliskRenderer.PortalMode.FAR, T3)), height, plane);
      } else {
         Random rnd = new Random(31100L);
         float time = (float)(Util.getMillis() % 700000L) / 700000.0F;

         for (int i = 0; i < 16; i++) {
            float n10 = i == 0 ? 65.0F : 16 - i;
            float scale = i == 0 ? 0.125F : (i == 1 ? 0.5F : 0.0625F);
            float n12 = i == 0 ? 0.1F : 1.0F / (n10 + 1.0F);
            float r;
            float g;
            float b;
            if (i == 0) {
               b = 1.0F;
               g = 1.0F;
               r = 1.0F;
            } else {
               r = rnd.nextFloat() * 0.5F + 0.1F;
               g = rnd.nextFloat() * 0.5F + 0.4F;
               b = rnd.nextFloat() * 0.5F + 0.5F;
            }

            RenderType rt = getPortalRenderType(
               i == 0 ? TileEldritchObeliskRenderer.PortalMode.BASE : TileEldritchObeliskRenderer.PortalMode.ADDITIVE, i == 0 ? T1 : T2
            );
            VertexConsumer vc = buffer.getBuffer(rt);
            drawLayeredQuad(pose, vc, height, plane, i, scale, r * n12, g * n12, b * n12, n10, time, eyeX, eyeY, eyeZ);
         }
      }
   }

   private static void drawLayeredQuad(
      PoseStack pose,
      VertexConsumer vc,
      int height,
      TileEldritchObeliskRenderer.Plane plane,
      int layer,
      float scale,
      float r,
      float g,
      float b,
      float n10,
      float time,
      float eyeX,
      float eyeY,
      float eyeZ
   ) {
      Pose last = pose.last();
      float rot = (layer * layer * 4321.0F + layer * 9.0F) * 2.0F * (float) Math.PI / 180.0F;
      float cs = Mth.cos(rot);
      float sn = Mth.sin(rot);
      float parallax = n10 / 0.001F;
      float[][] verts = plane.isZ
         ? new float[][]{{-0.5F, height}, {-0.5F, 0.0F}, {0.5F, 0.0F}, {0.5F, height}}
         : new float[][]{{height, -0.5F}, {height, 0.5F}, {0.0F, 0.5F}, {0.0F, -0.5F}};
      float pMain = (plane.isZ ? eyeX : eyeZ) + parallax;
      float pY = eyeY + parallax;

      for (float[] v : verts) {
         float x = plane.isZ ? v[0] : plane.offset;
         float y = plane.isZ ? v[1] : v[0];
         float z = plane.isZ ? plane.offset : v[1];
         float uBase = (plane.isZ ? v[0] : v[1]) - pMain;
         float vBase = (plane.isZ ? v[1] : v[0]) - pY;
         float u = uBase * scale * cs - vBase * scale * sn + 0.5F;
         float vv = uBase * scale * sn + vBase * scale * cs + 0.5F + time;
         vc.addVertex(last.pose(), x, y, z)
            .setColor(r, g, b, 1.0F)
            .setUv(u, vv)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setUv2(240, 240)
            .setNormal(plane.normalX(), 0.0F, plane.normalZ());
      }
   }

   private static void drawSimplePlane(PoseStack pose, VertexConsumer vc, int height, TileEldritchObeliskRenderer.Plane plane) {
      Pose last = pose.last();
      float[][] verts = plane.isZ
         ? new float[][]{{-0.5F, height, 0.0F, 1.0F}, {-0.5F, 0.0F, 0.0F, 0.0F}, {0.5F, 0.0F, 1.0F, 0.0F}, {0.5F, height, 1.0F, 1.0F}}
         : new float[][]{{height, -0.5F, 0.0F, 1.0F}, {height, 0.5F, 1.0F, 1.0F}, {0.0F, 0.5F, 1.0F, 0.0F}, {0.0F, -0.5F, 0.0F, 0.0F}};

      for (float[] v : verts) {
         float x = plane.isZ ? v[0] : plane.offset;
         float y = plane.isZ ? v[1] : v[0];
         float z = plane.isZ ? plane.offset : v[1];
         vc.addVertex(last.pose(), x, y, z)
            .setColor(1.0F, 1.0F, 1.0F, 1.0F)
            .setUv(v[2], v[3])
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setUv2(240, 240)
            .setNormal(plane.normalX(), 0.0F, plane.normalZ());
      }
   }

   private static void renderBakedModel(BakedModel model, PoseStack pose, VertexConsumer vc, int light) {
      RandomSource rand = RandomSource.create(42L);

      for (Direction dir : Direction.values()) {
         for (BakedQuad q : model.getQuads(null, dir, rand)) {
            vc.putBulkData(pose.last(), q, 1.0F, 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY, true);
         }
      }

      for (BakedQuad q : model.getQuads(null, null, rand)) {
         vc.putBulkData(pose.last(), q, 1.0F, 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY, true);
      }
   }

   private static void renderSides(PoseStack pose, MultiBufferSource buffer, ResourceLocation tex, int light, int overlay, int height) {
      VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
      Pose last = pose.last();

      for (int i = 0; i < 4; i++) {
         pose.pushPose();
         pose.mulPose(Axis.YP.rotationDegrees(i * 90.0F));
         pose.translate(0.0, 0.0, -0.5);
         Pose m = pose.last();
         float[][] corners = new float[][]{{-0.5F, height, 0.0F, 1.0F}, {0.5F, height, 1.0F, 1.0F}, {0.5F, 0.0F, 1.0F, 0.0F}, {-0.5F, 0.0F, 0.0F, 0.0F}};

         for (float[] c : corners) {
            vc.addVertex(m.pose(), c[0], c[1], 0.0F)
               .setColor(1.0F, 1.0F, 1.0F, 1.0F)
               .setUv(c[2], c[3])
               .setOverlay(overlay)
               .setUv2(light & 65535, light >> 16)
               .setNormal(0.0F, 0.0F, 1.0F);
         }

         pose.popPose();
      }
   }

   private static enum Plane {
      Z_POS(true, 0.499F),
      Z_NEG(true, -0.499F),
      X_POS(false, 0.499F),
      X_NEG(false, -0.499F);

      final boolean isZ;
      final float offset;

      private Plane(boolean isZ, float offset) {
         this.isZ = isZ;
         this.offset = offset;
      }

      float normalX() {
         return this.isZ ? 0.0F : Math.signum(this.offset);
      }

      float normalZ() {
         return this.isZ ? Math.signum(this.offset) : 0.0F;
      }
   }

   private static enum PortalMode {
      BASE,
      ADDITIVE,
      FAR;
   }

   /** The block entity's own box: NeoForge 21.1 asks the renderer, not the block entity. */
   @Override
   public AABB getRenderBoundingBox(EldritchObeliskBlockEntity blockEntity) {
      return blockEntity.getRenderBoundingBox();
   }
}
