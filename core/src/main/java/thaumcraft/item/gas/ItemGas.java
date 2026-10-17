package thaumcraft.item.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.block.airy.gazy.BlockGas;
import thaumcraft.init.registry.ModBlocks;

public class ItemGas extends Item {
   private final Block setBlock;

   public ItemGas(Block setBlock) {
      super(new Properties().stacksTo(64).rarity(Rarity.UNCOMMON));
      this.setBlock = setBlock;
   }

   public ItemGas() {
      this((Block)ModBlocks.GASEOUS_SHADOW.get());
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      BlockPos pos = player.blockPosition().above();
      Object var10000 = null;
      boolean air = level.isEmptyBlock(pos);
      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }

      level.playSound(null, player.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
      if (air) {
         if (!level.isClientSide) {
            level.setBlock(pos, (BlockState)this.setBlock.defaultBlockState().setValue(BlockGas.LEVEL, 4), 2);
            level.scheduleTick(pos, this.setBlock, 10);
         } else {
            player.swing(hand);
         }
      }

      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
   }
}
