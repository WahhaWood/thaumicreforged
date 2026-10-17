package thaumcraft.api.aspects;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface IEssenceTransport {
   boolean isConnectable(Direction var1);

   boolean canInputFrom(Direction var1);

   boolean canOutputTo(Direction var1);

   void setSuction(Aspect var1, int var2);

   Aspect getSuctionType(@Nullable Direction var1);

   int getSuctionAmount(@Nullable Direction var1);

   int extractEssence(Aspect var1, int var2, Direction var3);

   int injectEssence(Aspect var1, int var2, Direction var3);

   Aspect getEssenceType(@Nullable Direction var1);

   int getEssenceAmount(Direction var1);

   int getMinimumSuction();

   boolean renderExtendedTube();
}
