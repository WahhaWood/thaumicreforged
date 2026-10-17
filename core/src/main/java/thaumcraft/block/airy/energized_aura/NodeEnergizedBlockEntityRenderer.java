package thaumcraft.block.airy.energized_aura;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.CullStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.RenderStateShard.OverlayStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderStateShard.WriteMaskStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.block.airy.aura_node.AuraNodeRenderer;

public class NodeEnergizedBlockEntityRenderer implements BlockEntityRenderer<NodeEnergizedBlockEntity> {
   public static final ResourceLocation TX1_A = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/item/lightningringv.png");
   public static final ResourceLocation TX1_B = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/items/lightningringv.png");
   private static final Map<ResourceLocation, RenderType> RING_TYPES = new HashMap<>();
   private static final TransparencyStateShard SRC_ALPHA_ONE = new TransparencyStateShard("thaumcraft_src_alpha_one", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   private static final ShaderStateShard EYES_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEyesShader);
   private static final CullStateShard NO_CULL = new CullStateShard(false);
   private static final LightmapStateShard LIGHTMAP = new LightmapStateShard(true);
   private static final OverlayStateShard OVERLAY = new OverlayStateShard(true);
   private static final WriteMaskStateShard COLOR_WRITE = new WriteMaskStateShard(true, false);
   private static boolean META_LOADED = false;
   private static ResourceLocation RESOLVED_TEX = TX1_A;
   private static int FRAMES = 16;

   public NodeEnergizedBlockEntityRenderer(Context ctx) {
   }

   public void render(
      @NotNull NodeEnergizedBlockEntity node,
      float pt,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource bufferSource,
      int brightnessOverride,
      int overlayTexture
   ) {
      Level level = node.getLevel();
      if (level != null) {
         Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
         if (cameraEntity != null) {
            pose.pushPose();
            AuraNodeRenderer.renderNode(
               cameraEntity, pose, bufferSource, 64.0, true, false, pt, node.getBlockPos(), node.getAuraBase(), node.getNodeType(), node.getNodeModifier()
            );
            pose.popPose();
            ResourceLocation tex = getTexture();
            int frames = getFrames(tex);
            int frame = 0;
            if (frames > 1) {
               long nano = System.nanoTime();
               Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
               double relX = node.getBlockPos().getX() + 0.5 - camPos.x;
               double t = nano / 40000000L + relX;
               frame = (int)(t % frames);
               if (frame < 0) {
                  frame += frames;
               }
            }

            float u0 = (float)frame / frames;
            float u1 = (float)(frame + 1) / frames;
            float size = 0.33F;
            float alpha = 0.9F;
            int a = (int)(alpha * 255.0F);
            if (a < 0) {
               a = 0;
            }

            if (a > 255) {
               a = 255;
            }

            pose.pushPose();
            pose.translate(0.5, 0.5, 0.5);
            pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            VertexConsumer vc = bufferSource.getBuffer(ringType(tex));
            Matrix4f mat = pose.last().pose();
            Matrix3f nmat = pose.last().normal();
            int overlay = OverlayTexture.NO_OVERLAY;
            int light = 15728880;
            vc.addVertex(mat, -size, -size, 0.0F)
               .setColor(255, 255, 255, a)
               .setUv(u0, 1.0F)
               .setOverlay(overlay)
               .setUv2(light & 65535, light >> 16)
               .setNormal(0.0F, 0.0F, 1.0F);
            vc.addVertex(mat, size, -size, 0.0F)
               .setColor(255, 255, 255, a)
               .setUv(u1, 1.0F)
               .setOverlay(overlay)
               .setUv2(light & 65535, light >> 16)
               .setNormal(0.0F, 0.0F, 1.0F);
            vc.addVertex(mat, size, size, 0.0F)
               .setColor(255, 255, 255, a)
               .setUv(u1, 0.0F)
               .setOverlay(overlay)
               .setUv2(light & 65535, light >> 16)
               .setNormal(0.0F, 0.0F, 1.0F);
            vc.addVertex(mat, -size, size, 0.0F)
               .setColor(255, 255, 255, a)
               .setUv(u0, 0.0F)
               .setOverlay(overlay)
               .setUv2(light & 65535, light >> 16)
               .setNormal(0.0F, 0.0F, 1.0F);
            pose.popPose();
         }
      }
   }

   private static RenderType ringType(ResourceLocation tex) {
      return RING_TYPES.computeIfAbsent(
         tex,
         t -> RenderType.create(
            "thaumcraft_node_energized_ring",
            DefaultVertexFormat.NEW_ENTITY,
            Mode.QUADS,
            256,
            false,
            true,
            CompositeState.builder()
               .setShaderState(EYES_SHADER)
               .setTextureState(new TextureStateShard(t, false, false))
               .setTransparencyState(SRC_ALPHA_ONE)
               .setCullState(NO_CULL)
               .setLightmapState(LIGHTMAP)
               .setOverlayState(OVERLAY)
               // TC4 drew the ring with alpha test on, so its clear corners wrote no depth; the eyes shader has no
               // alpha test, so with depth write the whole quad cut a square out of the node and of anything translucent behind it.
               .setWriteMaskState(COLOR_WRITE)
               .createCompositeState(false)
         )
      );
   }

   private static ResourceLocation getTexture() {
      if (META_LOADED) {
         return RESOLVED_TEX;
      } else {
         ResourceManager rm = Minecraft.getInstance().getResourceManager();
         if (rm.getResource(TX1_A).isPresent()) {
            RESOLVED_TEX = TX1_A;
         } else if (rm.getResource(TX1_B).isPresent()) {
            RESOLVED_TEX = TX1_B;
         } else {
            RESOLVED_TEX = TX1_A;
         }

         return RESOLVED_TEX;
      }
   }

   private static int getFrames(ResourceLocation tex) {
      if (META_LOADED) {
         return FRAMES;
      } else {
         META_LOADED = true;

         try {
            Resource res = (Resource)Minecraft.getInstance().getResourceManager().getResource(tex).orElse(null);
            if (res == null) {
               return FRAMES;
            }

            try (InputStream in = res.open()) {
               NativeImage img = NativeImage.read(in);

               try {
                  int w = img.getWidth();
                  int h = img.getHeight();
                  if (w > 0 && h > 0 && w >= h && w % h == 0) {
                     FRAMES = Math.max(1, w / h);
                  }
               } catch (Throwable var8) {
                  if (img != null) {
                     try {
                        img.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (img != null) {
                  img.close();
               }
            }
         } catch (Exception var10) {
         }

         return FRAMES;
      }
   }
}
