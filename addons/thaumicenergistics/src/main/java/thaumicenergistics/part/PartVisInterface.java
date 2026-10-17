package thaumicenergistics.part;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import appeng.util.InteractionUtil;
import java.lang.ref.WeakReference;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.IDigiVisSource;
import thaumicenergistics.integration.tc.DigiVisSourceData;
import thaumicenergistics.integration.tc.VisProviderProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.block.vis_relay.VisRelayBlockEntity;

public class PartVisInterface extends AEBasePart implements IGridTickable, IDigiVisSource {
   private static final String NBT_KEY_UID = "uid";
   private static final String NBT_KEY_IS_PROVIDER = "isProvider";
   private static final String NBT_KEY_PROVIDER_SOURCE = "linkedSource";
   private static final int TIME_TO_CLEAR = 500;
   private static final int POWER_PER_REQUESTED_VIS = 4;
   public static final ResourceLocation MODEL = ThaumicEnergistics.location("part/vis_interface");
   public static final IPartModel MODELS = new PartModel(MODEL);
   private long UID = 0L;
   private int visDrainingColor = 0;
   private long lastColorUpdate = 0L;
   private WeakReference<VisRelayBlockEntity> cachedRelay = new WeakReference<>(null);
   private boolean isProvider = false;
   private DigiVisSourceData visP2PSourceInfo = new DigiVisSourceData();
   private VisProviderProxy visProviderSubTile = null;

   public PartVisInterface(IPartItem<?> partItem) {
      super(partItem);
      this.getMainNode().setIdlePowerUsage(0.0).addService(IGridTickable.class, this);
      this.UID = System.currentTimeMillis() ^ (long)this.hashCode();
   }

   private int consumeVisFromVisNetwork(Aspect digiVisAspect, int amount) {
      VisRelayBlockEntity visRelay = this.getRelay();
      if (visRelay == null) {
         return 0;
      } else {
         IGrid grid = this.getGrid();
         if (grid == null) {
            return 0;
         } else {
            IEnergyService eGrid = grid.getEnergyService();
            if (eGrid == null) {
               return 0;
            } else {
               double drainedPower = eGrid.extractAEPower(4.0, Actionable.SIMULATE, PowerMultiplier.CONFIG);
               if (drainedPower < 4.0) {
                  return 0;
               } else {
                  int amountReceived = visRelay.consumeVis(digiVisAspect, amount);
                  if (amountReceived > 0) {
                     eGrid.extractAEPower(4.0, Actionable.MODULATE, PowerMultiplier.CONFIG);
                  }

                  return amountReceived;
               }
            }
         }
      }
   }

   private boolean isP2PSourceValid() {
      if (!this.visP2PSourceInfo.hasSourceData()) {
         return false;
      } else {
         IDigiVisSource p2pSource = this.visP2PSourceInfo.tryGetSource(this.getGrid());
         if (p2pSource == null) {
            return false;
         } else if (!(p2pSource instanceof PartVisInterface)) {
            return false;
         } else {
            return this.equals(p2pSource) ? false : !((PartVisInterface)p2pSource).isVisProvider();
         }
      }
   }

   private void setDrainColor(int color) {
      if (color != 0) {
         if (color == this.visDrainingColor) {
            this.lastColorUpdate = System.currentTimeMillis();
            return;
         }

         if (System.currentTimeMillis() - this.lastColorUpdate <= 250L) {
            return;
         }

         this.lastColorUpdate = System.currentTimeMillis();
      }

      this.visDrainingColor = color;
      this.getHost().markForUpdate();
   }

   private void setIsVisProvider(boolean isProviding) {
      if (!isProviding) {
         this.visP2PSourceInfo.clearData();
         if (this.visProviderSubTile != null) {
            this.visProviderSubTile.setRemoved();
            this.visProviderSubTile = null;
         }
      }

      this.isProvider = isProviding;
   }

   @Override
   public int consumeVis(Aspect digiVisAspect, int amount) {
      if (!this.isActive()) {
         return 0;
      } else {
         int amountReceived = 0;
         if (this.isProvider) {
            if (this.isP2PSourceValid()) {
               IDigiVisSource source = this.visP2PSourceInfo.tryGetSource(this.getGrid());
               amountReceived = source.consumeVis(digiVisAspect, amount);
            }
         } else {
            amountReceived = this.consumeVisFromVisNetwork(digiVisAspect, amount);
         }

         if (amountReceived > 0) {
            this.setDrainColor(digiVisAspect.getColor());
         }

         return amountReceived;
      }
   }

   @Override
   public IGrid getGrid() {
      return this.getMainNode().getGrid();
   }

   @Override
   public long getUID() {
      return this.UID;
   }

   public void getBoxes(IPartCollisionHelper helper) {
      helper.addBox(6.0, 6.0, 15.0, 10.0, 10.0, 16.0);
      helper.addBox(4.0, 4.0, 14.0, 12.0, 12.0, 15.0);
      helper.addBox(5.0, 5.0, 13.0, 11.0, 11.0, 14.0);
   }

   public float getCableConnectionLength(AECableType cable) {
      return 2.0F;
   }

   public IPartModel getStaticModels() {
      return MODELS;
   }

