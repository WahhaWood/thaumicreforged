package thaumicenergistics.init;

import appeng.api.storage.ITerminalHost;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.MenuLocators;
import thaumicenergistics.menu.ArcaneAssemblerMenu;
import thaumicenergistics.menu.ArcaneCraftingTermMenu;
import thaumicenergistics.menu.CrucibleAssemblerMenu;
import thaumicenergistics.menu.CrucibleInscriberMenu;
import thaumicenergistics.menu.IndustrialAlchemyFurnaceMenu;
import thaumicenergistics.menu.InfusionAssemblerMenu;
import thaumicenergistics.menu.InfusionInscriberMenu;
import thaumicenergistics.menu.KnowledgeInscriberMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
   public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.MENU, "thaumicenergistics");
   public static final DeferredHolder<MenuType<?>, MenuType<IndustrialAlchemyFurnaceMenu>> INDUSTRIAL_ALCHEMY_FURNACE = REGISTER.register(
      "industrial_alchemy_furnace", () -> new MenuType((IContainerFactory<IndustrialAlchemyFurnaceMenu>) (containerId, inv, data) -> new IndustrialAlchemyFurnaceMenu(containerId, inv, data), FeatureFlags.VANILLA_SET)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<InfusionInscriberMenu>> INFUSION_INSCRIBER = REGISTER.register(
      "infusion_inscriber", () -> new MenuType((IContainerFactory<InfusionInscriberMenu>) (containerId, inv, data) -> new InfusionInscriberMenu(containerId, inv, data), FeatureFlags.VANILLA_SET)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<KnowledgeInscriberMenu>> KNOWLEDGE_INSCRIBER = REGISTER.register(
      "knowledge_inscriber", () -> new MenuType((IContainerFactory<KnowledgeInscriberMenu>) (containerId, inv, data) -> new KnowledgeInscriberMenu(containerId, inv, data), FeatureFlags.VANILLA_SET)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<InfusionAssemblerMenu>> INFUSION_ASSEMBLER = REGISTER.register(
      "infusion_assembler", () -> new MenuType((IContainerFactory<InfusionAssemblerMenu>) (containerId, inv, data) -> new InfusionAssemblerMenu(containerId, inv, data), FeatureFlags.VANILLA_SET)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<ArcaneAssemblerMenu>> ARCANE_ASSEMBLER = REGISTER.register(
      "arcane_assembler", () -> new MenuType((IContainerFactory<ArcaneAssemblerMenu>) (containerId, inv, data) -> new ArcaneAssemblerMenu(containerId, inv, data), FeatureFlags.VANILLA_SET)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<CrucibleAssemblerMenu>> CRUCIBLE_ASSEMBLER = REGISTER.register(
      "crucible_assembler", () -> new MenuType((IContainerFactory<CrucibleAssemblerMenu>) (containerId, inv, data) -> new CrucibleAssemblerMenu(containerId, inv, data), FeatureFlags.VANILLA_SET)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<CrucibleInscriberMenu>> CRUCIBLE_INSCRIBER = REGISTER.register(
      "crucible_inscriber", () -> new MenuType((IContainerFactory<CrucibleInscriberMenu>) (containerId, inv, data) -> new CrucibleInscriberMenu(containerId, inv, data), FeatureFlags.VANILLA_SET)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<ArcaneCraftingTermMenu>> ARCANE_CRAFTING_TERM = REGISTER.register("arcane_crafting_term", () -> {
      MenuType<ArcaneCraftingTermMenu> type = new MenuType((IContainerFactory)(containerId, inv, data) -> {
         MenuHostLocator locator = MenuLocators.readFromPacket(data);
         ITerminalHost host = (ITerminalHost)locator.locate(inv.player, ITerminalHost.class);
         if (host == null) {
            throw new IllegalStateException("Could not find arcane terminal host");
         } else {
            ArcaneCraftingTermMenu menu = new ArcaneCraftingTermMenu(containerId, inv, host);
            menu.setReturnedFromSubScreen(data.readBoolean());
            return menu;
         }
      }, FeatureFlags.VANILLA_SET);
      MenuOpener.addOpener(type, (player, locator, fromSubMenu) -> {
         if (player instanceof ServerPlayer serverPlayer) {
            ITerminalHost host = (ITerminalHost)locator.locate(player, ITerminalHost.class);
            if (host == null) {
               return false;
            } else {
               Component title = Component.empty();
               if (host instanceof Nameable nameable && nameable.hasCustomName()) {
                  title = nameable.getCustomName();
               }

               SimpleMenuProvider menu = new SimpleMenuProvider((wnd, p, pl) -> {
                  ArcaneCraftingTermMenu m = new ArcaneCraftingTermMenu(wnd, p, host);
                  m.setLocator(locator);
                  return m;
               }, title);
               serverPlayer.openMenu(menu, buffer -> {
                  MenuLocators.writeToPacket(buffer, locator);
                  buffer.writeBoolean(fromSubMenu);
               });
               return true;
            }
         } else {
            return false;
         }
      });
      return type;
   });

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
   }
}
