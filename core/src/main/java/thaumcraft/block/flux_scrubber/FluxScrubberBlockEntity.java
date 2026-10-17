package thaumcraft.block.flux_scrubber;

import java.util.ArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.block.fluid.AbstractLiquidBlock;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXBlockSparkle;

public class FluxScrubberBlockEntity extends ThaumcraftBlockEntity implements IEssenceTransport, ITickable {
   public int essence = 0;
   public int charges = 0;
   public int power = 0;
   public int count = 0;
   ArrayList<BlockPos> checklist = new ArrayList<>();

   public FluxScrubberBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.FLUX_SCRUBBER.get(), blockPos, blockState);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      // Phase of the bobbing animation, as TC4 seeded it on both sides.
      if (this.count == 0) {
         this.count = level.random.nextInt(1000);
      }
   }

   /** TC4 TileFluxScrubber.updateEntity: Aer from the vis network powers it, every 4 flux blocks cleaned have a 1 in 4 chance of a Praecantatio. */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.charges >= 4) {
         this.charges -= 4;
         if (level.random.nextInt(4) == 0) {
            this.essence = Math.min(4, this.essence + 1);
            this.setChanged();
         }
      }

      if (this.power < 5) {
         this.power += VisNetHandler.drainVis(level, pos, Aspect.AIR, 10);
      }

      if (this.power >= 5) {
         this.checkFlux(level, pos);
      }
   }

   /** Looks at 16 positions per tick from a shuffled list of the 33x33x33 cube and lowers the first flux goo or gas within 16 blocks by one level. */
   private void checkFlux(Level level, BlockPos pos) {
      int distance = 16;
      if (this.checklist.isEmpty()) {
         for (BlockPos p : BlockPos.betweenClosed(pos.offset(-distance, -distance, -distance), pos.offset(distance, distance, distance))) {
            this.checklist.add(p.immutable());
         }

         Util.shuffle(this.checklist, level.random);
      }

      for (int cc = 0; cc < 16 && !this.checklist.isEmpty(); cc++) {
         // TC4 took the head of the shuffled list; the tail is the same draw without shifting 36k entries.
         BlockPos p = this.checklist.remove(this.checklist.size() - 1);
         BlockState target = level.getBlockState(p);
         if (isFlux(target) && p.getCenter().distanceToSqr(pos.getCenter()) < distance * distance) {
            this.power -= 5;
            FluidState fluid = level.getFluidState(p);
            int amount = AbstractLiquidBlock.getFiniteAmount(fluid);
            if (amount > 1 && fluid.getType() instanceof FlowingFluid flowing) {
               level.setBlock(p, AbstractLiquidBlock.createFiniteBlockState(flowing, amount - 1), 3);
            } else {
               level.removeBlock(p, false);
            }

            PacketHandler.sendToAllAround(new PacketFXBlockSparkle(p, 14483711), level, p.getX(), p.getY(), p.getZ(), 32.0);
            this.charges++;
            this.setChanged();
            return;
         }
      }
   }

   private static boolean isFlux(BlockState state) {
      return state.is(ModBlocks.FLUX_GOO.get()) || state.is(ModBlocks.FLUX_GAS.get());
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadAdditional(compound, provider);
      this.charges = compound.getInt("charges");
      this.power = compound.getInt("power");
      this.essence = compound.getInt("essence");
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveAdditional(compound, provider);
      compound.putInt("charges", this.charges);
      compound.putInt("power", this.power);
      compound.putInt("essence", this.essence);
   }

   @Override
   public boolean isConnectable(Direction face) {
      return face == this.getFacing();
   }

   @Override
   public boolean canOutputTo(Direction face) {
      return face == this.getFacing();
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return false;
   }

   @Override
   public void setSuction(Aspect aspect, int amount) {
   }

   @Override
   public boolean renderExtendedTube() {
      return false;
   }

   @Override
   public int getMinimumSuction() {
      return 0;
   }

   @Override
   public Aspect getSuctionType(Direction face) {
      return null;
   }

   @Override
   public int getSuctionAmount(Direction face) {
      return 0;
   }

   @Override
   public Aspect getEssenceType(Direction loc) {
      return Aspect.MAGIC;
   }

   @Override
   public int getEssenceAmount(Direction loc) {
      return this.essence;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction loc) {
      return 0;
   }

   /** TC4 takeEssentia: tubes on the front take the Praecantatio it made. */
   @Override
   public int extractEssence(Aspect aspect, int amount, net.minecraft.core.Direction loc) {
      int re = Math.min(this.essence, amount);
      this.essence -= re;
      this.setChanged();
      return re;
   }

}
