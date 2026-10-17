package thaumcraft.item.equipment.blood_sword;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.IRepairable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.material.ToolMaterial;
import thaumcraft.util.ItemStackData;

public class ItemBloodSword extends SwordItem implements IRepairable {
   public static final String TAG_ACTIVATED = "Active";
   private static final Tier BLOOD_TIER = new ToolMaterial(
      0, 950, 0.0F, 0.0F, ThaumcraftApi.TOOL_MATERIAL_THAUMIUM.getEnchantmentValue(), () -> Ingredient.EMPTY
   );
   private static final int BASE_DAMAGE = 10;
   private static final int DAMAGE_MODIFIER = 9;
   private static final float SPEED_MODIFIER = -2.4F;
   private static final ResourceLocation MOVE_SPEED_ID = ResourceLocation.fromNamespaceAndPath("thaumcraft", "blood_sword_speed");
   private int handleNext = 0;

   public ItemBloodSword() {
      super(BLOOD_TIER, new Properties().attributes(SwordItem.createAttributes(BLOOD_TIER, 9, -2.4F)));
   }

   @NotNull
   public UseAnim getUseAnimation(@NotNull ItemStack stack) {
      return UseAnim.BLOCK;
   }

   public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
      return 72000;
   }

   @NotNull
   public ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
      return super.getDefaultAttributeModifiers(stack)
         .withModifierAdded(Attributes.MOVEMENT_SPEED, new AttributeModifier(MOVE_SPEED_ID, 0.25, Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND);
   }

   public static boolean isActive(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag != null && tag.getInt("Active") == 1;
   }

   private static void setActivated(ItemStack stack, boolean activated) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.putInt("Active", activated ? 1 : 0);
      ItemStackData.setTag(stack, tag);
   }

   private static void addDrop(LivingDropsEvent event, ItemStack dropStack) {
      LivingEntity living = event.getEntity();
      ItemEntity entityItem = new ItemEntity(living.level(), living.getX(), living.getY(), living.getZ(), dropStack);
      entityItem.setPickUpDelay(10);
      event.getDrops().add(entityItem);
   }
}
