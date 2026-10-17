package thaumcraft.api.material;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public final class ArmorMaterialImpl {
   private ArmorMaterialImpl() {
   }

   public static ArmorMaterial create(
      String name,
      int durabilityMultiplier,
      EnumMap<Type, Integer> defenseForType,
      int enchantmentValue,
      float toughness,
      float knockbackResistance,
      @Nullable Supplier<Ingredient> repairIngredient
   ) {
      List<Layer> layers = List.of(new Layer(ResourceLocation.fromNamespaceAndPath("thaumcraft", name.toLowerCase(Locale.ROOT))));
      Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_IRON;
      return new ArmorMaterial(
         defenseForType,
         enchantmentValue,
         equipSound,
         repairIngredient == null ? () -> Ingredient.EMPTY : repairIngredient,
         layers,
         toughness,
         knockbackResistance
      );
   }
}
