package thaumcraft.block.tube.valve;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.tube.BlockTube;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.relics.ItemResonator;
import thaumcraft.item.wands.ItemWandCasting;

public class BlockValve extends BlockTube {
   public BlockValve(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new TubeValveBlockEntity(pos, state);
   }

   /** TC4 BlockTube.onBlockActivated (valve): a click with anything but a wand, resonator or valve opens or shuts it by hand. */
   public ItemInteractionResult useItemOn(
      ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
   ) {
      if (heldItem.getItem() instanceof ItemWandCasting || heldItem.getItem() instanceof ItemResonator || heldItem.is(this.asItem())
         || !(level.getBlockEntity(pos) instanceof TubeValveBlockEntity valve)) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }

      if (!level.isClientSide) {
         valve.toggleValve();
         level.playSound(null, pos, ModSounds.SQUEEK.get(), SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
      }

      return ItemInteractionResult.sidedSuccess(level.isClientSide);
   }
}
