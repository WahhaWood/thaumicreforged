package thaumcraft.data.provider;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider.IntrinsicTagAppender;
import net.minecraft.data.tags.TagsProvider.TagLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModItemTags;
import thaumcraft.init.registry.ModItems;

public class ItemTagsGenerator extends ItemTagsProvider {
   public ItemTagsGenerator(
      PackOutput output,
      CompletableFuture<Provider> lookupProvider,
      CompletableFuture<TagLookup<Block>> blockTagProvider,
      ExistingFileHelper existingFileHelper
   ) {
      super(output, lookupProvider, blockTagProvider, "thaumcraft", existingFileHelper);
   }

   protected void addTags(@NotNull Provider provider) {
      this.tag(ModItemTags.SHARDS_BASICS)
         .add(
            new Item[]{
               (Item)ModItems.AIR_SHARD.get(),
               (Item)ModItems.FIRE_SHARD.get(),
               (Item)ModItems.WATER_SHARD.get(),
               (Item)ModItems.EARTH_SHARD.get(),
               (Item)ModItems.ORDER_SHARD.get(),
               (Item)ModItems.ENTROPY_SHARD.get()
            }
         );
      this.tag(ModItemTags.SHARDS).addTag(ModItemTags.SHARDS_BASICS).add((Item)ModItems.BALANCED_SHARD.get());
      this.tag(ItemTags.LOGS).add(new Item[]{(Item)ModItems.GREATWOOD_LOG.get(), (Item)ModItems.SILVERWOOD_LOG.get()});
      this.tag(ModItemTags.ORES_SHARD)
         .add(
            new Item[]{
               (Item)ModItems.AIR_SHARD_ORE.get(),
               (Item)ModItems.FIRE_SHARD_ORE.get(),
               (Item)ModItems.WATER_SHARD_ORE.get(),
               (Item)ModItems.EARTH_SHARD_ORE.get(),
               (Item)ModItems.ORDER_SHARD_ORE.get(),
               (Item)ModItems.ENTROPY_SHARD_ORE.get()
            }
         );
      this.tag(ModItemTags.ORES_AMBER).add((Item)ModItems.AMBER_ORE.get());
      this.tag(ModItemTags.ORES_CINNABAR).add((Item)ModItems.CINNABAR_ORE.get());
      this.tag(Tags.Items.ORES).addTag(ModItemTags.ORES_SHARD).addTag(ModItemTags.ORES_AMBER).addTag(ModItemTags.ORES_CINNABAR);
      this.tag(ModItemTags.ADVANCED_GOLEMS)
         .add(
            new Item[]{
               (Item)ModItems.GOLEM_STRAW.get(),
               (Item)ModItems.GOLEM_WOOD.get(),
               (Item)ModItems.GOLEM_TALLOW.get(),
               (Item)ModItems.GOLEM_CLAY.get(),
               (Item)ModItems.GOLEM_FLESH.get(),
               (Item)ModItems.GOLEM_STONE.get(),
               (Item)ModItems.GOLEM_IRON.get(),
               (Item)ModItems.GOLEM_THAUMIUM.get()
            }
         );
      this.tag(ModItemTags.THAUMIUM_INGOT).add((Item)ModItems.THAUMIUM_INGOT.get());
      this.tag(ModItemTags.THAUMIUM_NUGGET).add((Item)ModItems.THAUMIUM_NUGGET.get());
      this.tag(ModItemTags.VOID_INGOT).add((Item)ModItems.VOID_METAL_INGOT.get());
      this.tag(ModItemTags.VOID_NUGGET).add((Item)ModItems.VOID_METAL_NUGGET.get());
      this.tag(ModItemTags.ICHORIUM_INGOT).add((Item)ModItems.ICHORIUM.get());
      this.tag(ModItemTags.ICHORIUM_NUGGET).add((Item)ModItems.ICHORIUM_NUGGET.get());
      this.tag(ModItemTags.SHARD_FIRE).add((Item)ModItems.FIRE_SHARD.get());
      this.tag(ModItemTags.SHARD_AIR).add((Item)ModItems.AIR_SHARD.get());
      this.tag(ModItemTags.SHARD_WATER).add((Item)ModItems.WATER_SHARD.get());
      this.tag(ModItemTags.SHARD_EARTH).add((Item)ModItems.EARTH_SHARD.get());
      this.tag(ModItemTags.SHARD_ORDER).add((Item)ModItems.ORDER_SHARD.get());
      this.tag(ModItemTags.SHARD_ENTROPY).add((Item)ModItems.ENTROPY_SHARD.get());
      this.tag(ModItemTags.SHARD_BALANCED).add((Item)ModItems.BALANCED_SHARD.get());
      IntrinsicTagAppender<Item> appender = this.tag(ModItemTags.CANDLES);
      ModItems.CANDLE.values().stream().map(DeferredHolder::get).forEach(appender::add);
   }
}
