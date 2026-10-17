package thaumcraft.entities.brainless.projectile.shock_orb;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import thaumcraft.particles.fx.ParticleUtils;

@OnlyIn(Dist.CLIENT)
public class RenderElectricOrb extends EntityRenderer<EntityShockOrb> {
   public RenderElectricOrb(Context context) {
      super(context);
      this.shadowRadius = 0.0F;
   }

   public void render(
      @NotNull EntityShockOrb entity,
      float entityYaw,
      float partialTick,
      @NotNull PoseStack poseStack,
      @NotNull MultiBufferSource bufferSource,
      int packedLight
   ) {
      poseStack.pushPose();
      Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
      Quaternionf rotation = camera.rotation();
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShaderTexture(0, ParticleUtils.PARTICLE_TEXTURE);
      float u0 = (1 + entity.tickCount % 6) / 8.0F;
      float u1 = u0 + 0.125F;
      float v0 = 0.875F;
      float v1 = v0 + 0.125F;
      float pulse = Mth.sin(entity.tickCount / 5.0F) * 0.2F + 0.2F;
      poseStack.scale(1.0F + pulse, 1.0F + pulse, 1.0F + pulse);
      poseStack.mulPose(rotation);
      Matrix4f matrix = poseStack.last().pose();
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder buffer = tesselator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      float halfWidth = 0.5F;
      int alpha = 204;
      buffer.addVertex(matrix, -halfWidth, -halfWidth, 0.0F).setUv(u0, v1).setColor(255, 255, 255, alpha);
      buffer.addVertex(matrix, halfWidth, -halfWidth, 0.0F).setUv(u1, v1).setColor(255, 255, 255, alpha);
      buffer.addVertex(matrix, halfWidth, halfWidth, 0.0F).setUv(u1, v0).setColor(255, 255, 255, alpha);
      buffer.addVertex(matrix, -halfWidth, halfWidth, 0.0F).setUv(u0, v0).setColor(255, 255, 255, alpha);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
      poseStack.popPose();
      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull EntityShockOrb entity) {
      return ParticleUtils.PARTICLE_TEXTURE;
   }
}
