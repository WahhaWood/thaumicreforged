package thaumcraft.block.mana_pod;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.init.registry.ModBlockEntities;

public class ManaPodBlockEntity extends BlockEntity implements IAspectContainer {
   public Aspect aspect = null;

   public ManaPodBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.MANA_POD.get(), pos, state);
   }

   public void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.aspect = Aspect.getAspect(tag.getString("aspect"));
   }

   protected void saveAdditional(CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      if (this.aspect != null) {
         tag.putString("aspect", this.aspect.getTag());
      }
   }

   public CompoundTag getUpdateTag(Provider provider) {
      return this.saveWithoutMetadata(provider);
   }

   public void handleUpdateTag(CompoundTag tag, Provider provider) {
      this.loadAdditional(tag, provider);
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider provider) {
      CompoundTag tag = pkt.getTag();
      if (tag != null) {
         this.loadAdditional(tag, provider);
      }
   }

   public void markAspectDirty() {
      this.setChanged();
      Level level = this.getLevel();
      if (level != null && !level.isClientSide) {
         BlockState state = this.getBlockState();
         level.sendBlockUpdated(this.worldPosition, state, state, 3);
      }
   }

   public void initializeGeneratedAspect() {
      Level level = this.getLevel();
      if (level != null && this.aspect == null) {
         this.aspect = getRandomPodAspect(level);
         this.markAspectDirty();
      }
   }

   /**
    * Рост и скрещивание (порт {@code TileManaPod#checkGrowth}):
    * возраст +1; на возрасте 3 — скрещивание с соседями (комбинации аспектов);
    * без аспекта — случайный первичный (1/8 — plant).
    */
   public void checkGrowth() {
      Level level = this.getLevel();
      if (level == null || level.isClientSide) {
         return;
      }

      BlockState state = this.getBlockState();
      if (!(state.getBlock() instanceof BlockManaPod)) {
         return;
      }

      int age = state.getValue(BlockManaPod.AGE);
      if (age < 7) {
         age++;
         level.setBlock(this.worldPosition, state.setValue(BlockManaPod.AGE, age), 3);
      }

      if (age > 2) {
         if (age == 3) {
            AspectList neighbors = new AspectList();
            if (this.aspect != null) {
               neighbors.add(this.aspect, 1);
            }

            for (Direction direction : Direction.Plane.HORIZONTAL) {
               BlockPos neighborPos = this.worldPosition.relative(direction);
               if (level.getBlockEntity(neighborPos) instanceof ManaPodBlockEntity neighbor
                  && neighbor.aspect != null) {
                  neighbors.add(neighbor.aspect, 1);
               }
            }

            if (neighbors.size() > 1) {
               java.util.ArrayList<Aspect> options = new java.util.ArrayList<>();
               for (Aspect first : neighbors.getAspects()) {
                  options.add(first);
                  for (Aspect second : neighbors.getAspects()) {
                     if (first != second) {
                        Aspect combo = thaumcraft.research.ResearchManager.getCombinationResult(first, second);
                        if (combo != null) {
                           options.add(combo);
                           options.add(combo);
                        }
                     }
                  }
               }

               if (!options.isEmpty()) {
                  this.aspect = options.get(level.random.nextInt(options.size()));
                  this.markAspectDirty();
               }
            }

            if (neighbors.size() >= 1 && this.aspect == null) {
               this.aspect = neighbors.getAspectsSortedAmount()[0];
               this.markAspectDirty();
            }
         }

         if (this.aspect == null) {
            this.aspect = getRandomPodAspect(level);
            this.markAspectDirty();
         }
      }
   }

   public static Aspect getRandomPodAspect(Level level) {
      if (level.random.nextInt(8) == 0) {
         return Aspect.PLANT;
      } else {
         ArrayList<Aspect> outlist = Aspect.getPrimalAspects();
         return outlist.get(level.random.nextInt(outlist.size()));
      }
   }

   @Override
   public AspectList getAspects() {
      BlockState state = this.getBlockState();
      if (!(state.getBlock() instanceof BlockManaPod)) {
         return null;
      } else {
         return this.aspect != null && state.getValue(BlockManaPod.AGE) == 7 ? new AspectList().add(this.aspect, 1) : null;
      }
   }

   @Override
   public void setAspects(AspectList aspectList) {
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return false;
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
}
