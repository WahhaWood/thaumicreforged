package thaumcraft.entities.smart.taint.creeper;

import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderTaintCreeper extends CreeperRenderer {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/creeper.png");

   public RenderTaintCreeper(Context ctx) {
      super(ctx);
   }

   public ResourceLocation getTextureLocation(Creeper entity) {
      return TEX;
   }
}
