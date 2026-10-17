package thaumcraft.block.deconstruction_table;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.util.BlockEntityMenuValidator;

public class DeconstructionTableBlockEntity extends ThaumcraftSideInvBlockEntity implements MenuProvider {
   @Nullable
   public Aspect aspect;
   public int breaktime;

   public DeconstructionTableBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.DECONSTRUCTION_TABLE.get(), pos, state, 1);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag tag) {
      super.loadCustom(tag);
      this.loadAspect(tag);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.loadCustom(tag, provider);
      this.loadAspect(tag);
   }

   private void loadAspect(@NotNull CompoundTag tag) {
      if (tag.contains("Aspect")) {
         String a = tag.getString("Aspect");
         this.aspect = a.isEmpty() ? null : Aspect.getAspect(a);
      } else {
         this.aspect = null;
      }
   }

   @Override
   public void saveCustom(@NotNull CompoundTag tag) {
      super.saveCustom(tag);
      this.saveAspect(tag);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.saveCustom(tag, provider);
      this.saveAspect(tag);
   }

   private void saveAspect(@NotNull CompoundTag tag) {
      if (this.aspect != null) {
         tag.putString("Aspect", this.aspect.getTag());
      } else {
         tag.putString("Aspect", "");
      }
   }

   @NotNull
   public CompoundTag getUpdateTag(@NotNull Provider provider) {
      CompoundTag tag = super.getUpdateTag(provider);
      this.saveAspect(tag);
      return tag;
   }

   @Nullable
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      AspectList al = ThaumcraftCraftingManager.getObjectTags(stack);
      al = ThaumcraftCraftingManager.getBonusTags(stack, al);
      return al != null && al.size() > 0;
   }

   @Override
   public boolean stillValid(@NotNull Player player) {
      return BlockEntityMenuValidator.isValid(player, this);
   }

   @NotNull
   public Component getDisplayName() {
      return Component.translatable("container.thaumcraft.deconstruction_table");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inv, @NotNull Player player) {
      return new DeconstructionTableMenu(windowId, inv, this);
   }

   public int getBreakTimeScaled(int pixels) {
      return Mth.clamp(this.breaktime * pixels / 40, 0, pixels);
   }

   public boolean canBreak() {
      ItemStack stack = this.getItem(0);
      if (!stack.isEmpty() && this.aspect == null) {
         AspectList al = ThaumcraftCraftingManager.getObjectTags(stack);
         al = ThaumcraftCraftingManager.getBonusTags(stack, al);
         return al != null && al.size() > 0;
      }
      return false;
   }

   public void breakItem() {
      if (this.canBreak()) {
         ItemStack stack = this.getItem(0);
         AspectList al = ThaumcraftCraftingManager.getObjectTags(stack);
         al = ThaumcraftCraftingManager.getBonusTags(stack, al);
         AspectList primals = thaumcraft.research.ResearchManager.reduceToPrimals(al);
         if (primals != null && this.level != null && this.level.random.nextInt(80) < primals.visSize()) {
            Aspect[] aspects = primals.getAspects();
            if (aspects.length > 0) {
               this.aspect = aspects[this.level.random.nextInt(aspects.length)];
            }
         }

         stack.shrink(1);
         if (stack.isEmpty()) {
            this.setItem(0, ItemStack.EMPTY);
         }
      }
   }

   public static void serverTick(
      net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos,
      net.minecraft.world.level.block.state.BlockState state, DeconstructionTableBlockEntity be
   ) {
      boolean flag1 = false;
      if (be.breaktime == 0 && be.canBreak()) {
         be.breaktime = 40;
         flag1 = true;
      }

      if (be.breaktime > 0 && be.canBreak()) {
         be.breaktime--;
         if (be.breaktime == 0) {
            be.breakItem();
            flag1 = true;
         }
      } else if (be.breaktime != 0) {
         be.breaktime = 0;
         flag1 = true;
      }

      if (flag1) {
         be.setChanged();
         level.sendBlockUpdated(pos, state, state, 3);
      }
   }
}
