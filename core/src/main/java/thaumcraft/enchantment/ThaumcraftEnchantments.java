package thaumcraft.enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.Enchantment.Cost;
import net.minecraft.world.item.enchantment.Enchantment.EnchantmentDefinition;
import thaumcraft.api.IRepairable;
import thaumcraft.init.registry.ModEnchantment;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.armor.hover_harness.ItemHoverHarness;
import thaumcraft.item.armor.ichor_gem.ItemGemBoots;
import thaumcraft.item.equipment.blood_sword.ItemBloodSword;

public final class ThaumcraftEnchantments {
   private static final ThreadLocal<HolderGetter<Enchantment>> ENCHANTMENT_LOOKUP = new ThreadLocal<>();
   private static final HolderOwner<Enchantment> EXCLUSIVE_OWNER = new HolderOwner<Enchantment>() {};

   private ThaumcraftEnchantments() {
   }

   public static void withEnchantmentLookup(HolderGetter<Enchantment> lookup, Runnable action) {
      ENCHANTMENT_LOOKUP.set(lookup);

      try {
         action.run();
      } finally {
         ENCHANTMENT_LOOKUP.remove();
      }
   }

   public static Enchantment repair() {
      return create(
         "repair",
         support(item -> item == Items.BOOK || item instanceof IRepairable || item.isRepairable(item.getDefaultInstance())),
         2,
         2,
         cost(20, 10),
         cost(70, 10),
         2,
         exclusive(Enchantments.UNBREAKING),
         EquipmentSlotGroup.values()
      );
   }

   public static Enchantment haste() {
      return create(
         "haste",
         support(item -> item == Items.BOOK || isBoot(item) || item instanceof ItemHoverHarness),
         5,
         3,
         cost(15, 9),
         cost(65, 9),
         1,
         HolderSet.direct(new Holder[0]),
         EquipmentSlotGroup.FEET
      );
   }

   public static Enchantment ascentBoost() {
      return create(
         "ascent_boost",
         support(item -> item instanceof ArmorItem armor && armor.getType() == Type.LEGGINGS),
         2,
         4,
         cost(10, 8),
         cost(50, 8),
         2,
         HolderSet.direct(new Holder[0]),
         EquipmentSlotGroup.LEGS
      );
   }

   public static Enchantment slowFall() {
      return create(
         "slow_fall",
         support(item -> item == Items.BOOK || isBoot(item) && !(item instanceof ItemGemBoots)),
         5,
         3,
         cost(15, 9),
         cost(65, 9),
         1,
         exclusive(Enchantments.FEATHER_FALLING, ModEnchantment.SHOCKWAVE),
         EquipmentSlotGroup.FEET
      );
   }

   public static Enchantment autoSmelt() {
      return create(
         "auto_smelt",
         support(item -> item == Items.BOOK || item instanceof DiggerItem && item != ModItems.ELEMENTAL_AXE.get()),
         2,
         1,
         cost(20, 10),
         cost(60, 10),
         2,
         exclusive(Enchantments.UNBREAKING, ModEnchantment.REPAIR),
         EquipmentSlotGroup.MAINHAND
      );
   }

   public static Enchantment desintegrate() {
      return create(
         "desintegrate",
         support(item -> item instanceof DiggerItem),
         2,
         1,
         cost(20, 10),
         cost(60, 10),
         2,
         exclusive(Enchantments.UNBREAKING, ModEnchantment.REPAIR),
         EquipmentSlotGroup.MAINHAND
      );
   }

   public static Enchantment quickDraw() {
      return create(
         "quick_draw", support(item -> item instanceof BowItem), 5, 2, cost(12, 8), cost(52, 8), 1, exclusive(Enchantments.PUNCH), EquipmentSlotGroup.MAINHAND
      );
   }

   public static Enchantment vampirism() {
      return create(
         "vampirism",
         support(item -> item == Items.BOOK || item instanceof SwordItem || item instanceof AxeItem || item instanceof ItemBloodSword),
         2,
         2,
         cost(20, 10),
         cost(60, 10),
         2,
         exclusive(Enchantments.FIRE_ASPECT, Enchantments.KNOCKBACK),
         EquipmentSlotGroup.MAINHAND
      );
   }

   public static Enchantment dispersedStrikes() {
      return create(
         "dispersed_strikes",
         support(ThaumcraftEnchantments::isWeaponItem),
         2,
         5,
         cost(15, 9),
         cost(65, 9),
         2,
         exclusive(Enchantments.UNBREAKING, ModEnchantment.REPAIR, Enchantments.KNOCKBACK, Enchantments.LOOTING),
         EquipmentSlotGroup.MAINHAND
      );
   }

