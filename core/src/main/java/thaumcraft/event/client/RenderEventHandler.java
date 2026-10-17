package thaumcraft.event.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post;
import net.neoforged.neoforge.client.event.RenderHighlightEvent.Block;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.RenderTooltipEvent.GatherComponents;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IArchitect;
import thaumcraft.api.IGoggles;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.research.ScanResult;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.block.airy.energized_aura.NodeEnergizedBlockEntity;
import thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity;
import thaumcraft.block.wand_recharger.WandPedestalBlockEntity;
import thaumcraft.client.tooltip.AspectTooltipData;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.item.golem_bell.ItemGolemBell;
import thaumcraft.item.placement_mirror.ItemPlacementMirror;
import thaumcraft.item.relics.ItemThaumometer;
import thaumcraft.particles.fx.ParticleUtils;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ScanManager;
import thaumcraft.util.ItemStackData;
import thaumcraft.util.PlayerNotifications;
import thaumcraft.util.ThaumcraftRenderTypes;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class RenderEventHandler {
   public static List<?> blockTags = new ArrayList();
   int q;
   public static float tagscale = 0.0F;
   private static final int THAUMOMETER_NAME_Y = -40;
   private static final int THAUMOMETER_TYPE_Y = -52;
   private static final float GOGGLES_ASPECT_TEXT_Z = 8.0F;
   public long scanCount;
   public int scanX;
   public int scanY;
   public int scanZ;
   public int scanRadius = 8;
   int[][][] scannedBlocks;
   public REHWandHandler wandHandler = new REHWandHandler();
   public static boolean resetShaders = false;
   public static boolean fogFiddled = false;
   public static float fogTarget = 0.0F;
   public static int fogDuration = 0;
   public static float prevVignetteBrightness = 0.0F;
   public static float targetBrightness = 1.0F;
   public static final ResourceLocation vignetteTexPath = Thaumcraft.location("textures/misc/vignette.png");
   public static final ResourceLocation GOLEM_TEX_SCRIPT = Thaumcraft.location("textures/misc/script.png");
   public static final ResourceLocation GOLEM_TEX_EMPTY = Thaumcraft.location("textures/block/empty.png");
   public static final ResourceLocation GOLEM_TEX_HOME = Thaumcraft.location("textures/misc/home.png");
   public static final ResourceLocation GOLEM_TEX_MARKER = Thaumcraft.location("textures/misc/mark.png");
   public static final ResourceLocation UNKNOWN_ASPECT_TEX = Thaumcraft.location("textures/aspects/_unknown.png");

   public RenderEventHandler() {
      this.q = 0;
      this.scanCount = 0L;
      this.scanX = 0;
      this.scanY = 0;
      this.scanZ = 0;
      this.scannedBlocks = new int[17][17][17];
   }

   @OnlyIn(Dist.CLIENT)
   public void startScan(Player player, BlockPos center, long untilMillis, int radius) {
      Minecraft mc = Minecraft.getInstance();

      assert mc.level != null;

      if (mc.level.isClientSide) {
         this.scanX = center.getX();
         this.scanY = center.getY();
         this.scanZ = center.getZ();
         this.scanCount = untilMillis;
         this.scanRadius = Mth.clamp(radius, 1, 8);
         this.scannedBlocks = new int[17][17][17];
         int r = this.scanRadius;

         for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
               for (int dz = -r; dz <= r; dz++) {
                  int code = -1;
                  BlockPos p = center.offset(dx, dy, dz);
                  BlockState state = mc.level.getBlockState(p);
                  if (state.isAir() || state.is(Blocks.BEDROCK)) {
                     code = -1;
                  } else if (!state.getFluidState().isEmpty()) {
                     Fluid fluid = state.getFluidState().getType();
                     if (fluid == Fluids.LAVA) {
                        code = -10;
                     } else if (fluid == Fluids.WATER) {
                        code = -5;
                     } else {
                        code = -2;
                     }
                  } else if (state.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)) {
                     code = 0;
                  }

                  this.scannedBlocks[dx + 8][dy + 8][dz + 8] = code;
               }
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderScanOverlay(PoseStack poseStack, Camera camera, float partialTick, long nowMillis) {
      long left = this.scanCount - nowMillis;
      float alpha = 1.0F;
      if (left < 300L) {
         alpha = (float)left / 300.0F;
      }

      Vec3 cam = camera.getPosition();
      poseStack.pushPose();
      poseStack.translate(-cam.x, -cam.y, -cam.z);
      RenderSystem.disableDepthTest();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      BufferSource buf = Minecraft.getInstance().renderBuffers().bufferSource();
      VertexConsumer vc = buf.getBuffer(UtilsFX.SCAN_XRAY_LINES);
      int r = this.scanRadius;

      for (int dx = -r; dx <= r; dx++) {
         for (int dy = -r; dy <= r; dy++) {
            for (int dz = -r; dz <= r; dz++) {
               int code = this.scannedBlocks[dx + 8][dy + 8][dz + 8];
               if (code != -1) {
                  BlockPos p = new BlockPos(this.scanX + dx, this.scanY + dy, this.scanZ + dz);
                  AABB box = new AABB(p).inflate(0.002);
                  float rr = 0.2F;
                  float gg = 1.0F;
                  float bb = 0.2F;
                  if (code == -5) {
                     rr = 0.2F;
                     gg = 0.4F;
                     bb = 1.0F;
                  }

                  if (code == -10) {
                     rr = 1.0F;
                     gg = 0.4F;
                     bb = 0.1F;
                  }

                  LevelRenderer.renderLineBox(poseStack, vc, box, rr, gg, bb, alpha * 0.65F);
               }
            }
         }
      }

      buf.endBatch(UtilsFX.SCAN_XRAY_LINES);
      RenderSystem.disableBlend();
      RenderSystem.enableDepthTest();
      poseStack.popPose();
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onHandRender(RenderHandEvent event) {
      if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().getItem() instanceof ItemThaumometer) {
         Minecraft minecraft = Minecraft.getInstance();
         AbstractClientPlayer player = minecraft.player;
         if (player != null && player.getOffhandItem().isEmpty()) {
            PlayerRenderer renderer = (PlayerRenderer)minecraft.getEntityRenderDispatcher().getRenderer(player);
            RenderSystem.setShaderTexture(0, player.getSkin().texture());
            PoseStack pose = event.getPoseStack();
            pose.pushPose();
            pose.translate(1.5F, -3.3F, -2.22F);
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.mulPose(Axis.XP.rotationDegrees(40.0F));
            pose.mulPose(Axis.ZN.rotationDegrees(40.0F));
            pose.scale(3.0F, 3.0F, 3.0F);
            renderer.renderRightHand(pose, event.getMultiBufferSource(), event.getPackedLight(), player);
            pose.popPose();
            pose.pushPose();
            pose.translate(-1.5F, -3.3F, -2.22F);
            pose.mulPose(Axis.YN.rotationDegrees(180.0F));
            pose.mulPose(Axis.XP.rotationDegrees(40.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(40.0F));
            pose.scale(3.0F, 3.0F, 3.0F);
            renderer.renderLeftHand(pose, event.getMultiBufferSource(), event.getPackedLight(), player);
            pose.popPose();
         }
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onGatherTooltipComponents(GatherComponents event) {
      if (ModList.get().isLoaded("jei")) {
         Minecraft minecraft = Minecraft.getInstance();
         Player player = minecraft.player;
         if (player != null) {
            AspectList aspects = this.getScannedAspectsForTooltip(player, event.getItemStack());
            if (aspects != null && aspects.size() > 0) {
               event.getTooltipElements().add(Either.left(Component.empty()));
               event.getTooltipElements().add(Either.left(Component.literal("Aspects:").withStyle(ChatFormatting.GOLD)));
               PlayerKnowledge knowledge = PlayerKnowledge.of(player);

               for (Aspect aspect : aspects.getAspectsSorted()) {
                  if (aspect != null) {
                     boolean discovered = knowledge.hasDiscoveredAspect(aspect);
                     Component aspectName = discovered
                        ? aspect.getName().copy().withStyle(style -> style.withColor(aspect.getColor()))
                        : Component.translatable("tc.aspect.unknown").withStyle(ChatFormatting.DARK_GRAY);
                     ResourceLocation texture = discovered && aspect.getImage() != null ? aspect.getImage() : UNKNOWN_ASPECT_TEX;
                     int color = discovered ? aspect.getColor() : 10132122;
                     event.getTooltipElements().add(Either.right(new AspectTooltipData(texture, color, aspectName, aspects.getAmount(aspect))));
                  }
               }
            }
         }
      }
   }

   private AspectList getScannedAspectsForTooltip(Player player, ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      } else {
         ItemStack probe = stack.copy();
         probe.setCount(1);
         ScanResult scan = ScanResult.ofItem(probe);
         return !ScanManager.hasBeenScanned(player, scan) ? null : ScanManager.getScanAspects(scan, player.level());
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void renderOverlay(Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      GuiGraphics graphics = event.getGuiGraphics();
      Window window = minecraft.getWindow();
      long time = System.nanoTime() / 1000000L;
      if (event.getName().equals(VanillaGuiLayers.TITLE)) {
         if (!PlayerNotifications.getListAndUpdate(time).isEmpty()) {
            this.renderNotifyHUD(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(), time);
         }

         if (!PlayerNotifications.getAspectListAndUpdate(time).isEmpty()) {
            this.renderAspectHUD(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(), time);
         }

         this.wandHandler.handleFociRadial(minecraft, graphics, window, time);
      } else if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
         Player player = minecraft.player;
         if (player == null) {
            return;
         }

         PoseStack pose = graphics.pose();
         float screenWidth = window.getGuiScaledWidth();
         float screenHeight = window.getGuiScaledHeight();
         float ratio = screenWidth / screenHeight;
         float scaleValue = ratio < 1.7777778F ? screenHeight / (1.0F + (1.7777778F - ratio)) : screenHeight;
         scaleValue = scaleValue * 4.5F / 1080.0F;
         pose.pushPose();
         pose.translate(screenWidth / 2.0F, screenHeight / 2.0F, 0.0F);
         pose.scale(scaleValue, scaleValue, 1.0F);
         this.renderThaumometerScan(graphics, player);
         pose.popPose();
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onRenderHighlight(Block event) {
      BlockPos pos = event.getTarget().getBlockPos();
      Minecraft mc = Minecraft.getInstance();

      assert Minecraft.getInstance().level != null;

      BlockState state = Minecraft.getInstance().level.getBlockState(pos);
      if (state.is((net.minecraft.world.level.block.Block)ModBlocks.AURA_NODE.get())
         || state.is((net.minecraft.world.level.block.Block)ModBlocks.ENERGIZED_AURA_NODE.get())) {
         event.setCanceled(true);
      }

      ItemStack stack = mc.player.getMainHandItem();
      if (!(stack.getItem() instanceof IArchitect) || stack.getItem() instanceof ItemFocusBasic) {
         stack = mc.player.getOffhandItem();
         if (!(stack.getItem() instanceof IArchitect) || stack.getItem() instanceof ItemFocusBasic) {
            return;
         }
      }

      int ticks = mc.player.tickCount;
      if (this.wandHandler.handleArchitectOverlay(stack, event, ticks, event.getTarget())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onRenderLevelStage(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         Minecraft minecraft = Minecraft.getInstance();
         Player player = minecraft.player;
         if (player != null && minecraft.level != null) {
            long now = Util.getMillis();
            if (this.scanCount > now) {
               this.renderScanOverlay(event.getPoseStack(), event.getCamera(), event.getPartialTick().getGameTimeDeltaPartialTick(false), now);
            }

            if (tagscale > 0.0F) {
               tagscale -= 0.005F;
            }

            this.renderGolemBellMarkers(event.getPoseStack(), event.getCamera(), event.getPartialTick().getGameTimeDeltaPartialTick(false), player);
            this.renderPlacementMirrorPrediction(event.getPoseStack(), event.getCamera(), player);
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            boolean hasGoggles = !helmet.isEmpty() && helmet.getItem() instanceof IGoggles goggles && goggles.showIngamePopups(helmet, player);
            if (hasGoggles || !blockTags.isEmpty()) {
               HitResult hitResult = minecraft.hitResult;
               if (hitResult != null && hitResult.getType() == Type.BLOCK) {
                  BlockHitResult blockHit = (BlockHitResult)hitResult;
                  BlockPos targetPos = blockHit.getBlockPos();
                  Direction side = blockHit.getDirection();
                  if (blockTags.size() >= 5) {
                     int tagX = (Integer)blockTags.get(0);
                     int tagY = (Integer)blockTags.get(1);
                     int tagZ = (Integer)blockTags.get(2);
                     AspectList tagAspects = (AspectList)blockTags.get(3);
                     Direction tagSide = Direction.from3DDataValue((Integer)blockTags.get(4));
                     if (tagX == targetPos.getX() && tagY == targetPos.getY() && tagZ == targetPos.getZ()) {
                        if (tagscale < 0.5F) {
                           tagscale = tagscale + (0.031F - tagscale / 10.0F);
                        }

                        this.drawTagsOnContainer(
                           event.getPoseStack(),
                           event.getCamera(),
                           tagX + tagSide.getStepX() / 2.0,
                           tagY + tagSide.getStepY() / 2.0,
                           tagZ + tagSide.getStepZ() / 2.0,
                           tagAspects,
                           tagSide,
                           event.getPartialTick().getGameTimeDeltaPartialTick(false)
                        );
                     }
                  }

                  if (hasGoggles) {
                     BlockEntity blockEntity = minecraft.level.getBlockEntity(targetPos);
                     if (blockEntity instanceof IAspectContainer container) {
                        AspectList aspects = container.getAspects();
                        if (aspects != null && aspects.size() > 0) {
                           boolean airAbove = minecraft.level.isEmptyBlock(targetPos.above());
                           if (tagscale < 0.3F) {
                              tagscale = tagscale + (0.031F - tagscale / 10.0F);
                           }

                           double offsetY = airAbove ? 0.4 : 0.0;
                           if (blockEntity instanceof WandPedestalBlockEntity) {
                              offsetY = 1.0;
                           } else if (blockEntity instanceof NodeEnergizedBlockEntity) {
                              offsetY = 0.0;
                           }

                           double renderX = targetPos.getX();
                           double renderY = targetPos.getY() + offsetY;
                           double renderZ = targetPos.getZ();
                           Direction displaySide = airAbove ? Direction.UP : side;
                           if (blockEntity instanceof RunicMatrixBlockEntity) {
                              Vec3 camPos = event.getCamera().getPosition();
                              Vec3 tagCenter = new Vec3(renderX + 0.5, renderY + 0.5, renderZ + 0.5);
                              Vec3 dirToPlayer = camPos.subtract(tagCenter).normalize();
                              double shiftAmount = 0.25;
                              renderX += dirToPlayer.x * shiftAmount;
                              renderY += dirToPlayer.y * shiftAmount;
                              renderZ += dirToPlayer.z * shiftAmount;
                           }

                           this.drawTagsOnContainer(
                              event.getPoseStack(),
                              event.getCamera(),
                              renderX,
                              renderY,
                              renderZ,
                              aspects,
                              displaySide,
                              event.getPartialTick().getGameTimeDeltaPartialTick(false)
                           );
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderGolemBellMarkers(PoseStack poseStack, Camera camera, float partialTick, Player player) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc.level;
      if (level != null) {
         ItemStack stack = player.getMainHandItem();
         CompoundTag tag = ItemStackData.getTag(stack);
         if (tag == null || !tag.contains("markers", 9)) {
            stack = player.getOffhandItem();
            tag = ItemStackData.getTag(stack);
            if (tag == null || !tag.contains("markers", 9)) {
               return;
            }
         }

         Entity golem = null;
         BlockPos homePos = null;
         Direction homeFace = null;
         if (stack.getItem() instanceof ItemGolemBell) {
            BlockPos homeAir = ItemGolemBell.getGolemHomeCoords(stack);
            homeFace = ItemGolemBell.getGolemHomeFace(stack);
            if (homeAir != null && homeFace != null) {
               homePos = homeAir.relative(homeFace.getOpposite());
            }

            int gid = ItemGolemBell.getGolemId(stack);
            if (gid > -1) {
               golem = level.getEntity(gid);
            }

            if (!(golem instanceof EntityGolemBase)) {
               return;
            }
         }

         Vec3 cam = camera.getPosition();
         float time = (float)(System.nanoTime() / 30000000L);
         BufferSource buf = mc.renderBuffers().bufferSource();
         poseStack.pushPose();
         poseStack.translate(-cam.x, -cam.y, -cam.z);
         if (golem != null
            && homePos != null
            && homeFace != null
            && player.distanceToSqr(homePos.getX() + 0.5, homePos.getY() + 0.5, homePos.getZ() + 0.5) < 4096.0) {
            float r = (float)(Math.sin(time % 32767.0F / 12.0F + homeFace.ordinal()) * 0.2F + 0.8F);
            float g = (float)(Math.sin(time % 32767.0F / 14.0F + homeFace.ordinal()) * 0.2F + 0.8F);
            float b = (float)(Math.sin(time % 32767.0F / 16.0F + homeFace.ordinal()) * 0.2F + 0.8F);
            drawFaceQuad(poseStack, buf.getBuffer(ThaumcraftRenderTypes.golemOverlayQuad(GOLEM_TEX_HOME)), homePos, homeFace, 0.65F, 0.78431374F, r, g, b);
         }

         ListTag tl = tag.getList("markers", 10);

         for (int q = 0; q < tl.size(); q++) {
            CompoundTag m = tl.getCompound(q);
            BlockPos origin = BlockPos.of(m.getLong("Pos"));
            int sideId = m.getByte("Side") & 7;
            if (sideId < 6) {
               int color = m.getByte("Color");
               if (markerInThisDimension(m, level)) {
                  Direction dir = Direction.from3DDataValue(sideId);
                  BlockPos airPos = origin.relative(dir);
                  if (!(player.distanceToSqr(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5) >= 4096.0)) {
                     float[] rgb = markerRGB(color, time, sideId);
                     drawFaceQuad(
                        poseStack,
                        buf.getBuffer(ThaumcraftRenderTypes.golemOverlayQuad(GOLEM_TEX_MARKER)),
                        origin,
                        dir,
                        0.4F,
                        0.78431374F,
                        rgb[0],
                        rgb[1],
                        rgb[2]
                     );
                     if (level.isEmptyBlock(origin)) {
                        for (int a = 0; a < 6; a++) {
                           Direction d = Direction.from3DDataValue(a);
                           BlockPos p = origin.relative(d);
                           drawFaceQuad(
                              poseStack,
                              buf.getBuffer(ThaumcraftRenderTypes.golemOverlayQuad(GOLEM_TEX_EMPTY)),
                              p,
                              d.getOpposite(),
                              0.98F,
                              0.78431374F,
                              rgb[0],
                              rgb[1],
                              rgb[2]
                           );
                        }
                     }

                     if (golem != null && Config.golemLinkQuality > 3) {
                        drawMarkerLine(
                           poseStack,
                           buf.getBuffer(ThaumcraftRenderTypes.golemLinkStrip(GOLEM_TEX_SCRIPT)),
                           golem,
                           origin,
                           dir,
                           sideId,
                           color,
                           partialTick,
                           time
                        );
                     }
                  }
               }
            }
         }

         poseStack.popPose();
         buf.endBatch(ThaumcraftRenderTypes.golemOverlayQuad(GOLEM_TEX_HOME));
         buf.endBatch(ThaumcraftRenderTypes.golemOverlayQuad(GOLEM_TEX_EMPTY));
         buf.endBatch(ThaumcraftRenderTypes.golemOverlayQuad(GOLEM_TEX_MARKER));
         buf.endBatch(ThaumcraftRenderTypes.golemLinkStrip(GOLEM_TEX_SCRIPT));
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderPlacementMirrorPrediction(PoseStack poseStack, Camera camera, Player localPlayer) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
         VertexConsumer vc = buffer.getBuffer(UtilsFX.SCAN_XRAY_LINES);
         Vec3 camPos = camera.getPosition();

         for (Player player : level.players()) {
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof ItemPlacementMirror) {
               ItemStack bound = ItemPlacementMirror.getPrototype(held);
               if (!bound.isEmpty()) {
                  double range = 64.0;
                  Vec3 eyePos = player.getEyePosition(1.0F);
                  Vec3 viewVec = player.getViewVector(1.0F);
                  Vec3 endPos = eyePos.add(viewVec.scale(range));
                  BlockHitResult hitResult = level.clip(
                     new ClipContext(
                        eyePos, endPos, net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE, player
                     )
                  );
                  if (hitResult.getType() != Type.MISS) {
                     List<BlockPos> positions = ItemPlacementMirror.getTargetPositions(held, player, hitResult);
                     boolean hasMaterial = ItemPlacementMirror.hasMaterial(player, bound);
                     float red = hasMaterial ? 0.6F : 1.0F;
                     float green = hasMaterial ? 0.85F : 0.25F;
                     float blue = hasMaterial ? 1.0F : 0.25F;
                     float alpha = player == localPlayer ? 0.8F : 0.35F;

                     for (BlockPos pos : positions) {
                        AABB box = new AABB(pos).move(-camPos.x, -camPos.y, -camPos.z).inflate(0.002);
                        LevelRenderer.renderLineBox(poseStack, vc, box, red, green, blue, alpha);
                     }
                  }
               }
            }
         }

         buffer.endBatch(UtilsFX.SCAN_XRAY_LINES);
      }
   }

   private static boolean markerInThisDimension(CompoundTag m, Level level) {
      if (m.contains("dim_rl", 8)) {
         return m.getString("dim_rl").equals(level.dimension().location().toString());
      } else if (m.contains("Dim", 8)) {
         return m.getString("Dim").equals(level.dimension().location().toString());
      } else if (m.contains("dim", 3)) {
         int dim = m.getInt("dim");
         if (dim == 0) {
            return level.dimension() == Level.OVERWORLD;
         } else if (dim == -1) {
            return level.dimension() == Level.NETHER;
         } else {
            return dim == 1 ? level.dimension() == Level.END : true;
         }
      } else {
         return true;
      }
   }

   private static float[] markerRGB(int color, float time, int side) {
      if (color > -1) {
         int rgb = UtilsFX.colors[color];
         return new float[]{(rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F};
      } else {
         return new float[]{
            (float)(Math.sin(time % 32767.0F / 12.0F + side) * 0.2F + 0.8F),
            (float)(Math.sin(time % 32767.0F / 14.0F + side) * 0.2F + 0.8F),
            (float)(Math.sin(time % 32767.0F / 16.0F + side) * 0.2F + 0.8F)
         };
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static void drawFaceQuad(PoseStack poseStack, VertexConsumer vc, BlockPos pos, Direction face, float scale, float alpha, float r, float g, float b) {
      poseStack.pushPose();
      poseStack.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      float ox = face.getStepX() * 0.501F;
      float oy = face.getStepY() * 0.501F;
      float oz = face.getStepZ() * 0.501F;
      poseStack.translate(ox, oy, oz);
      switch (face) {
         case SOUTH:
         default:
            break;
         case NORTH:
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            break;
         case EAST:
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            break;
         case WEST:
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            break;
         case UP:
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            break;
         case DOWN:
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
      }

      poseStack.scale(scale, scale, scale);
      Matrix4f mat = poseStack.last().pose();
      vc.addVertex(mat, -0.5F, -0.5F, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 1.0F).setUv2(240, 240);
      vc.addVertex(mat, 0.5F, -0.5F, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 1.0F).setUv2(240, 240);
      vc.addVertex(mat, 0.5F, 0.5F, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 0.0F).setUv2(240, 240);
      vc.addVertex(mat, -0.5F, 0.5F, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 0.0F).setUv2(240, 240);
      poseStack.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   private static void drawMarkerLine(
      PoseStack poseStack, VertexConsumer vc, Entity golem, BlockPos origin, Direction side, int sideId, int color, float partialTick, float time
   ) {
      double ePX = golem.xOld + (golem.getX() - golem.xOld) * partialTick;
      double ePY = golem.yOld + (golem.getY() - golem.yOld) * partialTick;
      double ePZ = golem.zOld + (golem.getZ() - golem.zOld) * partialTick;
      double ds1y = ePY + golem.getBbHeight();
      double airX = origin.getX() + 0.5 + side.getStepX();
      double airY = origin.getY() + 0.5 + side.getStepY();
      double airZ = origin.getZ() + 0.5 + side.getStepZ();
      double midX = origin.getX() + 0.5 + side.getStepX() * 0.5;
      double midY = origin.getY() + 0.5 + side.getStepY() * 0.5;
      double midZ = origin.getZ() + 0.5 + side.getStepZ() * 0.5;
      double dc1x = airX - ePX;
      double dc1y = airY - ds1y;
      double dc1z = airZ - ePZ;
      double dc2x = midX - ePX;
      double dc2y = midY - ds1y;
      double dc2z = midZ - ePZ;
      double d3 = midX - ePX;
      double d4 = midY - ePY;
      double d5 = midZ - ePZ;
      float dist = (float)Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
      float blocks = Math.round(dist);
      float length = blocks * Config.golemLinkQuality;
      if (!(length <= 0.0F)) {
         float baseR = 1.0F;
         float baseG = 1.0F;
         float baseB = 1.0F;
         if (color > -1) {
            int rgb = UtilsFX.colors[color];
            baseR = (rgb >> 16 & 0xFF) / 255.0F;
            baseG = (rgb >> 8 & 0xFF) / 255.0F;
            baseB = (rgb & 0xFF) / 255.0F;
         }

         Matrix4f mat = poseStack.last().pose();
         float f9 = 0.0F;
         float f10 = 1.0F;
         float scroll = Mth.frac(time * 0.005F);

         for (int i = 0; i <= length; i++) {
            float f2 = i / length;
            float f2a = i * 1.5F / length;
            f2a = Math.min(0.75F, f2a);
            float f3 = 1.0F - Math.abs(i - length / 2.0F) / (length / 2.0F);
            float f4 = 0.0F;
            float r = baseR;
            float g = baseG;
            float b = baseB;
            if (color == -1) {
               r = (float)(Math.sin(time % 32767.0F / 12.0F + sideId + i) * 0.2F + 0.8F);
               g = (float)(Math.sin(time % 32767.0F / 14.0F + sideId + i) * 0.2F + 0.8F);
               b = (float)(Math.sin(time % 32767.0F / 16.0F + sideId + i) * 0.2F + 0.8F);
            }

            double wob1 = sideId * 20.0 + origin.getZ() % 16.0 + dist * (1.0F - f2) * Config.golemLinkQuality - time % 32767.0F / 5.0F;
            double wob2 = sideId * 20.0 + origin.getX() % 16.0 + dist * (1.0F - f2) * Config.golemLinkQuality - time % 32767.0F / 5.0F;
            double wob3 = sideId * 20.0 + origin.getY() % 16.0 + dist * (1.0F - f2) * Config.golemLinkQuality - time % 32767.0F / 5.0F;
            double dx = dc1x + Math.sin(wob1 / 4.0) * 0.5 * f3;
            double dy = dc1y + Math.sin(wob2 / 3.0) * 0.5 * f3;
            double dz = dc1z + Math.sin(wob3 / 2.0) * 0.5 * f3;
            if (i > length - Config.golemLinkQuality / 2) {
               double dx2 = dc2x + Math.sin(wob1 / 4.0) * 0.5 * f3;
               double dy2 = dc2y + Math.sin(wob2 / 3.0) * 0.5 * f3;
               double dz2 = dc2z + Math.sin(wob3 / 2.0) * 0.5 * f3;
               float f3b = (length - i) / (Config.golemLinkQuality / 2.0F);
               f4 = 1.0F - f3b;
               dx = dx2 * f4 + dx * f3b;
               dy = dy2 * f4 + dy * f3b;
               dz = dz2 * f4 + dz * f3b;
            }

            int ir = Mth.clamp((int)(r * 255.0F), 0, 255);
            int ig = Mth.clamp((int)(g * 255.0F), 0, 255);
            int ib = Mth.clamp((int)(b * 255.0F), 0, 255);
            int a = Mth.clamp((int)(f2a * (1.0F - f4) * 255.0F), 0, 255);
            float u = (1.0F - f2) * dist - scroll;
            float px = (float)(ePX + dx * f2);
            float py = (float)(ds1y + dy * f2);
            float pz = (float)(ePZ + dz * f2);
            vc.addVertex(mat, px, py - 0.05F, pz).setColor(ir, ig, ib, a).setUv(u, f10).setUv2(240, 240);
            vc.addVertex(mat, px, py + 0.05F, pz).setColor(ir, ig, ib, a).setUv(u, f9).setUv2(240, 240);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void drawTagsOnContainer(PoseStack poseStack, Camera camera, double x, double y, double z, AspectList aspects, Direction direction, float partialTick) {
      if (aspects != null && aspects.size() != 0) {
         Minecraft minecraft = Minecraft.getInstance();
         Player player = minecraft.player;
         if (player != null) {
            Vec3 cameraPos = camera.getPosition();
            double camX = cameraPos.x;
            double camY = cameraPos.y;
            double camZ = cameraPos.z;
            int maxPerRow = 5;
            int aspectIndex = 0;
            float rowOffset = 0.0F;
            int totalAspects = aspects.size();
            poseStack.pushPose();
            poseStack.translate(
               -camX + x + 0.5 + tagscale * 2.0F * direction.getStepX(),
               -camY + y + 0.5 + tagscale * 2.0F * direction.getStepY(),
               -camZ + z + 0.5 + tagscale * 2.0F * direction.getStepZ()
            );
            float yaw = (float)(Math.atan2(camX - (x + 0.5), camZ - (z + 0.5)) * 180.0 / Math.PI) + 180.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.disableDepthTest();

            for (Aspect aspect : aspects.getAspects()) {
               int rowCount = Math.min(totalAspects - aspectIndex / 5 * 5, 5);
               if (aspectIndex >= 5) {
                  aspectIndex = 0;
                  rowOffset -= tagscale * 1.05F;
                  totalAspects -= 5;
                  rowCount = Math.min(totalAspects, 5);
               }

               float xOffset = (aspectIndex - rowCount / 2.0F + 0.5F) * tagscale * 4.0F * tagscale;
               Color color = new Color(aspect.getColor());
               poseStack.pushPose();
               poseStack.translate(xOffset, -rowOffset, 0.0F);
               poseStack.scale(tagscale, tagscale, tagscale);
               PlayerKnowledge knowledge = PlayerKnowledge.of(player);
               ResourceLocation aspectImage;
               if (knowledge.hasDiscoveredAspect(aspect)) {
                  aspectImage = aspect.getImage();
               } else {
                  aspectImage = Thaumcraft.location("textures/aspects/_unknown.png");
               }

               RenderSystem.setShaderTexture(0, aspectImage);
               UtilsFX.renderQuadCenteredFromTexture(
                  poseStack, 1.0F, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 220, 771, 0.75F
               );
               int amount = aspects.getAmount(aspect);
               if (amount >= 0) {
                  String amountStr = String.valueOf(amount);
                  poseStack.scale(0.04F, 0.04F, 0.04F);
                  poseStack.translate(0.0F, 6.0F, 8.0F);
                  int textWidth = minecraft.font.width(amountStr);
                  minecraft.font
                     .drawInBatch(
                        amountStr,
                        14 - textWidth,
                        1.0F,
                        1118481,
                        false,
                        poseStack.last().pose(),
                        minecraft.renderBuffers().bufferSource(),
                        DisplayMode.NORMAL,
                        0,
                        15728880
                     );
                  poseStack.translate(0.0, 0.0, -0.1);
                  minecraft.font
                     .drawInBatch(
                        amountStr,
                        13 - textWidth,
                        0.0F,
                        16777215,
                        false,
                        poseStack.last().pose(),
                        minecraft.renderBuffers().bufferSource(),
                        DisplayMode.NORMAL,
                        0,
                        15728880
                     );
                  minecraft.renderBuffers().bufferSource().endBatch();
               }

               poseStack.popPose();
               aspectIndex++;
            }

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            poseStack.popPose();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void drawTextInAir(PoseStack poseStack, Camera camera, double x, double y, double z, String text) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null) {
         Vec3 cameraPos = camera.getPosition();
         double camX = cameraPos.x;
         double camY = cameraPos.y;
         double camZ = cameraPos.z;
         poseStack.pushPose();
         poseStack.translate(-camX + x + 0.5, -camY + y + 0.5, -camZ + z + 0.5);
         float yaw = (float)(Math.atan2(camX - (x + 0.5), camZ - (z + 0.5)) * 180.0 / Math.PI) + 180.0F;
         poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
         poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
         poseStack.scale(0.02F, 0.02F, 0.02F);
         int textWidth = minecraft.font.width(text);
         RenderSystem.enableBlend();
         minecraft.font
            .drawInBatch(
               text,
               1.0F - textWidth / 2.0F,
               1.0F,
               1118481,
               false,
               poseStack.last().pose(),
               minecraft.renderBuffers().bufferSource(),
               DisplayMode.NORMAL,
               0,
               15728880
            );
         poseStack.translate(0.0, 0.0, -0.1);
         minecraft.font
            .drawInBatch(
               text,
               -textWidth / 2.0F,
               0.0F,
               16777215,
               false,
               poseStack.last().pose(),
               minecraft.renderBuffers().bufferSource(),
               DisplayMode.NORMAL,
               0,
               15728880
            );
         minecraft.renderBuffers().bufferSource().endBatch();
         poseStack.popPose();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void renderNotifyHUD(GuiGraphics graphics, int screenWidth, int screenHeight, long time) {
      Minecraft minecraft = Minecraft.getInstance();
      PoseStack pose = graphics.pose();
      pose.pushPose();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      ArrayList<PlayerNotifications.Notification> listAndUpdate = PlayerNotifications.getListAndUpdate(time);
      float n7 = -8.0F;

      for (int i = 0; i < listAndUpdate.size() && i < Config.notificationMax; i++) {
         PlayerNotifications.Notification notification = listAndUpdate.get(i);
         Component text = notification.text;
         int n8 = minecraft.font.width(text) / 2;
         int n9 = 255;
         if (i == listAndUpdate.size() - 1 && notification.created > time) {
            n9 = 255 - (int)((float)(notification.created - time) / (Config.notificationDelay / 4) * 240.0F);
         }

         if (notification.expire < time + Config.notificationDelay) {
            n9 = (int)(255.0F - (float)(time + Config.notificationDelay - notification.expire) / Config.notificationDelay * 240.0F);
            n7 = -8.0F * (n9 / 255.0F);
         }

         int n10 = (n9 / 2 << 24) + 16711680 + 65280 + 255;
         pose.pushPose();
         pose.translate(screenWidth - n8 - 10, screenHeight - i * 8 + n7, 0.0F);
         pose.scale(0.5F, 0.5F, 0.5F);
         graphics.drawString(minecraft.font, text, -4, -8, n10);
         pose.popPose();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
         if (notification.image != null) {
            pose.pushPose();
            pose.translate(screenWidth - 9, screenHeight - i * 8 + n7 - 6.0F, 0.0F);
            pose.scale(0.03125F, 0.03125F, 0.03125F);
            RenderSystem.setShaderTexture(0, notification.image);
            graphics.setColor(
               ARGB32.red(notification.color) / 255.0F, ARGB32.green(notification.color) / 255.0F, ARGB32.blue(notification.color) / 255.0F, n9 / 511.0F
            );
            UtilsFX.drawTexturedQuad(graphics, notification.image, 0, 0, 0, 0, 256, 256, -90);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            pose.popPose();
         }

         if (i == listAndUpdate.size() - 1 && notification.created > time) {
            float n11 = (float)(notification.created - time) / (Config.notificationDelay / 4);
            int n12 = 255 - (int)(n11 * 240.0F);
            pose.pushPose();
            pose.translate(
               screenWidth - 5 - 8.0F * n11 - (1.0F - n11) * (1.0F - n11) * (1.0F - n11) * n8 * 3.0F, screenHeight - i * 8 + n7 - 2.0F - 8.0F * n11, 0.0F
            );
            pose.scale(n11, n11, n11);
            graphics.setColor(1.0F, 1.0F, 1.0F, 0.5F - n12 / 511.0F);

            assert minecraft.player != null;

            UtilsFX.drawTexturedQuad(
               graphics, ParticleUtils.PARTICLE_TEXTURE, 0, 0, 16 * ((minecraft.player.tickCount + i * 3) % 16), 80, 16, 16, -90 - listAndUpdate.size()
            );
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            pose.popPose();
         }
      }

      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      pose.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   private void renderAspectHUD(GuiGraphics graphics, int screenWidth, int screenHeight, long time) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      float n6 = 0.0F;

      for (PlayerNotifications.AspectNotification aspectNotification : PlayerNotifications.getAspectListAndUpdate(time)) {
         if (aspectNotification.created <= time) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            if (aspectNotification.aspect.getImage() != null) {
               pose.pushPose();
               int n7 = (int)(screenWidth * aspectNotification.startX);
               int n8 = (int)(screenHeight * aspectNotification.startY);
               int n10 = -8;
               int n11 = (int)(screenWidth * (0.25F + aspectNotification.startX));
               int n12 = (int)(screenHeight * aspectNotification.startY);
               double n13 = (double)(time - aspectNotification.created) / (aspectNotification.expire - aspectNotification.created);
               double n14 = (1.0 - n13) * (1.0 - n13) * n7 + 2.0 * (1.0 - n13) * n13 * n11 + n13 * n13 * screenWidth;
               double n15 = (1.0 - n13) * (1.0 - n13) * n8 + 2.0 * (1.0 - n13) * n13 * n12 + n13 * n13 * n10;
               float n16 = 1.0F;
               if (n13 < 0.3F) {
                  n16 = (float)(n13 / 0.3F);
               } else if (n13 > 0.66F) {
                  n16 = (float)(1.0 - (n13 - 0.66F) / 0.34F);
               }

               if (n16 > n6) {
                  n6 = n16;
               }

               pose.translate(n14, n15, 0.0);
               pose.scale(0.075F * n16, 0.075F * n16, 0.075F * n16);
               int color = aspectNotification.aspect.getColor();
               graphics.setColor(ARGB32.red(color) / 255.0F, ARGB32.green(color) / 255.0F, ARGB32.blue(color) / 255.0F, n16 * 0.66F);
               UtilsFX.drawTexturedQuad(graphics, aspectNotification.aspect.getImage(), 0, 0, 0, 0, 256, 256, -90);
               graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               pose.popPose();
            }
         }
      }

      if (n6 > 0.0F) {
         pose.pushPose();
         pose.translate(screenWidth - 16, 0.0, 0.0);
         pose.scale(0.0625F, 0.0625F, 0.0625F);
         graphics.setColor(1.0F, 1.0F, 1.0F, n6);
         UtilsFX.drawTexturedQuad(graphics, Thaumcraft.location("textures/item/thaumonomicon.png"), 0, 0, 0, 0, 256, 256, -90);
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         pose.popPose();
      }

      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      pose.popPose();
   }

   private static Component getPhenomenaName(String phenomena) {
      if (phenomena == null) {
         return Component.literal("?");
      } else if (phenomena.startsWith(NodeEnergizedBlockEntity.SCAN_PREFIX)) {
         return Component.translatable("block.thaumcraft.energized_aura_node");
      } else if (phenomena.startsWith("NODE")) {
         return Component.translatable("block.thaumcraft.aura_node");
      } else {
         return switch (phenomena) {
            case "WATER" -> Component.translatable("block.minecraft.water");
            case "LAVA" -> Component.translatable("block.minecraft.lava");
            case "FLUX_GOO" -> Component.translatable("fluid.fluxGoo");
            case "FLUX_GAS" -> Component.translatable("fluid.fluxGas");
            case "PURE" -> Component.translatable("fluid.fluidPure");
            case "DEATH" -> Component.translatable("fluid.fluidDeath");
            default -> Component.literal(phenomena);
         };
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static String getNodeTypeText(INode node) {
      String text = I18n.get("nodetype." + node.getNodeType() + ".name", new Object[0]);
      if (node.getNodeModifier() != null) {
         text = text + ", " + I18n.get("nodemod." + node.getNodeModifier() + ".name", new Object[0]);
      }

      return text;
   }

   @OnlyIn(Dist.CLIENT)
   private void renderThaumometerScan(GuiGraphics graphics, Player player) {
      ItemStack thaumometer = player.getMainHandItem();
      if (thaumometer.getItem() instanceof ItemThaumometer && player.getOffhandItem().isEmpty()) {
         Font font = Minecraft.getInstance().font;
         PoseStack pose = graphics.pose();
         Level level = player.level();
         ScanResult doScan = ((ItemThaumometer)thaumometer.getItem()).doScan(thaumometer, player);
         if (doScan != null) {
            AspectList list = null;
            pose.translate(0.0F, 0.0F, -0.01F);
            Component s = Component.literal("?");
            ItemStack itemStack = ItemStack.EMPTY;
            switch (doScan.type) {
               case ITEM:
                  itemStack = doScan.stack;
                  if (ScanManager.hasBeenScanned(player, doScan)) {
                     list = ScanManager.getScanAspects(doScan, level);
                  }
                  break;
               case BLOCK:
                  itemStack = doScan.stack;
                  if (itemStack.isEmpty() && doScan.block != null) {
                     s = Component.translatable(doScan.block.getDescriptionId());
                  }

                  if (ScanManager.hasBeenScanned(player, doScan)) {
                     list = ScanManager.getScanAspects(doScan, level);
                  }
                  break;
               case ENTITY:
                  if (doScan.entity instanceof ItemEntity itemEntity) {
                     itemStack = itemEntity.getItem();
                  } else {
                     s = doScan.entity.getDisplayName();
                  }

                  if (ScanManager.hasBeenScanned(player, doScan)) {
                     list = ScanManager.getScanAspects(doScan, level);
                  }
                  break;
               case PHENOMENA:
                  s = getPhenomenaName(doScan.phenomena);
                  if (ScanManager.hasBeenScanned(player, doScan)) {
                     BlockPos energizedPos = doScan.phenomena.startsWith(NodeEnergizedBlockEntity.SCAN_PREFIX)
                        ? NodeEnergizedBlockEntity.parseScanId(level, doScan.phenomena.substring(NodeEnergizedBlockEntity.SCAN_PREFIX.length()))
                        : null;
                     if (energizedPos != null && level.getBlockEntity(energizedPos) instanceof NodeEnergizedBlockEntity energized) {
                        // Its output a tick, as goggles show it (TC4 RenderEventHandler: IAspectContainer.getAspects).
                        list = energized.getAspects();
                     } else if (doScan.phenomena.startsWith("NODE")) {
                        INode node = ScanManager.getNode(level, doScan.phenomena.substring(4));
                        if (node != null) {
                           list = node.getAspects();
                           pose.pushPose();
                           String s2 = getNodeTypeText(node);
                           graphics.drawString(font, s2, -font.width(s2) / 2, -52, 15642134);
                           pose.popPose();
                        } else {
                           list = ScanManager.getScanAspects(doScan, level);
                        }
                     } else {
                        list = ScanManager.getScanAspects(doScan, level);
                     }
                  }
            }

            if (!itemStack.isEmpty()) {
               s = itemStack.getHoverName();
            }

            if (list != null) {
               int n9 = 0;
               int n10 = 0;
               int size = list.size();
               int n11 = Math.min(5, size) * 8;

               for (Aspect aspect : list.getAspectsSorted()) {
                  pose.pushPose();
                  pose.scale(1.5F, 1.5F, 1.0F);
                  float x = -n11 + n9 * 16;
                  float y = -8 + n10 * 16;
                  UtilsFX.drawTag(graphics, (int)x, (int)y, aspect, 0.0F, 0, 0.01F, 1, 1.0F);
                  int amount = list.getAmount(aspect);
                  if (amount > 0) {
                     pose.pushPose();
                     pose.translate(0.0F, 0.0F, 10.0F);
                     String amountStr = String.valueOf(amount);
                     pose.pushPose();
                     pose.translate(x + 16.0F, y + 16.0F, 0.0F);
                     pose.scale(0.5F, 0.5F, 1.0F);
                     int textWidth = font.width(amountStr);
                     graphics.drawString(font, amountStr, -textWidth, -8, 16777215, true);
                     pose.popPose();
                     pose.popPose();
                  }

                  pose.popPose();
                  if (++n9 >= 5 - n10) {
                     n9 = 0;
                     n10++;
                     size -= 5 - n10;
                     n11 = Math.min(5 - n10, size) * 8;
                  }
               }
            }

            this.renderThaumometerTargetName(graphics, font, s);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderThaumometerTargetName(GuiGraphics graphics, Font font, Component name) {
      List<FormattedCharSequence> lines = font.split(name, 110);
      if (!lines.isEmpty()) {
         int visibleLines = Math.min(2, lines.size());
         float scale = visibleLines > 1 ? 0.9F : 1.0F;
         int lineHeight = 9;
         int startY = -((visibleLines - 1) * lineHeight) / 2;
         PoseStack pose = graphics.pose();
         pose.pushPose();
         pose.translate(1.0F, -40.0F, 200.0F);
         pose.scale(scale, scale, 1.0F);

         for (int i = 0; i < visibleLines; i++) {
            FormattedCharSequence line = lines.get(i);
            graphics.drawString(font, line, -font.width(line) / 2, startY + i * lineHeight, 16777215, false);
         }

         pose.popPose();
      }
   }
}
