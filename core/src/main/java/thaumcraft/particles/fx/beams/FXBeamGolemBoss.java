package thaumcraft.particles.fx.beams;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXBeamGolemBoss extends Particle {
   public int particle = 16;
   LivingEntity boss;
   double movX;
   double movY;
   double movZ;
   private float length;
   private float rotYaw;
   private float rotPitch;
   private float prevYaw;
   private float prevPitch;
   private Entity targetEntity;
   private double tX;
   private double tY;
   private double tZ;
   private double ptX;
   private double ptY;
   private double ptZ;
   private int type;
   private float endMod;
   private boolean reverse;
   private boolean pulse;
   private int rotationspeed;
   private float prevSize;
   public int blendmode;
   public float width;
   public static final ResourceLocation BEAM_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam.png");
   public static final ResourceLocation BEAM1_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam1.png");
   public static final ResourceLocation BEAM2_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam2.png");
   public static final ResourceLocation BEAM3_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam3.png");

   public FXBeamGolemBoss(ClientLevel level, LivingEntity boss, Entity target, float red, float green, float blue, int maxAge) {
      super(level, boss.getX(), boss.getY(), boss.getZ(), 0.0, 0.0, 0.0);
      this.boss = boss;
      this.movX = 0.0;
      this.movY = 0.0;
      this.movZ = 0.0;
      this.length = 0.0F;
      this.rotYaw = 0.0F;
      this.rotPitch = 0.0F;
      this.prevYaw = 0.0F;
      this.prevPitch = 0.0F;
      this.targetEntity = target;
      this.tX = 0.0;
      this.tY = 0.0;
      this.tZ = 0.0;
      this.ptX = 0.0;
      this.ptY = 0.0;
      this.ptZ = 0.0;
      this.type = 0;
      this.endMod = 1.0F;
      this.reverse = false;
      this.pulse = true;
      this.rotationspeed = 5;
      this.prevSize = 0.0F;
      this.blendmode = 1;
      this.width = 1.0F;
      float yawRad = -boss.yBodyRot * (float) (Math.PI / 180.0) - (float) Math.PI;
      float cos = Mth.cos(yawRad);
      float sin = Mth.sin(yawRad);
      float pitchCos = -Mth.cos(-boss.getXRot() * (float) (Math.PI / 180.0));
      Vec3 lookVec = new Vec3(sin * pitchCos, Mth.sin(-boss.getXRot() * (float) (Math.PI / 180.0)), cos * pitchCos);
      double startX = boss.getX() + lookVec.x() * 0.5;
      double startY = boss.getY() + boss.getEyeHeight();
      double startZ = boss.getZ() + lookVec.z() * 0.5;
      this.x = startX;
      this.xo = startX;
      this.y = startY;
      this.yo = startY;
      this.z = startZ;
      this.zo = startZ;
      this.rCol = red;
      this.gCol = green;
      this.bCol = blue;
      this.setSize(0.02F, 0.02F);
      this.hasPhysics = false;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      if (target != null) {
         this.tX = target.xo;
         this.tY = target.getBoundingBox().minY + target.getBbHeight() / 2.0F;
         this.tZ = target.zo;
      }

      float dx = (float)(this.x - this.tX);
      float dy = (float)(this.y - this.tY);
      float dz = (float)(this.z - this.tZ);
      this.length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
      double horizDist = Mth.sqrt(dx * dx + dz * dz);
      this.rotYaw = (float)(Math.atan2(dx, dz) * 180.0 / Math.PI);
      this.rotPitch = (float)(Math.atan2(dy, horizDist) * 180.0 / Math.PI);
      this.prevYaw = this.rotYaw;
      this.prevPitch = this.rotPitch;
      this.lifetime = maxAge;
      ParticleUtils.checkParticleDistance(this, 50);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.ptX = this.tX;
      this.ptY = this.tY;
      this.ptZ = this.tZ;
      float yawRad = -this.boss.yBodyRot * (float) (Math.PI / 180.0) - (float) Math.PI;
      float cos = Mth.cos(yawRad);
      float sin = Mth.sin(yawRad);
      float pitchCos = -Mth.cos(-this.boss.getXRot() * (float) (Math.PI / 180.0));
      Vec3 lookVec = new Vec3(sin * pitchCos, Mth.sin(-this.boss.getXRot() * (float) (Math.PI / 180.0)), cos * pitchCos);
      this.x = this.boss.getX() + lookVec.x() * 0.5;
      this.y = this.boss.getY() + this.boss.getEyeHeight();
      this.z = this.boss.getZ() + lookVec.z() * 0.5;
      this.prevYaw = this.rotYaw;
      this.prevPitch = this.rotPitch;
      if (this.targetEntity != null) {
         this.tX = this.targetEntity.xo;
         this.tY = this.targetEntity.getBoundingBox().minY + this.targetEntity.getBbHeight() / 2.0F;
         this.tZ = this.targetEntity.zo;
      }

      float dx = (float)(this.x - this.tX);
      float dy = (float)(this.y - this.tY);
      float dz = (float)(this.z - this.tZ);
      this.length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
      double horizDist = Mth.sqrt(dx * dx + dz * dz);
      this.rotYaw = (float)(Math.atan2(dx, dz) * 180.0 / Math.PI);
      this.rotPitch = (float)(Math.atan2(dy, horizDist) * 180.0 / Math.PI);
      if (this.age++ >= this.lifetime) {
         this.remove();
      }
   }

   public void setRGB(float red, float green, float blue) {
      this.rCol = red;
      this.gCol = green;
      this.bCol = blue;
   }

   public void setType(int type) {
      this.type = type;
   }

   public void setEndMod(float endMod) {
      this.endMod = endMod;
   }

   public void setReverse(boolean reverse) {
      this.reverse = reverse;
   }

   public void setPulse(boolean pulse) {
      this.pulse = pulse;
   }

   public void setRotationspeed(int rotationspeed) {
      this.rotationspeed = rotationspeed;
   }

   public void render(@NotNull VertexConsumer consumer, @NotNull Camera camera, float partialTick) {
      Tesselator tesselator = Tesselator.getInstance();
      float playerTicks = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0.0F;
      float worldTime = (float)(this.level.getGameTime() % (360 / this.rotationspeed) * this.rotationspeed) + this.rotationspeed * partialTick;
      float beamWidth = this.width;
      if (this.pulse) {
         beamWidth = this.prevSize + (Math.min(this.age / 4.0F, this.width) - this.prevSize) * partialTick;
      }

      float alpha = 0.4F;
      if (this.pulse && this.lifetime - this.age <= 4) {
         alpha = 0.4F - (4 - (this.lifetime - this.age)) * 0.1F;
      }
      ResourceLocation texture = switch (this.type) {
         case 1 -> BEAM1_TEXTURE;
         case 2 -> BEAM2_TEXTURE;
         case 3 -> BEAM3_TEXTURE;
         default -> BEAM_TEXTURE;
      };
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderTexture(0, texture);
      RenderSystem.disableCull();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.depthMask(false);
      float scrollOffset = playerTicks + partialTick;
      if (this.reverse) {
         scrollOffset *= -1.0F;
      }

      float texOffset = -scrollOffset * 0.2F - Mth.floor(-scrollOffset * 0.1F);
      PoseStack poseStack = new PoseStack();
      float px = (float)(Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x);
      float py = (float)(Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y);
      float pz = (float)(Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z);
      poseStack.translate(px, py, pz);
      float interpYaw = Mth.lerp(partialTick, this.prevYaw, this.rotYaw);
      float interpPitch = Mth.lerp(partialTick, this.prevPitch, this.rotPitch);
      poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
      poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F + interpYaw));
      poseStack.mulPose(Axis.XP.rotationDegrees(interpPitch));
      double startWidth = -0.15 * beamWidth;
      double endWidthStart = 0.15 * beamWidth;
      double endWidth1 = -0.15 * beamWidth * this.endMod;
      double endWidth2 = 0.15 * beamWidth * this.endMod;
      poseStack.mulPose(Axis.YP.rotationDegrees(worldTime));
      Matrix4f matrix = poseStack.last().pose();

      for (int i = 0; i < 3; i++) {
         double beamLength = this.length * beamWidth / this.width;
         double u0 = 0.0;
         double u1 = 1.0;
         double v0 = -1.0F + texOffset + i / 3.0F;
         double v1 = this.length * beamWidth / this.width + v0;
         poseStack.mulPose(Axis.YP.rotationDegrees(60.0F));
         matrix = poseStack.last().pose();
         BufferBuilder buffer = tesselator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         buffer.addVertex(matrix, (float)endWidth1, (float)beamLength, 0.0F).setUv((float)u1, (float)v1).setColor(this.rCol, this.gCol, this.bCol, alpha);
         buffer.addVertex(matrix, (float)startWidth, 0.0F, 0.0F).setUv((float)u1, (float)v0).setColor(this.rCol, this.gCol, this.bCol, alpha);
         buffer.addVertex(matrix, (float)endWidthStart, 0.0F, 0.0F).setUv((float)u0, (float)v0).setColor(this.rCol, this.gCol, this.bCol, alpha);
         buffer.addVertex(matrix, (float)endWidth2, (float)beamLength, 0.0F).setUv((float)u0, (float)v1).setColor(this.rCol, this.gCol, this.bCol, alpha);
         BufferUploader.drawWithShader(buffer.buildOrThrow());
      }

      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      RenderSystem.enableCull();
      this.renderImpact(camera, partialTick);
      this.prevSize = beamWidth;
   }

   private void renderImpact(Camera camera, float partialTick) {
      Tesselator tesselator = Tesselator.getInstance();
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      float u0 = this.age % 16 / 16.0F;
      float u1 = u0 + 0.0624375F;
      float v0 = 0.3125F;
      float v1 = v0 + 0.0624375F;
      float scale = this.endMod / 4.0F * this.width;
      float px = (float)(Mth.lerp(partialTick, this.ptX, this.tX) - camera.getPosition().x);
      float py = (float)(Mth.lerp(partialTick, this.ptY, this.tY) - camera.getPosition().y);
      float pz = (float)(Mth.lerp(partialTick, this.ptZ, this.tZ) - camera.getPosition().z);
      Quaternionf rotation = camera.rotation();
      Vector3f[] vertices = new Vector3f[]{
         new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
      };

      for (Vector3f vertex : vertices) {
         vertex.rotate(rotation);
         vertex.mul(scale);
         vertex.add(px, py, pz);
      }

      BufferBuilder buffer = tesselator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      buffer.addVertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, 0.66F);
      buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, 0.66F);
      buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, 0.66F);
      buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, 0.66F);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
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
      return new AABB(this.x, this.y, this.z, this.tX, this.tY, this.tZ).inflate(1.0 + this.width);
   }
}
