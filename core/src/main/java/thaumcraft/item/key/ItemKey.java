package thaumcraft.item.key;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.opaque.OwnedBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.util.ItemStackData;

public class ItemKey extends Item {
   public ItemKey() {
      super(new Properties().rarity(Rarity.UNCOMMON));
   }

   public boolean isFoil(@NotNull ItemStack stack) {
      return ItemStackData.hasTag(stack);
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Level level = context.getLevel();
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         BlockPos clickedPos = context.getClickedPos();
         BlockState state = level.getBlockState(clickedPos);
         boolean isDoor = state.is((Block)ModBlocks.ARCANE_DOOR.get());
         boolean isPlate = state.is((Block)ModBlocks.ARCANE_PRESSURE_PLATE.get());
         if (!isDoor && !isPlate) {
            return InteractionResult.PASS;
         } else {
            byte type = (byte)(isPlate ? 1 : 0);
            BlockPos basePos;
            if (isDoor && state.hasProperty(DoorBlock.HALF) && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
               basePos = clickedPos.below();
            } else {
               basePos = clickedPos;
            }

            Object var10000 = null;
            String loc = basePos.getX() + "," + basePos.getY() + "," + basePos.getZ();
            if (level.getBlockEntity(basePos) instanceof OwnedBlockEntity owned) {
               boolean isIronKey = stack.is((Item)ModItems.IRON_KEY.get());
               String playerName = player.getScoreboardName();
               String givePrefix = isIronKey ? "0" : "1";
               CompoundTag tag = ItemStackData.getTag(stack);
               boolean isBound = tag != null && tag.contains("location") && tag.contains("type");
               Style style = Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_PURPLE);
               if (!isBound) {
                  return InteractionResult.sidedSuccess(level.isClientSide);
               } else {
                  String boundLoc = tag.getString("location");
                  byte boundType = tag.getByte("type");
                  if (boundType == type && loc.equals(boundLoc)) {
                     boolean alreadyHas = player.isCreative()
                        || playerName.equals(owned.owner)
                        || owned.accessList.contains("0" + playerName)
                        || owned.accessList.contains("1" + playerName);
                     if (!alreadyHas) {
                        return InteractionResult.sidedSuccess(level.isClientSide);
                     } else {
                        if (!level.isClientSide) {
                        }

                        return InteractionResult.sidedSuccess(level.isClientSide);
                     }
                  } else {
                     if (!level.isClientSide) {
                     }

                     return InteractionResult.sidedSuccess(level.isClientSide);
                  }
               }
            } else {
               return InteractionResult.sidedSuccess(level.isClientSide);
            }
         }
      }
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null && tag.contains("location")) {
         String location = tag.getString("location");

         try {
            String[] ss = location.split(",");
            location = "x " + ss[0] + ", z " + ss[2] + ", y " + ss[1];
         } catch (Exception var10) {
         }

         Style style = Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_PURPLE);
         byte type = tag.getByte("type");
         components.add(Component.translatable("tc.key9").setStyle(style));
         components.add((type == 0 ? Component.translatable("tc.key10") : Component.translatable("tc.key11")).setStyle(style));
         components.add(Component.literal(location).setStyle(style));
      }
   }
}
