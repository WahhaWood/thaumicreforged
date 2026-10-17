package thaumicenergistics.ae2.facade;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.me.storage.ExternalStorageFacade;
import java.util.Set;
import thaumicenergistics.ae2.storage.AEAspectKey;
import thaumicenergistics.ae2.storage.AEAspectKeyType;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssenceTransport;

public class AspectHandlerFacade extends ExternalStorageFacade {
   private final IAspectContainer handler;
   private final Direction direction;

   public AspectHandlerFacade(IAspectContainer handler, Direction direction) {
      this.handler = handler;
      this.direction = direction;
   }

   public int getSlots() {
      AspectList aspectList = this.handler.getAspects();
      return aspectList == null ? 0 : aspectList.size();
   }

   @Nullable
   public GenericStack getStackInSlot(int slot) {
      AspectList aspectList = this.handler.getAspects();
      if (aspectList == null) {
         return null;
      } else {
         Aspect[] aspects = aspectList.getAspects();
         if (slot >= 0 && slot < aspects.length) {
            Aspect aspect = aspects[slot];
            if (aspect != null) {
               int amount = this.handler.containerContains(aspect);
               if (amount > 0) {
                  return new GenericStack(AEAspectKey.of(aspect), (long)amount);
               }
            }

            return null;
         } else {
            return null;
         }
      }
   }

   public AEKeyType getKeyType() {
      return AEAspectKeyType.INSTANCE;
   }

   protected int insertExternal(AEKey what, int amount, Actionable mode) {
      if (what instanceof AEAspectKey aspectKey) {
         Aspect aspect = aspectKey.getAspect();
         if (this.handler.doesContainerAccept(aspect)) {
            if (mode == Actionable.SIMULATE) {
               return amount;
            }

            int notInserted = this.handler.addToContainer(aspect, amount);
            return amount - notInserted;
         }
      }

      return 0;
   }

   protected int extractExternal(AEKey what, int amount, Actionable mode) {
      if (what instanceof AEAspectKey aspectKey) {
         Aspect aspect = aspectKey.getAspect();
         int availableAmount = this.handler.containerContains(aspect);
         if (this.handler instanceof IEssenceTransport transport) {
            Aspect essentiaType = transport.getEssenceType(this.direction);
            if (essentiaType == aspect) {
               availableAmount = Math.min(availableAmount, transport.getEssenceAmount(this.direction));
            } else if (essentiaType != null) {
               return 0;
            }
         }

         int toExtract = Math.min(amount, availableAmount);
         if (toExtract > 0) {
            if (mode == Actionable.SIMULATE) {
               return toExtract;
            }

            if (this.handler.takeFromContainer(aspect, toExtract)) {
               return toExtract;
            }
         }
      }

      return 0;
   }

   public boolean containsAnyFuzzy(Set<AEKey> keys) {
      AspectList aspectList = this.handler.getAspects();
      if (aspectList != null) {
         for (Aspect aspect : aspectList.getAspects()) {
            if (aspect != null && keys.contains(AEAspectKey.of(aspect))) {
               return true;
            }
         }
      }

      return false;
   }

   public void getAvailableStacks(KeyCounter out) {
      AspectList aspectList = this.handler.getAspects();
      if (aspectList != null) {
         for (Aspect aspect : aspectList.getAspects()) {
            if (aspect != null) {
               int amount = this.handler.containerContains(aspect);
               if (amount > 0) {
                  if (this.extractableOnly) {
                     IAspectContainer var9 = this.handler;
                     if (var9 instanceof IEssenceTransport) {
                        IEssenceTransport transport = (IEssenceTransport)var9;
                        if (transport.getEssenceType(this.direction) != aspect || transport.getEssenceAmount(this.direction) <= 0) {
                           continue;
                        }
                     }
                  }

                  AEAspectKey what = AEAspectKey.of(aspect);
                  if (what != null) {
                     out.add(what, (long)amount);
                  }
               }
            }
         }
      }
   }
}
