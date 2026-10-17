package thaumcraft.item.curios;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.block.vis_relay.VisRelayBlockEntity;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.init.registry.ModItems;
import thaumcraft.util.CuriosSlotUtils;
import thaumcraft.util.ItemStackData;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemAmuletVis extends Item implements ICurioItem, IRunicArmor {
   private final DecimalFormat myFormatter = new DecimalFormat("#######.##");
   private final int maxVis;

   public ItemAmuletVis(int maxVis, Rarity rarity) {
      super(new Properties().stacksTo(1).setNoRepair().rarity(rarity));
      this.maxVis = maxVis;
   }

   public boolean canEquip(SlotContext slotContext, ItemStack stack) {
      return stack.getItem() == this && CuriosSlotUtils.isAmuletSlot(slotContext.identifier());
   }

   public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
      return true;
   }

   /**
    * Работа надетого амулета — порт {@code ItemAmuletVis.onWornTick} (1.7.10,
    * строки 62-105).
    *
    * <p>Метода не было вовсе: амулет был ёмкостью для виса, который
    * некуда было деть. Две обязанности оригинала:
    *
    * <ol>
    * <li>Раз в 5 тиков переливать из амулета в жезл в руке до 5 единиц виса за
    *     аспект — столько, сколько влезет и сколько есть.</li>
    * <li>Если рядом ретранслятор виса (в радиусе 26, запись даёт
    *     {@code TileVisRelay.nearbyPlayers}), забирать из него недостающее и
    *     вызывать {@code triggerConsumeEffect}. Если ретранслятор из карты
    *     пропал или оказался дальше 26 блоков, игрок удаляется из карты.</li>
    * </ol>
    *
    * <p>Обе части требуют портированной половины: {@code nearbyPlayers} в
    * {@code VisRelayBlockEntity} до этого раунда никем не заполнялся, так что
    * читать карту было нечего.
    */
   public void curioTick(SlotContext slotContext, ItemStack stack) {
      LivingEntity living = slotContext.entity();

      if (living == null || living.level().isClientSide() || living.tickCount % 5 != 0) {
         return;
      }

      ItemStack wandStack = living.getItemInHand(InteractionHand.MAIN_HAND);

      if (wandStack.getItem() instanceof ItemWandCasting wand) {
         AspectList withRoom = wand.getAspectsWithRoom(wandStack);

         for (Aspect aspect : withRoom.getAspects()) {
            if (aspect == null || this.getVis(stack, aspect) <= 0) {
               continue;
            }

            // 1.7.10:74-79 — не больше 5 и не больше остатка в жезле.
            int amount = Math.min(5, wand.getMaxVis(wandStack) - wand.getVis(wandStack, aspect));
            amount = Math.min(amount, this.getVis(stack, aspect));

            if (amount > 0) {
               this.storeVis(stack, aspect, this.getVis(stack, aspect) - amount);
               wand.storeVis(wandStack, aspect, wand.getVis(wandStack, aspect) + amount);
            }
         }
      }

      VisRelayBlockEntity relay =
         VisRelayBlockEntity.nearbyPlayers.get(living.getId()) == null
            ? null : VisRelayBlockEntity.nearbyPlayers.get(living.getId()).get();

      if (relay == null
         || relay.distanceToSqr(living.getX(), living.getY(), living.getZ()) >= 676.0D) {
         // 26.0 в квадрате — 676.0; в оригинале сравнение строгое «< 26.0».
         VisRelayBlockEntity.nearbyPlayers.remove(living.getId());
         return;
      }

      AspectList all = this.getAspectsWithRoom(stack);

      for (Aspect aspect : all.getAspects()) {
         if (aspect == null) {
            continue;
         }

         // 1.7.10:93-96 — тянем ровно недостающее до предела.
         int amount = relay.consumeVis(aspect,
            Math.min(5, this.getMaxVis(stack) - this.getVis(stack, aspect)));

         if (amount > 0) {
            this.addRealVis(stack, aspect, amount, true);
            relay.triggerConsumeEffect(aspect);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      if (stack.is((Item)ModItems.VIS_AMULET_LESSER.get())) {
         components.add(Component.translatable("item.ItemAmuletVis.text").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
      }

      components.add(Component.literal(ChatFormatting.GOLD + I18n.get("item.capacity.text", new Object[0]) + " " + this.getMaxVis(stack) / 100));
      CompoundTag compound = ItemStackData.getTag(stack);
      if (compound != null) {
         for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (compound.contains(aspect.getTag())) {
               String amount = this.myFormatter.format(compound.getInt(aspect.getTag()) / 100.0F);
               components.add(aspect.getName().withStyle(aspect.getTooltipColor()).append(" x " + amount));
            }
         }
      }
   }

   public int getMaxVis(ItemStack stack) {
      return this.maxVis;
   }

   public int getVis(ItemStack is, Aspect aspect) {
      CompoundTag compound = ItemStackData.getTag(is);
      int out = 0;
      if (compound != null) {
         out = compound.getInt(aspect.getTag());
      }

      return out;
   }

   public void storeVis(ItemStack is, Aspect aspect, int amount) {
      ItemStackData.getOrCreateTag(is).putInt(aspect.getTag(), amount);
   }

   public AspectList getAspectsWithRoom(ItemStack wandstack) {
      AspectList out = new AspectList();
      AspectList cur = this.getAllVis(wandstack);

      for (Aspect aspect : cur.getAspects()) {
         if (cur.getAmount(aspect) < this.getMaxVis(wandstack)) {
            out.add(aspect, 1);
         }
      }

      return out;
   }

   public AspectList getAllVis(ItemStack is) {
      CompoundTag compound = ItemStackData.getTag(is);
      AspectList out = new AspectList(this.getMaxVis(is));

      for (Aspect aspect : Aspect.getPrimalAspects()) {
         if (compound != null && compound.contains(aspect.getTag())) {
            out.merge(aspect, compound.getInt(aspect.getTag()));
         } else {
            out.merge(aspect, 0);
         }
      }

      return out;
   }

   public boolean consumeAllVis(ItemStack is, Player player, AspectList aspects, boolean doit, boolean crafting) {
      if (aspects != null && aspects.size() != 0) {
         for (Aspect aspect : aspects.getAspects()) {
            if (this.getVis(is, aspect) < aspects.getAmount(aspect)) {
               return false;
            }
         }

         if (doit) {
            for (Aspect aspectx : aspects.getAspects()) {
               this.storeVis(is, aspectx, this.getVis(is, aspectx) - aspects.getAmount(aspectx));
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public int addVis(ItemStack is, Aspect aspect, int amount, boolean doit) {
      if (!aspect.isPrimal()) {
         return 0;
      } else {
         int storeAmount = this.getVis(is, aspect) + amount * 100;
         int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
         if (doit) {
            this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));
         }

         return leftover / 100;
      }
   }

   public int addRealVis(ItemStack is, Aspect aspect, int amount, boolean doit) {
      if (!aspect.isPrimal()) {
         return 0;
      } else {
         int storeAmount = this.getVis(is, aspect) + amount;
         int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
         if (doit) {
            this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));
         }

         return leftover;
      }
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }
}