   public VisRelayBlockEntity getRelay() {
      VisRelayBlockEntity tVR = this.cachedRelay.get();
      BlockEntity tile = this.getBlockEntity();
      if (tile != null && tile.getLevel() != null) {
         Level level = tile.getLevel();
         if (tVR != null && tVR == level.getBlockEntity(tVR.getBlockPos())) {
            return tVR;
         } else {
            Direction side = this.getSide();
            if (side == null) {
               return null;
            } else {
               BlockPos facingPos = tile.getBlockPos().relative(side);
               if (level.getBlockEntity(facingPos) instanceof VisRelayBlockEntity relay) {
                  try {
                     if (relay.getFacing().ordinal() == side.getOpposite().ordinal()) {
                        this.cachedRelay = new WeakReference<>(relay);
                        return relay;
                     }
                  } catch (Exception var9) {
                     this.cachedRelay = new WeakReference<>(relay);
                     return relay;
                  }
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }

   public boolean isVisProvider() {
      return this.isProvider;
   }

   public boolean useStandardMemoryCard() {
      return false;
   }

   public boolean onUseItemOn(ItemStack playerHolding, Player player, InteractionHand hand, Vec3 position) {
      return !playerHolding.isEmpty() && playerHolding.getItem() instanceof IMemoryCard memoryCard
         ? this.useMemoryCard(playerHolding, player, memoryCard, InteractionUtil.isInAlternateUseMode(player))
         : false;
   }

   public boolean onShiftClicked(Player player, Vec3 position) {
      ItemStack playerHolding = player.getMainHandItem();
      return !playerHolding.isEmpty() && playerHolding.getItem() instanceof IMemoryCard memoryCard
         ? this.useMemoryCard(playerHolding, player, memoryCard, true)
         : false;
   }

   private boolean useMemoryCard(ItemStack playerHolding, Player player, IMemoryCard memoryCard, boolean saveSource) {
      Level level = this.getLevel();
      if (level == null || level.isClientSide()) {
         return true;
      } else if (saveSource) {
         DigiVisSourceData sourceData = new DigiVisSourceData(this);
         sourceData.writeToMemoryCard(playerHolding, this.getPartItem().asItem().getDescription());
         memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
         this.setIsVisProvider(false);
         this.getHost().markForSave();
         return true;
      } else {
         if (this.visP2PSourceInfo.readFromMemoryCard(playerHolding)) {
            if (this.isP2PSourceValid()) {
               this.setIsVisProvider(true);
               memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
            } else {
               this.visP2PSourceInfo.clearData();
               memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
            }

            this.getHost().markForSave();
         } else if (DigiVisSourceData.isMemoryCardBlank(playerHolding)) {
            if (this.isProvider) {
               this.setIsVisProvider(false);
               memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
               this.getHost().markForSave();
            }
         } else {
            memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
         }

         return true;
      }
   }

   public TickingRequest getTickingRequest(IGridNode node) {
      return new TickingRequest(30, 30, false);
   }

   public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
      if (this.visDrainingColor != 0 && System.currentTimeMillis() - this.lastColorUpdate > 500L) {
         this.setDrainColor(0);
      }

      if (this.isProvider) {
         boolean hasProvider = this.visProviderSubTile != null;
         if (!this.isP2PSourceValid()) {
            if (hasProvider) {
               this.visProviderSubTile.setRemoved();
               this.visProviderSubTile = null;
            }
         } else {
            boolean hasRelay = this.getRelay() != null;
            if (!hasRelay && hasProvider) {
               this.visProviderSubTile.setRemoved();
               this.visProviderSubTile = null;
            } else if (hasRelay && !hasProvider) {
               this.visProviderSubTile = new VisProviderProxy(this);
               Level level = this.getLevel();
               if (level != null) {
                  VisNetHandler.addSource(level, this.visProviderSubTile);
               }
            } else if (hasProvider) {
               this.visProviderSubTile.tick();
            }
         }
      }

      return TickRateModulation.SAME;
   }

   public void readFromNBT(CompoundTag data, Provider registries) {
      super.readFromNBT(data, registries);
      if (data.contains("uid")) {
         this.UID = data.getLong("uid");
      }

      if (data.contains("isProvider")) {
         this.isProvider = data.getBoolean("isProvider");
         if (data.contains("linkedSource")) {
            this.visP2PSourceInfo.readFromNBT(data, "linkedSource");
         }
      }
   }

   public void writeToNBT(CompoundTag data, Provider registries) {
      super.writeToNBT(data, registries);
      data.putLong("uid", this.UID);
      if (this.isProvider) {
         data.putBoolean("isProvider", this.isProvider);
         this.visP2PSourceInfo.writeToNBT(data, "linkedSource");
      }
   }

   public void writeToStream(RegistryFriendlyByteBuf data) {
      super.writeToStream(data);
      data.writeInt(this.visDrainingColor);
   }

   public boolean readFromStream(RegistryFriendlyByteBuf data) {
      boolean redraw = super.readFromStream(data);
      int oldColor = this.visDrainingColor;
      this.visDrainingColor = data.readInt();
      return redraw | this.visDrainingColor != oldColor;
   }
}
