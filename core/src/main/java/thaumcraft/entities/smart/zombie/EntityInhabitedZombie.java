package thaumcraft.entities.smart.zombie;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EntityInhabitedZombie extends Zombie {
   public EntityInhabitedZombie(EntityType<? extends EntityInhabitedZombie> type, Level level) {
      super(type, level);
   }

   @NotNull
   public static Builder createAttributes() {
      return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
   }

   public boolean killedEntity(@NotNull ServerLevel level, @NotNull LivingEntity victim) {
      return false;
   }

}
