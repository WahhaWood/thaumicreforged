package thaumicenergistics.ae2.recipe;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * What one assembler offers to the ME network: the recipes of its kind, which of them are switched on,
 * and the patterns built from the enabled ones. The same object lives in the block entity on both sides:
 * the server builds patterns from it, the client GUI lists its recipes.
 */
public final class AssemblerRecipes {
   public enum Action {
      TOGGLE,
      ENABLE_ALL,
      DISABLE_ALL
   }

   /**
    * Bumped whenever recipes may have changed (datapack reload on the server, recipe sync on the client).
    * An assembler compares it with the value it built its list for.
    */
   private static final AtomicInteger GENERATION = new AtomicInteger();
   /** Server-side assemblers in loaded chunks, to re-offer their patterns after /reload. */
   private static final Set<RecipeAssembler> LOADED = new HashSet<>();

   private final AssemblerKind kind;
   private final RecipeToggles toggles;
   @Nullable
   private Map<ResourceLocation, AssemblerRecipe> recipes;
   private int recipesGeneration;
   @Nullable
   private List<IPatternDetails> patterns;

   public AssemblerRecipes(AssemblerKind kind) {
      this.kind = kind;
      this.toggles = new RecipeToggles(kind.enabledByDefault);
   }

   public static void invalidate() {
      GENERATION.incrementAndGet();
   }

   public static void track(RecipeAssembler assembler) {
      LOADED.add(assembler);
   }

   public static void untrack(RecipeAssembler assembler) {
      LOADED.remove(assembler);
   }

   /** Called after a datapack reload: every loaded assembler re-reads its recipes and re-offers patterns. */
   public static void refreshAll() {
      invalidate();
      for (RecipeAssembler assembler : List.copyOf(LOADED)) {
         assembler.recipeBook().patterns = null;
         ICraftingProvider.requestUpdate(assembler.getMainNode());
      }
   }

   public AssemblerKind kind() {
      return this.kind;
   }

   /** All usable recipes of this assembler's kind, by id, in GUI order. */
   public Map<ResourceLocation, AssemblerRecipe> recipes(@Nullable Level level) {
      if (level == null) {
         return Map.of();
      }

      int generation = GENERATION.get();
      if (this.recipes == null || this.recipesGeneration != generation) {
         this.recipes = this.kind.collect(level);
         this.recipesGeneration = generation;
         this.patterns = null;
      }

      return this.recipes;
   }

   public List<IPatternDetails> patterns(@Nullable Level level) {
      Map<ResourceLocation, AssemblerRecipe> all = this.recipes(level);
      if (this.patterns == null) {
         List<IPatternDetails> list = new ArrayList<>();
         for (AssemblerRecipe recipe : all.values()) {
            if (this.toggles.isEnabled(recipe.id())) {
               list.add(new AssemblerPattern(recipe));
            }
         }

         this.patterns = List.copyOf(list);
      }

      return this.patterns;
   }

   public boolean isEnabled(ResourceLocation id) {
      return this.toggles.isEnabled(id);
   }

   /** Server side: flips one recipe, or all of them, and re-offers the patterns. */
   public void apply(RecipeAssembler assembler, Action action, @Nullable ResourceLocation id) {
      switch (action) {
         case TOGGLE -> {
            if (id == null || !this.recipes(assembler.getLevel()).containsKey(id)) {
               return;
            }

            this.toggles.toggle(id);
         }
         case ENABLE_ALL -> this.toggles.setAll(true);
         case DISABLE_ALL -> this.toggles.setAll(false);
      }

      this.patterns = null;
      ICraftingProvider.requestUpdate(assembler.getMainNode());
      assembler.setChanged();
      assembler.markForUpdate();
   }

   public void save(CompoundTag tag) {
      this.toggles.save(tag);
   }

   public void load(CompoundTag tag) {
      this.toggles.load(tag);
      this.patterns = null;
   }

   public void write(FriendlyByteBuf buf) {
      this.toggles.write(buf);
   }

   public void read(FriendlyByteBuf buf) {
      this.toggles.read(buf);
      this.patterns = null;
   }
}
