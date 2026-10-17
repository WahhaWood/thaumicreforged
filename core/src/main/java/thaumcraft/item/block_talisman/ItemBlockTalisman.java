package thaumcraft.item.block_talisman;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.util.InventoryUtils;
import thaumcraft.util.ItemStackData;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemBlockTalisman extends Item implements ICurioItem {
   private static final String TAG_STORED_STACK = "StoredStack";
   private static final String TAG_COUNT = "Count";
   private static final String TAG_ACTIVE = "Active";

   public ItemBlockTalisman() {
      super(new Properties().stacksTo(1).rarity(Rarity.RARE));
   }

   public boolean canEquip(SlotContext slotContext, ItemStack stack) {
      return stack.getItem() == this && slotContext.identifier().equals("ring");
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack talisman = player.getItemInHand(hand);
      if (player.isShiftKeyDown() && !getStoredItem(talisman).isEmpty()) {
         boolean newState = !isActive(talisman);
         setActive(talisman, newState);
         float pitch = newState ? 1.15F : 0.85F;
         level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, pitch);
         return InteractionResultHolder.sidedSuccess(talisman, level.isClientSide);
      } else {
         return InteractionResultHolder.pass(talisman);
      }
   }

   @NotNull
   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         Level level = context.getLevel();
         BlockPos pos = context.getClickedPos();
         ItemStack talisman = context.getItemInHand();
         ItemStack stored = getStoredItem(talisman);
         if (!stored.isEmpty() && getStoredCount(talisman) != 0) {
            int currentCount = getStoredCount(talisman);
            if (currentCount <= 0) {
               return InteractionResult.PASS;
            } else {
               int inserted = insertIntoContainer(level, pos, context.getClickedFace(), stored, currentCount, !level.isClientSide);
               if (inserted > 0) {
                  if (!level.isClientSide && !player.getAbilities().instabuild) {
                     removeStoredCount(talisman, inserted);
                  }

                  return InteractionResult.sidedSuccess(level.isClientSide);
               } else if (!(stored.getItem() instanceof BlockItem)) {
                  return InteractionResult.PASS;
               } else {
                  final ItemStack placeStack = stored.copy();
                  placeStack.setCount(1);
                  UseOnContext placeContext = new UseOnContext(player, context.getHand(), hitResult(context)) {
                     @NotNull
                     public ItemStack getItemInHand() {
                        return placeStack;
                     }
                  };
                  InteractionResult result = placeStack.useOn(placeContext);
                  if (result.consumesAction()) {
                     if (!level.isClientSide && !player.getAbilities().instabuild) {
                        removeStoredCount(talisman, 1);
                     }

                     return result;
                  } else {
                     return InteractionResult.PASS;
                  }
               }
            }
         } else {
            BlockState state = level.getBlockState(pos);
            BlockHitResult hitResult = hitResult(context);
            ItemStack blockStack = state.getCloneItemStack(hitResult, level, pos, player);
            if (!blockStack.isEmpty()) {
               blockStack.setCount(1);
               setStoredItem(talisman, blockStack);
               player.displayClientMessage(Component.translatable("item.thaumcraft.block_talisman.bound", new Object[]{blockStack.getHoverName()}), true);
               return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
               return InteractionResult.FAIL;
            }
         }
      }
   }

   private static BlockHitResult hitResult(UseOnContext context) {
      return new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
   }

   private static int insertIntoContainer(Level level, BlockPos pos, Direction side, ItemStack stored, int currentCount, boolean doInsert) {
      if (level.getBlockEntity(pos) instanceof Container container) {
         ItemStack toInsert = stored.copy();
         toInsert.setCount(Math.min(currentCount, toInsert.getMaxStackSize()));
         ItemStack remainder = InventoryUtils.placeItemStackIntoInventory(toInsert, container, side, doInsert);
         return toInsert.getCount() - remainder.getCount();
      } else {
         return 0;
      }
   }

   public static boolean isActive(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag != null && tag.getBoolean("Active");
   }

   public static void setActive(ItemStack stack, boolean active) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.putBoolean("Active", active);
      ItemStackData.setTag(stack, tag);
   }

   public static ItemStack getStoredItem(ItemStack talisman) {
      CompoundTag tag = ItemStackData.getTag(talisman);
      return tag != null && tag.contains("StoredStack") ? ItemStackData.loadStack(tag.getCompound("StoredStack")) : ItemStack.EMPTY;
   }

   public static void setStoredItem(ItemStack talisman, ItemStack stack) {
      CompoundTag tag = ItemStackData.getOrCreateTag(talisman);
      if (stack.isEmpty()) {
         tag.remove("StoredStack");
         ItemStackData.setTag(talisman, tag);
      } else {
         ItemStack stored = stack.copy();
         stored.setCount(1);
         tag.put("StoredStack", ItemStackData.saveStack(stored));
         ItemStackData.setTag(talisman, tag);
      }
   }

   public static int getStoredCount(ItemStack talisman) {
      CompoundTag tag = ItemStackData.getTag(talisman);
      return tag == null ? 0 : tag.getInt("Count");
   }

   public static void setStoredCount(ItemStack talisman, int count) {
      CompoundTag tag = ItemStackData.getOrCreateTag(talisman);
      tag.putInt("Count", Math.max(0, count));
      ItemStackData.setTag(talisman, tag);
   }

   private static int removeStoredCount(ItemStack talisman, int count) {
      int current = getStoredCount(talisman);
      int removed = Math.min(current, Math.max(count, 0));
      setStoredCount(talisman, current - removed);
      return removed;
   }

   private static void addStoredCount(ItemStack talisman, int count) {
      if (count > 0) {
         long total = (long)getStoredCount(talisman) + count;
         setStoredCount(talisman, (int)Math.min(2147483647L, total));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      ItemStack stored = getStoredItem(stack);
      if (!stored.isEmpty()) {
         tooltip.add(
            Component.translatable("item.thaumcraft.block_talisman.bound", new Object[]{stored.getHoverName()})
               .append(Component.literal(" (x" + getStoredCount(stack) + ")"))
               .withStyle(ChatFormatting.GOLD)
         );
      }

      tooltip.add(
         Component.translatable(isActive(stack) ? "item.thaumcraft.block_talisman.active" : "item.thaumcraft.block_talisman.inactive")
            .withStyle(isActive(stack) ? ChatFormatting.GREEN : ChatFormatting.RED)
      );
   }
}
