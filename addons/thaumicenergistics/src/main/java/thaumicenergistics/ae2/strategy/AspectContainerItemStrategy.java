package thaumicenergistics.ae2.strategy;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import thaumicenergistics.ae2.storage.AEAspectKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.block.jar.standart.JarBlockItem;
import thaumcraft.item.phial_essence.ItemPhialEssence;
import thaumcraft.util.ItemStackData;

public class AspectContainerItemStrategy implements ContainerItemStrategy<AEAspectKey, AspectContainerItemStrategy.Context> {
   public static final AspectContainerItemStrategy INSTANCE = new AspectContainerItemStrategy();
   private final List<AspectContainerItemStrategy.InteractFunctions> interactions = new ArrayList<>();

   private AspectContainerItemStrategy() {
      this.registerInteract(stack -> stack.getItem() instanceof ItemPhialEssence ? 8 : 0, data -> {
         if (data.aspectList != null && !data.aspectList.aspects.isEmpty()) {
            data.item.setAspects(data.container, data.aspectList);
         } else {
            data.item.setAspects(data.container, null);
         }

         return data.container;
      });
      this.registerInteract(stack -> stack.getItem() instanceof JarBlockItem ? 64 : 0, data -> {
         if (data.aspectList != null && !data.aspectList.aspects.isEmpty()) {
            data.item.setAspects(data.container, data.aspectList);
         } else {
            data.item.setAspects(data.container, null);
            ItemStackData.removeTagKey(data.container, "AspectFilter");
         }

         return data.container;
      });
   }

   public void registerInteract(ToIntFunction<ItemStack> functionCapacity, Function<AspectContainerItemStrategy.InteractData, ItemStack> functionSave) {
      this.registerInteract(functionCapacity, stack -> (IEssenceContainerItem)stack.getItem(), functionSave);
   }

   public void registerInteract(
      ToIntFunction<ItemStack> functionCapacity,
      Function<ItemStack, IEssenceContainerItem> functionToItem,
      Function<AspectContainerItemStrategy.InteractData, ItemStack> functionSave
   ) {
      AspectContainerItemStrategy.InteractFunctions data = new AspectContainerItemStrategy.InteractFunctions();
      data.functionCapacity = functionCapacity;
      data.functionToItem = functionToItem;
      data.functionSave = functionSave;
      this.interactions.add(data);
   }

   @Nullable
   public GenericStack getContainedStack(ItemStack stack) {
      AspectContainerItemStrategy.InteractFunctions functions = this.getFunction(stack);
      if (functions != null) {
         IEssenceContainerItem item = functions.functionToItem.apply(stack);
         AspectList aspectList = item.getAspects(stack);
         if (aspectList != null && !aspectList.aspects.isEmpty()) {
            for (Entry<Aspect, Integer> entry : aspectList.aspects.entrySet()) {
               if (entry.getValue() > 0) {
                  return new GenericStack(AEAspectKey.of(entry.getKey()), (long)entry.getValue().intValue());
               }
            }
         }
      }

      return null;
   }

   @Nullable
   public AspectContainerItemStrategy.Context findCarriedContext(Player player, AbstractContainerMenu menu) {
      return this.getFunction(menu.getCarried()) != null ? new AspectContainerItemStrategy.CarriedContext(player, menu) : null;
   }

   @Nullable
   public AspectContainerItemStrategy.Context findPlayerSlotContext(Player player, int slot) {
      return this.getFunction(player.getInventory().getItem(slot)) != null ? new AspectContainerItemStrategy.PlayerInvContext(player, slot) : null;
   }

   public long extract(AspectContainerItemStrategy.Context context, AEAspectKey what, long amount, Actionable mode) {
      ItemStack copy = context.getStack().copyWithCount(1);
      AspectContainerItemStrategy.InteractFunctions functions = this.getFunction(copy);
      if (functions == null) {
         return 0L;
      } else {
         AspectContainerItemStrategy.InteractData data = new AspectContainerItemStrategy.InteractData(copy, functions);
         if (data.aspectList != null && !data.aspectList.aspects.isEmpty()) {
            Aspect aspect = what.getAspect();
            int extracted = Math.min(Ints.saturatedCast(amount), data.aspectList.getAmount(aspect));
            if (extracted > 0 && mode == Actionable.MODULATE) {
               data.aspectList.remove(aspect, extracted);
               copy = functions.functionSave.apply(data);
               context.shrink(1);
               context.addOverflow(copy);
            }

            return (long)extracted;
         } else {
            return 0L;
         }
      }
   }

