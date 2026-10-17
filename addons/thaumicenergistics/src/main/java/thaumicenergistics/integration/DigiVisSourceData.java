package thaumicenergistics.integration.tc;

import appeng.api.ids.AEComponents;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.items.tools.MemoryCardItem;
import java.util.Map;
import thaumicenergistics.api.IDigiVisSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DigiVisSourceData {
   public static final String SOURCE_UNLOC_NAME = "thaumicenergistics.vis_source";
   private static final String MEMORY_CARD_SOURCE_UID = "thaumicenergistics.vis_source.uid";
   private static final String NBT_KEY_UID = "SourceUID";
   private long sourceUID = 0L;
   private boolean hasData = false;

   public DigiVisSourceData() {
   }

   public DigiVisSourceData(IDigiVisSource source) {
      this.sourceUID = source.getUID();
      this.hasData = true;
   }

   public boolean hasSourceData() {
      return this.hasData;
   }

   public void clearData() {
      this.sourceUID = 0L;
      this.hasData = false;
   }

   public static boolean isMemoryCardBlank(ItemStack memoryCard) {
      return memoryCard.get(AEComponents.EXPORTED_SETTINGS_SOURCE) == null;
   }

   public boolean readFromMemoryCard(ItemStack memoryCard) {
      Map<String, String> settings = (Map<String, String>)memoryCard.get(AEComponents.EXPORTED_SETTINGS);
      String encodedUID = settings == null ? null : settings.get("thaumicenergistics.vis_source.uid");
      if (encodedUID == null) {
         this.clearData();
         return false;
      } else {
         try {
            this.sourceUID = Long.parseLong(encodedUID);
            this.hasData = true;
            return true;
         } catch (NumberFormatException var5) {
            this.clearData();
            return false;
         }
      }
   }

   public void writeToMemoryCard(ItemStack memoryCard, Component sourceName) {
      MemoryCardItem.clearCard(memoryCard);
      memoryCard.set(AEComponents.EXPORTED_SETTINGS_SOURCE, sourceName);
      memoryCard.set(AEComponents.EXPORTED_SETTINGS, Map.of("thaumicenergistics.vis_source.uid", Long.toString(this.sourceUID)));
   }

   @Nullable
   public IDigiVisSource tryGetSource(@Nullable IGrid grid) {
      if (this.hasData && grid != null) {
         for (IGridNode node : grid.getNodes()) {
            if (node.getOwner() instanceof IDigiVisSource visSource && visSource.getUID() == this.sourceUID && visSource.isActive()) {
               return visSource;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public void readFromNBT(CompoundTag tag) {
      if (tag.contains("SourceUID")) {
         this.sourceUID = tag.getLong("SourceUID");
         this.hasData = true;
      } else {
         this.clearData();
      }
   }

   public void readFromNBT(CompoundTag tag, String key) {
      if (tag.contains(key)) {
         this.readFromNBT(tag.getCompound(key));
      } else {
         this.clearData();
      }
   }

   public CompoundTag writeToNBT() {
      CompoundTag tag = new CompoundTag();
      if (this.hasData) {
         tag.putLong("SourceUID", this.sourceUID);
      }

      return tag;
   }

   public void writeToNBT(CompoundTag tag, String key) {
      if (this.hasData) {
         tag.put(key, this.writeToNBT());
      }
   }
}
