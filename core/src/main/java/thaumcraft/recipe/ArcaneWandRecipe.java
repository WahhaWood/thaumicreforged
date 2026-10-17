package thaumcraft.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
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
import thaumcraft.util.ItemStackData;

public class ArcaneWandRecipe implements IArcaneRecipe {
   public static final ArcaneWandRecipe INSTANCE = new ArcaneWandRecipe();
   private static final ResourceLocation ID = Thaumcraft.location("dynamic_arcane_wand");

   private ArcaneWandRecipe() {
   }

   public boolean matches(CraftingContainer container, Level level, @Nullable Player player) {
      ArcaneWandRecipe.WandComponents components = this.getComponents(container);
      return components != null
         && ArcaneWandRecipeSupport.hasCapResearch(player, components.cap())
         && ArcaneWandRecipeSupport.hasRodResearch(player, components.rod());
   }

   public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
      return this.getComponents(input) != null;
   }

   @NotNull
   public ItemStack assemble(@NotNull CraftingInput input, @NotNull Provider access) {
      ArcaneWandRecipe.WandComponents components = this.getComponents(input);
      return components == null ? ItemStack.EMPTY : ItemWandCasting.createWand(components.rod(), components.cap());
   }

   @NotNull
   @Override
   public ItemStack getResultItem(CraftingContainer container, Provider access) {
      return this.assemble(toInput(container), access);
   }

   @Override
   public AspectList getAspects(CraftingContainer container) {
      ArcaneWandRecipe.WandComponents components = this.getComponents(container);
      return components == null ? new AspectList() : ArcaneWandRecipeSupport.getPrimalCost(components.cap().getCraftCost() * components.rod().getCraftCost());
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
   private ArcaneWandRecipe.WandComponents getComponents(CraftingContainer container) {
      return this.getComponents(toInput(container));
   }

   @Nullable
   private ArcaneWandRecipe.WandComponents getComponents(CraftingInput container) {
      if (container.width() < 3 || container.height() < 3) {
         return null;
      } else if (this.isEmpty(container, 0, 0)
         && this.isEmpty(container, 0, 1)
         && this.isEmpty(container, 1, 0)
         && this.isEmpty(container, 1, 2)
         && this.isEmpty(container, 2, 1)
         && this.isEmpty(container, 2, 2)) {
         ItemStack firstCapStack = container.getItem(6);
         ItemStack secondCapStack = container.getItem(2);
         ItemStack rodStack = container.getItem(4);
         if (firstCapStack.isEmpty() || secondCapStack.isEmpty() || rodStack.isEmpty()) {
            return null;
         } else if (!ItemStackData.sameItemSameTags(firstCapStack, secondCapStack)) {
            return null;
         } else {
            ItemWandCap cap = ArcaneWandRecipeSupport.findCap(firstCapStack);
            ItemWandRod rod = ArcaneWandRecipeSupport.findRod(rodStack);
            if (cap == null || rod == null) {
               return null;
            } else {
               return cap == ModItems.IRON_WAND_CAP.get() && rod == ModItems.WOOD_WAND_ROD.get() ? null : new ArcaneWandRecipe.WandComponents(cap, rod);
            }
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

   private record WandComponents(ItemWandCap cap, ItemWandRod rod) {
   }
}
