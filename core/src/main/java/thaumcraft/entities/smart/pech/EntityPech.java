package thaumcraft.entities.smart.pech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModEnchantment;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.util.InventoryUtils;

public class EntityPech extends Monster implements RangedAttackMob {
   public static final String PECH_DROP_TAG = "thaumcraft.pech_drop";
   private static final EntityDataAccessor<Byte> PECH_TYPE = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> TAMED = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BOOLEAN);
   public ItemStack[] loot = new ItemStack[9];
   public boolean trading = false;
   public boolean updateAINextTick = false;
   public float mumble = 0.0F;
   int chargecount = 0;
   private LivingEntity entityToAttack;
   static HashMap<Integer, Integer> valuedItems = new HashMap<>();
   public static HashMap<Integer, ArrayList<List<?>>> tradeInventory = new HashMap<>();

   public EntityPech(EntityType<? extends EntityPech> type, Level level) {
      super(type, level);

      for (int i = 0; i < this.loot.length; i++) {
         this.loot[i] = ItemStack.EMPTY;
      }

      this.handDropChances[0] = 0.2F;
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.MOVEMENT_SPEED, 0.5);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(PECH_TYPE, (byte)0);
      builder.define(ANGER, 0);
      builder.define(TAMED, false);
   }

   public Component getName() {
      if (this.hasCustomName()) {
         return this.getCustomName();
      } else {
         switch (this.getPechType()) {
            case 0:
               return Component.translatable("entity.Thaumcraft.Pech.name");
            case 1:
               return Component.translatable("entity.Thaumcraft.Pech.1.name");
            case 2:
               return Component.translatable("entity.Thaumcraft.Pech.2.name");
            default:
               return Component.translatable("entity.Thaumcraft.Pech.name");
         }
      }
   }

   public int getPechType() {
      return (Byte)this.entityData.get(PECH_TYPE);
   }

   public int getAnger() {
      return (Integer)this.entityData.get(ANGER);
   }

   public boolean isTamed() {
      return (Boolean)this.entityData.get(TAMED);
   }

   public void setPechType(int par1) {
      this.entityData.set(PECH_TYPE, (byte)par1);
   }

   public void setAnger(int par1) {
      this.entityData.set(ANGER, par1);
   }

   public void setTamed(boolean par1) {
      this.entityData.set(TAMED, par1);
   }

   /**
    * Выдача снаряжения при спавне: {@code addRandomArmor} существовал, но не
    * вызывался ниоткуда — печи всегда ходили с пустыми руками, а шанс дропа
    * 0.2 висел на пустом слоте.
    */
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      net.minecraft.world.level.ServerLevelAccessor level,
      net.minecraft.world.DifficultyInstance difficulty,
      net.minecraft.world.entity.MobSpawnType reason,
      @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData data
   ) {
      this.addRandomArmor();
      return super.finalizeSpawn(level, difficulty, reason, data);
   }

   @Override
   public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putByte("PechType", (byte)this.getPechType());
      tag.putInt("Anger", this.getAnger());
      tag.putBoolean("Tamed", this.isTamed());
      net.minecraft.nbt.ListTag lootList = new net.minecraft.nbt.ListTag();
      for (int i = 0; i < this.loot.length; i++) {
         if (this.loot[i] != null && !this.loot[i].isEmpty()) {
            net.minecraft.nbt.CompoundTag slot = new net.minecraft.nbt.CompoundTag();
            slot.putByte("Slot", (byte)i);
            slot.put("Item", thaumcraft.util.ItemStackData.saveStack(this.loot[i]));
            lootList.add(slot);
         }
      }

      tag.put("PechLoot", lootList);
   }

   @Override
   public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setPechType(tag.getByte("PechType"));
      this.setAnger(tag.getInt("Anger"));
      this.setTamed(tag.getBoolean("Tamed"));
      net.minecraft.nbt.ListTag lootList = tag.getList("PechLoot", 10);
      for (int i = 0; i < lootList.size(); i++) {
         net.minecraft.nbt.CompoundTag slot = lootList.getCompound(i);
         int s = slot.getByte("Slot") & 255;
         if (s >= 0 && s < this.loot.length) {
            this.loot[s] = thaumcraft.util.ItemStackData.loadStack(slot.getCompound("Item"));
         }
      }
   }

   @Override
   protected void dropCustomDeathLoot(
      @NotNull net.minecraft.server.level.ServerLevel level,
      @NotNull net.minecraft.world.damagesource.DamageSource source,
      boolean hitByPlayer
   ) {
      super.dropCustomDeathLoot(level, source, hitByPlayer);
      // Нафармленное добро не сгорает вместе с торговцем: PECH_DROP_TAG был
      // задуман, но выдача так и не была написана.
      for (ItemStack stack : this.loot) {
         if (stack != null && !stack.isEmpty()) {
            this.spawnAtLocation(stack.copy());
         }
      }
   }

   public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
      super.setItemSlot(slot, stack);
      if (!this.level().isClientSide && slot == EquipmentSlot.MAINHAND) {
         this.updateAINextTick = true;
      }
   }

   protected void addRandomArmor() {
      super.populateDefaultEquipmentSlots(this.random, this.level().getCurrentDifficultyAt(this.blockPosition()));
      switch (this.random.nextInt(20)) {
         case 0:
         case 12:
            ItemStack wand = new ItemStack((ItemLike)ModItems.WAND_CASTING.get());
            ItemStack focus = new ItemStack((ItemLike)ModItems.FOCUS_PECH.get());
            ((ItemWandCasting)wand.getItem()).setFocus(wand, focus);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.EARTH, 2 + this.random.nextInt(6), true);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.ENTROPY, 2 + this.random.nextInt(6), true);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.WATER, 2 + this.random.nextInt(6), true);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.AIR, this.random.nextInt(4), true);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.FIRE, this.random.nextInt(4), true);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.ORDER, this.random.nextInt(4), true);
            this.setItemSlot(EquipmentSlot.MAINHAND, wand);
            break;
         case 1:
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            break;
         case 2:
         case 4:
         case 10:
         case 11:
         case 13:
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            break;
         case 3:
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
            break;
         case 5:
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            break;
         case 6:
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
            break;
         case 7:
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
            break;
         case 8:
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_PICKAXE));
            break;
         case 9:
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
      }
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
      return size.height() * 0.66F;
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      try {
         if (this.loot == null) {
            return true;
         } else {
            int q = 0;

            for (ItemStack is : this.loot) {
               if (is != null && !is.isEmpty() && is.getCount() > 0) {
                  q++;
               }
            }

            return q < 5;
         }
      } catch (Exception var8) {
         return true;
      }
   }

   public boolean canBeLeashed() {
      return false;
   }

   public void handleEntityEvent(byte id) {
      if (id == 16) {
         float unused = this.mumble = (float) Math.PI;
      } else if (id == 17) {
         float unused = this.mumble = (float) (Math.PI * 2);
      } else if (id == 18) {
         for (int unused = 0; unused < 5; unused++) {
            double _x = this.random.nextGaussian() * 0.02;
            double _xx = this.random.nextGaussian() * 0.02;
            double _xxx = this.random.nextGaussian() * 0.02;
            this.level()
               .addParticle(
                  ParticleTypes.HAPPY_VILLAGER,
                  this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                  this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                  this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                  _x,
                  _xx,
                  _xxx
               );
         }
      } else if (id != 19) {
         super.handleEntityEvent(id);
      } else {
         for (int unused = 0; unused < 5; unused++) {
            double _x = this.random.nextGaussian() * 0.02;
            double _xx = this.random.nextGaussian() * 0.02;
            double _xxx = this.random.nextGaussian() * 0.02;
            this.level()
               .addParticle(
                  ParticleTypes.ANGRY_VILLAGER,
                  this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                  this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                  this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                  _x,
                  _xx,
                  _xxx
               );
         }

         this.mumble = (float) (Math.PI * 2);
      }
   }

   public void playAmbientSound() {
      if (!this.level().isClientSide) {
         if (this.random.nextInt(3) == 0) {
            AABB box = this.getBoundingBox().inflate(4.0, 2.0, 4.0);

            for (Entity e : this.level().getEntities(this, box)) {
               if (e instanceof EntityPech) {
                  this.level().broadcastEntityEvent(this, (byte)17);
                  SoundEvent s = tcSound("pech_trade");
                  if (s != null) {
                     this.playSound(s, this.getSoundVolume(), this.getVoicePitch());
                  }

                  return;
               }
            }
         }

         this.level().broadcastEntityEvent(this, (byte)16);
      }

      super.playAmbientSound();
   }

   public int getAmbientSoundInterval() {
      return 120;
   }

   protected float getSoundVolume() {
      return 0.4F;
   }

   protected SoundEvent getAmbientSound() {
      return tcSound("pech_idle");
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return tcSound("pech_hit");
   }

   protected SoundEvent getDeathSound() {
      return tcSound("pech_death");
   }

   public void performRangedAttack(LivingEntity target, float f) {
      this.swing(InteractionHand.MAIN_HAND);
   }

   private void becomeAngryAt(Entity par1Entity) {
      if (par1Entity instanceof LivingEntity le) {
         this.entityToAttack = le;
         if (this.getAnger() <= 0) {
            this.level().broadcastEntityEvent(this, (byte)19);
            SoundEvent s = tcSound("pech_charge");
            if (s != null) {
               this.playSound(s, this.getSoundVolume(), this.getVoicePitch());
            }
         }

         this.setTarget(le);
         this.setAnger(400 + this.random.nextInt(400));
         this.setTamed(false);
         this.updateAINextTick = true;
      }
   }

   public int getArmorValue() {
      int i = super.getArmorValue() + 2;
      if (i > 20) {
         i = 20;
      }

      return i;
   }

   public boolean hurt(@NotNull DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         Entity entity = source.getEntity();
         if (entity instanceof Player) {
            AABB box = this.getBoundingBox().inflate(32.0, 16.0, 32.0);

            for (Entity e : this.level().getEntities(this, box)) {
               if (e instanceof EntityPech p) {
                  p.becomeAngryAt(entity);
               }
            }

            this.becomeAngryAt(entity);
         }

         return super.hurt(source, amount);
      }
   }

   public void tick() {
      if (this.mumble > 0.0F) {
         this.mumble *= 0.75F;
      }

      if (this.random.nextInt(15) == 0 && this.getAnger() > 0) {
         double unused = this.random.nextGaussian() * 0.02;
         double _x = this.random.nextGaussian() * 0.02;
         double _xx = this.random.nextGaussian() * 0.02;
         this.level()
            .addParticle(
               ParticleTypes.ANGRY_VILLAGER,
               this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
               this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
               this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
               unused,
               _x,
               _xx
            );
      }

      if (this.random.nextInt(25) == 0 && this.isTamed()) {
         double var8 = this.random.nextGaussian() * 0.02;
         double var9 = this.random.nextGaussian() * 0.02;
         double var10 = this.random.nextGaussian() * 0.02;
         this.level()
            .addParticle(
               ParticleTypes.HAPPY_VILLAGER,
               this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
               this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
               this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
               var8,
               var9,
               var10
            );
      }

      super.tick();
   }

   protected LivingEntity findPlayerToAttack() {
      return this.getAnger() == 0 ? null : this.level().getNearestPlayer(this, 16.0);
   }

   public boolean canPickup(ItemStack entityItem) {
      if (entityItem == null) {
         return false;
      } else if (!this.isTamed() && valuedItems.containsKey(BuiltInRegistries.ITEM.getId(entityItem.getItem()))) {
         return true;
      } else {
         for (int a = 0; a < this.loot.length; a++) {
            if (this.loot[a] != null && !this.loot[a].isEmpty() && this.loot[a].getCount() <= 0) {
               this.loot[a] = ItemStack.EMPTY;
            }

            if (this.loot[a] == null || this.loot[a].isEmpty()) {
               return true;
            }

            if (InventoryUtils.areItemStacksEqualStrict(entityItem, this.loot[a])
               && entityItem.getCount() + this.loot[a].getCount() <= this.loot[a].getMaxStackSize()) {
               return true;
            }
         }

         return false;
      }
   }

   public ItemStack pickupItem(ItemStack entityItem) {
      if (entityItem == null) {
         return entityItem;
      } else if (!this.isTamed() && this.isValued(entityItem)) {
         if (this.random.nextInt(10) < this.getValue(entityItem)) {
            this.setTamed(true);
            this.updateAINextTick = true;
            this.level().broadcastEntityEvent(this, (byte)18);
         }

         entityItem.shrink(1);
         return entityItem.getCount() <= 0 ? ItemStack.EMPTY : entityItem;
      } else {
         for (int a = 0; a < this.loot.length; a++) {
            if (this.loot[a] != null && !this.loot[a].isEmpty() && this.loot[a].getCount() <= 0) {
               this.loot[a] = ItemStack.EMPTY;
            }

            if (entityItem.getCount() > 0
               && this.loot[a] != null
               && !this.loot[a].isEmpty()
               && this.loot[a].getCount() < this.loot[a].getMaxStackSize()
               && InventoryUtils.areItemStacksEqualStrict(entityItem, this.loot[a])) {
               if (entityItem.getCount() + this.loot[a].getCount() <= this.loot[a].getMaxStackSize()) {
                  this.loot[a].grow(entityItem.getCount());
                  return ItemStack.EMPTY;
               }

               int sz = Math.min(entityItem.getCount(), this.loot[a].getMaxStackSize() - this.loot[a].getCount());
               this.loot[a].grow(sz);
               entityItem.shrink(sz);
            }

            if (entityItem.getCount() <= 0) {
               entityItem = ItemStack.EMPTY;
            }
         }

         for (int var4 = 0; var4 < this.loot.length; var4++) {
            if (this.loot[var4] != null && !this.loot[var4].isEmpty() && this.loot[var4].getCount() <= 0) {
               this.loot[var4] = ItemStack.EMPTY;
            }

            if (entityItem.getCount() > 0 && (this.loot[var4] == null || this.loot[var4].isEmpty())) {
               this.loot[var4] = entityItem.copy();
               return ItemStack.EMPTY;
            }
         }

         if (entityItem != null && entityItem.getCount() <= 0) {
            entityItem = null;
         }

         return entityItem;
      }
   }

   @NotNull
   protected InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
      if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      } else if (!player.getItemInHand(hand).isEmpty() && player.getItemInHand(hand).getItem() instanceof NameTagItem) {
         return InteractionResult.PASS;
      } else if (this.isTamed()) {
         if (!this.level().isClientSide) {
            ((ServerPlayer)player)
               .openMenu(new SimpleMenuProvider((id, inv, p) -> new PechMenu(id, inv, this), this.getName()), buf -> buf.writeInt(this.getId()));
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         return super.mobInteract(player, hand);
      }
   }

   public boolean isValued(ItemStack item) {
      if (item == null) {
         return false;
      } else {
         boolean value = valuedItems.containsKey(BuiltInRegistries.ITEM.getId(item.getItem()));
         if (!value) {
            AspectList al = ThaumcraftCraftingManager.getObjectTags(item);
            al = ThaumcraftCraftingManager.getBonusTags(item, al);
            if (al.getAmount(Aspect.GREED) > 0) {
               value = true;
            }
         }

         return value;
      }
   }

   public int getValue(ItemStack item) {
      if (item == null) {
         return 0;
      } else {
         int key = BuiltInRegistries.ITEM.getId(item.getItem());
         int value = valuedItems.getOrDefault(key, 0);
         if (value == 0) {
            AspectList al = ThaumcraftCraftingManager.getObjectTags(item);
            al = ThaumcraftCraftingManager.getBonusTags(item, al);
            value = Math.min(32, al.getAmount(Aspect.GREED));
         }

         return value;
      }
   }

   private static SoundEvent tcSound(String path) {
      return (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", path));
   }

   private static ItemStack enchantedBook(Holder<Enchantment> ench, int level) {
      return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ench, level));
   }

   private static ItemStack enchantedBook(ResourceKey<Enchantment> ench, int level) {
      RegistryLookup<Enchantment> lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
      return lookup == null ? ItemStack.EMPTY : enchantedBook(lookup.getOrThrow(ench), level);
   }

   private int getHeldEnchantmentLevel(ResourceKey<Enchantment> enchantment) {
      return this.registryAccess()
         .lookupOrThrow(Registries.ENCHANTMENT)
         .get(enchantment)
         .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, this.getMainHandItem()))
         .orElse(0);
   }

   static {
      valuedItems.put(BuiltInRegistries.ITEM.getId((Item)ModItems.MANA_BEAN.get()), 1);
      valuedItems.put(BuiltInRegistries.ITEM.getId(Items.GOLD_INGOT), 2);
      valuedItems.put(BuiltInRegistries.ITEM.getId(Items.GOLDEN_APPLE), 2);
      valuedItems.put(BuiltInRegistries.ITEM.getId(Items.ENDER_PEARL), 3);
      valuedItems.put(BuiltInRegistries.ITEM.getId(Items.DIAMOND), 4);
      valuedItems.put(BuiltInRegistries.ITEM.getId(Items.EMERALD), 5);
      ArrayList<List<?>> forInv = new ArrayList<>();
      forInv.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.MANA_BEAN.get())));
      forInv.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.CINNABAR_CLUSTER.get())));
      forInv.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.THAUMIUM_NUGGET.get())));
      forInv.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.VOID_METAL_NUGGET.get())));
      forInv.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.COPPER_NUGGET.get())));
      forInv.add(Arrays.asList(2, new ItemStack(Items.BLAZE_ROD)));
      forInv.add(Arrays.asList(2, new ItemStack((ItemLike)ModItems.SHIMMER_LEAF.get())));
      forInv.add(Arrays.asList(2, PotionContents.createItemStack(Items.POTION, Potions.STRONG_HEALING)));
      forInv.add(Arrays.asList(2, PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE)));
      forInv.add(Arrays.asList(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
      forInv.add(Arrays.asList(3, new ItemStack((ItemLike)ModItems.KNOWLEDGE_FRAGMENT.get())));
      forInv.add(Arrays.asList(3, new ItemStack(Items.GOLDEN_APPLE)));
      forInv.add(Arrays.asList(3, PotionContents.createItemStack(Items.POTION, Potions.STRONG_STRENGTH)));
      forInv.add(Arrays.asList(3, PotionContents.createItemStack(Items.POTION, Potions.LONG_INVISIBILITY)));
      forInv.add(Arrays.asList(5, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
      forInv.add(Arrays.asList(4, new ItemStack((ItemLike)ModItems.THAUMIUM_PICKAXE.get())));
      forInv.add(Arrays.asList(5, new ItemStack((ItemLike)ModItems.ETHEREAL_BLOOM.get())));
      forInv.add(Arrays.asList(5, new ItemStack((ItemLike)ModItems.ETHEREAL_BLOOM.get())));
      tradeInventory.put(0, forInv);
      ArrayList<List<?>> forMag = new ArrayList<>();
      forMag.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.MANA_BEAN.get())));
      forMag.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.AIR_SHARD.get())));
      forMag.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.FIRE_SHARD.get())));
      forMag.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.WATER_SHARD.get())));
      forMag.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.EARTH_SHARD.get())));
      forMag.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.ORDER_SHARD.get())));
      forMag.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.ENTROPY_SHARD.get())));
      forMag.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.KNOWLEDGE_FRAGMENT.get())));
      forMag.add(Arrays.asList(2, new ItemStack((ItemLike)ModItems.KNOWLEDGE_FRAGMENT.get())));
      forMag.add(Arrays.asList(2, PotionContents.createItemStack(Items.POTION, Potions.REGENERATION)));
      forMag.add(Arrays.asList(2, PotionContents.createItemStack(Items.POTION, Potions.STRONG_SWIFTNESS)));
      forMag.add(Arrays.asList(3, enchantedBook(ModEnchantment.HASTE.getKey(), 1)));
      forMag.add(Arrays.asList(3, new ItemStack(Items.GOLDEN_APPLE)));
      forMag.add(Arrays.asList(3, PotionContents.createItemStack(Items.POTION, Potions.STRONG_LEAPING)));
      forMag.add(Arrays.asList(3, PotionContents.createItemStack(Items.POTION, Potions.NIGHT_VISION)));
      forMag.add(Arrays.asList(4, new ItemStack((ItemLike)ModItems.AIR_CRYSTAL_CLUSTER.get())));
      forMag.add(Arrays.asList(4, new ItemStack((ItemLike)ModItems.FIRE_CRYSTAL_CLUSTER.get())));
      forMag.add(Arrays.asList(4, new ItemStack((ItemLike)ModItems.WATER_CRYSTAL_CLUSTER.get())));
      forMag.add(Arrays.asList(4, new ItemStack((ItemLike)ModItems.EARTH_CRYSTAL_CLUSTER.get())));
      forMag.add(Arrays.asList(4, new ItemStack((ItemLike)ModItems.ORDER_CRYSTAL_CLUSTER.get())));
      forMag.add(Arrays.asList(4, new ItemStack((ItemLike)ModItems.ENTROPY_CRYSTAL_CLUSTER.get())));
      forMag.add(Arrays.asList(4, new ItemStack((ItemLike)ModItems.MIXED_CRYSTAL_CLUSTER.get())));
      forMag.add(Arrays.asList(5, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
      forMag.add(Arrays.asList(5, enchantedBook(ModEnchantment.REPAIR.getKey(), 1)));
      forMag.add(Arrays.asList(5, new ItemStack((ItemLike)ModItems.FOCUS_POUCH.get())));
      forMag.add(Arrays.asList(5, new ItemStack((ItemLike)ModItems.FOCUS_PECH.get())));
      forMag.add(Arrays.asList(5, new ItemStack((ItemLike)ModItems.VIS_AMULET.get())));
      tradeInventory.put(1, forMag);
      ArrayList<List<?>> forArc = new ArrayList<>();
      forArc.add(Arrays.asList(1, new ItemStack((ItemLike)ModItems.MANA_BEAN.get())));

      for (DyeColor color : DyeColor.values()) {
         if (ModBlocks.CANDLE.containsKey(color)) {
            forArc.add(Arrays.asList(1, new ItemStack((ItemLike)ModBlocks.CANDLE.get(color).get())));
         }
      }

      forArc.add(Arrays.asList(2, new ItemStack(Items.GHAST_TEAR)));
      forArc.add(Arrays.asList(2, PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE)));
      forArc.add(Arrays.asList(2, PotionContents.createItemStack(Items.POTION, Potions.STRONG_HEALING)));
      forArc.add(Arrays.asList(2, enchantedBook(Enchantments.POWER, 1)));
      forArc.add(Arrays.asList(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
      forArc.add(Arrays.asList(3, new ItemStack((ItemLike)ModItems.KNOWLEDGE_FRAGMENT.get())));
      forArc.add(Arrays.asList(3, PotionContents.createItemStack(Items.POTION, Potions.LONG_TURTLE_MASTER)));
      forArc.add(Arrays.asList(3, PotionContents.createItemStack(Items.POTION, Potions.STRONG_LEAPING)));
      forArc.add(Arrays.asList(3, new ItemStack(Items.GOLDEN_APPLE)));
      forArc.add(Arrays.asList(5, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
      forArc.add(Arrays.asList(4, new ItemStack((ItemLike)ModItems.THAUMIUM_BOOTS.get())));
      forArc.add(Arrays.asList(5, new ItemStack((ItemLike)ModItems.RUNIC_RING.get())));
      forArc.add(Arrays.asList(5, enchantedBook(Enchantments.FLAME, 1)));
      forArc.add(Arrays.asList(5, enchantedBook(Enchantments.INFINITY, 1)));
      tradeInventory.put(2, forArc);
   }

   public static class PechRangedAttackGoal extends Goal {
      private final EntityPech pech;
      private final double speedModifier;
      private final int minAttackInterval;
      private final int maxAttackInterval;
      private final float attackRadius;
      private int attackTime = -1;
      private int seeTime;

      PechRangedAttackGoal(EntityPech pech, double speedModifier, int minAttackInterval, int maxAttackInterval, float attackRadius) {
         this.pech = pech;
         this.speedModifier = speedModifier;
         this.minAttackInterval = minAttackInterval;
         this.maxAttackInterval = maxAttackInterval;
         this.attackRadius = attackRadius;
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      public boolean canUse() {
         return this.pech.getTarget() != null;
      }

      public boolean canContinueToUse() {
         return this.canUse();
      }

      public void start() {
         this.attackTime = 0;
         this.seeTime = 0;
      }

      public void stop() {
         this.seeTime = 0;
         this.attackTime = -1;
      }

      public void tick() {
         LivingEntity target = this.pech.getTarget();
         if (target != null) {
            double d0 = this.pech.distanceToSqr(target);
            boolean canSee = this.pech.getSensing().hasLineOfSight(target);
            if (canSee) {
               this.seeTime++;
            } else {
               this.seeTime = 0;
            }

            double radiusSqr = this.attackRadius * this.attackRadius;
            if (d0 <= radiusSqr && this.seeTime >= 20) {
               this.pech.getNavigation().stop();
            } else {
               this.pech.getNavigation().moveTo(target, this.speedModifier);
            }

            this.pech.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (--this.attackTime == 0) {
               if (!canSee) {
                  return;
               }

               float f = Mth.sqrt((float)d0) / this.attackRadius;
               float clamped = Mth.clamp(f, 0.1F, 1.0F);
               this.pech.performRangedAttack(target, clamped);
               int interval = this.minAttackInterval;
               if (this.maxAttackInterval > this.minAttackInterval) {
                  interval = this.minAttackInterval + this.pech.random.nextInt(this.maxAttackInterval - this.minAttackInterval + 1);
               }

               this.attackTime = interval;
            } else if (this.attackTime < 0) {
               int interval = this.minAttackInterval;
               if (this.maxAttackInterval > this.minAttackInterval) {
                  interval = this.minAttackInterval + this.pech.random.nextInt(this.maxAttackInterval - this.minAttackInterval + 1);
               }

               this.attackTime = interval;
            }
         }
      }
   }
}
