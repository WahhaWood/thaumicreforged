package thaumcraft.block.summoner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.arcane_pedestal.ArcanePedestalBlockEntity;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.item.equipment.blood_sword.EnumMobAspect;
import thaumcraft.item.mob_aspect.ItemMobAspect;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXEssenceSource;

public class SummonerBlockEntity extends BlockEntity {
   private static final int CHECK_INTERVAL = 300;
   private static final int INFUSED_SUMMON_INTERVAL = 1200;
   private static final String TAG_NO_ASPECT_DROP = "tc.summoner_no_aspect_drop";

   public SummonerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.SUMMONER.get(), pos, state);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, SummonerBlockEntity blockEntity) {
      if (level.getGameTime() % 300L == 0L) {
         if (!level.hasNeighborSignal(pos)) {
            for (int radius = 1; radius < 6; radius++) {
               List<ArcanePedestalBlockEntity> pedestals = blockEntity.findPedestalsWithMobAspect(radius);

               for (int i = 0; i < pedestals.size(); i++) {
                  for (int j = i + 1; j < pedestals.size(); j++) {
                     for (int k = j + 1; k < pedestals.size(); k++) {
                        ArcanePedestalBlockEntity ped1 = pedestals.get(i);
                        ArcanePedestalBlockEntity ped2 = pedestals.get(j);
                        ArcanePedestalBlockEntity ped3 = pedestals.get(k);
                        if (blockEntity.trySummon(level, pos, ped1, ped2, ped3)) {
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private List<ArcanePedestalBlockEntity> findPedestalsWithMobAspect(int radius) {
      List<ArcanePedestalBlockEntity> pedestals = new ArrayList<>();
      if (this.level == null) {
         return pedestals;
      } else {
         for (int x = this.worldPosition.getX() - radius; x < this.worldPosition.getX() + radius; x++) {
            for (int z = this.worldPosition.getZ() - radius; z < this.worldPosition.getZ() + radius; z++) {
               if (this.level.getBlockEntity(new BlockPos(x, this.worldPosition.getY(), z)) instanceof ArcanePedestalBlockEntity pedestal) {
                  ItemStack stack = pedestal.getItem(0);
                  if (!stack.isEmpty() && stack.getItem() instanceof ItemMobAspect) {
                     pedestals.add(pedestal);
                  }
               }
            }
         }

         return pedestals;
      }
   }

   private boolean trySummon(Level level, BlockPos summonerPos, ArcanePedestalBlockEntity ped1, ArcanePedestalBlockEntity ped2, ArcanePedestalBlockEntity ped3) {
      ItemStack stack1 = ped1.getItem(0);
      ItemStack stack2 = ped2.getItem(0);
      ItemStack stack3 = ped3.getItem(0);
      Aspect aspect1 = ItemMobAspect.getAspect(stack1);
      Aspect aspect2 = ItemMobAspect.getAspect(stack2);
      Aspect aspect3 = ItemMobAspect.getAspect(stack3);
      if (aspect1 != null && aspect2 != null && aspect3 != null) {
         List<Aspect> aspects = Arrays.asList(aspect1, aspect2, aspect3);

         for (EnumMobAspect recipe : EnumMobAspect.values()) {
            List<Aspect> recipeAspects = Arrays.asList(recipe.aspects);
            if (recipeAspects.containsAll(aspects) && aspects.containsAll(recipeAspects)) {
               boolean infused = ItemMobAspect.isInfused(stack1) && ItemMobAspect.isInfused(stack2) && ItemMobAspect.isInfused(stack3);
               if (infused && level.getGameTime() % 1200L != 0L) {
                  return true;
               }

               if (!infused) {
                  ped1.setItem(0, ItemStack.EMPTY);
                  ped2.setItem(0, ItemStack.EMPTY);
                  ped3.setItem(0, ItemStack.EMPTY);
                  ped1.setChanged();
                  ped2.setChanged();
                  ped3.setChanged();
               }

               if ((
                     !infused
                        || ItemMobAspect.lastUsedTabletMatches(stack1, this)
                           && ItemMobAspect.lastUsedTabletMatches(stack2, this)
                           && ItemMobAspect.lastUsedTabletMatches(stack3, this)
                  )
                  && !level.isClientSide) {
                  spawnEntity((ServerLevel)level, summonerPos, recipe, infused);
                  sendFx((ServerLevel)level, summonerPos, ped1, ped2, ped3, aspect1, aspect2, aspect3);
               }

               if (infused) {
                  ItemMobAspect.markLastUsedTablet(stack1, this);
                  ItemMobAspect.markLastUsedTablet(stack2, this);
                  ItemMobAspect.markLastUsedTablet(stack3, this);
                  ped1.setChanged();
                  ped2.setChanged();
                  ped3.setChanged();
               }

               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static void sendFx(
      ServerLevel level,
      BlockPos summonerPos,
      ArcanePedestalBlockEntity ped1,
      ArcanePedestalBlockEntity ped2,
      ArcanePedestalBlockEntity ped3,
      Aspect a1,
      Aspect a2,
      Aspect a3
   ) {
      sendTrail(level, ped1.getBlockPos(), summonerPos, a1.getColor());
      sendTrail(level, ped2.getBlockPos(), summonerPos, a2.getColor());
      sendTrail(level, ped3.getBlockPos(), summonerPos, a3.getColor());
   }

   private static void sendTrail(ServerLevel level, BlockPos from, BlockPos to, int color) {
      byte dx = (byte)(from.getX() - to.getX());
      byte dy = (byte)(from.getY() - to.getY());
      byte dz = (byte)(from.getZ() - to.getZ());
      PacketHandler.sendToAllAround(new PacketFXEssenceSource(from, dx, dy, dz, color), level, from, 32.0);
   }

   private static void spawnEntity(ServerLevel level, BlockPos pos, EnumMobAspect recipe, boolean infused) {
      Entity entity = createEntityForDimension(level, recipe);
      if (entity != null) {
         entity.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
         if (Config.summonerDenyDropAspect || infused) {
            entity.getPersistentData().putBoolean("tc.summoner_no_aspect_drop", true);
         }

         level.addFreshEntity(entity);
         if (entity instanceof Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null);
            mob.playAmbientSound();
         }
      }
   }

   private static Entity createEntityForDimension(ServerLevel level, EnumMobAspect recipe) {
      if (recipe == EnumMobAspect.SKELETON && level.dimensionType().ultraWarm()) {
         Optional<EntityType<?>> witherSkeleton = EntityType.byString("minecraft:wither_skeleton");
         if (witherSkeleton.isPresent()) {
            return witherSkeleton.get().create(level);
         }
      }

      return recipe.createEntity(level);
   }

   public static boolean denyAspectDrop(Entity entity) {
      return entity.getPersistentData().getBoolean("tc.summoner_no_aspect_drop");
   }
}
