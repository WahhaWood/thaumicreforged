package thaumicenergistics.block.provider;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.storage.MEStorage;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import thaumicenergistics.ae2.storage.AEAspectKey;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.IEssentiaProviderWatcher;
import thaumicenergistics.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXEssenceSource;

public class EssentiaProviderBlockEntity extends ProviderBaseBlockEntity implements IEssenceTransport {
   private static final int TICK_RATE_IDLE = 15;
   private static final int TICK_RATE_URGENT = 5;
   private int tickCount = 0;
   private int tickRate = 15;
   private Set<WeakReference<IEssentiaProviderWatcher>> blockWatchers;

   public EssentiaProviderBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.ESSENTIA_PROVIDER.get(), pos, state);
   }

   @Override
   protected double getIdlePowerUsage() {
      return 3.0;
   }

   public boolean isConnectable(Direction direction) {
      return true;
   }

   public boolean canInputFrom(Direction direction) {
      return true;
   }

   public boolean canOutputTo(Direction direction) {
      return true;
   }

   public void setSuction(Aspect aspect, int amount) {
   }

   @Nullable
   public Aspect getSuctionType(@Nullable Direction direction) {
      return null;
   }

   public int getSuctionAmount(@Nullable Direction direction) {
      return 8;
   }

   public int getMinimumSuction() {
      return 1;
   }

   public int extractEssence(Aspect aspect, int amount, Direction direction) {
      return this.extractEssentiaFromNetwork(aspect, amount, false);
   }

   public int injectEssence(Aspect aspect, int amount, Direction direction) {
      return this.addEssentia(aspect, amount, direction, Actionable.MODULATE);
   }

   @Nullable
   public Aspect getEssenceType(@Nullable Direction direction) {
      Aspect wanted = this.getNeighborWantedAspect(direction);
      return wanted != null && this.getAspectAmountInNetwork(wanted) > 0L ? wanted : null;
   }

   public int getEssenceAmount(Direction direction) {
      Aspect wanted = this.getNeighborWantedAspect(direction);
      return wanted != null ? (int)Math.min(this.getAspectAmountInNetwork(wanted), 2147483647L) : 0;
   }

   public boolean renderExtendedTube() {
      return false;
   }

   public int addEssentia(Aspect aspect, int amount, Direction side, Actionable mode) {
      IGrid grid = this.getMainNode().getGrid();
      if (grid == null) {
         return 0;
      } else {
         AEAspectKey key = AEAspectKey.of(aspect);
         if (key == null) {
            return 0;
         } else {
            MEStorage storage = grid.getStorageService().getInventory();
            long inserted = storage.insert(key, (long)amount, mode, this.getMachineSource());
            if (mode == Actionable.MODULATE && inserted > 0L) {
               this.tickRate = 5;
            }

            return (int)Math.min(inserted, 2147483647L);
         }
      }
   }

   @Nullable
   private Aspect getNeighborWantedAspect(@Nullable Direction side) {
      if (side != null && this.level != null) {
         return this.level.getBlockEntity(this.worldPosition.relative(side)) instanceof IEssenceTransport transport
            ? transport.getSuctionType(side.getOpposite())
            : null;
      } else {
         return null;
      }
   }

   private void takeEssentiaFromNeighbors() {
      if (this.level != null) {
         for (Direction dir : Direction.values()) {
            BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(dir));
            if (neighbor instanceof IEssenceTransport) {
               IEssenceTransport transport = (IEssenceTransport)neighbor;
               if (transport.canOutputTo(dir.getOpposite())) {
                  int neighborSuction = transport.getSuctionAmount(dir.getOpposite());
                  if (neighborSuction < this.getSuctionAmount(dir)) {
                     Aspect essenceType = transport.getEssenceType(dir.getOpposite());
                     if (essenceType != null) {
                        int available = transport.getEssenceAmount(dir.getOpposite());
                        if (available > 0) {
                           int toTake = Math.min(available, 1);
                           int injected = this.addEssentia(essenceType, toTake, dir, Actionable.SIMULATE);
                           if (injected > 0) {
                              int extracted = transport.extractEssence(essenceType, injected, dir.getOpposite());
                              if (extracted > 0) {
                                 int accepted = this.addEssentia(essenceType, extracted, dir, Actionable.MODULATE);
                                 if (accepted > 0 && this.level instanceof net.minecraft.server.level.ServerLevel) {
                                    BlockPos sourcePos = this.worldPosition.relative(dir);
                                    PacketHandler.sendToAllAround(
                                       new PacketFXEssenceSource(
                                          sourcePos,
                                          (byte)(sourcePos.getX() - this.worldPosition.getX()),
                                          (byte)(sourcePos.getY() - this.worldPosition.getY()),
                                          (byte)(sourcePos.getZ() - this.worldPosition.getZ()),
                                          essenceType.getColor()
                                       ),
                                       this.level,
                                       this.worldPosition,
                                       32.0
                                    );
                                 }
                                 if (accepted < extracted) {
                                    int leftover = extracted - accepted;
                                    int putBack = 0;
                                    try {
                                       putBack = transport.injectEssence(essenceType, leftover, dir.getOpposite());
                                    } catch (Exception ignored) {
                                    }
                                    if (putBack < leftover) {
                                       ThaumicEnergistics.LOGGER.warn("Essentia provider at {} voided {} {} (source refused pushback)",
                                          this.worldPosition, leftover - putBack, essenceType.getTag());
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void serverTick() {
      if (this.level == null || this.level.isClientSide()) {
         return;
      }

      if (!this.isActive()) {
         return;
      }

      this.tickCount++;
      if (this.tickCount >= this.tickRate) {
         this.tickCount = 0;
         this.tickRate = TICK_RATE_IDLE;
         this.takeEssentiaFromNeighbors();
      }
   }

   public void registerBlockWatcher(IEssentiaProviderWatcher watcher) {
      if (this.blockWatchers == null) {
         this.blockWatchers = new HashSet<>();
      }

      this.blockWatchers.add(new WeakReference<>(watcher));
   }

   @Override
   protected void onPowerChange(boolean isPowered) {
      this.notifyWatchers(w -> w.onProviderPowerChange(isPowered));
   }

   @Override
   public void onBreakBlock() {
      this.notifyWatchers(IEssentiaProviderWatcher::onProviderBroken);
      if (this.blockWatchers != null) {
         this.blockWatchers.clear();
      }
   }

   private void notifyWatchers(Consumer<IEssentiaProviderWatcher> action) {
      if (this.blockWatchers != null) {
         Iterator<WeakReference<IEssentiaProviderWatcher>> iter = this.blockWatchers.iterator();

         while (iter.hasNext()) {
            IEssentiaProviderWatcher watcher = iter.next().get();
            if (watcher != null) {
               action.accept(watcher);
            } else {
               iter.remove();
            }
         }
      }
   }
}
