package thaumcraft.block.wand_recharger;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.api.nodes.INode;
import thaumcraft.block.jar.node.NodeJarBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.item.curios.ItemAmuletVis;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.research.ResearchManager;

public class WandPedestalBlockEntity extends ThaumcraftSideInvBlockEntity implements IAspectContainer, ITickable {
   int counter = 0;
   boolean somethingChanged = false;
   public boolean draining = false;
   public int drainX = 0;
   public int drainY = 0;
   public int drainZ = 0;
   public int drainColor = 0;
   ArrayList<BlockPos> nodes = null;
   private BlockPos lastDrainedNodePos = null;

   public WandPedestalBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.WAND_PEDESTAL.get(), blockPos, blockState, 1);
   }

   public int getMaxStackSize() {
      return 1;
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.savePedestalData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.savePedestalData(compound);
   }

   private void savePedestalData(@NotNull CompoundTag compound) {
      compound.putBoolean("draining", this.draining);
      compound.putInt("drainX", this.drainX);
      compound.putInt("drainY", this.drainY);
      compound.putInt("drainZ", this.drainZ);
      compound.putInt("drainColor", this.drainColor);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.loadPedestalData(compound);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.loadPedestalData(compound);
   }

   private void loadPedestalData(@NotNull CompoundTag compound) {
      this.draining = compound.getBoolean("draining");
      this.drainX = compound.getInt("drainX");
      this.drainY = compound.getInt("drainY");
      this.drainZ = compound.getInt("drainZ");
      this.drainColor = compound.getInt("drainColor");
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.nodes == null) {
         this.findNodes();
      }

      this.counter++;
      boolean recalc = false;
      if (this.counter % 20 == 0 && this.somethingChanged && this.nodes != null && !this.nodes.isEmpty() && !this.getItem(0).isEmpty()) {
         this.setChanged();
         this.markForUpdate();
         this.somethingChanged = false;
      }

      if (this.counter % 5 == 0 && this.nodes != null && !this.nodes.isEmpty() && !this.getItem(0).isEmpty()) {
         boolean hasThingy = level.getBlockState(pos.above()).is((Block)ModBlocks.WAND_PEDESTAL_FOCUS.get());
         ItemStack stack = this.getItem(0);

         if (stack.getItem() instanceof ItemWandCasting wand) {
            int min = 1;
            if (wand.getCap(stack).getTag().equals("iron") || wand.getRod(stack).getTag().equals("wood")) {
               min = 0;
            }

            AspectList as = wand.getAspectsWithRoom(stack);
            boolean wasDraining = this.draining;
            this.draining = false;
            if (as != null && as.size() > 0) {
               drainLoop:
               for (BlockPos nodePos : this.nodes) {
                  if (level.getBlockEntity(nodePos) instanceof INode node && !(level.getBlockEntity(nodePos) instanceof NodeJarBlockEntity)) {
                     for (Aspect aspect : as.getAspects()) {
                        if (node.getAspects().getAmount(aspect) > min) {
                           wand.addVis(stack, aspect, 1, true);
                           node.takeFromContainer(aspect, 1);
                           this.somethingChanged = true;
                           this.draining = true;
                           this.drainX = nodePos.getX();
                           this.drainY = nodePos.getY();
                           this.drainZ = nodePos.getZ();
                           this.drainColor = aspect.getColor();
                           this.setChanged();
                           this.markForUpdate();
                           break drainLoop;
                        }
                     }

                     if (hasThingy) {
                        for (Aspect aspectx : node.getAspects().getAspects()) {
                           if (aspectx != null && !aspectx.isPrimal()) {
                              AspectList primals = ResearchManager.reduceToPrimals(new AspectList().add(aspectx, 1));
                              for (Aspect aspect2 : as.getAspects()) {
                                 if (primals.getAmount(aspect2) > 0 && node.getAspects().getAmount(aspectx) > min) {
                                    wand.addVis(stack, aspect2, 1, true);
                                    node.takeFromContainer(aspectx, 1);
                                    this.somethingChanged = true;
                                    this.draining = true;
                                    this.drainX = nodePos.getX();
                                    this.drainY = nodePos.getY();
                                    this.drainZ = nodePos.getZ();
                                    this.drainColor = aspectx.getColor();
                                    this.setChanged();
                                    this.markForUpdate();
                                    break drainLoop;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (!this.draining) {
                  recalc = true;
               }
            }

            if (wasDraining != this.draining) {
               this.setChanged();
               this.markForUpdate();
            }
         } else if (stack.getItem() instanceof ItemAmuletVis amulet) {
            int minx = 1;
            AspectList as = amulet.getAspectsWithRoom(stack);
            boolean wasDraining = this.draining;
            this.draining = false;
            if (as != null && as.size() > 0) {
               drainAmuletLoop:
               for (BlockPos nodePos : this.nodes) {
                  if (level.getBlockEntity(nodePos) instanceof INode node && !(level.getBlockEntity(nodePos) instanceof NodeJarBlockEntity)) {
                     for (Aspect aspectxx : as.getAspects()) {
                        if (node.getAspects().getAmount(aspectxx) > minx) {
                           amulet.addVis(stack, aspectxx, 1, true);
                           node.takeFromContainer(aspectxx, 1);
                           this.somethingChanged = true;
                           this.draining = true;
                           this.drainX = nodePos.getX();
                           this.drainY = nodePos.getY();
                           this.drainZ = nodePos.getZ();
                           this.drainColor = aspectxx.getColor();
                           this.setChanged();
                           this.markForUpdate();
                           break drainAmuletLoop;
                        }
                     }

                     if (hasThingy) {
                        for (Aspect aspectxxx : node.getAspects().getAspects()) {
                           if (aspectxxx != null && !aspectxxx.isPrimal()) {
                              AspectList primals = ResearchManager.reduceToPrimals(new AspectList().add(aspectxxx, 1));
                              for (Aspect aspect2x : as.getAspects()) {
                                 if (primals.getAmount(aspect2x) > 0 && node.getAspects().getAmount(aspectxxx) > minx) {
                                    amulet.addVis(stack, aspect2x, 1, true);
                                    node.takeFromContainer(aspectxxx, 1);
                                    this.somethingChanged = true;
                                    this.draining = true;
                                    this.drainX = nodePos.getX();
                                    this.drainY = nodePos.getY();
                                    this.drainZ = nodePos.getZ();
                                    this.drainColor = aspectxxx.getColor();
                                    this.setChanged();
                                    this.markForUpdate();
                                    break drainAmuletLoop;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (!this.draining) {
                  recalc = true;
               }
            }

            if (wasDraining != this.draining) {
               this.setChanged();
               this.markForUpdate();
            }
         } else if (this.draining) {
            this.draining = false;
            this.setChanged();
            this.markForUpdate();
         }
      }

      if (this.counter % 100 == 0 && (recalc || this.nodes == null || this.nodes.isEmpty())) {
         this.findNodes();
      }
   }

   private void findNodes() {
      this.nodes = new ArrayList<>();
      if (this.level == null) return;
      for (int xx = -8; xx <= 8; xx++) {
         for (int yy = -8; yy <= 8; yy++) {
            for (int zz = -8; zz <= 8; zz++) {
               BlockPos p = this.worldPosition.offset(xx, yy, zz);
               if (this.level.getBlockEntity(p) instanceof INode) {
                  this.nodes.add(p);
               }
            }
         }
      }
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      return stack.getItem() instanceof ItemWandCasting || stack.getItem() instanceof ItemAmuletVis;
   }

   @Override
   public AspectList getAspects() {
      ItemStack stack = this.getItem(0);
      if (stack.getItem() instanceof ItemWandCasting wand) {
         AspectList al = wand.getAllVis(stack);
         AspectList out = new AspectList();

         for (Aspect a : al.getAspectsSorted()) {
            out.add(a, al.getAmount(a) / 100);
         }

         return out;
      } else if (!(stack.getItem() instanceof ItemAmuletVis amulet)) {
         return null;
      } else {
         AspectList al = amulet.getAllVis(stack);
         AspectList out = new AspectList();

         for (Aspect a : al.getAspectsSorted()) {
            out.add(a, al.getAmount(a) / 100);
         }

         return out;
      }
   }

   @Override
   public void setAspects(AspectList aspectList) {
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      return 0;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      return false;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return 0;
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }
}
