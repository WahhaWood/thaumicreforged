package thaumcraft.research;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.obj.ObjectAspectUtils;
import thaumcraft.entities.smart.golem.EntityGolemBase;

final class ScanIdentity {
   private ScanIdentity() {
   }

   static int getItemHash(ItemStack stack) {
      return getItemIdentity(stack).hashCode();
   }

   static int getBlockHash(Block block) {
      return getBlockIdentity(block).hashCode();
   }

   static int getEntityHash(Entity entity) {
      return getEntityIdentity(entity).hashCode();
   }

   private static String getItemIdentity(ItemStack stack) {
      String scanKey = ThaumcraftApi.getObjectAspectScanKey(stack);
      if (scanKey != null) {
         return scanKey;
      } else {
         try {
            String identity = ObjectAspectUtils.getItemKey(stack.getItem()) + ":" + ObjectAspectUtils.getLegacyMeta(stack);
            CompoundTag scanTag = ObjectAspectUtils.getScanTag(stack);
            if (scanTag != null) {
               identity = identity + "|" + scanTag;
            }

            return identity;
         } catch (Exception var4) {
            return "oops:" + stack;
         }
      }
   }

   private static String getBlockIdentity(Block block) {
      String scanKey = ThaumcraftApi.getBlockAspectScanKey(block.defaultBlockState());
      if (scanKey != null) {
         return scanKey;
      } else {
         ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
         return key == null ? block.toString() : key.toString();
      }
   }

   private static String getEntityIdentity(Entity entity) {
      ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      String identity = key == null ? "generic" : key.toString();
      if (entity instanceof Player) {
         identity = "player_" + entity.getScoreboardName();
      }

      String entityAspectKey = ThaumcraftApi.getEntityAspectScanKey(entity);
      if (entityAspectKey != null) {
         identity = identity + "|" + entityAspectKey;
      }

      if (EntityAspectResolver.isChildEntity(entity)) {
         identity = identity + "CHILD";
      }

      if (entity instanceof ZombieVillager) {
         identity = identity + "VILLAGER";
      }

      if (entity instanceof Creeper creeper) {
         if (creeper.isIgnited()) {
            identity = identity + "FLASHING";
         }

         if (creeper.isPowered()) {
            identity = identity + "POWERED";
         }
      }

      if (entity instanceof EntityGolemBase golemBase) {
         identity = identity + golemBase.getGolemType().name();
      }

      return identity;
   }
}