   public static Enchantment finalStrike() {
      return create(
         "final_strike",
         support(ThaumcraftEnchantments::isWeaponItem),
         2,
         5,
         cost(15, 9),
         cost(65, 9),
         2,
         exclusive(Enchantments.UNBREAKING, ModEnchantment.REPAIR, Enchantments.SHARPNESS, Enchantments.SMITE),
         EquipmentSlotGroup.MAINHAND
      );
   }

   public static Enchantment focusedStrikes() {
      return create(
         "focused_strikes",
         support(ThaumcraftEnchantments::isWeaponItem),
         2,
         5,
         cost(15, 9),
         cost(65, 9),
         2,
         exclusive(Enchantments.UNBREAKING, ModEnchantment.REPAIR, Enchantments.KNOCKBACK, Enchantments.LOOTING),
         EquipmentSlotGroup.MAINHAND
      );
   }

   public static Enchantment pounce() {
      return create(
         "pounce",
         support(item -> item instanceof ArmorItem armor && armor.getType() == Type.LEGGINGS),
         5,
         5,
         cost(10, 8),
         cost(50, 8),
         2,
         HolderSet.direct(new Holder[0]),
         EquipmentSlotGroup.LEGS
      );
   }

   public static Enchantment shatter() {
      return create(
         "shatter",
         support(item -> item instanceof DiggerItem),
         5,
         5,
         cost(15, 9),
         cost(65, 9),
         2,
         exclusive(Enchantments.EFFICIENCY, ModEnchantment.DESINTEGRATE),
         EquipmentSlotGroup.MAINHAND
      );
   }

   public static Enchantment shockwave() {
      return create(
         "shockwave",
         support(item -> item == Items.BOOK || isBoot(item) && !(item instanceof ItemGemBoots)),
         5,
         5,
         cost(15, 9),
         cost(65, 9),
         1,
         exclusive(Enchantments.FEATHER_FALLING, ModEnchantment.SLOW_FALL),
         EquipmentSlotGroup.FEET
      );
   }

   public static Enchantment tunnel() {
      return create(
         "tunnel",
         support(item -> item instanceof DiggerItem),
         5,
         5,
         cost(15, 9),
         cost(65, 9),
         2,
         exclusive(ModEnchantment.SHATTER, Enchantments.EFFICIENCY),
         EquipmentSlotGroup.MAINHAND
      );
   }

   public static Enchantment valiance() {
      return create(
         "valiance",
         support(ThaumcraftEnchantments::isWeaponItem),
         2,
         5,
         cost(15, 9),
         cost(65, 9),
         2,
         exclusive(Enchantments.UNBREAKING, ModEnchantment.REPAIR, Enchantments.SHARPNESS, Enchantments.SMITE),
         EquipmentSlotGroup.MAINHAND
      );
   }

   private static Enchantment create(
      String name,
      HolderSet<Item> supportedItems,
      int weight,
      int maxLevel,
      Cost minCost,
      Cost maxCost,
      int anvilCost,
      HolderSet<Enchantment> exclusiveSet,
      EquipmentSlotGroup... slots
   ) {
      EnchantmentDefinition definition = Enchantment.definition(supportedItems, weight, maxLevel, minCost, maxCost, anvilCost, slots);
      return Enchantment.enchantment(definition).exclusiveWith(exclusiveSet).build(ResourceLocation.fromNamespaceAndPath("thaumcraft", name));
   }

   private static HolderSet<Enchantment> exclusive(Object... enchantments) {
      List<Holder<Enchantment>> holders = new ArrayList<>();

      for (Object enchantment : enchantments) {
         if (enchantment instanceof Holder<?> holder) {
            holders.add(resolveHolder(holder));
         } else if (enchantment instanceof ResourceKey<?> key) {
            holders.add(resolveKey((ResourceKey<Enchantment>)key));
         }
      }

      return HolderSet.direct(holders);
   }

   private static Holder<Enchantment> resolveHolder(Holder<?> holder) {
      return holder.unwrapKey().map(key -> resolveKey((ResourceKey<Enchantment>)key)).orElse((Holder<Enchantment>)holder);
   }

   private static Holder<Enchantment> resolveKey(ResourceKey<Enchantment> key) {
      HolderGetter<Enchantment> lookup = ENCHANTMENT_LOOKUP.get();
      return lookup != null ? lookup.getOrThrow(key) : Reference.createStandAlone(EXCLUSIVE_OWNER, key);
   }

   private static HolderSet<Item> support(Predicate<Item> filter) {
      return HolderSet.direct(BuiltInRegistries.ITEM.stream().filter(filter).map(Item::builtInRegistryHolder).toList());
   }

   private static Cost cost(int base, int perLevel) {
      return Enchantment.dynamicCost(base, perLevel);
   }

   private static boolean isBoot(Item item) {
      return item instanceof ArmorItem armor && armor.getType() == Type.BOOTS;
   }

   private static boolean isWeaponItem(Item item) {
      return item == Items.BOOK || item instanceof SwordItem || item instanceof AxeItem || item instanceof ItemBloodSword;
   }
}
