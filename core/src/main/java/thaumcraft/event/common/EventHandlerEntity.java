package thaumcraft.event.common;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import thaumcraft.api.IBootsTraveller;
import thaumcraft.init.registry.ModEnchantment;
import thaumcraft.item.armor.hover_harness.HoverLogic;
import thaumcraft.item.armor.hover_harness.ItemHoverHarness;
import thaumcraft.item.cat_amulet.ItemCatAmulet;
import thaumcraft.item.equipment.ichor.SoulHeartHelper;
import top.theillusivec4.curios.api.CuriosApi;

public class EventHandlerEntity {
   public HashMap<Integer, Float> prevStep = new HashMap<>();
   public static HashMap<String, ArrayList<WeakReference<Entity>>> linkedEntities = new HashMap<>();

   @SubscribeEvent
   public void onEntityJoinLevel(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof Creeper creeper && !event.getLevel().isClientSide) {
         creeper.goalSelector.addGoal(3, new ItemCatAmulet.AvoidAmuletBearerGoal(creeper));
      }

      // Синхронизация душевных сердец при входе игрока (иначе HUD пуст до первого удара).
      if (event.getEntity() instanceof ServerPlayer serverPlayer && !event.getLevel().isClientSide) {
         SoulHeartHelper.updateClient(serverPlayer);
      }
   }

   /**
    * Душевные сердца (Soul Hearts) пробуждённого ихориевого меча поглощают урон
    * (порт {@code SoulHeartHandler#onPlayerDamage} из TT2).
    */
   @SubscribeEvent
   public void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
      EventHandlerRunic.applyFortressMasks(event.getEntity(), event.getSource(), event.getAmount());
      if (event.getEntity() instanceof Player player && event.getAmount() > 0.0F) {
         event.setAmount(SoulHeartHelper.removeHP(player, (int)event.getAmount()));
         // Рунический щит поглощает остаток после душевных сердец.
         event.setAmount(thaumcraft.Thaumcraft.instance.runicEventHandler.absorbDamage(player, event.getSource(), event.getAmount()));
      }
   }

   @SubscribeEvent
   public void onLivingSetTarget(LivingChangeTargetEvent event) {
      if (event.getNewAboutToBeSetTarget() instanceof Player player) {
         CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            boolean hasAmulet = handler.findFirstCurio(stack -> stack.getItem() instanceof ItemCatAmulet).isPresent();
            if (hasAmulet) {
               event.setCanceled(true);
            }
         });
      }
   }

   /**
    * Авторемонт вещей с чарами Repair за вис из инвентаря (порт
    * {@code EventHandlerEntity#doRepair} из TC4 1.7.10).
    */
   public static void doRepair(ItemStack stack, Player player) {
      net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> repairHolder =
         (net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>)player.registryAccess()
            .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
            .get(thaumcraft.init.registry.ModEnchantment.REPAIR.getKey())
            .orElse(null);
      int level = repairHolder == null
         ? 0
         : net.minecraft.world.item.enchantment.EnchantmentHelper.getTagEnchantmentLevel(repairHolder, stack);
      if (level <= 0) {
         return;
      }

      level = Math.min(level, 2);
      thaumcraft.api.aspects.AspectList cost = thaumcraft.recipe.ThaumcraftCraftingManager.getObjectTags(stack);
      if (cost == null || cost.size() == 0) {
         return;
      }

      cost = thaumcraft.research.ResearchManager.reduceToPrimals(cost);
      thaumcraft.api.aspects.AspectList finalCost = new thaumcraft.api.aspects.AspectList();
      for (thaumcraft.api.aspects.Aspect aspect : cost.getAspects()) {
         if (aspect != null) {
            finalCost.merge(aspect, (int)Math.sqrt(cost.getAmount(aspect) * 2) * level);
         }
      }

      boolean paid;
      if (stack.getItem() instanceof thaumcraft.api.IRepairableExtended extended) {
         paid = extended.doRepair(stack, player, level)
            && thaumcraft.item.wands.WandManager.consumeVisFromInventory(player, finalCost);
      } else {
         paid = thaumcraft.item.wands.WandManager.consumeVisFromInventory(player, finalCost);
      }

      if (paid) {
         stack.setDamageValue(Math.max(0, stack.getDamageValue() - level));
      }
   }

   @SubscribeEvent
   public void livingTick(Post event) {
      Player player = event.getEntity();
      Level level = player.level();
      this.updateSpeed(player);
      if (level.isClientSide
         && (player.isShiftKeyDown() || !(player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof IBootsTraveller))
         && this.prevStep.containsKey(player.getId())) {
         player.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(this.prevStep.get(player.getId()).floatValue());
         this.prevStep.remove(player.getId());
      }
      if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
         if (player.tickCount > 0 && player.tickCount % 2000 == 0 && !player.hasEffect(thaumcraft.init.registry.ModMobEffect.WARP_WARD)) {
            WarpEvents.checkWarpEvent(serverPlayer);
         }

         if (player.tickCount % 10 == 0 && player.hasEffect(thaumcraft.init.registry.ModMobEffect.DEATH_GAZE)) {
            WarpEvents.checkDeathGaze(serverPlayer);
         }

         // A harness taken off (or dropped) no longer ticks, so its flight is removed here.
         if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ItemHoverHarness)) {
            HoverLogic.stopHover(player);
         }

         // Runic shield: recount every 40 ticks inside, recharge on the TC4 interval.
         thaumcraft.Thaumcraft.instance.runicEventHandler.serverTick(serverPlayer);

         // Авторемонт чинимых вещей с чарами Repair (каждые 40 тиков, как в оригинале).
         if (player.tickCount % 40 == 0 && !player.getAbilities().instabuild) {
            for (ItemStack stack : player.getInventory().items) {
               if (!stack.isEmpty()
                  && stack.isDamaged()
                  && stack.getItem() instanceof thaumcraft.api.IRepairable
                  && !(stack.getItem() instanceof thaumcraft.item.armor.hover_harness.ItemHoverHarness)) {
                  doRepair(stack, player);
               }
            }

            for (ItemStack stack : player.getInventory().armor) {
               if (!stack.isEmpty()
                  && stack.isDamaged()
                  && stack.getItem() instanceof thaumcraft.api.IRepairable) {
                  doRepair(stack, player);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onItemExpire(net.neoforged.neoforge.event.entity.item.ItemExpireEvent event) {
      if (event.getEntity() != null
         && !event.getEntity().getItem().isEmpty()
         && event.getEntity().getItem().getItem() instanceof thaumcraft.item.bath_salts.ItemBathSalts) {
         net.minecraft.core.BlockPos pos = event.getEntity().blockPosition();
         Level level = event.getEntity().level();
         net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
         if (state.is(net.minecraft.world.level.block.Blocks.WATER) && state.getFluidState().isSource()) {
            level.setBlockAndUpdate(pos, thaumcraft.init.registry.ModBlocks.FLUID_PURE.get().defaultBlockState());
         }
      }
   }

   private void updateSpeed(Player player) {
      try {
         if (!player.getAbilities().flying && player.moveDist > 0.0F) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (!boots.isEmpty()) {
               Holder<Enchantment> hasteHolder = (Holder<Enchantment>)player.registryAccess()
                  .lookupOrThrow(Registries.ENCHANTMENT)
                  .get(ModEnchantment.HASTE.getKey())
                  .orElse(null);
               int haste = hasteHolder == null ? 0 : EnchantmentHelper.getTagEnchantmentLevel(hasteHolder, boots);
               if (haste > 0) {
               }
            }
         }
      } catch (Exception var5) {
      }
   }

   @SubscribeEvent
   public void playerJumps(LivingJumpEvent event) {
      // Ботинки путешественника: прыжок на 3 блока (порт EventHandlerEntity#playerJumps из TC4 1.7.10).
      // Пробуждённые ихоровые сапоги: +0.3 к прыжку (порт ItemGemBoots#onPlayerJump из TT2).
      if (event.getEntity() instanceof Player player) {
         net.minecraft.world.item.ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
         if (feet.getItem() instanceof IBootsTraveller) {
            player.push(0.0, 0.275, 0.0);
         } else if (feet.getItem() instanceof thaumcraft.item.armor.ichor_gem.ItemGemBoots
            && thaumcraft.item.armor.ichor_gem.ItemIchorArmorGem.isEnabled(feet)) {
            player.push(0.0, 0.3, 0.0);
         }
      }
   }
}
