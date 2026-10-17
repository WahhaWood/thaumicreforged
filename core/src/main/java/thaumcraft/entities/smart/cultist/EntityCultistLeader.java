package thaumcraft.entities.smart.cultist;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import thaumcraft.entities.boss.EntityThaumcraftBoss;
import thaumcraft.entities.smart.modifiers.ChampionModifier;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModItems;
import thaumcraft.util.EntityUtils;

public class EntityCultistLeader extends EntityThaumcraftBoss implements RangedAttackMob {
   private static final EntityDataAccessor<Byte> TITLE = SynchedEntityData.defineId(EntityCultistLeader.class, EntityDataSerializers.BYTE);
   private final String[] titles = new String[]{
      "Alberic",
      "Anselm",
      "Bastian",
      "Beturian",
      "Chabier",
      "Chorache",
      "Chuse",
      "Dodorol",
      "Ebardo",
      "Ferrando",
      "Fertus",
      "Guillen",
      "Larpe",
      "Obano",
      "Zelipe"
   };

   @Override
   public void tick() {
      super.tick();
      this.buffCultists(this.level());
   }

   /**
    * Каждый тик подкрепляет культистов силой — порт
    * {@code EntityCultistLeader.func_70619_bc} (1.7.10, строки 164-177).
    *
    * <p>В порте не было: лидер вызывал магов, но не давал им усиления, хотя у
    * оригинала это основная его роль в бою — с силой 2 культисты бьют вдвое
    * больнее.
    *
    * <p>{@code Potion.field_76428_l} — сила (расставка полей восстановлена по
    * {@code EntityThaumcraftBoss}, строки 236-238, где в 1.12.2 это STRENGTH).
    */
   public void buffCultists(Level level) {
      for (EntityCultist cultist : this.level().getEntitiesOfClass(EntityCultist.class,
         this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D))) {
         if (!cultist.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST)) {
            cultist.addEffect(new net.minecraft.world.effect.MobEffectInstance(
               net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 60, 1));
         }
      }
   }

   public EntityCultistLeader(EntityType<? extends EntityCultistLeader> type, Level level) {
      super(type, level);
      this.xpReward = 40;
   }

   public EntityCultistLeader(Level level) {
      super((EntityType<? extends Monster>)ModEntities.CULTIST_LEADER.get(), level);
      this.xpReward = 40;
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TITLE, (byte)0);
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return EntityThaumcraftBoss.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.MAX_HEALTH, 125.0).add(Attributes.ATTACK_DAMAGE, 5.0);
   }

   @Override
   public void generateName() {
      int t = (int)this.getAttributeValue(Holder.direct(EntityUtils.CHAMPION_MOD));
      if (t >= 0 && t < ChampionModifier.mods.length) {
         this.setCustomName(
            Component.translatable("entity.Thaumcraft.CultistLeader.name", new Object[]{this.getTitle(), ChampionModifier.mods[t].getModName()})
         );
      }
   }

   private String getTitle() {
      int idx = (Byte)this.entityData.get(TITLE);
      if (idx < 0 || idx >= this.titles.length) {
         idx = 0;
      }

      return this.titles[idx];
   }

   private void setTitle(int title) {
      this.entityData.set(TITLE, (byte)title);
   }

   @Override
   protected void populateDefaultEquipmentSlots(@NotNull RandomSource randomSource, @NotNull DifficultyInstance difficultyInstance) {
      this.setItemSlot(EquipmentSlot.HEAD, new ItemStack((ItemLike)ModItems.HELMET_CULTIST_LEADER.get()));
      this.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike)ModItems.CHEST_CULTIST_LEADER.get()));
      this.setItemSlot(EquipmentSlot.LEGS, new ItemStack((ItemLike)ModItems.LEGGINGS_CULTIST_LEADER.get()));
      this.setItemSlot(EquipmentSlot.FEET, new ItemStack((ItemLike)ModItems.BOOTS_CULTIST.get()));
      if (this.level().getDifficulty() == Difficulty.EASY) {
         this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack((ItemLike)ModItems.VOID_SWORD.get()));
      } else {
         this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack((ItemLike)ModItems.CRIMSON_BLADE.get()));
      }

      // Шанс дропа снаряжения как у рыцаря/клирика: без setDropChance дефолт
      // Mob = 0 и сет лидера не падает никогда.
      this.setDropChance(EquipmentSlot.HEAD, 0.2F);
      this.setDropChance(EquipmentSlot.CHEST, 0.2F);
      this.setDropChance(EquipmentSlot.LEGS, 0.2F);
      this.setDropChance(EquipmentSlot.FEET, 0.2F);
      super.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
   }

   @Override
   protected void populateDefaultEquipmentEnchantments(
      @NotNull ServerLevelAccessor level, @NotNull RandomSource randomSource, @NotNull DifficultyInstance difficultyInstance
   ) {
      DifficultyInstance di = this.level().getCurrentDifficultyAt(this.blockPosition());
      float f = di.getSpecialMultiplier();
      ItemStack held = this.getMainHandItem();
      if (!held.isEmpty() && this.random.nextFloat() < 0.5F * f) {
         ItemStack out = EnchantmentHelper.enchantItem(this.random, held, (int)(7.0F + f * this.random.nextInt(22)), this.registryAccess(), Optional.empty());
         this.setItemSlot(EquipmentSlot.MAINHAND, out);
      }

      super.populateDefaultEquipmentEnchantments(level, randomSource, difficultyInstance);
   }

   /**
    * В 1.21 {@code Mob.finalizeSpawn} больше НЕ вызывает раздачу снаряжения —
    * без этого оверрайда лидер спавнился бы голым: его слоты и чары выше
    * никогда не выполнялись бы.
    */
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      @NotNull ServerLevelAccessor level,
      @NotNull DifficultyInstance difficulty,
      @NotNull net.minecraft.world.entity.MobSpawnType reason,
      @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData data
   ) {
      this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
      this.populateDefaultEquipmentEnchantments(level, level.getRandom(), difficulty);
      return super.finalizeSpawn(level, difficulty, reason, data);
   }

   public boolean isAlliedTo(Entity other) {
      return other instanceof EntityCultist || other instanceof EntityCultistLeader || super.isAlliedTo(other);
   }

   public boolean canAttack(LivingEntity target) {
      return !(target instanceof EntityCultistCleric) && !(target instanceof EntityCultistLeader) && !(target instanceof EntityCultistKnight)
         ? super.canAttack(target)
         : false;
   }

   public void performRangedAttack(LivingEntity target, float distanceFactor) {
      if (this.hasLineOfSight(target)) {
         this.swing(InteractionHand.MAIN_HAND);
         this.getLookControl().setLookAt(target.getX(), target.getY() + target.getBbHeight() / 2.0F, target.getZ(), 30.0F, 30.0F);
      }
   }
}
