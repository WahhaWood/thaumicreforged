package thaumcraft.data.provider;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.OrCondition;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.block.jar.node.NodeJarBlockItem;
import thaumcraft.data.builder.ResearchItemBuilder;
import thaumcraft.data.condition.ConfigBooleanCondition;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModItemTags;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.mob_aspect.NumericAspectHelper;
import thaumcraft.item.wands.ItemWandCap;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.ItemWandRod;
import thaumcraft.util.ItemStackData;

public class ResearchGenerator implements DataProvider {
   private static final Set<String> KAMI_TINKERER_RESEARCH = Set.of(
      "protoclay",
      "placement_mirror",
      "dimension_shards",
      "ichor",
      "ichor_cloth",
      "ichorium",
      "cap_ichor",
      "rod_ichorcloth",
      "ichor_pouch",
      "cat_amulet",
      "block_talisman",
      "ichor_sword_gem",
      "ichor_shovel_gem",
      "ichor_pick_gem",
      "ichor_tools",
      "focus_shadowbeam",
      "focus_recall",
      "ichorcloth_legs_gem",
      "ichorcloth_helm_gem",
      "ichorcloth_chest_gem",
      "ichorcloth_boots_gem",
      "ichorcloth_armor",
      "warp_gate",
      "focus_xp_drain",
      "ichor_axe_gem",
      "ender_shard",
      "nether_shard"
   );
   protected final PathProvider researchPathProvider;
   private ItemStack basicWand;

   public ResearchGenerator(PackOutput output) {
      this.researchPathProvider = output.createPathProvider(Target.DATA_PACK, "research");
   }

   protected void buildResearch(@NotNull Consumer<ResearchItemBuilder> consumer) {
      this.basicWand = ItemWandCasting.createWand((ItemWandRod)ModItems.WOOD_WAND_ROD.get(), (ItemWandCap)ModItems.IRON_WAND_CAP.get());
      this.basicResearch(consumer);
      this.thaumaturgyResearch(consumer);
      this.alchemyResearch(consumer);
      this.artificeResearch(consumer);
      this.golemancyResearch(consumer);
      this.eldritchResearch(consumer);
      this.tinkerer(consumer);
   }

