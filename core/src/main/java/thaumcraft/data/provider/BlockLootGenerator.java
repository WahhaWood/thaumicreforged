package thaumcraft.data.provider;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;

public class BlockLootGenerator extends BlockLootSubProvider {
   private final Set<Block> knownBlocks = new LinkedHashSet<>();

   public BlockLootGenerator(Provider registries) {
      super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registries);
   }

   protected void generate() {
      for (Item item : BuiltInRegistries.ITEM) {
         if (item instanceof BlockItem blockItem
            && "thaumcraft".equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())
            && BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).getNamespace().equals("thaumcraft")) {
            Block block = blockItem.getBlock();
            if (block.getLootTable() != BuiltInLootTables.EMPTY
               && block != ModBlocks.GREATWOOD_LEAVES.get()
               && block != ModBlocks.SILVERWOOD_LEAVES.get()
               && block != ModBlocks.CHARGED_OBSIDIAN_TOTEM.get()
               && !isOreWithItemDrop(block)) {
               if (block instanceof SlabBlock) {
                  this.add(block, this.createSlabItemTable(block));
               } else if (block instanceof DoorBlock) {
                  this.add(block, this.createDoorTable(block));
               } else {
                  this.dropOther(block, item);
               }
            }
         }
      }

      this.add(
         (Block)ModBlocks.GREATWOOD_LEAVES.get(),
         this.createLeavesDrops((Block)ModBlocks.GREATWOOD_LEAVES.get(), (Block)ModBlocks.GREATWOOD_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES)
      );
      this.add(
         (Block)ModBlocks.SILVERWOOD_LEAVES.get(),
         this.createLeavesDrops((Block)ModBlocks.SILVERWOOD_LEAVES.get(), (Block)ModBlocks.SILVERWOOD_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES)
      );
      // TC4 damageDropped(8) = 1: the node totem drops a plain totem (the node's essence comes from the block itself).
      this.dropOther(ModBlocks.CHARGED_OBSIDIAN_TOTEM.get(), ModItems.OBSIDIAN_TOTEM.get());
      this.addOreDrop((Block)ModBlocks.AMBER_ORE.get(), (Item)ModItems.AMBER.get());
      this.addOreDrop((Block)ModBlocks.AIR_SHARD_ORE.get(), (Item)ModItems.AIR_SHARD.get());
      this.addOreDrop((Block)ModBlocks.FIRE_SHARD_ORE.get(), (Item)ModItems.FIRE_SHARD.get());
      this.addOreDrop((Block)ModBlocks.WATER_SHARD_ORE.get(), (Item)ModItems.WATER_SHARD.get());
      this.addOreDrop((Block)ModBlocks.EARTH_SHARD_ORE.get(), (Item)ModItems.EARTH_SHARD.get());
      this.addOreDrop((Block)ModBlocks.ORDER_SHARD_ORE.get(), (Item)ModItems.ORDER_SHARD.get());
      this.addOreDrop((Block)ModBlocks.ENTROPY_SHARD_ORE.get(), (Item)ModItems.ENTROPY_SHARD.get());
   }

   private void addOreDrop(Block ore, Item drop) {
      Reference<Enchantment> fortune = this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
      this.add(
         ore,
         this.createSilkTouchDispatchTable(
            ore,
            (Builder)this.applyExplosionDecay(
               ore,
               LootItem.lootTableItem(drop)
                  .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                  .apply(ApplyBonusCount.addUniformBonusCount(fortune))
            )
         )
      );
   }

   private static boolean isOreWithItemDrop(Block block) {
      return block == ModBlocks.AMBER_ORE.get()
         || block == ModBlocks.AIR_SHARD_ORE.get()
         || block == ModBlocks.FIRE_SHARD_ORE.get()
         || block == ModBlocks.WATER_SHARD_ORE.get()
         || block == ModBlocks.EARTH_SHARD_ORE.get()
         || block == ModBlocks.ORDER_SHARD_ORE.get()
         || block == ModBlocks.ENTROPY_SHARD_ORE.get();
   }

   protected void add(Block block, net.minecraft.world.level.storage.loot.LootTable.Builder table) {
      super.add(block, table);
      this.knownBlocks.add(block);
   }

   protected Iterable<Block> getKnownBlocks() {
      return this.knownBlocks;
   }
}
