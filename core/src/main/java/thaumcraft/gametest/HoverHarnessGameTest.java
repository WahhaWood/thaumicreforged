package thaumcraft.gametest;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.block.jar.standart.JarBlockItem;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.armor.hover_harness.HoverLogic;
import thaumcraft.util.EntityUtils;
import thaumcraft.util.ItemStackData;

/** Hover harness against TC4 Hover: worn only, burns Potentia, leaves other flight alone. */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class HoverHarnessGameTest {

   @GameTest(template = "empty_1x1x1")
   public static void carriedHarnessDoesNotFly(GameTestHelper helper) {
      FakePlayer player = player(helper);
      ItemStack harness = fuelledHarness(5);
      player.getInventory().setItem(3, harness);

      harness.inventoryTick(helper.getLevel(), player, 3, false);

      helper.assertFalse(flight(player).hasModifier(EntityUtils.HOVER_FLIGHT.id()), "a harness in the inventory must not grant flight");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void wornHarnessBurnsPotentiaAndStops(GameTestHelper helper) {
      FakePlayer player = player(helper);
      ItemStack harness = fuelledHarness(2);
      player.setItemSlot(EquipmentSlot.CHEST, harness);

      for (int i = 0; i < 360; i++) {
         HoverLogic.tickHover(player, harness);
      }

      helper.assertTrue(HoverLogic.checkFuel(harness).fuelAmount() == 1, "one Potentia per 360 ticks, left " + HoverLogic.checkFuel(harness).fuelAmount());
      helper.assertTrue(flight(player).hasModifier(EntityUtils.HOVER_FLIGHT.id()), "a fuelled harness flies");

      for (int i = 0; i < 361; i++) {
         HoverLogic.tickHover(player, harness);
      }

      helper.assertFalse(HoverLogic.isActive(harness), "an empty jar switches the harness off");
      helper.assertFalse(flight(player).hasModifier(EntityUtils.HOVER_FLIGHT.id()), "an empty harness stops flying");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void removingHarnessKeepsWingsFlight(GameTestHelper helper) {
      FakePlayer player = player(helper);
      flight(player).addOrUpdateTransientModifier(EntityUtils.FLIGHT);

      HoverLogic.stopHover(player);

      helper.assertTrue(flight(player).hasModifier(EntityUtils.FLIGHT.id()), "the ichor wings flight must survive");
      helper.succeed();
   }

   private static FakePlayer player(GameTestHelper helper) {
      FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "tc_hover"));
      player.setPos(helper.absoluteVec(Vec3.ZERO));
      return player;
   }

   private static AttributeInstance flight(FakePlayer player) {
      return player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
   }

   static ItemStack fuelledHarness(int potentia) {
      ItemStack jar = new ItemStack(ModItems.JAR.get());
      ((JarBlockItem)jar.getItem()).setAspects(jar, new AspectList().add(Aspect.ENERGY, potentia));
      ItemStack harness = new ItemStack(ModItems.HOVER_HARNESS.get());
      ItemStackData.addTagElement(harness, "jar", ItemStackData.saveStack(jar));
      HoverLogic.setActive(harness, true);
      return harness;
   }
}
