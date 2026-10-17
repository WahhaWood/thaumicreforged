package thaumcraft.block.lamp.growth;

import java.util.ArrayList;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class ArcaneLampGrowthBlockEntity extends ThaumcraftBlockEntity implements IEssenceTransport, ITickable {
   private boolean reserve = false;
   public int charges = -1;
   BlockPos lpos = BlockPos.ZERO;
   Block lid = Blocks.AIR;
   ArrayList<BlockPos> checklist = new ArrayList<>();
   int drawDelay = 0;

   public ArcaneLampGrowthBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ARCANE_LAMP_GROWTH.get(), blockPos, blockState);
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
      this.reserve = compound.getBoolean("reserve");
      this.charges = compound.getInt("charges");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      compound.putBoolean("reserve", this.reserve);
      compound.putInt("charges", this.charges);
   }

   /** Серверная логика: тянет herba-эссенцию и ускоряет рост растений (порт TileArcaneLampGrowth). */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.charges <= 0) {
         if (this.reserve) {
            this.charges = 100;
            this.reserve = false;
            this.setChanged();
            this.markForUpdate();
         } else if (this.drawEssentia(level)) {
            this.charges = 100;
            this.setChanged();
            this.markForUpdate();
         }
      }

      if (!this.reserve && this.drawEssentia(level)) {
         this.reserve = true;
      }

      if (this.charges == 0) {
         this.charges = -1;
         this.markForUpdate();
      }

      if (this.charges > 0 && level.getGameTime() % 20L == 0L) {
         this.growPlants(level);
      }
   }

   private boolean drawEssentia(Level level) {
      if (++this.drawDelay % 5 != 0) {
         return false;
      }

      if (!(thaumcraft.api.ThaumcraftApiHelper.getConnectableTile(level, this.worldPosition, this.getFacing()) instanceof IEssenceTransport ic)) {
         return false;
      }

      if (!ic.canOutputTo(this.getFacing().getOpposite())) {
         return false;
      }

      return ic.getSuctionAmount(this.getFacing().getOpposite()) < this.getSuctionAmount(this.getFacing())
         && ic.extractEssence(thaumcraft.api.aspects.Aspect.PLANT, 1, this.getFacing().getOpposite()) == 1;
   }

   private void growPlants(Level level) {
      if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
         return;
      }

      int radius = 4;
      BlockPos pos = this.worldPosition;
      for (int dx = -radius; dx <= radius; dx++) {
         for (int dz = -radius; dz <= radius; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
               BlockPos target = pos.offset(dx, dy, dz);
               BlockState state = serverLevel.getBlockState(target);
               if (state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock growable
                  && growable.isValidBonemealTarget(serverLevel, target, state)) {
                  growable.performBonemeal(serverLevel, serverLevel.random, target, state);
                  this.charges -= 5;
                  return;
               }
            }
         }
      }
   }

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
      return Aspect.PLANT;
   }

   @Override
   public int getSuctionAmount(Direction face) {
      return face == this.getFacing() && (!this.reserve || this.charges <= 0) ? 128 : 0;
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
   public int extractEssence(Aspect aspect, int amount, Direction loc) {
      return 0;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction loc) {
      return 0;
   }
}
