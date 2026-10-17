package thaumcraft.entities.smart.taint.pig;

import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderTaintPig extends PigRenderer {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/pig.png");

   public RenderTaintPig(Context ctx) {
      super(ctx);
   }

   public ResourceLocation getTextureLocation(Pig entity) {
      return TEX;
   }
}
