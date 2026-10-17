package thaumcraft.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCap;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.ItemWandRod;
import thaumcraft.research.ResearchManager;
import thaumcraft.util.ItemStackData;

public class ArcaneSceptreRecipe implements IArcaneRecipe {
   public static final ArcaneSceptreRecipe INSTANCE = new ArcaneSceptreRecipe();
   private static final ResourceLocation ID = Thaumcraft.location("dynamic_arcane_sceptre");

   private ArcaneSceptreRecipe() {
   }

   public boolean matches(CraftingContainer container, Level level, @Nullable Player player) {
      ArcaneSceptreRecipe.SceptreComponents components = this.getComponents(container);
      return components != null
         && (player == null || ResearchManager.isResearchComplete(player, "sceptre"))
         && ArcaneWandRecipeSupport.hasCapResearch(player, components.cap())
         && ArcaneWandRecipeSupport.hasRodResearch(player, components.rod());
   }

   public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
      return this.getComponents(input) != null;
   }

   @NotNull
   public ItemStack assemble(@NotNull CraftingInput input, @NotNull Provider access) {
      ArcaneSceptreRecipe.SceptreComponents components = this.getComponents(input);
      return components == null ? ItemStack.EMPTY : ItemWandCasting.createSceptre(components.rod(), components.cap());
   }

   @NotNull
   @Override
   public ItemStack getResultItem(CraftingContainer container, Provider access) {
      return this.assemble(toInput(container), access);
   }

   @Override
   public AspectList getAspects(CraftingContainer container) {
      ArcaneSceptreRecipe.SceptreComponents components = this.getComponents(container);
      if (components == null) {
         return new AspectList();
      } else {
         int cost = (int)(components.cap().getCraftCost() * components.rod().getCraftCost() * 1.5F);
         return ArcaneWandRecipeSupport.getPrimalCost(cost);
      }
   }

   @NotNull
   public ItemStack getResultItem(@NotNull Provider access) {
      return ItemStack.EMPTY;
   }

   @Override
   public AspectList getAspects() {
      return new AspectList();
   }

   @NotNull
   public NonNullList<Ingredient> getIngredients() {
      return NonNullList.create();
   }

   @NotNull
   public CraftingBookCategory category() {
      return CraftingBookCategory.MISC;
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width >= 3 && height >= 3;
   }

   @NotNull
   public ResourceLocation getId() {
      return ID;
   }

   @NotNull
   public RecipeSerializer<?> getSerializer() {
      return ShapedArcaneRecipe.SERIALIZER;
   }

   @NotNull
   public RecipeType<?> getType() {
      return ShapedArcaneRecipe.TYPE;
   }

   @Override
   public String getResearchKey() {
      return "";
   }

   @Nullable
   private ArcaneSceptreRecipe.SceptreComponents getComponents(CraftingContainer container) {
      return this.getComponents(toInput(container));
   }

   @Nullable
   private ArcaneSceptreRecipe.SceptreComponents getComponents(CraftingInput container) {
      if (container.width() < 3 || container.height() < 3) {
         return null;
      } else if (this.isEmpty(container, 0, 0) && this.isEmpty(container, 0, 1) && this.isEmpty(container, 1, 2) && this.isEmpty(container, 2, 2)) {
         ItemStack topCap = container.getItem(1);
         ItemStack rightCap = container.getItem(5);
         ItemStack bottomCap = container.getItem(6);
         ItemStack rodStack = container.getItem(4);
         ItemStack focusStack = container.getItem(2);
         if (topCap.isEmpty() || rightCap.isEmpty() || bottomCap.isEmpty() || rodStack.isEmpty() || focusStack.isEmpty()) {
            return null;
         } else if (!ItemStackData.sameItemSameTags(topCap, rightCap) || !ItemStackData.sameItemSameTags(topCap, bottomCap)) {
            return null;
         } else if (!focusStack.is((Item)ModItems.PRIMAL_CHARM.get())) {
            return null;
         } else {
            ItemWandCap cap = ArcaneWandRecipeSupport.findCap(topCap);
            ItemWandRod rod = ArcaneWandRecipeSupport.findRod(rodStack);
            return cap != null && rod != null ? new ArcaneSceptreRecipe.SceptreComponents(cap, rod) : null;
         }
      } else {
         return null;
      }
   }

   private boolean isEmpty(CraftingContainer container, int x, int y) {
      return container.getItem(x + y * 3).isEmpty();
   }

   private boolean isEmpty(CraftingInput container, int x, int y) {
      return container.getItem(x + y * 3).isEmpty();
   }

   private static CraftingInput toInput(CraftingContainer container) {
      NonNullList<ItemStack> grid = NonNullList.withSize(9, ItemStack.EMPTY);

      for (int i = 0; i < Math.min(9, container.getContainerSize()); i++) {
         grid.set(i, container.getItem(i));
      }

      return CraftingInput.of(3, 3, grid);
   }

   private record SceptreComponents(ItemWandCap cap, ItemWandRod rod) {
   }
}
