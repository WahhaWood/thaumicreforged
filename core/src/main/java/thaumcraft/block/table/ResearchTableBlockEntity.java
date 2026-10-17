package thaumcraft.block.table;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IScribeTools;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blockentity.ThaumcraftInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.research_notes.ItemResearchNotes;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchNoteData;

public class ResearchTableBlockEntity extends ThaumcraftInvBlockEntity implements MenuProvider, ITickable {
   public AspectList bonusAspects = new AspectList();
   int nextRecalc = 0;
   public ResearchNoteData data = null;

   public ResearchTableBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.RESEARCH_TABLE.get(), blockPos, blockState, 2);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.loadTableData(compound);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.loadTableData(compound);
   }

   private void loadTableData(@NotNull CompoundTag compound) {
      this.nextRecalc = compound.getInt("nextRecalc");
      this.bonusAspects = new AspectList();
      ListTag listTag = compound.getList("bonusAspects", 8);

      for (int i = 0; i < listTag.size(); i++) {
         Aspect aspect = Aspect.getAspect(listTag.getString(i));
         if (aspect != null) {
            this.bonusAspects.merge(aspect, 1);
         }
      }

      this.gatherResults();
      this.syncVisualState();
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.saveTableData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.saveTableData(compound);
   }

   private void saveTableData(@NotNull CompoundTag compound) {
      compound.putInt("nextRecalc", this.nextRecalc);
      ListTag listTag = new ListTag();
      this.bonusAspects.forEach((aspect, integer) -> {
         if (aspect != null && integer > 0) {
            listTag.add(StringTag.valueOf(aspect.getTag()));
         }
      });
      compound.put("bonusAspects", listTag);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
   }

   public void setChanged() {
      super.setChanged();
      this.gatherResults();
      this.syncVisualState();
   }

   public void gatherResults() {
      this.data = null;
      ItemStack notes = this.getItem(1);
      if (notes.getItem() instanceof ItemResearchNotes) {
         this.data = ResearchManager.getData(notes);
      }
   }

   private void syncVisualState() {
      if (this.level != null && !this.level.isClientSide) {
         BlockState state = this.getBlockState();
         if (state.is((Block)ModBlocks.RESEARCH_TABLE.get())) {
            boolean hasTools = this.getItem(0).getItem() instanceof IScribeTools;
            boolean hasNotes = this.getItem(1).getItem() instanceof ItemResearchNotes;
            if ((Boolean)state.getValue(ResearchTableBlock.HAS_TOOLS) != hasTools || (Boolean)state.getValue(ResearchTableBlock.HAS_NOTES) != hasNotes) {
               this.level
                  .setBlockAndUpdate(
                     this.worldPosition,
                     (BlockState)((BlockState)state.setValue(ResearchTableBlock.HAS_TOOLS, hasTools)).setValue(ResearchTableBlock.HAS_NOTES, hasNotes)
                  );
            }
         }
      }
   }

   @NotNull
   public Component getDisplayName() {
      return Component.literal("Research Table");
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      return switch (index) {
         case 0 -> stack.getItem() instanceof IScribeTools;
         case 1 -> {
            if (stack.getItem() instanceof ItemResearchNotes notes && !notes.isDiscovery(stack)) {
               yield true;
            }

            yield false;
         }
         default -> false;
      };
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      return new AABB(
         this.worldPosition.getX() - 1,
         this.worldPosition.getY(),
         this.worldPosition.getZ() - 1,
         this.worldPosition.getX() + 2,
         this.worldPosition.getY() + 2,
         this.worldPosition.getZ() + 2
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.handleUpdateTag(tag, provider);
      this.refreshClientState();
   }

   @OnlyIn(Dist.CLIENT)
   private void refreshClientState() {
      if (this.level != null && this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   /** TC4 TileResearchTable: block event 1 plays the learn sound at the table for everyone nearby (research done, notes copied). */
   public void playLearnEffect() {
      if (this.level != null) {
         this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, 1);
      }
   }

   /** Block events must return true on the server as well: only then does the server send them to clients. */
   @Override
   public boolean triggerEvent(int i, int j) {
      if (i == 1) {
         if (this.level.isClientSide) {
            this.level.playLocalSound(this.worldPosition, (SoundEvent)ModSounds.LEARN.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         return true;
      } else {
         return super.triggerEvent(i, j);
      }
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowsId, @NotNull Inventory inventory, @NotNull Player player) {
      return new ResearchTableMenu(windowsId, inventory, this);
   }
}
