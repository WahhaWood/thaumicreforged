package thaumcraft.block.mirror;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.LevelBlockPos;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXEssenceSource;

/**
 * Эссенция-зеркало (порт {@code TileMirrorEssentia} из TC4 1.7.10).
 * Удалённый кран эссенции: отдаёт по 1 единице аспекта, забирая её из сосудов
 * рядом с парным зеркалом (куб в направлении грани, радиус 8).
 */
public class MirrorEssenceBlockEntity extends ThaumcraftBlockEntity implements IAspectSource {
   public boolean linked = false;
   @Nullable
   public LevelBlockPos link;
   public int instability = 0;
   int count = 0;
   int inc = 40;

   public MirrorEssenceBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.MIRROR_ESSENCE.get(), pos, state);
   }

   @Nullable
   public static ServerLevel targetLevel(LevelBlockPos pos) {
      return ServerLifecycleHooks.getCurrentServer() == null ? null : ServerLifecycleHooks.getCurrentServer().getLevel(pos.dim);
   }

   public void restoreLink() {
      if (this.link == null || this.level == null || this.level.isClientSide) {
         return;
      }

      ServerLevel targetWorld = targetLevel(this.link);
      if (targetWorld == null) {
         return;
      }

      if (targetWorld.getBlockEntity(this.link.asBlockPos()) instanceof MirrorEssenceBlockEntity tm
         && !tm.isLinkValid()) {
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

      if (targetWorld.getBlockEntity(this.link.asBlockPos()) instanceof MirrorEssenceBlockEntity tm) {
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

      if (targetWorld.getBlockEntity(this.link.asBlockPos()) instanceof MirrorEssenceBlockEntity tm) {
         return tm.linked
            && tm.link != null
            && tm.link.asBlockPos().equals(this.worldPosition)
            && tm.link.dim.equals(this.level.dimension());
      }

      return false;
   }

   /**
    * Отдать 1 единицу аспекта из сосудов рядом с парным зеркалом
    * (порт {@code TileMirrorEssentia#takeFromContainer} + {@code EssentiaHandler#drainEssentia}).
    */
   @Override
   public boolean takeFromContainer(@NotNull Aspect aspect, int amount) {
      if (this.level == null || this.level.isClientSide || this.link == null || amount > 1) {
         return false;
      }

      if (!this.isLinkValid()) {
         return false;
      }

      ServerLevel targetWorld = targetLevel(this.link);
      if (targetWorld == null) {
         return false;
      }

      Direction face = Direction.UP;
      if (targetWorld.getBlockEntity(this.link.asBlockPos()) instanceof MirrorEssenceBlockEntity tm) {
         face = tm.getFacing();
      }

      for (BlockPos sourcePos : essentiaSources(targetWorld, this.link.asBlockPos(), face, 8)) {
         BlockEntity be = targetWorld.getBlockEntity(sourcePos);
         if (be instanceof IAspectSource source && !(be instanceof MirrorEssenceBlockEntity)) {
            if (source.takeFromContainer(aspect, 1)) {
               PacketHandler.sendToAllAround(
                  new PacketFXEssenceSource(
                     this.link.asBlockPos(),
                     (byte)(this.link.getX() - sourcePos.getX()),
                     (byte)(this.link.getY() - sourcePos.getY()),
                     (byte)(this.link.getZ() - sourcePos.getZ()),
                     aspect.getColor()
                  ),
                  targetWorld,
                  this.link.asBlockPos(),
                  32.0
               );
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Зона поиска сосудов (порт {@code EssentiaHandler#getSources}):
    * (2r+1)² в плоскости грани × r вглубь по направлению грани.
    */
   static List<BlockPos> essentiaSources(Level world, BlockPos origin, @Nullable Direction direction, int range) {
      List<BlockPos> out = new ArrayList<>();
      int start = direction == null ? -range : 0;
      Direction dir = direction == null ? Direction.UP : direction;

      for (int aa = -range; aa <= range; aa++) {
         for (int bb = -range; bb <= range; bb++) {
            for (int cc = start; cc < range; cc++) {
               if (aa == 0 && bb == 0 && cc == 0) {
                  continue;
               }

               int xx = origin.getX();
               int yy = origin.getY();
               int zz = origin.getZ();
               if (dir.getStepY() != 0) {
                  xx += aa;
                  yy += cc * dir.getStepY();
                  zz += bb;
               } else if (dir.getStepX() == 0) {
                  xx += aa;
                  yy += bb;
                  zz += cc * dir.getStepZ();
               } else {
                  xx += cc * dir.getStepX();
                  yy += aa;
                  zz += bb;
               }

               BlockPos pos = new BlockPos(xx, yy, zz);
               if (world.getBlockEntity(pos) instanceof IAspectSource) {
                  out.add(pos);
               }
            }
         }
      }

      return out;
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, MirrorEssenceBlockEntity be) {
      if (level.isClientSide) {
         return;
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
   public AspectList getAspects() {
      return new AspectList();
   }

   @Override
   public void setAspects(AspectList aspectList) {
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return false;
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      return amount;
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return 0;
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
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public Direction getFacing() {
      if (this.getBlockState().hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
         return (Direction)this.getBlockState()
            .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
      }

      return Direction.UP;
   }
}
