package thaumcraft;

import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.api.wands.WandTriggerRegistry;
import thaumcraft.block.advanced_node_infuser.AdvancedNodeInfuserRenderer;
import thaumcraft.block.advanced_node_infuser.AdvancedNodeInfuserScreen;
import thaumcraft.block.airy.aura_node.AuraNodeRenderer;
import thaumcraft.block.airy.energized_aura.NodeEnergizedBlockEntityRenderer;
import thaumcraft.block.alchemy_furnace.AlchemyFurnaceScreen;
import thaumcraft.block.arcane_alembic.ArcaneAlembicBlockEntityRenderer;
import thaumcraft.block.arcane_pedestal.PedestalBlockEntityRenderer;
import thaumcraft.block.arcane_workbench.ArcaneWorkbenchBlockEntityRenderer;
import thaumcraft.block.arcane_workbench.ArcaneWorkbenchScreen;
import thaumcraft.block.banner.BannerRenderer;
import thaumcraft.block.bellows.BellowsRenderer;
import thaumcraft.block.bore.render.BoreBaseRenderer;
import thaumcraft.block.bore.render.BoreRenderer;
import thaumcraft.block.bore.render.BoreScreen;
import thaumcraft.block.crucible.CrucibleRenderer;
import thaumcraft.block.crystal.CrystalClusterRenderer;
import thaumcraft.block.deconstruction_table.render.DeconstructionTableBlockEntityRenderer;
import thaumcraft.block.deconstruction_table.render.DeconstructionTableScreen;
import thaumcraft.block.eccence_reservoir.render.TileEssenceReservoirRenderer;
import thaumcraft.block.eldritch.altar.TileEldritchCapRenderer;
import thaumcraft.block.eldritch.lock.EldritchLockRenderer;
import thaumcraft.block.eldritch.nothing.EldritchNothingBlockEntityRenderer;
import thaumcraft.block.eldritch.obelisk.TileEldritchObeliskRenderer;
import thaumcraft.block.encharter.EnchanterBlockEntityRenderer;
import thaumcraft.block.encharter.EnchanterScreen;
import thaumcraft.block.ethereal_bloom.EtherealBloomBlockEntityRenderer;
import thaumcraft.block.flux_scrubber.FluxScrubberRenderer;
import thaumcraft.block.focal_manipulator.FocalManipulatorBlockEntityRenderer;
import thaumcraft.block.focal_manipulator.FocalManipulatorScreen;
import thaumcraft.block.hole.HoleBlockEntityRenderer;
import thaumcraft.block.infusion_claw.render.InfusionClawScreen;
import thaumcraft.block.infusion_matrix.RunicMatrixRenderer;
import thaumcraft.block.jar.render.BrainJarBlockEntityRenderer;
import thaumcraft.block.jar.render.JarBlockEntityRendered;
import thaumcraft.block.jar.render.NodeJarBlockEntityRenderer;
import thaumcraft.block.lamp.ArcaneLampRenderer;
import thaumcraft.block.lifter.LifterBlockEntityRenderer;
import thaumcraft.block.magic_box.MagicBoxScreen;
import thaumcraft.block.magnet.MagnetBlockEntityRenderer;
import thaumcraft.block.magnet.MobMagnetScreen;
import thaumcraft.block.mana_pod.ManaPodBlockEntityRenderer;
import thaumcraft.block.mirror.render.MirrorBlockEntityRenderer;
import thaumcraft.block.node_stabilizer.NodeStabilizerRenderer;
import thaumcraft.block.node_transducer.NodeTransducerRenderer;
import thaumcraft.block.opaque.render.WardedGlassModelLoader;
import thaumcraft.block.platform.render.PlatformBlockModelLoader;
import thaumcraft.block.spa.SpaScreen;
import thaumcraft.block.table.ResearchTableScreen;
import thaumcraft.block.thaumatorium.ThaumatoriumScreen;
import thaumcraft.block.tube.render.CentrifugeRenderer;
import thaumcraft.block.vis_charged_relay.VisChargeRelayRenderer;
import thaumcraft.block.vis_relay.VisRelayBlockEntityRenderer;
import thaumcraft.block.wand_charger.WandChargerRenderer;
import thaumcraft.block.wand_charger.WandChargerScreen;
import thaumcraft.block.wand_recharger.WandPedestalBlockEntityRenderer;
import thaumcraft.block.warded.render.WardedBlockEntityRenderer;
import thaumcraft.block.warded.render.WardedBlockModelLoader;
import thaumcraft.block.warp_gate.WarpGateScreen;
import thaumcraft.client.config.ThaumcraftClientConfig;
import thaumcraft.config.ThaumcraftCommonConfig;
import thaumcraft.data.condition.ModConditions;
import thaumcraft.dimension.outer_lands.OuterLandsEffects;
import thaumcraft.enchantment.EnchantmentManager;
import thaumcraft.entities.smart.golem.render.screen.GolemScreen;
import thaumcraft.entities.smart.pech.render.PechScreen;
import thaumcraft.entities.smart.travelling_trunk.render.TravelingTrunkScreen;
import thaumcraft.event.client.RenderEventHandler;
import thaumcraft.event.common.DimensionalShardDropHandler;
import thaumcraft.event.common.EventHandlerEntity;
import thaumcraft.event.common.EventHandlerRunic;
import thaumcraft.event.common.EventHandlerWorld;
import thaumcraft.event.common.GemArmorEvents;
import thaumcraft.event.common.KeyHandler;
import thaumcraft.event.common.ModEnchantmentEvents;
import thaumcraft.event.common.ServerTickEventsFML;
import thaumcraft.event.common.ThaumcraftCommonEvents;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModAspects;
import thaumcraft.init.registry.ModAttachments;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModCapabilities;
import thaumcraft.init.registry.ModCreativeTab;
import thaumcraft.init.registry.ModEnchantment;
import thaumcraft.init.registry.ModFeatures;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModFluids;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModLootFunctions;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.init.registry.ModMobEffect;
import thaumcraft.init.registry.ModRecipes;
import thaumcraft.init.registry.ModResearch;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.alumentum.AlumentumDispenseBehavior;
import thaumcraft.item.armor.hover_harness.HoverHarnessScreen;
import thaumcraft.item.block_talisman.ItemBlockTalisman;
import thaumcraft.item.cleansing_talisman.ItemCleansingTalisman;
import thaumcraft.item.compass_stone.ItemCompassStone;
import thaumcraft.item.equipment.blood_sword.ItemBloodSword;
import thaumcraft.item.equipment.ichor.IchorToolHelper;
import thaumcraft.item.focus_pouch.FocusPouchScreen;
import thaumcraft.item.golem_placer.ItemGolemPlacer;
import thaumcraft.item.hand_mirror.HandMirrorScreen;
import thaumcraft.item.ichor_pouch.IchorPouchScreen;
import thaumcraft.item.mob_aspect.NumericAspectHelper;
import thaumcraft.item.research_notes.ItemResearchNotes;
import thaumcraft.item.xp_talisman.ItemXPTalisman;
import thaumcraft.network.EventHandlerNetwork;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.playerdata.PacketSyncWarp;
import thaumcraft.network.playerdata.PacketWarpMessage;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.PlayerKnowledgeEventHandler;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ScanManager;
import thaumcraft.util.ItemStackData;

