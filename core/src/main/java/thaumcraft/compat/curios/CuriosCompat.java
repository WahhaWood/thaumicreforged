package thaumcraft.compat.curios;

import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import thaumcraft.init.registry.ModItems;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Registers every {@link ICurioItem} in our item registry with Curios so the
 * curios inventory (and other mods querying it) accepts and ticks them.
 *
 * <p>Curios 9.x no longer reads slot support from item code alone: an item is
 * only usable in curios slots once it has a curios item-capability, which the
 * official runtime path provides via {@link CuriosApi#registerCurio}.
 * Scanning the registry instead of listing fields keeps future baubles working
 * without touching this method.</p>
 */
public final class CuriosCompat {
   private CuriosCompat() {
   }

   public static void registerCurios(FMLCommonSetupEvent event) {
      event.enqueueWork(() -> {
         for (DeferredHolder<Item, ? extends Item> holder : ModItems.REGISTER.getEntries()) {
            Item item = holder.get();
            if (item instanceof ICurioItem curio) {
               CuriosApi.registerCurio(item, curio);
            }
         }
      });
   }
}
