package thaumcraft.init.registry;

import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.entities.boss.EntityThaumcraftBoss;
import thaumcraft.entities.brainless.alumentum.AlumentumEntity;
import thaumcraft.entities.brainless.aspect_orb.AspectOrbEntity;
import thaumcraft.entities.brainless.dart.EntityDart;
import thaumcraft.entities.brainless.eldrich_orb.EntityEldritchOrb;
import thaumcraft.entities.brainless.falling_taint.EntityFallingTaint;
import thaumcraft.entities.brainless.following.FollowingItemEntity;
import thaumcraft.entities.brainless.grate.GrateItemEntity;
import thaumcraft.entities.brainless.permanent.PermanentItemEntity;
import thaumcraft.entities.brainless.projectile.ember.EntityEmber;
import thaumcraft.entities.brainless.projectile.explosive_orb.EntityExplosiveOrb;
import thaumcraft.entities.brainless.projectile.frost_shard.EntityFrostShard;
import thaumcraft.entities.brainless.projectile.golem_orb.EntityGolemOrb;
import thaumcraft.entities.brainless.projectile.primal_arrow.EntityPrimalArrow;
import thaumcraft.entities.brainless.projectile.primal_orb.EntityPrimalOrb;
import thaumcraft.entities.brainless.projectile.shadow_beam.EntityShadowbeam;
import thaumcraft.entities.brainless.projectile.shock_orb.EntityShockOrb;
import thaumcraft.entities.brainless.special.SpecialItemEntity;
import thaumcraft.entities.brainless.taint_bottle.EntityBottleTaint;
import thaumcraft.entities.smart.cultist.EntityCultist;
import thaumcraft.entities.smart.cultist.EntityCultistCleric;
import thaumcraft.entities.smart.cultist.EntityCultistKnight;
import thaumcraft.entities.smart.cultist.EntityCultistLeader;
import thaumcraft.entities.smart.cultist_portal.EntityCultistPortal;
import thaumcraft.entities.smart.edlritch_crab.EntityEldritchCrab;
import thaumcraft.entities.smart.eldrich_guardian.EntityEldritchGuardian;
import thaumcraft.entities.smart.eldrich_warden.EntityEldritchWarden;
import thaumcraft.entities.smart.eldritch_golem.EntityEldritchGolem;
import thaumcraft.entities.smart.fire_bat.EntityFireBat;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.entities.smart.golem.bobber.EntityGolemBobber;
import thaumcraft.entities.smart.mind_spider.EntityMindSpider;
import thaumcraft.entities.smart.pech.EntityPech;
import thaumcraft.entities.smart.pech_blast.EntityPechBlast;
import thaumcraft.entities.smart.taint.chicken.EntityTaintChicken;
import thaumcraft.entities.smart.taint.cow.EntityTaintCow;
import thaumcraft.entities.smart.taint.creeper.EntityTaintCreeper;
import thaumcraft.entities.smart.taint.pig.EntityTaintPig;
import thaumcraft.entities.smart.taint.sheep.EntityTaintSheep;
import thaumcraft.entities.smart.taint.spider.EntityTaintSpider;
import thaumcraft.entities.smart.taint.spore.EntityTaintSpore;
import thaumcraft.entities.smart.taint.spore.EntityTaintSporeSwarmer;
import thaumcraft.entities.smart.taint.spore.EntityTaintSwarm;
import thaumcraft.entities.smart.taint.villager.EntityTaintVillager;
import thaumcraft.entities.smart.tentacle.EntityTantacle;
import thaumcraft.entities.smart.tentacle.EntityTentacleSmall;
import thaumcraft.entities.smart.tentacle.boss.EntityTantacleGiant;
import thaumcraft.entities.smart.thaumic_slime.EntityThaumicSlime;
import thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk;
import thaumcraft.entities.smart.watcher.EntityWatcher;
import thaumcraft.entities.smart.wisp.EntityWisp;
import thaumcraft.entities.smart.zombie.EntityBrainyZombie;
import thaumcraft.entities.smart.zombie.EntityGiantBrainyZombie;
import thaumcraft.entities.smart.zombie.EntityInhabitedZombie;
import thaumcraft.util.EntityUtils;

