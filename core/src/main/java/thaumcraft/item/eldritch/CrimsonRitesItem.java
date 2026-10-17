package thaumcraft.item.eldritch;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrimsonRitesItem extends EldritchObjectItem {
   @Override
   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(Component.translatable("item.ItemEldritchObject.text.2").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)));
      components.add(Component.translatable("item.ItemEldritchObject.text.3").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_BLUE)));
   }

   @NotNull
   @Override
   public net.minecraft.world.InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull net.minecraft.world.entity.player.Player player, @NotNull net.minecraft.world.InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide) {
         if (!thaumcraft.research.ResearchManager.isResearchComplete(player, "crimson")) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
               thaumcraft.network.PacketHandler.sendTo(new thaumcraft.network.playerdata.PacketResearchComplete("crimson"), serverPlayer);
            }
            thaumcraft.Thaumcraft.setup.getResearchManager().completeResearch(player, "crimson");
            level.playSound(null, player.getX(), player.getY(), player.getZ(), (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.LEARN.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.75F, 1.0F);
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, false);
         }
      }
      return net.minecraft.world.InteractionResultHolder.pass(stack);
   }
}
