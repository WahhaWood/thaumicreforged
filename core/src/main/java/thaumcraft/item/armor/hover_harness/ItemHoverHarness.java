package thaumcraft.item.armor.hover_harness;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.block.jar.standart.JarBlockItem;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.util.ItemStackData;

public class ItemHoverHarness extends ArmorItem implements IRepairable, IVisDiscountGear, IRunicArmor, net.minecraft.world.MenuProvider {
   public ItemHoverHarness() {
      super(ThaumcraftApi.ARMOR_MATERIAL_SPECIAL, Type.CHESTPLATE, new Properties().durability(400).rarity(Rarity.EPIC));
   }

   @NotNull
   public Component getDisplayName() {
      return Component.translatable("item.thaumcraft.hover_harness");
   }

   @org.jetbrains.annotations.Nullable
   public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, @NotNull net.minecraft.world.entity.player.Inventory inventory, @NotNull Player player) {
      return new HoverHarnessMenu(id, inventory, player.level(), player.blockPosition());
   }

   @NotNull
   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (level.isClientSide) {
         return InteractionResultHolder.success(stack);
      } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
         serverPlayer.openMenu(this, buf -> buf.writeBlockPos(player.blockPosition()));
         return InteractionResultHolder.consume(stack);
      }
      return InteractionResultHolder.pass(stack);
   }

   /** Only the worn harness hovers (TC4 onArmorTick); one carried in the inventory does nothing. */
   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
      if (entity instanceof Player player && player.getItemBySlot(EquipmentSlot.CHEST) == stack) {
         HoverLogic.tickHover(player, stack);
      }
   }

   public boolean onDroppedByPlayer(ItemStack item, Player player) {
      if (player.containerMenu instanceof HoverHarnessMenu) {
         player.closeContainer();
      }

      return super.onDroppedByPlayer(item, player);
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      return aspect == Aspect.AIR ? 5 : 2;
   }

   public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
      return repair.is(Items.GOLD_INGOT) || super.isValidRepairItem(toRepair, repair);
   }

   @OnlyIn(Dist.CLIENT)
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return ResourceLocation.parse("thaumcraft:textures/model/hoverharness.png");
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         private ModelHoverHarness model;

         public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
            if (equipmentSlot != EquipmentSlot.CHEST) {
               return original;
            } else {
               if (this.model == null) {
                  this.model = new ModelHoverHarness(Minecraft.getInstance().getEntityModels().bakeLayer(ModelHoverHarness.LAYER_LOCATION));
               }

               ((net.minecraft.client.model.HumanoidModel) original).copyPropertiesTo(this.model);
               this.model.setAllVisible(false);
               this.model.body.visible = true;
               return this.model;
            }
         }
      });
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      Level level = context.level();
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null && tag.contains("jar", 10)) {
         ItemStack jar = ItemStackData.loadStack(tag.getCompound("jar"));
         if (jar.getItem() instanceof JarBlockItem jarItem) {
            AspectList aspects = jarItem.getAspects(jar);
            if (aspects != null && aspects.size() > 0) {
               Player player = Minecraft.getInstance().player;

               for (Aspect a : aspects.getAspectsSorted()) {
                  if (player != null && PlayerKnowledge.of(player).hasDiscoveredAspect(a)) {
                     tooltip.add(Component.literal(a.getName() + " x " + aspects.getAmount(a)).withStyle(ChatFormatting.AQUA));
                  } else {
                     tooltip.add(Component.translatable("tc.aspect.unknown").withStyle(ChatFormatting.DARK_GRAY));
                  }
               }
            }
         }
      }

      Player player = Minecraft.getInstance().player;
      tooltip.add(Component.empty());
      tooltip.add(Component.translatable("tc.visdiscount").append(": " + this.getVisDiscount(stack, player, null) + "%").withStyle(ChatFormatting.DARK_PURPLE));
      tooltip.add(
         Component.translatable("tc.visdiscount")
            .append(" (Aer): " + this.getVisDiscount(stack, player, Aspect.AIR) + "%")
            .withStyle(ChatFormatting.DARK_PURPLE)
      );
   }
}
