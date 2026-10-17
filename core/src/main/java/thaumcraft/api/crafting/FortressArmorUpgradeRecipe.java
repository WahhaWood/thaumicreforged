package thaumcraft.api.crafting;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
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
import thaumcraft.api.aspects.AspectList;
import thaumcraft.util.ItemStackData;

public class FortressArmorUpgradeRecipe implements IInfusionModifierRecipe {
   public static final RecipeType<FortressArmorUpgradeRecipe> TYPE = RecipeType.simple(Thaumcraft.location("fortress_armor_upgrade"));
   public static final RecipeSerializer<FortressArmorUpgradeRecipe> SERIALIZER = new FortressArmorUpgradeRecipe.Serializer();
   private static final ResourceLocation GENERATED_ID = Thaumcraft.location("generated_fortress_armor_upgrade");
   public static final String TAG_GOGGLES = "goggles";
   public static final String TAG_MASK = "mask";
   public final ResourceLocation id;
   public final AspectList aspects;
   public final String researchKey;
   public final NonNullList<Ingredient> components;
   public final Ingredient central;
   public final FortressArmorUpgradeRecipe.UpgradeKind kind;
   public final int instability;

   public FortressArmorUpgradeRecipe(
      ResourceLocation id,
      String researchKey,
      FortressArmorUpgradeRecipe.UpgradeKind kind,
      int instability,
      AspectList aspects,
      Ingredient central,
      NonNullList<Ingredient> components
   ) {
      this.id = id;
      this.researchKey = researchKey;
      this.kind = kind;
      this.instability = instability;
      this.aspects = aspects;
      this.central = central;
      this.components = components;
   }

   @NotNull
   public NonNullList<Ingredient> getIngredients() {
      return this.components;
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
      return this.getDisplayResult();
   }

   @NotNull
   public ItemStack getDisplayResult() {
      ItemStack[] stacks = this.central.getItems();
      return stacks.length == 0 ? ItemStack.EMPTY : this.modifierItem(stacks[0].copy());
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

   @Override
   public boolean matches(List<ItemStack> input, ItemStack central) {
      if (this.central.test(central) && this.kind.canApplyTo(central)) {
         List<ItemStack> inputs = new ArrayList<>();

         for (ItemStack stack : input) {
            inputs.add(stack.copy());
         }

         label29:
         for (Ingredient ingredient : this.components) {
            for (int i = 0; i < inputs.size(); i++) {
               if (ingredient.test(inputs.get(i))) {
                  inputs.remove(i);
                  continue label29;
               }
            }

            return false;
         }

         return inputs.isEmpty();
      } else {
         return false;
      }
   }

   @Override
   public ItemStack modifierItem(ItemStack central) {
      ItemStack copy = central.copy();
      this.kind.apply(copy);
      return copy;
   }

   @Override
   public AspectList getAspects() {
      return this.aspects;
   }

   @Override
   public int getInstability() {
      return this.instability;
   }

   @Override
   public String getResearchKey() {
      return this.researchKey;
   }

   @Override
   public List<Ingredient> getIngredientList() {
      return this.components;
   }

   public Ingredient getCentral() {
      return this.central;
   }

   public FortressArmorUpgradeRecipe(
      String researchKey,
      FortressArmorUpgradeRecipe.UpgradeKind kind,
      int instability,
      AspectList aspects,
      Ingredient central,
      NonNullList<Ingredient> components
   ) {
      this(GENERATED_ID, researchKey, kind, instability, aspects, central, components);
   }

   public static class Serializer implements RecipeSerializer<FortressArmorUpgradeRecipe> {
      @NotNull
      public MapCodec<FortressArmorUpgradeRecipe> codec() {
         return RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                  Codec.STRING.fieldOf("research").forGetter(recipe -> recipe.researchKey),
                  Codec.STRING
                     .xmap(FortressArmorUpgradeRecipe.UpgradeKind::byName, FortressArmorUpgradeRecipe.UpgradeKind::getSerializedName)
                     .fieldOf("upgrade")
                     .forGetter(recipe -> recipe.kind),
                  Codec.INT.optionalFieldOf("instability", 0).forGetter(recipe -> recipe.instability),
                  AspectList.CODEC.fieldOf("aspects").forGetter(recipe -> recipe.aspects),
                  Ingredient.CODEC_NONEMPTY.fieldOf("central").forGetter(recipe -> recipe.central),
                  Ingredient.CODEC_NONEMPTY
                     .listOf()
                     .xmap(NonNullList::copyOf, Function.identity())
                     .fieldOf("components")
                     .forGetter(recipe -> recipe.components)
               )
               .apply(builder, FortressArmorUpgradeRecipe::new)
         );
      }

      @NotNull
      public StreamCodec<RegistryFriendlyByteBuf, FortressArmorUpgradeRecipe> streamCodec() {
         return StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            recipe -> recipe.researchKey,
            ByteBufCodecs.STRING_UTF8.map(FortressArmorUpgradeRecipe.UpgradeKind::byName, FortressArmorUpgradeRecipe.UpgradeKind::getSerializedName),
            recipe -> recipe.kind,
            ByteBufCodecs.VAR_INT,
            recipe -> recipe.instability,
            AspectList.STREAM_CODEC,
            recipe -> recipe.aspects,
            Ingredient.CONTENTS_STREAM_CODEC,
            recipe -> recipe.central,
            ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC),
            recipe -> recipe.components,
            FortressArmorUpgradeRecipe::new
         );
      }
   }

   public static enum UpgradeKind {
      GOGGLES("goggles", -1) {
         @Override
         public boolean canApplyTo(ItemStack stack) {
            CompoundTag tag = ItemStackData.getTag(stack);
            return !stack.isEmpty() && (tag == null || !tag.contains("goggles"));
         }

         @Override
         public void apply(ItemStack stack) {
            ItemStackData.addTagElement(stack, "goggles", ByteTag.valueOf((byte)1));
         }
      },
      MASK_GRINNING_DEVIL("mask_grinning_devil", 0),
      MASK_ANGRY_GHOST("mask_angry_ghost", 1),
      MASK_SIPPING_FIEND("mask_sipping_fiend", 2);

      private final String id;
      private final int maskId;

      private UpgradeKind(String id, int maskId) {
         this.id = id;
         this.maskId = maskId;
      }

      public boolean canApplyTo(ItemStack stack) {
         CompoundTag tag = ItemStackData.getTag(stack);
         return !stack.isEmpty() && (tag == null || !tag.contains("mask"));
      }

      public void apply(ItemStack stack) {
         ItemStackData.getOrCreateTag(stack).putInt("mask", this.maskId);
      }

      public String getSerializedName() {
         return this.id;
      }

      public static FortressArmorUpgradeRecipe.UpgradeKind byName(String name) {
         String normalized = name.toLowerCase(Locale.ROOT);

         for (FortressArmorUpgradeRecipe.UpgradeKind kind : values()) {
            if (kind.id.equals(normalized)) {
               return kind;
            }
         }

         throw new JsonSyntaxException("Unknown fortress armor upgrade kind: " + name);
      }
   }
}
