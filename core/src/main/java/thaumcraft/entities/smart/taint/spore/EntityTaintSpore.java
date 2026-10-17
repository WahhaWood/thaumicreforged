package thaumcraft.entities.smart.taint.spore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.init.registry.ModBlocks;

public class EntityTaintSpore extends Monster implements ITaintedMob, IEntityWithComplexSpawn {
   public final List<Object> swarm = new ArrayList<>();
   protected int growth = 0;
   public float displaySize = 0.0F;
   private static final EntityDataAccessor<Byte> SPORE_SIZE = SynchedEntityData.defineId(EntityTaintSpore.class, EntityDataSerializers.BYTE);
   private static final int GROWTH_TICKS = 1200;
   private static final int MAX_SIZE = 10;
   private static final double RENDER_DISTANCE_SQR = 4096.0;

   public EntityTaintSpore(EntityType<? extends EntityTaintSpore> type, Level level) {
      super(type, level);
   }

   public EntityTaintSpore(Level level) {
      this((net.minecraft.world.entity.EntityType<? extends EntityTaintSpore>)thaumcraft.init.registry.ModEntities.TAINT_SPORE.get(), level);
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(SPORE_SIZE, (byte)2);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
      return super.getAddEntityPacket(entity);
   }

   public void setSporeSize(int size) {
      this.entityData.set(SPORE_SIZE, (byte)size);
      this.xpReward = size;
      this.refreshDimensions();
   }

   public int getSporeSize() {
      return (Byte)this.entityData.get(SPORE_SIZE);
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 4096.0;
   }

   public boolean isPickable() {
      return true;
   }

   public boolean isPushable() {
      return false;
   }

   public void move(MoverType type, Vec3 vec) {
      double y = vec.y;
      vec = new Vec3(0.0, Math.min(y, 0.0), 0.0);
      BlockPos below = this.blockPosition().below();
      BlockState belowState = this.level().getBlockState(below);
      if (!belowState.is((Block)ModBlocks.MATURE_SPORE_STALK.get())) {
         super.move(type, vec);
      }
   }

   public void tick() {
      super.tick();
      this.sporeOnUpdate();
   }

   protected void sporeOnUpdate() {
      int currentSize = this.getSporeSize();
      if (this.level().isClientSide) {
         if (this.displaySize < currentSize) {
            this.displaySize += 0.02F;
         }

         Iterator unused = this.swarm.iterator();

         while (unused.hasNext()) {
            Object _x = unused.next();
            if (_x == null) {
               unused.remove();
            } else if (_x instanceof Entity _xx && _xx.isRemoved()) {
               unused.remove();
            }
         }

         if (this.swarm.size() < currentSize / 3) {
            this.swarm.add(Thaumcraft.setup.swarmParticleFX(this.level(), this, 0.1F, 10.0F, 0.0F));
         }
      }

      BlockPos below = this.blockPosition().below();
      if (!this.level().getBlockState(below).is((Block)ModBlocks.MATURE_SPORE_STALK.get())) {
         this.spiderBurst();
      } else if (this.deathTime > 0) {
         this.spiderBurst();
      }
   }

   public void playerTouch(Player player) {
      this.spiderBurst();
   }

   protected void spiderBurst() {
      if (this.level().isClientSide) {
         this.sploosh(50);
      } else if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
         // Серверная половина лопанья: из споры вылупляются пауки (с капой,
         // чтобы касание роя не устраивало экспоненту).
         int nearby = serverLevel.getEntitiesOfClass(
            thaumcraft.entities.smart.taint.spider.EntityTaintSpider.class,
            new net.minecraft.world.phys.AABB(this.blockPosition()).inflate(8.0)
         ).size();
         for (int i = 0; i < 2 && nearby + i < 6; i++) {
            thaumcraft.entities.smart.taint.spider.EntityTaintSpider spider =
               new thaumcraft.entities.smart.taint.spider.EntityTaintSpider(
                  (net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.monster.Spider>)thaumcraft.init.registry.ModEntities.TAINT_SPIDER.get(),
                  this.level()
               );
            spider.setPos(this.getX(), this.getY(), this.getZ());
            spider.finalizeSpawn(
               serverLevel,
               serverLevel.getCurrentDifficultyAt(spider.blockPosition()),
               net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED,
               null
            );
            serverLevel.addFreshEntity(spider);
         }
      }
   }

   protected void sploosh(int amt) {
      for (int unused = 0; unused < amt; unused++) {
         Thaumcraft.setup.splooshFX(this);
      }
   }

   public void writeSpawnData(RegistryFriendlyByteBuf data) {
      data.writeFloat(this.getSporeSize());
   }

   public void readSpawnData(RegistryFriendlyByteBuf data) {
      if (data.isReadable()) {
         this.displaySize = data.readFloat();
      }
   }

   protected float getSoundVolume() {
      return 0.1F;
   }

   public int getAmbientSoundInterval() {
      return 200;
   }

   protected SoundEvent getAmbientSound() {
      return getThaumcraftSound("swarm");
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return getThaumcraftSound("gore");
   }

   protected SoundEvent getDeathSound() {
      return getThaumcraftSound("gore");
   }

   private static SoundEvent getThaumcraftSound(String path) {
      return (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", path));
   }
}
