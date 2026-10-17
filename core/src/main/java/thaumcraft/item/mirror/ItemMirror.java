package thaumcraft.item.mirror;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.block.mirror.BlockMirror;
import thaumcraft.block.mirror.MirrorBlockEntity;
import thaumcraft.block.mirror.MirrorEssenceBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemMirror extends BlockItem implements IEssenceContainerItem {
   public ItemMirror(Block block) {
      super(block, new Properties().stacksTo(1));
   }

   /**
    * ПКМ зеркалом-предметом по стоящему зеркалу — снять «слепок» связи
    * (порт {@code BlockMirrorItem#onItemUseFirst} из TC4 1.7.10).
    */
   @Override
   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Level level = context.getLevel();
      Player player = context.getPlayer();
      BlockPos pos = context.getClickedPos();
      if (player == null) {
         return InteractionResult.PASS;
      }

      BlockEntity be = level.getBlockEntity(pos);
      boolean essenceItem = this.getBlock() instanceof BlockMirror mirrorBlock && mirrorBlock.isEssence();
      boolean applicable = !essenceItem && be instanceof MirrorBlockEntity
         || essenceItem && be instanceof MirrorEssenceBlockEntity;
      if (!applicable) {
         return InteractionResult.PASS;
      }

      if (level.isClientSide) {
         player.swing(context.getHand(), true);
         return InteractionResult.SUCCESS;
      }

      boolean valid = be instanceof MirrorBlockEntity mirror ? mirror.isLinkValid() : ((MirrorEssenceBlockEntity)be).isLinkValid();
      if (!valid) {
         ItemStack copy = stack.copy();
         copy.setCount(1);
         CompoundTag tag = thaumcraft.util.ItemStackData.getOrCreateTag(copy);
         tag.putInt("linkX", pos.getX());
         tag.putInt("linkY", pos.getY());
         tag.putInt("linkZ", pos.getZ());
         tag.putString("linkDim", level.dimension().location().toString());
         if (!player.getInventory().add(copy)) {
            level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), copy));
         }

         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }

         level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 2.0F);
         return InteractionResult.SUCCESS;
      }

      player.displayClientMessage(
         Component.literal("§5§o").append(Component.translatable("tc.mirror.already_linked")), true
      );
      return InteractionResult.SUCCESS;
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         private ItemMirrorRenderer renderer;

         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.renderer == null) {
               this.renderer = new ItemMirrorRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
            }

            return this.renderer;
         }
      });
   }
}
