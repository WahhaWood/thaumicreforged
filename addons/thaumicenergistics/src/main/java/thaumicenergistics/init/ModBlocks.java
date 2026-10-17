package thaumicenergistics.init;

import thaumicenergistics.block.arcane_assembler.AdvancedArcaneAssemblerBlock;
import thaumicenergistics.block.arcane_assembler.ArcaneAssemblerBlock;
import thaumicenergistics.block.crucible_assembler.CrucibleAssemblerBlock;
import thaumicenergistics.block.crucible_inscriber.CrucibleInscriberBlock;
import thaumicenergistics.block.gearbox.GearBoxBlock;
import thaumicenergistics.block.gearbox.GolemGearBoxBlock;
import thaumicenergistics.block.industrial_alchemy_furnace.IndustrialAlchemyFurnaceBlock;
import thaumicenergistics.block.infusion_assembler.InfusionAssemblerBlock;
import thaumicenergistics.block.infusion_inscriber.InfusionInscriberBlock;
import thaumicenergistics.block.knowledge_inscriber.KnowledgeInscriberBlock;
import thaumicenergistics.block.provider.EssentiaProviderBlock;
import thaumicenergistics.block.provider.InfusionProviderBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public class ModBlocks {
   public static final Blocks REGISTER = DeferredRegister.createBlocks("thaumicenergistics");
   public static final DeferredBlock<Block> ESSENTIA_PROVIDER = REGISTER.register("essentia_provider", EssentiaProviderBlock::new);
   public static final DeferredBlock<Block> INFUSION_PROVIDER = REGISTER.register("infusion_provider", InfusionProviderBlock::new);
   public static final DeferredBlock<Block> GEAR_BOX = REGISTER.register("gear_box", GearBoxBlock::new);
   public static final DeferredBlock<Block> GOLEM_GEAR_BOX = REGISTER.register("golem_gear_box", GolemGearBoxBlock::new);
   public static final DeferredBlock<Block> INDUSTRIAL_ALCHEMY_FURNACE = REGISTER.register("industrial_alchemy_furnace", IndustrialAlchemyFurnaceBlock::new);
   public static final DeferredBlock<Block> INFUSION_INSCRIBER = REGISTER.register("infusion_inscriber", InfusionInscriberBlock::new);
   public static final DeferredBlock<Block> KNOWLEDGE_INSCRIBER = REGISTER.register("knowledge_inscriber", KnowledgeInscriberBlock::new);
   public static final DeferredBlock<Block> INFUSION_ASSEMBLER = REGISTER.register("infusion_assembler", InfusionAssemblerBlock::new);
   public static final DeferredBlock<Block> ARCANE_ASSEMBLER = REGISTER.register("arcane_assembler", ArcaneAssemblerBlock::new);
   public static final DeferredBlock<Block> ARCANE_ASSEMBLER_1 = REGISTER.register("arcane_assembler_1", () -> new AdvancedArcaneAssemblerBlock(1));
   public static final DeferredBlock<Block> ARCANE_ASSEMBLER_2 = REGISTER.register("arcane_assembler_2", () -> new AdvancedArcaneAssemblerBlock(2));
   public static final DeferredBlock<Block> ARCANE_ASSEMBLER_3 = REGISTER.register("arcane_assembler_3", () -> new AdvancedArcaneAssemblerBlock(3));
   public static final DeferredBlock<Block> CRUCIBLE_ASSEMBLER = REGISTER.register("crucible_assembler", CrucibleAssemblerBlock::new);
   public static final DeferredBlock<Block> CRUCIBLE_INSCRIBER = REGISTER.register("crucible_inscriber", CrucibleInscriberBlock::new);

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
   }
}
