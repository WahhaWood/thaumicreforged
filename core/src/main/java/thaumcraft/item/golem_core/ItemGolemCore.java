package thaumcraft.item.golem_core;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemGolemCore extends Item {
   private static final Map<GolemCore, ItemGolemCore> BY_CORE = new EnumMap<>(GolemCore.class);
   private final GolemCore core;

   public ItemGolemCore(GolemCore core) {
      super(new Properties().stacksTo(64));
      this.core = core;
      BY_CORE.put(core, this);
   }

   public GolemCore getCore() {
      return this.core;
   }

   @Nullable
   public static ItemGolemCore getItemByCore(GolemCore core) {
      return BY_CORE.get(core);
   }

   public static ItemStack getStackByCore(GolemCore core) {
      ItemGolemCore item = getItemByCore(core);
      return item == null ? ItemStack.EMPTY : new ItemStack(item);
   }

   public boolean hasGUI() {
      return this.core.hasGUI();
   }

   public boolean canSort() {
      return this.core.canSort();
   }

   public boolean hasInventory() {
      return this.core.hasInventory();
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
      Level level = context.level();
   }

   /** ПКМ ядром по голему: ставим ядро, старое выпадает (как в 1.7.10). */
   @Override
   public net.minecraft.world.InteractionResult interactLivingEntity(
      ItemStack stack, net.minecraft.world.entity.player.Player player, net.minecraft.world.entity.LivingEntity target,
      net.minecraft.world.InteractionHand hand
   ) {
      if (!(target instanceof thaumcraft.entities.smart.golem.EntityGolemBase golem)) {
         return net.minecraft.world.InteractionResult.PASS;
      }

      if (golem.level().isClientSide()) {
         return net.minecraft.world.InteractionResult.SUCCESS;
      }

      thaumcraft.item.golem_core.GolemCore old = golem.getCore();
      if (old == this.core) {
         return net.minecraft.world.InteractionResult.PASS;
      }

      golem.setCore(this.core);
      golem.setupGolemInventory();
      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }

      ItemStack oldStack = getStackByCore(old);
      if (!oldStack.isEmpty()) {
         golem.spawnAtLocation(oldStack);
      }

      return net.minecraft.world.InteractionResult.SUCCESS;
   }
}