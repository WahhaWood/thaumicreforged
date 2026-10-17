package thaumcraft.entities.smart.golem;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class Marker {
   private final BlockPos pos;
   private final ResourceKey<Level> dimension;
   private final Direction side;
   private final byte color;

   public Marker(BlockPos pos, ResourceKey<Level> dimension, Direction side, byte color) {
      this.pos = pos;
      this.dimension = dimension;
      this.side = side;
      this.color = color;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int getX() {
      return this.pos.getX();
   }

   public int getY() {
      return this.pos.getY();
   }

   public int getZ() {
      return this.pos.getZ();
   }

   public ResourceKey<Level> getDimension() {
      return this.dimension;
   }

   public Direction getSide() {
      return this.side;
   }

   public byte getColor() {
      return this.color;
   }

   public Marker withColor(byte newColor) {
      return new Marker(this.pos, this.dimension, this.side, newColor);
   }

   public Marker withSide(Direction newSide) {
      return new Marker(this.pos, this.dimension, newSide, this.color);
   }

   public Marker offset(Direction dir) {
      return new Marker(this.pos.relative(dir), this.dimension, this.side, this.color);
   }

   public boolean isInDimension(Level level) {
      return level.dimension().equals(this.dimension);
   }

   public double distanceSqTo(BlockPos other) {
      return this.pos.distSqr(other);
   }

   public CompoundTag writeToNBT() {
      CompoundTag tag = new CompoundTag();
      tag.putLong("Pos", this.pos.asLong());
      tag.putString("Dim", this.dimension.location().toString());
      tag.putByte("Side", (byte)this.side.ordinal());
      tag.putByte("Color", this.color);
      return tag;
   }

   public static Marker readFromNBT(CompoundTag tag) {
      BlockPos pos = BlockPos.of(tag.getLong("Pos"));
      ResourceKey<Level> dim = Level.OVERWORLD;
      if (tag.contains("Dim", 8)) {
         dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("Dim")));
      }

      Direction side = Direction.values()[tag.getByte("Side") & 7];
      byte color = tag.getByte("Color");
      return new Marker(pos, dim, side, color);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Marker other)
            ? false
            : this.pos.equals(other.pos) && this.dimension.equals(other.dimension) && this.side == other.side && this.color == other.color;
      }
   }

   public boolean equalsFuzzy(Marker other) {
      return other == null
         ? false
         : this.pos.equals(other.pos)
            && this.dimension.equals(other.dimension)
            && this.side == other.side
            && (this.color == other.color || this.color == -1 || other.color == -1);
   }

   public boolean matchesPosition(BlockPos otherPos, ResourceKey<Level> otherDim) {
      return this.pos.equals(otherPos) && this.dimension.equals(otherDim);
   }

   public boolean matchesPosition(BlockPos otherPos, ResourceKey<Level> otherDim, Direction otherSide) {
      return this.pos.equals(otherPos) && this.dimension.equals(otherDim) && this.side == otherSide;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.pos, this.dimension, this.side, this.color);
   }

   @Override
   public String toString() {
      return "Marker{pos=" + this.pos + ", dim=" + this.dimension.location() + ", side=" + this.side + ", color=" + this.color + "}";
   }
}
