package thaumcraft.init.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
   public static final TagKey<Block> ORES_SHARD = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/shard"));
   public static final TagKey<Block> ORES_AMBER = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/amber"));
   public static final TagKey<Block> ORES_CINNABAR = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/cinnabar"));
   public static final TagKey<Block> OBSIDIAN_TOTEMS = BlockTags.create(ResourceLocation.fromNamespaceAndPath("thaumcraft", "obsidian_totems"));
   public static final TagKey<Block> TAINT_MATERIAL = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("thaumcraft", "taint_material"));
}
