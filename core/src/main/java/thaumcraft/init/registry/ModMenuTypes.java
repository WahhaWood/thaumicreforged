package thaumcraft.init.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.api.container.GhostSlotsMenu;
import thaumcraft.block.advanced_node_infuser.AdvancedNodeInfuserMenu;
import thaumcraft.block.alchemy_furnace.AlchemyFurnaceMenu;
import thaumcraft.block.arcane_workbench.ArcaneWorkbenchMenu;
import thaumcraft.block.bore.BoreMenu;
import thaumcraft.block.deconstruction_table.DeconstructionTableMenu;
import thaumcraft.block.encharter.EnchanterMenu;
import thaumcraft.block.focal_manipulator.FocalManipulatorMenu;
import thaumcraft.block.infusion_claw.InfusionClawMenu;
import thaumcraft.block.magic_box.MagicBoxMenu;
import thaumcraft.block.magnet.MobMagnetMenu;
import thaumcraft.block.spa.SpaMenu;
import thaumcraft.block.table.ResearchTableMenu;
import thaumcraft.block.thaumatorium.ThaumatoriumMenu;
import thaumcraft.block.wand_charger.WandChargerMenu;
import thaumcraft.block.warp_gate.WarpGateMenu;
import thaumcraft.entities.smart.golem.GolemMenu;
import thaumcraft.entities.smart.pech.PechMenu;
import thaumcraft.entities.smart.travelling_trunk.inventory.MenuTravelingTrunk;
import thaumcraft.item.armor.hover_harness.HoverHarnessMenu;
import thaumcraft.item.focus_pouch.FocusPouchMenu;
import thaumcraft.item.hand_mirror.HandMirrorMenu;
import thaumcraft.item.ichor_pouch.IchorPouchMenu;

public class ModMenuTypes {
   private static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.MENU, "thaumcraft");
   public static final DeferredHolder<MenuType<?>, MenuType<HoverHarnessMenu>> HOVER_HARNESS = REGISTER.register(
      "hover_harness", () -> IMenuTypeExtension.create(HoverHarnessMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<HandMirrorMenu>> HAND_MIRROR = REGISTER.register(
      "hand_mirror", () -> IMenuTypeExtension.create(HandMirrorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<ResearchTableMenu>> RESEARCH_TABLE = REGISTER.register(
      "research_table", () -> IMenuTypeExtension.create(ResearchTableMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<FocusPouchMenu>> FOCUS_POUCH = REGISTER.register(
      "focus_pouch", () -> IMenuTypeExtension.create(FocusPouchMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<AlchemyFurnaceMenu>> ALCHEMY_FURNACE = REGISTER.register(
      "alchemy_furnace", () -> IMenuTypeExtension.create(AlchemyFurnaceMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<AdvancedNodeInfuserMenu>> ADVANCED_NODE_INFUSER = REGISTER.register(
      "advanced_node_infuser", () -> IMenuTypeExtension.create(AdvancedNodeInfuserMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<BoreMenu>> ARCANE_BORE = REGISTER.register(
      "arcane_bore", () -> IMenuTypeExtension.create(BoreMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<ArcaneWorkbenchMenu>> ARCANE_WORKBENCH = REGISTER.register(
      "arcane_workbench", () -> IMenuTypeExtension.create(ArcaneWorkbenchMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<DeconstructionTableMenu>> DECONSTRUCTION_TABLE = REGISTER.register(
      "deconstruction_table", () -> IMenuTypeExtension.create(DeconstructionTableMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<FocalManipulatorMenu>> FOCAL_MANIPULATOR = REGISTER.register(
      "focal_manipulator", () -> IMenuTypeExtension.create(FocalManipulatorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<WandChargerMenu>> WAND_CHARGER = REGISTER.register(
      "wand_charger", () -> IMenuTypeExtension.create(WandChargerMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<EnchanterMenu>> ENCHANTER = REGISTER.register(
      "enchanter", () -> IMenuTypeExtension.create(EnchanterMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<GhostSlotsMenu>> GHOST_SLOTS = REGISTER.register(
      "ghost_slots", () -> IMenuTypeExtension.create(GhostSlotsMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<InfusionClawMenu>> INFUSION_CLAW = REGISTER.register(
      "infusion_claw", () -> IMenuTypeExtension.create(InfusionClawMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<MagicBoxMenu>> MAGIC_BOX = REGISTER.register(
      "magic_box", () -> IMenuTypeExtension.create(MagicBoxMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<MobMagnetMenu>> MOB_MAGNET = REGISTER.register(
      "mob_magnet", () -> IMenuTypeExtension.create(MobMagnetMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<SpaMenu>> SPA = REGISTER.register("spa", () -> IMenuTypeExtension.create(SpaMenu::new));
   public static final DeferredHolder<MenuType<?>, MenuType<ThaumatoriumMenu>> THAUMATORIUM = REGISTER.register(
      "thaumatorium", () -> IMenuTypeExtension.create(ThaumatoriumMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<MenuTravelingTrunk>> TRAVELLING_TRUNK = REGISTER.register(
      "travelling_trunk", () -> IMenuTypeExtension.create(MenuTravelingTrunk::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<GolemMenu>> GOLEM = REGISTER.register(
      "golem", () -> IMenuTypeExtension.create(GolemMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<IchorPouchMenu>> ICHOR_POUCH = REGISTER.register(
      "ichor_pouch", () -> IMenuTypeExtension.create(IchorPouchMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<PechMenu>> PECH = REGISTER.register(
      "pech", () -> IMenuTypeExtension.create(PechMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<WarpGateMenu>> WARP_GATE = REGISTER.register(
      "warp_gate", () -> IMenuTypeExtension.create(WarpGateMenu::new)
   );

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
   }
}
