package thaumcraft.api.aspects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import thaumcraft.recipe.ThaumcraftCraftingManager;

public class AspectList implements Serializable {
   private static final Codec<AspectList.AspectEntry> ENTRY_CODEC = RecordCodecBuilder.create(
      builder -> builder.group(
            Codec.STRING.fieldOf("key").forGetter(AspectList.AspectEntry::key),
            Codec.INT.optionalFieldOf("amount", 1).forGetter(AspectList.AspectEntry::amount)
         )
         .apply(builder, AspectList.AspectEntry::new)
   );
   public static final Codec<AspectList> CODEC = ENTRY_CODEC.listOf().xmap(AspectList::fromEntries, AspectList::toEntries);
   public static final StreamCodec<RegistryFriendlyByteBuf, AspectList> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, AspectList>() {
      public AspectList decode(RegistryFriendlyByteBuf buf) {
         AspectList list = new AspectList();
         int size = buf.readShort();

         for (int i = 0; i < size; i++) {
            list.aspects.put(Aspect.getAspect(buf.readUtf()), Integer.valueOf(buf.readShort()));
         }

         return list;
      }

      public void encode(RegistryFriendlyByteBuf buf, AspectList value) {
         buf.writeShort(value.aspects.size());
         value.aspects.forEach((aspect, amount) -> {
            buf.writeUtf(aspect.getTag());
            buf.writeShort(amount);
         });
      }
   };
   public LinkedHashMap<Aspect, Integer> aspects = new LinkedHashMap<>();
   private int maxAmount = 32767;

   public AspectList(ItemLike itemLike) {
      this(new ItemStack(itemLike));
   }

   public AspectList(ItemStack stack) {
      try {
         AspectList resolvedAspects = ThaumcraftCraftingManager.getObjectTags(stack);
         if (resolvedAspects != null) {
            for (Aspect tag : resolvedAspects.getAspects()) {
               this.add(tag, resolvedAspects.getAmount(tag));
            }
         }
      } catch (Exception var7) {
      }
   }

   public AspectList(int maxAmount) {
      this.maxAmount = maxAmount;
   }

   public AspectList() {
   }

   public void setMaxAmount(int maxAmount) {
      this.maxAmount = maxAmount;
      this.aspects.replaceAll((aspect, integer) -> Math.min(integer, this.maxAmount));
   }

   @Override
   public String toString() {
      return this.aspects == null ? "{}" : this.aspects.toString();
   }

   public void forEach(BiConsumer<Aspect, Integer> action) {
      this.aspects.forEach(action);
   }

   public void clear() {
      this.aspects.clear();
   }

   public AspectList copy() {
      AspectList out = new AspectList(this.maxAmount);

      for (Aspect a : this.getAspects()) {
         out.add(a, this.getAmount(a));
      }

      return out;
   }

   public int size() {
      return this.aspects.size();
   }

   public int visSize() {
      int q = 0;

      for (Aspect as : this.aspects.keySet()) {
         q += this.getAmount(as);
      }

      return q;
   }

   public Aspect[] getAspects() {
      return this.aspects.keySet().toArray(new Aspect[0]);
   }

   public Aspect[] getPrimalAspects() {
      AspectList t = new AspectList();

      for (Aspect as : this.aspects.keySet()) {
         if (as.isPrimal()) {
            t.add(as, 1);
         }
      }

      return t.aspects.keySet().toArray(new Aspect[0]);
   }

   public Aspect[] getAspectsSorted() {
      return this.aspects.keySet().stream().filter(Objects::nonNull).sorted(Comparator.comparing(Aspect::getTag)).toArray(Aspect[]::new);
   }

   public Aspect[] getAspectsSortedAmount() {
      return this.aspects
         .keySet()
         .stream()
         .filter(Objects::nonNull)
         .sorted((a1, a2) -> Integer.compare(this.getAmount(a2), this.getAmount(a1)))
         .toArray(Aspect[]::new);
   }

   public void setAmount(Aspect key, int amount) {
      this.aspects.put(key, amount);
   }

   public int getAmount(Aspect key) {
      return this.aspects.get(key) == null ? 0 : this.aspects.get(key);
   }

   public boolean reduce(Aspect key, int amount) {
      if (this.getAmount(key) >= amount) {
         int am = this.getAmount(key) - amount;
         this.aspects.put(key, am);
         return true;
      } else {
         return false;
      }
   }

