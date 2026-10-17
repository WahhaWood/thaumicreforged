package thaumcraft.enchantment;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import thaumcraft.api.IEnchantmentRule;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.init.registry.ModEnchantment;
import thaumcraft.research.ResearchManager;

public final class EnchantmentManager {
   public static final Map<ResourceLocation, Map<Integer, EnchantmentData>> enchantmentData = new LinkedHashMap<>();
   public static final Multimap<ResourceLocation, IEnchantmentRule> rules = ArrayListMultimap.create();
   private static List<ResourceLocation> sortedEnchantmentIds = List.of();

   private EnchantmentManager() {
   }

   public static void initEnchantmentData() {
      if (enchantmentData.isEmpty()) {
         registerExponentialCostData(
            Enchantments.PROTECTION, 1, 4, texture("protection"), true, new AspectList().add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 7), "infusion_enchantment"
         );
         registerExponentialCostData(
            Enchantments.FIRE_PROTECTION,
            1,
            4,
            texture("fire_protection"),
            true,
            new AspectList().add(Aspect.FIRE, 10).add(Aspect.ENTROPY, 3).add(Aspect.WATER, 4),
            "infusion_enchantment"
         );
         registerExponentialCostData(
            Enchantments.FEATHER_FALLING,
            1,
            4,
            texture("feather_falling"),
            true,
            new AspectList().add(Aspect.AIR, 16).add(Aspect.ORDER, 5),
            "infusion_enchantment"
         );
         registerExponentialCostData(
            Enchantments.BLAST_PROTECTION,
            1,
            4,
            texture("blast_protection"),
            true,
            new AspectList().add(Aspect.EARTH, 5).add(Aspect.FIRE, 5).add(Aspect.ENTROPY, 8),
            "infusion_enchantment"
         );
         registerExponentialCostData(
            ModEnchantment.HASTE.getKey(),
            1,
            3,
            texture("haste"),
            true,
            new AspectList().add(Aspect.AIR, 10).add(Aspect.ENTROPY, 5).add(Aspect.EARTH, 5),
            "infusion_enchantment"
         );
         registerExponentialCostData(
            ModEnchantment.REPAIR.getKey(),
            1,
            2,
            texture("repair"),
            true,
            new AspectList().add(Aspect.WATER, 20).add(Aspect.FIRE, 20).add(Aspect.EARTH, 20).add(Aspect.AIR, 20).add(Aspect.ORDER, 20).add(Aspect.ENTROPY, 5),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.PROJECTILE_PROTECTION,
            1,
            4,
            texture("projectile_protection"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.ARMOR, 4).add(Aspect.FLIGHT, 4),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.RESPIRATION,
            1,
            3,
            texture("respiration"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.AIR, 8).add(Aspect.WATER, 8),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.AQUA_AFFINITY,
            1,
            1,
            texture("aqua_affinity"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.MOTION, 8).add(Aspect.WATER, 8),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.THORNS,
            1,
            3,
            texture("thorns"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 8).add(Aspect.PLANT, 8),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.SHARPNESS, 1, 5, texture("sharpness"), true, new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 8), "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.SMITE,
            1,
            5,
            texture("smite"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 4).add(Aspect.UNDEAD, 4),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.BANE_OF_ARTHROPODS,
            1,
            5,
            texture("bane_of_awful_turds"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 4).add(Aspect.BEAST, 4),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.KNOCKBACK,
            1,
            2,
            texture("knockback"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 3).add(Aspect.MOTION, 3),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.FIRE_ASPECT,
            1,
            2,
            texture("fire_aspect"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 4).add(Aspect.FIRE, 8),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.LOOTING,
            1,
            3,
            texture("looting"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 4).add(Aspect.GREED, 8),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.EFFICIENCY,
            1,
            5,
            texture("efficiency"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.TOOL, 4).add(Aspect.ORDER, 4),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.SILK_TOUCH,
            1,
            1,
            texture("silk_touch"),
            true,
            new AspectList().add(Aspect.MAGIC, 16).add(Aspect.TOOL, 16).add(Aspect.ORDER, 16).add(Aspect.HARVEST, 16).add(Aspect.MINE, 16),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.UNBREAKING,
            1,
            3,
            texture("unbreaking"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.TOOL, 4).add(Aspect.ORDER, 8),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.FORTUNE,
            1,
            3,
            texture("fortune"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.TOOL, 4).add(Aspect.GREED, 8),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.POWER, 1, 5, texture("power"), true, new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 8), "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.PUNCH,
            1,
            2,
            texture("punch"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 3).add(Aspect.MOTION, 3),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.FLAME,
            1,
            1,
            texture("flame"),
            true,
            new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 4).add(Aspect.FIRE, 8),
            "infusion_enchantment"
         );
         registerRecipeDerivedCostData(
            Enchantments.INFINITY,
            1,
            1,
            texture("infinity"),
            true,
            new AspectList().add(Aspect.MAGIC, 8).add(Aspect.WEAPON, 16).add(Aspect.VOID, 16).add(Aspect.EXCHANGE, 16),
            "infusion_enchantment"
         );
         registerExponentialCostData(
            ModEnchantment.ASCENT_BOOST.getKey(),
            1,
            4,
            texture("ascent_boost"),
            false,
            new AspectList().add(Aspect.ENTROPY, 8).add(Aspect.AIR, 10),
            "ttench_ascent_boost"
         );
         registerExponentialCostData(
            ModEnchantment.SLOW_FALL.getKey(), 1, 3, texture("slow_fall"), false, new AspectList().add(Aspect.ORDER, 8).add(Aspect.AIR, 10), "ttench_slow_fall"
         );
         registerExponentialCostData(
            ModEnchantment.AUTO_SMELT.getKey(),
            1,
            1,
            texture("auto_smelt"),
            false,
            new AspectList().add(Aspect.ENTROPY, 20).add(Aspect.FIRE, 30),
            "ttench_auto_smelt"
         );
         registerExponentialCostData(
            ModEnchantment.DESINTEGRATE.getKey(),
            1,
            1,
            texture("desintegrate"),
            false,
            new AspectList().add(Aspect.ENTROPY, 25).add(Aspect.AIR, 10).add(Aspect.EARTH, 10),
            "ttench_desintegrate"
         );
         registerExponentialCostData(
            ModEnchantment.QUICK_DRAW.getKey(),
            1,
            2,
            texture("quick_draw"),
            false,
            new AspectList().add(Aspect.ORDER, 10).add(Aspect.AIR, 10).add(Aspect.WATER, 5),
            "ttench_quick_draw"
         );
         registerExponentialCostData(
            ModEnchantment.VAMPIRISM.getKey(),
            1,
            2,
            texture("vamprisim"),
            false,
            new AspectList().add(Aspect.ENTROPY, 8).add(Aspect.FIRE, 10).add(Aspect.WATER, 10),
            "ttench_vampirism"
         );
         registerExponentialCostData(
            ModEnchantment.FOCUSED_STRIKES.getKey(),
            1,
            5,
            texture("focused_strikes"),
            false,
            new AspectList().add(Aspect.ORDER, 12).add(Aspect.AIR, 10).add(Aspect.WATER, 10),
            "ttench_focused"
         );
         registerExponentialCostData(
            ModEnchantment.DISPERSED_STRIKES.getKey(),
            1,
            5,
            texture("dispersed_strikes"),
            false,
            new AspectList().add(Aspect.ENTROPY, 12).add(Aspect.FIRE, 10).add(Aspect.EARTH, 10),
            "ttench_dispersed"
         );
         registerExponentialCostData(
            ModEnchantment.VALIANCE.getKey(),
            1,
            5,
            texture("valiance"),
            false,
            new AspectList().add(Aspect.ORDER, 12).add(Aspect.FIRE, 10).add(Aspect.EARTH, 10),
            "ttench_valiance"
         );
         registerExponentialCostData(
            ModEnchantment.FINAL_STRIKE.getKey(),
            1,
            5,
            texture("final_strike"),
            false,
            new AspectList().add(Aspect.ENTROPY, 16).add(Aspect.FIRE, 16),
            "ttench_final"
         );
         registerExponentialCostData(
            ModEnchantment.TUNNEL.getKey(), 1, 5, texture("tunnel"), false, new AspectList().add(Aspect.EARTH, 16).add(Aspect.ORDER, 16), "ttench_tunnel"
         );
         registerExponentialCostData(
            ModEnchantment.SHATTER.getKey(), 1, 5, texture("shatter"), false, new AspectList().add(Aspect.EARTH, 16).add(Aspect.ENTROPY, 16), "ttench_shatter"
         );
         registerExponentialCostData(
            ModEnchantment.SHOCKWAVE.getKey(),
            1,
            5,
            texture("shockwave"),
            false,
            new AspectList().add(Aspect.EARTH, 16).add(Aspect.AIR, 16),
            "ttench_shockwave"
         );
         registerExponentialCostData(
            ModEnchantment.POUNCE.getKey(), 1, 5, texture("pounce"), false, new AspectList().add(Aspect.EARTH, 16).add(Aspect.AIR, 16), "ttench_pounce"
         );
         rebuildSortedEnchantmentIds();
      }
   }

   public static boolean canApply(ItemStack stack, Holder<Enchantment> enchant, List<ResourceLocation> currentEnchants, Provider registries) {
      if (enchant != null && stack != null && !stack.isEmpty()) {
         ResourceLocation enchantId = enchant.unwrapKey().<ResourceLocation>map(ResourceKey::location).orElse(null);
         if (enchantId != null && stack.supportsEnchantment(enchant) && !currentEnchants.contains(enchantId)) {
            RegistryLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);

            for (ResourceLocation currentId : currentEnchants) {
               ResourceKey<Enchantment> currentKey = ResourceKey.create(Registries.ENCHANTMENT, currentId);
               Optional<Reference<Enchantment>> current = lookup.get(currentKey);
               if (current.isPresent() && !Enchantment.areCompatible(enchant, (Holder)current.get())) {
                  return false;
               }
            }

            for (IEnchantmentRule rule : rules.get(enchantId)) {
               if (rule != null && rule.cantApplyAlongside(currentEnchants)) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean canEnchantmentBeUsed(Player player, Holder<Enchantment> enchant) {
      ResourceLocation id = enchant.unwrapKey().<ResourceLocation>map(ResourceKey::location).orElse(null);
      if (id == null) {
         return false;
      } else if (!enchantmentData.containsKey(id)) {
         return false;
      } else {
         EnchantmentData data = enchantmentData.get(id).get(1);
         return data != null && (data.research.isEmpty() || ResearchManager.isResearchComplete(player, data.research));
      }
   }

   public static void registerExponentialCostData(
      ResourceKey<Enchantment> enchantment, int minLevel, int maxLevel, ResourceLocation texture, boolean vanilla, AspectList level1Aspects
   ) {
      registerExponentialCostData(enchantment, minLevel, maxLevel, texture, vanilla, level1Aspects, "");
   }

   public static void registerExponentialCostData(
      ResourceKey<Enchantment> enchantment, int minLevel, int maxLevel, ResourceLocation texture, boolean vanilla, AspectList level1Aspects, String research
   ) {
      registerExponentialCostData(enchantment.location(), minLevel, maxLevel, texture, vanilla, level1Aspects, research);
   }

   public static void registerExponentialCostData(
      ResourceLocation enchantment, Enchantment value, ResourceLocation texture, boolean vanilla, AspectList level1Aspects, String research
   ) {
      registerExponentialCostData(enchantment, value.getMinLevel(), value.getMaxLevel(), texture, vanilla, level1Aspects, research);
   }

   public static void registerExponentialCostData(
      ResourceLocation enchantment, int minLevel, int maxLevel, ResourceLocation texture, boolean vanilla, AspectList level1Aspects, String research
   ) {
      for (int level = minLevel; level <= maxLevel; level++) {
         double multiplier = level * (1.0 + level * 0.2);
         EnchantmentData data = new EnchantmentData(texture, vanilla, multiplyAspectList(level1Aspects, multiplier), research);
         registerData(enchantment, level, data);
      }
   }

   private static void registerRecipeDerivedCostData(
      ResourceKey<Enchantment> enchantment, int minLevel, int maxLevel, ResourceLocation texture, boolean vanilla, AspectList recipeAspects, String research
   ) {
      registerExponentialCostData(enchantment, minLevel, maxLevel, texture, vanilla, ResearchManager.reduceToPrimals(recipeAspects, true), research);
   }

   private static void registerData(ResourceLocation enchantment, int level, EnchantmentData data) {
      if (enchantment != null) {
         enchantmentData.computeIfAbsent(enchantment, key -> new HashMap<>()).put(level, data);
      }
   }

   private static void rebuildSortedEnchantmentIds() {
      sortedEnchantmentIds = List.copyOf(new ArrayList<>(enchantmentData.keySet()));
   }

   public static List<ResourceLocation> getSortedEnchantmentIds() {
      if (sortedEnchantmentIds.isEmpty() && !enchantmentData.isEmpty()) {
         rebuildSortedEnchantmentIds();
      }

      return sortedEnchantmentIds;
   }

   private static AspectList multiplyAspectList(AspectList source, double multiplier) {
      AspectList result = new AspectList();

      for (Aspect aspect : source.getAspects()) {
         int amount = Math.max(1, (int)Math.round(source.getAmount(aspect) * multiplier));
         result.add(aspect, amount);
      }

      return result;
   }

   private static ResourceLocation texture(String name) {
      return ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/enchant/" + name + ".png");
   }
}
