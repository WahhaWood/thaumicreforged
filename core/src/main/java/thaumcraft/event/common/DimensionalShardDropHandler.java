package thaumcraft.event.common;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModItems;

public class DimensionalShardDropHandler {
   @SubscribeEvent
   public void onLivingDrops(LivingDropsEvent event) {
      if (event.getSource().getEntity() instanceof Player) {
         if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            if (event.getEntity() instanceof EnderMan
               && isDropAllowed(serverLevel, Config.enderCrystalDropCheckWorld, Level.END)
               && roll(serverLevel, Config.enderCrystalDropChance)) {
               addDrop(event, (Item)ModItems.ENDER_SHARD.get());
            }

            if ((event.getEntity() instanceof ZombifiedPiglin || event.getEntity() instanceof Piglin)
               && isDropAllowed(serverLevel, Config.netherCrystalDropCheckWorld, Level.NETHER)
               && roll(serverLevel, Config.netherCrystalDropChance)) {
               addDrop(event, (Item)ModItems.NETHER_SHARD.get());
            }
         }
      }
   }

   private static boolean isDropAllowed(ServerLevel level, boolean checkDimension, ResourceKey<Level> expected) {
      return !checkDimension || level.dimension() == expected;
   }

   private static boolean roll(ServerLevel level, float chance) {
      if (chance <= 0.0F) {
         return false;
      } else {
         return chance >= 1.0F ? true : level.random.nextFloat() <= chance;
      }
   }

   private static void addDrop(LivingDropsEvent event, Item dropItem) {
      LivingEntity entity = event.getEntity();
      ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(dropItem));
      event.getDrops().add(itemEntity);
   }
}
