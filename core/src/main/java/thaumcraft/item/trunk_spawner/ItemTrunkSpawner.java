package thaumcraft.item.trunk_spawner;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.item.golem_upgrade.GolemUpgrade;
import thaumcraft.item.golem_upgrade.ItemGolemUpgrade;
import thaumcraft.item.trunk_spawner.render.ItemTrunkSpawnerRenderer;
import thaumcraft.util.ItemStackData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemTrunkSpawner extends Item {
   public ItemTrunkSpawner() {
      super(new Properties().stacksTo(1));
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      CompoundTag compound = ItemStackData.getTag(stack);
      if (compound != null) {
         if (compound.contains("upgrade", 1)) {
            int rawUpgrade = compound.getByte("upgrade");
            if (rawUpgrade > -1 && rawUpgrade < GolemUpgrade.values().length) {
               GolemUpgrade upgrade = GolemUpgrade.values()[rawUpgrade];
               ItemGolemUpgrade upgradeItem = ItemGolemUpgrade.getItemByUpgrade(upgrade);
               if (upgradeItem != null) {
                  components.add(Component.translatable(upgradeItem.getDescriptionId()).setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));
               }
            }
         }

         if (compound.contains("inventory", 10) || compound.contains("inventory", 9)) {
            components.add(Component.translatable("item.TrunkSpawner.text.1"));
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return new ItemTrunkSpawnerRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
         }
      });
   }

   /**
    * Вызов путешествующего сундука — порт {@code ItemTrunkSpawner.func_77648_a}
    * (1.7.10, строки 69-119), то есть {@code onItemUse}: действие по клику по
    * блоку, а не по воздуху. Поэтому хук — {@code useOn}.
    *
    * <p>Раньше метода не было вообще: класс состоял из подсказки и рендера, поэтому
    * {@link thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk}
    * (портирован целиком, со своим GUI и инвентарём) нельзя было вызвать ничем,
    * а исследование {@code travel_trunk} выдавало мёртвый предмет.
    *
    * <p>Позиция — соседняя клетка той стороны, по которой кликнули. На заборе
    * сундук встаёт на полблока выше (как в оригинале и как в {@code ItemGolemPlacer}).
    */
   @Override
   public InteractionResult useOn(UseOnContext ctx) {
      ItemStack stack = ctx.getItemInHand();

      if (ctx.getLevel().isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      if (!(ctx.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
         return InteractionResult.FAIL;
      }

      Level level = ctx.getLevel();
      BlockPos clickedPos = ctx.getClickedPos();
      net.minecraft.core.Direction face = ctx.getClickedFace();
      BlockPos spawnPos = clickedPos.relative(face);

      double yOff = 0.0D;

      if (face == net.minecraft.core.Direction.UP) {
         Block clicked = level.getBlockState(clickedPos).getBlock();
         if (clicked == net.minecraft.world.level.block.Blocks.OAK_FENCE
            || clicked == net.minecraft.world.level.block.Blocks.NETHER_BRICK_FENCE) {
            yOff = 0.5D;
         }
      }

      thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk trunk =
         new thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk(
            (EntityType)thaumcraft.init.registry.ModEntities.TRAVELING_TRUNK.get(), serverLevel);

      trunk.moveTo(
         spawnPos.getX() + 0.5D, spawnPos.getY() + yOff, spawnPos.getZ() + 0.5D,
         level.random.nextFloat() * 360.0F, 0.0F
      );
      trunk.setYHeadRot(trunk.getYRot());
      trunk.yBodyRot = trunk.getYRot();

      if (ctx.getPlayer() != null) {
         trunk.setOwner(ctx.getPlayer().getScoreboardName());
      }

      if (stack.has(DataComponents.CUSTOM_NAME)) {
         trunk.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
      }

      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null) {
         if (tag.contains("upgrade", net.minecraft.nbt.Tag.TAG_BYTE)) {
            trunk.setUpgrade(tag.getByte("upgrade"));
            trunk.setInvSize();
         }

         if (tag.contains("inventory", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            trunk.getInventory().deserializeNBT(tag.getCompound("inventory"));
         }
      }

      // func_110161_a(null) в 1.7.10 назначает постоянство и разбирает эффекты
      // появления. Постоянство сундук уже просит в своём конструкторе, а своих
      // эффектов появления у него нет, поэтому отдельного вызова не делаем —
      // так же, как это сделано в ItemGolemPlacer.
      serverLevel.addFreshEntity(trunk);

      if (ctx.getPlayer() != null && !ctx.getPlayer().getAbilities().instabuild) {
         stack.shrink(1);
      }

      return InteractionResult.CONSUME;
   }
}
