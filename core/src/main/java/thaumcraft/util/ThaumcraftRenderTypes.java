package thaumcraft.util;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.MultiTextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TexturingStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.RenderType.CompositeState.CompositeStateBuilder;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import thaumcraft.util.record.AuraKey;
import thaumcraft.util.record.FloatyAuraKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public final class ThaumcraftRenderTypes {
   private static final Map<ResourceLocation, RenderType> WATCHER_BEAM = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> GOLEM_OVERLAY_QUAD = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> GOLEM_LINK_STRIP = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> LABEL_ASPECT_ICON = new ConcurrentHashMap<>();
   private static final Map<AuraKey, RenderType> NODE = new ConcurrentHashMap<>();
   private static final Map<FloatyAuraKey, RenderType> FLOATY_LINE = new ConcurrentHashMap<>();
   public static final ResourceLocation TUNNEL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/tunnel.png");
   public static final ResourceLocation PARTICLEFIELD = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particlefield.png");
   public static final ResourceLocation PARTICLEFIELD32 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particlefield32.png");
   public static final ResourceLocation ELD_LOCK_TUNNEL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/tunnel.png");
   public static final ResourceLocation ELD_LOCK_FIELD = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particlefield.png");
   public static final ResourceLocation ELD_LOCK_FIELD_32 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particlefield32.png");
   public static final ResourceLocation ELD_CUBE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/eldritch_cube.png");
   private static final TransparencyStateShard ADDITIVE = new TransparencyStateShard("thaumcraft_additive", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.ONE, DestFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   private static final TransparencyStateShard ADDITIVE_ALPHA_TRANSPARENCY = new TransparencyStateShard("thaumcraft_additive_alpha", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
      RenderSystem.depthMask(true);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   private static final TransparencyStateShard ADDITIVE_ALPHA_NO_DEPTH_WRITE_TRANSPARENCY = new TransparencyStateShard(
      "thaumcraft_additive_alpha_no_depth_write", () -> {
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
      }, () -> {
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
      }
   );
   private static final TexturingStateShard IDENTITY_TEXTURING = new TexturingStateShard(
      "thaumcraft_identity_texturing", RenderSystem::resetTextureMatrix, RenderSystem::resetTextureMatrix
   );
   private static final Function<ResourceLocation, RenderType> FORBIDDEN = Util.memoize(
      texture -> RenderType.create(
         "thaumcraft_forbidden_" + texture.getNamespace() + "_" + texture.getPath().replace('/', '_'),
         DefaultVertexFormat.POSITION_TEX_COLOR,
         Mode.QUADS,
         256,
         false,
         true,
         CompositeState.builder()
            .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
            .setTextureState(new TextureStateShard(texture, true, false))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setLightmapState(RenderStateShard.NO_LIGHTMAP)
            .setOverlayState(RenderStateShard.NO_OVERLAY)
            .setTexturingState(IDENTITY_TEXTURING)
            .createCompositeState(false)
      )
   );
   private static final Function<ResourceLocation, RenderType> FORBIDDEN_GUI = Util.memoize(
      texture -> RenderType.create(
         "thaumcraft_forbidden_gui_" + texture.getNamespace() + "_" + texture.getPath().replace('/', '_'),
         DefaultVertexFormat.POSITION_TEX_COLOR,
         Mode.QUADS,
         256,
         false,
         true,
         CompositeState.builder()
            .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
            .setTextureState(new TextureStateShard(texture, true, false))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setLightmapState(RenderStateShard.NO_LIGHTMAP)
            .setOverlayState(RenderStateShard.NO_OVERLAY)
            .createCompositeState(false)
      )
   );
   private static final Function<ResourceLocation, RenderType> JAR_GLASS = Util.memoize(
      texture -> createEntityTranslucentNoDepthWrite("thaumcraft_jar_glass", texture)
   );
   private static final Function<ResourceLocation, RenderType> JAR_LIQUID = Util.memoize(
      texture -> createEntityTranslucentNoDepthWrite("thaumcraft_jar_liquid", texture)
   );
   private static final Function<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_NO_DEPTH_WRITE = Util.memoize(
      texture -> createEntityTranslucentNoDepthWrite("thaumcraft_entity_translucent_no_depth_write", texture)
   );
   private static final RenderType PORTAL = RenderType.create(
      "thaumcraft_eldritch_nothing_portal",
      DefaultVertexFormat.POSITION,
      Mode.QUADS,
      256,
      false,
      false,
      CompositeState.builder()
         .setShaderState(new ShaderStateShard(GameRenderer::getRendertypeEndPortalShader))
         .setTextureState(MultiTextureStateShard.builder().add(TUNNEL, false, false).add(PARTICLEFIELD, false, false).build())
         .createCompositeState(false)
   );
   private static final RenderType FAR = RenderType.create(
      "thaumcraft_eldritch_nothing_far",
      DefaultVertexFormat.POSITION_TEX,
      Mode.QUADS,
      256,
      false,
      false,
      CompositeState.builder()
         .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexShader))
         .setTextureState(new TextureStateShard(PARTICLEFIELD32, false, false))
         .createCompositeState(false)
   );
   private static final RenderType ELD_LOCK_PORTAL = RenderType.create(
      "tc_eldritch_lock_portal",
      DefaultVertexFormat.POSITION,
      Mode.QUADS,
      256,
      false,
      false,
      CompositeState.builder()
         .setShaderState(new ShaderStateShard(GameRenderer::getRendertypeEndPortalShader))
         .setTextureState(MultiTextureStateShard.builder().add(ELD_LOCK_TUNNEL, false, false).add(ELD_LOCK_FIELD, false, false).build())
         .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
         .setCullState(RenderStateShard.NO_CULL)
         .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
         .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
         .createCompositeState(false)
   );
   private static final RenderType ELD_LOCK_PORTAL_FAR = RenderType.create(
      "tc_eldritch_lock_portal_far",
      DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
      Mode.QUADS,
      256,
      false,
      true,
      CompositeState.builder()
         .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
         .setTextureState(new TextureStateShard(ELD_LOCK_FIELD_32, false, false))
         .setTransparencyState(ADDITIVE)
         .setCullState(RenderStateShard.NO_CULL)
         .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
         .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
         .setLightmapState(RenderStateShard.LIGHTMAP)
         .createCompositeState(false)
   );

   @OnlyIn(Dist.CLIENT)
   private ThaumcraftRenderTypes() {
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType node(ResourceLocation texture, boolean dark, boolean disableDepth) {
      return NODE.computeIfAbsent(
         new AuraKey(texture, dark, disableDepth),
         key -> {
            CompositeStateBuilder builder = CompositeState.builder()
               .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
               .setTextureState(new TextureStateShard(key.texture(), false, false))
               .setTransparencyState(key.dark() ? RenderStateShard.TRANSLUCENT_TRANSPARENCY : RenderStateShard.LIGHTNING_TRANSPARENCY)
               .setCullState(RenderStateShard.NO_CULL)
               .setWriteMaskState(RenderStateShard.COLOR_WRITE)
               .setLightmapState(RenderStateShard.LIGHTMAP)
               .setOverlayState(RenderStateShard.OVERLAY)
               .setLayeringState(RenderStateShard.NO_LAYERING)
               .setOutputState(RenderStateShard.TRANSLUCENT_TARGET);
            builder.setDepthTestState(key.disableDepth() ? RenderStateShard.NO_DEPTH_TEST : RenderStateShard.LEQUAL_DEPTH_TEST);
            return RenderType.create(
               "thaumcraft_node_" + (key.dark() ? "dark" : "add") + (key.disableDepth() ? "_nodepth" : ""),
               DefaultVertexFormat.NEW_ENTITY,
               Mode.QUADS,
               256,
               false,
               true,
               builder.createCompositeState(false)
            );
         }
      );
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType getFloatyLineRenderType(ResourceLocation texture, boolean disableDepth) {
      return FLOATY_LINE.computeIfAbsent(
         new FloatyAuraKey(texture, disableDepth),
         key -> {
            CompositeStateBuilder builder = CompositeState.builder()
               .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
               .setTextureState(new TextureStateShard(key.texture(), false, false))
               .setTransparencyState(ADDITIVE_ALPHA_NO_DEPTH_WRITE_TRANSPARENCY)
               .setCullState(RenderStateShard.NO_CULL)
               .setWriteMaskState(RenderStateShard.COLOR_WRITE)
               .setLightmapState(RenderStateShard.LIGHTMAP)
               .setOverlayState(RenderStateShard.OVERLAY)
               .setLayeringState(RenderStateShard.NO_LAYERING)
               .setOutputState(RenderStateShard.MAIN_TARGET);
            builder.setDepthTestState(key.disableDepth() ? RenderStateShard.NO_DEPTH_TEST : RenderStateShard.LEQUAL_DEPTH_TEST);
            return RenderType.create(
               "thaumcraft_floaty_line" + (key.disableDepth() ? "_nodepth" : ""),
               DefaultVertexFormat.NEW_ENTITY,
               Mode.TRIANGLE_STRIP,
               256,
               false,
               true,
               builder.createCompositeState(false)
            );
         }
      );
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType forbidden(ResourceLocation texture) {
      return FORBIDDEN.apply(texture);
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType forbiddenGui(ResourceLocation texture) {
      return FORBIDDEN_GUI.apply(texture);
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType jarGlass(ResourceLocation texture) {
      return JAR_GLASS.apply(texture);
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType jarLiquid(ResourceLocation texture) {
      return JAR_LIQUID.apply(texture);
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType entityTranslucentNoDepthWrite(ResourceLocation texture) {
      return ENTITY_TRANSLUCENT_NO_DEPTH_WRITE.apply(texture);
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType labelAspectIcon(ResourceLocation texture) {
      return LABEL_ASPECT_ICON.computeIfAbsent(
         texture,
         tex -> RenderType.create(
            "thaumcraft_label_aspect_icon_" + tex.getNamespace() + "_" + tex.getPath().replace('/', '_'),
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            Mode.QUADS,
            256,
            false,
            false,
            CompositeState.builder()
               .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
               .setTextureState(new TextureStateShard(tex, false, false))
               .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
               .setCullState(RenderStateShard.NO_CULL)
               .setLightmapState(RenderStateShard.LIGHTMAP)
               .setOverlayState(RenderStateShard.NO_OVERLAY)
               .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
               .setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
               .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
               .setWriteMaskState(RenderStateShard.COLOR_WRITE)
               .createCompositeState(false)
         )
      );
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType watcherBeam(ResourceLocation texture) {
      return WATCHER_BEAM.computeIfAbsent(
         texture,
         tex -> RenderType.create(
            "thaumcraft_watcher_beam_" + tex.getNamespace() + "_" + tex.getPath().replace('/', '_'),
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            Mode.QUADS,
            256,
            false,
            true,
            CompositeState.builder()
               .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
               .setTextureState(new TextureStateShard(tex, false, false))
               .setTransparencyState(ADDITIVE_ALPHA_TRANSPARENCY)
               .setCullState(RenderStateShard.NO_CULL)
               .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
               .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
               .setLightmapState(RenderStateShard.LIGHTMAP)
               .setOverlayState(RenderStateShard.NO_OVERLAY)
               .createCompositeState(false)
         )
      );
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType golemOverlayQuad(ResourceLocation texture) {
      return GOLEM_OVERLAY_QUAD.computeIfAbsent(
         texture,
         tex -> RenderType.create(
            "thaumcraft_golem_overlay_" + tex.getNamespace() + "_" + tex.getPath().replace('/', '_'),
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            Mode.QUADS,
            256,
            false,
            true,
            CompositeState.builder()
               .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
               .setTextureState(new TextureStateShard(tex, false, false))
               .setTransparencyState(ADDITIVE_ALPHA_NO_DEPTH_WRITE_TRANSPARENCY)
               .setCullState(RenderStateShard.NO_CULL)
               .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
               .setWriteMaskState(RenderStateShard.COLOR_WRITE)
               .setLightmapState(RenderStateShard.LIGHTMAP)
               .setOverlayState(RenderStateShard.NO_OVERLAY)
               .createCompositeState(false)
         )
      );
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType golemLinkStrip(ResourceLocation texture) {
      return GOLEM_LINK_STRIP.computeIfAbsent(
         texture,
         tex -> RenderType.create(
            "thaumcraft_golem_link_" + tex.getNamespace() + "_" + tex.getPath().replace('/', '_'),
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            Mode.TRIANGLE_STRIP,
            256,
            false,
            true,
            CompositeState.builder()
               .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
               .setTextureState(new TextureStateShard(tex, false, false))
               .setTransparencyState(ADDITIVE_ALPHA_NO_DEPTH_WRITE_TRANSPARENCY)
               .setCullState(RenderStateShard.NO_CULL)
               .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
               .setWriteMaskState(RenderStateShard.COLOR_WRITE)
               .setLightmapState(RenderStateShard.LIGHTMAP)
               .setOverlayState(RenderStateShard.NO_OVERLAY)
               .createCompositeState(false)
         )
      );
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType obeliskLayer(String name, ResourceLocation texture, int layer, float scale, boolean additive) {
      TexturingStateShard texturing = new TexturingStateShard("thaumcraft_obelisk_texmat_" + layer, () -> {
         float t = (float)(Util.getMillis() % 700000L) / 250000.0F;
         float rotDeg = (layer * layer * 4321.0F + layer * 9.0F) * 2.0F;
         float rotRad = rotDeg * (float) (Math.PI / 180.0);
         Matrix4f matrix = new Matrix4f();
         matrix.translation(0.0F, t, 0.0F);
         matrix.scale(scale);
         matrix.translate(0.5F, 0.5F, 0.0F);
         matrix.rotateZ(rotRad);
         matrix.translate(-0.5F, -0.5F, 0.0F);
         RenderSystem.setTextureMatrix(matrix);
      }, RenderSystem::resetTextureMatrix);
      return RenderType.create(
         name,
         DefaultVertexFormat.NEW_ENTITY,
         Mode.QUADS,
         256,
         false,
         true,
         CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
            .setTextureState(new TextureStateShard(texture, false, false))
            .setTransparencyState(additive ? ADDITIVE : RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShard.NO_CULL)
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .setOverlayState(RenderStateShard.OVERLAY)
            .setTexturingState(texturing)
            .createCompositeState(true)
      );
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType portal() {
      return PORTAL;
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType far() {
      return FAR;
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType eldritchLockPortal() {
      return ELD_LOCK_PORTAL;
   }

   @OnlyIn(Dist.CLIENT)
   public static RenderType eldritchLockPortalFar() {
      return ELD_LOCK_PORTAL_FAR;
   }

   @OnlyIn(Dist.CLIENT)
   private static RenderType createEntityTranslucentNoDepthWrite(String name, ResourceLocation texture) {
      return RenderType.create(
         name,
         DefaultVertexFormat.NEW_ENTITY,
         Mode.QUADS,
         256,
         false,
         true,
         CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
            .setTextureState(new TextureStateShard(texture, false, false))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShard.NO_CULL)
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .setOverlayState(RenderStateShard.OVERLAY)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
            .createCompositeState(true)
      );
   }
}
