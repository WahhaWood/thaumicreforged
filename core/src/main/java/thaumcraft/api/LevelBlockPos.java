package thaumcraft.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class LevelBlockPos extends BlockPos {
   public ResourceKey<Level> dim;

   public LevelBlockPos(CompoundTag compound) {
      super(compound.getInt("w_x"), compound.getInt("w_y"), compound.getInt("w_z"));
      if (compound.contains("w_d")) {
         this.dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(compound.getString("w_d")));
      } else {
         this.dim = Level.OVERWORLD;
      }
   }

   public LevelBlockPos(int x, int y, int z, ResourceKey<Level> dim) {
      super(x, y, z);
      this.dim = dim;
   }

   public LevelBlockPos(Vec3i vec3i, ResourceKey<Level> dim) {
      super(vec3i);
      this.dim = dim;
   }

   public LevelBlockPos(BlockEntity blockEntity) {
      super(blockEntity.getBlockPos());
      if (blockEntity.getLevel() != null) {
         this.dim = blockEntity.getLevel().dimension();
      } else {
         this.dim = Level.OVERWORLD;
      }
   }

   public LevelBlockPos(LevelBlockPos coordinates) {
      super(coordinates);
      this.dim = coordinates.dim;
   }

   public BlockPos asBlockPos() {
      return new BlockPos(this.getX(), this.getY(), this.getZ());
   }

   public ServerLevel getLevel() {
      return ServerLifecycleHooks.getCurrentServer() == null ? null : ServerLifecycleHooks.getCurrentServer().getLevel(this.dim);
   }

   public boolean equals(Object o) {
      return this == o
         ? true
         : o instanceof LevelBlockPos blockPos
            && blockPos.dim == this.dim
            && blockPos.getX() == this.getX()
            && blockPos.getY() == this.getY()
            && blockPos.getZ() == this.getZ();
   }

   public int hashCode() {
      return super.hashCode() + this.dim.hashCode();
   }

   public void set(int x, int y, int z, ResourceKey<Level> dim) {
      this.setX(x);
      this.setY(y);
      this.setZ(z);
      this.dim = dim;
   }

   public int compareTo(@NotNull Vec3i vec3i) {
      return vec3i instanceof LevelBlockPos blockPos && this.dim == blockPos.dim ? super.compareTo(vec3i) : -1;
   }

   public void save(CompoundTag compound) {
      compound.putInt("w_x", this.getX());
      compound.putInt("w_y", this.getY());
      compound.putInt("w_z", this.getZ());
      compound.putString("w_d", this.dim.location().toString());
   }
}
