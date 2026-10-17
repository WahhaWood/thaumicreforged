package thaumcraft.gametest;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.event.common.EventHandlerRunic;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.util.ItemStackData;

/** Fortress helm masks and the runic shield, against TC4 EventHandlerRunic. */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class FortressRunicGameTest {

   /** Sipping Fiend: hitting for 12+ damage always heals the wearer by 1. */
   @GameTest(template = "empty_9x5x9")
   public static void sippingFiendHealsWearer(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      player.setItemSlot(EquipmentSlot.HEAD, maskedHelm(2));
      player.setHealth(10.0F);
      Cow cow = helper.spawn(EntityType.COW, new BlockPos(4, 1, 4));

      cow.hurt(player.damageSources().playerAttack(player), 24.0F);

      helper.assertTrue(player.getHealth() == 11.0F, "mask 2 must heal the attacker by 1, health " + player.getHealth());
      helper.succeed();
   }

   /** Angry Ghost: a melee hit of 10+ damage on the wearer always withers the attacker. */
   @GameTest(template = "empty_9x5x9")
   public static void angryGhostWithersAttacker(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      player.setItemSlot(EquipmentSlot.HEAD, maskedHelm(1));
      Cow cow = helper.spawn(EntityType.COW, new BlockPos(4, 1, 4));

      player.hurt(player.damageSources().mobAttack(cow), 12.0F);

      helper.assertTrue(cow.hasEffect(MobEffects.WITHER), "mask 1 must wither the attacker");
      helper.succeed();
   }

   /** One point per 40 ticks, each paid with Aer and Terra from a wand in the inventory. */
   @GameTest(template = "empty_1x1x1", timeoutTicks = 300)
   public static void shieldRechargesForAerAndTerra(GameTestHelper helper) {
      FakePlayer player = fakePlayer(helper);
      player.setItemSlot(EquipmentSlot.HEAD, hardenedHelm(3));
      ItemStack wand = ItemWandCasting.createWand(ModItems.WOOD_WAND_ROD.get(), ModItems.IRON_WAND_CAP.get());
      ItemWandCasting wandItem = (ItemWandCasting)wand.getItem();
      wandItem.addRealVis(wand, Aspect.AIR, 1000, true);
      wandItem.addRealVis(wand, Aspect.EARTH, 1000, true);
      player.getInventory().setItem(1, wand);
      EventHandlerRunic shield = Thaumcraft.instance.runicEventHandler;

      helper.onEachTick(() -> shield.serverTick(player));
      helper.succeedWhen(() -> {
         helper.assertTrue(shield.getRunicCharge(player) == 3, "shield must fill to 3, charge " + shield.getRunicCharge(player));
         ItemStack paid = player.getInventory().getItem(1);
         helper.assertTrue(wandItem.getVis(paid, Aspect.AIR) < 1000 && wandItem.getVis(paid, Aspect.EARTH) < 1000, "recharge must cost Aer and Terra");
      });
   }

   @GameTest(template = "empty_1x1x1", timeoutTicks = 200)
   public static void shieldWithoutVisStaysEmpty(GameTestHelper helper) {
      FakePlayer player = fakePlayer(helper);
      player.setItemSlot(EquipmentSlot.HEAD, hardenedHelm(3));
      EventHandlerRunic shield = Thaumcraft.instance.runicEventHandler;

      helper.onEachTick(() -> shield.serverTick(player));
      helper.runAfterDelay(100, () -> {
         helper.assertTrue(shield.getRunicMax(player) == 3, "max charge comes from the hardened helm");
         helper.assertTrue(shield.getRunicCharge(player) == 0, "no vis, no recharge");
         helper.succeed();
      });
   }

   @GameTest(template = "empty_1x1x1")
   public static void drowningGoesThroughShield(GameTestHelper helper) {
      FakePlayer player = fakePlayer(helper);
      EventHandlerRunic shield = Thaumcraft.instance.runicEventHandler;
      shield.runicInfo.put(player.getUUID(), new Integer[]{10, 0, 0, 0, 0});
      shield.runicCharge.put(player.getUUID(), 10);

      helper.assertTrue(shield.absorbDamage(player, player.damageSources().drown(), 4.0F) == 4.0F, "drowning must ignore the shield");
      helper.assertTrue(shield.absorbDamage(player, player.damageSources().generic(), 4.0F) == 0.0F, "generic damage is absorbed");
      helper.assertTrue(shield.getRunicCharge(player) == 6, "absorbed damage spends charge");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void emergencyAmuletRefillsBrokenShield(GameTestHelper helper) {
      FakePlayer player = fakePlayer(helper);
      EventHandlerRunic shield = Thaumcraft.instance.runicEventHandler;
      shield.runicInfo.put(player.getUUID(), new Integer[]{10, 0, 0, 0, 1});
      shield.runicCharge.put(player.getUUID(), 2);

      float left = shield.absorbDamage(player, player.damageSources().generic(), 5.0F);

      helper.assertTrue(left == 3.0F, "a broken shield passes the rest of the damage, got " + left);
      helper.assertTrue(shield.getRunicCharge(player) == 8, "the emergency amulet refills 8 per amulet");
      helper.succeed();
   }

   private static FakePlayer fakePlayer(GameTestHelper helper) {
      FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "tc_shield"));
      player.setPos(helper.absoluteVec(Vec3.ZERO));
      return player;
   }

   private static ItemStack hardenedHelm(int hardening) {
      ItemStack helm = new ItemStack(ModItems.THAUMIUM_FORTRESS_HELMET.get());
      ItemStackData.putByte(helm, "RS.HARDEN", (byte)hardening);
      return helm;
   }

   private static ItemStack maskedHelm(int mask) {
      ItemStack helm = new ItemStack(ModItems.THAUMIUM_FORTRESS_HELMET.get());
      ItemStackData.putInt(helm, "mask", mask);
      return helm;
   }
}
