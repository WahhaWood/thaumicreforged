package thaumcraft.block.jar.standart;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.block.jar.JarBaseBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class JarBlockEntity extends JarBaseBlockEntity implements IAspectSource, IEssenceTransport, ITickable {
   @Nullable
   public Aspect aspect = null;
   @Nullable
   public Aspect aspectFilter = null;
   public int amount = 0;
   public int maxAmount = 64;
   public boolean forgeLiquid = false;
   public int lid = 0;
   protected int tickCounter = 0;

   public JarBlockEntity(BlockPos pos, BlockState state) {
      this((BlockEntityType<?>)ModBlockEntities.JAR.get(), pos, state);
   }

   protected JarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   /** Банка тянет эссенцию из трубы над собой (порт fillJar из 1.7.10). */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (++this.tickCounter % 5 == 0 && this.amount < this.maxAmount) {
         this.fillJar(level, pos);
      }
   }

   protected void fillJar(Level level, BlockPos pos) {
      if (!(thaumcraft.api.ThaumcraftApiHelper.getConnectableTile(level, pos, Direction.UP) instanceof IEssenceTransport ic)) {
         return;
      }

      if (!ic.canOutputTo(Direction.DOWN)) {
         return;
      }

      int mySuction = this.getSuctionAmount(Direction.UP);
      Aspect wanted = null;
      if (this.aspectFilter != null) {
         wanted = this.aspectFilter;
      } else if (this.aspect != null && this.amount > 0) {
         wanted = this.aspect;
      } else if (ic.getEssenceAmount(Direction.DOWN) > 0
         && ic.getSuctionAmount(Direction.DOWN) < mySuction
         && mySuction >= ic.getMinimumSuction()) {
         wanted = ic.getEssenceType(Direction.DOWN);
      }

      if (wanted != null && ic.getSuctionAmount(Direction.DOWN) < mySuction) {
         int taken = ic.extractEssence(wanted, 1, Direction.DOWN);
         if (taken > 0) {
            this.addToContainer(wanted, taken);
            this.setChanged();
         }
      }
   }

   @Override
   protected void loadCustom(@NotNull CompoundTag tag) {
      this.aspect = Aspect.getAspect(tag.getString("Aspect"));
      this.aspectFilter = Aspect.getAspect(tag.getString("AspectFilter"));
      this.amount = tag.getShort("Amount");
   }

   @Override
   protected void saveCustom(@NotNull CompoundTag tag) {
      if (this.aspect != null) {
         tag.putString("Aspect", this.aspect.getTag());
      }

      if (this.aspectFilter != null) {
         tag.putString("AspectFilter", this.aspectFilter.getTag());
      }

      tag.putShort("Amount", (short)this.amount);
   }

   @Override
   public AspectList getAspects() {
      AspectList al = new AspectList();
      if (this.aspect != null && this.amount > 0) {
         al.add(this.aspect, this.amount);
      }

      return al;
   }

   @Override
   public void setAspects(AspectList aspectList) {
      Aspect newAspect = null;
      int newAmount = 0;
      if (aspectList != null && aspectList.size() > 0) {
         Aspect[] aspects = aspectList.getAspects();
         if (aspects.length > 0) {
            newAspect = aspects[0];
            if (newAspect != null) {
               newAmount = Math.min(this.maxAmount, aspectList.getAmount(newAspect));
            }
         }
      }

      if (this.aspect != newAspect || this.amount != newAmount) {
         this.aspect = newAspect;
         this.amount = newAmount;
         this.setChanged();
         this.markForUpdate();
      }
   }

   @Override
   public int addToContainer(@NotNull Aspect aspect, int amount) {
      if (amount == 0) {
         return 0;
      } else if ((this.amount >= this.maxAmount || aspect != this.aspect) && this.amount != 0) {
         return amount;
      } else {
         this.aspect = aspect;
         int added = Math.min(amount, this.maxAmount - this.amount);
         this.amount += added;
         if (added > 0) {
            this.setChanged();
            this.markForUpdate();
         }

         return amount - added;
      }
   }

   @Override
   public boolean takeFromContainer(@NotNull Aspect aspect, int amount) {
      if (this.amount >= amount && aspect == this.aspect) {
         this.amount -= amount;
         if (this.amount <= 0) {
            this.aspect = null;
            this.amount = 0;
         }

         this.setChanged();
         this.markForUpdate();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      if (aspectList != null && aspectList.size() != 0) {
         if (aspectList.size() != 1) {
            return false;
         } else {
            Aspect requested = aspectList.getAspects()[0];
            return requested != null && this.takeFromContainer(requested, aspectList.getAmount(requested));
         }
      } else {
         return true;
      }
   }

   @Override
   public boolean doesContainerContainAmount(@NotNull Aspect aspect, int amount) {
      return this.amount >= amount && aspect == this.aspect;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      if (aspectList != null && aspectList.size() != 0) {
         if (aspectList.size() != 1) {
            return false;
         } else {
            Aspect requested = aspectList.getAspects()[0];
            return requested != null && this.doesContainerContainAmount(requested, aspectList.getAmount(requested));
         }
      } else {
         return true;
      }
   }

   @Override
   public int containerContains(Aspect aspect) {
      return aspect == this.aspect ? this.amount : 0;
   }

   @Override
   public boolean doesContainerAccept(@NotNull Aspect aspect) {
      return this.aspectFilter == null || aspect.equals(this.aspectFilter);
   }

   @Override
   public boolean isConnectable(Direction face) {
      return face == Direction.UP;
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return face == Direction.UP;
   }

   @Override
   public boolean canOutputTo(Direction face) {
      return face == Direction.UP;
   }

   @Override
   public void setSuction(Aspect p0, int p1) {
   }

   @Override
   public int getMinimumSuction() {
      return this.aspectFilter != null ? 64 : 32;
   }

   @Override
   public boolean renderExtendedTube() {
      return true;
   }

   @Nullable
   @Override
   public Aspect getSuctionType(Direction face) {
      return this.aspectFilter != null ? this.aspectFilter : this.aspect;
   }

   @Override
   public int getSuctionAmount(Direction face) {
      if (this.amount >= this.maxAmount) {
         return 0;
      } else {
         return this.aspectFilter != null ? 64 : 32;
      }
   }

   @Nullable
   @Override
   public Aspect getEssenceType(Direction face) {
      return this.aspect;
   }

   @Override
   public int getEssenceAmount(Direction face) {
      return this.amount;
   }

   @Override
   public int extractEssence(Aspect aspect, int amount, Direction face) {
      return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction face) {
      if (!this.canInputFrom(face)) {
         return 0;
      } else {
         int remainder = this.addToContainer(aspect, amount);
         return amount - remainder;
      }
   }
}
