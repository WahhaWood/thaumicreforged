package thaumcraft.entities.smart.taint.villager;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderTaintVillager extends MobRenderer<EntityTaintVillager, VillagerModel<EntityTaintVillager>> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/villager.png");

   public RenderTaintVillager(Context ctx) {
      super(ctx, new VillagerModel(ctx.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
   }

   public ResourceLocation getTextureLocation(EntityTaintVillager entity) {
      return TEX;
   }

   @OnlyIn(Dist.CLIENT)
   protected void scale(EntityTaintVillager entity, PoseStack poseStack, float partialTick) {
      poseStack.scale(0.9375F, 0.9375F, 0.9375F);
   }
}
