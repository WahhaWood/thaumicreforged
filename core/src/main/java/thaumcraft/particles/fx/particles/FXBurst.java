package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.block.airy.aura_node.AuraNodeRenderer;

public class FXBurst extends Particle {
   public boolean noClip = false;
   private final float quadSize;

   public FXBurst(ClientLevel level, double x, double y, double z, float scale) {
      super(level, x, y, z);
      this.rCol = 1.0F;
      this.gCol = 1.0F;
      this.bCol = 1.0F;
      this.gravity = 0.0F;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.setSize(0.01F, 0.01F);
      this.quadSize = scale;
      this.lifetime = 31;
      this.noClip = false;
      this.hasPhysics = true;
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.hasPhysics = !this.noClip;
      if (this.age++ >= this.lifetime) {
         this.remove();
      }
   }

   public void setGravity(float g) {
      this.gravity = g;
   }

   public void render(VertexConsumer ignored, Camera camera, float partialTicks) {
      if (!this.removed) {
         Vec3 camPos = camera.getPosition();
         float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camPos.x);
         float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camPos.y);
         float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camPos.z);
         float alpha = 0.75F;
         float u0 = this.age % 32 / 32.0F;
         float u1 = u0 + 0.03125F;
         float v0 = 0.96875F;
         float v1 = 1.0F;
         float half = this.quadSize;
         Quaternionf q = new Quaternionf(camera.rotation());
         Vector3f c1 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(half);
         Vector3f c2 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(half);
         Vector3f c3 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(half);
         Vector3f c4 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(half);
         int light = 15728880;
         ResourceLocation tex = AuraNodeRenderer.nodetex;
         RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
         RenderSystem.setShaderTexture(0, tex);
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         Tesselator t = Tesselator.getInstance();
         BufferBuilder buf = t.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);
         buf.addVertex(px + c1.x(), py + c1.y(), pz + c1.z()).setUv(u1, v1).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, alpha);
         buf.addVertex(px + c2.x(), py + c2.y(), pz + c2.z()).setUv(u1, v0).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, alpha);
         buf.addVertex(px + c3.x(), py + c3.y(), pz + c3.z()).setUv(u0, v0).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, alpha);
         buf.addVertex(px + c4.x(), py + c4.y(), pz + c4.z()).setUv(u0, v1).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, alpha);
         BufferUploader.drawWithShader(buf.buildOrThrow());
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
         RenderSystem.depthMask(true);
         RenderSystem.blendFunc(770, 771);
      }
   }
}
