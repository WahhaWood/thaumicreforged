package thaumcraft.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventHandler;

public final class ThaumcraftEventBindings {
   public static final ThaumcraftEventBindings INSTANCE = new ThaumcraftEventBindings();
   public final EventHandler aspects = ThaumcraftKubeJSEvents.ASPECTS;
   public final EventHandler research = ThaumcraftKubeJSEvents.RESEARCH;

   private ThaumcraftEventBindings() {
   }
}
