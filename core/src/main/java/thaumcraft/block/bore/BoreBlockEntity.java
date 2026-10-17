package thaumcraft.block.bore;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IRepairable;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blockentity.ThaumcraftInvBlockEntity;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.IWandable;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModEnchantment;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.equipment.ItemElementalPickaxe;
import thaumcraft.item.wands.foci.ItemFocusExcavation;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.misc.PacketBoreDig;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.research.ResearchManager;
import thaumcraft.util.Utils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BoreBlockEntity extends ThaumcraftInvBlockEntity implements IWandable, MenuProvider, ITickable {
   public int rotX = 0;
   public int rotZ = 0;
   public int tarX = 0;
   public int tarZ = 0;
   public int speedX = 0;
   public int speedZ = 0;
   public boolean hasFocus = false;
   public boolean hasPickaxe = false;
   public int fortune;
   public int speed;
   public int area;
   BlockPos digPos = BlockPos.ZERO;
   boolean toDig = false;
   int paused = 100;
   int maxPause = 100;
   private int count = 0;
   private int lastX = 0;
   private int lastZ = 0;
   private int lastY = 0;
   private int spiral = 0;
   private float currentRadius = 0.0F;
   public int maxRadius = 2;
   private float radInc = 0.0F;
   private long repairCounter = 0L;
   private AspectList repairCost = new AspectList();
   private final AspectList currentRepairVis = new AspectList();
   private int blockCount = 0;
   private float speedyTime = 0.0F;
   BoreBaseBlockEntity base = null;
   public float vRadX = 0.0F;
   public float vRadZ = 0.0F;
   public float tRadX = 0.0F;
   public float tRadZ = 0.0F;
   public float mRadX = 0.0F;
   public float mRadZ = 0.0F;
   public int topRotation = 0;
   private long soundDelay = 0L;
   private Object beam1 = null;
   private Object beam2 = null;
   private int beamlength = 0;
   private BlockState digBlock = Blocks.AIR.defaultBlockState();
   private int clientDigFxCooldown = 0;
   private Direction orientation = Direction.UP;
   private int postDigDelay = 0;

   public BoreBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ARCANE_BORE.get(), blockPos, blockState, 2);
      this.orientation = inferInitialOrientation(blockState);
   }

   private static Direction inferInitialOrientation(BlockState state) {
      return state.hasProperty(BlockStateProperties.VERTICAL_DIRECTION) ? (Direction)state.getValue(BlockStateProperties.VERTICAL_DIRECTION) : Direction.UP;
   }

   private Direction getBaseOrientation() {
      BlockState st = this.getBlockState();
      if (st.hasProperty(BlockStateProperties.VERTICAL_DIRECTION)) {
         return (Direction)st.getValue(BlockStateProperties.VERTICAL_DIRECTION);
      } else {
         if (st.hasProperty(BlockStateProperties.FACING)) {
            Direction f = (Direction)st.getValue(BlockStateProperties.FACING);
            if (f.getAxis().isVertical()) {
               return f;
            }
         }

         return Direction.UP;
      }
   }

   private void updateRotationLogic() {
      if (this.rotX < this.tarX) {
         this.rotX = this.rotX + this.speedX;
         if (this.rotX < this.tarX) {
            this.speedX++;
         } else {
            this.speedX = (int)(this.speedX / 3.0F);
         }
      } else if (this.rotX > this.tarX) {
         this.rotX = this.rotX + this.speedX;
         if (this.rotX > this.tarX) {
            this.speedX--;
         } else {
            this.speedX = (int)(this.speedX / 3.0F);
         }
      } else {
         this.speedX = 0;
      }

      if (this.rotZ < this.tarZ) {
         this.rotZ = this.rotZ + this.speedZ;
         if (this.rotZ < this.tarZ) {
            this.speedZ++;
         } else {
            this.speedZ = (int)(this.speedZ / 3.0F);
         }
      } else if (this.rotZ > this.tarZ) {
         this.rotZ = this.rotZ + this.speedZ;
         if (this.rotZ > this.tarZ) {
            this.speedZ--;
         } else {
            this.speedZ = (int)(this.speedZ / 3.0F);
         }
      } else {
         this.speedZ = 0;
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (this.validateBaseClient()) {
         this.updateRotationLogic();
         if (this.clientDigFxCooldown > 0) {
            this.clientDigFxCooldown--;
         }

         if (this.gettingPower() && this.areItemsValid()) {
            this.clientDigVisualTick();
         } else {
            this.clientIdleVisualTick();
         }
      }
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.speedyTime < 20.0F) {
         this.speedyTime += (float)VisNetHandler.drainVis(level, pos, Aspect.ENTROPY, 100) / 5.0F;
         if (this.speedyTime < 20.0F && this.validateBaseServer() && this.base.drawEssentia()) {
            this.speedyTime += 20.0F;
         }
      }

      this.updateRotationLogic();

      if (this.gettingPower() && this.areItemsValid()) {
         this.dig();
      }

      if (this.hasPickaxe && !this.getItem(1).isEmpty()) {
         ItemStack pick = this.getItem(1);
         if (this.repairCounter++ % 40L == 0L && pick.isDamaged()) {
            this.doRepair(pick);
         }

         if (this.repairCost != null && this.repairCost.size() > 0 && this.repairCounter % 5L == 0L) {
            for (Aspect a : this.repairCost.getAspects()) {
               if (this.currentRepairVis.getAmount(a) < this.repairCost.getAmount(a)) {
                  this.currentRepairVis.add(a, VisNetHandler.drainVis(level, pos, a, this.repairCost.getAmount(a)));
               }
            }
         }
      }
   }

   private boolean validateBaseServer() {
      Direction baseOr = this.getBaseOrientation();
      BlockPos basePos = this.worldPosition.relative(baseOr);
      if (this.base == null || this.base.isRemoved() || !this.base.getBlockPos().equals(basePos)) {
         if (this.level.getBlockEntity(basePos) instanceof BoreBaseBlockEntity boreBase) {
            this.base = boreBase;
         } else {
            this.base = null;
            return false;
         }
      }
      return true;
   }

   private boolean validateBaseClient() {
      Direction baseOr = this.getBaseOrientation();
      BlockPos basePos = this.worldPosition.relative(baseOr);
      if (!this.level.isLoaded(basePos)) {
         return false;
      } else {
         if (this.base == null || this.base.isRemoved() || !this.base.getBlockPos().equals(basePos)) {
            if (!(this.level.getBlockEntity(basePos) instanceof BoreBaseBlockEntity boreBase)) {
               this.base = null;
               return false;
            }

            this.base = boreBase;
         }

         return true;
      }
   }

   private void clientIdleVisualTick() {
      if (this.topRotation % 90 != 0) {
         this.topRotation = this.topRotation + Math.min(10, 90 - this.topRotation % 90);
      }

      this.vRadX *= 0.9F;
      this.vRadZ *= 0.9F;
   }

   private void clientDigVisualTick() {
      if (this.rotX == this.tarX && this.rotZ == this.tarZ) {
         this.paused++;
         if (this.paused < this.maxPause && this.soundDelay < System.currentTimeMillis()) {
            this.soundDelay = System.currentTimeMillis() + 1200L + this.level.random.nextInt(100);
            this.level
               .playLocalSound(
                  this.worldPosition.getX() + 0.5,
                  this.worldPosition.getY() + 0.5,
                  this.worldPosition.getZ() + 0.5,
                  (SoundEvent)ModSounds.RUMBLE.get(),
                  SoundSource.BLOCKS,
                  0.25F,
                  0.9F + this.level.random.nextFloat() * 0.2F,
                  false
               );
         }

         if (this.beamlength > 0 && this.paused > this.maxPause) {
            this.beamlength--;
         }

         if (this.toDig) {
            this.paused = 0;
            this.beamlength = 64;
            BlockState st = this.level.getBlockState(this.digPos);
            if (!st.isAir()) {
               this.maxPause = 10 + Math.max(10 - this.speed, (int)(st.getDestroySpeed(this.level, this.digPos) * 2.0F) - this.speed * 2);
            } else {
               this.maxPause = 20;
            }

            if (this.speedyTime <= 0.0F) {
               this.maxPause *= 4;
            }

            this.toDig = false;
            this.calculateClientTargetRotation();
            if (this.speedyTime > 0.0F) {
               this.speedyTime--;
            }
         }

         if (this.paused < this.maxPause) {
            if (this.vRadX < this.tRadX) {
               this.vRadX = this.vRadX + this.mRadX;
            } else if (this.vRadX > this.tRadX) {
               this.vRadX = this.vRadX - this.mRadX;
            }

            if (this.vRadZ < this.tRadZ) {
               this.vRadZ = this.vRadZ + this.mRadZ;
            } else if (this.vRadZ > this.tRadZ) {
               this.vRadZ = this.vRadZ - this.mRadZ;
            }
         } else {
            this.vRadX *= 0.9F;
            this.vRadZ *= 0.9F;
         }

         this.mRadX *= 0.9F;
         this.mRadZ *= 0.9F;
         this.performClientBeamRaycast();
      } else {
         if (this.topRotation % 90 != 0) {
            this.topRotation = this.topRotation + Math.min(10, 90 - this.topRotation % 90);
         }

         this.vRadX *= 0.9F;
         this.vRadZ *= 0.9F;
      }
   }

   private void calculateClientTargetRotation() {
      Vec3 vec3 = this.worldPosition.getCenter().subtract(this.digPos.getCenter());
      float degX = (float)(Math.atan2(vec3.z, vec3.x) * 180.0 / Math.PI);
      float degY = (float)(-(Math.atan2(vec3.y, vec3.length()) * 180.0 / Math.PI)) + 90.0F;
      this.tRadX = Mth.wrapDegrees(this.rotX) + degX;
      if (this.orientation.ordinal() == 5) {
         if (this.tRadX > 180.0F) {
            this.tRadX -= 360.0F;
         }

         if (this.tRadX < -180.0F) {
            this.tRadX += 360.0F;
         }
      }

      this.tRadZ = degY - this.rotZ;
      if (this.orientation.ordinal() <= 1) {
         this.tRadZ += 180.0F;
         if (this.vRadX - this.tRadX >= 180.0F) {
            this.vRadX -= 360.0F;
         }

         if (this.vRadX - this.tRadX <= -180.0F) {
            this.vRadX += 360.0F;
         }
      }

      this.mRadX = Math.abs((this.vRadX - this.tRadX) / 6.0F);
      this.mRadZ = Math.abs((this.vRadZ - this.tRadZ) / 6.0F);
   }

   private void performClientBeamRaycast() {
      float rotXCalc = this.rotX + 90 - this.vRadX;
      float rotZCalc = this.rotZ + 90 - this.vRadZ;
      float sinRotX = Mth.sin(rotXCalc / 180.0F * (float) Math.PI);
      float cosRotX = Mth.cos(rotXCalc / 180.0F * (float) Math.PI);
      float sinRotZ = Mth.sin(rotZCalc / 180.0F * (float) Math.PI);
      float cosRotZ = Mth.cos(rotZCalc / 180.0F * (float) Math.PI);
      float dx = sinRotX * cosRotZ;
      float dz = cosRotX * cosRotZ;
      Vec3 start = this.worldPosition.getCenter();
      Vec3 offset = start.add(dx, sinRotZ, dz);
      Vec3 end = start.add(dx * this.beamlength, sinRotZ * this.beamlength, dz * this.beamlength);
      BlockHitResult hitResult = this.level.clip(new ClipContext(offset, end, Block.COLLIDER, Fluid.NONE, CollisionContext.empty()));
      byte beamState = 0;
      double tx = end.x;
      double ty = end.y;
      double tz = end.z;
      if (hitResult.getType() == Type.BLOCK) {
         Vec3 location = hitResult.getLocation();
         tx = location.x;
         ty = location.y;
         tz = location.z;
         beamState = 5;
         BlockPos hitPos = hitResult.getBlockPos();
         BlockState hitState = this.level.getBlockState(hitPos);
         if (!hitState.isAir()) {
            BlockPos p = this.worldPosition.relative(this.getFacing());
            this.maybeSpawnContinuousDigFx(hitPos, p, hitState);
         }
      }

      this.topRotation = this.topRotation + this.beamlength / 6;
      this.beam1 = Thaumcraft.setup
         .beamBore(this.level, start.x, start.y, start.z, tx, ty, tz, 1, 166, true, beamState > 0 ? 2.0F : 0.0F, this.beam1, beamState);
      this.beam2 = Thaumcraft.setup
         .beamBore(this.level, start.x, start.y, start.z, tx, ty, tz, 2, 16746581, false, beamState > 0 ? 2.0F : 0.0F, this.beam2, beamState);
      if (this.level.isEmptyBlock(this.digPos) && !this.digBlock.isAir()) {
         this.playClientBreakEffects();
         this.digBlock = Blocks.AIR.defaultBlockState();
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void maybeSpawnContinuousDigFx(BlockPos hitPos, BlockPos boreTipPos, BlockState hitState) {
      int particleLevel = Thaumcraft.setup.particleCount(1);
      if (particleLevel > 0 && this.clientDigFxCooldown <= 0) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null) {
            double maxDistance = particleLevel > 1 ? 32.0 : 20.0;
            if (mc.player.distanceToSqr(hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5) > maxDistance * maxDistance) {
               return;
            }
         }

         int minDelay = particleLevel > 1 ? 3 : 6;
         this.clientDigFxCooldown = minDelay + this.level.random.nextInt(2);
         Thaumcraft.setup
            .boreDigFx(this.level, hitPos.getX(), hitPos.getY(), hitPos.getZ(), boreTipPos.getX(), boreTipPos.getY(), boreTipPos.getZ(), hitState.getBlock(), 0);
      }
   }

   private void playClientBreakEffects() {
      SoundType soundType = this.digBlock.getSoundType();
      this.level
         .playLocalSound(
            this.digPos.getX() + 0.5F,
            this.digPos.getY() + 0.5F,
            this.digPos.getZ() + 0.5F,
            soundType.getBreakSound(),
            SoundSource.BLOCKS,
            (soundType.getVolume() + 1.0F) / 2.0F,
            soundType.getPitch() * 0.8F,
            false
         );
      BlockPos p = this.worldPosition.relative(this.getFacing());

      for (int i = 0; i < Thaumcraft.setup.particleCount(10); i++) {
         Thaumcraft.setup
            .boreDigFx(this.level, this.digPos.getX(), this.digPos.getY(), this.digPos.getZ(), p.getX(), p.getY(), p.getZ(), this.digBlock.getBlock(), 0);
      }
   }

   private Holder<Enchantment> enchantmentHolder(ResourceKey<Enchantment> key) {
      return this.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
   }

   private boolean areItemsValid() {
      if (this.hasFocus && this.hasPickaxe) {
         ItemStack stack = this.getItem(1);
         return stack.isDamageableItem() && stack.getDamageValue() + 1 < stack.getMaxDamage();
      } else {
         return false;
      }
   }

   private void doRepair(ItemStack is) {
      Holder<Enchantment> repairHolder = (Holder<Enchantment>)this.level.registryAccess()
         .lookupOrThrow(Registries.ENCHANTMENT)
         .get(ModEnchantment.REPAIR.getKey())
         .orElse(null);
      int level = repairHolder == null ? 0 : EnchantmentHelper.getTagEnchantmentLevel(repairHolder, is);
      if (level > 0) {
         if (level > 2) {
            level = 2;
         }

         if (is.getItem() instanceof IRepairable) {
            AspectList cost = ThaumcraftCraftingManager.getObjectTags(is);
            if (cost == null || cost.size() == 0) {
               return;
            }

            cost = ResearchManager.reduceToPrimals(cost);

            for (Aspect a : cost.getAspects()) {
               if (a != null) {
                  this.repairCost.merge(a, (int)Math.sqrt((double)(cost.getAmount(a) * 2)) * level);
               }
            }

            boolean doIt = true;
            for (Aspect ax : this.repairCost.getAspects()) {
               if (this.currentRepairVis.getAmount(ax) < this.repairCost.getAmount(ax)) {
                  doIt = false;
                  break;
               }
            }

            if (doIt) {
               for (Aspect axx : this.repairCost.getAspects()) {
                  this.currentRepairVis.reduce(axx, this.repairCost.getAmount(axx));
               }

               is.setDamageValue(Math.max(0, is.getDamageValue() - level));
               this.setChanged();
            }
         } else {
            this.repairCost = new AspectList();
         }
      }
   }

   private void dig() {
      if (this.rotX == this.tarX && this.rotZ == this.tarZ) {
         if (!this.validateBaseServer()) {
            return;
         }

         if (--this.count > 0) {
            return;
         }

         if (this.toDig) {
            this.toDig = false;
            BlockState bi = this.level.getBlockState(this.digPos);
            if (!bi.isAir()) {
               int tfortune = this.fortune;
               boolean silktouch = false;
               ItemStack pick = this.getItem(1);
               if (!pick.isEmpty()) {
                  Holder<Enchantment> silk = this.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
                  if (EnchantmentHelper.getTagEnchantmentLevel(silk, pick) > 0) {
                     silktouch = true;
                     tfortune = 0;
                  }
               }
               ItemStack focusStack = this.getItem(0);
               if (!silktouch && focusStack.getItem() instanceof ItemFocusExcavation focus && focus.isUpgradedWith(focusStack, FocusUpgradeType.silktouch)) {
                  silktouch = true;
                  tfortune = 0;
               }

               List<ItemStack> items = new ArrayList<>();
               if (this.level instanceof ServerLevel serverLevel) {
                  LootParams.Builder builder = new LootParams.Builder(serverLevel)
                     .withParameter(LootContextParams.ORIGIN, this.digPos.getCenter())
                     .withParameter(LootContextParams.TOOL, pick)
                     .withOptionalParameter(LootContextParams.BLOCK_ENTITY, this.level.getBlockEntity(this.digPos));
                  items = new ArrayList<>(bi.getDrops(builder));
               }

               AABB aabb = new AABB(this.digPos).inflate(1.0);
               for (ItemEntity e : this.level.getEntitiesOfClass(ItemEntity.class, aabb)) {
                  items.add(e.getItem().copy());
                  e.discard();
               }

               Direction baseFacing = (Direction)this.base.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
               BlockPos invPos = this.base.getBlockPos().relative(baseFacing);
               var itemHandler = this.level.getCapability(Capabilities.ItemHandler.BLOCK, invPos, baseFacing.getOpposite());

               for (ItemStack is : items) {
                  ItemStack dropped = is.copy();
                  if (!silktouch && ((pick.getItem() instanceof ItemElementalPickaxe) || (focusStack.getItem() instanceof ItemFocusExcavation f && f.isUpgradedWith(focusStack, ItemFocusExcavation.dowsing)))) {
                     dropped = Utils.findSpecialMiningResult(is, 0.2F + (float)tfortune * 0.075F, this.level.random);
                  }

                  if (itemHandler != null) {
                     dropped = ItemHandlerHelper.insertItemStacked(itemHandler, dropped, false);
                  }

                  if (!dropped.isEmpty()) {
                     double px = (double)this.base.getBlockPos().getX() + 0.5 + (double)baseFacing.getStepX() * 0.66;
                     double py = (double)this.base.getBlockPos().getY() + 0.4;
                     double pz = (double)this.base.getBlockPos().getZ() + 0.5 + (double)baseFacing.getStepZ() * 0.66;
                     ItemEntity ei = new ItemEntity(this.level, px, py, pz, dropped);
                     ei.setDeltaMovement((double)(0.075F * (float)baseFacing.getStepX()), 0.025, (double)(0.075F * (float)baseFacing.getStepZ()));
                     this.level.addFreshEntity(ei);
                  }
               }

               if (!pick.isEmpty()) {
                  pick.setDamageValue(pick.getDamageValue() + 1);
                  if (pick.getDamageValue() >= pick.getMaxDamage()) {
                     this.setItem(1, ItemStack.EMPTY);
                  }
               }

               this.level.destroyBlock(this.digPos, false);
               if (this.speedyTime > 0.0F) {
                  this.speedyTime--;
               }
            }

            this.findNextBlockToDig();
         }
      }
   }

   private void findNextBlockToDig() {
      if (this.radInc == 0.0F) {
         this.radInc = (float)(this.maxRadius + this.area) / 360.0F;
      }

      int x = this.lastX;
      int z = this.lastZ;
      int y = this.lastY;

      while (x == this.lastX && z == this.lastZ && y == this.lastY) {
         this.spiral += 2;
         if (this.spiral >= 360) {
            this.spiral -= 360;
         }

         this.currentRadius = this.currentRadius + this.radInc;
         if (this.currentRadius > (float)(this.maxRadius + this.area) || this.currentRadius < (float)(-(this.maxRadius + this.area))) {
            this.radInc *= -1.0F;
         }

         Vec3 vsource = new Vec3(
            (double)(this.worldPosition.getX() + this.orientation.getStepX()) + 0.5,
            (double)(this.worldPosition.getY() + this.orientation.getStepY()) + 0.5,
            (double)(this.worldPosition.getZ() + this.orientation.getStepZ()) + 0.5
         );
         Vec3 vtar = new Vec3(0.0, (double)this.currentRadius, 0.0);
         vtar = vtar.zRot((float)this.spiral / 180.0F * (float) Math.PI);
         vtar = vtar.yRot((float) (Math.PI / 2) * (float)this.orientation.getStepX());
         vtar = vtar.xRot((float) (Math.PI / 2) * (float)this.orientation.getStepY());
         Vec3 vres = vsource.add(vtar.x, vtar.y, vtar.z);
         x = Mth.floor(vres.x);
         y = Mth.floor(vres.y);
         z = Mth.floor(vres.z);
      }

      this.lastX = x;
      this.lastZ = z;
      this.lastY = y;
      x += this.orientation.getStepX();
      y += this.orientation.getStepY();
      z += this.orientation.getStepZ();

      for (int depth = 0; depth < 64; depth++) {
         x += this.orientation.getStepX();
         y += this.orientation.getStepY();
         z += this.orientation.getStepZ();
         BlockPos checkPos = new BlockPos(x, y, z);
         BlockState blockState = this.level.getBlockState(checkPos);
         if (blockState.getDestroySpeed(this.level, checkPos) < 0.0F) {
            break;
         }

         if (!this.level.isEmptyBlock(checkPos) && !blockState.isAir() && blockState.isSolid()) {
            this.digPos = checkPos;
            if (++this.blockCount > 2) {
               this.blockCount = 0;
            }

            this.count = Math.max(10 - this.speed, (int)(blockState.getDestroySpeed(this.level, checkPos) * 2.0F) - this.speed * 2);
            if (this.speedyTime < 1.0F) {
               this.count *= 4;
            }

            this.toDig = true;
            Vec3 start = this.worldPosition.getCenter().add(this.orientation.getStepX(), this.orientation.getStepY(), this.orientation.getStepZ());
            Vec3 end = checkPos.getCenter();
            BlockHitResult hit = this.level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, CollisionContext.empty()));
            if (hit.getType() == Type.BLOCK) {
               BlockPos hitPos = hit.getBlockPos();
               BlockState hitState = this.level.getBlockState(hitPos);
               if (hitState.getDestroySpeed(this.level, hitPos) >= 0.0F && hitState.isSolid()) {
                  this.count = Math.max(10 - this.speed, (int)(hitState.getDestroySpeed(this.level, hitPos) * 2.0F) - this.speed * 2);
                  if (this.speedyTime < 1.0F) {
                     this.count *= 4;
                  }
                  this.digPos = hitPos;
               }
            }

            this.sendDigEvent();
            break;
         }
      }
   }

   public void sendDigEvent() {
      int x = this.digPos.getX() - this.worldPosition.getX() + 64;
      int y = this.digPos.getY() - this.worldPosition.getY() + 64;
      int z = this.digPos.getZ() - this.worldPosition.getZ() + 64;
      int c = (x & 0xFF) << 16 | (y & 0xFF) << 8 | z & 0xFF;
      PacketHandler.sendToAllAround(new PacketBoreDig(this.worldPosition, c, this.speedyTime), this.level, this.worldPosition, 64.0);
   }

   private void recalculateItems() {
      this.fortune = 0;
      this.area = 0;
      this.speed = 0;
      ItemStack stack0 = this.getItem(0);
      if (stack0.getItem() instanceof ItemFocusExcavation focus) {
         this.fortune = focus.getUpgradeLevel(stack0, FocusUpgradeType.treasure);
         this.area = focus.getUpgradeLevel(stack0, FocusUpgradeType.enlarge);
         this.speed = this.speed + focus.getUpgradeLevel(stack0, FocusUpgradeType.potency);
         this.hasFocus = true;
      } else {
         this.hasFocus = false;
      }

      ItemStack stack1 = this.getItem(1);
      if (stack1.getItem() instanceof PickaxeItem) {
         this.hasPickaxe = true;
         int f = EnchantmentHelper.getTagEnchantmentLevel(this.enchantmentHolder(Enchantments.FORTUNE), stack1);
         if (f > this.fortune) {
            this.fortune = f;
         }

         this.speed = this.speed + EnchantmentHelper.getTagEnchantmentLevel(this.enchantmentHolder(Enchantments.EFFICIENCY), stack1);
      } else {
         this.hasPickaxe = false;
      }
   }

   @Override
   public void setChanged() {
      super.setChanged();
      this.recalculateItems();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   public boolean gettingPower() {
      Direction baseOr = this.getBaseOrientation();
      return this.level.hasNeighborSignal(this.worldPosition) || this.level.hasNeighborSignal(this.worldPosition.relative(baseOr));
   }

   @Override
   public Direction getFacing() {
      return this.orientation;
   }

   public void setOrientation(Direction or, boolean initial) {
      this.orientation = or;
      this.lastX = 0;
      this.lastZ = 0;
      switch (or.ordinal()) {
         case 0:
            this.tarZ = 180;
            this.tarX = 0;
            break;
         case 1:
            this.tarZ = 0;
            this.tarX = 0;
            break;
         case 2:
            this.tarZ = 90;
            this.tarX = 270;
            break;
         case 3:
            this.tarZ = 90;
            this.tarX = 90;
            break;
         case 4:
            this.tarZ = 90;
            this.tarX = 0;
            break;
         case 5:
            this.tarZ = 90;
            this.tarX = 180;
      }

      if (initial) {
         this.rotX = this.tarX;
         this.rotZ = this.tarZ;
         this.speedX = 0;
         this.speedZ = 0;
      }

      if (this.level == null || !this.level.isClientSide) {
         this.toDig = false;
         this.radInc = 0.0F;
         this.digPos = BlockPos.ZERO;
         this.postDigDelay = 0;
         this.count = 0;
      }

      this.paused = 100;
      this.tRadX = 0.0F;
      this.tRadZ = 0.0F;
      this.mRadX = 0.0F;
      this.mRadZ = 0.0F;
      this.clientDigFxCooldown = 0;
      this.markForUpdate();
   }

   public void getDigEvent(int j, float syncSpeedyTime) {
      int x = (j >> 16 & 0xFF) - 64;
      int y = (j >> 8 & 0xFF) - 64;
      int z = (j & 0xFF) - 64;
      this.digPos = this.worldPosition.offset(x, y, z);
      this.toDig = true;
      this.digBlock = this.level.getBlockState(this.digPos);
      this.speedyTime = syncSpeedyTime;
      this.clientDigFxCooldown = this.level != null ? this.level.random.nextInt(3) : 0;
   }

   public boolean triggerEvent(int i, int j) {
      if (i != 99) {
         return super.triggerEvent(i, j);
      } else {
         if (this.level.isClientSide && j != 0) {
            BlockState st = net.minecraft.world.level.block.Block.stateById(j);
            if (!st.isAir()) {
               SoundType soundType = st.getSoundType();
               Vec3 vec3 = this.digPos.getCenter();
               this.level
                  .playLocalSound(
                     vec3.x,
                     vec3.y,
                     vec3.z,
                     soundType.getBreakSound(),
                     SoundSource.BLOCKS,
                     (soundType.getVolume() + 1.0F) / 2.0F,
                     soundType.getPitch() * 0.8F,
                     false
                  );
               BlockPos blockPos = this.worldPosition.relative(this.getFacing());

               for (int a = 0; a < Thaumcraft.setup.particleCount(10); a++) {
                  Thaumcraft.setup
                     .boreDigFx(
                        this.level,
                        this.digPos.getX(),
                        this.digPos.getY(),
                        this.digPos.getZ(),
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        st.getBlock(),
                        0
                     );
               }
            }
         }

         return true;
      }
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadAdditional(compound, provider);
      if (compound.contains("SpeedyTime")) {
         this.speedyTime = compound.getShort("SpeedyTime");
      }

      if (!compound.contains("Orientation")) {
         this.orientation = inferInitialOrientation(this.getBlockState());
         this.setOrientation(this.orientation, true);
      }

      this.recalculateItems();
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveAdditional(compound, provider);
      compound.putShort("SpeedyTime", (short)this.speedyTime);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.loadBoreData(compound);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.loadBoreData(compound);
   }

   private void loadBoreData(@NotNull CompoundTag compound) {
      if (compound.contains("Orientation")) {
         Direction o = Direction.from3DDataValue(compound.getByte("Orientation"));
         this.orientation = o;
         this.setOrientation(o, true);
      }

      this.hasFocus = compound.getBoolean("HasFocus");
      this.hasPickaxe = compound.getBoolean("HasPickaxe");
      this.fortune = compound.getInt("Fortune");
      this.speed = compound.getInt("Speed");
      this.area = compound.getInt("Area");
      this.speedyTime = compound.getFloat("SpeedyTimeSync");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.saveBoreData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.saveBoreData(compound);
   }

   private void saveBoreData(@NotNull CompoundTag compound) {
      compound.putByte("Orientation", (byte)this.orientation.get3DDataValue());
      compound.putBoolean("HasFocus", this.hasFocus);
      compound.putBoolean("HasPickaxe", this.hasPickaxe);
      compound.putInt("Fortune", this.fortune);
      compound.putInt("Speed", this.speed);
      compound.putInt("Area", this.area);
      compound.putFloat("SpeedyTimeSync", this.speedyTime);
   }

   @Override
   public int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos blockPos, @NotNull Direction direction
   ) {
      player.swing(hand);
      return 0;
   }

   @NotNull
   @Override
   public ItemStack onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      return ItemStack.EMPTY;
   }

   @Override
   public void onUsingWandTick(@NotNull ItemStack stack, @NotNull Player player, int count) {
   }

   @Override
   public void onWandStoppedUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player, int count) {
   }

   @NotNull
   public Component getDisplayName() {
      return Component.empty();
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inventory, @NotNull Player player) {
      return new BoreMenu(windowId, inventory, this);
   }
}
