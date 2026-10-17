package thaumcraft.data.condition;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;

public final class ModConditions {
   public static final DeferredRegister<MapCodec<? extends ICondition>> REGISTER = DeferredRegister.create(Keys.CONDITION_CODECS, "thaumcraft");
   public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigBooleanCondition>> CONFIG_BOOLEAN = REGISTER.register(
      "config_flag", () -> ConfigBooleanCondition.CODEC
   );

   private ModConditions() {
   }
}
