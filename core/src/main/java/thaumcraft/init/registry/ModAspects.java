package thaumcraft.init.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags.Items;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.entities.smart.pech.EntityPech;
import thaumcraft.entities.smart.wisp.EntityWisp;

public class ModAspects {
   private static final String POWERED_CREEPER_HASH = "powered";
   private static final String PECH_TYPE_0_HASH = "PechType0";
   private static final String PECH_TYPE_1_HASH = "PechType1";
   private static final String PECH_TYPE_2_HASH = "PechType2";

   public static void init() {
      registerItemAspects();
      registerEntityAspects();
   }

   private static void registerEntityAspects() {
      registerVanillaEntityAspects();
      registerThaumcraftEntityAspects();
      registerSpecializedEntityAspects();
   }

   private static void registerVanillaEntityAspects() {
      ThaumcraftApi.registerEntityTag(EntityType.ZOMBIE, new AspectList().add(Aspect.UNDEAD, 2).add(Aspect.MAN, 1).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerEntityTag(EntityType.GIANT, new AspectList().add(Aspect.UNDEAD, 4).add(Aspect.MAN, 3).add(Aspect.EARTH, 3));
      ThaumcraftApi.registerEntityTag(EntityType.SKELETON, new AspectList().add(Aspect.UNDEAD, 3).add(Aspect.MAN, 1).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerEntityTag(EntityType.WITHER_SKELETON, new AspectList().add(Aspect.UNDEAD, 4).add(Aspect.MAN, 1).add(Aspect.FIRE, 2));
      ThaumcraftApi.registerEntityTag(
         EntityType.CREEPER, new AspectList().add(Aspect.PLANT, 2).add(Aspect.FIRE, 2), entity -> !((Creeper)entity).isPowered(), null
      );
      ThaumcraftApi.registerEntityTag(
         EntityType.CREEPER,
         new AspectList().add(Aspect.PLANT, 3).add(Aspect.FIRE, 3).add(Aspect.ENERGY, 3),
         entity -> ((Creeper)entity).isPowered(),
         "powered"
      );
      ThaumcraftApi.registerEntityTag(EntityType.HORSE, new AspectList().add(Aspect.BEAST, 4).add(Aspect.EARTH, 1).add(Aspect.AIR, 1));
      ThaumcraftApi.registerEntityTag(EntityType.PIG, new AspectList().add(Aspect.BEAST, 2).add(Aspect.EARTH, 2));
      ThaumcraftApi.registerEntityTag(EntityType.EXPERIENCE_ORB, new AspectList().add(Aspect.MIND, 5));
      ThaumcraftApi.registerEntityTag(EntityType.SHEEP, new AspectList().add(Aspect.BEAST, 2).add(Aspect.EARTH, 2));
      ThaumcraftApi.registerEntityTag(EntityType.COW, new AspectList().add(Aspect.BEAST, 3).add(Aspect.EARTH, 3));
      ThaumcraftApi.registerEntityTag(EntityType.MOOSHROOM, new AspectList().add(Aspect.BEAST, 3).add(Aspect.PLANT, 1).add(Aspect.EARTH, 2));
      ThaumcraftApi.registerEntityTag(EntityType.SNOW_GOLEM, new AspectList().add(Aspect.COLD, 3).add(Aspect.WATER, 1));
      ThaumcraftApi.registerEntityTag(EntityType.OCELOT, new AspectList().add(Aspect.BEAST, 3).add(Aspect.ENTROPY, 3));
      ThaumcraftApi.registerEntityTag(EntityType.CHICKEN, new AspectList().add(Aspect.BEAST, 2).add(Aspect.FLIGHT, 2).add(Aspect.AIR, 1));
      ThaumcraftApi.registerEntityTag(EntityType.SQUID, new AspectList().add(Aspect.BEAST, 2).add(Aspect.WATER, 2));
      ThaumcraftApi.registerEntityTag(EntityType.WOLF, new AspectList().add(Aspect.BEAST, 3).add(Aspect.EARTH, 3));
      ThaumcraftApi.registerEntityTag(EntityType.BAT, new AspectList().add(Aspect.BEAST, 1).add(Aspect.FLIGHT, 1).add(Aspect.AIR, 1));
      ThaumcraftApi.registerEntityTag(EntityType.BOAT, new AspectList().add(Aspect.MECHANISM, 2).add(Aspect.WATER, 2));
      ThaumcraftApi.registerEntityTag(EntityType.SPIDER, new AspectList().add(Aspect.BEAST, 3).add(Aspect.ENTROPY, 2));
      ThaumcraftApi.registerEntityTag(EntityType.SLIME, new AspectList().add(Aspect.SLIME, 2).add(Aspect.WATER, 2));
      ThaumcraftApi.registerEntityTag(EntityType.GHAST, new AspectList().add(Aspect.UNDEAD, 3).add(Aspect.FIRE, 2));
      ThaumcraftApi.registerEntityTag(EntityType.ZOMBIFIED_PIGLIN, new AspectList().add(Aspect.UNDEAD, 4).add(Aspect.FIRE, 2));
      ThaumcraftApi.registerEntityTag(EntityType.ENDERMAN, new AspectList().add(Aspect.ELDRITCH, 4).add(Aspect.TRAVEL, 2).add(Aspect.AIR, 2));
      ThaumcraftApi.registerEntityTag(EntityType.CAVE_SPIDER, new AspectList().add(Aspect.BEAST, 2).add(Aspect.POISON, 2).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerEntityTag(EntityType.SILVERFISH, new AspectList().add(Aspect.BEAST, 1).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerEntityTag(EntityType.BLAZE, new AspectList().add(Aspect.ELDRITCH, 4).add(Aspect.FIRE, 1));
      ThaumcraftApi.registerEntityTag(EntityType.MAGMA_CUBE, new AspectList().add(Aspect.SLIME, 3).add(Aspect.FIRE, 2));
      ThaumcraftApi.registerEntityTag(EntityType.ENDER_DRAGON, new AspectList().add(Aspect.ELDRITCH, 20).add(Aspect.BEAST, 20).add(Aspect.ENTROPY, 20));
      ThaumcraftApi.registerEntityTag(EntityType.WITHER, new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.ENTROPY, 20).add(Aspect.FIRE, 15));
      ThaumcraftApi.registerEntityTag(EntityType.WITCH, new AspectList().add(Aspect.MAN, 3).add(Aspect.MAGIC, 2).add(Aspect.FIRE, 1));
      ThaumcraftApi.registerEntityTag(EntityType.VILLAGER, new AspectList().add(Aspect.MAN, 3).add(Aspect.AIR, 2));
      ThaumcraftApi.registerEntityTag(EntityType.IRON_GOLEM, new AspectList().add(Aspect.METAL, 4).add(Aspect.EARTH, 3));
      ThaumcraftApi.registerEntityTag(EntityType.MINECART, new AspectList().add(Aspect.MECHANISM, 3).add(Aspect.AIR, 2));
      ThaumcraftApi.registerEntityTag(EntityType.CHEST_MINECART, new AspectList().add(Aspect.MECHANISM, 3).add(Aspect.AIR, 1).add(Aspect.VOID, 1));
      ThaumcraftApi.registerEntityTag(EntityType.FURNACE_MINECART, new AspectList().add(Aspect.MECHANISM, 3).add(Aspect.AIR, 1).add(Aspect.FIRE, 1));
      ThaumcraftApi.registerEntityTag(EntityType.TNT_MINECART, new AspectList().add(Aspect.MECHANISM, 3).add(Aspect.AIR, 1).add(Aspect.FIRE, 1));
      ThaumcraftApi.registerEntityTag(EntityType.HOPPER_MINECART, new AspectList().add(Aspect.MECHANISM, 3).add(Aspect.AIR, 1).add(Aspect.EXCHANGE, 1));
      ThaumcraftApi.registerEntityTag(EntityType.SPAWNER_MINECART, new AspectList().add(Aspect.MECHANISM, 3).add(Aspect.AIR, 1).add(Aspect.MAGIC, 1));
      ThaumcraftApi.registerEntityTag(EntityType.END_CRYSTAL, new AspectList().add(Aspect.ELDRITCH, 3).add(Aspect.MAGIC, 3).add(Aspect.HEAL, 3));
      ThaumcraftApi.registerEntityTag(EntityType.ITEM_FRAME, new AspectList().add(Aspect.SENSES, 3).add(Aspect.CLOTH, 1));
      ThaumcraftApi.registerEntityTag(EntityType.PAINTING, new AspectList().add(Aspect.SENSES, 5).add(Aspect.CLOTH, 3));
   }

   private static void registerThaumcraftEntityAspects() {
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.PRIMAL_ORB.get(),
         new AspectList().add(Aspect.AIR, 5).add(Aspect.ENTROPY, 10).add(Aspect.MAGIC, 10).add(Aspect.ENERGY, 10)
      );
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.FIRE_BAT.get(), new AspectList().add(Aspect.BEAST, 2).add(Aspect.FLIGHT, 1).add(Aspect.FIRE, 2)
      );
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.THAUMIC_SLIME.get(), new AspectList().add(Aspect.SLIME, 2).add(Aspect.MAGIC, 1).add(Aspect.WATER, 1)
      );
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.BRAINY_ZOMBIE.get(),
         new AspectList().add(Aspect.UNDEAD, 3).add(Aspect.MAN, 1).add(Aspect.MIND, 1).add(Aspect.EARTH, 1)
      );
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.GIANT_BRAINY_ZOMBIE.get(),
         new AspectList().add(Aspect.UNDEAD, 4).add(Aspect.MAN, 2).add(Aspect.MIND, 1).add(Aspect.EARTH, 2)
      );
      ThaumcraftApi.registerEntityTag((EntityType<? extends Entity>)ModEntities.TENTACLE.get(), new AspectList().add(Aspect.TAINT, 3).add(Aspect.WATER, 2));
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.TENTACLE_SMALL.get(), new AspectList().add(Aspect.TAINT, 1).add(Aspect.WATER, 1)
      );
      ThaumcraftApi.registerEntityTag((EntityType<? extends Entity>)ModEntities.TAINT_SPIDER.get(), new AspectList().add(Aspect.TAINT, 1).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerEntityTag((EntityType<? extends Entity>)ModEntities.TAINT_SPORE.get(), new AspectList().add(Aspect.TAINT, 2).add(Aspect.AIR, 2));
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.TAINT_SPORE_SWARMER.get(), new AspectList().add(Aspect.TAINT, 2).add(Aspect.AIR, 2)
      );
      ThaumcraftApi.registerEntityTag((EntityType<? extends Entity>)ModEntities.TAINT_SWARM.get(), new AspectList().add(Aspect.TAINT, 3).add(Aspect.AIR, 3));
      ThaumcraftApi.registerEntityTag((EntityType<? extends Entity>)ModEntities.TAINT_PIG.get(), new AspectList().add(Aspect.TAINT, 2).add(Aspect.EARTH, 2));
      ThaumcraftApi.registerEntityTag((EntityType<? extends Entity>)ModEntities.TAINT_SHEEP.get(), new AspectList().add(Aspect.TAINT, 2).add(Aspect.EARTH, 2));
      ThaumcraftApi.registerEntityTag((EntityType<? extends Entity>)ModEntities.TAINT_COW.get(), new AspectList().add(Aspect.TAINT, 3).add(Aspect.EARTH, 3));
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.TAINT_CHICKEN.get(), new AspectList().add(Aspect.TAINT, 2).add(Aspect.FLIGHT, 2).add(Aspect.AIR, 1)
      );
      ThaumcraftApi.registerEntityTag((EntityType<? extends Entity>)ModEntities.TAINT_VILLAGER.get(), new AspectList().add(Aspect.TAINT, 3).add(Aspect.AIR, 2));
      ThaumcraftApi.registerEntityTag((EntityType<? extends Entity>)ModEntities.TAINT_CREEPER.get(), new AspectList().add(Aspect.TAINT, 2).add(Aspect.FIRE, 2));
      ThaumcraftApi.registerEntityTag((EntityType<? extends Entity>)ModEntities.MIND_SPIDER.get(), new AspectList().add(Aspect.TAINT, 2).add(Aspect.FIRE, 2));
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.ELDRITCH_GUARDIAN.get(), new AspectList().add(Aspect.ELDRITCH, 4).add(Aspect.DEATH, 2).add(Aspect.UNDEAD, 4)
      );
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.ELDRITCH_ORB.get(), new AspectList().add(Aspect.ELDRITCH, 2).add(Aspect.DEATH, 2)
      );
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.CULTIST_KNIGHT.get(), new AspectList().add(Aspect.ELDRITCH, 1).add(Aspect.MAN, 2).add(Aspect.ENTROPY, 1)
      );
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.CULTIST_CLERIC.get(), new AspectList().add(Aspect.ELDRITCH, 1).add(Aspect.MAN, 2).add(Aspect.ENTROPY, 1)
      );
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.GOLEM.get(), new AspectList().add(Aspect.AIR, 2).add(Aspect.EARTH, 2).add(Aspect.MAGIC, 2)
      );
   }

   private static void registerSpecializedEntityAspects() {
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.PECH.get(),
         new AspectList().add(Aspect.MAN, 2).add(Aspect.MAGIC, 2).add(Aspect.EXCHANGE, 2).add(Aspect.GREED, 2),
         entity -> ((EntityPech)entity).getPechType() == 0,
         "PechType0"
      );
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.PECH.get(),
         new AspectList().add(Aspect.MAN, 2).add(Aspect.MAGIC, 2).add(Aspect.EXCHANGE, 2).add(Aspect.WEAPON, 2),
         entity -> ((EntityPech)entity).getPechType() == 1,
         "PechType1"
      );
      ThaumcraftApi.registerEntityTag(
         (EntityType<? extends Entity>)ModEntities.PECH.get(),
         new AspectList().add(Aspect.MAN, 2).add(Aspect.MAGIC, 4).add(Aspect.EXCHANGE, 2),
         entity -> ((EntityPech)entity).getPechType() == 2,
         "PechType2"
      );

      for (Aspect aspect : Aspect.aspects.values()) {
         ThaumcraftApi.registerEntityTag(
            (EntityType<? extends Entity>)ModEntities.WISP.get(),
            new AspectList().add(aspect, 2).add(Aspect.MAGIC, 1).add(Aspect.AIR, 1),
            entity -> aspect.getTag().equals(((EntityWisp)entity).getAspectTag()),
            "Type" + aspect.getTag()
         );
      }
   }

   private static void registerItemAspects() {
      ThaumcraftApi.registerObjectTag(Items.STONES, new AspectList().add(Aspect.EARTH, 2));
      ThaumcraftApi.registerObjectTag(Items.COBBLESTONES, new AspectList().add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1));
      ThaumcraftApi.registerObjectTag(ItemTags.LOGS, new AspectList().add(Aspect.TREE, 4));
      ThaumcraftApi.registerObjectTag(ItemTags.PLANKS, new AspectList().add(Aspect.TREE, 1));
      ThaumcraftApi.registerObjectTag(ItemTags.WOODEN_SLABS, new AspectList().add(Aspect.TREE, 1));
      ThaumcraftApi.registerObjectTag(ItemTags.WOODEN_STAIRS, new AspectList().add(Aspect.TREE, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.STICK, new AspectList().add(Aspect.TREE, 1));
      ThaumcraftApi.registerObjectTag(ItemTags.SAPLINGS, new AspectList().add(Aspect.TREE, 1).add(Aspect.PLANT, 2));
      ThaumcraftApi.registerObjectTag(ItemTags.LEAVES, new AspectList().add(Aspect.PLANT, 1));
      ThaumcraftApi.registerObjectTag(Items.DYES, new AspectList().add(Aspect.SENSES, 1));
      ThaumcraftApi.registerObjectTag(Items.ORES_LAPIS, new AspectList().add(Aspect.EARTH, 1).add(Aspect.SENSES, 3));
      ThaumcraftApi.registerObjectTag(Items.ORES_DIAMOND, new AspectList().add(Aspect.EARTH, 1).add(Aspect.GREED, 3).add(Aspect.CRYSTAL, 3));
      ThaumcraftApi.registerObjectTag(Items.GEMS_DIAMOND, new AspectList().add(Aspect.CRYSTAL, 4).add(Aspect.GREED, 4));
      ThaumcraftApi.registerObjectTag(Items.ORES_REDSTONE, new AspectList().add(Aspect.EARTH, 1).add(Aspect.ENERGY, 2).add(Aspect.MECHANISM, 2));
      ThaumcraftApi.registerObjectTag(Items.ORES_EMERALD, new AspectList().add(Aspect.EARTH, 1).add(Aspect.GREED, 4).add(Aspect.CRYSTAL, 3));
      ThaumcraftApi.registerObjectTag(Items.GEMS_EMERALD, new AspectList().add(Aspect.CRYSTAL, 4).add(Aspect.GREED, 5));
      ThaumcraftApi.registerObjectTag(Items.ORES_QUARTZ, new AspectList().add(Aspect.EARTH, 1).add(Aspect.CRYSTAL, 3));
      ThaumcraftApi.registerObjectTag(Items.GEMS_QUARTZ, new AspectList().add(Aspect.CRYSTAL, 1).add(Aspect.ENERGY, 1));
      ThaumcraftApi.registerObjectTag(Items.NUGGETS_GOLD, new AspectList().add(Aspect.METAL, 1));
      ThaumcraftApi.registerObjectTag(Items.NUGGETS_IRON, new AspectList().add(Aspect.METAL, 1));
      ThaumcraftApi.registerObjectTag(Items.ORES_IRON, new AspectList().add(Aspect.EARTH, 1).add(Aspect.METAL, 3));
      ThaumcraftApi.registerObjectTag(forgeTag("dusts/iron"), new AspectList().add(Aspect.METAL, 3).add(Aspect.ENTROPY, 1));
      ThaumcraftApi.registerObjectTag(Items.INGOTS_IRON, new AspectList().add(Aspect.METAL, 4));
      ThaumcraftApi.registerObjectTag(Items.ORES_GOLD, new AspectList().add(Aspect.EARTH, 1).add(Aspect.METAL, 2).add(Aspect.GREED, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("dusts/gold"), new AspectList().add(Aspect.METAL, 2).add(Aspect.ENTROPY, 1).add(Aspect.GREED, 1));
      ThaumcraftApi.registerObjectTag(Items.INGOTS_GOLD, new AspectList().add(Aspect.METAL, 3).add(Aspect.GREED, 2));
      ThaumcraftApi.registerObjectTag(Items.ORES_COAL, new AspectList().add(Aspect.EARTH, 1).add(Aspect.ENERGY, 2).add(Aspect.FIRE, 1));
      ThaumcraftApi.registerObjectTag(ItemTags.COALS, new AspectList().add(Aspect.ENERGY, 2).add(Aspect.FIRE, 2));
      ThaumcraftApi.registerObjectTag(Items.DUSTS_REDSTONE, new AspectList().add(Aspect.ENERGY, 2).add(Aspect.MECHANISM, 1));
      ThaumcraftApi.registerObjectTag(Items.DUSTS_GLOWSTONE, new AspectList().add(Aspect.SENSES, 1).add(Aspect.LIGHT, 2));
      ThaumcraftApi.registerObjectTag(Blocks.GLOWSTONE, new AspectList().add(Aspect.SENSES, 3).add(Aspect.LIGHT, 10));
      ThaumcraftApi.registerObjectTag(forgeTag("nuggets/copper"), new AspectList().add(Aspect.METAL, 1));
      ThaumcraftApi.registerObjectTag(Items.INGOTS_COPPER, new AspectList().add(Aspect.METAL, 3).add(Aspect.EXCHANGE, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("dusts/copper"), new AspectList().add(Aspect.METAL, 2).add(Aspect.ENTROPY, 1).add(Aspect.EXCHANGE, 1));
      ThaumcraftApi.registerObjectTag(Items.ORES_COPPER, new AspectList().add(Aspect.METAL, 2).add(Aspect.EARTH, 1).add(Aspect.EXCHANGE, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("nuggets/tin"), new AspectList().add(Aspect.METAL, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("ingots/tin"), new AspectList().add(Aspect.METAL, 3).add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("dusts/tin"), new AspectList().add(Aspect.METAL, 2).add(Aspect.ENTROPY, 1).add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("ores/tin"), new AspectList().add(Aspect.METAL, 3).add(Aspect.ENTROPY, 1).add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("nuggets/silver"), new AspectList().add(Aspect.METAL, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("ingots/silver"), new AspectList().add(Aspect.METAL, 3).add(Aspect.GREED, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("dusts/silver"), new AspectList().add(Aspect.METAL, 2).add(Aspect.ENTROPY, 1).add(Aspect.GREED, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("ores/silver"), new AspectList().add(Aspect.METAL, 3).add(Aspect.ENTROPY, 1).add(Aspect.GREED, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("nuggets/lead"), new AspectList().add(Aspect.METAL, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("ingots/lead"), new AspectList().add(Aspect.METAL, 3).add(Aspect.ORDER, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("dusts/lead"), new AspectList().add(Aspect.METAL, 2).add(Aspect.ENTROPY, 1).add(Aspect.ORDER, 1));
      ThaumcraftApi.registerObjectTag(forgeTag("ores/lead"), new AspectList().add(Aspect.METAL, 3).add(Aspect.ENTROPY, 1).add(Aspect.ORDER, 1));
      ThaumcraftApi.registerObjectTag(
         Blocks.BEDROCK, new AspectList().add(Aspect.VOID, 16).add(Aspect.ENTROPY, 16).add(Aspect.EARTH, 16).add(Aspect.DARKNESS, 16)
      );
      ThaumcraftApi.registerObjectTag(ItemTags.DIRT, new AspectList().add(Aspect.EARTH, 2));
      ThaumcraftApi.registerObjectTag(Blocks.GRASS_BLOCK, new AspectList().add(Aspect.EARTH, 1).add(Aspect.PLANT, 1));
      ThaumcraftApi.registerObjectTag(Blocks.FARMLAND, new AspectList().add(Aspect.EARTH, 1).add(Aspect.HARVEST, 2));
      ThaumcraftApi.registerObjectTag(ItemTags.SAND, new AspectList().add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1));
      ThaumcraftApi.registerObjectTag(Blocks.SHORT_GRASS, new AspectList().add(Aspect.EARTH, 1).add(Aspect.PLANT, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.CLAY_BALL, new AspectList().add(Aspect.WATER, 1).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerObjectTag(Items.END_STONES, new AspectList().add(Aspect.EARTH, 1).add(Aspect.DARKNESS, 1));
      ThaumcraftApi.registerObjectTag(Items.GRAVELS, new AspectList().add(Aspect.EARTH, 1).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerObjectTag(Blocks.MYCELIUM, new AspectList().add(Aspect.EARTH, 1).add(Aspect.PLANT, 1));
      ThaumcraftApi.registerObjectTag(Blocks.CLAY, new AspectList().add(Aspect.EARTH, 3).add(Aspect.WATER, 3));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.BRICK, new AspectList().add(Aspect.EARTH, 1).add(Aspect.FIRE, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.NETHER_BRICK, new AspectList().add(Aspect.FIRE, 1));
      ThaumcraftApi.registerObjectTag(Blocks.SOUL_SAND, new AspectList().add(Aspect.EARTH, 1).add(Aspect.TRAP, 1).add(Aspect.SOUL, 1));
      ThaumcraftApi.registerObjectTag(Items.NETHERRACKS, new AspectList().add(Aspect.EARTH, 1).add(Aspect.EARTH, 1).add(Aspect.FIRE, 1));
      ThaumcraftApi.registerObjectTag(Blocks.NETHER_BRICKS, new AspectList().add(Aspect.EARTH, 2).add(Aspect.FIRE, 1));
      ThaumcraftApi.registerObjectTag(Items.GLASS_BLOCKS_COLORLESS, new AspectList().add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(Items.GLASS_BLOCKS, new AspectList().add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(Blocks.MOSSY_COBBLESTONE, new AspectList().add(Aspect.EARTH, 1).add(Aspect.PLANT, 1).add(Aspect.MAGIC, 1));
      ThaumcraftApi.registerObjectTag(Items.OBSIDIANS, new AspectList().add(Aspect.EARTH, 2).add(Aspect.FIRE, 2).add(Aspect.DARKNESS, 1));
      registerLegacyVanillaParityOverrides();
      ThaumcraftApi.registerObjectTag(Blocks.DEAD_BUSH, new AspectList().add(Aspect.PLANT, 1).add(Aspect.ENTROPY, 1));
      ThaumcraftApi.registerObjectTag(Blocks.VINE, new AspectList().add(Aspect.PLANT, 1));
      ThaumcraftApi.registerObjectTag(Items.SEEDS, new AspectList().add(Aspect.PLANT, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.MELON, new AspectList().add(Aspect.HUNGER, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.NETHER_WART, new AspectList().add(Aspect.PLANT, 1).add(Aspect.MAGIC, 1));
      ThaumcraftApi.registerObjectTag(ItemTags.FLOWERS, new AspectList().add(Aspect.PLANT, 1).add(Aspect.LIFE, 1).add(Aspect.SENSES, 1));
      ThaumcraftApi.registerObjectTag(Blocks.CACTUS, new AspectList().add(Aspect.PLANT, 3).add(Aspect.WATER, 1).add(Aspect.ENTROPY, 1));
      ThaumcraftApi.registerObjectTag(Blocks.BROWN_MUSHROOM, new AspectList().add(Aspect.PLANT, 1).add(Aspect.DARKNESS, 1).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerObjectTag(Blocks.RED_MUSHROOM, new AspectList().add(Aspect.PLANT, 1).add(Aspect.DARKNESS, 1).add(Aspect.FIRE, 1));
      ThaumcraftApi.registerObjectTag(Blocks.BROWN_MUSHROOM_BLOCK, new AspectList().add(Aspect.PLANT, 2).add(Aspect.DARKNESS, 1).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerObjectTag(Blocks.RED_MUSHROOM_BLOCK, new AspectList().add(Aspect.PLANT, 2).add(Aspect.DARKNESS, 1).add(Aspect.FIRE, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.SUGAR_CANE, new AspectList().add(Aspect.PLANT, 1).add(Aspect.WATER, 1).add(Aspect.AIR, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.WHEAT, new AspectList().add(Aspect.CROP, 2).add(Aspect.HUNGER, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.APPLE, new AspectList().add(Aspect.CROP, 2).add(Aspect.HUNGER, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.CARROT, new AspectList().add(Aspect.CROP, 1).add(Aspect.HUNGER, 1).add(Aspect.SENSES, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.POTATO, new AspectList().add(Aspect.CROP, 1).add(Aspect.HUNGER, 1).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.BAKED_POTATO, new AspectList().add(Aspect.CROP, 1).add(Aspect.HUNGER, 2));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.POISONOUS_POTATO, new AspectList().add(Aspect.CROP, 1).add(Aspect.POISON, 2));
      ThaumcraftApi.registerObjectTag(Blocks.PUMPKIN, new AspectList().add(Aspect.CROP, 2));
      ThaumcraftApi.registerComplexObjectTag(Blocks.MELON, new AspectList().add(Aspect.CROP, 2).remove(Aspect.HUNGER, 4));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.NAME_TAG, new AspectList().add(Aspect.MIND, 2).add(Aspect.BEAST, 2));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.LEATHER_HORSE_ARMOR, new AspectList().add(Aspect.ARMOR, 2).add(Aspect.BEAST, 4));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.IRON_HORSE_ARMOR, new AspectList().add(Aspect.ARMOR, 2).add(Aspect.BEAST, 2));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.GOLDEN_HORSE_ARMOR, new AspectList().add(Aspect.ARMOR, 4).add(Aspect.BEAST, 2));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.DIAMOND_HORSE_ARMOR, new AspectList().add(Aspect.ARMOR, 6).add(Aspect.BEAST, 2));
      ThaumcraftApi.registerObjectTag(Blocks.FIRE, new AspectList().add(Aspect.FIRE, 4));
      ThaumcraftApi.registerObjectTag(Blocks.WATER, new AspectList().add(Aspect.WATER, 4));
      ThaumcraftApi.registerObjectTag(Blocks.LAVA, new AspectList().add(Aspect.FIRE, 3).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerObjectTag(Blocks.ICE, new AspectList().add(Aspect.COLD, 4));
      ThaumcraftApi.registerObjectTag(Blocks.PACKED_ICE, new AspectList().add(Aspect.COLD, 3).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.SNOWBALL, new AspectList().add(Aspect.COLD, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.COOKIE, new AspectList().add(Aspect.HUNGER, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.POTION, new AspectList().add(Aspect.WATER, 1).add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(Blocks.TORCH, new AspectList().add(Aspect.LIGHT, 1));
      ThaumcraftApi.registerObjectTag(Blocks.COBWEB, new AspectList().add(Aspect.TRAP, 2).add(Aspect.CLOTH, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.FLINT, new AspectList().add(Aspect.EARTH, 1).add(Aspect.TOOL, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.STRING, new AspectList().add(Aspect.BEAST, 1).add(Aspect.CLOTH, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.SLIME_BALL, new AspectList().add(Aspect.SLIME, 2));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.LEATHER, new AspectList().add(Aspect.CLOTH, 2).add(Aspect.BEAST, 1).add(Aspect.ARMOR, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.ROTTEN_FLESH, new AspectList().add(Aspect.MAN, 1).add(Aspect.FLESH, 2));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.FEATHER, new AspectList().add(Aspect.FLIGHT, 2).add(Aspect.AIR, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.BONE, new AspectList().add(Aspect.DEATH, 2).add(Aspect.FLESH, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.EGG, new AspectList().add(Aspect.SLIME, 1).add(Aspect.LIFE, 1).add(Aspect.BEAST, 1));
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.SPIDER_EYE, new AspectList().add(Aspect.SENSES, 2).add(Aspect.BEAST, 2).add(Aspect.POISON, 2)
      );
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.GUNPOWDER, new AspectList().add(Aspect.FIRE, 4).add(Aspect.ENTROPY, 4));
      ThaumcraftApi.registerObjectTag(ItemTags.WOOL, new AspectList().add(Aspect.CLOTH, 4).add(Aspect.CRAFT, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.SALMON, new AspectList().add(Aspect.FLESH, 3).add(Aspect.LIFE, 1).add(Aspect.WATER, 1));
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.COOKED_SALMON, new AspectList().add(Aspect.CRAFT, 1).add(Aspect.FLESH, 4).add(Aspect.HUNGER, 3)
      );
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.CHICKEN, new AspectList().add(Aspect.FLESH, 3).add(Aspect.LIFE, 2).add(Aspect.BEAST, 1));
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.COOKED_CHICKEN, new AspectList().add(Aspect.CRAFT, 1).add(Aspect.FLESH, 4).add(Aspect.HUNGER, 3)
      );
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.PORKCHOP, new AspectList().add(Aspect.FLESH, 3).add(Aspect.LIFE, 1).add(Aspect.BEAST, 1));
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.COOKED_PORKCHOP, new AspectList().add(Aspect.CRAFT, 1).add(Aspect.FLESH, 3).add(Aspect.HUNGER, 3)
      );
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.BEEF, new AspectList().add(Aspect.FLESH, 4).add(Aspect.LIFE, 2).add(Aspect.BEAST, 1));
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.COOKED_BEEF, new AspectList().add(Aspect.CRAFT, 1).add(Aspect.FLESH, 4).add(Aspect.HUNGER, 4)
      );
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.BLAZE_ROD, new AspectList().add(Aspect.FIRE, 4).add(Aspect.MAGIC, 2));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.SADDLE, new AspectList().add(Aspect.BEAST, 2).add(Aspect.CLOTH, 3).add(Aspect.TRAVEL, 3));
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.ENDER_PEARL, new AspectList().add(Aspect.ELDRITCH, 4).add(Aspect.MAGIC, 2).add(Aspect.TRAVEL, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.GHAST_TEAR, new AspectList().add(Aspect.WATER, 1).add(Aspect.UNDEAD, 4).add(Aspect.SOUL, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.SKELETON_SKULL, new AspectList().add(Aspect.DEATH, 4).add(Aspect.SOUL, 4).add(Aspect.UNDEAD, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.WITHER_SKELETON_SKULL,
         new AspectList().add(Aspect.DEATH, 4).add(Aspect.SOUL, 4).add(Aspect.UNDEAD, 4).add(Aspect.POISON, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.ZOMBIE_HEAD, new AspectList().add(Aspect.DEATH, 4).add(Aspect.SOUL, 4).add(Aspect.FLESH, 4)
      );
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.PLAYER_HEAD, new AspectList().add(Aspect.DEATH, 4).add(Aspect.SOUL, 4).add(Aspect.MAN, 4));
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.CREEPER_HEAD, new AspectList().add(Aspect.DEATH, 4).add(Aspect.SOUL, 4).add(Aspect.ENTROPY, 2).add(Aspect.FIRE, 2)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_11, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_13, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.WATER, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_CAT, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.BEAST, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_CHIRP, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.EARTH, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_FAR, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.ELDRITCH, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_MALL, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.MAN, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_MELLOHI, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.CRAFT, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_STAL, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.DARKNESS, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_STRAD,
         new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.MECHANISM, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_WARD, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.MAGIC, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_BLOCKS, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.TOOL, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_WAIT, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.TRAP, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_OTHERSIDE,
         new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.VOID, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_RELIC, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.MAGIC, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_5, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MUSIC_DISC_PIGSTEP, new AspectList().add(Aspect.SENSES, 4).add(Aspect.AIR, 4).add(Aspect.BEAST, 4).add(Aspect.GREED, 4)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.NETHER_STAR, new AspectList().add(Aspect.ELDRITCH, 8).add(Aspect.MAGIC, 8).add(Aspect.ORDER, 8).add(Aspect.LIGHT, 8)
      );
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.CHAINMAIL_HELMET, new AspectList().add(Aspect.METAL, 8));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.CHAINMAIL_CHESTPLATE, new AspectList().add(Aspect.METAL, 12));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.CHAINMAIL_LEGGINGS, new AspectList().add(Aspect.METAL, 11));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.CHAINMAIL_BOOTS, new AspectList().add(Aspect.METAL, 7));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.PAPER, new AspectList().add(Aspect.MIND, 1));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.BOOK, new AspectList().add(Aspect.MIND, 3));
      ThaumcraftApi.registerDerivedObjectTag(net.minecraft.world.item.Items.ENCHANTED_BOOK, net.minecraft.world.item.Items.BOOK, aspects -> aspects);
      ThaumcraftApi.registerComplexObjectTag(Blocks.BOOKSHELF, new AspectList().add(Aspect.MIND, 8));
      ThaumcraftApi.registerObjectTag(Blocks.DRAGON_EGG, new AspectList().add(Aspect.ELDRITCH, 8).add(Aspect.BEAST, 8).add(Aspect.MAGIC, 8));
      ThaumcraftApi.registerObjectTag(Blocks.NETHER_PORTAL, new AspectList().add(Aspect.FIRE, 4).add(Aspect.TRAVEL, 4));
      ThaumcraftApi.registerObjectTag(Blocks.END_PORTAL, new AspectList().add(Aspect.ELDRITCH, 4).add(Aspect.TRAVEL, 4));
      ThaumcraftApi.registerObjectTag(Blocks.END_PORTAL_FRAME, new AspectList().add(Aspect.ELDRITCH, 4).add(Aspect.MECHANISM, 4).add(Aspect.TRAVEL, 4));
      ThaumcraftApi.registerObjectTag(Blocks.SPAWNER, new AspectList().add(Aspect.BEAST, 4).add(Aspect.TRAVEL, 4).add(Aspect.UNDEAD, 4).add(Aspect.MAGIC, 4));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.ENDER_EYE, new AspectList().add(Aspect.SENSES, 4));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.ARROW, new AspectList().add(Aspect.WEAPON, 1));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.GLASS_BOTTLE, new AspectList().add(Aspect.VOID, 1));
      ThaumcraftApi.registerComplexObjectTag(Items.CHESTS_WOODEN, Blocks.CHEST, new AspectList().add(Aspect.VOID, 4));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.GOLDEN_APPLE, new AspectList().add(Aspect.MAGIC, 2).add(Aspect.HEAL, 4));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE, new AspectList().add(Aspect.MAGIC, 4).add(Aspect.HEAL, 8));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.BOWL, new AspectList().add(Aspect.VOID, 1));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.MUSHROOM_STEW, new AspectList().add(Aspect.HUNGER, 4));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.MINECART, new AspectList().add(Aspect.MECHANISM, 2).add(Aspect.TRAVEL, 4));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.IRON_DOOR, new AspectList().add(Aspect.MECHANISM, 2).add(Aspect.MOTION, 1));
      ThaumcraftApi.registerComplexObjectTag(
         ItemTags.WOODEN_DOORS, net.minecraft.world.item.Items.OAK_DOOR, new AspectList().add(Aspect.MECHANISM, 1).add(Aspect.MOTION, 1)
      );
      ThaumcraftApi.registerComplexObjectTag(
         ItemTags.BOATS, net.minecraft.world.item.Items.OAK_BOAT, new AspectList().add(Aspect.WATER, 4).add(Aspect.TRAVEL, 4)
      );
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.FLINT_AND_STEEL, new AspectList().add(Aspect.FIRE, 4));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.FISHING_ROD, new AspectList().add(Aspect.WATER, 1).add(Aspect.TOOL, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.BUCKET, new AspectList().add(Aspect.METAL, 8).add(Aspect.VOID, 1));
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.WATER_BUCKET, new AspectList(net.minecraft.world.item.Items.BUCKET).add(Aspect.WATER, 4));
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.LAVA_BUCKET, new AspectList(net.minecraft.world.item.Items.BUCKET).add(Aspect.FIRE, 4).add(Aspect.EARTH, 1)
      );
      ThaumcraftApi.registerObjectTag(
         net.minecraft.world.item.Items.MILK_BUCKET,
         new AspectList(net.minecraft.world.item.Items.BUCKET).add(Aspect.HUNGER, 2).add(Aspect.HEAL, 2).add(Aspect.WATER, 2)
      );
      ThaumcraftApi.registerObjectTag(net.minecraft.world.item.Items.CAKE, new AspectList().add(Aspect.WATER, 4).add(Aspect.HUNGER, 4).add(Aspect.LIFE, 4));
      ThaumcraftApi.registerComplexObjectTag(Blocks.BREWING_STAND, new AspectList().add(Aspect.CRAFT, 2).add(Aspect.WATER, 2));
      ThaumcraftApi.registerComplexObjectTag(Blocks.STONE_BUTTON, new AspectList().add(Aspect.MECHANISM, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.RAIL, new AspectList().add(Aspect.METAL, 1).add(Aspect.TRAVEL, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.DETECTOR_RAIL, new AspectList().add(Aspect.MECHANISM, 1).add(Aspect.SENSES, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.POWERED_RAIL, new AspectList().add(Aspect.MECHANISM, 1).add(Aspect.ENERGY, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.ACTIVATOR_RAIL, new AspectList().add(Aspect.MECHANISM, 2));
      ThaumcraftApi.registerComplexObjectTag(ItemTags.WOODEN_FENCES, Blocks.OAK_FENCE_GATE, new AspectList().add(Aspect.MECHANISM, 1).add(Aspect.TRAVEL, 1));
      ThaumcraftApi.registerComplexObjectTag(
         ItemTags.WOODEN_PRESSURE_PLATES, Blocks.OAK_PRESSURE_PLATE, new AspectList().add(Aspect.MECHANISM, 1).add(Aspect.SENSES, 1)
      );
      ThaumcraftApi.registerComplexObjectTag(Blocks.STONE_PRESSURE_PLATE, new AspectList().add(Aspect.MECHANISM, 1).add(Aspect.SENSES, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, new AspectList().add(Aspect.MECHANISM, 1).add(Aspect.SENSES, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, new AspectList().add(Aspect.MECHANISM, 1).add(Aspect.SENSES, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.LEVER, new AspectList().add(Aspect.MECHANISM, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.REDSTONE_TORCH, new AspectList().add(Aspect.MECHANISM, 2));
      ThaumcraftApi.registerComplexObjectTag(Blocks.PISTON, new AspectList().add(Aspect.MECHANISM, 2).add(Aspect.MOTION, 4));
      ThaumcraftApi.registerComplexObjectTag(Blocks.STICKY_PISTON, new AspectList().add(Aspect.MECHANISM, 2).add(Aspect.MOTION, 4));
      ThaumcraftApi.registerComplexObjectTag(Blocks.JUKEBOX, new AspectList().add(Aspect.SENSES, 4).add(Aspect.MECHANISM, 2).add(Aspect.AIR, 4));
      ThaumcraftApi.registerComplexObjectTag(Blocks.NOTE_BLOCK, new AspectList().add(Aspect.SENSES, 4).add(Aspect.MECHANISM, 1).add(Aspect.AIR, 4));
      ThaumcraftApi.registerComplexObjectTag(ItemTags.WOODEN_TRAPDOORS, Blocks.OAK_TRAPDOOR, new AspectList().add(Aspect.MOTION, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.BLAST_FURNACE, new AspectList().add(Aspect.FIRE, 4));
      ThaumcraftApi.registerComplexObjectTag(Blocks.FURNACE, new AspectList().add(Aspect.FIRE, 2));
      ThaumcraftApi.registerComplexObjectTag(Blocks.ENCHANTING_TABLE, new AspectList().add(Aspect.MAGIC, 8).add(Aspect.CRAFT, 4));
      ThaumcraftApi.registerObjectTag(Blocks.CRAFTING_TABLE, new AspectList().add(Aspect.CRAFT, 4));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.CLOCK, new AspectList().add(Aspect.MECHANISM, 2));
      ThaumcraftApi.registerObjectTag(ItemTags.ANVIL, new AspectList().add(Aspect.METAL, 64).add(Aspect.CRAFT, 2).add(Aspect.TOOL, 2));
      ThaumcraftApi.registerComplexObjectTag(Blocks.BEACON, new AspectList().add(Aspect.AURA, 2).add(Aspect.MAGIC, 2).add(Aspect.EXCHANGE, 2));
      ThaumcraftApi.registerComplexObjectTag(ItemTags.WOODEN_BUTTONS, Blocks.OAK_BUTTON, new AspectList().add(Aspect.MECHANISM, 1));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.CARROT_ON_A_STICK, new AspectList().add(Aspect.TRAVEL, 4));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.FLOWER_POT, new AspectList().add(Aspect.VOID, 1).add(Aspect.PLANT, 1));
      ThaumcraftApi.registerComplexObjectTag(net.minecraft.world.item.Items.GOLDEN_CARROT, new AspectList().add(Aspect.SENSES, 2));
      ThaumcraftApi.registerComplexObjectTag(Blocks.ENDER_CHEST, new AspectList().merge(Aspect.EXCHANGE, 2).merge(Aspect.TRAVEL, 2).merge(Aspect.VOID, 4));
      ThaumcraftApi.registerComplexObjectTag(Blocks.HOPPER, new AspectList().merge(Aspect.MECHANISM, 1).merge(Aspect.EXCHANGE, 1).merge(Aspect.VOID, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.DROPPER, new AspectList().merge(Aspect.MECHANISM, 1).merge(Aspect.EXCHANGE, 1).merge(Aspect.VOID, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.DISPENSER, new AspectList().merge(Aspect.MECHANISM, 1).merge(Aspect.EXCHANGE, 1).merge(Aspect.VOID, 1));
      ThaumcraftApi.registerObjectTag(Blocks.TRIPWIRE_HOOK, new AspectList().add(Aspect.SENSES, 1).add(Aspect.MECHANISM, 1).add(Aspect.TRAP, 1));
      ThaumcraftApi.registerComplexObjectTag(
         Blocks.DAYLIGHT_DETECTOR, new AspectList().merge(Aspect.SENSES, 2).merge(Aspect.LIGHT, 3).merge(Aspect.MECHANISM, 3)
      );
      ThaumcraftApi.registerComplexObjectTag((ItemLike)ModItems.TABLE.get(), new AspectList().add(Aspect.TOOL, 1));
      ThaumcraftApi.registerDerivedObjectTag((ItemLike)ModItems.ARCANE_WORKBENCH.get(), (ItemLike)ModItems.TABLE.get(), aspects -> aspects.add(Aspect.CRAFT, 4));
      ThaumcraftApi.registerObjectTag(
         ModItems.COPPER_CLUSTER.get(), new AspectList().add(Aspect.ORDER, 1).add(Aspect.METAL, 5).add(Aspect.EARTH, 1).add(Aspect.EXCHANGE, 2)
      );
      ThaumcraftApi.registerObjectTag(
         ModItems.TIN_CLUSTER.get(), new AspectList().add(Aspect.ORDER, 1).add(Aspect.METAL, 5).add(Aspect.EARTH, 1).add(Aspect.CRYSTAL, 2)
      );
      ThaumcraftApi.registerObjectTag(
         ModItems.SILVER_CLUSTER.get(), new AspectList().add(Aspect.ORDER, 1).add(Aspect.METAL, 5).add(Aspect.EARTH, 1).add(Aspect.GREED, 2)
      );
      ThaumcraftApi.registerObjectTag(
         ModItems.LEAD_CLUSTER.get(), new AspectList().add(Aspect.ORDER, 1).add(Aspect.METAL, 5).add(Aspect.EARTH, 1).add(Aspect.ORDER, 2)
      );
      ThaumcraftApi.registerObjectTag(ModItems.IRON_CLUSTER.get(), new AspectList().add(Aspect.ORDER, 1).add(Aspect.METAL, 6).add(Aspect.EARTH, 1));
      ThaumcraftApi.registerObjectTag(
         ModItems.GOLD_CLUSTER.get(), new AspectList().add(Aspect.ORDER, 1).add(Aspect.METAL, 4).add(Aspect.EARTH, 1).add(Aspect.GREED, 2)
      );
      ThaumcraftApi.registerObjectTag(
         ModItems.CINNABAR_CLUSTER.get(),
         new AspectList().add(Aspect.ORDER, 1).add(Aspect.METAL, 4).add(Aspect.EARTH, 1).add(Aspect.EXCHANGE, 4).add(Aspect.POISON, 2)
      );
      ThaumcraftApi.registerObjectTag(ModItems.QUICKSILVER_NUGGET.get(), new AspectList().add(Aspect.METAL, 1));
      ThaumcraftApi.registerObjectTag(ModItems.THAUMIUM_NUGGET.get(), new AspectList().add(Aspect.METAL, 1));
      ThaumcraftApi.registerObjectTag(
         ModItems.CINNABAR_ORE.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.METAL, 2).add(Aspect.EXCHANGE, 2).add(Aspect.POISON, 1)
      );
      ThaumcraftApi.registerObjectTag(ModItems.AMBER_ORE.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.TRAP, 3).add(Aspect.CRYSTAL, 2));
      ThaumcraftApi.registerObjectTag(ModItems.AIR_SHARD_ORE.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.AIR, 3).add(Aspect.CRYSTAL, 2));
      ThaumcraftApi.registerObjectTag(ModItems.FIRE_SHARD_ORE.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.FIRE, 3).add(Aspect.CRYSTAL, 2));
      ThaumcraftApi.registerObjectTag(ModItems.WATER_SHARD_ORE.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.WATER, 3).add(Aspect.CRYSTAL, 2));
      ThaumcraftApi.registerObjectTag(ModItems.EARTH_SHARD_ORE.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.EARTH, 3).add(Aspect.CRYSTAL, 2));
      ThaumcraftApi.registerObjectTag(ModItems.ORDER_SHARD_ORE.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.ORDER, 3).add(Aspect.CRYSTAL, 2));
      ThaumcraftApi.registerObjectTag(ModItems.ENTROPY_SHARD_ORE.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 3).add(Aspect.CRYSTAL, 2));
      ThaumcraftApi.registerObjectTag(ModItems.TAINT_CRUST.get(), new AspectList().add(Aspect.TREE, 1).add(Aspect.TAINT, 3));
      ThaumcraftApi.registerObjectTag(ModItems.TAINT_SOIL.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.TAINT, 3));
      ThaumcraftApi.registerObjectTag(ModItems.FIBROUS_TAINT.get(), new AspectList().add(Aspect.LIFE, 1).add(Aspect.TAINT, 2));
      ThaumcraftApi.registerObjectTag(ModItems.TAINTED_GRASS.get(), new AspectList().add(Aspect.PLANT, 1).add(Aspect.TAINT, 1));
      ThaumcraftApi.registerObjectTag(ModItems.TAINTED_PLANT.get(), new AspectList().add(Aspect.PLANT, 1).add(Aspect.TAINT, 1));
      ThaumcraftApi.registerObjectTag(ModItems.SPORE_STALK.get(), new AspectList().add(Aspect.BEAST, 1).add(Aspect.PLANT, 1).add(Aspect.TAINT, 2));
      ThaumcraftApi.registerObjectTag(ModItems.MATURE_SPORE_STALK.get(), new AspectList().add(Aspect.BEAST, 1).add(Aspect.PLANT, 1).add(Aspect.TAINT, 2));
      ThaumcraftApi.registerObjectTag(ModItems.OBSIDIAN_TOTEM.get(), new AspectList().add(Aspect.EARTH, 4).add(Aspect.DARKNESS, 2).add(Aspect.ELDRITCH, 2));
      ThaumcraftApi.registerObjectTag(ModItems.GREATWOOD_LOG.get(), new AspectList().add(Aspect.TREE, 3).add(Aspect.MAGIC, 1));
      ThaumcraftApi.registerObjectTag(ModItems.SILVERWOOD_LOG.get(), new AspectList().add(Aspect.TREE, 3).add(Aspect.MAGIC, 1).add(Aspect.ORDER, 1));
      ThaumcraftApi.registerObjectTag(ModItems.GREATWOOD_LEAVES.get(), new AspectList().add(Aspect.PLANT, 1));
      ThaumcraftApi.registerObjectTag(ModItems.SILVERWOOD_LEAVES.get(), new AspectList().add(Aspect.PLANT, 1));
      ThaumcraftApi.registerObjectTag(ModItems.GREATWOOD_SAPLING.get(), new AspectList().add(Aspect.PLANT, 2).add(Aspect.TREE, 1).add(Aspect.MAGIC, 1));
      ThaumcraftApi.registerObjectTag(ModItems.SILVERWOOD_SAPLING.get(), new AspectList().add(Aspect.PLANT, 2).add(Aspect.TREE, 1).add(Aspect.MAGIC, 1));
      ThaumcraftApi.registerObjectTag(ModItems.SHIMMER_LEAF.get(), new AspectList().add(Aspect.PLANT, 2).add(Aspect.EXCHANGE, 2).add(Aspect.MAGIC, 2));
      ThaumcraftApi.registerObjectTag(ModItems.CINDER_PEARL.get(), new AspectList().add(Aspect.PLANT, 2).add(Aspect.FIRE, 2).add(Aspect.MAGIC, 2));
      ThaumcraftApi.registerObjectTag(ModItems.VISHROOM.get(), new AspectList().add(Aspect.PLANT, 2).add(Aspect.POISON, 1).add(Aspect.MAGIC, 2));
      ThaumcraftApi.registerObjectTag(ModItems.ARCANE_STONE.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.MAGIC, 1));
      ThaumcraftApi.registerObjectTag(ModItems.ARCANE_BRICKS.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.MAGIC, 1));
      ThaumcraftApi.registerObjectTag(ModItems.QUICKSILVER.get(), new AspectList().add(Aspect.METAL, 3).add(Aspect.POISON, 1).add(Aspect.EXCHANGE, 2));
      ThaumcraftApi.registerObjectTag(ModItems.ZOMBIE_BRAIN.get(), new AspectList().add(Aspect.FLESH, 2).add(Aspect.MIND, 4).add(Aspect.UNDEAD, 2));
      ThaumcraftApi.registerObjectTag(ModItems.AMBER.get(), new AspectList().add(Aspect.TRAP, 2).add(Aspect.CRYSTAL, 2));
      ThaumcraftApi.registerObjectTag(ModItems.KNOWLEDGE_FRAGMENT.get(), new AspectList().add(Aspect.MIND, 8));
      ThaumcraftApi.registerObjectTag(ModItems.TAINT_SLIME.get(), new AspectList().add(Aspect.TAINT, 3).add(Aspect.SLIME, 1));
      ThaumcraftApi.registerObjectTag(ModItems.TAINT_TENDRIL.get(), new AspectList().add(Aspect.TAINT, 2).add(Aspect.GREED, 1).add(Aspect.HUNGER, 1));
      ThaumcraftApi.registerObjectTag(ModItems.GOLD_COIN.get(), new AspectList().add(Aspect.GREED, 1));
      ThaumcraftApi.registerObjectTag(ModItems.LOOT_BAG.get(), new AspectList().add(Aspect.GREED, 8));
      ThaumcraftApi.registerObjectTag(ModItems.LOOT_BAG_UNCOMMON.get(), new AspectList().add(Aspect.GREED, 16));
      ThaumcraftApi.registerObjectTag(ModItems.LOOT_BAG_RARE.get(), new AspectList().add(Aspect.GREED, 32));
      ThaumcraftApi.registerObjectTag(ModItems.BEEF_NUGGET.get(), new AspectList().add(Aspect.HUNGER, 1));
      ThaumcraftApi.registerObjectTag(ModItems.CHICKEN_NUGGET.get(), new AspectList().add(Aspect.HUNGER, 1));
      ThaumcraftApi.registerObjectTag(ModItems.PORK_NUGGET.get(), new AspectList().add(Aspect.HUNGER, 1));
      ThaumcraftApi.registerObjectTag(ModItems.FISH_NUGGET.get(), new AspectList().add(Aspect.HUNGER, 1));
      ThaumcraftApi.registerComplexObjectTag((ItemLike)ModItems.TRIPLE_MEAT_TREAT.get(), new AspectList().add(Aspect.HEAL, 1).remove(Aspect.HUNGER, 1));
      ThaumcraftApi.registerObjectTag(ModItems.AIR_SHARD.get(), new AspectList().add(Aspect.MAGIC, 1).add(Aspect.AIR, 2).add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(ModItems.FIRE_SHARD.get(), new AspectList().add(Aspect.MAGIC, 1).add(Aspect.FIRE, 2).add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(ModItems.WATER_SHARD.get(), new AspectList().add(Aspect.MAGIC, 1).add(Aspect.WATER, 2).add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(ModItems.EARTH_SHARD.get(), new AspectList().add(Aspect.MAGIC, 1).add(Aspect.EARTH, 2).add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(ModItems.ORDER_SHARD.get(), new AspectList().add(Aspect.MAGIC, 1).add(Aspect.ORDER, 2).add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(ModItems.ENTROPY_SHARD.get(), new AspectList().add(Aspect.MAGIC, 1).add(Aspect.ENTROPY, 2).add(Aspect.CRYSTAL, 1));
      ThaumcraftApi.registerObjectTag(
         ModItems.CANDLE.get(DyeColor.WHITE).get(), new AspectList().add(Aspect.LIGHT, 2).add(Aspect.FLESH, 1).add(Aspect.MAGIC, 1)
      );
      ThaumcraftApi.registerObjectTag(ModBlocks.GLIMMER_OF_LIGHT.get(), new AspectList().add(Aspect.LIGHT, 1));
      registerThaumcraftParityOverrides();
      ThaumcraftApi.registerObjectTag(ModItems.PHIAL_ESSENCE.get(), new AspectList().add(Aspect.VOID, 1));
      ThaumcraftApi.registerObjectTag(ModItems.WISP_ESSENCE.get(), new AspectList().add(Aspect.AURA, 2));
      ThaumcraftApi.registerObjectTag(ModItems.CRYSTAL_ESSENCE.get(), new AspectList());
      ThaumcraftApi.registerComplexObjectTag((ItemLike)ModItems.GOGGLES_REVEALING.get(), new AspectList().add(Aspect.SENSES, 4));
      ThaumcraftApi.registerComplexObjectTag((ItemLike)ModItems.ARCANE_SENSOR.get(), new AspectList().add(Aspect.SENSES, 4));
      ThaumcraftApi.registerObjectTag(ModItems.CURIOS_RING_IRON.get(), new AspectList().add(Aspect.MAGIC, 5));
      ThaumcraftApi.registerObjectTag(
         ModItems.FOCUS_PECH.get(),
         new AspectList().add(Aspect.MAGIC, 5).add(Aspect.POISON, 5).add(Aspect.ENTROPY, 5).add(Aspect.ELDRITCH, 5).add(Aspect.WEAPON, 5)
      );
      ThaumcraftApi.registerObjectTag(ModItems.CHEST_CULTIST_PLATE.get(), new AspectList().add(Aspect.METAL, 5).add(Aspect.ELDRITCH, 1));
      ThaumcraftApi.registerObjectTag(ModItems.CHEST_CULTIST_ROBE.get(), new AspectList().add(Aspect.METAL, 3).add(Aspect.CLOTH, 2).add(Aspect.ELDRITCH, 1));
      ThaumcraftApi.registerObjectTag(ModItems.CHEST_CULTIST_LEADER.get(), new AspectList().add(Aspect.METAL, 5).add(Aspect.ELDRITCH, 2));
      ThaumcraftApi.registerObjectTag(ModItems.LEGGINGS_CULTIST_PLATE.get(), new AspectList().add(Aspect.METAL, 5).add(Aspect.ELDRITCH, 1));
      ThaumcraftApi.registerObjectTag(ModItems.LEGGINGS_CULTIST_LEADER.get(), new AspectList().add(Aspect.METAL, 5).add(Aspect.ELDRITCH, 2));
      ThaumcraftApi.registerObjectTag(ModItems.LEGGINGS_CULTIST_ROBE.get(), new AspectList().add(Aspect.METAL, 3).add(Aspect.CLOTH, 2).add(Aspect.ELDRITCH, 1));
      ThaumcraftApi.registerObjectTag(ModItems.HELMET_CULTIST_PLATE.get(), new AspectList().add(Aspect.METAL, 5).add(Aspect.ELDRITCH, 1));
      ThaumcraftApi.registerObjectTag(ModItems.HELMET_CULTIST_LEADER.get(), new AspectList().add(Aspect.METAL, 5).add(Aspect.ELDRITCH, 2));
      ThaumcraftApi.registerObjectTag(ModItems.HELMET_CULTIST_ROBE.get(), new AspectList().add(Aspect.METAL, 3).add(Aspect.CLOTH, 2).add(Aspect.ELDRITCH, 1));
      ThaumcraftApi.registerObjectTag(ModItems.BOOTS_CULTIST.get(), new AspectList().add(Aspect.METAL, 4).add(Aspect.ELDRITCH, 1));
      ThaumcraftApi.registerObjectTag(ModItems.CRIMSON_CULT_BANNER.get(), new AspectList().add(Aspect.ELDRITCH, 1).add(Aspect.TREE, 2).add(Aspect.CLOTH, 3));
      ThaumcraftApi.registerObjectTag(
         ModItems.ELDRITCH_EYE.get(),
         new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.AURA, 3).add(Aspect.MAGIC, 3).add(Aspect.SENSES, 3).add(Aspect.SOUL, 3)
      );
      ThaumcraftApi.registerObjectTag(
         ModItems.CRIMSON_RITES.get(), new AspectList().add(Aspect.MIND, 5).add(Aspect.MAGIC, 3).add(Aspect.ELDRITCH, 3).add(Aspect.SOUL, 3)
      );
      ThaumcraftApi.registerObjectTag(ModItems.RUNE_TABLET.get(), new AspectList().add(Aspect.TRAP, 4).add(Aspect.MIND, 4).add(Aspect.MECHANISM, 4));
      ThaumcraftApi.registerObjectTag(
         ModItems.PRIMORDIAL_PEARL.get(),
         new AspectList().add(Aspect.AIR, 16).add(Aspect.EARTH, 16).add(Aspect.FIRE, 16).add(Aspect.WATER, 16).add(Aspect.ORDER, 16).add(Aspect.ENTROPY, 16)
      );
      registerEldritchStructureParityOverrides();
      ThaumcraftApi.registerObjectTag(ModItems.GLOWING_CRUSTED_STONE.get(), new AspectList().add(Aspect.LIGHT, 1).add(Aspect.EARTH, 1).add(Aspect.ELDRITCH, 1));
      ThaumcraftApi.registerObjectTag(ModItems.GLYPHED_STONE.get(), new AspectList().add(Aspect.MIND, 2).add(Aspect.EARTH, 1).add(Aspect.ELDRITCH, 1));
      ThaumcraftApi.registerObjectTag(ModItems.ANCIENT_STONE.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.ELDRITCH, 1));
      ThaumcraftApi.registerObjectTag(ModItems.ANCIENT_ROCK.get(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.ELDRITCH, 1));
   }

   private static void registerLegacyVanillaParityOverrides() {
      ThaumcraftApi.registerObjectTag(Blocks.INFESTED_STONE, new AspectList().add(Aspect.EARTH, 2).add(Aspect.BEAST, 1).add(Aspect.TRAP, 1));
      ThaumcraftApi.registerObjectTag(Blocks.INFESTED_COBBLESTONE, new AspectList().add(Aspect.EARTH, 2).add(Aspect.BEAST, 1).add(Aspect.TRAP, 1));
      ThaumcraftApi.registerObjectTag(Blocks.INFESTED_STONE_BRICKS, new AspectList().add(Aspect.EARTH, 2).add(Aspect.BEAST, 1).add(Aspect.TRAP, 1));
      ThaumcraftApi.registerObjectTag(Blocks.INFESTED_MOSSY_STONE_BRICKS, new AspectList().add(Aspect.EARTH, 2).add(Aspect.BEAST, 1).add(Aspect.TRAP, 1));
      ThaumcraftApi.registerObjectTag(Blocks.INFESTED_CRACKED_STONE_BRICKS, new AspectList().add(Aspect.EARTH, 2).add(Aspect.BEAST, 1).add(Aspect.TRAP, 1));
      ThaumcraftApi.registerObjectTag(Blocks.INFESTED_CHISELED_STONE_BRICKS, new AspectList().add(Aspect.EARTH, 2).add(Aspect.BEAST, 1).add(Aspect.TRAP, 1));
      ThaumcraftApi.registerObjectTag(Blocks.INFESTED_DEEPSLATE, new AspectList().add(Aspect.EARTH, 2).add(Aspect.BEAST, 1).add(Aspect.TRAP, 1));
      ThaumcraftApi.registerObjectTag(ItemTags.STONE_BRICKS, new AspectList().add(Aspect.EARTH, 2));
      ThaumcraftApi.registerObjectTag(Blocks.MOSSY_STONE_BRICKS, new AspectList(Blocks.STONE_BRICKS).remove(Aspect.EARTH, 1).add(Aspect.PLANT, 1));
      ThaumcraftApi.registerObjectTag(Blocks.CRACKED_STONE_BRICKS, new AspectList(Blocks.STONE_BRICKS).remove(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1));
      ThaumcraftApi.registerObjectTag(Blocks.CHISELED_STONE_BRICKS, new AspectList(Blocks.STONE_BRICKS).remove(Aspect.EARTH, 1).add(Aspect.ORDER, 1));
      ThaumcraftApi.registerComplexObjectTag(Blocks.SANDSTONE, new AspectList().add(Aspect.EARTH, 1).remove(Aspect.EARTH, 1));
      ThaumcraftApi.registerObjectTag(Blocks.CHISELED_SANDSTONE, new AspectList(Blocks.SANDSTONE).remove(Aspect.EARTH, 1).add(Aspect.MAGIC, 1));
      ThaumcraftApi.registerObjectTag(Blocks.SMOOTH_SANDSTONE, new AspectList(Blocks.SANDSTONE).remove(Aspect.EARTH, 1).add(Aspect.ORDER, 1));
      ThaumcraftApi.registerObjectTag(Blocks.TALL_GRASS, new AspectList().add(Aspect.PLANT, 1).add(Aspect.AIR, 1));
      ThaumcraftApi.registerObjectTag(Blocks.LARGE_FERN, new AspectList().add(Aspect.PLANT, 1).add(Aspect.AIR, 1));
      ThaumcraftApi.registerObjectTag(Blocks.SUNFLOWER, new AspectList().add(Aspect.PLANT, 1).add(Aspect.AIR, 1));
      ThaumcraftApi.registerObjectTag(Blocks.LILAC, new AspectList().add(Aspect.PLANT, 1).add(Aspect.AIR, 1));
      ThaumcraftApi.registerObjectTag(Blocks.ROSE_BUSH, new AspectList().add(Aspect.PLANT, 1).add(Aspect.AIR, 1));
      ThaumcraftApi.registerObjectTag(Blocks.PEONY, new AspectList().add(Aspect.PLANT, 1).add(Aspect.AIR, 1));
      ThaumcraftApi.registerObjectTag(Blocks.LILY_PAD, new AspectList().add(Aspect.PLANT, 2).add(Aspect.WATER, 1));
   }

   private static void registerThaumcraftParityOverrides() {
      ThaumcraftApi.registerDerivedObjectTag(
         (ItemLike)ModItems.SALIS_MUNDUS.get(), (ItemLike)ModItems.BALANCED_SHARD.get(), aspects -> aspects.add(Aspect.MAGIC, 2).remove(Aspect.CRYSTAL)
      );
      ThaumcraftApi.registerDerivedObjectTag(
         (ItemLike)ModItems.CRUCIBLE.get(), net.minecraft.world.item.Items.CAULDRON, aspects -> aspects.add(Aspect.CRAFT, 4).add(Aspect.MAGIC, 4)
      );
      ThaumcraftApi.registerDerivedObjectTag(
         (ItemLike)ModItems.THAUMONOMICON.get(), Blocks.BOOKSHELF, aspects -> aspects.add(Aspect.MAGIC, 2).merge(Aspect.MIND, 2)
      );
      ThaumcraftApi.registerObjectTag(ModBlocks.ADVANCED_ALCHEMY_FURNACE.get(), new AspectList().add(Aspect.MAGIC, 8).add(Aspect.WATER, 8).add(Aspect.CRAFT, 8));
   }

   private static void registerEldritchStructureParityOverrides() {
      AspectList eldritchStructureAspects = new AspectList().add(Aspect.VOID, 8).add(Aspect.ELDRITCH, 8).add(Aspect.SENSES, 4);
      ThaumcraftApi.registerObjectTag(
         Ingredient.of(
            new ItemLike[]{
               (ItemLike)ModItems.ELDRITCH_ALTAR.get(),
               (ItemLike)ModItems.ANCIENT_DOORWAY.get(),
               (ItemLike)ModItems.ANCIENT_LOCKING_MECHANISM.get(),
               (ItemLike)ModBlocks.ELDRITCH_CRAB_SPAWNER.get(),
               (ItemLike)ModItems.RUNED_STONE.get()
            }
         ),
         eldritchStructureAspects.copy()
      );
      ThaumcraftApi.registerObjectTag(ModBlocks.ELDRITCH_OBELISK.get(), eldritchStructureAspects.copy());
      ThaumcraftApi.registerObjectTag(ModBlocks.ELDRITCH_OBELISK_PART.get(), eldritchStructureAspects.copy());
      ThaumcraftApi.registerObjectTag(ModBlocks.ELDRITCH_TRAP.get(), eldritchStructureAspects.copy());
      ThaumcraftApi.registerObjectTag(ModBlocks.ELDRITCH_CRAB_SPAWNER.get(), eldritchStructureAspects.copy());
      ThaumcraftApi.registerObjectTag(ModBlocks.ELDRITCH_PORTAL.get(), new AspectList().add(Aspect.VOID, 8).add(Aspect.ELDRITCH, 8).add(Aspect.TRAVEL, 8));
   }

   private static TagKey<Item> forgeTag(String s) {
      return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", s));
   }

   private ModAspects() {
   }
}