   public AspectList remove(Aspect key, int amount) {
      int am = this.getAmount(key) - amount;
      if (am <= 0) {
         this.aspects.remove(key);
      } else {
         this.aspects.put(key, am);
      }

      return this;
   }

   public AspectList remove(Aspect key) {
      this.aspects.remove(key);
      return this;
   }

   public AspectList add(Aspect aspect, int amount) {
      int storage = this.aspects.getOrDefault(aspect, 0);
      if (storage >= this.maxAmount) {
         return this;
      } else {
         amount += storage;
         if (amount > this.maxAmount) {
            amount = this.maxAmount;
         }

         if (amount <= 0) {
            return this;
         } else {
            this.aspects.put(aspect, amount);
            return this;
         }
      }
   }

   public AspectList merge(Aspect aspect, int amount) {
      if (this.aspects.containsKey(aspect)) {
         int oldamount = this.aspects.get(aspect);
         if (amount < oldamount) {
            amount = oldamount;
         }
      }

      if (amount > this.maxAmount) {
         amount = this.maxAmount;
      }

      this.aspects.put(aspect, Math.max(0, amount));
      return this;
   }

   public AspectList add(AspectList in) {
      for (Aspect a : in.getAspects()) {
         this.add(a, in.getAmount(a));
      }

      return this;
   }

   public AspectList merge(AspectList in) {
      for (Aspect a : in.getAspects()) {
         this.merge(a, in.getAmount(a));
      }

      return this;
   }

   public void load(CompoundTag compound) {
      this.load(compound, "aspects");
   }

   public void load(CompoundTag compound, String label) {
      this.aspects.clear();
      if (compound != null) {
         ListTag listTag = compound.getList(label, 10);

         for (int j = 0; j < listTag.size(); j++) {
            CompoundTag rs = listTag.getCompound(j);
            if (rs.contains("key")) {
               Aspect aspect = Aspect.getAspect(rs.getString("key"));
               if (aspect != null) {
                  this.add(aspect, rs.getInt("amount"));
               }
            }
         }
      }
   }

   public void save(CompoundTag compound) {
      this.save(compound, "aspects");
   }

   public void save(CompoundTag compound, String label) {
      if (compound != null) {
         ListTag listTag = new ListTag();

         for (Aspect aspect : this.getAspects()) {
            if (aspect != null) {
               CompoundTag f = new CompoundTag();
               f.putString("key", aspect.getTag());
               f.putInt("amount", this.getAmount(aspect));
               listTag.add(f);
            }
         }

         compound.put(label, listTag);
      }
   }

   public void toNetwork(FriendlyByteBuf buf) {
      buf.writeShort(this.aspects.size());
      this.aspects.forEach((aspect, amount) -> {
         buf.writeUtf(aspect.getTag());
         buf.writeShort(amount);
      });
   }

   public JsonArray toJson() {
      JsonArray array = new JsonArray();
      this.aspects.forEach((aspect, amount) -> {
         JsonObject object = new JsonObject();
         object.addProperty("key", aspect.getTag());
         if (amount > 1) {
            object.addProperty("amount", amount);
         }

         array.add(object);
      });
      return array;
   }

   public static AspectList fromNetwork(FriendlyByteBuf buf) {
      AspectList aspectList = new AspectList();
      int size = buf.readShort();

      for (int i = 0; i < size; i++) {
         aspectList.aspects.put(Aspect.getAspect(buf.readUtf()), Integer.valueOf(buf.readShort()));
      }

      return aspectList;
   }

   public static AspectList fromJson(JsonArray array) {
      AspectList aspectList = new AspectList();

      for (JsonElement element : array) {
         JsonObject o = element.getAsJsonObject();
         aspectList.setAmount(Aspect.fromJson(o), GsonHelper.getAsShort(o, "amount", (short)1));
      }

      return aspectList;
   }

   private static AspectList fromEntries(List<AspectList.AspectEntry> entries) {
      AspectList list = new AspectList();

      for (AspectList.AspectEntry entry : entries) {
         Aspect aspect = Aspect.getAspect(entry.key());
         if (aspect != null) {
            list.setAmount(aspect, entry.amount());
         }
      }

      return list;
   }

   private static List<AspectList.AspectEntry> toEntries(AspectList list) {
      ArrayList<AspectList.AspectEntry> entries = new ArrayList<>();

      for (Aspect aspect : list.getAspects()) {
         entries.add(new AspectList.AspectEntry(aspect.getTag(), list.getAmount(aspect)));
      }

      return entries;
   }

   private record AspectEntry(String key, int amount) {
   }
}
