package thaumcraft.block.mirror;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.LevelBlockPos;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.util.ItemStackData;

/**
 * Магическое зеркало (порт {@code TileMirror} из TC4 1.7.10).
 * Спаренные зеркала переносят выброшенные в них предметы друг другу (в т.ч. между измерениями).
 * Каждый перенос растит нестабильность — она тормозит выдачу и снимается вис Ordo из сети.
 */
public class MirrorBlockEntity extends ThaumcraftBlockEntity {
   public boolean linked = false;
   @Nullable
   public LevelBlockPos link;
   public int instability;
   private int count = 0;
   private int inc = 40;
   private final List<ItemStack> outputStacks = new ArrayList<>();

   public MirrorBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.MIRROR.get(), blockPos, blockState);
   }

   @Nullable
   public static ServerLevel targetLevel(LevelBlockPos pos) {
      return ServerLifecycleHooks.getCurrentServer() == null ? null : ServerLifecycleHooks.getCurrentServer().getLevel(pos.dim);
   }

   /** Восстановить связь после перезахода (порт {@code TileMirror#restoreLink}). */
   public void restoreLink() {
      if (this.link == null || this.level == null || this.level.isClientSide) {
         return;
      }

      ServerLevel targetWorld = targetLevel(this.link);
      if (targetWorld == null) {
         return;
      }

      if (targetWorld.getBlockEntity(this.link.asBlockPos()) instanceof MirrorBlockEntity tm && !tm.isLinkValid()) {
         tm.linked = true;
         tm.link = new LevelBlockPos(this.worldPosition, this.level.dimension());
         tm.setChanged();
         this.linked = true;
         this.setChanged();
      }
   }

   public boolean isLinkValid() {
      if (!this.linked || this.link == null || this.level == null) {
         return false;
      }

      ServerLevel targetWorld = targetLevel(this.link);
      if (targetWorld == null) {
         return false;
      }

      if (targetWorld.getBlockEntity(this.link.asBlockPos()) instanceof MirrorBlockEntity tm) {
         if (!tm.linked
            || tm.link == null
            || !tm.link.asBlockPos().equals(this.worldPosition)
            || !tm.link.dim.equals(this.level.dimension())) {
            this.linked = false;
            this.setChanged();
            return false;
         }

         return true;
      }

      this.linked = false;
      this.setChanged();
      return false;
   }

   public boolean isLinkValidSimple() {
      if (!this.linked || this.link == null) {
         return false;
      }

      ServerLevel targetWorld = targetLevel(this.link);
      if (targetWorld == null) {
         return false;
      }

      if (targetWorld.getBlockEntity(this.link.asBlockPos()) instanceof MirrorBlockEntity tm) {
         return tm.linked
            && tm.link != null
            && tm.link.asBlockPos().equals(this.worldPosition)
            && tm.link.dim.equals(this.level.dimension());
      }

      return false;
   }

   /** Перенести выброшенный предмет парному зеркалу (порт {@code TileMirror#transport}). */
   public boolean transport(ItemEntity entity) {
      if (this.level == null || this.level.isClientSide || this.link == null || entity.isRemoved()) {
         return false;
      }

      if (this.linked && this.isLinkValid()) {
         ServerLevel targetWorld = targetLevel(this.link);
         if (targetWorld != null
            && targetWorld.getBlockEntity(this.link.asBlockPos()) instanceof MirrorBlockEntity target) {
            target.addStack(entity.getItem().copy());
            this.addInstability(entity.getItem().getCount());
            entity.discard();
            this.setChanged();
            target.setChanged();
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, 0);
            return true;
         }
      }

      return false;
   }

   /** Выдать один предмет из очереди наружу (порт {@code TileMirror#eject}). */
   public void eject() {
      if (this.level == null || this.level.isClientSide || this.outputStacks.isEmpty() || this.count <= 20) {
         return;
      }

      int i = this.level.random.nextInt(this.outputStacks.size());
      ItemStack stored = this.outputStacks.get(i);
      if (stored != null && !stored.isEmpty()) {
         ItemStack out = stored.copy();
         out.setCount(1);
         if (this.spawnItem(out)) {
            stored.shrink(1);
            this.addInstability(1);
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, 0);
            if (stored.isEmpty()) {
               this.outputStacks.remove(i);
            }

            this.setChanged();
         }
      }
   }

   public boolean spawnItem(ItemStack stack) {
      if (this.level == null) {
         return false;
      }

      try {
         Direction face = this.getFacing();
         ItemEntity entity = new ItemEntity(
            this.level,
            this.worldPosition.getX() + 0.5 - face.getStepX() * 0.3,
            this.worldPosition.getY() + 0.5 - face.getStepY() * 0.3,
            this.worldPosition.getZ() + 0.5 - face.getStepZ() * 0.3,
            stack
         );
         entity.setDeltaMovement(face.getStepX() * 0.15F, face.getStepY() * 0.15F, face.getStepZ() * 0.15F);
         entity.setDefaultPickUpDelay();
         this.level.addFreshEntity(entity);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   protected void addInstability(int amount) {
      this.instability += amount;
      if (this.level != null && this.link != null) {
         ServerLevel targetWorld = targetLevel(this.link);
         if (targetWorld != null
            && targetWorld.getBlockEntity(this.link.asBlockPos()) instanceof MirrorBlockEntity tm) {
            tm.instability = Math.max(0, tm.instability + amount);
            tm.setChanged();
         }
      }
   }

   public void addStack(ItemStack stack) {
      this.outputStacks.add(stack);
      this.setChanged();
   }

   /** Диагностика для моста: тики и размер очереди. */
   public int tickCount() {
      return this.count;
   }

   public int queuedStacks() {
      return this.outputStacks.size();
   }

   public void add(@NotNull ItemStack stack) {
      this.addStack(stack.copy());
   }

   /** Развал связи при снятии парного зеркала (порт {@code TileMirror#invalidateLink}). */
   public void invalidateLink() {
      if (this.link == null || this.level == null || this.level.isClientSide) {
         return;
      }

      ServerLevel targetWorld = targetLevel(this.link);
      if (targetWorld != null
         && targetWorld.hasChunkAt(this.link.asBlockPos())
         && targetWorld.getBlockEntity(this.link.asBlockPos()) instanceof MirrorBlockEntity tm) {
         tm.linked = false;
         tm.setChanged();
      }
   }

   /** Серверный тик (порт {@code TileMirror#func_145845_h} + {@code checkInstability}). */
   public static void serverTick(Level level, BlockPos pos, BlockState state, MirrorBlockEntity be) {
      if (level.isClientSide) {
         return;
      }

      // Подбор выброшенных предметов рядом (entityInside с пустой коллизией срабатывает
      // нестабильно — тик-скан гарантирует перенос 1в1 с оригиналом).
      if (be.linked) {
         for (ItemEntity item : level.getEntitiesOfClass(
            ItemEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(0.75),
            e -> e.isAlive() && !e.isRemoved() && !e.hasPickUpDelay()
         )) {
            be.transport(item);
         }
      }

      int tickrate = be.instability / 50;
      if (tickrate == 0 || be.count % (tickrate * tickrate) == 0) {
         be.eject();
      }

      if (be.instability > 0 && be.count % 20 == 0) {
         be.instability--;
         be.setChanged();
      }

      if (be.instability > 0) {
         int drained = VisNetHandler.drainVis(level, pos, Aspect.ORDER, Math.min(be.instability, 1));
         if (drained > 0) {
            be.addInstability(-drained);
            be.setChanged();
         }
      }

      if (be.count++ % be.inc == 0) {
         if (!be.isLinkValidSimple()) {
            if (be.inc < 600) {
               be.inc += 20;
            }

            be.restoreLink();
         } else {
            be.inc = 40;
         }
      }
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      this.linked = compound.getBoolean("linked");
      if (compound.contains("link", 10)) {
         this.link = new LevelBlockPos(compound.getCompound("link"));
      } else {
         this.link = null;
      }

      this.instability = compound.getInt("instability");
      ListTag listTag = compound.getList("Items", 10);
      this.outputStacks.clear();

      for (int i = 0; i < listTag.size(); i++) {
         ItemStack stack = ItemStackData.loadStack(listTag.getCompound(i));
         if (!stack.isEmpty()) {
            this.outputStacks.add(stack);
         }
      }
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      compound.putBoolean("linked", this.linked);
      if (this.link != null) {
         CompoundTag linkTag = new CompoundTag();
         this.link.save(linkTag);
         compound.put("link", linkTag);
      }

      compound.putInt("instability", this.instability);
      ListTag listTag = new ListTag();

      for (ItemStack stack : this.outputStacks) {
         if (!stack.isEmpty()) {
            listTag.add(ItemStackData.saveStack(stack));
         }
      }

      compound.put("Items", listTag);
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   /** Block events must return true on the server as well: only then does the server send them to clients. */
   @Override
   public boolean triggerEvent(int i, int j) {
      if (i == 1) {
         if (this.level.isClientSide) {
            this.level
               .playLocalSound(
                  this.worldPosition.getX(),
                  this.worldPosition.getY(),
                  this.worldPosition.getZ(),
                  SoundEvents.PORTAL_TRAVEL,
                  SoundSource.BLOCKS,
                  0.5F,
                  1.0F,
                  false
               );
         }

         return true;
      } else {
         return super.triggerEvent(i, j);
      }
   }

   @Override
   public Direction getFacing() {
      return this.getBlockState().hasProperty(BlockStateProperties.FACING)
         ? (Direction)this.getBlockState().getValue(BlockStateProperties.FACING)
         : Direction.UP;
   }
}
