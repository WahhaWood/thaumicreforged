package thaumcraft.entities.smart.travelling_trunk;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.entities.smart.travelling_trunk.inventory.TrunkItemHandler;
import thaumcraft.event.common.EventHandlerEntity;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.golem_upgrade.GolemUpgrade;
import thaumcraft.item.golem_upgrade.ItemGolemUpgrade;

public class EntityTravelingTrunk extends PathfinderMob implements OwnableEntity, MenuProvider {
   private static final EntityDataAccessor<Byte> DATA_OPEN = SynchedEntityData.defineId(EntityTravelingTrunk.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> DATA_STAY = SynchedEntityData.defineId(EntityTravelingTrunk.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<String> DATA_OWNER_NAME = SynchedEntityData.defineId(EntityTravelingTrunk.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Byte> DATA_UPGRADE = SynchedEntityData.defineId(EntityTravelingTrunk.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> DATA_ROWS = SynchedEntityData.defineId(EntityTravelingTrunk.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Integer> DATA_ANGER = SynchedEntityData.defineId(EntityTravelingTrunk.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(
      EntityTravelingTrunk.class, EntityDataSerializers.OPTIONAL_UUID
   );
   private static final int DEFAULT_SLOT_COUNT = 27;
   private static final int EXPANDED_SLOT_COUNT = 36;
   protected TrunkItemHandler inventory;
   public float lidrot;
   public float lidrotO;
   public float field_768_a;
   public float field_767_b;
   public int slotCount = 27;
   private int jumpDelay;
   private int eatDelay;
   private int attackTime;

   public EntityTravelingTrunk(EntityType<? extends EntityTravelingTrunk> type, Level level) {
      super(type, level);
      this.inventory = new TrunkItemHandler(36);
      this.inventory.setOnChanged(this::onInventoryChanged);
      this.eatDelay = 0;
      this.jumpDelay = this.random.nextInt(20) + 10;
      this.lidrot = 0.0F;
      this.lidrotO = 0.0F;
      this.setPersistenceRequired();
      // 1.7.10 зовёт setInvSize() из конструктора (EntityTravelingTrunk:105),
      // поэтому новый сундук без апгрейдов сразу 3 ряда, а не 4.
      this.setInvSize();
   }

   public static Builder createAttributes() {
      return PathfinderMob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 75.0)
         .add(Attributes.ATTACK_DAMAGE, 4.0)
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_OPEN, (byte)0);
      builder.define(DATA_STAY, (byte)0);
      builder.define(DATA_OWNER_NAME, "");
      builder.define(DATA_UPGRADE, (byte)-1);
      builder.define(DATA_ROWS, (byte)3);
      builder.define(DATA_ANGER, 0);
      builder.define(DATA_OWNER_UUID, Optional.empty());
   }

   @Nullable
   public IItemHandler getItemHandler(@Nullable Direction side) {
      return this.isAlive() ? this.inventory : null;
   }

   private void onInventoryChanged() {
   }

   public TrunkItemHandler getInventory() {
      return this.inventory;
   }

   @Override
   public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      // Без этого инвентарь сундука-горема обнулялся при выгрузке чанка:
      // сериализация в TrunkItemHandler была, но сущность её не вызывала.
      tag.put("TrunkInventory", this.inventory.serializeNBT());
   }

   @Override
   public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("TrunkInventory", 10)) {
         this.inventory.deserializeNBT(tag.getCompound("TrunkInventory"));
      }
   }

   @Override
   protected void dropCustomDeathLoot(
      @NotNull net.minecraft.server.level.ServerLevel level,
      @NotNull net.minecraft.world.damagesource.DamageSource source,
      boolean hitByPlayer
   ) {
      super.dropCustomDeathLoot(level, source, hitByPlayer);
      // Содержимое высыпается при убийстве (dropAllItems существовал, но смерть
      // его не вызывала).
      for (int i = 0; i < this.inventory.getSlots(); i++) {
         ItemStack stack = this.inventory.getStackInSlot(i);
         if (!stack.isEmpty()) {
            this.spawnAtLocation(stack.copy());
         }
      }
   }

   @Nullable
   public GolemUpgrade getUpgradeType() {
      int upgrade = this.getUpgrade();
      if (upgrade >= 0 && upgrade < GolemUpgrade.values().length) {
         GolemUpgrade type = GolemUpgrade.values()[upgrade];
         return ItemGolemUpgrade.getItemByUpgrade(type) != null ? type : null;
      } else {
         return null;
      }
   }

   public boolean hasUpgrade(GolemUpgrade upgrade) {
      return this.getUpgradeType() == upgrade;
   }

   public boolean fireImmune() {
      return true;
   }

   public boolean hurt(@NotNull DamageSource source, float amount) {
      if (source.is(DamageTypes.CACTUS)) {
         return false;
      } else {
         return this.hasUpgrade(GolemUpgrade.WATER) ? false : super.hurt(source, amount);
      }
   }

   public void tick() {
      this.lidrotO = this.lidrot;
      this.field_767_b = this.field_768_a;
      if (this.field_768_a > 0.0F) {
         this.field_768_a *= 0.6F;
      }

      super.tick();
      if (this.attackTime > 0) {
         this.attackTime--;
      }

      if (this.isInWater()) {
         this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y + 0.033, this.getDeltaMovement().z);
      }

      if (this.level().isClientSide) {
         this.updateLidAnimation();
      }
   }

