package thaumicenergistics.ae2.recipe;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import thaumicenergistics.ae2.storage.AEAspectKey;
import thaumicenergistics.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.util.ItemStackData;

/**
 * A Thaumcraft recipe offered to the ME network as a processing pattern. There is no pattern item in
 * anyone's inventory: the definition is a hidden item carrying only the recipe id, which the decoder
 * turns back into the pattern when a crafting CPU reloads its job. Every item of a tagged ingredient is
 * a valid substitute; the first one is what AE2 autocrafts if none is stored. Crucible and infusion
 * recipes also ask the network for their essentia.
 */
public final class AssemblerPattern implements IPatternDetails {
   private static final String TAG_RECIPE = "recipe";

   private final AEItemKey definition;
   private final AssemblerRecipe recipe;
   private final IInput[] inputs;
   private final List<GenericStack> outputs;

   public AssemblerPattern(AssemblerRecipe recipe) {
      this.recipe = recipe;
      ItemStack stack = new ItemStack(ModItems.RECIPE_PATTERN.get());
      CompoundTag tag = new CompoundTag();
      tag.putString(TAG_RECIPE, recipe.id().toString());
      ItemStackData.setTag(stack, tag);
      this.definition = Objects.requireNonNull(AEItemKey.of(stack));

      List<IInput> list = new ArrayList<>();
      for (AssemblerRecipe.Entry entry : recipe.items()) {
         list.add(new ItemInput(entry.ingredient(), entry.count()));
      }

      if (recipe.kind() != AssemblerKind.ARCANE) {
         for (Map.Entry<Aspect, Integer> aspect : recipe.aspects().aspects.entrySet()) {
            if (aspect.getKey() != null && aspect.getValue() > 0) {
               list.add(new AspectInput(AEAspectKey.of(aspect.getKey()), aspect.getValue()));
            }
         }
      }

      this.inputs = list.toArray(IInput[]::new);
      this.outputs = List.of(Objects.requireNonNull(GenericStack.fromItemStack(recipe.output())));
   }

   public AssemblerRecipe recipe() {
      return this.recipe;
   }

   public ResourceLocation recipeId() {
      return this.recipe.id();
   }

   /** Vis for the arcane assembler, essentia for the other two. */
   public AspectList getAspects() {
      return this.recipe.aspects();
   }

   @Override
   public AEItemKey getDefinition() {
      return this.definition;
   }

   @Override
   public IInput[] getInputs() {
      return this.inputs;
   }

   @Override
   public List<GenericStack> getOutputs() {
      return this.outputs;
   }

   /** Only our own providers ever push these, straight into the assembler. */
   @Override
   public boolean supportsPushInputsToExternalInventory() {
      return false;
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof AssemblerPattern other && other.definition.equals(this.definition);
   }

   @Override
   public int hashCode() {
      return this.definition.hashCode();
   }

   private static final class ItemInput implements IInput {
      private final Ingredient ingredient;
      private final long count;
      private final GenericStack[] possible;

      private ItemInput(Ingredient ingredient, int count) {
         this.ingredient = ingredient;
         this.count = count;
         Set<AEItemKey> keys = new LinkedHashSet<>();
         for (ItemStack stack : ingredient.getItems()) {
            AEItemKey key = AEItemKey.of(stack);
            if (key != null) {
               keys.add(key);
            }
         }

         this.possible = keys.stream().map(key -> new GenericStack(key, 1)).toArray(GenericStack[]::new);
      }

      @Override
      public GenericStack[] getPossibleInputs() {
         return this.possible;
      }

      @Override
      public long getMultiplier() {
         return this.count;
      }

      @Override
      public boolean isValid(AEKey input, Level level) {
         return input instanceof AEItemKey item && item.matches(this.ingredient);
      }

      @Nullable
      @Override
      public AEKey getRemainingKey(AEKey template) {
         return null;
      }
   }

   private static final class AspectInput implements IInput {
      private final AEAspectKey key;
      private final long amount;

      private AspectInput(AEAspectKey key, long amount) {
         this.key = key;
         this.amount = amount;
      }

      @Override
      public GenericStack[] getPossibleInputs() {
         return new GenericStack[]{new GenericStack(this.key, 1)};
      }

      @Override
      public long getMultiplier() {
         return this.amount;
      }

      @Override
      public boolean isValid(AEKey input, Level level) {
         return this.key.equals(input);
      }

      @Nullable
      @Override
      public AEKey getRemainingKey(AEKey template) {
         return null;
      }
   }

   /** Rebuilds a pattern from its definition (crafting CPUs save jobs by definition only). */
   public static final class Decoder implements IPatternDetailsDecoder {
      @Override
      public boolean isEncodedPattern(ItemStack stack) {
         return stack.is(ModItems.RECIPE_PATTERN.get());
      }

      @Nullable
      @Override
      public IPatternDetails decodePattern(AEItemKey what, Level level) {
         if (what == null || level == null || !what.is(ModItems.RECIPE_PATTERN.get())) {
            return null;
         }

         CompoundTag tag = ItemStackData.getTag(what.getReadOnlyStack());
         ResourceLocation id = tag == null ? null : ResourceLocation.tryParse(tag.getString(TAG_RECIPE));
         RecipeHolder<?> holder = id == null ? null : level.getRecipeManager().byKey(id).orElse(null);
         AssemblerRecipe recipe = holder == null ? null : AssemblerKind.convert(holder.id(), holder.value(), level.registryAccess());
         return recipe == null ? null : new AssemblerPattern(recipe);
      }
   }
}