public class ModEntities {
   private static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, "thaumcraft");
   private static final DeferredRegister<Attribute> ATTRIBUTE_REGISTER = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, "thaumcraft");
   public static final DeferredHolder<Attribute, Attribute> CHAMPION_MOD = ATTRIBUTE_REGISTER.register("champion_mod", () -> EntityUtils.CHAMPION_MOD);
   public static final DeferredHolder<EntityType<?>, EntityType<AspectOrbEntity>> ASPECT_ORB = defaultEntity(
      "aspect_orb", 0.125F, 0.125F, 16, 20, MobCategory.MISC, AspectOrbEntity::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<SpecialItemEntity>> SPECIAL_ITEM = defaultEntity(
      "special_item", 8, 20, MobCategory.MISC, SpecialItemEntity::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<GrateItemEntity>> GRATE_ITEM = defaultEntity(
      "grate_item", 8, 20, MobCategory.MISC, GrateItemEntity::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<FollowingItemEntity>> FOLLOWING_ITEM = defaultEntity(
      "following_item", 8, 20, MobCategory.MISC, FollowingItemEntity::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<AlumentumEntity>> ALUMENTUM = defaultEntity(
      "alumentum", 8, 20, MobCategory.MISC, AlumentumEntity::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityShadowbeam>> SHADOW_BEAM = defaultEntity(
      "shadow_beam", 8, 20, MobCategory.MISC, EntityShadowbeam::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityShockOrb>> SHOCK_ORB = defaultEntity(
      "shock_orb", 8, 20, MobCategory.MISC, EntityShockOrb::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityPrimalOrb>> PRIMAL_ORB = REGISTER.register(
      "primal_orb", () -> Builder.<EntityPrimalOrb>of(EntityPrimalOrb::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(64).updateInterval(10).build("primal_orb")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityPrimalArrow>> PRIMAL_ARROW = defaultEntity(
      "primal_arrow", 0.5F, 0.5F, 10, 20, MobCategory.MISC, EntityPrimalArrow::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityPechBlast>> PECH_BLAST = REGISTER.register(
      "pech_blast",
      () -> Builder.<EntityPechBlast>of(EntityPechBlast::new, MobCategory.MISC)
         .sized(0.25F, 0.25F)
         .clientTrackingRange(8)
         .updateInterval(10)
         .build(ResourceLocation.fromNamespaceAndPath("thaumcraft", "pech_blast").toString())
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityFallingTaint>> FALLING_TAINT = REGISTER.register(
      "falling_taint",
      () -> Builder.<EntityFallingTaint>of(EntityFallingTaint::new, MobCategory.MISC).sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(20).build("falling_taint")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityGolemOrb>> GOLEM_ORB = defaultEntity(
      "golem_orb", 8, 20, MobCategory.MISC, EntityGolemOrb::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityFrostShard>> FROST_SHARD = defaultEntity(
      "frost_shard", 8, 20, MobCategory.MISC, EntityFrostShard::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityExplosiveOrb>> EXPLOSIVE_ORB = defaultEntity(
      "explosive_orb", 8, 20, MobCategory.MISC, EntityExplosiveOrb::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityEmber>> EMBER = defaultEntity("ember", 8, 20, MobCategory.MISC, EntityEmber::new);
   public static final DeferredHolder<EntityType<?>, EntityType<EntityEldritchOrb>> ELDRITCH_ORB = defaultEntity(
      "eldritch_orb", 8, 20, MobCategory.MISC, EntityEldritchOrb::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityDart>> DART = defaultEntity("dart", 0.5F, 0.5F, 10, 20, MobCategory.MISC, EntityDart::new);
   public static final DeferredHolder<EntityType<?>, EntityType<EntityBottleTaint>> BOTTLE_TAINT = defaultEntity(
      "bottle_taint", 8, 20, MobCategory.MISC, EntityBottleTaint::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityGolemBase>> GOLEM = defaultEntity(
      "golem", 0.4F, 0.95F, 64, 3, MobCategory.MISC, EntityGolemBase::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityGolemBobber>> GOLEM_BOBBER = defaultEntity(
      "golem_bobber", 0.25F, 0.25F, 64, 64, MobCategory.MISC, EntityGolemBobber::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<PermanentItemEntity>> PERMANENT_ITEM = REGISTER.register(
      "permanent_item",
      () -> Builder.<PermanentItemEntity>of(PermanentItemEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(3).build("permanent_item")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityCultist>> CULTIST = defaultEntity(
      "cultist", 0.6F, 1.8F, 8, MobCategory.MONSTER, EntityCultist::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityCultistCleric>> CULTIST_CLERIC = defaultEntity(
      "cultist_cleric", 0.6F, 1.8F, 8, MobCategory.MONSTER, EntityCultistCleric::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityCultistKnight>> CULTIST_KNIGHT = defaultEntity(
      "cultist_knight", 0.6F, 1.8F, 8, MobCategory.MONSTER, EntityCultistKnight::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityEldritchCrab>> ELDRITCH_CRAB = defaultEntity(
      "eldritch_crab", 0.8F, 0.6F, 8, MobCategory.MONSTER, EntityEldritchCrab::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityEldritchGuardian>> ELDRITCH_GUARDIAN = defaultEntity(
      "eldritch_guardian", 0.8F, 2.25F, 8, MobCategory.MONSTER, EntityEldritchGuardian::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityFireBat>> FIRE_BAT = REGISTER.register(
      "fire_bat", () -> Builder.of(EntityFireBat::new, MobCategory.MONSTER).sized(0.5F, 0.9F).clientTrackingRange(8).fireImmune().build("fire_bat")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityGiantBrainyZombie>> GIANT_BRAINY_ZOMBIE = defaultEntity(
      "giant_brainy_zombie", 0.6F, 1.95F, 8, MobCategory.MONSTER, EntityGiantBrainyZombie::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityBrainyZombie>> BRAINY_ZOMBIE = defaultEntity(
      "brainy_zombie", 0.6F, 1.95F, 8, MobCategory.MONSTER, EntityBrainyZombie::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityMindSpider>> MIND_SPIDER = defaultEntity(
      "mind_spider", 0.3F, 0.3F, 8, MobCategory.MONSTER, EntityMindSpider::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityPech>> PECH = defaultEntity("pech", 0.6F, 1.8F, 8, MobCategory.MONSTER, EntityPech::new);
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTantacle>> TENTACLE = defaultEntity(
      "tentacle", 0.66F, 3.0F, 8, MobCategory.MONSTER, EntityTantacle::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTentacleSmall>> TENTACLE_SMALL = defaultEntity(
      "tentacle_small", 0.22F, 1.0F, 8, MobCategory.MONSTER, EntityTentacleSmall::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintChicken>> TAINT_CHICKEN = defaultEntity(
      "taint_chicken", 0.5F, 0.8F, 8, MobCategory.MONSTER, EntityTaintChicken::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintCow>> TAINT_COW = defaultEntity(
      "taint_cow", 0.9F, 1.3F, 8, MobCategory.MONSTER, EntityTaintCow::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintCreeper>> TAINT_CREEPER = defaultEntity(
      "taint_creeper", 0.6F, 1.7F, 8, MobCategory.MONSTER, EntityTaintCreeper::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintPig>> TAINT_PIG = defaultEntity(
      "taint_pig", 0.9F, 0.9F, 8, MobCategory.MONSTER, EntityTaintPig::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintSheep>> TAINT_SHEEP = defaultEntity(
      "taint_sheep", 0.9F, 1.3F, 8, MobCategory.MONSTER, EntityTaintSheep::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintSpider>> TAINT_SPIDER = defaultEntity(
      "taint_spider", 0.4F, 0.3F, 8, MobCategory.MONSTER, EntityTaintSpider::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintSpore>> TAINT_SPORE = defaultEntity(
      "taint_spore", 0.3F, 0.3F, 8, MobCategory.MONSTER, EntityTaintSpore::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintSporeSwarmer>> TAINT_SPORE_SWARMER = defaultEntity(
      "taint_spore_swarmer", 1.0F, 1.0F, 8, MobCategory.MONSTER, EntityTaintSporeSwarmer::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintSwarm>> TAINT_SWARM = defaultEntity(
      "taint_swarm", 2.0F, 2.0F, 8, MobCategory.MONSTER, EntityTaintSwarm::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintVillager>> TAINT_VILLAGER = defaultEntity(
      "taint_villager", 0.6F, 1.95F, 8, MobCategory.MONSTER, EntityTaintVillager::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityThaumicSlime>> THAUMIC_SLIME = defaultEntity(
      "thaumic_slime", 2.04F, 2.04F, 10, MobCategory.MONSTER, EntityThaumicSlime::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityWisp>> WISP = defaultEntity("wisp", 0.9F, 0.9F, 8, MobCategory.MONSTER, EntityWisp::new);
   public static final DeferredHolder<EntityType<?>, EntityType<EntityWatcher>> WATCHER = defaultEntity(
      "watcher", 0.85F, 0.85F, 8, MobCategory.MONSTER, EntityWatcher::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityInhabitedZombie>> INHABITED_ZOMBIE = defaultEntity(
      "inhabited_zombie", 0.6F, 1.95F, 8, MobCategory.MONSTER, EntityInhabitedZombie::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTravelingTrunk>> TRAVELING_TRUNK = REGISTER.register(
      "traveling_trunk",
      () -> Builder.of(EntityTravelingTrunk::new, MobCategory.CREATURE)
         .sized(0.8F, 0.8F)
         .clientTrackingRange(64)
         .updateInterval(3)
         .fireImmune()
         .build(ResourceLocation.fromNamespaceAndPath("thaumcraft", "traveling_trunk").toString())
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityCultistLeader>> CULTIST_LEADER = defaultEntity(
      "cultist_leader", 0.75F, 2.25F, 8, MobCategory.MONSTER, EntityCultistLeader::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityCultistPortal>> CULTIST_PORTAL = REGISTER.register(
      "cultist_portal",
      () -> Builder.of(EntityCultistPortal::new, MobCategory.MONSTER).sized(1.5F, 3.0F).clientTrackingRange(8).fireImmune().build("cultist_portal")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityEldritchGolem>> ELDRITCH_GOLEM = REGISTER.register(
      "eldritch_golem",
      () -> Builder.of(EntityEldritchGolem::new, MobCategory.MONSTER).sized(1.5F, 3.5F).clientTrackingRange(8).fireImmune().build("eldritch_golem")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityEldritchWarden>> ELDRITCH_WARDEN = defaultEntity(
      "eldritch_warden", 1.5F, 3.5F, 8, MobCategory.MONSTER, EntityEldritchWarden::new
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityTantacleGiant>> TENTACLE_GIANT = defaultEntity(
      "tentacle_giant", 1.1F, 6.0F, 8, MobCategory.MONSTER, EntityTantacleGiant::new
   );
   public static int entWizardId = 190;
   public static int entBankerId = 191;
   public static HashMap<Class, Integer> championModWhitelist = new HashMap<>();

   private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> defaultEntity(String name, MobCategory category, EntityFactory<T> factory) {
      return defaultEntity(name, 0.25F, 0.25F, category, factory);
   }

   private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> defaultEntity(
      String name, int clientTrackingRange, int updateInterval, MobCategory category, EntityFactory<T> factory
   ) {
      return defaultEntity(name, 0.25F, 0.25F, clientTrackingRange, updateInterval, category, factory);
   }

   private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> defaultEntity(
      String name, float width, float height, MobCategory category, EntityFactory<T> factory
   ) {
      return defaultEntity(name, width, height, 5, 3, category, factory);
   }

   private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> defaultEntity(
      String name, float width, float height, int clientTrackingRange, MobCategory category, EntityFactory<T> factory
   ) {
      return defaultEntity(name, width, height, clientTrackingRange, 3, category, factory);
   }

   private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> defaultEntity(
      String name, float width, float height, int clientTrackingRange, int updateInterval, MobCategory category, EntityFactory<T> factory
   ) {
      return REGISTER.register(
         name, () -> Builder.of(factory, category).sized(width, height).clientTrackingRange(clientTrackingRange).updateInterval(updateInterval).build(name)
      );
   }

   private static AttributeSupplier withChampion(net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder builder) {
      return builder.add(CHAMPION_MOD, ((Attribute)CHAMPION_MOD.get()).getDefaultValue()).build();
   }

   public static void registerAttributes(EntityAttributeCreationEvent event) {
      event.put(
         (EntityType)CULTIST.get(),
         Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 32.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.MOVEMENT_SPEED, 0.3).build()
      );
      event.put(
         (EntityType)CULTIST_CLERIC.get(),
         Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.MOVEMENT_SPEED, 0.3).build()
      );
      event.put(
         (EntityType)CULTIST_KNIGHT.get(),
         Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 36.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.MOVEMENT_SPEED, 0.3).build()
      );
      event.put(
         (EntityType)ELDRITCH_CRAB.get(),
         Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.MOVEMENT_SPEED, 0.3).build()
      );
      event.put(
         (EntityType)ELDRITCH_GUARDIAN.get(),
         withChampion(
            Monster.createMonsterAttributes()
               .add(Attributes.MAX_HEALTH, 50.0)
               .add(Attributes.FOLLOW_RANGE, 40.0)
               .add(Attributes.ATTACK_DAMAGE, 7.0)
               .add(Attributes.MOVEMENT_SPEED, 0.28)
         )
      );
      event.put((EntityType)FIRE_BAT.get(), Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 1.0).build());
      event.put(
         (EntityType)GIANT_BRAINY_ZOMBIE.get(),
         Zombie.createAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0).build()
      );
      event.put(
         (EntityType)BRAINY_ZOMBIE.get(),
         Zombie.createAttributes().add(Attributes.MAX_HEALTH, 25.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0).build()
      );
      event.put((EntityType)MIND_SPIDER.get(), Spider.createAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.ATTACK_DAMAGE, 1.0).build());
      event.put(
         (EntityType)PECH.get(),
         Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.MOVEMENT_SPEED, 0.5).build()
      );
      event.put((EntityType)TENTACLE.get(), Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.ATTACK_DAMAGE, 7.0).build());
      event.put((EntityType)TENTACLE_SMALL.get(), Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 2.0).build());
      event.put(
         (EntityType)TAINT_CHICKEN.get(),
         Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.MOVEMENT_SPEED, 0.4).build()
      );
      event.put(
         (EntityType)TAINT_COW.get(),
         Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.MOVEMENT_SPEED, 0.27).build()
      );
      event.put(
         (EntityType)TAINT_CREEPER.get(),
         Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.MOVEMENT_SPEED, 0.25).build()
      );
      event.put(
         (EntityType)TAINT_PIG.get(),
         Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.MOVEMENT_SPEED, 0.275).build()
      );
      event.put(
         (EntityType)TAINT_SHEEP.get(),
         Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.MOVEMENT_SPEED, 0.25).build()
      );
      event.put((EntityType)TAINT_SPIDER.get(), Spider.createAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 2.0).build());
      event.put((EntityType)TAINT_SPORE.get(), Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.ATTACK_DAMAGE, 1.0).build());
      event.put(
         (EntityType)TAINT_SPORE_SWARMER.get(), Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 75.0).add(Attributes.ATTACK_DAMAGE, 2.0).build()
      );
      event.put((EntityType)TAINT_SWARM.get(), Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 22.0).add(Attributes.ATTACK_DAMAGE, 2.0).build());
      event.put(
         (EntityType)TAINT_VILLAGER.get(),
         Zombie.createAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.MOVEMENT_SPEED, 0.3).build()
      );
      event.put((EntityType)THAUMIC_SLIME.get(), Monster.createMonsterAttributes().build());
      event.put((EntityType)WISP.get(), EntityWisp.createAttributes().build());
      event.put((EntityType)WATCHER.get(), EntityWatcher.createAttributes().build());
      event.put(
         (EntityType)INHABITED_ZOMBIE.get(),
         Zombie.createAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0).build()
      );
      event.put(
         (EntityType)CULTIST_LEADER.get(),
         withChampion(
            EntityThaumcraftBoss.createAttributes().add(Attributes.MAX_HEALTH, 125.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.MOVEMENT_SPEED, 0.32)
         )
      );
      event.put(
         (EntityType)CULTIST_PORTAL.get(),
         withChampion(
            Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 500.0).add(Attributes.ATTACK_DAMAGE, 0.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
         )
      );
      event.put(
         (EntityType)ELDRITCH_GOLEM.get(),
         withChampion(
            EntityThaumcraftBoss.createAttributes().add(Attributes.MAX_HEALTH, 250.0).add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.MOVEMENT_SPEED, 0.3)
         )
      );
      event.put(
         (EntityType)ELDRITCH_WARDEN.get(),
         withChampion(
            EntityThaumcraftBoss.createAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.MOVEMENT_SPEED, 0.33)
         )
      );
      event.put(
         (EntityType)TENTACLE_GIANT.get(), withChampion(Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 125.0).add(Attributes.ATTACK_DAMAGE, 9.0))
      );
      event.put((EntityType)TRAVELING_TRUNK.get(), EntityTravelingTrunk.createAttributes().build());
      event.put((EntityType)GOLEM.get(), EntityGolemBase.createAttributes().build());
   }

   private static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
      event.register(
         (EntityType)FIRE_BAT.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, ModEntities::checkFireBatSpawnRules, Operation.REPLACE
      );
      event.register(
         (EntityType)WISP.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, ModEntities::checkWispSpawnRules, Operation.REPLACE
      );
      event.register(
         (EntityType)WATCHER.get(), SpawnPlacementTypes.IN_WATER, Types.MOTION_BLOCKING_NO_LEAVES, EntityWatcher::checkWatcherSpawnRules, Operation.REPLACE
      );
      EntityType<?>[] monsterGroundTypes = new EntityType<?>[]{
         (EntityType<?>)BRAINY_ZOMBIE.get(),
         (EntityType<?>)GIANT_BRAINY_ZOMBIE.get(),
         (EntityType<?>)ELDRITCH_GUARDIAN.get(),
         (EntityType<?>)PECH.get(),
         (EntityType<?>)TENTACLE.get(),
         (EntityType<?>)TENTACLE_SMALL.get(),
         (EntityType<?>)CULTIST_KNIGHT.get(),
         (EntityType<?>)CULTIST_CLERIC.get(),
         (EntityType<?>)ELDRITCH_CRAB.get(),
         (EntityType<?>)MIND_SPIDER.get(),
         (EntityType<?>)TAINT_SPIDER.get(),
         (EntityType<?>)TAINT_SPORE.get(),
         (EntityType<?>)TAINT_SPORE_SWARMER.get(),
         (EntityType<?>)TAINT_SWARM.get(),
         (EntityType<?>)TAINT_CREEPER.get(),
         (EntityType<?>)TAINT_CHICKEN.get(),
         (EntityType<?>)TAINT_COW.get(),
         (EntityType<?>)TAINT_PIG.get(),
         (EntityType<?>)TAINT_SHEEP.get(),
         (EntityType<?>)TAINT_VILLAGER.get(),
         (EntityType<?>)THAUMIC_SLIME.get(),
         (EntityType<?>)INHABITED_ZOMBIE.get()
      };
      for (EntityType<?> type : monsterGroundTypes) {
         event.register(
            (EntityType)type, SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, Operation.REPLACE
         );
      }
   }

   private static boolean checkFireBatSpawnRules(
      EntityType<EntityFireBat> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
   }

   private static boolean checkWispSpawnRules(EntityType<EntityWisp> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
      if (level.getDifficulty() == Difficulty.PEACEFUL) {
         return false;
      } else if (level.getBrightness(LightLayer.SKY, pos) > random.nextInt(32)) {
         return false;
      } else {
         DimensionType dimensionType = level.dimensionType();
         int blockLightLimit = dimensionType.monsterSpawnBlockLightLimit();
         if (blockLightLimit < 15 && level.getBrightness(LightLayer.BLOCK, pos) > blockLightLimit) {
            return false;
         } else {
            int localBrightness = level.getLevel().isThundering() ? level.getMaxLocalRawBrightness(pos, 10) : level.getMaxLocalRawBrightness(pos);
            if (localBrightness > dimensionType.monsterSpawnLightTest().sample(random)) {
               return false;
            } else {
               BlockPos below = pos.below();
               return spawnType == MobSpawnType.SPAWNER || level.getBlockState(below).isValidSpawn(level, below, type);
            }
         }
      }
   }

   public static void init(IEventBus bus) {
      ATTRIBUTE_REGISTER.register(bus);
      REGISTER.register(bus);
      bus.addListener(ModEntities::onRegisterSpawnPlacements);
      int n = 0;
   }

   public static void initEntitySpawns() {
   }
}
