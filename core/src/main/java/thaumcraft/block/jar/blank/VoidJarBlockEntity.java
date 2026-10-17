package thaumcraft.block.jar.blank;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.block.jar.standart.JarBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class VoidJarBlockEntity extends JarBlockEntity {
   public VoidJarBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.VOID_JAR.get(), pos, state);
   }

   /** TC4 TileJarFillableVoid: keeps drawing even when full, and whatever does not fit is destroyed. */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (++this.tickCounter % 5 == 0) {
         this.fillJar(level, pos);
      }
   }

   @Override
   public int getMinimumSuction() {
      return this.aspectFilter != null ? 48 : 32;
   }

   @Override
   public int getSuctionAmount(Direction face) {
      return this.aspectFilter != null && this.amount < this.maxAmount ? 48 : 32;
   }

   @Override
   public int addToContainer(@org.jetbrains.annotations.NotNull thaumcraft.api.aspects.Aspect aspect, int amount) {
      if (amount == 0) {
         return 0;
      }
      if (aspect == this.aspect || this.amount == 0) {
         this.aspect = aspect;
         this.amount += amount;
         if (this.amount > this.maxAmount) {
            this.amount = this.maxAmount;
         }
         this.setChanged();
         this.markForUpdate();
         return 0;
      }
      return amount;
   }

   @Override
   public ResourceLocation getModel() {
      return ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/model/jar_void.png");
   }
}
