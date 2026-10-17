package thaumcraft.item.armor.cultist_leader;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.ThaumcraftApi;

public class ItemCultistLeaderArmor extends ArmorItem implements IRepairable, IRunicArmor {
   public ItemCultistLeaderArmor(Type type) {
      super(ThaumcraftApi.ARMOR_MATERIAL_THAUMIUM_FORTRESS, type, new Properties().rarity(Rarity.RARE));
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return ResourceLocation.parse("thaumcraft:textures/model/cultist_leader_armor.png");
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         private ModelLeaderArmor inner;
         private ModelLeaderArmor outer;

         private void ensureInit() {
            if (this.inner == null || this.outer == null) {
               EntityModelSet set = Minecraft.getInstance().getEntityModels();
               this.inner = new ModelLeaderArmor(set.bakeLayer(ModelLeaderArmor.LAYER_INNER));
               this.outer = new ModelLeaderArmor(set.bakeLayer(ModelLeaderArmor.LAYER_OUTER));
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
