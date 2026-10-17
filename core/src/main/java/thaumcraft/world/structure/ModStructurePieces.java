package thaumcraft.world.structure;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import thaumcraft.Thaumcraft;

/** Тип куска кодовой структуры (см. {@link CodeBuiltPiece}). */
public class ModStructurePieces {
   public static final DeferredRegister<StructurePieceType> TYPES =
      DeferredRegister.create(Registries.STRUCTURE_PIECE, Thaumcraft.MODID);

   public static final DeferredHolder<StructurePieceType, StructurePieceType> CODE_BUILT =
      TYPES.register("code_built", () -> (context, tag) -> new CodeBuiltPiece(context, tag));

   public static void init(IEventBus bus) {
      TYPES.register(bus);
   }
}
