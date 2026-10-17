package thaumcraft.compat.jei;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.research.PlayerKnowledge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class JeiUtil {
   public static final ResourceLocation TEXTURE = Thaumcraft.location("textures/screen/research_book_overlay.png");

   public static boolean isHovering(double mouseX, double mouseY, int x, int y, int width, int height) {
      return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
   }

   @OnlyIn(Dist.CLIENT)
   public static List<Component> getAspectTooltip(Aspect aspect, int amount) {
      Player player = Minecraft.getInstance().player;
      boolean discovered = player != null && PlayerKnowledge.of(player).hasDiscoveredAspect(aspect);
      return discovered
         ? List.of(aspect.getName(), aspect.getLocalizedDescription(), Component.literal("x" + amount))
         : List.of(Component.translatable("tc.aspect.unknown"), Component.literal("x" + amount));
   }
}
