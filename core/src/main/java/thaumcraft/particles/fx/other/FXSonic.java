package thaumcraft.particles.fx.other;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class FXSonic extends Particle {
   private final Entity target;
   private float yaw;
   private float pitch;
   private float quadSize;

   public FXSonic(ClientLevel level, double x, double y, double z, Entity target, int maxAge) {
      super(level, x, y, z);
      this.target = target;
      this.rCol = 1.0F;
      this.gCol = 1.0F;
      this.bCol = 1.0F;
      int half = Math.max(1, maxAge / 2);
      this.lifetime = Math.max(1, maxAge + this.random.nextInt(half));
      this.hasPhysics = false;
      this.gravity = 0.0F;
      this.quadSize = 1.0F;
      this.alpha = 1.0F;
      this.snapToTarget();
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
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else if (this.target != null && !this.target.isRemoved()) {
         this.snapToTarget();
      } else {
         this.remove();
      }
   }

   private void snapToTarget() {
      if (this.target instanceof LivingEntity le) {
         this.yaw = le.getYHeadRot();
         this.pitch = le.getXRot();
         this.x = le.getX();
         this.y = le.getEyeY();
         this.z = le.getZ();
      } else {
         this.yaw = this.target.getYRot();
         this.pitch = this.target.getXRot();
         this.x = this.target.getX();
         this.y = this.target.getY() + this.target.getBbHeight() * 0.85;
         this.z = this.target.getZ();
      }
   }

   public void render(VertexConsumer ignored, Camera camera, float partialTicks) {
      if (!this.removed && this.target != null && !this.target.isRemoved()) {
         float t = (this.age + partialTicks) / this.lifetime;
         int frame = Math.min(15, (int)(14.0F * t) + 1);
         ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/ripple" + frame + ".png");
         Vec3 camPos = camera.getPosition();
         double sx = Mth.lerp(partialTicks, this.xo, this.x);
         double sy = Mth.lerp(partialTicks, this.yo, this.y);
         double sz = Mth.lerp(partialTicks, this.zo, this.z);
         Vec3 dir = this.target.getViewVector(partialTicks);
         double dist = 2.0 * this.target.getBbHeight() + this.target.getBbWidth() * 0.5;
         Vec3 center = new Vec3(sx, sy, sz).add(dir.scale(dist));
         Vec3 up = new Vec3(0.0, 1.0, 0.0);
         Vec3 right = dir.cross(up);
         if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
         } else {
            right = right.normalize();
         }

         Vec3 up2 = right.cross(dir).normalize();
         float s = (float)(0.25 * this.target.getBbHeight());
         Vec3 r = right.scale(s);
         Vec3 u = up2.scale(s);
         Vec3 p1 = center.subtract(r).subtract(u);
         Vec3 p2 = center.subtract(r).add(u);
         Vec3 p3 = center.add(r).add(u);
         Vec3 p4 = center.add(r).subtract(u);
         float px1 = (float)(p1.x - camPos.x);
         float py1 = (float)(p1.y - camPos.y);
         float pz1 = (float)(p1.z - camPos.z);
         float px2 = (float)(p2.x - camPos.x);
         float py2 = (float)(p2.y - camPos.y);
         float pz2 = (float)(p2.z - camPos.z);
         float px3 = (float)(p3.x - camPos.x);
         float py3 = (float)(p3.y - camPos.y);
         float pz3 = (float)(p3.z - camPos.z);
         float px4 = (float)(p4.x - camPos.x);
         float py4 = (float)(p4.y - camPos.y);
         float pz4 = (float)(p4.z - camPos.z);
         RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
         RenderSystem.setShaderTexture(0, tex);
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(770, 1);
         float c = 0.5F;
         Tesselator tes = Tesselator.getInstance();
         BufferBuilder buf = tes.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         buf.addVertex(px1, py1, pz1).setUv(0.0F, 1.0F).setColor(c, c, c, 1.0F);
         buf.addVertex(px2, py2, pz2).setUv(0.0F, 0.0F).setColor(c, c, c, 1.0F);
         buf.addVertex(px3, py3, pz3).setUv(1.0F, 0.0F).setColor(c, c, c, 1.0F);
         buf.addVertex(px4, py4, pz4).setUv(1.0F, 1.0F).setColor(c, c, c, 1.0F);
         BufferUploader.drawWithShader(buf.buildOrThrow());
         RenderSystem.blendFunc(770, 771);
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
      }
   }
}
