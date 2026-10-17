package thaumcraft.block.tube;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.block.bellows.BellowsBlockEntity;

public class BlockTube extends BaseEntityBlock {
   public static final MapCodec<BlockTube> CODEC = simpleCodec(BlockTube::new);
   public static final EnumProperty<TubeSide> DOWN = EnumProperty.create("down", TubeSide.class);
   public static final EnumProperty<TubeSide> UP = EnumProperty.create("up", TubeSide.class);
   public static final EnumProperty<TubeSide> NORTH = EnumProperty.create("north", TubeSide.class);
   public static final EnumProperty<TubeSide> SOUTH = EnumProperty.create("south", TubeSide.class);
   public static final EnumProperty<TubeSide> WEST = EnumProperty.create("west", TubeSide.class);
   public static final EnumProperty<TubeSide> EAST = EnumProperty.create("east", TubeSide.class);
   public static final BooleanProperty HAS_ANY = BooleanProperty.create("has_any");
   public static final Map<Direction, EnumProperty<TubeSide>> DIR_TO_PROP = new EnumMap<>(Direction.class);
   private final Map<BlockState, VoxelShape> shapeCache = new HashMap<>();
   private static final VoxelShape CORE = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);
   private static final VoxelShape ARM_DOWN = Block.box(4.0, 0.0, 4.0, 12.0, 4.0, 12.0);
   private static final VoxelShape ARM_UP = Block.box(4.0, 12.0, 4.0, 12.0, 16.0, 12.0);
   private static final VoxelShape ARM_NORTH = Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 4.0);
   private static final VoxelShape ARM_SOUTH = Block.box(4.0, 4.0, 12.0, 12.0, 12.0, 16.0);
   private static final VoxelShape ARM_WEST = Block.box(0.0, 4.0, 4.0, 4.0, 12.0, 12.0);
   private static final VoxelShape ARM_EAST = Block.box(12.0, 4.0, 4.0, 16.0, 12.0, 12.0);

   public BlockTube(Properties properties) {
      super(properties);
      BlockState defaultState = (BlockState)((BlockState)this.stateDefinition.any()).setValue(HAS_ANY, false);

      for (EnumProperty<TubeSide> prop : DIR_TO_PROP.values()) {
         defaultState = (BlockState)defaultState.setValue(prop, TubeSide.NONE);
      }

      this.registerDefaultState(defaultState);
      this.generateShapes(this.stateDefinition.getPossibleStates());
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{DOWN, UP, NORTH, SOUTH, WEST, EAST, HAS_ANY});
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TubeBlockEntity(pos, state);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.makeConnections(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (placer != null && level.getBlockEntity(pos) instanceof TubeBlockEntity tube) {
         tube.facing = Direction.orderedByNearest(placer)[0].getOpposite();
         tube.setChanged();
      }
   }

   public BlockState updateShape(
      BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos
   ) {
      return this.makeConnections(state, level, currentPos);
   }

   public BlockState recalculateConnections(BlockState state, LevelAccessor level, BlockPos pos) {
      return this.makeConnections(state, level, pos);
   }

   private BlockState makeConnections(BlockState state, LevelAccessor level, BlockPos pos) {
      BlockState newState = state;
      boolean any = false;
      boolean[] open = null;
      if (level.getBlockEntity(pos) instanceof TubeBlockEntity t) {
         open = t.openSides;
      } else if (level.getBlockEntity(pos) instanceof thaumcraft.block.tube.buffer.TubeBufferBlockEntity b) {
         open = b.openSides;
      }

      for (Direction dir : Direction.values()) {
         TubeSide sideState = TubeSide.NONE;
         boolean isMySideOpen = open == null || open[dir.get3DDataValue()];
         if (isMySideOpen) {
            BlockEntity neighborBe = level.getBlockEntity(pos.relative(dir));
            if (neighborBe instanceof IEssenceTransport transport) {
               if (transport.isConnectable(dir.getOpposite())) {
                  if (transport.renderExtendedTube()) {
                     sideState = TubeSide.EXTENDED;
                  } else {
                     sideState = TubeSide.NORMAL;
                  }

                  any = true;
               }
            } else if (neighborBe instanceof BellowsBlockEntity blockEntity && blockEntity.getFacing().getAxis().isHorizontal()) {
               sideState = TubeSide.NORMAL;
               any = true;
            }
         }

         newState = (BlockState)newState.setValue((Property)DIR_TO_PROP.get(dir), sideState);
      }

      return (BlockState)newState.setValue(HAS_ANY, any);
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      super.neighborChanged(state, level, pos, block, fromPos, isMoving);
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
         BlockEntity be = level.getBlockEntity(pos);
         if (be instanceof TubeBlockEntity) {
            level.updateNeighbourForOutputSignal(pos, this);
         }

         super.onRemove(state, level, pos, newState, isMoving);
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return this.shapeCache.getOrDefault(state, Shapes.block());
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return this.shapeCache.getOrDefault(state, Shapes.block());
   }

   private void generateShapes(List<BlockState> states) {
      for (BlockState state : states) {
         VoxelShape shape = CORE;
         if (state.getValue(UP) != TubeSide.NONE) {
            shape = Shapes.or(shape, ARM_UP);
         }

         if (state.getValue(DOWN) != TubeSide.NONE) {
            shape = Shapes.or(shape, ARM_DOWN);
         }

         if (state.getValue(NORTH) != TubeSide.NONE) {
            shape = Shapes.or(shape, ARM_NORTH);
         }

         if (state.getValue(SOUTH) != TubeSide.NONE) {
            shape = Shapes.or(shape, ARM_SOUTH);
         }

         if (state.getValue(WEST) != TubeSide.NONE) {
            shape = Shapes.or(shape, ARM_WEST);
         }

         if (state.getValue(EAST) != TubeSide.NONE) {
            shape = Shapes.or(shape, ARM_EAST);
         }

         this.shapeCache.put(state, shape);
      }
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

   static {
      DIR_TO_PROP.put(Direction.DOWN, DOWN);
      DIR_TO_PROP.put(Direction.UP, UP);
      DIR_TO_PROP.put(Direction.NORTH, NORTH);
      DIR_TO_PROP.put(Direction.SOUTH, SOUTH);
      DIR_TO_PROP.put(Direction.WEST, WEST);
      DIR_TO_PROP.put(Direction.EAST, EAST);
   }
}
