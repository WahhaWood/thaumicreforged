package thaumcraft.entities.smart.taint.chicken;

import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderTaintChicken extends ChickenRenderer {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/chicken.png");

   public RenderTaintChicken(Context ctx) {
      super(ctx);
   }

   public ResourceLocation getTextureLocation(Chicken entity) {
      return TEX;
   }
}
