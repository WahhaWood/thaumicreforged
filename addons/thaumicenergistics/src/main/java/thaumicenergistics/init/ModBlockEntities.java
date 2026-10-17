package thaumicenergistics.init;

import java.util.Arrays;
import java.util.Collection;
import thaumicenergistics.block.arcane_assembler.ArcaneAssemblerBlockEntity;
import thaumicenergistics.block.arcane_assembler.ArcaneAssemblerBlockEntityFactory;
import thaumicenergistics.block.crucible_assembler.CrucibleAssemblerBlockEntity;
import thaumicenergistics.block.crucible_inscriber.CrucibleInscriberBlockEntity;
import thaumicenergistics.block.gearbox.entity.GearBoxBlockEntity;
import thaumicenergistics.block.industrial_alchemy_furnace.IndustrialAlchemyFurnaceBlockEntity;
import thaumicenergistics.block.infusion_assembler.InfusionAssemblerBlockEntity;
import thaumicenergistics.block.infusion_inscriber.InfusionInscriberBlockEntity;
import thaumicenergistics.block.knowledge_inscriber.KnowledgeInscriberBlockEntity;
import thaumicenergistics.block.provider.EssentiaProviderBlockEntity;
import thaumicenergistics.block.provider.InfusionProviderBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
   public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "thaumicenergistics");
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssentiaProviderBlockEntity>> ESSENTIA_PROVIDER = register(
      "essentia_provider", EssentiaProviderBlockEntity::new, ModBlocks.ESSENTIA_PROVIDER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfusionProviderBlockEntity>> INFUSION_PROVIDER = register(
      "infusion_provider", InfusionProviderBlockEntity::new, ModBlocks.INFUSION_PROVIDER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GearBoxBlockEntity>> GEAR_BOX = register(
      "gear_box", GearBoxBlockEntity::new, ModBlocks.GEAR_BOX, ModBlocks.GOLEM_GEAR_BOX
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IndustrialAlchemyFurnaceBlockEntity>> INDUSTRIAL_ALCHEMY_FURNACE = register(
      "industrial_alchemy_furnace", IndustrialAlchemyFurnaceBlockEntity::new, ModBlocks.INDUSTRIAL_ALCHEMY_FURNACE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfusionInscriberBlockEntity>> INFUSION_INSCRIBER = register(
      "infusion_inscriber", InfusionInscriberBlockEntity::new, ModBlocks.INFUSION_INSCRIBER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KnowledgeInscriberBlockEntity>> KNOWLEDGE_INSCRIBER = register(
      "knowledge_inscriber", KnowledgeInscriberBlockEntity::new, ModBlocks.KNOWLEDGE_INSCRIBER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfusionAssemblerBlockEntity>> INFUSION_ASSEMBLER = register(
      "infusion_assembler", InfusionAssemblerBlockEntity::new, ModBlocks.INFUSION_ASSEMBLER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneAssemblerBlockEntity>> ARCANE_ASSEMBLER = register(
      "arcane_assembler",
      ArcaneAssemblerBlockEntityFactory::create,
      ModBlocks.ARCANE_ASSEMBLER,
      ModBlocks.ARCANE_ASSEMBLER_1,
      ModBlocks.ARCANE_ASSEMBLER_2,
      ModBlocks.ARCANE_ASSEMBLER_3
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleAssemblerBlockEntity>> CRUCIBLE_ASSEMBLER = register(
      "crucible_assembler", CrucibleAssemblerBlockEntity::new, ModBlocks.CRUCIBLE_ASSEMBLER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleInscriberBlockEntity>> CRUCIBLE_INSCRIBER = register(
      "crucible_inscriber", CrucibleInscriberBlockEntity::new, ModBlocks.CRUCIBLE_INSCRIBER
   );

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
   }

   private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
      String name, BlockEntitySupplier<T> supplier, DeferredHolder<Block, ? extends Block>... blocks
   ) {
      return REGISTER.register(name, () -> Builder.of(supplier, Arrays.stream(blocks).map(DeferredHolder::get).toArray(Block[]::new)).build(null));
   }

   private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
      String name, BlockEntitySupplier<T> supplier, Collection<DeferredHolder<Block, Block>> collection
   ) {
      return REGISTER.register(name, () -> Builder.of(supplier, collection.stream().map(DeferredHolder::get).toArray(Block[]::new)).build(null));
   }
}
