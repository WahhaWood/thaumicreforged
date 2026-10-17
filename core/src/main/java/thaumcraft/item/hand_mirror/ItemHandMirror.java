package thaumcraft.item.hand_mirror;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.LevelBlockPos;
import thaumcraft.block.mirror.MirrorBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.util.ItemStackData;

public class ItemHandMirror extends Item implements MenuProvider {
   public ItemHandMirror() {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
   }

   public boolean isFoil(ItemStack stack) {
      return ItemStackData.hasTag(stack);
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      BlockPos blockPos = context.getClickedPos();
      Level level = context.getLevel();
      if (level.getBlockState(blockPos).is((Block)ModBlocks.MIRROR.get())) {
         Player player = context.getPlayer();
         if (level.isClientSide) {
            if (player != null) player.swing(context.getHand());
            return InteractionResult.SUCCESS;
         } else if (level.getBlockEntity(blockPos) instanceof MirrorBlockEntity mirror && mirror.linked) {
            LevelBlockPos lbp = new LevelBlockPos(blockPos, level.dimension());
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            lbp.save(tag);
            ItemStackData.setTag(stack, tag);
            level.playSound(null, blockPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.0F);
            if (player != null) {
               player.sendSystemMessage(Component.translatable("tc.handmirrorlinked").setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_PURPLE)));
            }
            return InteractionResult.SUCCESS;
         } else {
            if (player != null) {
               player.sendSystemMessage(Component.translatable("tc.handmirrorerror").setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_PURPLE)));
            }
            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @NotNull
   @Override
   public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (level.isClientSide) {
         return net.minecraft.world.InteractionResultHolder.success(stack);
      } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
         if (ItemStackData.hasTag(stack)) {
            LevelBlockPos blockPos = new LevelBlockPos(ItemStackData.getTag(stack));
            Level level1 = level.getServer().getLevel(blockPos.dim);
            if (level1 != null && level1.getBlockEntity(blockPos.asBlockPos()) instanceof MirrorBlockEntity) {
               serverPlayer.openMenu(this);
               return net.minecraft.world.InteractionResultHolder.consume(stack);
            } else {
               ItemStackData.setTag(stack, null);
               level.playSound(null, player, (SoundEvent)ModSounds.ZAP.get(), SoundSource.AMBIENT, 1.0F, 0.8F);
               player.sendSystemMessage(Component.translatable("tc.handmirrorerror").setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_PURPLE)));
               return net.minecraft.world.InteractionResultHolder.fail(stack);
            }
         }
      }
      return net.minecraft.world.InteractionResultHolder.pass(stack);
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      if (ItemStackData.hasTag(stack)) {
         LevelBlockPos levelBlockPos = new LevelBlockPos(ItemStackData.getTag(stack));
         components.add(
            Component.literal(
               I18n.get("tc.handmirrorlinkedto", new Object[0])
                  + " "
                  + levelBlockPos.getX()
                  + ","
                  + levelBlockPos.getY()
                  + ","
                  + levelBlockPos.getZ()
                  + " in "
                  + levelBlockPos.dim.location()
            )
         );
      }
   }

   public static boolean transport(ItemStack mirror, ItemStack items, Player player, Level level) {
      if (ItemStackData.hasTag(mirror)) {
         LevelBlockPos blockPos = new LevelBlockPos(ItemStackData.getTag(mirror));
         Level level1 = level.getServer().getLevel(blockPos.dim);
         if (level1 == null) {
            return false;
         } else if (level1.getBlockEntity(blockPos.asBlockPos()) instanceof MirrorBlockEntity tm) {
            Direction linkedFacing = tm.getFacing();
            ItemEntity ie2 = new ItemEntity(
               level1,
               blockPos.getX() + 0.5 - linkedFacing.getStepX() * 0.3,
               blockPos.getY() + 0.5 - linkedFacing.getStepY() * 0.3,
               blockPos.getZ() + 0.5 - linkedFacing.getStepZ() * 0.3,
               items.copy()
            );
            ie2.setDeltaMovement(linkedFacing.getStepX() * 0.15F, linkedFacing.getStepY() * 0.15F, linkedFacing.getStepZ() * 0.15F);
            ie2.setPortalCooldown(20);
            level1.addFreshEntity(ie2);
            level.playSound(null, player, SoundEvents.ENDERMAN_TELEPORT, SoundSource.AMBIENT, 0.1F, 1.0F);
            level1.blockEvent(blockPos, (Block)ModBlocks.MIRROR.get(), 1, 0);
            return true;
         } else {
            ItemStackData.setTag(mirror, null);
            level.playSound(null, player, (SoundEvent)ModSounds.ZAP.get(), SoundSource.AMBIENT, 1.0F, 0.8F);
            player.sendSystemMessage(Component.translatable("tc.handmirrorerror").setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_PURPLE)));
            return false;
         }
      } else {
         return false;
      }
   }

   @NotNull
   public Component getDisplayName() {
      return Component.empty();
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowsId, @NotNull Inventory inventory, @NotNull Player player) {
      return new HandMirrorMenu(windowsId, inventory);
   }
}
