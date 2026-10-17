package thaumcraft.entities.smart.golem.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.block.jar.standart.JarBlockItem;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.entities.smart.golem.hook.GolemHooks;
import thaumcraft.item.golem_core.GolemCore;
import thaumcraft.item.golem_core.ItemGolemCore;
import thaumcraft.item.golem_decoration.GolemDecorationType;
import thaumcraft.item.golem_upgrade.GolemUpgrade;
import thaumcraft.item.golem_upgrade.ItemGolemUpgrade;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class RenderGolemBase extends MobRenderer<EntityGolemBase, ModelGolem> {
   public static final ResourceLocation TEX_DECORATION = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/golem_decoration.png");
   public static final ResourceLocation TEX_DAMAGE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/golem_damage.png");
   public static final ResourceLocation TEX_CLAY = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/golem_clay.png");
   public static final ResourceLocation TEX_STONE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/golem_stone.png");
   public static final ResourceLocation TEX_WOOD = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/golem_wood.png");
   public static final ResourceLocation TEX_TALLOW = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/golem_tallow.png");
   public static final ResourceLocation TEX_IRON = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/golem_iron.png");
   public static final ResourceLocation TEX_STRAW = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/golem_straw.png");
   public static final ResourceLocation TEX_FLESH = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/golem_flesh.png");
   public static final ResourceLocation TEX_THAUMIUM = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/golem_thaumium.png");

   public RenderGolemBase(Context ctx) {
      super(ctx, new ModelGolem(ctx.bakeLayer(ModelGolem.LAYER_LOCATION)), 0.25F);
      this.addLayer(new RenderGolemBase.AccessoriesLayer(this, new ModelGolemAccessories(ctx.bakeLayer(ModelGolemAccessories.LAYER_LOCATION))));
      this.addLayer(new RenderGolemBase.DamageLayer(this));
      this.addLayer(new RenderGolemBase.CoreAndUpgradesLayer(this));
      this.addLayer(new RenderGolemBase.CarriedItemLayer(this));
      this.addLayer(new RenderGolemBase.GolemHookLayer(this));
   }

   @OnlyIn(Dist.CLIENT)
   public void render(
      @NotNull EntityGolemBase entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight
   ) {
      float r = 1.0F;
      float g = 1.0F;
      float b = 1.0F;
      if (entity.healing > 0) {
         float n = entity.healing / 10.0F;
         r = 0.5F + n;
         g = 0.9F + entity.healing / 5.0F;
         b = 0.5F + n;
      }

      RenderSystem.setShaderColor(r, g, b, 1.0F);
      super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @NotNull
   public Vec3 getRenderOffset(@NotNull EntityGolemBase entity, float partialTicks) {
      return new Vec3(0.0, 0.925, 0.0);
   }

   @OnlyIn(Dist.CLIENT)
   protected void scale(EntityGolemBase entity, PoseStack poseStack, float partialTick) {
      poseStack.scale(0.4F, 0.4F, 0.4F);
   }

   protected void setupRotations(
      @NotNull EntityGolemBase entity, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float scale
   ) {
      super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTick, scale);
      float limbSwingAmount = entity.walkAnimation.speed(partialTick);
      if (limbSwingAmount >= 0.01F) {
         float limbSwing = entity.walkAnimation.position(partialTick);
         float period = 13.0F;
         float t = (limbSwing - limbSwingAmount * (1.0F - partialTick) + 6.0F) % period;
         float sway = 6.5F * ((Math.abs(t - period * 0.5F) - period * 0.25F) / (period * 0.25F));
         poseStack.mulPose(Axis.ZP.rotationDegrees(sway));
      }
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull EntityGolemBase entity) {
      return switch (entity.getGolemType()) {
         case STRAW -> TEX_STRAW;
         case WOOD -> TEX_WOOD;
         case CLAY -> TEX_CLAY;
         case STONE -> TEX_STONE;
         case IRON -> TEX_IRON;
         case TALLOW -> TEX_TALLOW;
         case FLESH -> TEX_FLESH;
         case THAUMIUM -> TEX_THAUMIUM;
         default -> TEX_WOOD;
      };
   }

   public static final class AccessoriesLayer extends RenderLayer<EntityGolemBase, ModelGolem> {
      private final ModelGolemAccessories accessoriesModel;

      protected AccessoriesLayer(RenderGolemBase parent, ModelGolemAccessories accessoriesModel) {
         super(parent);
         this.accessoriesModel = accessoriesModel;
      }

      @OnlyIn(Dist.CLIENT)
      public void render(
         @NotNull PoseStack poseStack,
         @NotNull MultiBufferSource buffer,
         int packedLight,
         @NotNull EntityGolemBase entity,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (!entity.currentDecorations.isEmpty() || entity.advanced) {
            ((ModelGolem)this.getParentModel()).copyPropertiesTo(this.accessoriesModel);
            VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(RenderGolemBase.TEX_DECORATION));
            this.accessoriesModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
            this.accessoriesModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            if (entity.hasDecoration(GolemDecorationType.DART)) {
               this.accessoriesModel.golemDartgun.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
            }

            if (entity.hasDecoration(GolemDecorationType.FEZ)) {
               this.accessoriesModel.golemHeadFez.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
            }

            if (entity.hasDecoration(GolemDecorationType.TOP_HAT)) {
               this.accessoriesModel.golemHeadHatRim.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
               this.accessoriesModel.golemHeadHat.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
            }

            if (entity.hasDecoration(GolemDecorationType.BOWTIE)) {
               this.accessoriesModel.golemBowtie.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
            }

            if (entity.hasDecoration(GolemDecorationType.ARMOR)) {
               this.accessoriesModel.golemPlate.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
               this.accessoriesModel.golemPlateLeft.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
               this.accessoriesModel.golemPlateRight.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
            }

            if (entity.hasDecoration(GolemDecorationType.GOGGLES)) {
               this.accessoriesModel.golemHeadGlasses.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
            }

            if (entity.hasDecoration(GolemDecorationType.VISOR)) {
               this.accessoriesModel.golemVisor.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
            }

            if (entity.hasDecoration(GolemDecorationType.MACE)) {
               this.accessoriesModel.golemMace.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
            }

            if (entity.advanced) {
               this.accessoriesModel.golemHeadBrain.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
               this.accessoriesModel.golemHeadJar.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
               if (entity.getCore() != GolemCore.BLANK) {
                  this.accessoriesModel.golemEvilHead.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
               }
            }
         }
      }
   }

   public static final class CarriedItemLayer extends RenderLayer<EntityGolemBase, ModelGolem> {
      public static final ResourceLocation BUCKET_OBJ = ResourceLocation.fromNamespaceAndPath("thaumcraft", "models/obj/bucket.obj");
      public static final ResourceLocation BUCKET_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/bucket.png");
      private static final float JAR_X = -0.5F;
      private static final float JAR_Y = 2.0F;
      private static final float JAR_Z = -0.78F;
      private static final float JAR_SCALE = 3.0F;
      public static final ModelResourceLocation BUCKET_MODEL = ModelResourceLocation.standalone(Thaumcraft.location("block/static/bucket"));

      public CarriedItemLayer(RenderGolemBase parent) {
         super(parent);
      }

      @OnlyIn(Dist.CLIENT)
      public void render(
         @NotNull PoseStack poseStack,
         @NotNull MultiBufferSource buffer,
         int packedLight,
         @NotNull EntityGolemBase entity,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (entity.deathTime == 0) {
            if (entity.getCore() == GolemCore.FISHING) {
               this.renderFishingRod(poseStack, buffer, packedLight, entity);
            }

            ItemStack carried = entity.getCarriedForDisplay();
            if (entity.getCore() == GolemCore.LIQUID) {
               this.renderBucketObj(poseStack, buffer, packedLight, entity, carried);
            } else if (!carried.isEmpty()) {
               if (carried.getItem() instanceof JarBlockItem) {
                  float s = 3.0F;
                  if (entity.getCore() == GolemCore.ESSENCE) {
                     s *= (float)(0.5 + Math.min(64, entity.getCarryLimit()) / 128.0);
                  }

                  poseStack.pushPose();
                  poseStack.translate(-0.5F, 2.0F, -0.78F);
                  poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                  poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                  poseStack.translate(-0.5F, -0.5F, -0.5F);
                  poseStack.scale(s, s, s);
                  Minecraft.getInstance()
                     .getItemRenderer()
                     .renderStatic(
                        entity,
                        carried,
                        ItemDisplayContext.NONE,
                        false,
                        poseStack,
                        buffer,
                        entity.level(),
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        entity.getId() + 991
                     );
                  poseStack.popPose();
               } else {
                  poseStack.pushPose();
                  poseStack.translate(0.0F, 2.0F, -1.0F);
                  poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                  poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                  poseStack.scale(2.0F, 2.0F, 2.0F);
                  Minecraft.getInstance()
                     .getItemRenderer()
                     .renderStatic(
                        entity,
                        carried,
                        ItemDisplayContext.FIXED,
                        false,
                        poseStack,
                        buffer,
                        entity.level(),
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        entity.getId() + 456
                     );
                  poseStack.popPose();
               }
            }
         }
      }

      @OnlyIn(Dist.CLIENT)
      private void renderFishingRod(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, @NotNull EntityGolemBase entity) {
         ModelGolem model = (ModelGolem)this.getParentModel();
         poseStack.pushPose();
         model.root().translateAndRotate(poseStack);
         model.golemRightArm.translateAndRotate(poseStack);
         poseStack.translate(-0.625F, 1.375F, 0.0F);
         poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
         poseStack.translate(0.0F, 0.06F, 0.02F);
         poseStack.scale(2.35F, 2.35F, 2.35F);
         Minecraft.getInstance()
            .getItemRenderer()
            .renderStatic(
               entity,
               new ItemStack(Items.FISHING_ROD),
               ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
               false,
               poseStack,
               buffer,
               entity.level(),
               packedLight,
               OverlayTexture.NO_OVERLAY,
               entity.getId() + 123
            );
         poseStack.popPose();
      }

      @OnlyIn(Dist.CLIENT)
      private void renderBucketObj(
         @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, @NotNull EntityGolemBase entity, @NotNull ItemStack carried
      ) {
         BakedModel model = Minecraft.getInstance().getModelManager().getModel(BUCKET_MODEL);
         if (model != Minecraft.getInstance().getModelManager().getMissingModel()) {
            poseStack.pushPose();
            poseStack.translate(0.0, 3.52, -1.0);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.cutout());
            Minecraft.getInstance()
               .getBlockRenderer()
               .getModelRenderer()
               .renderModel(poseStack.last(), vertexBuilder, null, model, 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
            FluidUtil.getFluidContained(carried).ifPresent(fluidStack -> {
               if (!fluidStack.isEmpty()) {
                  IClientFluidTypeExtensions fluidProps = IClientFluidTypeExtensions.of(fluidStack.getFluid());
                  ResourceLocation fluidTexture = fluidProps.getStillTexture(fluidStack);
                  int fluidColor = fluidProps.getTintColor(fluidStack);
                  TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidTexture);
                  int fluidLuminosity = fluidStack.getFluid().getFluidType().getLightLevel();
                  int fluidLight = fluidLuminosity > 0 ? 15728768 : packedLight;
                  poseStack.pushPose();
                  poseStack.translate(0.0F, 0.0F, 1.0F);
                  poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                  UtilsFX.renderFluidFace(buffer, poseStack, sprite, 0.75F, fluidColor, fluidLight);
                  poseStack.popPose();
               }
            });
            poseStack.popPose();
         }
      }
   }

   public static final class CoreAndUpgradesLayer extends RenderLayer<EntityGolemBase, ModelGolem> {
      public CoreAndUpgradesLayer(RenderGolemBase parent) {
         super(parent);
      }

      @OnlyIn(Dist.CLIENT)
      public void render(
         @NotNull PoseStack poseStack,
         @NotNull MultiBufferSource buffer,
         int packedLight,
         @NotNull EntityGolemBase entity,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         float inv = 2.5F;
         float coreX = -0.08F;
         float bothY = 0.05F;
         if (entity.getCore() != GolemCore.BLANK) {
            boolean plate = entity.hasDecoration(GolemDecorationType.ARMOR);
            poseStack.pushPose();
            poseStack.scale(inv, inv, inv);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.translate(0.0875F + coreX, -0.96F + bothY + 0.05F, 0.15F + (plate ? 0.03F : 0.0F));
            poseStack.scale(0.175F, 0.175F, 0.175F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            Item coreItem = ItemGolemCore.getItemByCore(entity.getCore());
            if (coreItem != null) {
               ItemStack coreStack = new ItemStack(coreItem);
               Minecraft.getInstance()
                  .getItemRenderer()
                  .renderStatic(
                     entity,
                     coreStack,
                     ItemDisplayContext.FIXED,
                     false,
                     poseStack,
                     buffer,
                     entity.level(),
                     packedLight,
                     OverlayTexture.NO_OVERLAY,
                     entity.getId()
                  );
            }

            poseStack.popPose();
         }

         int len = entity.getUpgradeSlotCount();
         float step = 0.13F;

         for (int i = 0; i < len; i++) {
            int upId = entity.getUpgrade(i);
            if (upId >= 0 && upId < GolemUpgrade.values().length) {
               Item upItem = ItemGolemUpgrade.getItemByUpgrade(GolemUpgrade.values()[upId]);
               if (upItem != null) {
                  poseStack.pushPose();
                  poseStack.scale(inv, inv, inv);
                  poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                  poseStack.translate(-0.01F - step * (len - 1) / 2.0F + step * i, -1.106F + bothY, 0.099F);
                  poseStack.scale(0.1F, 0.1F, 0.1F);
                  ItemStack upStack = new ItemStack(upItem);
                  Minecraft.getInstance()
                     .getItemRenderer()
                     .renderStatic(
                        entity,
                        upStack,
                        ItemDisplayContext.FIXED,
                        false,
                        poseStack,
                        buffer,
                        entity.level(),
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        entity.getId() + 31 + i
                     );
                  poseStack.popPose();
               }
            }
         }
      }
   }

   public static final class DamageLayer extends RenderLayer<EntityGolemBase, ModelGolem> {
      protected DamageLayer(RenderGolemBase parent) {
         super(parent);
      }

      @OnlyIn(Dist.CLIENT)
      public void render(
         @NotNull PoseStack poseStack,
         @NotNull MultiBufferSource buffer,
         int packedLight,
         @NotNull EntityGolemBase entity,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         float hp = entity.getHealthPercentage();
         if (!(hp >= 1.0F)) {
            VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(RenderGolemBase.TEX_DAMAGE));
            ModelGolem model = (ModelGolem)this.getParentModel();
            model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            int alpha = Mth.clamp((int)((1.0F - hp) * 255.0F), 0, 255);
            model.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, alpha << 24 | 16777215);
         }
      }
   }

   public static final class GolemHookLayer extends RenderLayer<EntityGolemBase, ModelGolem> {
      protected GolemHookLayer(RenderGolemBase parent) {
         super(parent);
      }

      @OnlyIn(Dist.CLIENT)
      public void render(
         @NotNull PoseStack poseStack,
         @NotNull MultiBufferSource buffer,
         int packedLight,
         @NotNull EntityGolemBase entity,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (GolemHooks.hasRenderHandlers()) {
            GolemHooks.hook_RenderGolem(entity, entity.hookHandlerData, poseStack, buffer, packedLight, partialTick);
         }
      }
   }
}
