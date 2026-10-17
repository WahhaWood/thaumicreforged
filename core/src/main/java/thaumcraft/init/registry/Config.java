package thaumcraft.init.registry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.biome.Biome.TemperatureModifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags.Biomes;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.entities.brainless.aspect_orb.AspectOrbEntity;
import thaumcraft.entities.smart.cultist.EntityCultistCleric;
import thaumcraft.entities.smart.cultist.EntityCultistKnight;
import thaumcraft.entities.smart.cultist.EntityCultistLeader;
import thaumcraft.entities.smart.cultist_portal.EntityCultistPortal;
import thaumcraft.entities.smart.edlritch_crab.EntityEldritchCrab;
import thaumcraft.entities.smart.eldrich_guardian.EntityEldritchGuardian;
import thaumcraft.entities.smart.eldrich_warden.EntityEldritchWarden;
import thaumcraft.entities.smart.eldritch_golem.EntityEldritchGolem;
import thaumcraft.entities.smart.pech.EntityPech;
import thaumcraft.entities.smart.taint.spore.EntityTaintSpore;
import thaumcraft.entities.smart.wisp.EntityWisp;
import thaumcraft.entities.smart.zombie.EntityInhabitedZombie;
import thaumcraft.util.CropUtils;
import thaumcraft.util.Utils;
import thaumcraft.world.biomes.BiomeHandler;

