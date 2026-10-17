package thaumcraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IInfusionModifierRecipe;
import thaumcraft.event.common.EventHandlerRunic;
import thaumcraft.util.ItemStackData;

public class InfusionRunicAugmentRecipe implements IInfusionModifierRecipe {
   public static final RecipeType<InfusionRunicAugmentRecipe> TYPE = RecipeType.simple(Thaumcraft.location("runic_augmentation"));
   public static final RecipeSerializer<InfusionRunicAugmentRecipe> SERIALIZER = new InfusionRunicAugmentRecipe.Serializer();
   private final ResourceLocation id;
   private final String researchKey;
   private final NonNullList<Ingredient> baseComponents;
   private final ItemStack displayCentral;

   public InfusionRunicAugmentRecipe(ResourceLocation id, String researchKey, ItemStack displayCentral, NonNullList<Ingredient> baseComponents) {
      this.id = id;
      this.researchKey = researchKey;
      this.displayCentral = displayCentral.copy();
      this.baseComponents = NonNullList.create();
      this.baseComponents.addAll(baseComponents);
   }

   public InfusionRunicAugmentRecipe(String researchKey, ItemStack displayCentral, NonNullList<Ingredient> baseComponents) {
      this(Thaumcraft.location("generated_runic_augmentation"), researchKey, displayCentral, baseComponents);
   }

   public boolean matches(@NotNull RecipeInput container, @NotNull Level level) {
      return false;
   }

   @NotNull
   public ItemStack assemble(@NotNull RecipeInput container, @NotNull Provider registryAccess) {
      return ItemStack.EMPTY;
   }

   public boolean canCraftInDimensions(int width, int height) {
      return false;
   }

   @NotNull
   public ItemStack getResultItem(@NotNull Provider registryAccess) {
      return this.displayCentral.isEmpty() ? ItemStack.EMPTY : this.modifierItem(this.displayCentral);
   }

   @NotNull
   public ResourceLocation getId() {
      return this.id;
   }

   @NotNull
   public RecipeSerializer<?> getSerializer() {
      return SERIALIZER;
   }

   @NotNull
   public RecipeType<?> getType() {
      return TYPE;
   }

   public ItemStack getDisplayCentral() {
      return this.displayCentral.copy();
   }

   @Override
   public boolean matches(List<ItemStack> components, ItemStack central) {
      if (!(central.getItem() instanceof IRunicArmor)) {
         return false;
      } else {
         List<ItemStack> inputs = new ArrayList<>();

         for (ItemStack stack : components) {
            inputs.add(stack.copy());
         }

         label30:
         for (Ingredient ingredient : this.getIngredientList(central)) {
            for (int i = 0; i < inputs.size(); i++) {
               if (ingredient.test(inputs.get(i))) {
                  inputs.remove(i);
                  continue label30;
               }
            }

            return false;
         }

         return inputs.isEmpty();
      }
   }

   @Override
   public ItemStack modifierItem(ItemStack central) {
      ItemStack copy = central.copy();
      ItemStackData.getOrCreateTag(copy).putByte("RS.HARDEN", (byte)(EventHandlerRunic.getHardening(central) + 1));
      return copy;
   }

   @Override
   public AspectList getAspects() {
      return this.displayCentral.isEmpty() ? new AspectList() : this.getAspects(this.displayCentral);
   }

   @Override
   public AspectList getAspects(ItemStack central) {
      AspectList aspects = new AspectList();
      int amount = (int)(32.0 * Math.pow(2.0, EventHandlerRunic.getFinalCharge(central)));
      if (amount > 0) {
         aspects.add(Aspect.ARMOR, amount / 2);
         aspects.add(Aspect.MAGIC, amount / 2);
         aspects.add(Aspect.ENERGY, amount);
      }

      return aspects;
   }

   @Override
   public int getInstability() {
      return this.displayCentral.isEmpty() ? 0 : this.getInstability(this.displayCentral);
   }

   @Override
   public int getInstability(ItemStack central) {
      return 5 + EventHandlerRunic.getFinalCharge(central) / 2;
   }

   @Override
   public String getResearchKey() {
      return this.researchKey;
   }

   @Override
   public List<Ingredient> getIngredientList() {
      return this.displayCentral.isEmpty() ? List.copyOf(this.baseComponents) : this.getIngredientList(this.displayCentral);
   }

   @Override
   public List<Ingredient> getIngredientList(ItemStack central) {
      NonNullList<Ingredient> ingredients = NonNullList.create();
      ingredients.addAll(this.baseComponents);
      if (!this.baseComponents.isEmpty()) {
         Ingredient repeated = (Ingredient)this.baseComponents.get(this.baseComponents.size() - 1);

         for (int i = 0; i < EventHandlerRunic.getFinalCharge(central); i++) {
            ingredients.add(repeated);
         }
      }

      return ingredients;
   }

   public static class Serializer implements RecipeSerializer<InfusionRunicAugmentRecipe> {
      @NotNull
      public MapCodec<InfusionRunicAugmentRecipe> codec() {
         return RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                  Codec.STRING.fieldOf("research").forGetter(recipe -> recipe.researchKey),
                  ItemStack.STRICT_CODEC.optionalFieldOf("display", ItemStack.EMPTY).forGetter(recipe -> recipe.displayCentral),
                  Ingredient.CODEC_NONEMPTY
                     .listOf()
                     .xmap(NonNullList::copyOf, Function.identity())
                     .fieldOf("components")
                     .forGetter(recipe -> recipe.baseComponents)
               )
               .apply(builder, InfusionRunicAugmentRecipe::new)
         );
      }

      @NotNull
      public StreamCodec<RegistryFriendlyByteBuf, InfusionRunicAugmentRecipe> streamCodec() {
         return StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            recipe -> recipe.researchKey,
            ItemStack.STREAM_CODEC,
            recipe -> recipe.displayCentral,
            ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC),
            recipe -> recipe.baseComponents,
            InfusionRunicAugmentRecipe::new
         );
      }
   }
}