@Mod("thaumcraft")
public class Thaumcraft {
   public static final String MODID = "thaumcraft";
   public static CommonSetup setup = new CommonSetup();
   public static Thaumcraft instance;
   public static Logger LOGGER = LogManager.getLogger("THAUMCRAFT");
   public EventHandlerWorld worldEventHandler;
   public EventHandlerNetwork networkEventHandler;
   public ServerTickEventsFML serverTickEvents;
   public EventHandlerEntity entityEventHandler;
   public EventHandlerRunic runicEventHandler;
   public RenderEventHandler renderEventHandler;
   public static final Logger log = LogManager.getLogger("THAUMCRAFT");
   public static boolean isHalloween = false;
   public boolean aspectShift = false;

   public Thaumcraft(IEventBus bus) {
      instance = this;
      ModConditions.REGISTER.register(bus);
      ThaumcraftCommonConfig.register();
      ThaumcraftClientConfig.load();
      setup = net.neoforged.fml.loading.FMLEnvironment.dist.isClient() ? createClientSetup(bus) : new CommonSetup();
      ThaumcraftApi.setSetup(setup);
      ResearchManager.init(bus);
      ModAttachments.REGISTER.register(bus);
      ModBlocks.REGISTER.register(bus);
      ModItems.REGISTER.register(bus);
      ModBlockEntities.REGISTER.register(bus);
      ModEntities.init(bus);
      ModMenuTypes.init(bus);
      ModMobEffect.init(bus);
      ModSounds.init(bus);
      ModFluids.register(bus);
      ModRecipes.init(bus);
      ModEnchantment.REGISTER.register(bus);
      ModCreativeTab.init(bus);
      ModLootFunctions.init(bus);
      ModFeatures.init(bus);
      thaumcraft.world.structure.ModStructures.init(bus);
      thaumcraft.world.structure.ModStructurePieces.init(bus);
      bus.addListener(ModCapabilities::register);
      bus.addListener(this::onCommonSetup);
      bus.addListener(PacketHandler::init);
      bus.addListener(this::onLoadComplete);
      bus.addListener(ModEntities::registerAttributes);
      NeoForge.EVENT_BUS.register(new ThaumcraftCommonEvents());
      if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
         clientWiring(bus);
      }

