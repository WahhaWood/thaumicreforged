package thaumcraft.block.log;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.nodes.INode;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.init.registry.ModSounds;

public class BlockMagicalLog extends RotatedPillarBlock implements EntityBlock {
   public BlockMagicalLog() {
      super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.5F).lightLevel(value -> 7).ignitedByLava());
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState blockState, @NotNull Builder builder) {
      List<ItemStack> stacks = super.getDrops(blockState, builder);
      if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof INode node && node.getAspects().size() > 0) {
         // В оригинале бревно падает вместе с эссенциями, а не вместо них.
         stacks.addAll(node.getDrops());
      }

      return stacks;
   }

   public void playerDestroy(
      @NotNull Level level,
      @NotNull Player player,
      @NotNull BlockPos pos,
      @NotNull BlockState state,
      @Nullable BlockEntity blockEntity,
      @NotNull ItemStack tool
   ) {
      player.awardStat(Stats.BLOCK_MINED.get(this));
      player.causeFoodExhaustion(0.005F);
      if (level instanceof ServerLevel serverLevel) {
         Builder loot = new Builder(serverLevel)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withParameter(LootContextParams.TOOL, tool)
            .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);

         for (ItemStack drop : this.getDrops(state, loot)) {
            Block.popResource(level, pos, drop);
         }

         state.spawnAfterBreak(serverLevel, pos, tool, false);
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new AuraNodeBlockEntity(blockPos, blockState);
   }

   protected void spawnDestroyParticles(@NotNull Level level, @NotNull Player player, @NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      Vec3 vec3 = blockPos.getCenter();
      Thaumcraft.setup.burst(level, vec3.x, vec3.y, vec3.z, 1.0F);
      level.playLocalSound(vec3.x, vec3.y, vec3.z, (SoundEvent)ModSounds.CRAFT_FAIL.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
      super.spawnDestroyParticles(level, player, blockPos, blockState);
   }

   @javax.annotation.Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? (lvl, pos, st, be) -> {
         if (be instanceof ITickable t) {
            t.clientTick(lvl, pos, st);
         }
      } : (lvl, pos, st, be) -> {
         if (be instanceof ITickable t) {
            t.serverTick(lvl, pos, st);
         }
      };
   }
}