   private void basicResearch(@NotNull Consumer<ResearchItemBuilder> consumer) {
      String category = "basics";
      ResearchItemBuilder.research("aspects", category, 0, 0, Thaumcraft.location("textures/misc/r_aspects.png"))
         .stub()
         .round()
         .autoUnlock()
         .pageDesc(1, 3)
         .finish(consumer);
      ResearchItemBuilder.research("pech", category, -4, -4, Thaumcraft.location("textures/misc/r_pech.png"))
         .stub()
         .round()
         .autoUnlock()
         .pageDesc(1, 2)
         .finish(consumer);
      ResearchItemBuilder.research("nodes", category, -2, 0, Thaumcraft.location("textures/misc/r_nodes.png"))
         .stub()
         .round()
         .autoUnlock()
         .pageDesc(1, 3)
         .finish(consumer);
      ResearchItemBuilder.research("warp", category, 0, 2, Thaumcraft.location("textures/misc/r_warp.png"))
         .stub()
         .round()
         .autoUnlock()
         .pageDesc(1, 3)
         .finish(consumer);
      ResearchItemBuilder.research("research", category, 2, 0, (ItemLike)ModItems.INKWELL.get())
         .stub()
         .round()
         .autoUnlock()
         .pageDesc(1, 2)
         .pageRecipe("crafting", "thaumcraft:shaped/thaumometer")
         .pageDesc(3, 4)
         .pageRecipe("crafting", "thaumcraft:shapeless/inkwell")
         .pageRecipe("crafting", "thaumcraft:shapeless/inkwell_from_phial")
         .pageRecipe("crafting", "thaumcraft:shapeless/inkwell_repair")
         .pageDesc(5, 12)
         .finish(consumer);
      ResearchItemBuilder.research("knowfrag", category, 3, -2, (ItemLike)ModItems.KNOWLEDGE_FRAGMENT.get())
         .stub()
         .round()
         .autoUnlock()
         .parent("research")
         .pageDesc(1)
         .pageRecipe("crafting", "thaumcraft:shaped/research_notes")
         .finish(consumer);
      ResearchItemBuilder.research("thaumonomicon", category, 1, -2, (ItemLike)ModItems.THAUMONOMICON.get())
         .stub()
         .round()
         .autoUnlock()
         .parent("research")
         .pageDesc(1)
         .pageStructure(null, ModItems.WAND_CASTING.get(), Blocks.BOOKSHELF)
         .finish(consumer);
      ResearchItemBuilder.research("ore", category, -2, -2, Ingredient.of(ModItemTags.ORES_SHARD))
         .stub()
         .round()
         .autoUnlock()
         .pageDesc(1, 2)
         .pageRecipes(
            "crafting",
            "thaumcraft:shapeless/air_crystal_cluster",
            "thaumcraft:shapeless/fire_crystal_cluster",
            "thaumcraft:shapeless/water_crystal_cluster",
            "thaumcraft:shapeless/earth_crystal_cluster",
            "thaumcraft:shapeless/order_crystal_cluster",
            "thaumcraft:shapeless/entropy_crystal_cluster"
         )
         .pageDesc(3, 4)
         .finish(consumer);
      ResearchItemBuilder.research("plants", category, -2, -4, (ItemLike)ModItems.GREATWOOD_SAPLING.get())
         .stub()
         .round()
         .autoUnlock()
         .pageDesc(1)
         .pageRecipe("crafting", "thaumcraft:shapeless/greatwood_planks")
         .pageDesc(2)
         .pageRecipe("crafting", "thaumcraft:shapeless/silverwood_planks")
         .pageDesc(3, 6)
         .finish(consumer);
      ResearchItemBuilder.research("enchant", category, -4, -2, Thaumcraft.location("textures/misc/r_enchant.png"))
         .stub()
         .round()
         .autoUnlock()
         .pageDesc(1, 2)
         .finish(consumer);
      ResearchItemBuilder.research("nodetapper1", category, -4, 1, Thaumcraft.location("textures/misc/r_nodetap1.png"))
         .tag(Aspect.AURA, 3)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.MOTION, 3)
         .tag(Aspect.EXCHANGE, 3)
         .complexity(2)
         .round()
         .parent("nodes")
         .pageDesc(1)
         .finish(consumer);
      ResearchItemBuilder.research("nodepreserve", category, -6, 2, Thaumcraft.location("textures/misc/r_nodepreserve.png"))
         .tag(Aspect.AURA, 3)
         .tag(Aspect.GREED, 3)
         .tag(Aspect.SENSES, 3)
         .complexity(2)
         .round()
         .parent("nodetapper1")
         .pageDesc(1)
         .finish(consumer);
      ItemStack jarIs = new ItemStack((ItemLike)ModItems.NODE_JAR.get());
      ((NodeJarBlockItem)jarIs.getItem())
         .setAspects(jarIs, new AspectList().add(Aspect.AIR, 40).add(Aspect.FIRE, 40).add(Aspect.WATER, 40).add(Aspect.EARTH, 40));
      ((NodeJarBlockItem)jarIs.getItem()).setNodeAttributes(jarIs, NodeType.NORMAL, null, "");
      ResearchItemBuilder.research("node_jar", category, -7, 4, jarIs)
         .tag(Aspect.AURA, 6)
         .tag(Aspect.GREED, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.MOTION, 3)
         .complexity(3)
         .concealed()
         .parent("nodepreserve")
         .pageDesc(1)
         .pageStructure(
            3,
            4,
            3,
            new AspectList().add(Aspect.FIRE, 70).add(Aspect.EARTH, 70).add(Aspect.AIR, 70).add(Aspect.WATER, 70).add(Aspect.ORDER, 70).add(Aspect.ENTROPY, 70),
            ItemTags.WOODEN_SLABS,
            ItemTags.WOODEN_SLABS,
            ItemTags.WOODEN_SLABS,
            ItemTags.WOODEN_SLABS,
            ItemTags.WOODEN_SLABS,
            ItemTags.WOODEN_SLABS,
            ItemTags.WOODEN_SLABS,
            ItemTags.WOODEN_SLABS,
            ItemTags.WOODEN_SLABS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            ModBlocks.AURA_NODE.get(),
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS,
            Blocks.GLASS
         )
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("nodetapper2", category, -3, 3, Thaumcraft.location("textures/misc/r_nodetap2.png"))
         .tag(Aspect.AURA, 6)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.MOTION, 3)
         .tag(Aspect.EXCHANGE, 3)
         .complexity(2)
         .special()
         .round()
         .parent("nodetapper1")
         .pageDesc(1)
         .finish(consumer);
      ResearchItemBuilder.research("researcher1", category, 4, 1, Thaumcraft.location("textures/misc/r_researcher1.png"))
         .tag(Aspect.MIND, 3)
         .tag(Aspect.SENSES, 3)
         .tag(Aspect.ORDER, 3)
         .complexity(1)
         .round()
         .parent("research")
         .pageDesc(1)
         .finish(consumer);
      ResearchItemBuilder.research("deconstructor", category, 6, 2, (ItemLike)ModItems.DECONSTRUCTION_TABLE.get())
         .tag(Aspect.MIND, 3)
         .tag(Aspect.CRAFT, 3)
         .tag(Aspect.ENTROPY, 3)
         .complexity(1)
         .round()
         .parent("researcher1")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/deconstruction_table")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("researcher2", category, 3, 3, Thaumcraft.location("textures/misc/r_researcher2.png"))
         .tag(Aspect.MIND, 6)
         .tag(Aspect.ORDER, 3)
         .tag(Aspect.SENSES, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .special()
         .round()
         .parent("researcher1")
         .warp(1)
         .pageDesc(1)
         .finish(consumer);
      ResearchItemBuilder.research("researchdupe", category, 4, 5, Thaumcraft.location("textures/misc/r_resdupe.png"))
         .tag(Aspect.MIND, 6)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.SENSES, 3)
         .tag(Aspect.GREED, 3)
         .tag(Aspect.CRAFT, 3)
         .complexity(3)
         .round()
         .parent("researcher2")
         .pageDesc(1)
         .finish(consumer);
      ResearchItemBuilder.research("crimson", category, 0, 4, (ItemLike)ModItems.CRIMSON_RITES.get())
         .stub()
         .hidden()
         .round()
         .special()
         .warp(3)
         .triggers(new ItemStack((ItemLike)ModItems.CRIMSON_RITES.get()))
         .pageDesc(1)
         .finish(consumer);
   }

   private void tinkerer(@NotNull Consumer<ResearchItemBuilder> consumer) {
      String category = "tinkerer";
      this.tinkererResearch("dark_quartz", category, -2, 2, (ItemLike)ModItems.DARK_QUARTZ.get())
         .stub()
         .autoUnlock()
         .round()
         .pageDesc(1)
         .pageRecipes("crafting", "thaumcraft:shaped/dark_quartz_coal", "thaumcraft:shaped/dark_quartz_charcoal")
         .pageRecipe("crafting", "thaumcraft:shaped/dark_quartz_block")
         .pageRecipe("crafting", "thaumcraft:shaped/dark_quartz_slab")
         .pageRecipe("crafting", "thaumcraft:shaped/dark_quartz_stairs")
         .pageRecipe("crafting", "thaumcraft:shaped/dark_quartz_pillar")
         .pageRecipe("crafting", "thaumcraft:shaped/chiseled_dark_quartz_block")
         .finish(consumer);
      this.tinkererResearch("gaseous_light", category, 0, -3, (ItemLike)ModItems.GAS_LIGHT.get())
         .tag(Aspect.LIGHT, 2)
         .tag(Aspect.AIR, 1)
         .complexity(1)
         .parent("nitor")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/gaseous_light")
         .finish(consumer);
      this.tinkererResearch("gaseous_shadow", category, -1, -5, (ItemLike)ModItems.GAS_SHADOW.get())
         .tag(Aspect.DARKNESS, 2)
         .tag(Aspect.AIR, 1)
         .tag(Aspect.MOTION, 4)
         .complexity(2)
         .secondary()
         .parent("gaseous_light")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/gaseous_shadow")
         .finish(consumer);
      this.tinkererResearch("gas_remover", category, -2, -7, (ItemLike)ModItems.GAS_REMOVAL.get())
         .tag(Aspect.DARKNESS, 2)
         .tag(Aspect.LIGHT, 2)
         .round()
         .parent("gaseous_shadow")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/gas_remover")
         .finish(consumer);
      this.tinkererResearch("spell_cloth", category, 3, 2, (ItemLike)ModItems.SPELL_CLOTH.get())
         .tag(Aspect.MAGIC, 2)
         .tag(Aspect.CLOTH, 1)
         .complexity(2)
         .parentHidden("enchfabric")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/spell_cloth")
         .finish(consumer);
      this.tinkererResearch("enchanter", category, 5, 4, (ItemLike)ModItems.ENCHANTER.get())
         .tag(Aspect.MAGIC, 2)
         .tag(Aspect.AURA, 1)
         .tag(Aspect.ELDRITCH, 1)
         .tag(Aspect.DARKNESS, 1)
         .tag(Aspect.MIND, 1)
         .complexity(5)
         .parent("spell_cloth")
         .pageDesc(1)
         .pageDesc(2)
         .pageDesc(3)
         .pageRecipe("infusion", "thaumcraft:infusion/enchanter")
         .finish(consumer);
      this.tinkererResearch("xp_talisman", category, 4, -1, (ItemLike)ModItems.XP_TALISMAN.get())
         .tag(Aspect.GREED, 1)
         .tag(Aspect.MAGIC, 1)
         .tag(Aspect.MAN, 1)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("brain_jar", "spell_cloth")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/xp_talisman")
         .finish(consumer);
      this.tinkererResearch("blood_sword", category, -4, 6, (ItemLike)ModItems.BLOOD_SWORD.get())
         .tag(Aspect.HUNGER, 2)
         .tag(Aspect.WEAPON, 1)
         .tag(Aspect.FLESH, 1)
         .tag(Aspect.SOUL, 1)
         .complexity(3)
         .warp(1)
         .secondary()
         .parent("cleansing_talisman")
         .pageDesc(1)
         .pageDesc(2)
         .pageRecipe("infusion", "thaumcraft:infusion/blood_sword")
         .finish(consumer);
      this.tinkererResearch("magnet", category, -6, 3, (ItemLike)ModItems.MAGNET.get())
         .tag(Aspect.MECHANISM, 2)
         .tag(Aspect.MOTION, 1)
         .tag(Aspect.SENSES, 1)
         .complexity(3)
         .concealed()
         .parent("interface")
         .parentHidden("focus_telekinesis")
         .autoInfusionParent(false)
         .pageDesc(1)
         .pageDesc(2)
         .pageRecipe("crucible", "thaumcraft:crucible/magnet")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/mob_magnet")
         .finish(consumer);
      this.tinkererResearch("revealing_helm", category, 0, 0, (ItemLike)ModItems.REVEALING_HELMET.get())
         .tag(Aspect.AURA, 2)
         .tag(Aspect.ARMOR, 1)
         .complexity(1)
         .parent("goggles")
         .parentHidden("thaumium")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/revealing_helmet")
         .finish(consumer);
      this.tinkererResearch("dimension_shards", category, 7, 8, (ItemLike)ModItems.ENDER_SHARD.get()).stub().round().autoUnlock().pageDesc(1).finish(consumer);
      this.tinkererResearch("ender_shard", category, 8, 10, (ItemLike)ModItems.ENDER_SHARD.get())
         .tag(Aspect.ELDRITCH, 4)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.CRYSTAL, 2)
         .complexity(5)
         .parent("dimension_shards")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/ender_shard")
         .finish(consumer);
      this.tinkererResearch("nether_shard", category, 6, 10, (ItemLike)ModItems.NETHER_SHARD.get())
         .tag(Aspect.FIRE, 4)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.CRYSTAL, 2)
         .complexity(5)
         .parent("dimension_shards")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/nether_shard")
         .finish(consumer);
      this.tinkererResearch("ichor", category, 9, 8, (ItemLike)ModItems.ICHOR.get())
         .tag(Aspect.MAN, 1)
         .tag(Aspect.LIGHT, 2)
         .tag(Aspect.SOUL, 1)
         .tag(Aspect.TAINT, 1)
         .complexity(5)
         .warp(5)
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/ichor")
         .finish(consumer);
      this.tinkererResearch("ichor_cloth", category, 11, 7, (ItemLike)ModItems.ICHOR_CLOTH.get())
         .tag(Aspect.CLOTH, 2)
         .tag(Aspect.LIGHT, 1)
         .tag(Aspect.CRAFT, 1)
         .tag(Aspect.SENSES, 1)
         .complexity(5)
         .concealed()
         .parent("ichor")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/ichor_cloth")
         .finish(consumer);
      this.tinkererResearch("ichorium", category, 11, 9, (ItemLike)ModItems.ICHORIUM.get())
         .tag(Aspect.METAL, 2)
         .tag(Aspect.LIGHT, 1)
         .tag(Aspect.CRAFT, 1)
         .tag(Aspect.TOOL, 1)
         .complexity(5)
         .concealed()
         .parent("ichor")
         .parentHidden("ichor_cloth")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/ichorium")
         .finish(consumer);
      this.tinkererResearch("cap_ichor", category, 11, 11, (ItemLike)ModItems.ICHOR_WAND_CAP.get())
         .tag(Aspect.TOOL, 2)
         .tag(Aspect.METAL, 1)
         .tag(Aspect.LIGHT, 1)
         .tag(Aspect.MAGIC, 1)
         .complexity(5)
         .warp(2)
         .concealed()
         .parent("ichorium")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/cap_ichor")
         .finish(consumer);
      this.tinkererResearch("rod_ichorcloth", category, 14, 2, (ItemLike)ModItems.ICHOR_WAND_ROD.get())
         .tag(Aspect.TOOL, 2)
         .tag(Aspect.CLOTH, 1)
         .tag(Aspect.LIGHT, 1)
         .tag(Aspect.MAGIC, 1)
         .complexity(5)
         .warp(1)
         .concealed()
         .parent("ichor_cloth")
         .parentHidden("cap_ichor")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/rod_ichorcloth")
         .finish(consumer);
      this.tinkererResearch("ichor_pouch", category, 13, 6, (ItemLike)ModItems.ICHOR_POUCH.get())
         .tag(Aspect.VOID, 2)
         .tag(Aspect.CLOTH, 1)
         .tag(Aspect.ELDRITCH, 1)
         .tag(Aspect.MAN, 1)
         .complexity(5)
         .concealed()
         .parent("ichor_cloth")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/ichor_pouch")
         .finish(consumer);
      this.tinkererResearch("cat_amulet", category, 13, 10, (ItemLike)ModItems.CAT_AMULET.get())
         .tag(Aspect.MIND, 2)
         .tag(Aspect.ORDER, 1)
         .tag(Aspect.DARKNESS, 1)
         .tag(Aspect.DEATH, 1)
         .complexity(5)
         .concealed()
         .parent("ichorium")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/cat_amulet")
         .finish(consumer);
      this.tinkererResearch("ichor_tools", category, 13, 12, (ItemLike)ModItems.ICHOR_PICKAXE.get())
         .tag(Aspect.TOOL, 2)
         .tag(Aspect.WEAPON, 1)
         .tag(Aspect.METAL, 1)
         .tag(Aspect.CRAFT, 1)
         .complexity(5)
         .warp(2)
         .concealed()
         .parent("ichorium")
         .parentHidden("rod_ichorcloth")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/ichor_pickaxe")
         .pageRecipe("arcane", "thaumcraft:arcane/ichor_shovel")
         .pageRecipe("arcane", "thaumcraft:arcane/ichor_axe")
         .pageRecipe("arcane", "thaumcraft:arcane/ichor_sword")
         .finish(consumer);
      this.tinkererResearch("ichor_pick_gem", category, 13, 15, (ItemLike)ModItems.ICHOR_PICKAXE_ADV.get())
         .tag(Aspect.FIRE, 2)
         .tag(Aspect.TOOL, 1)
         .tag(Aspect.MINE, 1)
         .tag(Aspect.EARTH, 1)
         .complexity(5)
         .concealed()
         .parent("ichor_tools")
         .pageDesc(1)
         .pageDesc(2)
         .pageRecipe("infusion", "thaumcraft:infusion/ichor_pickaxe_adv")
         .finish(consumer);
      this.tinkererResearch("ichor_shovel_gem", category, 15, 15, (ItemLike)ModItems.ICHOR_SHOVEL_ADV.get())
         .tag(Aspect.EARTH, 3)
         .tag(Aspect.TOOL, 1)
         .tag(Aspect.MINE, 1)
         .complexity(5)
         .concealed()
         .parent("ichor_tools")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/ichor_shovel_adv")
         .finish(consumer);
      this.tinkererResearch("ichor_axe_gem", category, 16, 14, (ItemLike)ModItems.ICHOR_AXE_ADV.get())
         .tag(Aspect.WATER, 2)
         .tag(Aspect.TOOL, 1)
         .tag(Aspect.TREE, 1)
         .tag(Aspect.CROP, 1)
         .complexity(5)
         .concealed()
         .parent("ichor_tools")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/ichor_axe_adv")
         .finish(consumer);
      this.tinkererResearch("ichor_sword_gem", category, 16, 12, (ItemLike)ModItems.ICHOR_SWORD_ADV.get())
         .tag(Aspect.AIR, 2)
         .tag(Aspect.WEAPON, 1)
         .tag(Aspect.SOUL, 1)
         .tag(Aspect.HUNGER, 1)
         .complexity(5)
         .concealed()
         .parent("ichor_tools")
         .pageDesc(1)
         .pageDesc(2)
         .pageRecipe("infusion", "thaumcraft:infusion/ichor_sword_adv")
         .finish(consumer);
      this.tinkererResearch("block_talisman", category, 14, 17, (ItemLike)ModItems.BLOCK_TALISMAN.get())
         .tag(Aspect.VOID, 2)
         .tag(Aspect.DARKNESS, 1)
         .tag(Aspect.ELDRITCH, 1)
         .tag(Aspect.MAGIC, 1)
         .complexity(5)
         .concealed()
         .parent("ichor_pick_gem", "ichor_shovel_gem")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/block_talisman")
         .finish(consumer);
      this.tinkererResearch("ichorcloth_armor", category, 17, 5, (ItemLike)ModItems.ICHOR_HELMET.get())
         .tag(Aspect.ARMOR, 2)
         .tag(Aspect.CLOTH, 1)
         .tag(Aspect.LIGHT, 1)
         .tag(Aspect.CRAFT, 1)
         .complexity(5)
         .warp(4)
         .concealed()
         .parent("ichor_cloth")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/ichor_helmet")
         .pageRecipe("arcane", "thaumcraft:arcane/ichor_chest")
         .pageRecipe("arcane", "thaumcraft:arcane/ichor_leggings")
         .pageRecipe("arcane", "thaumcraft:arcane/ichor_boots")
         .finish(consumer);
      this.tinkererResearch("ichorcloth_helm_gem", category, 18, 3, (ItemLike)ModItems.ICHOR_GEM_HELMET.get())
         .tag(Aspect.WATER, 2)
         .tag(Aspect.HEAL, 1)
         .tag(Aspect.HUNGER, 1)
         .tag(Aspect.AURA, 1)
         .complexity(5)
         .concealed()
         .parent("ichorcloth_armor")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/ichor_gem_helmet")
         .finish(consumer);
      this.tinkererResearch("ichorcloth_chest_gem", category, 17, 7, (ItemLike)ModItems.ICHOR_GEM_CHEST.get())
         .tag(Aspect.AIR, 2)
         .tag(Aspect.MOTION, 1)
         .tag(Aspect.FLIGHT, 1)
         .tag(Aspect.ELDRITCH, 1)
         .complexity(5)
         .concealed()
         .parent("ichorcloth_armor")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/ichor_gem_chest")
         .finish(consumer);
      this.tinkererResearch("ichorcloth_legs_gem", category, 17, 9, (ItemLike)ModItems.ICHOR_GEM_LEGGINGS.get())
         .tag(Aspect.FIRE, 2)
         .tag(Aspect.HEAL, 1)
         .tag(Aspect.GREED, 1)
         .tag(Aspect.ENERGY, 1)
         .complexity(5)
         .concealed()
         .parent("ichorcloth_armor")
         .pageDesc(1)
         .pageDesc(2)
         .pageRecipe("infusion", "thaumcraft:infusion/ichor_gem_leggings")
         .finish(consumer);
      this.tinkererResearch("ichorcloth_boots_gem", category, 15, 10, (ItemLike)ModItems.ICHOR_GEM_BOOTS.get())
         .tag(Aspect.EARTH, 2)
         .tag(Aspect.TRAVEL, 1)
         .tag(Aspect.MINE, 1)
         .tag(Aspect.PLANT, 1)
         .complexity(5)
         .concealed()
         .parent("ichorcloth_armor")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/ichor_gem_boots")
         .finish(consumer);
      this.tinkererResearch("placement_mirror", category, 17, 16, (ItemLike)ModItems.PLACEMENT_MIRROR.get())
         .tag(Aspect.CRAFT, 2)
         .tag(Aspect.CRYSTAL, 1)
         .tag(Aspect.ELDRITCH, 1)
         .tag(Aspect.MIND, 1)
         .complexity(5)
         .concealed()
         .parent("block_talisman")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/placement_mirror")
         .finish(consumer);
      this.tinkererResearch("protoclay", category, 12, 17, (ItemLike)ModItems.PROTOCLAY.get())
         .tag(Aspect.TOOL, 2)
         .tag(Aspect.MINE, 1)
         .tag(Aspect.MAN, 1)
         .tag(Aspect.MECHANISM, 1)
         .complexity(5)
         .concealed()
         .parent("ichor_pick_gem")
         .parentHidden("ichor_shovel_gem")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/protoclay")
         .finish(consumer);
      this.tinkererResearch("interface", category, -4, 2, (ItemLike)ModItems.TRANSVECTOR_INTERFACE.get())
         .tag(Aspect.ENTROPY, 4)
         .tag(Aspect.ORDER, 4)
         .complexity(1)
         .parent("dark_quartz")
         .pageDesc(1)
         .pageDesc(2)
         .pageRecipe("arcane", "thaumcraft:arcane/connector")
         .pageDesc(3)
         .pageRecipe("arcane", "thaumcraft:arcane/transvector_interface")
         .finish(consumer);
      this.tinkererResearch("dislocator", category, -6, 1, (ItemLike)ModItems.TRANSVECTOR_DISLOCATOR.get())
         .tag(Aspect.TRAVEL, 2)
         .tag(Aspect.MECHANISM, 1)
         .tag(Aspect.ELDRITCH, 1)
         .complexity(3)
         .concealed()
         .secondary()
         .parent("interface")
         .parentHidden("mirror")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/transvector_dislocator")
         .finish(consumer);
      this.tinkererResearch("cleansing_talisman", category, -3, 4, (ItemLike)ModItems.CLEANSING_TALISMAN.get())
         .tag(Aspect.HEAL, 2)
         .tag(Aspect.ORDER, 1)
         .tag(Aspect.POISON, 1)
         .complexity(3)
         .secondary()
         .parent("dark_quartz")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/cleansing_talisman")
         .finish(consumer);
      this.tinkererResearch("bright_nitor", category, 1, -5, (ItemLike)ModItems.BRIGHT_NITOR.get())
         .tag(Aspect.LIGHT, 2)
         .tag(Aspect.FIRE, 1)
         .tag(Aspect.ENERGY, 1)
         .tag(Aspect.AIR, 1)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("gaseous_light")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/bright_nitor")
         .finish(consumer);
      this.tinkererResearch("funnel", category, 0, -7, (ItemLike)ModItems.FUNNEL.get())
         .tag(Aspect.TOOL, 1)
         .tag(Aspect.TRAVEL, 2)
         .complexity(1)
         .secondary()
         .concealed()
         .parent("bright_nitor")
         .parentHidden("distilessence")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/funnel")
         .finish(consumer);
      this.tinkererResearch("repairer", category, -1, -9, (ItemLike)ModItems.REPAIRER.get())
         .tag(Aspect.TOOL, 2)
         .tag(Aspect.CRAFT, 1)
         .tag(Aspect.ORDER, 1)
         .tag(Aspect.MAGIC, 1)
         .complexity(3)
         .concealed()
         .parent("funnel")
         .parentHidden("thaumium", "enchfabric")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/repairer")
         .finish(consumer);
      this.tinkererResearch("platform", category, -2, 6, (ItemLike)ModItems.PLATFORM.get())
         .tag(Aspect.SENSES, 2)
         .tag(Aspect.TREE, 1)
         .tag(Aspect.MOTION, 1)
         .complexity(3)
         .secondary()
         .concealed()
         .parent("cleansing_talisman")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/platform")
         .finish(consumer);
      this.tinkererResearch("mobilizer", category, -7, 5, (ItemLike)ModItems.MOBILIZER.get())
         .tag(Aspect.MOTION, 2)
         .tag(Aspect.ORDER, 2)
         .complexity(3)
         .secondary()
         .parent("magnet")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/mobilizer")
         .pageRecipe("arcane", "thaumcraft:arcane/mobilizer_relay")
         .finish(consumer);
      this.tinkererResearch("summon", category, -5, 8, (ItemLike)ModItems.SUMMONER.get())
         .tag(Aspect.WEAPON, 1)
         .tag(Aspect.BEAST, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(3)
         .warp(3)
         .parent("blood_sword")
         .pageDesc(1)
         .pageDesc(2)
         .pageRecipes("crafting", this.mobAspectRecipeIds("shaped/mob_aspect_condensed_"))
         .pageRecipes("infusion", this.mobAspectRecipeIds("infusion/mob_aspect_infused_"))
         .pageRecipe("infusion", "thaumcraft:infusion/summoner")
         .finish(consumer);
      this.tinkererResearch("warp_gate", category, 19, 6, (ItemLike)ModItems.WARP_GATE.get())
         .tag(Aspect.TRAVEL, 2)
         .tag(Aspect.ELDRITCH, 1)
         .tag(Aspect.FLIGHT, 1)
         .tag(Aspect.MECHANISM, 1)
         .complexity(5)
         .concealed()
         .parent("ichorcloth_chest_gem")
         .parentHidden("ichorcloth_boots_gem")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/warp_gate")
         .pageRecipe("infusion", "thaumcraft:infusion/sky_pearl")
         .finish(consumer);
      this.tinkererResearch("focus_smelt", category, -2, -2, (ItemLike)ModItems.FOCUS_SMELT.get())
         .tag(Aspect.FIRE, 2)
         .tag(Aspect.ENERGY, 1)
         .tag(Aspect.MAGIC, 1)
         .complexity(2)
         .concealed()
         .parent("focus_excavation")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/focus_smelt")
         .finish(consumer);
      this.tinkererResearch("focus_flight", category, -3, -4, (ItemLike)ModItems.FOCUS_FLIGHT.get())
         .tag(Aspect.MOTION, 1)
         .tag(Aspect.MAGIC, 1)
         .tag(Aspect.AIR, 2)
         .complexity(2)
         .concealed()
         .parent("focus_smelt")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/focus_flight")
         .finish(consumer);
      this.tinkererResearch("focus_telekinesis", category, -4, -6, (ItemLike)ModItems.FOCUS_TELEKINESIS.get())
         .tag(Aspect.ELDRITCH, 2)
         .tag(Aspect.MAGIC, 1)
         .tag(Aspect.MOTION, 1)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("focus_flight")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/focus_telekinesis")
         .finish(consumer);
      this.tinkererResearch("focus_deflect", category, -4, -3, (ItemLike)ModItems.FOCUS_DEFLECT.get())
         .tag(Aspect.MOTION, 2)
         .tag(Aspect.AIR, 1)
         .tag(Aspect.ORDER, 1)
         .tag(Aspect.DEATH, 1)
         .complexity(3)
         .secondary()
         .concealed()
         .parent("focus_smelt")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/focus_deflect")
         .finish(consumer);
      this.tinkererResearch("focus_heal", category, -6, -4, (ItemLike)ModItems.FOCUS_HEAL.get())
         .tag(Aspect.HEAL, 2)
         .tag(Aspect.SOUL, 1)
         .tag(Aspect.MAGIC, 1)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("focus_deflect")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/focus_heal")
         .finish(consumer);
      this.tinkererResearch("focus_ender_chest", category, -6, -2, (ItemLike)ModItems.FOCUS_ENDER_CHEST.get())
         .tag(Aspect.ELDRITCH, 2)
         .tag(Aspect.VOID, 1)
         .tag(Aspect.MAGIC, 1)
         .complexity(2)
         .warp(1)
         .concealed()
         .parent("focus_deflect")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/focus_ender_chest")
         .finish(consumer);
      this.tinkererResearch("focus_recall", category, 20, 8, (ItemLike)ModItems.FOCUS_RECALL.get())
         .condition(ConfigBooleanCondition.allowMirrors())
         .condition(ConfigBooleanCondition.enableKami())
         .tag(Aspect.TRAVEL, 2)
         .tag(Aspect.ELDRITCH, 1)
         .tag(Aspect.FLIGHT, 1)
         .tag(Aspect.MAGIC, 1)
         .complexity(5)
         .concealed()
         .parent("warp_gate")
         .parentHidden("rod_ichorcloth")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/focus_recall")
         .finish(consumer);
      this.tinkererResearch("focus_shadowbeam", category, 14, 4, (ItemLike)ModItems.FOCUS_SHADOWBEAM.get())
         .tag(Aspect.DARKNESS, 2)
         .tag(Aspect.MAGIC, 1)
         .tag(Aspect.ELDRITCH, 1)
         .tag(Aspect.TAINT, 1)
         .complexity(5)
         .concealed()
         .parent("rod_ichorcloth")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/focus_shadowbeam")
         .finish(consumer);
      this.tinkererResearch("focus_xp_drain", category, 12, 3, (ItemLike)ModItems.FOCUS_XP_DRAIN.get())
         .tag(Aspect.MIND, 2)
         .tag(Aspect.MAGIC, 1)
         .tag(Aspect.AURA, 1)
         .tag(Aspect.MAN, 1)
         .complexity(5)
         .concealed()
         .parent("rod_ichorcloth")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/focus_xp_drain")
         .finish(consumer);
      this.tinkererResearch("ttench_ascent_boost", category, 6, 2, this.enchantResearchIcon("ascent_boost"))
         .tag(Aspect.AIR, 1)
         .tag(Aspect.MOTION, 1)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("enchanter")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_ascent_boost")
         .finish(consumer);
      this.tinkererResearch("ttench_slow_fall", category, 7, 3, this.enchantResearchIcon("slow_fall"))
         .tag(Aspect.AIR, 1)
         .tag(Aspect.MOTION, 1)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("enchanter")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_slow_fall")
         .finish(consumer);
      this.tinkererResearch("ttench_auto_smelt", category, 7, 5, this.enchantResearchIcon("auto_smelt"))
         .tag(Aspect.FIRE, 1)
         .tag(Aspect.ENTROPY, 1)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("enchanter")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_auto_smelt")
         .finish(consumer);
      this.tinkererResearch("ttench_desintegrate", category, 6, 6, this.enchantResearchIcon("desintegrate"))
         .tag(Aspect.ENTROPY, 1)
         .tag(Aspect.VOID, 1)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("enchanter")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_desintegrate")
         .finish(consumer);
      this.tinkererResearch("ttench_quick_draw", category, 4, 6, this.enchantResearchIcon("quick_draw"))
         .tag(Aspect.SENSES, 1)
         .tag(Aspect.WEAPON, 1)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("enchanter")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_quick_draw")
         .finish(consumer);
      this.tinkererResearch("ttench_vampirism", category, 3, 5, this.enchantResearchIcon("vamprisim"))
         .tag(Aspect.HUNGER, 1)
         .tag(Aspect.WEAPON, 1)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("enchanter")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_vampirism")
         .finish(consumer);
      this.tinkererResearch("ttench_focused", category, 2, 7, this.enchantResearchIcon("focused_strikes"))
         .tag(Aspect.ORDER, 1)
         .tag(Aspect.WEAPON, 1)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("ttench_vampirism")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_focused")
         .finish(consumer);
      this.tinkererResearch("ttench_dispersed", category, 1, 6, this.enchantResearchIcon("dispersed_strikes"))
         .tag(Aspect.ENTROPY, 1)
         .tag(Aspect.WEAPON, 1)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("ttench_vampirism")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_dispersed")
         .finish(consumer);
      this.tinkererResearch("ttench_final", category, 0, 8, this.enchantResearchIcon("final_strike"))
         .tag(Aspect.ENTROPY, 3)
         .tag(Aspect.ORDER, 3)
         .tag(Aspect.WEAPON, 3)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("ttench_focused", "ttench_dispersed")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_final")
         .finish(consumer);
      this.tinkererResearch("ttench_pounce", category, 7, 0, this.enchantResearchIcon("pounce"))
         .tag(Aspect.AIR, 3)
         .tag(Aspect.ORDER, 3)
         .tag(Aspect.ARMOR, 3)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("ttench_ascent_boost")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_pounce")
         .finish(consumer);
      this.tinkererResearch("ttench_shatter", category, 5, 8, this.enchantResearchIcon("shatter"))
         .tag(Aspect.EARTH, 3)
         .tag(Aspect.ENTROPY, 3)
         .tag(Aspect.TOOL, 1)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("ttench_desintegrate")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_shatter")
         .finish(consumer);
      this.tinkererResearch("ttench_shockwave", category, 9, 2, this.enchantResearchIcon("shockwave"))
         .tag(Aspect.AIR, 3)
         .tag(Aspect.ENTROPY, 3)
         .tag(Aspect.ARMOR, 1)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("ttench_slow_fall")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_shockwave")
         .finish(consumer);
      this.tinkererResearch("ttench_tunnel", category, 9, 6, this.enchantResearchIcon("tunnel"))
         .tag(Aspect.EARTH, 3)
         .tag(Aspect.ORDER, 3)
         .tag(Aspect.TOOL, 1)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("ttench_auto_smelt")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_tunnel")
         .finish(consumer);
      this.tinkererResearch("ttench_valiance", category, 1, 4, this.enchantResearchIcon("valiance"))
         .tag(Aspect.WEAPON, 3)
         .tag(Aspect.HEAL, 3)
         .tag(Aspect.MAGIC, 2)
         .complexity(2)
         .secondary()
         .parent("ttench_vampirism")
         .pageDesc(1)
         .pageRecipe("enchant", "thaumcraft:enchant/ttench_valiance")
         .finish(consumer);
   }

   private ResearchItemBuilder tinkererResearch(String key, String category, int column, int row, ItemLike icon) {
      return ResearchItemBuilder.research(key, category, column, row, icon)
         .researchType(Thaumcraft.location(KAMI_TINKERER_RESEARCH.contains(key) ? "kami" : "tt"));
   }

   private ResearchItemBuilder tinkererResearch(String key, String category, int column, int row, ResourceLocation icon) {
      return ResearchItemBuilder.research(key, category, column, row, icon)
         .researchType(Thaumcraft.location(KAMI_TINKERER_RESEARCH.contains(key) ? "kami" : "tt"));
   }

   private static String entityTriggerId(EntityType<?> entityType) {
      return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
   }

   private ResourceLocation enchantResearchIcon(String path) {
      return Thaumcraft.location("textures/enchant/" + path + ".png");
   }

   private String[] mobAspectRecipeIds(String pathPrefix) {
      List<String> ids = new ArrayList<>();

      for (int i = 0; i < NumericAspectHelper.getCount(); i++) {
         Aspect aspect = NumericAspectHelper.getAspect(i);
         if (aspect != null) {
            ids.add("thaumcraft:" + pathPrefix + aspect.getTag());
         }
      }

      return ids.toArray(String[]::new);
   }

   private void thaumaturgyResearch(@NotNull Consumer<ResearchItemBuilder> consumer) {
      String category = "thaumaturgy";
      ResearchItemBuilder.research(
            "basic_thaumaturgy",
            category,
            0,
            0,
            ItemWandCasting.createWand((ItemWandRod)ModItems.WOOD_WAND_ROD.get(), (ItemWandCap)ModItems.IRON_WAND_CAP.get())
         )
         .autoUnlock()
         .stub()
         .round()
         .pageDesc(1, 2)
         .pageRecipe("crafting", "thaumcraft:shaped/iron_wand_cap")
         .pageRecipe("crafting", "thaumcraft:shaped/basic_wand")
         .finish(consumer);
      ResearchItemBuilder.research("focus_fire", category, 2, -2, (ItemLike)ModItems.FOCUS_FIRE.get())
         .tag(Aspect.FIRE, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(1)
         .parent("basic_thaumaturgy")
         .pageDesc(1, 2)
         .pageRecipe("arcane", "thaumcraft:arcane/focus_fire")
         .finish(consumer);
      ResearchItemBuilder.research("focus_frost", category, 1, -5, (ItemLike)ModItems.FOCUS_FROST.get())
         .tag(Aspect.WATER, 3)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.COLD, 6)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("focus_fire")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/focus_frost")
         .finish(consumer);
      ResearchItemBuilder.research("focus_hell_bat", category, 3, -7, (ItemLike)ModItems.FOCUS_HELL_BAT.get())
         .tag(Aspect.TRAVEL, 3)
         .tag(Aspect.BEAST, 6)
         .tag(Aspect.FIRE, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .warp(2)
         .hidden()
         .triggers(entityTriggerId((EntityType<?>)ModEntities.FIRE_BAT.get()))
         .triggers(Aspect.FIRE)
         .parentHidden("focus_fire", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/focus_hell_bat")
         .finish(consumer);
      ResearchItemBuilder.research("focus_excavation", category, 0, -3, (ItemLike)ModItems.FOCUS_EXCAVATION.get())
         .tag(Aspect.EARTH, 3)
         .tag(Aspect.ENTROPY, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .concealed()
         .parent("focus_fire")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/focus_excavation")
         .finish(consumer);
      ResearchItemBuilder.research("focus_warding", category, -2, -4, (ItemLike)ModItems.FOCUS_WARDING.get())
         .tag(Aspect.EARTH, 6)
         .tag(Aspect.ARMOR, 3)
         .tag(Aspect.ORDER, 3)
         .tag(Aspect.MIND, 3)
         .complexity(3)
         .concealed()
         .parent("focus_excavation", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/focus_warding")
         .finish(consumer);
      ResearchItemBuilder.research("focus_shock", category, 3, -5, (ItemLike)ModItems.FOCUS_SHOCK.get())
         .tag(Aspect.AIR, 3)
         .tag(Aspect.ENERGY, 6)
         .tag(Aspect.MAGIC, 3)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("focus_fire")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/focus_shock")
         .finish(consumer);
      ResearchItemBuilder.research("focus_trade", category, 4, -3, (ItemLike)ModItems.FOCUS_TRADE.get())
         .tag(Aspect.EARTH, 3)
         .tag(Aspect.EXCHANGE, 6)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .concealed()
         .parent("focus_fire")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/focus_trade")
         .finish(consumer);
      ResearchItemBuilder.research("focus_portable_hole", category, 7, -2, (ItemLike)ModItems.FOCUS_PORTABLE_HOLE.get())
         .tag(Aspect.TRAVEL, 3)
         .tag(Aspect.ENTROPY, 3)
         .tag(Aspect.ELDRITCH, 6)
         .tag(Aspect.AIR, 3)
         .complexity(2)
         .concealed()
         .parent("focus_trade", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/focus_portable_hole")
         .finish(consumer);
      ResearchItemBuilder.research("focus_pouch", category, 4, -1, (ItemLike)ModItems.FOCUS_POUCH.get())
         .tag(Aspect.VOID, 6)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.TOOL, 3)
         .complexity(1)
         .parent("focus_fire")
         .secondary()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/focus_pouch")
         .finish(consumer);
      ResearchItemBuilder.research("gold_cap", category, 3, 2, (ItemLike)ModItems.GOLD_WAND_CAP.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.GREED, 3)
         .tag(Aspect.TOOL, 3)
         .complexity(1)
         .parent("basic_thaumaturgy")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/gold_wand_cap")
         .finish(consumer);
      ResearchItemBuilder.research("thaumium_cap", category, 5, 4, (ItemLike)ModItems.THAUMIUM_INERT_WAND_CAP.get())
         .tag(Aspect.METAL, 6)
         .tag(Aspect.MAGIC, 6)
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.AURA, 3)
         .complexity(2)
         .parent("gold_cap", "thaumium", "infusion")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/thaumium_inert_wand_cap")
         .pageRecipe("infusion", "thaumcraft:infusion/thaumium_wand_cap")
         .finish(consumer);
      ResearchItemBuilder.research("copper_cap", category, 2, 0, (ItemLike)ModItems.COPPER_WAND_CAP.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.TOOL, 3)
         .complexity(1)
         .parent("basic_thaumaturgy")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/copper_wand_cap")
         .finish(consumer);
      ResearchItemBuilder.research("silver_cap", category, 5, 1, (ItemLike)ModItems.SILVER_WAND_CAP.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.GREED, 3)
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.AURA, 3)
         .complexity(1)
         .concealed()
         .parent("gold_cap", "infusion")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/silver_wand_cap")
         .pageRecipe("infusion", "thaumcraft:infusion/silver_wand_cap")
         .finish(consumer);
      ResearchItemBuilder.research("greatwood_rod", category, -5, 2, (ItemLike)ModItems.GREATWOOD_WAND_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.TREE, 6)
         .tag(Aspect.MAGIC, 3)
         .complexity(1)
         .parent("basic_thaumaturgy")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/greatwood_wand_rod")
         .finish(consumer);
      ResearchItemBuilder.research("reed_rod", category, -5, -1, (ItemLike)ModItems.REED_WAND_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.AIR, 6)
         .tag(Aspect.PLANT, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("greatwood_rod", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/reed_wand_rod")
         .finish(consumer);
      ResearchItemBuilder.research("blaze_rod", category, -7, 0, (ItemLike)ModItems.BLAZE_WAND_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.FIRE, 6)
         .tag(Aspect.ENERGY, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("greatwood_rod", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/blaze_wand_rod")
         .finish(consumer);
      ResearchItemBuilder.research("obsidian_rod", category, -8, 2, (ItemLike)ModItems.OBSIDIAN_WAND_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.EARTH, 6)
         .tag(Aspect.FIRE, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("greatwood_rod", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/obsidian_wand_rod")
         .finish(consumer);
      ResearchItemBuilder.research("ice_rod", category, -7, 4, (ItemLike)ModItems.ICE_WAND_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.COLD, 6)
         .tag(Aspect.WATER, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("greatwood_rod", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/ice_wand_rod")
         .finish(consumer);
      ResearchItemBuilder.research("quartz_rod", category, -5, 5, (ItemLike)ModItems.QUARTZ_WAND_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.ORDER, 6)
         .tag(Aspect.CRYSTAL, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("greatwood_rod", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/quartz_wand_rod")
         .finish(consumer);
      ResearchItemBuilder.research("bone_rod", category, -3, 0, (ItemLike)ModItems.BONE_WAND_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.ENTROPY, 6)
         .tag(Aspect.UNDEAD, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .warp(1)
         .secondary()
         .concealed()
         .parent("greatwood_rod", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/bone_wand_rod")
         .finish(consumer);
      ResearchItemBuilder.research("silverwood_rod", category, -2, 5, (ItemLike)ModItems.SILVERWOOD_WAND_ROD.get())
         .tag(Aspect.TOOL, 6)
         .tag(Aspect.TREE, 6)
         .tag(Aspect.MAGIC, 9)
         .complexity(3)
         .parent("greatwood_rod", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/silverwood_wand_rod")
         .finish(consumer);
      ResearchItemBuilder.research(
            "sceptre",
            category,
            0,
            4,
            ItemWandCasting.createSceptre((ItemWandRod)ModItems.SILVERWOOD_WAND_ROD.get(), (ItemWandCap)ModItems.THAUMIUM_WAND_CAP.get())
         )
         .tag(Aspect.TOOL, 6)
         .tag(Aspect.CRAFT, 6)
         .tag(Aspect.TREE, 6)
         .tag(Aspect.MAGIC, 9)
         .complexity(3)
         .concealed()
         .parent("silverwood_rod")
         .pageDesc(1)
         .pageRecipes(
            "arcane", "thaumcraft:arcane/sceptre_iron_wood", "thaumcraft:arcane/sceptre_gold_greatwood", "thaumcraft:arcane/sceptre_thaumium_silverwood"
         )
         .finish(consumer);
      ResearchItemBuilder.research("greatwood_staff_rod", category, -1, 7, (ItemLike)ModItems.GREATWOOD_STAFF_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.TREE, 6)
         .tag(Aspect.MAGIC, 3)
         .complexity(1)
         .parent("silverwood_rod")
         .pageDesc(1, 2)
         .pageRecipe("arcane", "thaumcraft:arcane/greatwood_staff_rod")
         .finish(consumer);
      ResearchItemBuilder.research("reed_staff_rod", category, -5, -2, (ItemLike)ModItems.REED_STAFF_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.AIR, 6)
         .tag(Aspect.PLANT, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("reed_rod")
         .parentHidden("greatwood_staff_rod")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/reed_staff_rod")
         .finish(consumer);
      ResearchItemBuilder.research("blaze_staff_rod", category, -8, -1, (ItemLike)ModItems.BLAZE_STAFF_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.FIRE, 6)
         .tag(Aspect.ENERGY, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("blaze_rod")
         .parentHidden("greatwood_staff_rod")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/blaze_staff_rod")
         .finish(consumer);
      ResearchItemBuilder.research("obsidian_staff_rod", category, -9, 2, (ItemLike)ModItems.OBSIDIAN_STAFF_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.EARTH, 6)
         .tag(Aspect.FIRE, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("obsidian_rod")
         .parentHidden("greatwood_staff_rod")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/obsidian_staff_rod")
         .finish(consumer);
      ResearchItemBuilder.research("ice_staff_rod", category, -8, 5, (ItemLike)ModItems.ICE_STAFF_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.COLD, 6)
         .tag(Aspect.WATER, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("ice_rod")
         .parentHidden("greatwood_staff_rod")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/ice_staff_rod")
         .finish(consumer);
      ResearchItemBuilder.research("quartz_staff_rod", category, -4, 6, (ItemLike)ModItems.QUARTZ_STAFF_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.ORDER, 6)
         .tag(Aspect.CRYSTAL, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("quartz_rod")
         .parentHidden("greatwood_staff_rod")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/quartz_staff_rod")
         .finish(consumer);
      ResearchItemBuilder.research("bone_staff_rod", category, -2, -1, (ItemLike)ModItems.BONE_STAFF_ROD.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.ENTROPY, 6)
         .tag(Aspect.UNDEAD, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .warp(1)
         .secondary()
         .concealed()
         .parent("bone_rod")
         .parentHidden("greatwood_staff_rod")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/bone_staff_rod")
         .finish(consumer);
      ResearchItemBuilder.research("silverwood_staff_rod", category, -1, 5, (ItemLike)ModItems.SILVERWOOD_STAFF_ROD.get())
         .tag(Aspect.TOOL, 6)
         .tag(Aspect.TREE, 6)
         .tag(Aspect.MAGIC, 9)
         .complexity(3)
         .secondary()
         .concealed()
         .parent("silverwood_rod")
         .parentHidden("greatwood_staff_rod")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/silverwood_staff_rod")
         .finish(consumer);
      ResearchItemBuilder.research("wand_pedestal", category, -9, -6, (ItemLike)ModItems.WAND_PEDESTAL.get())
         .tag(Aspect.AURA, 6)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.ENERGY, 3)
         .complexity(2)
         .concealed()
         .parent("infusion", "nodepreserve", "node_stabilizer")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/wand_pedestal")
         .finish(consumer);
      ResearchItemBuilder.research("wand_pedestal_focus", category, -10, -7, (ItemLike)ModItems.WAND_PEDESTAL_FOCUS.get())
         .tag(Aspect.AURA, 6)
         .tag(Aspect.MAGIC, 6)
         .tag(Aspect.EXCHANGE, 6)
         .tag(Aspect.ENERGY, 3)
         .tag(Aspect.TOOL, 3)
         .complexity(3)
         .secondary()
         .concealed()
         .parent("wand_pedestal")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/wand_pedestal_focus")
         .finish(consumer);
      ResearchItemBuilder.research("vis_amulet", category, -9, -8, (ItemLike)ModItems.VIS_AMULET.get())
         .tag(Aspect.AURA, 3)
         .tag(Aspect.MAGIC, 6)
         .tag(Aspect.ENERGY, 3)
         .tag(Aspect.VOID, 3)
         .complexity(2)
         .concealed()
         .parent("wand_pedestal")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/vis_amulet")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("node_stabilizer", category, -7, -4, (ItemLike)ModItems.NODE_STABILIZER.get())
         .tag(Aspect.AURA, 4)
         .tag(Aspect.ORDER, 4)
         .tag(Aspect.ENERGY, 4)
         .complexity(1)
         .parent("nodepreserve")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/node_stabilizer")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("advanced_node_stabilizer", category, -8, -3, (ItemLike)ModItems.ADVANCED_NODE_STABILIZER.get())
         .tag(Aspect.AURA, 9)
         .tag(Aspect.MAGIC, 6)
         .tag(Aspect.ORDER, 6)
         .tag(Aspect.ENERGY, 6)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("node_stabilizer")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/advanced_node_stabilizer")
         .finish(consumer);
      ResearchItemBuilder.research("vis_power", category, -5, -6, (ItemLike)ModItems.NODE_TRANSDUCER.get())
         .tag(Aspect.AURA, 3)
         .tag(Aspect.MECHANISM, 3)
         .tag(Aspect.ENERGY, 6)
         .complexity(2)
         .parent("node_stabilizer")
         .special()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/node_transducer")
         .pageDesc(2, 3)
         .pageRecipe("arcane", "thaumcraft:arcane/vis_relay")
         .pageDesc(4, 5)
         .finish(consumer);
      ResearchItemBuilder.research("vis_charge_relay", category, -7, -6, (ItemLike)ModItems.VIS_CHARGE_RELAY.get())
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.AURA, 3)
         .tag(Aspect.MECHANISM, 3)
         .tag(Aspect.ENERGY, 6)
         .complexity(2)
         .parent("vis_power", "wand_pedestal")
         .parentHidden("greatwood_rod")
         .secondary()
         .concealed()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/vis_charge_relay")
         .finish(consumer);
      ResearchItemBuilder.research("focal_manipulator", category, -3, -8, (ItemLike)ModItems.FOCAL_MANIPULATOR.get())
         .tag(Aspect.MAGIC, 8)
         .tag(Aspect.TOOL, 8)
         .tag(Aspect.CRAFT, 5)
         .tag(Aspect.CRYSTAL, 5)
         .tag(Aspect.ENERGY, 5)
         .complexity(2)
         .parentHidden("infusion", "focus_fire")
         .parent("vis_power")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/focal_manipulator")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("vampire_bat", category, 4, -8, Thaumcraft.location("textures/foci/vampirebats.png"))
         .tag(Aspect.HUNGER, 5)
         .tag(Aspect.LIFE, 5)
         .tag(Aspect.MAGIC, 5)
         .complexity(1)
         .secondary()
         .parent("focus_hell_bat")
         .parentHidden("focal_manipulator")
         .pageText("focus.upgrade.vampirebats.text")
         .finish(consumer);
   }

   private void alchemyResearch(@NotNull Consumer<ResearchItemBuilder> consumer) {
      String category = "alchemy";
      ResearchItemBuilder.research("phial_essence", category, 0, -2, (ItemLike)ModItems.PHIAL_ESSENCE.get())
         .complexity(0)
         .stub()
         .round()
         .autoUnlock()
         .pageDesc(1)
         .pageRecipe("crafting", "thaumcraft:shaped/phial_essence")
         .finish(consumer);
      ResearchItemBuilder.research("crucible", category, 0, 0, (ItemLike)ModItems.CRUCIBLE.get())
         .complexity(0)
         .stub()
         .autoUnlock()
         .pageDesc(1, 3)
         .pageStructure(null, this.basicWand, Blocks.CAULDRON)
         .pageDesc(4)
         .pageRecipes(
            "crucible",
            "thaumcraft:crucible/balanced_shard_from_air",
            "thaumcraft:crucible/balanced_shard_from_fire",
            "thaumcraft:crucible/balanced_shard_from_water",
            "thaumcraft:crucible/balanced_shard_from_earth",
            "thaumcraft:crucible/balanced_shard_from_order",
            "thaumcraft:crucible/balanced_shard_from_entropy"
         )
         .pageDesc(5)
         .pageRecipe("smelting", "thaumcraft:salis_mundus_from_smelting")
         .finish(consumer);
      ResearchItemBuilder.research("nitor", category, 2, -1, (ItemLike)ModItems.NITOR.get())
         .tag(Aspect.LIGHT, 3)
         .tag(Aspect.FIRE, 1)
         .complexity(1)
         .parent("crucible")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/nitor")
         .finish(consumer);
      ResearchItemBuilder.research("alumentum", category, 2, 1, (ItemLike)ModItems.ALUMENTUM.get())
         .tag(Aspect.ENERGY, 3)
         .tag(Aspect.FIRE, 1)
         .complexity(1)
         .parent("crucible")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/alumentum")
         .finish(consumer);
      ResearchItemBuilder.research("alchemical_duplication", category, -4, 0, Thaumcraft.location("textures/misc/r_alchmult.png"))
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.GREED, 3)
         .tag(Aspect.CRAFT, 3)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("tallow")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/gunpowder_dupe")
         .pageRecipe("crucible", "thaumcraft:crucible/slime_ball_dupe")
         .pageRecipe("crucible", "thaumcraft:crucible/clay_ball_dupe")
         .pageRecipe("crucible", "thaumcraft:crucible/glowstone_dust_dupe")
         .pageRecipe("crucible", "thaumcraft:crucible/ink_sac_dupe")
         .finish(consumer);
      ResearchItemBuilder.research("alchemical_manufacture", category, -5, -2, Thaumcraft.location("textures/misc/r_alchman.png"))
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.CRAFT, 3)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("alchemical_duplication")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/cobweb")
         .pageRecipe("crucible", "thaumcraft:crucible/mossy_cobblestone")
         .pageRecipe("crucible", "thaumcraft:crucible/ice")
         .finish(consumer);
      ResearchItemBuilder.research("entropic_processing", category, -6, 1, Thaumcraft.location("textures/misc/r_alchent.png"))
         .tag(Aspect.MAGIC, 1)
         .tag(Aspect.ENTROPY, 3)
         .tag(Aspect.CRAFT, 1)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("alchemical_duplication")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/cracked_stone_bricks")
         .pageRecipe("crucible", "thaumcraft:crucible/bone_meal")
         .finish(consumer);
      ResearchItemBuilder.research("liquid_death", category, -7, 3, (ItemLike)ModItems.FLUID_DEATH_BUCKET.get())
         .tag(Aspect.DEATH, 3)
         .tag(Aspect.POISON, 3)
         .tag(Aspect.ENTROPY, 1)
         .tag(Aspect.WATER, 1)
         .complexity(2)
         .warp(3)
         .hidden()
         .triggers(Aspect.DEATH, Aspect.POISON)
         .parent("entropic_processing")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/bucket_death")
         .finish(consumer);
      ResearchItemBuilder.research("bottle_taint", category, -8, 1, (ItemLike)ModItems.BOTTLE_TAINT.get())
         .tag(Aspect.TAINT, 5)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.ENTROPY, 1)
         .tag(Aspect.WATER, 1)
         .complexity(2)
         .warp(2)
         .hidden()
         .triggers(Aspect.TAINT)
         .parent("entropic_processing")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/bottle_taint")
         .finish(consumer);
      ResearchItemBuilder.research("thaumium", category, -1, 3, (ItemLike)ModItems.THAUMIUM_INGOT.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(1)
         .hidden()
         .triggers(Aspect.METAL)
         .parent("crucible")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/thaumium_ingot")
         .pageRecipe("crafting", "thaumcraft:shaped/thaumium_axe")
         .pageRecipe("crafting", "thaumcraft:shaped/thaumium_sword")
         .pageRecipe("crafting", "thaumcraft:shaped/thaumium_pickaxe")
         .pageRecipe("crafting", "thaumcraft:shaped/thaumium_shovel")
         .pageRecipe("crafting", "thaumcraft:shaped/thaumium_hoe")
         .pageRecipe("crafting", "thaumcraft:shaped/thaumium_helmet")
         .pageRecipe("crafting", "thaumcraft:shaped/thaumium_chest")
         .pageRecipe("crafting", "thaumcraft:shaped/thaumium_leggings")
         .pageRecipe("crafting", "thaumcraft:shaped/thaumium_boots")
         .finish(consumer);
      ResearchItemBuilder.research("pure_iron", category, -2, 5, (ItemLike)ModItems.IRON_CLUSTER.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.ORDER, 3)
         .complexity(1)
         .concealed()
         .parent("thaumium")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/iron_cluster")
         .finish(consumer);
      ResearchItemBuilder.research("pure_gold", category, -4, 3, (ItemLike)ModItems.GOLD_CLUSTER.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.ORDER, 2)
         .tag(Aspect.GREED, 1)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("pure_iron")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/gold_cluster")
         .finish(consumer);
      ResearchItemBuilder.research("pure_copper", category, -4, 5, (ItemLike)ModItems.COPPER_CLUSTER.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.ORDER, 2)
         .tag(Aspect.EXCHANGE, 1)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("pure_iron")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/copper_cluster")
         .finish(consumer);
      ResearchItemBuilder.research("pure_tin", category, -4, 7, (ItemLike)ModItems.TIN_CLUSTER.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.ORDER, 2)
         .tag(Aspect.CRYSTAL, 1)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("pure_iron")
         .condition(new OrCondition(List.of(new ModLoadedCondition("mekanism"), new ModLoadedCondition("industrialreforged"))))
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/tin_cluster")
         .finish(consumer);
      ResearchItemBuilder.research("pure_lead", category, -2, 9, (ItemLike)ModItems.LEAD_CLUSTER.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.ORDER, 3)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("pure_iron")
         .condition(new OrCondition(List.of(new ModLoadedCondition("mekanism"), new ModLoadedCondition("industrialreforged"))))
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/lead_cluster")
         .finish(consumer);
      ResearchItemBuilder.research("pure_silver", category, 0, 9, (ItemLike)ModItems.SILVER_CLUSTER.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.ORDER, 2)
         .tag(Aspect.GREED, 1)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("pure_iron")
         .condition(new ModLoadedCondition("industrialreforged"))
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/silver_cluster")
         .finish(consumer);
      ResearchItemBuilder.research("tallow", category, -2, 0, (ItemLike)ModItems.MAGIC_TALLOW.get())
         .tag(Aspect.FLESH, 3)
         .tag(Aspect.MAGIC, 1)
         .complexity(1)
         .parent("crucible")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/magic_tallow")
         .pageRecipe("crafting", "thaumcraft:shaped/candle")
         .finish(consumer);
      ResearchItemBuilder.research("ethereal_bloom", category, -2, -3, (ItemLike)ModItems.ETHEREAL_BLOOM.get())
         .tag(Aspect.MAGIC, 1)
         .tag(Aspect.PLANT, 6)
         .tag(Aspect.HEAL, 3)
         .tag(Aspect.TAINT, 6)
         .complexity(2)
         .hidden()
         .triggers(Aspect.TAINT)
         .concealed()
         .parent("crucible")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/ethereal_bloom")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("bath_salts", category, -4, -4, (ItemLike)ModItems.BATH_SALTS.get())
         .tag(Aspect.MIND, 3)
         .tag(Aspect.AURA, 3)
         .tag(Aspect.ORDER, 3)
         .tag(Aspect.HEAL, 3)
         .complexity(2)
         .hidden()
         .triggers(Aspect.HEAL)
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/bath_salts")
         .finish(consumer);
      ResearchItemBuilder.research("sanity_soap", category, -3, -6, (ItemLike)ModItems.SANITY_SOAP.get())
         .tag(Aspect.MIND, 5)
         .tag(Aspect.ORDER, 5)
         .tag(Aspect.HEAL, 5)
         .tag(Aspect.ELDRITCH, 5)
         .complexity(1)
         .parent("bath_salts")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/sanity_soap")
         .finish(consumer);
      ResearchItemBuilder.research("arcane_spa", category, -6, -5, (ItemLike)ModItems.ARCANE_SPA.get())
         .tag(Aspect.WATER, 3)
         .tag(Aspect.MECHANISM, 3)
         .tag(Aspect.ORDER, 3)
         .complexity(1)
         .secondary()
         .parent("bath_salts")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/arcane_spa")
         .finish(consumer);
      ResearchItemBuilder.research("distilessence", category, 5, -1, (ItemLike)ModItems.ARCANE_ALEMBIC.get())
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.WATER, 3)
         .tag(Aspect.SLIME, 3)
         .complexity(1)
         .siblings("jar_label")
         .parent("nitor", "alumentum")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/alchemy_furnace")
         .pageDesc(2)
         .pageRecipe("arcane", "thaumcraft:arcane/vis_filter")
         .pageRecipe("arcane", "thaumcraft:arcane/arcane_alembic")
         .pageRecipe("arcane", "thaumcraft:arcane/alchemical_construct")
         .finish(consumer);
      ResearchItemBuilder.research("jar_label", category, 4, -3, (ItemLike)ModItems.JAR.get())
         .complexity(0)
         .parent("distilessence")
         .stub()
         .round()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/jar")
         .pageDesc(2)
         .pageRecipe("crafting", "thaumcraft:shapeless/jar_label")
         .pageDesc(3)
         .pageRecipes("crafting", this.jarLabelRecipeIds().toArray(String[]::new))
         .pageRecipe("crafting", "thaumcraft:shapeless/jar_label_null")
         .finish(consumer);
      ResearchItemBuilder.research("void_jar", category, 5, -5, (ItemLike)ModItems.VOID_JAR.get())
         .tag(Aspect.WATER, 3)
         .tag(Aspect.ENTROPY, 3)
         .tag(Aspect.VOID, 6)
         .complexity(1)
         .parent("jar_label")
         .secondary()
         .concealed()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/void_jar")
         .finish(consumer);
      ResearchItemBuilder.research("tubes", category, 7, 0, (ItemLike)ModItems.ESSENTIA_TUBE.get())
         .tag(Aspect.WATER, 3)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.EXCHANGE, 3)
         .complexity(1)
         .parent("distilessence")
         .secondary()
         .concealed()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/essence_tube")
         .pageDesc(2)
         .pageRecipe("arcane", "thaumcraft:arcane/essence_valve")
         .pageDesc(3)
         .pageRecipe("arcane", "thaumcraft:arcane/resonator")
         .pageDesc(4)
         .finish(consumer);
      ResearchItemBuilder.research("tube_filter", category, 9, 1, (ItemLike)ModItems.FILTERED_ESSENTIA_TUBE.get())
         .tag(Aspect.WATER, 3)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.ORDER, 3)
         .complexity(2)
         .parent("tubes")
         .secondary()
         .concealed()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/filtered_essence_tube")
         .pageDesc(2)
         .pageRecipe("arcane", "thaumcraft:arcane/restricted_essence_tube")
         .pageRecipe("arcane", "thaumcraft:arcane/directional_essence_tube")
         .finish(consumer);
      ResearchItemBuilder.research("essence_crystal", category, 8, -2, (ItemLike)ModItems.ESSENTIA_CRYSTALLIZER.get())
         .tag(Aspect.WATER, 5)
         .tag(Aspect.CRYSTAL, 5)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.MAGIC, 5)
         .complexity(1)
         .concealed()
         .parent("tubes")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/essence_crystallizer")
         .finish(consumer);
      ResearchItemBuilder.research("centrifuge", category, 10, 0, (ItemLike)ModItems.ALCHEMICAL_CENTRIFUGE.get())
         .tag(Aspect.ENTROPY, 3)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.CRAFT, 3)
         .complexity(2)
         .parent("tube_filter")
         .concealed()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/alchemical_centrifuge")
         .pageDesc(2, 3)
         .pageRecipe("arcane", "thaumcraft:arcane/essence_buffer")
         .finish(consumer);
      ResearchItemBuilder.research("thaumatorium", category, 10, -2, Thaumcraft.location("textures/misc/alchemy_construct.png"))
         .tag(Aspect.WATER, 3)
         .tag(Aspect.MAGIC, 6)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.CRAFT, 3)
         .complexity(3)
         .parent("centrifuge")
         .concealed()
         .pageDesc(1)
         .pageStructure(
            1,
            3,
            1,
            new AspectList().add(Aspect.FIRE, 15).add(Aspect.ORDER, 30).add(Aspect.WATER, 30),
            ModBlocks.ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.CRUCIBLE.get()
         )
         .pageDesc(2, 3)
         .pageRecipe("arcane", "thaumcraft:arcane/mnemonic_matrix")
         .finish(consumer);
   }

   private void artificeResearch(@NotNull Consumer<ResearchItemBuilder> consumer) {
      String category = "artifice";
      ResearchItemBuilder.research("basic_artiface", category, 0, 1, (ItemLike)ModItems.PRIMAL_CHARM.get())
         .complexity(0)
         .stub()
         .round()
         .autoUnlock()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/primal_charm")
         .pageRecipe("crafting", "thaumcraft:shaped/curios_amulet")
         .pageRecipe("crafting", "thaumcraft:shaped/curios_ring")
         .pageRecipe("crafting", "thaumcraft:shaped/curios_belt")
         .pageRecipe("arcane", "thaumcraft:arcane/mirror_glass")
         .finish(consumer);
      ResearchItemBuilder.research("arcane_stone", category, 5, -2, (ItemLike)ModItems.ARCANE_STONE.get())
         .complexity(0)
         .stub()
         .autoUnlock()
         .round()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/arcane_stone_block")
         .pageRecipe("crafting", "thaumcraft:shaped/arcane_stone_bricks")
         .pageRecipe("crafting", "thaumcraft:shaped/arcane_stone_stairs")
         .pageRecipe("crafting", "thaumcraft:shaped/arcane_stone_slab")
         .finish(consumer);
      ResearchItemBuilder.research("grate", category, 2, -1, (ItemLike)ModItems.GRATE.get())
         .complexity(0)
         .stub()
         .autoUnlock()
         .round()
         .pageDesc(1)
         .pageRecipe("crafting", "thaumcraft:shaped/grate")
         .finish(consumer);
      ResearchItemBuilder.research("table", category, 0, -1, (ItemLike)ModItems.TABLE.get())
         .complexity(0)
         .stub()
         .autoUnlock()
         .round()
         .pageDesc(1)
         .pageRecipe("crafting", "thaumcraft:shaped/table")
         .finish(consumer);
      ResearchItemBuilder.research("arcane_workbench", category, -1, -3, (ItemLike)ModItems.ARCANE_WORKBENCH.get())
         .complexity(0)
         .stub()
         .autoUnlock()
         .round()
         .parent("table")
         .pageDesc(1)
         .pageStructure(null, this.basicWand, ModBlocks.TABLE.get())
         .finish(consumer);
      ResearchItemBuilder.research("research_table", category, 1, -3, (ItemLike)ModBlocks.RESEARCH_TABLE.get())
         .complexity(0)
         .stub()
         .autoUnlock()
         .round()
         .parent("table")
         .pageDesc(1)
         .pageStructure(1, 2, 2, null, Blocks.AIR, ModItems.INKWELL.get(), ModBlocks.TABLE.get(), ModBlocks.TABLE.get())
         .finish(consumer);
      ResearchItemBuilder.research("thaumometer", category, 2, 1, (ItemLike)ModItems.THAUMOMETER.get())
         .stub()
         .autoUnlock()
         .round()
         .pageDesc(1)
         .pageRecipe("crafting", "thaumcraft:shaped/thaumometer")
         .finish(consumer);
      ResearchItemBuilder.research("pave_travel", category, 4, -4, (ItemLike)ModItems.TRAVEL_STONE.get())
         .tag(Aspect.TRAVEL, 3)
         .tag(Aspect.EARTH, 3)
         .tag(Aspect.FLIGHT, 3)
         .complexity(1)
         .parent("arcane_stone")
         .secondary()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/travel_stone")
         .finish(consumer);
      ResearchItemBuilder.research("pave_ward", category, 6, -4, (ItemLike)ModItems.WARDING_STONE.get())
         .tag(Aspect.MOTION, 3)
         .tag(Aspect.TRAP, 3)
         .tag(Aspect.BEAST, 3)
         .complexity(1)
         .parent("arcane_stone")
         .secondary()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/warding_stone")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("goggles", category, 4, 1, (ItemLike)ModItems.GOGGLES_REVEALING.get())
         .tag(Aspect.SENSES, 3)
         .tag(Aspect.AURA, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(1)
         .parent("thaumometer")
         .concealed()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/goggles_revealing")
         .finish(consumer);
      ResearchItemBuilder.research("arcane_ear", category, 6, 0, (ItemLike)ModItems.ARCANE_SENSOR.get())
         .tag(Aspect.SENSES, 3)
         .tag(Aspect.ENERGY, 3)
         .tag(Aspect.AIR, 3)
         .complexity(1)
         .parent("goggles")
         .concealed()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/arcane_sensor")
         .finish(consumer);
      ResearchItemBuilder.research("compass_stone", category, 6, 2, (ItemLike)ModItems.COMPASS_STONE.get())
         .tag(Aspect.SENSES, 3)
         .tag(Aspect.DARKNESS, 3)
         .tag(Aspect.ELDRITCH, 3)
         .tag(Aspect.AURA, 3)
         .complexity(1)
         .warp(2)
         .parent("goggles")
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/compass_stone")
         .finish(consumer);
      ResearchItemBuilder.research("levitator", category, -3, -3, (ItemLike)ModItems.LIFTER.get())
         .tag(Aspect.MOTION, 3)
         .tag(Aspect.FLIGHT, 3)
         .tag(Aspect.AIR, 3)
         .complexity(1)
         .concealed()
         .parent("nitor")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/lifter")
         .finish(consumer);
      ResearchItemBuilder.research("infernal_furnace", category, -4, -1, Thaumcraft.location("textures/misc/r_infernalfurnace.png"))
         .tag(Aspect.FIRE, 6)
         .tag(Aspect.METAL, 3)
         .tag(Aspect.CRAFT, 3)
         .tag(Aspect.AURA, 3)
         .complexity(2)
         .warp(2)
         .parent("nitor", "alumentum")
         .concealed()
         .pageDesc(1)
         .pageStructure(
            3,
            3,
            3,
            new AspectList().add(Aspect.FIRE, 50).add(Aspect.EARTH, 50),
            Blocks.NETHER_BRICKS,
            Blocks.OBSIDIAN,
            Blocks.NETHER_BRICKS,
            Blocks.OBSIDIAN,
            ModBlocks.HOLE.get(),
            Blocks.OBSIDIAN,
            Blocks.NETHER_BRICKS,
            Blocks.OBSIDIAN,
            Blocks.NETHER_BRICKS,
            Blocks.NETHER_BRICKS,
            Blocks.OBSIDIAN,
            Blocks.NETHER_BRICKS,
            Blocks.OBSIDIAN,
            Blocks.LAVA,
            Blocks.IRON_BARS,
            Blocks.NETHER_BRICKS,
            Blocks.OBSIDIAN,
            Blocks.NETHER_BRICKS,
            Blocks.NETHER_BRICKS,
            Blocks.OBSIDIAN,
            Blocks.NETHER_BRICKS,
            Blocks.OBSIDIAN,
            Blocks.OBSIDIAN,
            Blocks.OBSIDIAN,
            Blocks.NETHER_BRICKS,
            Blocks.OBSIDIAN,
            Blocks.NETHER_BRICKS
         )
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("bellows", category, -6, -2, (ItemLike)ModItems.ARCANE_BELLOWS.get())
         .tag(Aspect.AIR, 6)
         .tag(Aspect.MECHANISM, 3)
         .tag(Aspect.MOTION, 3)
         .complexity(1)
         .parent("infernal_furnace")
         .secondary()
         .concealed()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/arcane_bellows")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("enchfabric", category, 0, 3, (ItemLike)ModItems.CLOTH.get())
         .tag(Aspect.CLOTH, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(1)
         .secondary()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/cloth")
         .pageDesc(2)
         .pageRecipe("arcane", "thaumcraft:arcane/cloth_chest")
         .pageRecipe("arcane", "thaumcraft:arcane/cloth_leggings")
         .pageRecipe("arcane", "thaumcraft:arcane/cloth_boots")
         .finish(consumer);
      ResearchItemBuilder.research("infusion", category, -4, 5, (ItemLike)ModItems.RUNIC_MATRIX.get())
         .tag(Aspect.MAGIC, 6)
         .tag(Aspect.MECHANISM, 3)
         .tag(Aspect.CRAFT, 6)
         .complexity(2)
         .parent("distilessence")
         .concealed()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/runix_matrix")
         .pageRecipe("arcane", "thaumcraft:arcane/arcane_pedestal")
         .pageDesc(2)
         .pageStructure(
            3,
            3,
            3,
            new AspectList().add(Aspect.FIRE, 25).add(Aspect.EARTH, 25).add(Aspect.ORDER, 25).add(Aspect.AIR, 25).add(Aspect.ENTROPY, 25).add(Aspect.WATER, 25),
            ModBlocks.HOLE.get(),
            Blocks.AIR,
            ModBlocks.HOLE.get(),
            Blocks.AIR,
            ModBlocks.RUNIC_MATRIX.get(),
            Blocks.AIR,
            ModBlocks.HOLE.get(),
            Blocks.AIR,
            ModBlocks.HOLE.get(),
            ModBlocks.ARCANE_STONE.get(),
            Blocks.AIR,
            ModBlocks.ARCANE_STONE.get(),
            Blocks.AIR,
            Blocks.AIR,
            Blocks.AIR,
            ModBlocks.ARCANE_STONE.get(),
            Blocks.AIR,
            ModBlocks.ARCANE_STONE.get(),
            ModBlocks.ARCANE_BRICKS.get(),
            Blocks.AIR,
            ModBlocks.ARCANE_BRICKS.get(),
            Blocks.AIR,
            ModBlocks.ARCANE_PEDESTAL.get(),
            Blocks.AIR,
            ModBlocks.ARCANE_BRICKS.get(),
            Blocks.AIR,
            ModBlocks.ARCANE_BRICKS.get()
         )
         .pageDesc(3, 5)
         .finish(consumer);
      ResearchItemBuilder.research("symmetry_calculator", category, -3, 5, (ItemLike)ModItems.SYMMETRY_CALCULATOR.get())
         .tag(Aspect.ORDER, 5)
         .tag(Aspect.ENTROPY, 5)
         .tag(Aspect.CRAFT, 3)
         .complexity(2)
         .concealed()
         .parent("infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/symmetry_calculator")
         .finish(consumer);
      ResearchItemBuilder.research("infusion_claw", category, -6, -4, (ItemLike)ModItems.INFUSION_CLAW.get())
         .tag(Aspect.MAGIC, 5)
         .tag(Aspect.MECHANISM, 4)
         .tag(Aspect.VOID, 3)
         .tag(Aspect.CRAFT, 3)
         .complexity(3)
         .parentHidden("void_metal", "adv_alchemy_furnace")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/infusion_claw")
         .finish(consumer);
      ResearchItemBuilder.research("flux_scrubber", category, -8, -3, (ItemLike)ModItems.FLUX_SCRUBBER.get())
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.TRAP, 3)
         .tag(Aspect.AIR, 3)
         .tag(Aspect.WATER, 3)
         .complexity(1)
         .parentHidden("infusion")
         .parent("vis_power", "bellows", "tubes")
         .secondary()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/flux_scrubber")
         .finish(consumer);
      ResearchItemBuilder.research("runic_armor", category, 3, 4, (ItemLike)ModItems.RUNIC_RING.get())
         .tag(Aspect.ARMOR, 6)
         .tag(Aspect.AIR, 3)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.ENERGY, 3)
         .tag(Aspect.MIND, 3)
         .complexity(3)
         .parentHidden("infusion")
         .parent("enchfabric")
         .concealed()
         .pageDesc(1, 2)
         .pageRecipe("infusion", "thaumcraft:infusion/runic_ring")
         .pageRecipe("infusion", "thaumcraft:infusion/runic_amulet")
         .pageRecipe("infusion", "thaumcraft:infusion/runic_girdle")
         .finish(consumer);
      ResearchItemBuilder.research("runic_charged", category, 2, 3, (ItemLike)ModItems.RUNIC_RING_CHARGED.get())
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.ARMOR, 3)
         .tag(Aspect.ENERGY, 6)
         .complexity(2)
         .parent("runic_armor")
         .secondary()
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/runic_ring_charged")
         .finish(consumer);
      ResearchItemBuilder.research("runic_healing", category, 4, 3, (ItemLike)ModItems.RUNIC_RING_REGEN.get())
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.ARMOR, 3)
         .tag(Aspect.HEAL, 4)
         .tag(Aspect.WATER, 4)
         .complexity(2)
         .parent("runic_armor")
         .secondary()
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/runic_ring_regen")
         .finish(consumer);
      ResearchItemBuilder.research("runic_kinetic", category, 2, 5, (ItemLike)ModItems.RUNIC_GIRDLE_KINETIC.get())
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.ARMOR, 3)
         .tag(Aspect.AIR, 6)
         .complexity(2)
         .parent("runic_armor")
         .secondary()
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/runic_girdle_kinetic")
         .finish(consumer);
      ResearchItemBuilder.research("runic_emergency", category, 4, 5, (ItemLike)ModItems.RUNIC_AMULET_EMERGENCY.get())
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.ARMOR, 3)
         .tag(Aspect.EARTH, 4)
         .tag(Aspect.VOID, 4)
         .complexity(2)
         .parent("runic_armor")
         .secondary()
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/runic_amulet_emergency")
         .finish(consumer);
      ResearchItemBuilder.research("banners", category, 4, 8, (ItemLike)ModItems.BANNER.get(DyeColor.PURPLE).get())
         .tag(Aspect.SENSES, 3)
         .tag(Aspect.CLOTH, 3)
         .tag(Aspect.MAGIC, 1)
         .complexity(1)
         .hidden()
         .triggers(new ItemStack((ItemLike)ModItems.CRIMSON_CULT_BANNER.get()))
         .secondary()
         .pageDesc(1)
         .pageRecipes(
            "arcane",
            "thaumcraft:arcane/banner_white",
            "thaumcraft:arcane/banner_orange",
            "thaumcraft:arcane/banner_magenta",
            "thaumcraft:arcane/banner_light_blue",
            "thaumcraft:arcane/banner_yellow",
            "thaumcraft:arcane/banner_lime",
            "thaumcraft:arcane/banner_pink",
            "thaumcraft:arcane/banner_gray",
            "thaumcraft:arcane/banner_light_gray",
            "thaumcraft:arcane/banner_cyan",
            "thaumcraft:arcane/banner_purple",
            "thaumcraft:arcane/banner_blue",
            "thaumcraft:arcane/banner_brown",
            "thaumcraft:arcane/banner_green",
            "thaumcraft:arcane/banner_red",
            "thaumcraft:arcane/banner_black"
         )
         .finish(consumer);
      ResearchItemBuilder.research("runic_augmentation", category, 6, 4, Thaumcraft.location("textures/misc/r_runicupg.png"))
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.ARMOR, 3)
         .tag(Aspect.EXCHANGE, 4)
         .tag(Aspect.GREED, 4)
         .complexity(1)
         .parent("runic_armor")
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion_runic_augmentation", "thaumcraft:runic_augmentation/chest_robe_custom")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("traveller_boots", category, -1, 5, (ItemLike)ModItems.TRAVELLER_BOOTS.get())
         .tag(Aspect.TRAVEL, 3)
         .tag(Aspect.EARTH, 3)
         .tag(Aspect.FLIGHT, 3)
         .tag(Aspect.WATER, 3)
         .complexity(2)
         .parent("enchfabric", "infusion")
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/traveller_boots")
         .finish(consumer);
      ResearchItemBuilder.research("hover_harness", category, 1, 7, (ItemLike)ModItems.HOVER_HARNESS.get())
         .tag(Aspect.FLIGHT, 6)
         .tag(Aspect.TRAVEL, 6)
         .tag(Aspect.AIR, 6)
         .tag(Aspect.MECHANISM, 3)
         .complexity(3)
         .parent("traveller_boots")
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/hover_harness")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("girdle_hover", category, 2, 7, (ItemLike)ModItems.GIRDLE_HOVER.get())
         .tag(Aspect.FLIGHT, 6)
         .tag(Aspect.TRAVEL, 3)
         .tag(Aspect.AIR, 3)
         .tag(Aspect.MOTION, 6)
         .complexity(3)
         .hidden()
         .triggers(Aspect.FLIGHT)
         .parent("hover_harness")
         .secondary()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/girdle_hover")
         .finish(consumer);
      ResearchItemBuilder.research("mirror", category, -1, 8, (ItemLike)ModItems.MIRROR.get())
         .tag(Aspect.TRAVEL, 6)
         .tag(Aspect.ELDRITCH, 3)
         .tag(Aspect.DARKNESS, 3)
         .tag(Aspect.CRYSTAL, 3)
         .complexity(2)
         .hidden()
         .triggers(entityTriggerId(EntityType.ENDERMAN))
         .triggers(
            new ItemStack(Items.ENDER_PEARL), new ItemStack(Blocks.NETHER_PORTAL), new ItemStack(Blocks.END_PORTAL), new ItemStack(Blocks.END_PORTAL_FRAME)
         )
         .parent("infusion")
         .pageDesc(1, 2)
         .pageRecipe("infusion", "thaumcraft:infusion/mirror")
         .pageDesc(3)
         .finish(consumer);
      ResearchItemBuilder.research("mirror_hand", category, 1, 9, (ItemLike)ModItems.MIRROR_HAND.get())
         .tag(Aspect.TOOL, 6)
         .tag(Aspect.ELDRITCH, 3)
         .tag(Aspect.CRYSTAL, 3)
         .tag(Aspect.TRAVEL, 3)
         .complexity(2)
         .concealed()
         .secondary()
         .parent("mirror")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/mirror_hand")
         .finish(consumer);
      ResearchItemBuilder.research("mirror_essence", category, -1, 10, (ItemLike)ModItems.MIRROR_ESSENCE.get())
         .tag(Aspect.TRAVEL, 6)
         .tag(Aspect.ELDRITCH, 3)
         .tag(Aspect.WATER, 3)
         .tag(Aspect.MAGIC, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("mirror")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/mirror_essence")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("arcane_bore", category, -3, 8, (ItemLike)ModItems.ARCANE_BORE.get())
         .tag(Aspect.MINE, 6)
         .tag(Aspect.MOTION, 3)
         .tag(Aspect.MECHANISM, 3)
         .tag(Aspect.TOOL, 3)
         .complexity(2)
         .concealed()
         .parent("focus_excavation", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/arcane_bore")
         .pageDesc(2)
         .pageRecipe("arcane", "thaumcraft:arcane/arcane_bore_base")
         .pageDesc(3)
         .finish(consumer);
      ResearchItemBuilder.research("arcane_lamp", category, -3, 1, (ItemLike)ModItems.ARCANE_LAMP.get())
         .tag(Aspect.LIGHT, 3)
         .tag(Aspect.SENSES, 3)
         .tag(Aspect.DARKNESS, 3)
         .complexity(1)
         .secondary()
         .parent("nitor")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/arcane_lamp")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("lamp_growth", category, -4, 3, (ItemLike)ModItems.ARCANE_LAMP_GROWTH.get())
         .tag(Aspect.LIGHT, 3)
         .tag(Aspect.PLANT, 6)
         .tag(Aspect.LIFE, 3)
         .tag(Aspect.CROP, 3)
         .complexity(2)
         .hidden()
         .triggers(Aspect.LIGHT, Aspect.CROP)
         .parent("arcane_lamp", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/arcane_lamp_growth")
         .finish(consumer);
      ResearchItemBuilder.research("lamp_fertility", category, -2, 3, (ItemLike)ModItems.ARCANE_LAMP_FERTILITY.get())
         .tag(Aspect.BEAST, 6)
         .tag(Aspect.LIFE, 6)
         .tag(Aspect.LIGHT, 3)
         .complexity(2)
         .hidden()
         .triggers(Aspect.LIGHT, Aspect.LIFE)
         .parent("arcane_lamp", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/arcane_lamp_fertility")
         .finish(consumer);
      ResearchItemBuilder.research("bone_bow", category, -7, 1, (ItemLike)ModItems.BONE_BOW.get())
         .tag(Aspect.WEAPON, 3)
         .tag(Aspect.AIR, 3)
         .tag(Aspect.MOTION, 3)
         .complexity(1)
         .hidden()
         .triggers(new ItemStack(Items.BOW), new ItemStack(Items.BONE))
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/bone_bow")
         .finish(consumer);
      ResearchItemBuilder.research("primal_arrow", category, -9, 0, (ItemLike)ModItems.ARROW_AIR.get())
         .tag(Aspect.WEAPON, 3)
         .tag(Aspect.AIR, 3)
         .tag(Aspect.FIRE, 3)
         .tag(Aspect.WATER, 3)
         .tag(Aspect.EARTH, 3)
         .tag(Aspect.ORDER, 3)
         .tag(Aspect.ENTROPY, 3)
         .complexity(2)
         .concealed()
         .parent("bone_bow")
         .pageDesc(1)
         .pageRecipes(
            "arcane",
            "thaumcraft:arcane/air_arrow",
            "thaumcraft:arcane/fire_arrow",
            "thaumcraft:arcane/water_arrow",
            "thaumcraft:arcane/earth_arrow",
            "thaumcraft:arcane/order_arrow",
            "thaumcraft:arcane/entropy_arrow"
         )
         .pageDesc(2, 3)
         .finish(consumer);
      ResearchItemBuilder.research("elemental_axe", category, -7, 4, (ItemLike)ModItems.ELEMENTAL_AXE.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.WATER, 3)
         .tag(Aspect.MOTION, 3)
         .complexity(2)
         .parent("thaumium", "infusion")
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/elemental_axe")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("elemental_pickaxe", category, -7, 3, (ItemLike)ModItems.ELEMENTAL_PICKAXE.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.FIRE, 3)
         .tag(Aspect.SENSES, 3)
         .complexity(2)
         .parent("thaumium", "infusion")
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/elemental_pickaxe")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("elemental_sword", category, -7, 5, (ItemLike)ModItems.ELEMENTAL_SWORD.get())
         .tag(Aspect.WEAPON, 3)
         .tag(Aspect.AIR, 3)
         .tag(Aspect.ENERGY, 3)
         .complexity(2)
         .parent("thaumium", "infusion")
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/elemental_sword")
         .finish(consumer);
      ResearchItemBuilder.research("elemental_shovel", category, -7, 6, (ItemLike)ModItems.ELEMENTAL_SHOVEL.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.EARTH, 3)
         .tag(Aspect.CRAFT, 3)
         .complexity(2)
         .parent("thaumium", "infusion")
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/elemental_shovel")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("elemental_hoe", category, -7, 7, (ItemLike)ModItems.ELEMENTAL_HOE.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.LIFE, 3)
         .tag(Aspect.CROP, 3)
         .complexity(2)
         .parent("thaumium", "infusion")
         .concealed()
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/elemental_hoe")
         .finish(consumer);
      ResearchItemBuilder.research("warded_arcana", category, -5, -4, (ItemLike)ModItems.ARCANE_DOOR.get())
         .tag(Aspect.TOOL, 6)
         .tag(Aspect.MIND, 3)
         .tag(Aspect.MECHANISM, 3)
         .tag(Aspect.ARMOR, 3)
         .complexity(2)
         .parent("thaumium")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/arcane_door")
         .pageDesc(2)
         .pageRecipe("arcane", "thaumcraft:arcane/iron_key")
         .pageRecipe("arcane", "thaumcraft:arcane/gold_key")
         .pageDesc(3)
         .pageRecipe("arcane", "thaumcraft:arcane/arcane_pressure_plate")
         .pageDesc(4)
         .pageRecipe("arcane", "thaumcraft:arcane/warded_glass")
         .finish(consumer);
      ResearchItemBuilder.research("brain_jar", category, -5, 9, (ItemLike)ModItems.BRAIN_JAR.get())
         .tag(Aspect.HUNGER, 3)
         .tag(Aspect.MIND, 3)
         .tag(Aspect.UNDEAD, 3)
         .tag(Aspect.GREED, 3)
         .complexity(2)
         .warp(3)
         .parent("infusion")
         .hidden()
         .triggers(new ItemStack((ItemLike)ModItems.QUICKSILVER.get()))
         .triggers(entityTriggerId((EntityType<?>)ModEntities.BRAINY_ZOMBIE.get()), entityTriggerId((EntityType<?>)ModEntities.GIANT_BRAINY_ZOMBIE.get()))
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/brain_jar")
         .finish(consumer);
      ResearchItemBuilder.research("infusion_enchantment", category, -6, 11, Thaumcraft.location("textures/misc/r_enchant.png"))
         .tag(Aspect.MAGIC, 6)
         .tag(Aspect.MIND, 3)
         .tag(Aspect.WEAPON, 3)
         .tag(Aspect.ARMOR, 3)
         .tag(Aspect.TOOL, 3)
         .complexity(3)
         .concealed()
         .parent("brain_jar")
         .pageDesc(1, 3)
         .pageRecipe("enchant", "thaumcraft:enchant/repair")
         .pageRecipe("enchant", "thaumcraft:enchant/haste")
         .pageRecipe("enchant", "thaumcraft:enchant/0")
         .pageRecipe("enchant", "thaumcraft:enchant/1")
         .pageRecipe("enchant", "thaumcraft:enchant/2")
         .pageRecipe("enchant", "thaumcraft:enchant/3")
         .pageRecipe("enchant", "thaumcraft:enchant/4")
         .pageRecipe("enchant", "thaumcraft:enchant/5")
         .pageRecipe("enchant", "thaumcraft:enchant/6")
         .pageRecipe("enchant", "thaumcraft:enchant/7")
         .pageRecipe("enchant", "thaumcraft:enchant/8")
         .pageRecipe("enchant", "thaumcraft:enchant/9")
         .pageRecipe("enchant", "thaumcraft:enchant/10")
         .pageRecipe("enchant", "thaumcraft:enchant/11")
         .pageRecipe("enchant", "thaumcraft:enchant/12")
         .pageRecipe("enchant", "thaumcraft:enchant/13")
         .pageRecipe("enchant", "thaumcraft:enchant/14")
         .pageRecipe("enchant", "thaumcraft:enchant/15")
         .pageRecipe("enchant", "thaumcraft:enchant/16")
         .pageRecipe("enchant", "thaumcraft:enchant/17")
         .pageRecipe("enchant", "thaumcraft:enchant/18")
         .pageRecipe("enchant", "thaumcraft:enchant/19")
         .pageRecipe("enchant", "thaumcraft:enchant/20")
         .pageRecipe("enchant", "thaumcraft:enchant/21")
         .finish(consumer);
      ResearchItemBuilder.research("armor_fortress", category, -8, 9, (ItemLike)ModItems.THAUMIUM_FORTRESS_HELMET.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.ARMOR, 5)
         .tag(Aspect.CRAFT, 5)
         .complexity(2)
         .parent("thaumium", "infusion_enchantment")
         .hidden()
         .triggers(Aspect.ARMOR)
         .pageDesc(1, 2)
         .pageRecipe("infusion", "thaumcraft:infusion/thaumium_fortress_helmet")
         .pageRecipe("infusion", "thaumcraft:infusion/thaumium_fortress_chest")
         .pageRecipe("infusion", "thaumcraft:infusion/thaumium_fortress_leggings")
         .finish(consumer);
      ResearchItemBuilder.research("helm_goggles", category, -9, 7, (ItemLike)ModItems.GOGGLES_REVEALING.get())
         .tag(Aspect.SENSES, 5)
         .tag(Aspect.AURA, 3)
         .tag(Aspect.ARMOR, 3)
         .complexity(2)
         .parentHidden("goggles")
         .parent("armor_fortress")
         .concealed()
         .secondary()
         .pageDesc(1)
         .pageRecipe("infusion_fortress", "thaumcraft:fortress/helm_goggles")
         .finish(consumer);
      ResearchItemBuilder.research("mask_grinning_devil", category, -10, 8, Thaumcraft.location("textures/misc/r_mask0.png"))
         .tag(Aspect.HEAL, 5)
         .tag(Aspect.MIND, 5)
         .tag(Aspect.ARMOR, 3)
         .complexity(2)
         .parent("armor_fortress")
         .concealed()
         .secondary()
         .pageDesc(1)
         .pageRecipe("infusion_fortress", "thaumcraft:fortress/mask_grinning_devil")
         .finish(consumer);
      ResearchItemBuilder.research("mask_angry_ghost", category, -10, 9, Thaumcraft.location("textures/misc/r_mask1.png"))
         .tag(Aspect.ENTROPY, 5)
         .tag(Aspect.DEATH, 5)
         .tag(Aspect.ARMOR, 3)
         .complexity(2)
         .warp(1)
         .parent("armor_fortress")
         .concealed()
         .secondary()
         .pageDesc(1)
         .pageRecipe("infusion_fortress", "thaumcraft:fortress/mask_angry_ghost")
         .finish(consumer);
      ResearchItemBuilder.research("mask_sipping_fiend", category, -10, 10, Thaumcraft.location("textures/misc/r_mask2.png"))
         .tag(Aspect.UNDEAD, 5)
         .tag(Aspect.LIFE, 5)
         .tag(Aspect.ARMOR, 3)
         .complexity(2)
         .warp(1)
         .parent("armor_fortress")
         .concealed()
         .secondary()
         .pageDesc(1)
         .pageRecipe("infusion_fortress", "thaumcraft:fortress/mask_sipping_fiend")
         .finish(consumer);
      ResearchItemBuilder.research("update_matrix", category, -4, 11, Thaumcraft.location("textures/misc/matrixupdate.png"))
         .tag(Aspect.AURA, 64)
         .tag(Aspect.WEATHER, 16)
         .tag(Aspect.CRAFT, 15)
         .tag(Aspect.MIND, 64)
         .tag(Aspect.TOOL, 64)
         .tag(Aspect.ENERGY, 64)
         .tag(Aspect.MAGIC, 256)
         .complexity(3)
         .concealed()
         .parent("brain_jar")
         .parentHidden("thaumium")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/matrix_update")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("update_speed", category, 0, 12, (ItemLike)ModItems.SPEED_RUNE.get())
         .tag(Aspect.SENSES, 5)
         .tag(Aspect.AURA, 3)
         .tag(Aspect.ARMOR, 3)
         .complexity(2)
         .concealed()
         .parent("update_matrix")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/speed_rune")
         .pageDesc(2)
         .pageRecipe("infusion", "thaumcraft:infusion/matrix_speed")
         .finish(consumer);
      ResearchItemBuilder.research("update_efficiency", category, -2, 13, (ItemLike)ModItems.EFFICIENCY_RUNE.get())
         .tag(Aspect.SENSES, 5)
         .tag(Aspect.AURA, 3)
         .tag(Aspect.ARMOR, 3)
         .complexity(2)
         .concealed()
         .parent("update_matrix")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/efficiency_rune")
         .pageDesc(2)
         .pageRecipe("infusion", "thaumcraft:infusion/matrix_efficiency")
         .finish(consumer);
      ResearchItemBuilder.research("update_clean", category, -3, 14, (ItemLike)ModItems.CLEANER_RUNE.get())
         .tag(Aspect.SENSES, 5)
         .tag(Aspect.AURA, 3)
         .tag(Aspect.ARMOR, 3)
         .complexity(2)
         .concealed()
         .parent("update_matrix")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/cleaner_rune")
         .pageDesc(2)
         .pageRecipe("infusion", "thaumcraft:infusion/matrix_clean")
         .finish(consumer);
      ResearchItemBuilder.research("update_lucky", category, -4, 16, (ItemLike)ModItems.LUCKY_RUNE.get())
         .tag(Aspect.SENSES, 5)
         .tag(Aspect.AURA, 3)
         .tag(Aspect.ARMOR, 3)
         .complexity(2)
         .concealed()
         .parent("update_matrix")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/lucky_rune")
         .pageDesc(2)
         .pageRecipe("infusion", "thaumcraft:infusion/matrix_lucky")
         .finish(consumer);
      ResearchItemBuilder.research("update_stable", category, 0, 16, (ItemLike)ModItems.STABLE_RUNE.get())
         .tag(Aspect.SENSES, 5)
         .tag(Aspect.AURA, 3)
         .tag(Aspect.ARMOR, 3)
         .complexity(3)
         .concealed()
         .parent("update_speed", "update_efficiency", "update_clean", "update_lucky")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/stable_rune")
         .pageDesc(2)
         .pageRecipe("infusion", "thaumcraft:infusion/matrix_stable")
         .finish(consumer);
   }

   private void golemancyResearch(@NotNull Consumer<ResearchItemBuilder> consumer) {
      String category = "golemancy";
      ResearchItemBuilder.research("hungry_chest", category, -1, 0, (ItemLike)ModBlocks.HUNGRY_CHEST.get())
         .tag(Aspect.HUNGER, 3)
         .tag(Aspect.VOID, 3)
         .complexity(1)
         .secondary()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/hungry_chest")
         .finish(consumer);
      ResearchItemBuilder.research("golem_fetter", category, 4, 8, (ItemLike)ModItems.GOLEM_FETTER.get())
         .tag(Aspect.TRAP, 3)
         .tag(Aspect.MECHANISM, 3)
         .complexity(1)
         .parent("golem_stone")
         .secondary()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_fetter")
         .finish(consumer);
      ResearchItemBuilder.research("travel_trunk", category, 0, 4, (ItemLike)ModItems.TRUNK_SPAWNER.get())
         .tag(Aspect.SOUL, 3)
         .tag(Aspect.TRAVEL, 3)
         .tag(Aspect.TREE, 3)
         .tag(Aspect.VOID, 3)
         .complexity(2)
         .concealed()
         .parent("infusion", "golem_wood")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/trunk_spawner")
         .pageText("tc.research.travel_trunk.page.UAI")
         .pageText("tc.research.travel_trunk.page.UEA")
         .pageText("tc.research.travel_trunk.page.UFI")
         .pageText("tc.research.travel_trunk.page.UWA")
         .pageText("tc.research.travel_trunk.page.UOR")
         .pageText("tc.research.travel_trunk.page.UEN")
         .finish(consumer);
      ResearchItemBuilder.research("golem_straw", category, 0, 2, (ItemLike)ModItems.GOLEM_STRAW.get())
         .tag(Aspect.SOUL, 3)
         .tag(Aspect.MOTION, 3)
         .tag(Aspect.CROP, 3)
         .tag(Aspect.EXCHANGE, 3)
         .complexity(2)
         .siblings("golem_core_gather", "golem_bell")
         .parent("hungry_chest")
         .pageDesc(1, 2)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_straw")
         .pageDesc(3)
         .finish(consumer);
      ResearchItemBuilder.research("golem_wood", category, 2, 4, (ItemLike)ModItems.GOLEM_WOOD.get())
         .tag(Aspect.SOUL, 4)
         .tag(Aspect.MOTION, 4)
         .tag(Aspect.TREE, 3)
         .tag(Aspect.EXCHANGE, 3)
         .complexity(2)
         .secondary()
         .parent("golem_straw")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_wood")
         .finish(consumer);
      ResearchItemBuilder.research("golem_tallow", category, 4, 6, (ItemLike)ModItems.GOLEM_TALLOW.get())
         .tag(Aspect.SOUL, 3)
         .tag(Aspect.MOTION, 3)
         .tag(Aspect.FLESH, 3)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.EXCHANGE, 3)
         .complexity(2)
         .concealed()
         .parent("golem_clay", "tallow")
         .pageDesc(1)
         .pageRecipe("crafting", "thaumcraft:shaped/magic_tallow_block")
         .pageRecipe("crucible", "thaumcraft:crucible/golem_tallow")
         .finish(consumer);
      ResearchItemBuilder.research("golem_clay", category, 2, 6, (ItemLike)ModItems.GOLEM_CLAY.get())
         .tag(Aspect.SOUL, 6)
         .tag(Aspect.MOTION, 6)
         .tag(Aspect.EARTH, 3)
         .tag(Aspect.EXCHANGE, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("golem_wood")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_clay")
         .finish(consumer);
      ResearchItemBuilder.research("golem_flesh", category, 4, 4, (ItemLike)ModItems.GOLEM_FLESH.get())
         .tag(Aspect.SOUL, 7)
         .tag(Aspect.MOTION, 7)
         .tag(Aspect.FLESH, 6)
         .tag(Aspect.EXCHANGE, 3)
         .complexity(2)
         .warp(3)
         .concealed()
         .parent("golem_wood")
         .pageDesc(1)
         .pageRecipe("crafting", "thaumcraft:shaped/flesh_block")
         .pageRecipe("crucible", "thaumcraft:crucible/golem_flesh")
         .finish(consumer);
      ResearchItemBuilder.research("golem_stone", category, 2, 8, (ItemLike)ModItems.GOLEM_STONE.get())
         .tag(Aspect.SOUL, 6)
         .tag(Aspect.MOTION, 6)
         .tag(Aspect.EARTH, 3)
         .tag(Aspect.EXCHANGE, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("golem_clay")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_stone")
         .finish(consumer);
      ResearchItemBuilder.research("golem_iron", category, 0, 10, (ItemLike)ModItems.GOLEM_IRON.get())
         .tag(Aspect.SOUL, 9)
         .tag(Aspect.MOTION, 9)
         .tag(Aspect.METAL, 3)
         .tag(Aspect.EXCHANGE, 3)
         .complexity(2)
         .secondary()
         .concealed()
         .parent("golem_stone")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_iron")
         .finish(consumer);
      ResearchItemBuilder.research("golem_thaumium", category, 2, 10, (ItemLike)ModItems.GOLEM_THAUMIUM.get())
         .tag(Aspect.SOUL, 10)
         .tag(Aspect.MOTION, 10)
         .tag(Aspect.METAL, 3)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.EXCHANGE, 3)
         .complexity(2)
         .concealed()
         .parent("golem_iron", "thaumium")
         .pageDesc(1)
         .pageRecipe("crafting", "thaumcraft:shaped/thaumium_block")
         .pageRecipe("crucible", "thaumcraft:crucible/golem_thaumium")
         .finish(consumer);
      ResearchItemBuilder.research("golem_bell", category, 3, 0, (ItemLike)ModItems.GOLEM_BELL.get())
         .complexity(0)
         .parent("golem_straw")
         .stub()
         .pageDesc(1, 2)
         .pageRecipe("arcane", "thaumcraft:arcane/iron_bell")
         .finish(consumer);
      ResearchItemBuilder.research("golem_core_gather", category, -3, 3, (ItemLike)ModItems.GOLEM_CORE_GATHER.get())
         .complexity(1)
         .concealed()
         .parent("golem_straw")
         .stub()
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_core_blank")
         .pageDesc(2)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_core_gather")
         .finish(consumer);
      ResearchItemBuilder.research("golem_core_fill", category, -5, 3, (ItemLike)ModItems.GOLEM_CORE_FILL.get())
         .tag(Aspect.HUNGER, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.VOID, 3)
         .complexity(2)
         .concealed()
         .secondary()
         .parent("golem_core_gather")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_core_fill")
         .finish(consumer);
      ResearchItemBuilder.research("golem_core_empty", category, -5, 1, (ItemLike)ModItems.GOLEM_CORE_EMPTY.get())
         .tag(Aspect.VOID, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.GREED, 3)
         .complexity(2)
         .concealed()
         .secondary()
         .parent("golem_core_gather")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_core_empty")
         .finish(consumer);
      ResearchItemBuilder.research("core_sorting", category, -7, 2, (ItemLike)ModItems.GOLEM_CORE_SORTING.get())
         .tag(Aspect.VOID, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.GREED, 3)
         .tag(Aspect.HUNGER, 3)
         .complexity(2)
         .concealed()
         .secondary()
         .parent("golem_core_empty", "golem_core_fill", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/golem_core_sorting")
         .finish(consumer);
      ResearchItemBuilder.research("core_use", category, -7, 0, (ItemLike)ModItems.GOLEM_CORE_USE.get())
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.MECHANISM, 3)
         .tag(Aspect.MAN, 3)
         .complexity(3)
         .concealed()
         .parent("golem_core_empty", "infusion")
         .pageDesc(1, 2)
         .pageRecipe("infusion", "thaumcraft:infusion/golem_core_use")
         .pageDesc(3)
         .finish(consumer);
      ResearchItemBuilder.research("golem_core_harvest", category, -2, 5, (ItemLike)ModItems.GOLEM_CORE_HARVEST.get())
         .tag(Aspect.HARVEST, 6)
         .tag(Aspect.CROP, 3)
         .tag(Aspect.TRAVEL, 3)
         .complexity(2)
         .concealed()
         .parent("golem_core_gather")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_core_harvest")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("core_fishing", category, -2, 7, (ItemLike)ModItems.GOLEM_CORE_FISHING.get())
         .tag(Aspect.WATER, 3)
         .tag(Aspect.HARVEST, 3)
         .tag(Aspect.BEAST, 3)
         .tag(Aspect.HUNGER, 3)
         .complexity(2)
         .concealed()
         .secondary()
         .parent("golem_core_harvest", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/golem_core_fishing")
         .pageDesc(2, 5)
         .finish(consumer);
      ResearchItemBuilder.research("core_chop", category, -1, 7, (ItemLike)ModItems.GOLEM_CORE_LUMBER.get())
         .tag(Aspect.TREE, 6)
         .tag(Aspect.HARVEST, 3)
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.ENERGY, 3)
         .complexity(2)
         .concealed()
         .secondary()
         .parent("golem_core_harvest", "elemental_axe")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/golem_core_chop")
         .finish(consumer);
      ResearchItemBuilder.research("golem_core_guard", category, -4, 5, (ItemLike)ModItems.GOLEM_CORE_GUARD.get())
         .tag(Aspect.WEAPON, 3)
         .tag(Aspect.TRAP, 3)
         .tag(Aspect.SENSES, 3)
         .complexity(2)
         .concealed()
         .parent("golem_core_gather")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_core_guard")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("golem_core_butcher", category, -3, 7, (ItemLike)ModItems.GOLEM_CORE_BUTCHER.get())
         .tag(Aspect.WEAPON, 3)
         .tag(Aspect.BEAST, 3)
         .tag(Aspect.SENSES, 3)
         .tag(Aspect.HARVEST, 3)
         .complexity(2)
         .warp(1)
         .concealed()
         .secondary()
         .parent("golem_core_guard", "golem_core_harvest")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_core_butcher")
         .finish(consumer);
      ResearchItemBuilder.research("golem_core_liquid", category, -7, 4, (ItemLike)ModItems.GOLEM_CORE_LIQUID.get())
         .tag(Aspect.WATER, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.TRAVEL, 3)
         .complexity(2)
         .concealed()
         .parent("golem_core_fill")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/golem_core_liquid")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("golem_core_alchemy", category, -9, 3, (ItemLike)ModItems.GOLEM_CORE_ESSENCE.get())
         .tag(Aspect.WATER, 3)
         .tag(Aspect.TRAVEL, 3)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.ENERGY, 3)
         .complexity(2)
         .concealed()
         .secondary()
         .parent("golem_core_liquid", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/golem_core_alchemy")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("golem_upgrade_air", category, 7, -3, (ItemLike)ModItems.GOLEM_UPGRADE_AIR.get())
         .tag(Aspect.AIR, 6)
         .tag(Aspect.MOTION, 3)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("golem_bell")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_upgrade_air")
         .finish(consumer);
      ResearchItemBuilder.research("golem_upgrade_earth", category, 6, -2, (ItemLike)ModItems.GOLEM_UPGRADE_EARTH.get())
         .tag(Aspect.EARTH, 6)
         .tag(Aspect.LIFE, 3)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("golem_bell")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_upgrade_earth")
         .finish(consumer);
      ResearchItemBuilder.research("golem_upgrade_fire", category, 5, -1, (ItemLike)ModItems.GOLEM_UPGRADE_FIRE.get())
         .tag(Aspect.FIRE, 6)
         .tag(Aspect.ENERGY, 3)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("golem_bell")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_upgrade_fire")
         .finish(consumer);
      ResearchItemBuilder.research("golem_upgrade_water", category, 5, 1, (ItemLike)ModItems.GOLEM_UPGRADE_WATER.get())
         .tag(Aspect.WATER, 6)
         .tag(Aspect.SENSES, 3)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("golem_bell")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_upgrade_water")
         .finish(consumer);
      ResearchItemBuilder.research("golem_upgrade_order", category, 6, 2, (ItemLike)ModItems.GOLEM_UPGRADE_ORDER.get())
         .tag(Aspect.ORDER, 6)
         .tag(Aspect.MIND, 3)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("golem_bell")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_upgrade_order")
         .finish(consumer);
      ResearchItemBuilder.research("golem_upgrade_entropy", category, 7, 3, (ItemLike)ModItems.GOLEM_UPGRADE_ENTROPY.get())
         .tag(Aspect.ENTROPY, 6)
         .tag(Aspect.MIND, 3)
         .complexity(1)
         .concealed()
         .secondary()
         .parent("golem_bell")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_upgrade_entropy")
         .finish(consumer);
      ResearchItemBuilder.research("advanced_golem", category, 8, 0, this.advancedGolemResearchIcon())
         .tag(Aspect.LIFE, 3)
         .tag(Aspect.ENERGY, 3)
         .tag(Aspect.MIND, 6)
         .tag(Aspect.SENSES, 3)
         .complexity(2)
         .warp(5)
         .concealed()
         .parent(
            "infusion", "golem_upgrade_air", "golem_upgrade_earth", "golem_upgrade_fire", "golem_upgrade_water", "golem_upgrade_order", "golem_upgrade_entropy"
         )
         .pageDesc(1)
         .pageRecipes("infusion", this.advancedGolemRecipeIds().toArray(String[]::new))
         .finish(consumer);
      ResearchItemBuilder.research("golem_decor_top_hat", category, 5, 10, (ItemLike)ModItems.GOLEM_DECOR_TOP_HAT.get())
         .tag(Aspect.CLOTH, 2)
         .tag(Aspect.LIFE, 1)
         .tag(Aspect.GREED, 1)
         .complexity(1)
         .hidden()
         .secondary()
         .trigger(ItemTags.WOOL)
         .triggers(Aspect.CLOTH)
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_decor_top_hat")
         .finish(consumer);
      ResearchItemBuilder.research("golem_decor_googles", category, 6, 10, (ItemLike)ModItems.GOLEM_DECOR_GOGGLES.get())
         .tag(Aspect.CLOTH, 2)
         .tag(Aspect.SENSES, 1)
         .tag(Aspect.GREED, 1)
         .complexity(1)
         .hidden()
         .secondary()
         .trigger(ItemTags.WOOL)
         .triggers(Aspect.CLOTH)
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_decor_googles")
         .finish(consumer);
      ResearchItemBuilder.research("golem_decor_bowtie", category, 7, 10, (ItemLike)ModItems.GOLEM_DECOR_BOWTIE.get())
         .tag(Aspect.CLOTH, 2)
         .tag(Aspect.TRAVEL, 1)
         .tag(Aspect.GREED, 1)
         .complexity(1)
         .hidden()
         .secondary()
         .trigger(ItemTags.WOOL)
         .triggers(Aspect.CLOTH)
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_decor_bowtie")
         .finish(consumer);
      ResearchItemBuilder.research("golem_decor_fez", category, 8, 10, (ItemLike)ModItems.GOLEM_DECOR_FEZ.get())
         .tag(Aspect.CLOTH, 2)
         .tag(Aspect.ENERGY, 1)
         .tag(Aspect.GREED, 1)
         .complexity(1)
         .hidden()
         .secondary()
         .trigger(ItemTags.WOOL)
         .triggers(Aspect.CLOTH)
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_decor_fez")
         .finish(consumer);
      ResearchItemBuilder.research("golem_decor_dart", category, 5, 11, (ItemLike)ModItems.GOLEM_DECOR_DART.get())
         .tag(Aspect.FLIGHT, 1)
         .tag(Aspect.WEAPON, 2)
         .tag(Aspect.GREED, 1)
         .complexity(1)
         .hidden()
         .secondary()
         .triggers(Aspect.WEAPON)
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_decor_dart")
         .finish(consumer);
      ResearchItemBuilder.research("golem_decor_visor", category, 6, 11, (ItemLike)ModItems.GOLEM_DECOR_VISOR.get())
         .tag(Aspect.SENSES, 1)
         .tag(Aspect.ARMOR, 2)
         .tag(Aspect.GREED, 1)
         .complexity(1)
         .hidden()
         .secondary()
         .triggers(Aspect.ARMOR)
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_decor_visor")
         .finish(consumer);
      ResearchItemBuilder.research("golem_decor_armor", category, 7, 11, (ItemLike)ModItems.GOLEM_DECOR_ARMOR.get())
         .tag(Aspect.METAL, 1)
         .tag(Aspect.ARMOR, 2)
         .tag(Aspect.GREED, 1)
         .complexity(1)
         .hidden()
         .secondary()
         .triggers(Aspect.ARMOR)
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_decor_armor")
         .finish(consumer);
      ResearchItemBuilder.research("golem_decor_mace", category, 8, 11, (ItemLike)ModItems.GOLEM_DECOR_MACE.get())
         .tag(Aspect.METAL, 1)
         .tag(Aspect.WEAPON, 2)
         .tag(Aspect.GREED, 1)
         .complexity(1)
         .hidden()
         .secondary()
         .triggers(Aspect.WEAPON)
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/golem_decor_mace")
         .finish(consumer);
   }

   private void eldritchResearch(@NotNull Consumer<ResearchItemBuilder> consumer) {
      String category = "eldritch";
      ResearchItemBuilder.research("eldritch_minor", category, 1, 0, Thaumcraft.location("textures/misc/r_eldritchminor.png"))
         .hidden()
         .round()
         .special()
         .triggers(Aspect.ELDRITCH)
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/void_seed")
         .finish(consumer);
      ResearchItemBuilder.research("eldritch_major", category, -1, 0, Thaumcraft.location("textures/misc/r_eldritchmajor.png"))
         .stub()
         .hidden()
         .round()
         .special()
         .parent("eldritch_minor")
         .triggers(new ItemStack((ItemLike)ModItems.VOID_SEED.get()))
         .pageDesc(1, 2)
         .finish(consumer);
      ResearchItemBuilder.research("oculus", category, -2, 2, (ItemLike)ModItems.ELDRITCH_EYE.get())
         .tag(Aspect.MIND, 3)
         .tag(Aspect.DARKNESS, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.TRAVEL, 6)
         .tag(Aspect.ELDRITCH, 6)
         .complexity(1)
         .warp(6)
         .round()
         .concealed()
         .parent("crimson", "eldritch_major")
         .special()
         .pageDesc(1)
         .pageStructure(
            1,
            3,
            1,
            null,
            ModBlocks.AURA_NODE.get(),
            ModItems.ELDRITCH_EYE.get(),
            ModBlocks.ELDRITCH_ALTAR.get()
         )
         .pageRecipe("infusion", "thaumcraft:infusion/eldritch_eye")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("ender_outer", category, -3, 4, Thaumcraft.location("textures/misc/r_outer.png"))
         .complexity(1)
         .stub()
         .hidden()
         .round()
         .parent("oculus")
         .pageDesc(1)
         .finish(consumer);
      ResearchItemBuilder.research("outerrev", category, -5, 3, Thaumcraft.location("textures/misc/r_outerrev.png"))
         .tag(Aspect.ELDRITCH, 4)
         .tag(Aspect.MIND, 4)
         .complexity(1)
         .lost()
         .secondary()
         .special()
         .parent("ender_outer")
         .triggers(new ItemStack((ItemLike)ModItems.GLYPHED_STONE.get()), new ItemStack((ItemLike)ModItems.RUNED_STONE.get()))
         .pageDesc(1)
         .finish(consumer);
      ResearchItemBuilder.research("prim_pearl", category, 0, 4, (ItemLike)ModItems.PRIMORDIAL_PEARL.get())
         .tag(Aspect.AIR, 8)
         .tag(Aspect.EARTH, 8)
         .tag(Aspect.FIRE, 8)
         .tag(Aspect.WATER, 8)
         .tag(Aspect.ORDER, 8)
         .tag(Aspect.ENTROPY, 8)
         .complexity(1)
         .lost()
         .secondary()
         .special()
         .parent("eldritch_minor")
         .triggers(new ItemStack((ItemLike)ModItems.PRIMORDIAL_PEARL.get()))
         .pageDesc(1, 2)
         .finish(consumer);
      ResearchItemBuilder.research("prim_node", category, 0, 6, Thaumcraft.location("textures/misc/r_nodes_2.png"))
         .tag(Aspect.AURA, 1)
         .tag(Aspect.MAGIC, 1)
         .tag(Aspect.ORDER, 1)
         .tag(Aspect.ENTROPY, 1)
         .complexity(1)
         .warp(1)
         .secondary()
         .parent("prim_pearl")
         .pageDesc(1)
         .finish(consumer);
      ResearchItemBuilder.research("adv_alchemy_furnace", category, -2, 6, (ItemLike)ModItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get())
         .tag(Aspect.AURA, 1)
         .tag(Aspect.MAGIC, 1)
         .tag(Aspect.ORDER, 1)
         .tag(Aspect.ENTROPY, 1)
         .complexity(1)
         .secondary()
         .parent("prim_pearl", "distilessence", "vis_power")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/advanced_alchemical_construct")
         .pageDesc(2)
         .pageStructure(
            3,
            2,
            3,
            new AspectList().add(Aspect.FIRE, 50).add(Aspect.WATER, 50).add(Aspect.ORDER, 50),
            ModBlocks.ARCANE_ALEMBIC.get(),
            ModBlocks.ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.ARCANE_ALEMBIC.get(),
            ModBlocks.ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.HOLE.get(),
            ModBlocks.ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.ARCANE_ALEMBIC.get(),
            ModBlocks.ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.ARCANE_ALEMBIC.get(),
            ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.ALCHEMY_FURNACE.get(),
            ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get(),
            ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get()
         )
         .finish(consumer);
      ResearchItemBuilder.research("primal_crusher", category, 2, 5, (ItemLike)ModItems.PRIMAL_CRUSHER.get())
         .tag(Aspect.MINE, 6)
         .tag(Aspect.TOOL, 6)
         .tag(Aspect.ENTROPY, 6)
         .tag(Aspect.VOID, 6)
         .tag(Aspect.WEAPON, 6)
         .tag(Aspect.ELDRITCH, 6)
         .tag(Aspect.GREED, 6)
         .complexity(2)
         .concealed()
         .parent("prim_pearl")
         .parentHidden("void_metal", "elemental_pickaxe", "elemental_shovel")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/primal_crusher")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("sanity_checker", category, 2, 2, (ItemLike)ModItems.SANITY_CHECKER.get())
         .tag(Aspect.MIND, 5)
         .tag(Aspect.ELDRITCH, 3)
         .tag(Aspect.SENSES, 5)
         .complexity(1)
         .parent("eldritch_minor")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/sanity_checker")
         .finish(consumer);
      ResearchItemBuilder.research("void_metal", category, 2, -2, (ItemLike)ModItems.VOID_METAL_INGOT.get())
         .tag(Aspect.METAL, 3)
         .tag(Aspect.ELDRITCH, 3)
         .tag(Aspect.DARKNESS, 3)
         .tag(Aspect.VOID, 5)
         .complexity(2)
         .parent("thaumium", "eldritch_minor")
         .pageDesc(1)
         .pageRecipe("crucible", "thaumcraft:crucible/void_metal_ingot")
         .pageDesc(2)
         .pageRecipe("crafting", "thaumcraft:shaped/void_axe")
         .pageRecipe("crafting", "thaumcraft:shaped/void_sword")
         .pageRecipe("crafting", "thaumcraft:shaped/void_pickaxe")
         .pageRecipe("crafting", "thaumcraft:shaped/void_shovel")
         .pageRecipe("crafting", "thaumcraft:shaped/void_hoe")
         .pageRecipe("crafting", "thaumcraft:shaped/void_helmet")
         .pageRecipe("crafting", "thaumcraft:shaped/void_chest")
         .pageRecipe("crafting", "thaumcraft:shaped/void_leggings")
         .pageRecipe("crafting", "thaumcraft:shaped/void_boots")
         .finish(consumer);
      ResearchItemBuilder.research("essence_reservoir", category, 4, -3, (ItemLike)ModItems.ESSENCE_RESERVOIR.get())
         .tag(Aspect.WATER, 5)
         .tag(Aspect.VOID, 3)
         .tag(Aspect.EXCHANGE, 3)
         .tag(Aspect.MAGIC, 5)
         .tag(Aspect.VOID, 5)
         .complexity(2)
         .parent("void_metal", "centrifuge", "infusion")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/essence_reservoir")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("void_cap", category, 5, -1, (ItemLike)ModItems.VOID_WAND_CAP.get())
         .tag(Aspect.VOID, 5)
         .tag(Aspect.ELDRITCH, 5)
         .tag(Aspect.TOOL, 3)
         .tag(Aspect.MAGIC, 3)
         .tag(Aspect.AURA, 3)
         .complexity(3)
         .warp(1)
         .concealed()
         .parent("thaumium_cap", "void_metal")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/void_inert_wand_cap")
         .pageRecipe("infusion", "thaumcraft:infusion/void_wand_cap")
         .finish(consumer);
      ResearchItemBuilder.research("armor_void_fortress", category, 0, -3, (ItemLike)ModItems.VOID_ROBE_HELMET.get())
         .tag(Aspect.ARMOR, 5)
         .tag(Aspect.ELDRITCH, 3)
         .tag(Aspect.CLOTH, 3)
         .tag(Aspect.DARKNESS, 3)
         .tag(Aspect.VOID, 5)
         .complexity(3)
         .concealed()
         .secondary()
         .parent("void_metal", "enchfabric", "eldritch_major")
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/void_robe_helmet")
         .pageRecipe("infusion", "thaumcraft:infusion/void_robe_chest")
         .pageRecipe("infusion", "thaumcraft:infusion/void_robe_leggings")
         .finish(consumer);
      ResearchItemBuilder.research("focus_primal", category, 4, 1, (ItemLike)ModItems.FOCUS_PRIMAL.get())
         .tag(Aspect.AIR, 6)
         .tag(Aspect.WATER, 6)
         .tag(Aspect.FIRE, 6)
         .tag(Aspect.EARTH, 6)
         .tag(Aspect.ORDER, 6)
         .tag(Aspect.ENTROPY, 6)
         .tag(Aspect.MAGIC, 6)
         .complexity(2)
         .warp(2)
         .concealed()
         .parent("eldritch_minor")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/focus_primal")
         .finish(consumer);
      ResearchItemBuilder.research("primal_staff_rod", category, 6, 2, (ItemLike)ModItems.PRIMAL_STAFF_ROD.get())
         .tag(Aspect.AIR, 9)
         .tag(Aspect.EARTH, 9)
         .tag(Aspect.FIRE, 9)
         .tag(Aspect.WATER, 9)
         .tag(Aspect.ORDER, 9)
         .tag(Aspect.ENTROPY, 9)
         .tag(Aspect.TOOL, 9)
         .tag(Aspect.MAGIC, 12)
         .complexity(3)
         .warp(3)
         .hidden()
         .parent("focus_primal")
         .parentHidden(
            "silverwood_staff_rod",
            "bone_staff_rod",
            "greatwood_staff_rod",
            "blaze_staff_rod",
            "reed_staff_rod",
            "obsidian_staff_rod",
            "quartz_staff_rod",
            "ice_staff_rod"
         )
         .triggers(entityTriggerId((EntityType<?>)ModEntities.PRIMAL_ORB.get()))
         .triggers(new ItemStack((ItemLike)ModItems.FOCUS_PRIMAL.get()))
         .pageDesc(1)
         .pageRecipe("infusion", "thaumcraft:infusion/primal_staff_rod")
         .finish(consumer);
      ResearchItemBuilder.research("distorted_fragment", category, 4, 4, (ItemLike)ModItems.DISTORTED_FRAGMENT.get())
         .tag(Aspect.MAGIC, 5)
         .tag(Aspect.TAINT, 3)
         .tag(Aspect.DARKNESS, 5)
         .concealed()
         .parent("sanity_checker")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/distorted_fragment")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("clean_fragment", category, 4, 6, (ItemLike)ModItems.CLEAN_FRAGMENT.get())
         .tag(Aspect.MAGIC, 5)
         .tag(Aspect.AURA, 3)
         .tag(Aspect.CRYSTAL, 5)
         .concealed()
         .parent("distorted_fragment")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/clean_fragment")
         .pageDesc(2)
         .finish(consumer);
      ResearchItemBuilder.research("volume_of_purification", category, 2, 8, (ItemLike)ModItems.VOLUME_OF_PURIFICATION.get())
         .tag(Aspect.AURA, 5)
         .tag(Aspect.TAINT, 3)
         .tag(Aspect.POISON, 5)
         .concealed()
         .parent("clean_fragment")
         .pageDesc(1)
         .pageRecipe("arcane", "thaumcraft:arcane/volume_of_purification")
         .pageDesc(2)
         .finish(consumer);
   }

   @NotNull
   public CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
      Set<ResourceLocation> set = Sets.newHashSet();
      List<CompletableFuture<?>> list = new ArrayList<>();
      this.buildResearch(p_253413_ -> {
         ResourceLocation location = Thaumcraft.location(p_253413_.category + "/" + p_253413_.key);
         if (!set.add(location)) {
            throw new IllegalStateException("Duplicate recipe " + location);
         } else {
            list.add(DataProvider.saveStable(cachedOutput, p_253413_.toJson(), this.researchPathProvider.json(location)));
         }
      });
      return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
   }

   private Ingredient advancedGolemResearchIcon() {
      List<Ingredient> ingredients = new ArrayList<>();

      for (ItemLike itemLike : this.basicGolems()) {
         ingredients.add(DataComponentIngredient.of(true, this.advancedGolemStack(itemLike)));
      }

      return CompoundIngredient.of(ingredients.toArray(Ingredient[]::new));
   }

   private List<String> advancedGolemRecipeIds() {
      List<String> ids = new ArrayList<>();

      for (ItemLike itemLike : this.basicGolems()) {
         ids.add(Thaumcraft.location("infusion/advanced_golem_" + this.golemIdSuffix(itemLike)).toString());
      }

      return ids;
   }

   private List<String> jarLabelRecipeIds() {
      List<String> ids = new ArrayList<>();

      for (Aspect aspect : this.sortedAspects()) {
         ids.add(Thaumcraft.location("shapeless/jar_label_" + aspect.getTag()).toString());
      }

      return ids;
   }

   private ItemStack advancedGolemStack(ItemLike itemLike) {
      ItemStack stack = new ItemStack(itemLike);
      ItemStackData.getOrCreateTag(stack).putByte("advanced", (byte)1);
      return stack;
   }

   private List<ItemLike> basicGolems() {
      return List.of(
         (ItemLike)ModItems.GOLEM_STRAW.get(),
         (ItemLike)ModItems.GOLEM_WOOD.get(),
         (ItemLike)ModItems.GOLEM_TALLOW.get(),
         (ItemLike)ModItems.GOLEM_CLAY.get(),
         (ItemLike)ModItems.GOLEM_FLESH.get(),
         (ItemLike)ModItems.GOLEM_STONE.get(),
         (ItemLike)ModItems.GOLEM_IRON.get(),
         (ItemLike)ModItems.GOLEM_THAUMIUM.get()
      );
   }

   private String golemIdSuffix(ItemLike itemLike) {
      return BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath().replaceFirst("^golem_", "");
   }

   private List<Aspect> sortedAspects() {
      List<Aspect> aspects = new ArrayList<>(Aspect.aspects.values());
      aspects.removeIf(aspect -> aspect == null);
      aspects.sort(Comparator.comparing(Aspect::getTag));
      return aspects;
   }

   @NotNull
   public String getName() {
      return "Researches";
   }
}
