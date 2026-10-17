package thaumcraft.entities.smart.eldrich_warden.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import thaumcraft.entities.smart.eldrich_guardian.render.EldritchGuardianModel;
import thaumcraft.entities.smart.eldrich_warden.EntityEldritchWarden;

public class EldritchWardenRenderer extends MobRenderer<EntityEldritchWarden, EldritchGuardianModel<EntityEldritchWarden>> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/eldritch_warden.png");

   public EldritchWardenRenderer(Context ctx) {
      super(ctx, new EldritchGuardianModel(ctx.bakeLayer(EldritchGuardianModel.LAYER_LOCATION)), 0.8F);
      this.addLayer(new EldritchWardenEyeLayer(this));
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull EntityEldritchWarden entity) {
      return TEX;
   }

   protected void scale(@NotNull EntityEldritchWarden entity, @NotNull PoseStack poseStack, float partialTickTime) {
      poseStack.scale(1.5F, 1.5F, 1.5F);
   }

   @NotNull
   public Vec3 getRenderOffset(@NotNull EntityEldritchWarden entity, float partialTicks) {
      int st = entity.getSpawnTimer();
      if (st > 0) {
         double k = st / 150.0;
         return new Vec3(0.0, -entity.getBbHeight() * k, 0.0);
      } else {
         return Vec3.ZERO;
      }
   }

   protected RenderType getRenderType(EntityEldritchWarden entity, boolean bodyVisible, boolean translucent, boolean glowing) {
      return RenderType.entityTranslucent(this.getTextureLocation(entity));
   }
}
