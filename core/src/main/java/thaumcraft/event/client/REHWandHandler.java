package thaumcraft.event.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderHighlightEvent.Block;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.api.IArchitect;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.event.common.KeyHandler;
import thaumcraft.item.wands.ItemFocusPouch;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.misc.PacketFocusChangeToServer;
import thaumcraft.util.UtilsFX;

public class REHWandHandler {
   static float radialHudScale = 0.0F;
   TreeMap<String, Integer> foci = new TreeMap<>();
   HashMap<String, ItemStack> fociItem = new HashMap<>();
   HashMap<String, Boolean> fociHover = new HashMap<>();
   HashMap<String, Float> fociScale = new HashMap<>();
   long lastTime = 0L;
   boolean lastState = false;
   int lastArcHash = 0;
   ArrayList<BlockPos> architectBlocks = new ArrayList<>();
   String tex = "textures/misc/architect_arrows.png";
   public static final ResourceLocation ARCHITECT_AXIS_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/architect_arrows.png");
   public static final ResourceLocation ARCHITECT_OVERLAY_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/warded_glass_1.png");

   @OnlyIn(Dist.CLIENT)
   public void handleFociRadial(Minecraft minecraft, GuiGraphics graphics, Window window, long n) {
      if (KeyHandler.radialActive || radialHudScale > 0.0F) {
         if (!KeyHandler.radialActive) {
            if (minecraft.screen == null && this.lastState) {
               if (!minecraft.mouseHandler.isMouseGrabbed()) {
                  minecraft.mouseHandler.grabMouse();
               }

               this.lastState = false;
            }
         } else {
            if (minecraft.screen != null) {
               KeyHandler.radialActive = false;
               KeyHandler.radialLock = true;
               minecraft.mouseHandler.grabMouse();
               minecraft.mouseHandler.releaseMouse();
               return;
            }

            if (radialHudScale == 0.0F) {
               Player player = minecraft.player;
               this.foci.clear();
               this.fociItem.clear();
               this.fociHover.clear();
               this.fociScale.clear();
               int n3 = 0;

               for (int k = 0; k < 36; k++) {
                  ItemStack itemStack2 = player.getInventory().getItem(k);
                  if (itemStack2.getItem() instanceof ItemFocusBasic focusBasic) {
                     this.foci.put(focusBasic.getSortingHelper(itemStack2), k);
                     this.fociItem.put(focusBasic.getSortingHelper(itemStack2), itemStack2.copy());
                     this.fociScale.put(focusBasic.getSortingHelper(itemStack2), 1.0F);
                     this.fociHover.put(focusBasic.getSortingHelper(itemStack2), false);
                  }

                  if (itemStack2.getItem() instanceof ItemFocusPouch focusPouch) {
                     n3++;
                     NonNullList<ItemStack> inventory2 = focusPouch.getInventory(itemStack2);

                     for (int l = 0; l < inventory2.size(); l++) {
                        ItemStack itemStack3 = (ItemStack)inventory2.get(l);
                        if (itemStack3.getItem() instanceof ItemFocusBasic focusBasic) {
                           this.foci.put(focusBasic.getSortingHelper(itemStack3), l + n3 * 1000);
                           this.fociItem.put(focusBasic.getSortingHelper(itemStack3), itemStack3.copy());
                           this.fociScale.put(focusBasic.getSortingHelper(itemStack3), 1.0F);
                           this.fociHover.put(focusBasic.getSortingHelper(itemStack3), false);
                        }
                     }
                  }
               }

               if (this.foci.size() > 0 && minecraft.mouseHandler.isMouseGrabbed()) {
                  minecraft.mouseHandler.releaseMouse();
               }
            }
         }

         this.renderFocusRadialHUD(
            minecraft, graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(), n, minecraft.getTimer().getGameTimeDeltaPartialTick(false)
         );
         if (n > this.lastTime) {
            for (String s : this.fociHover.keySet()) {
               float scale = this.fociScale.get(s);
               if (this.fociHover.get(s)) {
                  if (!KeyHandler.radialActive && !KeyHandler.radialLock) {
                     PacketHandler.sendToServer(new PacketFocusChangeToServer(s));
                     KeyHandler.radialLock = true;
                  }

                  if (scale < 1.3F) {
                     this.fociScale.put(s, scale + 0.025F);
                  }
               } else if (scale > 1.0F) {
                  this.fociScale.put(s, scale - 0.025F);
               }
            }

            if (!KeyHandler.radialActive) {
               radialHudScale -= 0.05F;
            } else if (radialHudScale < 1.0F) {
               radialHudScale += 0.05F;
            }

            if (radialHudScale > 1.0F) {
               radialHudScale = 1.0F;
            }

            if (radialHudScale < 0.0F) {
               radialHudScale = 0.0F;
               KeyHandler.radialLock = false;
            }

            this.lastTime = n + 5L;
            this.lastState = KeyHandler.radialActive;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderFocusRadialHUD(Minecraft minecraft, GuiGraphics graphics, double n, double n2, long n3, float n4) {
      InteractionHand hand = InteractionHand.MAIN_HAND;
      ItemStack currentItem = minecraft.player.getItemInHand(hand);
      if (!(currentItem.getItem() instanceof ItemWandCasting)) {
         currentItem = minecraft.player.getItemInHand(hand);
         if (!(currentItem.getItem() instanceof ItemWandCasting)) {
            return;
         }
      }

      ItemWandCasting itemWand = (ItemWandCasting)currentItem.getItem();
      ItemFocusBasic focus = itemWand.getFocus(currentItem);
      Window window = minecraft.getWindow();
      double mx = minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
      double my = minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
      if (this.fociItem.size() != 0) {
         PoseStack pose = graphics.pose();
         pose.pushPose();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         pose.pushPose();
         pose.translate(n / 2.0, n2 / 2.0, 0.0);
         ItemStack focusItem = null;
         float n7 = 16.0F + this.fociItem.size() * 2.5F;
         UtilsFX.bindTexture("textures/misc/radial.png");
         pose.pushPose();
         float rotation = n4 + minecraft.player.tickCount % 720 / 2.0F;
         pose.mulPose(Axis.ZP.rotationDegrees(rotation));
         RenderSystem.setShaderGlintAlpha(0.003921569F);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
         UtilsFX.renderQuadCenteredFromTexture(pose, n7 * 2.75F * radialHudScale, 0.5F, 0.5F, 0.5F, 200, 771, 0.5F);
         RenderSystem.disableBlend();
         RenderSystem.setShaderGlintAlpha(0.1F);
         pose.popPose();
         UtilsFX.bindTexture("textures/misc/radial2.png");
         pose.pushPose();
         pose.mulPose(Axis.ZN.rotationDegrees(rotation));
         RenderSystem.setShaderGlintAlpha(0.003921569F);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
         UtilsFX.renderQuadCenteredFromTexture(pose, n7 * 2.55F * radialHudScale, 0.5F, 0.5F, 0.5F, 200, 771, 0.5F);
         RenderSystem.disableBlend();
         RenderSystem.setShaderGlintAlpha(0.1F);
         pose.popPose();
         if (focus != null) {
            pose.pushPose();
            ItemStack focusStack = itemWand.getFocusItem(currentItem);
            ItemStack copy = focusStack.copy();
            graphics.renderItem(copy, -8, -8);
            pose.popPose();
            int n8 = (int)(mx - n / 2.0);
            int n9 = (int)(my - n2 / 2.0);
            if (n8 >= -10 && n8 <= 10 && n9 >= -10 && n9 <= 10) {
               focusItem = focusStack;
            }
         }

         pose.scale(radialHudScale, radialHudScale, radialHudScale);
         float n10 = -90.0F * radialHudScale;
         float n11 = 360.0F / this.fociItem.size();
         String s = this.foci.firstKey();

         for (int i = 0; i < this.fociItem.size(); i++) {
            double n12 = Mth.cos(n10 / 180.0F * (float) Math.PI) * n7;
            double n13 = Mth.sin(n10 / 180.0F * (float) Math.PI) * n7;
            n10 += n11;
            pose.pushPose();
            pose.translate(n12, n13, 100.0);
            pose.scale(this.fociScale.get(s), this.fociScale.get(s), this.fociScale.get(s));
            ItemStack copy = this.fociItem.get(s).copy();
            graphics.renderItem(copy, -8, -8);
            pose.popPose();
            if (!KeyHandler.radialLock && KeyHandler.radialActive) {
               int n14 = (int)(mx - n / 2.0 - n12);
               int n15 = (int)(my - n2 / 2.0 - n13);
               if (n14 >= -10 && n14 <= 10 && n15 >= -10 && n15 <= 10) {
                  this.fociHover.put(s, true);
                  focusItem = this.fociItem.get(s);
                  if (minecraft.mouseHandler.isLeftPressed()) {
                     KeyHandler.radialLock = true;
                     PacketHandler.sendToServer(new PacketFocusChangeToServer(s));
                     break;
                  }
               } else {
                  this.fociHover.put(s, false);
               }
            }

            s = this.foci.higherKey(s);
         }

         pose.popPose();
         if (focusItem != null) {
            graphics.renderTooltip(
               minecraft.font,
               focusItem.getTooltipLines(
                  TooltipContext.of(minecraft.level), minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL
               ),
               Optional.empty(),
               -4,
               20
            );
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         pose.popPose();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public boolean handleArchitectOverlay(ItemStack stack, Block event, int ticksExisted, BlockHitResult hit) {
      if (!(stack.getItem() instanceof IArchitect architect)) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer player = mc.player;
         ClientLevel level = mc.level;
         if (player == null || level == null) {
            return false;
         } else if (hit == null) {
            return false;
         } else {
            BlockPos origin = hit.getBlockPos();
            Direction side = hit.getDirection();
            int hash = ("" + origin.getX() + origin.getY() + origin.getZ() + side.ordinal() + ticksExisted / 5).hashCode();
            if (hash != this.lastArcHash) {
               this.lastArcHash = hash;
               ArrayList<BlockPos> list = architect.getArchitectBlocks(stack, level, origin, side, player);
               this.architectBlocks = list == null ? new ArrayList<>() : new ArrayList<>(list);
            }

            if (this.architectBlocks != null && !this.architectBlocks.isEmpty()) {
               PoseStack pose = event.getPoseStack();
               MultiBufferSource buffer = event.getMultiBufferSource();
               Camera camera = event.getCamera();
               float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
               Vec3 camPos = camera.getPosition();
               RenderSystem.depthMask(false);
               RenderSystem.disableDepthTest();
               RenderSystem.disableCull();
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
               boolean ax = architect.showAxis(stack, level, player, side, IArchitect.EnumAxis.X);
               boolean ay = architect.showAxis(stack, level, player, side, IArchitect.EnumAxis.Y);
               boolean az = architect.showAxis(stack, level, player, side, IArchitect.EnumAxis.Z);
               this.drawArchitectAxis(pose, buffer, camPos, origin, partialTicks, player, ax, ay, az);

               for (BlockPos p : this.architectBlocks) {
                  this.drawOverlayBlock(pose, buffer, camPos, p, ticksExisted);
               }

               RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
               RenderSystem.disableBlend();
               RenderSystem.enableCull();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(true);
               return true;
            } else {
               return false;
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void drawOverlayBlock(PoseStack pose, MultiBufferSource buffer, Vec3 camPos, BlockPos pos, int ticksExisted) {
      float r = Mth.sin(ticksExisted / 2.0F + pos.getX()) * 0.2F + 0.3F;
      float g = Mth.sin(ticksExisted / 3.0F + pos.getY()) * 0.2F + 0.3F;
      float b = Mth.sin(ticksExisted / 4.0F + pos.getZ()) * 0.2F + 0.8F;
      float a = 0.2F;
      pose.pushPose();
      pose.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
      VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(ARCHITECT_OVERLAY_TEX));
      Matrix4f mat = pose.last().pose();
      Matrix3f nmat = pose.last().normal();
      boolean d = this.shouldSideBeRendered(pos, Direction.DOWN);
      boolean u = this.shouldSideBeRendered(pos, Direction.UP);
      boolean n = this.shouldSideBeRendered(pos, Direction.NORTH);
      boolean s = this.shouldSideBeRendered(pos, Direction.SOUTH);
      boolean w = this.shouldSideBeRendered(pos, Direction.WEST);
      boolean e = this.shouldSideBeRendered(pos, Direction.EAST);
      renderCube(mat, nmat, vc, r, g, b, a, 15728880, d, u, n, s, w, e);
      pose.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   private void drawArchitectAxis(
      PoseStack pose, MultiBufferSource buffer, Vec3 camPos, BlockPos origin, float partialTicks, Player player, boolean x, boolean y, boolean z
   ) {
      if (x || y || z) {
         float rr = Mth.sin(player.tickCount / 4.0F + origin.getX()) * 0.2F + 0.3F;
         float gg = Mth.sin(player.tickCount / 3.0F + origin.getY()) * 0.2F + 0.3F;
         float bb = Mth.sin(player.tickCount / 2.0F + origin.getZ()) * 0.2F + 0.8F;
         pose.pushPose();
         pose.translate(origin.getX() + 0.5 - camPos.x, origin.getY() + 0.5 - camPos.y, origin.getZ() + 0.5 - camPos.z);
         VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(ARCHITECT_AXIS_TEX));
         Matrix4f mat = pose.last().pose();
         Matrix3f nmat = pose.last().normal();
         float alpha = 0.33F;
         pose.mulPose(Axis.XP.rotationDegrees(90.0F));
         if (x) {
            pose.pushPose();
            renderQuadCentered(mat, nmat, vc, 1.0F, rr, gg, bb, alpha, 15728880);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            mat = pose.last().pose();
            nmat = pose.last().normal();
            renderQuadCentered(mat, nmat, vc, 1.0F, rr, gg, bb, alpha, 15728880);
            pose.popPose();
         }

         if (z) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
            mat = pose.last().pose();
            nmat = pose.last().normal();
            renderQuadCentered(mat, nmat, vc, 1.0F, rr, gg, bb, alpha, 15728880);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            mat = pose.last().pose();
            nmat = pose.last().normal();
            renderQuadCentered(mat, nmat, vc, 1.0F, rr, gg, bb, alpha, 15728880);
            pose.popPose();
         }

         if (y) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            mat = pose.last().pose();
            nmat = pose.last().normal();
            renderQuadCentered(mat, nmat, vc, 1.0F, rr, gg, bb, alpha, 15728880);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            mat = pose.last().pose();
            nmat = pose.last().normal();
            renderQuadCentered(mat, nmat, vc, 1.0F, rr, gg, bb, alpha, 15728880);
            pose.popPose();
         }

         pose.popPose();
      }
   }

   private boolean shouldSideBeRendered(BlockPos pos, Direction dir) {
      return this.architectBlocks != null && !this.architectBlocks.isEmpty() ? !this.architectBlocks.contains(pos.relative(dir)) : true;
   }

   @OnlyIn(Dist.CLIENT)
   private static void renderQuadCentered(Matrix4f mat, Matrix3f nmat, VertexConsumer vc, float size, float r, float g, float b, float a, int light) {
      float h = size * 0.5F;
      vertex(vc, mat, nmat, -h, -h, 0.0F, r, g, b, a, 0.0F, 1.0F, light, 0.0F, 0.0F, 1.0F);
      vertex(vc, mat, nmat, h, -h, 0.0F, r, g, b, a, 1.0F, 1.0F, light, 0.0F, 0.0F, 1.0F);
      vertex(vc, mat, nmat, h, h, 0.0F, r, g, b, a, 1.0F, 0.0F, light, 0.0F, 0.0F, 1.0F);
      vertex(vc, mat, nmat, -h, h, 0.0F, r, g, b, a, 0.0F, 0.0F, light, 0.0F, 0.0F, 1.0F);
   }

   @OnlyIn(Dist.CLIENT)
   private static void renderCube(
      Matrix4f mat,
      Matrix3f nmat,
      VertexConsumer vc,
      float r,
      float g,
      float b,
      float a,
      int light,
      boolean down,
      boolean up,
      boolean north,
      boolean south,
      boolean west,
      boolean east
   ) {
      float eps = 0.001F;
      float x0 = -eps;
      float x1 = 1.0F + eps;
      float y0 = -eps;
      float y1 = 1.0F + eps;
      float z0 = -eps;
      float z1 = 1.0F + eps;
      if (down) {
         vertex(vc, mat, nmat, x0, y0, z1, r, g, b, a, 0.0F, 0.0F, light, 0.0F, -1.0F, 0.0F);
         vertex(vc, mat, nmat, x1, y0, z1, r, g, b, a, 1.0F, 0.0F, light, 0.0F, -1.0F, 0.0F);
         vertex(vc, mat, nmat, x1, y0, z0, r, g, b, a, 1.0F, 1.0F, light, 0.0F, -1.0F, 0.0F);
         vertex(vc, mat, nmat, x0, y0, z0, r, g, b, a, 0.0F, 1.0F, light, 0.0F, -1.0F, 0.0F);
      }

      if (up) {
         vertex(vc, mat, nmat, x0, y1, z0, r, g, b, a, 0.0F, 0.0F, light, 0.0F, 1.0F, 0.0F);
         vertex(vc, mat, nmat, x1, y1, z0, r, g, b, a, 1.0F, 0.0F, light, 0.0F, 1.0F, 0.0F);
         vertex(vc, mat, nmat, x1, y1, z1, r, g, b, a, 1.0F, 1.0F, light, 0.0F, 1.0F, 0.0F);
         vertex(vc, mat, nmat, x0, y1, z1, r, g, b, a, 0.0F, 1.0F, light, 0.0F, 1.0F, 0.0F);
      }

      if (north) {
         vertex(vc, mat, nmat, x1, y0, z0, r, g, b, a, 0.0F, 1.0F, light, 0.0F, 0.0F, -1.0F);
         vertex(vc, mat, nmat, x0, y0, z0, r, g, b, a, 1.0F, 1.0F, light, 0.0F, 0.0F, -1.0F);
         vertex(vc, mat, nmat, x0, y1, z0, r, g, b, a, 1.0F, 0.0F, light, 0.0F, 0.0F, -1.0F);
         vertex(vc, mat, nmat, x1, y1, z0, r, g, b, a, 0.0F, 0.0F, light, 0.0F, 0.0F, -1.0F);
      }

      if (south) {
         vertex(vc, mat, nmat, x0, y0, z1, r, g, b, a, 0.0F, 1.0F, light, 0.0F, 0.0F, 1.0F);
         vertex(vc, mat, nmat, x1, y0, z1, r, g, b, a, 1.0F, 1.0F, light, 0.0F, 0.0F, 1.0F);
         vertex(vc, mat, nmat, x1, y1, z1, r, g, b, a, 1.0F, 0.0F, light, 0.0F, 0.0F, 1.0F);
         vertex(vc, mat, nmat, x0, y1, z1, r, g, b, a, 0.0F, 0.0F, light, 0.0F, 0.0F, 1.0F);
      }

      if (west) {
         vertex(vc, mat, nmat, x0, y0, z0, r, g, b, a, 0.0F, 1.0F, light, -1.0F, 0.0F, 0.0F);
         vertex(vc, mat, nmat, x0, y0, z1, r, g, b, a, 1.0F, 1.0F, light, -1.0F, 0.0F, 0.0F);
         vertex(vc, mat, nmat, x0, y1, z1, r, g, b, a, 1.0F, 0.0F, light, -1.0F, 0.0F, 0.0F);
         vertex(vc, mat, nmat, x0, y1, z0, r, g, b, a, 0.0F, 0.0F, light, -1.0F, 0.0F, 0.0F);
      }

      if (east) {
         vertex(vc, mat, nmat, x1, y0, z1, r, g, b, a, 0.0F, 1.0F, light, 1.0F, 0.0F, 0.0F);
         vertex(vc, mat, nmat, x1, y0, z0, r, g, b, a, 1.0F, 1.0F, light, 1.0F, 0.0F, 0.0F);
         vertex(vc, mat, nmat, x1, y1, z0, r, g, b, a, 1.0F, 0.0F, light, 1.0F, 0.0F, 0.0F);
         vertex(vc, mat, nmat, x1, y1, z1, r, g, b, a, 0.0F, 0.0F, light, 1.0F, 0.0F, 0.0F);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static void vertex(
      VertexConsumer vc,
      Matrix4f mat,
      Matrix3f nmat,
      float x,
      float y,
      float z,
      float r,
      float g,
      float b,
      float a,
      float u,
      float v,
      int light,
      float nx,
      float ny,
      float nz
   ) {
      vc.addVertex(mat, x, y, z)
         .setColor(r, g, b, a)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(light & 65535, light >> 16)
         .setNormal(nx, ny, nz);
   }
}
