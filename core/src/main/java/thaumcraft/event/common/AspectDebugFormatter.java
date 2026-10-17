package thaumcraft.event.common;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.research.ScanManager;

final class AspectDebugFormatter {
   private AspectDebugFormatter() {
   }

   static List<Component> describeHeldItem(ItemStack stack) {
      AspectList directAspects = ThaumcraftApi.getObjectTags(stack);
      AspectList finalAspects = ThaumcraftCraftingManager.getObjectTags(stack);
      AspectList bonusAspects = ThaumcraftCraftingManager.getBonusTags(stack, finalAspects);
      return List.of(
         debugLine("Item", String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()))),
         debugLine("Hash", ScanManager.getItemScanHash(stack)),
         debugLine("Registry key", String.valueOf(ThaumcraftApi.getObjectAspectScanKey(stack))),
         debugLine("Direct", directAspects),
         debugLine("Final", finalAspects),
         debugLine("Bonus", bonusAspects)
      );
   }

   static List<Component> describeEntity(Entity entity) {
      AspectList aspects = ScanManager.generateEntityAspects(entity);
      return List.of(
         debugLine("Entity", String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()))),
         debugLine("Hash", ScanManager.getEntityScanHash(entity)),
         debugLine("Registry key", String.valueOf(ThaumcraftApi.getEntityAspectScanKey(entity))),
         debugLine("Aspects", aspects)
      );
   }

   private static Component debugLine(String label, Object value) {
      return Component.literal(label + ": " + value);
   }
}