   public long insert(AspectContainerItemStrategy.Context context, AEAspectKey what, long amount, Actionable mode) {
      ItemStack copy = context.getStack().copyWithCount(1);
      AspectContainerItemStrategy.InteractFunctions functions = this.getFunction(copy);
      if (functions == null) {
         return 0L;
      } else {
         AspectContainerItemStrategy.InteractData data = new AspectContainerItemStrategy.InteractData(copy, functions);
         Aspect aspect = what.getAspect();
         if (data.aspectList != null && !data.aspectList.aspects.isEmpty()) {
            if (!data.aspectList.aspects.containsKey(aspect)) {
               return 0L;
            }
         } else {
            CompoundTag tag = ItemStackData.getTag(copy);
            if (tag != null && tag.contains("AspectFilter") && aspect != Aspect.getAspect(tag.getString("AspectFilter"))) {
               return 0L;
            }

            data.aspectList = new AspectList();
         }

         int stored = data.aspectList.getAmount(aspect);
         int capacity = functions.functionCapacity.applyAsInt(copy);
         if (stored >= capacity) {
            return 0L;
         } else {
            int inserted = Math.min(Ints.saturatedCast(amount), capacity - stored);
            if (mode == Actionable.MODULATE) {
               data.aspectList.add(aspect, inserted);
               copy = functions.functionSave.apply(data);
               context.shrink(1);
               context.addOverflow(copy);
            }

            return (long)inserted;
         }
      }
   }

   public void playFillSound(Player player, AEAspectKey what) {
   }

   public void playEmptySound(Player player, AEAspectKey what) {
   }

   @Nullable
   public GenericStack getExtractableContent(AspectContainerItemStrategy.Context context) {
      return this.getContainedStack(context.getStack());
   }

   private AspectContainerItemStrategy.InteractFunctions getFunction(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         for (AspectContainerItemStrategy.InteractFunctions data : this.interactions) {
            if (data.functionCapacity.applyAsInt(stack) > 0) {
               return data;
            }
         }
      }

      return null;
   }

   public static class CarriedContext implements AspectContainerItemStrategy.Context {
      private final Player player;
      private final AbstractContainerMenu menu;

      CarriedContext(Player player, AbstractContainerMenu menu) {
         this.player = player;
         this.menu = menu;
      }

      @Override
      public ItemStack getStack() {
         return this.menu.getCarried();
      }

      @Override
      public void shrink(int amount) {
         ItemStack stack = this.getStack();
         stack.shrink(amount);
         if (stack.isEmpty()) {
            this.setStack(ItemStack.EMPTY);
         }
      }

      @Override
      public void setStack(ItemStack stack) {
         this.menu.setCarried(stack);
      }

      @Override
      public void addOverflow(ItemStack stack) {
         ItemStack carried = this.getStack();
         if (carried.isEmpty()) {
            this.setStack(stack);
         } else if (!this.player.getInventory().add(stack)) {
            this.player.drop(stack, false);
         }
      }
   }

   interface Context {
      ItemStack getStack();

      void shrink(int var1);

      void setStack(ItemStack var1);

      void addOverflow(ItemStack var1);
   }

   public static class InteractData {
      public ItemStack container;
      public AspectList aspectList;
      public final IEssenceContainerItem item;

      public InteractData(ItemStack container, AspectContainerItemStrategy.InteractFunctions functions) {
         this.container = container;
         this.item = functions.functionToItem.apply(container);
         this.aspectList = this.item.getAspects(container);
      }
   }

   public static class InteractFunctions {
      public ToIntFunction<ItemStack> functionCapacity;
      public Function<ItemStack, IEssenceContainerItem> functionToItem;
      public Function<AspectContainerItemStrategy.InteractData, ItemStack> functionSave;
   }

   public static class PlayerInvContext implements AspectContainerItemStrategy.Context {
      private final Player player;
      private final int slot;

      PlayerInvContext(Player player, int slot) {
         this.player = player;
         this.slot = slot;
      }

      @Override
      public ItemStack getStack() {
         return this.player.getInventory().getItem(this.slot);
      }

      @Override
      public void shrink(int amount) {
         this.player.getInventory().removeItem(this.slot, amount);
      }

      @Override
      public void setStack(ItemStack stack) {
         this.player.getInventory().setItem(this.slot, stack);
      }

      @Override
      public void addOverflow(ItemStack stack) {
         if (!this.player.getInventory().add(stack)) {
            this.player.drop(stack, false);
         }
      }
   }
}
