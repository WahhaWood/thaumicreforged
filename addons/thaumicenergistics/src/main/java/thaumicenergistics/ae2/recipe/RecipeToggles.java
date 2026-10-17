package thaumicenergistics.ae2.recipe;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Which recipes an assembler offers to the network: a default for all recipes plus the ids switched the
 * other way. "Enable all" / "disable all" just set the default and clear the exceptions, so the stored
 * data stays small and recipes added later by datapacks follow the current default.
 */
public final class RecipeToggles {
   private static final String TAG_DEFAULT = "recipes_all_enabled";
   private static final String TAG_EXCEPTIONS = "recipes_exceptions";

   private boolean allEnabled;
   private final Set<ResourceLocation> exceptions = new HashSet<>();

   public RecipeToggles(boolean allEnabled) {
      this.allEnabled = allEnabled;
   }

   public boolean isEnabled(ResourceLocation id) {
      return this.allEnabled != this.exceptions.contains(id);
   }

   public void toggle(ResourceLocation id) {
      if (!this.exceptions.remove(id)) {
         this.exceptions.add(id);
      }
   }

   public void setAll(boolean enabled) {
      this.allEnabled = enabled;
      this.exceptions.clear();
   }

   public void save(CompoundTag tag) {
      tag.putBoolean(TAG_DEFAULT, this.allEnabled);
      ListTag list = new ListTag();
      for (ResourceLocation id : this.exceptions) {
         list.add(StringTag.valueOf(id.toString()));
      }

      tag.put(TAG_EXCEPTIONS, list);
   }

   /** Keeps the kind's default when the tag has never been written (fresh block). */
   public void load(CompoundTag tag) {
      if (tag.contains(TAG_DEFAULT)) {
         this.allEnabled = tag.getBoolean(TAG_DEFAULT);
      }

      this.exceptions.clear();
      ListTag list = tag.getList(TAG_EXCEPTIONS, Tag.TAG_STRING);
      for (int i = 0; i < list.size(); i++) {
         ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
         if (id != null) {
            this.exceptions.add(id);
         }
      }
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeBoolean(this.allEnabled);
      buf.writeVarInt(this.exceptions.size());
      for (ResourceLocation id : this.exceptions) {
         buf.writeResourceLocation(id);
      }
   }

   public void read(FriendlyByteBuf buf) {
      this.allEnabled = buf.readBoolean();
      this.exceptions.clear();
      int size = buf.readVarInt();
      for (int i = 0; i < size; i++) {
         this.exceptions.add(buf.readResourceLocation());
      }
   }
}
