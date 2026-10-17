package thaumcraft.entities.brainless.projectile.explosive_orb;

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
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import thaumcraft.particles.fx.ParticleUtils;

@OnlyIn(Dist.CLIENT)
public class RenderExplosiveOrb<T extends Entity> extends EntityRenderer<T> {
   public RenderExplosiveOrb(Context context) {
      super(context);
      this.shadowRadius = 0.0F;
   }

   public void render(
      @NotNull T entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight
   ) {
      poseStack.pushPose();
      Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
      Quaternionf rotation = camera.rotation();
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 771);
      RenderSystem.setShaderTexture(0, ParticleUtils.PARTICLE_TEXTURE_2);
      float u0 = entity.tickCount % 4 / 16.0F;
      float u1 = u0 + 0.0625F;
      float v0 = 0.8125F;
      float v1 = v0 + 0.0625F;
      poseStack.scale(0.7F, 0.7F, 0.7F);
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
   public ResourceLocation getTextureLocation(@NotNull T entity) {
      return ParticleUtils.PARTICLE_TEXTURE_2;
   }
}
