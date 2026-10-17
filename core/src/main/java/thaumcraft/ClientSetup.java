package thaumcraft;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin.Model;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Block;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.api.render.ITIntColor;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.block.banner.BannerRenderer;
import thaumcraft.block.crucible.CrucibleBlockEntity;
import thaumcraft.block.crystal.CrystalClusterRenderer;
import thaumcraft.block.deconstruction_table.render.DeconstructionTableBlockEntityRenderer;
import thaumcraft.block.eldritch.portal.render.EldritchPortalRendererBlockEntity;
import thaumcraft.block.funnel.render.FunnelBlockEntityRenderer;
import thaumcraft.block.hungry_chest.render.ChestModel;
import thaumcraft.block.hungry_chest.render.TileChestHungryRenderer;
import thaumcraft.block.infusion_claw.render.InfusionClawBlockEntityRenderer;
import thaumcraft.block.magnet.ModelMagnet;
import thaumcraft.block.mana_pod.ModelManaPod;
import thaumcraft.block.platform.PlatformBlockEntity;
import thaumcraft.block.render.RecoloredModel;
import thaumcraft.block.repairer.render.RepairerBlockEntityRenderer;
import thaumcraft.block.repairer.render.RepairerModel;
import thaumcraft.block.tube.render.TubeBufferBlockEntityRenderer;
import thaumcraft.block.tube.render.TubeOnewayBlockEntityRenderer;
import thaumcraft.block.tube.render.TubeValveBlockEntityRenderer;
import thaumcraft.block.tube.render.model.ModelTubeValve;
import thaumcraft.block.warded.WardedBlockEntity;
import thaumcraft.client.tooltip.AspectTooltipClientComponent;
import thaumcraft.client.tooltip.AspectTooltipData;
import thaumcraft.entities.brainless.aspect_orb.RenderAspectOrb;
import thaumcraft.entities.brainless.dart.RenderDart;
import thaumcraft.entities.brainless.eldrich_orb.render.RenderEldritchOrb;
import thaumcraft.entities.brainless.falling_taint.RenderFallingTaint;
import thaumcraft.entities.brainless.projectile.ember.RenderEmber;
import thaumcraft.entities.brainless.projectile.explosive_orb.RenderExplosiveOrb;
import thaumcraft.entities.brainless.projectile.frost_shard.RenderFrostShard;
import thaumcraft.entities.brainless.projectile.golem_orb.RenderGolemOrb;
import thaumcraft.entities.brainless.projectile.primal_orb.RenderPrimalOrb;
import thaumcraft.entities.brainless.projectile.shock_orb.RenderElectricOrb;
import thaumcraft.entities.brainless.special.SpecialItemEntityRender;
import thaumcraft.entities.smart.cultist.render.ModelCultist;
import thaumcraft.entities.smart.cultist.render.RenderCultist;
import thaumcraft.entities.smart.cultist_portal.render.RenderCultistPortal;
import thaumcraft.entities.smart.edlritch_crab.EldritchCrabRenderer;
import thaumcraft.entities.smart.edlritch_crab.ModelEldritchCrab;
import thaumcraft.entities.smart.eldrich_guardian.render.EldritchGuardianModel;
import thaumcraft.entities.smart.eldrich_guardian.render.EldritchGuardianRenderer;
import thaumcraft.entities.smart.eldrich_warden.render.EldritchWardenRenderer;
import thaumcraft.entities.smart.eldritch_golem.RenderEldritchGolem;
import thaumcraft.entities.smart.eldritch_golem.render.ModelEldritchGolem;
import thaumcraft.entities.smart.fire_bat.render.ModelFireBat;
import thaumcraft.entities.smart.fire_bat.render.RenderFireBat;
import thaumcraft.entities.smart.golem.render.ModelGolem;
import thaumcraft.entities.smart.golem.render.ModelGolemAccessories;
import thaumcraft.entities.smart.golem.render.RenderGolemBase;
import thaumcraft.entities.smart.golem.render.RenderGolemBobber;
import thaumcraft.entities.smart.mind_spider.render.RenderMindSpider;
import thaumcraft.entities.smart.pech.render.ModelPech;
import thaumcraft.entities.smart.pech.render.RenderPech;
import thaumcraft.entities.smart.pech_blast.RenderPechBlast;
import thaumcraft.entities.smart.taint.chicken.RenderTaintChicken;
import thaumcraft.entities.smart.taint.cow.RenderTaintCow;
import thaumcraft.entities.smart.taint.creeper.RenderTaintCreeper;
import thaumcraft.entities.smart.taint.pig.RenderTaintPig;
import thaumcraft.entities.smart.taint.sheep.RenderTaintSheep;
import thaumcraft.entities.smart.taint.spider.RenderTaintSpider;
import thaumcraft.entities.smart.taint.spore.render.ModelTaintSpore;
import thaumcraft.entities.smart.taint.spore.render.ModelTaintSporeSwarmer;
import thaumcraft.entities.smart.taint.spore.render.RenderTaintSpore;
import thaumcraft.entities.smart.taint.spore.render.RenderTaintSporeSwarmer;
import thaumcraft.entities.smart.taint.spore.render.RenderTaintSwarm;
import thaumcraft.entities.smart.taint.villager.RenderTaintVillager;
import thaumcraft.entities.smart.tentacle.render.ModelTantacle;
import thaumcraft.entities.smart.tentacle.render.TantacleRenderer;
import thaumcraft.entities.smart.thaumic_slime.RenderThaumicSlime;
import thaumcraft.entities.smart.travelling_trunk.render.ModelTrunk;
import thaumcraft.entities.smart.travelling_trunk.render.RenderTravelingTrunk;
import thaumcraft.entities.smart.watcher.render.ModelWatcher;
import thaumcraft.entities.smart.watcher.render.RenderWatcher;
import thaumcraft.entities.smart.wisp.render.RenderWisp;
import thaumcraft.entities.smart.zombie.render.RenderZombie;
import thaumcraft.event.client.ClientTickEventsFML;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.armor.cultist_leader.ModelLeaderArmor;
import thaumcraft.item.armor.cultist_plate.ModelKnightArmor;
import thaumcraft.item.armor.cultist_robe.ModelRobe;
import thaumcraft.item.armor.hover_harness.HoverHarnessLayer;
import thaumcraft.item.armor.hover_harness.ModelHoverHarness;
import thaumcraft.item.armor.ichor_gem.ModelWings;
import thaumcraft.item.armor.thaumium_fortress.ModelFortressArmor;
import thaumcraft.item.curios.ItemBaubleBlanks;
import thaumcraft.item.spell_cloth.ItemSpellCloth;
import thaumcraft.item.trunk_spawner.render.ItemTrunkSpawnerRenderer;
import thaumcraft.item.wands.ItemWandCap;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.ItemWandRod;
import thaumcraft.item.wands.render.WandRender;
import thaumcraft.particles.fx.beams.FXBeam;
import thaumcraft.particles.fx.beams.FXBeamBore;
import thaumcraft.particles.fx.beams.FXBeamPower;
import thaumcraft.particles.fx.beams.FXBeamWand;
import thaumcraft.particles.fx.bolt.FXLightningBolt;
import thaumcraft.particles.fx.other.FXBlockWard;
import thaumcraft.particles.fx.particles.FXBlockRunes;
import thaumcraft.particles.fx.particles.FXBoreParticles;
import thaumcraft.particles.fx.particles.FXBoreSparkle;
import thaumcraft.particles.fx.particles.FXBreaking;
import thaumcraft.particles.fx.particles.FXBubble;
import thaumcraft.particles.fx.particles.FXBurst;
import thaumcraft.particles.fx.particles.FXDrop;
import thaumcraft.particles.fx.particles.FXEssentiaTrail;
import thaumcraft.particles.fx.particles.FXGeneric;
import thaumcraft.particles.fx.particles.FXSmokeSpiral;
import thaumcraft.particles.fx.particles.FXSmokeTrail;
import thaumcraft.particles.fx.particles.FXSpark;
import thaumcraft.particles.fx.particles.FXSparkle;
import thaumcraft.particles.fx.particles.FXSparkleTrail;
import thaumcraft.particles.fx.particles.FXSwarm;
import thaumcraft.particles.fx.particles.FXVent;
import thaumcraft.particles.fx.particles.FXWisp;
import thaumcraft.particles.fx.particles.FXWispArcing;
import thaumcraft.particles.fx.particles.FXWispEG;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchNoteData;

