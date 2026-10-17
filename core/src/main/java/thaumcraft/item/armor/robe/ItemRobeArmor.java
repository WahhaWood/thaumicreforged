package thaumcraft.item.armor.robe;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.util.ItemStackData;

public class ItemRobeArmor extends ArmorItem implements IRepairable, IVisDiscountGear, IRunicArmor {
   private static final int DEFAULT_ROBE_COLOR = 6961280;

   public ItemRobeArmor(Type type) {
      super(ThaumcraftApi.ARMOR_MATERIAL_SPECIAL, type, new Properties().rarity(Rarity.UNCOMMON));
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      if (this.type == Type.CHESTPLATE || this.type == Type.BOOTS) {
         return ResourceLocation.parse("thaumcraft:textures/model/robes_1.png");
      } else {
         return this.type == Type.LEGGINGS
            ? ResourceLocation.parse("thaumcraft:textures/model/robes_2.png")
            : ResourceLocation.parse("thaumcraft:textures/model/robes_1.png");
      }
   }

   public int getColor(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      CompoundTag displayTag = tag != null && tag.contains("display", 10) ? tag.getCompound("display") : null;
      return displayTag != null && displayTag.contains("color", 3) ? displayTag.getInt("color") : 6961280;
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      return this.type == Type.BOOTS ? 1 : 2;
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
}