   private void updateLidAnimation() {
      if (!this.onGround() && this.getDeltaMovement().y < 0.0 && !this.isInWater()) {
         this.lidrot += 0.015F;
      }

      if ((this.onGround() || this.isInWater()) && !this.isOpen()) {
         this.lidrot -= 0.1F;
         if (this.lidrot < 0.0F) {
            this.lidrot = 0.0F;
         }
      }

      if (this.isOpen()) {
         this.lidrot += 0.035F;
      }

      float maxLid = this.isOpen() ? 0.5F : 0.2F;
      if (this.lidrot > maxLid) {
         this.lidrot = maxLid;
      }
   }

   protected void customServerAiStep() {
      super.customServerAiStep();
   }

   private void updateLinkedEntities(LivingEntity owner) {
      ArrayList<WeakReference<Entity>> ll = EventHandlerEntity.linkedEntities.get(owner.getScoreboardName());
      if (ll == null) {
         ll = new ArrayList<>();
      }

      boolean add = true;

      for (WeakReference<Entity> wr : ll) {
         Entity e = wr.get();
         if (e != null && e.getId() == this.getId()) {
            add = false;
            break;
         }
      }

      if (add) {
         ll.add(new WeakReference<>(this));
         EventHandlerEntity.linkedEntities.put(owner.getScoreboardName(), ll);
      }
   }

