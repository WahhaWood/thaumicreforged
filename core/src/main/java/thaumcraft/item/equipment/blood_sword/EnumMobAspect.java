package thaumcraft.item.equipment.blood_sword;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.init.registry.ModEntities;

public enum EnumMobAspect {
   SNOW_GOLEM(() -> EntityType.SNOW_GOLEM, new Aspect[]{Aspect.WATER, Aspect.WATER, Aspect.MAN}),
   BAT(() -> EntityType.BAT, new Aspect[]{Aspect.AIR, Aspect.AIR, Aspect.FLIGHT}, 1.9F, -0.3F),
   BLAZE(() -> EntityType.BLAZE, new Aspect[]{Aspect.FIRE, Aspect.FIRE, Aspect.FIRE}),
   CAVE_SPIDER(() -> EntityType.CAVE_SPIDER, new Aspect[]{Aspect.BEAST, Aspect.POISON, Aspect.POISON}),
   CHICKEN(() -> EntityType.CHICKEN, new Aspect[]{Aspect.CROP, Aspect.FLIGHT, Aspect.BEAST}),
   COW(() -> EntityType.COW, new Aspect[]{Aspect.BEAST, Aspect.EARTH, Aspect.BEAST}),
   CREEPER(() -> EntityType.CREEPER, new Aspect[]{Aspect.MAGIC, Aspect.BEAST, Aspect.ELDRITCH}),
   ENDERMAN(() -> EntityType.ENDERMAN, new Aspect[]{Aspect.ELDRITCH, Aspect.ELDRITCH, Aspect.MAN}, 0.3F, 0.0F),
   GHAST(() -> EntityType.GHAST, new Aspect[]{Aspect.FIRE, Aspect.FLIGHT, Aspect.FLIGHT}, 0.1F, 0.2F),
   HORSE(() -> EntityType.HORSE, new Aspect[]{Aspect.BEAST, Aspect.BEAST, Aspect.TRAVEL}),
   IRON_GOLEM(() -> EntityType.IRON_GOLEM, new Aspect[]{Aspect.METAL, Aspect.METAL, Aspect.MAN}, 0.3F, 0.0F),
   MAGMA_CUBE(() -> EntityType.MAGMA_CUBE, new Aspect[]{Aspect.FIRE, Aspect.SLIME, Aspect.SLIME}, 0.6F, 0.0F) {
      @Override
      public Entity createEntity(Level level) {
         Entity e = super.createEntity(level);
         if (e instanceof MagmaCube mc) {
            mc.setSize(1, true);
         }

         return e;
      }
   },
   MOOSHROOM(() -> EntityType.MOOSHROOM, new Aspect[]{Aspect.BEAST, Aspect.EARTH, Aspect.CROP}),
   OCELOT(() -> EntityType.OCELOT, new Aspect[]{Aspect.BEAST, Aspect.EARTH, Aspect.ELDRITCH}),
   PIG(() -> EntityType.PIG, new Aspect[]{Aspect.BEAST, Aspect.EARTH, Aspect.TRAVEL}),
   ZOMBIFIED_PIGLIN(() -> EntityType.ZOMBIFIED_PIGLIN, new Aspect[]{Aspect.UNDEAD, Aspect.FLESH, Aspect.FIRE}),
   SHEEP(() -> EntityType.SHEEP, new Aspect[]{Aspect.EARTH, Aspect.CLOTH, Aspect.BEAST}),
   SILVERFISH(() -> EntityType.SILVERFISH, new Aspect[]{Aspect.METAL, Aspect.METAL, Aspect.EARTH}),
   SKELETON(() -> EntityType.SKELETON, new Aspect[]{Aspect.UNDEAD, Aspect.MAN, Aspect.UNDEAD}),
   SLIME(() -> EntityType.SLIME, new Aspect[]{Aspect.SLIME, Aspect.SLIME, Aspect.BEAST}, 0.6F, 0.0F) {
      @Override
      public Entity createEntity(Level level) {
         Entity e = super.createEntity(level);
         if (e instanceof Slime slime) {
            slime.setSize(1, true);
         }

         return e;
      }
   },
   SPIDER(() -> EntityType.SPIDER, new Aspect[]{Aspect.BEAST, Aspect.UNDEAD, Aspect.UNDEAD}),
   SQUID(() -> EntityType.SQUID, new Aspect[]{Aspect.WATER, Aspect.WATER, Aspect.WATER}, 0.3F, 0.5F),
   VILLAGER(() -> EntityType.VILLAGER, new Aspect[]{Aspect.MAN, Aspect.MAN, Aspect.MAN}),
   WITCH(() -> EntityType.WITCH, new Aspect[]{Aspect.MAGIC, Aspect.UNDEAD, Aspect.ELDRITCH}, 0.35F, 0.0F),
   WOLF(() -> EntityType.WOLF, new Aspect[]{Aspect.BEAST, Aspect.BEAST, Aspect.BEAST}),
   ZOMBIE(() -> EntityType.ZOMBIE, new Aspect[]{Aspect.FLESH, Aspect.FLESH, Aspect.UNDEAD}),
   BRAINY_ZOMBIE(ModEntities.BRAINY_ZOMBIE::get, new Aspect[]{Aspect.MAGIC, Aspect.UNDEAD, Aspect.FLESH}),
   FIREBAT(ModEntities.FIRE_BAT::get, new Aspect[]{Aspect.FLIGHT, Aspect.FIRE, Aspect.MAGIC}, 1.9F, -0.3F),
   WISP(ModEntities.WISP::get, new Aspect[]{Aspect.AIR, Aspect.MAGIC, Aspect.MAGIC});

