package thaumcraft.api.research;

import java.util.Iterator;
import java.util.LinkedHashMap;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.research.ResearchManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ResearchApi {
   public static ResearchCategoryList getResearchList(String s) {
      return ResearchManager.LOADER.get(s);
   }

   public static LinkedHashMap<String, ResearchCategoryList> getResearchLists() {
      return ResearchManager.LOADER.getByName();
   }

   public static void registerResearchItemType(ResourceLocation id, IResearchItemFactory factory) {
      ResearchManager.LOADER.registerResearchItemType(id, factory);
   }

   public static void registerCategory(String name, ResourceLocation icon, ResourceLocation background, int lineColor) {
      ResearchManager.LOADER.registerCategory(name, new ResearchCategoryList(icon, background, lineColor));
   }

   public static void registerCategory(String name, ResourceLocation icon, ResourceLocation background) {
      ResearchManager.LOADER.registerCategory(name, new ResearchCategoryList(icon, background));
   }

   public static void completeResearch(Player player, String key) {
      ThaumcraftApi.getSetup().getResearchManager().completeResearch(player, key);
   }

   @OnlyIn(Dist.CLIENT)
   public static String getCategoryName(String s) {
      return I18n.get("tc.research.category." + s, new Object[0]);
   }

   public static ResearchItem getResearch(String s) {
      Iterator<ResearchCategoryList> iterator = ResearchManager.LOADER.getByName().values().iterator();

      while (iterator.hasNext()) {
         for (ResearchItem next : iterator.next().research.values()) {
            if (next.key.equals(s)) {
               return next;
            }
         }
      }

      return null;
   }
}
