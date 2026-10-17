package thaumcraft.init.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;

@EventBusSubscriber(
   modid = "thaumcraft"
)
public class ModDimensions {
   public static final ResourceKey<Level> OUTER_LANDS = ResourceKey.create(
      Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("thaumcraft", "outer_lands")
   );
   public static final ResourceKey<Level> BEDROCK = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("thaumcraft", "bedrock"));

   @SubscribeEvent
   public static void onChangedDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (OUTER_LANDS.equals(event.getTo())) {
            player.sendSystemMessage(Component.literal("Entering The Outer Lands"));
         } else if (OUTER_LANDS.equals(event.getFrom())) {
            player.sendSystemMessage(Component.literal("Leaving The Outer Lands"));
         }

         if (BEDROCK.equals(event.getTo())) {
            player.sendSystemMessage(Component.literal("Entering Bedrock"));
         } else if (BEDROCK.equals(event.getFrom())) {
            player.sendSystemMessage(Component.literal("Leaving Bedrock"));
         }
      }
   }
}
