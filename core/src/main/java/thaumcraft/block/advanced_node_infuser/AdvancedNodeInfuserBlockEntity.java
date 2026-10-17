package thaumcraft.block.advanced_node_infuser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.block.jar.node.NodeJarBlockItem;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModItems;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXEssenceSource;
import thaumcraft.research.ResearchManager;
import thaumcraft.util.ItemStackData;

public class AdvancedNodeInfuserBlockEntity extends ThaumcraftSideInvBlockEntity implements ITickable, MenuProvider, IAspectContainer {
   private static final int SEARCH_RANGE = 5;
   private static final int BUFFER_CAPACITY = 10;
   private static final int SOURCE_CACHE_INTERVAL = 100;
   private static final int EMPTY_SOURCE_RETRY_INTERVAL = 20;
   private final AspectList toConsume = new AspectList();
   private List<BlockPos> cachedSources = List.of();
   private int countToConsume = 0;
   private long nextSourceCacheRefresh = 0L;
   public int count = 0;

   public AdvancedNodeInfuserBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.ADVANCED_NODE_INFUSER.get(), pos, state, 1);
   }

   public void setChanged() {
      super.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.loadInfuserData(compound);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.loadInfuserData(compound);
   }

   private void loadInfuserData(@NotNull CompoundTag compound) {
      this.countToConsume = compound.getInt("countToConsume");
      this.toConsume.load(compound, "toConsume");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.saveInfuserData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.saveInfuserData(compound);
   }

   private void saveInfuserData(@NotNull CompoundTag compound) {
      compound.putInt("countToConsume", this.countToConsume);
      this.toConsume.save(compound, "toConsume");
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      return index == 0 && stack.is((Item)ModItems.NODE_JAR.get()) && ItemStackData.hasTag(stack);
   }

   public int getMaxStackSize() {
      return 1;
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (this.hasValidJar() && !level.hasNeighborSignal(pos)) {
         if (this.count < 37) {
            this.count++;
         }
      } else if (this.count > 0) {
         this.count--;
      }
   }

   @NotNull
   public Component getDisplayName() {
      return Component.translatable("block.thaumcraft.advanced_node_infuser");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inventory, @NotNull Player player) {
      return new AdvancedNodeInfuserMenu(windowId, inventory, this);
   }

   public boolean hasValidJar() {
      return this.canPlaceItem(0, this.getJarStack());
   }

   @NotNull
   public ItemStack getJarStack() {
      return this.getItem(0);
   }

   @Nullable
   private NodeJarBlockItem getJarItem() {
      return this.getJarStack().getItem() instanceof NodeJarBlockItem item ? item : null;
   }

   @Nullable
   public AspectList getNodeAspects() {
      NodeJarBlockItem item = this.getJarItem();
      return item != null ? item.getAspects(this.getJarStack()) : null;
   }

   @Nullable
   public NodeType getNodeType() {
      NodeJarBlockItem item = this.getJarItem();
      return item != null ? item.getNodeType(this.getJarStack()) : null;
   }

   @Nullable
   public NodeModifier getNodeModifier() {
      NodeJarBlockItem item = this.getJarItem();
      return item != null ? item.getNodeModifier(this.getJarStack()) : null;
   }

   @NotNull
   public String getNodeId() {
      NodeJarBlockItem item = this.getJarItem();
      return item != null ? item.getNodeId(this.getJarStack()) : "0";
   }

   private void applyConsumedAspects() {
      NodeJarBlockItem item = this.getJarItem();
      if (item != null) {
         ItemStack stack = this.getJarStack();
         AspectList aspects = item.getAspects(stack);
         if (aspects == null) {
            aspects = new AspectList();
         }

         AspectList primals = ResearchManager.reduceToPrimals(this.toConsume.copy(), true);

         for (Aspect aspect : primals.getAspects()) {
            aspects.add(aspect, 1);
         }

         item.setAspects(stack, aspects);
      }
   }

   private boolean searchForEssence(List<BlockPos> positions) {
      if (this.level != null && !positions.isEmpty()) {
         boolean sawInvalidSource = false;
         ArrayList<Aspect> aspects = new ArrayList<>(Aspect.aspects.values());
         Collections.shuffle(aspects);

         for (BlockPos sourcePos : positions) {
            if (this.level.getBlockEntity(sourcePos) instanceof IAspectSource source) {
               for (Aspect aspect : aspects) {
                  if (this.drainAspect(source, sourcePos, aspect)) {
                     this.toConsume.add(aspect, 1);
                     this.setChanged();
                     return true;
                  }
               }
            } else {
               sawInvalidSource = true;
            }
         }

         if (sawInvalidSource) {
            this.nextSourceCacheRefresh = 0L;
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean drainAspect(IAspectSource source, BlockPos sourcePos, Aspect aspect) {
      if (this.level != null && source.takeFromContainer(aspect, 1)) {
         PacketHandler.sendToAllAround(
            new PacketFXEssenceSource(
               this.worldPosition,
               (byte)(this.worldPosition.getX() - sourcePos.getX()),
               (byte)(this.worldPosition.getY() - sourcePos.getY()),
               (byte)(this.worldPosition.getZ() - sourcePos.getZ()),
               aspect.getColor()
            ),
            this.level,
            this.worldPosition,
            32.0
         );
         return true;
      } else {
         return false;
      }
   }

   private List<BlockPos> getCachedSources() {
      if (this.level == null) {
         return List.of();
      } else {
         long gameTime = this.level.getGameTime();
         if (gameTime < this.nextSourceCacheRefresh && !this.cachedSources.isEmpty()) {
            return this.cachedSources;
         } else {
            this.cachedSources = this.collectSources();
            this.nextSourceCacheRefresh = gameTime + (this.cachedSources.isEmpty() ? 20 : 100);
            return this.cachedSources;
         }
      }
   }

   private List<BlockPos> collectSources() {
      if (this.level == null) {
         return List.of();
      } else {
         ArrayList<BlockPos> out = new ArrayList<>();

         for (BlockPos pos : BlockPos.betweenClosed(this.worldPosition.offset(-5, -5, -5), this.worldPosition.offset(5, 5, 5))) {
            if (!pos.equals(this.worldPosition) && this.level.getBlockEntity(pos) instanceof IAspectSource) {
               out.add(pos.immutable());
            }
         }

         return out;
      }
   }

   private boolean tryApplyConsumedAspects() {
      if (this.toConsume.visSize() < 10) {
         return false;
      } else {
         this.applyConsumedAspects();
         this.toConsume.clear();
         this.setChanged();
         return true;
      }
   }

   @Override
   public AspectList getAspects() {
      return this.toConsume;
   }

   @Override
   public void setAspects(AspectList aspectList) {
      this.toConsume.clear();
      if (aspectList != null) {
         this.toConsume.add(aspectList);
      }

      this.setChanged();
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return aspect != null && this.hasValidJar() && this.toConsume.visSize() < 10;
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      if (amount > 0 && this.doesContainerAccept(aspect)) {
         int inserted = Math.min(amount, 10 - this.toConsume.visSize());
         if (inserted <= 0) {
            return amount;
         } else {
            this.toConsume.add(aspect, inserted);
            this.setChanged();
            return amount - inserted;
         }
      } else {
         return amount;
      }
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      if (!this.doesContainerContainAmount(aspect, amount)) {
         return false;
      } else {
         this.toConsume.remove(aspect, amount);
         this.setChanged();
         return true;
      }
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      if (!this.doesContainerContain(aspectList)) {
         return false;
      } else {
         for (Aspect aspect : aspectList.getAspects()) {
            this.toConsume.remove(aspect, aspectList.getAmount(aspect));
         }

         this.setChanged();
         return true;
      }
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return this.toConsume.getAmount(aspect) >= amount;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      if (aspectList == null) {
         return true;
      } else {
         for (Aspect aspect : aspectList.getAspects()) {
            if (this.toConsume.getAmount(aspect) < aspectList.getAmount(aspect)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public int containerContains(Aspect aspect) {
      return this.toConsume.getAmount(aspect);
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      return new AABB(
         this.worldPosition.getX(),
         this.worldPosition.getY(),
         this.worldPosition.getZ(),
         this.worldPosition.getX() + 1,
         this.worldPosition.getY() + 2,
         this.worldPosition.getZ() + 1
      );
   }
}
