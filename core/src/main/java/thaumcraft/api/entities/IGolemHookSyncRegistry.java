package thaumcraft.api.entities;

public interface IGolemHookSyncRegistry {
   char getSyncCharOrDefault(int var1, char var2);

   int registerSyncChar(IGolemHookHandler var1, char var2);

   void updateSyncChar(IGolemHookHandler var1, int var2, char var3);
}
