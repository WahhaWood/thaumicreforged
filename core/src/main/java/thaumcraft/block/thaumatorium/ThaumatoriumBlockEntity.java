package thaumcraft.block.thaumatorium;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.util.InventoryUtils;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;

public class ThaumatoriumBlockEntity extends ThaumcraftSideInvBlockEntity implements IAspectContainer, IEssenceTransport, MenuProvider, ITickable {
   private static final int UPGRADE_SCAN_INTERVAL = 40;
   private static final int CRAFT_CHECK_INTERVAL = 5;
   private final AspectList essence = new AspectList();
   private final Map<ResourceLocation, AspectList> recipes = new LinkedHashMap<>();
   private ResourceLocation currentCraft;
   private Aspect currentSuction;
   private int maxRecipes = 1;
   private int venting;
   private int counter;
   private boolean heated;
   private CrucibleRecipe currentRecipe;
   /** TC4 recipePlayer: who chose each recipe, to fire the crafting event for them. */
   private final Map<ResourceLocation, String> recipePlayers = new HashMap<>();

   public ThaumatoriumBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.THAUMATORIUM.get(), blockPos, blockState, 1);
   }

   private ThaumatoriumBlockEntity getMaster() {
      if (this.level == null) {
         return null;
      } else {
         BlockState state = this.getBlockState();
         if (state.hasProperty(ThaumatoriumBlock.ELEMENT) && state.getValue(ThaumatoriumBlock.ELEMENT) == ThaumatoriumBlock.Element.CENTER) {
            return this;
         } else if (state.hasProperty(ThaumatoriumBlock.ELEMENT)
            && state.getValue(ThaumatoriumBlock.ELEMENT) == ThaumatoriumBlock.Element.TOP
            && this.level.getBlockEntity(this.worldPosition.below()) instanceof ThaumatoriumBlockEntity master) {
            return master;
         } else {
            return state.hasProperty(ThaumatoriumBlock.ELEMENT)
                  && state.getValue(ThaumatoriumBlock.ELEMENT) == ThaumatoriumBlock.Element.BOTTOM
                  && this.level.getBlockEntity(this.worldPosition.above()) instanceof ThaumatoriumBlockEntity master
               ? master
               : null;
         }
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (this.isMaster()) {
         if (this.venting > 0) {
            this.venting--;
            Direction direction = this.getHorizontalFacing();
            RandomSource random = this.level.random;
            float fx = 0.1F - random.nextFloat() * 0.2F;
            float fz = 0.1F - random.nextFloat() * 0.2F;
            float fy = 0.1F - random.nextFloat() * 0.2F;
            float fx2 = 0.1F - random.nextFloat() * 0.2F;
            float fz2 = 0.1F - random.nextFloat() * 0.2F;
            float fy2 = 0.1F - random.nextFloat() * 0.2F;
            int color = 16777215;
            Thaumcraft.setup
               .drawVentParticles(
                  this.level,
                  this.worldPosition.getX() + 0.5F + fx + direction.getStepX() / 2.0F,
                  this.worldPosition.getY() + 0.5F + fy,
                  this.worldPosition.getZ() + 0.5F + fz + direction.getStepZ() / 2.0F,
                  direction.getStepX() / 4.0F + fx2,
                  fy2,
                  direction.getStepZ() / 4.0F + fz2,
                  color
               );
         }
      }
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (!this.isMaster()) {
         return;
      }

      if (this.counter == 0 || this.counter % UPGRADE_SCAN_INTERVAL == 0) {
         this.heated = this.checkHeat();
         this.getUpgrades();
      }

      this.counter++;
      if (this.heated && !this.gettingPower() && this.counter % CRAFT_CHECK_INTERVAL == 0 && !this.recipes.isEmpty()) {
         ItemStack inputStack = this.getItem(0);
         if (inputStack.isEmpty()) {
            this.currentSuction = null;
            return;
         }

         this.resolveCurrentRecipe(inputStack);
         if (this.currentRecipe == null || this.currentCraft == null) {
            this.currentSuction = null;
            return;
         }

         ItemStack output = this.getCurrentOutputRecipe();
         if (!this.canOutput(output)) {
            return;
         }

         boolean done = true;
         this.currentSuction = null;

         AspectList requiredAspects = this.recipes.get(this.currentCraft);
         if (requiredAspects != null) {
            for (Aspect aspect : requiredAspects.getAspectsSorted()) {
               if (this.essence.getAmount(aspect) < requiredAspects.getAmount(aspect)) {
                  this.currentSuction = aspect;
                  done = false;
                  break;
               }
            }
         }

         if (done) {
            this.completeRecipe();
         } else if (this.currentSuction != null) {
            this.fill();
         }
      }
   }

   public boolean gettingPower() {
      if (this.level == null) {
         return false;
      }
      return this.level.hasNeighborSignal(this.worldPosition)
         || this.level.hasNeighborSignal(this.worldPosition.below())
         || this.level.hasNeighborSignal(this.worldPosition.above());
   }

   public void getUpgrades() {
      if (this.level == null) {
         return;
      }
      int mr = 1;
      Direction facing = this.getHorizontalFacing();

      for (int yy = 0; yy <= 1; yy++) {
         for (Direction dir : Direction.values()) {
            if (dir != Direction.DOWN && dir != facing) {
               BlockPos checkPos = this.worldPosition.above(yy).relative(dir);
               BlockState checkState = this.level.getBlockState(checkPos);
               if (checkState.is((Block)ModBlocks.MNEMONIC_MATRIX.get())) {
                  Direction hopperFacing = checkState.getValue(BlockStateProperties.FACING_HOPPER);
                  if (hopperFacing == dir.getOpposite()) {
                     mr += 2;
                  }
               }
            }
         }
      }

      if (mr != this.maxRecipes) {
         this.maxRecipes = mr;
         while (this.recipes.size() > this.maxRecipes) {
            ResourceLocation lastKey = null;
            for (ResourceLocation key : this.recipes.keySet()) {
               lastKey = key;
            }
            if (lastKey != null) {
               this.recipes.remove(lastKey);
            }
         }
         this.setChanged();
         this.markForUpdate();
      }
   }

   void fill() {
      if (this.level == null || this.currentSuction == null) {
         return;
      }
      Direction facing = this.getHorizontalFacing();
      for (int y = 0; y <= 1; y++) {
         for (Direction dir : Direction.values()) {
            if (dir != facing && dir != Direction.DOWN && (y != 0 || dir != Direction.UP)) {
               BlockEntity te = ThaumcraftApiHelper.getConnectableTile(this.level, this.worldPosition.above(y), dir);
               if (te instanceof IEssenceTransport ic) {
                  Direction opp = dir.getOpposite();
                  if (ic.getEssenceAmount(opp) > 0
                     && ic.getSuctionAmount(opp) < this.getSuctionAmount(null)
                     && this.getSuctionAmount(null) >= ic.getMinimumSuction()) {
                     int ess = ic.extractEssence(this.currentSuction, 1, opp);
                     if (ess > 0) {
                        this.addToContainer(this.currentSuction, ess);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private void completeRecipe() {
      ItemStack inputStack = this.getItem(0);
      if (this.currentRecipe != null && this.currentCraft != null && this.currentRecipe.matches(this.essence, inputStack)) {
         ItemStack catalyst = this.removeItem(0, 1);
         if (catalyst.isEmpty()) {
            return;
         }

         this.essence.aspects.clear();
         ItemStack dropped = this.getCurrentOutputRecipe();
         ServerPlayer crafter = this.level.getServer() == null
            ? null
            : this.level.getServer().getPlayerList().getPlayerByName(this.recipePlayers.getOrDefault(this.currentCraft, ""));
         if (crafter != null) {
            EventHooks.firePlayerCraftingEvent(crafter, dropped, new SimpleContainer(catalyst));
         }

         Direction facing = this.getHorizontalFacing();
         BlockPos targetPos = this.worldPosition.relative(facing);
         BlockEntity inventory = this.level.getBlockEntity(targetPos);
         if (inventory instanceof Container container) {
            dropped = InventoryUtils.placeItemStackIntoInventory(dropped, container, facing.getOpposite(), true);
         } else {
            IItemHandler handler = (IItemHandler)this.level.getCapability(ItemHandler.BLOCK, targetPos, facing.getOpposite());
            if (handler != null) {
               dropped = ItemHandlerHelper.insertItemStacked(handler, dropped, false);
            }
         }

         if (!dropped.isEmpty()) {
            ItemEntity ei = new ItemEntity(
               this.level,
               this.worldPosition.getX() + 0.5 + facing.getStepX() * 0.66,
               this.worldPosition.getY() + 0.33,
               this.worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.66,
               dropped.copy()
            );
            ei.setDeltaMovement(
               0.075 * facing.getStepX(),
               0.025,
               0.075 * facing.getStepZ()
            );
            this.level.addFreshEntity(ei);
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 0, 0);
         }

         this.level.playSound(
            null,
            this.worldPosition.getX() + 0.5,
            this.worldPosition.getY() + 0.5,
            this.worldPosition.getZ() + 0.5,
            SoundEvents.FIRE_EXTINGUISH,
            SoundSource.BLOCKS,
            0.25F,
            2.6F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.8F
         );
         this.clearCurrentRecipeReference();
         this.setChanged();
         this.markForUpdate();
      }
   }

   private void resolveCurrentRecipe(ItemStack inputStack) {
      if (!this.hasMatchingCurrentRecipe(inputStack)) {
         this.clearCurrentRecipeReference();

         for (ResourceLocation id : this.recipes.keySet()) {
            CrucibleRecipe recipe = ThaumcraftApi.getCrucibleRecipeFromId(id);
            if (recipe != null && recipe.catalystMatches(inputStack)) {
               this.currentCraft = id;
               this.currentRecipe = recipe;
               return;
            }
         }
      }
   }

   private boolean hasMatchingCurrentRecipe(ItemStack inputStack) {
      return this.currentCraft != null
         && this.currentRecipe != null
         && this.recipes.containsKey(this.currentCraft)
         && this.currentRecipe.catalystMatches(inputStack);
   }

   private void clearCurrentRecipeReference() {
      this.currentCraft = null;
      this.currentRecipe = null;
      this.currentSuction = null;
   }

   private boolean canOutput(ItemStack outputStack) {
      if (outputStack.isEmpty()) {
         return false;
      } else {
         Direction direction = this.getHorizontalFacing();
         BlockEntity blockEntity = this.level.getBlockEntity(this.worldPosition.relative(direction));
         if (blockEntity == null) {
            return true;
         } else {
            IItemHandler handler = (IItemHandler)this.level.getCapability(ItemHandler.BLOCK, this.worldPosition.relative(direction), direction.getOpposite());
            return handler == null || ItemHandlerHelper.insertItemStacked(handler, outputStack, true).isEmpty();
         }
      }
   }

   private boolean updateRequiredSuction() {
      this.currentSuction = null;
      AspectList requiredAspects = this.recipes.get(this.currentCraft);
      if (requiredAspects == null) {
         return false;
      } else {
         for (Aspect aspect : requiredAspects.getAspectsSorted()) {
            if (this.essence.getAmount(aspect) < requiredAspects.getAmount(aspect)) {
               this.currentSuction = aspect;
               return false;
            }
         }

         return true;
      }
   }

   private boolean isMaster() {
      return this.getBlockState().hasProperty(ThaumatoriumBlock.ELEMENT)
         && this.getBlockState().getValue(ThaumatoriumBlock.ELEMENT) == ThaumatoriumBlock.Element.CENTER;
   }

   @Nullable
   private ThaumatoriumBlockEntity getInventoryTarget() {
      ThaumatoriumBlockEntity master = this.getMaster();
      return master != null ? master : this;
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      return new AABB(
         this.worldPosition.getX() - 1,
         this.worldPosition.getY() - 1,
         this.worldPosition.getZ() - 1,
         this.worldPosition.getX() + 2,
         this.worldPosition.getY() + 2,
         this.worldPosition.getZ() + 2
      );
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.loadThaumatoriumData(compound);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.loadThaumatoriumData(compound);
   }

   private void loadThaumatoriumData(@NotNull CompoundTag compound) {
      this.essence.load(compound);
      this.maxRecipes = compound.getByte("maxrec");
      this.recipes.clear();
      this.recipePlayers.clear();
      this.clearCurrentRecipeReference();
      ListTag listTag = compound.getList("recipes", 10);

      for (int i = 0; i < listTag.size(); i++) {
         CompoundTag tag = listTag.getCompound(i);
         AspectList aspectList = new AspectList();
         aspectList.load(tag);
         ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
         if (id != null) {
            this.recipes.put(id, aspectList);
            this.recipePlayers.put(id, tag.getString("player"));
         }
      }
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.saveThaumatoriumData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.saveThaumatoriumData(compound);
   }

   private void saveThaumatoriumData(@NotNull CompoundTag compound) {
      if (this.isMaster()) {
         compound.putByte("maxrec", (byte)this.maxRecipes);
         this.essence.save(compound);
         ListTag listTag = new ListTag();

         for (Entry<ResourceLocation, AspectList> entry : this.recipes.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", entry.getKey().toString());
            tag.putString("player", this.recipePlayers.getOrDefault(entry.getKey(), ""));
            entry.getValue().save(tag);
            listTag.add(tag);
         }

         compound.put("recipes", listTag);
      }
   }

   boolean checkHeat() {
      BlockPos blockPos = this.worldPosition.below(2);
      BlockState blockState = this.level.getBlockState(blockPos);
      return blockState.is(BlockTags.FIRE) || blockState.getFluidState().is(FluidTags.LAVA) || blockState.is((Block)ModBlocks.NITOR.get());
   }

   public ItemStack getCurrentOutputRecipe() {
      if (this.currentRecipe != null) {
         return this.currentRecipe.getResultItem().copy();
      } else if (this.currentCraft != null && !this.recipes.isEmpty()) {
         CrucibleRecipe recipe = ThaumcraftApi.getCrucibleRecipeFromId(this.currentCraft);
         return recipe == null ? ItemStack.EMPTY : recipe.getResultItem().copy();
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void resetActiveRecipeState() {
      ThaumatoriumBlockEntity master = this.getMaster();
      if (master != null && master != this) {
         master.resetActiveRecipeState();
      } else {
         this.clearCurrentRecipeReference();
         this.essence.aspects.clear();
      }
   }

   public void syncActiveRecipeState() {
      ThaumatoriumBlockEntity master = this.getMaster();
      if (master != null && master != this) {
         master.syncActiveRecipeState();
      } else {
         if (this.level != null && !this.level.isClientSide() && this.recipes.keySet().removeIf(id -> ThaumcraftApi.getCrucibleRecipeFromId(id) == null)) {
            this.markForUpdate();
            this.setChanged();
         }

         if (this.recipes.isEmpty() || this.currentCraft != null && !this.recipes.containsKey(this.currentCraft)) {
            this.resetActiveRecipeState();
         }
      }
   }

   public boolean hasSelectedRecipes() {
      ThaumatoriumBlockEntity master = this.getMaster();
      return master != null && master != this ? master.hasSelectedRecipes() : !this.recipes.isEmpty();
   }

   public int getSelectedRecipeCount() {
      ThaumatoriumBlockEntity master = this.getMaster();
      return master != null && master != this ? master.getSelectedRecipeCount() : this.recipes.size();
   }

   public int getMaxRecipes() {
      ThaumatoriumBlockEntity master = this.getMaster();
      return master != null && master != this ? master.getMaxRecipes() : this.maxRecipes;
   }

   public boolean isRecipeSelected(ResourceLocation id) {
      ThaumatoriumBlockEntity master = this.getMaster();
      return master != null && master != this ? master.isRecipeSelected(id) : this.recipes.containsKey(id);
   }

   public void toggleRecipeSelection(RecipeHolder<CrucibleRecipe> holder) {
      this.toggleRecipeSelection(holder, "");
   }

   public void toggleRecipeSelection(RecipeHolder<CrucibleRecipe> holder, String playerName) {
      ThaumatoriumBlockEntity master = this.getMaster();
      if (master != null && master != this) {
         master.toggleRecipeSelection(holder, playerName);
      } else {
         ResourceLocation id = holder.id();
         if (this.recipes.containsKey(id)) {
            this.recipes.remove(id);
            this.recipePlayers.remove(id);
            if (id.equals(this.currentCraft) || this.recipes.isEmpty()) {
               this.resetActiveRecipeState();
            }
         } else {
            if (this.recipes.size() >= this.maxRecipes) {
               this.clearSelectedRecipes();
            }

            this.recipes.put(id, ((CrucibleRecipe)holder.value()).aspects.copy());
            this.recipePlayers.put(id, playerName);
         }

         this.syncActiveRecipeState();
         this.setChanged();
         this.markForUpdate();
      }
   }

   public void clearSelectedRecipes() {
      ThaumatoriumBlockEntity master = this.getMaster();
      if (master != null && master != this) {
         master.clearSelectedRecipes();
      } else {
         this.recipes.clear();
         this.recipePlayers.clear();
         this.resetActiveRecipeState();
      }
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      ThaumatoriumBlockEntity master = this.getMaster();
      if (master != null && master != this) {
         return master.addToContainer(aspect, amount);
      } else if (this.currentRecipe == null) {
         return amount;
      } else {
         int ce = this.currentRecipe.aspects.getAmount(aspect) - this.essence.getAmount(aspect);
         if (ce > 0) {
            int add = Math.min(ce, amount);
            this.essence.add(aspect, add);
            this.markForUpdate();
            this.setChanged();
            return amount - add;
         } else {
            return amount;
         }
      }
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      ThaumatoriumBlockEntity master = this.getMaster();
      if (master != null && master != this) {
         return master.takeFromContainer(aspect, amount);
      } else if (this.essence.getAmount(aspect) >= amount) {
         this.essence.remove(aspect, amount);
         this.markForUpdate();
         this.setChanged();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      ThaumatoriumBlockEntity master = this.getMaster();
      return master != null && master != this ? master.doesContainerContainAmount(aspect, amount) : this.essence.getAmount(aspect) >= amount;
   }

   @Override
   public int containerContains(Aspect aspect) {
      ThaumatoriumBlockEntity master = this.getMaster();
      return master != null && master != this ? master.containerContains(aspect) : this.essence.getAmount(aspect);
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }

   public boolean triggerEvent(int i, int j) {
      if (i >= 0) {
         if (this.level.isClientSide) {
            this.venting = 7;
         }

         return true;
      } else {
         return super.triggerEvent(i, j);
      }
   }

   @Override
   public boolean isConnectable(Direction face) {
      return face != this.getHorizontalFacing();
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return face != this.getHorizontalFacing();
   }

   @Override
   public boolean canOutputTo(Direction face) {
      return false;
   }

   @Override
   public void setSuction(Aspect aspect, int amount) {
      ThaumatoriumBlockEntity master = this.getMaster();
      if (master != null && master != this) {
         master.setSuction(aspect, amount);
      } else {
         this.currentSuction = aspect;
      }
   }

   @Override
   public Aspect getSuctionType(Direction loc) {
      ThaumatoriumBlockEntity master = this.getMaster();
      return master != null && master != this ? master.getSuctionType(loc) : this.currentSuction;
   }

   @Override
   public int getSuctionAmount(Direction loc) {
      ThaumatoriumBlockEntity master = this.getMaster();
      if (master != null && master != this) {
         return master.getSuctionAmount(loc);
      } else {
         return this.currentSuction != null ? 128 : 0;
      }
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
   public int extractEssence(Aspect aspect, int amount, Direction face) {
      ThaumatoriumBlockEntity master = this.getMaster();
      if (master != null && master != this) {
         return master.extractEssence(aspect, amount, face);
      } else {
         return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
      }
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction face) {
      ThaumatoriumBlockEntity master = this.getMaster();
      if (master != null && master != this) {
         return master.injectEssence(aspect, amount, face);
      } else {
         return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
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

   @Override
   public AspectList getAspects() {
      ThaumatoriumBlockEntity master = this.getMaster();
      return master != null && master != this ? master.getAspects() : this.essence;
   }

   @Override
   public void setAspects(AspectList aspectList) {
      ThaumatoriumBlockEntity master = this.getMaster();
      if (master != null && master != this) {
         master.setAspects(aspectList);
      } else {
         this.essence.aspects.clear();
         if (aspectList != null) {
            this.essence.aspects.putAll(aspectList.copy().aspects);
         }
      }
   }

   @Override
   public boolean isEmpty() {
      ThaumatoriumBlockEntity target = this.getInventoryTarget();
      return target != null && target != this ? target.isEmpty() : super.isEmpty();
   }

   @NotNull
   @Override
   public ItemStack getItem(int index) {
      ThaumatoriumBlockEntity target = this.getInventoryTarget();
      return target != null && target != this ? target.getItem(index) : super.getItem(index);
   }

   @NotNull
   @Override
   public ItemStack removeItem(int index, int amount) {
      ThaumatoriumBlockEntity target = this.getInventoryTarget();
      return target != null && target != this ? target.removeItem(index, amount) : super.removeItem(index, amount);
   }

   @NotNull
   @Override
   public ItemStack removeItemNoUpdate(int index) {
      ThaumatoriumBlockEntity target = this.getInventoryTarget();
      return target != null && target != this ? target.removeItemNoUpdate(index) : super.removeItemNoUpdate(index);
   }

   @Override
   public void setItem(int index, @NotNull ItemStack stack) {
      ThaumatoriumBlockEntity target = this.getInventoryTarget();
      if (target != null && target != this) {
         target.setItem(index, stack);
      } else {
         super.setItem(index, stack);
      }
   }

   @Override
   public void clearContent() {
      ThaumatoriumBlockEntity target = this.getInventoryTarget();
      if (target != null && target != this) {
         target.clearContent();
      } else {
         super.clearContent();
      }
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      return this.getMaster() != null;
   }

   @NotNull
   @Override
   public int[] getSlotsForFace(@NotNull Direction direction) {
      return this.getMaster() != null ? new int[]{0} : new int[0];
   }

   @Override
   public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
      return this.getMaster() != null;
   }

   @Override
   public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
      return this.getMaster() != null;
   }

   @NotNull
   @Override
   public IItemHandler getItemHandler(@Nullable Direction side) {
      ThaumatoriumBlockEntity master = this.getMaster();
      return master != null && master != this ? master.getItemHandler(side) : super.getItemHandler(side);
   }

   @NotNull
   public Component getDisplayName() {
      return Component.empty();
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inventory, @NotNull Player player) {
      return this.isMaster() ? new ThaumatoriumMenu(windowId, inventory, this) : null;
   }
}
