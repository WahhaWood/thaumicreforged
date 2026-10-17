package thaumcraft.item.mana_bean;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.IEssenceContainerItem;

public class ItemManaBean extends BlockItem implements IEssenceContainerItem {
   public ItemManaBean(Block block) {
      super(block, new Properties().food(new Builder().nutrition(1).saturationModifier(0.5F).build()));
   }

   public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
      return 10;
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      this.appendHoverAspects(stack, components);
   }

   @NotNull
   public InteractionResult useOn(@NotNull UseOnContext ctx) {
      Player player = ctx.getPlayer();
      if (player == null) {
         return InteractionResult.PASS;
      } else if (ctx.getClickedFace() != Direction.DOWN) {
         return InteractionResult.PASS;
      } else {
         Level level = ctx.getLevel();
         ItemStack stack = ctx.getItemInHand();
         BlockPos logPos = ctx.getClickedPos();
         BlockPos placePos = logPos.below();
         if (!player.mayUseItemAt(placePos, ctx.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
         } else if (!level.isEmptyBlock(placePos)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            BlockState state = this.getBlock().defaultBlockState();
            if (!state.canSurvive(level, placePos)) {
               return InteractionResult.PASS;
            }

            // Сажаем как обычный BlockItem (аспект назначится ростом/скрещиванием).
            return super.useOn(ctx);
         }
      }
   }
}
