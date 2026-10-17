package thaumcraft.entities.smart.taint.cow;

import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderTaintCow extends CowRenderer {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/cow.png");

   public RenderTaintCow(Context ctx) {
      super(ctx);
   }

   public ResourceLocation getTextureLocation(Cow entity) {
      return TEX;
   }
}
