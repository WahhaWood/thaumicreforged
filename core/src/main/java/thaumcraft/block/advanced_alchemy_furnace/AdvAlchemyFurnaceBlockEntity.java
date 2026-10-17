package thaumcraft.block.advanced_alchemy_furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.recipe.ThaumcraftCraftingManager;

public class AdvAlchemyFurnaceBlockEntity extends ThaumcraftBlockEntity implements ITickable, IAspectContainer {
   public AspectList aspects = new AspectList();
   public int vis;
   public int maxVis = 500;
   public int heat = 0;
   public int power1 = 0;
   public int power2 = 0;
   public int maxPower = 500;
   private int count = 0;
   private int processed = 0;
   private int fillValue;
   private int burnValue;
   /**
    * Флаг разборки. Ставится любой деталью печи, увидевшей, что ядро на месте
    * (оригинал: {@code BlockAlchemyFurnace.func_149749_a} → {@code tile.destroy = true}),
    * и разбирается на следующем тике. В 1.7.10 проверка отложена на тик именно для того,
    * чтобы восстановление шло не из середины раскладки соседних блоков.
    */
   private boolean destroy;

   /** Деталь печи сообщила, что состав развалился: разобрать конструкцию на следующем тике. */
   public void requestDismantle() {
      this.destroy = true;
      this.setChanged();
   }

   public AdvAlchemyFurnaceBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ADVANCED_ALCHEMY_FURNACE.get(), blockPos, blockState);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.vis = compound.getShort("vis");
      this.heat = compound.getShort("heat");
      this.aspects.load(compound);
      this.power1 = compound.getShort("power1");
      this.power2 = compound.getShort("power2");
      this.burnValue = compound.getByte("burn");
      this.fillValue = compound.getByte("fill");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      compound.putShort("vis", (short)this.vis);
      compound.putShort("heat", (short)this.heat);
      this.aspects.save(compound);
      compound.putShort("power1", (short)this.power1);
      compound.putShort("power2", (short)this.power2);
      compound.putByte("burn", (byte)this.burnValue);
      compound.putByte("fill", (byte)this.fillValue);
   }

   /** Порт TileAlchemyFurnaceAdvanced (1.7.10): нагрев без огня — ignis, плавка — perditio/aqua из ауры. */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      this.count++;

      if (this.destroy) {
         this.destroy = false;
         AdvAlchemyFurnaceBlock.checkIntegrity(level, pos);
         return;
      }

      if (this.processed > 0) {
         this.processed--;
      }

      if (this.count % 5 == 0) {
         int prevHeat = this.heat;
         if (this.heat > 0) {
            this.heat--;
         }

         if (this.heat <= this.maxPower) {
            this.heat += VisNetHandler.drainVis(level, pos, Aspect.FIRE, 50);
         }

         if (this.power1 <= this.maxPower) {
            this.power1 += VisNetHandler.drainVis(level, pos, Aspect.ENTROPY, 50);
         }

         if (this.power2 <= this.maxPower) {
            this.power2 += VisNetHandler.drainVis(level, pos, Aspect.WATER, 50);
         }

         if (prevHeat / 50 != this.heat / 50) {
            this.markForUpdate();
         }

         AABB area = new AABB(
            pos.getX() + 0.1, pos.getY() + 0.6, pos.getZ() + 0.1,
            pos.getX() + 0.9, pos.getY() + 1.5, pos.getZ() + 0.9
         );
         for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            ItemStack stack = item.getItem();
            if (stack.isEmpty()) {
               continue;
            }

            if (this.process(stack) == ResultSmelt.SUCCESS) {
               stack.shrink(1);
               if (stack.isEmpty()) {
                  item.discard();
               } else {
                  item.setItem(stack);
               }

               level.playSound(
                  null, pos, ModSounds.BUBBLE.get(), SoundSource.BLOCKS, 0.2F, 1.0F + level.random.nextFloat() * 0.4F
               );
               break;
            }
         }
      }

      if (this.count % 10 == 0) {
         this.updateStructure(level, pos);
      }
   }

   /** Плавка одного предмета: жрёт heat*2 + power1 + power2 (порт process из 1.7.10). */
   public ResultSmelt process(ItemStack stack) {
      if (this.processed > 0) {
         return ResultSmelt.WAIT;
      }

      if (stack == null || stack.isEmpty()) {
         return ResultSmelt.NON_SMELT;
      }

      AspectList al = ThaumcraftCraftingManager.getObjectTags(stack);
      al = ThaumcraftCraftingManager.getBonusTags(stack, al);
      if (al == null || al.size() == 0) {
         return ResultSmelt.NON_SMELT;
      }

      int aa = al.visSize();
      if (aa + this.aspects.visSize() > this.maxVis) {
         return ResultSmelt.FULL;
      }

      if (aa * 2 > this.heat || aa > this.power1 || aa > this.power2) {
         return ResultSmelt.WAIT;
      }

      this.heat -= aa * 2;
      this.power1 -= aa;
      this.power2 -= aa;
      this.processed += (int)(5.0F + Math.max(0.0F, (1.0F - (float)this.heat / (float)this.maxPower) * 100.0F));
      this.aspects.add(al);
      this.vis = this.aspects.visSize();
      this.setChanged();
      this.markForUpdate();
      return ResultSmelt.SUCCESS;
   }

   /** Обновляет визуальные состояния деталей (жар на burn-блоках, заполнение на баках). */
   private void updateStructure(Level level, BlockPos pos) {
      int burn = (int)Math.min(4.0F, (float)this.heat / this.maxPower * 4.0F);
      int fill = (int)Math.min(9.0F, (float)this.vis / this.maxVis * 9.0F);

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 && dz == 0) {
               continue;
            }

            for (int dy = 0; dy <= 1; dy++) {
               BlockPos partPos = pos.offset(dx, dy, dz);
               BlockState partState = level.getBlockState(partPos);
               if (partState.getBlock() instanceof AdvAlchemyFurnaceBurnBlock
                  && (Integer)partState.getValue(AdvAlchemyFurnaceBurnBlock.BURN) != burn) {
                  level.setBlock(partPos, partState.setValue(AdvAlchemyFurnaceBurnBlock.BURN, burn), 2);
               } else if (partState.getBlock() instanceof AdvAlchemyFurnaceTankBlock
                  && (Integer)partState.getValue(AdvAlchemyFurnaceTankBlock.FILL) != fill) {
                  level.setBlock(partPos, partState.setValue(AdvAlchemyFurnaceTankBlock.FILL, fill), 2);
               }
            }
         }
      }
   }

   @Override
   public AspectList getAspects() {
      return this.aspects;
   }

   @Override
   public void setAspects(AspectList aspectList) {
      this.aspects = aspectList == null ? new AspectList() : aspectList;
      this.vis = this.aspects.visSize();
      this.setChanged();
      this.markForUpdate();
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return this.aspects.visSize() < this.maxVis;
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      int added = Math.min(amount, Math.max(0, this.maxVis - this.aspects.visSize()));
      if (added > 0) {
         this.aspects.add(aspect, added);
         this.vis = this.aspects.visSize();
         this.setChanged();
         this.markForUpdate();
      }

      return amount - added;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      if (this.aspects.getAmount(aspect) < amount) {
         return false;
      }

      this.aspects.remove(aspect, amount);
      this.vis = this.aspects.visSize();
      this.setChanged();
      this.markForUpdate();
      return true;
   }

   @Deprecated
   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      for (Aspect aspect : aspectList.getAspects()) {
         if (this.aspects.getAmount(aspect) < aspectList.getAmount(aspect)) {
            return false;
         }
      }

      for (Aspect aspect : aspectList.getAspects()) {
         this.aspects.remove(aspect, aspectList.getAmount(aspect));
      }

      this.vis = this.aspects.visSize();
      this.setChanged();
      this.markForUpdate();
      return true;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return this.aspects.getAmount(aspect) >= amount;
   }

   @Deprecated
   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      for (Aspect aspect : aspectList.getAspects()) {
         if (this.aspects.getAmount(aspect) < aspectList.getAmount(aspect)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return this.aspects.getAmount(aspect);
   }

   public static enum ResultSmelt {
      SUCCESS,
      FULL,
      WAIT,
      NON_SMELT;
   }
}