   private void handleTeleportToOwner(LivingEntity owner) {
      if (!this.getStay() && !(this.distanceTo(owner) <= 20.0F)) {
         int i = Mth.floor(owner.getX()) - 2;
         int j = Mth.floor(owner.getZ()) - 2;
         int k = Mth.floor(owner.getBoundingBox().minY);

         for (int l = 0; l <= 4; l++) {
            for (int i1 = 0; i1 <= 4; i1++) {
               if (l < 1 || i1 < 1 || l > 3 || i1 > 3) {
                  BlockPos below = new BlockPos(i + l, k - 1, j + i1);
                  BlockPos pos = new BlockPos(i + l, k, j + i1);
                  BlockPos posUp = pos.above();
                  BlockState belowState = this.level().getBlockState(below);
                  FluidState belowFluid = this.level().getFluidState(below);
                  boolean okBelow = belowState.isSolidRender(this.level(), below) || belowFluid.is(FluidTags.WATER);
                  boolean space1 = this.level().getBlockState(pos).getCollisionShape(this.level(), pos).isEmpty();
                  boolean space2 = this.level().getBlockState(posUp).getCollisionShape(this.level(), posUp).isEmpty();
                  if (okBelow && space1 && space2) {
                     this.level().playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.5F, 1.0F);
                     this.moveTo(i + l + 0.5, k, j + i1 + 0.5, this.getYRot(), this.getXRot());
                     this.setTarget(null);
                     return;
                  }
               }
            }
         }
      }
   }

   private void handleTargeting(LivingEntity owner) {
      LivingEntity target = this.getTarget();
      if (target != null && !target.isAlive()) {
         this.setTarget(null);
         this.setAnger(5);
      }

      if (!this.getStay() && this.hasUpgrade(GolemUpgrade.FIRE) && this.getAnger() == 0 && this.getTarget() == null) {
         LivingEntity ownerTarget = null;
         if (owner instanceof Mob mob) {
            ownerTarget = mob.getTarget();
         }

         if (ownerTarget == null && owner instanceof Player p) {
            ownerTarget = p.getLastHurtMob();
         }

         if (ownerTarget != null && ownerTarget.isAlive() && this.hasLineOfSight(ownerTarget)) {
            this.setAnger(600);
            this.setTarget(ownerTarget);
         }
      }
   }

   private void handleMovement(LivingEntity owner) {
      boolean move = false;
      LivingEntity at = this.getTarget();
      LivingEntity desired = null;
      if (this.getAnger() > 0 && at != null && at.isAlive() && at != owner) {
         desired = at;
         move = true;
         if (this.attackTime <= 0
            && this.distanceTo(at) < 1.5
            && at.getBoundingBox().maxY > this.getBoundingBox().minY
            && at.getBoundingBox().minY < this.getBoundingBox().maxY) {
            this.attackTime = 10 + this.random.nextInt(5);
            at.hurt(this.damageSources().mobAttack(this), 4.0F);
            this.level().broadcastEntityEvent(this, (byte)17);
            this.level().playSound(null, this.blockPosition(), SoundEvents.BLAZE_HURT, SoundSource.HOSTILE, 0.5F, this.random.nextFloat() * 0.1F + 0.9F);
         }
      } else if (this.distanceTo(owner) > 5.0F && this.getAnger() == 0 && !this.getStay()) {
         desired = owner;
         move = true;
      }

      if (desired != null) {
         this.getLookControl().setLookAt(desired, 10.0F, 20.0F);
         this.faceToward(desired.position(), 20.0F);
      }

      if ((this.onGround() || this.isInWater()) && this.jumpDelay-- <= 0 && move && desired != null) {
         this.performJump(desired);
      } else {
         this.jumping = false;
      }
   }

   private void faceToward(Vec3 pos, float maxTurnDeg) {
      double dx = pos.x - this.getX();
      double dz = pos.z - this.getZ();
      if (!(dx * dx + dz * dz < 1.0E-6)) {
         float desiredYaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
         float delta = Mth.wrapDegrees(desiredYaw - this.getYRot());
         float turn = Mth.clamp(delta, -maxTurnDeg, maxTurnDeg);
         float newYaw = this.getYRot() + turn;
         this.setYRot(newYaw);
         this.yRotO = newYaw;
         this.yBodyRot = newYaw;
         this.yBodyRotO = newYaw;
         this.yHeadRot = newYaw;
         this.yHeadRotO = newYaw;
      }
   }

   private void performJump(@NotNull LivingEntity desired) {
      boolean fast = this.hasUpgrade(GolemUpgrade.AIR);
      this.jumpDelay = (this.random.nextInt(10) + 5) / 3;
      this.jumping = true;
      this.field_768_a = 1.0F;
      Vec3 dir = desired.position().subtract(this.position());
      dir = new Vec3(dir.x, 0.0, dir.z);
      double len2 = dir.lengthSqr();
      if (len2 < 1.0E-6) {
         float yaw = this.getYRot() * (float) (Math.PI / 180.0);
         dir = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
      } else {
         dir = dir.normalize();
      }

      float strafe = 1.0F - this.random.nextFloat() * 2.0F;
      Vec3 side = new Vec3(-dir.z, 0.0, dir.x);
      double forwardMag = fast ? 0.55 : 0.45;
      if (this.isInWater()) {
         forwardMag *= 0.75;
      }

      double yImpulse = this.isInWater() ? 0.18 : 0.42;
      Vec3 impulse = dir.scale(forwardMag).add(side.scale(strafe * 0.2));
      this.faceToward(this.position().add(dir), 90.0F);
      this.setDeltaMovement(this.getDeltaMovement().x + impulse.x, yImpulse, this.getDeltaMovement().z + impulse.z);
      this.hasImpulse = true;
      this.level().playSound(null, this.blockPosition(), SoundEvents.CHEST_CLOSE, SoundSource.NEUTRAL, 0.1F, this.random.nextFloat() * 0.1F + 0.9F);
   }

   public boolean removeWhenFarAway(double d) {
      return false;
   }

   public void setInvSize() {
      this.setRows(this.hasUpgrade(GolemUpgrade.EARTH) ? 4 : 3);
      this.slotCount = this.getRows() * 9;
      // В 1.7.10 размер инвентаря и есть rows*9 (EntityTravelingTrunk.setInvSize),
      // поэтому контейнер ровно такой же, как окно GUI. Раньше обработчик создавался
      // на 36 слотов и не менялся: без апгрейда EARTH окно показывало 27 слотов, а
      // контейнер отдавал 36, и предметы «прятались» в невидимой части.
      this.inventory.resize(this.slotCount);
   }

   @NotNull
   public InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
      if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      } else {
         ItemStack stack = player.getItemInHand(hand);
         if (!stack.isEmpty()
            && stack.getItem() == ModItems.GOLEM_BELL.get()
            && this.hasUpgrade(GolemUpgrade.WATER)
            && !this.func_152113_b().equals(player.getScoreboardName())) {
            return InteractionResult.SUCCESS;
         } else if (this.getUpgrade() == -1 && !stack.isEmpty() && stack.getItem() instanceof ItemGolemUpgrade) {
            player.swing(hand);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         } else if (!stack.isEmpty() && stack.has(DataComponents.FOOD) && this.getHealth() < this.getMaxHealth()) {
            FoodProperties fp = (FoodProperties)stack.get(DataComponents.FOOD);
            stack.shrink(1);
            if (fp != null) {
               this.heal(fp.nutrition());
            }

            this.lidrot = 0.15F;
            this.lidrotO = 0.15F;
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         } else {
            if (this.level().isClientSide) {
               return InteractionResult.SUCCESS;
            } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
               serverPlayer.openMenu(this, buf -> buf.writeInt(this.getId()));
               return InteractionResult.CONSUME;
            }
            return InteractionResult.CONSUME;
         }
      }
   }

   public int getUpgrade() {
      return (Byte)this.entityData.get(DATA_UPGRADE);
   }

   public void setUpgrade(int upgrade) {
      this.entityData.set(DATA_UPGRADE, (byte)upgrade);
   }

   public int getRows() {
      return (Byte)this.entityData.get(DATA_ROWS);
   }

   public void setRows(int rows) {
      this.entityData.set(DATA_ROWS, (byte)rows);
   }

   public int getAnger() {
      return (Integer)this.entityData.get(DATA_ANGER);
   }

   public void setAnger(int anger) {
      this.entityData.set(DATA_ANGER, anger);
   }

   public boolean isOpen() {
      return (Byte)this.entityData.get(DATA_OPEN) == 1;
   }

   public void setOpen(boolean v) {
      this.entityData.set(DATA_OPEN, (byte)(v ? 1 : 0));
   }

   public boolean getStay() {
      return (Byte)this.entityData.get(DATA_STAY) == 1;
   }

   public void setStay(boolean v) {
      this.entityData.set(DATA_STAY, (byte)(v ? 1 : 0));
   }

   public String func_152113_b() {
      return (String)this.entityData.get(DATA_OWNER_NAME);
   }

   public void setOwner(String name) {
      this.entityData.set(DATA_OWNER_NAME, name);
      if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
         MinecraftServer server = sl.getServer();
         ServerPlayer sp = server.getPlayerList().getPlayerByName(name);
         this.entityData.set(DATA_OWNER_UUID, sp == null ? Optional.empty() : Optional.of(sp.getUUID()));
      }
   }

   @Nullable
   public UUID getOwnerUUID() {
      return (UUID)((Optional)this.entityData.get(DATA_OWNER_UUID)).orElse(null);
   }

   @Nullable
   public LivingEntity getOwner() {
      return this.getOwnerEntity();
   }

   @Nullable
   public LivingEntity getOwnerEntity() {
      if (this.level().isClientSide) {
         return null;
      } else {
         UUID u = this.getOwnerUUID();
         if (u != null && this.level() instanceof ServerLevel sl) {
            ServerPlayer sp = sl.getServer().getPlayerList().getPlayer(u);
            if (sp != null) {
               return sp;
            }
         }

         return this.level() instanceof ServerLevel sl2 ? sl2.getServer().getPlayerList().getPlayerByName(this.func_152113_b()) : null;
      }
   }

   public void handleEntityEvent(byte id) {
      if (id != 17 && id != 18) {
         super.handleEntityEvent(id);
      } else {
         this.lidrot = 0.15F;
         this.lidrotO = 0.15F;
      }
   }

   public void travelToDimension(int par1) {
      if (!this.getStay() && !this.isRemoved() && this.isAlive()) {
         if (this.level() instanceof ServerLevel sl) {
            ResourceKey<Level> key;
            if (par1 == -1) {
               key = Level.NETHER;
            } else if (par1 == 0) {
               key = Level.OVERWORLD;
            } else {
               if (par1 != 1) {
                  return;
               }

               key = Level.END;
            }

            if (this.level().dimension() != key) {
               MinecraftServer server = sl.getServer();
               ServerLevel dest = server.getLevel(key);
               if (dest != null) {
                  try {
                     ServerPlayer ownerInDest = server.getPlayerList().getPlayerByName(this.func_152113_b());
                     if (this.changeDimension(new DimensionTransition(dest, this, DimensionTransition.DO_NOTHING)) instanceof EntityTravelingTrunk trunk) {
                        if (ownerInDest != null && ownerInDest.level().dimension() == key) {
                           trunk.moveTo(ownerInDest.getX(), ownerInDest.getY() + 0.25, ownerInDest.getZ(), trunk.getYRot(), trunk.getXRot());
                        }

                        trunk.setTarget(null);
                     }
                  } catch (Exception var9) {
                     Thaumcraft.log.error("Error while teleporting traveling trunk to dimension " + par1);
                     var9.printStackTrace();
                  }
               }
            }
         }
      }
   }

   public void playChestSound(boolean open) {
      this.level()
         .playSound(
            null,
            this.blockPosition(),
            open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE,
            SoundSource.NEUTRAL,
            0.5F,
            this.random.nextFloat() * 0.1F + 0.9F
         );
   }

   @NotNull
   public Component getDisplayName() {
      return Component.translatable("entity.trunk.guiname");
   }

   @Override
   public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
      int containerId, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.entity.player.Player player
   ) {
      return new thaumcraft.entities.smart.travelling_trunk.inventory.MenuTravelingTrunk(containerId, inventory, this);
   }

}
