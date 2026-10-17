package thaumcraft.item.relics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.block.tube.buffer.TubeBufferBlockEntity;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.util.ItemStackData;

public class ItemResonator extends Item {
   public ItemResonator() {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
   }

   public boolean isFoil(ItemStack stack) {
      return ItemStackData.hasTag(stack);
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Level level = context.getLevel();
      BlockPos blockPos = context.getClickedPos();
      BlockEntity blockEntity = level.getBlockEntity(blockPos);
      if (blockEntity instanceof IEssenceTransport transport) {
         Player player = context.getPlayer();
         if (level.isClientSide) {
            player.swing(context.getHand());
            return super.onItemUseFirst(stack, context);
         } else {
            Direction direction = context.getClickedFace();
            if (blockEntity instanceof TubeBufferBlockEntity buffer) {
               AspectList aspectList = buffer.getAspects();
               if (aspectList.size() > 0) {
                  for (Aspect aspect : aspectList.getAspectsSorted()) {
                     player.sendSystemMessage(Component.translatable("tc.resonator1", new Object[]{aspectList.getAmount(aspect), aspect.getName()}));
                  }
               }
            } else if (transport.getEssenceType(direction) != null) {
               player.sendSystemMessage(
                  Component.translatable("tc.resonator1", new Object[]{transport.getEssenceAmount(direction), transport.getEssenceType(direction).getName()})
               );
            }

            Component s = Component.translatable("tc.resonator3");
            if (transport.getSuctionType(direction) != null) {
               s = transport.getSuctionType(direction).getName();
            }

            player.sendSystemMessage(Component.translatable("tc.resonator2", new Object[]{transport.getSuctionAmount(direction), s}));
            level.playSound(null, blockPos, (SoundEvent)ModSounds.ALEMBIC_KNOCK.get(), SoundSource.AMBIENT, 0.5F, 1.9F + level.random.nextFloat() * 0.1F);
            return InteractionResult.CONSUME;
         }
      } else {
         return super.onItemUseFirst(stack, context);
      }
   }
}
