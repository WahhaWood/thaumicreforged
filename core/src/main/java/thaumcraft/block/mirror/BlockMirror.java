package thaumcraft.block.mirror;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.ThaumcraftEntityBlock;
import thaumcraft.block.jar.standart.JarBlock;
import thaumcraft.init.registry.ModBlockEntities;

public class BlockMirror extends ThaumcraftEntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.FACING;
   private final boolean essence;
   protected static final VoxelShape SHAPE_UP = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
   protected static final VoxelShape SHAPE_DOWN = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
   protected static final VoxelShape SHAPE_EAST = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
   protected static final VoxelShape SHAPE_WEST = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);

   public BlockMirror(boolean essence) {
      super(Properties.of().sound(JarBlock.SOUND_TYPE).strength(1.0F, 10.0F).noOcclusion());
      this.essence = essence;
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   public boolean isEssence() {
      return this.essence;
   }

   /**
    * Установка зеркала с меткой связи в руке — связь переносится в блок-энтити
    * (порт {@code BlockMirrorItem#onBlockPlacedBy} из TC4 1.7.10).
    */
   @Override
   public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity entity, @NotNull ItemStack stack) {
      super.setPlacedBy(level, pos, state, entity, stack);
      if (!level.isClientSide && thaumcraft.util.ItemStackData.hasTag(stack)) {
         net.minecraft.nbt.CompoundTag tag = thaumcraft.util.ItemStackData.getTag(stack);
         if (tag != null
            && tag.contains("linkX")
            && level.getBlockEntity(pos) instanceof MirrorBlockEntity mirror) {
            mirror.link = new thaumcraft.api.LevelBlockPos(
               tag.getInt("linkX"), tag.getInt("linkY"), tag.getInt("linkZ"),
               net.minecraft.resources.ResourceKey.create(
                  net.minecraft.core.registries.Registries.DIMENSION,
                  net.minecraft.resources.ResourceLocation.parse(tag.getString("linkDim"))
               )
            );
            mirror.restoreLink();
         }

         if (tag != null
            && tag.contains("linkX")
            && level.getBlockEntity(pos) instanceof MirrorEssenceBlockEntity essenceMirror) {
            essenceMirror.link = new thaumcraft.api.LevelBlockPos(
               tag.getInt("linkX"), tag.getInt("linkY"), tag.getInt("linkZ"),
               net.minecraft.resources.ResourceKey.create(
                  net.minecraft.core.registries.Registries.DIMENSION,
                  net.minecraft.resources.ResourceLocation.parse(tag.getString("linkDim"))
               )
            );
            essenceMirror.restoreLink();
         }
      }
   }

   /**
    * Выброшенные предметы засасывает в зеркало и переносит парному
    * (порт {@code BlockMirror#func_149670_a} из TC4 1.7.10).
    */
   @Override
   public void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
      super.entityInside(state, level, pos, entity);
      if (!level.isClientSide
         && !this.essence
         && entity instanceof ItemEntity item
         && !item.isRemoved()
         && !item.hasPickUpDelay()
         && level.getBlockEntity(pos) instanceof MirrorBlockEntity mirror) {
         mirror.transport(item);
      }
   }

   /**
    * Снятое зеркало помнит последнюю связь (порт {@code BlockMirror#getDrops} из TC4 1.7.10).
    */
   @Override
   public java.util.List<ItemStack> getDrops(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      java.util.List<ItemStack> drops = super.getDrops(state, builder);
      BlockEntity be = builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
      if (be instanceof MirrorBlockEntity mirror && mirror.linked && mirror.link != null) {
         for (ItemStack drop : drops) {
            if (drop.getItem() instanceof thaumcraft.item.mirror.ItemMirror) {
               net.minecraft.nbt.CompoundTag tag = thaumcraft.util.ItemStackData.getOrCreateTag(drop);
               tag.putInt("linkX", mirror.link.getX());
               tag.putInt("linkY", mirror.link.getY());
               tag.putInt("linkZ", mirror.link.getZ());
               tag.putString("linkDim", mirror.link.dim.location().toString());
            }
         }
      }

      if (be instanceof MirrorEssenceBlockEntity essenceMirror && essenceMirror.linked && essenceMirror.link != null) {
         for (ItemStack drop : drops) {
            if (drop.getItem() instanceof thaumcraft.item.mirror.ItemMirror) {
               net.minecraft.nbt.CompoundTag tag = thaumcraft.util.ItemStackData.getOrCreateTag(drop);
               tag.putInt("linkX", essenceMirror.link.getX());
               tag.putInt("linkY", essenceMirror.link.getY());
               tag.putInt("linkZ", essenceMirror.link.getZ());
               tag.putString("linkDim", essenceMirror.link.dim.location().toString());
            }
         }
      }

      return drops;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction face = context.getClickedFace();
      BlockState state = (BlockState)this.defaultBlockState().setValue(FACING, face);
      return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      Direction facing = (Direction)state.getValue(FACING);
      BlockPos supportPos = pos.relative(facing.getOpposite());
      return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
   }

   public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, dir, neighbor, level, pos, neighborPos);
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      if (level.isClientSide()) {
         return null;
      } else {
         return this.essence
            ? createTickerHelper(type, ModBlockEntities.MIRROR_ESSENCE.get(), MirrorEssenceBlockEntity::serverTick)
            : createTickerHelper(type, ModBlockEntities.MIRROR.get(), MirrorBlockEntity::serverTick);
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return (BlockEntity)(this.essence ? new MirrorEssenceBlockEntity(pos, state) : new MirrorBlockEntity(pos, state));
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return switch ((Direction)state.getValue(FACING)) {
         case DOWN -> SHAPE_DOWN;
         case UP -> SHAPE_UP;
         case NORTH -> SHAPE_NORTH;
         case SOUTH -> SHAPE_SOUTH;
         case WEST -> SHAPE_WEST;
         case EAST -> SHAPE_EAST;
         default -> throw new MatchException(null, null);
      };
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return Shapes.empty();
   }
}
