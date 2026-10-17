package thaumcraft.entities.smart.fire_bat.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import thaumcraft.entities.smart.fire_bat.EntityFireBat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderFireBat extends MobRenderer<EntityFireBat, ModelFireBat<EntityFireBat>> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/firebat.png");
   public static final ResourceLocation TEX_VAMPIRE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/vampirebat.png");

   public RenderFireBat(Context ctx) {
      super(ctx, new ModelFireBat(ctx.bakeLayer(ModelFireBat.LAYER_LOCATION)), 0.25F);
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull EntityFireBat entity) {
      return entity.getIsVampire() ? TEX_VAMPIRE : TEX;
   }

   @OnlyIn(Dist.CLIENT)
   protected void scale(EntityFireBat entity, PoseStack poseStack, float partialTick) {
      float s = !entity.getIsDevil() && !entity.getIsVampire() ? 0.35F : 0.6F;
      poseStack.scale(s, s, s);
   }

   @OnlyIn(Dist.CLIENT)
   protected void setupRotations(EntityFireBat entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float scale) {
      if (!entity.getIsBatHanging()) {
         poseStack.translate(0.0F, Mth.cos(ageInTicks * 0.3F) * 0.1F, 0.0F);
      } else {
         poseStack.translate(0.0F, -0.1F, 0.0F);
      }

      super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTick, scale);
   }

   protected int getBlockLightLevel(EntityFireBat entity, BlockPos pos) {
      return 15;
   }
}
