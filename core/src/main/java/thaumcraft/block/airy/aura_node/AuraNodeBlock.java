package thaumcraft.block.airy.aura_node;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.api.nodes.INode;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.util.UtilsFX;

public class AuraNodeBlock extends TickableEntityBlock {
   private final VoxelShape shape = box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);

   public AuraNodeBlock() {
      super(Properties.of().sound(SoundType.WOOL).strength(2.0F, 200.0F).lightLevel(value -> 8).noCollission());
   }

   @NotNull
   @Override
   @OnlyIn(Dist.CLIENT)
   public RenderShape getRenderShape(@NotNull BlockState blockState) {
      return RenderShape.INVISIBLE;
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return this.shape;
   }

   @OnlyIn(Dist.CLIENT)
   public void attack(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player) {
      UtilsFX.infusedStoneSparkle(level, blockPos, 0);
   }

   protected void spawnDestroyParticles(@NotNull Level level, @NotNull Player player, @NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      Thaumcraft.setup.burst(level, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 1.0F);
      level.playLocalSound(blockPos, (SoundEvent)ModSounds.CRAFT_FAIL.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
      super.spawnDestroyParticles(level, player, blockPos, blockState);
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState blockState, @NotNull Builder builder) {
      List<ItemStack> stacks = new ArrayList<>();
      if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof INode node && node.getAspects().size() > 0) {
         stacks = node.getDrops();
      }

      return stacks;
   }

   public boolean canHarvestBlock(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Player player) {
      return true;
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
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new AuraNodeBlockEntity(blockPos, blockState);
   }

   @Override
   public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity entity, @NotNull ItemStack stack) {
      if (stack.getItem() instanceof thaumcraft.item.aura_node.PresetAuraNodeItem preset) {
         preset.configurePlacedNode(level, pos, entity);
      } else if (!level.isClientSide) {
         if (level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node) {
            java.util.List<thaumcraft.api.aspects.Aspect> primals = thaumcraft.api.aspects.Aspect.getPrimalAspects();
            thaumcraft.api.aspects.AspectList aspects = new thaumcraft.api.aspects.AspectList();
            int count = 1 + level.random.nextInt(4);
            for (int i = 0; i < count; i++) {
               thaumcraft.api.aspects.Aspect aspect = primals.get(level.random.nextInt(primals.size()));
               aspects.add(aspect, 8 + level.random.nextInt(24));
            }
            node.setAspects(aspects);
            float roll = level.random.nextFloat();
            node.setNodeType(roll < 0.12F ? thaumcraft.api.nodes.NodeType.UNSTABLE : (roll < 0.2F ? thaumcraft.api.nodes.NodeType.DARK : thaumcraft.api.nodes.NodeType.NORMAL));
            if (level.random.nextFloat() < 0.25F) {
               node.setNodeModifier(level.random.nextBoolean() ? thaumcraft.api.nodes.NodeModifier.BRIGHT : thaumcraft.api.nodes.NodeModifier.PALE);
            }
            node.setChanged();
            node.markForUpdate();
         }
      }
   }
}
