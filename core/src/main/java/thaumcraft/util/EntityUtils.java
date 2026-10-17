package thaumcraft.util;

import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;
import thaumcraft.entities.boss.EntityThaumcraftBoss;
import thaumcraft.entities.brainless.special.SpecialItemEntity;
import thaumcraft.entities.smart.modifiers.ChampionModifier;
import thaumcraft.init.registry.Config;

public class EntityUtils {
   public static final Attribute CHAMPION_MOD = new RangedAttribute("tc.mobmod", -2.0, -2.0, 100.0).setSyncable(true);
   public static final AttributeModifier CHAMPION_HEALTH = attributeModifier("a62bef38-48cc-42a6-ac5e-ef913841c4fd", 30.0, Operation.ADD_VALUE);
   public static final AttributeModifier CHAMPION_DAMAGE = attributeModifier("a340d2db-d881-4c25-ac62-f0ad14cd63b0", 2.0, Operation.ADD_MULTIPLIED_TOTAL);
   public static final AttributeModifier BOLDBUFF = attributeModifier("4b1edd33-caa9-47ae-a702-d86c05701037", 0.3, Operation.ADD_MULTIPLIED_BASE);
   public static final AttributeModifier MIGHTYBUFF = attributeModifier("7163897f-07f5-49b3-9ce4-b74beb83d2d3", 3.0, Operation.ADD_MULTIPLIED_TOTAL);
   public static final AttributeModifier[] HPBUFF = new AttributeModifier[]{
      attributeModifier("54d621c1-dd4d-4b43-8bd2-5531c8875797", 50.0, Operation.ADD_VALUE),
      attributeModifier("f51257dc-b7fa-4f7a-92d7-75d68e8592c4", 50.0, Operation.ADD_VALUE),
      attributeModifier("3d6b2e42-4141-4364-b76d-0e8664bbd0bb", 50.0, Operation.ADD_VALUE),
      attributeModifier("02c97a08-801c-4131-afa2-1427a6151934", 50.0, Operation.ADD_VALUE),
      attributeModifier("0f354f6a-33c5-40be-93be-81b1338567f1", 50.0, Operation.ADD_VALUE)
   };
   public static final AttributeModifier[] DMGBUFF = new AttributeModifier[]{
      attributeModifier("534f8c57-929a-48cf-bbd6-0fd851030748", 0.5, Operation.ADD_VALUE),
      attributeModifier("d317a76e-0e7c-4c61-acfd-9fa286053b32", 0.5, Operation.ADD_VALUE),
      attributeModifier("ff462d63-26a2-4363-830e-143ed97e2a4f", 0.5, Operation.ADD_VALUE),
      attributeModifier("cf1eb39e-0c67-495f-887c-0d3080828d2f", 0.5, Operation.ADD_VALUE),
      attributeModifier("3cfab9da-2701-43d8-ac07-885f16fa4117", 0.5, Operation.ADD_VALUE)
   };
   public static final AttributeModifier FLIGHT = attributeModifier("6b2f5664-d55f-43b0-970b-e79c778c6e98", 1.0, Operation.ADD_VALUE);
   /** The hover harness has its own id, so taking it off never strips the flight of the ichor wings. */
   public static final AttributeModifier HOVER_FLIGHT = attributeModifier("hover_harness_flight", 1.0, Operation.ADD_VALUE);

   private static AttributeModifier attributeModifier(String path, double amount, Operation operation) {
      return new AttributeModifier(ResourceLocation.fromNamespaceAndPath("thaumcraft", path), amount, operation);
   }

   public static Entity getPointedEntity(Entity entity, double minRange, double range, float padding) {
      return getPointedEntity(entity, minRange, range, padding, false);
   }

