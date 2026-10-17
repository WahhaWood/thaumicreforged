package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.block.infusion_claw.InfusionClawBlockEntity;
import thaumcraft.block.infusion_claw.InfusionClawMenu;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.entities.smart.golem.EnumGolemType;
import thaumcraft.entities.smart.golem.GolemMenu;
import thaumcraft.entities.smart.pech.EntityPech;
import thaumcraft.entities.smart.pech.PechMenu;
import thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk;
import thaumcraft.entities.smart.travelling_trunk.inventory.MenuTravelingTrunk;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.item.golem_core.GolemCore;

/**
 * Screens send their buttons with handleInventoryButtonClick; only the menu's clickMenuButton acts on them on the
 * server. These menus had none, so the buttons did nothing (TC4 Container*.enchantItem).
 */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class MenuButtonGameTest {
   /** TC4 ContainerPech.generateContents: the trade button spends the offered item on goods. */
   @GameTest(template = "empty_9x5x9")
   public static void pechTradeButtonTrades(GameTestHelper helper) {
      EntityPech pech = ModEntities.PECH.get().create(helper.getLevel());
      pech.moveTo(helper.absoluteVec(new BlockPos(4, 1, 4).getCenter()));
      pech.setTamed(true);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      PechMenu menu = new PechMenu(0, player.getInventory(), pech);
      menu.getMerchantInventory().setItem(0, new ItemStack(Items.ENDER_PEARL, 2));
      helper.assertTrue(menu.isReadyToTrade(), "an ender pearl must be a valued offer");

      helper.assertTrue(menu.clickMenuButton(player, 0), "the trade button must be handled");
      helper.assertTrue(menu.getMerchantInventory().getItem(0).getCount() == 1, "one offered pearl must be spent");
      boolean goods = false;
      for (int i = 1; i <= 4; i++) {
         goods |= !menu.getMerchantInventory().getItem(i).isEmpty();
      }

      helper.assertTrue(goods, "the pech must give goods for the pearl");
      helper.succeed();
   }

   /** TC4 ContainerGolem.enchantItem: marker colour buttons step the colour, 50+ flip toggles, 66/67 page. */
   @GameTest(template = "empty_9x5x9")
   public static void golemButtonsSetColoursAndToggles(GameTestHelper helper) {
      EntityGolemBase golem = ModEntities.GOLEM.get().create(helper.getLevel());
      golem.setGolemType(EnumGolemType.THAUMIUM);
      golem.setCore(GolemCore.EMPTY);
      golem.setupGolemInventory();
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      GolemMenu menu = new GolemMenu(0, player.getInventory(), golem);
      int slots = menu.getSlotCount();
      helper.assertTrue(slots > 0, "an empty core golem must have an inventory");

      menu.clickMenuButton(player, 0);
      helper.assertTrue(golem.getColors(0) == 15, "minus on 'any colour' must wrap to 15, got " + golem.getColors(0));
      menu.clickMenuButton(player, slots);
      helper.assertTrue(golem.getColors(0) == -1, "plus on 15 must wrap to 'any colour', got " + golem.getColors(0));
      boolean before = golem.getToggles()[1];
      menu.clickMenuButton(player, 51);
      helper.assertTrue(golem.getToggles()[1] != before, "button 51 must flip toggle 1");
      if (menu.maxScroll > 0) {
         menu.clickMenuButton(player, 67);
         helper.assertTrue(menu.currentScroll == 1, "button 67 must page the slots forward");
      }

      helper.succeed();
   }

   /** TC4 ContainerTravelingTrunk.enchantItem: button 1 switches stay/follow. */
   @GameTest(template = "empty_9x5x9")
   public static void trunkStayButtonToggles(GameTestHelper helper) {
      EntityTravelingTrunk trunk = ModEntities.TRAVELING_TRUNK.get().create(helper.getLevel());
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      MenuTravelingTrunk menu = new MenuTravelingTrunk(0, player.getInventory(), trunk);
      boolean before = trunk.getStay();
      helper.assertTrue(menu.clickMenuButton(player, 1), "the stay button must be handled");
      helper.assertTrue(trunk.getStay() != before, "the stay button must switch stay/follow");
      helper.succeed();
   }

   /** The infusion claw screen sends the wanted lock state. */
   @GameTest(template = "empty_9x5x9")
   public static void infusionClawLockButton(GameTestHelper helper) {
      BlockPos pos = new BlockPos(4, 1, 4);
      helper.setBlock(pos, ModBlocks.INFUSION_CLAW.get());
      InfusionClawBlockEntity claw = (InfusionClawBlockEntity)helper.getBlockEntity(pos);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      InfusionClawMenu menu = new InfusionClawMenu(0, player.getInventory(), claw);
      menu.clickMenuButton(player, 1);
      helper.assertTrue(claw.isLocked(), "button 1 must lock the claw");
      menu.clickMenuButton(player, 0);
      helper.assertFalse(claw.isLocked(), "button 0 must unlock the claw");
      helper.succeed();
   }
}
