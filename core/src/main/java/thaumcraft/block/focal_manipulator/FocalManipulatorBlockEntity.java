package thaumcraft.block.focal_manipulator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.research.ResearchManager;

public class FocalManipulatorBlockEntity extends ThaumcraftSideInvBlockEntity implements ITickable, MenuProvider {
   public AspectList aspects = new AspectList();
   public int size = 0;
   public int upgrade = -1;
   public int rank = -1;
   int ticks = 0;
   public boolean reset = false;
   public static final int XP_MULT = 8;
   public static final int VIS_MULT = 200;
   private ItemStack currentItem = ItemStack.EMPTY;

   public FocalManipulatorBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.FOCAL_MANIPULATOR.get(), pos, state, 1);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, FocalManipulatorBlockEntity be) {
      if (!be.isRemoved()) {
         if (level.isClientSide) {
            be.clientTick(level, pos, state);
         } else {
            be.serverTick(level, pos, state);
         }
      }
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      boolean complete = false;
      if (this.rank < 0) {
         this.rank = 0;
      }

      this.ticks++;
      if (this.ticks % 5 == 0) {
         if (this.size > 0 && (this.aspects.visSize() <= 0 || this.getItem(0).isEmpty())) {
            complete = true;
            level.playSound(null, pos, (SoundEvent)ModSounds.CRAFT_FAIL.get(), SoundSource.BLOCKS, 0.33F, 1.0F);
         }

         if (this.size > 0) {
            for (Aspect aspect : this.aspects.getAspectsSortedAmount()) {
               int drain = VisNetHandler.drainVis(
                  level, pos, aspect, Math.min(100, this.aspects.getAmount(aspect))
               );
               if (drain > 0) {
                  this.aspects.reduce(aspect, drain);
                  this.markForUpdate();
                  this.setChanged();
               }
            }

            if (this.aspects.visSize() <= 0 && !this.getItem(0).isEmpty()) {
               complete = true;
               ItemStack focusStack = this.getItem(0);
               if (focusStack.getItem() instanceof ItemFocusBasic focus) {
                  focus.applyUpgrade(focusStack, FocusUpgradeType.types[this.upgrade], this.rank);
               }
               level.playSound(null, pos, (SoundEvent)ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
         }
      }

      if (complete) {
         this.size = 0;
         this.rank = -1;
         this.aspects = new AspectList();
         this.markForUpdate();
         this.setChanged();
      }
   }

   public boolean startCraft(int id, Player p) {
      if (this.size <= 0 && !this.getItem(0).isEmpty() && this.getItem(0).getItem() instanceof ItemFocusBasic focus) {
         ItemStack focusStack = this.getItem(0);
         short[] s = focus.getAppliedUpgrades(focusStack);
         this.rank = 1;

         while (this.rank <= 5 && s[this.rank - 1] != -1) {
            this.rank++;
         }

         int xp = this.rank * 8;
         if (p.experienceLevel < xp && !p.getAbilities().instabuild) {
            return false;
         } else {
            FocusUpgradeType[] ut = focus.getPossibleUpgradesByRank(focusStack, this.rank);
            if (ut == null) {
               return false;
            } else {
               boolean b = false;

               for (int a = 0; a < ut.length; a++) {
                  if (ut[a].id == id) {
                     b = true;
                     break;
                  }
               }

               if (!b) {
                  return false;
               } else if (id <= FocusUpgradeType.types.length - 1
                  && FocusUpgradeType.types[id] != null
                  && focus.canApplyUpgrade(focusStack, p, FocusUpgradeType.types[id], this.rank)) {
                  int amt = 200;

                  for (int ax = 1; ax < this.rank; ax++) {
                     amt *= 2;
                  }

                  AspectList tal = new AspectList();

                  for (Aspect as : FocusUpgradeType.types[id].aspects.getAspects()) {
                     tal.add(as, amt);
                  }

                  this.aspects = ResearchManager.reduceToPrimals(tal);
                  this.size = this.aspects.visSize();
                  this.upgrade = id;
                  if (!p.getAbilities().instabuild) {
                     p.giveExperienceLevels(-xp);
                  }

                  this.setChanged();
                  this.markForUpdate();
                  this.level.playSound(null, this.worldPosition, (SoundEvent)ModSounds.CRAFT_START.get(), SoundSource.BLOCKS, 0.25F, 1.0F);
                  return true;
               } else {
                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.loadManipulatorData(compound);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.loadManipulatorData(compound);
   }

   private void loadManipulatorData(@NotNull CompoundTag compound) {
      this.aspects.load(compound);
      this.size = compound.getInt("size");
      this.upgrade = compound.getInt("upgrade");
      this.rank = compound.getInt("rank");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.saveManipulatorData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.saveManipulatorData(compound);
   }

   private void saveManipulatorData(@NotNull CompoundTag compound) {
      this.aspects.save(compound);
      compound.putInt("size", this.size);
      compound.putInt("upgrade", this.upgrade);
      compound.putInt("rank", this.rank);
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.loadAdditional(tag, provider);
      if (tag.contains("currentFocusSlot")) {
         this.currentItem = ItemStack.parseOptional(provider, tag.getCompound("currentFocusSlot"));
      } else {
         this.currentItem = ItemStack.EMPTY;
      }
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.saveAdditional(tag, provider);
      if (!this.currentItem.isEmpty()) {
         tag.put("currentFocusSlot", this.currentItem.saveOptional(provider));
      }
   }

   public AABB getRenderBoundingBox() {
      BlockPos p = this.worldPosition;
      return new AABB(p.getX(), p.getY() - 1, p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1);
   }

   @Override
   public void setItem(int index, @NotNull ItemStack stack) {
      super.setItem(index, stack);
      if (this.level != null && this.level.isClientSide) {
         this.reset = true;
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (this.level != null) {
         if (this.size > 0) {
            double x = this.worldPosition.getX() + 0.5 + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.3F;
            double y = this.worldPosition.getY() + 1.25 + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.3F;
            double z = this.worldPosition.getZ() + 0.5 + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.3F;
            Thaumcraft.setup
               .drawGenericParticles(
                  this.level,
                  x,
                  y,
                  z,
                  0.0,
                  0.0,
                  0.0,
                  0.5F + this.level.random.nextFloat() * 0.4F,
                  1.0F - this.level.random.nextFloat() * 0.4F,
                  1.0F - this.level.random.nextFloat() * 0.4F,
                  0.8F,
                  false,
                  112,
                  9,
                  1,
                  6 + this.level.random.nextInt(5),
                  0,
                  0.7F + this.level.random.nextFloat() * 0.4F
               );
         }
      }
   }

   @NotNull
   public Component getDisplayName() {
      return Component.empty();
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowsId, @NotNull Inventory inventory, @NotNull Player player) {
      return new FocalManipulatorMenu(windowsId, inventory, this);
   }
}
