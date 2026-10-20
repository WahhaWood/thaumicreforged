package thaumcraft.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

/**
 * Aspects of other mods' items ({@code ModItemAspects}): by common tag and by id. Each check runs only when its mod is
 * loaded, so the test passes in a bare dev run; run it with the mods in {@code run-gametest/mods}
 * ({@code -PgameTestDir=run-gametest}) to cover them.
 */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class ModItemAspectsGameTest {
   @GameTest(template = "empty_17x9x17")
   public static void otherModsItemsHaveAspects(GameTestHelper helper) {
      expect(helper, "industrialreforged", "nickel_ingot", Aspect.METAL, Aspect.EXCHANGE);
      expect(helper, "industrialreforged", "uranium_ore", Aspect.METAL, Aspect.ENERGY);
      expect(helper, "industrialreforged", "rubber", Aspect.SLIME);
      expect(helper, "botania", "manasteel_ingot", Aspect.METAL, Aspect.MAGIC);
      expect(helper, "botania", "livingwood_log", Aspect.TREE, Aspect.LIFE);
      expect(helper, "botania", "white_mystical_petal", Aspect.PLANT, Aspect.SENSES);
      expect(helper, "ae2", "fluix_crystal", Aspect.CRYSTAL, Aspect.ENERGY);
      expect(helper, "ae2", "singularity", Aspect.VOID);
      helper.succeed();
   }

   private static void expect(GameTestHelper helper, String namespace, String path, Aspect... aspects) {
      if (!ModList.get().isLoaded(namespace)) {
         return;
      }

      ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
      helper.assertTrue(BuiltInRegistries.ITEM.containsKey(id), id + " should exist");
      AspectList found = ThaumcraftApiHelper.getObjectAspects(new ItemStack(BuiltInRegistries.ITEM.get(id)));
      for (Aspect aspect : aspects) {
         helper.assertTrue(found != null && found.getAmount(aspect) > 0, id + " should have " + aspect.getTag() + ", has " + describe(found));
      }
   }

   private static String describe(AspectList list) {
      if (list == null || list.size() == 0) {
         return "none";
      }

      StringBuilder out = new StringBuilder();
      for (Aspect aspect : list.getAspects()) {
         out.append(aspect.getTag()).append('=').append(list.getAmount(aspect)).append(' ');
      }

      return out.toString().trim();
   }
}
