package thaumcraft.item.wands.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.item.wands.ItemWandCap;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.ItemWandRod;

@OnlyIn(Dist.CLIENT)
public class WandRender extends BlockEntityWithoutLevelRenderer {
   public static final WandRender INSTANCE = new WandRender();
   public static int renderIndex;
   private final Minecraft minecraft = Minecraft.getInstance();
   private final Map<String, BakedModel> rods = new HashMap<>();
   private final Map<String, BakedModel> caps = new HashMap<>();
   private final Map<String, BakedModel> focus = new HashMap<>();
   private final Byte2ObjectMap<BakedModel> runes = new Byte2ObjectArrayMap();
   private boolean loaded = false;

   @OnlyIn(Dist.CLIENT)
   private WandRender() {
      super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
   }

   public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
      this.loaded = false;
      this.rods.clear();
      this.caps.clear();
      this.focus.clear();
      this.runes.clear();
   }

   @OnlyIn(Dist.CLIENT)
   public void renderByItem(
      @NotNull ItemStack stack,
      @NotNull ItemDisplayContext context,
      @NotNull PoseStack poseStack,
      @NotNull MultiBufferSource bufferSource,
      int combinedLight,
      int combinedOverlay
   ) {
      if (!this.loaded) {
         this.loaded = true;
         ModelManager mm = this.minecraft.getModelManager();
         ItemWandCap.TYPES.values().forEach(cap -> this.caps.put(cap.getTag(), mm.getModel(ModelResourceLocation.standalone(cap.getModel()))));
         ItemWandRod.TYPES.values().forEach(rod -> {
            if (!rod.isStaff()) {
               this.rods.put(rod.getTag(), mm.getModel(ModelResourceLocation.standalone(rod.getModel())));
            }
         });
         ItemFocusBasic.TYPES.forEach((s, f) -> this.focus.put(s, mm.getModel(ModelResourceLocation.standalone(f.getModelLocation()))));

         for (byte i = 0; i < 16; i++) {
            this.runes.put(i, mm.getModel(ModelResourceLocation.standalone(Thaumcraft.location("item/wand/runes/" + i))));
         }
      }

      if (stack.getItem() instanceof ItemWandCasting wandItem) {
         ItemWandRod var28 = wandItem.getRod(stack);
         ItemWandCap wandCap = wandItem.getCap(stack);
         if (var28 != null && wandCap != null) {
            ItemFocusBasic focusObj = wandItem.getFocus(stack);
            boolean staff = wandItem.isStaff(stack);
            boolean sceptre = wandItem.isSceptre(stack);
            boolean hasRunes = wandItem.hasRunes(stack);
            poseStack.pushPose();
            switch (context) {
               case GROUND:
                  poseStack.translate(0.5F, 0.3F, 0.5F);
                  poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                  poseStack.scale(0.5F, 0.5F, 0.5F);
                  if (staff) {
                     poseStack.translate(0.0F, 0.5F, 0.0F);
                     poseStack.scale(0.9F, 0.9F, 0.9F);
                  }
                  break;
               case GUI:
                  poseStack.scale(0.8F, 0.8F, 0.8F);
                  poseStack.translate(1.0F, 0.25F, 1.0F);
                  poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
                  if (staff) {
                     poseStack.scale(0.62F, 0.62F, 0.62F);
                     poseStack.translate(0.2F, 0.0F, 0.0F);
                  }
                  break;
               case FIRST_PERSON_RIGHT_HAND:
               case FIRST_PERSON_LEFT_HAND:
                  poseStack.scale(0.6F, 0.6F, 0.6F);
                  poseStack.translate(1.2F, 0.4F, 0.4F);
                  poseStack.mulPose(Axis.YN.rotationDegrees(45.0F));
                  break;
               case THIRD_PERSON_RIGHT_HAND:
               case THIRD_PERSON_LEFT_HAND:
                  poseStack.scale(0.5F, 0.5F, 0.5F);
                  poseStack.translate(1.0F, 0.8F, 1.2F);
                  break;
               case FIXED:
                  poseStack.scale(0.75F, 0.75F, 0.75F);
                  poseStack.mulPose(Axis.YN.rotationDegrees(180.0F));
                  poseStack.translate(-0.7F, 0.2F, -0.65F);
            }

            boolean thirdPerson = context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            if (staff) {
               if (thirdPerson) {
                  poseStack.translate(0.0F, -0.28F, 0.0F);
               } else {
                  poseStack.translate(0.0F, 0.14F, 0.0F);
               }
            } else if (sceptre) {
               poseStack.translate(0.0F, -0.06F, 0.0F);
            }

            ItemRenderer renderer = this.minecraft.getItemRenderer();
            VertexConsumer buffer = ItemRenderer.getFoilBufferDirect(bufferSource, RenderType.cutout(), true, stack.hasFoil());
            poseStack.pushPose();
            if (staff) {
               poseStack.translate(0.0F, -0.24F, 0.0F);
               poseStack.scale(1.2F, 2.0F, 1.2F);
            }

            String rodKey = staff ? var28.getTag().replace("_staff", "") : var28.getTag();
            BakedModel modelRod = this.rods.get(rodKey);
            if (modelRod != null) {
               renderer.renderModelLists(modelRod, stack, combinedLight, combinedOverlay, poseStack, buffer);
            } else {
               System.out.println("Missing rod model for key: " + rodKey);
            }

            poseStack.popPose();
            BakedModel modelCap = this.caps.get(wandCap.getTag());
            if (modelCap != null) {
               if (staff) {
                  this.renderCap(renderer, modelCap, stack, combinedLight, combinedOverlay, poseStack, buffer, 1.9F, false, 1.2F, 1.12F, 1.23F);
                  this.renderCap(renderer, modelCap, stack, combinedLight, combinedOverlay, poseStack, buffer, -0.4F, true, 1.2F, 1.12F, 1.23F);
                  this.renderCap(renderer, modelCap, stack, combinedLight, combinedOverlay, poseStack, buffer, 1.55F, false, 1.1F, 1.1F, 1.1F);
               } else if (sceptre) {
                  this.renderCap(renderer, modelCap, stack, combinedLight, combinedOverlay, poseStack, buffer, 1.1F, false, 1.1F, 1.1F, 1.1F);
                  this.renderCap(renderer, modelCap, stack, combinedLight, combinedOverlay, poseStack, buffer, 0.0F, true, 1.1F, 1.1F, 1.1F);
                  this.renderCap(renderer, modelCap, stack, combinedLight, combinedOverlay, poseStack, buffer, 0.825F, false, 1.0F, 0.66F, 1.0F);
               } else {
                  this.renderCap(renderer, modelCap, stack, combinedLight, combinedOverlay, poseStack, buffer, 1.05F, false, 1.0F, 1.0F, 1.0F);
                  this.renderCap(renderer, modelCap, stack, combinedLight, combinedOverlay, poseStack, buffer, -0.05F, true, 1.0F, 1.0F, 1.0F);
               }
            }

            if (focusObj != null) {
               BakedModel modelFocus = this.focus.get(focusObj.getType());
               if (modelFocus != null) {
                  poseStack.pushPose();
                  if (staff) {
                     poseStack.translate(0.0F, -0.0475F, 0.0F);
                     poseStack.scale(1.025F, 1.0525F, 1.025F);
                  }

                  renderer.renderModelLists(modelFocus, stack, combinedLight, combinedOverlay, poseStack, buffer);
                  poseStack.popPose();
               }
            }

            if (sceptre && this.minecraft.player != null) {
               poseStack.pushPose();
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(770, 1);
               poseStack.translate(0.0F, 0.93F, 0.0F);
               VertexConsumer runeBuffer = bufferSource.getBuffer(RenderType.translucent());

               for (int i = 0; i < 10; i++) {
                  poseStack.pushPose();
                  poseStack.mulPose(Axis.YP.rotationDegrees(36 * i + this.minecraft.player.tickCount));
                  this.drawRune(poseStack, stack, combinedLight, combinedOverlay, runeBuffer, 0.16F, -0.01F, -0.125F, i, this.minecraft.player);
                  poseStack.popPose();
               }

               RenderSystem.blendFunc(770, 771);
               RenderSystem.disableBlend();
               poseStack.popPose();
            }

            if (hasRunes && this.minecraft.player != null) {
               poseStack.pushPose();
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(770, 1);
               VertexConsumer runeBuffer = bufferSource.getBuffer(RenderType.translucent());
               float runeStart = staff ? 0.48F : 0.36F;
               float runeStep = staff ? 0.18F : 0.14F;
               float runeZ = staff ? -0.1F : -0.08F;

               for (int j = 0; j < 4; j++) {
                  poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

                  for (int k = 0; k < 14; k++) {
                     this.drawRune(
                        poseStack,
                        stack,
                        combinedLight,
                        combinedOverlay,
                        runeBuffer,
                        runeStart + k * runeStep,
                        -0.01F,
                        runeZ,
                        (k + j * 3) % 16,
                        this.minecraft.player
                     );
                  }
               }

               RenderSystem.blendFunc(770, 771);
               RenderSystem.disableBlend();
               poseStack.popPose();
            }

            poseStack.popPose();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderCap(
      ItemRenderer renderer,
      BakedModel modelCap,
      ItemStack stack,
      int combinedLight,
      int combinedOverlay,
      PoseStack poseStack,
      VertexConsumer buffer,
      float yOffset,
      boolean flipX,
      float sx,
      float sy,
      float sz
   ) {
      poseStack.pushPose();
      poseStack.translate(0.0F, yOffset, 0.0F);
      if (flipX) {
         poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
      }

      if (sx != 1.0F || sy != 1.0F || sz != 1.0F) {
         poseStack.scale(sx, sy, sz);
      }

      renderer.renderModelLists(modelCap, stack, combinedLight, combinedOverlay, poseStack, buffer);
      poseStack.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   public boolean applyForgeHandTransform(
      PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess
   ) {
      int side = arm == HumanoidArm.RIGHT ? 1 : -1;
      InteractionHand usedHand = player.getUsedItemHand();
      boolean renderMain = arm == player.getMainArm();
      boolean correctHand = usedHand == InteractionHand.MAIN_HAND == renderMain;
      poseStack.translate(side * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F);
      boolean using = player.isUsingItem() && correctHand && player.getUseItem().getItem() == itemInHand.getItem();
      if (itemInHand.getItem() instanceof ItemWandCasting wandItem) {
         boolean var23 = wandItem.isStaff(itemInHand);
         boolean sceptre = wandItem.isSceptre(itemInHand);
         float gripY;
         if (var23) {
            gripY = -0.38F;
         } else if (sceptre) {
            gripY = -0.14F;
         } else {
            gripY = -0.22F;
         }

         poseStack.translate(0.0F, gripY, 0.0F);
         if (!using) {
            this.applyItemArmAttackTransform(poseStack, arm, swingProcess);
            return true;
         } else {
            ItemStack focusItem = wandItem.getFocusItem(itemInHand);
            ItemFocusBasic focus = wandItem.getFocus(itemInHand);
            float useTicks = itemInHand.getUseDuration(player) - player.getUseItemRemainingTicks() + partialTick;
            float n = Math.min(useTicks, 3.0F);
            poseStack.translate(0.0F, 0.25F, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F * side));
            poseStack.mulPose(Axis.XP.rotationDegrees(10.0F));
            poseStack.mulPose(Axis.XN.rotationDegrees(60.0F * (n / 3.0F)));
            ItemFocusBasic.WandFocusAnimation anim = wandItem.animation;
            if (anim == null && focus != null) {
               anim = focus.getAnimation(focusItem);
            }

            if (anim == ItemFocusBasic.WandFocusAnimation.WAVE) {
               poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(useTicks / 10.0F) * 10.0F * side));
               poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(useTicks / 15.0F) * 10.0F));
            } else if (anim == ItemFocusBasic.WandFocusAnimation.CHARGE) {
               float shake = Mth.sin(useTicks / 0.8F) * 1.0F;
               poseStack.mulPose(Axis.ZP.rotationDegrees(shake * side));
               poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(useTicks / 0.7F) * 1.0F));
            }

            poseStack.translate(0.0F, -0.25F, 0.0F);
            return true;
         }
      } else {
         if (!using) {
            this.applyItemArmAttackTransform(poseStack, arm, swingProcess);
         }

         return true;
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float swingProcess) {
      int i = arm == HumanoidArm.RIGHT ? 1 : -1;
      float f = Mth.sin(swingProcess * swingProcess * (float) Math.PI);
      poseStack.mulPose(Axis.YP.rotationDegrees(i * (45.0F + f * -20.0F)));
      float f1 = Mth.sin(Mth.sqrt(swingProcess) * (float) Math.PI);
      poseStack.mulPose(Axis.ZP.rotationDegrees(i * f1 * -20.0F));
      poseStack.mulPose(Axis.XP.rotationDegrees(f1 * -80.0F));
      poseStack.mulPose(Axis.YP.rotationDegrees(i * -45.0F));
   }

   @OnlyIn(Dist.CLIENT)
   private void drawRune(
      PoseStack poseStack, ItemStack stack, int combinedLight, int combinedOverlay, VertexConsumer buffer, float x, float y, float z, int idx, Player player
   ) {
      renderIndex = idx;
      poseStack.pushPose();
      float bob = Mth.sin((player.tickCount + idx * 5) / 10.0F) * 0.2F;
      poseStack.translate(x, y, z);
      poseStack.mulPose(Axis.YN.rotationDegrees(60.0F));
      poseStack.scale(0.8F + bob / 40.0F, 0.8F + bob / 40.0F, 1.0F);
      BakedModel runeModel = (BakedModel)this.runes.get((byte)idx);
      if (runeModel != null) {
         this.minecraft.getItemRenderer().renderModelLists(runeModel, stack, combinedLight, combinedOverlay, poseStack, buffer);
      }

      poseStack.popPose();
   }
}
