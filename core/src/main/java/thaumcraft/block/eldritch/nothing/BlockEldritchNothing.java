package thaumcraft.block.eldritch.nothing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import thaumcraft.util.BlockUtils;

public class BlockEldritchNothing extends Block implements EntityBlock {
   public static final BooleanProperty EXPOSED = BooleanProperty.create("exposed");
   // Оригинал дополнительно задаёт бокс отрисовки func_149668_a с отступом 0.125
   // (func_149676_a(0,0,0,1,1,1) + func_149675_a(true)), но блок невидим
   // (RenderShape.INVISIBLE, иконка thaumcraft:blank), поэтому в 1.21 этот бокс
   // не воспроизводится: ни форма выделения, ни форма отрисовки пустые.

   public BlockEldritchNothing() {
      // NB: без randomTicks — у блока нет tick(), а пустые планирования тиков
      // на тысячах блоков лабиринта это зря потраченные итерации планировщика.
      super(Properties.of().strength(-1.0F, 6000000.0F).sound(SoundType.WOOL).lightLevel(s -> 3).noOcclusion());
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(EXPOSED, Boolean.FALSE));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{EXPOSED});
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
      if (!level.isClientSide) {
      }

      super.onPlace(state, level, pos, oldState, isMoving);
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if (!level.isClientSide) {
      }

      super.neighborChanged(state, level, pos, block, fromPos, isMoving);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (level instanceof Level l && !l.isClientSide) {
         boolean exposed = BlockUtils.isBlockExposed(l, pos);
         return (BlockState)state.setValue(EXPOSED, exposed);
      } else {
         return state;
      }
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.INVISIBLE;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   /**
    * В 1.7.10 {@code func_149633_g} (getCollisionBoundingBox) возвращает пустой
    * бокс — блок «Portal to Nothing» полностью проходим. Проверено: в оригинале
    * нет ни коллизии, ни бокса выделения; урон наносится из
    * {@link #entityInside}. Если вернуть здесь коллизию, весь лабиринт встанет
    * непроходимым, потому что палитра VOID (паттерн PAT_CONNECT и стены проходов)
    * состоит почти целиком из этого блока.
    */
   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
      return ItemStack.EMPTY;
   }

   /**
    * Порог оригинала: {@code entity.ticksExisted > 20} (секунда внутри блока),
    * 8 урона магией, минуя броню. Источник — {@code DamageSource.field_76380_i},
    * то есть ванильный magic: тем же источником бьёт аура-узел ({@code TileNode})
    * и от него же защищается голем.
    *
    * <p>Оригинал проверяет {@code capabilities.allowFlying}; в 1.21 эквивалент —
    * творческий режим, как и в портированном {@code AuraNodeBlockEntity}.
    */
   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      if (!level.isClientSide && entity.tickCount > 20) {
         if (!(entity instanceof Player player && player.isCreative())) {
            entity.hurt(level.damageSources().magic(), 8.0F);
         }
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return state.getValue(EXPOSED) ? new EldritchNothingBlockEntity(pos, state) : null;
   }
}