public class Config {
   public static final String CATEGORY_ENCH = "Enchantments";
   public static final String CATEGORY_ENTITIES = "Entities";
   public static final String CATEGORY_BIOMES = "Biomes";
   public static final String CATEGORY_RESEARCH = "Research";
   public static final String CATEGORY_GEN = "World_Generation";
   public static final String CATEGORY_REGEN = "World_Regeneration";
   public static final String CATEGORY_SPAWN = "Monster_Spawning";
   public static final String CATEGORY_RUNIC = "Runic_Shielding";
   public static int biomeTaintID = 192;
   public static int biomeMagicalForestID = 193;
   public static int biomeEerieID = 194;
   public static int biomeEldritchID = 195;
   public static int biomeTaintWeight = 2;
   public static int biomeMagicalForestWeight = 2;
   public static int taintSpreadRate = 200;
   public static boolean taintFromFlux = true;
   public static boolean hardNode = true;
   public static boolean wuss = false;
   public static int dimensionOuterId = -42;
   public static boolean championMobs = true;
   public static int shieldRecharge = 2000;
   public static int shieldWait = 80;
   public static int shieldCost = 50;
   public static boolean colorBlind = false;
   public static boolean shaders = true;
   public static boolean crooked = true;
   public static boolean showTags = false;
   public static boolean blueBiome = false;
   public static boolean allowMirrors = true;
   public static boolean auraNodeDebug = true;
   public static boolean enableKami = true;
   public static boolean dialBottom = false;
   public static int nodeRefresh = 10;
   public static final float auraSize = 4.0F;
   public static boolean genAura = true;
   public static boolean genStructure = true;
   public static boolean genCinnibar = true;
   public static boolean genAmber = true;
   public static boolean genInfusedStone = true;
   public static boolean genTrees = true;
   public static boolean genTaint = true;
   public static boolean regenAura = false;
   public static boolean regenStructure = false;
   public static boolean regenCinnibar = false;
   public static boolean regenAmber = false;
   public static boolean regenInfusedStone = false;
   public static boolean regenTrees = false;
   public static boolean regenTaint = false;
   public static String regenKey = "DEFAULT";
   public static boolean wardedStone = true;
   public static boolean golemChestInteract = true;
   public static int nodeRarity = 36;
   public static int specialNodeRarity = 18;
   public static int notificationDelay = 5000;
   public static int notificationMax = 15;
   public static boolean glowyTaint = true;
   public static int researchDifficulty = 0;
   public static int aspectTotalCap = 100;
   public static int golemDelay = 5;
   public static int golemIgnoreDelay = 10000;
   public static int golemLinkQuality = 16;
   public static boolean CwardedStone = true;
   public static boolean CallowMirrors = true;
   public static boolean CauraNodeDebug = true;
   public static boolean CenableKami = true;
   public static boolean ChardNode = true;
   public static boolean Cwuss = false;
   public static int CresearchDifficulty = 0;
   public static int CaspectTotalCap = 100;
   public static boolean spawnAngryZombie = true;
   public static boolean spawnFireBat = true;
   public static boolean spawnTaintacle = true;
   public static boolean spawnWisp = true;
   public static boolean spawnTaintSpore = true;
   public static boolean spawnPech = true;
   public static boolean spawnElder = true;
   public static ArrayList<Aspect> aspectOrder = new ArrayList<>();
   public static boolean allowReplaceDirtInRegions = false;
   public static boolean allowSetLightInRegions = false;
   public static boolean enableGenLegsLight = true;
   public static boolean enableIchorPickAdvAllBreaking = false;
   public static boolean enableIchorPickAdvBedrockBreaking = false;
   public static float enderCrystalDropChance = 1.0F;
   public static boolean enderCrystalDropCheckWorld = true;
   public static int ichorAxeMaxBlocks = 64;
   public static float netherCrystalDropChance = 1.0F;
   public static boolean netherCrystalDropCheckWorld = true;
   public static boolean robeStratosphereProjectileProtect = true;
   public static boolean summonerDenyDropAspect = false;
   public static final Config.ItemBlockList transvectorBlackList = new Config.ItemBlockList();
   public static boolean transvectorDenyInventory = true;
   public static boolean transvectorDenySameBlock = true;
   public static boolean transvectorEnableBlackList = false;
   public static boolean transvectorEnableWhiteList = false;
   public static final Config.ItemBlockList transvectorWhiteList = new Config.ItemBlockList();
   public static boolean blockEldritchNothing = true;
   public static boolean crucibleCheckRegions = true;
   public static boolean crucibleRightClickOnlyPlayer = true;
   public static boolean crucibleRightClickUse = true;
   public static boolean customChampionsNaming = false;
   public static boolean disableTransparentForWardedBlocks = true;
   public static boolean enableBlurredVision = true;
   public static boolean enableBottleTaint = true;
   public static boolean enableDeathGase = true;
   public static boolean enableFocusTrade = true;
   public static boolean enableFocusWarding = true;
   public static boolean enableInfectiousVisExhaust = true;
   public static boolean enablePlayerFluidDeathAspectDrop = true;
   public static boolean enableSunScorned = true;
   public static boolean enableThaumarhia = true;
   public static boolean enableUnhunger = true;
   public static boolean enableVisExhaust = true;
   public static boolean essenceReservoirCheckRegions = true;
   public static boolean fixInfinityMirrorTransportItem = true;
   public static boolean fluidPureUserCheckRegions = false;
   public static boolean fluxScrubberCheckRegions = false;
   public static boolean guardianCheckRegions = true;
   public static boolean infusionRecipeCheckPedestalsOnSearch = false;
   public static boolean learnsOneUse = true;
   public static boolean placeMultiBlockPartsWandOnly = true;
   public static boolean postItemCraftedEventForOffline = true;
   public static boolean protectCrucible = false;
   public static boolean removeBlockMagicBoxFromGame = true;
   public static boolean suddenlySpidersCheckRegions = true;
   public static boolean tryArcaneSmeltItemsToBonusIfRecipeNotFound = true;
   public static final Config.ItemBlockList focusTradePickBlackList = new Config.ItemBlockList();
   public static final Config.ItemBlockList focusTradeReplaceBlackList = new Config.ItemBlockList();
   public static final Config.ItemBlockList focusWardingBlackList = new Config.ItemBlockList();
   public static float fluidDeathAspectDropChance = 1.0F;
   public static float zombieBrainAspectChance = 20.0F;
   public static int alchemyFurnaceItemInteractDelay = 20;
   public static int deathGazeMaxRadius = 8;
   public static int elementalAxeFindDropRadius = 10;
   public static int fluxScrubberSkipTicks = 0;
   public static int lightSourceLifeTime = 600;
   public static int pureWaterBathSaltsCheckRegions = 1;
   public static int searchEssenceSourcesCoolDown = 5000;
   public static int suddenlySpidersSpawnMaxCount = 20;
   public static int visRelayUpdatePeriod = 0;

