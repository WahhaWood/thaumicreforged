package thaumcraft.block.crucible;

import java.awt.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.wands.IWandable;
import thaumcraft.block.bellows.BellowsBlockEntity;
import thaumcraft.block.thaumatorium.ThaumatoriumBlockEntity;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.research.PlayerKnowledge;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.EventHooks;
import java.util.Set;

public class CrucibleBlockEntity extends ThaumcraftBlockEntity implements IFluidHandler, IWandable, IAspectContainer, ITickable {
   public short heat = 0;
   public AspectList aspects = new AspectList();
   public final int maxTags = 100;
   int bellows = -1;
   private int delay = 0;
   public FluidTank tank = new FluidTank(1000, fluidStack -> fluidStack.getFluid() == Fluids.WATER);
   private long counter = -100L;
   int prevcolor = 0;
   int prevx = 0;
   int prevy = 0;

   public CrucibleBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.CRUCIBLE.get(), blockPos, blockState);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      this.heat = compound.getShort("Heat");
      this.tank.readFromNBT(provider, compound);
      if (compound.contains("Empty")) {
         this.tank.setFluid(FluidStack.EMPTY);
      }

      this.aspects.load(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      compound.putShort("Heat", this.heat);
      this.tank.writeToNBT(provider, compound);
      this.aspects.save(compound);
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag compound, Provider provider) {
      super.loadAdditional(compound, provider);
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag compound, Provider provider) {
      super.saveAdditional(compound, provider);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.counter++;
      int prevheat = this.heat;
      if (this.tank.getFluidAmount() > 0) {
         this.drawEffects();
      }

      if (prevheat < 151 && this.heat >= 151) {
         this.heat++;
      }
   }

   private void drawEffects() {
      RandomSource rand = this.level.random;
      BlockPos blockPos = this.worldPosition;
      float x = blockPos.getX();
      float y = blockPos.getY();
      float z = blockPos.getZ();
      if (this.heat > 150) {
         Thaumcraft.setup.crucibleFroth(this.level, x + 0.2F + rand.nextFloat() * 0.6F, y + this.getFluidHeight(), z + 0.2F + rand.nextFloat() * 0.6F);
         if (rand.nextInt(15) == 0) {
            this.level.playLocalSound(
               x + 0.5,
               y + 0.5,
               z + 0.5,
               (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.BUBBLE.get(),
               SoundSource.BLOCKS,
               0.2F + rand.nextFloat() * 0.1F,
               0.9F + rand.nextFloat() * 0.2F,
               false
            );
         }
         if (rand.nextInt(3) == 0) {
            float aspectIntensity = Math.min(1.0F, this.tagAmount() / 50.0F);
            float r = aspectIntensity > 0 ? (0.28F + aspectIntensity * 0.50F) : 0.3F;
            float g = aspectIntensity > 0 ? (0.52F - aspectIntensity * 0.40F) : 0.5F;
            float b = aspectIntensity > 0 ? (0.88F - aspectIntensity * 0.02F) : 0.85F;
            Thaumcraft.setup.crucibleBubble(this.level, x + 0.2F + rand.nextFloat() * 0.6F, y + 0.05F + this.getFluidHeight(), z + 0.2F + rand.nextFloat() * 0.6F, r, g, b);
         }
         if (this.tagAmount() > 100) {
            for (int a = 0; a < 2; a++) {
               Thaumcraft.setup.crucibleFrothDown(this.level, x, y + 1.0F, z + rand.nextFloat());
               Thaumcraft.setup.crucibleFrothDown(this.level, x + 1.0F, y + 1.0F, z + rand.nextFloat());
               Thaumcraft.setup.crucibleFrothDown(this.level, x + rand.nextFloat(), y + 1.0F, z);
               Thaumcraft.setup.crucibleFrothDown(this.level, x + rand.nextFloat(), y + 1.0F, z + 1.0F);
            }
         }
      }

      int bubbleFreq = this.heat > 150 ? 3 : 6;
      if (rand.nextInt(bubbleFreq) == 0 && this.aspects.size() > 0) {
         int color = this.aspects.getAspects()[rand.nextInt(this.aspects.size())].getColor() + -16777216;
         int xX = 5 + rand.nextInt(22);
         int yY = 5 + rand.nextInt(22);
         this.delay = rand.nextInt(10);
         this.prevcolor = color;
         this.prevx = xX;
         this.prevy = yY;
         Color c = new Color(color);
         float r = c.getRed() / 255.0F;
         float g = c.getGreen() / 255.0F;
         float b = c.getBlue() / 255.0F;
         Thaumcraft.setup.crucibleBubble(this.level, x + xX / 32.0F + 0.015625F, y + 0.05F + this.getFluidHeight(), z + yY / 32.0F + 0.015625F, r, g, b);
      }
   }

   private void push(ItemEntity entity) {
      RandomSource rand = this.level.random;
      entity.setDeltaMovement((rand.nextFloat() - rand.nextFloat()) * 0.2F, 0.35F, (rand.nextFloat() - rand.nextFloat()) * 0.2F);
      this.level.playSound(null, entity, SoundEvents.SNIFFER_DROP_SEED, SoundSource.BLOCKS, 0.2F, (rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F);
   }


   /** Порт TileCrucible (1.7.10): жар от огня/лавы/нитора, кипение, переполнение флюксом. */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      this.counter++;
      int prevheat = this.heat;
      if (this.bellows < 0) {
         this.getBellows(level, pos);
      }

      if (this.tank.getFluidAmount() <= 0) {
         if (this.heat > 0) {
            this.heat--;
         }
      } else {
         if (isHeatSource(level.getBlockState(pos.below()))) {
            if (this.heat < 200) {
               this.heat = (short)(this.heat + 1 + this.bellows * 2);
               if (prevheat < 151 && this.heat >= 151) {
                  this.setChanged();
                  this.markForUpdate();
               }
            }
         } else if (this.heat > 0) {
            this.heat--;
            if (this.heat == 149) {
               this.setChanged();
               this.markForUpdate();
            }
         }
      }

      if (this.counter % 5L == 0L && this.heat > 150 && this.tank.getFluidAmount() > 0) {
         net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(
            this.worldPosition.getX() + 0.05, this.worldPosition.getY() + 0.25, this.worldPosition.getZ() + 0.05,
            this.worldPosition.getX() + 0.95, this.worldPosition.getY() + 1.7, this.worldPosition.getZ() + 0.95
         );
         for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (item.isAlive() && !item.getItem().isEmpty()) {
               this.attemptSmelt(item);
            }
         }
      }

      if (this.tagAmount() > 100 && this.counter % 5L == 0L) {
         this.takeRandomFromSource();
         this.spill(level, pos);
      }

      if (this.counter > 100L && this.heat > 150) {
         this.counter = 0L;
         if (this.tagAmount() > 0) {
            Aspect[] list = this.aspects.getAspects();
            Aspect a = list[level.random.nextInt(list.length)];
            if (a.isPrimal()) {
               Aspect[] list2 = this.aspects.getAspects();
               a = list2[level.random.nextInt(list2.length)];
            }

            this.tank.drain(2, IFluidHandler.FluidAction.EXECUTE);
            this.aspects.remove(a, 1);
            Aspect[] components = a.getComponents();
            if (!a.isPrimal() && components != null && components.length >= 2) {
               this.aspects.add(components[level.random.nextBoolean() ? 0 : 1], 1);
            } else {
               this.spill(level, pos);
            }
         }

         this.setChanged();
         this.markForUpdate();
      }
   }

   /** TC4 heats the crucible over lava, fire or nitor only (Material.lava / Material.fire / blockAiry:1). */
   public static boolean isHeatSource(BlockState below) {
      return below.is(Blocks.FIRE) || below.is(Blocks.SOUL_FIRE) || below.is(Blocks.LAVA) || below.is(ModBlocks.NITOR.get());
   }

   private void getBellows(Level level, BlockPos pos) {
      this.bellows = BellowsBlockEntity.getBellows(
         level, pos, new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}
      );
   }

   /** Попытка переплавить предмет в тигле (порт attemptSmelt). */
   public void attemptSmelt(ItemEntity entity) {
      if (this.level == null) {
         return;
      }

      ItemStack item = entity.getItem();
      if (item.isEmpty()) {
         return;
      }

      int stacksize = item.getCount();
      boolean bubble = false;
      boolean event = false;
      // Recipes follow the research of whoever threw the item, as in TC4; an item without a thrower
      // (hopper, dropper, offline player) only dissolves.
      Player thrower = entity.getOwner() instanceof Player p ? p : null;
      Set<String> research = thrower == null ? Set.of() : PlayerKnowledge.of(thrower).getResearchCompleted();

      for (int a = 0; a < stacksize; a++) {
         CrucibleRecipe rc = ThaumcraftCraftingManager.findCrucibleRecipe(this.aspects, item, research);
         if (rc != null && this.tank.getFluidAmount() > 0) {
            ItemStack out = rc.getResultItem().copy();
            if (thrower != null) {
               EventHooks.firePlayerCraftingEvent(thrower, out, new SimpleContainer(item.copyWithCount(1)));
            }

            this.aspects = rc.removeMatching(this.aspects);
            this.tank.drain(50, IFluidHandler.FluidAction.EXECUTE);
            this.ejectItem(out);
            event = true;
            stacksize--;
            this.counter = -250L;
         } else {
            AspectList ot = ThaumcraftCraftingManager.getObjectTags(item);
            ot = ThaumcraftCraftingManager.getBonusTags(item, ot);
            if (ot == null || ot.size() == 0) {
               this.push(entity);
               return;
            }

            for (Aspect tag : ot.getAspects()) {
               this.aspects.add(tag, ot.getAmount(tag));
            }

            bubble = true;
            stacksize--;
            this.counter = -150L;
         }
      }

      if (bubble) {
         this.level.playSound(
            null, entity, thaumcraft.init.registry.ModSounds.BUBBLE.get(), SoundSource.BLOCKS,
            0.2F, 1.0F + this.level.random.nextFloat() * 0.4F
         );
         this.markForUpdate();
         this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 2, 1);
      }

      if (event) {
         this.markForUpdate();
         this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 2, 5);
      }

      if (stacksize <= 0) {
         entity.discard();
      } else {
         item.setCount(stacksize);
         entity.setItem(item);
      }

      this.setChanged();
   }

   private void ejectItem(ItemStack stack) {
      if (stack.isEmpty() || this.level == null) {
         return;
      }

      thaumcraft.entities.brainless.special.SpecialItemEntity out = new thaumcraft.entities.brainless.special.SpecialItemEntity(
         this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.71, this.worldPosition.getZ() + 0.5, stack
      );
      out.setDeltaMovement((this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.01F, 0.1, (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.01F);
      this.level.addFreshEntity(out);
   }

   /** Перелив флюкса при переполнении (порт spill). */
   private void spill(Level level, BlockPos pos) {
      if (level.random.nextInt(4) != 0) {
         return;
      }

      BlockPos above = pos.above();
      if (level.isEmptyBlock(above)) {
         boolean gas = level.random.nextBoolean();
         level.setBlock(above, (gas ? thaumcraft.init.registry.ModBlocks.FLUX_GAS.get() : thaumcraft.init.registry.ModBlocks.FLUX_GOO.get()).defaultBlockState(), 3);
         return;
      }

      BlockPos target = pos.offset(level.random.nextInt(3) - 1, level.random.nextInt(3) - 1, level.random.nextInt(3) - 1);
      if (level.isEmptyBlock(target)) {
         boolean gas = level.random.nextBoolean();
         level.setBlock(target, (gas ? thaumcraft.init.registry.ModBlocks.FLUX_GAS.get() : thaumcraft.init.registry.ModBlocks.FLUX_GOO.get()).defaultBlockState(), 3);
      }
   }

   public AspectList takeRandomFromSource() {
      AspectList output = new AspectList();
      if (this.aspects.size() > 0 && this.level != null) {
         Aspect tag = this.aspects.getAspects()[this.level.random.nextInt(this.aspects.getAspects().length)];
         output.add(tag, 1);
         this.aspects.remove(tag, 1);
      }

      this.setChanged();
      this.markForUpdate();
      return output;
   }

   /** Слив остатков (жезл с шифтом, порт spillRemnants). */
   public void spillRemnants() {
      if (this.level == null) {
         return;
      }

      if (this.tank.getFluidAmount() > 0 || this.aspects.visSize() > 0) {
         this.tank.setFluid(FluidStack.EMPTY);
         for (int a = 0; a < this.aspects.visSize() / 2; a++) {
            this.spill(this.level, this.worldPosition);
         }

         this.aspects = new AspectList();
      }

      this.setChanged();
      this.markForUpdate();
   }

   /** Тикер для блока. */
   public int tagAmount() {
      int tt = 0;
      if (this.aspects.size() <= 0) {
         return 0;
      } else {
         for (Aspect tag : this.aspects.getAspects()) {
            tt += this.aspects.getAmount(tag);
         }

         return tt;
      }
   }

   public float getFluidHeight() {
      float base = 0.3F + 0.5F * ((float)this.tank.getFluidAmount() / this.tank.getCapacity());
      float out = base + this.tagAmount() / 100.0F * (1.0F - base);
      if (out > 1.0F) {
         out = 1.001F;
      }

      if (out == 1.0F) {
         out = 0.9999F;
      }

      return out;
   }

   public boolean triggerEvent(int i, int j) {
      if (i == 1) {
         if (this.level.isClientSide) {
            Thaumcraft.setup.blockSparkle(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), -9999, 5);
         }

         return true;
      } else if (i != 2) {
         return super.triggerEvent(i, j);
      } else {
         Thaumcraft.setup.crucibleBoilSound(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
         if (this.level.isClientSide) {
            for (int q = 0; q < 10; q++) {
               Thaumcraft.setup.crucibleBoil(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this, j);
            }
         }

         return true;
      }
   }

   public int getTanks() {
      return this.tank.getTanks();
   }

   @NotNull
   public FluidStack getFluidInTank(int tank) {
      return this.tank.getFluidInTank(tank);
   }

   public int getTankCapacity(int tank) {
      return this.tank.getTankCapacity(tank);
   }

   public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
      return this.tank.isFluidValid(tank, stack);
   }

   public int fill(FluidStack resource, FluidAction action) {
      if (!resource.isEmpty()
         && resource.getFluid() == Fluids.WATER
         && !(this.level.getBlockEntity(this.worldPosition.above()) instanceof ThaumatoriumBlockEntity)) {
         if (action == FluidAction.EXECUTE) {
            this.setChanged();
            this.markForUpdate();
         }

         return this.tank.fill(resource, action);
      } else {
         return 0;
      }
   }

   @NotNull
   public FluidStack drain(FluidStack resource, FluidAction action) {
      if (resource != null && resource.isFluidEqual(this.tank.getFluid())) {
         if (action == FluidAction.EXECUTE) {
            this.setChanged();
            this.markForUpdate();
         }

         return this.tank.drain(resource, action);
      } else {
         return FluidStack.EMPTY;
      }
   }

   @NotNull
   public FluidStack drain(int maxDrain, FluidAction action) {
      if (action == FluidAction.EXECUTE) {
         this.setChanged();
         this.markForUpdate();
      }

      return this.tank.drain(maxDrain, action);
   }

   @NotNull
   public IFluidHandler getFluidHandler(@Nullable Direction side) {
      return this;
   }

   public void invalidateCaps() {
   }

   @Override
   public int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos blockPos, @NotNull Direction direction
   ) {
      return 0;
   }

   @NotNull
   @Override
   public ItemStack onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      if (!level.isClientSide && player.isShiftKeyDown()) {
         this.spillRemnants();
      }

      return player.getItemInHand(hand);
   }

   @Override
   public void onUsingWandTick(@NotNull ItemStack stack, @NotNull Player player, int count) {
   }

   @Override
   public void onWandStoppedUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player, int count) {
   }

   @Override
   public AspectList getAspects() {
      return this.aspects;
   }

   @Override
   public void setAspects(AspectList aspectList) {
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      return 0;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      return false;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return 0;
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }
}
