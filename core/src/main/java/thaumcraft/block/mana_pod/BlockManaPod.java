package thaumcraft.block.mana_pod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.init.registry.ModBiomes;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.api.aspects.Aspect;

public class BlockManaPod extends Block implements EntityBlock {
   public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);
   private static final VoxelShape[] SHAPES = new VoxelShape[]{
      Block.box(4.0, 12.0, 4.0, 12.0, 16.0, 12.0),
      Block.box(4.0, 10.0, 4.0, 12.0, 16.0, 12.0),
      Block.box(4.0, 8.0, 4.0, 12.0, 16.0, 12.0),
      Block.box(4.0, 6.0, 4.0, 12.0, 16.0, 12.0),
      Block.box(4.0, 5.0, 4.0, 12.0, 16.0, 12.0),
      Block.box(4.0, 4.0, 4.0, 12.0, 16.0, 12.0),
      Block.box(4.0, 3.0, 4.0, 12.0, 16.0, 12.0),
      Block.box(4.0, 2.0, 4.0, 12.0, 16.0, 12.0)
   };

   public BlockManaPod() {
      super(Properties.of().randomTicks().noCollission().noOcclusion().lightLevel(s -> (Integer)s.getValue(AGE)));
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> b) {
      b.add(new Property[]{AGE});
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPES[state.getValue(AGE)];
   }

   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
      return true;
   }

   public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      int age = (Integer)state.getValue(AGE);
      return super.getDestroyProgress(state, player, level, pos) * (8 - age);
   }

   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      if (ctx.getClickedFace() != Direction.DOWN) {
         return null;
      } else {
         BlockState state = (BlockState)this.defaultBlockState().setValue(AGE, 0);
         return this.canSurvive(state, ctx.getLevel(), ctx.getClickedPos()) ? state : null;
      }
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState support = level.getBlockState(pos.above());
      boolean biomeOk = level.getBiome(pos).is(ModBiomes.MAGICAL_FOREST);
      boolean supportOk = support.is((Block)ModBlocks.SILVERWOOD_LOG.get())
         || support.is((Block)ModBlocks.SILVERWOOD_LOG_NODE.get())
         || support.is(BlockTags.LOGS);
      return biomeOk && supportOk;
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      super.neighborChanged(state, level, pos, block, fromPos, isMoving);
      if (!level.isClientSide && !state.canSurvive(level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   /**
    * Рост стручка (порт {@code BlockManaPod#func_149674_a}): без опоры ломается,
    * иначе 1/30 — тик роста у блок-энтити.
    */
   @Override
   public void randomTick(@NotNull BlockState state, @NotNull net.minecraft.server.level.ServerLevel level, @NotNull BlockPos pos, @NotNull net.minecraft.util.RandomSource random) {
      super.randomTick(state, level, pos, random);
      if (!state.canSurvive(level, pos)) {
         level.destroyBlock(pos, true);
      } else if (random.nextInt(30) == 0
         && level.getBlockEntity(pos) instanceof ManaPodBlockEntity pod) {
         pod.checkGrowth();
      }
   }

   /**
    * Дроп стручков (порт {@code BlockManaPod#getDrops}): молодые (возраст < 2) пустые;
    * зрелый даёт 1 боб, полностью выросший (7) иногда 2; аспект — из блок-энтити.
    */
   @Override
   public java.util.List<ItemStack> getDrops(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      int age = state.getValue(AGE);
      if (age < 2) {
         return java.util.List.of();
      }

      int count = 1;
      net.minecraft.world.level.Level level = builder.getLevel();
      if (age == 7 && level.random.nextFloat() > 0.33F) {
         count = 2;
      }

      Aspect aspect = Aspect.PLANT;
      net.minecraft.world.level.block.entity.BlockEntity be = builder.getOptionalParameter(
         net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY
      );
      if (be instanceof ManaPodBlockEntity pod && pod.aspect != null) {
         aspect = pod.aspect;
      }

      java.util.List<ItemStack> drops = new java.util.ArrayList<>();
      for (int i = 0; i < count; i++) {
         ItemStack bean = new ItemStack((ItemLike)ModItems.MANA_BEAN.get());
         if (bean.getItem() instanceof thaumcraft.item.mana_bean.ItemManaBean beanItem) {
            beanItem.setAspects(bean, new thaumcraft.api.aspects.AspectList().add(aspect, 1));
         }

         drops.add(bean);
      }

      return drops;
   }

   public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity be, ItemStack tool) {
      super.playerDestroy(level, player, pos, state, be, tool);
   }

   public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
      return new ItemStack((ItemLike)ModItems.MANA_BEAN.get());
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new ManaPodBlockEntity(pos, state);
   }
}
