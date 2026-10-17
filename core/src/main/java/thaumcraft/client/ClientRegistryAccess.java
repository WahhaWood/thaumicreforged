package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

/** Registries of the connected client world; null outside a world. Load only on the client. */
public final class ClientRegistryAccess {
   private ClientRegistryAccess() {
   }

   @Nullable
   public static HolderLookup.Provider get() {
      ClientLevel level = Minecraft.getInstance().level;
      return level == null ? null : level.registryAccess();
   }
}
