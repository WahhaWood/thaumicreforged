package thaumcraft.research;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.init.registry.ModAttachments;

public class PlayerKnowledge implements INBTSerializable<CompoundTag> {
   private static final Map<UUID, PlayerKnowledge> CACHE = new HashMap<>();
   private static final Set<UUID> CACHE_MISS = new HashSet<>();
   private final Player player;
   private final HashSet<String> researchCompleted = new HashSet<>();
   private final AspectList aspectsDiscovered = new AspectList();
   private final HashSet<String> objectsScanned = new HashSet<>();
   private final HashSet<String> entitiesScanned = new HashSet<>();
   private final HashSet<String> phenomenaScanned = new HashSet<>();
   private int warpCount;
   private int warp;
   private int warpSticky;
   private int warpTemp;

   public PlayerKnowledge(Player player) {
      this.player = player;
   }

   public Player getPlayer() {
      return this.player;
   }

   public void clear() {
      this.researchCompleted.clear();
      this.aspectsDiscovered.clear();
      this.objectsScanned.clear();
      this.entitiesScanned.clear();
      this.phenomenaScanned.clear();
      this.warp = 0;
      this.warpTemp = 0;
      this.warpSticky = 0;
   }

   @NotNull
   public HashSet<String> getResearchCompleted() {
      return this.researchCompleted;
   }

   @NotNull
   public HashSet<String> getObjectsScanned() {
      return this.objectsScanned;
   }

   @NotNull
   public HashSet<String> getEntitiesScanned() {
      return this.entitiesScanned;
   }

   @NotNull
   public HashSet<String> getPhenomenaScanned() {
      return this.phenomenaScanned;
   }

   @NotNull
   public AspectList getAspectsDiscovered() {
      AspectList list = this.aspectsDiscovered;
      if (list.size() <= 6) {
         this.addDiscoveredPrimalAspects();
      }

      return list;
   }

   public boolean hasDiscoveredAspect(Aspect aspect) {
      return this.getAspectsDiscovered().aspects.containsKey(aspect);
   }

