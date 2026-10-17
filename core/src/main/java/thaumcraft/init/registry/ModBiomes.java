package thaumcraft.init.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import thaumcraft.Thaumcraft;

public class ModBiomes {
   public static final ResourceKey<Biome> TAINT = ResourceKey.create(Registries.BIOME, Thaumcraft.location("taint"));
   public static final ResourceKey<Biome> EERIE = ResourceKey.create(Registries.BIOME, Thaumcraft.location("eerie"));
   public static final ResourceKey<Biome> MAGICAL_FOREST = ResourceKey.create(Registries.BIOME, Thaumcraft.location("magical_forest"));
   public static final ResourceKey<Biome> ELDRITCH_LANDS = ResourceKey.create(Registries.BIOME, Thaumcraft.location("eldritch_lands"));
}
