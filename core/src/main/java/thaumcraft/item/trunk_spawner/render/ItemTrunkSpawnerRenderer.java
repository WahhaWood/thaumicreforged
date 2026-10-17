package thaumcraft.item.trunk_spawner.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.block.hungry_chest.render.ChestModel;
import thaumcraft.item.golem_upgrade.GolemUpgrade;
import thaumcraft.item.golem_upgrade.ItemGolemUpgrade;
import thaumcraft.util.ItemStackData;

public class ItemTrunkSpawnerRenderer extends BlockEntityWithoutLevelRenderer {
   public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/trunk.png");
   public static final ModelLayerLocation TRUNK_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "trunk_spawner"), "main");
   private final ChestModel chestModel;

   public ItemTrunkSpawnerRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
      super(dispatcher, modelSet);
      ModelPart root = modelSet.bakeLayer(TRUNK_LAYER);
      this.chestModel = new ChestModel(root);
   }

   public void renderByItem(
      ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay
   ) {
      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);
      poseStack.scale(1.0F, -1.0F, -1.0F);
      poseStack.translate(0.0, -0.7, 0.0);
      VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
      this.chestModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay);
      if (ItemStackData.hasTag(stack) && ItemStackData.getTag(stack).contains("upgrade", 1)) {
         int rawUpgrade = ItemStackData.getTag(stack).getByte("upgrade");
         if (rawUpgrade >= 0 && rawUpgrade < GolemUpgrade.values().length) {
            GolemUpgrade upgrade = GolemUpgrade.values()[rawUpgrade];
            ItemGolemUpgrade upgradeItem = ItemGolemUpgrade.getItemByUpgrade(upgrade);
            if (upgradeItem != null) {
               ItemStack upgradeStack = new ItemStack(upgradeItem);
               TextureAtlasSprite sprite = Minecraft.getInstance().getItemRenderer().getModel(upgradeStack, null, null, 0).getParticleIcon();
               poseStack.pushPose();
               poseStack.translate(-0.075F, -0.185F, -0.245F);
               poseStack.scale(0.15F, 0.15F, 0.15F);
               VertexConsumer overlayConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
               Matrix4f pose = poseStack.last().pose();
               Matrix3f normal = poseStack.last().normal();
               float u0 = sprite.getU1();
               float u1 = sprite.getU0();
               float v0 = sprite.getV1();
               float v1 = sprite.getV0();
               overlayConsumer.addVertex(pose, 0.0F, 0.0F, 0.0F)
                  .setColor(255, 255, 255, 255)
                  .setUv(u0, v0)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setLight(packedLight)
                  .setNormal(0.0F, 0.0F, 1.0F);
               overlayConsumer.addVertex(pose, 1.0F, 0.0F, 0.0F)
                  .setColor(255, 255, 255, 255)
                  .setUv(u1, v0)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setLight(packedLight)
                  .setNormal(0.0F, 0.0F, 1.0F);
               overlayConsumer.addVertex(pose, 1.0F, 1.0F, 0.0F)
                  .setColor(255, 255, 255, 255)
                  .setUv(u1, v1)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setLight(packedLight)
                  .setNormal(0.0F, 0.0F, 1.0F);
               overlayConsumer.addVertex(pose, 0.0F, 1.0F, 0.0F)
                  .setColor(255, 255, 255, 255)
                  .setUv(u0, v1)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setLight(packedLight)
                  .setNormal(0.0F, 0.0F, 1.0F);
               poseStack.popPose();
            }
         }
      }

      poseStack.popPose();
   }
}
