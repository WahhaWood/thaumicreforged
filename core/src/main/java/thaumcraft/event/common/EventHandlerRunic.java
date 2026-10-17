package thaumcraft.event.common;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IWarpingGear;
import thaumcraft.item.armor.thaumium_fortress.ItemFortressArmor;
import thaumcraft.network.PacketHandler;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.wands.WandManager;
import thaumcraft.network.fx.PacketFXShield;
import thaumcraft.network.playerdata.PacketRunicCharge;
import thaumcraft.util.ItemStackData;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EventHandlerRunic {
   public HashMap<UUID, Integer> runicCharge = new HashMap<>();
   private final HashMap<UUID, Long> nextCycle = new HashMap<>();
   private final HashMap<UUID, Integer> lastCharge = new HashMap<>();
   public HashMap<UUID, Integer[]> runicInfo = new HashMap<>();
   private final HashMap<String, Long> upgradeCooldown = new HashMap<>();
   private final HashMap<UUID, Long> rechargeBlockedUntil = new HashMap<>();
   public boolean isDirty = true;

   public int getRunicCharge(Player player) {
      return this.runicCharge.getOrDefault(player.getUUID(), 0);
   }

   public int getRunicMax(Player player) {
      Integer[] info = this.runicInfo.get(player.getUUID());
      return info == null ? 0 : info[0];
   }

   public boolean hasRunicEquipment(Player player) {
      return this.getRunicMax(player) > 0;
   }

   public boolean hasRunicShield(Player player) {
      return this.hasRunicCharge(player);
   }

   public boolean hasRunicCharge(Player player) {
      return this.getRunicCharge(player) > 0 && this.getRunicMax(player) > 0;
   }

   /** TC4 Config.shieldRecharge (2000 ms), shieldWait (80 ticks) and shieldCost (50 centivis of Aer and Terra). */
   private static final int RECHARGE_TICKS = 40;
   private static final int CHARGED_RING_BONUS_TICKS = 10;
   private static final int RECHARGE_WAIT_TICKS = 80;
   private static final int RECHARGE_VIS_COST = 50;

   /**
    * Server tick of the runic shield (port of EventHandlerRunic.livingTick, TC4 4.2.3.5), called every tick.
    * Every 40 ticks (or when gear changed) the maximum and the ring/amulet/girdle upgrades are recounted.
    * One point recharges per interval and costs Aer and Terra vis from the vis amulet or a wand; after the
    * shield breaks it waits {@link #RECHARGE_WAIT_TICKS} first.
    */
   public void serverTick(ServerPlayer player) {
      UUID id = player.getUUID();
      long now = player.level().getGameTime();
      if (this.isDirty || player.tickCount % 40 == 0) {
         this.isDirty = false;
         if (!this.recount(player)) {
            return;
         }
      }

      Integer[] info = this.runicInfo.get(id);
      if (info == null || now < this.rechargeBlockedUntil.getOrDefault(id, 0L)) {
         return;
      }

      int charge = this.runicCharge.getOrDefault(id, 0);
      if (charge > info[0]) {
         charge = info[0];
      } else if (charge < info[0]
         && this.nextCycle.getOrDefault(id, 0L) <= now
         && WandManager.consumeVisFromInventory(player, new AspectList().add(Aspect.AIR, RECHARGE_VIS_COST).add(Aspect.EARTH, RECHARGE_VIS_COST))) {
         int interval = Math.max(CHARGED_RING_BONUS_TICKS, RECHARGE_TICKS - info[1] * CHARGED_RING_BONUS_TICKS);
         this.nextCycle.put(id, now + interval);
         charge++;
      }

      this.runicCharge.put(id, charge);
      if (this.lastCharge.getOrDefault(id, -1) != charge) {
         this.lastCharge.put(id, charge);
         PacketHandler.sendTo(new PacketRunicCharge(player, (short)charge, info[0]), player);
      }
   }

   /** Recounts {max, charged, kinetic, healing, emergency} from armor and Curios; false when nothing is runic. */
   private boolean recount(ServerPlayer player) {
      UUID id = player.getUUID();
      int max = 0;
      int charged = 0;
      int kinetic = 0;
      int healing = 0;
      int emergency = 0;
      for (ItemStack stack : player.getInventory().armor) {
         max += getFinalCharge(stack);
      }

      List<SlotResult> curios = CuriosApi.getCuriosInventory(player)
         .map(handler -> handler.findCurios(stack -> stack.getItem() instanceof IRunicArmor))
         .orElse(List.of());
      for (SlotResult result : curios) {
         ItemStack stack = result.stack();
         max += getFinalCharge(stack);
         if (stack.is(ModItems.RUNIC_RING_CHARGED.get())) {
            charged++;
         } else if (stack.is(ModItems.RUNIC_RING_REGEN.get())) {
            healing++;
         } else if (stack.is(ModItems.RUNIC_AMULET_EMERGENCY.get())) {
            emergency++;
         } else if (stack.is(ModItems.RUNIC_GIRDLE_KINETIC.get())) {
            kinetic++;
         }
      }

      if (max <= 0) {
         boolean had = this.runicInfo.remove(id) != null;
         this.runicCharge.put(id, 0);
         this.lastCharge.put(id, 0);
         if (had) {
            PacketHandler.sendTo(new PacketRunicCharge(player, (short)0, 0), player);
         }

         return false;
      }

      this.runicInfo.put(id, new Integer[]{max, charged, kinetic, healing, emergency});
      return true;
   }

   /**
    * Damage absorption by the runic shield (port of EventHandlerRunic.entityHurt, TC4): one point of charge
    * per point of damage. Drowning, starvation, the void and wither go through, as in TC4. When the shield
    * breaks, the kinetic girdle explodes, the regeneration ring heals and the emergency amulet refills it,
    * each on its own cooldown. Returns the damage left.
    */
   public float absorbDamage(Player player, DamageSource source, float amount) {
      if (player.level().isClientSide || amount <= 0.0F || bypassesShield(source)) {
         return amount;
      }

      UUID id = player.getUUID();
      Integer[] info = this.runicInfo.get(id);
      int charge = this.runicCharge.getOrDefault(id, 0);
      if (info == null || charge <= 0) {
         return amount;
      }

      int target = source.getEntity() != null ? source.getEntity().getId() : -1;
      if (source.is(DamageTypes.FALL)) {
         target = -2;
      } else if (source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.FALLING_ANVIL)) {
         target = -3;
      }

      PacketHandler.sendToAllAround(new PacketFXShield(player.getId(), target), player.level(), player.position(), 64.0);
      if (charge > amount) {
         charge = (int)(charge - amount);
         amount = 0.0F;
      } else {
         amount -= charge;
         charge = 0;
      }

      long now = player.level().getGameTime();
      if (charge <= 0 && info[2] > 0 && this.offCooldown(id, 2, now, 400)) {
         player.level().explode(player, player.getX(), player.getY() + player.getBbHeight() / 2.0F, player.getZ(),
            1.5F + info[2] * 0.5F, Level.ExplosionInteraction.NONE);
      }

      if (charge <= 0 && info[3] > 0 && this.offCooldown(id, 3, now, 400)) {
         player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, info[3]));
         player.level().playSound(null, player.blockPosition(), ModSounds.RUNIC_SHIELD_EFFECT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
      }

      if (charge <= 0 && info[4] > 0 && this.offCooldown(id, 4, now, 1200)) {
         charge = Math.min(info[0], 8 * info[4]);
         this.isDirty = true;
         player.level().playSound(null, player.blockPosition(), ModSounds.RUNIC_SHIELD_CHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
      }

      if (charge <= 0) {
         this.rechargeBlockedUntil.put(id, now + RECHARGE_WAIT_TICKS);
      }

      this.runicCharge.put(id, charge);
      this.lastCharge.put(id, charge);
      if (player instanceof ServerPlayer serverPlayer) {
         PacketHandler.sendTo(new PacketRunicCharge(serverPlayer, (short)charge, info[0]), serverPlayer);
      }

      return amount;
   }

   /** TC4 lets drowning, starvation, the void and wither through the shield; /kill too. */
   public static boolean bypassesShield(DamageSource source) {
      return source.is(DamageTypes.DROWN)
         || source.is(DamageTypes.STARVE)
         || source.is(DamageTypes.FELL_OUT_OF_WORLD)
         || source.is(DamageTypes.WITHER)
         || source.is(DamageTypes.GENERIC_KILL);
   }

   private boolean offCooldown(UUID id, int upgrade, long now, int cooldownTicks) {
      String key = id + ":" + upgrade;
      if (this.upgradeCooldown.getOrDefault(key, Long.MIN_VALUE) > now) {
         return false;
      }

      this.upgradeCooldown.put(key, now + cooldownTicks);
      return true;
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void tooltipEvent(ItemTooltipEvent event) {
      int charge = getFinalCharge(event.getItemStack());
      if (charge > 0) {
         event.getToolTip()
            .add(Component.literal(I18n.get("item.runic.charge", new Object[0]) + " +" + charge).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
      }

      int warp = getFinalWarp(event.getItemStack(), event.getEntity());
      if (warp > 0) {
         event.getToolTip()
            .add(Component.literal(I18n.get("item.warping", new Object[0]) + " " + warp).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)));
      }
   }

   public static int getFinalCharge(ItemStack stack) {
      if (stack.getItem() instanceof IRunicArmor armor) {
         int var4 = armor.getRunicCharge(stack);
         CompoundTag compound = ItemStackData.getTag(stack);
         if (compound != null && compound.contains("RS.HARDEN")) {
            var4 += compound.getByte("RS.HARDEN");
         }

         return var4;
      } else {
         return 0;
      }
   }

   public static int getFinalWarp(ItemStack stack, Player player) {
      return stack.getItem() instanceof IWarpingGear armor ? armor.getWarp(stack, player) : 0;
   }

   public static int getHardening(ItemStack stack) {
      if (!(stack.getItem() instanceof IRunicArmor)) {
         return 0;
      } else {
         CompoundTag compound = ItemStackData.getTag(stack);
         return compound != null && compound.contains("RS.HARDEN") ? compound.getByte("RS.HARDEN") : 0;
      }
   }

   /**
    * Fortress helm masks (port of EventHandlerRunic.entityHurt, TC4): the Sipping Fiend (2) heals its wearer
    * for 1 when they hit something, the Angry Ghost (1) withers whoever hits the wearer in melee. The chance
    * grows with the damage: amount / 12 and amount / 10.
    */
   public static void applyFortressMasks(LivingEntity target, DamageSource source, float amount) {
      if (target.level().isClientSide || amount <= 0.0F) {
         return;
      }

      if (source.getDirectEntity() instanceof Player leecher && hasMask(leecher, 2, amount, 12.0F)) {
         leecher.heal(1.0F);
      }

      if (target instanceof Player player && source.getDirectEntity() instanceof LivingEntity attacker && hasMask(player, 1, amount, 10.0F)) {
         attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
      }
   }

   private static boolean hasMask(Player player, int mask, float amount, float det) {
      ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
      if (!(helm.getItem() instanceof ItemFortressArmor)) {
         return false;
      } else {
         CompoundTag compound = ItemStackData.getTag(helm);
         return compound != null && compound.contains("mask") && compound.getInt("mask") == mask && player.level().random.nextFloat() < amount / det;
      }
   }
}