   public static void initialize(File file) {
   }

   public static void save() {
   }

   public static void syncConfigurable() {
   }

   /**
    * Регистрация таблиц добычи: содержимое мешков с лутом и сундуки данжей,
    * шахт, пирамид и крепостей. В 1.7.10 этот код жил здесь же, в {@code Config},
    * поэтому и заглушка осталась на этом месте.
    */
   public static void initLoot() {
      ModLoot.init();
   }

   public static void initModCompatibility() {
      Thaumcraft.log.info("Adding entities to MFR safari net blacklist.");
      registerSafariNetBlacklist(AspectOrbEntity.class);
      registerSafariNetBlacklist(EntityWisp.class);
      registerSafariNetBlacklist(EntityPech.class);
      registerSafariNetBlacklist(EntityTaintSpore.class);
      registerSafariNetBlacklist(EntityEldritchGuardian.class);
      registerSafariNetBlacklist(EntityEldritchWarden.class);
      registerSafariNetBlacklist(EntityEldritchGolem.class);
      registerSafariNetBlacklist(EntityCultistCleric.class);
      registerSafariNetBlacklist(EntityCultistKnight.class);
      registerSafariNetBlacklist(EntityCultistLeader.class);
      registerSafariNetBlacklist(EntityCultistPortal.class);
      registerSafariNetBlacklist(EntityEldritchCrab.class);
      registerSafariNetBlacklist(EntityInhabitedZombie.class);
   }

   public static void registerSafariNetBlacklist(Class<?> clazz) {
      try {
         Class<?> forName = Class.forName("powercrystals.minefactoryreloaded.MFRRegistry");
         if (forName != null) {
            forName.getMethod("registerSafariNetBlacklist", Class.class).invoke(forName, clazz);
         }
      } catch (Exception var2) {
      }
   }

