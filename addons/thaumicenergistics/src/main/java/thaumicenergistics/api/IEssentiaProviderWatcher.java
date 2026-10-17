package thaumicenergistics.api;


public interface IEssentiaProviderWatcher {
   void onProviderBroken();

   void onProviderPowerChange(boolean var1);
}
