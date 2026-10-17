package thaumcraft.block.hungry_chest;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class ChestHungryBlockEntity extends ThaumcraftSideInvBlockEntity implements MenuProvider, ITickable {
   public float lidAngle;
   public float prevLidAngle;
   public int numUsingPlayers;
   private int eatAnimTicks;

   public ChestHungryBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.HUNGRY_CHEST.get(), blockPos, blockState, 27);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.prevLidAngle = this.lidAngle;
      if (this.eatAnimTicks > 0) {
         this.eatAnimTicks--;
      }

      float desiredAngle = this.numUsingPlayers > 0 ? 1.0F : (this.eatAnimTicks > 0 ? 0.5F : 0.0F);
      float openSpeed = 0.04F;
      float closeSpeed = 0.04F;
      if (this.lidAngle < desiredAngle) {
         this.lidAngle += openSpeed;
      } else if (this.lidAngle > desiredAngle) {
         this.lidAngle -= closeSpeed;
      }

      if (this.lidAngle > 1.0F) {
         this.lidAngle = 1.0F;
      }

      if (this.lidAngle < 0.0F) {
         this.lidAngle = 0.0F;
      }

      if (this.numUsingPlayers > 0 && this.prevLidAngle == 0.0F && this.lidAngle > 0.0F) {
         this.level.playLocalSound(this.worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
      }

      if (this.numUsingPlayers == 0 && this.prevLidAngle > 0.0F && this.lidAngle == 0.0F) {
         this.level.playLocalSound(this.worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
      }
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      java.util.List<net.minecraft.world.entity.item.ItemEntity> items = level.getEntitiesOfClass(
         net.minecraft.world.entity.item.ItemEntity.class,
         new net.minecraft.world.phys.AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1.5, pos.getZ() + 1)
      );
      for (net.minecraft.world.entity.item.ItemEntity itemEntity : items) {
         if (itemEntity.isAlive()) {
            net.minecraft.world.item.ItemStack stack = itemEntity.getItem();
            net.minecraft.world.item.ItemStack remainder = this.add(stack);
            if (remainder.getCount() != stack.getCount()) {
               level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.25F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
               level.blockEvent(pos, state.getBlock(), 2, 10);
            }
            if (remainder.isEmpty()) {
               itemEntity.discard();
            } else {
               itemEntity.setItem(remainder);
            }
         }
      }
   }

   public boolean triggerEvent(int id, int type) {
      if (id == 1) {
         this.numUsingPlayers = type;
         return true;
      } else if (id == 2) {
         this.eatAnimTicks = Math.max(this.eatAnimTicks, type);
         return true;
      } else {
         return super.triggerEvent(id, type);
      }
   }

   @NotNull
   public Component getDisplayName() {
      return Component.literal("Hungry Chest");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowsId, @NotNull Inventory inventory, @NotNull Player player) {
      return new ChestMenu(MenuType.GENERIC_9x3, windowsId, inventory, this, 3);
   }
}
