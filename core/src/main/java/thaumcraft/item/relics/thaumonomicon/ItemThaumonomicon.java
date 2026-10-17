package thaumcraft.item.relics.thaumonomicon;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.relics.thaumonomicon.render.ResearchBrowserScreen;

public class ItemThaumonomicon extends Item {
   public ItemThaumonomicon() {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      if (!(player instanceof ServerPlayer serverPlayer)) {
         level.playSound(player, player, (SoundEvent)ModSounds.PAGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
         this.openScreen();
      }

      return super.use(level, player, hand);
   }

   @OnlyIn(Dist.CLIENT)
   private void openScreen() {
      Minecraft.getInstance().setScreen(new ResearchBrowserScreen(Minecraft.getInstance().player));
   }
}
