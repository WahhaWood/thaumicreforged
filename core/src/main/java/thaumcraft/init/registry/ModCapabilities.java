package thaumcraft.init.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.block.crucible.CrucibleBlockEntity;
import thaumcraft.block.grate.GrateBlockEntity;
import thaumcraft.block.infusion_claw.InfusionClawBlockEntity;
import thaumcraft.block.magic_box.MagicBoxBlockEntity;
import thaumcraft.block.spa.SpaBlockEntity;
import thaumcraft.block.transvector.TransvectorInterfaceBlockEntity;

public final class ModCapabilities {
   private ModCapabilities() {
   }

   public static void register(RegisterCapabilitiesEvent event) {
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.ALCHEMY_FURNACE.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.ARCANE_WORKBENCH.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.ARCANE_PEDESTAL.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.WARP_GATE.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.ADVANCED_NODE_INFUSER.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.FUNNEL.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.ENCHANTER.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.DECONSTRUCTION_TABLE.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.FOCAL_MANIPULATOR.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.WAND_PEDESTAL.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.INFUSION_PILLAR.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.WAND_CHARGER.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.HUNGRY_CHEST.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.REPAIRER.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.THAUMATORIUM.get());
      registerSideInventory(event, (BlockEntityType)ModBlockEntities.ARCANE_SPA.get());
      event.registerBlockEntity(ItemHandler.BLOCK, (BlockEntityType)ModBlockEntities.GRATE.get(), GrateBlockEntity::getItemHandler);
      event.registerBlockEntity(ItemHandler.BLOCK, (BlockEntityType)ModBlockEntities.INFUSION_CLAW.get(), InfusionClawBlockEntity::getItemHandler);
      event.registerBlockEntity(ItemHandler.BLOCK, (BlockEntityType)ModBlockEntities.MAGIC_BOX.get(), MagicBoxBlockEntity::getItemHandler);
      event.registerBlockEntity(
         ItemHandler.BLOCK, (BlockEntityType)ModBlockEntities.TRANSVECTOR_INTERFACE.get(), TransvectorInterfaceBlockEntity::getItemHandler
      );
      event.registerBlockEntity(FluidHandler.BLOCK, (BlockEntityType)ModBlockEntities.CRUCIBLE.get(), CrucibleBlockEntity::getFluidHandler);
      event.registerBlockEntity(FluidHandler.BLOCK, (BlockEntityType)ModBlockEntities.ARCANE_SPA.get(), SpaBlockEntity::getFluidHandler);
      event.registerEntity(ItemHandler.ENTITY, ModEntities.GOLEM.get(), (entity, context) -> entity.getItemHandler((net.minecraft.core.Direction) null));
      event.registerEntity(FluidHandler.ENTITY, ModEntities.GOLEM.get(), (entity, context) -> entity.getFluidHandler((net.minecraft.core.Direction) null));
      event.registerEntity(ItemHandler.ENTITY, ModEntities.TRAVELING_TRUNK.get(), (entity, context) -> entity.getItemHandler((net.minecraft.core.Direction) null));
   }

   private static <BE extends ThaumcraftSideInvBlockEntity> void registerSideInventory(RegisterCapabilitiesEvent event, BlockEntityType<BE> type) {
      event.registerBlockEntity(ItemHandler.BLOCK, type, ThaumcraftSideInvBlockEntity::getItemHandler);
   }
}
