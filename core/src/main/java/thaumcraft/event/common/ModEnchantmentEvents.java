package thaumcraft.event.common;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Tick;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Post;
import thaumcraft.init.registry.ModEnchantment;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.util.ItemStackData;

public class ModEnchantmentEvents {
   private static final String NBT_LAST_TARGET = "TTEnchantLastTarget";
   private static final String NBT_SUCCESSIVE_STRIKE = "TTEnchantSuccessiveStrike";
   private static final String NBT_TUNNEL_DIRECTION = "TTEnchantTunnelDir";

   @SubscribeEvent
   public void onEntityDamaged(LivingIncomingDamageEvent event) {
      if (event.getSource().getEntity() instanceof LivingEntity attacker) {
         ItemStack heldItem = attacker.getMainHandItem();
         if (!heldItem.isEmpty()) {
            if (attacker instanceof Player player) {
               int pounce = getArmorMaxEnchantmentLevel(player, ModEnchantment.POUNCE.getKey());
               if (pounce > 0 && player.level().getBlockState(player.blockPosition().below()).isAir()) {
                  event.setAmount((float)(event.getAmount() * (1.0 + 0.25 * pounce)));
               }
            }

            int finalStrike = getEnchantLevel(attacker, heldItem, ModEnchantment.FINAL_STRIKE.getKey());
            if (finalStrike > 0 && attacker.getRandom().nextInt(20 - finalStrike) == 0) {
               event.setAmount(event.getAmount() * 3.0F);
            }

            int valiance = getEnchantLevel(attacker, heldItem, ModEnchantment.VALIANCE.getKey());
            if (valiance > 0 && attacker.getHealth() / attacker.getMaxHealth() < 0.5F) {
               event.setAmount((float)(event.getAmount() * (1.0 + 0.1 * valiance)));
            }

            int focusedStrikes = getEnchantLevel(attacker, heldItem, ModEnchantment.FOCUSED_STRIKES.getKey());
            int dispersedStrikes = getEnchantLevel(attacker, heldItem, ModEnchantment.DISPERSED_STRIKES.getKey());
            if (focusedStrikes > 0 || dispersedStrikes > 0 && !(event.getEntity() instanceof Player)) {
               CompoundTag tag = ItemStackData.getOrCreateTag(heldItem);
               UUID lastTarget = tag.hasUUID("TTEnchantLastTarget") ? tag.getUUID("TTEnchantLastTarget") : new UUID(0L, 0L);
               int successiveStrikes = tag.getInt("TTEnchantSuccessiveStrike");
               UUID currentTarget = event.getEntity().getUUID();
               if (!lastTarget.equals(currentTarget)) {
                  tag.putUUID("TTEnchantLastTarget", currentTarget);
               } else {
                  successiveStrikes = Math.max(successiveStrikes + 1, 5);
               }

               tag.putInt("TTEnchantSuccessiveStrike", successiveStrikes);
               if (focusedStrikes > 0) {
                  float amount = event.getAmount();
                  amount /= 2.0F;
                  amount += (float)(0.5 * Math.max(successiveStrikes, 5) * amount * focusedStrikes);
                  event.setAmount(amount);
               }

               if (dispersedStrikes > 0 && !(event.getEntity() instanceof Player)) {
                  float amount = event.getAmount();
                  amount *= (float)(1.0 + 0.6 * dispersedStrikes + successiveStrikes / 5.0F);
                  amount /= (float)(1.0 + 0.25 * dispersedStrikes + successiveStrikes * 2.5);
                  event.setAmount(amount);
               }

               ItemStackData.setTag(heldItem, tag);
            }

            int vampirism = getEnchantLevel(attacker, heldItem, ModEnchantment.VAMPIRISM.getKey());
            if (vampirism > 0) {
               if (attacker instanceof Player playerx && false) {
                  return;
               }

               attacker.heal(vampirism);
               attacker.level().playSound(null, event.getEntity().blockPosition(), (SoundEvent)ModSounds.ZAP.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onEntityUpdate(Post event) {
      if (event.getEntity() instanceof Player player) {
         int var6 = getArmorMaxEnchantmentLevel(player, ModEnchantment.SLOW_FALL.getKey());
         double min = -0.0784000015258789;
         if (var6 > 0 && !player.isShiftKeyDown() && player.getDeltaMovement().y < min && player.fallDistance >= 2.9F) {
            player.setDeltaMovement(player.getDeltaMovement().x, player.getDeltaMovement().y / (1.0F + var6 * 0.33F), player.getDeltaMovement().z);
            player.fallDistance = Math.max(2.9F, player.fallDistance - var6 / 3.0F);
            if (player.level().isClientSide) {
               player.level()
                  .addParticle(
                     ParticleTypes.CLOUD,
                     player.getX() + 0.25,
                     player.getY() - 1.0,
                     player.getZ() + 0.25,
                     -player.getDeltaMovement().x,
                     player.getDeltaMovement().y,
                     -player.getDeltaMovement().z
                  );
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onItemUseTick(Tick event) {
      if (event.getEntity() instanceof Player player) {
         ItemStack usingItem = event.getItem();
         if (usingItem.getItem() instanceof BowItem) {
            int quickDraw = getEnchantLevel(player, player.getMainHandItem(), ModEnchantment.QUICK_DRAW.getKey());
            if (quickDraw > 0) {
               int used = usingItem.getUseDuration(player) - event.getDuration();
               int mod = 6 - Mth.clamp(quickDraw, 1, 2);
               if (used > 0 && used % mod == 0) {
                  event.setDuration(Math.max(0, event.getDuration() - 1));
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onPlayerJump(LivingJumpEvent event) {
      if (event.getEntity() instanceof Player player) {
         int boost = getArmorMaxEnchantmentLevel(player, ModEnchantment.ASCENT_BOOST.getKey());
         if (boost >= 1 && !player.isShiftKeyDown()) {
            player.setDeltaMovement(player.getDeltaMovement().x, player.getDeltaMovement().y * ((boost + 2) / 2.0), player.getDeltaMovement().z);
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public void onFall(LivingFallEvent event) {
      if (event.getEntity() instanceof Player player) {
         if (!player.level().isClientSide) {
            int shockwave = getEnchantLevel(player, player.getItemBySlot(EquipmentSlot.FEET), ModEnchantment.SHOCKWAVE.getKey());
            if (shockwave > 0 && !(event.getDistance() <= 3.0F)) {
               AABB area = player.getBoundingBox().inflate(10.0);

               for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, area)) {
                  if (target != player && true) {
                     target.hurt(player.damageSources().fall(), 0.1F * shockwave * event.getDistance());
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onBreakBlock(BreakEvent event) {
      Player player = event.getPlayer();
      ItemStack item = player.getMainHandItem();
      if (!item.isEmpty()) {
         int tunnel = getEnchantLevel(player, item, ModEnchantment.TUNNEL.getKey());
         if (tunnel > 0) {
            CompoundTag tag = ItemStackData.getOrCreateTag(item);
            tag.putFloat("TTEnchantTunnelDir", player.getYRot());
            ItemStackData.setTag(item, tag);
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onGetHarvestSpeed(BreakSpeed event) {
      Player player = event.getEntity();
      ItemStack heldItem = player.getMainHandItem();
      if (!heldItem.isEmpty() && event.getPosition().isPresent()) {
         int shatter = getEnchantLevel(player, heldItem, ModEnchantment.SHATTER.getKey());
         if (shatter > 0) {
            if (event.getState().getDestroySpeed(player.level(), event.getPosition().orElse(BlockPos.ZERO)) > 20.0F) {
               event.setNewSpeed(event.getNewSpeed() * (3.0F * shatter));
            } else {
               event.setNewSpeed(event.getNewSpeed() * 0.8F);
            }
         }

         int tunnel = getEnchantLevel(player, heldItem, ModEnchantment.TUNNEL.getKey());
         CompoundTag heldTag = ItemStackData.getTag(heldItem);
         if (tunnel > 0 && heldTag != null && heldTag.contains("TTEnchantTunnelDir")) {
            float oldDir = heldTag.getFloat("TTEnchantTunnelDir");
            float diff = Math.abs(oldDir - player.getYRot());
            if (diff < 50.0F) {
               event.setNewSpeed((float)(event.getNewSpeed() * (1.0 + 0.2 * tunnel)));
            } else {
               event.setNewSpeed(event.getNewSpeed() * 0.3F);
            }
         }

         int desintegrate = getEnchantLevel(player, heldItem, ModEnchantment.DESINTEGRATE.getKey());
         int autoSmelt = getEnchantLevel(player, heldItem, ModEnchantment.AUTO_SMELT.getKey());
         BlockPos pos = event.getPosition().orElse(BlockPos.ZERO);
         boolean desintegrateApplies = desintegrate > 0 && event.getState().getDestroySpeed(player.level(), pos) <= 1.5F;
         boolean autoSmeltApplies = autoSmelt > 0 && event.getState().is(BlockTags.LOGS);
         if (desintegrateApplies || autoSmeltApplies) {
            if (player.level() instanceof ServerLevel serverLevel) {
               heldItem.hurtAndBreak(1, serverLevel, player, p -> {});
            }

            event.setNewSpeed(Float.MAX_VALUE);
         } else if (desintegrate > 0 || autoSmelt > 0) {
            event.setCanceled(true);
         }
      }
   }

   private static int getEnchantLevel(LivingEntity entity, ItemStack stack, ResourceKey<Enchantment> enchantment) {
      return stack.isEmpty()
         ? 0
         : entity.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .get(enchantment)
            .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
            .orElse(0);
   }

   private static int getArmorMaxEnchantmentLevel(Player player, ResourceKey<Enchantment> enchantment) {
      Holder<Enchantment> holder = (Holder<Enchantment>)player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment).orElse(null);
      if (holder == null) {
         return 0;
      } else {
         int max = 0;

         for (ItemStack armorStack : player.getArmorSlots()) {
            max = Math.max(max, EnchantmentHelper.getItemEnchantmentLevel(holder, armorStack));
         }

         return max;
      }
   }
}
