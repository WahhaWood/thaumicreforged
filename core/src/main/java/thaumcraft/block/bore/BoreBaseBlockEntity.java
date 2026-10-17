package thaumcraft.block.bore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.wands.IWandable;
import thaumcraft.init.registry.ModBlockEntities;

public class BoreBaseBlockEntity extends ThaumcraftBlockEntity implements IWandable, IEssenceTransport {
   public BoreBaseBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ARCANE_BORE_BASE.get(), blockPos, blockState);
   }

   @Override
   public int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos blockPos, @NotNull Direction direction
   ) {
      if (direction.getAxis().isHorizontal() && this.getHorizontalFacing() != direction) {
         player.swing(hand);
      }

      return 0;
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

   public boolean drawEssentia() {
      for (Direction facing : Direction.values()) {
         net.minecraft.world.level.block.entity.BlockEntity te = thaumcraft.api.ThaumcraftApiHelper.getConnectableTile(this.level, this.worldPosition, facing);
         if (te instanceof IEssenceTransport ic) {
            if (!ic.canOutputTo(facing.getOpposite())) {
               continue;
            }

            if (ic.getSuctionAmount(facing.getOpposite()) < this.getSuctionAmount(facing) && ic.extractEssence(Aspect.ENTROPY, 1, facing.getOpposite()) == 1) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean isConnectable(Direction face) {
      return true;
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return true;
   }

   @Override
   public boolean canOutputTo(Direction face) {
      return false;
   }

   @Override
   public void setSuction(Aspect aspect, int amount) {
   }

   @Override
   public Aspect getSuctionType(Direction face) {
      return Aspect.ENTROPY;
   }

   @Override
   public int getSuctionAmount(Direction face) {
      return face != this.getHorizontalFacing() ? 128 : 0;
   }

   @Override
   public int extractEssence(Aspect aspect, int amount, Direction face) {
      return 0;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction face) {
      return 0;
   }

   @Override
   public Aspect getEssenceType(Direction face) {
      return null;
   }

   @Override
   public int getEssenceAmount(Direction face) {
      return 0;
   }

   @Override
   public int getMinimumSuction() {
      return 0;
   }

   @Override
   public boolean renderExtendedTube() {
      return true;
   }
}
