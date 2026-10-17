package thaumcraft.block.advanced_alchemy_furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class AdvAlchemyFurnaceNozzleBlockEntity extends ThaumcraftBlockEntity implements IAspectContainer, IEssenceTransport, ITickable {
   private Direction facing = null;
   private AdvAlchemyFurnaceBlockEntity furnace = null;
   private boolean initialized = false;

   public AdvAlchemyFurnaceNozzleBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ADVANCED_ALCHEMY_FURNACE_NOZZLE.get(), blockPos, blockState);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (!this.initialized) {
         this.findFurnace();
         this.initialized = true;
      }
   }

   private void findFurnace() {
      if (this.level != null) {
         if (this.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction blockFacing = (Direction)this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            this.facing = blockFacing.getOpposite();
            BlockPos furnacePos = this.worldPosition.relative(blockFacing);
            if (this.level.getBlockEntity(furnacePos) instanceof AdvAlchemyFurnaceBlockEntity advFurnace) {
               this.furnace = advFurnace;
               return;
            }
         }

         if (this.furnace == null) {
            for (Direction dir : Direction.values()) {
               if (this.level.getBlockEntity(this.worldPosition.relative(dir)) instanceof AdvAlchemyFurnaceBlockEntity advFurnace) {
                  this.facing = dir.getOpposite();
                  this.furnace = advFurnace;
                  break;
               }
            }
         }
      }
   }

   @Nullable
   public AdvAlchemyFurnaceBlockEntity getFurnace() {
      if (this.furnace == null || this.furnace.isRemoved()) {
         this.findFurnace();
      }

      return this.furnace;
   }

   @Override
   public AspectList getAspects() {
      AdvAlchemyFurnaceBlockEntity f = this.getFurnace();
      return f != null ? f.aspects : null;
   }

   @Override
   public void setAspects(AspectList aspectList) {
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      return amount;
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      AdvAlchemyFurnaceBlockEntity f = this.getFurnace();
      return f == null ? false : f.aspects.getAmount(aspect) >= amount;
   }

   @Override
   public int containerContains(Aspect aspect) {
      AdvAlchemyFurnaceBlockEntity f = this.getFurnace();
      return f == null ? 0 : f.aspects.getAmount(aspect);
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return false;
   }

   @Override
   public boolean isConnectable(Direction face) {
      return face == this.facing;
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return false;
   }

   @Override
   public boolean canOutputTo(Direction face) {
      return face == this.facing;
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

   @Nullable
   @Override
   public Aspect getSuctionType(Direction face) {
      return null;
   }

   @Override
   public int getSuctionAmount(Direction face) {
      return 0;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction direction) {
      return 0;
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.initialized = false;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      AdvAlchemyFurnaceBlockEntity furnace = this.getFurnace();
      if (furnace == null || furnace.aspects.getAmount(aspect) < amount) {
         return false;
      }

      furnace.aspects.remove(aspect, amount);
      return true;
   }


   @Override
   public int getEssenceAmount(net.minecraft.core.Direction face) {
      AdvAlchemyFurnaceBlockEntity furnace = this.getFurnace();
      return furnace == null ? 0 : furnace.aspects.visSize();
   }


   @Override
   public Aspect getEssenceType(net.minecraft.core.Direction face) {
      return this.getSuctionType(face);
   }


   @Override
   public int extractEssence(Aspect aspect, int amount, net.minecraft.core.Direction loc) {
      AdvAlchemyFurnaceBlockEntity furnace = this.getFurnace();
      if (furnace == null) {
         return 0;
      }

      int available = furnace.aspects.getAmount(aspect);
      int taken = Math.min(available, amount);
      if (taken > 0) {
         furnace.aspects.remove(aspect, taken);
      }

      return taken;
   }

}
