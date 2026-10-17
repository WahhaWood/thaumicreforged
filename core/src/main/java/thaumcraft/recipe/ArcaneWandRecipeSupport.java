package thaumcraft.recipe;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCap;
import thaumcraft.item.wands.ItemWandRod;
import thaumcraft.research.ResearchManager;

public final class ArcaneWandRecipeSupport {
   private ArcaneWandRecipeSupport() {
   }

   @Nullable
   public static ItemWandCap findCap(ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      } else {
         for (ItemWandCap cap : ItemWandCap.TYPES.values()) {
            if (stack.is(cap)) {
               return cap;
            }
         }

         return null;
      }
   }

   @Nullable
   public static ItemWandRod findRod(ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      } else {
         for (ItemWandRod rod : ItemWandRod.TYPES.values()) {
            if (matchesRodIngredient(stack, rod)) {
               return rod;
            }
         }

         return null;
      }
   }

   public static boolean matchesRodIngredient(ItemStack stack, ItemWandRod rod) {
      return rod == ModItems.WOOD_WAND_ROD.get() ? stack.is(Items.STICK) : stack.is(rod);
   }

   public static boolean hasCapResearch(@Nullable Player player, ItemWandCap cap) {
      String key = getCapResearchKey(cap);
      return player == null || key.isEmpty() || ResearchManager.isResearchComplete(player, key);
   }

   public static boolean hasRodResearch(@Nullable Player player, ItemWandRod rod) {
      String key = getRodResearchKey(rod);
      return player == null || key.isEmpty() || ResearchManager.isResearchComplete(player, key);
   }

   public static AspectList getPrimalCost(int amount) {
      AspectList list = new AspectList();

      for (Aspect aspect : Aspect.getPrimalAspects()) {
         list.add(aspect, amount);
      }

      return list;
   }

   public static String getCapResearchKey(ItemWandCap cap) {
      String var1 = cap.getTag();

      return switch (var1) {
         case "iron" -> "basic_thaumaturgy";
         case "gold" -> "gold_cap";
         case "copper" -> "copper_cap";
         case "silver" -> "silver_cap";
         case "thaumium" -> "thaumium_cap";
         case "void" -> "void_cap";
         case "ichor" -> "cap_ichor";
         default -> "";
      };
   }

   public static String getRodResearchKey(ItemWandRod rod) {
      String var1 = rod.getTag();

      return switch (var1) {
         case "wood" -> "basic_thaumaturgy";
         case "greatwood" -> "greatwood_rod";
         case "silverwood" -> "silverwood_rod";
         case "obsidian" -> "obsidian_rod";
         case "ice" -> "ice_rod";
         case "quartz" -> "quartz_rod";
         case "reed" -> "reed_rod";
         case "blaze" -> "blaze_rod";
         case "bone" -> "bone_rod";
         case "ichor" -> "rod_ichorcloth";
         default -> "";
      };
   }
}
