package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXSparkle extends Particle {
   public boolean leyLineEffect = false;
   public int multiplier = 2;
   public float quadSize;
   public boolean shrink = true;
   public int particle = 16;
   public boolean tinkle = false;
   public int blendmode = 1;
   public boolean slowdown = true;
   public int currentColor = 0;
   public boolean noClip = false;
   private float baseQuad;

   public FXSparkle(ClientLevel level, double x, double y, double z, float scale, float r, float g, float b, int multiplier) {
      super(level, x, y, z);
      if (r == 0.0F) {
         r = 1.0F;
      }

      this.rCol = r;
      this.gCol = g;
      this.bCol = b;
      this.gravity = 0.0F;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.quadSize = 1.0F;
      this.scale(scale);
      this.baseQuad = this.quadSize;
      this.multiplier = Math.max(1, multiplier);
      this.lifetime = 3 * this.multiplier;
      this.noClip = false;
      this.hasPhysics = true;
      this.setSize(0.01F, 0.01F);
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
   }

   public FXSparkle(ClientLevel level, double x, double y, double z, float scale, int currentColor, int multiplier) {
      this(level, x, y, z, scale, 0.0F, 0.0F, 0.0F, multiplier);
      this.currentColor = currentColor;
      switch (currentColor) {
         case 0:
            this.rCol = 0.75F + level.random.nextFloat() * 0.25F;
            this.gCol = 0.25F + level.random.nextFloat() * 0.25F;
            this.bCol = 0.75F + level.random.nextFloat() * 0.25F;
            break;
         case 1:
            this.rCol = 0.5F + level.random.nextFloat() * 0.3F;
            this.gCol = 0.5F + level.random.nextFloat() * 0.3F;
            this.bCol = 0.2F;
            break;
         case 2:
            this.rCol = 0.2F;
            this.gCol = 0.2F;
            this.bCol = 0.7F + level.random.nextFloat() * 0.3F;
            break;
         case 3:
            this.rCol = 0.2F;
            this.gCol = 0.7F + level.random.nextFloat() * 0.3F;
            this.bCol = 0.2F;
            break;
         case 4:
            this.rCol = 0.7F + level.random.nextFloat() * 0.3F;
            this.gCol = 0.2F;
            this.bCol = 0.2F;
            break;
         case 5:
            this.blendmode = 771;
            this.rCol = level.random.nextFloat() * 0.1F;
            this.gCol = level.random.nextFloat() * 0.1F;
            this.bCol = level.random.nextFloat() * 0.1F;
            break;
         case 6:
            this.rCol = 0.8F + level.random.nextFloat() * 0.2F;
            this.gCol = 0.8F + level.random.nextFloat() * 0.2F;
            this.bCol = 0.8F + level.random.nextFloat() * 0.2F;
            break;
         case 7:
            this.rCol = 0.2F;
            this.gCol = 0.5F + level.random.nextFloat() * 0.3F;
            this.bCol = 0.6F + level.random.nextFloat() * 0.3F;
      }
   }

   /** Однотонный вариант: фиксированный RGB (для нитора и прочих «ровных» огоньков). */
   public FXSparkle(
      ClientLevel level, double x, double y, double z, double tx, double ty, double tz,
      float scale, float r, float g, float b, int multiplier
   ) {
      this(level, x, y, z, scale, r, g, b, multiplier);
      double dx = tx - this.x;
      double dy = ty - this.y;
      double dz = tz - this.z;
      if (this.lifetime > 0) {
         this.xd = dx / (double)this.lifetime;
         this.yd = dy / (double)this.lifetime;
         this.zd = dz / (double)this.lifetime;
      }
   }

   public FXSparkle(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, float scale, int color, int multiplier) {
      this(level, x, y, z, scale, color, multiplier);
      double dx = tx - this.x;
      double dy = ty - this.y;
      double dz = tz - this.z;
      if (this.lifetime > 0) {
         this.xd = dx / this.lifetime;
         this.yd = dy / this.lifetime;
         this.zd = dz / this.lifetime;
      }
   }

   public void setGravity(float g) {
      this.gravity = g;
   }

   public void setRGB(float r, float g, float b) {
      this.rCol = r;
      this.gCol = g;
      this.bCol = b;
   }

   public ParticleRenderType getRenderType() {
      return ParticleUtils.TC_PARTICLE;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.hasPhysics = !this.noClip;
      if (this.age == 0 && this.tinkle && this.random.nextInt(10) == 0) {
         float pitch = 0.7F * ((this.random.nextFloat() - this.random.nextFloat()) * 0.6F + 2.0F);
         this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.AMBIENT, 0.02F, pitch, false);
      }

      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.yd = this.yd - 0.04 * this.gravity;
         if (!this.noClip) {
            this.pushOutOfBlocks(this.x, this.y, this.z);
         }

         this.move(this.xd, this.yd, this.zd);
         if (this.slowdown) {
            this.xd *= 0.9080000019073486;
            this.yd *= 0.9080000019073486;
            this.zd *= 0.9080000019073486;
            if (this.onGround) {
               this.xd *= 0.7F;
               this.zd *= 0.7F;
            }
         }

         if (this.leyLineEffect) {
            this.spawnLeyLineChild();
         }
      }
   }

   private void spawnLeyLineChild() {
      double sx = this.xo + (this.random.nextFloat() - this.random.nextFloat()) * 0.1;
      double sy = this.yo + (this.random.nextFloat() - this.random.nextFloat()) * 0.1;
      double sz = this.zo + (this.random.nextFloat() - this.random.nextFloat()) * 0.1;
      int mult = 3 + this.random.nextInt(3);
      FXSparkle child = new FXSparkle(this.level, sx, sy, sz, 1.0F, this.currentColor, mult);
      child.noClip = true;
      Minecraft.getInstance().particleEngine.add(child);
   }

   public void render(VertexConsumer buf, Camera camera, float partialTicks) {
      if (!this.removed) {
         float alpha = 1.0F;
         int frame = (this.particle + this.age / Math.max(1, this.multiplier)) % 4;
         float u0 = frame / 16.0F;
         float u1 = u0 + 0.0624375F;
         float v0 = 0.25F;
         float v1 = v0 + 0.0624375F;
         float size = 0.1F * this.baseQuad;
         if (this.shrink) {
            float s = (this.lifetime - (this.age - 1) + 1.0F) / this.lifetime;
            size *= Mth.clamp(s, 0.0F, 1.0F);
         }

         Vec3 camPos = camera.getPosition();
         float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camPos.x);
         float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camPos.y);
         float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camPos.z);
         Quaternionf q = new Quaternionf(camera.rotation());
         Vector3f c1 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(size);
         Vector3f c2 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(size);
         Vector3f c3 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(size);
         Vector3f c4 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(size);
         int light = 15728880;
         buf.addVertex(px + c1.x(), py + c1.y(), pz + c1.z()).setUv(u1, v1).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, alpha);
         buf.addVertex(px + c2.x(), py + c2.y(), pz + c2.z()).setUv(u1, v0).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, alpha);
         buf.addVertex(px + c3.x(), py + c3.y(), pz + c3.z()).setUv(u0, v0).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, alpha);
         buf.addVertex(px + c4.x(), py + c4.y(), pz + c4.z()).setUv(u0, v1).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, alpha);
      }
   }

   protected boolean pushOutOfBlocks(double x, double y, double z) {
      BlockPos pos = BlockPos.containing(x, y, z);
      if (!this.level.getFluidState(pos).isEmpty()) {
         return false;
      } else {
         BlockState state = this.level.getBlockState(pos);
         if (state.isAir()) {
            return false;
         } else if (state.getCollisionShape(this.level, pos).isEmpty()) {
            return false;
         } else {
            int bx = pos.getX();
            int by = pos.getY();
            int bz = pos.getZ();
            double fx = x - bx;
            double fy = y - by;
            double fz = z - bz;
            boolean freeXN = this.isFree(bx - 1, by, bz);
            boolean freeXP = this.isFree(bx + 1, by, bz);
            boolean freeYN = this.isFree(bx, by - 1, bz);
            boolean freeYP = this.isFree(bx, by + 1, bz);
            boolean freeZN = this.isFree(bx, by, bz - 1);
            boolean freeZP = this.isFree(bx, by, bz + 1);
            int dir = -1;
            double best = 9999.0;
            if (freeXN && fx < best) {
               best = fx;
               dir = 0;
            }

            if (freeXP && 1.0 - fx < best) {
               best = 1.0 - fx;
               dir = 1;
            }

            if (freeYN && fy < best) {
               best = fy;
               dir = 2;
            }

            if (freeYP && 1.0 - fy < best) {
               best = 1.0 - fy;
               dir = 3;
            }

            if (freeZN && fz < best) {
               best = fz;
               dir = 4;
            }

            if (freeZP && 1.0 - fz < best) {
               dir = 5;
            }

            float push = this.random.nextFloat() * 0.05F + 0.025F;
            float jitter = (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;
            switch (dir) {
               case 0:
                  this.xd = -push;
                  this.yd = jitter;
                  this.zd = jitter;
                  break;
               case 1:
                  this.xd = push;
                  this.yd = jitter;
                  this.zd = jitter;
                  break;
               case 2:
                  this.yd = -push;
                  this.xd = jitter;
                  this.zd = jitter;
                  break;
               case 3:
                  this.yd = push;
                  this.xd = jitter;
                  this.zd = jitter;
                  break;
               case 4:
                  this.zd = -push;
                  this.xd = jitter;
                  this.yd = jitter;
                  break;
               case 5:
                  this.zd = push;
                  this.xd = jitter;
                  this.yd = jitter;
                  break;
               default:
                  return false;
            }

            return true;
         }
      }
   }

   private boolean isFree(int x, int y, int z) {
      BlockPos p = new BlockPos(x, y, z);
      BlockState st = this.level.getBlockState(p);
      if (st.isAir()) {
         return true;
      } else {
         return !this.level.getFluidState(p).isEmpty() ? true : st.getCollisionShape(this.level, p).isEmpty();
      }
   }
}