   public boolean hasDiscoveredParentAspects(Aspect aspect) {
      if (aspect == null) {
         return false;
      } else {
         Aspect[] components = aspect.getComponents();
         if (components == null) {
            return true;
         } else {
            for (Aspect component : components) {
               if (!this.aspectsDiscovered.aspects.containsKey(component)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public void addDiscoveredPrimalAspects() {
      AspectList list = this.aspectsDiscovered;
      if (!list.aspects.containsKey(Aspect.AIR)) {
         list.add(Aspect.AIR, 0);
      }

      if (!list.aspects.containsKey(Aspect.FIRE)) {
         list.add(Aspect.FIRE, 0);
      }

      if (!list.aspects.containsKey(Aspect.EARTH)) {
         list.add(Aspect.EARTH, 0);
      }

      if (!list.aspects.containsKey(Aspect.WATER)) {
         list.add(Aspect.WATER, 0);
      }

      if (!list.aspects.containsKey(Aspect.ORDER)) {
         list.add(Aspect.ORDER, 0);
      }

      if (!list.aspects.containsKey(Aspect.ENTROPY)) {
         list.add(Aspect.ENTROPY, 0);
      }
   }

   public void addDiscoveredAspect(Aspect aspect) {
      if (!this.aspectsDiscovered.aspects.containsKey(aspect)) {
         this.aspectsDiscovered.add(aspect, 0);
      }
   }

   public short getAspectPoolFor(@NotNull Aspect aspect) {
      return (short)this.aspectsDiscovered.getAmount(aspect);
   }

   public boolean addAspectPool(@NotNull Aspect aspect, short amount) {
      if (amount != 0) {
         if (amount > 0) {
            this.aspectsDiscovered.add(aspect, amount);
            return true;
         }

         if (this.aspectsDiscovered.getAmount(aspect) > 0) {
            this.aspectsDiscovered.reduce(aspect, -amount);
            return true;
         }
      }

      return false;
   }

   public void setAspectPool(@NotNull Aspect aspect, short amount) {
      this.aspectsDiscovered.aspects.put(aspect, Integer.valueOf(amount));
   }

   public int getWarpCounter() {
      return this.warpCount;
   }

   public void setWarpCounter(int value) {
      this.warpCount = value;
   }

   public int getWarpTotal() {
      return this.warp + this.warpTemp + this.warpSticky;
   }

   public int getWarpPerm() {
      return this.warp;
   }

   public int getWarpTemp() {
      return this.warpTemp;
   }

   public int getWarpSticky() {
      return this.warpSticky;
   }

   public void addWarpTemp(int value) {
      this.warpTemp = Math.max(0, this.warpTemp + value);
   }

   public void addWarpPerm(int value) {
      this.warp = Math.max(0, this.warp + value);
   }

   public void addWarpSticky(int value) {
      this.warpSticky = Math.max(0, this.warpSticky + value);
   }

   public void setWarpSticky(int value) {
      this.warpSticky = Math.max(0, value);
   }

   public void setWarpPerm(int value) {
      this.warp = Math.max(0, value);
   }

   public void setWarpTemp(int value) {
      this.warpTemp = Math.max(0, value);
   }

   public CompoundTag serializeNBT() {
      CompoundTag compound = new CompoundTag();
      ListTag tagList = new ListTag();

      for (String s : this.researchCompleted) {
         tagList.add(StringTag.valueOf(s));
      }

      compound.put("research", tagList);
      tagList = new ListTag();

      for (Entry<Aspect, Integer> entry : this.aspectsDiscovered.aspects.entrySet()) {
         if (entry.getKey() != null) {
            CompoundTag aspectTag = new CompoundTag();
            aspectTag.putString("key", entry.getKey().getTag());
            aspectTag.putShort("amount", (short)Mth.clamp(entry.getValue(), 0, 32767));
            tagList.add(aspectTag);
         }
      }

      compound.put("aspects", tagList);
      CompoundTag scanTag = new CompoundTag();
      tagList = new ListTag();

      for (String s : this.objectsScanned) {
         tagList.add(StringTag.valueOf(s));
      }

      scanTag.put("objects", tagList);
      tagList = new ListTag();

      for (String s : this.entitiesScanned) {
         tagList.add(StringTag.valueOf(s));
      }

      scanTag.put("entities", tagList);
      tagList = new ListTag();

      for (String s : this.phenomenaScanned) {
         tagList.add(StringTag.valueOf(s));
      }

      scanTag.put("phenomenas", tagList);
      compound.put("scan", scanTag);
      if (Thaumcraft.instance.runicEventHandler.runicCharge.containsKey(this.player.getUUID())) {
         compound.putInt("shielding", Thaumcraft.instance.runicEventHandler.runicCharge.get(this.player.getUUID()));
      }

      CompoundTag eldritchTag = new CompoundTag();
      eldritchTag.putInt("perm", this.warp);
      eldritchTag.putInt("temp", this.warpTemp);
      eldritchTag.putInt("sticky", this.warpSticky);
      eldritchTag.putInt("counter", this.warpCount);
      compound.put("eldritch", eldritchTag);
      return compound;
   }

   public CompoundTag serializeNBT(Provider provider) {
      return this.serializeNBT();
   }

   public void deserializeNBT(Provider provider, CompoundTag compound) {
      this.deserializeNBT(compound);
   }

   public void deserializeNBT(CompoundTag compound) {
      this.clear();
      ListTag tagList = compound.getList("research", 8);

      for (int j = 0; j < tagList.size(); j++) {
         this.researchCompleted.add(tagList.getString(j));
      }

      tagList = compound.getList("aspects", 10);

      for (int j = 0; j < tagList.size(); j++) {
         CompoundTag rs = tagList.getCompound(j);
         if (rs.contains("key")) {
            Aspect aspect = Aspect.getAspect(rs.getString("key"));
            short amount = rs.getShort("amount");
            if (aspect != null) {
               this.aspectsDiscovered.aspects.put(aspect, Integer.valueOf(amount));
            }
         }
      }

      CompoundTag scanTag = compound.getCompound("scan");
      tagList = scanTag.getList("objects", 8);

      for (int jx = 0; jx < tagList.size(); jx++) {
         this.objectsScanned.add(tagList.getString(jx));
      }

      tagList = scanTag.getList("entities", 8);

      for (int jx = 0; jx < tagList.size(); jx++) {
         this.entitiesScanned.add(tagList.getString(jx));
      }

      tagList = scanTag.getList("phenomenas", 8);

      for (int jx = 0; jx < tagList.size(); jx++) {
         this.phenomenaScanned.add(tagList.getString(jx));
      }

      if (compound.contains("shielding")) {
         Thaumcraft.instance.runicEventHandler.runicCharge.put(this.player.getUUID(), compound.getInt("shielding"));
         Thaumcraft.instance.runicEventHandler.isDirty = true;
      }

      CompoundTag eldritchTag = compound.getCompound("eldritch");
      this.warp = eldritchTag.getInt("perm");
      this.warpTemp = eldritchTag.getInt("temp");
      this.warpSticky = eldritchTag.getInt("sticky");
      this.warpCount = eldritchTag.getInt("counter");
   }

   public void refreshProgressionState() {
      if (this.player != null && !this.player.level().isClientSide) {
         this.applyAutoUnlockResearch();
         this.applyPlayerKnowledgeHooks();
         this.ensurePrimalAspectPools();
      }
   }

   private void applyAutoUnlockResearch() {
      if (this.player != null) {
         ResearchManager researchManager = Thaumcraft.setup.getResearchManager();

         for (ResearchCategoryList cat : ResearchManager.LOADER.getByName().values()) {
            for (ResearchItem ri : cat.research.values()) {
               if (ri.isAutoUnlock()) {
                  researchManager.completeResearch(this.player, ri.key);
               }
            }
         }
      }
   }

   private void applyPlayerKnowledgeHooks() {
      if (this.player != null) {
         for (ResearchCategoryList cat : ResearchManager.LOADER.getByName().values()) {
            for (ResearchItem ri : cat.research.values()) {
               ri.onPlayerKnowledgeLoaded(this.player, this.researchCompleted);
            }
         }
      }
   }

   private void ensurePrimalAspectPools() {
      if (this.player != null) {
         short amount = (short)(15 + this.player.level().random.nextInt(5));

         for (Aspect aspect : Aspect.aspects.values()) {
            if (aspect.getComponents() == null && !this.aspectsDiscovered.aspects.containsKey(aspect)) {
               this.addDiscoveredAspect(aspect);
               this.setAspectPool(aspect, amount);
            }
         }
      }
   }

   @NotNull
   public static PlayerKnowledge of(Player player) {
      return (PlayerKnowledge)player.getData(ModAttachments.PLAYER_KNOWLEDGE);
   }
}
