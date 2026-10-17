package thaumcraft.init.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.loot.function.SetWispEssenceAspectsFunction;

public final class ModLootFunctions {
   private static final DeferredRegister<LootItemFunctionType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.LOOT_FUNCTION_TYPE, "thaumcraft");
   public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetWispEssenceAspectsFunction>> SET_WISP_ESSENCE_ASPECTS = REGISTER.register(
      "set_wisp_essence_aspects", () -> new LootItemFunctionType(SetWispEssenceAspectsFunction.CODEC)
   );

   private ModLootFunctions() {
   }

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
   }
}
