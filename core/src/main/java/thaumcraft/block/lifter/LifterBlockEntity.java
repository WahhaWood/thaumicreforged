package thaumcraft.block.lifter;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;

public class LifterBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   private int counter = 0;
   public int rangeAbove = 0;
   public boolean requiresUpdate = true;
   public boolean lastPowerState = false;
   private final Set<Player> playersInRange = new HashSet<>();

   public LifterBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.LIFTER.get(), blockPos, blockState);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.counter++;
      if (this.requiresUpdate || this.counter % 20 == 0) {
         this.updateRange();
      }

      if (this.rangeAbove > 0 && !this.gettingPower()) {
         for (Entity e : level.getEntitiesOfClass(
            Entity.class,
            new AABB(pos.above().getX(), pos.above().getY(), pos.above().getZ(), pos.getX() + 1, pos.getY() + 1 + this.rangeAbove, pos.getZ() + 1)
         )) {
            if ((e instanceof ItemEntity || e.isPushable() || e instanceof Horse) && e.isAlive() && !e.isShiftKeyDown()) {
               int miny = (int)(e.getBoundingBox().minY - 2.0);
               if (e.onGround()) {
                  miny = Mth.floor(e.getBoundingBox().minY);
               }

               for (int a = 0; a < 3; a++) {
                  Thaumcraft.setup
                     .smokeSpiral(level, e.getX(), e.getBoundingBox().minY + e.getBbHeight() / 2.0F, e.getZ(), 1.5F, level.random.nextInt(360), miny, 14540253);
               }
            }
         }
      }
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      this.counter++;
      if (this.requiresUpdate || this.counter % 20 == 0) {
         this.updateRange();
      }

      if (this.rangeAbove > 0 && !this.gettingPower()) {
         AABB aabb = new AABB(
            pos.getX(), pos.getY() + 1, pos.getZ(),
            pos.getX() + 1, pos.getY() + 1 + this.rangeAbove, pos.getZ() + 1
         );
         java.util.List<Entity> targets = level.getEntitiesOfClass(Entity.class, aabb);
         for (Entity e : targets) {
            if ((e instanceof ItemEntity || e.isPushable() || e instanceof Horse || e instanceof Player) && e.isAlive()) {
               var motion = e.getDeltaMovement();
               if (e.isShiftKeyDown()) {
                  if (motion.y < 0.0) {
                     e.setDeltaMovement(motion.x, motion.y * 0.9, motion.z);
                  }
               } else if (motion.y < 0.35) {
                  e.setDeltaMovement(motion.x, motion.y + 0.1, motion.z);
               }
               e.resetFallDistance();
               e.hurtMarked = true;
            }
         }
      }
   }

   private void updateRange() {
      this.lastPowerState = this.gettingPower();
      this.requiresUpdate = false;
      int max = 10;
      int i = 1;

      while (true) {
         BlockPos blockPos = this.worldPosition.below(i);
         if (!this.level.getBlockState(blockPos).is((Block)ModBlocks.LIFTER.get()) || this.level.hasNeighborSignal(blockPos)) {
            for (this.rangeAbove = 0; this.rangeAbove < max; this.rangeAbove++) {
               BlockPos blockPosx = this.worldPosition.above(this.rangeAbove + 1);
               BlockState blockState = this.level.getBlockState(blockPosx);
               if (!blockState.isAir() && !blockState.getCollisionShape(this.level, blockPosx).isEmpty()) {
                  break;
               }
            }

            return;
         }

         max += 10;
         i++;
      }
   }

   public boolean gettingPower() {
      return this.level.hasNeighborSignal(this.worldPosition) || this.level.hasNeighborSignal(this.worldPosition.above());
   }
}
