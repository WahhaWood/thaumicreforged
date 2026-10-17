package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.particles.fx.ParticleUtils;

public class FXWisp extends Particle {
   public Entity target;
   public boolean shrink;
   public boolean tinkle;
   public int blendmode = 1;
   public boolean noClip = false;
   private float moteParticleScale;
   private float quadSize;
   private int moteHalfLife;
   private static final float U0 = 0.0F;
   private static final float U1 = 0.125F;
   private static final float V0 = 0.875F;
   private static final float V1 = 1.0F;

   public FXWisp(ClientLevel level, double x, double y, double z, float r, float g, float b) {
      this(level, x, y, z, 1.0F, r, g, b);
   }

   public FXWisp(ClientLevel level, double x, double y, double z, float scale, float r, float g, float b) {
      super(level, x, y, z);
      this.target = null;
      this.shrink = false;
      this.tinkle = false;
      this.blendmode = 1;
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
      // В 1.7.10 виспы рисовались мелко (particleScale ~0.1-0.5), раньше размер
      // терялся и все виспы были квадратом в целый блок — восстанавливаем пропорцию.
      this.moteParticleScale = this.quadSize * Math.max(0.2F, scale) * 0.5F;
      this.lifetime = (int)(36.0 / (Math.random() * 0.3 + 0.7));
      this.moteHalfLife = Math.max(1, this.lifetime / 2);
      this.noClip = false;
      this.hasPhysics = true;
      this.setSize(0.1F, 0.1F);
      Entity cam = Minecraft.getInstance().getCameraEntity();
      int range = 50;
      if (Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FAST) {
         range = 25;
      }

      if (cam != null && cam.distanceToSqr(x, y, z) > range * range) {
         this.lifetime = 0;
      }

      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
   }

   public FXWisp(ClientLevel level, double x, double y, double z, float scale, int type) {
      this(level, x, y, z, scale, 0.0F, 0.0F, 0.0F);
      switch (type) {
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
            this.rCol = 0.7F + level.random.nextFloat() * 0.3F;
            this.gCol = 0.5F + level.random.nextFloat() * 0.2F;
            this.bCol = 0.3F + level.random.nextFloat() * 0.1F;
      }
   }

   public FXWisp(ClientLevel level, double x, double y, double z, double x2, double y2, double z2, float scale, int type) {
      this(level, x, y, z, scale, type);
      if (this.lifetime > 0) {
         this.xd = (x2 - this.x) / this.lifetime;
         this.yd = (y2 - this.y) / this.lifetime;
         this.zd = (z2 - this.z) / this.lifetime;
      }
   }

   public FXWisp(ClientLevel level, double x, double y, double z, Entity target, int type) {
      this(level, x, y, z, 0.4F, type);
      this.target = target;
   }

   public FXWisp(ClientLevel level, double x, double y, double z, double x2, double y2, double z2, float scale, float r, float g, float b) {
      this(level, x, y, z, scale, r, g, b);
      if (this.lifetime > 0) {
         this.xd = (x2 - this.x) / this.lifetime;
         this.yd = (y2 - this.y) / this.lifetime;
         this.zd = (z2 - this.z) / this.lifetime;
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
      if (this.age == 0 && this.tinkle && this.random.nextInt(3) == 0) {
         float pitch = 0.5F * ((this.random.nextFloat() - this.random.nextFloat()) * 0.6F + 2.0F);
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
         if (this.target != null && !this.target.isRemoved()) {
            this.xd *= 0.985;
            this.yd *= 0.985;
            this.zd *= 0.985;
            double tx = this.target.getX();
            double ty = this.target.getY() + this.target.getBbHeight() / 2.0;
            double tz = this.target.getZ();
            double vx = tx - this.x;
            double vy = ty - this.y;
            double vz = tz - this.z;
            double dist = Math.sqrt(vx * vx + vy * vy + vz * vz);
            if (dist > 1.0E-6) {
               double pull = 0.2;
               vx /= dist;
               vy /= dist;
               vz /= dist;
               this.xd += vx * pull;
               this.yd += vy * pull;
               this.zd += vz * pull;
            }

            this.xd = Mth.clamp(this.xd, -0.2, 0.2);
            this.yd = Mth.clamp(this.yd, -0.2, 0.2);
            this.zd = Mth.clamp(this.zd, -0.2, 0.2);
         } else {
            this.xd *= 0.98F;
            this.yd *= 0.98F;
            this.zd *= 0.98F;
            if (this.onGround) {
               this.xd *= 0.7F;
               this.zd *= 0.7F;
            }
         }
      }
   }

   public void render(VertexConsumer buf, Camera camera, float partialTicks) {
      if (!this.removed) {
         float t = this.age + partialTicks;
         float f;
         if (this.shrink) {
            f = (this.lifetime - t) / this.lifetime;
         } else {
            f = t / this.moteHalfLife;
            if (f > 1.0F) {
               f = 2.0F - f;
            }
         }

         if (f < 0.0F) {
            f = 0.0F;
         }

         float scale = this.moteParticleScale * f;
         float half = 0.5F * scale;
         Vec3 camPos = camera.getPosition();
         float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camPos.x);
         float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camPos.y);
         float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camPos.z);
         Quaternionf q = new Quaternionf(camera.rotation());
         Vector3f c1 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(half);
         Vector3f c2 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(half);
         Vector3f c3 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(half);
         Vector3f c4 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(half);
         int light = 15728880;
         float a = 0.5F;
         buf.addVertex(px + c1.x(), py + c1.y(), pz + c1.z())
            .setUv(0.125F, 1.0F)
            .setUv2(light & 65535, light >> 16)
            .setColor(this.rCol, this.gCol, this.bCol, a);
         buf.addVertex(px + c2.x(), py + c2.y(), pz + c2.z())
            .setUv(0.125F, 0.875F)
            .setUv2(light & 65535, light >> 16)
            .setColor(this.rCol, this.gCol, this.bCol, a);
         buf.addVertex(px + c3.x(), py + c3.y(), pz + c3.z())
            .setUv(0.0F, 0.875F)
            .setUv2(light & 65535, light >> 16)
            .setColor(this.rCol, this.gCol, this.bCol, a);
         buf.addVertex(px + c4.x(), py + c4.y(), pz + c4.z()).setUv(0.0F, 1.0F).setUv2(light & 65535, light >> 16).setColor(this.rCol, this.gCol, this.bCol, a);
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
