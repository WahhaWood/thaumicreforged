package thaumcraft.entities.smart.travelling_trunk.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk;
import thaumcraft.item.golem_upgrade.GolemUpgrade;
import thaumcraft.item.golem_upgrade.ItemGolemUpgrade;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderTravelingTrunk extends MobRenderer<EntityTravelingTrunk, ModelTrunk> {
   public static final ResourceLocation RL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/trunk.png");
   public static final ResourceLocation RL_A = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/trunkangry.png");

   public RenderTravelingTrunk(Context ctx) {
      super(ctx, new ModelTrunk(ctx.bakeLayer(ModelTrunk.LAYER_LOCATION)), 0.5F);
      this.addLayer(new RenderTravelingTrunk.UpgradeLayer(this));
   }

   @OnlyIn(Dist.CLIENT)
   protected void scale(@NotNull EntityTravelingTrunk e, @NotNull PoseStack ps, float partialTickTime) {
      int n2 = 2;
      float n3 = (e.field_767_b + (e.field_768_a - e.field_767_b) * partialTickTime) / (n2 * 0.5F + 1.0F);
      float n4 = 1.0F / (n3 + 1.0F);
      float n7 = n4 / 1.4F;
      float n8 = e.hasUpgrade(GolemUpgrade.EARTH) ? n2 / 1.33F : n2 / 1.5F;
      ps.scale(n7 * n8, 0.5F / n7 * n8, n7 * n8);
      ps.translate(-0.5F, 0.5F, -0.5F);
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull EntityTravelingTrunk e) {
      return e.getAnger() > 0 ? RL_A : RL;
   }

   public static class UpgradeLayer extends RenderLayer<EntityTravelingTrunk, ModelTrunk> {
      public UpgradeLayer(RenderTravelingTrunk parent) {
         super(parent);
      }

      @OnlyIn(Dist.CLIENT)
      public void render(
         @NotNull PoseStack ps,
         @NotNull MultiBufferSource buffers,
         int light,
         @NotNull EntityTravelingTrunk e,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         GolemUpgrade upgrade = e.getUpgradeType();
         if (upgrade != null) {
            ItemStack stack = ItemGolemUpgrade.getStackByUpgrade(upgrade);
            if (!stack.isEmpty()) {
               ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
               BakedModel baked = itemRenderer.getModel(stack, e.level(), e, 0);
               TextureAtlasSprite sprite = baked.getParticleIcon();
               ps.pushPose();
               ((ModelTrunk)this.getParentModel()).chestKnob.translateAndRotate(ps);
               ps.translate(-0.075F, -0.115F, -0.94301F);
               ps.scale(0.15F, 0.15F, 0.15F);
               VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
               Matrix4f pose = ps.last().pose();
               Matrix3f normal = ps.last().normal();
               float u0 = sprite.getU1();
               float u1 = sprite.getU0();
               float v0 = sprite.getV1();
               float v1 = sprite.getV0();
               vc.addVertex(pose, 0.0F, 0.0F, 0.0F)
                  .setColor(255, 255, 255, 255)
                  .setUv(u0, v0)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setUv2(light & 65535, light >> 16)
                  .setNormal(0.0F, 0.0F, 1.0F);
               vc.addVertex(pose, 1.0F, 0.0F, 0.0F)
                  .setColor(255, 255, 255, 255)
                  .setUv(u1, v0)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setUv2(light & 65535, light >> 16)
                  .setNormal(0.0F, 0.0F, 1.0F);
               vc.addVertex(pose, 1.0F, 1.0F, 0.0F)
                  .setColor(255, 255, 255, 255)
                  .setUv(u1, v1)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setUv2(light & 65535, light >> 16)
                  .setNormal(0.0F, 0.0F, 1.0F);
               vc.addVertex(pose, 0.0F, 1.0F, 0.0F)
                  .setColor(255, 255, 255, 255)
                  .setUv(u0, v1)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setUv2(light & 65535, light >> 16)
                  .setNormal(0.0F, 0.0F, 1.0F);
               ps.popPose();
            }
         }
      }
   }
}
