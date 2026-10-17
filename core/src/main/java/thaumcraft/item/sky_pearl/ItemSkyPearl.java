package thaumcraft.item.sky_pearl;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.util.ItemStackData;

public class ItemSkyPearl extends Item {
   public static final String TAG_X = "x";
   public static final String TAG_Y = "y";
   public static final String TAG_Z = "z";
   public static final String TAG_DIM = "dim";

   public ItemSkyPearl() {
      super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
   }

   public static void setValues(ItemStack stack, BlockPos pos, ResourceLocation dim) {
      ItemStackData.getOrCreateTag(stack).putInt("x", pos.getX());
      ItemStackData.getOrCreateTag(stack).putInt("y", pos.getY());
      ItemStackData.getOrCreateTag(stack).putInt("z", pos.getZ());
      ItemStackData.getOrCreateTag(stack).putString("dim", dim.toString());
   }

   public static boolean isAttuned(ItemStack stack) {
      return ItemStackData.hasTag(stack) && ItemStackData.getTag(stack).contains("y") && ItemStackData.getInt(stack, "y") != -1;
   }

   public static int getX(ItemStack stack) {
      return isAttuned(stack) ? ItemStackData.getInt(stack, "x") : 0;
   }

   public static int getY(ItemStack stack) {
      return isAttuned(stack) ? ItemStackData.getInt(stack, "y") : 0;
   }

   public static int getZ(ItemStack stack) {
      return isAttuned(stack) ? ItemStackData.getInt(stack, "z") : 0;
   }

   public static String getDim(ItemStack stack) {
      return isAttuned(stack) ? ItemStackData.getString(stack, "dim") : "";
   }

   @NotNull
   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         BlockPos pos = context.getClickedPos();
         Block block = level.getBlockState(pos).getBlock();
         if (block == ModBlocks.WARP_GATE.get() && !isAttuned(context.getItemInHand())) {
            if (!level.isClientSide) {
               setValues(context.getItemInHand(), pos, level.dimension().location());
               level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown() && isAttuned(stack)) {
         if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
            ItemStackData.getOrCreateTag(stack).putInt("y", -1);
         }

         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      if (isAttuned(stack) && level != null) {
         int x = getX(stack);
         int y = getY(stack);
         int z = getZ(stack);
         tooltip.add(Component.literal("X: " + x));
         tooltip.add(Component.literal("Y: " + y));
         tooltip.add(Component.literal("Z: " + z));
         String dim = getDim(stack);
         String currentDim = level.dimension().location().toString();
         if (!dim.equals(currentDim)) {
            tooltip.add(Component.translatable("item.thaumcraft.sky_pearl.different_dim").withStyle(ChatFormatting.RED));
         } else {
            tooltip.add(Component.translatable("item.thaumcraft.sky_pearl.attuned").withStyle(ChatFormatting.BLUE));
         }
      }
   }

   public boolean isFoil(@NotNull ItemStack stack) {
      return isAttuned(stack);
   }
}
