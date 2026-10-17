package thaumcraft.block.lamp.fertility;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class ArcaneLampFertilityBlockEntity extends ThaumcraftBlockEntity implements IEssenceTransport, ITickable {
   public int charges = 0;
   int count = 0;
   int drawDelay = 0;

   public ArcaneLampFertilityBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ARCANE_LAMP_FERTILITY.get(), blockPos, blockState);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider provider) {
      super.onDataPacket(net, pkt, provider);
      if (this.level != null && this.level.isClientSide) {
         this.level.getLightEngine().checkBlock(this.worldPosition);
      }
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      this.charges = compound.getInt("charges");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      compound.putInt("charges", this.charges);
   }

   /** TC4 TileArcaneLampFertility.updateEntity: up to 4 charges of victus from the tube it faces; with 2+ it pairs animals every 300 ticks. */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.charges < 4 && this.drawEssentia(level)) {
         this.charges++;
         this.markForUpdate();
      }

      if (this.charges > 1 && this.count++ % 300 == 0) {
         this.updateAnimals(level);
      }
   }

   private boolean drawEssentia(Level level) {
      if (++this.drawDelay % 5 != 0) {
         return false;
      }

      Direction facing = this.getFacing();
      return ThaumcraftApiHelper.getConnectableTile(level, this.worldPosition, facing) instanceof IEssenceTransport ic
         && ic.canOutputTo(facing.getOpposite())
         && ic.getSuctionAmount(facing.getOpposite()) < this.getSuctionAmount(facing)
         && ic.extractEssence(Aspect.LIFE, 1, facing.getOpposite()) == 1;
   }

   /** Two free adults of a kind within 7 blocks fall in love, if there are no more than 7 of that kind (costs 2 charges). */
   private void updateAnimals(Level level) {
      List<Animal> animals = level.getEntitiesOfClass(Animal.class, new AABB(this.worldPosition).inflate(7.0));
      for (Animal animal : animals) {
         if (animal.getAge() != 0 || animal.isInLove()) {
            continue;
         }

         List<Animal> kind = animals.stream().filter(other -> other.getClass().equals(animal.getClass())).toList();
         if (kind.size() > 7) {
            continue;
         }

         Animal partner = null;
         for (Animal other : kind) {
            if (other.getAge() == 0 && !other.isInLove()) {
               if (partner != null) {
                  this.charges -= 2;
                  other.setInLove(null);
                  partner.setInLove(null);
                  this.markForUpdate();
                  return;
               }

               partner = other;
            }
         }
      }
   }

   @Override
   public boolean isConnectable(Direction face) {
      return face == this.getFacing();
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return face == this.getFacing();
   }

   @Override
   public boolean canOutputTo(Direction face) {
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
      return Aspect.LIFE;
   }

   @Override
   public int getSuctionAmount(Direction face) {
      return face == this.getFacing() ? 128 - this.charges * 10 : 0;
   }

   @Override
   public Aspect getEssenceType(Direction loc) {
      return null;
   }

   @Override
   public int getEssenceAmount(Direction loc) {
      return 0;
   }

   @Override
   public int extractEssence(Aspect aspect, int amount, Direction facing) {
      return 0;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction facing) {
      return 0;
   }
}
