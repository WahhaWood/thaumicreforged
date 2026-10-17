package thaumcraft.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class AspectTooltipData implements TooltipComponent {
   private final ResourceLocation texture;
   private final int color;
   private final Component name;
   private final int amount;

   public AspectTooltipData(ResourceLocation texture, int color, Component name, int amount) {
      this.texture = texture;
      this.color = color;
      this.name = name;
      this.amount = amount;
   }

   public ResourceLocation texture() {
      return this.texture;
   }

   public int color() {
      return this.color;
   }

   public Component name() {
      return this.name;
   }

   public int amount() {
      return this.amount;
   }
}
