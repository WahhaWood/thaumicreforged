package thaumicenergistics.client;

import appeng.api.client.AEKeyRendering;
import appeng.items.storage.BasicStorageCell;
import thaumicenergistics.ae2.recipe.AssemblerRecipes;
import thaumicenergistics.ae2.storage.AEAspectKey;
import thaumicenergistics.ae2.storage.AEAspectKeyType;
import thaumicenergistics.client.render.ArcaneAssemblerBlockEntityRenderer;
import thaumicenergistics.client.render.ArcaneAssemblerModel;
import thaumicenergistics.client.render.AspectKeyRenderHandler;
import thaumicenergistics.client.render.GearBoxBlockEntityRenderer;
import thaumicenergistics.client.render.GearBoxModel;
import thaumicenergistics.client.screen.ArcaneAssemblerScreen;
import thaumicenergistics.client.screen.ArcaneCraftingTermScreen;
import thaumicenergistics.client.screen.CrucibleAssemblerScreen;
import thaumicenergistics.client.screen.CrucibleInscriberScreen;
import thaumicenergistics.client.screen.IndustrialAlchemyFurnaceScreen;
import thaumicenergistics.client.screen.InfusionAssemblerScreen;
import thaumicenergistics.client.screen.InfusionInscriberScreen;
import thaumicenergistics.client.screen.KnowledgeInscriberScreen;
import thaumicenergistics.init.ModBlockEntities;
import thaumicenergistics.init.ModItems;
import thaumicenergistics.init.ModMenuTypes;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(
   modid = "thaumicenergistics",
   value = {Dist.CLIENT}
)
public class ClientRegistration {
   @SubscribeEvent
   public static void registerLayerDefinitions(RegisterLayerDefinitions event) {
      event.registerLayerDefinition(GearBoxModel.LAYER_LOCATION, GearBoxModel::createBodyLayer);
      event.registerLayerDefinition(ArcaneAssemblerModel.LAYER_LOCATION, ArcaneAssemblerModel::createBodyLayer);
   }

   @SubscribeEvent
   public static void registerBlockEntityRenderers(RegisterRenderers event) {
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.GEAR_BOX.get(), GearBoxBlockEntityRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.ARCANE_ASSEMBLER.get(), ArcaneAssemblerBlockEntityRenderer::new);
   }

   @SubscribeEvent
   public static void registerLayerDefinitions(RegisterClientExtensionsEvent event) {
      event.registerItem(ItemBlockEntityRenderer.getInstance(), new Holder[]{ModItems.GEAR_BOX, ModItems.GOLEM_GEAR_BOX, ModItems.ARCANE_ASSEMBLER});
   }

   @SubscribeEvent
   public static void registerScreens(RegisterMenuScreensEvent event) {
      event.register((MenuType)ModMenuTypes.INDUSTRIAL_ALCHEMY_FURNACE.get(), IndustrialAlchemyFurnaceScreen::new);
      event.register((MenuType)ModMenuTypes.INFUSION_INSCRIBER.get(), InfusionInscriberScreen::new);
      event.register((MenuType)ModMenuTypes.KNOWLEDGE_INSCRIBER.get(), KnowledgeInscriberScreen::new);
      event.register((MenuType)ModMenuTypes.ARCANE_CRAFTING_TERM.get(), ArcaneCraftingTermScreen::new);
      event.register((MenuType)ModMenuTypes.INFUSION_ASSEMBLER.get(), InfusionAssemblerScreen::new);
      event.register((MenuType)ModMenuTypes.ARCANE_ASSEMBLER.get(), ArcaneAssemblerScreen::new);
      event.register((MenuType)ModMenuTypes.CRUCIBLE_ASSEMBLER.get(), CrucibleAssemblerScreen::new);
      event.register((MenuType)ModMenuTypes.CRUCIBLE_INSCRIBER.get(), CrucibleInscriberScreen::new);
   }

   /** The server synced its recipes: assemblers rebuild the recipe lists their GUIs show. */
   @SubscribeEvent
   public static void onRecipesUpdated(RecipesUpdatedEvent event) {
      AssemblerRecipes.invalidate();
   }

   @SubscribeEvent
   public static void registerKeyRendering(FMLClientSetupEvent event) {
      event.enqueueWork(() -> AEKeyRendering.register(AEAspectKeyType.INSTANCE, AEAspectKey.class, new AspectKeyRenderHandler()));
   }

   @SubscribeEvent
   public static void registerItemColors(Item event) {
      event.register(
         (stack, tintIndex) -> ARGB32.opaque(BasicStorageCell.getColor(stack, tintIndex)),
         new ItemLike[]{
            (ItemLike)ModItems.ESSENTIA_CELL_1K.get(),
            (ItemLike)ModItems.ESSENTIA_CELL_4K.get(),
            (ItemLike)ModItems.ESSENTIA_CELL_16K.get(),
            (ItemLike)ModItems.ESSENTIA_CELL_64K.get(),
            (ItemLike)ModItems.ESSENTIA_CELL_256K.get(),
            (ItemLike)ModItems.ESSENTIA_CELL_1M.get(),
            (ItemLike)ModItems.ESSENTIA_CELL_4M.get(),
            (ItemLike)ModItems.ESSENTIA_CELL_16M.get(),
            (ItemLike)ModItems.ESSENTIA_CELL_CREATIVE.get()
         }
      );
      event.register(
         (stack, tintIndex) -> tintIndex == 1 ? ARGB32.opaque(Mth.hsvToRgb((float)(System.currentTimeMillis() % 10000L) / 10000.0F, 1.0F, 1.0F)) : -1,
         new ItemLike[]{(ItemLike)ModItems.VIS_UPGRADE.get()}
      );
   }
}