      // development-only control bridge (exec/скриншоты/автотесты)
      thaumcraft.debug.bridge.DevBridge.start();
   }

   /** Клиентская проводка событий (на сервере классы не грузятся). */
   @OnlyIn(Dist.CLIENT)
   private static CommonSetup createClientSetup(IEventBus bus) {
      return new ClientSetup(bus);
   }

   @OnlyIn(Dist.CLIENT)
   private void clientWiring(IEventBus bus) {
      this.renderEventHandler = new RenderEventHandler();
      bus.addListener(this::registerDimensionSpecialEffects);
      bus.addListener(this::registerGeometryLoaders);
      bus.addListener(this::registerMenuScreens);
      bus.addListener(this::clientSetup);
      KeyHandler keyHandler = new KeyHandler();
      bus.addListener(keyHandler::registerKeyMappings);
      NeoForge.EVENT_BUS.register(keyHandler);
   }

   @OnlyIn(Dist.CLIENT)
   public void registerGeometryLoaders(RegisterGeometryLoaders event) {
      event.register(location("warded_glass_ctm"), new WardedGlassModelLoader());
      event.register(location("warded_block_mimic"), new WardedBlockModelLoader());
      event.register(location("platform_block_mimic"), new PlatformBlockModelLoader());
   }

   public void onCommonSetup(FMLCommonSetupEvent event) {
      ThaumcraftCommonConfig.syncToLegacy();
      setup.researchManager = new ResearchManager();
      this.entityEventHandler = new EventHandlerEntity();
      this.runicEventHandler = new EventHandlerRunic();
      this.networkEventHandler = new EventHandlerNetwork();
      this.worldEventHandler = new EventHandlerWorld();
      NeoForge.EVENT_BUS.register(this.entityEventHandler);
      NeoForge.EVENT_BUS.register(this.worldEventHandler);
      if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
         // EventHandlerRunic целиком клиентский (его @SubscribeEvent помечен @OnlyIn(CLIENT))
         NeoForge.EVENT_BUS.register(this.runicEventHandler);
      }
      NeoForge.EVENT_BUS.register(this.networkEventHandler);
      NeoForge.EVENT_BUS.register(new PlayerKnowledgeEventHandler());
      NeoForge.EVENT_BUS.register(new GemArmorEvents());
      NeoForge.EVENT_BUS.register(new ModEnchantmentEvents());
      NeoForge.EVENT_BUS.register(new DimensionalShardDropHandler());
      NeoForge.EVENT_BUS.register(new ServerTickEventsFML());
      // KAMI-броня: обработчики урона/прыжков отдельных частей.
      NeoForge.EVENT_BUS.register(ModItems.ICHOR_GEM_LEGGINGS.get());
      NeoForge.EVENT_BUS.register(ModItems.BLOOD_SWORD.get());
      ThaumcraftApi.registerScanEventhandler(new ScanManager());
      Config.save();
      setup.registerHandlers();
      Config.initBiomeInfo();
      EnchantmentManager.initEnchantmentData();
      setup.registerDisplayInformation();
      WandTriggerRegistry.registerWandBlockTrigger(0, Blocks.BOOKSHELF);
      WandTriggerRegistry.registerWandBlockTrigger(1, Blocks.CAULDRON);
      WandTriggerRegistry.registerWandBlockTrigger(2, Blocks.OBSIDIAN);
      WandTriggerRegistry.registerWandBlockTrigger(2, Blocks.NETHER_BRICKS);
      WandTriggerRegistry.registerWandBlockTrigger(2, Blocks.IRON_BARS);
      WandTriggerRegistry.registerWandBlockTrigger(3, (Block)ModBlocks.RUNIC_MATRIX.get());
      WandTriggerRegistry.registerWandBlockTrigger(4, Blocks.GLASS);
      WandTriggerRegistry.registerWandBlockTrigger(5, (Block)ModBlocks.ALCHEMICAL_CONSTRUCT.get());
      WandTriggerRegistry.registerWandBlockTrigger(6, (Block)ModBlocks.AURA_NODE.get());
      WandTriggerRegistry.registerWandBlockTrigger(6, (Block)ModBlocks.ELDRITCH_ALTAR.get());
      WandTriggerRegistry.registerWandBlockTrigger(7, (Block)ModBlocks.ALCHEMICAL_CONSTRUCT.get());
      WandTriggerRegistry.registerWandBlockTrigger(7, (Block)ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
      WandTriggerRegistry.registerWandBlockTrigger(8, (Block)ModBlocks.TABLE.get());
      DispenserBlock.registerBehavior((ItemLike)ModItems.ALUMENTUM.get(), new AlumentumDispenseBehavior());
      thaumcraft.compat.curios.CuriosCompat.registerCurios(event);
   }

   @OnlyIn(Dist.CLIENT)
   public void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
      event.register(ResourceLocation.fromNamespaceAndPath("thaumcraft", "outer_lands"), new OuterLandsEffects());
   }

   @OnlyIn(Dist.CLIENT)
   private static void setFluidRender(ModFluids.ThaumFluidEntry entry) {
      ItemBlockRenderTypes.setRenderLayer((Fluid)entry.getSource().get(), RenderType.translucent());
      ItemBlockRenderTypes.setRenderLayer((Fluid)entry.getFlowing().get(), RenderType.translucent());
   }

   @OnlyIn(Dist.CLIENT)
   public void clientSetup(FMLClientSetupEvent event) {
      setFluidRender(ModFluids.FLUX_GOO);
      setFluidRender(ModFluids.FLUX_GAS);
      setFluidRender(ModFluids.FLUID_PURE);
      setFluidRender(ModFluids.FLUID_DEATH);
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.GREATWOOD_LEAVES.get(), RenderType.cutoutMipped());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.SILVERWOOD_LEAVES.get(), RenderType.cutoutMipped());
      ItemBlockRenderTypes.setFancy(Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.JAR.get(), JarBlockEntityRendered::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.VOID_JAR.get(), JarBlockEntityRendered::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.BRAIN_JAR.get(), BrainJarBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.NODE_JAR.get(), NodeJarBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.MAGNET.get(), MagnetBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.MOB_MAGNET.get(), MagnetBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ELDRITCH_OBELISK.get(), TileEldritchObeliskRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ELDRITCH_LOCK.get(), EldritchLockRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ETHEREAL_BLOOM.get(), EtherealBloomBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ARCANE_ALEMBIC.get(), ArcaneAlembicBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.AURA_NODE.get(), AuraNodeRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ARCANE_LAMP.get(), ArcaneLampRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ARCANE_LAMP_GROWTH.get(), ArcaneLampRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ARCANE_LAMP_FERTILITY.get(), ArcaneLampRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.RUNIC_MATRIX.get(), RunicMatrixRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.NODE_STABILIZER.get(), NodeStabilizerRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ADVANCED_NODE_INFUSER.get(), AdvancedNodeInfuserRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.NODE_TRANSDUCER.get(), NodeTransducerRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ALCHEMICAL_CENTRIFUGE.get(), CentrifugeRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.FLUX_SCRUBBER.get(), FluxScrubberRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ARCANE_BELLOWS.get(), BellowsRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ARCANE_BORE.get(), BoreRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ARCANE_BORE_BASE.get(), BoreBaseRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.BANNER.get(), BannerRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.CRYSTAL_CLUSTER.get(), CrystalClusterRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.MIRROR.get(), MirrorBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.MIRROR_ESSENCE.get(), MirrorBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.DECONSTRUCTION_TABLE.get(), DeconstructionTableBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ARCANE_WORKBENCH.get(), ArcaneWorkbenchBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ESSENCE_RESERVOIR.get(), TileEssenceReservoirRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ELDRITCH_ALTAR.get(), TileEldritchCapRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ELDRITCH_NOTHING.get(), EldritchNothingBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ARCANE_PEDESTAL.get(), PedestalBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ENERGIZED_AURA_NODE.get(), NodeEnergizedBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.VIS_RELAY.get(), VisRelayBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.VIS_CHARGE_RELAY.get(), VisChargeRelayRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.LIFTER.get(), LifterBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.WAND_PEDESTAL.get(), WandPedestalBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.WAND_CHARGER.get(), WandChargerRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.FOCAL_MANIPULATOR.get(), FocalManipulatorBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.ENCHANTER.get(), EnchanterBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.MANA_POD.get(), ManaPodBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.HOLE.get(), HoleBlockEntityRenderer::new);
      BlockEntityRenderers.register((BlockEntityType)ModBlockEntities.WARDED.get(), WardedBlockEntityRenderer::new);
      NeoForge.EVENT_BUS.register(new thaumcraft.event.client.SoulHeartClientHandler());
      ItemProperties.registerGeneric(location("discovery"), (itemStack, clientLevel, livingEntity, n) -> {
         if (itemStack.getItem() instanceof ItemResearchNotes notes) {
            return notes.isDiscovery(itemStack) ? 1.0F : 0.0F;
         } else {
            return 0.0F;
         }
      });
      ItemProperties.register(
         (Item)ModItems.COMPASS_STONE.get(), ResourceLocation.fromNamespaceAndPath("thaumcraft", "active"), (stack, level, entity, seed) -> {
            if (level != null && entity != null) {
               return ItemCompassStone.isActiveFor(level, entity) ? 1.0F : 0.0F;
            } else {
               return 0.0F;
            }
         }
      );
      ItemProperties.register(
         (Item)ModItems.JAR_LABEL.get(), ResourceLocation.fromNamespaceAndPath("thaumcraft", "has_aspect"), (stack, level, entity, seed) -> {
            if (!(stack.getItem() instanceof IEssenceContainerItem c)) {
               return 0.0F;
            } else {
               AspectList al = c.getAspects(stack);
               return al != null && al.size() > 0 ? 1.0F : 0.0F;
            }
         }
      );
      ResourceLocation modeProperty = location("ichor_mode");

      for (DeferredHolder<Item, Item> tool : new DeferredHolder[]{
         ModItems.ICHOR_PICKAXE_ADV, ModItems.ICHOR_AXE_ADV, ModItems.ICHOR_SHOVEL_ADV, ModItems.ICHOR_SWORD_ADV
      }) {
         ItemProperties.register((Item)tool.get(), modeProperty, (stack, level, entity, seed) -> IchorToolHelper.getMode(stack));
      }

      ItemProperties.register(
         (Item)ModItems.BLOCK_TALISMAN.get(),
         ResourceLocation.fromNamespaceAndPath("thaumcraft", "active"),
         (stack, level, entity, seed) -> ItemBlockTalisman.isActive(stack) ? 1.0F : 0.0F
      );
      ItemProperties.register(
         (Item)ModItems.XP_TALISMAN.get(), location("active"), (stack, level, entity, seed) -> ItemXPTalisman.isActive(stack) ? 1.0F : 0.0F
      );
      ItemProperties.register(
         (Item)ModItems.BLOOD_SWORD.get(),
         ResourceLocation.fromNamespaceAndPath("thaumcraft", "activated"),
         (stack, level, entity, seed) -> ItemBloodSword.isActive(stack) ? 1.0F : 0.0F
      );
      ItemProperties.register(
         (Item)ModItems.MOB_ASPECT.get(), ResourceLocation.fromNamespaceAndPath("thaumcraft", "mob_aspect_type"), (stack, level, entity, seed) -> {
            if (!ItemStackData.hasTag(stack)) {
               return 0.0F;
            } else {
               byte t = ItemStackData.getTag(stack).getByte("Type");
               if (t == 1) {
                  return 1.0F;
               } else {
                  return t == 2 ? 2.0F : 0.0F;
               }
            }
         }
      );
      ItemProperties.register(
         (Item)ModItems.MOB_ASPECT.get(), ResourceLocation.fromNamespaceAndPath("thaumcraft", "mob_aspect_id"), (stack, level, entity, seed) -> {
            int aspectCount = NumericAspectHelper.getCount();
            if (aspectCount <= 0) {
               return 0.0F;
            } else {
               CompoundTag tag = ItemStackData.getTag(stack);
               int id = tag == null ? 0 : tag.getInt("AspectNum");
               if (id < 0) {
                  id = 0;
               }

               if (id > aspectCount - 1) {
                  id = aspectCount - 1;
               }

               return id;
            }
         }
      );
      ItemProperties.register(
         (Item)ModItems.CLEANSING_TALISMAN.get(), location("active"), (stack, level, entity, seed) -> ItemCleansingTalisman.isActive(stack) ? 1.0F : 0.0F
      );
      ResourceLocation golemOverlayProperty = location("golem_overlay");

      for (DeferredHolder<Item, Item> golemItem : new DeferredHolder[]{
         ModItems.GOLEM_STRAW,
         ModItems.GOLEM_WOOD,
         ModItems.GOLEM_TALLOW,
         ModItems.GOLEM_CLAY,
         ModItems.GOLEM_FLESH,
         ModItems.GOLEM_STONE,
         ModItems.GOLEM_IRON,
         ModItems.GOLEM_THAUMIUM
      }) {
         ItemProperties.register((Item)golemItem.get(), golemOverlayProperty, (stack, level, entity, seed) -> ItemGolemPlacer.getOverlayPredicate(stack));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register((MenuType)ModMenuTypes.HOVER_HARNESS.get(), HoverHarnessScreen::new);
      event.register((MenuType)ModMenuTypes.HAND_MIRROR.get(), HandMirrorScreen::new);
      event.register((MenuType)ModMenuTypes.RESEARCH_TABLE.get(), ResearchTableScreen::new);
      event.register((MenuType)ModMenuTypes.FOCUS_POUCH.get(), FocusPouchScreen::new);
      event.register((MenuType)ModMenuTypes.ALCHEMY_FURNACE.get(), AlchemyFurnaceScreen::new);
      event.register((MenuType)ModMenuTypes.ADVANCED_NODE_INFUSER.get(), AdvancedNodeInfuserScreen::new);
      event.register((MenuType)ModMenuTypes.ARCANE_BORE.get(), BoreScreen::new);
      event.register((MenuType)ModMenuTypes.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
      event.register((MenuType)ModMenuTypes.DECONSTRUCTION_TABLE.get(), DeconstructionTableScreen::new);
      event.register((MenuType)ModMenuTypes.FOCAL_MANIPULATOR.get(), FocalManipulatorScreen::new);
      event.register((MenuType)ModMenuTypes.WAND_CHARGER.get(), WandChargerScreen::new);
      event.register((MenuType)ModMenuTypes.ENCHANTER.get(), EnchanterScreen::new);
      event.register((MenuType)ModMenuTypes.INFUSION_CLAW.get(), InfusionClawScreen::new);
      event.register((MenuType)ModMenuTypes.MAGIC_BOX.get(), MagicBoxScreen::new);
      event.register((MenuType)ModMenuTypes.MOB_MAGNET.get(), MobMagnetScreen::new);
      event.register((MenuType)ModMenuTypes.WARP_GATE.get(), WarpGateScreen::new);
      event.register((MenuType)ModMenuTypes.SPA.get(), SpaScreen::new);
      event.register((MenuType)ModMenuTypes.THAUMATORIUM.get(), ThaumatoriumScreen::new);
      event.register((MenuType)ModMenuTypes.TRAVELLING_TRUNK.get(), TravelingTrunkScreen::new);
      event.register((MenuType)ModMenuTypes.GOLEM.get(), GolemScreen::new);
      event.register((MenuType)ModMenuTypes.ICHOR_POUCH.get(), IchorPouchScreen::new);
      event.register((MenuType)ModMenuTypes.PECH.get(), PechScreen::new);
   }

   public void onLoadComplete(FMLLoadCompleteEvent evt) {
      ModEntities.initEntitySpawns();
      Config.initModCompatibility();
      ModAspects.init();
      ModResearch.init();
      Config.initLoot();
      Config.initMisc();
   }

   public static void addWarpToPlayer(Player player, int amount, boolean temporary) {
      if (player instanceof ServerPlayer serverPlayer) {
         PlayerKnowledge knowledge = PlayerKnowledge.of(player);
         if ((temporary || amount >= 0) && amount != 0) {
            if (temporary) {
               if (amount < 0 && knowledge.getWarpTemp() <= 0) {
                  return;
               }

               knowledge.addWarpTemp(amount);
               PacketHandler.sendTo(new PacketSyncWarp(player, (byte)2), serverPlayer);
               PacketHandler.sendTo(new PacketWarpMessage((byte)2, amount), serverPlayer);
            } else {
               knowledge.addWarpPerm(amount);
               PacketHandler.sendTo(new PacketSyncWarp(player, (byte)0), serverPlayer);
               PacketHandler.sendTo(new PacketWarpMessage((byte)0, amount), serverPlayer);
            }

            knowledge.setWarpCounter(knowledge.getWarpTotal());
         }
      }
   }

   public static void addStickyWarpToPlayer(Player player, int amount) {
      if (player instanceof ServerPlayer serverPlayer) {
         PlayerKnowledge knowledge = PlayerKnowledge.of(player);
         if (amount != 0 && (amount >= 0 || knowledge.getWarpSticky() > 0)) {
            knowledge.addWarpSticky(amount);
            PacketHandler.sendTo(new PacketSyncWarp(player, (byte)1), serverPlayer);
            PacketHandler.sendTo(new PacketWarpMessage((byte)1, amount), serverPlayer);
            knowledge.setWarpCounter(knowledge.getWarpTotal());
         }
      }
   }

   public static ResourceLocation location(String path) {
      return ResourceLocation.fromNamespaceAndPath("thaumcraft", path);
   }

   public static ModelResourceLocation staticModel(String path) {
      return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/static/" + path));
   }
}
