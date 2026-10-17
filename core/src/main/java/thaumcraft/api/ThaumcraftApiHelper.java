package thaumcraft.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.aspects.obj.ObjectAspect;
import thaumcraft.block.arcane_workbench.MagicWorkbenchBlockEntity;
import thaumcraft.recipe.ThaumcraftCraftingManager;

public class ThaumcraftApiHelper {
   public static AspectList cullTags(AspectList sourceTags) {
      AspectList culledTags = new AspectList();

      for (Aspect tag : sourceTags.getAspects()) {
         if (tag != null) {
            culledTags.add(tag, sourceTags.getAmount(tag));
         }
      }

      while (culledTags.size() > 6) {
         Aspect lowestAspect = null;
         float lowestWeight = 32767.0F;

         for (Aspect tagx : culledTags.getAspects()) {
            if (tagx != null) {
               float weightedAmount = culledTags.getAmount(tagx);
               if (tagx.isPrimal()) {
                  weightedAmount *= 0.9F;
               } else {
                  if (!tagx.getComponents()[0].isPrimal()) {
                     weightedAmount *= 1.1F;
                     if (!tagx.getComponents()[0].getComponents()[0].isPrimal()) {
                        weightedAmount *= 1.05F;
                     }

                     if (!tagx.getComponents()[0].getComponents()[1].isPrimal()) {
                        weightedAmount *= 1.05F;
                     }
                  }

                  if (!tagx.getComponents()[1].isPrimal()) {
                     weightedAmount *= 1.1F;
                     if (!tagx.getComponents()[1].getComponents()[0].isPrimal()) {
                        weightedAmount *= 1.05F;
                     }

                     if (!tagx.getComponents()[1].getComponents()[1].isPrimal()) {
                        weightedAmount *= 1.05F;
                     }
                  }
               }

               if (weightedAmount < lowestWeight) {
                  lowestWeight = weightedAmount;
                  lowestAspect = tagx;
               }
            }
         }

         culledTags.aspects.remove(lowestAspect);
      }

      return culledTags;
   }

   @NotNull
   public static ItemStack getStackInRowAndColumn(Object instance, int row, int column) {
      return ((MagicWorkbenchBlockEntity)instance).getStackInRowAndColumn(row, column);
   }

   public static AspectList getObjectAspects(ItemStack stack) {
      return ThaumcraftCraftingManager.getObjectTags(stack);
   }

   public static AspectList getRegisteredObjectTags(ItemStack stack) {
      return ThaumcraftApi.getObjectTags(stack);
   }

   public static AspectList getRegisteredObjectTags(ItemStack stack, @Nullable ObjectAspect excludedAspect) {
      return ThaumcraftApi.getObjectTags(stack, excludedAspect);
   }

   public static AspectList getBonusObjectTags(ItemStack stack, AspectList list) {
      return ThaumcraftCraftingManager.getBonusTags(stack, list);
   }

   @Nullable
   public static AspectList generateTags(ItemStack stack) {
      return AspectGenerationManager.generateTags(stack);
   }

   @Nullable
   public static AspectList generateTags(ItemStack stack, @Nullable ObjectAspect excludedAspect) {
      return AspectGenerationManager.generateTags(stack, excludedAspect);
   }

   public static BlockEntity getConnectableTile(LevelAccessor level, BlockPos blockPos, Direction face) {
      BlockEntity te = level.getBlockEntity(blockPos.relative(face));
      return te instanceof IEssenceTransport && ((IEssenceTransport)te).isConnectable(face.getOpposite()) ? te : null;
   }

   /**
    * TC4: block ray trace from {@code from} to the point {@code to} that skips the block the ray starts in
    * (the node casting it). Liquid sources stop the ray when {@code stopOnLiquid}; {@code collidableOnly}
    * ignores blocks without a collision box. Returns null on a miss, or a miss result when {@code returnMiss}.
    */
   @Nullable
   public static BlockHitResult rayTraceIgnoringSource(
      Level level, Vec3 from, Vec3 to, boolean stopOnLiquid, boolean collidableOnly, boolean returnMiss
   ) {
      BlockPos source = BlockPos.containing(from);
      ClipContext context = new ClipContext(
         from,
         to,
         collidableOnly ? ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE,
         stopOnLiquid ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE,
         CollisionContext.empty()
      );
      return BlockGetter.traverseBlocks(from, to, context, (ctx, pos) -> {
         if (pos.equals(source)) {
            return null;
         }

         BlockState state = level.getBlockState(pos);
         BlockHitResult block = level.clipWithInteractionOverride(from, to, pos, ctx.getBlockShape(state, level, pos), state);
         BlockHitResult fluid = ctx.getFluidShape(level.getFluidState(pos), level, pos).clip(from, to, pos);
         if (block == null || fluid == null) {
            return block == null ? fluid : block;
         } else {
            return from.distanceToSqr(block.getLocation()) <= from.distanceToSqr(fluid.getLocation()) ? block : fluid;
         }
      }, ctx -> returnMiss ? BlockHitResult.miss(to, Direction.getNearest(from.x - to.x, from.y - to.y, from.z - to.z), BlockPos.containing(to)) : null);
   }
}
