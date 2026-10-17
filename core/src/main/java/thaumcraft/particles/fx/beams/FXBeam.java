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
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXBeam extends Particle {
   public int particle = 16;
   boolean updated = false;
   double movX = 0.0;
   double movY = 0.0;
   double movZ = 0.0;
   private float length = 0.0F;
   private float rotYaw = 0.0F;
   private float rotPitch = 0.0F;
   private float prevYaw = 0.0F;
   private float prevPitch = 0.0F;
   private Entity targetEntity = null;
   private double tX = 0.0;
   private double tY = 0.0;
   private double tZ = 0.0;
   private int type = 0;
   private float endMod = 1.0F;
   private boolean reverse = false;
   private boolean pulse = true;
   private int rotationspeed = 5;
   private float prevSize = 0.0F;
   public int blendmode = 1;
   public float width = 1.0F;
   public static final ResourceLocation BEAM_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam.png");
   public static final ResourceLocation BEAM1_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam1.png");
   public static final ResourceLocation BEAM2_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam2.png");
   public static final ResourceLocation BEAM3_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam3.png");

   public FXBeam(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, float red, float green, float blue, int maxAge) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.rCol = red;
      this.gCol = green;
      this.bCol = blue;
      this.setSize(0.02F, 0.02F);
      this.hasPhysics = false;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.tX = tx;
      this.tY = ty;
      this.tZ = tz;
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

   public FXBeam(ClientLevel level, double x, double y, double z, Entity target, float red, float green, float blue, int maxAge) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.rCol = red;
      this.gCol = green;
      this.bCol = blue;
      this.setSize(0.02F, 0.02F);
      this.hasPhysics = false;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.targetEntity = target;
      this.tX = target.getX();
      this.tY = target.getY() + target.getEyeHeight() - target.getBbHeight() / 2.0F;
      this.tZ = target.getZ();
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

   public void updateBeam(double movX, double movY, double movZ, double tx, double ty, double tz) {
      this.movX = movX;
      this.movY = movY;
      this.movZ = movZ;
      this.tX = tx;
      this.tY = ty;
      this.tZ = tz;

      while (this.lifetime - this.age < 4) {
         this.lifetime++;
      }

      this.updated = true;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.updated) {
         this.x = this.movX;
         this.y = this.movY;
         this.z = this.movZ;
         this.updated = false;
      }

      this.prevYaw = this.rotYaw;
      this.prevPitch = this.rotPitch;
      if (this.targetEntity != null) {
         this.tX = this.targetEntity.getX();
         this.tY = this.targetEntity.getY() + this.targetEntity.getEyeHeight() - this.targetEntity.getBbHeight() / 2.0F;
         this.tZ = this.targetEntity.getZ();
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
      this.prevSize = beamWidth;
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
