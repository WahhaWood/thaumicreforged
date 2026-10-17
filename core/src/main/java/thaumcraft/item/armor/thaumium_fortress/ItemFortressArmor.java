package thaumcraft.item.armor.thaumium_fortress;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
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
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IGoggles;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.nodes.IRevealer;
import thaumcraft.util.ItemStackData;

public class ItemFortressArmor extends ArmorItem implements IRepairable, IRunicArmor, IGoggles, IRevealer {
   public ItemFortressArmor(Type type) {
      super(ThaumcraftApi.ARMOR_MATERIAL_THAUMIUM_FORTRESS, type, new Properties().rarity(Rarity.RARE));
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return ResourceLocation.parse("thaumcraft:textures/model/fortress_armor.png");
   }

   public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
      Level level = context.level();
      CompoundTag compound = ItemStackData.getTag(stack);
      if (compound != null) {
         if (compound.contains("goggles")) {
            components.add(Component.translatable("item.ItemGoggles.name").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)));
         }

         if (compound.contains("mask")) {
            components.add(Component.translatable("item.HelmetFortress.mask." + compound.getInt("mask")).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
         }
      }
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @Override
   public boolean showNodes(ItemStack itemstack, LivingEntity player) {
      return ItemStackData.hasTag(itemstack) && ItemStackData.getTag(itemstack).contains("goggles");
   }

   @Override
   public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
      return ItemStackData.hasTag(itemstack) && ItemStackData.getTag(itemstack).contains("goggles");
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         private ModelFortressArmor inner;
         private ModelFortressArmor outer;

         private void ensureInit() {
            if (this.inner == null || this.outer == null) {
               EntityModelSet set = Minecraft.getInstance().getEntityModels();
               this.inner = new ModelFortressArmor(set.bakeLayer(ModelFortressArmor.LAYER_INNER));
               this.outer = new ModelFortressArmor(set.bakeLayer(ModelFortressArmor.LAYER_OUTER));
            }
         }

         @OnlyIn(Dist.CLIENT)
         public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            this.ensureInit();
            HumanoidModel<?> model = slot != EquipmentSlot.LEGS && slot != EquipmentSlot.HEAD ? this.outer : this.inner;
            ((net.minecraft.client.model.HumanoidModel) original).copyPropertiesTo(model);
            model.setAllVisible(false);
            switch (slot) {
               case HEAD:
                  model.head.visible = true;
                  model.hat.visible = true;
                  break;
               case CHEST:
                  model.body.visible = true;
                  model.rightArm.visible = true;
                  model.leftArm.visible = true;
                  break;
               case LEGS:
                  model.body.visible = true;
                  model.rightLeg.visible = true;
                  model.leftLeg.visible = true;
                  break;
               case FEET:
                  model.rightLeg.visible = true;
                  model.leftLeg.visible = true;
            }

            return model;
         }
      });
   }
}