   public static Entity getPointedEntity(Entity shooter, double minRange, double range, float padding, boolean nonCollide) {
      Vec3 eyePos = shooter.getEyePosition(1.0F);
      Vec3 viewVec = shooter.getViewVector(1.0F);
      Vec3 endVec = eyePos.add(viewVec.scale(range));
      AABB searchBox = shooter.getBoundingBox().expandTowards(viewVec.scale(range)).inflate(padding);
      double rangeSqr = range * range;
      EntityHitResult result = ProjectileUtil.getEntityHitResult(
         shooter,
         eyePos,
         endVec,
         searchBox,
         target -> !target.isSpectator() && (target.isPickable() || nonCollide) && target.distanceTo(shooter) >= minRange,
         rangeSqr
      );
      return result == null ? null : result.getEntity();
   }

   public static Entity getPointedEntity(Player entity, double range, Class<? extends Entity> clazz) {
      Vec3 vec3 = entity.getEyePosition();
      range *= range;
      Vec3 vec31 = entity.getViewVector(1.0F);
      Vec3 vec32 = vec3.add(vec31.x * range, vec31.y * range, vec31.z * range);
      AABB aabb = entity.getBoundingBox().expandTowards(vec31.scale(range)).inflate(1.0, 1.0, 1.0);
      EntityHitResult result = ProjectileUtil.getEntityHitResult(
         entity, vec3, vec32, aabb, p_234237_ -> !p_234237_.isSpectator() && p_234237_.isPickable() && clazz.isInstance(p_234237_), range
      );
      return result == null ? null : result.getEntity();
   }

   public static boolean canEntityBeSeen(Entity entity, BlockEntity te) {
      return te.getLevel().clip(new ClipContext(te.getBlockPos().getCenter(), entity.position(), Block.COLLIDER, Fluid.NONE, entity)).getType() == Type.MISS;
   }

   public static boolean canEntityBeSeen(Entity entity, double x, double y, double z) {
      return entity.level().clip(new ClipContext(new Vec3(x, y, z), entity.position(), Block.COLLIDER, Fluid.NONE, entity)).getType() == Type.MISS;
   }

   public static boolean canEntityBeSeen(Entity entity, Entity entity2) {
      return entity.level().clip(new ClipContext(entity.position(), entity2.position(), Block.COLLIDER, Fluid.NONE, entity)).getType() == Type.MISS;
   }

   public static void setRecentlyHit(LivingEntity ent, int hit) {
   }

   public static int getRecentlyHit(LivingEntity ent) {
      return 0;
   }

   public static HitResult getMovingObjectPositionFromPlayer(Level level, Player player, boolean par3) {
      return ProjectileUtil.getHitResultOnViewVector(player, EntitySelector.NO_SPECTATORS, player.blockInteractionRange());
   }

   public static <T extends Entity> List<T> getEntitiesInRange(Level world, double x, double y, double z, Entity entity, Class<T> clazz, double range) {
      return world.getEntitiesOfClass(clazz, new AABB(x, y, z, x, y, z).inflate(range, range, range), t -> !t.isSpectator() && t != entity);
   }

   public static boolean isVisibleTo(float fov, Entity ent, Entity ent2, float range) {
      Vec3 x = ent2.position().add(0.0, ent2.getBbHeight() / 2.0F, 0.0);
      Vec3 t = ent.getEyePosition();
      return Utils.isLyingInCone(x, t, ent.getLookAngle().multiply(range, range, range).add(t), fov);
   }

   public static boolean isVisibleTo(float fov, Entity ent, double xx, double yy, double zz, float range) {
      Vec3 t = ent.getEyePosition();
      return Utils.isLyingInCone(new Vec3(xx, yy, zz), t, ent.getLookAngle().multiply(range, range, range).add(t), fov);
   }

   public static ItemEntity entityDropSpecialItem(Entity entity, ItemStack stack, float dropheight) {
      if (!stack.isEmpty()) {
         SpecialItemEntity entityitem = new SpecialItemEntity(entity.level(), entity.getX(), entity.getY() + dropheight, entity.getZ(), stack);
         entityitem.setPickUpDelay(10);
         entityitem.setDeltaMovement(0.0, 0.1, 0.0);
         Collection<ItemEntity> captureDrops = entity.captureDrops();
         if (captureDrops != null) {
            captureDrops.add(entityitem);
         } else {
            entity.level().addFreshEntity(entityitem);
         }

         return entityitem;
      } else {
         return null;
      }
   }

