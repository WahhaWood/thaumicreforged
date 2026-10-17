package thaumcraft.entities.smart.cultist;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModItems;

public class EntityCultistKnight extends EntityCultist {
   public EntityCultistKnight(EntityType<? extends EntityCultistKnight> type, Level level) {
      super(type, level);
   }

   public EntityCultistKnight(Level level) {
      super((EntityType<? extends EntityCultist>)ModEntities.CULTIST_KNIGHT.get(), level);
   }

   public static Builder createAttributes() {
      return EntityCultist.createAttributes().add(Attributes.MAX_HEALTH, 36.0);
   }

   /**
    * В 1.21 {@code Mob.finalizeSpawn} больше НЕ вызывает
    * {@code populateDefaultEquipmentSlots} сам (раньше вызывал) — без этого
    * оверрайда рыцари спавнились бы голыми несмотря на слоты выше.
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
    * Рыцари носят латный доспех культа — иначе его было негде взять, кроме
    * лидера в зале боссов. Шанс дропа выставлен явно: без setDropChance броня
    * с populateDefaultEquipmentSlots не падает никогда.
    */
   @Override
   protected void populateDefaultEquipmentSlots(
      net.minecraft.util.RandomSource randomSource, net.minecraft.world.DifficultyInstance difficultyInstance
   ) {
      this.setItemSlot(EquipmentSlot.HEAD, new ItemStack((ItemLike)ModItems.HELMET_CULTIST_PLATE.get()));
      this.setItemSlot(EquipmentSlot.CHEST, new ItemStack((ItemLike)ModItems.CHEST_CULTIST_PLATE.get()));
      this.setItemSlot(EquipmentSlot.LEGS, new ItemStack((ItemLike)ModItems.LEGGINGS_CULTIST_PLATE.get()));
      this.setItemSlot(EquipmentSlot.FEET, new ItemStack((ItemLike)ModItems.BOOTS_CULTIST.get()));
      this.setDropChance(EquipmentSlot.HEAD, 0.2F);
      this.setDropChance(EquipmentSlot.CHEST, 0.2F);
      this.setDropChance(EquipmentSlot.LEGS, 0.2F);
      this.setDropChance(EquipmentSlot.FEET, 0.2F);
      super.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
   }
}
