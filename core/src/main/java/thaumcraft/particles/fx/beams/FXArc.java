package thaumcraft.particles.fx.beams;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class FXArc extends Particle {
   public int particle = 16;
   ArrayList<Vec3> points = new ArrayList<>();
   private final Entity targetEntity = null;
   private double tX = 0.0;
   private double tY = 0.0;
   private double tZ = 0.0;
   public int blendmode = 1;
   public float length = 1.0F;
   private AABB bounds;
   public static final ResourceLocation BEAM_H_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beamh.png");

   public FXArc(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, float red, float green, float blue, double speed) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.rCol = red;
      this.gCol = green;
      this.bCol = blue;
      this.setSize(0.02F, 0.02F);
      this.hasPhysics = false;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.tX = tx - x;
      this.tY = ty - y;
      this.tZ = tz - z;
      this.lifetime = 1;
      double gravity = 0.115;
      double jitter = 0.25;
      Vec3 start = new Vec3(0.0, 0.0, 0.0);
      Vec3 end = new Vec3(this.tX, this.tY, this.tZ);
      Vec3 current = new Vec3(0.0, 0.0, 0.0);
      this.length = (float)end.length();
      Vec3 velocity = this.calculateVelocity(start, end, speed, gravity);
      double velocityLengthSq = velocity.lengthSqr();
      this.points.add(start);

      for (int i = 0; end.distanceToSqr(current) > velocityLengthSq && i < 50; i++) {
         Vec3 next = current.add(velocity);
         current = new Vec3(next.x(), next.y(), next.z());
         Vec3 jittered = new Vec3(
            next.x() + (this.random.nextDouble() - this.random.nextDouble()) * jitter,
            next.y() + (this.random.nextDouble() - this.random.nextDouble()) * jitter,
            next.z() + (this.random.nextDouble() - this.random.nextDouble()) * jitter
         );
         this.points.add(jittered);
         velocity = new Vec3(velocity.x(), velocity.y() - gravity / 1.9, velocity.z());
      }

      this.points.add(end);
      AABB box = new AABB(start, start);
      for (Vec3 p : this.points) {
         box = box.minmax(new AABB(p, p));
      }

      this.bounds = box.move(x, y, z).inflate(1.0);
   }

   private Vec3 calculateVelocity(Vec3 start, Vec3 end, double speed, double gravity) {
      double dx = end.x() - start.x();
      double dy = end.y() - start.y();
      double dz = end.z() - start.z();
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      double time = dist / speed;
      double vx = dx / time;
      double vy = dy / time + gravity * time / 2.0;
      double vz = dz / time;
      return new Vec3(vx, vy, vz).normalize().scale(speed);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      }
   }

   public void setRGB(float red, float green, float blue) {
      this.rCol = red;
      this.gCol = green;
      this.bCol = blue;
   }

   public void render(@NotNull VertexConsumer consumer, @NotNull Camera camera, float partialTick) {
      Tesselator tesselator = Tesselator.getInstance();
      float beamWidth = 0.25F;
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderTexture(0, BEAM_H_TEXTURE);
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      PoseStack poseStack = new PoseStack();
      float px = (float)(Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x);
      float py = (float)(Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y);
      float pz = (float)(Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z);
      poseStack.translate(px, py, pz);
      Matrix4f matrix = poseStack.last().pose();
      BufferBuilder buffer = tesselator.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);

      for (int i = 0; i < this.points.size(); i++) {
         Vec3 point = this.points.get(i);
         float u = i / this.length;
         buffer.addVertex(matrix, (float)point.x(), (float)point.y() - beamWidth, (float)point.z())
            .setUv(u, 1.0F)
            .setColor(this.rCol, this.gCol, this.bCol, 0.8F);
         buffer.addVertex(matrix, (float)point.x(), (float)point.y() + beamWidth, (float)point.z())
            .setUv(u, 0.0F)
            .setColor(this.rCol, this.gCol, this.bCol, 0.8F);
      }

      BufferUploader.drawWithShader(buffer.buildOrThrow());
      buffer = tesselator.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);

      for (int i = 0; i < this.points.size(); i++) {
         Vec3 point = this.points.get(i);
         float u = i / this.length;
         buffer.addVertex(matrix, (float)point.x() - beamWidth, (float)point.y(), (float)point.z() - beamWidth)
            .setUv(u, 1.0F)
            .setColor(this.rCol, this.gCol, this.bCol, 0.8F);
         buffer.addVertex(matrix, (float)point.x() + beamWidth, (float)point.y(), (float)point.z() + beamWidth)
            .setUv(u, 0.0F)
            .setColor(this.rCol, this.gCol, this.bCol, 0.8F);
      }

      BufferUploader.drawWithShader(buffer.buildOrThrow());
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
   }

   @NotNull
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   /** NeoForge culls particles by this box alone; it must span the whole beam, not the 0.02 box at its start. */
   @Override
   public AABB getRenderBoundingBox(float partialTicks) {
      return this.bounds;
   }
}
