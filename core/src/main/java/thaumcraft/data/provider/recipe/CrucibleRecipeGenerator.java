package thaumcraft.data.provider.recipe;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags.Items;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.OrCondition;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.data.builder.CrucibleRecipeBuilder;
import thaumcraft.init.registry.ModItems;

public class CrucibleRecipeGenerator extends RecipeProvider {
   public CrucibleRecipeGenerator(PackOutput output, CompletableFuture<Provider> registries) {
      super(output, registries);
   }

   public void generate(@NotNull RecipeOutput consumer) {
      this.buildRecipes(consumer);
   }

   protected void buildRecipes(@NotNull RecipeOutput consumer) {
      this.balancedShard((ItemLike)ModItems.AIR_SHARD.get(), Aspect.AIR, "air", consumer);
      this.balancedShard((ItemLike)ModItems.FIRE_SHARD.get(), Aspect.FIRE, "fire", consumer);
      this.balancedShard((ItemLike)ModItems.WATER_SHARD.get(), Aspect.WATER, "water", consumer);
      this.balancedShard((ItemLike)ModItems.EARTH_SHARD.get(), Aspect.EARTH, "earth", consumer);
      this.balancedShard((ItemLike)ModItems.ORDER_SHARD.get(), Aspect.ORDER, "order", consumer);
      this.balancedShard((ItemLike)ModItems.ENTROPY_SHARD.get(), Aspect.ENTROPY, "entropy", consumer);
      // Шарды измерений плавятся без модов техники: катализаторы ванильные (иначе рецепты
      // не генерируются, а исследования ссылаются в пустоту). Источник через
      // дроп мобов (DimensionalShardDropHandler) остаётся как альтернатива.
      CrucibleRecipeBuilder.crucible("ender_shard", (ItemLike)ModItems.ENDER_SHARD.get())
         .aspect(Aspect.ELDRITCH, 32)
         .aspect(Aspect.MAGIC, 16)
         .aspect(Aspect.CRYSTAL, 8)
         .catalyst(net.minecraft.world.item.Items.ENDER_PEARL)
         .save(consumer, Thaumcraft.location("crucible/ender_shard"));
      CrucibleRecipeBuilder.crucible("nether_shard", (ItemLike)ModItems.NETHER_SHARD.get())
         .aspect(Aspect.FIRE, 32)
         .aspect(Aspect.MAGIC, 16)
         .aspect(Aspect.CRYSTAL, 8)
         .catalyst(net.minecraft.world.item.Items.NETHER_WART)
         .save(consumer, Thaumcraft.location("crucible/nether_shard"));
      CrucibleRecipeBuilder.crucible("alumentum", (ItemLike)ModItems.ALUMENTUM.get())
         .aspect(Aspect.ENERGY, 3)
         .aspect(Aspect.FIRE, 3)
         .aspect(Aspect.ENTROPY, 3)
         .catalyst(ItemTags.COALS)
         .save(consumer, Thaumcraft.location("crucible/alumentum"));
      CrucibleRecipeBuilder.crucible("nitor", (ItemLike)ModItems.NITOR.get())
         .aspect(Aspect.ENERGY, 3)
         .aspect(Aspect.FIRE, 3)
         .aspect(Aspect.LIGHT, 3)
         .catalyst(Items.DUSTS_GLOWSTONE)
         .save(consumer, Thaumcraft.location("crucible/nitor"));
      CrucibleRecipeBuilder.crucible("thaumium", (ItemLike)ModItems.THAUMIUM_INGOT.get())
         .aspect(Aspect.MAGIC, 4)
         .catalyst(net.minecraft.world.item.Items.IRON_INGOT)
         .save(consumer, Thaumcraft.location("crucible/thaumium_ingot"));
      CrucibleRecipeBuilder.crucible("void_metal", (ItemLike)ModItems.VOID_METAL_INGOT.get())
         .aspect(Aspect.METAL, 8)
         .catalyst((ItemLike)ModItems.VOID_SEED.get())
         .save(consumer, Thaumcraft.location("crucible/void_metal_ingot"));
      CrucibleRecipeBuilder.crucible("void_metal", (ItemLike)ModItems.VOID_SEED.get())
         .aspect(Aspect.DARKNESS, 8)
         .aspect(Aspect.VOID, 8)
         .aspect(Aspect.ELDRITCH, 2)
         .catalyst(net.minecraft.world.item.Items.WHEAT_SEEDS)
         .save(consumer, Thaumcraft.location("crucible/void_seed"));
      CrucibleRecipeBuilder.crucible("tallow", (ItemLike)ModItems.MAGIC_TALLOW.get())
         .aspect(Aspect.MAGIC, 2)
         .catalyst(net.minecraft.world.item.Items.ROTTEN_FLESH)
         .save(consumer, Thaumcraft.location("crucible/magic_tallow"));
      CrucibleRecipeBuilder.crucible("alchemical_duplication", net.minecraft.world.item.Items.GUNPOWDER, 2)
         .aspect(Aspect.FIRE, 4)
         .aspect(Aspect.ENTROPY, 4)
         .catalyst(net.minecraft.world.item.Items.GUNPOWDER)
         .save(consumer, Thaumcraft.location("crucible/gunpowder_dupe"));
      CrucibleRecipeBuilder.crucible("alchemical_duplication", net.minecraft.world.item.Items.SLIME_BALL, 2)
         .aspect(Aspect.WATER, 2)
         .aspect(Aspect.LIFE, 2)
         .catalyst(net.minecraft.world.item.Items.SLIME_BALL)
         .save(consumer, Thaumcraft.location("crucible/slime_ball_dupe"));
      CrucibleRecipeBuilder.crucible("alchemical_duplication", net.minecraft.world.item.Items.CLAY_BALL, 2)
         .aspect(Aspect.WATER, 1)
         .aspect(Aspect.EARTH, 2)
         .catalyst(net.minecraft.world.item.Items.CLAY_BALL)
         .save(consumer, Thaumcraft.location("crucible/clay_ball_dupe"));
      CrucibleRecipeBuilder.crucible("alchemical_duplication", net.minecraft.world.item.Items.GLOWSTONE_DUST, 2)
         .aspect(Aspect.LIGHT, 3)
         .aspect(Aspect.SENSES, 1)
         .catalyst(Items.DUSTS_GLOWSTONE)
         .save(consumer, Thaumcraft.location("crucible/glowstone_dust_dupe"));
      CrucibleRecipeBuilder.crucible("alchemical_duplication", net.minecraft.world.item.Items.INK_SAC, 2)
         .aspect(Aspect.WATER, 2)
         .aspect(Aspect.SENSES, 2)
         .catalyst(net.minecraft.world.item.Items.INK_SAC)
         .save(consumer, Thaumcraft.location("crucible/ink_sac_dupe"));
      CrucibleRecipeBuilder.crucible("alchemical_manufacture", Blocks.COBWEB)
         .aspect(Aspect.TRAP, 2)
         .aspect(Aspect.CLOTH, 2)
         .catalyst(Items.STRINGS)
         .save(consumer, Thaumcraft.location("crucible/cobweb"));
      CrucibleRecipeBuilder.crucible("alchemical_manufacture", Blocks.MOSSY_COBBLESTONE)
         .aspect(Aspect.PLANT, 2)
         .aspect(Aspect.MAGIC, 1)
         .catalyst(Items.COBBLESTONES)
         .save(consumer, Thaumcraft.location("crucible/mossy_cobblestone"));
      CrucibleRecipeBuilder.crucible("alchemical_manufacture", Blocks.ICE)
         .aspect(Aspect.ORDER, 1)
         .aspect(Aspect.COLD, 1)
         .catalyst(Blocks.SNOW_BLOCK)
         .save(consumer, Thaumcraft.location("crucible/ice"));
      CrucibleRecipeBuilder.crucible("entropic_processing", Blocks.CRACKED_STONE_BRICKS)
         .aspect(Aspect.ENTROPY, 2)
         .catalyst(Blocks.STONE_BRICKS)
         .save(consumer, Thaumcraft.location("crucible/cracked_stone_bricks"));
      CrucibleRecipeBuilder.crucible("entropic_processing", net.minecraft.world.item.Items.BONE_MEAL, 4)
         .aspect(Aspect.ENTROPY, 1)
         .catalyst(net.minecraft.world.item.Items.BONE)
         .save(consumer, Thaumcraft.location("crucible/bone_meal"));
      CrucibleRecipeBuilder.crucible("pure_iron", (ItemLike)ModItems.IRON_CLUSTER.get())
         .aspect(Aspect.METAL, 1)
         .aspect(Aspect.ORDER, 1)
         .catalyst(Items.ORES_IRON)
         .save(consumer, Thaumcraft.location("crucible/iron_cluster"));
      CrucibleRecipeBuilder.crucible("pure_gold", (ItemLike)ModItems.GOLD_CLUSTER.get())
         .aspect(Aspect.METAL, 1)
         .aspect(Aspect.ORDER, 1)
         .catalyst(Items.ORES_GOLD)
         .save(consumer, Thaumcraft.location("crucible/gold_cluster"));
      CrucibleRecipeBuilder.crucible("pure_copper", (ItemLike)ModItems.COPPER_CLUSTER.get())
         .aspect(Aspect.METAL, 1)
         .aspect(Aspect.ORDER, 1)
         .catalyst(Items.ORES_COPPER)
         .save(consumer, Thaumcraft.location("crucible/copper_cluster"));
      RecipeOutput tinLeadConsumer = consumer.withConditions(
         new ICondition[]{new OrCondition(List.of(new ModLoadedCondition("mekanism"), new ModLoadedCondition("industrialreforged")))}
      );
      CrucibleRecipeBuilder.crucible("pure_tin", (ItemLike)ModItems.TIN_CLUSTER.get())
         .aspect(Aspect.METAL, 1)
         .aspect(Aspect.ORDER, 1)
         .catalyst(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/tin")))
         .save(tinLeadConsumer, Thaumcraft.location("crucible/tin_cluster"));
      CrucibleRecipeBuilder.crucible("pure_lead", (ItemLike)ModItems.LEAD_CLUSTER.get())
         .aspect(Aspect.METAL, 1)
         .aspect(Aspect.ORDER, 1)
         .catalyst(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/lead")))
         .save(tinLeadConsumer, Thaumcraft.location("crucible/lead_cluster"));
      CrucibleRecipeBuilder.crucible("pure_silver", (ItemLike)ModItems.SILVER_CLUSTER.get())
         .aspect(Aspect.METAL, 1)
         .aspect(Aspect.ORDER, 1)
         .catalyst(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/silver")))
         .save(consumer.withConditions(new ICondition[]{new ModLoadedCondition("industrialreforged")}), Thaumcraft.location("crucible/silver_cluster"));
      CrucibleRecipeBuilder.crucible("ethereal_bloom", (ItemLike)ModItems.ETHEREAL_BLOOM.get())
         .aspect(Aspect.MAGIC, 16)
         .aspect(Aspect.PLANT, 16)
         .aspect(Aspect.HEAL, 16)
         .aspect(Aspect.TAINT, 8)
         .catalyst((ItemLike)ModItems.SHIMMER_LEAF.get())
         .save(consumer, Thaumcraft.location("crucible/ethereal_bloom"));
      CrucibleRecipeBuilder.crucible("liquid_death", (ItemLike)ModItems.FLUID_DEATH_BUCKET.get())
         .aspect(Aspect.DEATH, 32)
         .aspect(Aspect.POISON, 32)
         .aspect(Aspect.ENTROPY, 32)
         .catalyst(net.minecraft.world.item.Items.BUCKET)
         .save(consumer, Thaumcraft.location("crucible/bucket_death"));
      ItemStack itemStack = new ItemStack((ItemLike)ModItems.PHIAL_ESSENCE.get());
      ((IEssenceContainerItem)itemStack.getItem()).setAspects(itemStack, new AspectList().add(Aspect.TAINT, 8));
      CrucibleRecipeBuilder.crucible("bottle_taint", (ItemLike)ModItems.BOTTLE_TAINT.get())
         .aspect(Aspect.TAINT, 8)
         .aspect(Aspect.MAGIC, 8)
         .catalyst(DataComponentIngredient.of(true, itemStack))
         .save(consumer, Thaumcraft.location("crucible/bottle_taint"));
      CrucibleRecipeBuilder.crucible("golem_straw", (ItemLike)ModItems.GOLEM_STRAW.get())
         .aspect(Aspect.MAN, 4)
         .aspect(Aspect.MOTION, 4)
         .aspect(Aspect.SOUL, 4)
         .catalyst(Blocks.HAY_BLOCK)
         .save(consumer, Thaumcraft.location("crucible/golem_straw"));
      CrucibleRecipeBuilder.crucible("golem_wood", (ItemLike)ModItems.GOLEM_WOOD.get())
         .aspect(Aspect.MAN, 4)
         .aspect(Aspect.MOTION, 4)
         .aspect(Aspect.SOUL, 4)
         .catalyst((ItemLike)ModItems.GREATWOOD_LOG.get())
         .save(consumer, Thaumcraft.location("crucible/golem_wood"));
      CrucibleRecipeBuilder.crucible("golem_tallow", (ItemLike)ModItems.GOLEM_TALLOW.get())
         .aspect(Aspect.MAN, 8)
         .aspect(Aspect.MOTION, 8)
         .aspect(Aspect.SOUL, 8)
         .catalyst((ItemLike)ModItems.MAGIC_TALLOW_BLOCK.get())
         .save(consumer, Thaumcraft.location("crucible/golem_tallow"));
      CrucibleRecipeBuilder.crucible("golem_clay", (ItemLike)ModItems.GOLEM_CLAY.get())
         .aspect(Aspect.MAN, 4)
         .aspect(Aspect.MOTION, 4)
         .aspect(Aspect.SOUL, 4)
         .catalyst(Blocks.CLAY)
         .save(consumer, Thaumcraft.location("crucible/golem_clay"));
      CrucibleRecipeBuilder.crucible("golem_flesh", (ItemLike)ModItems.GOLEM_FLESH.get())
         .aspect(Aspect.MAN, 8)
         .aspect(Aspect.MOTION, 8)
         .aspect(Aspect.SOUL, 8)
         .catalyst((ItemLike)ModItems.FLESH_BLOCK.get())
         .save(consumer, Thaumcraft.location("crucible/golem_flesh"));
      CrucibleRecipeBuilder.crucible("golem_stone", (ItemLike)ModItems.GOLEM_STONE.get())
         .aspect(Aspect.MAN, 4)
         .aspect(Aspect.MOTION, 4)
         .aspect(Aspect.SOUL, 4)
         .catalyst(Blocks.STONE)
         .save(consumer, Thaumcraft.location("crucible/golem_stone"));
      CrucibleRecipeBuilder.crucible("golem_iron", (ItemLike)ModItems.GOLEM_IRON.get())
         .aspect(Aspect.MAN, 4)
         .aspect(Aspect.MOTION, 4)
         .aspect(Aspect.SOUL, 4)
         .catalyst(Blocks.IRON_BLOCK)
         .save(consumer, Thaumcraft.location("crucible/golem_iron"));
      CrucibleRecipeBuilder.crucible("golem_thaumium", (ItemLike)ModItems.GOLEM_THAUMIUM.get())
         .aspect(Aspect.MAN, 8)
         .aspect(Aspect.MOTION, 8)
         .aspect(Aspect.SOUL, 8)
         .catalyst((ItemLike)ModItems.THAUMIUM_BLOCK.get())
         .save(consumer, Thaumcraft.location("crucible/golem_thaumium"));
      CrucibleRecipeBuilder.crucible("golem_core_gather", (ItemLike)ModItems.GOLEM_CORE_GATHER.get())
         .aspect(Aspect.GREED, 5)
         .aspect(Aspect.EARTH, 5)
         .catalyst((ItemLike)ModItems.GOLEM_CORE_BLANK.get())
         .save(consumer, Thaumcraft.location("crucible/golem_core_gather"));
      CrucibleRecipeBuilder.crucible("golem_core_fill", (ItemLike)ModItems.GOLEM_CORE_FILL.get())
         .aspect(Aspect.HUNGER, 5)
         .aspect(Aspect.VOID, 5)
         .catalyst((ItemLike)ModItems.GOLEM_CORE_BLANK.get())
         .save(consumer, Thaumcraft.location("crucible/golem_core_fill"));
      CrucibleRecipeBuilder.crucible("golem_core_empty", (ItemLike)ModItems.GOLEM_CORE_EMPTY.get())
         .aspect(Aspect.GREED, 5)
         .aspect(Aspect.VOID, 5)
         .catalyst((ItemLike)ModItems.GOLEM_CORE_BLANK.get())
         .save(consumer, Thaumcraft.location("crucible/golem_core_empty"));
      CrucibleRecipeBuilder.crucible("golem_core_harvest", (ItemLike)ModItems.GOLEM_CORE_HARVEST.get())
         .aspect(Aspect.HARVEST, 5)
         .aspect(Aspect.CROP, 5)
         .catalyst((ItemLike)ModItems.GOLEM_CORE_BLANK.get())
         .save(consumer, Thaumcraft.location("crucible/golem_core_harvest"));
      CrucibleRecipeBuilder.crucible("golem_core_guard", (ItemLike)ModItems.GOLEM_CORE_GUARD.get())
         .aspect(Aspect.WEAPON, 5)
         .aspect(Aspect.TRAP, 5)
         .catalyst((ItemLike)ModItems.GOLEM_CORE_BLANK.get())
         .save(consumer, Thaumcraft.location("crucible/golem_core_guard"));
      CrucibleRecipeBuilder.crucible("golem_core_butcher", (ItemLike)ModItems.GOLEM_CORE_BUTCHER.get())
         .aspect(Aspect.FLESH, 5)
         .aspect(Aspect.BEAST, 5)
         .catalyst((ItemLike)ModItems.GOLEM_CORE_BLANK.get())
         .save(consumer, Thaumcraft.location("crucible/golem_core_butcher"));
      CrucibleRecipeBuilder.crucible("golem_core_liquid", (ItemLike)ModItems.GOLEM_CORE_LIQUID.get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.VOID, 5)
         .catalyst((ItemLike)ModItems.GOLEM_CORE_BLANK.get())
         .save(consumer, Thaumcraft.location("crucible/golem_core_liquid"));
      CrucibleRecipeBuilder.crucible("bath_salts", (ItemLike)ModItems.BATH_SALTS.get())
         .aspect(Aspect.MIND, 6)
         .aspect(Aspect.AURA, 6)
         .aspect(Aspect.ORDER, 6)
         .aspect(Aspect.HEAL, 6)
         .catalyst((ItemLike)ModItems.SALIS_MUNDUS.get())
         .save(consumer, Thaumcraft.location("crucible/bath_salts"));
      CrucibleRecipeBuilder.crucible("sanity_soap", (ItemLike)ModItems.SANITY_SOAP.get())
         .aspect(Aspect.MIND, 16)
         .aspect(Aspect.ELDRITCH, 16)
         .aspect(Aspect.ORDER, 16)
         .aspect(Aspect.HEAL, 16)
         .catalyst((ItemLike)ModItems.MAGIC_TALLOW_BLOCK.get())
         .save(consumer, Thaumcraft.location("crucible/sanity_soap"));
      CrucibleRecipeBuilder.crucible("gaseous_light", (ItemLike)ModItems.GAS_LIGHT.get())
         .aspect(Aspect.LIGHT, 16)
         .aspect(Aspect.AIR, 10)
         .aspect(Aspect.MOTION, 8)
         .catalyst((ItemLike)ModItems.NITOR.get())
         .save(consumer, Thaumcraft.location("crucible/gaseous_light"));
      CrucibleRecipeBuilder.crucible("gaseous_shadow", (ItemLike)ModItems.GAS_SHADOW.get())
         .aspect(Aspect.DARKNESS, 16)
         .aspect(Aspect.AIR, 10)
         .aspect(Aspect.MOTION, 8)
         .catalyst((ItemLike)ModItems.NITOR.get())
         .save(consumer, Thaumcraft.location("crucible/gaseous_shadow"));
      CrucibleRecipeBuilder.crucible("bright_nitor", (ItemLike)ModItems.BRIGHT_NITOR.get())
         .aspect(Aspect.ENERGY, 25)
         .aspect(Aspect.LIGHT, 25)
         .aspect(Aspect.AIR, 10)
         .aspect(Aspect.FIRE, 10)
         .catalyst((ItemLike)ModItems.NITOR.get())
         .save(consumer, Thaumcraft.location("crucible/bright_nitor"));
      CrucibleRecipeBuilder.crucible("spell_cloth", (ItemLike)ModItems.SPELL_CLOTH.get())
         .aspect(Aspect.MAGIC, 10)
         .aspect(Aspect.ENTROPY, 6)
         .aspect(Aspect.EXCHANGE, 4)
         .catalyst((ItemLike)ModItems.CLOTH.get())
         .save(consumer, Thaumcraft.location("crucible/spell_cloth"));
      CrucibleRecipeBuilder.crucible("magnet", (ItemLike)ModItems.MAGNET.get())
         .aspect(Aspect.BEAST, 4)
         .aspect(Aspect.MIND, 8)
         .aspect(Aspect.SENSES, 8)
         .catalyst(net.minecraft.world.item.Items.ENDER_PEARL)
         .save(consumer, Thaumcraft.location("crucible/magnet"));
      CrucibleRecipeBuilder.crucible("update_speed", (ItemLike)ModItems.SPEED_RUNE.get())
         .aspect(Aspect.MOTION, 8)
         .aspect(Aspect.SENSES, 4)
         .aspect(Aspect.AURA, 4)
         .catalyst((ItemLike)ModItems.AIR_CRYSTAL_CLUSTER.get())
         .save(consumer, Thaumcraft.location("crucible/speed_rune"));
      CrucibleRecipeBuilder.crucible("update_efficiency", (ItemLike)ModItems.EFFICIENCY_RUNE.get())
         .aspect(Aspect.MIND, 8)
         .aspect(Aspect.MECHANISM, 4)
         .aspect(Aspect.AURA, 4)
         .catalyst((ItemLike)ModItems.ORDER_CRYSTAL_CLUSTER.get())
         .save(consumer, Thaumcraft.location("crucible/efficiency_rune"));
      CrucibleRecipeBuilder.crucible("update_clean", (ItemLike)ModItems.CLEANER_RUNE.get())
         .aspect(Aspect.HEAL, 6)
         .aspect(Aspect.ORDER, 6)
         .aspect(Aspect.AURA, 6)
         .catalyst((ItemLike)ModItems.MIXED_CRYSTAL_CLUSTER.get())
         .save(consumer, Thaumcraft.location("crucible/cleaner_rune"));
      CrucibleRecipeBuilder.crucible("update_lucky", (ItemLike)ModItems.LUCKY_RUNE.get())
         .aspect(Aspect.EXCHANGE, 8)
         .aspect(Aspect.GREED, 6)
         .aspect(Aspect.ELDRITCH, 4)
         .catalyst((ItemLike)ModItems.EARTH_CRYSTAL_CLUSTER.get())
         .save(consumer, Thaumcraft.location("crucible/lucky_rune"));
      CrucibleRecipeBuilder.crucible("update_stable", (ItemLike)ModItems.STABLE_RUNE.get())
         .aspect(Aspect.ORDER, 16)
         .aspect(Aspect.EXCHANGE, 16)
         .aspect(Aspect.VOID, 16)
         .catalyst((ItemLike)ModItems.PRIMAL_CHARM.get())
         .save(consumer, Thaumcraft.location("crucible/stable_rune"));
      ResourceLocation thermalUpgrade5 = ResourceLocation.parse("thermal:upgrade_augment_5");
      ResourceLocation thermalUpgrade4 = ResourceLocation.parse("thermal:upgrade_augment_4");
      if (BuiltInRegistries.ITEM.containsKey(thermalUpgrade5) && BuiltInRegistries.ITEM.containsKey(thermalUpgrade4)) {
         Item upgrade5 = (Item)BuiltInRegistries.ITEM.get(thermalUpgrade5);
         Item upgrade4 = (Item)BuiltInRegistries.ITEM.get(thermalUpgrade4);
         CrucibleRecipeBuilder.crucible("upgrade_augment_5", upgrade5)
            .aspect(Aspect.ELDRITCH, 16)
            .aspect(Aspect.MAGIC, 32)
            .aspect(Aspect.ENERGY, 64)
            .catalyst(Ingredient.of(new ItemLike[]{upgrade4}))
            .save(consumer.withConditions(new ICondition[]{new ModLoadedCondition("thermal")}), Thaumcraft.location("crucible/upgrade_augment_5"));
      }
   }

   private void balancedShard(ItemLike itemLike, Aspect aspect, String key, RecipeOutput consumer) {
      CrucibleRecipeBuilder builder = CrucibleRecipeBuilder.crucible("crucible", (ItemLike)ModItems.BALANCED_SHARD.get());

      for (Aspect primalAspect : Aspect.getPrimalAspects()) {
         if (primalAspect != aspect) {
            builder.aspect(primalAspect, 2);
         }
      }

      builder.catalyst(itemLike).save(consumer, Thaumcraft.location("crucible/balanced_shard_from_" + key));
   }
}
