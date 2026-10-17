package thaumcraft.api.wands;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.util.ItemStackData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemFocusBasic extends Item {
   public static final Map<String, ItemFocusBasic> TYPES = new HashMap<>();
   private static final DecimalFormat FORMATTER = new DecimalFormat("#####.##");
   private final String type;

   public ItemFocusBasic(String type) {
      this(type, Rarity.RARE);
   }

   protected ItemFocusBasic(String type, Rarity rarity) {
      super(new Properties().setNoRepair().stacksTo(1).rarity(rarity));
      this.type = type;
      synchronized (TYPES) {
         TYPES.put(type, this);
      }
   }

   public String getType() {
      return this.type;
   }

   public ResourceLocation getModelLocation() {
      ResourceLocation key = BuiltInRegistries.ITEM.getKey(this);
      String namespace = key != null ? key.getNamespace() : "thaumcraft";
      return ResourceLocation.fromNamespaceAndPath(namespace, "item/wand/focus/" + this.type);
   }

   public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag) {
      AspectList al = this.getVisCost(stack);
      if (al != null && al.size() > 0) {
         components.add(Component.translatable(this.isVisCostPerTick(stack) ? "item.Focus.cost2" : "item.Focus.cost1"));

         for (Aspect aspect : al.getAspectsSorted()) {
            String amount = FORMATTER.format(al.getAmount(aspect) / 100.0F);
            components.add(Component.literal(aspect.getStringName() + " x " + amount).setStyle(Style.EMPTY.withColor(aspect.getColor())));
         }
      }

      this.addFocusInformation(stack, null, components, tooltipFlag);
   }

   @OnlyIn(Dist.CLIENT)
   public void addFocusInformation(@NotNull ItemStack focusstack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag) {
      LinkedHashMap<Short, Integer> map = new LinkedHashMap<>();

      for (short id : this.getAppliedUpgrades(focusstack)) {
         if (id >= 0) {
            int amt = 1;
            if (map.containsKey(id)) {
               amt = map.get(id) + 1;
            }

            map.put(id, amt);
         }
      }

      Style style = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE);

      for (short idx : map.keySet()) {
         components.add(
            Component.literal(
                  FocusUpgradeType.types[idx].getLocalizedName() + (map.get(idx) > 1 ? " " + I18n.get("enchantment.level." + map.get(idx), new Object[0]) : "")
               )
               .setStyle(style)
         );
      }
   }

   public int getFocusColor(ItemStack focusstack) {
      return 0;
   }

   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack focusstack) {
      return ItemFocusBasic.WandFocusAnimation.WAVE;
   }

   public AspectList getVisCost(ItemStack focusstack) {
      return new AspectList();
   }

   public boolean isVisCostPerTick(ItemStack focusstack) {
      return false;
   }

   public ItemStack onFocusRightClick(ItemStack wandStack, Level world, Player player, InteractionHand hand) {
      return ItemStack.EMPTY;
   }

   public ItemStack onFocusEntityClick(ItemStack wandStack, Level world, Player player, InteractionHand hand, Entity entity) {
      return ItemStack.EMPTY;
   }

   public ItemStack onFocusBlockClick(ItemStack wandStack, Level world, Player player, InteractionHand hand, BlockPos blockPos, Direction direction) {
      return ItemStack.EMPTY;
   }

   public boolean onFocusBlockStartBreak(ItemStack wandStack, BlockPos blockPos, Player player) {
      return false;
   }

   public void onUsingFocusTick(ItemStack wandStack, Player player, int count) {
   }

   public void onPlayerStoppedUsingFocus(ItemStack wandStack, Level level, Player player, int count) {
   }

   public boolean acceptsEnchant(int level) {
      return false;
   }

   public String getSortingHelper(ItemStack focusstack) {
      StringBuilder out = new StringBuilder();

      for (short id : this.getAppliedUpgrades(focusstack)) {
         out.append((int)id);
      }

      return out.toString();
   }

   public int getActivationCooldown(ItemStack focusstack) {
      return 0;
   }

   public int getMaxAreaSize(ItemStack focusstack) {
      return 1;
   }

   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
      return null;
   }

   public short[] getAppliedUpgrades(ItemStack focusstack) {
      short[] l = new short[]{-1, -1, -1, -1, -1};
      ListTag nbttaglist = this.getFocusUpgradeTagList(focusstack);
      if (nbttaglist != null) {
         for (int j = 0; j < nbttaglist.size() && j < 5; j++) {
            l[j] = nbttaglist.getCompound(j).getShort("id");
         }
      }

      return l;
   }

   public boolean applyUpgrade(ItemStack focusstack, FocusUpgradeType type, int rank) {
      short[] upgrades = this.getAppliedUpgrades(focusstack);
      if (rank - 1 >= 0 && rank - 1 < upgrades.length && upgrades[rank - 1] == -1 && rank <= 5) {
         upgrades[rank - 1] = type.id;
         this.setFocusUpgradeTagList(focusstack, upgrades);
         return true;
      } else {
         return false;
      }
   }

   public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
      return true;
   }

   public boolean isUpgradedWith(ItemStack focusstack, FocusUpgradeType upgradeType) {
      return this.getUpgradeLevel(focusstack, upgradeType) > 0;
   }

   public int getUpgradeLevel(ItemStack focusstack, FocusUpgradeType upgradeType) {
      short[] list = this.getAppliedUpgrades(focusstack);
      int level = 0;

      for (short id : list) {
         if (id == upgradeType.id) {
            level++;
         }
      }

      return level;
   }

   private ListTag getFocusUpgradeTagList(ItemStack focusstack) {
      CompoundTag compound = ItemStackData.getTag(focusstack);
      return compound == null ? null : compound.getList("upgrade", 10);
   }

   private void setFocusUpgradeTagList(ItemStack focusstack, short[] upgrades) {
      CompoundTag compound = ItemStackData.getOrCreateTag(focusstack);
      ListTag listTag = new ListTag();
      compound.put("upgrade", listTag);

      for (short id : upgrades) {
         CompoundTag f = new CompoundTag();
         f.putShort("id", id);
         listTag.add(f);
      }
   }

   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean selected) {
      CompoundTag compound = ItemStackData.getTag(stack);
      if (compound != null && compound.contains("ench")) {
         compound.remove("ench");
      }
   }

   public static enum WandFocusAnimation {
      NONE,
      WAVE,
      CHARGE;
   }
}
