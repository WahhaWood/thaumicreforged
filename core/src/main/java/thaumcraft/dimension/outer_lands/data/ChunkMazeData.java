package thaumcraft.dimension.outer_lands.data;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class ChunkMazeData {
   private ChunkMazeData() {
   }

   public interface IMazeChunkData {
      boolean isGenerated();

      void setGenerated(boolean var1);
   }

   public static final class Impl implements ChunkMazeData.IMazeChunkData, INBTSerializable<CompoundTag> {
      private boolean generated;

      @Override
      public boolean isGenerated() {
         return this.generated;
      }

      @Override
      public void setGenerated(boolean value) {
         this.generated = value;
      }

      public CompoundTag serializeNBT(Provider provider) {
         CompoundTag tag = new CompoundTag();
         tag.putBoolean("maze_done", this.isGenerated());
         return tag;
      }

      public void deserializeNBT(Provider provider, CompoundTag nbt) {
         this.setGenerated(nbt.getBoolean("maze_done"));
      }
   }
}
