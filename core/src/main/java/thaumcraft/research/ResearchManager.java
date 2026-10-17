package thaumcraft.research;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IScribeTools;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.page.ArcaneRecipePage;
import thaumcraft.api.research.page.CraftingRecipePage;
import thaumcraft.api.research.page.CrucibleRecipePage;
import thaumcraft.api.research.page.FormedStructurePage;
import thaumcraft.api.research.page.FortressArmorUpgradeRecipePage;
import thaumcraft.api.research.page.ImagePage;
import thaumcraft.api.research.page.InfusionEnchantmentRecipePage;
import thaumcraft.api.research.page.InfusionRecipePage;
import thaumcraft.api.research.page.RunicAugmentRecipePage;
import thaumcraft.api.research.page.SmeltingRecipePage;
import thaumcraft.api.research.page.TextPage;
import thaumcraft.api.research.type.KamiResearchItem;
import thaumcraft.api.research.type.TTResearchItem;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModItems;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.playerdata.PacketResearchComplete;
import thaumcraft.network.playerdata.PacketSyncScannedEntities;
import thaumcraft.network.playerdata.PacketSyncScannedItems;
import thaumcraft.network.playerdata.PacketSyncScannedPhenomena;
import thaumcraft.util.HexUtils;
import thaumcraft.util.ItemStackData;

public class ResearchManager {
   public static final ResearchLoader LOADER = new ResearchLoader();

   public static void init(IEventBus bus) {
      LOADER.registerResearchItemType(Thaumcraft.location("default"), ResearchItem::new);
      LOADER.registerResearchItemType(Thaumcraft.location("tt"), TTResearchItem::new);
      LOADER.registerResearchItemType(Thaumcraft.location("kami"), KamiResearchItem::new);
      LOADER.registerCategory("basics", Thaumcraft.location("textures/item/thaumonomicon.png"), Thaumcraft.location("textures/screen/research_background.png"));
      LOADER.registerCategory(
         "thaumaturgy", Thaumcraft.location("textures/misc/r_thaumaturgy.png"), Thaumcraft.location("textures/screen/research_background.png")
      );
      LOADER.registerCategory("alchemy", Thaumcraft.location("textures/misc/r_crucible.png"), Thaumcraft.location("textures/screen/research_background.png"));
      LOADER.registerCategory("artifice", Thaumcraft.location("textures/misc/r_artifice.png"), Thaumcraft.location("textures/screen/research_background.png"));
      LOADER.registerCategory("golemancy", Thaumcraft.location("textures/misc/r_golemancy.png"), Thaumcraft.location("textures/screen/research_background.png"));
      LOADER.registerCategory(
         "eldritch",
         Thaumcraft.location("textures/misc/r_eldritch.png"),
         Thaumcraft.location("textures/screen/research_background_eldritch.png"),
         Color.WHITE.getRGB()
      );
      LOADER.registerCategory("tinkerer", Thaumcraft.location("textures/misc/r_tinkerer.png"), Thaumcraft.location("textures/screen/research_background.png"));
      LOADER.registerPageType("text", TextPage::new);
      LOADER.registerPageType("image", ImagePage::new);
      LOADER.registerPageType("crafting", CraftingRecipePage::new);
      LOADER.registerPageType("smelting", SmeltingRecipePage::new);
      LOADER.registerPageType("arcane", ArcaneRecipePage::new);
      LOADER.registerPageType("crucible", CrucibleRecipePage::new);
      LOADER.registerPageType("infusion", InfusionRecipePage::new);
      LOADER.registerPageType("enchant", InfusionEnchantmentRecipePage::new);
      LOADER.registerPageType("infusion_enchant", InfusionEnchantmentRecipePage::new);
      LOADER.registerPageType("infusion_fortress", FortressArmorUpgradeRecipePage::new);
      LOADER.registerPageType("infusion_runic_augmentation", RunicAugmentRecipePage::new);
      LOADER.registerPageType("formed_structure", FormedStructurePage::new);
   }

