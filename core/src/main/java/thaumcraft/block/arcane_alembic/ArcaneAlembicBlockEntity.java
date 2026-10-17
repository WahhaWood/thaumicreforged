package thaumcraft.block.arcane_alembic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.wands.IWandable;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;

public class ArcaneAlembicBlockEntity extends ThaumcraftBlockEntity implements IAspectContainer, IWandable, IEssenceTransport {
   public Aspect aspect;
   public Aspect aspectFilter = null;
   public int amount = 0;
   public int maxAmount = 32;
   public boolean aboveAlembic = false;
   public boolean aboveFurnace = false;

   public ArcaneAlembicBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ARCANE_ALEMBIC.get(), blockPos, blockState);
   }

   @Override
   public AspectList getAspects() {
      return this.aspect != null ? new AspectList().add(this.aspect, this.amount) : new AspectList();
   }

   @Override
   public void setAspects(AspectList aspectList) {
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      BlockPos pos = this.worldPosition;
      return new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 1, pos.getZ() + 2);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      String filterTag = compound.getString("AspectFilter");
      this.aspectFilter = filterTag.isEmpty() ? null : Aspect.getAspect(filterTag);
      String tag = compound.getString("aspect");
      this.aspect = tag.isEmpty() ? null : Aspect.getAspect(tag);
      this.amount = compound.getShort("amount");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      compound.putString("aspect", this.aspect != null ? this.aspect.getTag() : "");
      compound.putString("AspectFilter", this.aspectFilter != null ? this.aspectFilter.getTag() : "");
      compound.putShort("amount", (short)this.amount);
   }

   public boolean canUpdate() {
      return false;
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      if (this.amount < this.maxAmount && aspect == this.aspect || this.amount == 0) {
         this.aspect = aspect;
         int added = Math.min(amount, this.maxAmount - this.amount);
         this.amount += added;
         amount -= added;
      }

      this.setChanged();
      this.markForUpdate();
      return amount;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      if (this.amount == 0 || this.aspect == null) {
         this.aspect = null;
         this.amount = 0;
      }

      if (this.aspect != null && this.amount >= amount && aspect == this.aspect) {
         this.amount -= amount;
         if (this.amount <= 0) {
            this.aspect = null;
            this.amount = 0;
         }

         this.setChanged();
         this.markForUpdate();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      return this.amount > 0 && this.aspect != null && aspectList.getAmount(this.aspect) > 0;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return this.amount >= amount && aspect == this.aspect;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return aspect == this.aspect ? this.amount : 0;
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   public void getAppearance() {
      this.aboveAlembic = false;
      BlockState blockState = this.level.getBlockState(this.worldPosition.below());
      this.aboveFurnace = blockState.is((Block)ModBlocks.ALCHEMY_FURNACE.get());
      if (blockState.is((Block)ModBlocks.ARCANE_ALEMBIC.get()) && blockState.getValue(BlockStateProperties.HORIZONTAL_FACING) == this.getHorizontalFacing()) {
         this.aboveAlembic = true;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider provider) {
      super.onDataPacket(net, pkt, provider);
      this.getAppearance();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.handleUpdateTag(tag, provider);
      this.getAppearance();
   }

   @Override
   public int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos blockPos, @NotNull Direction direction
   ) {
      if (direction.getAxis().isHorizontal()) {
         BlockState blockState = level.getBlockState(blockPos);
         level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, direction));
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

   @Override
   public boolean isConnectable(Direction face) {
      return face != Direction.DOWN && face != this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return false;
   }

   @Override
   public boolean canOutputTo(Direction face) {
      return face != Direction.DOWN && face != this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
   }

   @Override
   public void setSuction(Aspect aspect, int amount) {
   }

   @Override
   public Aspect getSuctionType(Direction loc) {
      return null;
   }

   @Override
   public int getSuctionAmount(Direction loc) {
      return 0;
   }

   @Override
   public Aspect getEssenceType(Direction loc) {
      return this.aspect;
   }

   @Override
   public int getEssenceAmount(Direction loc) {
      return this.amount;
   }

   @Override
   public int extractEssence(Aspect aspect, int amount, Direction face) {
      return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction loc) {
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
