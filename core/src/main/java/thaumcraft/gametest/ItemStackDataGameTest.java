package thaumcraft.gametest;

import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.util.ItemStackData;

/**
 * Item data layer: stored stacks keep every component, and edits through ItemStackData never leak
 * into copies (including the slot snapshot the server syncs against).
 */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class ItemStackDataGameTest {

   @GameTest(template = "empty_1x1x1")
   public static void storedStackKeepsComponents(GameTestHelper helper) {
      HolderLookup.Provider registries = helper.getLevel().registryAccess();
      ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
      sword.enchant(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), 3);
      sword.setDamageValue(100);
      sword.set(DataComponents.CUSTOM_NAME, Component.literal("Blade"));
      ItemStackData.putInt(sword, "thaum", 7);

      ItemStack loaded = ItemStackData.loadStack(ItemStackData.saveStack(sword));

      helper.assertTrue(ItemStack.matches(sword, loaded), "stored sword must come back identical, got " + loaded.getComponentsPatch());
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void storedShulkerKeepsContents(GameTestHelper helper) {
      ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
      shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND, 12))));

      ItemStack loaded = ItemStackData.loadStack(ItemStackData.saveStack(shulker));

      helper.assertTrue(ItemStack.matches(shulker, loaded), "shulker contents must survive storage");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void legacyStoredStackStillLoads(GameTestHelper helper) {
      CompoundTag custom = new CompoundTag();
      custom.putInt("thaum", 3);
      CompoundTag legacy = new CompoundTag();
      legacy.putString("id", "minecraft:stick");
      legacy.put("Count", ByteTag.valueOf((byte)5));
      legacy.put("tag", custom);

      ItemStack loaded = ItemStackData.loadStack(legacy);

      helper.assertTrue(loaded.is(Items.STICK) && loaded.getCount() == 5, "legacy id/Count must load, got " + loaded);
      helper.assertTrue(ItemStackData.getInt(loaded, "thaum") == 3, "legacy tag must become CUSTOM_DATA");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void editingCopyLeavesOriginal(GameTestHelper helper) {
      ItemStack original = new ItemStack(Items.STICK);
      ItemStackData.putInt(original, "x", 1);
      ItemStack copy = original.copy();

      ItemStackData.getOrCreateTag(copy).putInt("x", 2);

      helper.assertTrue(ItemStackData.getInt(original, "x") == 1, "edit of a copy leaked into the original");
      helper.assertTrue(ItemStackData.getInt(copy, "x") == 2, "edit of the copy is lost");
      helper.succeed();
   }

   /** The server syncs a slot only when the live stack stops matching its snapshot copy. */
   @GameTest(template = "empty_1x1x1")
   public static void slotSnapshotSeesEveryEdit(GameTestHelper helper) {
      ItemStack wand = ItemWandCasting.createWand(ModItems.WOOD_WAND_ROD.get(), ModItems.IRON_WAND_CAP.get());
      ItemWandCasting item = (ItemWandCasting)wand.getItem();

      for (int i = 1; i <= 3; i++) {
         ItemStack snapshot = wand.copy();
         item.storeVis(wand, Aspect.AIR, i * 100);
         helper.assertFalse(ItemStack.matches(snapshot, wand), "vis edit #" + i + " is invisible to slot sync");
      }

      ItemStack snapshot = wand.copy();
      ItemStackData.getOrCreateTag(wand).putInt("areax", 2);
      helper.assertFalse(ItemStack.matches(snapshot, wand), "in-place tag edit is invisible to slot sync");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void wandFocusKeepsComponents(GameTestHelper helper) {
      ItemStack wand = ItemWandCasting.createWand(ModItems.WOOD_WAND_ROD.get(), ModItems.IRON_WAND_CAP.get());
      ItemWandCasting item = (ItemWandCasting)wand.getItem();
      ItemStack focus = new ItemStack(ModItems.FOCUS_FIRE.get());
      focus.set(DataComponents.CUSTOM_NAME, Component.literal("Spark"));

      item.setFocus(wand, focus);

      helper.assertTrue(item.getFocus(wand) == ModItems.FOCUS_FIRE.get(), "focus type lost");
      helper.assertTrue(ItemStack.matches(focus, item.getFocusItem(wand)), "focus stack must come back identical");
      helper.succeed();
   }
}
