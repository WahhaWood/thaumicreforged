package thaumcraft.item.equipment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.SimpleTier;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IWarpingGear;
import thaumcraft.init.registry.ModItems;

public class ItemPrimalCrusher extends DiggerItem implements IRepairable, IWarpingGear {
   private static final SimpleTier PRIMAL_CRUSHER_TIER = new SimpleTier(
      BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 500, 8.0F, 4.0F, 20, () -> Ingredient.of(new ItemLike[]{(ItemLike)ModItems.PRIMAL_CHARM.get()})
   );
   private static boolean isBreakingArea = false;

   public ItemPrimalCrusher() {
      super(
         PRIMAL_CRUSHER_TIER,
         BlockTags.MINEABLE_WITH_PICKAXE,
         new Properties().rarity(Rarity.EPIC).attributes(DiggerItem.createAttributes(PRIMAL_CRUSHER_TIER, 3.5F, 1.0F))
      );
   }

   public boolean isCorrectToolForDrops(@NotNull ItemStack stack, @NotNull BlockState blockState) {
      return blockState.is(BlockTags.MINEABLE_WITH_SHOVEL) || super.isCorrectToolForDrops(stack, blockState);
   }

   public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState blockState) {
      return !blockState.is(BlockTags.MINEABLE_WITH_PICKAXE) && !blockState.is(BlockTags.MINEABLE_WITH_SHOVEL) ? 1.0F : this.getTier().getSpeed();
   }

   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean selected) {
      if (stack.isDamageableItem() && entity instanceof LivingEntity livingEntity && livingEntity.tickCount % 20 == 0) {
         stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
      }
   }

   public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
      if (isBreakingArea) {
         return false;
      } else {
         if (player instanceof ServerPlayer serverPlayer && !serverPlayer.isShiftKeyDown()) {
            isBreakingArea = true;

            try {
               HitResult ray = player.pick(20.0, 0.0F, false);
               Direction direction = ray.getType() == Type.BLOCK
                  ? (serverPlayer.level().getBlockState(pos).isAir() ? serverPlayer.getDirection() : ((BlockHitResult)ray).getDirection())
                  : serverPlayer.getDirection();
               int xRange = 0;
               int yRange = 0;
               int zRange = 0;
               if (direction == Direction.UP || direction == Direction.DOWN) {
                  xRange = 1;
                  zRange = 1;
               } else if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                  xRange = 1;
                  yRange = 1;
               } else if (direction == Direction.EAST || direction == Direction.WEST) {
                  zRange = 1;
                  yRange = 1;
               }

               for (int xPos = -xRange; xPos <= xRange; xPos++) {
                  for (int yPos = -yRange; yPos <= yRange; yPos++) {
                     for (int zPos = -zRange; zPos <= zRange; zPos++) {
                        if (xPos != 0 || yPos != 0 || zPos != 0) {
                           BlockPos targetPos = pos.offset(xPos, yPos, zPos);
                           BlockState targetState = serverPlayer.level().getBlockState(targetPos);
                           if (!targetState.isAir()
                              && targetState.getDestroySpeed(serverPlayer.level(), targetPos) >= 0.0F
                              && serverPlayer.gameMode.destroyBlock(targetPos)) {
                              itemstack.hurtAndBreak(1, serverPlayer.serverLevel(), serverPlayer, item -> {});
                           }
                        }
                     }
                  }
               }
            } finally {
               isBreakingArea = false;
            }
         }

         return false;
      }
   }

   public int getEnchantmentValue() {
      return 20;
   }

   @Override
   public int getWarp(ItemStack itemstack, Player player) {
      return 2;
   }
}
