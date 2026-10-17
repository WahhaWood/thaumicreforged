package thaumcraft.block.infusion_claw;

import com.mojang.authlib.GameProfile;
import java.util.EnumMap;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ClickBehavior;
import thaumcraft.api.ITickable;
import thaumcraft.api.RegisteredClick;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.foci.ItemFocusPrimal;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.misc.PacketStartAnimation;
import thaumcraft.util.BlockEntityMenuValidator;
import thaumcraft.util.ItemStackData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class InfusionClawBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider, ITickable {
   private static final GameProfile FAKE_PROFILE = new GameProfile(UUID.fromString("b23c8c3f-d7bd-49b3-970a-8e86728bab82"), "[ThaumClaw]");
   private static final ItemWandCasting WAND_ITEM = (ItemWandCasting)ModItems.WAND_CASTING.get();
   private static final ItemFocusPrimal WAND_FOCUS = (ItemFocusPrimal)ModItems.FOCUS_PRIMAL.get();
   private static final AspectList MAX_WAND_COST = new AspectList()
      .add(Aspect.WATER, 250)
      .add(Aspect.AIR, 250)
      .add(Aspect.EARTH, 250)
      .add(Aspect.FIRE, 250)
      .add(Aspect.ORDER, 250)
      .add(Aspect.ENTROPY, 250);
   private Boolean redstoneState = null;
   private int count = 0;
   private UUID ownerUUID = null;
   private String ownerNameCache = "";
   private ItemStack wandStack = ItemStack.EMPTY;
   private boolean isLocked = false;
   private int cooldown;
   public float lastRenderTick;
   public float[] animationStates = new float[12];
   private final IItemHandler unsidedWrapper = new SidedInvWrapper(this, null);
   private final EnumMap<Direction, IItemHandler> sidedWrappers = new EnumMap<>(Direction.class);

   public InfusionClawBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.INFUSION_CLAW.get(), pos, state);
   }

   @OnlyIn(Dist.CLIENT)
   public void onLoad() {
      if (this.level != null && this.level.isClientSide) {
         Player unused = Minecraft.getInstance().player;
         this.lastRenderTick = unused == null ? 0.0F : unused.tickCount;
      }

      super.onLoad();
   }

   public boolean isLocked() {
      return this.isLocked;
   }

   public void setIsLocked(boolean isLocked) {
      this.isLocked = isLocked;
      this.markForUpdate();
   }

   public String getOwnerName() {
      return this.ownerNameCache;
   }

   public boolean hasOwner() {
      return this.ownerUUID != null;
   }

   private void markForUpdate() {
      this.setChanged();
      if (this.level != null) {
         BlockState st = this.getBlockState();
         this.level.sendBlockUpdated(this.worldPosition, st, st, 3);
      }
   }

   private ClickBehavior getClickBehavior(Level world, BlockPos pos) {
      return pos.getY() >= world.getMinBuildHeight()
            && !world.isClientSide
            && world instanceof ServerLevel
            && this.hasOwner()
            && !world.isEmptyBlock(pos)
            && !this.wandStack.isEmpty()
         ? RegisteredClick.getClawClickBehavior(world, pos)
         : null;
   }

   public boolean isRunning() {
      if (this.level == null) {
         return false;
      } else {
         return this.level.isClientSide
            ? this.animationStates != null && this.animationStates[8] + this.animationStates[9] + this.animationStates[11] != 0.0F
            : this.cooldown > 0;
      }
   }

   private void startRunning() {
      if (this.level != null) {
         PacketStartAnimation pkt = new PacketStartAnimation((byte)0, this.worldPosition);
         PacketHandler.sendToAllAround(pkt, this.level, this.worldPosition, 48.0);
         this.cooldown = 440;
      }
   }

   private boolean hasSufficientVis() {
      return !this.wandStack.isEmpty() && WAND_ITEM.consumeAllVis(this.wandStack, null, MAX_WAND_COST, false, false);
   }

   public int getContainerSize() {
      return 1;
   }

   public boolean isEmpty() {
      return this.wandStack.isEmpty();
   }

   public ItemStack getItem(int slot) {
      return slot == 0 ? this.wandStack : ItemStack.EMPTY;
   }

   public ItemStack removeItem(int slot, int amount) {
      if (slot == 0 && amount > 0 && !this.isRunning() && !this.wandStack.isEmpty()) {
         ItemStack out = this.wandStack.copy();
         this.wandStack = ItemStack.EMPTY;
         this.markForUpdate();
         return out;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack removeItemNoUpdate(int slot) {
      if (slot != 0) {
         return ItemStack.EMPTY;
      } else {
         ItemStack out = this.wandStack;
         this.wandStack = ItemStack.EMPTY;
         return out;
      }
   }

   public void setItem(int slot, ItemStack stack) {
      if (slot == 0) {
         this.wandStack = stack;
         this.markForUpdate();
      }
   }

   public boolean stillValid(Player player) {
      return BlockEntityMenuValidator.isValid(player, this);
   }

   public void clearContent() {
      this.wandStack = ItemStack.EMPTY;
   }

   public boolean isItemValidForSlot(int slot, ItemStack stack) {
      return !this.isRunning() && slot == 0 && stack.getItem() == WAND_ITEM && !WAND_ITEM.isSceptre(stack) && WAND_ITEM.getFocus(stack) == null;
   }

   public int[] getSlotsForFace(Direction side) {
      return new int[]{0};
   }

   public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
      return !this.isRunning() && side != null && side != Direction.UP && side != Direction.DOWN && this.isItemValidForSlot(slot, stack);
   }

   public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
      return (!this.isLocked() || !this.hasSufficientVis()) && this.canPlaceItemThroughFace(slot, stack, side);
   }

   public Component getDisplayName() {
      return Component.translatable("crew.info.InfusionClaw");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
      return new InfusionClawMenu(id, inv, this);
   }

   protected void saveAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.saveAdditional(tag, provider);
      this.writeTag(tag);
   }

   private void writeTag(@NotNull CompoundTag tag) {
      if (this.ownerUUID != null) {
         tag.putUUID("ownerUUID", this.ownerUUID);
         tag.putString("ownerName", this.ownerNameCache);
      }

      if (!this.wandStack.isEmpty()) {
         tag.put("wandStack", ItemStackData.saveStack(this.wandStack));
      }

      tag.putBoolean("isLocked", this.isLocked);
   }

   public void loadAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.loadAdditional(tag, provider);
      this.readTag(tag);
   }

   private void readTag(@NotNull CompoundTag tag) {
      if (tag.hasUUID("ownerUUID")) {
         this.ownerUUID = tag.getUUID("ownerUUID");
         this.ownerNameCache = tag.getString("ownerName");
      } else if (tag.contains("player")) {
         this.ownerNameCache = tag.getString("player");
      }

      this.wandStack = tag.contains("wandStack") ? ItemStackData.loadStack(tag.getCompound("wandStack")) : ItemStack.EMPTY;
      this.isLocked = tag.getBoolean("isLocked");
   }

   public CompoundTag getUpdateTag(@NotNull Provider provider) {
      CompoundTag tag = super.getUpdateTag(provider);
      this.writeTag(tag);
      return tag;
   }

   @Nullable
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Nullable
   public IItemHandler getItemHandler(@Nullable Direction facing) {
      if (this.remove || facing == null) {
         return null;
      } else {
         return facing == Direction.UP ? this.unsidedWrapper : this.sidedWrappers.computeIfAbsent(facing, dir -> new SidedInvWrapper(this, dir));
      }
   }

   public void setRemoved() {
      super.setRemoved();
      this.sidedWrappers.clear();
   }
}
