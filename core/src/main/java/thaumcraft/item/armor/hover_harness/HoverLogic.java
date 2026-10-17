package thaumcraft.item.armor.hover_harness;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.block.jar.standart.JarBlockItem;
import thaumcraft.init.registry.ModEnchantment;
import thaumcraft.item.curios.ItemGirdleHover;
import thaumcraft.util.EntityUtils;
import thaumcraft.util.ItemStackData;
import top.theillusivec4.curios.api.CuriosApi;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class HoverLogic {
   /** TC4 Hover.EFFICIENCY: one Potentia per 360 ticks of hovering; the hover girdle burns it every 288 for +0.21 speed. */
   private static final int EFFICIENCY_THRESHOLD = 360;
   private static final float GIRDLE_EFFICIENCY = 0.8F;
   private static final long SOUND_INTERVAL = 1200L;
   private static final String NBT_HOVER_ACTIVE = "hover_active";
   private static final String NBT_JAR = "jar";
   /** Hover ticks since the last Potentia was burnt, per player; kept off the item so it does not resync every tick. */
   private static final Map<UUID, Integer> FUEL_TICKS = new HashMap<>();
   private static long lastHoverSound;

   /** Worn-harness tick (port of Hover.handleHoverArmor, TC4): flight while switched on and fuelled. */
   public static void tickHover(Player player, ItemStack armor) {
      if (player.isCreative() || player.isSpectator()) {
         return;
      }

      boolean active = isActive(armor);
      HoverLogic.FuelInfo fuelInfo = checkFuel(armor);
      if (!player.level().isClientSide) {
         AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
         if (flight == null) {
            return;
         }

         if (active && fuelInfo.hasFuel() && !player.isSwimming() && expendFuel(player, armor, fuelInfo)) {
            if (!flight.hasModifier(EntityUtils.HOVER_FLIGHT.id())) {
               flight.addOrUpdateTransientModifier(EntityUtils.HOVER_FLIGHT);
            }

            if (!player.onGround() && !player.getAbilities().flying) {
               player.getAbilities().flying = true;
               player.onUpdateAbilities();
            }

            player.fallDistance = 0.0F;
         } else {
            // Out of Potentia the harness switches itself off, as TC4 toggles it.
            if (active && !checkFuel(armor).hasFuel()) {
               setActive(armor, false);
            }

            stopHover(player);
         }
      } else if (active && fuelInfo.hasFuel() && player.getAbilities().flying) {
         handleClientMovement(player, armor);
      }
   }

   /** Takes away the harness flight; flight from other sources (creative, ichor wings) is left alone. */
   public static void stopHover(Player player) {
      AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
      if (flight != null && flight.removeModifier(EntityUtils.HOVER_FLIGHT.id()) && flight.getValue() <= 0.0
         && !player.isCreative() && !player.isSpectator() && player.getAbilities().flying) {
         player.getAbilities().flying = false;
         player.onUpdateAbilities();
      }
   }

   /** Port of Hover.expendCharge: false once the last Potentia is burnt. */
   private static boolean expendFuel(Player player, ItemStack armor, HoverLogic.FuelInfo fuelInfo) {
      int ticks = FUEL_TICKS.merge(player.getUUID(), 1, Integer::sum);
      float limit = EFFICIENCY_THRESHOLD * (hasHoverGirdle(player) ? GIRDLE_EFFICIENCY : 1.0F);
      if (ticks < limit) {
         return true;
      }

      FUEL_TICKS.put(player.getUUID(), 0);
      consumeAspect(armor, fuelInfo.jar(), Aspect.ENERGY, 1);
      return fuelInfo.fuelAmount() > 1;
   }

   private static boolean hasHoverGirdle(Player player) {
      return CuriosApi.getCuriosInventory(player)
         .map(handler -> handler.isEquipped(stack -> stack.getItem() instanceof ItemGirdleHover))
         .orElse(false);
   }

   public static HoverLogic.FuelInfo checkFuel(ItemStack armor) {
      CompoundTag tag = ItemStackData.getTag(armor);
      if (tag != null && tag.contains("jar")) {
         ItemStack jarStack = ItemStackData.loadStack(tag.getCompound("jar"));
         if (!jarStack.isEmpty() && jarStack.getItem() instanceof JarBlockItem jarItem) {
            AspectList aspects = jarItem.getAspects(jarStack);
            if (aspects != null && aspects.getAmount(Aspect.ENERGY) > 0) {
               return new HoverLogic.FuelInfo(jarStack, aspects.getAmount(Aspect.ENERGY), true);
            }
         }

         return HoverLogic.FuelInfo.EMPTY;
      } else {
         return HoverLogic.FuelInfo.EMPTY;
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static void handleClientMovement(Player player, ItemStack armor) {
      if (player instanceof LocalPlayer localPlayer) {
         player.fallDistance = 0.0F;
         long currentTime = System.currentTimeMillis();
         if (currentTime > lastHoverSound + SOUND_INTERVAL) {
            player.level()
               .playSound(
                  player,
                  player.getX(),
                  player.getY(),
                  player.getZ(),
                  SoundEvents.BEACON_AMBIENT,
                  SoundSource.PLAYERS,
                  0.05F,
                  1.0F + player.level().random.nextFloat() * 0.05F
               );
            lastHoverSound = currentTime;
         }

         int hasteLevel = player.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .get(ModEnchantment.HASTE.getKey())
            .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, armor))
            .orElse(0);
         float mod = 0.7F + 0.075F * hasteLevel;
         if (hasHoverGirdle(player)) {
            mod += 0.21F;
         }

         mod = Math.min(mod, 1.0F);
         Vec3 motion = player.getDeltaMovement();
         double y = motion.y * mod;
         if (!localPlayer.input.jumping && !localPlayer.input.shiftKeyDown) {
            y = 0.0;
         }

         player.setDeltaMovement(motion.x * mod, y, motion.z * mod);
      }
   }

   private static void consumeAspect(ItemStack armor, ItemStack jar, Aspect aspect, int amount) {
      if (jar.getItem() instanceof JarBlockItem jarItem) {
         AspectList current = jarItem.getAspects(jar);
         current.remove(aspect, amount);
         jarItem.setAspects(jar, current);
         CompoundTag jarTag = ItemStackData.saveStack(jar);
         CompoundTag tag = ItemStackData.getOrCreateTag(armor);
         tag.put("jar", jarTag);
         ItemStackData.setTag(armor, tag);
      }
   }

   public static boolean toggleHover(ItemStack stack) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      boolean current = tag.getBoolean("hover_active");
      tag.putBoolean("hover_active", !current);
      ItemStackData.setTag(stack, tag);
      return !current;
   }

   public static void setActive(ItemStack stack, boolean active) {
      ItemStackData.putBoolean(stack, "hover_active", active);
   }

   public static boolean isActive(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag != null && tag.getBoolean("hover_active");
   }

   public record FuelInfo(ItemStack jar, int fuelAmount, boolean hasFuel) {
      public static final HoverLogic.FuelInfo EMPTY = new HoverLogic.FuelInfo(ItemStack.EMPTY, 0, false);
   }
}
