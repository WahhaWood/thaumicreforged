package thaumcraft.event.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ScanResult;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity;
import thaumcraft.block.jar.standart.JarBlockItem;
import thaumcraft.block.table.ResearchRecipeScreen;
import thaumcraft.event.common.EssenceHandler;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModMobEffect;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.relics.ItemSanityChecker;
import thaumcraft.item.relics.ItemThaumometer;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.foci.ItemFocusTrade;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.misc.PacketScanInventory;
import thaumcraft.particles.fx.ParticleUtils;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ScanManager;
import thaumcraft.util.ItemStackData;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class ClientTickEventsFML {
   private static final int SHADER_DESAT = 0;
   private static final int SHADER_BLUR = 1;
   private static final int SHADER_HUNGER = 2;
   private static final int SHADER_SUNSCORNED = 3;
   private static final Logger LOGGER = LogManager.getLogger();
   public static final ResourceLocation HUD_TEXTURE = Thaumcraft.location("textures/screen/hud.png");
   ItemStack lastItem;
   int lastCount;
   int lastInvChange;
   public static final ResourceLocation[] SHADER_RESOURCES = new ResourceLocation[]{
      ResourceLocation.withDefaultNamespace("shaders/post/desaturatetc.json"),
      ResourceLocation.withDefaultNamespace("shaders/post/blurredvision.json"),
      ResourceLocation.withDefaultNamespace("shaders/post/hunger.json"),
      ResourceLocation.withDefaultNamespace("shaders/post/sunscorned.json")
   };
   public static final ClientTickEventsFML INSTANCE = new ClientTickEventsFML();
   public int tickCount = 0;
   public static int warpVignette = 0;

   /**
    * Предел непрозрачности варп-виньетки — то самое «лимиты поставить».
    *
    * <p>У текстуры виньетки центр ЧЁРНЫЙ, а углы светлые. При альфе 1.0 это
    * заливало центр экрана наглухо и играть было нельзя. Здесь непрозрачность
    * ограничена, поэтому центр лишь притемняется, а фиолетовая кайма остаётся
    * видна. В TC4 такого предела нет (там эффект мультипликативный), это
    * отступление по просьбе игрока.
    */
   private static final float MAX_WARP_VIGNETTE = 0.6F;
   private boolean checkedDate = false;
   private long nextSyncTime = 0L;
   private int activePostShader = -1;
   private final boolean[] disabledPostShaders = new boolean[SHADER_RESOURCES.length];
   private CameraType lastCameraType = null;
   private boolean lastScreenOpen = false;
   private TextureTarget blurredVisionTarget;
   private final DecimalFormat decimalFormatter = new DecimalFormat("#######.##");
   private final Map<Integer, AspectList> oldVisValues = new HashMap<>();
   private ItemStack lastTradeItem = ItemStack.EMPTY;
   private int lastTradeCount = 0;
   private int lastInvTimesChanged = -1;
   private static final int SCAN_TICKS = 50;
   private static Slot currentSlot;
   private static int ticksHovered;
   private static ScanResult currentScan;

   public static void register() {
      NeoForge.EVENT_BUS.register(INSTANCE);
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void playerTick(Pre event) {
      Minecraft mc = Minecraft.getInstance();
      if (event.getEntity() != null && mc.player != null && event.getEntity().level().isClientSide) {
         if (event.getEntity().getId() == mc.player.getId()) {
            try {
               this.updateShadersAndFog(mc.player, mc);
            } catch (Exception var4) {
            }
         }
      }
   }

   private void tickEssentiaSourceFx(Level level) {
      if (!EssenceHandler.sourceFX.isEmpty()) {
         String[] keys = EssenceHandler.sourceFX.keySet().toArray(new String[0]);

         for (String key : keys) {
            EssenceHandler.EssenceSourceFX fx = EssenceHandler.sourceFX.get(key);
            if (fx != null) {
               if (fx.ticks <= 0) {
                  EssenceHandler.sourceFX.remove(key);
               } else {
                  BlockPos start = fx.start;
                  BlockPos end = fx.end;
                  int mod = 0;
                  BlockEntity be = level.getBlockEntity(start);
                  if (be instanceof RunicMatrixBlockEntity) {
                     mod = -1;
                  }

                  float scale;
                  int fxTick;
                  if (fx.ticks > 5) {
                     scale = 1.0F;
                     fxTick = this.tickCount;
                  } else {
                     scale = fx.ticks * fx.ticks / 25.0F;
                     fxTick = this.tickCount - (5 - fx.ticks);
                  }

                  Thaumcraft.setup
                     .essenceTrailFx(level, end.getX(), end.getY(), end.getZ(), start.getX(), mod + start.getY(), start.getZ(), fxTick, fx.color, scale);
                  fx.ticks--;
                  if (fx.ticks <= 0) {
                     EssenceHandler.sourceFX.remove(key);
                  } else {
                     EssenceHandler.sourceFX.put(key, fx);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void clientWorldTickPre(net.neoforged.neoforge.client.event.ClientTickEvent.Pre event) {
      this.tickCount++;
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         this.tickEssentiaSourceFx(mc.level);
      }

      if (mc.level != null && !this.checkedDate) {
         this.checkHalloween();
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void clientWorldTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         ItemStack carried = mc.player.containerMenu.getCarried();
         if (!(carried.getItem() instanceof ItemThaumometer)) {
            resetScan();
         } else {
            if (mc.screen instanceof AbstractContainerScreen<?> screen) {
               Slot hoveredSlot = screen.getSlotUnderMouse();
               if (hoveredSlot != null && hoveredSlot.hasItem() && hoveredSlot != currentSlot) {
                  resetScan();
                  currentSlot = hoveredSlot;
                  ScanResult potentialScan = ScanResult.ofItem(currentSlot.getItem());
                  if (ScanManager.isValidScanTarget(mc.player, potentialScan, "@")) {
                     ResearchRecipeScreen.cacheScannedStack(currentSlot.getItem());
                     currentScan = potentialScan;
                  } else {
                     currentScan = null;
                  }
               } else if (hoveredSlot == null) {
                  resetScan();
               }

               if (currentSlot != null && currentScan != null) {
                  ticksHovered++;
                  if (ticksHovered > 5 && ticksHovered % 2 == 0) {
                     float pitch = 0.45F + mc.level.random.nextFloat() * 0.1F;
                     mc.level
                        .playLocalSound(
                           mc.player.getX(),
                           mc.player.getY(),
                           mc.player.getZ(),
                           (SoundEvent)ModSounds.CAMERA_TICKS.get(),
                           SoundSource.PLAYERS,
                           0.2F,
                           pitch,
                           false
                        );
                  }

                  if (ticksHovered >= 50) {
                     int slotId = screen.getMenu().slots.indexOf(currentSlot);
                     if (slotId >= 0) {
                        PacketHandler.sendToServer(new PacketScanInventory(slotId));
                     }

                     // NB: сервер выполняет completeScan молча — при сбросе
                     // (нет тэгов / не открыты родители аспектов) клиенту не
                     // приходит ничего. Прогоняем ту же проверку локально,
                     // чтобы игрок увидел причину, как в мировом скане.
                     ScanManager.validScan(ScanManager.getScanAspects(currentScan, mc.level), mc.player);

                     ticksHovered = 0;
                     currentScan = null;
                  }
               }
            } else {
               resetScan();
            }
         }
      }
   }

   @SubscribeEvent
   public void clientTick(Post event) {
   }

   @OnlyIn(Dist.CLIENT)
   private static void renderScanningProgress(GuiGraphics graphics, int mouseX, int mouseY, float progress) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(0.0F, 0.0F, 300.0F);
      String text = ChatFormatting.GOLD + Component.translatable("tcinventoryscan.scanning").getString();
      if (progress >= 0.75F) {
         text = text + "...";
      } else if (progress >= 0.5F) {
         text = text + "..";
      } else if (progress >= 0.25F) {
         text = text + ".";
      }

      graphics.drawString(Minecraft.getInstance().font, text, mouseX + 8, mouseY - 16, 16777215, true);
      pose.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   private static void renderAspects(GuiGraphics graphics, int mouseX, int mouseY, ItemStack stack) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         ScanResult scan = ScanResult.ofItem(stack);
         if (ScanManager.hasBeenScanned(mc.player, scan)) {
            AspectList aspects = ThaumcraftCraftingManager.getObjectTags(stack);
            aspects = ThaumcraftCraftingManager.getBonusTags(stack, aspects);
            if (aspects.size() > 0) {
               PoseStack pose = graphics.pose();
               pose.pushPose();
               pose.translate(0.0F, 0.0F, 300.0F);
               int idx = 0;

               for (Aspect aspect : aspects.getAspectsSorted()) {
                  if (aspect != null) {
                     int amount = aspects.getAmount(aspect);
                     int xPos = mouseX + 8 + idx * 18;
                     int yPos = mouseY - 34;
                     UtilsFX.drawTag(graphics, xPos, yPos, aspect, amount, 0, 0.0F);
                     idx++;
                  }
               }

               pose.popPose();
            }
         }
      }
   }

   private static void resetScan() {
      currentSlot = null;
      ticksHovered = 0;
      currentScan = null;
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onRenderGuiOverlay(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post event) {
      if (event.getName().equals(VanillaGuiLayers.HOTBAR) || event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && !mc.options.hideGui && mc.isWindowActive() && mc.screen == null) {
            Player player = mc.player;
            long now = System.currentTimeMillis();
            GuiGraphics gg = event.getGuiGraphics();
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

            try {
               if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
                  this.renderWarpVignette(gg, player, partialTick);
                  this.renderBlurredOverlay(gg, player, partialTick);
                  this.renderHuds(gg, player, now, partialTick);
               } else if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)
                  && !player.getAbilities().instabuild
                  && Thaumcraft.instance.runicEventHandler.hasRunicCharge(player)) {
                  this.renderRunicArmorBar(gg, player);
               }
            } catch (Exception var9) {
               LOGGER.warn("Error rendering Thaumcraft HUD: " + var9.getMessage());
            }
         }
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void onScreenRenderPost(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         boolean showTags = Screen.hasShiftDown() && !Config.showTags || !Screen.hasShiftDown() && Config.showTags;
         if (event.getScreen() instanceof AbstractContainerScreen<?> container && showTags && !mc.mouseHandler.isMouseGrabbed()) {
            this.renderAspectsInGui(container, mc.player, event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
         }

         this.renderBlurredOverlay(event.getGuiGraphics(), mc.player, 0.0F);
         if (currentSlot != null && mc.player != null && mc.player.containerMenu.getCarried().getItem() instanceof ItemThaumometer) {
            GuiGraphics graphics = event.getGuiGraphics();
            int mx = event.getMouseX();
            int my = event.getMouseY();
            if (currentScan != null) {
               float progress = ticksHovered / 50.0F;
               renderScanningProgress(graphics, mx, my, progress);
            }

            renderAspects(graphics, mx, my, currentSlot.getItem());
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void updateShadersAndFog(Player player, Minecraft mc) {
      int desiredShader = -1;
      if (player.hasEffect(ModMobEffect.DEATH_GAZE)) {
         desiredShader = 0;
         warpVignette = 10;
      } else if (player.hasEffect(ModMobEffect.BLURRED)) {
         desiredShader = 1;
      } else if (player.hasEffect(ModMobEffect.UNNATURAL_HUNGER)) {
         desiredShader = 2;
      } else if (player.hasEffect(ModMobEffect.SUN_SCORNED)) {
         desiredShader = 3;
      }

      CameraType cameraType = mc.options.getCameraType();
      boolean screenOpen = mc.screen != null;
      boolean cameraChanged = this.lastCameraType != cameraType;
      boolean screenStateChanged = this.lastScreenOpen != screenOpen;
      this.lastCameraType = cameraType;
      this.lastScreenOpen = screenOpen;
      if (screenOpen) {
         desiredShader = -1;
      }

      if (desiredShader >= 0 && this.disabledPostShaders[desiredShader]) {
         desiredShader = -1;
      }

      boolean effectLoaded = mc.gameRenderer.currentEffect() != null;
      boolean forceReload = desiredShader >= 0 && (cameraChanged || screenStateChanged);
      if (desiredShader < 0) {
         if (this.activePostShader >= 0 || effectLoaded) {
            mc.gameRenderer.shutdownEffect();
            this.activePostShader = -1;
         }
      } else if (desiredShader != this.activePostShader || !effectLoaded || forceReload) {
         if (effectLoaded) {
            mc.gameRenderer.shutdownEffect();
         }

         ResourceLocation shaderResource = SHADER_RESOURCES[desiredShader];

         try {
            mc.gameRenderer.loadEffect(shaderResource);
            this.activePostShader = mc.gameRenderer.currentEffect() != null ? desiredShader : -1;
         } catch (Exception var12) {
            this.disabledPostShaders[desiredShader] = true;
            this.activePostShader = -1;
            LOGGER.error("Failed to load Thaumcraft post shader {}. Disabling it for the rest of the session.", shaderResource, var12);
            mc.gameRenderer.shutdownEffect();
         }
      }

      if (warpVignette > 0) {
         warpVignette--;
         RenderEventHandler.targetBrightness = 0.0F;
      } else {
         RenderEventHandler.targetBrightness = 1.0F;
      }

      if (RenderEventHandler.fogFiddled) {
         if (RenderEventHandler.fogDuration < 100) {
            RenderEventHandler.fogTarget = 0.1F * (RenderEventHandler.fogDuration / 100.0F);
         } else if (RenderEventHandler.fogTarget < 0.1F) {
            RenderEventHandler.fogTarget += 0.001F;
         }

         RenderEventHandler.fogDuration--;
         if (RenderEventHandler.fogDuration < 0) {
            RenderEventHandler.fogFiddled = false;
         }
      }
   }

   private void checkHalloween() {
      this.checkedDate = true;
      Calendar currentDate = Calendar.getInstance();
      if (currentDate.get(2) == 9 && currentDate.get(5) == 31) {
         Thaumcraft.isHalloween = true;
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderHuds(GuiGraphics gg, Player player, long now, float partialTick) {
      ItemStack chest = (ItemStack)player.getInventory().armor.get(2);
      if (!chest.isEmpty() && chest.getItem() == ModItems.HOVER_HARNESS.get()) {
         this.renderHoverHUD(gg, player, now, chest);
      }

      for (ItemStack heldStack : player.getHandSlots()) {
         if (!heldStack.isEmpty()) {
            if (heldStack.getItem() instanceof ItemWandCasting wand) {
               this.renderCastingWandHud(gg, player, now, heldStack, wand);
               break;
            }

            if (heldStack.getItem() instanceof ItemSanityChecker) {
               this.renderSanityHud(gg, player);
               break;
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderWarpVignette(GuiGraphics graphics, Player player, float partialTick) {
      float target = 1.0F - RenderEventHandler.targetBrightness;
      // В оригинале виньетка рисуется мультипликативно (glBlendFunc(0, SRC_COLOR),
      // серый glColor), поэтому затемнение никогда не заливало экран наглухо.
      // У нас стоял обычный альфа-бленд с фиолетовым тинтом, а у самой текстуры
      // ЦЕНТР ЧЁРНЫЙ: при альфе, доходящей до 1.0, экран становился чёрным с
      // фиолетовой каймой — «черно-фиолетовый эффект». Возвращаемся к
      // мультипликативному наложению и дополнительно ограничиваем затемнение,
      // чтобы картинку всегда было видно.
      RenderEventHandler.prevVignetteBrightness += (target - RenderEventHandler.prevVignetteBrightness) * 0.01F;
      float alpha = Mth.clamp(RenderEventHandler.prevVignetteBrightness, 0.0F, MAX_WARP_VIGNETTE);
      if (alpha > 0.005F) {
         Minecraft mc = Minecraft.getInstance();
         float width = mc.getWindow().getGuiScaledWidth();
         float height = mc.getWindow().getGuiScaledHeight();
         Matrix4f matrix = graphics.pose().last().pose();

         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.setShaderTexture(0, RenderEventHandler.vignetteTexPath);
         // Альфа-наложение с фиолетовым тинтом — так эффект и выглядел:
         // фиолетовая кайма и затемнённый центр. Мультипликативный вариант
         // (как в 1.7.10) здесь не подходит: у текстуры ЦЕНТР ЧЁРНЫЙ, поэтому
         // умножение гасило центр в ноль и убирало фиолетовый совсем.
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(0.85F, 0.25F, 0.95F, alpha);
         drawFullscreenQuad(matrix, 0.0F, 0.0F, width, height);

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderBlurredOverlay(GuiGraphics graphics, Player player, float partialTick) {
      if (player != null && player.hasEffect(ModMobEffect.BLURRED)) {
         Minecraft mc = Minecraft.getInstance();
         graphics.flush();
         RenderTarget mainTarget = mc.getMainRenderTarget();
         TextureTarget blurTarget = this.getBlurredVisionTarget(mc, mainTarget.width, mainTarget.height);
         blurTarget.bindWrite(true);
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableBlend();
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.setShaderTexture(0, mainTarget.getColorTextureId());
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         drawFullscreenQuad(graphics.pose().last().pose(), 0.0F, 0.0F, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
         mainTarget.bindWrite(false);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.setShaderTexture(0, blurTarget.getColorTextureId());
         float[][] passes = new float[][]{
            {0.0F, 0.0F, 0.2F},
            {-1.5F, 0.0F, 0.11F},
            {1.5F, 0.0F, 0.11F},
            {0.0F, -1.5F, 0.11F},
            {0.0F, 1.5F, 0.11F},
            {-1.2F, -1.2F, 0.08F},
            {1.2F, -1.2F, 0.08F},
            {-1.2F, 1.2F, 0.08F},
            {1.2F, 1.2F, 0.08F},
            {-2.8F, 0.0F, 0.06F},
            {2.8F, 0.0F, 0.06F},
            {0.0F, -2.8F, 0.06F},
            {0.0F, 2.8F, 0.06F},
            {-2.0F, -2.0F, 0.045F},
            {2.0F, -2.0F, 0.045F},
            {-2.0F, 2.0F, 0.045F},
            {2.0F, 2.0F, 0.045F}
         };
         Matrix4f matrix = graphics.pose().last().pose();
         float width = mc.getWindow().getGuiScaledWidth();
         float height = mc.getWindow().getGuiScaledHeight();

         for (float[] pass : passes) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, pass[2]);
            drawFullscreenQuad(matrix, pass[0], pass[1], width, height);
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         graphics.flush();
      }
   }

   @OnlyIn(Dist.CLIENT)
   private TextureTarget getBlurredVisionTarget(Minecraft mc, int width, int height) {
      if (this.blurredVisionTarget == null) {
         this.blurredVisionTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
      } else if (this.blurredVisionTarget.width != width || this.blurredVisionTarget.height != height) {
         this.blurredVisionTarget.resize(width, height, Minecraft.ON_OSX);
      }

      return this.blurredVisionTarget;
   }

   private static void drawFullscreenQuad(Matrix4f matrix, float xOffset, float yOffset, float width, float height) {
      BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      builder.addVertex(matrix, xOffset, height + yOffset, 0.0F).setUv(0.0F, 1.0F);
      builder.addVertex(matrix, width + xOffset, height + yOffset, 0.0F).setUv(1.0F, 1.0F);
      builder.addVertex(matrix, width + xOffset, yOffset, 0.0F).setUv(1.0F, 0.0F);
      builder.addVertex(matrix, xOffset, yOffset, 0.0F).setUv(0.0F, 0.0F);
      BufferUploader.drawWithShader(builder.buildOrThrow());
   }

   @OnlyIn(Dist.CLIENT)
   private void renderSanityHud(GuiGraphics gg, Player player) {
      gg.pose().pushPose();

      try {
         this.setupRenderState();
         gg.pose().translate(0.0F, 0.0F, 200.0F);
         gg.blit(HUD_TEXTURE, 1, 1, 152, 0, 20, 76);
         float warpTotal = PlayerKnowledge.of(player).getWarpTotal();
         int warpPerm = PlayerKnowledge.of(player).getWarpPerm();
         int warpSticky = PlayerKnowledge.of(player).getWarpSticky();
         int warpTemp = PlayerKnowledge.of(player).getWarpTemp();
         float scale = 1.0F;
         if (warpTotal > 100.0F) {
            scale = 100.0F / warpTotal;
            warpTotal = 100.0F;
         }

         int base = (int)((100.0F - warpTotal) / 100.0F * 48.0F);
         int t = (int)(warpTemp / 100.0F * 48.0F * scale);
         int s = (int)(warpSticky / 100.0F * 48.0F * scale);
         if (warpTemp > 0) {
            RenderSystem.setShaderColor(1.0F, 0.5F, 1.0F, 1.0F);
            gg.blit(HUD_TEXTURE, 7, 21 + base, 200, base, 8, t + base);
         }

         if (warpSticky > 0) {
            RenderSystem.setShaderColor(0.75F, 0.0F, 0.75F, 1.0F);
            gg.blit(HUD_TEXTURE, 7, 21 + t + base, 200, t + base, 8, t + s + base);
         }

         if (warpPerm > 0) {
            RenderSystem.setShaderColor(0.5F, 0.0F, 0.5F, 1.0F);
            gg.blit(HUD_TEXTURE, 7, 21 + t + s + base, 200, t + s + base, 8, 48);
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         gg.blit(HUD_TEXTURE, 1, 1, 176, 0, 20, 76);
         if (warpTotal >= 100.0F) {
            gg.blit(HUD_TEXTURE, 1, 1, 216, 0, 20, 16);
         }

         this.teardownRenderState();
      } finally {
         gg.pose().popPose();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderCastingWandHud(GuiGraphics gg, Player player, long now, ItemStack wandStack, ItemWandCasting wand) {
      Minecraft mc = Minecraft.getInstance();
      int screenHeight = mc.getWindow().getGuiScaledHeight();
      int selectedSlot = player.getInventory().selected;
      if (!this.oldVisValues.containsKey(selectedSlot) || this.nextSyncTime <= now) {
         this.oldVisValues.put(selectedSlot, wand.getAllVis(wandStack));
         this.nextSyncTime = now + 1000L;
      }

      gg.pose().pushPose();

      try {
         this.setupRenderState();
         gg.pose().translate(0.0F, Config.dialBottom ? screenHeight - 32 : 0, 200.0F);
         gg.pose().pushPose();
         gg.pose().scale(0.5F, 0.5F, 1.0F);
         gg.blit(HUD_TEXTURE, 0, 0, 0, 0, 64, 64);
         gg.pose().popPose();
         gg.pose().translate(16.0F, 16.0F, 0.0F);
         int maxVis = wand.getMaxVis(wandStack);
         ItemFocusBasic focus = wand.getFocus(wandStack);
         ItemStack focusItem = wand.getFocusItem(wandStack);
         AspectList vis = wand.getAllVis(wandStack);
         int idx = 0;

         for (Aspect aspect : vis.getAspects()) {
            if (aspect != null) {
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               int amount = vis.getAmount(aspect);
               int barHeight = (int)(30.0F * amount / maxVis);
               gg.pose().pushPose();
               if (!Config.dialBottom) {
                  gg.pose().mulPose(Axis.ZP.rotationDegrees(90.0F));
               }

               gg.pose().mulPose(Axis.ZP.rotationDegrees(-15.0F + idx * 24.0F));
               gg.pose().translate(0.0F, -32.0F, 0.0F);
               gg.pose().scale(0.5F, 0.5F, 1.0F);
               gg.pose().pushPose();
               int c = aspect.getColor();
               RenderSystem.setShaderColor((c >> 16 & 0xFF) / 255.0F, (c >> 8 & 0xFF) / 255.0F, (c & 0xFF) / 255.0F, 0.8F);
               gg.blit(HUD_TEXTURE, -4, 35 - barHeight, 104, 0, 8, barHeight);
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               gg.pose().popPose();
               gg.blit(HUD_TEXTURE, -8, -3, 72, 0, 16, 42);
               int offset = 0;
               if (focus != null && focus.getVisCost(focusItem).getAmount(aspect) > 0) {
                  gg.blit(HUD_TEXTURE, -4, -8, 136, 0, 8, 8);
                  offset = 8;
               }

               AspectList old = this.oldVisValues.get(selectedSlot);
               if (old != null) {
                  if (old.getAmount(aspect) > amount) {
                     gg.blit(HUD_TEXTURE, -4, -8 - offset, 128, 0, 8, 8);
                  } else if (old.getAmount(aspect) < amount) {
                     gg.blit(HUD_TEXTURE, -4, -8 - offset, 120, 0, 8, 8);
                  }
               }

               if (player.isCrouching()) {
                  this.drawWandText(gg, mc, amount, focus, focusItem, aspect, wand, wandStack, player);
               }

               gg.pose().popPose();
               idx++;
            }
         }

         if (focus != null) {
            ItemStack picked = ItemStack.EMPTY;
            if (focus instanceof ItemFocusTrade trade) {
               ItemStack pb = trade.getPickedBlock(wandStack);
               if (pb != null && !pb.isEmpty()) {
                  picked = pb;
               }
            }

            if (!picked.isEmpty()) {
               this.renderWandTradeHud(gg, player, now, picked);
            } else {
               gg.pose().pushPose();
               gg.pose().translate(-24.0F, -24.0F, 90.0F);
               gg.renderItem(wand.getFocusItem(wandStack), 16, 16);
               gg.pose().popPose();
               float cooldown = 0.0F;
               if (cooldown > 0.0F) {
                  gg.pose().pushPose();
                  gg.pose().translate(0.0F, 0.0F, 150.0F);
                  gg.pose().scale(0.5F, 0.5F, 1.0F);
                  String s = this.decimalFormatter.format(cooldown) + "s";
                  gg.drawString(mc.font, s, -(mc.font.width(s) / 2), -4, 16777215, false);
                  gg.pose().popPose();
               }
            }
         }

         this.teardownRenderState();
      } finally {
         gg.pose().popPose();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderWandTradeHud(GuiGraphics gg, Player player, long now, ItemStack lastItem) {
      if (lastItem != null && !lastItem.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         int invChange = player.getInventory().getTimesChanged();
         boolean samePicked = this.lastItem != null && ItemStack.isSameItem(this.lastItem, lastItem);
         int count = this.lastCount;
         player.getInventory().setChanged();
         if (invChange != this.lastInvChange || !samePicked) {
            count = 0;

            for (ItemStack st : player.getInventory().items) {
               if (!st.isEmpty() && st.getItem() == lastItem.getItem() && st.getDamageValue() == lastItem.getDamageValue()) {
                  count += st.getCount();
               }
            }

            this.lastItem = lastItem.copy();
            this.lastInvChange = invChange;
         }

         this.lastCount = count;
         gg.pose().pushPose();

         try {
            RenderSystem.enableDepthTest();

            try {
               gg.renderItem(lastItem, -8, -8);
            } catch (Exception var17) {
            }

            gg.pose().pushPose();
            String s = Integer.toString(count);
            int w = mc.font.width(s);
            gg.pose().translate(0.0F, -9, 500.0F);
            gg.pose().scale(0.5F, 0.5F, 1.0F);

            for (int j = -1; j <= 1; j++) {
               for (int k = -1; k <= 1; k++) {
                  if ((j == 0 || k == 0) && (j != 0 || k != 0)) {
                     gg.drawString(mc.font, s, j + 16 - w, k + 24, 0, false);
                  }
               }
            }

            gg.drawString(mc.font, s, 16 - w, 24, 16777215, false);
            gg.pose().popPose();
         } finally {
            gg.pose().popPose();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void drawWandText(
      GuiGraphics gg,
      Minecraft mc,
      int amount,
      ItemFocusBasic focus,
      ItemStack focusItem,
      Aspect aspect,
      ItemWandCasting wand,
      ItemStack wandStack,
      Player player
   ) {
      gg.pose().pushPose();
      gg.pose().mulPose(Axis.ZP.rotationDegrees(-90.0F));
      gg.drawString(mc.font, String.valueOf(amount / 100), -32, -4, 16777215, false);
      gg.pose().popPose();
      if (focus != null && focus.getVisCost(focusItem).getAmount(aspect) > 0) {
         float mod = wand.getConsumptionModifier(wandStack, player, aspect, false);
         String cost = this.decimalFormatter.format(focus.getVisCost(focusItem).getAmount(aspect) * mod / 100.0F);
         gg.pose().pushPose();
         gg.pose().mulPose(Axis.ZP.rotationDegrees(-90.0F));
         gg.drawString(mc.font, cost, 8, -4, 16777215, false);
         gg.pose().popPose();
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderRunicArmorBar(GuiGraphics gg, Player player) {
      Minecraft mc = Minecraft.getInstance();
      int w = mc.getWindow().getGuiScaledWidth();
      int h = mc.getWindow().getGuiScaledHeight();
      UUID playerId = player.getUUID();
      Integer[] info = Thaumcraft.instance.runicEventHandler.runicInfo.get(playerId);
      if (info != null) {
         float max = info[0].intValue();
         float charge = Thaumcraft.instance.runicEventHandler.getRunicCharge(player);
         if (!(max <= 0.0F) && !(charge <= 0.0F)) {
            gg.pose().pushPose();

            try {
               RenderSystem.disableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               gg.pose().translate(w / 2.0F - 91.0F, h - 39.0F, 200.0F);
               float pct = charge / max;
               int segments = (int)(pct * 10.0F);

               for (int i = 0; i < segments; i++) {
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  gg.blit(ParticleUtils.PARTICLE_TEXTURE, i * 8, 0, 160, 16, 9, 9);
                  gg.pose().pushPose();
                  gg.pose().scale(0.5F, 0.5F, 1.0F);
                  float a = Mth.sin(player.tickCount / 4.0F + i) * 0.4F + 0.6F;
                  RenderSystem.setShaderColor(1.0F, 0.75F, 0.24F, a);
                  gg.blit(ParticleUtils.PARTICLE_TEXTURE, i * 16, 0, i * 16, 96, 16, 16);
                  gg.pose().popPose();
               }

               RenderSystem.depthMask(true);
               RenderSystem.enableDepthTest();
               RenderSystem.disableBlend();
            } finally {
               gg.pose().popPose();
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderHoverHUD(GuiGraphics gg, Player player, long now, ItemStack chest) {
      Minecraft mc = Minecraft.getInstance();
      int h = mc.getWindow().getGuiScaledHeight();
      gg.pose().pushPose();

      try {
         this.setupRenderState();
         RenderSystem.depthMask(false);
         short energy = 0;
         CompoundTag chestTag = ItemStackData.getTag(chest);
         if (chestTag != null && chestTag.contains("jar")) {
            ItemStack jar = ItemStackData.loadStack(chestTag.getCompound("jar"));
            if (!jar.isEmpty() && jar.getItem() instanceof JarBlockItem jf && ItemStackData.hasTag(jar)) {
               AspectList al = jf.getAspects(jar);
               if (al != null && al.size() > 0) {
                  energy = (short)al.getAmount(Aspect.ENERGY);
               }
            }
         }

         int bar = Math.round(energy / 64.0F * 48.0F);
         gg.pose().translate(0.0F, 0.0F, 200.0F);
         RenderSystem.setShaderColor(0.0F, 1.0F, 0.75F, 1.0F);
         gg.blit(ParticleUtils.PARTICLE_TEXTURE, 6, h / 2 + 24 - bar, 224, 48 - bar, 8, bar);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         gg.blit(ParticleUtils.PARTICLE_TEXTURE, 5, h / 2 - 28, 240, 0, 10, 56);
         if (chestTag != null && chestTag.contains("hover") && chestTag.getByte("hover") == 1) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.66F);
            int frame = (int)(now % 700L / 50L);
            gg.blit(HUD_TEXTURE, 2, h / 2 - 43, 16 * frame, 32, 16, 16);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         }

         gg.renderItem(chest, 2, h / 2 - 43);
         RenderSystem.depthMask(true);
         this.teardownRenderState();
      } finally {
         gg.pose().popPose();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void renderAspectsInGui(AbstractContainerScreen<?> screen, Player player, GuiGraphics guiGraphics, int mouseX, int mouseY) {
      Minecraft mc = Minecraft.getInstance();
      int left = screen.getGuiLeft();
      int top = screen.getGuiTop();
      int ox = -8;
      int oy = -8;
      if (Thaumcraft.instance.aspectShift) {
         ox -= 8;
         oy -= 8;
      }

      ResourceLocation backTex = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/aspects/_back.png");
      ResourceLocation unknownTex = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/aspects/_unknown.png");
      RenderSystem.disableDepthTest();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      PoseStack pose = guiGraphics.pose();
      pose.pushPose();
      pose.translate(0.0F, 0.0F, 200.0F);

      for (int i = 0; i < screen.getMenu().slots.size(); i++) {
         Slot slot = (Slot)screen.getMenu().slots.get(i);
         if (slot.isActive() && this.isMouseOverSlot(slot, mouseX, mouseY, left, top) && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            int hash = ScanManager.generateItemHash(stack);
            String name = player.getGameProfile().getName();
            HashSet<String> scanned = PlayerKnowledge.of(player).getObjectsScanned();
            if (scanned != null && (scanned.contains("@" + hash) || scanned.contains("#" + hash))) {
               AspectList bonusTags = ThaumcraftCraftingManager.getBonusTags(stack, ThaumcraftCraftingManager.getObjectTags(stack));
               if (bonusTags != null && bonusTags.size() > 0) {
                  int idx = 0;

                  for (Aspect aspect : bonusTags.getAspectsSortedAmount()) {
                     if (aspect != null) {
                        int x = mouseX + 17 + idx * 18;
                        int y = mouseY + 7 - 33;
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        pose.pushPose();
                        pose.translate(x + ox - 2, y + oy - 2, 0.0F);
                        pose.scale(1.25F, 1.25F, 1.0F);
                        guiGraphics.blit(backTex, 0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
                        pose.popPose();
                        if (PlayerKnowledge.of(player).hasDiscoveredAspect(aspect)) {
                           UtilsFX.drawTag(guiGraphics, x + ox, y + oy, aspect, bonusTags.getAmount(aspect), 0, 0.0F);
                        } else {
                           guiGraphics.blit(unknownTex, x + ox, y + oy, 0.0F, 0.0F, 16, 16, 16, 16);
                        }

                        idx++;
                     }
                  }
               }
            }
            break;
         }
      }

      pose.popPose();
      RenderSystem.disableBlend();
      RenderSystem.enableDepthTest();
   }

   private void setupRenderState() {
      RenderSystem.disableDepthTest();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
   }

   private void teardownRenderState() {
      RenderSystem.disableBlend();
      RenderSystem.enableDepthTest();
   }

   private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY, int guiLeft, int guiTop) {
      mouseX -= guiLeft;
      mouseY -= guiTop;
      return mouseX >= slot.x - 1 && mouseX < slot.x + 16 + 1 && mouseY >= slot.y - 1 && mouseY < slot.y + 16 + 1;
   }
}
