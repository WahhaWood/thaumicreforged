package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXSmokeSpiral extends Particle {
   private final float radius;
   private final float startDeg;
   private final int minY;
   private static final int FRAME_BASE = 1;
   private static final int FRAME_COUNT = 5;
   private static final float TILE = 0.0625F;
   private static final float TILE_INNER = 0.0624375F;

   public FXSmokeSpiral(ClientLevel level, double x, double y, double z, float radius, int start, int miny) {
      super(level, x, y, z);
      this.radius = radius;
      this.startDeg = start;
      this.minY = miny;
      this.gravity = -0.01F;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.lifetime = 20 + level.random.nextInt(10);
      this.hasPhysics = false;
      this.setSize(0.01F, 0.01F);
      this.alpha = 1.0F;
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
   }

   public void setColor(float r, float g, float b) {
      this.rCol = r;
      this.gCol = g;
      this.bCol = b;
   }

   public void setRGB(float r, float g, float b) {
      this.setColor(r, g, b);
   }

   public ParticleRenderType getRenderType() {
      return ParticleUtils.TC_PARTICLE;
   }

   public void tick() {
      this.alpha = (float)(this.lifetime - this.age) / this.lifetime;
      if (this.age++ >= this.lifetime) {
         this.remove();
      }
   }

   public void render(VertexConsumer buf, Camera camera, float partialTicks) {
      if (!this.removed) {
         float progress = (this.age + partialTicks) / this.lifetime;
         int frame = 1 + (int)(progress * 4.0F);
         if (frame < 1) {
            frame = 1;
         }

         if (frame > 5) {
            frame = 5;
         }

         float angDeg = this.startDeg + 720.0F * progress;
         float pitchDeg = 90.0F - 180.0F * progress;
         float ang = angDeg * (float) (Math.PI / 180.0);
         float pitch = pitchDeg * (float) (Math.PI / 180.0);
         float vx = -Mth.sin(ang) * Mth.cos(pitch);
         float vz = Mth.cos(ang) * Mth.cos(pitch);
         float vy = -Mth.sin(pitch);
         float ox = vx * this.radius;
         float oy = vy * this.radius;
         float oz = vz * this.radius;
         double worldX = this.x + ox;
         double worldY = Math.max(this.y + oy, this.minY + 0.1);
         double worldZ = this.z + oz;
         Vec3 camPos = camera.getPosition();
         float px = (float)(worldX - camPos.x);
         float py = (float)(worldY - camPos.y);
         float pz = (float)(worldZ - camPos.z);
         float u0 = frame % 16 * 0.0625F;
         float v0 = frame / 16 * 0.0625F;
         float u1 = u0 + 0.0624375F;
         float v1 = v0 + 0.0624375F;
         float size = 0.15F;
         Quaternionf q = new Quaternionf(camera.rotation());
         Vector3f c1 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(size);
         Vector3f c2 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(size);
         Vector3f c3 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(size);
         Vector3f c4 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(size);
         int light = this.getLightColor(partialTicks);
         float a = 0.66F * this.alpha;
         buf.addVertex(px + c1.x(), py + c1.y(), pz + c1.z()).setUv(u1, v1).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
         buf.addVertex(px + c2.x(), py + c2.y(), pz + c2.z()).setUv(u1, v0).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
         buf.addVertex(px + c3.x(), py + c3.y(), pz + c3.z()).setUv(u0, v0).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
         buf.addVertex(px + c4.x(), py + c4.y(), pz + c4.z()).setUv(u0, v1).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
      }
   }
}