@OnlyIn(Dist.CLIENT)
public class ClientSetup extends CommonSetup {
   private static final int TAINT_PURPLE = 8203678;
   public static final ResourceLocation VIS_RELAY_CRYSTAL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/vis_relay_crystal");

   public ClientSetup(IEventBus bus) {
      bus.addListener(this::registerRenderers);
      bus.addListener(this::registerLayerDefinitions);
      bus.addListener(this::addLayers);
      bus.addListener(this::registerAdditionalModels);
      bus.addListener(this::registerColorBlock);
      bus.addListener(this::registerColorItem);
      bus.addListener(this::registerTooltipComponentFactories);
      bus.addListener(this::onModelBake);
   }

   @Override
   public void registerHandlers() {
      NeoForge.EVENT_BUS.register(ClientTickEventsFML.INSTANCE);
      if (Thaumcraft.instance != null && Thaumcraft.instance.renderEventHandler != null) {
         NeoForge.EVENT_BUS.register(Thaumcraft.instance.renderEventHandler);
      }
   }

   @Override
   public void registerDisplayInformation() {
      if (ModList.get().isLoaded("jei")) {
         Thaumcraft.instance.aspectShift = true;
      }

      this.setupItemRenderers();
      this.setupBlockRenderers();
      this.setupTileRenderers();
   }

   private void setupItemRenderers() {
   }

   private void setupTileRenderers() {
   }

   private void setupBlockRenderers() {
   }

