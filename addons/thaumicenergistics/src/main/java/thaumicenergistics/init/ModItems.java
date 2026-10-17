package thaumicenergistics.init;

import appeng.core.definitions.AEItems;
import java.util.function.Function;
import java.util.function.Supplier;
import thaumicenergistics.ae2.pattern.ArcanePatternItem;
import thaumicenergistics.ae2.pattern.CruciblePatternItem;
import thaumicenergistics.ae2.pattern.InfusionPatternItem;
import thaumicenergistics.ae2.recipe.RecipePatternItem;
import thaumicenergistics.item.ItemBrainCore;
import thaumicenergistics.item.ItemCreativeEssentiaCell;
import thaumicenergistics.item.ItemCrucibleCore;
import thaumicenergistics.item.ItemEssentiaStorageCell;
import thaumicenergistics.item.ItemFocusAEWrench;
import thaumicenergistics.item.ItemGolemWirelessBackpack;
import thaumicenergistics.item.ItemKnowledgeCore;
import thaumicenergistics.item.ItemMaterial;
import thaumicenergistics.item.ItemStorageCasing;
import thaumicenergistics.item.ItemVisUpgrade;
import thaumicenergistics.item.ThEPartItem;
import thaumicenergistics.part.PartArcaneTerminal;
import thaumicenergistics.part.PartVisInterface;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

