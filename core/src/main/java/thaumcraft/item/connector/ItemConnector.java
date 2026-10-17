package thaumcraft.item.connector;

import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.transvector.TransvectorBlockEntity;
import thaumcraft.block.transvector.TransvectorInterfaceBlockEntity;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.util.ItemStackData;

public class ItemConnector extends Item {
   private static final String TAG_HAS_POS = "hasPos";
   private static final String TAG_POS_X = "posx";
   private static final String TAG_POS_Y = "posy";
   private static final String TAG_POS_Z = "posz";
   private static final String TAG_GOLEM_UUID = "golem_uuid";
   private static final String MSG_NOT_INTERFACE = "item.thaumcraft.connector.not_interface";
   private static final String MSG_SET = "item.thaumcraft.connector.set";
   private static final String MSG_NOT_PRESENT = "item.thaumcraft.connector.not_present";
   private static final String MSG_TOO_FAR = "item.thaumcraft.connector.too_far";
   private static final String MSG_COMPLETE = "item.thaumcraft.connector.complete";
   private static final String MSG_RESET = "item.thaumcraft.connector.reset";
   private static final String MSG_INTERFACE_FAIL = "item.thaumcraft.connector.interface_fail";

   public ItemConnector() {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
   }

   private static boolean hasPos(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag == null) {
         return false;
      } else {
         return tag.contains("hasPos") ? tag.getBoolean("hasPos") : tag.contains("posy") && tag.getInt("posy") != -1;
      }
   }

   private static int getX(ItemStack stack) {
      return ItemStackData.getInt(stack, "posx");
   }

   private static int getY(ItemStack stack) {
      return ItemStackData.getInt(stack, "posy");
   }

   private static int getZ(ItemStack stack) {
      return ItemStackData.getInt(stack, "posz");
   }

   @Nullable
   private static BlockPos getStoredPos(ItemStack stack) {
      return hasPos(stack) ? new BlockPos(getX(stack), getY(stack), getZ(stack)) : null;
   }

   private static void setPos(ItemStack stack, BlockPos pos) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.putBoolean("hasPos", true);
      tag.putInt("posx", pos.getX());
      tag.putInt("posy", pos.getY());
      tag.putInt("posz", pos.getZ());
   }

   private static void clearPos(ItemStack stack) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.putBoolean("hasPos", false);
      tag.putInt("posy", -1);
   }

   @NotNull
   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         BlockPos pos = context.getClickedPos();
         if (!level.isClientSide) {
            InteractionResult denied = (InteractionResult)null;
            if (denied != null) {
               return denied;
            }
         }

         ItemStack stack = context.getItemInHand();
         BlockEntity clicked = level.getBlockEntity(pos);
         if (!hasPos(stack)) {
            if (!(clicked instanceof TransvectorBlockEntity)) {
               if (!level.isClientSide) {
                  player.displayClientMessage(Component.translatable("item.thaumcraft.connector.not_interface").withStyle(ChatFormatting.RED), true);
               }

               return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
               if (!level.isClientSide) {
                  setPos(stack, pos);
                  this.playSound(level, pos);
                  player.displayClientMessage(Component.translatable("item.thaumcraft.connector.set").withStyle(ChatFormatting.GREEN), true);
               }

               return InteractionResult.sidedSuccess(level.isClientSide);
            }
         } else {
            BlockPos sourcePos = getStoredPos(stack);
            if (sourcePos == null) {
               clearPos(stack);
               return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
               if (!level.isClientSide) {
                  InteractionResult denied = (InteractionResult)null;
                  if (denied != null) {
                     return denied;
                  }
               }

               if (!level.isLoaded(sourcePos)) {
                  if (!level.isClientSide) {
                     clearPos(stack);
                     player.displayClientMessage(Component.translatable("item.thaumcraft.connector.not_present").withStyle(ChatFormatting.RED), true);
                  }

                  return InteractionResult.sidedSuccess(level.isClientSide);
               } else {
                  if (!level.isClientSide) {
                     if (!(level.getBlockEntity(sourcePos) instanceof TransvectorBlockEntity source)) {
                        clearPos(stack);
                        player.displayClientMessage(Component.translatable("item.thaumcraft.connector.not_present").withStyle(ChatFormatting.RED), true);
                        return InteractionResult.sidedSuccess(false);
                     }

                     if (source instanceof TransvectorInterfaceBlockEntity && clicked instanceof TransvectorInterfaceBlockEntity) {
                        player.displayClientMessage(Component.translatable("item.thaumcraft.connector.interface_fail").withStyle(ChatFormatting.RED), true);
                        return InteractionResult.sidedSuccess(false);
                     }

                     if (!source.canLinkTo(pos)) {
                        Component message = sourcePos.equals(pos)
                           ? Component.literal("Invalid transvector target").withStyle(ChatFormatting.RED)
                           : Component.translatable("item.thaumcraft.connector.too_far").withStyle(ChatFormatting.RED);
                        player.displayClientMessage(message, true);
                        return InteractionResult.sidedSuccess(false);
                     }

                     source.setTargetPos(pos);
                     clearPos(stack);
                     this.playSound(level, pos);
                     player.displayClientMessage(Component.translatable("item.thaumcraft.connector.complete").withStyle(ChatFormatting.GREEN), true);
                  }

                  return InteractionResult.sidedSuccess(level.isClientSide);
               }
            }
         }
      }
   }

   @NotNull
   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
      if (!player.isShiftKeyDown() || !(target instanceof EntityGolemBase)) {
         return InteractionResult.PASS;
      } else if (!hasPos(stack)) {
         if (!player.level().isClientSide) {
            player.displayClientMessage(Component.translatable("item.thaumcraft.connector.golem_not_interface").withStyle(ChatFormatting.RED), true);
         }

         return InteractionResult.sidedSuccess(player.level().isClientSide);
      } else {
         if (!player.level().isClientSide) {
            BlockPos targetPos = getStoredPos(stack);
            if (targetPos == null) {
               clearPos(stack);
               return InteractionResult.sidedSuccess(false);
            }

            if (false) {
               return InteractionResult.FAIL;
            }

            UUID id = target.getUUID();
            ItemStackData.getOrCreateTag(stack).putUUID("golem_uuid", id);
            clearPos(stack);
            this.playSound(player.level(), target.blockPosition());
            player.displayClientMessage(Component.translatable("item.thaumcraft.connector.golem_complete").withStyle(ChatFormatting.GREEN), true);
         }

         return InteractionResult.sidedSuccess(player.level().isClientSide);
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown() && hasPos(stack)) {
         if (!level.isClientSide) {
            clearPos(stack);
            player.displayClientMessage(Component.translatable("item.thaumcraft.connector.reset").withStyle(ChatFormatting.YELLOW), true);
         }

         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      BlockPos storedPos = getStoredPos(stack);
      if (storedPos != null) {
         tooltip.add(Component.literal("Bound: " + storedPos.getX() + ", " + storedPos.getY() + ", " + storedPos.getZ()).withStyle(ChatFormatting.AQUA));
      }
   }

   private void playSound(Level level, BlockPos pos) {
      level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
   }
}
