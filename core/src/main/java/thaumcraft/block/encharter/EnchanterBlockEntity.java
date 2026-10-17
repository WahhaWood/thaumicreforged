package thaumcraft.block.encharter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.enchantment.EnchantmentData;
import thaumcraft.enchantment.EnchantmentManager;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.util.ItemStackData;

public class EnchanterBlockEntity extends ThaumcraftSideInvBlockEntity implements ITickable, MenuProvider {
   private static final String TAG_ENCHANTMENTS = "enchantments";
   private static final String TAG_LEVELS = "levels";
   private static final String TAG_TOTAL_ASPECTS = "totalAspects";
   private static final String TAG_CURRENT_ASPECTS = "currentAspects";
   private static final String TAG_WORKING = "working";
   public final List<ResourceLocation> enchantments = new ArrayList<>();
   public final List<Integer> levels = new ArrayList<>();
   public AspectList totalAspects = new AspectList();
   public AspectList currentAspects = new AspectList();
   public boolean working;
   private final List<EnchanterBlockEntity.PillarData> pillars = new ArrayList<>();

   public EnchanterBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.ENCHANTER.get(), pos, state, 2);
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      ItemStack tool = this.getItem(0);
      if (!this.isValidEnchantTarget(tool)) {
         if (this.working || !this.enchantments.isEmpty() || !this.levels.isEmpty()) {
            this.clearEnchantingState();
         }
         return;
      }

      if (!this.working) {
         return;
      }

      if (!this.checkPillars()) {
         this.working = false;
         this.markForUpdate();
         this.setChanged();
         return;
      }

      if (this.hasEnoughAspects()) {
         this.applyEnchantmentsAndFinish(level, pos, tool);
         return;
      }

      ItemStack wandStack = this.getItem(1);
      if (wandStack.getItem() instanceof ItemWandCasting wand && !wand.isStaff(wandStack)) {
         AspectList wandAspects = wand.getAllVis(wandStack);
         List<Aspect> primals = Aspect.getPrimalAspects();
         List<Aspect> aspectsThatCanGet = new ArrayList<>();

         for (Aspect a : primals) {
            int missing = this.totalAspects.getAmount(a) - this.currentAspects.getAmount(a);
            if (missing > 0 && wandAspects.getAmount(a) >= 100) {
               aspectsThatCanGet.add(a);
            }
         }

         if (!aspectsThatCanGet.isEmpty()) {
            int i = level.random.nextInt(aspectsThatCanGet.size());
            Aspect aspectToDrain = aspectsThatCanGet.get(i);
            boolean drained = wand.consumeAllVisCrafting(wandStack, null, new AspectList().add(aspectToDrain, 1), true);
            if (drained) {
               this.currentAspects.add(aspectToDrain, 1);
               this.markForUpdate();
               this.setChanged();

               if (level.random.nextInt(5) == 0) {
                  level.playSound(null, pos, ModSounds.WAND.get(), SoundSource.BLOCKS, 0.5F, 1.0F + level.random.nextFloat() * 0.2F);
               }
            }
         }
      }
   }

   private void applyEnchantmentsAndFinish(Level level, BlockPos pos, ItemStack tool) {
      this.working = false;
      this.currentAspects = new AspectList();
      this.totalAspects = new AspectList();

      var registryAccess = level.registryAccess();
      var enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);

      for (int i = 0; i < this.enchantments.size() && i < this.levels.size(); i++) {
         ResourceLocation enchantId = this.enchantments.get(i);
         int lvl = this.levels.get(i);
         var holder = enchantmentRegistry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, enchantId));
         if (holder.isPresent()) {
            tool.enchant(holder.get(), lvl);
         }
      }

      this.enchantments.clear();
      this.levels.clear();

      level.playSound(null, pos, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
      level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

      this.markForUpdate();
      this.setChanged();
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      ItemStack tool = this.getItem(0);
      if (!this.isValidEnchantTarget(tool)) {
         this.enchantments.clear();
         this.levels.clear();
      }

      if (this.working) {
         if (!tool.isEmpty() && !tool.isEnchanted()) {
            if (this.checkPillars()) {
               if (this.working) {
                  ItemStack wandStack = this.getItem(1);
                  if (wandStack.getItem() instanceof ItemWandCasting wand && !wand.isStaff(wandStack)) {
                     AspectList wandAspects = wand.getAllVis(wandStack);
                     List<Aspect> primals = Aspect.getPrimalAspects();
                     List<Aspect> aspectsThatCanGet = new ArrayList<>();

                     for (Aspect a : primals) {
                        int missing = this.totalAspects.getAmount(a) - this.currentAspects.getAmount(a);
                        if (missing > 0 && wandAspects.getAmount(a) >= 100) {
                           aspectsThatCanGet.add(a);
                        }
                     }

                     if (!aspectsThatCanGet.isEmpty()) {
                        int i = level.random.nextInt(aspectsThatCanGet.size());
                        if (!this.pillars.isEmpty() && level.random.nextBoolean()) {
                           EnchanterBlockEntity.PillarData p = this.pillars.get(i % this.pillars.size());
                           Thaumcraft.setup
                              .blockRunes(
                                 level,
                                 p.x,
                                 p.topY - 0.75,
                                 p.z,
                                 0.3F + level.random.nextFloat() * 0.7F,
                                 0.0F,
                                 0.3F + level.random.nextFloat() * 0.7F,
                                 15,
                                 level.random.nextFloat() / 8.0F
                              );
                           Thaumcraft.setup
                              .blockRunes(
                                 level,
                                 pos.getX(),
                                 pos.getY() + 0.25,
                                 pos.getZ(),
                                 0.3F + level.random.nextFloat() * 0.7F,
                                 0.0F,
                                 0.3F + level.random.nextFloat() * 0.7F,
                                 15,
                                 level.random.nextFloat() / 8.0F
                              );
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isValidEnchantTarget(ItemStack stack) {
      return !stack.isEmpty() && !stack.isEnchanted() && stack.getCount() == 1 && stack.getItem().isEnchantable(stack);
   }

   private boolean hasEnoughAspects() {
      for (Aspect aspect : Aspect.getPrimalAspects()) {
         if (this.currentAspects.getAmount(aspect) < this.totalAspects.getAmount(aspect)) {
            return false;
         }
      }

      return true;
   }

   public boolean checkPillars() {
      if (this.level == null) {
         return false;
      } else if (this.pillars.isEmpty()) {
         if (this.assignPillars()) {
            this.working = false;
            this.currentAspects = new AspectList();
            if (!this.level.isClientSide) {
               this.markForUpdate();
               super.setChanged();
            }

            return false;
         } else {
            return true;
         }
      } else {
         for (EnchanterBlockEntity.PillarData pillar : this.pillars) {
            int pillarHeight = this.findPillar(pillar.x, pillar.baseY, pillar.z);
            if (pillarHeight == -1) {
               this.pillars.clear();
               return this.checkPillars();
            }

            pillar.topY = pillarHeight;
         }

         return true;
      }
   }

   private boolean assignPillars() {
      BlockPos origin = this.getBlockPos();

      for (int x = origin.getX() - 4; x <= origin.getX() + 4; x++) {
         for (int z = origin.getZ() - 4; z <= origin.getZ() + 4; z++) {
            int height = this.findPillar(x, origin.getY(), z);
            if (height != -1) {
               this.pillars.add(new EnchanterBlockEntity.PillarData(x, origin.getY(), z, height));
            }

            if (this.pillars.size() == 6) {
               return false;
            }
         }
      }

      this.pillars.clear();
      return true;
   }

   private int findPillar(int x, int y, int z) {
      if (this.level == null) {
         return -1;
      } else {
         int obsidianFound = 0;
         int i = 0;

         while (true) {
            int yPos = y + i;
            if (yPos >= this.level.getMaxBuildHeight()) {
               return -1;
            }

            BlockPos p = new BlockPos(x, yPos, z);
            Block block = this.level.getBlockState(p).getBlock();
            if (block != ModBlocks.OBSIDIAN_TOTEM.get() && block != ModBlocks.CHARGED_OBSIDIAN_TOTEM.get()) {
               if (block == ModBlocks.NITOR.get()) {
                  if (obsidianFound >= 2 && obsidianFound < 13) {
                     return yPos;
                  }

                  return -1;
               }

               return -1;
            }

            obsidianFound++;
            i++;
         }
      }
   }

   public void updateAspectList() {
      this.recalculateAspectList();
      if (!this.working) {
         this.currentAspects = new AspectList();
      }

      this.markForUpdate();
      super.setChanged();
   }

   void recalculateAspectList() {
      this.totalAspects = new AspectList();

      for (int i = 0; i < this.enchantments.size(); i++) {
         ResourceLocation enchantId = this.enchantments.get(i);
         int lvl = this.levels.get(i);
         Map<Integer, EnchantmentData> perLevel = EnchantmentManager.enchantmentData.get(enchantId);
         if (perLevel != null) {
            EnchantmentData data = perLevel.get(lvl);
            if (data != null) {
               for (Aspect aspect : data.aspects.getAspectsSorted()) {
                  this.totalAspects.add(aspect, data.aspects.getAmount(aspect));
               }
            }
         }
      }
   }

   @Override
   public void loadCustom(@NotNull CompoundTag tag) {
      super.loadCustom(tag);
      this.loadEnchanterData(tag);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.loadCustom(tag, provider);
      this.loadEnchanterData(tag);
   }

   private void loadEnchanterData(@NotNull CompoundTag tag) {
      this.working = tag.getBoolean("working");
      this.currentAspects = new AspectList();
      this.currentAspects.load(tag.getCompound("currentAspects"));
      this.totalAspects = new AspectList();
      this.totalAspects.load(tag.getCompound("totalAspects"));
      this.enchantments.clear();
      if (tag.contains("enchantments", 9)) {
         ListTag enchList = tag.getList("enchantments", 8);

         for (int i = 0; i < enchList.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(enchList.getString(i));
            if (id != null) {
               this.enchantments.add(id);
            }
         }
      }

      this.levels.clear();
      int[] lvlArr = tag.getIntArray("levels");

      for (int v : lvlArr) {
         this.levels.add(v);
      }
   }

   @Override
   public void saveCustom(@NotNull CompoundTag tag) {
      super.saveCustom(tag);
      this.saveEnchanterData(tag);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.saveCustom(tag, provider);
      this.saveEnchanterData(tag);
   }

   private void saveEnchanterData(@NotNull CompoundTag tag) {
      tag.putBoolean("working", this.working);
      CompoundTag current = new CompoundTag();
      this.currentAspects.save(current);
      tag.put("currentAspects", current);
      CompoundTag total = new CompoundTag();
      this.totalAspects.save(total);
      tag.put("totalAspects", total);
      ListTag enchList = new ListTag();

      for (ResourceLocation enchantmentId : this.enchantments) {
         enchList.add(StringTag.valueOf(enchantmentId.toString()));
      }

      tag.put("enchantments", enchList);
      tag.putIntArray("levels", this.levels.stream().mapToInt(Integer::intValue).toArray());
   }

   public void setChanged() {
      super.setChanged();
   }

   @Override
   public void setItem(int index, @NotNull ItemStack stack) {
      ItemStack previous = index >= 0 && index < this.getContainerSize() ? this.getItem(index).copy() : ItemStack.EMPTY;
      super.setItem(index, stack);
      if (index == 0 && !this.working && !sameStack(previous, stack)) {
         this.clearEnchantingState();
      }
   }

   @NotNull
   @Override
   public ItemStack removeItem(int index, int amount) {
      ItemStack removed = super.removeItem(index, amount);
      if (index == 0 && !this.working && !removed.isEmpty()) {
         this.clearEnchantingState();
      }

      return removed;
   }

   @NotNull
   @Override
   public ItemStack removeItemNoUpdate(int index) {
      ItemStack removed = super.removeItemNoUpdate(index);
      if (index == 0 && !this.working && !removed.isEmpty()) {
         this.clearEnchantingState();
      }

      return removed;
   }

   private void clearEnchantingState() {
      boolean changed = this.working
         || !this.enchantments.isEmpty()
         || !this.levels.isEmpty()
         || this.currentAspects.visSize() > 0
         || this.totalAspects.visSize() > 0;
      this.working = false;
      this.enchantments.clear();
      this.levels.clear();
      this.currentAspects = new AspectList();
      this.totalAspects = new AspectList();
      if (changed && this.level != null && !this.level.isClientSide) {
         this.markForUpdate();
         super.setChanged();
      }
   }

   private static boolean sameStack(ItemStack a, ItemStack b) {
      if (a.isEmpty() && b.isEmpty()) {
         return true;
      } else {
         return a.isEmpty() != b.isEmpty() ? false : ItemStackData.sameItemSameTags(a, b) && a.getCount() == b.getCount();
      }
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      if (this.working) {
         return false;
      } else if (index != 0) {
         if (index == 1) {
            return stack.getItem() instanceof ItemWandCasting wand && !wand.isStaff(stack) ? wand.getCap(stack).getBasicDiscount() <= 1.0F : false;
         } else {
            return false;
         }
      } else {
         return stack.getCount() == 1 && stack.getItem().isEnchantable(stack) && !stack.isEnchanted();
      }
   }

   @Override
   public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
      return !this.working;
   }

   @NotNull
   public Component getDisplayName() {
      return Component.translatable("container.thaumcraft.enchanter");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowsId, @NotNull Inventory inventory, @NotNull Player player) {
      return new EnchanterMenu(windowsId, inventory, this);
   }

   public static final class PillarData {
      public final int x;
      public final int baseY;
      public final int z;
      public int topY;

      public PillarData(int x, int baseY, int z, int topY) {
         this.x = x;
         this.baseY = baseY;
         this.z = z;
         this.topY = topY;
      }
   }
}