public class ModItems {
   public static final Items REGISTER = DeferredRegister.createItems("thaumicenergistics");
   public static final DeferredItem<BlockItem> ESSENTIA_PROVIDER = registerBlock("essentia_provider", ModBlocks.ESSENTIA_PROVIDER);
   public static final DeferredItem<BlockItem> INFUSION_PROVIDER = registerBlock("infusion_provider", ModBlocks.INFUSION_PROVIDER);
   public static final DeferredItem<BlockItem> GEAR_BOX = registerBlock("gear_box", ModBlocks.GEAR_BOX);
   public static final DeferredItem<BlockItem> GOLEM_GEAR_BOX = registerBlock("golem_gear_box", ModBlocks.GOLEM_GEAR_BOX);
   public static final DeferredItem<BlockItem> INDUSTRIAL_ALCHEMY_FURNACE = registerBlock("industrial_alchemy_furnace", ModBlocks.INDUSTRIAL_ALCHEMY_FURNACE);
   public static final DeferredItem<BlockItem> INFUSION_INSCRIBER = registerBlock("infusion_inscriber", ModBlocks.INFUSION_INSCRIBER);
   public static final DeferredItem<BlockItem> KNOWLEDGE_INSCRIBER = registerBlock("knowledge_inscriber", ModBlocks.KNOWLEDGE_INSCRIBER);
   public static final DeferredItem<BlockItem> INFUSION_ASSEMBLER = registerBlock("infusion_assembler", ModBlocks.INFUSION_ASSEMBLER);
   public static final DeferredItem<BlockItem> ARCANE_ASSEMBLER = registerBlock("arcane_assembler", ModBlocks.ARCANE_ASSEMBLER);
   public static final DeferredItem<BlockItem> ARCANE_ASSEMBLER_1 = registerBlock("arcane_assembler_1", ModBlocks.ARCANE_ASSEMBLER_1);
   public static final DeferredItem<BlockItem> ARCANE_ASSEMBLER_2 = registerBlock("arcane_assembler_2", ModBlocks.ARCANE_ASSEMBLER_2);
   public static final DeferredItem<BlockItem> ARCANE_ASSEMBLER_3 = registerBlock("arcane_assembler_3", ModBlocks.ARCANE_ASSEMBLER_3);
   public static final DeferredItem<BlockItem> CRUCIBLE_ASSEMBLER = registerBlock("crucible_assembler", ModBlocks.CRUCIBLE_ASSEMBLER);
   public static final DeferredItem<BlockItem> CRUCIBLE_INSCRIBER = registerBlock("crucible_inscriber", ModBlocks.CRUCIBLE_INSCRIBER);
   public static final DeferredItem<Item> BRAIN_CORE = REGISTER.register("brain_core", ItemBrainCore::new);
   public static final DeferredItem<Item> INFUSION_PATTERN = REGISTER.register("infusion_pattern", InfusionPatternItem::new);
   public static final DeferredItem<Item> KNOWLEDGE_CORE = REGISTER.register("knowledge_core", ItemKnowledgeCore::new);
   public static final DeferredItem<Item> ARCANE_PATTERN = REGISTER.register("arcane_pattern", ArcanePatternItem::new);
   public static final DeferredItem<Item> CRUCIBLE_PATTERN = REGISTER.register("crucible_pattern", CruciblePatternItem::new);
   public static final DeferredItem<Item> RECIPE_PATTERN = REGISTER.register("recipe_pattern", RecipePatternItem::new);
   public static final DeferredItem<Item> CRUCIBLE_CORE = REGISTER.register("crucible_core", ItemCrucibleCore::new);
   public static final DeferredItem<Item> ESSENTIA_CELL_CASING = REGISTER.register("essentia_cell_casing", ItemStorageCasing::new);
   public static final DeferredItem<Item> ESSENTIA_CELL_1K = REGISTER.register(
      "essentia_cell_1k", () -> new ItemEssentiaStorageCell(AEItems.CELL_COMPONENT_1K, (ItemLike)ESSENTIA_CELL_CASING.get(), 0.5, 1, 8, 12)
   );
   public static final DeferredItem<Item> ESSENTIA_CELL_4K = REGISTER.register(
      "essentia_cell_4k", () -> new ItemEssentiaStorageCell(AEItems.CELL_COMPONENT_4K, (ItemLike)ESSENTIA_CELL_CASING.get(), 1.0, 4, 8, 12)
   );
   public static final DeferredItem<Item> ESSENTIA_CELL_16K = REGISTER.register(
      "essentia_cell_16k", () -> new ItemEssentiaStorageCell(AEItems.CELL_COMPONENT_16K, (ItemLike)ESSENTIA_CELL_CASING.get(), 1.5, 16, 8, 12)
   );
   public static final DeferredItem<Item> ESSENTIA_CELL_64K = REGISTER.register(
      "essentia_cell_64k", () -> new ItemEssentiaStorageCell(AEItems.CELL_COMPONENT_64K, (ItemLike)ESSENTIA_CELL_CASING.get(), 2.0, 64, 8, 12)
   );
   public static final DeferredItem<Item> ESSENTIA_CELL_256K = REGISTER.register(
      "essentia_cell_256k", () -> new ItemEssentiaStorageCell(AEItems.CELL_COMPONENT_256K, (ItemLike)ESSENTIA_CELL_CASING.get(), 2.5, 256, 8, 12)
   );
   public static final DeferredItem<Item> ESSENTIA_CELL_1M = REGISTER.register(
      "essentia_cell_1m", () -> new ItemEssentiaStorageCell(AEItems.CELL_COMPONENT_1K, (ItemLike)ESSENTIA_CELL_CASING.get(), 3.0, 1024, 8, 12)
   );
   public static final DeferredItem<Item> ESSENTIA_CELL_4M = REGISTER.register(
      "essentia_cell_4m", () -> new ItemEssentiaStorageCell(AEItems.CELL_COMPONENT_4K, (ItemLike)ESSENTIA_CELL_CASING.get(), 3.5, 4096, 8, 12)
   );
   public static final DeferredItem<Item> ESSENTIA_CELL_16M = REGISTER.register(
      "essentia_cell_16m", () -> new ItemEssentiaStorageCell(AEItems.CELL_COMPONENT_16K, (ItemLike)ESSENTIA_CELL_CASING.get(), 4.0, 16384, 8, 12)
   );
   public static final DeferredItem<Item> ESSENTIA_CELL_CREATIVE = REGISTER.register(
      "essentia_cell_creative", () -> new ItemCreativeEssentiaCell((ItemLike)ESSENTIA_CELL_CASING.get())
   );
   public static final DeferredItem<Item> VIS_UPGRADE = REGISTER.register("vis_upgrade", ItemVisUpgrade::new);
   public static final DeferredItem<Item> FOCUS_AEWRENCH = REGISTER.register("focus_aewrench", ItemFocusAEWrench::new);
   public static final DeferredItem<Item> DIFFUSION_CORE = REGISTER.register("diffusion_core", () -> new ItemMaterial(ItemMaterial.MaterialType.DIFFUSION_CORE));
   public static final DeferredItem<Item> COALESCENCE_CORE = REGISTER.register(
      "coalescence_core", () -> new ItemMaterial(ItemMaterial.MaterialType.COALESCENCE_CORE)
   );
   public static final DeferredItem<Item> IRON_GEAR = REGISTER.register("iron_gear", () -> new ItemMaterial(ItemMaterial.MaterialType.IRON_GEAR));
   public static final DeferredItem<Item> GOLEM_WIFI_BACKPACK = REGISTER.register("golem_wifi_backpack", ItemGolemWirelessBackpack::new);
   public static final DeferredItem<Item> VIS_INTERFACE = REGISTER.register(
      "vis_interface", () -> new ThEPartItem<>(new Properties().stacksTo(64), PartVisInterface.class, PartVisInterface::new)
   );
   public static final DeferredItem<Item> ARCANE_TERMINAL = REGISTER.register(
      "arcane_terminal", () -> new ThEPartItem<>(new Properties().stacksTo(64), PartArcaneTerminal.class, PartArcaneTerminal::new)
   );

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
   }

   private static <B extends Block> DeferredItem<BlockItem> registerBlock(String name, Supplier<B> block) {
      return registerBlock(name, block, b -> new BlockItem(b, new Properties()));
   }

   private static <I extends BlockItem, B extends Block> DeferredItem<I> registerBlock(String name, Supplier<B> block, Function<B, I> function) {
      return REGISTER.register(name, () -> function.apply(block.get()));
   }
}
