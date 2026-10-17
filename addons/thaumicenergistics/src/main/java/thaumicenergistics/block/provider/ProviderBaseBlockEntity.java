package thaumicenergistics.block.provider;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener.State;
import appeng.api.storage.MEStorage;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.me.helpers.MachineSource;
import thaumicenergistics.ae2.storage.AEAspectKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;

public abstract class ProviderBaseBlockEntity extends AENetworkedBlockEntity implements IColorableBlockEntity {
   private static final String NBT_KEY_COLOR = "TEColor";
   private static final String NBT_KEY_ATTACHMENT = "TEAttachSide";
   private static final String NBT_KEY_COLOR_FORCED = "ColorForced";
   private final MachineSource machineSource;
   protected int attachmentSide;
   private AEColor gridColor = AEColor.TRANSPARENT;
   protected boolean isActive;
   protected boolean isColorForced = false;

   public ProviderBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.machineSource = new MachineSource(this);
      this.attachmentSide = Direction.DOWN.ordinal();
   }

   public void setupProvider(int attachmentSide) {
      if (this.level != null && !this.level.isClientSide()) {
         this.attachmentSide = Mth.clamp(attachmentSide, 0, 5);
         this.getMainNode().setFlags(new GridFlags[]{GridFlags.REQUIRE_CHANNEL});
         this.getMainNode().setIdlePowerUsage(this.getIdlePowerUsage());
      }
   }

   protected abstract double getIdlePowerUsage();

   public AECableType getCableConnectionType(Direction dir) {
      return AECableType.SMART;
   }

   public AEColor getColor() {
      return this.gridColor;
   }

   protected void setProviderColor(AEColor color) {
      this.gridColor = color;
      this.getMainNode().setGridColor(color);
      if (this.level != null && !this.level.isClientSide()) {
         this.markForUpdate();
         this.saveChanges();
      }
   }

   public boolean recolourBlock(Direction side, AEColor color, @Nullable Player who) {
      this.isColorForced = true;
      this.setProviderColor(color);
      return true;
   }

   public void checkGridConnectionColor() {
      if (this.level != null && !this.level.isClientSide()) {
         if (!this.isColorForced) {
            AEColor[] sideColors = this.getNeighborCableColors();
            AEColor currentColor = this.gridColor;
            if (this.attachmentSide >= 0 && this.attachmentSide < 6 && sideColors[this.attachmentSide] != null) {
               if (sideColors[this.attachmentSide] != currentColor) {
                  this.setProviderColor(sideColors[this.attachmentSide]);
               }
            } else {
               for (int i = 0; i < 6; i++) {
                  if (sideColors[i] != null) {
                     if (sideColors[i] == currentColor) {
                        this.attachmentSide = i;
                        this.saveChanges();
                        return;
                     }

                     if (currentColor == AEColor.TRANSPARENT) {
                        this.attachmentSide = i;
                        this.setProviderColor(sideColors[i]);
                        return;
                     }
                  }
               }

               this.attachmentSide = -1;
               this.setProviderColor(AEColor.TRANSPARENT);
            }
         }
      }
   }

   private AEColor[] getNeighborCableColors() {
      AEColor[] colors = new AEColor[6];

      for (Direction dir : Direction.values()) {
         if (this.level.getBlockEntity(this.worldPosition.relative(dir)) instanceof CableBusBlockEntity cableBus) {
            colors[dir.ordinal()] = cableBus.getColor();
         }
      }

      return colors;
   }

   public boolean isActive() {
      if (this.level != null && !this.level.isClientSide()) {
         boolean wasActive = this.isActive;
         this.isActive = this.getMainNode().isActive();
         if (wasActive != this.isActive) {
            this.onPowerChange(this.isActive);
         }
      }

      return this.isActive;
   }

   protected void onPowerChange(boolean isPowered) {
   }

   public void onMainNodeStateChanged(State reason) {
      super.onMainNodeStateChanged(reason);
      if (reason == State.CHANNEL) {
         this.checkGridConnectionColor();
      }

      this.markForUpdate();
   }

   public MachineSource getMachineSource() {
      return this.machineSource;
   }

   protected int extractEssentiaFromNetwork(Aspect aspect, int amount, boolean mustMatch) {
      IGrid grid = this.getMainNode().getGrid();
      if (grid == null) {
         return 0;
      } else {
         AEAspectKey key = AEAspectKey.of(aspect);
         if (key == null) {
            return 0;
         } else {
            MEStorage storage = grid.getStorageService().getInventory();
            long simulated = storage.extract(key, (long)amount, Actionable.SIMULATE, this.machineSource);
            if (simulated == 0L) {
               return 0;
            } else if (mustMatch && simulated != (long)amount) {
               return 0;
            } else {
               long extracted = storage.extract(key, simulated, Actionable.MODULATE, this.machineSource);
               return (int)extracted;
            }
         }
      }
   }

   public long getAspectAmountInNetwork(Aspect aspect) {
      IGrid grid = this.getMainNode().getGrid();
      if (grid == null) {
         return 0L;
      } else {
         AEAspectKey key = AEAspectKey.of(aspect);
         return key == null ? 0L : grid.getStorageService().getCachedInventory().get(key);
      }
   }

   public void onBreakBlock() {
   }

   public void onReady() {
      super.onReady();
      this.checkGridConnectionColor();
   }

   public void setOwner(Player player) {
      this.getMainNode().setOwningPlayer(player);
   }

   public void loadTag(CompoundTag data, Provider registries) {
      super.loadTag(data, registries);
      if (data.contains("ColorForced")) {
         this.isColorForced = data.getBoolean("ColorForced");
      }

      if (data.contains("TEColor")) {
         int colorOrdinal = Mth.clamp(data.getInt("TEColor"), 0, AEColor.values().length - 1);
         this.setProviderColor(AEColor.values()[colorOrdinal]);
      }

      int attachSide = -1;
      if (data.contains("TEAttachSide")) {
         attachSide = data.getInt("TEAttachSide");
      }

      this.setupProvider(attachSide >= 0 && attachSide < 6 ? attachSide : 0);
   }

   public void saveAdditional(CompoundTag data, Provider registries) {
      super.saveAdditional(data, registries);
      data.putInt("TEColor", this.gridColor.ordinal());
      data.putInt("TEAttachSide", this.attachmentSide);
      data.putBoolean("ColorForced", this.isColorForced);
   }

   protected boolean readFromStream(RegistryFriendlyByteBuf data) {
      boolean changed = super.readFromStream(data);
      int colorOrdinal = Mth.clamp(data.readInt(), 0, AEColor.values().length - 1);
      AEColor newColor = AEColor.values()[colorOrdinal];
      if (newColor != this.gridColor) {
         this.gridColor = newColor;
         changed = true;
      }

      boolean active = data.readBoolean();
      if (active != this.isActive) {
         this.isActive = active;
         changed = true;
      }

      return changed;
   }

   protected void writeToStream(RegistryFriendlyByteBuf data) {
      super.writeToStream(data);
      data.writeInt(this.gridColor.ordinal());
      data.writeBoolean(this.isActive());
   }
}
