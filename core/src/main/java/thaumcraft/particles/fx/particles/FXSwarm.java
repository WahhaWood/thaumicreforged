package thaumcraft.particles.fx.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.init.registry.ModBlockTags;

public class FXSwarm extends Particle {
   public static final ResourceLocation PARTICLE_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles.png");
   private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
      public BufferBuilder begin(@NotNull Tesselator tesselator, @NotNull TextureManager textureManager) {
         RenderSystem.setShader(GameRenderer::getParticleShader);
         RenderSystem.setShaderTexture(0, FXSwarm.PARTICLE_TEXTURE);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
         RenderSystem.depthMask(false);
         return tesselator.begin(Mode.QUADS, DefaultVertexFormat.PARTICLE);
      }

      @NotNull
      @Override
      public String toString() {
         return "thaumcraft_swarm";
      }
   };
   private final Entity target;
   private float turnSpeed = 10.0F;
   private float speed = 0.2F;
   private float grav = 0.1F;
   private int deathtimer = 0;
   private float rotYaw = 0.0F;
   private float rotPitch = 0.0F;
   private float quadSize;
   private static final ArrayList<Long> buzzcount = new ArrayList<>();
   public int particle = 40;

   public FXSwarm(ClientLevel level, double x, double y, double z, Entity target, float r, float g, float b) {
      super(level, x, y, z);
      this.target = target;
      this.rCol = r;
      this.gCol = g;
      this.bCol = b;
      this.quadSize = this.random.nextFloat() * 0.5F + 1.0F;
      float s = 0.2F;
      this.xd = (this.random.nextFloat() - this.random.nextFloat()) * s;
      this.yd = (this.random.nextFloat() - this.random.nextFloat()) * s;
      this.zd = (this.random.nextFloat() - this.random.nextFloat()) * s;
      this.hasPhysics = true;
      this.setSize(0.02F, 0.02F);
      Entity cam = Minecraft.getInstance().getCameraEntity();
      int max = ((GraphicsStatus)Minecraft.getInstance().options.graphicsMode().get()).getId() > 0 ? 64 : 32;
      if (cam != null && cam.distanceToSqr(x, y, z) > max * max) {
         this.remove();
      }

      this.lifetime = Integer.MAX_VALUE;
   }

   public FXSwarm(
      ClientLevel level, double x, double y, double z, Entity target, float r, float g, float b, float speed, float turnSpeed, float particleGravity
   ) {
      this(level, x, y, z, target, r, g, b);
      this.speed = speed;
      this.turnSpeed = turnSpeed;
      this.grav = particleGravity;
   }

   @NotNull
   public ParticleRenderType getRenderType() {
      return RENDER_TYPE;
   }

   protected int getLightColor(float pt) {
      return 15728880;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.age++;
      boolean dying = this.target == null || !this.target.isAlive();
      if (!dying && this.target instanceof LivingEntity le) {
         dying = le.isDeadOrDying();
      }

      if (dying) {
         this.deathtimer++;
         this.yd = this.yd - this.grav / 2.0F;
         if (this.deathtimer > 50) {
            this.remove();
            return;
         }
      } else {
         this.yd = this.yd + this.grav;
      }

      this.pushOutOfBlocks(this.x, this.y, this.z);
      this.move(this.xd, this.yd, this.zd);
      this.xd *= 0.985;
      this.yd *= 0.985;
      this.zd *= 0.985;
      if (!dying) {
         boolean hurt = false;
         if (this.target instanceof LivingEntity le) {
            hurt = le.hurtTime > 0;
         }

         double d2 = this.target.distanceToSqr(this.x, this.y, this.z);
         if (d2 > this.target.getBbWidth() && !hurt) {
            this.faceEntity(
               this.target,
               this.turnSpeed / 2.0F + this.random.nextInt((int)(this.turnSpeed / 2.0F)),
               this.turnSpeed / 2.0F + this.random.nextInt((int)(this.turnSpeed / 2.0F))
            );
         } else {
            this.faceEntity(
               this.target,
               -(this.turnSpeed / 2.0F + this.random.nextInt((int)(this.turnSpeed / 2.0F))),
               -(this.turnSpeed / 2.0F + this.random.nextInt((int)(this.turnSpeed / 2.0F)))
            );
         }

         double mx = -Mth.sin(this.rotYaw * (float) (Math.PI / 180.0)) * Mth.cos(this.rotPitch * (float) (Math.PI / 180.0));
         double mz = Mth.cos(this.rotYaw * (float) (Math.PI / 180.0)) * Mth.cos(this.rotPitch * (float) (Math.PI / 180.0));
         double my = -Mth.sin(this.rotPitch * (float) (Math.PI / 180.0));
         this.setHeading(mx, my, mz, this.speed, 15.0F);
      }

      if (buzzcount.size() < 3 && this.random.nextInt(50) == 0 && this.level.getNearestPlayer(this.x, this.y, this.z, 8.0, false) != null) {
         SoundEvent se = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "fly"));
         if (se != null) {
            this.level.playLocalSound(this.x, this.y, this.z, se, SoundSource.AMBIENT, 0.03F, 0.5F + this.random.nextFloat() * 0.4F, false);
            buzzcount.add(System.nanoTime() + 1500000L);
         }
      }

      if (buzzcount.size() >= 3 && buzzcount.get(0) < System.nanoTime()) {
         buzzcount.remove(0);
      }
   }

   public void render(@NotNull VertexConsumer consumer, @NotNull Camera camera, float pt) {
      float pulse = Mth.sin(this.age / 3.0F) * 0.25F + 1.0F;
      float u0 = (7 + (this.age & 7)) / 16.0F;
      float u1 = u0 + 0.0624375F;
      float v0 = 0.25F;
      float v1 = v0 + 0.0624375F;
      float a = 0.75F * Mth.clamp((50.0F - this.deathtimer) / 50.0F, 0.0F, 1.0F);
      float rr = this.rCol;
      float gg = this.gCol;
      float bb = this.bCol;
      if (this.target instanceof LivingEntity le && le.hurtTime > 0) {
         gg *= 0.5F;
         bb *= 0.5F;
      }

      float size = 0.1F * this.quadSize * pulse;
      Vec3 camPos = camera.getPosition();
      float px = (float)(Mth.lerp(pt, this.xo, this.x) - camPos.x);
      float py = (float)(Mth.lerp(pt, this.yo, this.y) - camPos.y);
      float pz = (float)(Mth.lerp(pt, this.zo, this.z) - camPos.z);
      Quaternionf q = camera.rotation();
      Vector3f c0 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
      Vector3f c1 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
      Vector3f c2 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
      Vector3f c3 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(size).add(px, py, pz);
      int light = this.getLightColor(pt);
      consumer.addVertex(c0.x, c0.y, c0.z).setUv(u1, v1).setColor(rr, gg, bb, a).setUv2(light & 65535, light >> 16);
      consumer.addVertex(c1.x, c1.y, c1.z).setUv(u1, v0).setColor(rr, gg, bb, a).setUv2(light & 65535, light >> 16);
      consumer.addVertex(c2.x, c2.y, c2.z).setUv(u0, v0).setColor(rr, gg, bb, a).setUv2(light & 65535, light >> 16);
      consumer.addVertex(c3.x, c3.y, c3.z).setUv(u0, v1).setColor(rr, gg, bb, a).setUv2(light & 65535, light >> 16);
   }

   private void faceEntity(Entity entity, float yawSpeed, float pitchSpeed) {
      double dx = entity.getX() - this.x;
      double dz = entity.getZ() - this.z;
      AABB eb = entity.getBoundingBox();
      AABB pb = this.getBoundingBox();
      double dy = (eb.minY + eb.maxY) / 2.0 - (pb.minY + pb.maxY) / 2.0;
      double d = Mth.sqrt((float)(dx * dx + dz * dz));
      float yaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
      float pitch = (float)(-(Mth.atan2(dy, d) * (180.0 / Math.PI)));
      this.rotPitch = this.updateRotation(this.rotPitch, pitch, pitchSpeed);
      this.rotYaw = this.updateRotation(this.rotYaw, yaw, yawSpeed);
   }

   private float updateRotation(float cur, float target, float max) {
      float delta = Mth.wrapDegrees(target - cur);
      if (delta > max) {
         delta = max;
      }

      if (delta < -max) {
         delta = -max;
      }

      return cur + delta;
   }

   private void setHeading(double mx, double my, double mz, float speed, float randomness) {
      double len = Mth.sqrt((float)(mx * mx + my * my + mz * mz));
      if (!(len < 1.0E-7)) {
         mx /= len;
         my /= len;
         mz /= len;
         mx += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075F * randomness;
         my += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075F * randomness;
         mz += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075F * randomness;
         mx *= speed;
         my *= speed;
         mz *= speed;
         this.xd = mx;
         this.yd = my;
         this.zd = mz;
      }
   }

   private boolean pushOutOfBlocks(double x, double y, double z) {
      int ix = Mth.floor(x);
      int iy = Mth.floor(y);
      int iz = Mth.floor(z);
      BlockPos pos = new BlockPos(ix, iy, iz);
      BlockState state = this.level.getBlockState(pos);
      if (state.is(ModBlockTags.TAINT_MATERIAL)) {
         return false;
      } else if (state.isAir()) {
         return false;
      } else if (!state.isCollisionShapeFullBlock(this.level, pos)) {
         return false;
      } else if (this.level.containsAnyLiquid(this.getBoundingBox())) {
         return false;
      } else {
         double fx = x - ix;
         double fy = y - iy;
         double fz = z - iz;
         boolean w = !this.level.getBlockState(pos.west()).isCollisionShapeFullBlock(this.level, pos.west());
         boolean e = !this.level.getBlockState(pos.east()).isCollisionShapeFullBlock(this.level, pos.east());
         boolean d = !this.level.getBlockState(pos.below()).isCollisionShapeFullBlock(this.level, pos.below());
         boolean u = !this.level.getBlockState(pos.above()).isCollisionShapeFullBlock(this.level, pos.above());
         boolean n = !this.level.getBlockState(pos.north()).isCollisionShapeFullBlock(this.level, pos.north());
         boolean s = !this.level.getBlockState(pos.south()).isCollisionShapeFullBlock(this.level, pos.south());
         int side = -1;
         double best = 9999.0;
         if (w && fx < best) {
            best = fx;
            side = 0;
         }

         if (e && 1.0 - fx < best) {
            best = 1.0 - fx;
            side = 1;
         }

         if (d && fy < best) {
            best = fy;
            side = 2;
         }

         if (u && 1.0 - fy < best) {
            best = 1.0 - fy;
            side = 3;
         }

         if (n && fz < best) {
            best = fz;
            side = 4;
         }

         if (s && 1.0 - fz < best) {
            side = 5;
         }

         float push = this.random.nextFloat() * 0.05F + 0.025F;
         float rnd = (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;
         if (side == 0) {
            this.xd = -push;
            this.yd = rnd;
            this.zd = rnd;
            return true;
         } else if (side == 1) {
            this.xd = push;
            this.yd = rnd;
            this.zd = rnd;
            return true;
         } else if (side == 2) {
            this.yd = -push;
            this.xd = rnd;
            this.zd = rnd;
            return true;
         } else if (side == 3) {
            this.yd = push;
            this.xd = rnd;
            this.zd = rnd;
            return true;
         } else if (side == 4) {
            this.zd = -push;
            this.xd = rnd;
            this.yd = rnd;
            return true;
         } else if (side == 5) {
            this.zd = push;
            this.xd = rnd;
            this.yd = rnd;
            return true;
         } else {
            return false;
         }
      }
   }
}
