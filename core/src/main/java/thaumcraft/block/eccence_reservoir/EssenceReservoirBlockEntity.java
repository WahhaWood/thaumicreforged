package thaumcraft.block.eccence_reservoir;

import java.awt.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.wands.IWandable;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModSounds;

public class EssenceReservoirBlockEntity extends ThaumcraftBlockEntity implements IAspectSource, IWandable, IEssenceTransport, ITickable {
   public AspectList essence = new AspectList();
   public int maxAmount = 256;
   int count = 0;
   float tr = 1.0F;
   float tri = 0.0F;
   float tg = 1.0F;
   float tgi = 0.0F;
   float tb = 1.0F;
   float tbi = 0.0F;
   public float cr = 1.0F;
   public float cg = 1.0F;
   public float cb = 1.0F;
   public Aspect displayAspect = null;

   public EssenceReservoirBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ESSENCE_RESERVOIR.get(), blockPos, blockState);
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveAdditional(compound, provider);
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadAdditional(compound, provider);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.essence.load(compound);
      if (this.essence.visSize() > this.maxAmount) {
         this.essence = new AspectList();
      }
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.essence.save(compound);
   }

   @Override
   public AspectList getAspects() {
      return this.essence;
   }

   @Override
   public void setAspects(AspectList aspectList) {
      this.essence = aspectList.copy();
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return this.essence.getAmount(aspect) >= amount;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      for (Aspect tt : aspectList.getAspects()) {
         if (this.essence.getAmount(tt) < aspectList.getAmount(tt)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return this.essence.getAmount(aspect);
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }

   @Override
   public boolean isConnectable(Direction face) {
      return face == this.getFacing();
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return face == this.getFacing();
   }

   @Override
   public boolean canOutputTo(Direction face) {
      return face == this.getFacing();
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
      return 24;
   }

   @Override
   public Aspect getSuctionType(Direction loc) {
      return null;
   }

   @Override
   public int getSuctionAmount(Direction loc) {
      return this.essence.visSize() < this.maxAmount ? 24 : 0;
   }

   @Override
   public Aspect getEssenceType(Direction loc) {
      return this.essence.visSize() > 0 && loc == null ? this.essence.getAspects()[0] : null;
   }

   @Override
   public int getEssenceAmount(Direction loc) {
      return this.essence.visSize();
   }

   /** Порт fillReservoir: тянет эссенцию из подключённой трубы. */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (++this.count % 5 == 0 && this.essence.visSize() < this.maxAmount) {
         this.fillReservoir(level);
      }
   }

   private void fillReservoir(Level level) {
      if (!(thaumcraft.api.ThaumcraftApiHelper.getConnectableTile(level, this.worldPosition, this.getFacing()) instanceof IEssenceTransport ic)) {
         return;
      }

      if (!ic.canOutputTo(this.getFacing().getOpposite())) {
         return;
      }

      Aspect ta = null;
      if (ic.getEssenceAmount(this.getFacing().getOpposite()) > 0
         && ic.getSuctionAmount(this.getFacing().getOpposite()) < this.getSuctionAmount(this.getFacing())
         && this.getSuctionAmount(this.getFacing()) >= ic.getMinimumSuction()) {
         ta = ic.getEssenceType(this.getFacing().getOpposite());
      }

      if (ta != null && ic.getSuctionAmount(this.getFacing().getOpposite()) < this.getSuctionAmount(this.getFacing())) {
         this.addToContainer(ta, ic.extractEssence(ta, 1, this.getFacing().getOpposite()));
         this.setChanged();
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.count++;
      int vs = this.essence.visSize();
      if (vs > 0) {
         if (this.level.random.nextInt(500 - vs) == 0) {
            this.level
               .playLocalSound(
                  this.worldPosition, (SoundEvent)ModSounds.CREAK.get(), SoundSource.BLOCKS, 1.0F, 1.4F + this.level.random.nextFloat() * 0.2F, false
               );
         }

         if (this.count % 20 == 0 && this.essence.size() > 0) {
            this.displayAspect = this.essence.getAspects()[this.count / 20 % this.essence.size()];
            if (this.displayAspect != null) {
               Color c = new Color(this.displayAspect.getColor());
               this.tr = c.getRed() / 255.0F;
               this.tg = c.getGreen() / 255.0F;
               this.tb = c.getBlue() / 255.0F;
               this.tri = (this.cr - this.tr) / 20.0F;
               this.tgi = (this.cg - this.tg) / 20.0F;
               this.tbi = (this.cb - this.tb) / 20.0F;
            }
         }

         if (this.displayAspect == null) {
            this.tr = this.tg = this.tb = 1.0F;
            this.tri = this.tgi = this.tbi = 0.0F;
         } else {
            this.cr = this.cr - this.tri;
            this.cg = this.cg - this.tgi;
            this.cb = this.cb - this.tbi;
         }
      }
   }

   @Override
   public int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos blockPos, @NotNull Direction direction
   ) {
      if (this.getFacing() == direction) {
         return 0;
      } else {
         Direction newFacing = player.isShiftKeyDown() ? direction : direction.getOpposite();
         if (this.getFacing() != newFacing) {
            // Поворот жезлом: тело if было пустым (ThaumcraftBlockEntity.setFacing
            // мутирует копию стейта и ничего не пишет — пишем в мир напрямую).
            level.setBlock(
               blockPos, this.getBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, newFacing), 3
            );
            this.setChanged();
         }

         return 0;
      }
   }

   @NotNull
   @Override
   public ItemStack onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      return ItemStack.EMPTY;
   }

   @Override
   public void onUsingWandTick(@NotNull ItemStack stack, @NotNull Player player, int count) {
   }

   @Override
   public void onWandStoppedUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player, int count) {
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      if (this.essence.getAmount(aspect) < amount) {
         return false;
      }

      this.essence.remove(aspect, amount);
      return true;
   }


   @Override
   public int addToContainer(Aspect aspect, int amount) {
      int room = this.maxAmount - this.essence.visSize();
      int added = Math.min(Math.max(room, 0), amount);
      if (added > 0) {
         this.essence.add(aspect, added);
      }

      return amount - added;
   }


   @Override
   public int injectEssence(Aspect aspect, int amount, net.minecraft.core.Direction loc) {
      return this.addToContainer(aspect, amount);
   }


   @Override
   public int extractEssence(Aspect aspect, int amount, net.minecraft.core.Direction loc) {
      int available = this.essence.getAmount(aspect);
      int taken = Math.min(available, amount);
      if (taken > 0) {
         this.essence.remove(aspect, taken);
      }

      return taken;
   }

}
