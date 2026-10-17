package thaumcraft.item.armor.cultist_robe;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.IWarpingGear;
import thaumcraft.api.aspects.Aspect;

public class ItemCultistRobeArmor extends ArmorItem implements IRepairable, IRunicArmor, IVisDiscountGear, IWarpingGear {
   public ItemCultistRobeArmor(Type type) {
      super(ArmorMaterials.IRON, type, new Properties().rarity(Rarity.UNCOMMON));
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return ResourceLocation.parse("thaumcraft:textures/model/cultist_robe_armor.png");
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(
         Component.literal(
            ChatFormatting.DARK_PURPLE
               + I18n.get("tc.visdiscount", new Object[0])
               + ": "
               + this.getVisDiscount(stack, Minecraft.getInstance().player, null)
               + "%"
         )
      );
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      return 1;
   }

   @Override
   public int getWarp(ItemStack itemstack, Player player) {
      return 1;
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         private ModelRobe<LivingEntity> outer;
         private ModelRobe<LivingEntity> inner;

         public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            if (this.outer == null) {
               this.outer = new ModelRobe<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelRobe.OUTER));
               this.inner = new ModelRobe<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelRobe.INNER));
            }

            ModelRobe<LivingEntity> model = slot == EquipmentSlot.LEGS ? this.inner : this.outer;
            ((net.minecraft.client.model.HumanoidModel) original).copyPropertiesTo(model);
            ModelRobe.setSlotVisible(model, slot);
            return model;
         }

         @OnlyIn(Dist.CLIENT)
         public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            return ModelRobe.asGeneric((ModelRobe<?>)this.getHumanoidArmorModel(living, stack, slot, original));
         }
      });
   }
}
