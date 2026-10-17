package thaumcraft.init.registry;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.item.curios.ItemBaubleBlanks;
import thaumcraft.item.mob_aspect.NumericAspectHelper;
import thaumcraft.item.wands.ItemWandCap;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.ItemWandRod;
import thaumcraft.util.ItemStackData;

@EventBusSubscriber(
   modid = "thaumcraft",
   bus = Bus.MOD
)
public class ModCreativeTab {
   private static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "thaumcraft");
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTER.register(
      "main",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("item_group.thaumcraft.name"))
         .icon(() -> new ItemStack((ItemLike)ModItems.THAUMONOMICON.get()))
         .displayItems((params, output) -> displayItems(ModItems.REGISTER, output::accept))
         .build()
   );

   @SubscribeEvent
   public static void buildContents(BuildCreativeModeTabContentsEvent event) {
      if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
         for (DeferredHolder<Item, Item> egg : ModItems.SPAWN_EGGS) {
            event.accept((ItemLike)egg.get());
         }
      }

      if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
         RegistryLookup<Enchantment> enchantments = event.getParameters().holders().lookupOrThrow(Registries.ENCHANTMENT);
         ModEnchantment.REGISTER
            .getEntries()
            .forEach(
               enchantment -> enchantments.get(enchantment.getKey())
                  .ifPresent(
                     holder -> event.accept(
                        EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, ((Enchantment)holder.value()).getMaxLevel()))
                     )
                  )
            );
      }
   }

   public static void displayItems(DeferredRegister<Item> register, Consumer<ItemStack> consumer) {
      try {
         Set<Item> candleItems = ModItems.CANDLE.values().stream().<Item>map(DeferredHolder::get).collect(Collectors.toSet());

         for (DeferredHolder<Item, ? extends Item> object : register.getEntries()) {
            if (object != null) {
               ItemStack stack = new ItemStack((ItemLike)object.get());
               if (!candleItems.contains(object.get()) && !stack.is((Item)ModItems.WOOD_WAND_ROD.get())) {
                  if (stack.is((Item)ModItems.CURIOS_RING_IRON.get())) {
                     displayAspectRings(consumer);
                  } else if (stack.is((Item)ModItems.WAND_CASTING.get())) {
                     consumer.accept(ItemWandCasting.createWandFill((ItemWandRod)ModItems.WOOD_WAND_ROD.get(), (ItemWandCap)ModItems.IRON_WAND_CAP.get()));
                     consumer.accept(ItemWandCasting.createWandFill((ItemWandRod)ModItems.GREATWOOD_WAND_ROD.get(), (ItemWandCap)ModItems.GOLD_WAND_CAP.get()));
                     consumer.accept(
                        ItemWandCasting.createWandFill((ItemWandRod)ModItems.SILVERWOOD_WAND_ROD.get(), (ItemWandCap)ModItems.THAUMIUM_WAND_CAP.get())
                     );
                     consumer.accept(ItemWandCasting.createWandFill((ItemWandRod)ModItems.ICHOR_WAND_ROD.get(), (ItemWandCap)ModItems.ICHOR_WAND_CAP.get()));
                     consumer.accept(
                        ItemWandCasting.createSceptreFill((ItemWandRod)ModItems.SILVERWOOD_WAND_ROD.get(), (ItemWandCap)ModItems.THAUMIUM_WAND_CAP.get())
                     );
                  } else if (stack.is((Item)ModItems.PHIAL_ESSENCE.get())) {
                     consumer.accept(stack);
                     displayAspectItems(stack, 8, consumer);
                  } else if (stack.is((Item)ModItems.WISP_ESSENCE.get())) {
                     displayAspectItems(stack, 2, consumer);
                  } else if (stack.is((Item)ModItems.CRYSTAL_ESSENCE.get())) {
                     consumer.accept(stack);
                     displayAspectItems(stack, 2, consumer);
                  } else if (stack.is((Item)ModItems.MOB_ASPECT.get())) {
                     displayMobAspectItems(stack, consumer);
                  } else {
                     IEnergyStorage energy = (IEnergyStorage)stack.getCapability(EnergyStorage.ITEM);
                     if (energy != null) {
                        consumer.accept(stack);
                        stack = stack.copy();
                        IEnergyStorage filledEnergy = (IEnergyStorage)stack.getCapability(EnergyStorage.ITEM);
                        if (filledEnergy != null) {
                           filledEnergy.receiveEnergy(filledEnergy.getMaxEnergyStored(), false);
                        }

                        consumer.accept(stack);
                     } else {
                        consumer.accept(stack);
                     }
                  }
               }
            }
         }

         for (DyeColor color : DyeColor.values()) {
            DeferredHolder<Item, BlockItem> candle = (DeferredHolder<Item, BlockItem>)ModItems.CANDLE.get(color);
            if (candle != null) {
               consumer.accept(new ItemStack((ItemLike)candle.get()));
            }
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }
   }

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
   }

   private static void displayAspectItems(ItemStack stack, int amountAspect, Consumer<ItemStack> consumer) {
      if (stack.getItem() instanceof IEssenceContainerItem containerItem) {
         for (Aspect value : Aspect.aspects.values()) {
            ItemStack copy = stack.copy();
            containerItem.setAspects(copy, new AspectList().add(value, amountAspect));
            consumer.accept(copy);
         }
      }
   }

   private static void displayMobAspectItems(ItemStack base, Consumer<ItemStack> consumer) {
      for (int id = 0; id < NumericAspectHelper.getCount(); id++) {
         consumer.accept(createMobAspectStack(base, id, (byte)0));
         consumer.accept(createMobAspectStack(base, id, (byte)1));
         consumer.accept(createMobAspectStack(base, id, (byte)2));
      }
   }

   private static ItemStack createMobAspectStack(ItemStack base, int id, byte type) {
      ItemStack stack = base.copy();
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.putInt("AspectNum", id);
      tag.putByte("Type", type);
      ItemStackData.setTag(stack, tag);
      return stack;
   }

   private static void displayAspectRings(Consumer<ItemStack> consumer) {
      for (Aspect aspect : Aspect.getPrimalAspects()) {
         consumer.accept(ItemBaubleBlanks.createAspectRing(aspect));
      }
   }
}
