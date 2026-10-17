package thaumcraft.block.tube.centrifuge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModSounds;

public class CentrifugeBlockEntity extends ThaumcraftBlockEntity implements IAspectContainer, IEssenceTransport, ITickable {
   public Aspect aspectOut = null;
   public Aspect aspectIn = null;
   int count = 0;
   int process = 0;
   float rotationSpeed = 0.0F;
   public float previousRotation = 0.0F;
   public float rotation = 0.0F;

   public CentrifugeBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ALCHEMICAL_CENTRIFUGE.get(), blockPos, blockState);
   }

   public boolean canUpdate() {
      return true;
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      this.aspectIn = Aspect.getAspect(compound.getString("aspectIn"));
      this.aspectOut = Aspect.getAspect(compound.getString("aspectOut"));
      this.process = compound.getInt("process");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      if (this.aspectIn != null) {
         compound.putString("aspectIn", this.aspectIn.getTag());
      }

      if (this.aspectOut != null) {
         compound.putString("aspectOut", this.aspectOut.getTag());
      }

      compound.putInt("process", this.process);
   }

   @Override
   public AspectList getAspects() {
      AspectList al = new AspectList();
      if (this.aspectOut != null) {
         al.add(this.aspectOut, 1);
      }

      return al;
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      if (amount > 0 && this.aspectOut == null) {
         this.aspectOut = aspect;
         this.setChanged();
         this.markForUpdate();
         amount--;
      }

      return amount;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      if (this.aspectOut != null && aspect == this.aspectOut) {
         this.aspectOut = null;
         this.setChanged();
         this.markForUpdate();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return amount == 1 && aspect == this.aspectOut;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      for (Aspect tt : aspectList.getAspects()) {
         if (tt == this.aspectOut) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return aspect == this.aspectOut ? 1 : 0;
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }

   @Override
   public boolean isConnectable(Direction face) {
      return face == Direction.UP || face == Direction.DOWN;
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return face == Direction.DOWN;
   }

   @Override
   public boolean canOutputTo(Direction face) {
      return face == Direction.UP;
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
      return face == Direction.DOWN ? (this.gettingPower() ? 0 : (this.aspectIn == null ? 128 : 64)) : 0;
   }

   @Override
   public Aspect getEssenceType(Direction loc) {
      return this.aspectOut;
   }

   @Override
   public int getEssenceAmount(Direction loc) {
      return this.aspectOut != null ? 1 : 0;
   }

   @Override
   public int extractEssence(Aspect aspect, int amount, Direction face) {
      return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction face) {
      if (this.aspectIn == null && !aspect.isPrimal()) {
         this.aspectIn = aspect;
         this.process = 39;
         this.setChanged();
         this.markForUpdate();
         return 1;
      } else {
         return 0;
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.previousRotation = this.rotation;
      if (this.aspectIn != null && !this.gettingPower() && this.rotationSpeed < 20.0F) {
         this.rotationSpeed += 2.0F;
      }

      if ((this.aspectIn == null || this.gettingPower()) && this.rotationSpeed > 0.0F) {
         this.rotationSpeed -= 0.5F;
      }

      int pr = (int)this.rotation;
      this.rotation = this.rotation + this.rotationSpeed;
      if (this.rotation % 180.0F <= 20.0F && pr % 180 >= 160 && this.rotationSpeed > 0.0F) {
         this.level.playLocalSound(this.worldPosition, (SoundEvent)ModSounds.PUMP.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
      }
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (!this.gettingPower()) {
         if (this.aspectOut == null && this.aspectIn == null && ++this.count % 5 == 0) {
            this.drawEssentia();
         }

         if (this.process > 0) {
            this.process--;
         }

         if (this.aspectOut == null && this.aspectIn != null && this.process == 0) {
            this.processEssentia();
         }
      }
   }

   void processEssentia() {
      if (this.aspectIn == null || this.aspectIn.isPrimal()) {
         return;
      }
      Aspect[] comps = this.aspectIn.getComponents();
      if (comps != null && comps.length > 0 && this.level != null) {
         this.aspectOut = comps[this.level.random.nextInt(comps.length)];
      }
      this.aspectIn = null;
      this.setChanged();
      this.markForUpdate();
   }

   void drawEssentia() {
      if (this.level == null) {
         return;
      }
      BlockEntity te = ThaumcraftApiHelper.getConnectableTile(
         this.level, this.worldPosition, Direction.DOWN
      );
      if (te instanceof IEssenceTransport ic) {
         if (!ic.canOutputTo(Direction.UP)) {
            return;
         }

         Aspect ta = null;
         if (ic.getEssenceAmount(Direction.UP) > 0
            && ic.getSuctionAmount(Direction.UP) < this.getSuctionAmount(Direction.DOWN)
            && this.getSuctionAmount(Direction.DOWN) >= ic.getMinimumSuction()) {
            ta = ic.getEssenceType(Direction.UP);
         }

         if (ta != null
            && !ta.isPrimal()
            && ic.getSuctionAmount(Direction.UP) < this.getSuctionAmount(Direction.DOWN)
            && ic.extractEssence(ta, 1, Direction.UP) == 1) {
            this.aspectIn = ta;
            this.process = 39;
            this.setChanged();
            this.markForUpdate();
         }
      }
   }

   @Override
   public void setAspects(AspectList aspectList) {
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      return new AABB(
         this.worldPosition.getX() - 1,
         this.worldPosition.getY() - 1,
         this.worldPosition.getZ() - 1,
         this.worldPosition.getX() + 1,
         this.worldPosition.getY() + 1,
         this.worldPosition.getZ() + 1
      );
   }

   public boolean gettingPower() {
      return this.level.hasNeighborSignal(this.worldPosition);
   }
}
