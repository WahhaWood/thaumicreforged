package thaumcraft.init.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import thaumcraft.Thaumcraft;

public class ModItemTags {
   public static final TagKey<Item> SHARDS = ItemTags.create(Thaumcraft.location("shards"));
   public static final TagKey<Item> SHARDS_BASICS = ItemTags.create(Thaumcraft.location("shards/basics"));
   public static final TagKey<Item> ORES_SHARD = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/shard"));
   public static final TagKey<Item> ORES_AMBER = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/amber"));
   public static final TagKey<Item> ORES_CINNABAR = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/cinnabar"));
   public static final TagKey<Item> GOLEMS = ItemTags.create(Thaumcraft.location("golems"));
   public static final TagKey<Item> ADVANCED_GOLEMS = ItemTags.create(Thaumcraft.location("advanced_golems"));
   public static final TagKey<Item> CANDLES = ItemTags.create(Thaumcraft.location("candles"));
   public static final TagKey<Item> THAUMIUM_INGOT = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/thaumium"));
   public static final TagKey<Item> THAUMIUM_NUGGET = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "nuggets/thaumium"));
   public static final TagKey<Item> VOID_INGOT = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/void"));
   public static final TagKey<Item> VOID_NUGGET = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "nuggets/void"));
   public static final TagKey<Item> ICHORIUM_INGOT = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/ichorium"));
   public static final TagKey<Item> ICHORIUM_NUGGET = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "nuggets/ichorium"));
   public static final TagKey<Item> SHARD_AIR = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/air_shard"));
   public static final TagKey<Item> SHARD_FIRE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/fire_shard"));
   public static final TagKey<Item> SHARD_WATER = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/water_shard"));
   public static final TagKey<Item> SHARD_EARTH = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/earth_shard"));
   public static final TagKey<Item> SHARD_ORDER = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/order_shard"));
   public static final TagKey<Item> SHARD_ENTROPY = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/entropy_shard"));
   public static final TagKey<Item> SHARD_BALANCED = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/balanced_shard"));
}
