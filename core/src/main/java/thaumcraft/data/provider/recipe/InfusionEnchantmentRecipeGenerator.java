package thaumcraft.data.provider.recipe;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.data.builder.InfusionEnchantmentRecipeBuilder;
import thaumcraft.init.registry.ModEnchantment;
import thaumcraft.init.registry.ModItems;

public class InfusionEnchantmentRecipeGenerator extends RecipeProvider {
   public InfusionEnchantmentRecipeGenerator(PackOutput output, CompletableFuture<Provider> registries) {
      super(output, registries);
   }

   public void generate(@NotNull RecipeOutput consumer) {
      this.buildRecipes(consumer);
   }

   protected void buildRecipes(@NotNull RecipeOutput consumer) {
      this.buildVanillaEnchantRecipes(consumer);
      this.buildTTEnchantRecipes(consumer);
   }

   private void buildVanillaEnchantRecipes(@NotNull RecipeOutput consumer) {
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", ModEnchantment.REPAIR.getKey())
         .aspect(Aspect.MAGIC, 8)
         .aspect(Aspect.CRAFT, 10)
         .aspect(Aspect.ORDER, 10)
         .instability(4)
         .requires(Blocks.ANVIL)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/repair"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", ModEnchantment.HASTE.getKey())
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.TRAVEL, 8)
         .aspect(Aspect.FLIGHT, 8)
         .instability(3)
         .requires((ItemLike)ModItems.NITOR.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/haste"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.PROTECTION)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.ARMOR, 8)
         .instability(1)
         .requires(Items.IRON_INGOT)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/0"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.FIRE_PROTECTION)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.ARMOR, 4)
         .aspect(Aspect.FIRE, 4)
         .instability(1)
         .requires(Items.IRON_INGOT)
         .requires(Items.MAGMA_CREAM)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/1"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.BLAST_PROTECTION)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.ARMOR, 4)
         .aspect(Aspect.ENTROPY, 4)
         .instability(1)
         .requires(Items.IRON_INGOT)
         .requires(Items.GUNPOWDER)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/2"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.PROJECTILE_PROTECTION)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.ARMOR, 4)
         .aspect(Aspect.FLIGHT, 4)
         .instability(1)
         .requires(Items.IRON_INGOT)
         .requires(Items.ARROW)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/3"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.FEATHER_FALLING)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.AIR, 4)
         .aspect(Aspect.FLIGHT, 4)
         .instability(1)
         .requires(Items.FEATHER)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/4"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.RESPIRATION)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.AIR, 8)
         .aspect(Aspect.WATER, 8)
         .instability(2)
         .requires(Items.SUGAR_CANE)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/5"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.AQUA_AFFINITY)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.MOTION, 8)
         .aspect(Aspect.WATER, 8)
         .instability(2)
         .requires(Items.SUGAR_CANE)
         .requires(Items.SLIME_BALL)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/6"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.THORNS)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.WEAPON, 8)
         .aspect(Aspect.PLANT, 8)
         .instability(2)
         .requires(Blocks.DEAD_BUSH)
         .requires(Items.QUARTZ)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/7"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.SHARPNESS)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.WEAPON, 8)
         .instability(2)
         .requires(Items.IRON_SWORD)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/8"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.SMITE)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.WEAPON, 4)
         .aspect(Aspect.UNDEAD, 4)
         .instability(2)
         .requires(Items.IRON_SWORD)
         .requires(Items.GLOWSTONE_DUST)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/9"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.BANE_OF_ARTHROPODS)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.WEAPON, 4)
         .aspect(Aspect.BEAST, 4)
         .instability(2)
         .requires(Items.IRON_SWORD)
         .requires(Items.FERMENTED_SPIDER_EYE)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/10"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.KNOCKBACK)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.WEAPON, 3)
         .aspect(Aspect.MOTION, 3)
         .instability(1)
         .requires(Blocks.PISTON)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/11"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.FIRE_ASPECT)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.WEAPON, 4)
         .aspect(Aspect.FIRE, 8)
         .instability(3)
         .requires(Items.IRON_SWORD)
         .requires(Items.BLAZE_POWDER)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/12"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.LOOTING)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.WEAPON, 4)
         .aspect(Aspect.GREED, 8)
         .instability(3)
         .requires(Items.IRON_SWORD)
         .requires(Items.DIAMOND)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/13"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.EFFICIENCY)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.TOOL, 4)
         .aspect(Aspect.ORDER, 4)
         .instability(2)
         .requires(Items.IRON_PICKAXE)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/14"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.SILK_TOUCH)
         .aspect(Aspect.MAGIC, 16)
         .aspect(Aspect.TOOL, 16)
         .aspect(Aspect.ORDER, 16)
         .aspect(Aspect.HARVEST, 16)
         .aspect(Aspect.MINE, 16)
         .instability(5)
         .requires(Items.IRON_PICKAXE)
         .requires(Blocks.COBWEB)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/15"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.UNBREAKING)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.TOOL, 4)
         .aspect(Aspect.ORDER, 8)
         .instability(2)
         .requires(Items.IRON_PICKAXE)
         .requires(Blocks.OBSIDIAN)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/16"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.FORTUNE)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.TOOL, 4)
         .aspect(Aspect.GREED, 8)
         .instability(3)
         .requires(Items.IRON_PICKAXE)
         .requires(Items.DIAMOND)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/17"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.POWER)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.WEAPON, 8)
         .instability(2)
         .requires(Items.BOW)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/18"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.PUNCH)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.WEAPON, 3)
         .aspect(Aspect.MOTION, 3)
         .instability(2)
         .requires(Blocks.PISTON)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/19"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.FLAME)
         .aspect(Aspect.MAGIC, 4)
         .aspect(Aspect.WEAPON, 4)
         .aspect(Aspect.FIRE, 8)
         .instability(3)
         .requires(Items.BOW)
         .requires(Items.BLAZE_POWDER)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/20"));
      InfusionEnchantmentRecipeBuilder.enchant("infusion_enchantment", Enchantments.INFINITY)
         .aspect(Aspect.MAGIC, 8)
         .aspect(Aspect.WEAPON, 16)
         .aspect(Aspect.VOID, 16)
         .aspect(Aspect.EXCHANGE, 16)
         .instability(5)
         .requires(Items.BOW)
         .requires(Items.ARROW)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/21"));
   }

   private void buildTTEnchantRecipes(@NotNull RecipeOutput consumer) {
      InfusionEnchantmentRecipeBuilder.enchant("ttench_ascent_boost", ModEnchantment.ASCENT_BOOST.getKey())
         .aspect(Aspect.AIR, 8)
         .aspect(Aspect.MOTION, 8)
         .aspect(Aspect.MAGIC, 4)
         .instability(2)
         .requires(Items.FEATHER)
         .requires((ItemLike)ModItems.AIR_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_ascent_boost"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_slow_fall", ModEnchantment.SLOW_FALL.getKey())
         .aspect(Aspect.AIR, 8)
         .aspect(Aspect.MOTION, 6)
         .aspect(Aspect.MAGIC, 4)
         .instability(2)
         .requires(Items.FEATHER)
         .requires(Items.FEATHER)
         .requires((ItemLike)ModItems.AIR_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_slow_fall"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_auto_smelt", ModEnchantment.AUTO_SMELT.getKey())
         .aspect(Aspect.FIRE, 12)
         .aspect(Aspect.ENTROPY, 8)
         .aspect(Aspect.MAGIC, 4)
         .instability(3)
         .requires(Items.BLAZE_POWDER)
         .requires(Items.BLAZE_POWDER)
         .requires((ItemLike)ModItems.FIRE_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_auto_smelt"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_desintegrate", ModEnchantment.DESINTEGRATE.getKey())
         .aspect(Aspect.ENTROPY, 10)
         .aspect(Aspect.VOID, 8)
         .aspect(Aspect.MAGIC, 4)
         .instability(3)
         .requires(Blocks.TNT)
         .requires((ItemLike)ModItems.ENTROPY_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_desintegrate"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_quick_draw", ModEnchantment.QUICK_DRAW.getKey())
         .aspect(Aspect.SENSES, 8)
         .aspect(Aspect.WEAPON, 6)
         .aspect(Aspect.MAGIC, 4)
         .instability(2)
         .requires(Items.ARROW)
         .requires(Items.ARROW)
         .requires((ItemLike)ModItems.ORDER_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_quick_draw"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_vampirism", ModEnchantment.VAMPIRISM.getKey())
         .aspect(Aspect.HUNGER, 8)
         .aspect(Aspect.WEAPON, 6)
         .aspect(Aspect.MAGIC, 4)
         .instability(3)
         .requires(Items.GOLDEN_APPLE)
         .requires((ItemLike)ModItems.ZOMBIE_BRAIN.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_vampirism"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_focused", ModEnchantment.FOCUSED_STRIKES.getKey())
         .aspect(Aspect.ORDER, 8)
         .aspect(Aspect.WEAPON, 8)
         .aspect(Aspect.MAGIC, 4)
         .instability(3)
         .requires(Items.DIAMOND)
         .requires((ItemLike)ModItems.ORDER_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_focused"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_dispersed", ModEnchantment.DISPERSED_STRIKES.getKey())
         .aspect(Aspect.ENTROPY, 8)
         .aspect(Aspect.WEAPON, 8)
         .aspect(Aspect.MAGIC, 4)
         .instability(3)
         .requires(Items.DIAMOND)
         .requires((ItemLike)ModItems.ENTROPY_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_dispersed"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_final", ModEnchantment.FINAL_STRIKE.getKey())
         .aspect(Aspect.ENTROPY, 12)
         .aspect(Aspect.ORDER, 12)
         .aspect(Aspect.WEAPON, 10)
         .aspect(Aspect.MAGIC, 6)
         .instability(5)
         .requires(Items.DIAMOND)
         .requires(Items.NETHER_STAR)
         .requires((ItemLike)ModItems.ENTROPY_SHARD.get())
         .requires((ItemLike)ModItems.ORDER_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_final"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_pounce", ModEnchantment.POUNCE.getKey())
         .aspect(Aspect.AIR, 10)
         .aspect(Aspect.ORDER, 8)
         .aspect(Aspect.ARMOR, 6)
         .aspect(Aspect.MAGIC, 4)
         .instability(4)
         .requires(Items.FEATHER)
         .requires(Items.DIAMOND)
         .requires((ItemLike)ModItems.AIR_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_pounce"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_shatter", ModEnchantment.SHATTER.getKey())
         .aspect(Aspect.EARTH, 10)
         .aspect(Aspect.ENTROPY, 10)
         .aspect(Aspect.TOOL, 6)
         .aspect(Aspect.MAGIC, 4)
         .instability(4)
         .requires(Blocks.OBSIDIAN)
         .requires(Items.DIAMOND)
         .requires((ItemLike)ModItems.EARTH_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_shatter"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_shockwave", ModEnchantment.SHOCKWAVE.getKey())
         .aspect(Aspect.AIR, 10)
         .aspect(Aspect.ENTROPY, 10)
         .aspect(Aspect.ARMOR, 6)
         .aspect(Aspect.MAGIC, 4)
         .instability(4)
         .requires(Blocks.PISTON)
         .requires(Items.DIAMOND)
         .requires((ItemLike)ModItems.AIR_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_shockwave"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_tunnel", ModEnchantment.TUNNEL.getKey())
         .aspect(Aspect.EARTH, 10)
         .aspect(Aspect.ORDER, 10)
         .aspect(Aspect.TOOL, 6)
         .aspect(Aspect.MAGIC, 4)
         .instability(4)
         .requires(Items.DIAMOND)
         .requires(Items.IRON_PICKAXE)
         .requires((ItemLike)ModItems.ORDER_SHARD.get())
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_tunnel"));
      InfusionEnchantmentRecipeBuilder.enchant("ttench_valiance", ModEnchantment.VALIANCE.getKey())
         .aspect(Aspect.WEAPON, 10)
         .aspect(Aspect.HEAL, 8)
         .aspect(Aspect.MAGIC, 4)
         .instability(3)
         .requires(Items.GOLDEN_APPLE)
         .requires(Items.DIAMOND)
         .requires((ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("enchant/ttench_valiance"));
   }
}
