package thaumcraft.entities.smart.eldrich_guardian.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import thaumcraft.entities.smart.eldrich_guardian.EntityEldritchGuardian;
import thaumcraft.init.registry.ModDimensions;

public class EldritchGuardianRenderer extends MobRenderer<EntityEldritchGuardian, EldritchGuardianModel<EntityEldritchGuardian>> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/eldritch_guardian.png");

   public EldritchGuardianRenderer(Context ctx) {
      super(ctx, new EldritchGuardianModel(ctx.bakeLayer(EldritchGuardianModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(EntityEldritchGuardian entity) {
      return TEX;
   }

   protected RenderType getRenderType(EntityEldritchGuardian entity, boolean bodyVisible, boolean translucent, boolean glowing) {
      return RenderType.entityTranslucent(this.getTextureLocation(entity));
   }

   public void render(EntityEldritchGuardian entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      float alpha = this.computeAlpha(entity);
      ((EldritchGuardianModel)this.model).setRenderAlpha(alpha);

      try {
         super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      } finally {
         ((EldritchGuardianModel)this.model).setRenderAlpha(1.0F);
      }
   }

   private float computeAlpha(EntityEldritchGuardian entity) {
      Entity cam = Minecraft.getInstance().getCameraEntity();
      if (cam != null && entity.level() != null) {
         if (entity.level().dimension().equals(ModDimensions.OUTER_LANDS)) {
            return 1.0F;
         } else {
            double distSq = entity.distanceToSqr(cam);
            if (distSq < 256.0) {
               return 0.6F;
            } else {
               Difficulty diff = entity.level().getDifficulty();
               double max = diff == Difficulty.HARD ? 576.0 : 1024.0;
               double min = 256.0;
               double denom = max - min;
               if (denom <= 0.0) {
                  return 0.6F;
               } else {
                  double t = 1.0 - Math.min(denom, distSq - min) / denom;
                  float a = (float)(t * 0.6);
                  return Mth.clamp(a, 0.0F, 1.0F);
               }
            }
         }
      } else {
         return 1.0F;
      }
   }
}
