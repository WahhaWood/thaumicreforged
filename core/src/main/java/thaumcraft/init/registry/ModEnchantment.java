package thaumcraft.init.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.enchantment.ThaumcraftEnchantments;

public class ModEnchantment {
   public static final DeferredRegister<Enchantment> REGISTER = DeferredRegister.create(Registries.ENCHANTMENT, "thaumcraft");
   public static final DeferredHolder<Enchantment, Enchantment> REPAIR = REGISTER.register("repair", ThaumcraftEnchantments::repair);
   public static final DeferredHolder<Enchantment, Enchantment> HASTE = REGISTER.register("haste", ThaumcraftEnchantments::haste);
   public static final DeferredHolder<Enchantment, Enchantment> ASCENT_BOOST = REGISTER.register("ascent_boost", ThaumcraftEnchantments::ascentBoost);
   public static final DeferredHolder<Enchantment, Enchantment> SLOW_FALL = REGISTER.register("slow_fall", ThaumcraftEnchantments::slowFall);
   public static final DeferredHolder<Enchantment, Enchantment> AUTO_SMELT = REGISTER.register("auto_smelt", ThaumcraftEnchantments::autoSmelt);
   public static final DeferredHolder<Enchantment, Enchantment> DESINTEGRATE = REGISTER.register("desintegrate", ThaumcraftEnchantments::desintegrate);
   public static final DeferredHolder<Enchantment, Enchantment> QUICK_DRAW = REGISTER.register("quick_draw", ThaumcraftEnchantments::quickDraw);
   public static final DeferredHolder<Enchantment, Enchantment> VAMPIRISM = REGISTER.register("vampirism", ThaumcraftEnchantments::vampirism);
   public static final DeferredHolder<Enchantment, Enchantment> DISPERSED_STRIKES = REGISTER.register(
      "dispersed_strikes", ThaumcraftEnchantments::dispersedStrikes
   );
   public static final DeferredHolder<Enchantment, Enchantment> FINAL_STRIKE = REGISTER.register("final_strike", ThaumcraftEnchantments::finalStrike);
   public static final DeferredHolder<Enchantment, Enchantment> FOCUSED_STRIKES = REGISTER.register("focused_strikes", ThaumcraftEnchantments::focusedStrikes);
   public static final DeferredHolder<Enchantment, Enchantment> POUNCE = REGISTER.register("pounce", ThaumcraftEnchantments::pounce);
   public static final DeferredHolder<Enchantment, Enchantment> SHATTER = REGISTER.register("shatter", ThaumcraftEnchantments::shatter);
   public static final DeferredHolder<Enchantment, Enchantment> SHOCKWAVE = REGISTER.register("shockwave", ThaumcraftEnchantments::shockwave);
   public static final DeferredHolder<Enchantment, Enchantment> TUNNEL = REGISTER.register("tunnel", ThaumcraftEnchantments::tunnel);
   public static final DeferredHolder<Enchantment, Enchantment> VALIANCE = REGISTER.register("valiance", ThaumcraftEnchantments::valiance);

   public static void bootstrap(BootstrapContext<Enchantment> context) {
      ThaumcraftEnchantments.withEnchantmentLookup(context.lookup(Registries.ENCHANTMENT), () -> {
         context.register(REPAIR.getKey(), ThaumcraftEnchantments.repair());
         context.register(HASTE.getKey(), ThaumcraftEnchantments.haste());
         context.register(ASCENT_BOOST.getKey(), ThaumcraftEnchantments.ascentBoost());
         context.register(SLOW_FALL.getKey(), ThaumcraftEnchantments.slowFall());
         context.register(AUTO_SMELT.getKey(), ThaumcraftEnchantments.autoSmelt());
         context.register(DESINTEGRATE.getKey(), ThaumcraftEnchantments.desintegrate());
         context.register(QUICK_DRAW.getKey(), ThaumcraftEnchantments.quickDraw());
         context.register(VAMPIRISM.getKey(), ThaumcraftEnchantments.vampirism());
         context.register(DISPERSED_STRIKES.getKey(), ThaumcraftEnchantments.dispersedStrikes());
         context.register(FINAL_STRIKE.getKey(), ThaumcraftEnchantments.finalStrike());
         context.register(FOCUSED_STRIKES.getKey(), ThaumcraftEnchantments.focusedStrikes());
         context.register(POUNCE.getKey(), ThaumcraftEnchantments.pounce());
         context.register(SHATTER.getKey(), ThaumcraftEnchantments.shatter());
         context.register(SHOCKWAVE.getKey(), ThaumcraftEnchantments.shockwave());
         context.register(TUNNEL.getKey(), ThaumcraftEnchantments.tunnel());
         context.register(VALIANCE.getKey(), ThaumcraftEnchantments.valiance());
      });
   }
}