   private static void replacePermanentModifier(AttributeInstance attribute, AttributeModifier modifier) {
      if (attribute != null) {
         attribute.removeModifier(modifier);
         attribute.addPermanentModifier(modifier);
      }
   }

   public static void makeChampion(Mob entity, boolean persist) {
      int type = entity.getRandom().nextInt(ChampionModifier.mods.length);
      if (entity instanceof Creeper) {
         type = 0;
      }

      AttributeInstance attribute = entity.getAttribute(Holder.direct(CHAMPION_MOD));
      if (attribute != null) {
         replacePermanentModifier(attribute, ChampionModifier.mods[type].attributeMod);
         if (!(entity instanceof EntityThaumcraftBoss)) {
            replacePermanentModifier(entity.getAttribute(Attributes.MAX_HEALTH), CHAMPION_HEALTH);
            replacePermanentModifier(entity.getAttribute(Attributes.ATTACK_DAMAGE), CHAMPION_DAMAGE);
            entity.heal(25.0F);
            if (Config.customChampionsNaming) {
               entity.setCustomName(Component.literal(ChampionModifier.mods[type].getModName() + " " + entity.getScoreboardName()));
            }
         } else {
            ((EntityThaumcraftBoss)entity).generateName();
         }

         if (persist) {
            entity.setPersistenceRequired();
         }

         switch (type) {
            case 0:
               replacePermanentModifier(entity.getAttribute(Attributes.MOVEMENT_SPEED), BOLDBUFF);
               break;
            case 3:
               replacePermanentModifier(entity.getAttribute(Attributes.ATTACK_DAMAGE), MIGHTYBUFF);
               break;
            case 5:
               int bh = (int)entity.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2;
               entity.setAbsorptionAmount(entity.getAbsorptionAmount() + bh);
         }
      }
   }

   public static boolean checkSpawnRulesMonsterDefault(@NotNull LevelAccessor accessor, @NotNull Entity entity, @NotNull MobSpawnType spawnType) {
      if (accessor.getDifficulty() == Difficulty.PEACEFUL) {
         return false;
      } else {
         if (accessor instanceof ServerLevelAccessor serverAccessor) {
            BlockPos blockPos = entity.blockPosition();
            if (accessor.getBrightness(LightLayer.SKY, blockPos) > accessor.getRandom().nextInt(32)) {
               return false;
            }

            DimensionType dimensiontype = accessor.dimensionType();
            int i = dimensiontype.monsterSpawnBlockLightLimit();
            if (i < 15 && accessor.getBrightness(LightLayer.BLOCK, blockPos) > i) {
               return false;
            }

            int j = serverAccessor.getLevel().isThundering() ? accessor.getMaxLocalRawBrightness(blockPos, 10) : accessor.getMaxLocalRawBrightness(blockPos);
            if (j <= dimensiontype.monsterSpawnLightTest().sample(accessor.getRandom())) {
               blockPos = blockPos.below();
               return spawnType == MobSpawnType.SPAWNER || accessor.getBlockState(blockPos).isValidSpawn(accessor, blockPos, entity.getType());
            }
         }

         return false;
      }
   }

   public static boolean checkSpawnRulesMonsterNotLightDefault(@NotNull LevelAccessor accessor, @NotNull Entity entity, @NotNull MobSpawnType spawnType) {
      if (accessor.getDifficulty() == Difficulty.PEACEFUL) {
         return false;
      } else {
         BlockPos blockPos = entity.blockPosition().below();
         return spawnType == MobSpawnType.SPAWNER || accessor.getBlockState(blockPos).isValidSpawn(accessor, blockPos, entity.getType());
      }
   }
}
