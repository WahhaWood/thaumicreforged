package thaumcraft.config;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import thaumcraft.init.registry.Config;

public final class ThaumcraftCommonConfig {
   public static final ModConfigSpec SPEC;
   private static final BooleanValue WARDED_STONE;
   private static final BooleanValue ALLOW_MIRRORS;
   private static final BooleanValue ENABLE_KAMI;
   private static final BooleanValue AURA_NODE_DEBUG;
   private static final BooleanValue HARD_NODE;
   private static final BooleanValue WUSS;
   private static final IntValue RESEARCH_DIFFICULTY;
   private static final IntValue ASPECT_TOTAL_CAP;
   private static final IntValue SPECIAL_NODE_RARITY;

   private ThaumcraftCommonConfig() {
   }

   public static void register() {
      ModLoadingContext.get().getActiveContainer().registerConfig(Type.COMMON, SPEC);
   }

   public static void syncToLegacy() {
      Config.wardedStone = (Boolean)WARDED_STONE.get();
      Config.CwardedStone = Config.wardedStone;
      Config.allowMirrors = (Boolean)ALLOW_MIRRORS.get();
      Config.CallowMirrors = Config.allowMirrors;
      Config.enableKami = (Boolean)ENABLE_KAMI.get();
      Config.CenableKami = Config.enableKami;
      Config.auraNodeDebug = (Boolean)AURA_NODE_DEBUG.get();
      Config.CauraNodeDebug = Config.auraNodeDebug;
      Config.hardNode = (Boolean)HARD_NODE.get();
      Config.ChardNode = Config.hardNode;
      Config.wuss = (Boolean)WUSS.get();
      Config.Cwuss = Config.wuss;
      Config.researchDifficulty = (Integer)RESEARCH_DIFFICULTY.get();
      Config.CresearchDifficulty = Config.researchDifficulty;
      Config.aspectTotalCap = (Integer)ASPECT_TOTAL_CAP.get();
      Config.CaspectTotalCap = Config.aspectTotalCap;
      Config.specialNodeRarity = (Integer)SPECIAL_NODE_RARITY.get();
   }

   static {
      Builder builder = new Builder();
      builder.comment("Thaumcraft common configuration");
      builder.push("general");
      WARDED_STONE = builder.comment("If false, warded stone, doors and glass lose their hardened properties.").define("wardedStone", true);
      ALLOW_MIRRORS = builder.comment("If false, arcane mirror research and crafting recipes are disabled.").define("allowMirrors", true);
      ENABLE_KAMI = builder.comment("Enable Kami-related content.").define("enableKami", true);
      AURA_NODE_DEBUG = builder.comment("Enable verbose aura node generation debug logging.").define("auraNodeDebug", false);
      HARD_NODE = builder.comment("Negative nodes like hungry, tainted or dark nodes get additional effects.").define("hardNode", true);
      WUSS = builder.comment("Disables Warp and related mechanics.").define("wuss", false);
      RESEARCH_DIFFICULTY = builder.comment("0 = normal, -1 = easy, 1 = hard.").defineInRange("researchDifficulty", 0, -1, 1);
      ASPECT_TOTAL_CAP = builder.comment("The total amount of RP you can have in your pool per aspect before the soft cap kicks in.")
         .defineInRange("aspectTotalCap", 100, 1, Integer.MAX_VALUE);
      SPECIAL_NODE_RARITY = builder.comment(
            "The chance of a node being special (pure, dark, unstable, hungry). Roughly 1 in N new nodes will be special;",
            "a tenth of those are hungry. Also sets the modifier chance (1 in N/2). Affects only newly generated chunks."
         )
         .defineInRange("specialNodeRarity", 18, 3, Integer.MAX_VALUE);
      builder.pop();
      SPEC = builder.build();
   }
}
