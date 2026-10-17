package thaumcraft.item.armor.hover_harness;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.particles.fx.bolt.FXLightningBolt;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class HoverHarnessLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
   public static final ModelResourceLocation BACK_MODEL = ModelResourceLocation.standalone(
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/static/harness")
   );
   public static final ResourceLocation RING_SPRITE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "item/lightningring");
   private final Int2LongOpenHashMap timingShock = new Int2LongOpenHashMap();

   public HoverHarnessLayer(RenderLayerParent<T, M> parent) {
      super(parent);
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      T entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
      if (chest.getItem() instanceof ItemHoverHarness) {
         this.renderBackObj(poseStack, buffer, packedLight);
         if (entity instanceof Player player) {
            if (player.getAbilities().flying) {
               this.renderRings(poseStack, buffer);
               this.maybeSpawnBolt(player, entity.level(), partialTick);
            }
         }
      }
   }

   private void renderBackObj(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      BakedModel model = Minecraft.getInstance().getModelManager().getModel(BACK_MODEL);
      ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
      poseStack.pushPose();
      ((HumanoidModel)this.getParentModel()).body.translateAndRotate(poseStack);
      poseStack.scale(0.1F, 0.1F, 0.1F);
      poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
      poseStack.translate(0.0F, 0.33F, -3.7F);

      for (RenderType rt : model.getRenderTypes(ItemStack.EMPTY, false)) {
         VertexConsumer vc = buffer.getBuffer(rt);
         itemRenderer.renderModelLists(model, ItemStack.EMPTY, packedLight, OverlayTexture.NO_OVERLAY, poseStack, vc);
      }

      poseStack.popPose();
   }

   private void renderRings(PoseStack poseStack, MultiBufferSource buffer) {
      TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(RING_SPRITE);
      poseStack.pushPose();
      ((HumanoidModel)this.getParentModel()).body.translateAndRotate(poseStack);
      poseStack.translate(0.0F, 0.2F, 0.55F);
      int fullBright = 15728880;
      VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
      UtilsFX.drawQuadSpriteIconStyle(poseStack, vc, fullBright, sprite, 2.5F, 1.0F, 1.0F, 1.0F, 1.0F);
      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      poseStack.translate(0.0F, 0.0F, 0.03F);
      UtilsFX.drawQuadSpriteIconStyle(poseStack, vc, fullBright, sprite, 1.5F, 1.0F, 0.5F, 1.0F, 1.0F);
      poseStack.popPose();
      poseStack.popPose();
   }

   private void maybeSpawnBolt(Player player, Level level, float partialTick) {
      if (level instanceof ClientLevel cl) {
         long now = Util.getMillis();
         int id = player.getId();
         long next = this.timingShock.getOrDefault(id, 0L);
         if (next < now) {
            this.timingShock.put(id, now + 50L + level.random.nextInt(50));
            double px = Mth.lerp(partialTick, player.xo, player.getX());
            double py = Mth.lerp(partialTick, player.yo, player.getY());
            double pz = Mth.lerp(partialTick, player.zo, player.getZ());
            float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
            double yBase = py + player.getBbHeight() * 0.75;
            if (player.isCrouching()) {
               yBase -= 0.1;
            }

            double sx = px - Mth.cos((bodyYaw + 90.0F) * (float) (Math.PI / 180.0)) * 0.5;
            double sz = pz - Mth.sin((bodyYaw + 90.0F) * (float) (Math.PI / 180.0)) * 0.5;
            float yaw = bodyYaw - 90.0F - level.random.nextInt(180);
            float pitch = -80.0F + level.random.nextInt(160);
            Vec3 rayOrigin = new Vec3(px, yBase, pz);
            Vec3 dir = Vec3.directionFromRotation(pitch, yaw);
            Vec3 rayEnd = rayOrigin.add(dir.scale(6.0));
            HitResult hit = level.clip(new ClipContext(rayOrigin, rayEnd, Block.COLLIDER, Fluid.NONE, player));
            if (hit.getType() != Type.MISS) {
               Vec3 hv = hit.getLocation();
               FXLightningBolt bolt = new FXLightningBolt(cl, sx, yBase, sz, hv.x, hv.y, hv.z, level.random.nextLong(), 1, 2.0F, 3);
               bolt.defaultFractal();
               bolt.setType(6);
               bolt.setWidth(0.015F);
               bolt.finalizeBolt();
            }
         }
      }
   }
}