   private void registerTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
      event.register(AspectTooltipData.class, AspectTooltipClientComponent::new);
   }

   private void registerLayerDefinitions(RegisterLayerDefinitions event) {
      event.registerLayerDefinition(BannerRenderer.BANNER, BannerRenderer::createBodyLayer);
      event.registerLayerDefinition(CrystalClusterRenderer.MODEL, CrystalClusterRenderer::createBodyLayer);
      event.registerLayerDefinition(TileChestHungryRenderer.LAYER_LOCATION, ChestModel::createBodyLayer);
      event.registerLayerDefinition(ModelTubeValve.TUBE_VALVE, ModelTubeValve::createLayer);
      event.registerLayerDefinition(ModelTantacle.LAYER, ModelTantacle::createBodyLayer);
      event.registerLayerDefinition(ModelWatcher.LAYER_LOCATION, ModelWatcher::createBodyLayer);
      event.registerLayerDefinition(ModelEldritchGolem.LAYER_LOCATION, ModelEldritchGolem::createBodyLayer);
      event.registerLayerDefinition(EldritchGuardianModel.LAYER_LOCATION, EldritchGuardianModel::createBodyLayer);
      event.registerLayerDefinition(ModelFireBat.LAYER_LOCATION, ModelFireBat::createBodyLayer);
      event.registerLayerDefinition(ModelGolem.LAYER_LOCATION, ModelGolem::createBodyLayer);
      event.registerLayerDefinition(ModelGolemAccessories.LAYER_LOCATION, ModelGolemAccessories::createBodyLayer);
      event.registerLayerDefinition(ModelTrunk.LAYER_LOCATION, ModelTrunk::createBodyLayer);
      event.registerLayerDefinition(ItemTrunkSpawnerRenderer.TRUNK_LAYER, ChestModel::createBodyLayer);
      event.registerLayerDefinition(ModelEldritchCrab.LAYER_LOCATION, ModelEldritchCrab::createBodyLayer);
      event.registerLayerDefinition(ModelPech.LAYER_LOCATION, ModelPech::createBodyLayer);
      event.registerLayerDefinition(ModelHoverHarness.LAYER_LOCATION, ModelHoverHarness::createBodyLayer);
      event.registerLayerDefinition(ModelCultist.LAYER, ModelCultist::createBodyLayer);
      event.registerLayerDefinition(ModelTaintSpore.LAYER_LOCATION, ModelTaintSpore::createBodyLayer);
      event.registerLayerDefinition(ModelTaintSporeSwarmer.LAYER_LOCATION, ModelTaintSporeSwarmer::createBodyLayer);
      event.registerLayerDefinition(ModelManaPod.LAYER_LOCATION, ModelManaPod::createBodyLayer);
      event.registerLayerDefinition(RepairerModel.LAYER, RepairerModel::createLayer);
      event.registerLayerDefinition(ModelWings.LAYER_LOCATION, ModelWings::createLayerDefinition);
      event.registerLayerDefinition(ModelMagnet.LAYER_LOCATION, ModelMagnet::createBodyLayer);
      event.registerLayerDefinition(ModelKnightArmor.OUTER, ModelKnightArmor::createLayer);
      event.registerLayerDefinition(ModelKnightArmor.INNER, ModelKnightArmor::createLayer);
      event.registerLayerDefinition(ModelRobe.OUTER, ModelRobe::createLayer);
      event.registerLayerDefinition(ModelRobe.INNER, ModelRobe::createLayer);
      event.registerLayerDefinition(ModelLeaderArmor.LAYER_OUTER, () -> ModelLeaderArmor.createBodyLayer(true));
      event.registerLayerDefinition(ModelLeaderArmor.LAYER_INNER, () -> ModelLeaderArmor.createBodyLayer(false));
      event.registerLayerDefinition(ModelFortressArmor.LAYER_OUTER, () -> ModelFortressArmor.createBodyLayer(true));
      event.registerLayerDefinition(ModelFortressArmor.LAYER_INNER, () -> ModelFortressArmor.createBodyLayer(false));
   }

   public void addLayers(AddLayers event) {
      if (event.getSkin(Model.WIDE) instanceof PlayerRenderer pr) {
         pr.addLayer(new HoverHarnessLayer(pr));
      }

      if (event.getSkin(Model.SLIM) instanceof PlayerRenderer pr) {
         pr.addLayer(new HoverHarnessLayer(pr));
      }
   }

   private void registerAdditionalModels(RegisterAdditional event) {
      event.register(ModelResourceLocation.standalone(Thaumcraft.location("block/crucible_fluid")));
      ItemWandCap.TYPES.forEach((s, cap) -> event.register(ModelResourceLocation.standalone(cap.getModel())));
      ItemWandRod.TYPES.forEach((s, rod) -> {
         if (!rod.isStaff()) {
            event.register(ModelResourceLocation.standalone(rod.getModel()));
         }
      });
      ItemFocusBasic.TYPES.forEach((s, focus) -> event.register(ModelResourceLocation.standalone(focus.getModelLocation())));

      for (byte i = 0; i < 16; i++) {
         event.register(ModelResourceLocation.standalone(Thaumcraft.location("item/wand/runes/" + i)));
      }

      event.register(Thaumcraft.staticModel("bore_connect"));
      event.register(Thaumcraft.staticModel("runic_matrix"));
      event.register(Thaumcraft.staticModel("runic_matrix_over"));
      event.register(Thaumcraft.staticModel("node_stabilizer_piston"));
      event.register(Thaumcraft.staticModel("node_stabilizer_piston_over"));
      event.register(Thaumcraft.staticModel("advanced_node_infuser_piston"));
      event.register(Thaumcraft.staticModel("advanced_node_infuser_piston_over"));
      event.register(Thaumcraft.staticModel("node_transducer_piston"));
      event.register(Thaumcraft.staticModel("node_transducer_piston_over"));
      event.register(Thaumcraft.staticModel("alchemical_centrifuge_rotate"));
      event.register(Thaumcraft.staticModel("flux_scrubber_cap"));
      event.register(Thaumcraft.staticModel("arcane_bellows_bag"));
      event.register(Thaumcraft.staticModel("arcane_bellows_plank"));
      event.register(Thaumcraft.staticModel("harness"));
      event.register(Thaumcraft.staticModel("eldritch_altar"));
      event.register(Thaumcraft.staticModel("eldritch_altar_outer"));
      event.register(Thaumcraft.staticModel("bucket"));
      event.register(ModelResourceLocation.standalone(VIS_RELAY_CRYSTAL));
      event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/vis_charge_relay_crystal")));
   }

   @OnlyIn(Dist.CLIENT)
   private void onModelBake(ModifyBakingResult event) {
      for (String model : List.of("crystal", "crystal_0", "crystal_1", "crystal_2", "crystal_3")) {
         ModelResourceLocation key = ModelResourceLocation.standalone(Thaumcraft.location("block/" + model));
         BakedModel original = (BakedModel)event.getModels().get(key);
         if (original != null && !(original instanceof RecoloredModel)) {
            event.getModels().put(key, RecoloredModel.fromBlockState(original));
         }
      }

      Map<String, Integer> map = Map.of("air", -130, "fire", -50175, "water", -16740097, "earth", -16736256, "order", -1127169, "entropy", -11184777);

      for (Entry<String, Integer> entry : map.entrySet()) {
         ResourceLocation modelLoc = Thaumcraft.location("block/" + entry.getKey() + "_crystal_cluster");
         BakedModel original = (BakedModel)event.getModels().get(ModelResourceLocation.standalone(modelLoc));
         if (original != null) {
            event.getModels().put(ModelResourceLocation.standalone(modelLoc), new RecoloredModel(original, entry.getValue()));
         }

         ResourceLocation itemLoc = Thaumcraft.location(entry.getKey() + "_crystal");
         BakedModel itemModel = (BakedModel)event.getModels().get(ModelResourceLocation.inventory(itemLoc));
         if (itemModel != null) {
            event.getModels().put(ModelResourceLocation.inventory(itemLoc), new RecoloredModel(itemModel, entry.getValue()));
         }

         event.getModels()
            .replaceAll(
               (location, modelx) -> {
                  ResourceLocation clusterLoc = Thaumcraft.location(entry.getKey() + "_crystal_cluster");
                  if (location.id().equals(itemLoc) && !location.variant().equals("inventory")) {
                     return RecoloredModel.fromBlockState(modelx);
                  } else {
                     return (BakedModel)(location.id().equals(clusterLoc) && !location.variant().equals("inventory")
                        ? new RecoloredModel(modelx, entry.getValue())
                        : modelx);
                  }
               }
            );
      }

      ResourceLocation mixed = Thaumcraft.location("block/mixed_crystal_cluster");
      BakedModel originalx = (BakedModel)event.getModels().get(ModelResourceLocation.standalone(mixed));
      if (originalx != null) {
         event.getModels().put(ModelResourceLocation.standalone(mixed), new RecoloredModel(originalx, -130, -50175, -16740097));
      }
   }

   private void registerRenderers(RegisterRenderers event) {
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.HUNGRY_CHEST.get(), TileChestHungryRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.FUNNEL.get(), FunnelBlockEntityRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.FOLLOWING_ITEM.get(), SpecialItemEntityRender::new);
      event.registerEntityRenderer((EntityType)ModEntities.ALUMENTUM.get(), ThrownItemRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.DECONSTRUCTION_TABLE.get(), DeconstructionTableBlockEntityRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.TUBE_VALVE.get(), TubeValveBlockEntityRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.TUBE_ONEWAY.get(), TubeOnewayBlockEntityRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.TUBE_BUFFER.get(), TubeBufferBlockEntityRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.INFUSION_CLAW.get(), InfusionClawBlockEntityRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.ELDRITCH_PORTAL.get(), EldritchPortalRendererBlockEntity::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.REPAIRER.get(), RepairerBlockEntityRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.ELDRITCH_CRAB.get(), EldritchCrabRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.TENTACLE.get(), TantacleRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.TENTACLE_SMALL.get(), TantacleRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.TENTACLE_GIANT.get(), TantacleRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.WISP.get(), RenderWisp::new);
      event.registerEntityRenderer((EntityType)ModEntities.WATCHER.get(), RenderWatcher::new);
      event.registerEntityRenderer((EntityType)ModEntities.ELDRITCH_GOLEM.get(), RenderEldritchGolem::new);
      event.registerEntityRenderer((EntityType)ModEntities.SPECIAL_ITEM.get(), SpecialItemEntityRender::new);
      event.registerEntityRenderer((EntityType)ModEntities.ELDRITCH_GUARDIAN.get(), EldritchGuardianRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.ELDRITCH_WARDEN.get(), EldritchWardenRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.ELDRITCH_ORB.get(), RenderEldritchOrb::new);
      event.registerEntityRenderer((EntityType)ModEntities.TAINT_CHICKEN.get(), RenderTaintChicken::new);
      event.registerEntityRenderer((EntityType)ModEntities.TAINT_COW.get(), RenderTaintCow::new);
      event.registerEntityRenderer((EntityType)ModEntities.TAINT_PIG.get(), RenderTaintPig::new);
      event.registerEntityRenderer((EntityType)ModEntities.TAINT_SHEEP.get(), RenderTaintSheep::new);
      event.registerEntityRenderer((EntityType)ModEntities.TAINT_CREEPER.get(), RenderTaintCreeper::new);
      event.registerEntityRenderer((EntityType)ModEntities.TAINT_SPIDER.get(), RenderTaintSpider::new);
      event.registerEntityRenderer((EntityType)ModEntities.TAINT_VILLAGER.get(), RenderTaintVillager::new);
      event.registerEntityRenderer((EntityType)ModEntities.FIRE_BAT.get(), RenderFireBat::new);
      event.registerEntityRenderer((EntityType)ModEntities.GOLEM.get(), RenderGolemBase::new);
      event.registerEntityRenderer((EntityType)ModEntities.GOLEM_BOBBER.get(), RenderGolemBobber::new);
      event.registerEntityRenderer((EntityType)ModEntities.TRAVELING_TRUNK.get(), RenderTravelingTrunk::new);
      event.registerEntityRenderer((EntityType)ModEntities.PRIMAL_ORB.get(), RenderPrimalOrb::new);
      event.registerEntityRenderer((EntityType)ModEntities.PECH_BLAST.get(), RenderPechBlast::new);
      event.registerEntityRenderer((EntityType)ModEntities.PECH.get(), RenderPech::new);
      event.registerEntityRenderer((EntityType)ModEntities.BRAINY_ZOMBIE.get(), RenderZombie::new);
      event.registerEntityRenderer((EntityType)ModEntities.GIANT_BRAINY_ZOMBIE.get(), RenderZombie::new);
      event.registerEntityRenderer((EntityType)ModEntities.INHABITED_ZOMBIE.get(), RenderZombie::new);
      event.registerEntityRenderer((EntityType)ModEntities.THAUMIC_SLIME.get(), RenderThaumicSlime::new);
      event.registerEntityRenderer((EntityType)ModEntities.CULTIST.get(), RenderCultist::new);
      event.registerEntityRenderer((EntityType)ModEntities.CULTIST_KNIGHT.get(), RenderCultist::new);
      event.registerEntityRenderer((EntityType)ModEntities.CULTIST_CLERIC.get(), RenderCultist::new);
      event.registerEntityRenderer((EntityType)ModEntities.CULTIST_LEADER.get(), RenderCultist::new);
      event.registerEntityRenderer((EntityType)ModEntities.MIND_SPIDER.get(), RenderMindSpider::new);
      event.registerEntityRenderer((EntityType)ModEntities.CULTIST_PORTAL.get(), RenderCultistPortal::new);
      event.registerEntityRenderer((EntityType)ModEntities.TAINT_SPORE.get(), RenderTaintSpore::new);
      event.registerEntityRenderer((EntityType)ModEntities.TAINT_SPORE_SWARMER.get(), RenderTaintSporeSwarmer::new);
      event.registerEntityRenderer((EntityType)ModEntities.TAINT_SWARM.get(), RenderTaintSwarm::new);
      event.registerEntityRenderer((EntityType)ModEntities.BOTTLE_TAINT.get(), ThrownItemRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.FALLING_TAINT.get(), RenderFallingTaint::new);
      event.registerEntityRenderer((EntityType)ModEntities.EMBER.get(), RenderEmber::new);
      event.registerEntityRenderer((EntityType)ModEntities.FROST_SHARD.get(), RenderFrostShard::new);
      event.registerEntityRenderer((EntityType)ModEntities.EXPLOSIVE_ORB.get(), RenderExplosiveOrb::new);
      event.registerEntityRenderer((EntityType)ModEntities.SHOCK_ORB.get(), RenderElectricOrb::new);
      event.registerEntityRenderer((EntityType)ModEntities.PRIMAL_ORB.get(), RenderPrimalOrb::new);
      event.registerEntityRenderer((EntityType)ModEntities.GOLEM_ORB.get(), RenderGolemOrb::new);
      event.registerEntityRenderer((EntityType)ModEntities.SHADOW_BEAM.get(), ThrownItemRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.PERMANENT_ITEM.get(), SpecialItemEntityRender::new);
      event.registerEntityRenderer((EntityType)ModEntities.GRATE_ITEM.get(), ItemEntityRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.DART.get(), RenderDart::new);
      event.registerEntityRenderer((EntityType)ModEntities.ASPECT_ORB.get(), RenderAspectOrb::new);
   }

   private void registerColorBlock(Block event) {
      BlockColor blockColor = (state, level, pos, tint) -> {
         net.minecraft.world.level.block.Block b = state.getBlock();
         if (b instanceof ITIntColor tc) {
            return tc.getColor(tint);
         } else {
            return b.asItem() instanceof ITIntColor ic ? ic.getColor(tint) : -1;
         }
      };
      event.register(
         blockColor,
         new net.minecraft.world.level.block.Block[]{
            (net.minecraft.world.level.block.Block)ModBlocks.AIR_SHARD_ORE.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.FIRE_SHARD_ORE.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.WATER_SHARD_ORE.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.EARTH_SHARD_ORE.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.ORDER_SHARD_ORE.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.ENTROPY_SHARD_ORE.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.AIR_CRYSTAL.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.FIRE_CRYSTAL.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.WATER_CRYSTAL.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.EARTH_CRYSTAL.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.ORDER_CRYSTAL.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.ENTROPY_CRYSTAL.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.AIR_CRYSTAL_CLUSTER.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.FIRE_CRYSTAL_CLUSTER.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.WATER_CRYSTAL_CLUSTER.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.EARTH_CRYSTAL_CLUSTER.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.ORDER_CRYSTAL_CLUSTER.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.ENTROPY_CRYSTAL_CLUSTER.get(),
            (net.minecraft.world.level.block.Block)ModBlocks.MIXED_CRYSTAL_CLUSTER.get()
         }
      );
      event.register((state, level, pos, tintIndex) -> {
         if (tintIndex == 0) {
            return 40960;
         } else {
            return tintIndex == 1 ? 15650047 : -1;
         }
      }, new net.minecraft.world.level.block.Block[]{(net.minecraft.world.level.block.Block)ModBlocks.LIFTER.get()});
      event.register(blockColor, ModBlocks.CANDLE.values().stream().map(DeferredHolder::get).toArray(net.minecraft.world.level.block.Block[]::new));
      event.register((state, level, pos, tintIndex) -> {
         if (level != null && pos != null) {
            if (level.getBlockEntity(pos) instanceof WardedBlockEntity warded) {
               BlockState mimic = warded.getMimic();
               return event.getBlockColors().getColor(mimic, level, pos, tintIndex);
            } else {
               return -1;
            }
         } else {
            return -1;
         }
      }, new net.minecraft.world.level.block.Block[]{(net.minecraft.world.level.block.Block)ModBlocks.WARDED.get()});
      event.register((state, level, pos, tintIndex) -> {
         if (level != null && pos != null) {
            if (level.getBlockEntity(pos) instanceof PlatformBlockEntity platform) {
               BlockState mimic = platform.getMimic();
               return mimic.isAir() ? -1 : event.getBlockColors().getColor(mimic, level, pos, tintIndex);
            } else {
               return -1;
            }
         } else {
            return -1;
         }
      }, new net.minecraft.world.level.block.Block[]{(net.minecraft.world.level.block.Block)ModBlocks.PLATFORM.get()});
      event.register((state, level, pos, tintIndex) -> {
         if (tintIndex != 0) {
            return -1;
         } else {
            return level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : 8203678;
         }
      }, new net.minecraft.world.level.block.Block[]{(net.minecraft.world.level.block.Block)ModBlocks.FIBROUS_TAINT.get()});
      event.register((state, level, pos, tintIndex) -> {
         if (tintIndex != 0) {
            return -1;
         } else {
            return level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : 8203678;
         }
      }, new net.minecraft.world.level.block.Block[]{(net.minecraft.world.level.block.Block)ModBlocks.TAINT_SOIL.get()});
      event.register((state, level, pos, tintIndex) -> {
         if (tintIndex != 0) {
            return -1;
         } else {
            return level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : FoliageColor.getDefaultColor();
         }
      }, new net.minecraft.world.level.block.Block[]{(net.minecraft.world.level.block.Block)ModBlocks.GREATWOOD_LEAVES.get()});
   }

   @OnlyIn(Dist.CLIENT)
   private void registerColorItem(Item event) {
      event.register((stack, tint) -> {
         if (stack.getItem() instanceof ItemWandCasting casting) {
            if (tint == 0) {
               ItemFocusBasic basic = casting.getFocus(stack);
               if (basic != null) {
                  return ARGB32.opaque(basic.getFocusColor(casting.getFocusItem(stack)));
               }
            } else if (tint == 1) {
               Player player = Minecraft.getInstance().player;
               if (player != null) {
                  float r = Mth.sin((player.tickCount + WandRender.renderIndex * 5) / 5.0F) * 0.1F + 0.88F;
                  float g = Mth.sin((player.tickCount + WandRender.renderIndex * 5) / 7.0F) * 0.1F + 0.63F;
                  float a = Mth.sin((player.tickCount + WandRender.renderIndex * 5) / 10.0F) * 0.2F + 0.6F;
                  int A = Mth.clamp((int)(a * 255.0F), 0, 255);
                  int R = Mth.clamp((int)(r * 255.0F), 0, 255);
                  int G = Mth.clamp((int)(g * 255.0F), 0, 255);
                  int B = 51;
                  return ARGB32.color(A, R, G, B);
               }
            }
         }

         return -1;
      }, new ItemLike[]{(ItemLike)ModItems.WAND_CASTING.get()});
      event.register((stack, tintIndex) -> {
         if (tintIndex != 1) {
            return -1;
         } else {
            if (stack.getItem() instanceof IEssenceContainerItem c) {
               AspectList al = c.getAspects(stack);
               if (al != null && al.size() > 0) {
                  Aspect a = al.getAspects()[0];
                  if (a != null) {
                     return ARGB32.opaque(a.getColor());
                  }
               }
            }

            return -1;
         }
      }, new ItemLike[]{(ItemLike)ModItems.JAR_LABEL.get()});
      event.register((stack, tintIndex) -> {
         if (tintIndex != 0) {
            return -1;
         } else {
            Aspect aspect = ItemBaubleBlanks.getStoredAspect(stack);
            return aspect == null ? -1 : ARGB32.opaque(aspect.getColor());
         }
      }, new ItemLike[]{(ItemLike)ModItems.CURIOS_RING_IRON.get()});
      event.register((stack, tintIndex) -> {
         if (tintIndex == 0) {
            return ARGB32.opaque(40960);
         } else {
            return tintIndex == 1 ? ARGB32.opaque(15650047) : -1;
         }
      }, new ItemLike[]{((net.minecraft.world.level.block.Block)ModBlocks.LIFTER.get()).asItem()});
      event.register(ItemSpellCloth::colorFromStack, new ItemLike[]{(ItemLike)ModItems.SPELL_CLOTH.get()});
      // Кластеры: статическая OBJ-модель красится кодом аспекта и в мире
      // (блок-тины выше), и в инвентаре — иначе предметы серые.
      event.register((stack, tint) -> {
         if (tint == 0
            && stack.getItem() instanceof BlockItem blockItem
            && blockItem.getBlock() instanceof thaumcraft.block.crystal.CrystalClusterBlock cluster) {
            return ARGB32.opaque(cluster.getColor());
         }

         return -1;
      }, new ItemLike[]{
         (ItemLike)ModItems.AIR_CRYSTAL_CLUSTER.get(),
         (ItemLike)ModItems.FIRE_CRYSTAL_CLUSTER.get(),
         (ItemLike)ModItems.WATER_CRYSTAL_CLUSTER.get(),
         (ItemLike)ModItems.EARTH_CRYSTAL_CLUSTER.get(),
         (ItemLike)ModItems.ORDER_CRYSTAL_CLUSTER.get(),
         (ItemLike)ModItems.ENTROPY_CRYSTAL_CLUSTER.get(),
         (ItemLike)ModItems.MIXED_CRYSTAL_CLUSTER.get()
      });
      Aspect[] displayAspects = Aspect.aspects.values().toArray(new Aspect[0]);
      event.register(
         (stack, tint) -> {
            if (stack.getItem() instanceof IEssenceContainerItem containerItem) {
               boolean phial = stack.is((net.minecraft.world.item.Item)ModItems.PHIAL_ESSENCE.get());
               if (phial && tint != 1) {
                  return -1;
               }

               AspectList list = containerItem.getAspects(stack);
               if (list != null && list.size() > 0) {
                  return ARGB32.opaque(list.getAspects()[0].getColor());
               }

               if (stack.is((net.minecraft.world.item.Item)ModItems.CRYSTAL_ESSENCE.get()) || stack.is((net.minecraft.world.item.Item)ModItems.MANA_BEAN.get())
                  )
                {
                  int idx = (int)(System.currentTimeMillis() / 500L % displayAspects.length);
                  return ARGB32.opaque(displayAspects[idx].getColor());
               }

               if (phial) {
                  return ARGB32.opaque(11388377);
               }
            }

            return -1;
         },
         new ItemLike[]{
            (ItemLike)ModItems.PHIAL_ESSENCE.get(),
            (ItemLike)ModItems.WISP_ESSENCE.get(),
            (ItemLike)ModItems.CRYSTAL_ESSENCE.get(),
            (ItemLike)ModItems.MANA_BEAN.get()
         }
      );
      event.register((stack, tint) -> {
         if (tint == 1) {
            ResearchNoteData rd = ResearchManager.getData(stack);
            int rgb = rd == null ? 10066329 : rd.color;
            // Цвет аспекта хранится как обычный RGB (Aspect.getColor() без альфы).
            // В 1.21 тинт предмета читается как ARGB, и без старшего байта альфа
            // получается нулевой — лента «открытия» рисовалась полностью
            // прозрачной. Приписываем непрозрачность явно.
            return 0xFF000000 | (rgb & 0xFFFFFF);
         } else {
            return -1;
         }
      }, new ItemLike[]{(ItemLike)ModItems.RESEARCH_NOTES.get()});
      ItemColor itemColor = (stack, tint) -> {
         if (stack.getItem() instanceof ITIntColor c) {
            return c.getColor(tint);
         } else {
            return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ITIntColor c ? c.getColor(tint) : -1;
         }
      };
      event.register(
         itemColor,
         new ItemLike[]{
            (ItemLike)ModItems.AIR_SHARD_ORE.get(),
            (ItemLike)ModItems.FIRE_SHARD_ORE.get(),
            (ItemLike)ModItems.WATER_SHARD_ORE.get(),
            (ItemLike)ModItems.EARTH_SHARD_ORE.get(),
            (ItemLike)ModItems.ORDER_SHARD_ORE.get(),
            (ItemLike)ModItems.ENTROPY_SHARD_ORE.get(),
            (ItemLike)ModItems.AIR_CRYSTAL.get(),
            (ItemLike)ModItems.FIRE_CRYSTAL.get(),
            (ItemLike)ModItems.WATER_CRYSTAL.get(),
            (ItemLike)ModItems.EARTH_CRYSTAL.get(),
            (ItemLike)ModItems.ORDER_CRYSTAL.get(),
            (ItemLike)ModItems.ENTROPY_CRYSTAL.get()
         }
      );
      event.register(itemColor, ModItems.CANDLE.values().stream().map(DeferredHolder::get).toArray(ItemLike[]::new));
      event.register(
         (stack, tintIndex) -> tintIndex == 0 ? 8203678 : -1, new ItemLike[]{((net.minecraft.world.level.block.Block)ModBlocks.FIBROUS_TAINT.get()).asItem()}
      );
      event.register(
         (stack, tintIndex) -> tintIndex == 0 ? 8203678 : -1, new ItemLike[]{((net.minecraft.world.level.block.Block)ModBlocks.TAINT_SOIL.get()).asItem()}
      );
      event.register(
         (stack, tintIndex) -> tintIndex == 0 ? FoliageColor.getDefaultColor() : -1,
         new ItemLike[]{((net.minecraft.world.level.block.Block)ModBlocks.GREATWOOD_LEAVES.get()).asItem()}
      );
      event.register(
         (stack, tintIndex) -> {
            if (tintIndex != 0) {
               return -1;
            } else {
               DyedItemColor dyedColor = (DyedItemColor)stack.get(DataComponents.DYED_COLOR);
               return dyedColor == null ? -1 : dyedColor.rgb();
            }
         },
         new ItemLike[]{
            (ItemLike)ModItems.CLOTH_BOOTS.get(),
            (ItemLike)ModItems.CLOTH_CHEST.get(),
            (ItemLike)ModItems.CLOTH_LEGGINGS.get(),
            (ItemLike)ModItems.VOID_ROBE_HELMET.get(),
            (ItemLike)ModItems.VOID_ROBE_CHEST.get(),
            (ItemLike)ModItems.VOID_ROBE_LEGGINGS.get()
         }
      );
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public Level getClientWorld() {
      return Minecraft.getInstance().level;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public boolean isShiftKeyDown() {
      return Screen.hasShiftDown();
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   private static ClientLevel asClient(Level level) {
      return level instanceof ClientLevel cl ? cl : null;
   }

   @OnlyIn(Dist.CLIENT)
   public static void add(Particle p) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.particleEngine != null) {
         if (mc.isSameThread()) {
            mc.particleEngine.add(p);
         } else {
            mc.execute(() -> mc.particleEngine.add(p));
         }
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public int particleCount(int base) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.options != null) {
         ParticleStatus st = (ParticleStatus)mc.options.particles().get();

         return switch (st) {
            case MINIMAL -> 0;
            case DECREASED -> base;
            case ALL -> base * 2;
            default -> throw new MatchException(null, null);
         };
      } else {
         return base;
      }
   }

   private static float rf(int rgb) {
      return (rgb >> 16 & 0xFF) / 255.0F;
   }

   private static float gf(int rgb) {
      return (rgb >> 8 & 0xFF) / 255.0F;
   }

   private static float bf(int rgb) {
      return (rgb & 0xFF) / 255.0F;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void blockSparkle(Level world, int x, int y, int z, int colorRGB, int count) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         float r = rf(colorRGB);
         float g = gf(colorRGB);
         float b = bf(colorRGB);

         for (int i = 0; i < this.particleCount(count); i++) {
            float rr = r;
            float gg = g;
            float bb = b;
            if (colorRGB == -9999) {
               rr = 0.33F + cl.random.nextFloat() * 0.67F;
               gg = 0.33F + cl.random.nextFloat() * 0.67F;
               bb = 0.33F + cl.random.nextFloat() * 0.67F;
            }

            this.drawGenericParticles(
               cl,
               x - 0.1F + cl.random.nextFloat() * 1.2F,
               y - 0.1F + cl.random.nextFloat() * 1.2F,
               z - 0.1F + cl.random.nextFloat() * 1.2F,
               0.0,
               cl.random.nextFloat() * 0.02,
               0.0,
               rr - 0.2F + cl.random.nextFloat() * 0.4F,
               gg - 0.2F + cl.random.nextFloat() * 0.4F,
               bb - 0.2F + cl.random.nextFloat() * 0.4F,
               0.9F,
               false,
               112,
               9,
               1,
               5 + cl.random.nextInt(8),
               cl.random.nextInt(10),
               0.7F + cl.random.nextFloat() * 0.4F
            );
         }
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void sparkle(float x, float y, float z, float size, int colorRGB, float gravity) {
      ClientLevel cl = Minecraft.getInstance().level;
      if (cl != null) {
         if (cl.random.nextInt(6) < this.particleCount(2)) {
            FXSparkle fx = new FXSparkle(cl, x, y, z, size, colorRGB, 6);
            fx.setGravity(gravity);
            add(fx);
         }
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void sparkle(float x, float y, float z, int colorRGB) {
      ClientLevel cl = Minecraft.getInstance().level;
      if (cl != null) {
         if (cl.random.nextInt(6) < this.particleCount(2)) {
            FXSparkle fx = new FXSparkle(cl, x, y, z, 1.5F, colorRGB, 6);
            add(fx);
         }
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void sparkle(float x, float y, float z, float tx, float ty, float tz, float size, int colorRGB, int multiplier, float gravity) {
      ClientLevel cl = Minecraft.getInstance().level;
      if (cl != null) {
         if (cl.random.nextInt(6) < this.particleCount(2)) {
            FXSparkle fx = new FXSparkle(cl, x, y, z, tx, ty, tz, size, colorRGB, multiplier);
            fx.setGravity(gravity);
            add(fx);
         }
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void spark(float x, float y, float z, float size, float r, float g, float b, float a) {
      ClientLevel cl = Minecraft.getInstance().level;
      if (cl != null) {
         FXSpark fx = new FXSpark(cl, x, y, z, size);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void smokeSpiral(Level world, double x, double y, double z, float rad, int start, int miny, int colorRGB) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXSmokeSpiral fx = new FXSmokeSpiral(cl, x, y, z, rad, start, miny);
         fx.setColor(rf(colorRGB), gf(colorRGB), bf(colorRGB));
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void crucibleBoilSound(Level world, int x, int y, int z) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         SoundEvent spill = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "spill"));
         if (spill != null) {
            cl.playLocalSound(x + 0.5, y + 0.5, z + 0.5, spill, SoundSource.BLOCKS, 0.2F, 1.0F, false);
         }
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void crucibleBoil(Level world, int x, int y, int z, CrucibleBlockEntity tile, int j) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         for (int a = 0; a < this.particleCount(1); a++) {
            FXBubble fb = new FXBubble(
               cl, x + 0.2F + cl.random.nextFloat() * 0.6F, y + 0.1F + tile.getFluidHeight(), z + 0.2F + cl.random.nextFloat() * 0.6F, 0.0, 0.0, 0.0, 3
            );
            if (tile.aspects.size() == 0) {
               fb.setRGB(1.0F, 1.0F, 1.0F);
            } else {
               int c = tile.aspects.getAspects()[cl.random.nextInt(tile.aspects.getAspects().length)].getColor();
               fb.setRGB(rf(c), gf(c), bf(c));
            }

            fb.bubblespeed = 0.003 * j;
            add(fb);
         }
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void crucibleBubble(Level world, float x, float y, float z, float cr, float cg, float cb) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBubble fb = new FXBubble(cl, x, y, z, 0.0, 0.0, 0.0, 1);
         fb.setRGB(cr, cg, cb);
         add(fb);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void crucibleFroth(Level world, float x, float y, float z) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBubble fb = new FXBubble(cl, x, y, z, 0.0, 0.0, 0.0, -4);
         fb.setRGB(0.5F, 0.5F, 0.7F);
         fb.setFroth();
         add(fb);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void crucibleFrothDown(Level world, float x, float y, float z) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBubble fb = new FXBubble(cl, x, y, z, 0.0, 0.0, 0.0, -4);
         fb.setRGB(0.5F, 0.5F, 0.7F);
         add(fb);
      }
   }

   @Override
   public void wispFX(Level world, double x, double y, double z, float r, float g, float b, float a) {
      this.wispFX(world, x, y, z, r, g, b, a, 0.02F);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void wispFX(Level world, double x, double y, double z, float r, float g, float b, float a, float gravity) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXWisp fx = new FXWisp(cl, x, y, z, r, g, b, a);
         fx.setGravity(gravity);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void wispFX2(Level world, double x, double y, double z, float size, int type, boolean shrink, boolean clip, float gravity) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXWisp fx = new FXWisp(cl, x, y, z, size, type);
         fx.setGravity(gravity);
         fx.shrink = shrink;
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void wispFXEG(Level world, double x, double y, double z, Entity target) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         for (int a = 0; a < this.particleCount(1); a++) {
            FXWispEG fx = new FXWispEG(cl, x, y, z, target);
            add(fx);
         }
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void wispFX3(Level world, double x, double y, double z, double x2, double y2, double z2, float size, int type, boolean shrink, float gravity) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXWisp fx = new FXWisp(cl, x, y, z, x2, y2, z2, size, type);
         fx.setGravity(gravity);
         fx.shrink = shrink;
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void wispFX4(Level world, double x, double y, double z, Entity target, int type, boolean shrink, float gravity) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXWisp fx = new FXWisp(cl, x, y, z, target, type);
         fx.setGravity(gravity);
         fx.shrink = shrink;
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void excavateFX(int x, int y, int z, Player p, int bi, int md, int progress) {
      LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
      levelRenderer.destroyBlockProgress(p.getId(), new BlockPos(x, y, z), progress);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void burst(Level world, double x, double y, double z, float size) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBurst fx = new FXBurst(cl, x, y, z, size);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void sourceStreamFX(Level world, double sx, double sy, double sz, float tx, float ty, float tz, int tagColor) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXWispArcing fx = new FXWispArcing(cl, tx, ty, tz, sx, sy, sz, 0.1F, rf(tagColor), gf(tagColor), bf(tagColor));
         fx.setGravity(0.0F);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void bolt(Level world, Entity source, Entity target) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXLightningBolt bolt = new FXLightningBolt(cl, source, target, cl.random.nextLong(), 4);
         bolt.defaultFractal();
         bolt.setType(0);
         bolt.finalizeBolt();
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void nodeBolt(Level world, float x, float y, float z, Entity target) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXLightningBolt bolt = new FXLightningBolt(cl, x, y, z, target.getX(), target.getY(), target.getZ(), cl.random.nextLong(), 10, 4.0F, 5);
         bolt.defaultFractal();
         bolt.setType(3);
         bolt.finalizeBolt();
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void nodeBolt(Level world, float x, float y, float z, float x2, float y2, float z2) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXLightningBolt bolt = new FXLightningBolt(cl, x, y, z, x2, y2, z2, cl.random.nextLong(), 10, 4.0F, 5);
         bolt.defaultFractal();
         bolt.setType(0);
         bolt.finalizeBolt();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void excavateFX(int x, int y, int z, Player p, int progress) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.levelRenderer != null) {
         mc.levelRenderer.destroyBlockProgress(p.getId(), new BlockPos(x, y, z), progress);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void beam(
      Level world, double sx, double sy, double sz, double tx, double ty, double tz, int type, int colorRGB, boolean reverse, float endmod, int age
   ) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBeam beam = new FXBeam(cl, sx, sy, sz, tx, ty, tz, rf(colorRGB), gf(colorRGB), bf(colorRGB), age);
         beam.setType(type);
         beam.setEndMod(endmod);
         beam.setReverse(reverse);
         beam.setPulse(false);
         add(beam);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public Object beamCont(
      Level world, Player p, double tx, double ty, double tz, int type, int colorRGB, boolean reverse, float endmod, Object input, int impact
   ) {
      ClientLevel cl = asClient(world);
      if (cl == null) {
         return input;
      } else {
         FXBeamWand beam = input instanceof FXBeamWand bw ? bw : null;
         if (beam != null && beam.isAlive()) {
            beam.updateBeam(tx, ty, tz);
            beam.setEndMod(endmod);
            beam.impact = impact;
            return beam;
         } else {
            beam = new FXBeamWand(cl, p, tx, ty, tz, rf(colorRGB), gf(colorRGB), bf(colorRGB), 8);
            beam.setType(type);
            beam.setEndMod(endmod);
            beam.setReverse(reverse);
            beam.impact = impact;
            add(beam);
            return beam;
         }
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public Object beamBore(
      Level world,
      double px,
      double py,
      double pz,
      double tx,
      double ty,
      double tz,
      int type,
      int colorRGB,
      boolean reverse,
      float endmod,
      Object input,
      int impact
   ) {
      ClientLevel cl = asClient(world);
      if (cl == null) {
         return input;
      } else {
         FXBeamBore beam = input instanceof FXBeamBore bb ? bb : null;
         if (beam != null && beam.isAlive()) {
            beam.updateBeam(tx, ty, tz);
            beam.setEndMod(endmod);
            beam.impact = impact;
            return beam;
         } else {
            beam = new FXBeamBore(cl, px, py, pz, tx, ty, tz, rf(colorRGB), gf(colorRGB), bf(colorRGB), 8);
            beam.setType(type);
            beam.setEndMod(endmod);
            beam.setReverse(reverse);
            beam.impact = impact;
            add(beam);
            return beam;
         }
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void boreDigFx(Level world, int x, int y, int z, int x2, int y2, int z2, net.minecraft.world.level.block.Block block, int md) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         if (cl.random.nextInt(10) == 0) {
            FXBoreSparkle fx = new FXBoreSparkle(
               cl, x + cl.random.nextFloat(), y + cl.random.nextFloat(), z + cl.random.nextFloat(), x2 + 0.5, y2 + 0.5, z2 + 0.5
            );
            add(fx);
         } else {
            FXBoreParticles fx = new FXBoreParticles(
               cl,
               (double)(x + cl.random.nextFloat()),
               (double)(y + cl.random.nextFloat()),
               (double)(z + cl.random.nextFloat()),
               x2 + 0.5,
               y2 + 0.5,
               z2 + 0.5,
               block.defaultBlockState(),
               Direction.from3DDataValue(cl.random.nextInt(6))
            );
            fx.setColor(x, y, z);
            add(fx);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void hungryNodeFX(Level world, int sx, int sy, int sz, int tx, int ty, int tz, net.minecraft.world.level.block.Block block) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBoreParticles fx = new FXBoreParticles(
            cl,
            (double)(sx + cl.random.nextFloat()),
            (double)(sy + cl.random.nextFloat()),
            (double)(sz + cl.random.nextFloat()),
            tx + 0.5,
            ty + 0.5,
            tz + 0.5,
            block.defaultBlockState(),
            Direction.from3DDataValue(cl.random.nextInt(6))
         );
         fx.setColor(sx, sy, sz);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void essenceTrailFx(Level world, int x, int y, int z, int x2, int y2, int z2, int count, int colorRGB, float scale) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXEssentiaTrail fx = new FXEssentiaTrail(cl, x + 0.5, y + 0.5, z + 0.5, x2 + 0.5, y2 + 0.5, z2 + 0.5, count, colorRGB, scale);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void soulTrail(Level world, Entity source, Entity target, float r, float g, float b) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         for (int a = 0; a < this.particleCount(2); a++) {
            double sx = source.getX() - source.getBbWidth() / 2.0 + cl.random.nextFloat() * source.getBbWidth();
            double sy = source.getY() + cl.random.nextFloat() * source.getBbHeight();
            double sz = source.getZ() - source.getBbWidth() / 2.0 + cl.random.nextFloat() * source.getBbWidth();
            if (cl.random.nextInt(10) == 0) {
               FXSparkleTrail st = new FXSparkleTrail(cl, sx, sy, sz, target, r, g, b);
               add(st);
            } else {
               FXSmokeTrail st = new FXSmokeTrail(cl, sx, sy, sz, target, r, g, b);
               add(st);
            }
         }
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void furnaceLavaFx(Level world, int x, int y, int z, int facingX, int facingZ) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         ;
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void blockRunes(Level world, double x, double y, double z, float r, float g, float b, int dur, float grav) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBlockRunes fx = new FXBlockRunes(cl, x + 0.5, y + 0.5, z + 0.5, r, g, b, dur);
         fx.setGravity(grav);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void blockWard(Level world, double x, double y, double z, Direction side, float r, float g, float b) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBlockWard fx = new FXBlockWard(cl, x + 0.5, y + 0.5, z + 0.5, side, r, g, b);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public Particle swarmParticleFX(Level world, Entity target, float f1, float f2, float pg) {
      ClientLevel cl = asClient(world);
      if (cl == null) {
         return null;
      } else {
         FXSwarm fx = new FXSwarm(
            cl,
            target.getX() + (cl.random.nextFloat() - cl.random.nextFloat()) * 2.0F,
            target.getY() + (cl.random.nextFloat() - cl.random.nextFloat()) * 2.0F,
            target.getZ() + (cl.random.nextFloat() - cl.random.nextFloat()) * 2.0F,
            target,
            0.8F + cl.random.nextFloat() * 0.2F,
            cl.random.nextFloat() * 0.4F,
            1.0F - cl.random.nextFloat() * 0.2F,
            f1,
            f2,
            pg
         );
         add(fx);
         return fx;
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void taintsplosionFX(Entity e) {
      ClientLevel cl = asClient(e.level());
      if (cl != null) {
         double yPos = e.getY() + cl.random.nextFloat() * e.getBbHeight();
         FXBreaking fx = new FXBreaking(cl, e.getX(), yPos, e.getZ(), Items.SLIME_BALL);
         if (cl.random.nextBoolean()) {
            fx.setColor(0.6F, 0.0F, 0.3F);
         } else {
            fx.setColor(0.3F, 0.0F, 0.3F);
         }

         double mx = Math.random() * 2.0 - 1.0;
         double my = Math.random() * 2.0 - 1.0;
         double mz = Math.random() * 2.0 - 1.0;
         float f = (float)(Math.random() + Math.random() + 1.0) * 0.15F;
         float len = Mth.sqrt((float)(mx * mx + my * my + mz * mz));
         fx.setParticleSpeed(mx / len * f * 0.964, my / len * f * 0.964 + 0.1F, mz / len * f * 0.964);
         fx.setLifetime((int)(66.0F / (cl.random.nextFloat() * 0.9F + 0.1F)));
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void splooshFX(Level world, int x, int y, int z) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         float ang = cl.random.nextFloat() * (float) Math.PI * 2.0F;
         float rad = cl.random.nextFloat() * 0.5F + 0.5F;
         float ox = Mth.sin(ang) * 2.0F * 0.5F * rad;
         float oz = Mth.cos(ang) * 2.0F * 0.5F * rad;
         FXBreaking fx = new FXBreaking(cl, x + ox + 0.5, (double)(y + cl.random.nextFloat()), z + oz + 0.5, Items.SLIME_BALL);
         if (cl.random.nextBoolean()) {
            fx.setColor(0.6F, 0.0F, 0.3F);
            fx.setAlpha(0.4F);
         } else {
            fx.setColor(0.3F, 0.0F, 0.3F);
            fx.setAlpha(0.6F);
         }

         fx.setParticleMaxAge((int)(66.0F / (cl.random.nextFloat() * 0.9F + 0.1F)));
         add(fx);
      }
   }

   public void taintsplosionFX(Level world, Entity e) {
   }

   @OnlyIn(Dist.CLIENT)
   public void tentacleAriseFX(Level world, Entity e) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         BlockPos under = BlockPos.containing(e.getX(), e.getY() - 1.0, e.getZ());
         BlockState state = cl.getBlockState(under);
         int loops = (int)(2.0F * e.getBbHeight());

         for (int j = 0; j < loops; j++) {
            float ang = cl.random.nextFloat() * (float) Math.PI * e.getBbHeight();
            float rad = cl.random.nextFloat() * 0.5F + 0.5F;
            float ox = Mth.sin(ang) * e.getBbHeight() * 0.25F * rad;
            float oz = Mth.cos(ang) * e.getBbHeight() * 0.25F * rad;
            FXBreaking fx = new FXBreaking(cl, e.getX() + ox, e.getY(), e.getZ() + oz, Items.SLIME_BALL);
            fx.setColor(0.4F, 0.0F, 0.4F);
            fx.setAlpha(0.5F);
            fx.setParticleMaxAge((int)(66.0F / (cl.random.nextFloat() * 0.9F + 0.1F)));
            add(fx);
            if (!state.isAir()) {
               cl.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), e.getX() + ox, e.getY(), e.getZ() + oz, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void slimeJumpFX(Level world, Entity e, int i) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         float ang = cl.random.nextFloat() * (float) Math.PI * 2.0F;
         float rad = cl.random.nextFloat() * 0.5F + 0.5F;
         float ox = Mth.sin(ang) * i * 0.5F * rad;
         float oz = Mth.cos(ang) * i * 0.5F * rad;
         AABB bb = e.getBoundingBox();
         double midY = (bb.minY + bb.maxY) / 2.0;
         FXBreaking fx = new FXBreaking(cl, e.getX() + ox, midY, e.getZ() + oz, Items.SLIME_BALL);
         fx.setColor(0.7F, 0.0F, 1.0F);
         fx.setAlpha(0.4F);
         fx.setParticleMaxAge((int)(66.0F / (cl.random.nextFloat() * 0.9F + 0.1F)));
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void dropletFX(Level world, float x, float y, float z, float r, float g, float b) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXDrop fx = new FXDrop(cl, x, y, z, r, g, b);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void splooshFX(Entity e) {
      ClientLevel cl = asClient(e.level());
      if (cl != null) {
         float f = cl.random.nextFloat() * (float) Math.PI * 2.0F;
         float f1 = cl.random.nextFloat() * 0.5F + 0.5F;
         float f2 = Mth.sin(f) * 2.0F * 0.5F * f1;
         float f3 = Mth.cos(f) * 2.0F * 0.5F * f1;
         FXBreaking fx = new FXBreaking(cl, e.getX() + f2, e.getY() + cl.random.nextFloat() * e.getBbHeight(), e.getZ() + f3, Items.SLIME_BALL);
         if (cl.random.nextBoolean()) {
            fx.setColor(0.6F, 0.0F, 0.3F);
         } else {
            fx.setColor(0.3F, 0.0F, 0.3F);
         }

         fx.setLifetime((int)(66.0F / (cl.random.nextFloat() * 0.9F + 0.1F)));
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void slimeJumpFX(Entity e, int i) {
      ClientLevel cl = asClient(e.level());
      if (cl != null) {
         float f = cl.random.nextFloat() * (float) Math.PI * 2.0F;
         float f1 = cl.random.nextFloat() * 0.5F + 0.5F;
         float f2 = Mth.sin(f) * i * 0.5F * f1;
         float f3 = Mth.cos(f) * i * 0.5F * f1;
         double midY = e.getBoundingBox().getCenter().y;
         FXBreaking fx = new FXBreaking(cl, e.getX() + f2, midY, e.getZ() + f3, Items.SLIME_BALL);
         fx.setColor(0.7F, 0.0F, 1.0F);
         fx.setParticleMaxAge((int)(66.0F / (cl.random.nextFloat() * 0.9F + 0.1F)));
         add(fx);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void taintLandFX(Level world, Entity e) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         float ang = cl.random.nextFloat() * (float) Math.PI * 2.0F;
         float rad = cl.random.nextFloat() * 0.5F + 0.5F;
         float ox = Mth.sin(ang) * 2.0F * 0.5F * rad;
         float oz = Mth.cos(ang) * 2.0F * 0.5F * rad;
         AABB bb = e.getBoundingBox();
         double midY = (bb.minY + bb.maxY) / 2.0;
         FXBreaking fx = new FXBreaking(cl, e.getX() + ox, midY, e.getZ() + oz, Items.SLIME_BALL);
         fx.setColor(0.1F, 0.0F, 0.1F);
         fx.setAlpha(0.4F);
         fx.setParticleMaxAge((int)(66.0F / (cl.random.nextFloat() * 0.9F + 0.1F)));
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void hungryNodeFX(Level world, int sx, int sy, int sz, int tx, int ty, int tz, net.minecraft.world.level.block.Block block, int md) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBoreParticles fx = new FXBoreParticles(
            cl,
            (double)(sx + cl.random.nextFloat()),
            (double)(sy + cl.random.nextFloat()),
            (double)(sz + cl.random.nextFloat()),
            tx + 0.5,
            ty + 0.5,
            tz + 0.5,
            block.defaultBlockState(),
            Direction.from3DDataValue(cl.random.nextInt(6))
         );
         fx.setColor(sx, sy, sz);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void drawInfusionParticles1(Level world, double x, double y, double z, int x2, int y2, int z2, net.minecraft.world.item.Item item) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBoreParticles fx = new FXBoreParticles(cl, x, y, z, x2 + 0.5, y2 - 0.5, z2 + 0.5, item.asItem(), Direction.from3DDataValue(cl.random.nextInt(6)));
         fx.setColor(x2, y2, z2);
         fx.setAlpha(0.3F);
         fx.setParticleSpeed((float)cl.random.nextGaussian() * 0.03F, (float)cl.random.nextGaussian() * 0.03F, (float)cl.random.nextGaussian() * 0.03F);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void drawInfusionParticles2(Level world, double x, double y, double z, int x2, int y2, int z2, net.minecraft.world.level.block.Block block) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBoreParticles fx = new FXBoreParticles(
            cl, x, y, z, x2 + 0.5, y2 - 0.5, z2 + 0.5, block.defaultBlockState(), Direction.from3DDataValue(cl.random.nextInt(6))
         );
         fx.setColor(x2, y2, z2);
         fx.setAlpha(0.3F);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void drawInfusionParticles3(Level world, double x, double y, double z, int x2, int y2, int z2) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBoreSparkle fx = new FXBoreSparkle(cl, x, y, z, x2 + 0.5, y2 - 0.5, z2 + 0.5);
         fx.setColor(0.4F + cl.random.nextFloat() * 0.2F, 0.2F, 0.6F + cl.random.nextFloat() * 0.3F);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void drawInfusionParticles4(Level world, double x, double y, double z, int x2, int y2, int z2) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXBoreSparkle fx = new FXBoreSparkle(cl, x, y, z, x2 + 0.5, y2 - 0.5, z2 + 0.5);
         fx.setColor(0.2F, 0.6F + cl.random.nextFloat() * 0.3F, 0.3F);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void drawVentParticles(Level world, double x, double y, double z, double x2, double y2, double z2, int colorRGB) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXVent fx = new FXVent(cl, x, y, z, x2, y2, z2, colorRGB);
         fx.setAlpha(0.4F);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void drawVentParticles(Level world, double x, double y, double z, double x2, double y2, double z2, int colorRGB, float scale) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXVent fx = new FXVent(cl, x, y, z, x2, y2, z2, colorRGB);
         fx.setAlpha(0.4F);
         fx.setScale(scale);
         add(fx);
      }
   }

   @Override
   public void bottleTaintBreak(Level level, double x, double y, double z) {
      level.playSound(null, x, y, z, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void drawGenericParticles(
      Level world,
      double x,
      double y,
      double z,
      double xd,
      double yd,
      double zd,
      float r,
      float g,
      float b,
      float alpha,
      boolean loop,
      int start,
      int num,
      int inc,
      int age,
      int delay,
      float scale
   ) {
      ClientLevel cl = asClient(world);
      if (cl != null) {
         FXGeneric fx = new FXGeneric(cl, x, y, z, xd, yd, zd);
         fx.setMaxAge(age, delay);
         fx.setColor(r, g, b);
         fx.setAlpha(alpha);
         fx.setLoop(loop);
         fx.setParticles(start, num, inc);
         fx.setScale(scale);
         add(fx);
      }
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public Object beamPower(
      Level world, double px, double py, double pz, double tx, double ty, double tz, float r, float g, float b, boolean pulse, Object input
   ) {
      ClientLevel cl = asClient(world);
      if (cl == null) {
         return input;
      } else {
         FXBeamPower beam = input instanceof FXBeamPower bp ? bp : null;
         if (beam != null && beam.isAlive()) {
            beam.updateBeam(px, py, pz, tx, ty, tz);
            beam.setPulse(pulse, r, g, b);
            return beam;
         } else {
            beam = new FXBeamPower(cl, px, py, pz, tx, ty, tz, r, g, b, 8);
            add(beam);
            add(new FXBeamPower.Flare(cl, beam));
            return beam;
         }
      }
   }
}
