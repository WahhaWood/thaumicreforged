package thaumcraft.api.aspects.obj;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;

public record EntityAspect(EntityType<? extends Entity> entityType, AspectList aspects, @Nullable Predicate<Entity> predicate, @Nullable String hashKey) {
   public boolean matches(Entity entity) {
      return entity.getType() == this.entityType && (this.predicate == null || this.predicate.test(entity));
   }

   public boolean isPredicateSpecific() {
      return this.predicate != null;
   }

   public int matchPriority(Entity entity) {
      if (!this.matches(entity)) {
         return Integer.MIN_VALUE;
      } else {
         return this.isPredicateSpecific() ? 100 : 0;
      }
   }

   @Nullable
   public String getScanHashKey(Entity entity) {
      return this.matches(entity) ? this.hashKey : null;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return o.getClass() != this.getClass()
            ? false
            : Objects.equals(this.entityType, ((EntityAspect)o).entityType) && Objects.equals(this.hashKey, ((EntityAspect)o).hashKey);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityType, this.hashKey);
   }
}
