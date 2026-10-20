package thaumcraft.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

/**
 * Aspects for items of other mods; not part of the original Thaumcraft 4, which only knew tin, silver, lead and copper
 * beyond vanilla. Metals go by common tags, so any mod that tags its ingots, ores, raw materials and dusts gets them.
 * Items whose recipes the aspect generator cannot follow (machine recipes, mana infusion, inscribers) are listed by
 * id and registered only when their mod is loaded.
 */
public final class ModItemAspects {
   private ModItemAspects() {
   }

   public static void register() {
      registerMetals();
      if (ModList.get().isLoaded("industrialreforged")) {
         registerIndustrialReforged();
      }

      if (ModList.get().isLoaded("botania")) {
         registerBotania();
      }

      if (ModList.get().isLoaded("ae2")) {
         registerAppliedEnergistics();
      }
   }

   /** The pattern of the original's tin, silver and lead: ingot = metal + trait, ore and raw add entropy, dust has less metal. */
   private static void registerMetals() {
      metal("nickel", 3, Aspect.EXCHANGE);
      metal("uranium", 3, Aspect.ENERGY, Aspect.POISON);
      metal("tungsten", 3, Aspect.EARTH);
      metal("titanium", 3, Aspect.ARMOR);
      metal("osmium", 3, Aspect.ORDER);
      metal("aluminum", 2, Aspect.AIR);
      metal("platinum", 3, Aspect.GREED);
      metal("zinc", 2, Aspect.EXCHANGE);
      metal("iridium", 4, Aspect.ORDER, Aspect.CRYSTAL);
      metal("bronze", 3, Aspect.CRAFT);
      metal("steel", 3, Aspect.ORDER);
   }

   private static void metal(String name, int metal, Aspect... traits) {
      ThaumcraftApi.registerObjectTag(tag("ingots/" + name), withTraits(new AspectList().add(Aspect.METAL, metal), traits));
      ThaumcraftApi.registerObjectTag(tag("nuggets/" + name), new AspectList().add(Aspect.METAL, 1));
      ThaumcraftApi.registerObjectTag(tag("ores/" + name), withTraits(new AspectList().add(Aspect.METAL, metal).add(Aspect.ENTROPY, 1), traits));
      ThaumcraftApi.registerObjectTag(tag("raw_materials/" + name), withTraits(new AspectList().add(Aspect.METAL, metal).add(Aspect.ENTROPY, 1), traits));
      ThaumcraftApi.registerObjectTag(tag("dusts/" + name), withTraits(new AspectList().add(Aspect.METAL, metal - 1).add(Aspect.ENTROPY, 1), traits));
   }

   private static AspectList withTraits(AspectList aspects, Aspect... traits) {
      for (Aspect trait : traits) {
         aspects.add(trait, 1);
      }

      return aspects;
   }

   private static void registerIndustrialReforged() {
      item("industrialreforged", "resin", new AspectList().add(Aspect.TREE, 1).add(Aspect.SLIME, 2));
      item("industrialreforged", "rubber", new AspectList().add(Aspect.SLIME, 2).add(Aspect.MOTION, 1));
      item("industrialreforged", "scrap", new AspectList().add(Aspect.ENTROPY, 2).add(Aspect.VOID, 1));
      item("industrialreforged", "scrap_box", new AspectList().add(Aspect.ENTROPY, 4).add(Aspect.GREED, 2));
      item("industrialreforged", "industrial_diamond", new AspectList().add(Aspect.CRYSTAL, 4).add(Aspect.GREED, 1));
      item("industrialreforged", "energium_dust", new AspectList().add(Aspect.ENERGY, 3).add(Aspect.CRYSTAL, 1));
      item("industrialreforged", "iridium_shard", new AspectList().add(Aspect.METAL, 1).add(Aspect.ORDER, 1));
      item("industrialreforged", "iridium_ore", new AspectList().add(Aspect.METAL, 4).add(Aspect.ORDER, 4).add(Aspect.ENTROPY, 1));
   }

