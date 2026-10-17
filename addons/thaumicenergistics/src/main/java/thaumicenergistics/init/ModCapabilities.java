package thaumicenergistics.init;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import java.util.List;
import thaumicenergistics.block.industrial_alchemy_furnace.IndustrialAlchemyFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;

public final class ModCapabilities {
   private ModCapabilities() {
   }

   public static void register(RegisterCapabilitiesEvent event) {
      event.registerBlockEntity(
         ItemHandler.BLOCK, (BlockEntityType)ModBlockEntities.INDUSTRIAL_ALCHEMY_FURNACE.get(), IndustrialAlchemyFurnaceBlockEntity::getItemHandler
      );
      event.registerBlockEntity(
         EnergyStorage.BLOCK, (BlockEntityType)ModBlockEntities.INDUSTRIAL_ALCHEMY_FURNACE.get(), IndustrialAlchemyFurnaceBlockEntity::getEnergyStorage
      );
      registerGridNodeHosts(
         event,
         List.of(
            (BlockEntityType<? extends IInWorldGridNodeHost>)ModBlockEntities.ARCANE_ASSEMBLER.get(),
            (BlockEntityType<? extends IInWorldGridNodeHost>)ModBlockEntities.INFUSION_ASSEMBLER.get(),
            (BlockEntityType<? extends IInWorldGridNodeHost>)ModBlockEntities.CRUCIBLE_ASSEMBLER.get(),
            (BlockEntityType<? extends IInWorldGridNodeHost>)ModBlockEntities.ESSENTIA_PROVIDER.get(),
            (BlockEntityType<? extends IInWorldGridNodeHost>)ModBlockEntities.INFUSION_PROVIDER.get()
         )
      );
   }

   private static void registerGridNodeHosts(RegisterCapabilitiesEvent event, List<BlockEntityType<? extends IInWorldGridNodeHost>> types) {
      for (BlockEntityType<? extends IInWorldGridNodeHost> type : types) {
         event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, type, (be, context) -> (IInWorldGridNodeHost)be);
      }
   }
}
