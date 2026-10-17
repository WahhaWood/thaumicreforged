package thaumcraft.block.warp_gate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.sky_pearl.ItemSkyPearl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class WarpGateBlockEntity extends ThaumcraftSideInvBlockEntity implements MenuProvider, ITickable {
   private static final String TAG_LOCKED = "locked";
   private static final int INVENTORY_SIZE = 10;
   private static final int GUI_TRIGGER_COOLDOWN_TICKS = 10;
   private boolean locked;
   private boolean teleportedThisTick;
   private int guiTriggerCooldown;

   public WarpGateBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.WARP_GATE.get(), pos, state, 10);
   }

   public boolean isLocked() {
      return this.locked;
   }

   public void setLocked(boolean locked) {
      if (this.locked != locked) {
         this.locked = locked;
         this.setChanged();
         this.markForUpdate();
      }
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.loadGateData(compound);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.loadGateData(compound);
   }

   private void loadGateData(@NotNull CompoundTag compound) {
      this.locked = compound.getBoolean("locked");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.saveGateData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.saveGateData(compound);
   }

   private void saveGateData(@NotNull CompoundTag compound) {
      compound.putBoolean("locked", this.locked);
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      this.teleportedThisTick = false;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.teleportedThisTick = false;
      if (this.guiTriggerCooldown > 0) {
         this.guiTriggerCooldown--;
      }

      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer localPlayer = minecraft.player;
      if (localPlayer != null && minecraft.level == level && this.guiTriggerCooldown <= 0) {
         if (localPlayer.isShiftKeyDown()) {
            AABB triggerZone = new AABB(pos.getX(), pos.getY() + 1.0, pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.5, pos.getZ() + 1.0);
            if (triggerZone.contains(localPlayer.position())) {
               if (!(minecraft.screen instanceof WarpGateDestinationsScreen)) {
                  minecraft.setScreen(new WarpGateDestinationsScreen(this));
                  this.guiTriggerCooldown = 10;
               }
            }
         }
      }
   }

   public boolean teleportPlayer(ServerPlayer player, int index) {
      if (!this.teleportedThisTick && index >= 0 && index < this.getContainerSize()) {
         if (!player.level().dimension().location().toString().equals(this.getLevelDimension(this.getItem(index)))) {
            return false;
         } else if (!this.teleportPlayer(player, this.getItem(index))) {
            return false;
         } else {
            this.teleportedThisTick = true;
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean teleportPlayer(ServerPlayer player, ItemStack stack) {
      if (!stack.isEmpty() && ItemSkyPearl.isAttuned(stack)) {
         ServerLevel serverLevel = player.server.getLevel(player.level().dimension());
         if (serverLevel == null) {
            return false;
         } else {
            BlockPos destinationPos = new BlockPos(ItemSkyPearl.getX(stack), ItemSkyPearl.getY(stack), ItemSkyPearl.getZ(stack));
            if (serverLevel.getBlockEntity(destinationPos) instanceof WarpGateBlockEntity destinationGate) {
               if (destinationGate.isLocked()) {
                  player.sendSystemMessage(Component.translatable("ttmisc.noTeleport"));
                  return false;
               } else {
                  Level level = player.level();
                  level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.0F);
                  player.stopRiding();
                  player.teleportTo(
                     serverLevel, destinationPos.getX() + 0.5, destinationPos.getY() + 1.6, destinationPos.getZ() + 0.5, player.getYRot(), player.getXRot()
                  );
                  serverLevel.playSound(null, destinationPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.7F);
                  return true;
               }
            } else {
               player.sendSystemMessage(Component.translatable("ttmisc.noDest"));
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private String getLevelDimension(ItemStack stack) {
      return ItemSkyPearl.isAttuned(stack) ? ItemSkyPearl.getDim(stack) : "";
   }

   @NotNull
   public Component getDisplayName() {
      return Component.translatable("block.thaumcraft.warp_gate");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
      return new WarpGateMenu(id, inventory, this);
   }

   public int getMaxStackSize() {
      return 1;
   }

   public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
      return stack.getItem() == ModItems.SKY_PEARL.get();
   }
}
