package thaumcraft.block.funnel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.util.ItemStackData;

public class FunnelBlockEntity extends ThaumcraftSideInvBlockEntity implements IAspectContainer, ITickable {
   public FunnelBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.FUNNEL.get(), pos, state, 1);
   }

   @NotNull
   @Override
   public int[] getSlotsForFace(@NotNull Direction direction) {
      return direction == Direction.DOWN ? new int[0] : new int[]{0};
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      return index == 0 && isValidContainerItem(stack);
   }

   @Override
   public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
      return direction != Direction.DOWN;
   }

   public int getMaxStackSize() {
      return 1;
   }

   private static boolean isValidContainerItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         return stack.getItem() instanceof IEssenceContainerItem
            ? true
            : stack.getItem().builtInRegistryHolder().key().location().getPath().endsWith("aspect_jar");
      }
   }

   @Override
   public AspectList getAspects() {
      ItemStack stack = this.getItem(0);
      if (stack.isEmpty()) {
         return null;
      } else if (stack.getItem() instanceof IEssenceContainerItem containerItem) {
         return containerItem.getAspects(stack);
      } else {
         String path = stack.getItem().builtInRegistryHolder().key().location().getPath();
         if (!path.endsWith("aspect_jar")) {
            return null;
         } else {
            CompoundTag tag = ItemStackData.getTag(stack);
            if (tag == null) {
               return null;
            } else {
               String aspectTag = tag.getString("aspect");
               if (aspectTag.isEmpty()) {
                  return null;
               } else {
                  Aspect aspect = Aspect.getAspect(aspectTag);
                  if (aspect == null) {
                     return null;
                  } else {
                     return path.endsWith("infinity_aspect_jar") ? new AspectList().add(aspect, 64) : new AspectList().add(aspect, tag.getInt("aspectCount"));
                  }
               }
            }
         }
      }
   }

   @Override
   public void setAspects(AspectList aspectList) {
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return false;
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

   public class AspectSource {
      Aspect aspect;
      int amount;
      boolean infinite;

      public AspectSource(Aspect aspect, int amount, boolean infinite) {
         this.aspect = aspect;
         this.amount = amount;
         this.infinite = infinite;
      }

      public Aspect getAspect() {
         return this.aspect;
      }

      public int getAmount() {
         return this.amount;
      }

      public boolean isInfinite() {
         return this.infinite;
      }
   }
}
