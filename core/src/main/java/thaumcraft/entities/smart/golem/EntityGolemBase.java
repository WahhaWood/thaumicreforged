package thaumcraft.entities.smart.golem;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.api.entities.IGolemHookHandler;
import thaumcraft.entities.brainless.dart.EntityDart;
import thaumcraft.entities.smart.golem.handler.GolemFluidHandler;
import thaumcraft.entities.smart.golem.handler.GolemItemHandler;
import thaumcraft.entities.smart.golem.hook.GolemHooks;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.golem_core.GolemCore;
import thaumcraft.item.golem_core.ItemGolemCore;
import thaumcraft.item.golem_decoration.GolemDecorationType;
import thaumcraft.item.golem_upgrade.GolemUpgrade;
import thaumcraft.item.golem_upgrade.ItemGolemUpgrade;
import thaumcraft.util.ItemStackData;
import thaumcraft.util.Utils;

public class EntityGolemBase extends AbstractGolem implements IEntityWithComplexSpawn, net.minecraft.world.MenuProvider {
   static {
      // Грузим GolemHooks заранее: его DATA_HOOK_SYNC должен получить id до создания Builder,
      // иначе define() падает с IndexOutOfBounds.
      GolemHooks.hasHandlers();
   }

   private static final byte BYTE_EMPTY = -1;
   private static final char COLOR_EMPTY = 'h';
   private static final char UPGRADE_EMPTY = 'f';
   private static final EntityDataAccessor<ItemStack> DATA_CARRIED_STACK = SynchedEntityData.defineId(EntityGolemBase.class, EntityDataSerializers.ITEM_STACK);
   private static final EntityDataAccessor<String> DATA_OWNER = SynchedEntityData.defineId(EntityGolemBase.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Byte> DATA_TOGGLES = SynchedEntityData.defineId(EntityGolemBase.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> DATA_GOLEM_TYPE = SynchedEntityData.defineId(EntityGolemBase.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Integer> DATA_DECORATION_MASK = SynchedEntityData.defineId(EntityGolemBase.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_CORE = SynchedEntityData.defineId(EntityGolemBase.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<String> DATA_COLORS = SynchedEntityData.defineId(EntityGolemBase.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<String> DATA_UPGRADES = SynchedEntityData.defineId(EntityGolemBase.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Byte> DATA_HEALTH = SynchedEntityData.defineId(EntityGolemBase.class, EntityDataSerializers.BYTE);
   protected GolemItemHandler inventory;
   protected GolemFluidHandler fluidHandler;
   public ItemStack itemCarried = ItemStack.EMPTY;
   public ItemStack itemWatched = ItemStack.EMPTY;
   public Aspect essentia;
   public int essentiaAmount;
   public Direction homeFacing = Direction.NORTH;
   public int regenTimer;
   public int action;
   public int leftArm;
   public int rightArm;
   public int healing;
   public float bootup = -1.0F;
   boolean pdw;
   public boolean advanced = false;
   public boolean paused = false;
   public boolean inactive = false;
   public boolean flag = false;
   public byte[] colors = new byte[]{-1};
   public byte[] upgrades = new byte[]{-1};
   public EnumSet<GolemDecorationType> currentDecorations = EnumSet.noneOf(GolemDecorationType.class);
   public EnumGolemType golemType = EnumGolemType.WOOD;
   protected ArrayList<Marker> markers;
   public final HashMap<IGolemHookHandler, Object> hookHandlerData = new HashMap<>();
   private FakePlayer fakePlayer;
   @Nullable
   private UUID ownerUUID;

   public EntityGolemBase(EntityType<? extends EntityGolemBase> type, Level level) {
      super(type, level);
      this.regenTimer = 0;
      this.markers = new ArrayList<>();
      this.pdw = false;
      this.action = 0;
      this.leftArm = 0;
      this.rightArm = 0;
      this.healing = 0;
      this.setPersistenceRequired();
      this.inventory = new GolemItemHandler(1);
      this.fluidHandler = new GolemFluidHandler(1000);
      this.inventory.setOnChanged(this::onInventoryChanged);
      this.fluidHandler.setOnChanged(this::onFluidChanged);
      this.ensureUpgradeCapacity(true);
      this.ensureColorsCapacityToInventory(true);
      this.syncUpgradesToData();
      this.syncColorsToData();
      GolemHooks.hook_EntityInit(this, this.hookHandlerData);
      if (this.getNavigation() instanceof GroundPathNavigation groundNav) {
         groundNav.setCanOpenDoors(true);
         groundNav.setCanPassDoors(true);
      }

      this.getNavigation().setCanFloat(true);
   }

   public EntityGolemBase(Level level, EnumGolemType type, boolean adv) {
      this((EntityType<? extends EntityGolemBase>)ModEntities.GOLEM.get(), level);
      this.golemType = Objects.requireNonNullElse(type, EnumGolemType.WOOD);
      this.advanced = adv;
      if (!this.level().isClientSide) {
         this.entityData.set(DATA_GOLEM_TYPE, (byte)this.golemType.ordinal());
      }

      this.ensureUpgradeCapacity(true);
      this.syncUpgradesToData();
   }

   public static Builder createAttributes() {
      return AbstractGolem.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 20.0)
         .add(Attributes.ATTACK_DAMAGE, 1.0)
         .add(Attributes.MOVEMENT_SPEED, 0.6)
         .add(Attributes.FOLLOW_RANGE, 32.0)
         .add(Attributes.STEP_HEIGHT, 1.0)
         .add(Attributes.ARMOR, 0.0);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_CARRIED_STACK, ItemStack.EMPTY);
      builder.define(DATA_OWNER, "");
      builder.define(DATA_TOGGLES, (byte)0);
      builder.define(DATA_GOLEM_TYPE, (byte)0);
      builder.define(DATA_DECORATION_MASK, 0);
      builder.define(DATA_CORE, GolemCore.BLANK.ordinal());
      builder.define(DATA_COLORS, "");
      builder.define(DATA_UPGRADES, "");
      builder.define(DATA_HEALTH, (byte)0);
      GolemHooks.hook_DefineSynchedData(builder);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (DATA_DECORATION_MASK.equals(key) && this.level().isClientSide) {
         this.updateDecorationsFromMask((Integer)this.entityData.get(DATA_DECORATION_MASK));
      } else if (DATA_GOLEM_TYPE.equals(key) && this.level().isClientSide) {
         this.golemType = this.getGolemType();
         this.ensureUpgradeCapacity(false);
      } else if (DATA_UPGRADES.equals(key) && this.level().isClientSide) {
         this.decodeUpgradesFromData();
      } else if (DATA_CORE.equals(key) && this.level().isClientSide) {
         if (this.hasValidCore() && this.bootup < 0.0F) {
            this.bootup = 0.0F;
         } else if (!this.hasValidCore()) {
            this.bootup = -1.0F;
         }
      } else {
         if (DATA_COLORS.equals(key) && this.level().isClientSide) {
            this.decodeColorsFromData();
         }
      }
   }

   @Nullable
   public IItemHandler getItemHandler(@Nullable Direction side) {
      return this.isAlive() ? this.inventory : null;
   }

   @Nullable
   public IFluidHandler getFluidHandler(@Nullable Direction side) {
      return this.isAlive() ? this.fluidHandler : null;
   }

   private void onInventoryChanged() {
      this.ensureColorsCapacityToInventory(false);
      this.syncColorsToData();
      this.updateCarried();
   }

   private void onFluidChanged() {
      this.updateCarried();
   }

   public void setup(Direction side) {
      this.homeFacing = side == null ? Direction.NORTH : side;
      this.ensureUpgradeCapacity(false);
      this.syncUpgradesToData();
      this.setupGolem();
      this.setupGolemInventory();
   }

   public void setupGolemInventory() {
      if (this.getCoreItem(this.getCore()).hasInventory()) {
         if (this.hasValidCore()) {
            GolemInventoryBehaviors.configureInventory(this);
            int invSize = GolemInventoryBehaviors.getInventorySize(this);
            if (invSize > 0 && invSize != this.inventory.getSlotCount()) {
               GolemItemHandler newInventory = new GolemItemHandler(invSize);

               for (int i = 0; i < Math.min(this.inventory.getSlotCount(), invSize); i++) {
                  newInventory.setStackInSlot(i, this.inventory.getStackInSlot(i));
               }

               this.inventory = newInventory;
               this.inventory.setOnChanged(this::onInventoryChanged);
            }
         }

         this.ensureColorsCapacityToInventory(false);
         this.syncColorsToData();
      }
   }

   public void setupGolem() {
      if (!this.level().isClientSide) {
         this.entityData.set(DATA_GOLEM_TYPE, (byte)this.golemType.ordinal());
      }

      this.ensureUpgradeCapacity(false);
      this.syncUpgradesToData();
      GolemRuntimeSetup.applyMovementAndAttributes(this);
      GolemHooks.hook_SetupGolem(this, this.hookHandlerData);
   }

   public void aiStep() {
      super.aiStep();
      GolemHooks.hook_onEntityUpdate(this, this.hookHandlerData);
      if (this.level().isClientSide) {
         if (this.bootup < 0.0F && this.hasValidCore()) {
            this.bootup = 0.0F;
         } else if (this.bootup >= 0.0F && !this.hasValidCore()) {
            this.bootup = -1.0F;
         }
      }

      if (this.action > 0) {
         this.action--;
      }

      if (this.leftArm > 0) {
         this.leftArm--;
      }

      if (this.rightArm > 0) {
         this.rightArm--;
      }

      if (this.healing > 0) {
         this.healing--;
      }

      BlockPos pos = this.blockPosition();
      BlockPos below = pos.below();
      this.inactive = below.getY() > 0 && this.level().getBlockState(below).getBlock() == ModBlocks.MAGIC_TALLOW_BLOCK.get();
      if (this.level().isClientSide && this.bootup > 0.0F && this.hasValidCore()) {
         this.bootup = this.bootup * (this.bootup / 33.1F);
         this.playTCSoundLocal(this.bootup * 0.2F, this.bootup);
      }
   }

   private void handleRegeneration() {
      if (this.regenTimer > 0) {
         this.regenTimer--;
      } else {
         this.regenTimer = this.golemType.regenDelay;
         if (this.hasDecoration(GolemDecorationType.FEZ)) {
            this.regenTimer = (int)(this.regenTimer * 0.66F);
         }

         if (this.getHealth() < this.getMaxHealth()) {
            this.level().broadcastEntityEvent(this, (byte)5);
            this.heal(1.0F);
         }
      }
   }

   public void setDecorationsFromMask(int mask) {
      this.entityData.set(DATA_DECORATION_MASK, mask);
      this.updateDecorationsFromMask(mask);
   }

   int getDecorationMask() {
      return (Integer)this.entityData.get(DATA_DECORATION_MASK);
   }

   private void tryTeleportToHome() {
      BlockPos home = this.getHomePosition();
      if (home != null) {
         if (this.distanceToSqr(Vec3.atCenterOf(home)) >= 2304.0 || this.isInWall()) {
            int hx = home.getX();
            int hy = home.getY();
            int hz = home.getZ();

            for (int dy = 1; dy >= -1; dy--) {
               for (int dx = -1; dx <= 1; dx++) {
                  for (int dz = -1; dz <= 1; dz++) {
                     BlockPos floor = new BlockPos(hx + dx, hy - 1 + dy, hz + dz);
                     BlockPos stand = new BlockPos(hx + dx, hy + dy, hz + dz);
                     if (hasSolidTopSurface(this.level(), floor) && !isFullBlockCollision(this.level(), stand)) {
                        this.moveTo(hx + dx + 0.5, hy + dy, hz + dz + 0.5, this.getYRot(), this.getXRot());
                        this.getNavigation().stop();
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   public boolean hasDecoration(GolemDecorationType type) {
      return this.currentDecorations.contains(type);
   }

   private void syncDecorationsToMask() {
      int mask = 0;

      for (GolemDecorationType type : this.currentDecorations) {
         mask |= 1 << type.ordinal();
      }

      this.entityData.set(DATA_DECORATION_MASK, mask);
   }

   private void updateDecorationsFromMask(int mask) {
      this.currentDecorations.clear();

      for (GolemDecorationType type : GolemDecorationType.values()) {
         if ((mask >> type.ordinal() & 1) == 1) {
            this.currentDecorations.add(type);
         }
      }
   }

   public float getAIMoveSpeed() {
      if (!this.paused && !this.inactive) {
         float speed = this.getGolemTypeForLogic().speed * (this.hasDecoration(GolemDecorationType.BOWTIE) ? 1.1F : 1.0F);
         if (this.hasDecoration(GolemDecorationType.ARMOR)) {
            speed *= 0.88F;
         }

         speed *= 1.0F + this.getUpgradeAmount(GolemUpgrade.AIR.ordinal()) * 0.15F;
         if (this.advanced) {
            speed *= 1.1F;
         }

         if (this.isInWater()
            && (
               this.getGolemTypeForLogic() == EnumGolemType.STONE
                  || this.getGolemTypeForLogic() == EnumGolemType.IRON
                  || this.getGolemTypeForLogic() == EnumGolemType.THAUMIUM
            )) {
            speed *= 2.0F;
         }

         return speed;
      } else {
         return 0.0F;
      }
   }

   public int getCarryLimit() {
      int base = this.golemType.carry;
      if (this.level().isClientSide) {
         base = this.getGolemType().carry;
      }

      return base + Math.min(16, Math.max(4, base)) * this.getUpgradeAmount(GolemUpgrade.EARTH.ordinal());
   }

   public int getFluidCarryLimit() {
      return Mth.floor(Mth.sqrt(this.getCarryLimit())) * 1000;
   }

   public float getRange() {
      float dmod = 16 + this.getUpgradeAmount(GolemUpgrade.WATER.ordinal()) * 4;
      if (this.hasDecoration(GolemDecorationType.GOGGLES)) {
         dmod += Math.max(dmod * 0.1F, 1.0F);
      }

      if (this.advanced) {
         dmod += Math.max(dmod * 0.2F, 2.0F);
      }

      return dmod;
   }

   public boolean isWithinHomeDistance(int x, int y, int z) {
      float dmod = this.getRange();
      BlockPos home = this.getHomePosition();
      return home == null ? true : home.distSqr(new BlockPos(x, y, z)) < dmod * dmod;
   }

   void applyUpgradesArray(byte[] saved) {
      this.ensureUpgradeCapacity(false);
      Arrays.fill(this.upgrades, (byte)-1);
      if (saved != null) {
         int len = Math.min(saved.length, this.upgrades.length);
         System.arraycopy(saved, 0, this.upgrades, 0, len);
      }
   }

   void applyColorsArray(byte[] saved) {
      this.ensureColorsCapacityToInventory(true);
      Arrays.fill(this.colors, (byte)-1);
      if (saved != null) {
         int len = Math.min(saved.length, this.colors.length);
         System.arraycopy(saved, 0, this.colors, 0, len);
      }
   }

   void ensureColorsCapacityToInventory(boolean preserve) {
      int expected = Math.max(1, this.inventory == null ? 1 : this.inventory.getSlotCount());
      if (this.colors == null || this.colors.length != expected) {
         byte[] prev = this.colors == null ? new byte[0] : this.colors;
         byte[] next = new byte[expected];
         Arrays.fill(next, (byte)-1);
         if (preserve && prev.length > 0) {
            System.arraycopy(prev, 0, next, 0, Math.min(prev.length, next.length));
         }

         this.colors = next;
      }
   }

   void ensureUpgradeCapacity(boolean preserve) {
      int expected = Math.max(1, this.expectedUpgradeSlots());
      if (this.upgrades == null || this.upgrades.length != expected) {
         byte[] prev = this.upgrades == null ? new byte[0] : this.upgrades;
         byte[] next = new byte[expected];
         Arrays.fill(next, (byte)-1);
         if (preserve && prev.length > 0) {
            System.arraycopy(prev, 0, next, 0, Math.min(prev.length, next.length));
         }

         this.upgrades = next;
      }
   }

   private int expectedUpgradeSlots() {
      int base = this.getGolemTypeForLogic().upgrades;
      int adv = this.advanced ? 1 : 0;
      return base + adv;
   }

   private EnumGolemType getGolemTypeForLogic() {
      return this.level().isClientSide ? this.getGolemType() : this.golemType;
   }

   void syncColorsToData() {
      this.entityData.set(DATA_COLORS, this.encodeNibbleArray(this.colors, 'h'));
   }

   void syncUpgradesToData() {
      this.entityData.set(DATA_UPGRADES, this.encodeNibbleArray(this.upgrades, 'f'));
   }

   private void decodeColorsFromData() {
      this.ensureColorsCapacityToInventory(true);
      this.decodeNibbleStringInto((String)this.entityData.get(DATA_COLORS), this.colors, 'h');
   }

   private void decodeUpgradesFromData() {
      this.ensureUpgradeCapacity(true);
      this.decodeNibbleStringInto((String)this.entityData.get(DATA_UPGRADES), this.upgrades, 'f');
   }

   @NotNull
   public InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
      return GolemInteractionHandler.mobInteract(this, player, hand);
   }

   public InteractionResult customInteraction(Player player, InteractionHand hand) {
      return GolemInteractionHandler.customInteraction(this, player, hand);
   }

   private <T> T getEnumSafe(T[] values, int ordinal) {
      return ordinal >= 0 && ordinal < values.length ? values[ordinal] : values[0];
   }

   public ItemGolemCore getCoreItem(GolemCore core) {
      return ItemGolemCore.getItemByCore(core);
   }

   public ItemGolemUpgrade getUpgrageById(byte upgradeID) {
      GolemUpgrade upgrade = this.getEnumSafe(GolemUpgrade.values(), upgradeID);
      return ItemGolemUpgrade.getItemByUpgrade(upgrade);
   }

   void handleCookieFeeding(Player player, InteractionHand hand, ItemStack held) {
      held.shrink(1);
      player.swing(hand);

      for (int unused = 0; unused < 3; unused++) {
         double _x = this.random.nextGaussian() * 0.02;
         double _xx = this.random.nextGaussian() * 0.02;
         double _xxx = this.random.nextGaussian() * 0.02;
         this.level()
            .addParticle(
               ParticleTypes.HEART,
               this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
               this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
               this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
               _x,
               _xx,
               _xxx
            );
      }

      this.level()
         .playSound(
            null, this.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.3F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F
         );
   }

   protected void addDecoration(GolemDecorationType type, ItemStack itemStack) {
      if (!this.hasDecoration(type)) {
         if (type != GolemDecorationType.TOP_HAT && type != GolemDecorationType.FEZ
            || !this.hasDecoration(GolemDecorationType.TOP_HAT) && !this.hasDecoration(GolemDecorationType.FEZ)) {
            if (type != GolemDecorationType.GOGGLES && type != GolemDecorationType.VISOR
               || !this.hasDecoration(GolemDecorationType.GOGGLES) && !this.hasDecoration(GolemDecorationType.VISOR)) {
               if (type != GolemDecorationType.BOWTIE && type != GolemDecorationType.ARMOR
                  || !this.hasDecoration(GolemDecorationType.ARMOR) && !this.hasDecoration(GolemDecorationType.BOWTIE)) {
                  this.currentDecorations.add(type);
                  if (!this.level().isClientSide) {
                     this.syncDecorationsToMask();
                     itemStack.shrink(1);
                     this.playTCSoundGlobal("cameraclack", 1.0F, 1.0F);
                  }

                  this.setupGolem();
               }
            }
         }
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      this.paused = false;
      if (source.is(DamageTypes.CACTUS)) {
         return false;
      } else {
         if (source.is(DamageTypes.MAGIC) && this.getGolemTypeForLogic() == EnumGolemType.THAUMIUM) {
            amount *= 0.5F;
         }

         if (!source.is(DamageTypes.IN_WALL) && source.is(DamageTypes.OUTSIDE_BORDER)) {
         }

         Entity src = source.getEntity();
         if (src != null && this.getUpgradeAmount(GolemUpgrade.ENTROPY.ordinal()) > 0 && src.getId() != this.getId()) {
         }

         boolean r = super.hurt(source, amount);
         if (!this.level().isClientSide) {
            this.entityData.set(DATA_HEALTH, (byte)this.getHealth());
         }

         return r;
      }
   }

   private int getFireAspectLevel() {
      return this.registryAccess()
         .lookupOrThrow(Registries.ENCHANTMENT)
         .get(Enchantments.FIRE_ASPECT)
         .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, this.getMainHandItem()))
         .orElse(0);
   }

   public boolean isValidTarget(Entity target) {
      if (!target.isAlive()) {
         return false;
      } else if (target instanceof Player p && p.getGameProfile().getName().equals(this.getOwnerName())) {
         return false;
      } else if (!this.isWithinHomeDistance(Mth.floor(target.getX()), Mth.floor(target.getY()), Mth.floor(target.getZ()))) {
         return false;
      } else {
         boolean isUpgrade4 = this.getUpgradeAmount(GolemUpgrade.ORDER.ordinal()) > 0;
         if (this.getCore() == GolemCore.BUTCHER) {
            return target instanceof Animal a && !(target instanceof Enemy) && !(target instanceof TamableAnimal ta && ta.isTame()) ? !a.isBaby() : false;
         } else if (this.canAttackCreepers() && isUpgrade4 && target instanceof Creeper) {
            return true;
         } else if (this.canAttackHostiles() && target instanceof Enemy && !(target instanceof Creeper)) {
            return true;
         } else {
            return !this.canAttackAnimals()
                  || !isUpgrade4
                  || !(target instanceof Animal)
                  || target instanceof Enemy
                  || target instanceof TamableAnimal ta && ta.isTame()
               ? this.canAttackPlayers() && isUpgrade4 && target instanceof Player
               : true;
         }
      }
   }

   public short getColors(int slot) {
      if (slot >= 0 && slot < this.colors.length) {
         return this.colors[slot] == -1 ? -1 : (short)(this.colors[slot] & 255);
      } else {
         return -1;
      }
   }

   public void setColors(int slot, int color) {
      this.ensureColorsCapacityToInventory(true);
      if (slot >= 0 && slot < this.colors.length) {
         this.colors[slot] = (byte)color;
         this.syncColorsToData();
      }
   }

   public int getColorSlotCount() {
      this.ensureColorsCapacityToInventory(true);
      return this.colors.length;
   }

   public byte[] getColorsSnapshot() {
      this.ensureColorsCapacityToInventory(true);
      return Arrays.copyOf(this.colors, this.colors.length);
   }

   public void setColorsFromArray(byte[] saved) {
      this.applyColorsArray(saved);
      this.syncColorsToData();
   }

   public byte getUpgrade(int slot) {
      this.ensureUpgradeCapacity(true);
      return slot >= 0 && slot < this.upgrades.length ? this.upgrades[slot] : -1;
   }

   public int getUpgradeAmount(int type) {
      int count = 0;

      for (byte b : this.upgrades) {
         if (type == b) {
            count++;
         }
      }

      return count;
   }

   public void setUpgrade(int slot, byte upgrade) {
      this.ensureUpgradeCapacity(true);
      if (slot >= 0 && slot < this.upgrades.length) {
         this.upgrades[slot] = upgrade;
         this.syncUpgradesToData();
      }
   }

   public int getUpgradeSlotCount() {
      this.ensureUpgradeCapacity(true);
      return this.upgrades.length;
   }

   public byte[] getUpgradesSnapshot() {
      this.ensureUpgradeCapacity(true);
      return Arrays.copyOf(this.upgrades, this.upgrades.length);
   }

   public void setUpgradesFromArray(byte[] saved) {
      this.applyUpgradesArray(saved);
      this.syncUpgradesToData();
   }

   public String getOwnerName() {
      return (String)this.entityData.get(DATA_OWNER);
   }

   public void setOwner(String par1Str) {
      String ownerName = par1Str == null ? "" : par1Str;
      this.entityData.set(DATA_OWNER, ownerName);
      this.ownerUUID = this.resolveOwnerUUID(ownerName);
      this.fakePlayer = null;
   }

   public void setOwner(@Nullable Player player) {
      if (player == null) {
         this.setOwner("");
      } else {
         this.entityData.set(DATA_OWNER, player.getGameProfile().getName());
         this.ownerUUID = player.getUUID();
         this.fakePlayer = null;
      }
   }

   public void setOwnerData(@Nullable UUID ownerUUID, @Nullable String ownerName) {
      this.entityData.set(DATA_OWNER, ownerName == null ? "" : ownerName);
      this.ownerUUID = ownerUUID;
      this.fakePlayer = null;
   }

   @Nullable
   public UUID getOwnerUUID() {
      if (this.ownerUUID == null) {
         this.ownerUUID = this.resolveOwnerUUID(this.getOwnerName());
      }

      return this.ownerUUID;
   }

   public void setMarkers(ArrayList<Marker> markers) {
      this.markers = markers == null ? new ArrayList<>() : markers;
   }

   public ArrayList<Marker> getMarkers() {
      this.validateMarkers();
      return this.markers;
   }

   public ArrayList<Marker> getMarkersSnapshot() {
      this.validateMarkers();
      return new ArrayList<>(this.markers);
   }

   public void clearMarkers() {
      this.markers.clear();
   }

   public void replaceMarkers(List<Marker> markers) {
      this.markers = markers == null ? new ArrayList<>() : new ArrayList<>(markers);
   }

   protected void validateMarkers() {
      ResourceKey<Level> currentDim = this.level().dimension();
      this.markers.removeIf(m -> !m.getDimension().equals(currentDim));
   }

   public LivingEntity getOwner() {
      UUID ownerId = this.getOwnerUUID();
      if (ownerId != null) {
         Player player = this.level().getPlayerByUUID(ownerId);
         if (player != null) {
            return player;
         }
      }

      String ownerName = this.getOwnerName();
      if (ownerName != null && !ownerName.isEmpty()) {
         for (Player player : this.level().players()) {
            if (player.getGameProfile().getName().equals(ownerName)) {
               return player;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Nullable
   private UUID resolveOwnerUUID(@Nullable String ownerName) {
      if (ownerName != null && !ownerName.isEmpty()) {
         for (Player player : this.level().players()) {
            if (ownerName.equals(player.getGameProfile().getName())) {
               return player.getUUID();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private GameProfile getOwnerGameProfile(ServerLevel serverLevel) {
      UUID ownerId = this.getOwnerUUID();
      String ownerName = this.getOwnerName();
      if (ownerId != null) {
         Player onlineOwner = serverLevel.getPlayerByUUID(ownerId);
         if (onlineOwner != null) {
            ownerName = onlineOwner.getGameProfile().getName();
            this.entityData.set(DATA_OWNER, ownerName);
         }

         if (ownerName != null && !ownerName.isEmpty()) {
            return new GameProfile(ownerId, ownerName);
         }
      }

      String fallbackName = "[tcgolem:" + this.getStringUUID().substring(0, 8) + "]";
      return new GameProfile(this.getUUID(), fallbackName);
   }

   public void setCarried(ItemStack stack) {
      this.itemCarried = stack == null ? ItemStack.EMPTY : stack;
      this.updateCarried();
   }

   public void setWatchedItem(ItemStack stack) {
      this.itemWatched = stack == null ? ItemStack.EMPTY : stack;
   }

   public boolean hasSomething() {
      return this.inventory.hasItems();
   }

   public ItemStack getCarried() {
      if (!this.itemCarried.isEmpty() && this.itemCarried.getCount() <= 0) {
         this.setCarried(ItemStack.EMPTY);
      }

      return this.itemCarried;
   }

   public ItemStack getWatchedItem() {
      if (this.itemWatched == null || !this.itemWatched.isEmpty() && this.itemWatched.getCount() <= 0) {
         this.itemWatched = ItemStack.EMPTY;
      }

      return this.itemWatched;
   }

   public boolean hasWatchedItem() {
      return !this.getWatchedItem().isEmpty();
   }

   public void clearWatchedItem() {
      this.itemWatched = ItemStack.EMPTY;
   }

   public int getCarrySpace() {
      return this.itemCarried.isEmpty()
         ? this.getCarryLimit()
         : Math.min(this.getCarryLimit() - this.itemCarried.getCount(), this.itemCarried.getMaxStackSize() - this.itemCarried.getCount());
   }

   public boolean[] getToggles() {
      return Utils.unpack((Byte)this.entityData.get(DATA_TOGGLES));
   }

   public byte getTogglesValue() {
      return (Byte)this.entityData.get(DATA_TOGGLES);
   }

   public void setToggle(int index, boolean tog) {
      boolean[] fz = this.getToggles();
      if (index >= 0 && index < fz.length) {
         fz[index] = tog;
         this.entityData.set(DATA_TOGGLES, Utils.pack(fz));
      }
   }

   public void setTogglesValue(byte tog) {
      this.entityData.set(DATA_TOGGLES, tog);
   }

   public boolean fillUsesAnyAmountMode() {
      return this.getToggles()[0];
   }

   public boolean useTargetsEmptyBlocksOnly() {
      return this.getToggles()[0];
   }

   public boolean useBreaksBlocks() {
      return this.getToggles()[1];
   }

   public boolean useSneaksWhileActing() {
      return this.getToggles()[2];
   }

   public boolean canAttackHostiles() {
      return !this.getToggles()[1];
   }

   public boolean canAttackAnimals() {
      return !this.getToggles()[2];
   }

   public boolean canAttackPlayers() {
      return !this.getToggles()[3];
   }

   public boolean canAttackCreepers() {
      return !this.getToggles()[4];
   }

   public boolean checkOreDict() {
      return this.getToggles()[5];
   }

   public boolean ignoreDamage() {
      return this.getToggles()[6];
   }

   public void setGolemType(EnumGolemType type) {
      this.golemType = type == null ? EnumGolemType.WOOD : type;
      this.entityData.set(DATA_GOLEM_TYPE, (byte)this.golemType.ordinal());
      this.ensureColorsCapacityToInventory(false);
      this.ensureUpgradeCapacity(false);
      this.syncColorsToData();
      this.syncUpgradesToData();
      this.setupGolemInventory();
   }

   public boolean ignoreNBT() {
      return this.getToggles()[7];
   }

   public EnumGolemType getGolemType() {
      return EnumGolemType.getType((Byte)this.entityData.get(DATA_GOLEM_TYPE));
   }

   public int getGolemStrength() {
      return this.getGolemTypeForLogic().strength + this.getUpgradeAmount(GolemUpgrade.EARTH.ordinal());
   }

   public void setCore(GolemCore core) {
      this.entityData.set(DATA_CORE, core.ordinal());
   }

   public GolemCore getCore() {
      int ordinal = (Integer)this.entityData.get(DATA_CORE);
      return ordinal >= 0 && ordinal < GolemCore.values().length ? GolemCore.values()[ordinal] : GolemCore.BLANK;
   }

   public boolean hasValidCore() {
      return this.getCore() != GolemCore.BLANK;
   }

   @NotNull
   public ItemStack getCarriedForDisplay() {
      return (ItemStack)this.entityData.get(DATA_CARRIED_STACK);
   }

   public void updateCarried() {
      if (!this.itemCarried.isEmpty()) {
         this.entityData.set(DATA_CARRIED_STACK, this.itemCarried.copy());
      } else if (this.getCore() == GolemCore.LIQUID && this.fluidHandler.hasFluid()) {
         ItemStack disp = FluidUtil.getFilledBucket(this.fluidHandler.getCarriedFluid());
         this.entityData.set(DATA_CARRIED_STACK, disp);
      } else if (this.getCore() == GolemCore.ESSENCE) {
         ItemStack disp = new ItemStack((ItemLike)ModItems.JAR.get());
         int amt = (int)(64.0F * this.essentiaAmount / this.getCarryLimit());
         if (this.essentia != null && this.essentiaAmount > 0) {
            ((IEssenceContainerItem)disp.getItem()).setAspects(disp, new AspectList().add(this.essentia, amt));
         }

         this.entityData.set(DATA_CARRIED_STACK, disp);
      } else {
         this.entityData.set(DATA_CARRIED_STACK, ItemStack.EMPTY);
      }
   }

   void syncHealthToData() {
      this.entityData.set(DATA_HEALTH, (byte)this.getHealth());
   }

   public ArrayList<Byte> getColorsMatching(ItemStack match) {
      ArrayList<Byte> list = new ArrayList<>();

      for (int a = 0; a < this.inventory.getSlotCount(); a++) {
         ItemStack inv = this.inventory.getStackInSlot(a);
         if (!inv.isEmpty()) {
            boolean matched;
            if (this.checkOreDict()) {
               matched = ItemStack.isSameItem(inv, match);
            } else {
               matched = ItemStackData.sameItemSameTags(inv, match);
            }

            if (matched) {
               list.add(this.colors[a]);
            }
         }
      }

      return list;
   }

   public GolemItemHandler getInventory() {
      return this.inventory;
   }

   public GolemFluidHandler getFluidHandler() {
      return this.fluidHandler;
   }

   public FluidStack getFluidCarried() {
      return this.fluidHandler.getCarriedFluid();
   }

   public void setFluidCarried(FluidStack fluid) {
      this.fluidHandler.setCarriedFluid(fluid);
   }

   public void shootDart(LivingEntity target) {
      EntityDart dart = new EntityDart(
         (EntityType<? extends EntityDart>)ModEntities.DART.get(),
         this.level(),
         this,
         target,
         1.6F,
         7.0F - this.getUpgradeAmount(GolemUpgrade.WATER.ordinal()) * 1.75F
      );
      float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
      dart.setBaseDamage(f * 0.4F);
      this.playTCSoundGlobal("golemironshoot", 0.5F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.6F));
      this.level().addFreshEntity(dart);
      this.startLeftArmTimer();
   }

   public void startActionTimer() {
      if (this.action == 0) {
         this.action = 6;
         this.level().broadcastEntityEvent(this, (byte)4);
      }
   }

   public void startLeftArmTimer() {
      if (this.leftArm == 0) {
         this.leftArm = 5;
         this.level().broadcastEntityEvent(this, (byte)6);
      }
   }

   public void startRightArmTimer() {
      if (this.rightArm == 0) {
         this.rightArm = 5;
         this.level().broadcastEntityEvent(this, (byte)8);
      }
   }

   public void handleEntityEvent(byte id) {
      switch (id) {
         case 4:
            this.action = 6;
            break;
         case 5:
            this.healing = 5;
            int bonus = this.hasDecoration(GolemDecorationType.TOP_HAT) ? 5 : 0;
            if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
               Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(this.getGolemTypeForLogic().health + bonus);
            }
            break;
         case 6:
            this.leftArm = 5;
            break;
         case 7:
            this.bootup = 33.0F;
            break;
         case 8:
            this.rightArm = 5;
            break;
         default:
            super.handleEntityEvent(id);
      }
   }

   protected float getSoundVolume() {
      return 0.1F;
   }

   public void dropStuff() {
      if (!this.level().isClientSide && !this.itemCarried.isEmpty()) {
         this.spawnAtLocation(this.itemCarried, 0.5F);
         this.setCarried(ItemStack.EMPTY);
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.tcSound("cameraclack");
   }

   protected SoundEvent getHurtSound(@NotNull DamageSource source) {
      return this.tcSound("cameraclack");
   }

   protected SoundEvent getDeathSound() {
      return this.tcSound("cameraclack");
   }

   public void writeSpawnData(RegistryFriendlyByteBuf data) {
      data.writeVarInt(this.getCore().ordinal());
      data.writeBoolean(this.advanced);
      data.writeVarInt(this.inventory.getSlotCount());
      byte[] upgrades = this.getUpgradesSnapshot();
      data.writeVarInt(upgrades.length);

      for (byte b : upgrades) {
         data.writeByte(b);
      }
   }

   public void readSpawnData(RegistryFriendlyByteBuf data) {
      int coreOrd = data.readVarInt();
      if (coreOrd >= 0 && coreOrd < GolemCore.values().length) {
         this.setCore(GolemCore.values()[coreOrd]);
      } else {
         this.setCore(GolemCore.BLANK);
      }

      this.advanced = data.readBoolean();
      if (this.hasValidCore()) {
         this.bootup = 0.0F;
      }

      int slotCount = data.readVarInt();
      this.inventory = new GolemItemHandler(slotCount);
      this.inventory.setOnChanged(this::onInventoryChanged);
      this.ensureColorsCapacityToInventory(true);
      int upLen = data.readVarInt();
      this.upgrades = new byte[Math.max(1, upLen)];

      for (int a = 0; a < this.upgrades.length; a++) {
         this.upgrades[a] = data.readByte();
      }

      this.syncUpgradesToData();
      this.syncColorsToData();
      int bonus = this.hasDecoration(GolemDecorationType.TOP_HAT) ? 5 : 0;
      if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
         Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(this.getGolemTypeForLogic().health + bonus);
      }
   }

   @NotNull
   public Component getName() {
      return (Component)(this.hasCustomName()
         ? Objects.requireNonNull(this.getCustomName())
         : Component.translatable("item.ItemGolemPlacer." + this.getGolemType().ordinal() + ".name"));
   }

   private static boolean hasSolidTopSurface(Level level, BlockPos pos) {
      return level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP);
   }

   private static boolean isFullBlockCollision(Level level, BlockPos pos) {
      return level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
   }

   private SoundEvent tcSound(String path) {
      ResourceLocation id = ResourceLocation.fromNamespaceAndPath("thaumcraft", path);
      return (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(id);
   }

   void playTCSoundGlobal(String path, float volume, float pitch) {
      SoundEvent se = this.tcSound(path);
      if (se != null) {
         this.level().playSound(null, this.blockPosition(), se, SoundSource.NEUTRAL, volume, pitch);
      }
   }

   private void playTCSoundLocal(float volume, float pitch) {
      SoundEvent se = this.tcSound("cameraticks");
      if (se != null) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), se, SoundSource.NEUTRAL, volume, pitch, false);
      }
   }

   public BlockPos getHomePosition() {
      return this.hasRestriction() ? this.getRestrictCenter() : null;
   }

   public void setHomeArea(int x, int y, int z, int range) {
      this.restrictTo(new BlockPos(x, y, z), range);
   }

   public boolean canAttackClass(Class<?> par1Class) {
      return Villager.class != par1Class && EntityGolemBase.class != par1Class && Bat.class != par1Class;
   }

   public int getAttackSpeed() {
      return 20 - (this.advanced ? 2 : 0);
   }

   public int getTotalArmorValue() {
      int var1 = super.getArmorValue() + this.getGolemTypeForLogic().armor;
      if (this.hasDecoration(GolemDecorationType.VISOR)) {
         var1++;
      }

      if (this.hasDecoration(GolemDecorationType.ARMOR)) {
         var1 += 4;
      }

      if (var1 > 20) {
         var1 = 20;
      }

      return var1;
   }

   protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
      if (onGround && this.fallDistance > 0.0F) {
         if (state.isAir() && this.level().getBlockState(pos.below()).getBlock() instanceof FenceBlock) {
            BlockState s = this.level().getBlockState(pos.below());
         }

         this.causeFallDamage(this.fallDistance, 1.0F, this.damageSources().fall());
         this.fallDistance = 0.0F;
      } else if (y < 0.0) {
         this.fallDistance = (float)(this.fallDistance - y);
      }
   }

   public LivingEntity getTarget() {
      LivingEntity e = super.getTarget();
      if (e != null && !e.isAlive()) {
         e = null;
      }

      return e;
   }

   public void heal(float amount) {
      super.heal(amount);
      if (!this.level().isClientSide) {
         this.entityData.set(DATA_HEALTH, (byte)this.getHealth());
      }
   }

   public void setHealth(float health) {
      super.setHealth(health);
      if (!this.level().isClientSide) {
         this.entityData.set(DATA_HEALTH, (byte)this.getHealth());
      }
   }

   public float getHealthPercentage() {
      return ((Byte)this.entityData.get(DATA_HEALTH) & 255) / this.getMaxHealth();
   }

   public int getActionTimer() {
      return 3 - Math.abs(this.action - 3);
   }

   @NotNull
   protected PathNavigation createNavigation(@NotNull Level level) {
      return new GroundPathNavigation(this, level);
   }

   private String encodeNibbleArray(byte[] arr, char empty) {
      if (arr != null && arr.length != 0) {
         char[] out = new char[arr.length];

         for (int i = 0; i < arr.length; i++) {
            byte v = arr[i];
            if (v < 0) {
               out[i] = empty;
            } else {
               int nib = v & 15;
               out[i] = Character.forDigit(nib, 16);
            }
         }

         return new String(out);
      } else {
         return "";
      }
   }

   private void decodeNibbleStringInto(String s, byte[] out, char empty) {
      if (out != null && out.length != 0) {
         Arrays.fill(out, (byte)-1);
         if (s != null && !s.isEmpty()) {
            int len = Math.min(s.length(), out.length);

            for (int i = 0; i < len; i++) {
               char c = Character.toLowerCase(s.charAt(i));
               if (c == empty) {
                  out[i] = -1;
               } else {
                  int d = Character.digit(c, 16);
                  if (d < 0) {
                     out[i] = -1;
                  } else {
                     out[i] = (byte)d;
                  }
               }
            }
         }
      }
   }

   // ------------------------------------------------------------------
   // сохранение
   // ------------------------------------------------------------------

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putByte("GolemType", (byte)this.golemType.ordinal());
      tag.putInt("GolemCore", this.getCore().ordinal());
      tag.putByteArray("GolemColors", this.colors);
      tag.putByteArray("GolemUpgrades", this.upgrades);
      tag.putInt("DecorationMask", this.getDecorationMask());
      tag.putBoolean("Paused", this.paused);
      tag.putBoolean("Inactive", this.inactive);
      tag.putBoolean("Advanced", this.advanced);
      tag.putString("HomeFacing", this.homeFacing.getName());
      // Vanilla Mob does not save its restriction, so the home would be lost on every reload (TC4 saves HomeX/Y/Z).
      if (this.hasRestriction()) {
         BlockPos home = this.getRestrictCenter();
         tag.putInt("HomeX", home.getX());
         tag.putInt("HomeY", home.getY());
         tag.putInt("HomeZ", home.getZ());
         tag.putInt("HomeRange", (int)this.getRestrictRadius());
      }

      if (this.ownerUUID != null) {
         tag.putUUID("Owner", this.ownerUUID);
      }

      tag.putString("OwnerName", (String)this.entityData.get(DATA_OWNER));
      if (!this.itemCarried.isEmpty()) {
         tag.put("Carried", this.itemCarried.save(this.registryAccess()));
      }

      net.minecraft.nbt.ListTag markersTag = new net.minecraft.nbt.ListTag();

      for (Marker marker : this.getMarkers()) {
         CompoundTag m = new CompoundTag();
         m.putInt("x", marker.getX());
         m.putInt("y", marker.getY());
         m.putInt("z", marker.getZ());
         m.putString("side", marker.getSide().getName());
         m.putByte("color", marker.getColor());
         m.putString("dim", marker.getDimension().location().toString());
         markersTag.add(m);
      }

      tag.put("Markers", markersTag);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("GolemType")) {
         int ordinal = tag.getByte("GolemType");
         this.golemType = ordinal >= 0 && ordinal < EnumGolemType.values().length
            ? EnumGolemType.values()[ordinal]
            : EnumGolemType.WOOD;
      }

      this.entityData.set(DATA_GOLEM_TYPE, (byte)this.golemType.ordinal());
      if (tag.contains("GolemCore")) {
         int core = tag.getInt("GolemCore");
         this.setCore(core >= 0 && core < GolemCore.values().length ? GolemCore.values()[core] : GolemCore.BLANK);
         this.setupGolemInventory();
      }

      if (tag.contains("GolemColors")) {
         this.colors = tag.getByteArray("GolemColors");
      }

      if (tag.contains("GolemUpgrades")) {
         this.upgrades = tag.getByteArray("GolemUpgrades");
      }

      this.syncColorsToData();
      this.syncUpgradesToData();
      if (tag.contains("DecorationMask")) {
         this.setDecorationsFromMask(tag.getInt("DecorationMask"));
      }

      this.paused = tag.getBoolean("Paused");
      this.inactive = tag.getBoolean("Inactive");
      this.advanced = tag.getBoolean("Advanced");
      if (tag.contains("HomeX")) {
         this.restrictTo(new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ")), tag.contains("HomeRange") ? tag.getInt("HomeRange") : 32);
      }

      this.homeFacing = net.minecraft.core.Direction.byName(tag.getString("HomeFacing"));
      if (this.homeFacing == null) {
         this.homeFacing = net.minecraft.core.Direction.NORTH;
      }

      this.ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
      if (tag.contains("OwnerName")) {
         this.entityData.set(DATA_OWNER, tag.getString("OwnerName"));
      }

      if (tag.contains("Carried")) {
         this.itemCarried = ItemStack.parseOptional(this.registryAccess(), tag.getCompound("Carried"));
      }

      this.markers = new ArrayList<>();
      net.minecraft.nbt.ListTag markersTag = tag.getList("Markers", 10);

      for (int i = 0; i < markersTag.size(); i++) {
         CompoundTag m = markersTag.getCompound(i);
         net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim = net.minecraft.resources.ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION, net.minecraft.resources.ResourceLocation.parse(m.getString("dim"))
         );
         net.minecraft.core.Direction side = net.minecraft.core.Direction.byName(m.getString("side"));
         this.markers.add(
            new Marker(
               new net.minecraft.core.BlockPos(m.getInt("x"), m.getInt("y"), m.getInt("z")),
               dim,
               side == null ? net.minecraft.core.Direction.UP : side,
               m.getByte("color")
            )
         );
      }
   }


   // ------------------------------------------------------------------
   // AI (порт базовых задач из 1.7.10)
   // ------------------------------------------------------------------

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
      this.goalSelector.addGoal(4, new GolemGatherGoal());
      this.goalSelector.addGoal(5, new GolemDeliverGoal());
      this.goalSelector.addGoal(3, new GolemHarvestGoal());
      this.goalSelector.addGoal(6, new GolemEmptyGoal());
      this.goalSelector.addGoal(6, new GolemFillGoal());
      this.goalSelector.addGoal(6, new GolemSortingGoal());
      this.goalSelector.addGoal(6, new GolemUseGoal());
      this.goalSelector.addGoal(6, new GolemEssenceGoal());
      this.goalSelector.addGoal(6, new GolemLiquidGoal());
      this.goalSelector.addGoal(4, new GolemFishingGoal());
      this.goalSelector.addGoal(3, new GolemLumberGoal());
      this.goalSelector.addGoal(3, new GolemAttackGoal());
      this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 0.6));
      this.goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(10, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
   }

   /** Сбор предметов с земли (ядро GATHER). */
   private class GolemGatherGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private net.minecraft.world.entity.item.ItemEntity target;

      GolemGatherGoal() {
         this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         if (getCore() != GolemCore.GATHER || hasSomething()) {
            return false;
         }

         if (this.target == null || !this.target.isAlive()) {
            this.target = level()
               .getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, getBoundingBox().inflate(12.0))
               .stream()
               .filter(entity -> entity.isAlive() && !entity.getItem().isEmpty())
               .min(java.util.Comparator.comparingDouble(EntityGolemBase.this::distanceToSqr))
               .orElse(null);
         }

         return this.target != null;
      }

      @Override
      public boolean canContinueToUse() {
         return getCore() == GolemCore.GATHER && !hasSomething() && this.target != null && this.target.isAlive();
      }

      @Override
      public void tick() {
         if (this.target == null) {
            return;
         }

         getLookControl().setLookAt(this.target, 10.0F, 10.0F);
         if (distanceToSqr(this.target) <= 4.0) {
            ItemStack stack = this.target.getItem().copy();
            if (inventory.addItem(stack)) {
               this.target.discard();
            }

            this.target = null;
         } else {
            getNavigation().moveTo(this.target, 1.0);
         }
      }

      @Override
      public void stop() {
         this.target = null;
         getNavigation().stop();
      }
   }

   /** Доставка содержимого инвентаря в маркированный контейнер (ядро GATHER). */
   private class GolemDeliverGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private BlockPos target;

      GolemDeliverGoal() {
         this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         if (getCore() != GolemCore.GATHER || !hasSomething()) {
            return false;
         }

         this.target = findMarkedContainer();
         return this.target != null;
      }

      @Override
      public boolean canContinueToUse() {
         return getCore() == GolemCore.GATHER && hasSomething() && this.target != null;
      }

      @Override
      public void tick() {
         if (this.target == null) {
            return;
         }

         double x = this.target.getX() + 0.5;
         double y = this.target.getY() + 0.5;
         double z = this.target.getZ() + 0.5;
         getLookControl().setLookAt(x, y, z, 10.0F, 10.0F);
         if (distanceToSqr(x, y, z) <= 6.25) {
            depositTo(this.target);
            if (!hasSomething()) {
               this.target = null;
            }
         } else {
            getNavigation().moveTo(x, y, z, 1.0);
         }
      }

      @Override
      public void stop() {
         this.target = null;
         getNavigation().stop();
      }
   }

   /** Первый маркер, за которым стоит контейнер с местом под наши предметы. */
   @Nullable
   private BlockPos findMarkedContainer() {
      if (this.level() == null) {
         return null;
      }

      for (Marker marker : this.getMarkers()) {
         if (this.level().getBlockEntity(marker.getPos()) instanceof net.minecraft.world.Container container
            && hasRoomFor(container)) {
            return marker.getPos();
         }
      }

      return null;
   }

   private boolean hasRoomFor(net.minecraft.world.Container container) {
      net.neoforged.neoforge.items.IItemHandler handler = new net.neoforged.neoforge.items.wrapper.InvWrapper(container);

      for (int slot = 0; slot < this.inventory.getSlots(); slot++) {
         ItemStack stack = this.inventory.getStackInSlot(slot);
         if (!stack.isEmpty()) {
            ItemStack leftover = net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(handler, stack.copy(), true);
            if (leftover.getCount() < stack.getCount()) {
               return true;
            }
         }
      }

      return false;
   }

   /** Выложить инвентарь в контейнер по позиции. */
   private void depositTo(BlockPos pos) {
      if (this.level() == null || !(this.level().getBlockEntity(pos) instanceof net.minecraft.world.Container container)) {
         return;
      }

      net.neoforged.neoforge.items.IItemHandler handler = new net.neoforged.neoforge.items.wrapper.InvWrapper(container);
      for (int slot = 0; slot < this.inventory.getSlots(); slot++) {
         ItemStack stack = this.inventory.getStackInSlot(slot);
         if (stack.isEmpty()) {
            continue;
         }

         ItemStack leftover = net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(handler, stack.copy(), false);
         this.inventory.setStackInSlot(slot, leftover.isEmpty() ? ItemStack.EMPTY : leftover);
      }

      container.setChanged();
   }


   // ------------------------------------------------------------------
   // дополнительные ядра (HARVEST / LUMBER / GUARD / BUTCHER)
   // ------------------------------------------------------------------

   /** Ядро HARVEST: ломает созревшие посевы (как AIHarvestCrops в 1.7.10). */
   private class GolemHarvestGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private BlockPos target;

      GolemHarvestGoal() {
         this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         if (getCore() != GolemCore.HARVEST) {
            return false;
         }

         if (this.target == null || !isMatureCrop(this.target)) {
            this.target = findNearbyBlock(8, EntityGolemBase.this::isMatureCrop);
         }

         return this.target != null;
      }

      @Override
      public boolean canContinueToUse() {
         return getCore() == GolemCore.HARVEST && this.target != null && isMatureCrop(this.target);
      }

      @Override
      public void tick() {
         if (this.target == null) {
            return;
         }

         double x = this.target.getX() + 0.5;
         double y = this.target.getY() + 0.5;
         double z = this.target.getZ() + 0.5;
         getLookControl().setLookAt(x, y, z, 10.0F, 10.0F);
         if (distanceToSqr(x, y, z) <= 6.25) {
            level().destroyBlock(this.target, true);
            this.target = null;
         } else {
            getNavigation().moveTo(x, y, z, 1.0);
         }
      }

      @Override
      public void stop() {
         this.target = null;
         getNavigation().stop();
      }
   }

   /** Ядро LUMBER: рубит брёвна (как AIHarvestLogs в 1.7.10). */
   private class GolemLumberGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private BlockPos target;

      GolemLumberGoal() {
         this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         if (getCore() != GolemCore.LUMBER) {
            return false;
         }

         if (this.target == null || !isLog(this.target)) {
            this.target = findNearbyBlock(8, EntityGolemBase.this::isLog);
         }

         return this.target != null;
      }

      @Override
      public boolean canContinueToUse() {
         return getCore() == GolemCore.LUMBER && this.target != null && isLog(this.target);
      }

      @Override
      public void tick() {
         if (this.target == null) {
            return;
         }

         double x = this.target.getX() + 0.5;
         double y = this.target.getY() + 0.5;
         double z = this.target.getZ() + 0.5;
         getLookControl().setLookAt(x, y, z, 10.0F, 10.0F);
         if (distanceToSqr(x, y, z) <= 6.25) {
            level().destroyBlock(this.target, true);
            this.target = null;
         } else {
            getNavigation().moveTo(x, y, z, 1.0);
         }
      }

      @Override
      public void stop() {
         this.target = null;
         getNavigation().stop();
      }
   }

   /** Ядра GUARD (бьёт монстров) и BUTCHER (бьёт животных). */
   private class GolemAttackGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private net.minecraft.world.entity.LivingEntity target;
      private int cooldown;

      GolemAttackGoal() {
         this.setFlags(
            java.util.EnumSet.of(
               net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE,
               net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK,
               net.minecraft.world.entity.ai.goal.Goal.Flag.TARGET
            )
         );
      }

      @Override
      public boolean canUse() {
         GolemCore core = getCore();
         if (core != GolemCore.GUARD && core != GolemCore.BUTCHER) {
            return false;
         }

         net.minecraft.world.entity.EntityType<?> wanted = core == GolemCore.GUARD
            ? null
            : net.minecraft.world.entity.EntityType.CHICKEN;
         java.util.List<net.minecraft.world.entity.LivingEntity> candidates = level()
            .getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, getBoundingBox().inflate(16.0))
            .stream()
            .filter(entity -> entity != EntityGolemBase.this && entity.isAlive())
            .filter(entity -> core == GolemCore.GUARD
               ? entity instanceof net.minecraft.world.entity.monster.Monster
               : (entity instanceof net.minecraft.world.entity.animal.Animal && !(entity instanceof net.minecraft.world.entity.animal.horse.AbstractHorse)))
            .toList();
         this.target = candidates.isEmpty()
            ? null
            : candidates.stream()
               .min(java.util.Comparator.comparingDouble(EntityGolemBase.this::distanceToSqr))
               .orElse(null);
         return this.target != null;
      }

      @Override
      public boolean canContinueToUse() {
         return this.target != null && this.target.isAlive() && this.cooldown <= 60;
      }

      @Override
      public void start() {
         this.cooldown = 0;
      }

      @Override
      public void tick() {
         if (this.target == null) {
            return;
         }

         getLookControl().setLookAt(this.target, 10.0F, 10.0F);
         if (distanceToSqr(this.target) > 4.0) {
            getNavigation().moveTo(this.target, 1.0);
            return;
         }

         if (this.cooldown > 0) {
            this.cooldown--;
            return;
         }

         doHurtTarget(this.target);
         this.cooldown = 20;
      }

      @Override
      public void stop() {
         this.target = null;
         getNavigation().stop();
      }
   }

   @Nullable
   private BlockPos findNearbyBlock(int radius, java.util.function.Predicate<BlockPos> predicate) {
      if (this.level() == null) {
         return null;
      }

      BlockPos origin = this.blockPosition();
      BlockPos best = null;
      double bestDistance = Double.MAX_VALUE;
      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -3, -radius), origin.offset(radius, 3, radius))) {
         if (predicate.test(pos)) {
            double distance = this.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distance < bestDistance) {
               bestDistance = distance;
               best = pos.immutable();
            }
         }
      }

      return best;
   }

   private boolean isMatureCrop(BlockPos pos) {
      if (this.level() == null) {
         return false;
      }

      net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(pos);
      return state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && crop.isMaxAge(state);
   }

   private boolean isLog(BlockPos pos) {
      return this.level() != null && this.level().getBlockState(pos).is(net.minecraft.tags.BlockTags.LOGS);
   }


   // ------------------------------------------------------------------
   // контейнерные ядра: EMPTY / FILL / SORTING (порт семантики 1.7.10)
   // «Дом» — первый маркер голема, остальные — цели/источники.
   // ------------------------------------------------------------------

   @Nullable
   private Marker homeMarker() {
      return this.getMarkers().isEmpty() ? null : this.getMarkers().get(0);
   }

   private boolean containerHasItems(BlockPos pos, @Nullable java.util.function.Predicate<ItemStack> filter) {
      if (this.level() == null || !(this.level().getBlockEntity(pos) instanceof net.minecraft.world.Container container)) {
         return false;
      }

      for (int slot = 0; slot < container.getContainerSize(); slot++) {
         ItemStack stack = container.getItem(slot);
         if (!stack.isEmpty() && (filter == null || filter.test(stack))) {
            return true;
         }
      }

      return false;
   }

   /** Забрать предметы из контейнера в инвентарь голема. */
   private boolean takeFrom(BlockPos pos, @Nullable java.util.function.Predicate<ItemStack> filter) {
      if (this.level() == null || !(this.level().getBlockEntity(pos) instanceof net.minecraft.world.Container container)) {
         return false;
      }

      for (int slot = 0; slot < container.getContainerSize(); slot++) {
         ItemStack stack = container.getItem(slot);
         if (stack.isEmpty() || (filter != null && !filter.test(stack))) {
            continue;
         }

         ItemStack copy = stack.copy();
         if (this.inventory.addItem(copy)) {
            container.setItem(slot, ItemStack.EMPTY);
            container.setChanged();
            return true;
         }

         if (copy.getCount() < stack.getCount()) {
            stack.shrink(stack.getCount() - copy.getCount());
            container.setItem(slot, stack);
            container.setChanged();
            return true;
         }
      }

      return false;
   }

   /** Ближайшая (по порядку меток) цель: источник предметов. */
   @Nullable
   private BlockPos findSourceMarker(boolean skipFirst) {
      java.util.List<Marker> markers = this.getMarkers();
      for (int i = skipFirst ? 1 : 0; i < markers.size(); i++) {
         BlockPos pos = markers.get(i).getPos();
         if (containerHasItems(pos, null)) {
            return pos;
         }
      }

      return null;
   }

   /** Ближайшая (по порядку меток) цель: контейнер с местом. */
   @Nullable
   private BlockPos findRoomMarker(boolean skipFirst) {
      java.util.List<Marker> markers = this.getMarkers();
      for (int i = skipFirst ? 1 : 0; i < markers.size(); i++) {
         BlockPos pos = markers.get(i).getPos();
         if (this.level() != null
            && this.level().getBlockEntity(pos) instanceof net.minecraft.world.Container container
            && hasRoomFor(container)) {
            return pos;
         }
      }

      return null;
   }

   /** EMPTY: из дома — в другую метку. */
   private class GolemEmptyGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private BlockPos target;
      private boolean delivering;

      GolemEmptyGoal() {
         this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         if (getCore() != GolemCore.EMPTY) {
            return false;
         }

         if (!hasSomething()) {
            this.delivering = false;
            this.target = findSourceMarker(false);
         } else {
            this.delivering = true;
            this.target = findRoomMarker(true);
            if (this.target == null && homeMarker() != null) {
               this.target = homeMarker().getPos();
            }
         }

         return this.target != null;
      }

      @Override
      public boolean canContinueToUse() {
         return getCore() == GolemCore.EMPTY && this.target != null;
      }

      @Override
      public void tick() {
         if (this.target == null) {
            return;
         }

         double x = this.target.getX() + 0.5;
         double y = this.target.getY() + 0.5;
         double z = this.target.getZ() + 0.5;
         getLookControl().setLookAt(x, y, z, 10.0F, 10.0F);
         if (distanceToSqr(x, y, z) > 6.25) {
            getNavigation().moveTo(x, y, z, 1.0);
            return;
         }

         if (this.delivering) {
            if (level().getBlockEntity(this.target) instanceof net.minecraft.world.Container) {
               depositTo(this.target);
            } else {
               dropInventory();
            }

            this.target = null;
         } else if (takeFrom(this.target, null)) {
            this.target = null;
         } else {
            this.target = null;
         }
      }

      @Override
      public void stop() {
         this.target = null;
         getNavigation().stop();
      }
   }

   /** FILL: из других меток — в дом. */
   private class GolemFillGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private BlockPos target;
      private boolean delivering;

      GolemFillGoal() {
         this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         if (getCore() != GolemCore.FILL) {
            return false;
         }

         if (!hasSomething()) {
            this.delivering = false;
            this.target = findSourceMarker(true);
         } else {
            this.delivering = true;
            if (homeMarker() != null) {
               this.target = homeMarker().getPos();
            } else {
               this.target = findRoomMarker(false);
            }
         }

         return this.target != null;
      }

      @Override
      public boolean canContinueToUse() {
         return getCore() == GolemCore.FILL && this.target != null;
      }

      @Override
      public void tick() {
         if (this.target == null) {
            return;
         }

         double x = this.target.getX() + 0.5;
         double y = this.target.getY() + 0.5;
         double z = this.target.getZ() + 0.5;
         getLookControl().setLookAt(x, y, z, 10.0F, 10.0F);
         if (distanceToSqr(x, y, z) > 6.25) {
            getNavigation().moveTo(x, y, z, 1.0);
            return;
         }

         if (this.delivering) {
            if (level().getBlockEntity(this.target) instanceof net.minecraft.world.Container) {
               depositTo(this.target);
            } else {
               dropInventory();
            }
         } else {
            takeFrom(this.target, null);
         }

         this.target = null;
      }

      @Override
      public void stop() {
         this.target = null;
         getNavigation().stop();
      }
   }

   /** SORTING: подбирает предметы и несёт туда, где такие уже лежат. */
   private class GolemSortingGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private net.minecraft.world.entity.item.ItemEntity pickup;
      @Nullable
      private BlockPos target;

      GolemSortingGoal() {
         this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         if (getCore() != GolemCore.SORTING) {
            return false;
         }

         if (!hasSomething()) {
            this.pickup = level()
               .getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, getBoundingBox().inflate(12.0))
               .stream()
               .filter(entity -> entity.isAlive() && !entity.getItem().isEmpty() && matchesAnyMarker(entity.getItem()))
               .min(java.util.Comparator.comparingDouble(EntityGolemBase.this::distanceToSqr))
               .orElse(null);
            return this.pickup != null;
         }

         this.target = findMarkerForCarried();
         return this.target != null;
      }

      @Override
      public boolean canContinueToUse() {
         return getCore() == GolemCore.SORTING && (this.pickup != null || this.target != null);
      }

      @Override
      public void tick() {
         if (this.pickup != null) {
            if (!this.pickup.isAlive()) {
               this.pickup = null;
               return;
            }

            if (distanceToSqr(this.pickup) <= 4.0) {
               ItemStack stack = this.pickup.getItem().copy();
               if (inventory.addItem(stack)) {
                  this.pickup.discard();
               }

               this.pickup = null;
            } else {
               getNavigation().moveTo(this.pickup, 1.0);
            }

            return;
         }

         if (this.target != null) {
            double x = this.target.getX() + 0.5;
            double y = this.target.getY() + 0.5;
            double z = this.target.getZ() + 0.5;
            if (distanceToSqr(x, y, z) > 6.25) {
               getNavigation().moveTo(x, y, z, 1.0);
            } else {
               depositTo(this.target);
               this.target = null;
            }
         }
      }

      @Override
      public void stop() {
         this.pickup = null;
         this.target = null;
         getNavigation().stop();
      }

      private boolean matchesAnyMarker(ItemStack stack) {
         for (Marker marker : getMarkers()) {
            if (containerHasItems(marker.getPos(), candidate -> ItemStack.isSameItem(candidate, stack))) {
               return true;
            }
         }

         return false;
      }

      @Nullable
      private BlockPos findMarkerForCarried() {
         for (Marker marker : getMarkers()) {
            if (!(level().getBlockEntity(marker.getPos()) instanceof net.minecraft.world.Container container)) {
               continue;
            }

            for (int slot = 0; slot < inventory.getSlots(); slot++) {
               ItemStack carried = inventory.getStackInSlot(slot);
               if (carried.isEmpty()) {
                  continue;
               }

               if (containerHasItems(marker.getPos(), candidate -> ItemStack.isSameItem(candidate, carried)) && hasRoomFor(container)) {
                  return marker.getPos();
               }
            }
         }

         return findRoomMarker(false);
      }
   }

   /** Выбросить инвентарь на землю (метка — не контейнер). */
   private void dropInventory() {
      for (int slot = 0; slot < this.inventory.getSlots(); slot++) {
         ItemStack stack = this.inventory.getStackInSlot(slot);
         if (!stack.isEmpty()) {
            this.spawnAtLocation(stack);
            this.inventory.setStackInSlot(slot, ItemStack.EMPTY);
         }
      }
   }


   /** Фейковый игрок для «использования предмета» (ядро USE). */
   public net.neoforged.neoforge.common.util.FakePlayer getFakePlayer() {
      if (this.fakePlayer == null && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
         this.fakePlayer = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(serverLevel);
      }

      return this.fakePlayer;
   }

   /** Ядро USE: применяет предмет из инвентаря к помеченному блоку (как ПКМ игрока). */
   private class GolemUseGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private BlockPos target;
      private int cooldown;

      GolemUseGoal() {
         this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         if (getCore() != GolemCore.USE) {
            return false;
         }

         this.target = findUseMarker();
         return this.target != null;
      }

      @Override
      public boolean canContinueToUse() {
         return getCore() == GolemCore.USE && this.target != null;
      }

      @Override
      public void tick() {
         if (this.target == null) {
            return;
         }

         if (!hasSomething()) {
            this.target = null;
            return;
         }

         double x = this.target.getX() + 0.5;
         double y = this.target.getY() + 0.5;
         double z = this.target.getZ() + 0.5;
         getLookControl().setLookAt(x, y, z, 10.0F, 10.0F);
         if (distanceToSqr(x, y, z) > 6.25) {
            getNavigation().moveTo(x, y, z, 1.0);
            return;
         }

         if (this.cooldown > 0) {
            this.cooldown--;
            return;
         }

         useCarriedOn(this.target);
         this.cooldown = 20;
      }

      @Override
      public void stop() {
         this.target = null;
         getNavigation().stop();
      }
   }

   /** Первый маркер, за которым нет контейнера (цель для использования предмета). */
   @Nullable
   private BlockPos findUseMarker() {
      if (this.level() == null) {
         return null;
      }

      for (Marker marker : this.getMarkers()) {
         if (!(this.level().getBlockEntity(marker.getPos()) instanceof net.minecraft.world.Container)) {
            return marker.getPos();
         }
      }

      return null;
   }

   /** Применить предмет из инвентаря к блоку через фейкового игрока. */
   private boolean useCarriedOn(BlockPos pos) {
      if (this.level() == null) {
         return false;
      }

      int slot = -1;
      ItemStack stack = ItemStack.EMPTY;
      for (int i = 0; i < this.inventory.getSlots(); i++) {
         if (!this.inventory.getStackInSlot(i).isEmpty()) {
            slot = i;
            stack = this.inventory.getStackInSlot(i).copy();
            break;
         }
      }

      if (slot < 0) {
         return false;
      }

      net.neoforged.neoforge.common.util.FakePlayer fake = getFakePlayer();
      if (fake == null) {
         return false;
      }

      fake.setPos(this.getX(), this.getY(), this.getZ());
      fake.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
      net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
         net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false
      );
      net.minecraft.world.InteractionResult result = stack.useOn(
         new net.minecraft.world.item.context.UseOnContext(fake, net.minecraft.world.InteractionHand.MAIN_HAND, hit)
      );
      if (result.consumesAction()) {
         ItemStack leftover = fake.getMainHandItem();
         this.inventory.setStackInSlot(slot, leftover.isEmpty() ? ItemStack.EMPTY : leftover.copy());
         return true;
      }

      return false;
   }


   /** Ядро ESSENCE: переносит эссенцию между помеченными банками/контейнерами. */
   private class GolemEssenceGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private BlockPos target;
      @Nullable
      private BlockPos sourcePos;
      @Nullable
      private BlockPos destPos;
      private boolean delivering;

      GolemEssenceGoal() {
         this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         if (getCore() != GolemCore.ESSENCE || level() == null) {
            return false;
         }

         if (essentia == null || essentiaAmount <= 0) {
            essentia = null;
            essentiaAmount = 0;
            this.delivering = false;
            this.destPos = null;
            this.sourcePos = null;
            this.target = findEssentiaSource();
         } else {
            this.delivering = true;
            if (this.destPos == null || !canAcceptAt(this.destPos, essentia)) {
               this.destPos = findEssentiaTarget(essentia, this.sourcePos);
            }

            this.target = this.destPos;
         }

         return this.target != null;
      }

      @Override
      public boolean canContinueToUse() {
         return getCore() == GolemCore.ESSENCE && this.target != null;
      }

      @Override
      public void tick() {
         if (this.target == null) {
            return;
         }

         double x = this.target.getX() + 0.5;
         double y = this.target.getY() + 0.5;
         double z = this.target.getZ() + 0.5;
         getLookControl().setLookAt(x, y, z, 10.0F, 10.0F);
         if (distanceToSqr(x, y, z) > 6.25) {
            getNavigation().moveTo(x, y, z, 1.0);
            return;
         }

         if (!(level().getBlockEntity(this.target) instanceof thaumcraft.api.aspects.IAspectContainer container)) {
            this.target = null;
            return;
         }

         if (this.delivering) {
            int leftover = container.addToContainer(essentia, essentiaAmount);
            essentiaAmount = leftover;
            if (essentiaAmount <= 0) {
               essentia = null;
               essentiaAmount = 0;
            }

            markContainerUpdated(this.target);
            this.destPos = null;
            this.target = null;
         } else {
            AspectList aspects = container.getAspects();
            if (aspects == null || aspects.size() == 0) {
               this.target = null;
               return;
            }

            Aspect aspect = null;
            int best = 0;
            for (Aspect candidate : aspects.getAspects()) {
               int amount = aspects.getAmount(candidate);
               if (amount > best) {
                  best = amount;
                  aspect = candidate;
               }
            }

            if (aspect != null) {
               int take = Math.max(1, Math.min(getCarryLimit(), best));
               if (container.takeFromContainer(aspect, take)) {
                  this.sourcePos = this.target;
                  essentia = aspect;
                  essentiaAmount = take;
                  markContainerUpdated(this.target);
                  this.destPos = findEssentiaTarget(aspect, this.sourcePos);
               }
            }

            this.target = null;
         }
      }

      @Override
      public void stop() {
         this.target = null;
         getNavigation().stop();
      }
   }

   private void markContainerUpdated(BlockPos pos) {
      if (level().getBlockEntity(pos) != null) {
         level().getBlockEntity(pos).setChanged();
      }
   }

   private boolean canAcceptAt(BlockPos pos, Aspect aspect) {
      return level().getBlockEntity(pos) instanceof thaumcraft.api.aspects.IAspectContainer container
         && canAccept(container, aspect);
   }

   /** Источник эссенции — контейнер у дома голема (первая метка), как в 1.7.10. */
   @Nullable
   private BlockPos findEssentiaSource() {
      if (this.level() == null) {
         return null;
      }

      Marker home = homeMarker();
      if (home == null) {
         return null;
      }

      if (this.level().getBlockEntity(home.getPos()) instanceof thaumcraft.api.aspects.IAspectContainer container
         && container.getAspects() != null
         && container.getAspects().size() > 0) {
         return home.getPos();
      }

      return null;
   }

   /** Помеченная банка, готовая принять аспект (кроме источника). */
   @Nullable
   private BlockPos findEssentiaTarget(Aspect aspect, @Nullable BlockPos exclude) {
      if (this.level() == null) {
         return null;
      }

      Marker home = homeMarker();

      for (Marker marker : this.getMarkers()) {
         if (exclude != null && marker.getPos().equals(exclude)) {
            continue;
         }

         if (home != null && marker.getPos().equals(home.getPos())) {
            continue;
         }

         if (canAcceptAt(marker.getPos(), aspect)) {
            return marker.getPos();
         }
      }

      return null;
   }

   /** Проверка «примет ли контейнер аспект» без порчи содержимого. */
   private static boolean canAccept(thaumcraft.api.aspects.IAspectContainer container, Aspect aspect) {
      if (container.addToContainer(aspect, 1) == 0) {
         container.takeFromContainer(aspect, 1);
         return true;
      }

      return false;
   }

   /** Ядро FISHING: голем ловит рыбу у дома (порт AIFish из 1.7.10, лут — ванильная таблица рыбалки). */
   private class GolemFishingGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private BlockPos waterPos;
      @Nullable
      private thaumcraft.entities.smart.golem.bobber.EntityGolemBobber bobber;
      private float quality;
      private int count = -1;
      private long nextCast;

      GolemFishingGoal() {
         this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         if (getCore() != GolemCore.FISHING || level() == null || level().isClientSide) {
            return false;
         }

         if (this.count > 0 || level().getGameTime() < this.nextCast || !getNavigation().isDone()) {
            return false;
         }

         if (this.bobber != null && !this.bobber.isRemoved()) {
            this.bobber.discard();
         }

         BlockPos water = this.findWater();
         if (water == null) {
            this.nextCast = level().getGameTime() + 60L;
            return false;
         }

         this.waterPos = water;
         double wx = water.getX() + 0.5;
         double wy = water.getY() + 1.0;
         double wz = water.getZ() + 0.5;
         getLookControl().setLookAt(wx, wy, wz, 30.0F, 30.0F);
         double dx = wx - getX();
         double dy = wy - getEyeY();
         double dz = wz - getZ();
         float yaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
         float pitch = (float)(-(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0 / Math.PI)));
         setYRot(yaw);
         setXRot(pitch);
         setYHeadRot(yaw);
         setYBodyRot(yaw);
         this.bobber = new thaumcraft.entities.smart.golem.bobber.EntityGolemBobber(
            thaumcraft.init.registry.ModEntities.GOLEM_BOBBER.get(), level(), EntityGolemBase.this
         );
         if (!level().addFreshEntity(this.bobber)) {
            this.bobber = null;
            this.waterPos = null;
            this.nextCast = level().getGameTime() + 60L;
            return false;
         }

         level().playSound(
            null, getX(), getY(), getZ(),
            net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW,
            net.minecraft.sounds.SoundSource.NEUTRAL,
            0.5F, 0.4F / (level().random.nextFloat() * 0.4F + 0.8F)
         );
         this.count = 300 + level().random.nextInt(200);
         return true;
      }

      @Override
      public boolean canContinueToUse() {
         if (getCore() != GolemCore.FISHING
            || this.waterPos == null
            || this.bobber == null
            || this.bobber.isRemoved()
            || this.count <= 0) {
            return false;
         }

         this.count--;
         return true;
      }

      @Override
      public void tick() {
         if (this.waterPos == null) {
            return;
         }

         double wx = this.waterPos.getX() + 0.5;
         double wy = this.waterPos.getY() + 1.0;
         double wz = this.waterPos.getZ() + 0.5;
         getLookControl().setLookAt(wx, wy, wz, 30.0F, 30.0F);
         float chance = this.quality + getGolemStrength() * 1.5E-4F;
         if (level().random.nextFloat() < chance) {
            this.catchLoot(wx, wy, wz);
         }
      }

      @Override
      public void start() {
         startRightArmTimer();
      }

      @Override
      public void stop() {
         if (this.bobber != null && !this.bobber.isRemoved()) {
            this.bobber.discard();
         }

         this.bobber = null;
         this.waterPos = null;
         this.count = -1;
         this.nextCast = level().getGameTime() + 20L + level().random.nextInt(40);
      }

      /** Подбор воды у дома и оценка «качества» места (как в 1.7.10). */
      @Nullable
      private BlockPos findWater() {
         Marker home = homeMarker();
         if (home == null || level() == null) {
            return null;
         }

         int range = Math.max(4, (int)(getRange() / 2.0F));
         for (int attempt = 0; attempt < 32; attempt++) {
            int rx = home.getX() + level().random.nextInt(range * 2 + 1) - range;
            int ry = home.getY() + level().random.nextInt(5) - 2;
            int rz = home.getZ() + level().random.nextInt(range * 2 + 1) - range;
            BlockPos pos = new BlockPos(rx, ry, rz);
            if (level().getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER) && level().isEmptyBlock(pos.above())) {
               this.quality = this.computeQuality(pos);
               return pos;
            }
         }

         return null;
      }

      private float computeQuality(BlockPos water) {
         float q = 0.0F;

         for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos side = water.relative(dir);
            if (level().getFluidState(side).is(net.minecraft.tags.FluidTags.WATER) && level().isEmptyBlock(side.above())) {
               q += 3.0E-5F;
               if (level().canSeeSky(side.above())) {
                  q += 3.0E-5F;
               }

               for (int depth = 1; depth <= 3; depth++) {
                  if (level().getFluidState(side.below(depth)).is(net.minecraft.tags.FluidTags.WATER)) {
                     q += 1.5E-5F;
                  }
               }
            }
         }

         return q;
      }

      private void catchLoot(double wx, double wy, double wz) {
         int qty = 1;
         int air = getUpgradeAmount(GolemUpgrade.AIR.ordinal());
         if (air > 0 && level().random.nextInt(10) < air) {
            qty++;
         }

         for (int i = 0; i < qty; i++) {
            ItemStack stack = this.getFishingResult(wx, wy, wz);
            if (stack.isEmpty()) {
               continue;
            }

            if (getUpgradeAmount(GolemUpgrade.FIRE.ordinal()) > 0) {
               var recipe = level()
                  .getRecipeManager()
                  .getRecipeFor(
                     net.minecraft.world.item.crafting.RecipeType.SMELTING,
                     new net.minecraft.world.item.crafting.SingleRecipeInput(stack),
                     level()
                  );
               if (recipe.isPresent()) {
                  stack = recipe.get().value().getResultItem(level().registryAccess()).copy();
               }
            }

            net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(
               level(), wx, wy, wz, stack
            );
            item.setPickUpDelay(20);
            double dx = getX() - wx;
            double dy = getY() + 1.0 - wy;
            double dz = getZ() - wz;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double speed = 0.1;
            item.setDeltaMovement(dx * speed, dy * speed + Math.sqrt(Math.max(0.0, dist)) * 0.08, dz * speed);
            level().addFreshEntity(item);
         }

         if (this.bobber != null) {
            level().playSound(
               null, this.bobber.getX(), this.bobber.getY(), this.bobber.getZ(),
               net.minecraft.sounds.SoundEvents.FISHING_BOBBER_SPLASH,
               net.minecraft.sounds.SoundSource.NEUTRAL,
               0.15F, 1.0F + (level().random.nextFloat() - level().random.nextFloat()) * 0.4F
            );
            this.bobber.discard();
         }

         this.count = -1;
      }

      private ItemStack getFishingResult(double wx, double wy, double wz) {
         net.minecraft.server.MinecraftServer server = level().getServer();
         if (server == null) {
            return ItemStack.EMPTY;
         }

         var table = server.reloadableRegistries()
            .getLootTable(net.minecraft.world.level.storage.loot.BuiltInLootTables.FISHING);
         float luck = getUpgradeAmount(GolemUpgrade.ORDER.ordinal()) * 0.5F
            - getUpgradeAmount(GolemUpgrade.ENTROPY.ordinal()) * 0.25F;
         var params = new net.minecraft.world.level.storage.loot.LootParams.Builder((ServerLevel)level())
            .withParameter(
               net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, new Vec3(wx, wy, wz)
            )
            .withParameter(
               net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, ItemStack.EMPTY
            )
            .withParameter(
               net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY,
               this.bobber != null ? this.bobber : EntityGolemBase.this
            )
            .withLuck(luck)
            .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.FISHING);
         var loot = table.getRandomItems(params);
         return loot.isEmpty() ? ItemStack.EMPTY : loot.get(0).copy();
      }
   }

   /** Ядро LIQUID: переносит жидкость из резервуара у дома в помеченные резервуары. */
   private class GolemLiquidGoal extends net.minecraft.world.entity.ai.goal.Goal {
      @Nullable
      private BlockPos target;
      @Nullable
      private BlockPos destPos;
      private boolean delivering;

      GolemLiquidGoal() {
         this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         if (getCore() != GolemCore.LIQUID || level() == null) {
            return false;
         }

         if (!fluidHandler.hasFluid()) {
            this.delivering = false;
            this.destPos = null;
            this.target = findLiquidSource();
         } else {
            this.delivering = true;
            if (this.destPos == null || !canFillAt(this.destPos, fluidHandler.getCarriedFluid())) {
               this.destPos = findLiquidTarget(fluidHandler.getCarriedFluid());
            }

            this.target = this.destPos;
         }

         return this.target != null;
      }

      @Override
      public boolean canContinueToUse() {
         return getCore() == GolemCore.LIQUID && this.target != null;
      }

      @Override
      public void tick() {
         if (this.target == null) {
            return;
         }

         double x = this.target.getX() + 0.5;
         double y = this.target.getY() + 0.5;
         double z = this.target.getZ() + 0.5;
         getLookControl().setLookAt(x, y, z, 10.0F, 10.0F);
         if (distanceToSqr(x, y, z) > 6.25) {
            getNavigation().moveTo(x, y, z, 1.0);
            return;
         }

         IFluidHandler handler = fluidAt(this.target);
         if (handler == null) {
            this.target = null;
            return;
         }

         if (this.delivering) {
            FluidStack carried = fluidHandler.getCarriedFluid();
            int accepted = handler.fill(carried.copy(), IFluidHandler.FluidAction.EXECUTE);
            if (accepted > 0) {
               fluidHandler.drain(carried.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
            }

            if (!fluidHandler.hasFluid()) {
               this.destPos = null;
            }

            this.target = null;
         } else {
            int room = fluidHandler.getTankCapacity(0) - fluidHandler.getFluidInTank(0).getAmount();
            if (room <= 0) {
               this.target = null;
               return;
            }

            FluidStack drained = handler.drain(room, IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
               fluidHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            }

            this.target = null;
         }
      }

      @Override
      public void stop() {
         this.target = null;
         getNavigation().stop();
      }
   }

   @Nullable
   private IFluidHandler fluidAt(BlockPos pos) {
      if (this.level() == null) {
         return null;
      }

      IFluidHandler handler = this.level()
         .getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, pos, null);
      if (handler != null) {
         return handler;
      }

      return this.level().getBlockEntity(pos) instanceof IFluidHandler fluid ? fluid : null;
   }

   /** Источник жидкости — резервуар у дома голема. */
   @Nullable
   private BlockPos findLiquidSource() {
      Marker home = homeMarker();
      if (home == null) {
         return null;
      }

      IFluidHandler handler = fluidAt(home.getPos());
      if (handler != null && !handler.drain(1, IFluidHandler.FluidAction.SIMULATE).isEmpty()) {
         return home.getPos();
      }

      return null;
   }

   /** Помеченный резервуар, принимающий данную жидкость. */
   @Nullable
   private BlockPos findLiquidTarget(FluidStack fluid) {
      if (this.level() == null) {
         return null;
      }

      Marker home = homeMarker();

      for (Marker marker : this.getMarkers()) {
         if (home != null && marker.getPos().equals(home.getPos())) {
            continue;
         }

         if (canFillAt(marker.getPos(), fluid)) {
            return marker.getPos();
         }
      }

      return null;
   }

   private boolean canFillAt(BlockPos pos, FluidStack fluid) {
      IFluidHandler handler = fluidAt(pos);
      return handler != null && handler.fill(fluid.copy(), IFluidHandler.FluidAction.SIMULATE) > 0;
   }

   /** Отладка: имена активных AI-целей. */
   public String debugGoals() {
      StringBuilder sb = new StringBuilder();

      for (net.minecraft.world.entity.ai.goal.WrappedGoal wrapped : this.goalSelector.getAvailableGoals()) {
         if (!wrapped.isRunning()) {
            continue;
         }

         if (sb.length() > 0) {
            sb.append(',');
         }

         sb.append(wrapped.getGoal().getClass().getSimpleName());
      }

      return sb.toString();
   }

   @NotNull
   public Component getDisplayName() {
      return Component.translatable("entity.thaumcraft.golem");
   }

   @Nullable
   @Override
   public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
      int containerId, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.entity.player.Player player
   ) {
      return new GolemMenu(containerId, inventory, this);
   }
}