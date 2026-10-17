package thaumcraft.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class ThaumcraftKubeJSPlugin implements KubeJSPlugin {
   public void registerEvents(EventGroupRegistry registry) {
      registry.register(ThaumcraftKubeJSEvents.GROUP);
   }

   public void registerBindings(BindingRegistry event) {
      if (event.type().isServer()) {
         event.add("ThaumcraftEvents", ThaumcraftEventBindings.INSTANCE);
      }
   }

   public void registerClasses(ClassFilter filter) {
      if (filter.scriptType.isServer()) {
         filter.allow("thaumcraft.compat.kubejs");
      }
   }
}
