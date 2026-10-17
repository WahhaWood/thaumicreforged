package thaumcraft.block.bellows;

import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class BellowsBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   private static final Field COOKING_PROGRESS_FIELD;
   private static final Field COOKING_TOTAL_TIME_FIELD;
   public float inflation = 1.0F;
   boolean direction = false;
   boolean firstrun = true;
   public boolean onVanillaFurnace = false;
   public int delay = 0;

   public BellowsBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ARCANE_BELLOWS.get(), blockPos, blockState);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (!this.gettingPower()) {
         if (this.firstrun) {
            this.inflation = level.random.nextFloat() * 0.5F;
         }

         this.firstrun = false;
         if (this.inflation > 0.0F && !this.direction) {
            this.inflation -= 0.075F;
         }

         if (this.inflation <= 0.0F && !this.direction) {
            this.direction = true;
         }

         if (this.inflation < 1.0F && this.direction) {
            this.inflation += 0.025F;
         }

         if (this.inflation >= 1.0F && this.direction) {
            this.direction = false;
            level.playLocalSound(
               this.worldPosition,
               SoundEvents.GHAST_SHOOT,
               SoundSource.BLOCKS,
               0.01F,
               0.5F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F,
               false
            );
         }
      }
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (!this.gettingPower()) {
         this.delay++;
         if (this.delay >= 2) {
            this.delay = 0;
            Direction dir = this.getFacing();
            BlockPos targetPos = pos.relative(dir);
            if (level.getBlockEntity(targetPos) instanceof AbstractFurnaceBlockEntity furnace) {
               this.onVanillaFurnace = true;
               int progress = getCookingProgress(furnace);
               int total = getCookingTotalTime(furnace);
               if (progress > 0 && progress < total - 1) {
                  setCookingProgress(furnace, progress + 1);
               }
            } else {
               this.onVanillaFurnace = false;
            }
         }
      }
   }

   public boolean gettingPower() {
      return this.level.hasNeighborSignal(this.worldPosition);
   }

   public static int getBellows(Level level, BlockPos blockPos, Direction[] directions) {
      int bellows = 0;

      for (Direction direction : directions) {
         BlockPos pos = blockPos.relative(direction);
         if (level.getBlockEntity(pos) instanceof BellowsBlockEntity bell && bell.getFacing() == direction.getOpposite() && !level.hasNeighborSignal(pos)) {
            bellows++;
         }
      }

      return bellows;
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      this.onVanillaFurnace = compound.getBoolean("onVanillaFurnace");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      compound.putBoolean("onVanillaFurnace", this.onVanillaFurnace);
   }

   private static int getCookingProgress(AbstractFurnaceBlockEntity furnace) {
      try {
         return COOKING_PROGRESS_FIELD.getInt(furnace);
      } catch (IllegalAccessException var2) {
         throw new IllegalStateException("Unable to read furnace cooking progress", var2);
      }
   }

   private static int getCookingTotalTime(AbstractFurnaceBlockEntity furnace) {
      try {
         return COOKING_TOTAL_TIME_FIELD.getInt(furnace);
      } catch (IllegalAccessException var2) {
         throw new IllegalStateException("Unable to read furnace cooking total time", var2);
      }
   }

   private static void setCookingProgress(AbstractFurnaceBlockEntity furnace, int value) {
      try {
         COOKING_PROGRESS_FIELD.setInt(furnace, value);
      } catch (IllegalAccessException var3) {
         throw new IllegalStateException("Unable to update furnace cooking progress", var3);
      }
   }

   static {
      try {
         COOKING_PROGRESS_FIELD = AbstractFurnaceBlockEntity.class.getDeclaredField("cookingProgress");
         COOKING_PROGRESS_FIELD.setAccessible(true);
         COOKING_TOTAL_TIME_FIELD = AbstractFurnaceBlockEntity.class.getDeclaredField("cookingTotalTime");
         COOKING_TOTAL_TIME_FIELD.setAccessible(true);
      } catch (ReflectiveOperationException var1) {
         throw new ExceptionInInitializerError(var1);
      }
   }
}
