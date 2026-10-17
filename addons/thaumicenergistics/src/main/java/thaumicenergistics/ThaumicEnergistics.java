package thaumicenergistics;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.features.GridLinkables;
import appeng.api.parts.PartModels;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageCells;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import appeng.menu.SlotSemantic;
import thaumicenergistics.ae2.recipe.AssemblerPattern;
import thaumicenergistics.ae2.recipe.AssemblerRecipes;
import thaumicenergistics.ae2.storage.AEAspectKey;
import thaumicenergistics.ae2.storage.AEAspectKeyType;
import thaumicenergistics.ae2.storage.CreativeEssentiaCellHandler;
import thaumicenergistics.ae2.strategy.AspectContainerItemStrategy;
import thaumicenergistics.ae2.strategy.CreateStrategyUtil;
import thaumicenergistics.entity.WirelessGolemHandler;
import thaumicenergistics.init.ModBlockEntities;
import thaumicenergistics.init.ModBlocks;
import thaumicenergistics.init.ModCapabilities;
import thaumicenergistics.init.ModCreativeTabs;
import thaumicenergistics.init.ModItems;
import thaumicenergistics.init.ModMenuTypes;
import thaumicenergistics.init.ModSlotSemantics;
import thaumicenergistics.item.ItemGolemWirelessBackpack;
import thaumicenergistics.network.ThEPacketHandler;
import thaumicenergistics.part.PartArcaneTerminal;
import thaumicenergistics.part.PartVisInterface;
import thaumicenergistics.research.PseudoResearchItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.entities.smart.golem.hook.GolemHooks;

@Mod("thaumicenergistics")
public class ThaumicEnergistics {
   public static final String MODID = "thaumicenergistics";
   public static final Logger LOGGER = LogManager.getLogger();

   public static ResourceLocation location(String path) {
      return ResourceLocation.fromNamespaceAndPath("thaumicenergistics", path);
   }

   public ThaumicEnergistics(IEventBus bus) {
      SlotSemantic ignored = ModSlotSemantics.WAND;
      SlotSemantic ignored2 = ModSlotSemantics.ARCANE_ARMOR;
      ModBlocks.init(bus);
      ModBlockEntities.init(bus);
      ModItems.init(bus);
      ModMenuTypes.init(bus);
      ModCreativeTabs.init(bus);
      bus.addListener(ThEPacketHandler::init);
      bus.addListener(ModCapabilities::register);
      ResearchApi.registerCategory("thaumicenergistics", location("textures/misc/tab_icon.png"), location("textures/misc/research_background.png"), 8959743);
      ResearchApi.registerResearchItemType(location("pseudo"), PseudoResearchItem::new);
      StackExportStrategy.register(AEAspectKeyType.INSTANCE, CreateStrategyUtil::createExport);
      StackImportStrategy.register(AEAspectKeyType.INSTANCE, CreateStrategyUtil::createImport);
      ExternalStorageStrategy.register(AEAspectKeyType.INSTANCE, CreateStrategyUtil::createExternal);
      ContainerItemStrategies.register(AEAspectKeyType.INSTANCE, AEAspectKey.class, AspectContainerItemStrategy.INSTANCE);
      PartModels.registerModels(new ResourceLocation[]{PartVisInterface.MODEL, PartArcaneTerminal.MODEL_OFF, PartArcaneTerminal.MODEL_ON});
      bus.addListener(this::onRegister);
      bus.addListener(this::onCommonSetup);
      NeoForge.EVENT_BUS.addListener(ThaumicEnergistics::onDatapackSync);
      LOGGER.info("Thaumic Energistics initialized");
   }

   private void onRegister(RegisterEvent event) {
      if (event.getRegistryKey().equals(Registries.BLOCK)) {
         AEKeyTypes.register(AEAspectKeyType.INSTANCE);
      }
   }

   private static void onDatapackSync(OnDatapackSyncEvent event) {
      // null player = /reload: recipes may have changed, every assembler re-offers its patterns
      if (event.getPlayer() == null) {
         AssemblerRecipes.refreshAll();
      }
   }

   private void onCommonSetup(FMLCommonSetupEvent event) {
      PatternDetailsHelper.registerDecoder(new AssemblerPattern.Decoder());
      event.enqueueWork(() -> {
         StorageCells.addCellHandler(CreativeEssentiaCellHandler.INSTANCE);
         Upgrades.add(AEItems.SPEED_CARD, (ItemLike)ModBlocks.ARCANE_ASSEMBLER.get(), 4);
         Upgrades.add((ItemLike)ModItems.VIS_UPGRADE.get(), (ItemLike)ModBlocks.ARCANE_ASSEMBLER.get(), 1);
         Upgrades.add(AEItems.SPEED_CARD, (ItemLike)ModBlocks.ARCANE_ASSEMBLER_1.get(), 4);
         Upgrades.add((ItemLike)ModItems.VIS_UPGRADE.get(), (ItemLike)ModBlocks.ARCANE_ASSEMBLER_1.get(), 1);
         Upgrades.add(AEItems.SPEED_CARD, (ItemLike)ModBlocks.ARCANE_ASSEMBLER_2.get(), 4);
         Upgrades.add((ItemLike)ModItems.VIS_UPGRADE.get(), (ItemLike)ModBlocks.ARCANE_ASSEMBLER_2.get(), 1);
         Upgrades.add(AEItems.SPEED_CARD, (ItemLike)ModBlocks.ARCANE_ASSEMBLER_3.get(), 4);
         Upgrades.add((ItemLike)ModItems.VIS_UPGRADE.get(), (ItemLike)ModBlocks.ARCANE_ASSEMBLER_3.get(), 1);
         Upgrades.add(AEItems.SPEED_CARD, (ItemLike)ModBlocks.INFUSION_ASSEMBLER.get(), 4);
         Upgrades.add(AEItems.SPEED_CARD, (ItemLike)ModBlocks.CRUCIBLE_ASSEMBLER.get(), 5);
         GolemHooks.registerHandler(WirelessGolemHandler.getInstance());
         GridLinkables.register((ItemLike)ModItems.GOLEM_WIFI_BACKPACK.get(), ItemGolemWirelessBackpack.LINKABLE_HANDLER);
      });
   }
}