   public static boolean createClue(Player player, Object clue, AspectList aspects) {
      Level level = player.level();
      ArrayList<String> directKeys = new ArrayList<>();
      ArrayList<String> aspectKeys = new ArrayList<>();

      for (ResearchCategoryList rcl : ResearchApi.getResearchLists().values()) {
         label115:
         for (ResearchItem ri : rcl.research.values()) {
            boolean valid = ri.tags != null
               && ri.tags.size() > 0
               && (ri.isLost() || ri.isHidden())
               && !isResearchComplete(player, ri.key)
               && !isResearchComplete(player, "@" + ri.key);
            if (valid) {
               if (clue instanceof ItemStack itemStack && ri.getItemTriggers() != null && ri.getItemTriggers().length > 0) {
                  for (Ingredient stack : ri.getItemTriggers()) {
                     if (stack.test(itemStack)) {
                        directKeys.add(ri.key);
                        continue label115;
                     }
                  }
               } else if (clue instanceof String && ri.getEntityTriggers() != null && ri.getEntityTriggers().length > 0) {
                  for (String entity : ri.getEntityTriggers()) {
                     if (clue.equals(entity)) {
                        directKeys.add(ri.key);
                        continue label115;
                     }
                  }
               }

               if (aspects != null && aspects.size() > 0 && ri.getAspectTriggers() != null && ri.getAspectTriggers().length > 0) {
                  for (Aspect aspect : ri.getAspectTriggers()) {
                     if (aspects.getAmount(aspect) > 0) {
                        aspectKeys.add(ri.key);
                        break;
                     }
                  }
               }
            }
         }
      }

      ArrayList<String> keys = !directKeys.isEmpty() ? directKeys : aspectKeys;
      if (keys.size() > 0) {
         String key = keys.get(level.random.nextInt(keys.size()));
         PacketHandler.sendTo(new PacketResearchComplete("@" + key), (ServerPlayer)player);
         ResearchApi.completeResearch(player, "@" + key);
         return true;
      } else {
         return false;
      }
   }

   public static ItemStack createResearchNoteForPlayer(Level world, Player player, String key) {
      Inventory inventory = player.getInventory();
      ItemStack note = ItemStack.EMPTY;
      int slot = getResearchSlot(player, key);
      if (slot >= 0) {
         note = inventory.getItem(slot);
      } else if (consumeInkFromPlayer(player, false) && ContainerHelper.clearOrCountMatchingItems(inventory, stack -> stack.is(Items.PAPER), 1, false) == 1) {
         consumeInkFromPlayer(player, true);
         note = createNote(new ItemStack((ItemLike)ModItems.RESEARCH_NOTES.get()), key, world);
         if (!inventory.add(note)) {
            player.drop(note, false);
         }

         player.inventoryMenu.broadcastChanges();
      }

      return note;
   }

   public static String findHiddenResearch(Player player) {
      ArrayList<String> keys = new ArrayList<>();

      for (ResearchItem research : LOADER.getHidden()) {
         if (!isResearchComplete(player, research.key)
            && doesPlayerHaveRequisites(player, research.key)
            && (research.getItemTriggers() != null || research.getEntityTriggers() != null || research.getAspectTriggers() != null)) {
            keys.add(research.key);
         }
      }

      Random rand = new Random(player.level().getGameTime() / 10L / 5L);
      if (keys.size() > 0) {
         int r = rand.nextInt(keys.size());
         return keys.get(r);
      } else {
         return "FAIL";
      }
   }

   public static String findMatchingResearch(Player player, Aspect aspect) {
      String randomMatch = null;
      ArrayList<String> keys = new ArrayList<>();

      for (ResearchItem research : LOADER.getValid()) {
         if (!isResearchComplete(player, research.key) && doesPlayerHaveRequisites(player, research.key) && research.tags.getAmount(aspect) > 0) {
            keys.add(research.key);
         }
      }

      if (keys.size() > 0) {
         randomMatch = keys.get(player.level().random.nextInt(keys.size()));
      }

      return randomMatch;
   }

   public static int getResearchSlot(Player player, String key) {
      NonNullList<ItemStack> list = player.getInventory().items;

      for (int i = 0; i < list.size(); i++) {
         ItemStack stack = (ItemStack)list.get(i);
         if (stack.is((Item)ModItems.RESEARCH_NOTES.get()) && getData(stack) != null && getData(stack).key.equals(key)) {
            return i;
         }
      }

      return -1;
   }

   public static boolean consumeInkFromPlayer(Player player, boolean doit) {
      for (ItemStack stack : player.getInventory().items) {
         if (stack.getItem() instanceof IScribeTools && stack.getDamageValue() < stack.getMaxDamage()) {
            if (doit) {
               stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
            }

            return true;
         }
      }

      return false;
   }

