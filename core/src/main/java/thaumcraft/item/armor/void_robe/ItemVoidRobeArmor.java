package thaumcraft.item.armor.void_robe;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IGoggles;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.IWarpingGear;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.IRevealer;
import thaumcraft.item.armor.cultist_robe.ModelRobe;
import thaumcraft.util.ItemStackData;

public class ItemVoidRobeArmor extends ArmorItem implements IRepairable, IRunicArmor, IVisDiscountGear, IGoggles, IRevealer, IWarpingGear {
   private static final int DEFAULT_VOID_ROBE_COLOR = 0;
   private static final String VOID_ROBE_TEXTURE = "thaumcraft:textures/model/void_robe_armor.png";
   private static final String VOID_ROBE_TEXTURE_OVERLAY = "thaumcraft:textures/model/void_robe_armor_overlay.png";

   public ItemVoidRobeArmor(Type type) {
      super(ThaumcraftApi.ARMOR_MATERIAL_VOID, type, new Properties().rarity(Rarity.EPIC));
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return ResourceLocation.parse("thaumcraft:textures/model/void_robe_armor.png");
   }

   public int getColor(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      CompoundTag displayTag = tag != null && tag.contains("display", 10) ? tag.getCompound("display") : null;
      return displayTag != null && displayTag.contains("color", 3) ? displayTag.getInt("color") : 0;
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

         @NotNull
         @OnlyIn(Dist.CLIENT)
         public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            return ModelRobe.asGeneric((ModelRobe<?>)this.getHumanoidArmorModel(living, stack, slot, original));
         }
      });
   }

   @Override
   public int getRunicCharge(ItemStack stack) {
      return 0;
   }

   @Override
   public boolean showNodes(ItemStack stack, LivingEntity player) {
      return stack.getItem() instanceof ArmorItem ai && ai.getType() == Type.HELMET;
   }

   @Override
   public boolean showIngamePopups(ItemStack stack, LivingEntity player) {
      return stack.getItem() instanceof ArmorItem ai && ai.getType() == Type.HELMET;
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      return 5;
   }

   @Override
   public int getWarp(ItemStack itemstack, Player player) {
      return 2;
   }
}