   public static void initBiomeInfo() {
      TagKey<Biome> FOREST = BiomeTags.IS_FOREST;
      TagKey<Biome> TAIGA = BiomeTags.IS_TAIGA;
      TagKey<Biome> JUNGLE = BiomeTags.IS_JUNGLE;
      TagKey<Biome> SAVANNA = BiomeTags.IS_SAVANNA;
      TagKey<Biome> MOUNTAIN = BiomeTags.IS_MOUNTAIN;
      TagKey<Biome> OCEAN = BiomeTags.IS_OCEAN;
      TagKey<Biome> RIVER = BiomeTags.IS_RIVER;
      TagKey<Biome> BADLANDS = BiomeTags.IS_BADLANDS;
      TagKey<Biome> NETHER = BiomeTags.IS_NETHER;
      TagKey<Biome> END = BiomeTags.IS_END;
      TagKey<Biome> PLAINS = Biomes.IS_PLAINS;
      TagKey<Biome> SWAMP = Biomes.IS_SWAMP;
      TagKey<Biome> SANDY = Biomes.IS_SANDY;
      TagKey<Biome> SNOWY = Biomes.IS_SNOWY;
      TagKey<Biome> MUSHROOM = Biomes.IS_MUSHROOM;
      TagKey<Biome> CAVES = Biomes.IS_UNDERGROUND;
      TagKey<Biome> MAGICAL = BiomeHandler.biomeTag("thaumcraft", "magical_forest");
      BiomeHandler.registerTagInfo(OCEAN, 120, Aspect.WATER, true, 1.0F);
      BiomeHandler.registerTagInfo(RIVER, 100, Aspect.WATER, true, 0.8F);
      BiomeHandler.registerTagInfo(MUSHROOM, 150, Aspect.WATER, true, 1.5F);
      BiomeHandler.registerTagInfo(SANDY, 80, Aspect.FIRE, true, 0.2F);
      BiomeHandler.registerTagInfo(SAVANNA, 90, Aspect.FIRE, true, 0.4F);
      BiomeHandler.registerTagInfo(NETHER, 150, Aspect.FIRE, true, 0.1F);
      BiomeHandler.registerTagInfo(FOREST, 120, Aspect.EARTH, true, 1.0F);
      BiomeHandler.registerTagInfo(TAIGA, 100, Aspect.EARTH, true, 0.8F);
      BiomeHandler.registerTagInfo(JUNGLE, 130, Aspect.EARTH, true, 1.2F);
      BiomeHandler.registerTagInfo(BADLANDS, 90, Aspect.EARTH, true, 0.3F);
      BiomeHandler.registerTagInfo(CAVES, 100, Aspect.EARTH, true, 0.6F);
      BiomeHandler.registerTagInfo(MOUNTAIN, 110, Aspect.AIR, true, 0.8F);
      BiomeHandler.registerTagInfo(PLAINS, 100, Aspect.AIR, true, 0.8F);
      BiomeHandler.registerTagInfo(SNOWY, 90, Aspect.ORDER, true, 0.5F);
      BiomeHandler.registerTagInfo(SWAMP, 120, Aspect.ENTROPY, true, 0.2F);
      BiomeHandler.registerTagInfo(END, 100, Aspect.ENTROPY, true, 0.1F);
      BiomeHandler.registerTagInfo(MAGICAL, 100, null, true, 1.0F);
      BiomeHandler.registerPrecipInfo(Precipitation.SNOW, 80, Aspect.ORDER, false, 0.0F);
      BiomeHandler.registerTempInfo(TemperatureModifier.FROZEN, 100, Aspect.ORDER, false, 0.0F);
   }

   public static void initMisc() {
      CropUtils.addStandardCrop(new ItemStack(Blocks.MELON), 32767);
      CropUtils.addStandardCrop(new ItemStack(Blocks.PUMPKIN), 32767);
      CropUtils.addStandardCrop(new ItemStack((ItemLike)ModBlocks.MANA_BEAN.get()), 7);
      CropUtils.addStackedCrop(Blocks.SUGAR_CANE, 32767);
      CropUtils.addStackedCrop(Blocks.CACTUS, 32767);
      Utils.addSpecialMiningResult(new ItemStack((ItemLike)ModItems.CINNABAR_ORE.get()), new ItemStack((ItemLike)ModItems.CINNABAR_CLUSTER.get()), 1.0F);
      Utils.addSpecialMiningResult(new ItemStack(Blocks.IRON_ORE), new ItemStack((ItemLike)ModItems.IRON_CLUSTER.get()), 1.0F);
      Utils.addSpecialMiningResult(new ItemStack(Blocks.DEEPSLATE_IRON_ORE), new ItemStack((ItemLike)ModItems.IRON_CLUSTER.get()), 1.0F);
      addTaggedSpecialMiningResult("raw_materials/iron", (Item)ModItems.IRON_CLUSTER.get());
      Utils.addSpecialMiningResult(new ItemStack(Blocks.GOLD_ORE), new ItemStack((ItemLike)ModItems.GOLD_CLUSTER.get()), 0.9F);
      Utils.addSpecialMiningResult(new ItemStack(Blocks.DEEPSLATE_GOLD_ORE), new ItemStack((ItemLike)ModItems.GOLD_CLUSTER.get()), 0.9F);
      addTaggedSpecialMiningResult("raw_materials/gold", (Item)ModItems.GOLD_CLUSTER.get(), 0.9F);
      Utils.addSpecialMiningResult(new ItemStack(Blocks.COPPER_ORE), new ItemStack((ItemLike)ModItems.COPPER_CLUSTER.get()), 1.0F);
      Utils.addSpecialMiningResult(new ItemStack(Blocks.DEEPSLATE_COPPER_ORE), new ItemStack((ItemLike)ModItems.COPPER_CLUSTER.get()), 1.0F);
      addTaggedSpecialMiningResult("raw_materials/copper", (Item)ModItems.COPPER_CLUSTER.get());
      addTaggedSpecialMiningResult("raw_materials/tin", (Item)ModItems.TIN_CLUSTER.get());
      addTaggedSpecialMiningResult("raw_materials/lead", (Item)ModItems.LEAD_CLUSTER.get());
      addTaggedSpecialMiningResult("raw_materials/silver", (Item)ModItems.SILVER_CLUSTER.get());
      initSmeltingBonuses();
   }

