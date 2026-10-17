package thaumcraft.block.tube;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.wands.IWandable;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModSounds;

public class TubeBlockEntity extends ThaumcraftBlockEntity implements IEssenceTransport, IWandable, ITickable {
   public Direction facing = Direction.NORTH;
   public boolean[] openSides = new boolean[]{true, true, true, true, true, true};
   protected Aspect essenceType = null;
   protected Aspect suctionType = null;
   protected int essenceAmount = 0;
   protected int suction = 0;
   protected int venting = 0;
   protected int count = 0;
   protected int ventColor = 0;

   public TubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public TubeBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.TUBE.get(), pos, state);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.essenceType = Aspect.getAspect(compound.getString("type"));
      this.essenceAmount = compound.getInt("amount");
      this.facing = Direction.from3DDataValue(compound.getInt("side"));
      byte[] sides = compound.getByteArray("open");
      if (sides != null && sides.length == 6) {
         for (int a = 0; a < 6; a++) {
            this.openSides[a] = sides[a] == 1;
         }
      }

      this.suctionType = Aspect.getAspect(compound.getString("stype"));
      this.suction = compound.getInt("samount");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      if (this.essenceType != null) {
         compound.putString("type", this.essenceType.getTag());
      }

      compound.putInt("amount", this.essenceAmount);
      compound.putInt("side", this.facing.get3DDataValue());
      byte[] sides = new byte[6];

      for (int a = 0; a < 6; a++) {
         sides[a] = (byte)(this.openSides[a] ? 1 : 0);
      }

      compound.putByteArray("open", sides);
      if (this.suctionType != null) {
         compound.putString("stype", this.suctionType.getTag());
      }

      compound.putInt("samount", this.suction);
   }

   /** Серверная логика: пересчёт всасывания, выравнивание с соседями, вентиляция (порт TileTube из 1.7.10). */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.venting > 0) {
         this.venting--;
      }

      if (this.count == 0) {
         this.count = level.random.nextInt(10);
      }

      if (this.venting <= 0) {
         if (++this.count % 2 == 0) {
            this.calculateSuction(null, false, false);
            this.checkVenting(level, pos);
            if (this.essenceType != null && this.essenceAmount == 0) {
               this.essenceType = null;
            }
         }

         if (this.count % 5 == 0 && this.suction > 0) {
            this.equalizeWithNeighbours(level, pos, false);
         }
      }
   }

   protected void calculateSuction(@org.jetbrains.annotations.Nullable Aspect filter, boolean restrict, boolean directional) {
      this.suction = 0;
      this.suctionType = null;

      for (Direction dir : Direction.values()) {
         if (directional && this.facing != dir.getOpposite()) {
            continue;
         }

         if (!this.isConnectable(dir)) {
            continue;
         }

         if (!(thaumcraft.api.ThaumcraftApiHelper.getConnectableTile(this.level, this.worldPosition, dir) instanceof IEssenceTransport ic)) {
            continue;
         }

         Aspect theirType = ic.getSuctionType(dir.getOpposite());
         if ((filter == null || theirType == null || theirType == filter)
            && (filter != null || this.getEssenceAmount(dir) <= 0 || theirType == null || this.getEssenceType(dir) == theirType)
            && (
               filter == null
                  || this.getEssenceAmount(dir) <= 0
                  || this.getEssenceType(dir) == null
                  || theirType == null
                  || this.getEssenceType(dir) == theirType
            )) {
            int suck = ic.getSuctionAmount(dir.getOpposite());
            if (suck > 0 && suck > this.suction + 1) {
               Aspect st = theirType != null ? theirType : filter;
               this.setSuction(st, restrict ? suck / 2 : suck - 1);
            }
         }
      }
   }

   protected void checkVenting(Level level, BlockPos pos) {
      for (Direction dir : Direction.values()) {
         if (!this.isConnectable(dir)) {
            continue;
         }

         if (!(thaumcraft.api.ThaumcraftApiHelper.getConnectableTile(level, pos, dir) instanceof IEssenceTransport ic)) {
            continue;
         }

         int suck = ic.getSuctionAmount(dir.getOpposite());
         if (this.suction > 0
            && (suck == this.suction || suck == this.suction - 1)
            && this.suctionType != ic.getSuctionType(dir.getOpposite())) {
            // TC4: tell the clients to puff vent particles in the colour of the clashing suction.
            int index = this.suctionType == null ? -1 : List.copyOf(Aspect.aspects.values()).indexOf(this.suctionType);
            level.blockEvent(pos, this.getBlockState().getBlock(), 1, index);
            this.venting = 40;
         }
      }
   }

   protected void equalizeWithNeighbours(Level level, BlockPos pos, boolean directional) {
      if (this.essenceAmount > 0) {
         return;
      }

      for (Direction dir : Direction.values()) {
         // TC4: a one-way tube takes from every side but the one its suction comes from.
         if (directional && this.facing == dir.getOpposite()) {
            continue;
         }

         if (!this.isConnectable(dir)) {
            continue;
         }

         if (!(thaumcraft.api.ThaumcraftApiHelper.getConnectableTile(level, pos, dir) instanceof IEssenceTransport ic)) {
            continue;
         }

         if (!ic.canOutputTo(dir.getOpposite())) {
            continue;
         }

         Aspect theirType = ic.getEssenceType(dir.getOpposite());
         if ((this.getSuctionType(null) == null || this.getSuctionType(null) == theirType || theirType == null)
            && this.getSuctionAmount(null) > ic.getSuctionAmount(dir.getOpposite())
            && this.getSuctionAmount(null) >= ic.getMinimumSuction()) {
            Aspect wanted = this.getSuctionType(null);
            if (wanted == null) {
               wanted = theirType != null ? theirType : ic.getEssenceType(null);
            }

            if (wanted != null) {
               int taken = ic.extractEssence(wanted, 1, dir.getOpposite());
               if (taken > 0 && this.injectEssence(wanted, taken, dir) > 0) {
                  if (level.random.nextInt(100) == 0) {
                     level.blockEvent(pos, this.getBlockState().getBlock(), 0, 0);
                  }

                  this.setChanged();
                  return;
               }
            }
         }
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (this.venting > 0) {
         this.venting--;
         Random r = new Random(this.hashCode() * 4L);
         float rp = r.nextFloat() * 360.0F;
         float ry = r.nextFloat() * 360.0F;
         double fx = -Mth.sin(ry / 180.0F * (float) Math.PI) * Mth.cos(rp / 180.0F * (float) Math.PI);
         double fz = Mth.cos(ry / 180.0F * (float) Math.PI) * Mth.cos(rp / 180.0F * (float) Math.PI);
         double fy = -Mth.sin(rp / 180.0F * (float) Math.PI);
         Thaumcraft.setup.drawVentParticles(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, fx / 5.0, fy / 5.0, fx / 5.0, this.ventColor);
      }
   }

   @Override
   public int onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos pos, @NotNull Direction direction) {
      return this.onWandRightClick(level, player, hand, pos, direction, pos.getCenter().relative(direction, 0.5));
   }

   /**
    * TC4 TileTube.onWandRightClick: an arm opens or shuts that side (and the tube beyond it), the middle turns the tube.
    * Returns 1 rather than TC4's 0, so the wand's focus does not fire at the tube as well.
    */
   @Override
   public int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull Vec3 hit
   ) {
      int part = clickedPart(pos, hit, this::canConnectSide);
      if (!level.isClientSide) {
         level.playSound(null, pos, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.5F, 0.9F + level.random.nextFloat() * 0.2F);
         if (part < 6) {
            this.toggleSide(Direction.from3DDataValue(part));
         } else {
            this.rotate();
         }
      }

      player.swing(hand);
      return 1;
   }

   /**
    * Which part of a tube was hit: 0-5 an arm (a side), 6 the middle. An arm sticks out past the 8-pixel core; on the core
    * itself a face that has a transport behind it counts as that (shut) arm, as TC4 kept shut arms clickable.
    */
   public static int clickedPart(BlockPos pos, Vec3 hit, Predicate<Direction> hasArm) {
      Vec3 rel = hit.subtract(pos.getCenter());
      Direction face = Direction.getNearest(rel.x, rel.y, rel.z);
      double along = Math.abs(rel.get(face.getAxis()));
      return along > 0.25 + 1.0E-4 || hasArm.test(face) ? face.get3DDataValue() : 6;
   }

   /** TC4 TileTube.canConnectSide: there is an essentia transport on that side. */
   public boolean canConnectSide(Direction side) {
      return this.level != null && this.level.getBlockEntity(this.worldPosition.relative(side)) instanceof IEssenceTransport;
   }

   public void toggleSide(Direction side) {
      int i = side.get3DDataValue();
      this.openSides[i] = !this.openSides[i];
      if (this.level.getBlockEntity(this.worldPosition.relative(side)) instanceof TubeBlockEntity other) {
         other.openSides[side.getOpposite().get3DDataValue()] = this.openSides[i];
         other.setChanged();
         other.markForUpdate();
         other.updateBlockState();
      }

      this.setChanged();
      this.markForUpdate();
      this.updateBlockState();
   }

   /** TC4: turn to the next side (in direction order) whose opposite side leads to a connected transport. */
   protected void rotate() {
      int a = this.facing.get3DDataValue();
      while (++a < 20) {
         Direction next = Direction.from3DDataValue(a % 6);
         if (this.canConnectSide(next.getOpposite()) && this.isConnectable(next.getOpposite())) {
            this.setFacing(next);
            return;
         }
      }
   }

   /** Client side of the block events: 0 a creak now and then, 1 venting particles in the suction's colour (TC4). */
   @Override
   public boolean triggerEvent(int id, int type) {
      if (id == 0) {
         if (this.level.isClientSide) {
            this.level.playLocalSound(this.worldPosition, ModSounds.CREAK.get(), SoundSource.BLOCKS, 1.0F, 1.3F + this.level.random.nextFloat() * 0.2F, false);
         }

         return true;
      } else if (id == 1) {
         if (this.level.isClientSide) {
            if (this.venting <= 0) {
               this.level.playLocalSound(this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.1F, 1.0F + this.level.random.nextFloat() * 0.1F, false);
            }

            this.venting = 50;
            List<Aspect> order = List.copyOf(Aspect.aspects.values());
            int index = (byte)type;
            this.ventColor = index >= 0 && index < order.size() ? order.get(index).getColor() : 11184810;
         }

         return true;
      } else {
         return super.triggerEvent(id, type);
      }
   }

   public void updateBlockState() {
      if (this.level != null && !this.level.isClientSide) {
         BlockState currentState = this.level.getBlockState(this.worldPosition);
         if (currentState.getBlock() instanceof BlockTube tubeBlock) {
            BlockState newState = tubeBlock.recalculateConnections(currentState, this.level, this.worldPosition);
            if (!newState.equals(currentState)) {
               this.level.setBlock(this.worldPosition, newState, 3);
            }
         }
      }
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

   @Override
   public Direction getFacing() {
      return this.facing;
   }

   @Override
   public void setFacing(Direction facing) {
      if (facing != null) {
         this.facing = facing;
         this.setChanged();
         this.markForUpdate();
      }
   }

   @Override
   public boolean isConnectable(Direction face) {
      return face != null && this.openSides[face.get3DDataValue()];
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return face != null && this.openSides[face.get3DDataValue()];
   }

   @Override
   public boolean canOutputTo(Direction face) {
      return face != null && this.openSides[face.get3DDataValue()];
   }

   @Override
   public void setSuction(Aspect aspect, int amount) {
      this.suctionType = aspect;
      this.suction = amount;
   }

   @Override
   public Aspect getSuctionType(@Nullable Direction loc) {
      return this.suctionType;
   }

   @Override
   public int getSuctionAmount(@Nullable Direction loc) {
      return this.suction;
   }

   @Override
   public Aspect getEssenceType(@Nullable Direction loc) {
      return this.essenceType;
   }

   @Override
   public int getEssenceAmount(Direction loc) {
      return this.essenceAmount;
   }

   @Override
   public int extractEssence(Aspect aspect, int amount, Direction face) {
      if (this.canOutputTo(face) && this.essenceType == aspect && this.essenceAmount > 0 && amount > 0) {
         this.essenceAmount--;
         if (this.essenceAmount <= 0) {
            this.essenceType = null;
         }

         this.setChanged();
         return 1;
      } else {
         return 0;
      }
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction face) {
      if (this.canInputFrom(face) && this.essenceAmount == 0 && amount > 0) {
         this.essenceType = aspect;
         this.essenceAmount++;
         this.setChanged();
         return 1;
      } else {
         return 0;
      }
   }

   @Override
   public int getMinimumSuction() {
      return 0;
   }

   @Override
   public boolean renderExtendedTube() {
      return false;
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
}