   private static final Map<EnumMobAspect, Entity> entityCache = Maps.newHashMap();
   public final Aspect[] aspects;
   private final Supplier<EntityType<?>> typeSupplier;
   private final float scale;
   private final float offset;

   private EnumMobAspect(Supplier<EntityType<?>> type, Aspect[] aspects, float scale, float offset) {
      this.aspects = aspects;
      this.typeSupplier = type;
      this.scale = scale;
      this.offset = offset;
   }

   private EnumMobAspect(Supplier<EntityType<?>> type, Aspect[] aspects) {
      this(type, aspects, 1.1F, 0.0F);
   }

   public static Entity getEntityFromCache(EnumMobAspect aspect, Level level) {
      return level == null ? null : entityCache.computeIfAbsent(aspect, k -> k.createEntity(level));
   }

   public static Aspect[] getAspectsForEntity(Entity e) {
      return getAspectsForEntityType(e.getType());
   }

   public static Aspect[] getAspectsForEntityType(EntityType<?> type) {
      for (EnumMobAspect e : values()) {
         if (e.getType() == type) {
            return e.aspects;
         }
      }

      return null;
   }

   public static EnumMobAspect getMobAspectForType(ResourceLocation registryName) {
      if (registryName == null) {
         return null;
      } else {
         EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.get(registryName);
         if (type == null) {
            return null;
         } else {
            for (EnumMobAspect e : values()) {
               if (e.getType() == type) {
                  return e;
               }
            }

            return null;
         }
      }
   }

   public static EnumMobAspect getMobAspectForType(String registryNameStr) {
      ResourceLocation registryName = ResourceLocation.tryParse(registryNameStr);
      return registryName == null ? null : getMobAspectForType(registryName);
   }

   public float getVerticalOffset() {
      return this.offset;
   }

   public float getScale() {
      return this.scale;
   }

   public EntityType<?> getType() {
      return this.typeSupplier.get();
   }

   public Entity createEntity(Level level) {
      return this.getType().create(level);
   }

   @Override
   public String toString() {
      ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType());
      return key != null ? key.toString() : "unknown";
   }

   public ItemStack[] getAspectItemStacks() {
      return new ItemStack[this.aspects.length];
   }
}