   private static void registerBotania() {
      item("botania", "manasteel_ingot", new AspectList().add(Aspect.METAL, 3).add(Aspect.MAGIC, 2));
      item("botania", "terrasteel_ingot", new AspectList().add(Aspect.METAL, 4).add(Aspect.MAGIC, 4).add(Aspect.EARTH, 2));
      item("botania", "elementium_ingot", new AspectList().add(Aspect.METAL, 3).add(Aspect.MAGIC, 2).add(Aspect.AURA, 2));
      item("botania", "gaia_ingot", new AspectList().add(Aspect.METAL, 4).add(Aspect.MAGIC, 6).add(Aspect.SOUL, 4));
      item("botania", "mana_pearl", new AspectList().add(Aspect.MAGIC, 3).add(Aspect.TRAVEL, 3));
      item("botania", "mana_diamond", new AspectList().add(Aspect.CRYSTAL, 4).add(Aspect.MAGIC, 4).add(Aspect.GREED, 2));
      item("botania", "dragonstone", new AspectList().add(Aspect.CRYSTAL, 4).add(Aspect.MAGIC, 3).add(Aspect.AURA, 3));
      item("botania", "pixie_dust", new AspectList().add(Aspect.MAGIC, 2).add(Aspect.AURA, 2).add(Aspect.FLIGHT, 1));
      item("botania", "mana_powder", new AspectList().add(Aspect.MAGIC, 2).add(Aspect.ENTROPY, 1));
      item("botania", "mana_string", new AspectList().add(Aspect.CLOTH, 1).add(Aspect.MAGIC, 1));
      item("botania", "gaia_spirit", new AspectList().add(Aspect.SOUL, 4).add(Aspect.LIFE, 4).add(Aspect.MAGIC, 4));
      item("botania", "livingrock", new AspectList().add(Aspect.EARTH, 2).add(Aspect.LIFE, 1));
      // by id: the logs are in minecraft:logs too, and that tag would win
      item("botania", "livingwood_log", new AspectList().add(Aspect.TREE, 4).add(Aspect.LIFE, 1));
      item("botania", "dreamwood_log", new AspectList().add(Aspect.TREE, 4).add(Aspect.AURA, 1));
      ThaumcraftApi.registerObjectTag(botaniaTag("petals"), new AspectList().add(Aspect.PLANT, 1).add(Aspect.SENSES, 1));
      ThaumcraftApi.registerObjectTag(
         botaniaTag("small_mystical_flowers"), new AspectList().add(Aspect.PLANT, 1).add(Aspect.LIFE, 1).add(Aspect.SENSES, 1).add(Aspect.MAGIC, 1)
      );
      ThaumcraftApi.registerObjectTag(botaniaTag("runes"), new AspectList().add(Aspect.MAGIC, 3).add(Aspect.CRAFT, 1));
   }

   private static void registerAppliedEnergistics() {
      item("ae2", "certus_quartz_crystal", new AspectList().add(Aspect.CRYSTAL, 2).add(Aspect.MIND, 1));
      item("ae2", "charged_certus_quartz_crystal", new AspectList().add(Aspect.CRYSTAL, 2).add(Aspect.MIND, 1).add(Aspect.ENERGY, 2));
      item("ae2", "certus_quartz_dust", new AspectList().add(Aspect.CRYSTAL, 1).add(Aspect.ENTROPY, 1));
      item("ae2", "fluix_crystal", new AspectList().add(Aspect.CRYSTAL, 2).add(Aspect.ENERGY, 2).add(Aspect.EXCHANGE, 1));
      item("ae2", "fluix_dust", new AspectList().add(Aspect.CRYSTAL, 1).add(Aspect.ENERGY, 1).add(Aspect.ENTROPY, 1));
      item("ae2", "silicon", new AspectList().add(Aspect.CRYSTAL, 1).add(Aspect.EARTH, 1));
      item("ae2", "sky_stone_block", new AspectList().add(Aspect.EARTH, 2).add(Aspect.VOID, 1));
      item("ae2", "ender_dust", new AspectList().add(Aspect.ELDRITCH, 2).add(Aspect.TRAVEL, 2));
      item("ae2", "matter_ball", new AspectList().add(Aspect.VOID, 2).add(Aspect.EXCHANGE, 1));
      item("ae2", "singularity", new AspectList().add(Aspect.VOID, 6).add(Aspect.ELDRITCH, 2));
      item("ae2", "quantum_entangled_singularity", new AspectList().add(Aspect.VOID, 6).add(Aspect.ELDRITCH, 4).add(Aspect.TRAVEL, 4));
      item("ae2", "logic_processor", new AspectList().add(Aspect.MIND, 2).add(Aspect.METAL, 1));
      item("ae2", "calculation_processor", new AspectList().add(Aspect.MIND, 2).add(Aspect.CRYSTAL, 1));
      item("ae2", "engineering_processor", new AspectList().add(Aspect.MIND, 2).add(Aspect.CRYSTAL, 2));
   }

   private static void item(String namespace, String path, AspectList aspects) {
      BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(namespace, path))
         .ifPresent(item -> ThaumcraftApi.registerObjectTag(item, aspects));
   }

   private static TagKey<Item> tag(String path) {
      return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
   }

   private static TagKey<Item> botaniaTag(String path) {
      return ItemTags.create(ResourceLocation.fromNamespaceAndPath("botania", path));
   }
}
