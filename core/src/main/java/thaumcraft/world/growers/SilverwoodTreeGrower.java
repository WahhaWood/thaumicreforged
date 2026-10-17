package thaumcraft.world.growers;

import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import thaumcraft.Thaumcraft;

public final class SilverwoodTreeGrower {
   public static final TreeGrower GROWER = new TreeGrower(
      "silverwood", Optional.empty(), Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, Thaumcraft.location("silverwood_tree"))), Optional.empty()
   );

   private SilverwoodTreeGrower() {
   }
}
