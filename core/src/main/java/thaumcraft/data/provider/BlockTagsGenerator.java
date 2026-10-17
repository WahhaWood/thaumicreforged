package thaumcraft.data.provider;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider.IntrinsicTagAppender;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.init.registry.ModBlockTags;
import thaumcraft.init.registry.ModBlocks;

public class BlockTagsGenerator extends BlockTagsProvider {
   public BlockTagsGenerator(PackOutput output, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
      super(output, lookupProvider, "thaumcraft", existingFileHelper);
   }

   protected void addTags(@NotNull Provider provider) {
      IntrinsicTagAppender<Block> pickaxe = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
      pickaxe.add(
         new Block[]{
            (Block)ModBlocks.INFERNAL_FURNACE.get(),
            (Block)ModBlocks.CRUCIBLE.get(),
            (Block)ModBlocks.ARCANE_ALEMBIC.get(),
            (Block)ModBlocks.ALCHEMICAL_CONSTRUCT.get(),
            (Block)ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get(),
            (Block)ModBlocks.GRATE.get(),
            (Block)ModBlocks.ARCANE_LAMP.get(),
            (Block)ModBlocks.ARCANE_LAMP_GROWTH.get(),
            (Block)ModBlocks.ARCANE_LAMP_FERTILITY.get(),
            (Block)ModBlocks.THAUMATORIUM.get(),
            (Block)ModBlocks.MNEMONIC_MATRIX.get(),
            (Block)ModBlocks.VIS_RELAY.get(),
            (Block)ModBlocks.VIS_CHARGE_RELAY.get(),
            (Block)ModBlocks.ALCHEMY_FURNACE.get(),
            (Block)ModBlocks.ARCANE_PEDESTAL.get(),
            (Block)ModBlocks.RUNIC_MATRIX.get(),
            (Block)ModBlocks.INFUSION_PILLAR.get(),
            (Block)ModBlocks.WAND_PEDESTAL.get(),
            (Block)ModBlocks.WAND_PEDESTAL_FOCUS.get(),
            (Block)ModBlocks.NODE_STABILIZER.get(),
            (Block)ModBlocks.ADVANCED_NODE_STABILIZER.get(),
            (Block)ModBlocks.ADVANCED_NODE_INFUSER.get(),
            (Block)ModBlocks.NODE_TRANSDUCER.get(),
            (Block)ModBlocks.ARCANE_SPA.get(),
            (Block)ModBlocks.FOCAL_MANIPULATOR.get(),
            (Block)ModBlocks.SUMMONER.get(),
            (Block)ModBlocks.ENCHANTER.get(),
            (Block)ModBlocks.FLUX_SCRUBBER.get(),
            (Block)ModBlocks.ARCANE_PRESSURE_PLATE.get(),
            (Block)ModBlocks.FUNNEL.get(),
            (Block)ModBlocks.CINNABAR_ORE.get(),
            (Block)ModBlocks.AMBER_ORE.get(),
            (Block)ModBlocks.AIR_SHARD_ORE.get(),
            (Block)ModBlocks.FIRE_SHARD_ORE.get(),
            (Block)ModBlocks.WATER_SHARD_ORE.get(),
            (Block)ModBlocks.EARTH_SHARD_ORE.get(),
            (Block)ModBlocks.ORDER_SHARD_ORE.get(),
            (Block)ModBlocks.ENTROPY_SHARD_ORE.get(),
            (Block)ModBlocks.JAR.get(),
            (Block)ModBlocks.BRAIN_JAR.get(),
            (Block)ModBlocks.NODE_JAR.get(),
            (Block)ModBlocks.VOID_JAR.get(),
            (Block)ModBlocks.ARCANE_DOOR.get(),
            (Block)ModBlocks.WARDED.get(),
            (Block)ModBlocks.TRANSVECTOR_INTERFACE.get(),
            (Block)ModBlocks.TRANSVECTOR_DISLOCATOR.get(),
            (Block)ModBlocks.MOBILIZER.get(),
            (Block)ModBlocks.MOBILIZER_RELAY.get(),
            (Block)ModBlocks.AIR_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.FIRE_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.WATER_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.EARTH_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.ORDER_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.ENTROPY_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.MIXED_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.STRANGE_CRYSTALS.get(),
            (Block)ModBlocks.AIR_CRYSTAL.get(),
            (Block)ModBlocks.FIRE_CRYSTAL.get(),
            (Block)ModBlocks.WATER_CRYSTAL.get(),
            (Block)ModBlocks.EARTH_CRYSTAL.get(),
            (Block)ModBlocks.ORDER_CRYSTAL.get(),
            (Block)ModBlocks.ENTROPY_CRYSTAL.get(),
            (Block)ModBlocks.AMBER_BLOCK.get(),
            (Block)ModBlocks.AMBER_BRICKS.get(),
            (Block)ModBlocks.WARDED_GLASS.get(),
            (Block)ModBlocks.OBSIDIAN_TILE.get(),
            (Block)ModBlocks.TRAVEL_STONE.get(),
            (Block)ModBlocks.TRAVEL_STONE_SLAB.get(),
            (Block)ModBlocks.TRAVEL_STONE_STAIRS.get(),
            (Block)ModBlocks.DARK_QUARTZ_BLOCK.get(),
            (Block)ModBlocks.CHISELED_DARK_QUARTZ_BLOCK.get(),
            (Block)ModBlocks.DARK_QUARTZ_PILLAR.get(),
            (Block)ModBlocks.DARK_QUARTZ_SLAB.get(),
            (Block)ModBlocks.DARK_QUARTZ_STAIRS.get(),
            (Block)ModBlocks.WARP_GATE.get(),
            (Block)ModBlocks.WARDING_STONE.get(),
            (Block)ModBlocks.THAUMIUM_BLOCK.get(),
            (Block)ModBlocks.ARCANE_STONE.get(),
            (Block)ModBlocks.ARCANE_BRICKS.get(),
            (Block)ModBlocks.GOLEM_FETTER.get(),
            (Block)ModBlocks.ANCIENT_STONE.get(),
            (Block)ModBlocks.ANCIENT_ROCK.get(),
            (Block)ModBlocks.CRUSTED_STONE.get(),
            (Block)ModBlocks.ANCIENT_STONE_PEDESTAL.get(),
            (Block)ModBlocks.MAGIC_TALLOW_BLOCK.get(),
            (Block)ModBlocks.MIRROR.get(),
            (Block)ModBlocks.MIRROR_ESSENCE.get(),
            (Block)ModBlocks.ESSENCE_TUBE.get(),
            (Block)ModBlocks.ESSENCE_VALVE.get(),
            (Block)ModBlocks.FILTERED_ESSENCE_TUBE.get(),
            (Block)ModBlocks.ESSENCE_BUFFER.get(),
            (Block)ModBlocks.RESTRICTED_ESSENCE_TUBE.get(),
            (Block)ModBlocks.DIRECTIONAL_ESSENCE_TUBE.get(),
            (Block)ModBlocks.ESSENCE_CRYSTALLIZER.get(),
            (Block)ModBlocks.REPAIRER.get(),
            (Block)ModBlocks.ALCHEMICAL_CENTRIFUGE.get(),
            (Block)ModBlocks.ELDRITCH_ALTAR.get(),
            (Block)ModBlocks.ELDRITCH_TRAP.get(),
            (Block)ModBlocks.ELDRITCH_CRAB_SPAWNER.get(),
            (Block)ModBlocks.ELDRITCH_OBELISK.get(),
            (Block)ModBlocks.ELDRITCH_OBELISK_PART.get(),
            (Block)ModBlocks.GLOWING_CRUSTED_STONE.get(),
            (Block)ModBlocks.GLYPHED_STONE.get(),
            (Block)ModBlocks.RUNED_STONE.get(),
            (Block)ModBlocks.ESSENCE_RESERVOIR.get(),
            (Block)ModBlocks.ARCANE_STONE_SLAB.get(),
            (Block)ModBlocks.ARCANE_STONE_STAIRS.get(),
            (Block)ModBlocks.ELDRITCH_STONE_SLAB.get(),
            (Block)ModBlocks.ELDRITCH_STONE_STAIRS.get(),
            (Block)ModBlocks.ADVANCED_ALCHEMY_FURNACE.get(),
            (Block)ModBlocks.ADVANCED_ALCHEMY_FURNACE_BURN.get(),
            (Block)ModBlocks.ADVANCED_ALCHEMY_FURNACE_TANK.get(),
            (Block)ModBlocks.LOOT_URN.get(),
            (Block)ModBlocks.LOOT_URN_UNCOMMON.get(),
            (Block)ModBlocks.LOOT_URN_RARE.get(),
            (Block)ModBlocks.INFUSION_CLAW.get()
         }
      );
      pickaxe.addTag(ModBlockTags.OBSIDIAN_TOTEMS);
      IntrinsicTagAppender<Block> axe = this.tag(BlockTags.MINEABLE_WITH_AXE);
      axe.add(
         new Block[]{
            (Block)ModBlocks.ARCANE_BELLOWS.get(),
            (Block)ModBlocks.ARCANE_SENSOR.get(),
            (Block)ModBlocks.ARCANE_BORE_BASE.get(),
            (Block)ModBlocks.ARCANE_BORE.get(),
            (Block)ModBlocks.CRIMSON_CULT_BANNER.get(),
            (Block)ModBlocks.CRIMSON_CULT_WALL_BANNER.get(),
            (Block)ModBlocks.GREATWOOD_LOG.get(),
            (Block)ModBlocks.GREATWOOD_PLANKS.get(),
            (Block)ModBlocks.GREATWOOD_SLAB.get(),
            (Block)ModBlocks.GREATWOOD_STAIRS.get(),
            (Block)ModBlocks.SILVERWOOD_LOG.get(),
            (Block)ModBlocks.SILVERWOOD_PLANKS.get(),
            (Block)ModBlocks.SILVERWOOD_SLAB.get(),
            (Block)ModBlocks.SILVERWOOD_STAIRS.get(),
            (Block)ModBlocks.TABLE.get(),
            (Block)ModBlocks.RESEARCH_TABLE.get(),
            (Block)ModBlocks.DECONSTRUCTION_TABLE.get(),
            (Block)ModBlocks.ARCANE_WORKBENCH.get(),
            (Block)ModBlocks.HUNGRY_CHEST.get(),
            (Block)ModBlocks.LIFTER.get(),
            (Block)ModBlocks.MAGNET.get(),
            (Block)ModBlocks.MOB_MAGNET.get(),
            (Block)ModBlocks.SILVERWOOD_LOG_NODE.get(),
            (Block)ModBlocks.PLATFORM.get(),
            (Block)ModBlocks.MAGIC_BOX.get(),
            (Block)ModBlocks.LOOT_CRATE.get(),
            (Block)ModBlocks.LOOT_CRATE_UNCOMMON.get(),
            (Block)ModBlocks.LOOT_CRATE_RARE.get()
         }
      );

      for (DeferredHolder<Block, Block> banner : ModBlocks.BANNER.values()) {
         axe.add((Block)banner.get());
      }

      for (DeferredHolder<Block, Block> wallBanner : ModBlocks.WALL_BANNER.values()) {
         axe.add((Block)wallBanner.get());
      }

      this.tag(BlockTags.LOGS)
         .add(new Block[]{(Block)ModBlocks.GREATWOOD_LOG.get(), (Block)ModBlocks.SILVERWOOD_LOG.get(), (Block)ModBlocks.SILVERWOOD_LOG_NODE.get()});
      this.tag(BlockTags.LOGS_THAT_BURN)
         .add(new Block[]{(Block)ModBlocks.GREATWOOD_LOG.get(), (Block)ModBlocks.SILVERWOOD_LOG.get(), (Block)ModBlocks.SILVERWOOD_LOG_NODE.get()});
      this.tag(BlockTags.MINEABLE_WITH_SHOVEL)
         .add(new Block[]{(Block)ModBlocks.TAINT_CRUST.get(), (Block)ModBlocks.TAINT_SOIL.get(), (Block)ModBlocks.FLESH_BLOCK.get()});
      this.tag(BlockTags.MINEABLE_WITH_HOE).add(new Block[]{(Block)ModBlocks.GREATWOOD_LEAVES.get(), (Block)ModBlocks.SILVERWOOD_LEAVES.get()});
      IntrinsicTagAppender<Block> iron = this.tag(BlockTags.NEEDS_IRON_TOOL);
      iron.add(
         new Block[]{
            (Block)ModBlocks.INFERNAL_FURNACE.get(),
            (Block)ModBlocks.CRUCIBLE.get(),
            (Block)ModBlocks.ARCANE_ALEMBIC.get(),
            (Block)ModBlocks.ALCHEMICAL_CONSTRUCT.get(),
            (Block)ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get(),
            (Block)ModBlocks.GRATE.get(),
            (Block)ModBlocks.ARCANE_LAMP.get(),
            (Block)ModBlocks.ARCANE_LAMP_GROWTH.get(),
            (Block)ModBlocks.ARCANE_LAMP_FERTILITY.get(),
            (Block)ModBlocks.THAUMATORIUM.get(),
            (Block)ModBlocks.MNEMONIC_MATRIX.get(),
            (Block)ModBlocks.VIS_RELAY.get(),
            (Block)ModBlocks.VIS_CHARGE_RELAY.get(),
            (Block)ModBlocks.ALCHEMY_FURNACE.get(),
            (Block)ModBlocks.ARCANE_PEDESTAL.get(),
            (Block)ModBlocks.RUNIC_MATRIX.get(),
            (Block)ModBlocks.INFUSION_PILLAR.get(),
            (Block)ModBlocks.WAND_PEDESTAL.get(),
            (Block)ModBlocks.WAND_PEDESTAL_FOCUS.get(),
            (Block)ModBlocks.NODE_STABILIZER.get(),
            (Block)ModBlocks.ADVANCED_NODE_STABILIZER.get(),
            (Block)ModBlocks.ADVANCED_NODE_INFUSER.get(),
            (Block)ModBlocks.NODE_TRANSDUCER.get(),
            (Block)ModBlocks.ARCANE_SPA.get(),
            (Block)ModBlocks.FOCAL_MANIPULATOR.get(),
            (Block)ModBlocks.SUMMONER.get(),
            (Block)ModBlocks.ENCHANTER.get(),
            (Block)ModBlocks.FLUX_SCRUBBER.get(),
            (Block)ModBlocks.ARCANE_PRESSURE_PLATE.get(),
            (Block)ModBlocks.FUNNEL.get(),
            (Block)ModBlocks.CINNABAR_ORE.get(),
            (Block)ModBlocks.AMBER_ORE.get(),
            (Block)ModBlocks.AIR_SHARD_ORE.get(),
            (Block)ModBlocks.FIRE_SHARD_ORE.get(),
            (Block)ModBlocks.WATER_SHARD_ORE.get(),
            (Block)ModBlocks.EARTH_SHARD_ORE.get(),
            (Block)ModBlocks.ORDER_SHARD_ORE.get(),
            (Block)ModBlocks.ENTROPY_SHARD_ORE.get(),
            (Block)ModBlocks.JAR.get(),
            (Block)ModBlocks.BRAIN_JAR.get(),
            (Block)ModBlocks.NODE_JAR.get(),
            (Block)ModBlocks.VOID_JAR.get(),
            (Block)ModBlocks.ARCANE_DOOR.get(),
            (Block)ModBlocks.WARDED.get(),
            (Block)ModBlocks.TRANSVECTOR_INTERFACE.get(),
            (Block)ModBlocks.TRANSVECTOR_DISLOCATOR.get(),
            (Block)ModBlocks.MOBILIZER.get(),
            (Block)ModBlocks.MOBILIZER_RELAY.get(),
            (Block)ModBlocks.AIR_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.FIRE_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.WATER_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.EARTH_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.ORDER_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.ENTROPY_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.MIXED_CRYSTAL_CLUSTER.get(),
            (Block)ModBlocks.STRANGE_CRYSTALS.get(),
            (Block)ModBlocks.AIR_CRYSTAL.get(),
            (Block)ModBlocks.FIRE_CRYSTAL.get(),
            (Block)ModBlocks.WATER_CRYSTAL.get(),
            (Block)ModBlocks.EARTH_CRYSTAL.get(),
            (Block)ModBlocks.ORDER_CRYSTAL.get(),
            (Block)ModBlocks.ENTROPY_CRYSTAL.get(),
            (Block)ModBlocks.AMBER_BLOCK.get(),
            (Block)ModBlocks.AMBER_BRICKS.get(),
            (Block)ModBlocks.WARDED_GLASS.get(),
            (Block)ModBlocks.OBSIDIAN_TILE.get(),
            (Block)ModBlocks.TRAVEL_STONE.get(),
            (Block)ModBlocks.TRAVEL_STONE_SLAB.get(),
            (Block)ModBlocks.TRAVEL_STONE_STAIRS.get(),
            (Block)ModBlocks.DARK_QUARTZ_BLOCK.get(),
            (Block)ModBlocks.CHISELED_DARK_QUARTZ_BLOCK.get(),
            (Block)ModBlocks.DARK_QUARTZ_PILLAR.get(),
            (Block)ModBlocks.DARK_QUARTZ_SLAB.get(),
            (Block)ModBlocks.DARK_QUARTZ_STAIRS.get(),
            (Block)ModBlocks.WARP_GATE.get(),
            (Block)ModBlocks.WARDING_STONE.get(),
            (Block)ModBlocks.THAUMIUM_BLOCK.get(),
            (Block)ModBlocks.ARCANE_STONE.get(),
            (Block)ModBlocks.ARCANE_BRICKS.get(),
            (Block)ModBlocks.GOLEM_FETTER.get(),
            (Block)ModBlocks.ANCIENT_STONE.get(),
            (Block)ModBlocks.ANCIENT_ROCK.get(),
            (Block)ModBlocks.CRUSTED_STONE.get(),
            (Block)ModBlocks.ANCIENT_STONE_PEDESTAL.get(),
            (Block)ModBlocks.MAGIC_TALLOW_BLOCK.get(),
            (Block)ModBlocks.MIRROR.get(),
            (Block)ModBlocks.MIRROR_ESSENCE.get(),
            (Block)ModBlocks.ESSENCE_TUBE.get(),
            (Block)ModBlocks.ESSENCE_VALVE.get(),
            (Block)ModBlocks.FILTERED_ESSENCE_TUBE.get(),
            (Block)ModBlocks.ESSENCE_BUFFER.get(),
            (Block)ModBlocks.RESTRICTED_ESSENCE_TUBE.get(),
            (Block)ModBlocks.DIRECTIONAL_ESSENCE_TUBE.get(),
            (Block)ModBlocks.ESSENCE_CRYSTALLIZER.get(),
            (Block)ModBlocks.REPAIRER.get(),
            (Block)ModBlocks.ALCHEMICAL_CENTRIFUGE.get(),
            (Block)ModBlocks.ELDRITCH_ALTAR.get(),
            (Block)ModBlocks.ELDRITCH_TRAP.get(),
            (Block)ModBlocks.ELDRITCH_CRAB_SPAWNER.get(),
            (Block)ModBlocks.ELDRITCH_OBELISK.get(),
            (Block)ModBlocks.ELDRITCH_OBELISK_PART.get(),
            (Block)ModBlocks.GLOWING_CRUSTED_STONE.get(),
            (Block)ModBlocks.GLYPHED_STONE.get(),
            (Block)ModBlocks.RUNED_STONE.get(),
            (Block)ModBlocks.ESSENCE_RESERVOIR.get(),
            (Block)ModBlocks.ARCANE_STONE_SLAB.get(),
            (Block)ModBlocks.ARCANE_STONE_STAIRS.get(),
            (Block)ModBlocks.ELDRITCH_STONE_SLAB.get(),
            (Block)ModBlocks.ELDRITCH_STONE_STAIRS.get(),
            (Block)ModBlocks.ADVANCED_ALCHEMY_FURNACE.get(),
            (Block)ModBlocks.ADVANCED_ALCHEMY_FURNACE_BURN.get(),
            (Block)ModBlocks.ADVANCED_ALCHEMY_FURNACE_TANK.get(),
            (Block)ModBlocks.LOOT_URN.get(),
            (Block)ModBlocks.LOOT_URN_UNCOMMON.get(),
            (Block)ModBlocks.LOOT_URN_RARE.get(),
            (Block)ModBlocks.INFUSION_CLAW.get()
         }
      );
      iron.addTag(ModBlockTags.OBSIDIAN_TOTEMS);
      this.tag(ModBlockTags.OBSIDIAN_TOTEMS).add(new Block[]{(Block)ModBlocks.OBSIDIAN_TOTEM.get(), (Block)ModBlocks.CHARGED_OBSIDIAN_TOTEM.get()});
      this.tag(ModBlockTags.TAINT_MATERIAL)
         .add(
            new Block[]{
               (Block)ModBlocks.TAINT_CRUST.get(),
               (Block)ModBlocks.TAINT_SOIL.get(),
               (Block)ModBlocks.FLESH_BLOCK.get(),
               (Block)ModBlocks.TAINTED_GRASS.get(),
               (Block)ModBlocks.FIBROUS_TAINT.get(),
               (Block)ModBlocks.TAINTED_PLANT.get()
            }
         );
      this.tag(ModBlockTags.ORES_SHARD)
         .add(
            new Block[]{
               (Block)ModBlocks.AIR_SHARD_ORE.get(),
               (Block)ModBlocks.FIRE_SHARD_ORE.get(),
               (Block)ModBlocks.WATER_SHARD_ORE.get(),
               (Block)ModBlocks.EARTH_SHARD_ORE.get(),
               (Block)ModBlocks.ORDER_SHARD_ORE.get(),
               (Block)ModBlocks.ENTROPY_SHARD_ORE.get()
            }
         );
      this.tag(ModBlockTags.ORES_AMBER).add((Block)ModBlocks.AMBER_ORE.get());
      this.tag(ModBlockTags.ORES_CINNABAR).add((Block)ModBlocks.CINNABAR_ORE.get());
      this.tag(Blocks.ORES).addTag(ModBlockTags.ORES_SHARD).addTag(ModBlockTags.ORES_AMBER).addTag(ModBlockTags.ORES_CINNABAR);
   }
}
