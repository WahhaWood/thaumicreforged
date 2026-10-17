package thaumcraft.entities.smart.eldrich_guardian;

import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.event.client.ClientTickEventsFML;
import thaumcraft.init.registry.ModDimensions;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.particles.fx.other.FXSonic;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EntityEldritchGuardian extends Monster implements RangedAttackMob, IEldritchMob {
   public float armLiftL;
   public float armLiftR;
   private boolean lastBlast;
   /**
    * «Дом» существа — в 1.7.10 это поля {@code homeX/homeY/homeZ/homeRange}
    * класса EntityCreature, которые задавались вызовом
    * {@code func_110171_b(x, y, z, 16)} из генератора комнаты ключа
    * ({@code GenKeyRoom}). В 1.21 у Mob такого API нет — домашняя память
    * переехала в мозг (MemoryModuleType.HOME), поэтому хранилище и сохранение
    * в NBT повторяем здесь тем же способом, что и в EntityThaumcraftBoss.
    */
   private BlockPos homePos;
   private int homeRange = 16;
   private static final byte EVT_ARM_L = 15;
   private static final byte EVT_ARM_R = 16;
   private static final byte EVT_ARM_BOTH = 17;
   private static final byte EVT_SONIC = 19;
   private static long lastHeartbeatTime = 0L;
   public static final ResourceLocation OUTER_DIM = ResourceLocation.fromNamespaceAndPath("thaumcraft", "outer");

   public EntityEldritchGuardian(EntityType<? extends EntityEldritchGuardian> type, Level level) {
      super(type, level);
      this.xpReward = 20;
      this.armLiftL = 0.0F;
      this.armLiftR = 0.0F;
      this.lastBlast = false;
   }

   public EntityEldritchGuardian(Level level) {
      super((EntityType)ModEntities.ELDRITCH_GUARDIAN.get(), level);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
      this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.RangedAttackGoal(this, 1.0, 60, 32.0F));
      this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.0, false));
      this.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0F));
      this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.player.Player.class, true));
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         // Ослаблен под сервер: было 50 HP / 7 урона — стражи у алтарей
         // выкашивали игроков пачками шаров с игнором брони.
         .add(Attributes.MAX_HEALTH, 40.0)
         .add(Attributes.FOLLOW_RANGE, 40.0)
         .add(Attributes.MOVEMENT_SPEED, 0.28)
         .add(Attributes.ATTACK_DAMAGE, 6.0)
         .add(Attributes.ARMOR, 4.0);
   }


   private static ItemStack createWispEssence(Aspect aspect) {
      ItemStack essence = new ItemStack((ItemLike)ModItems.WISP_ESSENCE.get());
      ((IEssenceContainerItem)essence.getItem()).setAspects(essence, new AspectList().add(aspect, 2));
      return essence;
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         if (this.armLiftL > 0.0F) {
            this.armLiftL = Math.max(0.0F, this.armLiftL - 0.05F);
         }

         if (this.armLiftR > 0.0F) {
            this.armLiftR = Math.max(0.0F, this.armLiftR - 0.05F);
         }

         float unused = (float)(this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         float _x = (float)(this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         Thaumcraft.setup.wispFXEG(this.level(), unused, (float)(this.getY() + 0.22 * this.getBbHeight()), _x, this);
         this.clientWarpPulse();
      }
   }

   private boolean isInOuter() {
      return this.level().dimension().equals(ModDimensions.OUTER_LANDS);
   }

   @OnlyIn(Dist.CLIENT)
   private void clientWarpPulse() {
      if (!this.isInOuter()) {
         if (this.tickCount % 20 == 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
               if (System.currentTimeMillis() - lastHeartbeatTime >= 4000L) {
                  Difficulty diff = mc.level.getDifficulty();
                  if (diff != Difficulty.EASY) {
                     double d6 = diff == Difficulty.HARD ? 576.0 : 256.0;
                     if (!(mc.player.distanceToSqr(this) >= d6)) {
                        lastHeartbeatTime = System.currentTimeMillis();
                        ClientTickEventsFML.warpVignette = 100;
                        mc.player
                           .level()
                           .playLocalSound(
                              mc.player.getX(),
                              mc.player.getY(),
                              mc.player.getZ(),
                              (SoundEvent)ModSounds.HEARTBEAT.get(),
                              SoundSource.AMBIENT,
                              1.0F,
                              1.0F,
                              false
                           );
                     }
                  }
               }
            }
         }
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      return super.hurt(source, amount);
   }

   public void handleEntityEvent(byte id) {
      if (id == 15 && this.level().isClientSide) {
         float unused = this.armLiftL = 0.5F;
      } else if (id == 16 && this.level().isClientSide) {
         float unused = this.armLiftR = 0.5F;
      } else if (id == 17 && this.level().isClientSide) {
         this.armLiftL = 0.9F;
         this.armLiftR = 0.9F;
      } else if (id == 19 && this.level().isClientSide) {
         this.clientSpawnSonic();
      } else {
         super.handleEntityEvent(id);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void clientSpawnSonic() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         mc.particleEngine.add(new FXSonic(mc.level, this.getX(), this.getY(), this.getZ(), this, 10));
      }
   }

   /**
    * Страж Древних роняет рунную скрижаль — порт {@code func_70600_l} из 1.7.10
    * ({@code func_145779_a(ConfigItems.itemEldritchObject, 1)}).
    *
    * <p>Без этого скрижаль было негде взять: древний замок в боссовом зале
    * лабиринта открывается именно ей, а в порте предмет не выдавался ничем —
    * боссовый зал был недостижим.
    */
   @Override
   protected void dropCustomDeathLoot(
      @NotNull net.minecraft.server.level.ServerLevel level,
      @NotNull net.minecraft.world.damagesource.DamageSource source,
      boolean hitByPlayer
   ) {
      // Рунную скрижаль страж НЕ роняет (отключено сознательно): единственный
      // источник — постамент комнаты ключа. Был портом func_70600_l из 1.7.10,
      // но по решению держать скрижаль только в лабиринте дроп убран.
      super.dropCustomDeathLoot(level, source, hitByPlayer);
   }

   public boolean removeWhenFarAway(double dist) {
      return !this.hasRestriction();
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
      return 2.1F;
   }

   public boolean isAlliedTo(Entity other) {
      return other instanceof IEldritchMob || super.isAlliedTo(other);
   }

   @Override
   public void performRangedAttack(net.minecraft.world.entity.LivingEntity target, float distanceFactor) {
      double dx = target.getX() - this.getX();
      double dy = target.getY(0.5) - (this.getY() + this.getBbHeight() * 0.7);
      double dz = target.getZ() - this.getZ();
      thaumcraft.entities.brainless.eldrich_orb.EntityEldritchOrb orb = new thaumcraft.entities.brainless.eldrich_orb.EntityEldritchOrb(this.level(), this);
      orb.setPos(this.getX(), this.getY() + this.getBbHeight() * 0.7, this.getZ());
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist > 0.001) {
         orb.shoot(dx, dy, dz, 1.1F, 1.5F);
      }
      this.level().addFreshEntity(orb);
      this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)ModSounds.EGATTACK.get(), SoundSource.HOSTILE, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
      this.level().broadcastEntityEvent(this, (byte)17);
   }


   /** Порт {@code EntityCreature.func_110171_b}: позиция «дома» и радиус. */
   public void setHomePosition(BlockPos pos, int range) {
      this.homePos = pos.immutable();
      this.homeRange = range;
   }

   public BlockPos getHomePos() {
      return this.homePos;
   }

   public int getHomeRange() {
      return this.homeRange;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      if (this.homePos != null) {
         tag.putInt("HomePosX", this.homePos.getX());
         tag.putInt("HomePosY", this.homePos.getY());
         tag.putInt("HomePosZ", this.homePos.getZ());
      }
      tag.putInt("HomeRange", this.homeRange);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("HomePosX")) {
         this.homePos = new BlockPos(tag.getInt("HomePosX"), tag.getInt("HomePosY"), tag.getInt("HomePosZ"));
      }
      this.homeRange = tag.getInt("HomeRange");
   }

}
