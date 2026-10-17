package thaumcraft.block.alchemy_furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.block.arcane_alembic.ArcaneAlembicBlockEntity;
import thaumcraft.block.bellows.BellowsBlockEntity;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModItems;
import thaumcraft.recipe.ThaumcraftCraftingManager;

public class AlchemyFurnaceBlockEntity extends ThaumcraftSideInvBlockEntity
   implements MenuProvider,
   ITickable,
   IAspectContainer {
   private static final int[] SLOTS_BOTTOM = new int[]{1};
   private static final int[] SLOTS_TOP = new int[0];
   private static final int[] SLOTS_SIDES = new int[]{0};
   public AspectList aspects = new AspectList();
   public int vis;
   private final int maxVis = 50;
   public int smeltTime = 100;
   int bellows = -1;
   public boolean speedBoost = false;
   public int furnaceBurnTime;
   public int currentItemBurnTime;
   public int furnaceCookTime;
   public int count = 0;

   public AlchemyFurnaceBlockEntity(BlockPos blockPos, BlockState blockState) {
      this((BlockEntityType<?>)ModBlockEntities.ALCHEMY_FURNACE.get(), blockPos, blockState);
   }

   public AlchemyFurnaceBlockEntity(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
      super(type, blockPos, blockState, 2);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.loadFurnaceData(compound);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.loadFurnaceData(compound);
   }

   private void loadFurnaceData(@NotNull CompoundTag compound) {
      this.furnaceBurnTime = compound.getShort("BurnTime");
      this.speedBoost = compound.getBoolean("speedBoost");
      this.furnaceCookTime = compound.getShort("CookTime");
      this.currentItemBurnTime = getItemBurnTime(this.getItem(1));
      this.aspects.load(compound);
      this.vis = this.aspects.visSize();
      if (this.vis == 0 && compound.contains("Vis")) {
         this.vis = compound.getShort("Vis");
      }
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.saveFurnaceData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.saveFurnaceData(compound);
   }

   private void saveFurnaceData(@NotNull CompoundTag compound) {
      compound.putShort("BurnTime", (short)this.furnaceBurnTime);
      compound.putShort("Vis", (short)this.vis);
      compound.putBoolean("speedBoost", this.speedBoost);
      compound.putShort("CookTime", (short)this.furnaceCookTime);
      this.aspects.save(compound);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (this.furnaceBurnTime > 0) {
         this.furnaceBurnTime--;
      }
   }

   /** Порт TileAlchemyFurnace (1.7.10): топливо, плавка предметов в эссенцию, раздача в алембики сверху. */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      boolean wasBurning = this.furnaceBurnTime > 0;
      boolean changed = false;
      this.count++;
      if (this.furnaceBurnTime > 0) {
         this.furnaceBurnTime--;
      }

      if (this.bellows < 0 || this.count % 40 == 0) {
         this.bellows = BellowsBlockEntity.getBellows(level, pos, Direction.values());
      }

      if (this.count % (this.speedBoost ? 20 : 40) == 0 && this.aspects.size() > 0) {
         this.transferToAlembics(level, pos);
      }

      if (this.furnaceBurnTime == 0 && this.canSmelt()) {
         ItemStack fuel = this.getItem(1);
         this.currentItemBurnTime = this.furnaceBurnTime = getItemBurnTime(fuel);
         if (this.furnaceBurnTime > 0) {
            changed = true;
            this.speedBoost = fuel.is(ModItems.ALUMENTUM.get());
            ItemStack remaining = fuel.getCraftingRemainingItem();
            fuel.shrink(1);
            if (fuel.isEmpty()) {
               this.setItem(1, remaining);
            }
         }
      }

      if (this.isBurning() && this.canSmelt()) {
         this.furnaceCookTime++;
         if (this.furnaceCookTime >= this.smeltTime) {
            this.furnaceCookTime = 0;
            this.smeltItem();
            changed = true;
         }
      } else {
         this.furnaceCookTime = 0;
      }

      this.vis = this.aspects.visSize();
      boolean burning = this.furnaceBurnTime > 0;
      boolean filled = this.vis > 0;
      if (state.getValue(AlchemyFurnaceBlock.ACTIVE) != burning) {
         level.setBlock(pos, state.setValue(AlchemyFurnaceBlock.ACTIVE, burning), 3);
      }

      if (state.getValue(AlchemyFurnaceBlock.FILLED) != filled) {
         level.setBlock(pos, state.setValue(AlchemyFurnaceBlock.FILLED, filled), 3);
      }

      if (changed || wasBurning != burning) {
         this.setChanged();
         this.markForUpdate();
      }
   }

   /** Раздача эссенции в алембики над печью (до 5 штук). */
   private void transferToAlembics(Level level, BlockPos pos) {
      AspectList exclude = new AspectList();

      for (int deep = 1; deep <= 5; deep++) {
         if (!(level.getBlockEntity(pos.above(deep)) instanceof ArcaneAlembicBlockEntity alembic)) {
            break;
         }

         if (alembic.aspect != null
            && alembic.amount < alembic.maxAmount
            && this.aspects.getAmount(alembic.aspect) > 0) {
            this.aspects.remove(alembic.aspect, 1);
            alembic.addToContainer(alembic.aspect, 1);
            exclude.add(alembic.aspect, 1);
            this.setChanged();
         }
      }

      for (int deep = 1; deep <= 5; deep++) {
         if (!(level.getBlockEntity(pos.above(deep)) instanceof ArcaneAlembicBlockEntity alembic)) {
            break;
         }

         if (alembic.aspect == null || alembic.amount == 0) {
            Aspect aspect = null;
            if (alembic.aspectFilter == null) {
               aspect = this.takeRandomAspect(exclude);
            } else if (this.aspects.getAmount(alembic.aspectFilter) > 0) {
               this.aspects.remove(alembic.aspectFilter, 1);
               aspect = alembic.aspectFilter;
            }

            if (aspect != null) {
               alembic.addToContainer(aspect, 1);
               this.setChanged();
               break;
            }
         }
      }
   }

   private Aspect takeRandomAspect(AspectList exclude) {
      if (this.aspects.size() <= 0 || this.level == null) {
         return null;
      }

      AspectList temp = this.aspects.copy();

      for (Aspect aspect : exclude.getAspects()) {
         temp.remove(aspect);
      }

      if (temp.size() <= 0) {
         return null;
      }

      Aspect chosen = temp.getAspects()[this.level.random.nextInt(temp.getAspects().length)];
      this.aspects.remove(chosen, 1);
      return chosen;
   }

   private boolean canSmelt() {
      ItemStack input = this.getItem(0);
      if (input.isEmpty()) {
         return false;
      }

      AspectList aspects = ThaumcraftCraftingManager.getObjectTags(input);
      aspects = ThaumcraftCraftingManager.getBonusTags(input, aspects);
      if (aspects == null || aspects.size() == 0) {
         return false;
      }

      int size = aspects.visSize();
      if (size > this.maxVis - this.vis) {
         return false;
      }

      this.smeltTime = (int)(size * 10 * (1.0F - 0.125F * this.bellows));
      if (this.smeltTime <= 0) {
         this.smeltTime = 1;
      }

      return true;
   }

   private void smeltItem() {
      if (!this.canSmelt()) {
         return;
      }

      ItemStack input = this.getItem(0);
      AspectList aspects = ThaumcraftCraftingManager.getObjectTags(input);
      aspects = ThaumcraftCraftingManager.getBonusTags(input, aspects);

      for (Aspect aspect : aspects.getAspects()) {
         this.aspects.add(aspect, aspects.getAmount(aspect));
      }

      this.vis = this.aspects.visSize();
      input.shrink(1);
      if (input.isEmpty()) {
         this.setItem(0, ItemStack.EMPTY);
      }
   }

   @Override
   public AspectList getAspects() {
      return this.aspects;
   }

   @Override
   public void setAspects(AspectList aspectList) {
      this.aspects = aspectList == null ? new AspectList() : aspectList;
      this.vis = this.aspects.visSize();
      this.setChanged();
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      int added = Math.min(amount, Math.max(0, this.maxVis - this.vis));
      if (added > 0) {
         this.aspects.add(aspect, added);
         this.vis = this.aspects.visSize();
         this.setChanged();
         this.markForUpdate();
      }

      return amount - added;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      if (this.aspects.getAmount(aspect) < amount) {
         return false;
      }

      this.aspects.remove(aspect, amount);
      this.vis = this.aspects.visSize();
      this.setChanged();
      this.markForUpdate();
      return true;
   }

   @Deprecated
   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      for (Aspect aspect : aspectList.getAspects()) {
         if (this.aspects.getAmount(aspect) < aspectList.getAmount(aspect)) {
            return false;
         }
      }

      for (Aspect aspect : aspectList.getAspects()) {
         this.aspects.remove(aspect, aspectList.getAmount(aspect));
      }

      this.vis = this.aspects.visSize();
      this.setChanged();
      this.markForUpdate();
      return true;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return this.aspects.getAmount(aspect) >= amount;
   }

   @Deprecated
   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      for (Aspect aspect : aspectList.getAspects()) {
         if (this.aspects.getAmount(aspect) < aspectList.getAmount(aspect)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return this.aspects.getAmount(aspect);
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   @OnlyIn(Dist.CLIENT)
   public int getCookProgressScaled(int par1) {
      if (this.smeltTime <= 0) {
         this.smeltTime = 1;
      }

      return this.furnaceCookTime * par1 / this.smeltTime;
   }

   @OnlyIn(Dist.CLIENT)
   public int getContentsScaled(int par1) {
      return this.vis * par1 / this.maxVis;
   }

   @OnlyIn(Dist.CLIENT)
   public int getBurnTimeRemainingScaled(int par1) {
      if (this.currentItemBurnTime == 0) {
         this.currentItemBurnTime = 200;
      }

      return this.furnaceBurnTime * par1 / this.currentItemBurnTime;
   }

   public boolean isBurning() {
      return this.furnaceBurnTime > 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider provider) {
      super.onDataPacket(net, pkt, provider);
      if (this.level != null && this.level.isClientSide) {
         this.level.getLightEngine().checkBlock(this.worldPosition);
      }
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      if (index == 0) {
         AspectList al = ThaumcraftCraftingManager.getObjectTags(stack);
         al = ThaumcraftCraftingManager.getBonusTags(stack, al);
         if (al.size() > 0) {
            return true;
         }
      }

      return index == 1 && isItemFuel(stack);
   }

   @NotNull
   @Override
   public int[] getSlotsForFace(@NotNull Direction direction) {
      if (direction == Direction.DOWN) {
         return SLOTS_BOTTOM;
      } else {
         return direction == Direction.UP ? SLOTS_TOP : SLOTS_SIDES;
      }
   }

   @Override
   public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
      return direction == Direction.DOWN
         ? index == 1 && this.canPlaceItem(index, stack)
         : direction != Direction.UP && index == 0 && this.canPlaceItem(index, stack);
   }

   @Override
   public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
      return direction != Direction.DOWN || index == 1 || stack.is(Items.BUCKET);
   }

   public static boolean isItemFuel(ItemStack stack) {
      return getItemBurnTime(stack) > 0;
   }

   public static int getItemBurnTime(ItemStack stack) {
      return stack.is((Item)ModItems.ALUMENTUM.get()) ? 6400 : stack.getBurnTime(null);
   }

   private boolean isActive() {
      return (Boolean)this.getBlockState().getValue(AlchemyFurnaceBlock.ACTIVE);
   }

   private boolean isFilled() {
      return (Boolean)this.getBlockState().getValue(AlchemyFurnaceBlock.FILLED);
   }

   @NotNull
   public Component getDisplayName() {
      return Component.empty();
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowsId, @NotNull Inventory inventory, @NotNull Player player) {
      return new AlchemyFurnaceMenu(windowsId, inventory, this);
   }
}
