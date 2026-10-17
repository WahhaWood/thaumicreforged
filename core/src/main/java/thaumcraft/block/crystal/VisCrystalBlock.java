package thaumcraft.block.crystal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.render.ITIntColor;
import thaumcraft.init.registry.ModItems;

public class VisCrystalBlock extends Block implements ITIntColor {
   public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 3);
   public static final IntegerProperty GENERATION = IntegerProperty.create("gen", 1, 4);
   public static final BooleanProperty NORTH = BooleanProperty.create("north");
   public static final BooleanProperty EAST = BooleanProperty.create("east");
   public static final BooleanProperty SOUTH = BooleanProperty.create("south");
   public static final BooleanProperty WEST = BooleanProperty.create("west");
   public static final BooleanProperty UP = BooleanProperty.create("up");
   public static final BooleanProperty DOWN = BooleanProperty.create("down");
   private static final VoxelShape FULL = box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape SHAPE_UP = box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape SHAPE_DOWN = box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
   private static final VoxelShape SHAPE_EAST = box(8.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape SHAPE_WEST = box(0.0, 0.0, 0.0, 8.0, 16.0, 16.0);
   private static final VoxelShape SHAPE_SOUTH = box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0);
   private static final VoxelShape SHAPE_NORTH = box(0.0, 0.0, 0.0, 16.0, 16.0, 8.0);
   private final Aspect aspect;

   public VisCrystalBlock(Aspect aspect) {
      super(
         Properties.of()
            .mapColor(MapColor.STONE)
            .sound(CrystalClusterBlock.SOUND_TYPE)
            .strength(0.25F, 0.5F)
            .noOcclusion()
            .noCollission()
            .lightLevel(state -> 1)
            .randomTicks()
      );
      this.aspect = aspect;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any())
                                 .setValue(SIZE, 0))
                              .setValue(GENERATION, 1))
                           .setValue(UP, false))
                        .setValue(DOWN, false))
                     .setValue(NORTH, false))
                  .setValue(SOUTH, false))
               .setValue(EAST, false))
            .setValue(WEST, false)
      );
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{SIZE, GENERATION, UP, DOWN, NORTH, SOUTH, EAST, WEST});
   }

   @Override
   public int getColor(int tint) {
      return tint == 0 ? this.aspect.getColor() : -1;
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      int count = (Integer)state.getValue(SIZE) + 1;
      return List.of(new ItemStack(this.getShardItem(), count));
   }

   @NotNull
   public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return Shapes.empty();
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      int faces = countFaces(state);
      if (faces > 1) {
         return FULL;
      } else if ((Boolean)state.getValue(UP)) {
         return SHAPE_UP;
      } else if ((Boolean)state.getValue(DOWN)) {
         return SHAPE_DOWN;
      } else if ((Boolean)state.getValue(EAST)) {
         return SHAPE_EAST;
      } else if ((Boolean)state.getValue(WEST)) {
         return SHAPE_WEST;
      } else if ((Boolean)state.getValue(SOUTH)) {
         return SHAPE_SOUTH;
      } else {
         return state.getValue(NORTH) ? SHAPE_NORTH : FULL;
      }
   }

   @NotNull
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      BlockState state = updateAllFaces(context.getLevel(), context.getClickedPos(), this.defaultBlockState());
      return hasAnyFace(state) ? state : null;
   }

   @NotNull
   public BlockState updateShape(
      @NotNull BlockState state,
      @NotNull Direction direction,
      @NotNull BlockState neighborState,
      @NotNull LevelAccessor level,
      @NotNull BlockPos pos,
      @NotNull BlockPos neighborPos
   ) {
      BooleanProperty prop = propertyFor(direction);
      boolean attached = canAttachTo(level, neighborPos, direction);
      BlockState updated = (BlockState)state.setValue(prop, attached);
      return !hasAnyFace(updated) ? Blocks.AIR.defaultBlockState() : updated;
   }

   public void neighborChanged(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos, boolean isMoving
   ) {
      if (!level.isClientSide) {
         BlockState updated = updateAllFaces(level, pos, state);
         if (hasAnyFace(updated) && updated != state) {
            level.setBlock(pos, updated, 2);
         }
      }
   }

   public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
      BlockState updated = updateAllFaces(level, pos, state);
      return hasAnyFace(updated);
   }

   public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
      if (random.nextInt(3 + (Integer)state.getValue(GENERATION)) == 0) {
         int growth = (Integer)state.getValue(SIZE);
         if (growth < 3 && random.nextInt(2) == 0) {
            level.setBlock(pos, (BlockState)state.setValue(SIZE, growth + 1), 2);
         } else {
            if ((Integer)state.getValue(GENERATION) < 4 && random.nextInt(4) == 0) {
               BlockPos target = spreadTarget(level, pos, random);
               if (target != null) {
                  BlockState next = (BlockState)((BlockState)this.defaultBlockState().setValue(GENERATION, (Integer)state.getValue(GENERATION) + 1))
                     .setValue(SIZE, 0);
                  next = updateAllFaces(level, target, next);
                  if (hasAnyFace(next)) {
                     level.setBlock(target, next, 2);
                  }
               }
            }
         }
      }
   }

   public static BlockState updateAllFaces(LevelReader level, BlockPos pos, BlockState state) {
      return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.setValue(UP, canAttachTo(level, pos.above(), Direction.UP)))
                     .setValue(DOWN, canAttachTo(level, pos.below(), Direction.DOWN)))
                  .setValue(NORTH, canAttachTo(level, pos.north(), Direction.NORTH)))
               .setValue(SOUTH, canAttachTo(level, pos.south(), Direction.SOUTH)))
            .setValue(EAST, canAttachTo(level, pos.east(), Direction.EAST)))
         .setValue(WEST, canAttachTo(level, pos.west(), Direction.WEST));
   }

   public static boolean hasAnyFace(BlockState state) {
      return (Boolean)state.getValue(UP)
         || (Boolean)state.getValue(DOWN)
         || (Boolean)state.getValue(NORTH)
         || (Boolean)state.getValue(SOUTH)
         || (Boolean)state.getValue(EAST)
         || (Boolean)state.getValue(WEST);
   }

   private static int countFaces(BlockState state) {
      int c = 0;
      if ((Boolean)state.getValue(UP)) {
         c++;
      }

      if ((Boolean)state.getValue(DOWN)) {
         c++;
      }

      if ((Boolean)state.getValue(NORTH)) {
         c++;
      }

      if ((Boolean)state.getValue(SOUTH)) {
         c++;
      }

      if ((Boolean)state.getValue(EAST)) {
         c++;
      }

      if ((Boolean)state.getValue(WEST)) {
         c++;
      }

      return c;
   }

   private static BooleanProperty propertyFor(Direction direction) {
      return switch (direction) {
         case UP -> UP;
         case DOWN -> DOWN;
         case NORTH -> NORTH;
         case SOUTH -> SOUTH;
         case EAST -> EAST;
         case WEST -> WEST;
         default -> throw new MatchException(null, null);
      };
   }

   private static boolean canAttachTo(LevelReader level, BlockPos supportPos, Direction directionFromCrystal) {
      BlockState support = level.getBlockState(supportPos);
      if (support.isAir()) {
         return false;
      } else {
         return !support.is(BlockTags.STONE_ORE_REPLACEABLES)
               && !support.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
               && !support.is(BlockTags.BASE_STONE_OVERWORLD)
            ? false
            : support.isFaceSturdy(level, supportPos, directionFromCrystal.getOpposite());
      }
   }

   private static BlockPos spreadTarget(ServerLevel level, BlockPos pos, RandomSource random) {
      BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
      if (target.equals(pos)) {
         return null;
      } else {
         BlockState state = level.getBlockState(target);
         if (!state.getFluidState().isEmpty()) {
            return null;
         } else if (!state.isAir() && !state.canBeReplaced()) {
            return null;
         } else {
            return hasSupport(level, target) ? target : null;
         }
      }
   }

   private static boolean hasSupport(LevelAccessor level, BlockPos pos) {
      return canAttachTo(level, pos.above(), Direction.UP)
         || canAttachTo(level, pos.below(), Direction.DOWN)
         || canAttachTo(level, pos.north(), Direction.NORTH)
         || canAttachTo(level, pos.south(), Direction.SOUTH)
         || canAttachTo(level, pos.east(), Direction.EAST)
         || canAttachTo(level, pos.west(), Direction.WEST);
   }

   private Item getShardItem() {
      if (this.aspect == Aspect.AIR) {
         return (Item)ModItems.AIR_SHARD.get();
      } else if (this.aspect == Aspect.FIRE) {
         return (Item)ModItems.FIRE_SHARD.get();
      } else if (this.aspect == Aspect.WATER) {
         return (Item)ModItems.WATER_SHARD.get();
      } else if (this.aspect == Aspect.EARTH) {
         return (Item)ModItems.EARTH_SHARD.get();
      } else if (this.aspect == Aspect.ORDER) {
         return (Item)ModItems.ORDER_SHARD.get();
      } else {
         return this.aspect == Aspect.ENTROPY ? (Item)ModItems.ENTROPY_SHARD.get() : (Item)ModItems.AIR_SHARD.get();
      }
   }
}
