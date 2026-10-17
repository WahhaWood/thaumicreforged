package thaumcraft.world.structure;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.level.levelgen.structure.StructureType;
import thaumcraft.Thaumcraft;

/** Тип структур, построенных кодовой фичей (см. {@link CodeBuiltStructure}). */
public class ModStructures {
   public static final DeferredRegister<StructureType<?>> TYPES =
      DeferredRegister.create(Registries.STRUCTURE_TYPE, Thaumcraft.MODID);

   public static final DeferredHolder<StructureType<?>, StructureType<CodeBuiltStructure>> CODE_BUILT =
      TYPES.register("code_built", () -> () -> CodeBuiltStructure.CODEC);

   public static void init(IEventBus bus) {
      TYPES.register(bus);
   }
}
