package thaumcraft.init.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.api.potions.FluxTaintMobEffect;
import thaumcraft.api.potions.VisExhaustMobEffect;
import thaumcraft.effect.PotionBlurredVision;
import thaumcraft.effect.PotionDeathGaze;
import thaumcraft.effect.PotionInfectiousVisExhaust;
import thaumcraft.effect.PotionSunScorned;
import thaumcraft.effect.PotionThaumarhia;
import thaumcraft.effect.PotionUnnaturalHunger;
import thaumcraft.effect.PotionWarpWard;

public class ModMobEffect {
   private static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, "thaumcraft");
   public static final DeferredHolder<MobEffect, MobEffect> FLUX_TAINT = REGISTER.register("flux_taint", FluxTaintMobEffect::new);
   public static final DeferredHolder<MobEffect, MobEffect> VIS_EXHAUST = REGISTER.register("vis_exhaust", VisExhaustMobEffect::new);
   public static final DeferredHolder<MobEffect, MobEffect> BLURRED = REGISTER.register("blurred", PotionBlurredVision::new);
   public static final DeferredHolder<MobEffect, MobEffect> DEATH_GAZE = REGISTER.register("death_gaze", PotionDeathGaze::new);
   public static final DeferredHolder<MobEffect, MobEffect> INFECTIOUS_VIS_EXHAUST = REGISTER.register(
      "infectious_vis_exhaust", PotionInfectiousVisExhaust::new
   );
   public static final DeferredHolder<MobEffect, MobEffect> SUN_SCORNED = REGISTER.register("sun_scorned", PotionSunScorned::new);
   public static final DeferredHolder<MobEffect, MobEffect> THAUMARHIA = REGISTER.register("thaumarhia", PotionThaumarhia::new);
   public static final DeferredHolder<MobEffect, MobEffect> UNNATURAL_HUNGER = REGISTER.register("unnatural_hunger", PotionUnnaturalHunger::new);
   public static final DeferredHolder<MobEffect, MobEffect> WARP_WARD = REGISTER.register("warp_ward", PotionWarpWard::new);

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
   }
}
