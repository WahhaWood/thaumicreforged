package thaumcraft.compat.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.block.arcane_workbench.ArcaneWorkbenchMenu;
import thaumcraft.block.arcane_workbench.ArcaneWorkbenchScreen;
import thaumcraft.block.infusion_matrix.handlers.InfusionInstabilityHandler;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.wands.ItemWandCap;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.ItemWandRod;
import thaumcraft.recipe.ArcaneWandRecipeSupport;
import thaumcraft.research.ResearchManager;
import thaumcraft.util.ItemStackData;

@OnlyIn(Dist.CLIENT)
@JeiPlugin
public class ThaumcraftJeiPlugin implements IModPlugin {
   private static final ResourceLocation UID = Thaumcraft.location("jei_plugin");
   private static IJeiRuntime jeiRuntime;

   @NotNull
   public ResourceLocation getPluginUid() {
      return UID;
   }

   public void registerItemSubtypes(ISubtypeRegistration registration) {
      registration.registerSubtypeInterpreter((Item)ModItems.WAND_CASTING.get(), (stack, context) -> {
         CompoundTag tag = ItemStackData.getTag(stack);
         if (tag == null) {
            return "";
         } else {
            String cap = tag.getString("cap");
            String rod = tag.getString("rod");
            boolean sceptre = tag.contains("sceptre");
            if (cap.isEmpty() && rod.isEmpty() && !sceptre) {
               return "";
            } else {
               StringBuilder subtype = new StringBuilder();
               subtype.append("cap=").append(cap);
               subtype.append(";rod=").append(rod);
               subtype.append(";sceptre=").append(sceptre);
               if (context == UidContext.Ingredient && tag.contains("focus")) {
                  subtype.append(";focus=").append(tag.getCompound("focus"));
               }

               return subtype.toString();
            }
         }
      });
      registration.registerSubtypeInterpreter((Item)ModItems.JAR_LABEL.get(), (stack, context) -> {
         CompoundTag tag = ItemStackData.getTag(stack);
         return tag != null && !tag.isEmpty() ? tag.toString() : "";
      });
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      registration.addRecipeCategories(
         new IRecipeCategory[]{
            new ArcaneRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
            new CrucibleRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
            new InfusionRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
            new StabilizerRecipeCategory(registration.getJeiHelpers().getGuiHelper())
         }
      );
   }

   public void registerRecipes(@NotNull IRecipeRegistration registration) {
      RecipeManager recipeManager = getRecipeManager();
      if (recipeManager != null) {
         List<IArcaneRecipe> recipes = new ArrayList<>();
         recipes.addAll(getAllArcaneRecipes(recipeManager));
         registration.addRecipes(ArcaneRecipeCategory.RECIPE_TYPE, recipes);
         registration.addRecipes(
            CrucibleRecipeCategory.RECIPE_TYPE, recipeManager.getAllRecipesFor(CrucibleRecipe.TYPE).stream().map(RecipeHolder::value).toList()
         );
         registration.addRecipes(
            InfusionRecipeCategory.RECIPE_TYPE, recipeManager.getAllRecipesFor(InfusionRecipe.TYPE).stream().map(RecipeHolder::value).toList()
         );
         registration.addRecipes(StabilizerRecipeCategory.RECIPE_TYPE, getStabilizerEntries());
      }
   }

