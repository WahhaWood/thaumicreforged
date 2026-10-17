package thaumcraft.init.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageType {
   public static final ResourceKey<DamageType> TAINT = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("thaumcraft", "taint"));
   public static final ResourceKey<DamageType> TENTACLE = ResourceKey.create(
      Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("thaumcraft", "tentacle")
   );
   public static final ResourceKey<DamageType> DISSOLVE = ResourceKey.create(
      Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("thaumcraft", "dissolve")
   );
   public static final ResourceKey<DamageType> FIREBAT = ResourceKey.create(
      Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("thaumcraft", "firebat")
   );

   private ModDamageType() {
   }
}
