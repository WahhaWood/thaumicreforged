package thaumcraft.item.armor.ichor_gem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.init.registry.Config;
import thaumcraft.util.EntityUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemGemChest extends ItemIchorArmorGem {
   private static final List<UUID> playersWithFlight = new ArrayList<>();

   public ItemGemChest() {
      super(Type.CHESTPLATE);
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
      if (entity instanceof Player player) {
         if (!isEnabled(stack)) {
            removeFlight(player);
         } else {
            if (!level.isClientSide && Config.robeStratosphereProjectileProtect) {
               thaumcraft.item.wands.foci.ItemFocusDeflect.protectFromProjectiles(player);
            }

            this.updateFlight(player, stack);
         }
      }
   }

   private void updateFlight(Player player, ItemStack armor) {
      boolean shouldFly = isEnabled(armor) && this.isWearingThis(player);
      AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
      if (flight != null) {
         if (shouldFly) {
            if (!flight.hasModifier(EntityUtils.FLIGHT.id())) {
               flight.addOrUpdateTransientModifier(EntityUtils.FLIGHT);
            }
         } else {
            removeFlight(player);
         }
      }
   }

   private boolean isWearingThis(Player player) {
      ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
      return !chestArmor.isEmpty() && chestArmor.getItem() instanceof ItemGemChest;
   }

   private static void removeFlight(Player player) {
      AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
      if (flight != null) {
         if (flight.removeModifier(EntityUtils.FLIGHT.id()) && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
         }
      }
   }

   public static void onUnequipped(Player player) {
      removeFlight(player);
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      return 5;
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         private ModelWings<?> cached;

         public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            if (slot != EquipmentSlot.CHEST) {
               return original;
            } else {
               if (this.cached == null) {
                  this.cached = new ModelWings(Minecraft.getInstance().getEntityModels().bakeLayer(ModelWings.LAYER_LOCATION));
               }

               ((net.minecraft.client.model.HumanoidModel) original).copyPropertiesTo(this.cached);
               this.cached.head.visible = false;
               this.cached.hat.visible = false;
               this.cached.leftLeg.visible = false;
               this.cached.rightLeg.visible = false;
               this.cached.applyWingAnim(living);
               return this.cached;
            }
         }
      });
   }
}
