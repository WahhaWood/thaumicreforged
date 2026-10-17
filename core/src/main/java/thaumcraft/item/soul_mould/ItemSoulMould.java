package thaumcraft.item.soul_mould;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags.EntityTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.util.ItemStackData;

public class ItemSoulMould extends Item {
   private static final String TAG_PATTERN = "pattern";

   public ItemSoulMould() {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
   }

   private static void setPattern(ItemStack stack, LivingEntity living) {
      ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType());
      if (key != null) {
         ItemStackData.getOrCreateTag(stack).putString("pattern", key.toString());
      }
   }

   private static void clearPattern(ItemStack stack) {
      if (ItemStackData.hasTag(stack)) {
         ItemStackData.getTag(stack).remove("pattern");
      }
   }

   public static String getPatternName(ItemStack stack) {
      return ItemStackData.getString(stack, "pattern");
   }

   @NotNull
   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
      if (interactionTarget instanceof Player) {
         return InteractionResult.SUCCESS;
      } else if (interactionTarget.getType().is(EntityTypes.BOSSES)) {
         return InteractionResult.FAIL;
      } else {
         if (!player.level().isClientSide) {
            setPattern(stack, interactionTarget);
            player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F, 1.15F);
         }

         return InteractionResult.sidedSuccess(player.level().isClientSide);
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown()) {
         if (!level.isClientSide) {
            clearPattern(stack);
         }

         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      String id = getPatternName(stack);
      if (id.isEmpty()) {
         tooltip.add(Component.translatable("item.thaumcraft.soul_mould.non_assigned").withStyle(ChatFormatting.GRAY));
      } else {
         ResourceLocation key = ResourceLocation.tryParse(id);
         if (key == null) {
            tooltip.add(Component.translatable("item.thaumcraft.soul_mould.non_assigned").withStyle(ChatFormatting.GRAY));
         } else {
            String entityLang = "entity." + key.getNamespace() + "." + key.getPath();
            tooltip.add(
               Component.translatable("item.thaumcraft.soul_mould.pattern", new Object[]{Component.translatable(entityLang)}).withStyle(ChatFormatting.GOLD)
            );
         }
      }
   }
}
