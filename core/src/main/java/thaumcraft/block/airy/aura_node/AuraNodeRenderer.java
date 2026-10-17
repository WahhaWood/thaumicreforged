package thaumcraft.block.airy.aura_node;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.IRevealer;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.block.jar.node.NodeJarBlockEntity;
import thaumcraft.item.relics.ItemThaumometer;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class AuraNodeRenderer<T extends BlockEntity & INode> implements BlockEntityRenderer<T> {
   public static final ResourceLocation nodetex = Thaumcraft.location("textures/misc/nodes.png");
   private static final int VISUAL_ASPECT_CAP = 256;

   public AuraNodeRenderer(Context context) {
   }

   public void render(@NotNull T node, float pt, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int brightnessOverride, int overlayTexture) {
      double viewDistance = 64.0;
      Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
      if (cameraEntity != null) {
         boolean viewNode = false;
         boolean disableDepth = false;
         if (cameraEntity instanceof Player player) {
            if (node instanceof NodeJarBlockEntity) {
               viewNode = true;
            } else {
               Inventory inventory = player.getInventory();
               ItemStack helmStack = inventory.getArmor(3);
               if (helmStack.getItem() instanceof IRevealer revealer && revealer.showNodes(helmStack, player)) {
                  viewNode = true;
                  disableDepth = true;
               } else if (inventory.getSelected().getItem() instanceof ItemThaumometer && UtilsFX.isVisibleTo(0.44F, cameraEntity, node.getBlockPos())) {
                  viewNode = true;
                  disableDepth = true;
                  viewDistance = 48.0;
               }
            }
         }

         renderNode(
            cameraEntity,
            pose,
            bufferSource,
            viewDistance,
            viewNode,
            disableDepth,
            pt,
            node.getBlockPos(),
            node.getAspects(),
            node.getNodeType(),
            node.getNodeModifier()
         );
         if (node instanceof AuraNodeBlockEntity aura && aura.drainEntity != null && aura.drainCollision != null) {
            Entity drain = aura.drainEntity;
            if (drain instanceof Player p && !p.isUsingItem()) {
               aura.drainEntity = null;
               aura.drainCollision = null;
               return;
            }

            if (!(aura.drainCollision instanceof BlockHitResult hit)) {
               aura.drainEntity = null;
               aura.drainCollision = null;
               return;
            }

            int var46 = 0;
            float var47 = 1.0F;
            if (drain instanceof LivingEntity le && le.isUsingItem()) {
               var46 = le.getUseItem().getUseDuration(le) - le.getUseItemRemainingTicks();
               boolean isMainArmRight = le.getMainArm() == HumanoidArm.RIGHT;
               if (le.getUsedItemHand() == InteractionHand.OFF_HAND) {
                  isMainArmRight = !isMainArmRight;
               }

               var47 = isMainArmRight ? -5.5F : 3.0F;
            }

            float n7 = 0.0F;
            if (drain instanceof Player) {
               n7 = Mth.sin(var46 / 10.0F) * 10.0F;
            }

            float yaw = Mth.lerp(pt, drain.yRotO, drain.getYRot());
            float pitch = Mth.lerp(pt, drain.xRotO, drain.getXRot());
            Vec3 v = new Vec3(0.25 * var47, -0.1, 0.5);
            v = v.xRot(-pitch * (float) (Math.PI / 180.0));
            v = v.yRot(-yaw * (float) (Math.PI / 180.0));
            v = v.yRot(-n7 * 0.01F);
            v = v.xRot(-n7 * 0.015F);
            double ex = Mth.lerp(pt, drain.xo, drain.getX());
            double ey = Mth.lerp(pt, drain.yo, drain.getY());
            double ez = Mth.lerp(pt, drain.zo, drain.getZ());
            double startX = ex + v.x;
            double startY = ey + v.y + (drain == Minecraft.getInstance().player ? 0.0 : drain.getEyeHeight());
            double startZ = ez + v.z;
            BlockPos bp = hit.getBlockPos();
            double endX = bp.getX() + 0.5;
            double endY = bp.getY() + 0.5;
            double endZ = bp.getZ() + 0.5;
            BlockPos origin = aura.getBlockPos();
            pose.pushPose();
            UtilsFX.drawFloatyLine(
               pose,
               bufferSource,
               startX - origin.getX(),
               startY - origin.getY(),
               startZ - origin.getZ(),
               endX - origin.getX(),
               endY - origin.getY(),
               endZ - origin.getZ(),
               pt,
               aura.color.getRGB(),
               ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/wispy.png"),
               -0.02F,
               Math.min(var46, 10) / 10.0F
            );
            pose.popPose();
         }
      }
   }

   public static void renderNode(
      Entity entity,
      PoseStack pose,
      MultiBufferSource source,
      double viewDistance,
      boolean viewNode,
      boolean disableDepth,
      float partialTicks,
      BlockPos blockPos,
      AspectList list,
      NodeType type,
      @Nullable NodeModifier modifier
   ) {
      int x = blockPos.getX();
      int y = blockPos.getY();
      int z = blockPos.getZ();
      long nanoTime = System.nanoTime();
      int tx = (int)((nanoTime / 40000000L + x) % 32L);
      int ty = 0;
      if (tx >= 16) {
         tx -= 16;
         ty += 8;
      }

      if (list.size() > 0 && viewNode) {
         double distance = entity.position().distanceTo(blockPos.getCenter());
         if (distance > viewDistance) {
            return;
         }
      }

      pose.pushPose();
      if (list.size() > 0 && viewNode) {
         double distance = entity.position().distanceTo(blockPos.getCenter());
         float alpha = (float)((viewDistance - distance) / viewDistance);
         alpha = Mth.clamp(alpha, 0.0F, 1.0F);
         if (modifier != null) {
            alpha *= switch (modifier) {
               case BRIGHT -> 1.5F;
               case PALE -> 0.66F;
               case FADING -> Mth.sin(entity.tickCount / 3.0F) * 0.25F + 0.33F;
            };
         }

         alpha = Mth.clamp(alpha, 0.0F, 1.0F);
         int index = 0;
         float offY = 0.0F;
         float totalAmount = 0.0F;

         for (Aspect aspect : list.getAspects()) {
            int visualAmount = Mth.clamp(list.getAmount(aspect), 0, 256);
            totalAmount += visualAmount;
            pose.pushPose();
            float scale = 0.2F + (Mth.sin(entity.tickCount / (14.0F - index)) * 0.25F + 0.5F) * (visualAmount / 50.0F);
            offY = (float)(nanoTime / 5000000L) % (float)(5000L + 500L * index) / (5000.0F + 500.0F * index) * (float) (Math.PI * 2);
            float aspectAlpha = alpha;
            if (aspect.getBlend() == 771) {
               aspectAlpha = alpha * 1.5F;
            }

            aspectAlpha = Mth.clamp(aspectAlpha / Math.max(1.0F, list.size() / 2.0F), 0.0F, 1.0F);
            UtilsFX.renderFacingStrip(
               source,
               pose,
               nodetex,
               x + 0.5,
               y + 0.5,
               z + 0.5,
               offY,
               scale,
               aspectAlpha,
               16,
               ty,
               tx,
               partialTicks,
               aspect.getColor(),
               aspect.getBlend() == 771,
               disableDepth
            );
            pose.popPose();
            index++;
         }

         float avg = totalAmount / list.size();
         float scale = 0.1F + avg / 150.0F;
         ty++;
         switch (type) {
            case UNSTABLE:
               ty += 5;
               offY = 0.0F;
               break;
            case DARK:
               ty++;
               break;
            case TAINTED:
               ty += 4;
               break;
            case PURE:
               ty += 3;
               break;
            case HUNGRY:
               scale *= 0.75F;
               ty += 2;
         }

         UtilsFX.renderFacingStrip(
            source,
            pose,
            nodetex,
            x + 0.5,
            y + 0.5,
            z + 0.5,
            offY,
            scale,
            alpha,
            16,
            ty,
            tx,
            partialTicks,
            // TC4 set glColor4f(1, 0, 1) here, but the tessellator's own colour (white) overrides it per vertex.
            16777215,
            type == NodeType.DARK || type == NodeType.TAINTED,
            disableDepth
         );
      } else {
         UtilsFX.renderFacingStrip(
            source, pose, nodetex, x + 0.5, y + 0.5, z + 0.5, 0.0F, 0.5F, 0.1F, 16, ++ty, tx, partialTicks, 16777215, false, disableDepth
         );
      }

      pose.popPose();
   }
}
