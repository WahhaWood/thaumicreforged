package thaumcraft.entities.smart.cultist;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModItems;

public class EntityCultistCleric extends EntityCultist implements RangedAttackMob {
   private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(EntityCultistCleric.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<BlockPos> HOME_POS = SynchedEntityData.defineId(EntityCultistCleric.class, EntityDataSerializers.BLOCK_POS);

   public EntityCultistCleric(EntityType<? extends EntityCultistCleric> type, Level level) {
      super(type, level);
   }

   public EntityCultistCleric(Level level) {
      super((EntityType<? extends EntityCultist>)ModEntities.CULTIST_CLERIC.get(), level);
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(FLAGS, (byte)0);
      builder.define(HOME_POS, BlockPos.ZERO);
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return EntityCultist.createAttributes().add(Attributes.MAX_HEALTH, 30.0);
   }

   /**
    * В 1.21 {@code Mob.finalizeSpawn} больше НЕ вызывает
    * {@code populateDefaultEquipmentSlots} сам — без этого оверрайда клирики
    * спавнились бы голыми несмотря на слоты выше.
    */
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      net.minecraft.world.level.ServerLevelAccessor level,
      net.minecraft.world.DifficultyInstance difficulty,
      net.minecraft.world.entity.MobSpawnType reason,
      @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData data
   ) {
      this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
      return super.finalizeSpawn(level, difficulty, reason, data);
   }

   /**
    * Клирики носят робу культа (шлем/нагрудник/поножи + общие сапоги).
    * Шанс дропа выставлен явно — иначе робу не получить вне зала боссов.
    */
   @Override
   protected void populateDefaultEquipmentSlots(
      net.minecraft.util.RandomSource randomSource, net.minecraft.world.DifficultyInstance difficultyInstance
   ) {
      this.setItemSlot(EquipmentSlot.HEAD, new ItemStack((ItemLike)ModItems.HELMET_CULTIST_ROBE.get()));
      this.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike)ModItems.CHEST_CULTIST_ROBE.get()));
      this.setItemSlot(EquipmentSlot.LEGS, new ItemStack((ItemLike)ModItems.LEGGINGS_CULTIST_ROBE.get()));
      this.setItemSlot(EquipmentSlot.FEET, new ItemStack((ItemLike)ModItems.BOOTS_CULTIST.get()));
      this.setDropChance(EquipmentSlot.HEAD, 0.2F);
      this.setDropChance(EquipmentSlot.CHEST, 0.2F);
      this.setDropChance(EquipmentSlot.LEGS, 0.2F);
      this.setDropChance(EquipmentSlot.FEET, 0.2F);
      super.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
   }

   public void performRangedAttack(LivingEntity target, float distanceFactor) {
      this.swing(InteractionHand.MAIN_HAND);
   }

   @Override
   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return this.getIsRitualist() ? false : super.removeWhenFarAway(distanceToClosestPlayer);
   }

   public boolean getIsRitualist() {
      return ((Byte)this.entityData.get(FLAGS) & 1) != 0;
   }

   public void setIsRitualist(boolean v) {
      byte b = (Byte)this.entityData.get(FLAGS);
      if (v) {
         this.entityData.set(FLAGS, (byte)(b | 1));
      } else {
         this.entityData.set(FLAGS, (byte)(b & 254));
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         this.setIsRitualist(false);
         return super.hurt(source, amount);
      }
   }

   public void setHomeArea(BlockPos pos, int radius) {
      this.entityData.set(HOME_POS, pos);
      this.restrictTo(pos, radius);
   }

   @Override
   public BlockPos getHomePosition() {
      return (BlockPos)this.entityData.get(HOME_POS);
   }

   public boolean hasValidRitualHome() {
      BlockPos home = this.getHomePosition();
      return this.level().hasChunkAt(home) && this.level().getBlockState(home).is((Block)ModBlocks.ELDRITCH_ALTAR.get());
   }

   public boolean updateRitualLook(float maxPitchDelta, float maxYawDelta) {
      if (this.getIsRitualist() && this.hasValidRitualHome()) {
         BlockPos hp = this.getHomePosition();
         double d0 = hp.getX() + 0.5 - this.getX();
         double d1 = hp.getY() + 1.5 - this.getY() + this.getEyeHeight();
         double d2 = hp.getZ() + 0.5 - this.getZ();
         double d3 = Mth.sqrt((float)(d0 * d0 + d2 * d2));
         float yaw = (float)(Mth.atan2(d2, d0) * 180.0 / Math.PI) - 90.0F;
         float pitch = (float)(-(Mth.atan2(d1, d3) * 180.0 / Math.PI));
         this.setXRot(this.updateRotation(this.getXRot(), pitch, maxPitchDelta));
         float bodyYaw = this.updateRotation(this.getYRot(), yaw, maxYawDelta);
         this.setYRot(bodyYaw);
         this.setYHeadRot(this.updateRotation(this.getYHeadRot(), yaw, maxYawDelta));
         this.yBodyRot = bodyYaw;
         this.yBodyRotO = bodyYaw;
         return true;
      } else {
         return false;
      }
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         this.updateRitualLook(10.0F, 40.0F);
      }
   }

   private float updateRotation(float a, float b, float max) {
      float f = Mth.wrapDegrees(b - a);
      if (f > max) {
         f = max;
      }

      if (f < -max) {
         f = -max;
      }

      return a + f;
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "chant"));
   }

   public int getAmbientSoundInterval() {
      return 500;
   }
}
