package thaumcraft.item.armor.cultist_plate;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.entities.smart.zombie.EntityInhabitedZombie;

public class ItemCultistPlateArmor extends ArmorItem implements IRepairable, IRunicArmor {
   public ItemCultistPlateArmor(Type type) {
      super(ArmorMaterials.IRON, type, new Properties().rarity(Rarity.UNCOMMON));
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return ResourceLocation.parse(
         entity instanceof EntityInhabitedZombie ? "thaumcraft:textures/model/zombie_plate_armor.png" : "thaumcraft:textures/model/cultist_plate_armor.png"
      );
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         private ModelKnightArmor<LivingEntity> outer;
         private ModelKnightArmor<LivingEntity> inner;

         public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            if (this.outer == null) {
               this.outer = new ModelKnightArmor<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelKnightArmor.OUTER));
               this.inner = new ModelKnightArmor<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelKnightArmor.INNER));
            }

            ModelKnightArmor<LivingEntity> model = slot == EquipmentSlot.LEGS ? this.inner : this.outer;
            ((net.minecraft.client.model.HumanoidModel) original).copyPropertiesTo(model);
            ModelKnightArmor.setSlotVisible(model, slot);
            return model;
         }

         @NotNull
         @OnlyIn(Dist.CLIENT)
         public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            return this.getHumanoidArmorModel(living, stack, slot, original);
         }
      });
   }
}
