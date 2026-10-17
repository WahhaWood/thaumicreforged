package thaumicenergistics.init;

import appeng.crafting.pattern.EncodedPatternItem;
import java.util.function.Consumer;
import thaumicenergistics.ae2.recipe.RecipePatternItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
   public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "thaumicenergistics");
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTER.register(
      "main",
      () -> CreativeModeTab.builder()
            .title(Component.translatable("item_group.thaumicenergistics.name"))
            .icon(() -> new ItemStack((ItemLike)ModBlocks.ESSENTIA_PROVIDER.get()))
            .displayItems((params, output) -> displayItems(ModItems.REGISTER, output::accept))
            .build()
   );

   public static void displayItems(DeferredRegister<Item> register, Consumer<ItemStack> consumer) {
      try {
         for (DeferredHolder<Item, ? extends Item> object : register.getEntries()) {
            if (object != null && !(object.get() instanceof EncodedPatternItem) && !(object.get() instanceof RecipePatternItem)) {
               consumer.accept(new ItemStack((ItemLike)object.get()));
            }
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
   }
}