   public static boolean consumeInkFromTable(ItemStack stack, boolean doit) {
      if (stack != null && stack.getItem() instanceof IScribeTools && stack.getDamageValue() < stack.getMaxDamage()) {
         if (doit) {
            stack.setDamageValue(stack.getDamageValue() + 1);
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean checkResearchCompletion(ItemStack contents, ResearchNoteData note, Player player) {
      ArrayList<String> checked = new ArrayList<>();
      ArrayList<String> main = new ArrayList<>();
      ArrayList<String> remains = new ArrayList<>();

      for (HexUtils.Hex hex : note.hexes.values()) {
         if (note.hexEntries.get(hex.toString()).type == 1) {
            main.add(hex.toString());
         }
      }

      for (HexUtils.Hex hexx : note.hexes.values()) {
         if (note.hexEntries.get(hexx.toString()).type == 1) {
            main.remove(hexx.toString());
            checkConnections(note, hexx, checked, main, remains, player);
            break;
         }
      }

      if (main.size() != 0) {
         return false;
      } else {
         ArrayList<String> remove = new ArrayList<>();

         for (HexUtils.Hex hexxx : note.hexes.values()) {
            if (note.hexEntries.get(hexxx.toString()).type != 1 && !remains.contains(hexxx.toString())) {
               remove.add(hexxx.toString());
            }
         }

         for (String s : remove) {
            note.hexEntries.remove(s);
            note.hexes.remove(s);
         }

         note.complete = true;
         updateData(contents, note);
         return true;
      }
   }

   private static void checkConnections(
      ResearchNoteData note, HexUtils.Hex hex, ArrayList<String> checked, ArrayList<String> main, ArrayList<String> remains, Player player
   ) {
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      checked.add(hex.toString());

      for (int a = 0; a < 6; a++) {
         HexUtils.Hex target = hex.getNeighbour(a);
         if (!checked.contains(target.toString()) && note.hexEntries.containsKey(target.toString()) && note.hexEntries.get(target.toString()).type >= 1) {
            Aspect aspect1 = note.hexEntries.get(hex.toString()).aspect;
            Aspect aspect2 = note.hexEntries.get(target.toString()).aspect;
            if (knowledge.hasDiscoveredAspect(aspect1)
               && knowledge.hasDiscoveredAspect(aspect2)
               && (
                  !aspect1.isPrimal() && (aspect1.getComponents()[0] == aspect2 || aspect1.getComponents()[1] == aspect2)
                     || !aspect2.isPrimal() && (aspect2.getComponents()[0] == aspect1 || aspect2.getComponents()[1] == aspect1)
               )) {
               remains.add(target.toString());
               if (note.hexEntries.get(target.toString()).type == 1) {
                  main.remove(target.toString());
               }

               checkConnections(note, target, checked, main, remains, player);
            }
         }
      }
   }

   @NotNull
   public static ItemStack createNote(@NotNull ItemStack stack, String key, Level world) {
      ResearchItem rr = ResearchApi.getResearch(key);
      if (rr == null) {
         return ItemStack.EMPTY;
      } else {
         Aspect primaryaspect = rr.getResearchPrimaryTag();
         if (primaryaspect == null) {
            return ItemStack.EMPTY;
         } else {
            CompoundTag compound = ItemStackData.getOrCreateTag(stack);
            compound.putString("key", key);
            compound.putInt("color", primaryaspect.getColor());
            compound.putBoolean("complete", false);
            compound.putInt("copies", 0);
            int radius = 1 + Math.min(3, rr.getComplexity());
            HashMap<String, HexUtils.Hex> hexLocs = HexUtils.generateHexes(radius);
            ArrayList<HexUtils.Hex> outerRing = HexUtils.distributeRingRandomly(radius, rr.tags.size(), world.random);
            HashMap<String, ResearchManager.HexEntry> hexEntries = new HashMap<>();
            HashMap<String, HexUtils.Hex> hexes = new HashMap<>();

            for (HexUtils.Hex hex : hexLocs.values()) {
               hexes.put(hex.toString(), hex);
               hexEntries.put(hex.toString(), new ResearchManager.HexEntry(null, 0));
            }

            int count = 0;

            for (HexUtils.Hex hex : outerRing) {
               hexes.put(hex.toString(), hex);
               hexEntries.put(hex.toString(), new ResearchManager.HexEntry(rr.tags.getAspects()[count], 1));
               count++;
            }

            if (rr.getComplexity() > 1) {
               int blanks = rr.getComplexity() * 2;
               HexUtils.Hex[] temp = hexes.values().toArray(new HexUtils.Hex[0]);

               while (blanks > 0) {
                  int indx = world.random.nextInt(temp.length);
                  if (hexEntries.get(temp[indx].toString()) != null && hexEntries.get(temp[indx].toString()).type == 0) {
                     boolean gtg = true;

                     for (int n = 0; n < 6; n++) {
                        HexUtils.Hex neighbour = temp[indx].getNeighbour(n);
                        if (hexes.containsKey(neighbour.toString()) && hexEntries.get(neighbour.toString()).type == 1) {
                           int cc = 0;

                           for (int q = 0; q < 6; q++) {
                              if (hexes.containsKey(hexes.get(neighbour.toString()).getNeighbour(q).toString())) {
                                 cc++;
                              }

                              if (cc >= 2) {
                                 break;
                              }
                           }

                           if (cc < 2) {
                              gtg = false;
                              break;
                           }
                        }
                     }

                     if (gtg) {
                        hexes.remove(temp[indx].toString());
                        hexEntries.remove(temp[indx].toString());
                        temp = hexes.values().toArray(new HexUtils.Hex[0]);
                        blanks--;
                     }
                  }
               }
            }

            ListTag gridtag = new ListTag();

            for (HexUtils.Hex hex : hexes.values()) {
               CompoundTag gt = new CompoundTag();
               gt.putByte("hexq", (byte)hex.q);
               gt.putByte("hexr", (byte)hex.r);
               gt.putByte("type", (byte)hexEntries.get(hex.toString()).type);
               if (hexEntries.get(hex.toString()).aspect != null) {
                  gt.putString("aspect", hexEntries.get(hex.toString()).aspect.getTag());
               }

               gridtag.add(gt);
            }

            compound.put("hexgrid", gridtag);
            ItemStackData.setTag(stack, compound);
            return stack;
         }
      }
   }

   public static ResearchNoteData getData(ItemStack stack) {
      if (stack == null) {
         return null;
      } else {
         CompoundTag compound = ItemStackData.getTag(stack);
         if (compound == null) {
            return null;
         } else {
            ResearchNoteData data = new ResearchNoteData();
            data.key = compound.getString("key");
            data.color = compound.getInt("color");
            data.complete = compound.getBoolean("complete");
            data.copies = compound.getInt("copies");
            ListTag grid = compound.getList("hexgrid", 10);
            data.hexEntries = new HashMap<>();

            for (int x = 0; x < grid.size(); x++) {
               CompoundTag nbt = grid.getCompound(x);
               int q = nbt.getByte("hexq");
               int r = nbt.getByte("hexr");
               int type = nbt.getByte("type");
               String tag = nbt.getString("aspect");
               Aspect aspect = Aspect.getAspect(tag);
               HexUtils.Hex hex = new HexUtils.Hex(q, r);
               data.hexEntries.put(hex.toString(), new ResearchManager.HexEntry(aspect, type));
               data.hexes.put(hex.toString(), hex);
            }

            return data;
         }
      }
   }

   public static void updateData(ItemStack stack, ResearchNoteData data) {
      CompoundTag compound = ItemStackData.getOrCreateTag(stack);
      compound.putString("key", data.key);
      compound.putInt("color", data.color);
      compound.putBoolean("complete", data.complete);
      compound.putInt("copies", data.copies);
      ListTag gridtag = new ListTag();

      for (HexUtils.Hex hex : data.hexes.values()) {
         CompoundTag gt = new CompoundTag();
         gt.putByte("hexq", (byte)hex.q);
         gt.putByte("hexr", (byte)hex.r);
         gt.putByte("type", (byte)data.hexEntries.get(hex.toString()).type);
         if (data.hexEntries.get(hex.toString()).aspect != null) {
            gt.putString("aspect", data.hexEntries.get(hex.toString()).aspect.getTag());
         }

         gridtag.add(gt);
      }

      compound.put("hexgrid", gridtag);
      ItemStackData.setTag(stack, compound);
   }

   public static boolean isResearchComplete(@NotNull Player player, String key) {
      if (!key.startsWith("@") && ResearchApi.getResearch(key) == null) {
         return false;
      } else {
         HashSet<String> completed = getResearchForPlayer(player);
         return completed.size() > 0 && completed.contains(key);
      }
   }

   @NotNull
   public static HashSet<String> getResearchForPlayer(@NotNull Player player) {
      return PlayerKnowledge.of(player).getResearchCompleted();
   }

   public static boolean doesPlayerHaveRequisites(Player player, String key) {
      ResearchItem research = ResearchApi.getResearch(key);
      if (research == null) {
         return false;
      } else {
         Set<String> completed = getResearchForPlayer(player);
         return research.doesPlayerHaveRequisites(player, completed);
      }
   }

   public static Aspect getCombinationResult(Aspect aspect1, Aspect aspect2) {
      for (Aspect aspect : Aspect.aspects.values()) {
         Aspect[] components = aspect.getComponents();
         if (components != null && (components[0] == aspect1 && components[1] == aspect2 || components[0] == aspect2 && components[1] == aspect1)) {
            return aspect;
         }
      }

      return null;
   }

   public static AspectList reduceToPrimals(AspectList al) {
      return reduceToPrimals(al, false);
   }

   public static AspectList reduceToPrimals(AspectList al, boolean merge) {
      AspectList out = new AspectList();

      for (Aspect aspect : al.getAspects()) {
         if (aspect != null) {
            if (aspect.isPrimal()) {
               if (merge) {
                  out.merge(aspect, al.getAmount(aspect));
               } else {
                  out.add(aspect, al.getAmount(aspect));
               }
            } else {
               AspectList send = new AspectList();
               send.add(aspect.getComponents()[0], al.getAmount(aspect));
               send.add(aspect.getComponents()[1], al.getAmount(aspect));
               send = reduceToPrimals(send, merge);

               for (Aspect a : send.getAspects()) {
                  if (merge) {
                     out.merge(a, send.getAmount(a));
                  } else {
                     out.add(a, send.getAmount(a));
                  }
               }
            }
         }
      }

      return out;
   }

   public void completeResearch(Player player, String key) {
      HashSet<String> completed = getResearchForPlayer(player);
      if (completed.add(key) && !player.level().isClientSide) {
         int warp = ThaumcraftApi.getWarp(key);
         if (warp > 0 && !Config.wuss) {
            if (warp > 1) {
               int w2 = warp / 2;
               if (warp - w2 > 0) {
                  Thaumcraft.addWarpToPlayer(player, warp - w2, false);
               }

               if (w2 > 0) {
                  Thaumcraft.addStickyWarpToPlayer(player, w2);
               }
            } else {
               Thaumcraft.addWarpToPlayer(player, warp, false);
            }
         }
      }
   }

   public void completeAspect(Player player, Aspect aspect, short amount) {
      if (aspect != null) {
         PlayerKnowledge knowledge = PlayerKnowledge.of(player);
         knowledge.addDiscoveredAspect(aspect);
         knowledge.setAspectPool(aspect, amount);
      }
   }

   public void completeScannedObject(Player player, String key) {
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      HashSet<String> completed = knowledge.getObjectsScanned();
      if (completed.add(key)) {
         String t = key.replaceFirst("#", "@");
         if (key.startsWith("#")) {
            completed.remove(t);
         }

         if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendTo(new PacketSyncScannedItems(key), serverPlayer);
         }
      }
   }

   public void completeScannedEntity(Player player, String key) {
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      HashSet<String> completed = knowledge.getEntitiesScanned();
      if (completed.add(key)) {
         String t = key.replaceFirst("#", "@");
         if (key.startsWith("#")) {
            completed.remove(t);
         }

         if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendTo(new PacketSyncScannedEntities(key), serverPlayer);
         }
      }
   }

   public void completeScannedPhenomena(Player player, String key) {
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      HashSet<String> completed = knowledge.getPhenomenaScanned();
      if (completed.add(key)) {
         String t = key.replaceFirst("#", "@");
         if (key.startsWith("#")) {
            completed.remove(t);
         }

         if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendTo(new PacketSyncScannedPhenomena(key), serverPlayer);
         }
      }
   }

   public static void scheduleSave(Player player) {
   }

   public static class HexEntry {
      public Aspect aspect;
      public int type;

      public HexEntry(Aspect aspect, int type) {
         this.aspect = aspect;
         this.type = type;
      }
   }
}
