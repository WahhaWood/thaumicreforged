package thaumcraft.particles.fx.beams;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXBeamWand extends Particle {
   public int particle = 16;
   public int impact = 0;
   public Player player;
   private float length;
   private double tX;
   private double tY;
   private double tZ;
   private double ptX;
   private double ptY;
   private double ptZ;
   private int type = 0;
   private float endMod = 1.0F;
   private float quadSize;
   private boolean reverse = false;
   private boolean pulse = true;
   private int rotationspeed = 5;
   private float prevSize = 0.0F;
   private static final double START_FORWARD = 0.35;
   private static final double START_SIDE = 0.28;
   private static final double START_DOWN = 0.18;
   private static final float DOWN_OFFSET = -1.5F;
   private static final float FORWARD_OFFSET = 0.3F;
   private static final float THIRD_PERSON_HEIGHT_ADD = 0.25F;
   private static final float HAND_OFFSET = 0.35F;

   public FXBeamWand(ClientLevel level, Player player, double tx, double ty, double tz, float r, float g, float b, int maxAge) {
      super(level, player.getX(), player.getY(), player.getZ());
      this.player = player;
      this.rCol = r;
      this.gCol = g;
      this.bCol = b;
      this.hasPhysics = false;
      this.gravity = 0.0F;
      this.setSize(0.02F, 0.02F);
      this.quadSize = 1.0F;
      this.alpha = 1.0F;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.tX = tx;
      this.tY = ty;
      this.tZ = tz;
      this.ptX = tx;
      this.ptY = ty;
      this.ptZ = tz;
      this.lifetime = Math.max(1, maxAge);
      Vec3 sp = this.computeStart(1.0F);
      this.x = sp.x;
      this.y = sp.y;
      this.z = sp.z;
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.tooFar(player)) {
         this.lifetime = 0;
         this.remove();
      }

      this.length = (float)this.computeStart(1.0F).distanceTo(new Vec3(this.tX, this.tY, this.tZ));
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void updateBeam(double tx, double ty, double tz) {
      this.tX = tx;
      this.tY = ty;
      this.tZ = tz;

      while (this.lifetime - this.age < 4) {
         this.lifetime++;
      }
   }

   public void setRGB(float r, float g, float b) {
      this.rCol = r;
      this.gCol = g;
      this.bCol = b;
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
      this.rotationspeed = Math.max(1, rotationspeed);
   }

   public void tick() {
      if (this.player != null && !this.player.isRemoved()) {
         this.xo = this.x;
         this.yo = this.y;
         this.zo = this.z;
         Vec3 sp = this.computeStart(1.0F);
         this.x = sp.x;
         this.y = sp.y;
         this.z = sp.z;
         this.ptX = this.tX;
         this.ptY = this.tY;
         this.ptZ = this.tZ;
         this.length = (float)this.computeStart(1.0F).distanceTo(new Vec3(this.tX, this.tY, this.tZ));
         if (this.impact > 0) {
            this.impact--;
         }

         if (this.age++ >= this.lifetime) {
            this.remove();
         }
      } else {
         this.remove();
      }
   }

   public void render(VertexConsumer ignored, Camera camera, float pt) {
      if (!this.removed && this.player != null && !this.player.isRemoved()) {
         if (!this.tooFar(this.player)) {
            Vec3 start = this.startPos(pt);
            double ex = Mth.lerp(pt, this.ptX, this.tX);
            double ey = Mth.lerp(pt, this.ptY, this.tY);
            double ez = Mth.lerp(pt, this.ptZ, this.tZ);
            Vec3 endTarget = new Vec3(ex, ey, ez);
            Vec3 diff = endTarget.subtract(start);
            double dist = diff.length();
            if (!(dist < 1.0E-4)) {
               Vec3 forward = diff.normalize();
               float t = this.player.tickCount + pt;
               if (this.reverse) {
                  t *= -1.0F;
               }

               float scroll = -t * 0.2F - Mth.floor(-t * 0.1F);
               float targetSize = this.pulse ? Math.min(this.age / 4.0F, 1.0F) : 1.0F;
               float size = this.pulse ? this.prevSize + (targetSize - this.prevSize) * pt : 1.0F;
               float alpha = 0.4F;
               if (this.pulse && this.lifetime - this.age <= 4) {
                  alpha = 0.4F - (4 - (this.lifetime - this.age)) * 0.1F;
               }

               if (!(alpha <= 0.001F) && !(size <= 0.001F)) {
                  Vec3 camLook = new Vec3(camera.getLookVector());
                  Vec3 camUp = new Vec3(camera.getUpVector());
                  Vec3 right0 = forward.cross(camUp);
                  if (right0.lengthSqr() < 1.0E-6) {
                     right0 = forward.cross(camLook);
                  }

                  if (right0.lengthSqr() < 1.0E-6) {
                     if (Math.abs(forward.y) < 0.9) {
                        right0 = forward.cross(new Vec3(0.0, 1.0, 0.0));
                     } else {
                        right0 = forward.cross(new Vec3(1.0, 0.0, 0.0));
                     }
                  }

                  if (right0.lengthSqr() < 1.0E-8) {
                     this.prevSize = size;
                  } else {
                     right0 = right0.normalize();
                     Vec3 up0 = forward.cross(right0).normalize();
                     int cycle = Math.max(1, 360 / this.rotationspeed);
                     float rot = (float)(this.level.getGameTime() % cycle * this.rotationspeed) + this.rotationspeed * pt;
                     ResourceLocation beamTex = beamTexture(this.type);
                     RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                     RenderSystem.setShaderTexture(0, beamTex);
                     GlStateManager._texParameter(3553, 10242, 10497);
                     GlStateManager._texParameter(3553, 10243, 10497);
                     RenderSystem.disableCull();
                     RenderSystem.enableBlend();
                     RenderSystem.depthMask(false);
                     RenderSystem.blendFunc(770, 1);
                     Vec3 cam = camera.getPosition();
                     float w0 = 0.15F * size;
                     float w1 = 0.15F * size * this.endMod;
                     double beamLen = dist * size;
                     Vec3 beamEnd = start.add(forward.scale(beamLen));
                     Tesselator tes = Tesselator.getInstance();
                     BufferBuilder buf = tes.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                     int cr = Mth.clamp((int)(this.rCol * 255.0F), 0, 255);
                     int cg = Mth.clamp((int)(this.gCol * 255.0F), 0, 255);
                     int cb = Mth.clamp((int)(this.bCol * 255.0F), 0, 255);
                     int ca = Mth.clamp((int)(alpha * 255.0F), 0, 255);

                     for (int i = 0; i < 3; i++) {
                        float angDeg = rot + i * 60.0F;
                        float ang = angDeg * (float) (Math.PI / 180.0);
                        double cos = Math.cos(ang);
                        double sin = Math.sin(ang);
                        Vec3 side = right0.scale(cos).add(up0.scale(sin));
                        float v0 = -1.0F + scroll + i / 3.0F;
                        float v1 = (float)(beamLen + v0);
                        Vec3 sL = start.add(side.scale(-w0));
                        Vec3 sR = start.add(side.scale(w0));
                        Vec3 eL = beamEnd.add(side.scale(-w1));
                        Vec3 eR = beamEnd.add(side.scale(w1));
                        float x1 = (float)(eL.x - cam.x);
                        float y1 = (float)(eL.y - cam.y);
                        float z1 = (float)(eL.z - cam.z);
                        float x2 = (float)(sL.x - cam.x);
                        float y2 = (float)(sL.y - cam.y);
                        float z2 = (float)(sL.z - cam.z);
                        float x3 = (float)(sR.x - cam.x);
                        float y3 = (float)(sR.y - cam.y);
                        float z3 = (float)(sR.z - cam.z);
                        float x4 = (float)(eR.x - cam.x);
                        float y4 = (float)(eR.y - cam.y);
                        float z4 = (float)(eR.z - cam.z);
                        buf.addVertex(x1, y1, z1).setUv(1.0F, v1).setColor(cr, cg, cb, ca);
                        buf.addVertex(x2, y2, z2).setUv(1.0F, v0).setColor(cr, cg, cb, ca);
                        buf.addVertex(x3, y3, z3).setUv(0.0F, v0).setColor(cr, cg, cb, ca);
                        buf.addVertex(x4, y4, z4).setUv(0.0F, v1).setColor(cr, cg, cb, ca);
                     }

                     BufferUploader.drawWithShader(buf.buildOrThrow());
                     if (this.impact > 0) {
                        this.renderImpact(camera, pt, (float)ex, (float)ey, (float)ez);
                     }

                     RenderSystem.blendFunc(770, 771);
                     RenderSystem.depthMask(true);
                     RenderSystem.disableBlend();
                     RenderSystem.enableCull();
                     this.prevSize = size;
                  }
               } else {
                  this.prevSize = size;
               }
            }
         }
      }
   }

   private void renderImpact(Camera camera, float pt, float ex, float ey, float ez) {
      float s = this.endMod / 2.0F / (6 - this.impact);
      Vec3 camPos = camera.getPosition();
      float px = ex - (float)camPos.x;
      float py = ey - (float)camPos.y;
      float pz = ez - (float)camPos.z;
      Quaternionf q = new Quaternionf(camera.rotation());
      Vector3f c1 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(s);
      Vector3f c2 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(s);
      Vector3f c3 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(s);
      Vector3f c4 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(s);
      float u0 = this.age % 16 / 16.0F;
      float u1 = u0 + 0.0624375F;
      float v0 = 0.3125F;
      float v1 = v0 + 0.0624375F;
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderTexture(0, ParticleUtils.PARTICLE_TEXTURE);
      RenderSystem.disableCull();
      RenderSystem.enableBlend();
      RenderSystem.depthMask(false);
      RenderSystem.blendFunc(770, 1);
      int cr = Mth.clamp((int)(this.rCol * 255.0F), 0, 255);
      int cg = Mth.clamp((int)(this.gCol * 255.0F), 0, 255);
      int cb = Mth.clamp((int)(this.bCol * 255.0F), 0, 255);
      int ca = 168;
      Tesselator tes = Tesselator.getInstance();
      BufferBuilder buf = tes.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      buf.addVertex(px + c1.x(), py + c1.y(), pz + c1.z()).setUv(u1, v1).setColor(cr, cg, cb, ca);
      buf.addVertex(px + c2.x(), py + c2.y(), pz + c2.z()).setUv(u1, v0).setColor(cr, cg, cb, ca);
      buf.addVertex(px + c3.x(), py + c3.y(), pz + c3.z()).setUv(u0, v0).setColor(cr, cg, cb, ca);
      buf.addVertex(px + c4.x(), py + c4.y(), pz + c4.z()).setUv(u0, v1).setColor(cr, cg, cb, ca);
      BufferUploader.drawWithShader(buf.buildOrThrow());
      RenderSystem.blendFunc(770, 771);
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      RenderSystem.enableCull();
   }

   private Vec3 startPos(float pt) {
      return this.computeStart(pt);
   }

   private Vec3 computeStart(float pt) {
      Minecraft mc = Minecraft.getInstance();
      Entity camEnt = mc.getCameraEntity();
      boolean firstPerson = mc.options.getCameraType().isFirstPerson() && camEnt != null && camEnt.getId() == this.player.getId();
      HumanoidArm mainArm = this.player.getMainArm();
      InteractionHand activeHand = this.player.getUsedItemHand();
      if (this.player.getUseItemRemainingTicks() <= 0) {
         activeHand = this.player.swingingArm != null ? this.player.swingingArm : InteractionHand.MAIN_HAND;
      }

      boolean isRightHandPhysical;
      if (activeHand == InteractionHand.MAIN_HAND) {
         isRightHandPhysical = mainArm == HumanoidArm.RIGHT;
      } else {
         isRightHandPhysical = mainArm == HumanoidArm.LEFT;
      }

      float activeForwardOffset = 0.3F;
      float activeDownOffset = -1.5F;
      float activeSideOffset;
      if (isRightHandPhysical) {
         activeSideOffset = -0.35F;
         activeDownOffset += 0.2F;
      } else {
         activeSideOffset = 0.35F;
         activeDownOffset += 0.2F;
         activeForwardOffset += 0.2F;
         activeSideOffset -= 0.15F;
      }

      double px = Mth.lerp(pt, this.player.xo, this.player.getX());
      double py = Mth.lerp(pt, this.player.yo, this.player.getY());
      double pz = Mth.lerp(pt, this.player.zo, this.player.getZ());
      float yaw = Mth.lerp(pt, this.player.yRotO, this.player.getYRot());
      float yawRad = yaw * (float) (Math.PI / 180.0);
      Vec3 look = this.player.getViewVector(pt);
      double offsetY;
      if (firstPerson) {
         offsetY = 0.0;
      } else {
         offsetY = this.player.getBbHeight() / 2.0F + 0.25F;
      }

      double sideX = Mth.cos(yawRad) * activeSideOffset;
      double sideZ = Mth.sin(yawRad) * activeSideOffset;
      double forwardX = look.x * activeForwardOffset;
      double forwardY = look.y * activeForwardOffset;
      double forwardZ = look.z * activeForwardOffset;
      double startX = px + sideX + forwardX;
      double startY = py + offsetY - activeDownOffset + forwardY;
      double startZ = pz + sideZ + forwardZ;
      return new Vec3(startX, startY, startZ);
   }

   private boolean tooFar(Player p) {
      Entity cam = Minecraft.getInstance().getCameraEntity();
      if (cam == null) {
         return false;
      } else {
         GraphicsStatus gs = (GraphicsStatus)Minecraft.getInstance().options.graphicsMode().get();
         int max = gs == GraphicsStatus.FAST ? 25 : 50;
         Vec3 sp = this.computeStart(1.0F);
         double dx = cam.getX() - sp.x;
         double dy = cam.getY() - sp.y;
         double dz = cam.getZ() - sp.z;
         return dx * dx + dy * dy + dz * dz > max * max;
      }
   }

   private static ResourceLocation beamTexture(int type) {
      return switch (type) {
         case 1 -> ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam1.png");
         case 2 -> ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam2.png");
         case 3 -> ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam3.png");
         default -> ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/beam.png");
      };
   }

   public boolean shouldCull() {
      return false;
   }

   /** NeoForge culls particles by this box alone; it must span the whole beam, not the 0.02 box at its start. */
   @Override
   public AABB getRenderBoundingBox(float partialTicks) {
      return this.player == null
         ? super.getRenderBoundingBox(partialTicks)
         : new AABB(this.player.getX(), this.player.getEyeY(), this.player.getZ(), this.tX, this.tY, this.tZ).inflate(1.0);
   }
}
