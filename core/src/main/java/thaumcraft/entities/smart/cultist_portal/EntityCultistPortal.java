package thaumcraft.entities.smart.cultist_portal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.block.banner.BannerBlock;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXBlockArc;

public class EntityCultistPortal extends Monster {
   private static final int STAGE_BOSS = 12;
   private static final int TICKS_FOR_OPENING = 160;
   private static final int INITIAL_DELAY = 200;
   private static final double MAX_HP = 500.0;
   int stage;
   int stagecounter;
   public int pulse;
   private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(), BossBarColor.PURPLE, BossBarOverlay.PROGRESS);
   private static final Direction[] RITUAL_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

   public EntityCultistPortal(EntityType<? extends EntityCultistPortal> type, Level level) {
      super(type, level);
      this.stage = 0;
      this.stagecounter = 200;
      this.pulse = 0;
      this.xpReward = 30;
      this.setNoAi(true);
      this.setNoGravity(true);
      this.bossEvent.setDarkenScreen(false);
      this.bossEvent.setPlayBossMusic(false);
      this.bossEvent.setCreateWorldFog(false);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 500.0)
         .add(Attributes.ATTACK_DAMAGE, 0.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
         .add(Attributes.ARMOR, 5.0)
         .add(Attributes.MOVEMENT_SPEED, 0.0);
   }

   public boolean fireImmune() {
      return true;
   }

   public boolean isPickable() {
      return true;
   }

   public boolean isPushable() {
      return false;
   }

   public void push(Entity entity) {
   }

   public void travel(Vec3 travelVector) {
      this.setDeltaMovement(Vec3.ZERO);
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 4096.0;
   }

   public void tick() {
      super.tick();
      this.setDeltaMovement(Vec3.ZERO);
      this.hasImpulse = false;
      if (this.level().isClientSide && this.pulse > 0) {
         this.pulse--;
      }
   }

   private void handleSetupPhase() {
      if (this.stagecounter == 160 && this.stage == 0) {
         this.level().broadcastEntityEvent(this, (byte)16);

         for (int i = 0; i < 4; i++) {
            int index = 2 + i;
            Direction dir = RITUAL_DIRECTIONS[i];
            BlockPos bp = new BlockPos((int)this.getX() - dir.getStepX() * 6, (int)this.getY(), (int)this.getZ() + dir.getStepZ() * 6);
            Block woodenDevice = (Block)ModBlocks.CRIMSON_CULT_BANNER.get();
            BlockState st = woodenDevice.defaultBlockState();
            Direction facingDir = facingFromA(index);
            int rotationValue = RotationSegment.convertToSegment(facingDir.toYRot());
            st = (BlockState)st.setValue(BannerBlock.ROTATION, rotationValue);
            this.level().setBlock(bp, st, 3);
            this.sendBlockArc(bp);
            this.playTcSound("thaumcraft", "wandfail", 1.0F, 1.0F);
         }
      }

      if (this.stagecounter > 20 && this.stagecounter < 150 && this.stage == 0 && this.stagecounter % 13 == 0) {
         int ax = (int)this.getX() + this.random.nextInt(5) - this.random.nextInt(5);
         int bz = (int)this.getZ() + this.random.nextInt(5) - this.random.nextInt(5);
         if (ax != (int)this.getX() && bz != (int)this.getZ()) {
            BlockPos bp = new BlockPos(ax, (int)this.getY(), bz);
            if (this.level().isEmptyBlock(bp)) {
               this.level().broadcastEntityEvent(this, (byte)16);
               float rr = this.random.nextFloat();
               Block block = rr < 0.05F
                  ? (Block)ModBlocks.LOOT_CRATE_RARE.get()
                  : (rr < 0.2F ? (Block)ModBlocks.LOOT_CRATE_UNCOMMON.get() : (Block)ModBlocks.LOOT_CRATE.get());
               this.level().setBlock(bp, block.defaultBlockState(), 2);
               this.sendBlockArc(bp);
               this.playTcSound("thaumcraft", "wandfail", 1.0F, 1.0F);
            }
         }
      }
   }

   private void handleSpawningPhase() {
      if (this.level().getNearestPlayer(this, 48.0) == null) {
         this.stagecounter = 30 + this.random.nextInt(30);
      } else {
         this.level().broadcastEntityEvent(this, (byte)16);
         if (this.stage < 5) {
            this.stagecounter = 15 + this.random.nextInt(10 - this.stage) - this.stage;
            this.spawnMinions();
         } else if (this.stage == 12) {
            this.stagecounter = 50 + this.getTiming() * 2 + this.random.nextInt(50);
            this.spawnBoss();
         } else {
            int t = this.getTiming();
            this.stagecounter = t + this.random.nextInt(5 + t / 3);
            this.spawnMinions();
         }

         this.stage++;
      }
   }

   int getTiming() {
      AABB box = this.getBoundingBox().inflate(32.0);
      List<Entity> list = this.level().getEntities(this, box, e -> {
         ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
         return key != null && "thaumcraft".equals(key.getNamespace()) && key.getPath().startsWith("cultist");
      });
      return list.size() * 20;
   }

   void spawnMinions() {
      // Жёсткий кап при фарме: getTiming лишь замедляет спавн, но без капа AFK
      // толпа растёт бесконечно (у каждого культиста AI + рендер). Стадии при
      // этом идут дальше — портал доходит до суицида как в оригинале.
      AABB box = this.getBoundingBox().inflate(32.0);
      List<Entity> cultists = this.level().getEntities(this, box, e -> {
         ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
         return key != null && "thaumcraft".equals(key.getNamespace()) && key.getPath().startsWith("cultist");
      });
      if (cultists.size() >= 12) {
         return;
      }

      EntityType<?> mob = this.random.nextFloat() > 0.33F ? (EntityType)ModEntities.CULTIST_KNIGHT.get() : (EntityType)ModEntities.CULTIST_CLERIC.get();
      this.spawnEntity(mob, false);
      if (this.stage > 12) {
         this.hurt(this.damageSources().fellOutOfWorld(), 5 + this.random.nextInt(5));
      }
   }

   void spawnBoss() {
      this.spawnEntity((EntityType<?>)ModEntities.CULTIST_LEADER.get(), true);
   }

   private void spawnEntity(EntityType<?> spawnTo, boolean isBoss) {
      if (spawnTo != null) {
         Entity entity = spawnTo.create(this.level());
         if (entity != null) {
            double x = this.getX() + (this.random.nextDouble() - this.random.nextDouble());
            double z = this.getZ() + (this.random.nextDouble() - this.random.nextDouble());
            entity.setPos(x, this.getY() + 0.25, z);
            if (entity instanceof Mob mob && this.level() instanceof ServerLevel sl) {
               mob.finalizeSpawn(sl, sl.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
               if (mob instanceof PathfinderMob pm) {
                  pm.restrictTo(this.blockPosition(), 32);
               }
            }

            this.level().addFreshEntity(entity);
            this.burstAt(entity.getX(), entity.getY() + 0.5, entity.getZ());
            this.playTcSound("thaumcraft", "wandfail", 1.0F, 1.0F);
         }
      }
   }

   public void playerTouch(Player p) {
      super.playerTouch(p);
   }

   public boolean addEffect(MobEffectInstance effect, Entity source) {
      return false;
   }

   public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
      return false;
   }

   public void die(DamageSource source) {
      super.die(source);
   }

   public void handleEntityEvent(byte msg) {
      if (msg == 16 && !this.level().isClientSide) {
         this.pulse = 10;
      } else {
         super.handleEntityEvent(msg);
      }
   }

   private void sendBlockArc(BlockPos pos) {
      PacketHandler.sendToAllAround(new PacketFXBlockArc(pos, this.getId()), this.level(), this.getX(), this.getY(), this.getZ(), 32.0);
   }

   private void burstAt(double x, double y, double z) {
      if (this.level() instanceof ServerLevel sl) {
         sl.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
         sl.sendParticles(ParticleTypes.POOF, x, y, z, 8, 0.25, 0.25, 0.25, 0.01);
      }
   }

   private void playTcSound(String modid, String path, float volume, float pitch) {
      SoundEvent ev = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(modid, path));
      if (ev != null) {
         this.playSound(ev, volume, pitch);
      }
   }

   private static Direction facingFromA(int a) {
      return switch (a) {
         case 2 -> Direction.NORTH;
         case 3 -> Direction.SOUTH;
         case 4 -> Direction.EAST;
         case 5 -> Direction.WEST;
         default -> Direction.SOUTH;
      };
   }
}
