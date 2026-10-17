package thaumcraft.data.builder;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Holder.Reference;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionEnchantmentRecipe;

public class InfusionEnchantmentRecipeBuilder {
   private static final ThreadLocal<HolderGetter<Enchantment>> ENCHANTMENT_LOOKUP = new ThreadLocal<>();
   private final String researchKey;
   private final Holder<Enchantment> enchantment;
   private final List<Ingredient> components = Lists.newArrayList();
   private final AspectList aspectList = new AspectList();
   private int instability;

   public InfusionEnchantmentRecipeBuilder(String researchKey, Holder<Enchantment> enchantment) {
      this.researchKey = researchKey;
      this.enchantment = enchantment;
   }

   public void save(@NotNull RecipeOutput output, @NotNull ResourceLocation id) {
      output.accept(
         id,
         new InfusionEnchantmentRecipe(id, this.researchKey, this.enchantment, this.instability, this.aspectList, NonNullList.copyOf(this.components)),
         null
      );
   }

   @NotNull
   public InfusionEnchantmentRecipeBuilder requires(@NotNull TagKey<Item> tagKey) {
      return this.requires(Ingredient.of(tagKey));
   }

   @NotNull
   public InfusionEnchantmentRecipeBuilder requires(@NotNull ItemLike itemLike) {
      return this.requires(itemLike, 1);
   }

   @NotNull
   public InfusionEnchantmentRecipeBuilder requires(@NotNull ItemLike itemLike, int count) {
      for (int i = 0; i < count; i++) {
         this.requires(Ingredient.of(new ItemLike[]{itemLike}));
      }

      return this;
   }

   @NotNull
   public InfusionEnchantmentRecipeBuilder requires(@NotNull Ingredient ingredient) {
      this.components.add(ingredient);
      return this;
   }

   @NotNull
   public InfusionEnchantmentRecipeBuilder aspect(@NotNull Aspect aspect, int amount) {
      this.aspectList.add(aspect, amount);
      return this;
   }

   @NotNull
   public InfusionEnchantmentRecipeBuilder instability(int value) {
      this.instability = value;
      return this;
   }

   public static InfusionEnchantmentRecipeBuilder enchant(String researchKey, Holder<Enchantment> enchantment) {
      return new InfusionEnchantmentRecipeBuilder(researchKey, enchantment);
   }

   public static InfusionEnchantmentRecipeBuilder enchant(String researchKey, Enchantment enchantment) {
      return new InfusionEnchantmentRecipeBuilder(researchKey, Holder.direct(enchantment));
   }

   public static InfusionEnchantmentRecipeBuilder enchant(String researchKey, ResourceKey<Enchantment> enchantment) {
      HolderGetter<Enchantment> lookup = ENCHANTMENT_LOOKUP.get();
      Holder<Enchantment> holder = lookup != null ? lookup.getOrThrow(enchantment) : Reference.createStandAlone(new HolderOwner<Enchantment>() {}, enchantment);
      return new InfusionEnchantmentRecipeBuilder(researchKey, holder);
   }

   public static void withEnchantmentLookup(HolderGetter<Enchantment> lookup, Runnable action) {
      ENCHANTMENT_LOOKUP.set(lookup);

      try {
         action.run();
      } finally {
         ENCHANTMENT_LOOKUP.remove();
      }
   }
}
