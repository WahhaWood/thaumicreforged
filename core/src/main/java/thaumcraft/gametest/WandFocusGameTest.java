package thaumcraft.gametest;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.block.warp_gate.WarpGateBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.armor.hover_harness.HoverLogic;
import thaumcraft.item.sky_pearl.ItemSkyPearl;
import thaumcraft.item.wands.ItemFocusPouch;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.WandManager;
import thaumcraft.network.misc.PacketFlyToServer;
import thaumcraft.network.misc.PacketFocusChangeToServer;
import thaumcraft.network.misc.PacketItemKeyToServer;
import thaumcraft.network.misc.PacketWarpGateTeleport;

/**
 * Client-to-server packets go through their real server handlers. A FakePlayer is a ServerPlayer that is
 * not added to the player list, so it avoids the datapack sync that crashes mock server players here.
 */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class WandFocusGameTest {

   @GameTest(template = "empty_1x1x1")
   public static void focusFromInventoryGoesIntoWand(GameTestHelper helper) {
      FakePlayer player = player(helper);
      ItemStack wand = wand();
      player.setItemInHand(InteractionHand.MAIN_HAND, wand);
      ItemStack fire = new ItemStack(ModItems.FOCUS_FIRE.get());
      player.getInventory().setItem(5, fire.copy());

      new PacketFocusChangeToServer(key(fire)).executeServer(player);

      helper.assertTrue(wandItem().getFocus(player.getMainHandItem()) == ModItems.FOCUS_FIRE.get(), "wand must hold the fire focus");
      helper.assertTrue(player.getInventory().getItem(5).isEmpty(), "focus must leave the inventory slot");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void swapReturnsOldFocusToInventory(GameTestHelper helper) {
      FakePlayer player = player(helper);
      ItemStack wand = wand();
      wandItem().setFocus(wand, new ItemStack(ModItems.FOCUS_FROST.get()));
      player.setItemInHand(InteractionHand.MAIN_HAND, wand);
      ItemStack fire = new ItemStack(ModItems.FOCUS_FIRE.get());
      player.getInventory().setItem(9, fire.copy());

      new PacketFocusChangeToServer(key(fire)).executeServer(player);

      helper.assertTrue(wandItem().getFocus(player.getMainHandItem()) == ModItems.FOCUS_FIRE.get(), "wand must switch to fire");
      helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.FOCUS_FROST.get())), "old frost focus must return to inventory");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void removeStoresFocusInPouch(GameTestHelper helper) {
      FakePlayer player = player(helper);
      ItemStack wand = wand();
      wandItem().setFocus(wand, new ItemStack(ModItems.FOCUS_FIRE.get()));
      player.setItemInHand(InteractionHand.MAIN_HAND, wand);
      player.getInventory().setItem(3, new ItemStack(ModItems.FOCUS_POUCH.get()));

      new PacketFocusChangeToServer("REMOVE").executeServer(player);

      helper.assertTrue(wandItem().getFocus(player.getMainHandItem()) == null, "REMOVE must empty the wand");
      ItemStack pouch = player.getInventory().getItem(3);
      NonNullList<ItemStack> contents = ((ItemFocusPouch)pouch.getItem()).getInventory(pouch);
      helper.assertTrue(contents.get(0).is(ModItems.FOCUS_FIRE.get()), "removed focus must go into the pouch first");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void focusFromPouchGoesIntoWand(GameTestHelper helper) {
      FakePlayer player = player(helper);
      player.setItemInHand(InteractionHand.MAIN_HAND, wand());
      ItemStack pouch = new ItemStack(ModItems.FOCUS_POUCH.get());
      ItemFocusPouch pouchItem = (ItemFocusPouch)pouch.getItem();
      NonNullList<ItemStack> contents = pouchItem.getInventory(pouch);
      ItemStack shock = new ItemStack(ModItems.FOCUS_SHOCK.get());
      contents.set(2, shock.copy());
      pouchItem.setInventory(pouch, contents);
      // slot 0 is the selected hotbar slot, i.e. the main hand holding the wand
      player.getInventory().setItem(7, pouch);

      new PacketFocusChangeToServer(key(shock)).executeServer(player);

      helper.assertTrue(wandItem().getFocus(player.getMainHandItem()) == ModItems.FOCUS_SHOCK.get(), "wand must take the focus from the pouch");
      ItemStack after = player.getInventory().getItem(7);
      helper.assertTrue(pouchItem.getInventory(after).get(2).isEmpty(), "pouch slot must be emptied");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void miscKeyCyclesArchitectArea(GameTestHelper helper) {
      FakePlayer player = player(helper);
      ItemStack wand = wand();
      ItemStack trade = new ItemStack(ModItems.FOCUS_TRADE.get());
      ((ItemFocusBasic)trade.getItem()).applyUpgrade(trade, FocusUpgradeType.architect, 1);
      wandItem().setFocus(wand, trade);
      WandManager.setAreaX(wand, 0);
      WandManager.setAreaY(wand, 0);
      WandManager.setAreaZ(wand, 0);
      player.setItemInHand(InteractionHand.MAIN_HAND, wand);

      new PacketItemKeyToServer(1).executeServer(player);

      ItemStack held = player.getMainHandItem();
      helper.assertTrue(WandManager.getAreaX(held) == 1 && WandManager.getAreaZ(held) == 1, "G must grow the architect area");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void hoverToggleReachesWornHarness(GameTestHelper helper) {
      FakePlayer player = player(helper);
      ItemStack harness = HoverHarnessGameTest.fuelledHarness(5);
      HoverLogic.setActive(harness, false);
      player.setItemSlot(EquipmentSlot.CHEST, harness);

      new PacketFlyToServer(true).executeServer(player);
      helper.assertTrue(HoverLogic.isActive(player.getItemBySlot(EquipmentSlot.CHEST)), "server must switch the harness on");

      new PacketFlyToServer(false).executeServer(player);
      helper.assertFalse(HoverLogic.isActive(player.getItemBySlot(EquipmentSlot.CHEST)), "server must switch the harness off");
      helper.succeed();
   }

   @GameTest(template = "empty_9x5x9")
   public static void warpGateTeleportsOnlyFromTheGate(GameTestHelper helper) {
      BlockPos from = helper.absolutePos(new BlockPos(1, 1, 1));
      BlockPos to = helper.absolutePos(new BlockPos(7, 1, 7));
      helper.setBlock(new BlockPos(1, 1, 1), ModBlocks.WARP_GATE.get());
      helper.setBlock(new BlockPos(7, 1, 7), ModBlocks.WARP_GATE.get());
      ItemStack pearl = new ItemStack(ModItems.SKY_PEARL.get());
      ItemSkyPearl.setValues(pearl, to, helper.getLevel().dimension().location());
      ((WarpGateBlockEntity)helper.getLevel().getBlockEntity(from)).setItem(0, pearl);

      FakePlayer far = player(helper);
      far.setPos(Vec3.atBottomCenterOf(from).add(4.0, 1.0, 0.0));
      helper.assertFalse(PacketWarpGateTeleport.isStandingOnGate(far, from), "a player away from the gate must be rejected");

      // FakePlayer.connection.teleport is a no-op, so the move itself is checked through teleportPlayer's result.
      FakePlayer onGate = player(helper);
      onGate.setPos(Vec3.atBottomCenterOf(from.above()));
      helper.assertTrue(PacketWarpGateTeleport.isStandingOnGate(onGate, from), "a player on the gate must be accepted");
      helper.assertTrue(((WarpGateBlockEntity)helper.getLevel().getBlockEntity(from)).teleportPlayer(onGate, 0), "the linked gate must accept the teleport");
      helper.succeed();
   }

   private static FakePlayer player(GameTestHelper helper) {
      FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "tc_test"));
      player.setPos(helper.absoluteVec(Vec3.ZERO));
      return player;
   }

   private static ItemStack wand() {
      return ItemWandCasting.createWand(ModItems.WOOD_WAND_ROD.get(), ModItems.IRON_WAND_CAP.get());
   }

   private static ItemWandCasting wandItem() {
      return (ItemWandCasting)ModItems.WAND_CASTING.get();
   }

   private static String key(ItemStack focus) {
      return ((ItemFocusBasic)focus.getItem()).getSortingHelper(focus);
   }
}
