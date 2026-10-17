package thaumcraft.block.tube.buffer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.wands.IWandable;
import thaumcraft.block.bellows.BellowsBlockEntity;
import thaumcraft.block.tube.BlockTube;
import thaumcraft.block.tube.TubeBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModSounds;

public class TubeBufferBlockEntity extends ThaumcraftBlockEntity implements IAspectContainer, IEssenceTransport, ITickable, IWandable {
   public static final int MAX_AMOUNT = 8;
   public AspectList aspects = new AspectList();
   public boolean[] openSides = new boolean[]{true, true, true, true, true, true};
   public byte[] chokedSides = new byte[]{0, 0, 0, 0, 0, 0};
   private int count = 0;
   private int bellows = -1;

   public TubeBufferBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.TUBE_BUFFER.get(), pos, state);
   }

   public boolean[] getOpenSides() {
      return this.openSides;
   }

   public void setOpenSides(boolean[] sides) {
      if (sides != null && sides.length == 6) {
         this.openSides = sides;
         this.setChanged();
         this.markForUpdate();
      }
   }

   public byte[] getChokedSides() {
      return this.chokedSides;
   }

   public void setChokedSides(byte[] choke) {
      if (choke != null && choke.length == 6) {
         this.chokedSides = choke;
         this.setChanged();
         this.markForUpdate();
      }
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.aspects.load(compound);
      byte[] sides = compound.getByteArray("open");
      if (sides != null && sides.length == 6) {
         for (int a = 0; a < 6; a++) {
            this.openSides[a] = sides[a] == 1;
         }
      }

      byte[] choke = compound.getByteArray("choke");
      if (choke != null && choke.length == 6) {
         this.chokedSides = choke;
      }
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.aspects.save(compound);
      byte[] sides = new byte[6];

      for (int a = 0; a < 6; a++) {
         sides[a] = (byte)(this.openSides[a] ? 1 : 0);
      }

      compound.putByteArray("open", sides);
      compound.putByteArray("choke", this.chokedSides);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
   }

   @Override
   public AspectList getAspects() {
      return this.aspects;
   }

   @Override
   public void setAspects(AspectList aspectList) {
      this.aspects = aspectList == null ? new AspectList() : aspectList;
      this.setChanged();
      this.markForUpdate();
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      if (aspect != null && amount > 0) {
         int room = 8 - this.aspects.visSize();
         if (room <= 0) {
            return amount;
         } else {
            int add = Math.min(room, amount);
            this.aspects.add(aspect, add);
            this.setChanged();
            this.markForUpdate();
            return amount - add;
         }
      } else {
         return amount;
      }
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      if (aspect != null && amount > 0) {
         if (this.aspects.getAmount(aspect) >= amount) {
            this.aspects.remove(aspect, amount);
            this.setChanged();
            this.markForUpdate();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean takeFromContainer(AspectList list) {
      if (list != null && list.size() > 0) {
         for (Aspect a : list.getAspects()) {
            if (this.aspects.getAmount(a) < list.getAmount(a)) {
               return false;
            }
         }

         for (Aspect ax : list.getAspects()) {
            this.aspects.remove(ax, list.getAmount(ax));
         }

         this.setChanged();
         this.markForUpdate();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return aspect != null && amount > 0 && this.aspects.getAmount(aspect) >= amount;
   }

   @Override
   public boolean doesContainerContain(AspectList list) {
      if (list != null && list.size() > 0) {
         for (Aspect a : list.getAspects()) {
            if (this.aspects.getAmount(a) < list.getAmount(a)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public int containerContains(Aspect aspect) {
      return aspect == null ? 0 : this.aspects.getAmount(aspect);
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }

   @Override
   public boolean isConnectable(Direction face) {
      return face != null && this.openSides[face.get3DDataValue()];
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return face != null && this.openSides[face.get3DDataValue()] && this.chokedSides[face.get3DDataValue()] != 2;
   }

   @Override
   public boolean canOutputTo(Direction face) {
      return face != null && this.openSides[face.get3DDataValue()] && this.chokedSides[face.get3DDataValue()] != 2;
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
   public Aspect getSuctionType(@Nullable Direction loc) {
      return null;
   }

   @Override
   public int getSuctionAmount(@Nullable Direction loc) {
      if (loc != null && this.chokedSides[loc.get3DDataValue()] == 2) {
         return 0;
      } else {
         return this.bellows > 0 && (loc == null || this.chokedSides[loc.get3DDataValue()] != 1) ? this.bellows * 32 : 1;
      }
   }

   @Override
   public Aspect getEssenceType(@Nullable Direction loc) {
      if (this.aspects.size() > 0 && this.level != null) {
         Aspect[] a = this.aspects.getAspects();
         return a[this.level.random.nextInt(a.length)];
      } else {
         return null;
      }
   }

   @Override
   public int getEssenceAmount(Direction loc) {
      return this.aspects.visSize();
   }

   @Override
   public int extractEssence(Aspect aspect, int amount, Direction face) {
      if (this.level == null) {
         return 0;
      } else if (aspect != null && amount > 0) {
         if (!this.canOutputTo(face)) {
            return 0;
         } else {
            int reqSuction = 0;
            Aspect reqType = null;
            if (ThaumcraftApiHelper.getConnectableTile(this.level, this.worldPosition, face) instanceof IEssenceTransport req) {
               Direction opp = face.getOpposite();
               reqSuction = req.getSuctionAmount(opp);
               reqType = req.getSuctionType(opp);
            }

            for (Direction dir : Direction.values()) {
               if (dir != face
                  && this.canOutputTo(dir)
                  && ThaumcraftApiHelper.getConnectableTile(this.level, this.worldPosition, dir) instanceof IEssenceTransport ic) {
                  Direction opp = dir.getOpposite();
                  int sa = ic.getSuctionAmount(opp);
                  Aspect su = ic.getSuctionType(opp);
                  boolean typeMatch = su == null || su == aspect;
                  if (typeMatch && sa > reqSuction) {
                     return 0;
                  }
               }
            }

            int have = this.aspects.getAmount(aspect);
            if (have <= 0) {
               return 0;
            } else {
               int take = Math.min(amount, have);
               this.aspects.remove(aspect, take);
               this.setChanged();
               this.markForUpdate();
               return take;
            }
         }
      } else {
         return 0;
      }
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction face) {
      if (!this.canInputFrom(face)) {
         return 0;
      } else {
         int leftover = this.addToContainer(aspect, amount);
         return amount - leftover;
      }
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      this.count++;
      if (this.bellows < 0 || this.count % 20 == 0) {
         this.getBellows();
      }

      if (this.count % 5 == 0 && this.aspects.visSize() < 8) {
         this.fillBuffer();
      }
   }

   void fillBuffer() {
      if (this.level == null) {
         return;
      }
      for (Direction dir : Direction.values()) {
         if (this.canInputFrom(dir)) {
            BlockEntity te = ThaumcraftApiHelper.getConnectableTile(this.level, this.worldPosition, dir);
            if (te instanceof IEssenceTransport ic) {
               Direction opp = dir.getOpposite();
               if (ic.getEssenceAmount(opp) > 0
                  && ic.getSuctionAmount(opp) < this.getSuctionAmount(dir)
                  && this.getSuctionAmount(dir) >= ic.getMinimumSuction()) {
                  Aspect ta = ic.getEssenceType(opp);
                  if (ta != null) {
                     int taken = ic.extractEssence(ta, 1, opp);
                     if (taken > 0) {
                        this.addToContainer(ta, taken);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   public void getBellows() {
      if (this.level != null) {
         this.bellows = BellowsBlockEntity.getBellows(this.level, this.worldPosition, Direction.values());
      }
   }

   /** TC4 TileTubeBuffer.onWandRightClick: sneaking cycles how much the clicked side holds back, otherwise it opens or shuts. */
   @Override
   public int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull Vec3 hit
   ) {
      int side = TubeBlockEntity.clickedPart(pos, hit, face -> true);
      if (!level.isClientSide) {
         if (player.isShiftKeyDown()) {
            level.playSound(null, pos, ModSounds.SQUEEK.get(), SoundSource.BLOCKS, 0.6F, 1.1F + level.random.nextFloat() * 0.2F);
            this.cycleChoke(side);
         } else {
            level.playSound(null, pos, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.5F, 0.9F + level.random.nextFloat() * 0.2F);
            this.toggleSide(side);
         }
      }

      player.swing(hand);
      return 1;
   }

   @Override
   public int onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos pos, @NotNull Direction direction) {
      return this.onWandRightClick(level, player, hand, pos, direction, pos.getCenter().relative(direction, 0.5));
   }

   @NotNull
   @Override
   public ItemStack onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      return ItemStack.EMPTY;
   }

   @Override
   public void onUsingWandTick(@NotNull ItemStack stack, @NotNull Player player, int count) {
   }

   @Override
   public void onWandStoppedUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player, int count) {
   }

   public void toggleSide(int side) {
      if (side >= 0 && side < 6) {
         this.openSides[side] = !this.openSides[side];
         Direction dir = Direction.from3DDataValue(side);
         if (this.level != null) {
            BlockPos targetPos = this.worldPosition.relative(dir);
            BlockEntity tile = this.level.getBlockEntity(targetPos);
            if (tile instanceof TubeBlockEntity tube) {
               tube.openSides[dir.getOpposite().get3DDataValue()] = this.openSides[side];
               tube.setChanged();
               tube.markForUpdate();
               tube.updateBlockState();
            } else if (tile instanceof TubeBufferBlockEntity buffer) {
               buffer.openSides[dir.getOpposite().get3DDataValue()] = this.openSides[side];
               buffer.setChanged();
               buffer.markForUpdate();
               buffer.updateBlockState();
            }
         }
         this.setChanged();
         this.markForUpdate();
         this.updateBlockState();
      }
   }

   /** Arms follow the open sides, as for tubes. */
   public void updateBlockState() {
      BlockState state = this.getBlockState();
      if (this.level != null && !this.level.isClientSide && state.getBlock() instanceof BlockTube tube) {
         BlockState updated = tube.recalculateConnections(state, this.level, this.worldPosition);
         if (!updated.equals(state)) {
            this.level.setBlock(this.worldPosition, updated, 3);
         }
      }
   }

   public void cycleChoke(int side) {
      if (side >= 0 && side < 6) {
         this.chokedSides[side]++;
         if (this.chokedSides[side] > 2) {
            this.chokedSides[side] = 0;
         }
         this.setChanged();
         this.markForUpdate();
      }
   }
}
