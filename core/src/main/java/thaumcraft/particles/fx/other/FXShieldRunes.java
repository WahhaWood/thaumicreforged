package thaumcraft.particles.fx.other;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thaumcraft.entities.smart.cultist.EntityCultist;

public class FXShieldRunes extends Particle {
   Entity target = null;
   float yaw = 0.0F;
   float pitch = 0.0F;

   public FXShieldRunes(ClientLevel level, double n, double n2, double n3, Entity target, int n4, float yaw, float pitch) {
      super(level, n, n2, n3, 0.0, 0.0, 0.0);
      this.rCol = 1.0F;
      this.gCol = 1.0F;
      this.bCol = 1.0F;
      this.gravity = 0.0F;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.lifetime = n4 + this.random.nextInt(n4 / 2);
      this.hasPhysics = false;
      this.setSize(0.01F, 0.01F);
      this.target = target;
      this.yaw = yaw;
      this.pitch = pitch;
      double posX = target.getX();
      this.x = posX;
      this.xo = posX;
      double centerY = (target.getBoundingBox().minY + target.getBoundingBox().maxY) / 2.0;
      this.y = centerY;
      this.yo = centerY;
      double posZ = target.getZ();
      this.z = posZ;
      this.zo = posZ;
   }

   public void render(@NotNull VertexConsumer consumer, @NotNull Camera camera, float partialTick) {
      Tesselator tesselator = Tesselator.getInstance();
      RenderSystem.disableCull();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      float progress = (this.age + partialTick) / this.lifetime;
      int frameIndex = Math.min(15, (int)(14.0F * progress) + 1);
      float colorMod = 1.0F;
      ResourceLocation texture;
      if (this.target instanceof Mob && !(this.target instanceof EntityCultist)) {
         texture = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/ripple" + frameIndex + ".png");
         colorMod = 0.5F;
      } else {
         texture = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/hemis" + frameIndex + ".png");
      }

      RenderSystem.setShaderTexture(0, texture);
      double interpX = Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x;
      double interpY = Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y;
      double interpZ = Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z;
      PoseStack poseStack = new PoseStack();
      poseStack.translate(interpX, interpY, interpZ);
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - this.yaw));
      poseStack.mulPose(Axis.XN.rotationDegrees(this.pitch));
      float entityHeight = this.target.getBbHeight();
      float scale = 0.4F * entityHeight;
      poseStack.scale(scale, scale, scale);
      Matrix4f matrix = poseStack.last().pose();
      float alpha = Math.min(1.0F, (1.0F - progress) * 3.0F);
      int r = (int)(colorMod * 255.0F);
      int g = (int)(colorMod * 255.0F);
      int b = (int)(colorMod * 255.0F);
      int a = (int)(alpha * 255.0F);
      BufferBuilder buffer = tesselator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      buffer.addVertex(matrix, -1.0F, 1.0F, 0.5F).setUv(0.0F, 1.0F).setColor(r, g, b, a);
      buffer.addVertex(matrix, 1.0F, 1.0F, 0.5F).setUv(1.0F, 1.0F).setColor(r, g, b, a);
      buffer.addVertex(matrix, 1.0F, -1.0F, 0.5F).setUv(1.0F, 0.0F).setColor(r, g, b, a);
      buffer.addVertex(matrix, -1.0F, -1.0F, 0.5F).setUv(0.0F, 0.0F).setColor(r, g, b, a);
      buffer.addVertex(matrix, -0.7F, 1.0F, 0.0F).setUv(0.15F, 1.0F).setColor(r, g, b, a);
      buffer.addVertex(matrix, 0.7F, 1.0F, 0.0F).setUv(0.85F, 1.0F).setColor(r, g, b, a);
      buffer.addVertex(matrix, 1.0F, 1.0F, 0.5F).setUv(1.0F, 0.85F).setColor(r, g, b, a);
      buffer.addVertex(matrix, -1.0F, 1.0F, 0.5F).setUv(0.0F, 0.85F).setColor(r, g, b, a);
      buffer.addVertex(matrix, -1.0F, -1.0F, 0.5F).setUv(0.0F, 0.15F).setColor(r, g, b, a);
      buffer.addVertex(matrix, 1.0F, -1.0F, 0.5F).setUv(1.0F, 0.15F).setColor(r, g, b, a);
      buffer.addVertex(matrix, 0.7F, -1.0F, 0.0F).setUv(0.85F, 0.0F).setColor(r, g, b, a);
      buffer.addVertex(matrix, -0.7F, -1.0F, 0.0F).setUv(0.15F, 0.0F).setColor(r, g, b, a);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
      RenderSystem.disableBlend();
      RenderSystem.enableCull();
      RenderSystem.defaultBlendFunc();
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      }

      if (this.target != null && this.target.isAlive()) {
         this.x = this.target.getX();
         this.y = (this.target.getBoundingBox().minY + this.target.getBoundingBox().maxY) / 2.0;
         this.z = this.target.getZ();
      } else {
         this.remove();
      }
   }

   @NotNull
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }
}