   /**
    * TC4 ConfigRecipes: what the infernal furnace may throw out besides the smelting result. Ores went by ore dictionary
    * name; here by c: tags, raw materials included (E15). Tin, silver and lead nuggets come from whichever mod has them.
    */
   private static void initSmeltingBonuses() {
      String[] metals = {"gold", "iron", "copper", "cinnabar", "tin", "silver", "lead"};
      ItemLike[] clusters = {
         ModItems.GOLD_CLUSTER.get(), ModItems.IRON_CLUSTER.get(), ModItems.COPPER_CLUSTER.get(), ModItems.CINNABAR_CLUSTER.get(),
         ModItems.TIN_CLUSTER.get(), ModItems.SILVER_CLUSTER.get(), ModItems.LEAD_CLUSTER.get()
      };
      ItemLike[] nuggets = {Items.GOLD_NUGGET, Items.IRON_NUGGET, ModItems.COPPER_NUGGET.get(), ModItems.QUICKSILVER_NUGGET.get()};
      for (int i = 0; i < metals.length; i++) {
         for (TagKey<Item> ore : List.of(commonTag("ores/" + metals[i]), commonTag("raw_materials/" + metals[i]))) {
            if (i < nuggets.length) {
               ThaumcraftApi.addSmeltingBonus(ore, nuggets[i]);
            } else {
               ThaumcraftApi.addSmeltingBonus(ore, commonTag("nuggets/" + metals[i]));
            }
         }

         if (i < nuggets.length) {
            ThaumcraftApi.addSmeltingBonus(clusters[i], nuggets[i]);
         } else {
            ThaumcraftApi.addSmeltingBonus(clusters[i], commonTag("nuggets/" + metals[i]));
         }
      }

      ThaumcraftApi.addSmeltingBonus(Items.CHICKEN, ModItems.CHICKEN_NUGGET.get());
      ThaumcraftApi.addSmeltingBonus(Items.BEEF, ModItems.BEEF_NUGGET.get());
      ThaumcraftApi.addSmeltingBonus(Items.PORKCHOP, ModItems.PORK_NUGGET.get());
      // 1.7.10 fish of any kind; of today's fish only cod and salmon smelt.
      ThaumcraftApi.addSmeltingBonus(Items.COD, ModItems.FISH_NUGGET.get());
      ThaumcraftApi.addSmeltingBonus(Items.SALMON, ModItems.FISH_NUGGET.get());
   }

   private static TagKey<Item> commonTag(String path) {
      return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
   }

   private static void addOptionalSpecialMiningResult(String namespace, String path, ItemStack output, float chance) {
      ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
      BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> Utils.addSpecialMiningResult(new ItemStack(item), output, chance));
   }

   private static void addTaggedSpecialMiningResult(String path, Item output) {
      addTaggedSpecialMiningResult(path, output, 1.0F);
   }

   private static void addTaggedSpecialMiningResult(String path, Item output, float chance) {
      Utils.addSpecialMiningResult(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path)), new ItemStack(output), chance);
   }

   public static final class ItemBlockList {
      public boolean contains(ItemStack stack) {
         return false;
      }

      public boolean contains(Block block) {
         return false;
      }

      public boolean contains(Block block, int meta) {
         return false;
      }
   }
}
