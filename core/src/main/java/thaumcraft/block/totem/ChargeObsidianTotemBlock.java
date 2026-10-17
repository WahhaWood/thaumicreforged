package thaumcraft.block.totem;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.nodes.INode;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.init.registry.ModSounds;

/** TC4 BlockCosmeticSolid meta 8: an obsidian totem with a live aura node inside. */
public class ChargeObsidianTotemBlock extends ObsidianTotemBlock implements EntityBlock {
   public ChargeObsidianTotemBlock(Properties props) {
      super(props);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new AuraNodeBlockEntity(pos, state);
   }

   /** TC4 TileNode.canUpdate: the node in the totem recharges, drains and changes like any other. */
   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
      return level.isClientSide()
         ? (lvl, pos, st, be) -> ((ITickable)be).clientTick(lvl, pos, st)
         : (lvl, pos, st, be) -> ((ITickable)be).serverTick(lvl, pos, st);
   }

   /** TC4 damageDropped(8) = 1 and onBlockHarvested: a plain totem (loot table) plus wisp essence of the node's vis. */
   @NotNull
   @Override
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull Builder builder) {
      List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
      if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof INode node && node.getAspects().size() > 0) {
         drops.addAll(node.getDrops());
      }

      return drops;
   }

   @Override
   protected void spawnDestroyParticles(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state) {
      Thaumcraft.setup.burst(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0F);
      level.playLocalSound(pos, ModSounds.CRAFT_FAIL.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
      super.spawnDestroyParticles(level, player, pos, state);
   }
}
