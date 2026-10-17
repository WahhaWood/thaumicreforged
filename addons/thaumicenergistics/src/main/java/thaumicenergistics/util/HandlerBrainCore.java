package thaumicenergistics.util;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.GenericStack;
import java.util.ArrayList;
import java.util.function.BiFunction;
import thaumicenergistics.item.ItemBrainCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.util.ItemStackData;

public class HandlerBrainCore {
   private static final String NBTKEY_PATTERNS = "Patterns";
   private final BiFunction<CompoundTag, Level, IPatternDetails> patternFactory;
   private final ArrayList<IPatternDetails> patterns;
   private final int maxStored;
   private ItemStack brainCore;

   public HandlerBrainCore(int maxStored, BiFunction<CompoundTag, Level, IPatternDetails> patternFactory) {
      this.maxStored = maxStored;
      this.patterns = new ArrayList<>(this.maxStored);
      this.patternFactory = patternFactory;
   }

   private CompoundTag getOrCreateNBT() {
      return ItemStackData.getOrCreateTag(this.brainCore);
   }

   private void loadBrainCoreData(Level level) {
      this.patterns.clear();
      CompoundTag data = this.getOrCreateNBT();
      if (data.contains("Patterns", 9)) {
         ListTag plist = data.getList("Patterns", 10);

         for (int index = 0; index < plist.size(); index++) {
            try {
               this.patterns.add(this.patternFactory.apply(plist.getCompound(index), level));
            } catch (Exception var6) {
            }
         }
      }
   }

   private void saveBrainCoreData() {
      CompoundTag data = this.getOrCreateNBT();
      ListTag plist = new ListTag();

      for (IPatternDetails pattern : this.patterns) {
         if (pattern != null) {
            CompoundTag patternTag = ItemStackData.getTag(pattern.getDefinition().getReadOnlyStack());
            if (patternTag != null) {
               plist.add(patternTag.copy());
            }
         }
      }

      if (plist.size() > 0) {
         data.put("Patterns", plist);
      } else {
         data.remove("Patterns");
      }

      ItemStackData.setTag(this.brainCore, data);
   }

   public void addPattern(IPatternDetails pattern) {
      if (pattern != null) {
         if (this.hasRoomToStorePattern()) {
            IPatternDetails existingPattern = this.getPatternForItem(pattern.getPrimaryOutput());
            if (existingPattern == null) {
               this.patterns.add(pattern);
               this.saveBrainCoreData();
            }
         }
      }
   }

   public void close() {
      this.brainCore = null;
      this.patterns.clear();
   }

   public IPatternDetails getPatternForItem(GenericStack resultStack) {
      if (resultStack == null) {
         return null;
      } else {
         for (IPatternDetails p : this.patterns) {
            if (p != null && p.getPrimaryOutput() != null && resultStack.what().matches(p.getPrimaryOutput())) {
               return p;
            }
         }

         return null;
      }
   }

   public ArrayList<IPatternDetails> getPatterns() {
      return this.patterns;
   }

   public ArrayList<ItemStack> getStoredOutputs() {
      ArrayList<ItemStack> results = new ArrayList<>();

      for (IPatternDetails p : this.patterns) {
         if (p != null && p.getPrimaryOutput() != null) {
            results.add(p.getPrimaryOutput().what().wrapForDisplayOrFilter());
         }
      }

      return results;
   }

   public boolean hasCore() {
      return this.brainCore != null;
   }

   public boolean hasPatternFor(ItemStack resultStack) {
      GenericStack gs = GenericStack.fromItemStack(resultStack);
      return gs != null && this.getPatternForItem(gs) != null;
   }

   public boolean hasRoomToStorePattern() {
      return this.patterns.size() < this.maxStored;
   }

   public boolean isCoreChanged(ItemStack coreStack) {
      if (this.brainCore == null) {
         return true;
      } else if (coreStack == null || coreStack.isEmpty()) {
         return true;
      } else {
         return !(coreStack.getItem() instanceof ItemBrainCore) ? true : !ItemStackData.sameItemSameTags(coreStack, this.brainCore);
      }
   }

   public void open(ItemStack brainCore, Level level) {
      this.close();
      this.brainCore = brainCore;
      this.loadBrainCoreData(level);
   }

   public void removePattern(IPatternDetails pattern) {
      if (this.patterns.remove(pattern)) {
         this.saveBrainCoreData();
      }
   }
}
