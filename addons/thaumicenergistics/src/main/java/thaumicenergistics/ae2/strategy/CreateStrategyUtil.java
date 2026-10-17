package thaumicenergistics.ae2.strategy;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import thaumicenergistics.ae2.facade.AspectHandlerFacade;
import thaumicenergistics.ae2.storage.AEAspectKey;
import thaumicenergistics.ae2.storage.AEAspectKeyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.IAspectContainer;

public class CreateStrategyUtil {
   public static StackExportStrategy createExport(ServerLevel level, BlockPos fromPos, Direction fromSide) {
      return new CreateStrategyUtil.AspectExportStrategy(level, fromPos, fromSide);
   }

   public static StackImportStrategy createImport(ServerLevel level, BlockPos fromPos, Direction fromSide) {
      return new CreateStrategyUtil.AspectImportStrategy(level, fromPos, fromSide);
   }

   public static ExternalStorageStrategy createExternal(ServerLevel level, BlockPos fromPos, Direction fromSide) {
      return new CreateStrategyUtil.AspectExternalStorageStrategy(level, fromPos, fromSide);
   }

   @Nullable
   public static IAspectContainer getContainer(ServerLevel level, BlockPos pos) {
      BlockEntity be = level.getBlockEntity(pos);
      return be instanceof IAspectContainer ? (IAspectContainer)be : null;
   }

   public static class AspectExportStrategy implements StackExportStrategy {
      private final ServerLevel level;
      private final BlockPos fromPos;
      private final Direction fromSide;

      public AspectExportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
         this.level = level;
         this.fromPos = fromPos;
         this.fromSide = fromSide;
      }

      public long transfer(StackTransferContext context, AEKey what, long amount) {
         if (!(what instanceof AEAspectKey)) {
            return 0L;
         } else {
            IAspectContainer container = CreateStrategyUtil.getContainer(this.level, this.fromPos);
            if (container == null) {
               return 0L;
            } else {
               AspectHandlerFacade facade = new AspectHandlerFacade(container, this.fromSide);
               IStorageService inv = context.getInternalStorage();
               long extracted = StorageHelper.poweredExtraction(
                  context.getEnergySource(), inv.getInventory(), what, amount, context.getActionSource(), Actionable.SIMULATE
               );
               long wasInserted = facade.insert(what, extracted, Actionable.SIMULATE, context.getActionSource());
               if (wasInserted > 0L) {
                  extracted = StorageHelper.poweredExtraction(
                     context.getEnergySource(), inv.getInventory(), what, wasInserted, context.getActionSource(), Actionable.MODULATE
                  );
                  wasInserted = facade.insert(what, extracted, Actionable.MODULATE, context.getActionSource());
                  if (wasInserted < extracted) {
                     long leftover = extracted - wasInserted;
                     inv.getInventory().insert(what, leftover, Actionable.MODULATE, context.getActionSource());
                  }
               }

               return wasInserted;
            }
         }
      }

      public long push(AEKey what, long amount, Actionable mode) {
         if (!(what instanceof AEAspectKey)) {
            return 0L;
         } else {
            IAspectContainer container = CreateStrategyUtil.getContainer(this.level, this.fromPos);
            if (container == null) {
               return 0L;
            } else {
               AspectHandlerFacade facade = new AspectHandlerFacade(container, this.fromSide);
               return facade.insert(what, amount, mode, null);
            }
         }
      }
   }

   public static class AspectExternalStorageStrategy implements ExternalStorageStrategy {
      private final ServerLevel level;
      private final BlockPos fromPos;
      private final Direction fromSide;

      public AspectExternalStorageStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
         this.level = level;
         this.fromPos = fromPos;
         this.fromSide = fromSide;
      }

      @Nullable
      public MEStorage createWrapper(boolean extractableOnly, @Nullable Runnable injectOrExtractCallback) {
         IAspectContainer container = CreateStrategyUtil.getContainer(this.level, this.fromPos);
         if (container == null) {
            return null;
         } else {
            AspectHandlerFacade facade = new AspectHandlerFacade(container, this.fromSide);
            facade.setChangeListener(injectOrExtractCallback);
            facade.setExtractableOnly(extractableOnly);
            return facade;
         }
      }
   }

   public static class AspectImportStrategy implements StackImportStrategy {
      private final ServerLevel level;
      private final BlockPos fromPos;
      private final Direction fromSide;

      public AspectImportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
         this.level = level;
         this.fromPos = fromPos;
         this.fromSide = fromSide;
      }

      public boolean transfer(StackTransferContext context) {
         if (!context.isKeyTypeEnabled(AEAspectKeyType.INSTANCE)) {
            return false;
         } else {
            IAspectContainer container = CreateStrategyUtil.getContainer(this.level, this.fromPos);
            if (container == null) {
               return false;
            } else {
               AspectHandlerFacade facade = new AspectHandlerFacade(container, this.fromSide);
               long remainingTransferAmount = (long)context.getOperationsRemaining() * (long)AEAspectKeyType.INSTANCE.getAmountPerOperation();
               IStorageService inv = context.getInternalStorage();

               for (int i = 0; i < facade.getSlots() && remainingTransferAmount > 0L; i++) {
                  GenericStack resource = facade.getStackInSlot(i);
                  if (resource != null && context.isInFilter(resource.what()) != context.isInverted()) {
                     long amountForThisResource = inv.getInventory()
                        .insert(resource.what(), remainingTransferAmount, Actionable.SIMULATE, context.getActionSource());
                     long amount = facade.extract(resource.what(), amountForThisResource, Actionable.MODULATE, context.getActionSource());
                     if (amount > 0L) {
                        long inserted = inv.getInventory().insert(resource.what(), amount, Actionable.MODULATE, context.getActionSource());
                        if (inserted < amount) {
                           facade.insert(resource.what(), amount - inserted, Actionable.MODULATE, context.getActionSource());
                        }

                        long opsUsed = Math.max(1L, inserted / (long)AEAspectKeyType.INSTANCE.getAmountPerOperation());
                        context.reduceOperationsRemaining(opsUsed);
                        remainingTransferAmount -= inserted;
                     }
                  }
               }

               return false;
            }
         }
      }
   }
}
