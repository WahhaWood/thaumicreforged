package thaumcraft.item.eldritch;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemEldritchAltarPlacer extends Item {
   private static final int STRUCTURE_RADIUS = 3;
   private static final int STRUCTURE_HEIGHT = 8;
   private static final int SAFE_DISTANCE = 7;
   private static final int PORTAL_SCAN_RANGE = 48;

   public ItemEldritchAltarPlacer() {
      super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(Component.translatable("item.thaumcraft.eldritch_altar_placer.tooltip").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)));
      components.add(
         Component.translatable("item.thaumcraft.eldritch_altar_placer.tooltip2").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true))
      );
   }

   @NotNull
   public InteractionResult useOn(UseOnContext context) {
      // Предмет незавершённый (источника получения нет): вместо null, ронявшего
      // игру NPE на сервере, вежливый PASS.
      return InteractionResult.PASS;
   }

   private void sendBlockedMessage(@NotNull Player player, BlockPos basePos) {
      player.displayClientMessage(
         Component.translatable("item.thaumcraft.eldritch_altar_placer.blocked_at", new Object[]{basePos.getX(), basePos.getY(), basePos.getZ()})
            .withStyle(ChatFormatting.RED),
         true
      );
   }
}
