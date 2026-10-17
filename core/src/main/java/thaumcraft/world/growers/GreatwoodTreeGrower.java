package thaumcraft.world.growers;

import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import thaumcraft.Thaumcraft;

public final class GreatwoodTreeGrower {
   public static final TreeGrower GROWER = new TreeGrower(
      "greatwood", Optional.empty(), Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, Thaumcraft.location("greatwood_tree"))), Optional.empty()
   );

   private GreatwoodTreeGrower() {
   }
}
