package thaumcraft.block.essence_crystallizer;

import java.awt.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.crystall_essence.ItemCrystalEssence;
import thaumcraft.util.InventoryUtils;

public class EssenceCrystalizerBlockEntity extends ThaumcraftBlockEntity implements IAspectContainer, IEssenceTransport, ITickable {
   public Aspect aspect = null;
   public Direction facing = Direction.DOWN;
   private int count = 0;
   private int progress = 0;
   private static final int PROG_MAX = 200;
   public float spin = 0.0F;
   public float spinInc = 0.0F;
   private float tr = 1.0F;
   private float tg = 1.0F;
   private float tb = 1.0F;
   public float cr = 1.0F;
   public float cg = 1.0F;
   public float cb = 1.0F;
   private int venting = 0;

   public EssenceCrystalizerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.ESSENTIA_CRYSTALLIZER.get(), pos, state);
   }

   protected EssenceCrystalizerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      return new AABB(this.worldPosition).inflate(1.0);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      this.aspect = Aspect.getAspect(compound.getString("Aspect"));
      this.facing = Direction.from3DDataValue(compound.getByte("face"));
      this.progress = compound.getInt("progress");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      if (this.aspect != null) {
         compound.putString("Aspect", this.aspect.getTag());
      }

      compound.putByte("face", (byte)this.facing.get3DDataValue());
      compound.putInt("progress", this.progress);
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (++this.count % 5 == 0 && !this.gettingPower()) {
         if (this.aspect == null) {
            this.fillReservoir();
            this.progress = 0;
         } else {
            this.progress = this.progress
               + 1
               + VisNetHandler.drainVis(
                     level,
                     pos,
                     Aspect.EARTH,
                     Math.min(20, Math.max(1, (PROG_MAX - this.progress) / 2))
                  )
                  * 2;
         }
      }

      if (this.aspect != null && this.progress >= PROG_MAX) {
         this.eject();
         this.aspect = null;
         this.progress = 0;
         this.setChanged();
         this.markForUpdate();
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.clientTick();
   }

   public static void tick(Level level, BlockPos pos, BlockState state, EssenceCrystalizerBlockEntity blockEntity) {
      if (level.isClientSide) {
         blockEntity.clientTick();
      } else {
         blockEntity.serverTick(level, pos, state);
      }
   }

   private void clientTick() {
      if (this.aspect == null) {
         this.tr = this.tg = this.tb = 1.0F;
      } else {
         Color c = new Color(this.aspect.getColor());
         this.tr = c.getRed() / 220.0F;
         this.tg = c.getGreen() / 220.0F;
         this.tb = c.getBlue() / 220.0F;
      }

      if (this.cr < this.tr) {
         this.cr += 0.05F;
      }

      if (this.cr > this.tr) {
         this.cr -= 0.05F;
      }

      if (this.cg < this.tg) {
         this.cg += 0.05F;
      }

      if (this.cg > this.tg) {
         this.cg -= 0.05F;
      }

      if (this.cb < this.tb) {
         this.cb += 0.05F;
      }

      if (this.cb > this.tb) {
         this.cb -= 0.05F;
      }

      this.spin = this.spin + this.spinInc;
      if (this.spin > 360.0F) {
         this.spin -= 360.0F;
      }

      if (this.aspect != null && this.spinInc < 20.0F && !this.gettingPower()) {
         this.spinInc += 0.1F;
         if (this.spinInc > 20.0F) {
            this.spinInc = 20.0F;
         }
      } else if ((this.aspect == null || this.gettingPower()) && this.spinInc > 0.0F) {
         this.spinInc -= 0.2F;
         if (this.spinInc < 0.0F) {
            this.spinInc = 0.0F;
         }
      }

      if (this.venting > 0) {
         this.venting--;
         if (this.level != null) {
            float fx = 0.1F - this.level.random.nextFloat() * 0.2F;
            float fz = 0.1F - this.level.random.nextFloat() * 0.2F;
            float fy = 0.1F - this.level.random.nextFloat() * 0.2F;
            float fx2 = 0.1F - this.level.random.nextFloat() * 0.2F;
            float fz2 = 0.1F - this.level.random.nextFloat() * 0.2F;
            float fy2 = 0.1F - this.level.random.nextFloat() * 0.2F;
            int color = 16777215;
            Direction opp = this.facing.getOpposite();
            Thaumcraft.setup.drawVentParticles(
               this.level,
               (double)((float)this.worldPosition.getX() + 0.5F + fx + (float)opp.getStepX() / 2.1F),
               (double)((float)this.worldPosition.getY() + 0.5F + fy + (float)opp.getStepY() / 2.1F),
               (double)((float)this.worldPosition.getZ() + 0.5F + fz + (float)opp.getStepZ() / 2.1F),
               (double)((float)opp.getStepX() / 4.0F + fx2),
               (double)((float)opp.getStepY() / 4.0F + fy2),
               (double)((float)opp.getStepZ() / 4.0F + fz2),
               color
            );
         }
      }
   }

   public void eject() {
      ItemStack stack = new ItemStack((ItemLike)ModItems.CRYSTAL_ESSENCE.get(), 1);
      if (stack.getItem() instanceof ItemCrystalEssence itemCrystalEssence) {
         itemCrystalEssence.setAspects(stack, new AspectList().add(this.aspect, 1));
      }
      Direction opposite = this.facing.getOpposite();
      BlockPos targetPos = this.worldPosition.relative(opposite);
      if (this.level != null) {
         BlockEntity inventory = this.level.getBlockEntity(targetPos);
         if (inventory instanceof Container container) {
            stack = InventoryUtils.placeItemStackIntoInventory(stack, container, this.facing, true);
         }

         if (!stack.isEmpty()) {
            this.spawnItem(stack);
         }

         this.level.playSound(
            null,
            this.worldPosition.getX() + 0.5,
            this.worldPosition.getY() + 0.5,
            this.worldPosition.getZ() + 0.5,
            SoundEvents.FIRE_EXTINGUISH,
            SoundSource.BLOCKS,
            0.25F,
            2.6F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.8F
         );
      }
   }

   public boolean spawnItem(ItemStack stack) {
      if (this.level == null) {
         return false;
      }
      Direction opposite = this.facing.getOpposite();
      ItemEntity itemEntity = new ItemEntity(
         this.level,
         this.worldPosition.getX() + 0.5 + opposite.getStepX() * 0.65,
         this.worldPosition.getY() + 0.5 + opposite.getStepY() * 0.65,
         this.worldPosition.getZ() + 0.5 + opposite.getStepZ() * 0.65,
         stack
      );
      itemEntity.setDeltaMovement(
         opposite.getStepX() * 0.04,
         opposite.getStepY() * 0.04,
         opposite.getStepZ() * 0.04
      );
      this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 0, 0);
      return this.level.addFreshEntity(itemEntity);
   }

   void fillReservoir() {
      if (this.level == null) {
         return;
      }
      BlockEntity te = ThaumcraftApiHelper.getConnectableTile(this.level, this.worldPosition, this.facing);
      if (te instanceof IEssenceTransport ic) {
         Direction opposite = this.facing.getOpposite();
         if (!ic.canOutputTo(opposite)) {
            return;
         }

         Aspect ta = null;
         if (ic.getEssenceAmount(opposite) > 0
            && ic.getSuctionAmount(opposite) < this.getSuctionAmount(this.facing)
            && this.getSuctionAmount(this.facing) >= ic.getMinimumSuction()) {
            ta = ic.getEssenceType(opposite);
         }

         if (ta != null && ic.getSuctionAmount(opposite) < this.getSuctionAmount(this.facing)) {
            int taken = ic.extractEssence(ta, 1, opposite);
            if (taken > 0) {
               this.addToContainer(ta, taken);
               this.setChanged();
               this.markForUpdate();
            }
         }
      }
   }

   @Override
   public boolean triggerEvent(int id, int param) {
      if (id >= 0) {
         if (this.level != null && this.level.isClientSide) {
            this.venting = 7;
         }

         return true;
      } else {
         return super.triggerEvent(id, param);
      }
   }

   public boolean gettingPower() {
      return this.level != null && this.level.hasNeighborSignal(this.worldPosition);
   }

   @Override
   public AspectList getAspects() {
      AspectList al = new AspectList();
      if (this.aspect != null) {
         al.add(this.aspect, 1);
      }

      return al;
   }

   @Override
   public void setAspects(AspectList aspectList) {
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return amount == 1 && this.aspect != null && aspect == this.aspect;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      for (Aspect tt : aspectList.getAspects()) {
         if (this.aspect == null || this.aspect != tt || aspectList.getAmount(tt) != 1) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return this.aspect != null && aspect == this.aspect ? 1 : 0;
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }

   @Override
   public boolean isConnectable(Direction direction) {
      return direction == this.facing;
   }

   @Override
   public boolean canInputFrom(Direction direction) {
      return direction == this.facing;
   }

   @Override
   public boolean canOutputTo(Direction direction) {
      return false;
   }

   @Override
   public void setSuction(Aspect aspect, int amount) {
   }

   @Override
   public boolean renderExtendedTube() {
      return false;
   }

   @Override
   public int getMinimumSuction() {
      return 0;
   }

   @Override
   public Aspect getSuctionType(Direction direction) {
      return null;
   }

   @Override
   public int getSuctionAmount(Direction direction) {
      return this.gettingPower() ? 0 : (direction == this.facing && this.aspect == null ? 128 : 64);
   }

   @Override
   public Aspect getEssenceType(Direction direction) {
      return this.aspect;
   }

   @Override
   public int getEssenceAmount(Direction direction) {
      return this.aspect == null ? 0 : 1;
   }

   @Override
   public int extractEssence(Aspect aspect, int amount, Direction direction) {
      return 0;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      if (amount != 1 || this.aspect == null || this.aspect != aspect) {
         return false;
      }

      this.aspect = null;
      this.setChanged();
      this.markForUpdate();
      return true;
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      if (this.aspect == null && aspect != null && !this.gettingPower()) {
         this.aspect = aspect;
         this.setChanged();
         this.markForUpdate();
         return amount - 1;
      }

      return amount;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, net.minecraft.core.Direction loc) {
      return this.addToContainer(aspect, amount);
   }
}