   public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
      registration.addRecipeTransferHandler(
         ArcaneWorkbenchMenu.class, (MenuType)ModMenuTypes.ARCANE_WORKBENCH.get(), ArcaneRecipeCategory.RECIPE_TYPE, 2, 9, 11, 36
      );
      registration.addRecipeTransferHandler(ArcaneWorkbenchMenu.class, (MenuType)ModMenuTypes.ARCANE_WORKBENCH.get(), RecipeTypes.CRAFTING, 2, 9, 11, 36);
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addRecipeCatalyst(((BlockItem)ModItems.ARCANE_WORKBENCH.get()).getDefaultInstance(), new RecipeType[]{ArcaneRecipeCategory.RECIPE_TYPE});
      registration.addRecipeCatalyst(((BlockItem)ModItems.ARCANE_WORKBENCH.get()).getDefaultInstance(), new RecipeType[]{RecipeTypes.CRAFTING});
      registration.addRecipeCatalyst(((BlockItem)ModItems.CRUCIBLE.get()).getDefaultInstance(), new RecipeType[]{CrucibleRecipeCategory.RECIPE_TYPE});
      registration.addRecipeCatalyst(((BlockItem)ModItems.RUNIC_MATRIX.get()).getDefaultInstance(), new RecipeType[]{InfusionRecipeCategory.RECIPE_TYPE});
      registration.addRecipeCatalyst(((BlockItem)ModItems.RUNIC_MATRIX.get()).getDefaultInstance(), new RecipeType[]{StabilizerRecipeCategory.RECIPE_TYPE});
   }

   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addRecipeClickArea(ArcaneWorkbenchScreen.class, 143, 63, 18, 18, new RecipeType[]{RecipeTypes.CRAFTING, ArcaneRecipeCategory.RECIPE_TYPE});
   }

   public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
      ThaumcraftJeiPlugin.jeiRuntime = jeiRuntime;
      refreshRecipeVisibility();
   }

   public void onRuntimeUnavailable() {
      jeiRuntime = null;
   }

   @OnlyIn(Dist.CLIENT)
   private static RecipeManager getRecipeManager() {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft.getConnection() == null ? null : minecraft.getConnection().getRecipeManager();
   }

   @OnlyIn(Dist.CLIENT)
   public static void refreshRecipeVisibility() {
      if (jeiRuntime != null) {
         RecipeManager recipeManager = getRecipeManager();
         Player player = Minecraft.getInstance().player;
         if (recipeManager != null && player != null) {
            syncRecipeVisibility(
               jeiRuntime, ArcaneRecipeCategory.RECIPE_TYPE, getAllArcaneRecipes(recipeManager), getLockedArcaneRecipes(recipeManager, player)
            );
            List<CrucibleRecipe> crucibleRecipes = recipeManager.getAllRecipesFor(CrucibleRecipe.TYPE)
               .stream()
               .<CrucibleRecipe>map(RecipeHolder::value)
               .toList();
            List<InfusionRecipe> infusionRecipes = recipeManager.getAllRecipesFor(InfusionRecipe.TYPE)
               .stream()
               .<InfusionRecipe>map(RecipeHolder::value)
               .toList();
            syncRecipeVisibility(
               jeiRuntime, CrucibleRecipeCategory.RECIPE_TYPE, crucibleRecipes, getLockedRecipes(crucibleRecipes, player, recipe -> recipe.researchKey)
            );
            syncRecipeVisibility(
               jeiRuntime, InfusionRecipeCategory.RECIPE_TYPE, infusionRecipes, getLockedRecipes(infusionRecipes, player, recipe -> recipe.researchKey)
            );
         }
      }
   }

   private static List<StabilizerRecipeCategory.StabilizerEntry> getStabilizerEntries() {
      // Те же правила, что у матрицы: черепа, свечи (включая ванильные теги),
      // кристаллы и любые блоки с IInfusionStabiliser. Показываем вес ЗЕРКАЛЬНОЙ
      // ПАРЫ (два блока) — именно столько пара добавляет к стабильности.
      List<StabilizerRecipeCategory.StabilizerEntry> entries = new ArrayList<>();
      for (Block block : BuiltInRegistries.BLOCK) {
         BlockState state = block.defaultBlockState();
         if (!InfusionInstabilityHandler.isStabilizerBlock(state)) {
            continue;
         }
         if (block.asItem() instanceof BlockItem blockItem) {
            entries.add(
               new StabilizerRecipeCategory.StabilizerEntry(
                  blockItem.getDefaultInstance(),
                  InfusionInstabilityHandler.getStabilityValue(state) * 2.0
               )
            );
         }
      }
      entries.sort(Comparator.comparing(entry -> entry.stack().getDescriptionId()));
      return entries;
   }

   private static List<IArcaneRecipe> getAllArcaneRecipes(RecipeManager recipeManager) {
      List<IArcaneRecipe> recipes = new ArrayList<>();
      recipes.addAll(recipeManager.getAllRecipesFor(ShapedArcaneRecipe.TYPE).stream().map(RecipeHolder::value).toList());
      recipes.addAll(recipeManager.getAllRecipesFor(ShapelessArcaneRecipe.TYPE).stream().map(RecipeHolder::value).toList());
      recipes.addAll(ArcaneJeiRecipes.getDynamicArcaneRecipes());
      return recipes;
   }

   private static List<IArcaneRecipe> getLockedArcaneRecipes(RecipeManager recipeManager, Player player) {
      List<IArcaneRecipe> lockedRecipes = new ArrayList<>();

      for (IArcaneRecipe recipe : getAllArcaneRecipes(recipeManager)) {
         if (isArcaneRecipeLocked(recipe, player)) {
            lockedRecipes.add(recipe);
         }
      }

      return lockedRecipes;
   }

   private static boolean isArcaneRecipeLocked(IArcaneRecipe recipe, Player player) {
      String researchKey = recipe.getResearchKey();
      if (!researchKey.isEmpty() && !ResearchManager.isResearchComplete(player, researchKey)) {
         return true;
      } else {
         ItemStack result = recipe.getResultItem();
         if (!(result.getItem() instanceof ItemWandCasting)) {
            return false;
         } else {
            CompoundTag tag = ItemStackData.getTag(result);
            if (tag != null && tag.contains("cap") && tag.contains("rod")) {
               ItemWandCap cap = ItemWandCap.get(tag.getString("cap"));
               ItemWandRod rod = ItemWandRod.get(tag.getString("rod"));
               return ArcaneWandRecipeSupport.hasCapResearch(player, cap) && ArcaneWandRecipeSupport.hasRodResearch(player, rod)
                  ? tag.contains("sceptre") && !ResearchManager.isResearchComplete(player, "sceptre")
                  : true;
            } else {
               return false;
            }
         }
      }
   }

   private static <T> List<T> getLockedRecipes(List<T> recipes, Player player, Function<T, String> researchKeyGetter) {
      List<T> lockedRecipes = new ArrayList<>();

      for (T recipe : recipes) {
         String researchKey = researchKeyGetter.apply(recipe);
         if (!researchKey.isEmpty() && !ResearchManager.isResearchComplete(player, researchKey)) {
            lockedRecipes.add(recipe);
         }
      }

      return lockedRecipes;
   }

   private static <T> void syncRecipeVisibility(IJeiRuntime jeiRuntime, RecipeType<T> recipeType, Collection<T> allRecipes, Collection<T> lockedRecipes) {
      if (!allRecipes.isEmpty()) {
         jeiRuntime.getRecipeManager().unhideRecipes(recipeType, allRecipes);
      }

      if (!lockedRecipes.isEmpty()) {
         jeiRuntime.getRecipeManager().hideRecipes(recipeType, lockedRecipes);
      }
   }
}
