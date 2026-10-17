package thaumcraft.entities.smart.cultist;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull;

public class EntityCultist extends Monster {
   public EntityCultist(EntityType<? extends EntityCultist> type, Level level) {
      super(type, level);
      this.xpReward = 10;
      this.setCanPickUpLoot(false);
      this.setPathfindingMalus(PathType.WATER, 8.0F);
      this.setPathfindingMalus(PathType.WATER_BORDER, 8.0F);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 4.0);
   }

   public boolean canPickUpLoot() {
      return false;
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return true;
   }

   public boolean doHurtTarget(Entity target) {
      return super.doHurtTarget(target);
   }

   public void setHomeArea(int x, int y, int z, int radius) {
      BlockPos p = new BlockPos(x, y, z);
      this.restrictTo(p, radius);
   }

   public BlockPos getHomePosition() {
      return this.hasRestriction() ? this.getRestrictCenter() : BlockPos.ZERO;
   }

   public boolean isAlliedTo(Entity other) {
      return other instanceof EntityCultist || other instanceof EntityCultistLeader || super.isAlliedTo(other);
   }

   public boolean canAttack(@NotNull LivingEntity target) {
      return !(target instanceof EntityCultistCleric) && !(target instanceof EntityCultistLeader) && !(target instanceof EntityCultistKnight)
         ? super.canAttack(target)
         : false;
   }
}
