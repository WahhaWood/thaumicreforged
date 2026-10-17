package thaumcraft.event.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.block.sensor.SensorBlockEntity;
import thaumcraft.item.equipment.ItemElementalPickaxe;
import thaumcraft.item.equipment.ItemPrimalCrusher;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.foci.ItemFocusExcavation;
import thaumcraft.util.Utils;

public class EventHandlerWorld {
   /** TC4 EventHandlerWorld.noteEvent: arcane ears listen to the note blocks played this tick. */
   @SubscribeEvent
   public void onNotePlay(NoteBlockEvent.Play event) {
      if (event.getLevel() instanceof ServerLevel level) {
         BlockPos pos = event.getPos();
         SensorBlockEntity.noteBlockEvents
            .computeIfAbsent(level, l -> new ArrayList<>())
            .add(new Integer[]{pos.getX(), pos.getY(), pos.getZ(), event.getInstrument().ordinal(), event.getVanillaNoteId()});
      }
   }

   /** TC4 EventHandlerWorld.worldUnload: the unloaded level's vis sources leave the network. */
   @SubscribeEvent
   public void onLevelUnload(LevelEvent.Unload event) {
      if (event.getLevel() instanceof ServerLevel level) {
         VisNetHandler.sources.remove(level.dimension());
         SensorBlockEntity.noteBlockEvents.remove(level);
      }
   }

   /**
    * Замена дропа на самородные кластеры при добыче Киркой огня / Первородным дробителем /
    * жезлом с фокусом копания+Dowsing (порт {@code EventHandlerWorld#harvestEvent} из TC4 1.7.10).
    */
   @SubscribeEvent
   public void onBlockDrops(BlockDropsEvent event) {
      if (!(event.getLevel() instanceof ServerLevel level) || !(event.getBreaker() instanceof Player player)) {
         return;
      }

      List<net.minecraft.world.entity.item.ItemEntity> drops = event.getDrops();
      if (drops.isEmpty()) {
         return;
      }

      ItemStack held = player.getMainHandItem();
      if (!isSpecialMiningTool(held)) {
         return;
      }

      float chance = 0.2F + getSpecialMiningFortune(held) * 0.075F;
      boolean converted = false;
      for (net.minecraft.world.entity.item.ItemEntity drop : drops) {
         ItemStack original = drop.getItem();
         ItemStack replacement = Utils.findSpecialMiningResult(original, chance, level.random);
         if (!ItemStack.isSameItem(original, replacement)) {
            drop.setItem(replacement);
            converted = true;
         }
      }

      if (converted) {
         level.playSound(
            null, event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5,
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.2F, 0.7F + level.random.nextFloat() * 0.2F
         );
      }
   }

   private static boolean isSpecialMiningTool(ItemStack tool) {
      if (tool == null || tool.isEmpty()) {
         return false;
      } else if (!(tool.getItem() instanceof ItemElementalPickaxe) && !(tool.getItem() instanceof ItemPrimalCrusher)) {
         if (!(tool.getItem() instanceof ItemWandCasting wand)) {
            return false;
         } else {
            ItemStack focusStack = wand.getFocusItem(tool);
            return focusStack.getItem() instanceof ItemFocusBasic focus && focus.isUpgradedWith(focusStack, ItemFocusExcavation.dowsing);
         }
      } else {
         return true;
      }
   }

   private static int getSpecialMiningFortune(ItemStack tool) {
      if (tool != null && !tool.isEmpty()) {
         if (tool.getItem() instanceof ItemWandCasting wand) {
            ItemStack focusStack = wand.getFocusItem(tool);
            return focusStack.getItem() instanceof ItemFocusBasic focus ? focus.getUpgradeLevel(focusStack, FocusUpgradeType.treasure) : 0;
         } else {
            return tool.getEnchantments()
               .entrySet()
               .stream()
               .filter(entry -> ((Holder)entry.getKey()).is(Enchantments.FORTUNE))
               .mapToInt(Entry::getValue)
               .findFirst()
               .orElse(0);
         }
      } else {
         return 0;
      }
   }
}
