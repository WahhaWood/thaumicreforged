package thaumcraft.item.armor.traveller_boots;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IBootsTraveller;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.item.armor.hover_harness.HoverLogic;

public class ItemBootsTraveller extends ArmorItem implements IBootsTraveller, IRepairable, IRunicArmor {
   public ItemBootsTraveller() {
      super(ThaumcraftApi.ARMOR_MATERIAL_SPECIAL, Type.BOOTS, new Properties().durability(350).rarity(Rarity.RARE));
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return ResourceLocation.parse("thaumcraft:textures/model/bootstraveler.png");
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean selected) {
      if (entity instanceof Player player) {
         ItemStack feetStack = player.getItemBySlot(EquipmentSlot.FEET);
         if (feetStack != stack) {
            return;
         }

         if (!player.getAbilities().flying && player.zza > 0.0F) {
            if (level.isClientSide && !player.isShiftKeyDown()) {
               if (!Thaumcraft.instance.entityEventHandler.prevStep.containsKey(player.getId())) {
                  Thaumcraft.instance.entityEventHandler.prevStep.put(player.getId(), player.maxUpStep());
               }

               player.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1.0);
            }

            if (player.onGround()) {
               float bonus = 0.055F;
               if (player.isInWater()) {
                  bonus /= 4.0F;
               }

               player.moveRelative(bonus, new Vec3(0.0, 0.0, 1.0));
            } else if (HoverLogic.isActive(player.getItemBySlot(EquipmentSlot.CHEST))) {
               player.getAbilities().setFlyingSpeed(0.03F);
            } else {
               player.getAbilities().setFlyingSpeed(0.05F);
            }
         }

         if (player.fallDistance > 0.0F) {
            player.fallDistance -= 0.25F;
         }
      }
   }
}
