package thaumicenergistics.api;

import appeng.api.networking.IGrid;
import thaumcraft.api.aspects.Aspect;

public interface IDigiVisSource {
   int consumeVis(Aspect var1, int var2);

   IGrid getGrid();

   long getUID();

   boolean isActive();
}
