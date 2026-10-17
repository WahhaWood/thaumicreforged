package thaumcraft.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface ThaumcraftKubeJSEvents {
   EventGroup GROUP = EventGroup.of("ThaumcraftEvents");
   EventHandler ASPECTS = GROUP.server("aspects", () -> ThaumcraftAspectEventJS.class);
   EventHandler RESEARCH = GROUP.server("research", () -> ThaumcraftResearchEventJS.class);
}
