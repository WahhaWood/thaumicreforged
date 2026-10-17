package thaumcraft.block.infernal_furnace;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.blockentity.ThaumcraftInvBlockEntity;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.block.bellows.BellowsBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class InfernalFurnaceBlockEntity extends ThaumcraftInvBlockEntity {
   public int furnaceCookTime = 0;
   public int furnaceMaxCookTime = 0;
   public int speedyTime = 0;
   private int facingX = 0;
   private int facingZ = 0;
   private boolean facingInitialized = false;
   private boolean destroy = false;

   public InfernalFurnaceBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.INFERNAL_FURNACE.get(), pos, state, 32);
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.loadAdditional(tag, provider);
      this.furnaceCookTime = tag.getShort("CookTime");
      this.speedyTime = tag.getShort("SpeedyTime");
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.saveAdditional(tag, provider);
      tag.putShort("CookTime", (short)this.furnaceCookTime);
      tag.putShort("SpeedyTime", (short)this.speedyTime);
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, InfernalFurnaceBlockEntity be) {
      Objects.requireNonNull(be);
      be.tick(level, pos, state);
   }

   /** Порт TileArcaneFurnace (1.7.10): плавка предметов из инвентаря, меха, ignis-разгон, выброс результата. */
   private void tick(Level level, BlockPos pos, BlockState state) {
      if (!this.facingInitialized) {
         this.initFacing(level, pos, state);
      }

      boolean cookedflag = false;
      if (this.furnaceCookTime > 0) {
         this.furnaceCookTime--;
         cookedflag = true;
      }

      if (cookedflag && this.speedyTime > 0) {
         this.speedyTime--;
      }

      if (this.speedyTime <= 0) {
         this.speedyTime = VisNetHandler.drainVis(level, pos, Aspect.FIRE, 5);
      }

      if (this.furnaceMaxCookTime == 0) {
         this.furnaceMaxCookTime = this.calcCookTime(level, pos);
      }

      if (this.furnaceCookTime > this.furnaceMaxCookTime) {
         this.furnaceCookTime = this.furnaceMaxCookTime;
      }

      if (this.furnaceCookTime == 0 && cookedflag) {
         for (int a = 0; a < this.getContainerSize(); a++) {
            ItemStack input = this.getItem(a);
            if (input.isEmpty()) {
               continue;
            }

            var recipe = level.getRecipeManager()
               .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
            if (recipe.isPresent()) {
               ItemStack result = recipe.get().value().getResultItem(level.registryAccess()).copy();
               this.ejectItem(level, pos, result, input, recipe.get().value().getExperience());
               level.blockEvent(pos, state.getBlock(), 3, 0);
               input.shrink(1);
               if (input.isEmpty()) {
                  this.setItem(a, ItemStack.EMPTY);
               }

               break;
            }
         }
      }

      if (this.furnaceCookTime == 0 && !cookedflag) {
         for (int a = 0; a < this.getContainerSize(); a++) {
            if (!this.getItem(a).isEmpty() && this.canSmelt(level, a)) {
               this.furnaceMaxCookTime = this.calcCookTime(level, pos);
               this.furnaceCookTime = this.furnaceMaxCookTime;
               break;
            }
         }
      }
   }

   /** Направление выброса — в сторону решётки (index 14), как md 10 в 1.7.10. */
   private void initFacing(Level level, BlockPos pos, BlockState state) {
      this.facingInitialized = true;
      this.facingX = 0;
      this.facingZ = 0;

      for (Direction dir : Direction.Plane.HORIZONTAL) {
         BlockState neighbor = level.getBlockState(pos.relative(dir));
         if (neighbor.getBlock() instanceof InfernalFurnaceBlock
            && (Integer)neighbor.getValue(InfernalFurnaceBlock.INDEX) == InfernalFurnaceBlock.OPENING_INDEX) {
            this.facingX = dir.getStepX();
            this.facingZ = dir.getStepZ();
            return;
         }
      }

      if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
         Direction facing = (Direction)state
            .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
         this.facingX = facing.getStepX();
         this.facingZ = facing.getStepZ();
      }
   }

   private int getBellows(Level level, BlockPos pos) {
      int bellows = 0;
      Direction[] dirs = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

      for (Direction dir : dirs) {
         BlockPos bellowPos = pos.relative(dir, 2);
         if (level.getBlockEntity(bellowPos) instanceof BellowsBlockEntity bell
            && bell.getFacing() == dir.getOpposite()
            && !level.hasNeighborSignal(bellowPos)) {
            bellows++;
         }
      }

      return Math.min(3, bellows);
   }

   private int calcCookTime(Level level, BlockPos pos) {
      return (this.speedyTime > 0 ? 80 : 140) - 20 * this.getBellows(level, pos);
   }

   private boolean canSmelt(Level level, int slot) {
      ItemStack input = this.getItem(slot);
      if (input.isEmpty()) {
         return false;
      }

      return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level).isPresent();
   }

   /** Кладёт предмет в инвентарь; несъедобное (без рецепта) сгорает. */
   public boolean addItemsToInventory(ItemStack items) {
      for (int a = 0; a < this.getContainerSize(); a++) {
         ItemStack slot = this.getItem(a);
         if (!slot.isEmpty()
            && ItemStack.isSameItemSameComponents(slot, items)
            && slot.getCount() + items.getCount() <= slot.getMaxStackSize()) {
            slot.grow(items.getCount());
            if (this.level != null && !this.canSmelt(this.level, a)) {
               this.destroyItem(a);
            }

            this.setChanged();
            return true;
         }
      }

      for (int a = 0; a < this.getContainerSize(); a++) {
         if (this.getItem(a).isEmpty()) {
            this.setItem(a, items.copy());
            if (this.level != null && !this.canSmelt(this.level, a)) {
               this.destroyItem(a);
            }

            this.setChanged();
            return true;
         }
      }

      return false;
   }

   private void destroyItem(int slot) {
      this.setItem(slot, ItemStack.EMPTY);
      if (this.level == null) {
         return;
      }

      this.level.playSound(
         null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
         SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
         0.3F, 2.6F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.8F
      );
      if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
         serverLevel.sendParticles(
            ParticleTypes.LAVA,
            this.worldPosition.getX() + this.level.random.nextFloat(),
            this.worldPosition.getY() + 1.0,
            this.worldPosition.getZ() + this.level.random.nextFloat(),
            1, 0.0, 0.0, 0.0, 0.0
         );
      }
   }

   /** Выброс результата/бонуса/опыта в сторону решётки (порт ejectItem из 1.7.10). */
   private void ejectItem(Level level, BlockPos pos, ItemStack result, ItemStack smelted, float experience) {
      float lx = 0.5F + this.facingX * 1.2F;
      float lz = 0.5F + this.facingZ * 1.2F;
      int bellows = this.getBellows(level, pos);
      this.spawnItem(level, pos, result, lx, lz);

      // TC4 starts the bonus at 0: 1 in 4 without bellows, a 0.44 chance per bellows otherwise.
      ItemStack bonus = ThaumcraftApi.getSmeltingBonus(smelted);
      if (!bonus.isEmpty()) {
         int count = 0;
         if (bellows == 0) {
            if (level.random.nextInt(4) == 0) {
               count++;
            }
         } else {
            for (int a = 0; a < bellows; a++) {
               if (level.random.nextFloat() < 0.44F) {
                  count++;
               }
            }
         }

         if (count > 0) {
            this.spawnItem(level, pos, bonus.copyWithCount(count), lx, lz);
         }
      }

      // 1.7.10 считает опыт от числа получившихся предметов и не создаёт орбы вовсе,
      // если у рецепта xp == 0 (жёсткий минимум 1 выдавал орб даже за пустой рецепт).
      int xp = result.getCount();

      if (experience == 0.0F) {
         xp = 0;
      } else if (experience < 1.0F) {
         int exact = Mth.floor(xp * experience);

         if (exact < Mth.ceil(xp * experience) && level.random.nextFloat() < xp * experience - exact) {
            exact++;
         }

         xp = exact;
      }

      while (xp > 0) {
         int orbValue = ExperienceOrb.getExperienceValue(xp);
         xp -= orbValue;
         ExperienceOrb orb = new ExperienceOrb(level, pos.getX() + lx, pos.getY() + 0.4, pos.getZ() + lz, orbValue);
         orb.setDeltaMovement(this.motionX(level), 0.0, this.motionZ(level));
         level.addFreshEntity(orb);
      }
   }

   private void spawnItem(Level level, BlockPos pos, ItemStack stack, float lx, float lz) {
      if (stack.isEmpty()) {
         return;
      }

      ItemEntity item = new ItemEntity(
         level, pos.getX() + lx, pos.getY() + 0.4, pos.getZ() + lz, stack
      );
      item.setDeltaMovement(this.motionX(level), 0.0, this.motionZ(level));
      level.addFreshEntity(item);
   }

   private double motionX(Level level) {
      return this.facingX == 0
         ? (level.random.nextFloat() - level.random.nextFloat()) * 0.03F
         : this.facingX * 0.13F;
   }

   private double motionZ(Level level) {
      return this.facingZ == 0
         ? (level.random.nextFloat() - level.random.nextFloat()) * 0.03F
         : this.facingZ * 0.13F;
   }

   private static BooleanProperty getSideProperty(Direction dir) {
      return switch (dir) {
         case SOUTH -> CrossCollisionBlock.SOUTH;
         case WEST -> CrossCollisionBlock.WEST;
         case EAST -> CrossCollisionBlock.EAST;
         default -> CrossCollisionBlock.NORTH;
      };
   }

   private record SmeltResult(ItemStack result, float experience, boolean validRecipe) {
      static final InfernalFurnaceBlockEntity.SmeltResult EMPTY = new InfernalFurnaceBlockEntity.SmeltResult(ItemStack.EMPTY, 0.0F, false);
   }
}
