package thaumcraft.block.infernal_furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.block.ThaumcraftEntityBlock;

public class InfernalFurnaceBlock extends ThaumcraftEntityBlock {
   public static final IntegerProperty INDEX = IntegerProperty.create("index", 0, 25);
   private static final VoxelShape CORE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
   private static final int CORE_INDEX = 0;
   /** Индекс части, изображающей решётку (отверстие выброса) — соответствует md 10 в 1.7.10. */
   public static final int OPENING_INDEX = 14;
   /**
    * Индексы углов конструкции (12 штук) — при разборке возвращаются незеритовые кирпичи.
    * Схема та же, что в {@code WandManager.arcaneFurnaceIndex}: углы — это позиции, где
    * оба смещения по горизонтали ненулевые.
    */
   private static final java.util.Set<Integer> CORNER_INDICES =
      java.util.Set.of(1, 3, 7, 9, 10, 12, 15, 17, 18, 20, 23, 25);
   /** Индексы рёбер и донца (12 штук) — при разборке возвращается обсидиан. */
   private static final java.util.Set<Integer> EDGE_INDICES =
      java.util.Set.of(2, 4, 5, 6, 8, 11, 13, 16, 19, 21, 22, 24);
   private static final VoxelShape OPENING_WEST = Block.box(0.0, 0.0, 0.0, 8.0, 16.0, 16.0);
   private static final VoxelShape OPENING_EAST = Block.box(8.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape OPENING_NORTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 8.0);
   private static final VoxelShape OPENING_SOUTH = Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0);
   private static final int BLOCK_EVENT_LAVA_PARTICLES = 3;
   private static final int PARTICLE_COUNT = 5;
   private static final int SMOKE_PARTICLE_COUNT = 3;
   private static final float LAVA_DAMAGE = 3.0F;
   private static final int FIRE_DURATION = 10;

   public InfernalFurnaceBlock() {
      // forceSolidOn() раньше делал все состояния полностью «твёрдыми», из-за чего
      // тонкое ядро и половинная решётка теряли свою форму. В 1.7.10 этого эффекта нет:
      // там форма задаётся func_149743_a, а isSideSolid возвращает meta != 0.
      super(Properties.of().noOcclusion().strength(10.0F, 500.0F).lightLevel(state -> 3));
      this.registerDefaultState(
         (BlockState)((BlockState)this.defaultBlockState().setValue(INDEX, 0)).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
      );
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{INDEX, BlockStateProperties.HORIZONTAL_FACING});
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
      return state.getValue(INDEX) == 0 ? CORE_SHAPE : Shapes.block();
   }

   @NotNull
   @Override
   public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
      int idx = (Integer)state.getValue(INDEX);

      if (idx == 0) {
         return CORE_SHAPE;
      }

      if (idx == OPENING_INDEX) {
         return openingShape(level, pos);
      }

      return Shapes.block();
   }

   /**
    * Решётка выброса в 1.7.10 (md 10) занимает половину блока — ту, что примыкает к ядру,
    * а наружу открыта. Половина выбирается по соседу с индексом 0, как в {@code BlockArcaneFurnace}.
    */
   private static VoxelShape openingShape(@NotNull BlockGetter level, @NotNull BlockPos pos) {
      if (isCore(level, pos.west())) {
         return OPENING_WEST;
      }

      if (isCore(level, pos.east())) {
         return OPENING_EAST;
      }

      if (isCore(level, pos.north())) {
         return OPENING_NORTH;
      }

      return OPENING_SOUTH;
   }

   private static boolean isCore(@NotNull BlockGetter level, @NotNull BlockPos pos) {
      BlockState state = level.getBlockState(pos);

      return state.getBlock() instanceof InfernalFurnaceBlock && (Integer)state.getValue(INDEX) == 0;
   }

   /**
    * 1.7.10 isSideSolid = metadata != 0: a face between two solid parts is hidden, the thin core hides nothing.
    * The method is asked once per neighbour, so it has to look at {@code adjacentState}: returning true for
    * every neighbour hid all faces of the 25 solid parts and the assembled furnace turned invisible (1.0.6.0).
    */
   @Override
   protected boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentState, @NotNull Direction direction) {
      return isSolidPart(state) && adjacentState.is(this) && isSolidPart(adjacentState)
         || super.skipRendering(state, adjacentState, direction);
   }

   private static boolean isSolidPart(@NotNull BlockState state) {
      return (Integer)state.getValue(INDEX) != CORE_INDEX;
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      int idx = (Integer)state.getValue(INDEX);
      return (BlockEntity)(idx == 0 ? new InfernalFurnaceBlockEntity(pos, state) : new InfernalFurnaceNozzleBlockEntity(pos, state));
   }

   public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource rand) {
      if ((Integer)state.getValue(INDEX) == 0) {
         BlockState aboveState = level.getBlockState(pos.above());
         if (aboveState.isAir() || !aboveState.isCollisionShapeFullBlock(level, pos.above())) {
            for (int i = 0; i < 3; i++) {
               double x = pos.getX() + rand.nextDouble();
               double y = pos.getY() + 1.0 + rand.nextDouble() * 0.5;
               double z = pos.getZ() + rand.nextDouble();
               level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   public boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
      if (id == 3) {
         if (level.isClientSide) {
            this.spawnLavaParticles(level, pos);
         }

         return true;
      } else if (id == 1) {
         // TC4 BlockArcaneFurnace: the furnace has just been built with the wand.
         if (level.isClientSide) {
            Thaumcraft.setup.blockSparkle(level, pos.getX(), pos.getY(), pos.getZ(), 16736256, 5);
         }

         return true;
      } else {
         return super.triggerEvent(state, level, pos, id, param);
      }
   }

   private void spawnLavaParticles(Level level, BlockPos pos) {
      RandomSource rand = level.random;

      for (int i = 0; i < 5; i++) {
         double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 0.6;
         double y = pos.getY() + 0.5 + rand.nextDouble() * 0.4;
         double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 0.6;
         level.addParticle(ParticleTypes.LAVA, x, y, z, 0.0, 0.0, 0.0);
         level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + rand.nextFloat() * 0.1F, 0.9F + rand.nextFloat() * 0.15F, false);
      }
   }

   /**
    * Если хоть одна часть конструкции исчезла — печь разбирается обратно в исходные блоки.
    * В 1.7.10 это {@code onNeighborChange} + {@code restoreBlocks}; без этого разобранная
    * наполовину печь навсегда остаётся в мире и блокирует стройку.
    *
    * <p>Проверку запускает только ядро — в оригинале оба хука ({@code func_149695_a} и
    * {@code func_149749_a}) стоят под условием {@code metadata == 0}. Это не только
    * точность к оригиналу, но и условие сходимости: {@code restoreBlocks} ставит блоки с
    * {@code UPDATE_ALL}, и если бы реагировала каждая деталь, каждая замена будила бы
    * новую разборку — дерево вызовов ветвится и мир подвисает.
    *
    * <p>Хук зовут только у граней соседней клетки, поэтому само ядро (оно стоит по диагонали
    * к углам) уведомления не получит. Его разбор ловит {@link #onRemove}.
    */
   @Override
   protected void neighborChanged(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
      @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston
   ) {
      if (level.isClientSide() || (Integer)state.getValue(INDEX) != CORE_INDEX) {
         return;
      }

      this.checkIntegrity(level, pos);
   }

   /**
    * Ядро вынули или заменили — печь тут же рассыпается.
    *
    * <p>Именно так же поступает и оригинал: у него условие {@code metadata(x, y, z) == 0}
    * срабатывает и на воздухе, поэтому {@code onNeighborBlockChange} зовёт
    * {@code restoreBlocks} напрямую, без проверки целостности — иначе вынутое ядро,
    * на которое проверка не смотрит, оставило бы печь стоять. Здесь ядру не с кем
    * соседствовать, поэтому ловим момент снятия блока.
    */
   @Override
   public void onRemove(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
      @NotNull BlockState newState, boolean movedByPiston
   ) {
      if (!state.is(newState.getBlock()) && (Integer)state.getValue(INDEX) == CORE_INDEX && !level.isClientSide()) {
         this.restoreBlocks(level, pos);
      }

      super.onRemove(state, level, pos, newState, movedByPiston);
   }

   /** Проверка целостности от ядра: любая недостающая деталь — разборка всей печи. */
   private void checkIntegrity(@NotNull Level level, @NotNull BlockPos core) {
      for (int dy = -1; dy <= 1; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               // Ядро и дыра над ним в проверку не входят (1.7.10: yy != 1 && yy != 0 || zz != 0 || xx != 0).
               if ((dy == 0 || dy == 1) && dx == 0 && dz == 0) {
                  continue;
               }

               if (!level.getBlockState(core.offset(dx, dy, dz)).is(this)) {
                  this.restoreBlocks(level, core);
                  return;
               }
            }
         }
      }
   }

   private void restoreBlocks(@NotNull Level level, @NotNull BlockPos center) {
      // Ядро возвращаем первым и сразу воздухом (в 1.7.10 getItemDropped(0) — Item 0):
      // пока оно живо, setBlock(..., UPDATE_ALL) снова разбудит onNeighborChange.
      BlockState coreState = level.getBlockState(center);

      if (coreState.is(this)) {
         this.replaceWith(level, center, Blocks.AIR.defaultBlockState());
      }

      for (int dy = -1; dy <= 1; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               BlockPos target = center.offset(dx, dy, dz);
               BlockState targetState = level.getBlockState(target);

               if (!targetState.is(this)) {
                  continue;
               }

               int idx = (Integer)targetState.getValue(INDEX);
               BlockState restored;

               if (idx == OPENING_INDEX) {
                  restored = Blocks.IRON_BARS.defaultBlockState();
               } else if (CORNER_INDICES.contains(idx)) {
                  restored = Blocks.NETHER_BRICKS.defaultBlockState();
               } else if (EDGE_INDICES.contains(idx)) {
                  restored = Blocks.OBSIDIAN.defaultBlockState();
               } else {
                  return;
               }

               this.replaceWith(level, target, restored);
            }
         }
      }
   }

   private void replaceWith(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState) {
      BlockState oldState = level.getBlockState(pos);
      level.setBlock(pos, newState, Block.UPDATE_ALL);
      level.sendBlockUpdated(pos, oldState, newState, Block.UPDATE_ALL);
   }

   @Override
   protected void entityInside(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull net.minecraft.world.entity.Entity entity
   ) {
      if ((Integer)state.getValue(INDEX) != 0 || level.isClientSide()) {
         return;
      }

      // Втягивание к центру ядра: 1.7.10 уводит всё, что застряло в углах, внутрь (0.3..0.7).
      double motionX = entity.getDeltaMovement().x;
      double motionZ = entity.getDeltaMovement().z;

      if (entity.getX() < pos.getX() + 0.3) {
         motionX += 1.0E-4;
      }

      if (entity.getX() > pos.getX() + 0.7) {
         motionX -= 1.0E-4;
      }

      if (entity.getZ() < pos.getZ() + 0.3) {
         motionZ += 1.0E-4;
      }

      if (entity.getZ() > pos.getZ() + 0.7) {
         motionZ -= 1.0E-4;
      }

      if (entity instanceof net.minecraft.world.entity.item.ItemEntity item) {
         // Предмет подбирается только когда он реально лежит на земле, а не летит мимо.
         item.setDeltaMovement(0.025, item.getDeltaMovement().y, motionZ);

         if (item.onGround() && !item.getItem().isEmpty()
            && level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity furnace
            && furnace.addItemsToInventory(item.getItem())) {
            item.discard();
         }

         return;
      }

      entity.setDeltaMovement(motionX, entity.getDeltaMovement().y, motionZ);

      if (entity instanceof net.minecraft.world.entity.LivingEntity living && !living.fireImmune()) {
         living.hurt(level.damageSources().lava(), LAVA_DAMAGE);
         living.igniteForSeconds(FIRE_DURATION);
      }
   }

   @Nullable
   @Override
   public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
      @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type
   ) {
      return (lvl, pos, st, be) -> {
         if (be instanceof InfernalFurnaceBlockEntity furnace) {
            if (!lvl.isClientSide()) {
               InfernalFurnaceBlockEntity.serverTick(lvl, pos, st, furnace);
            }
         } else if (be instanceof InfernalFurnaceNozzleBlockEntity nozzle && !lvl.isClientSide()) {
            nozzle.serverTick(lvl, pos, st);
         }
      };
   }

   @Nullable
   private InfernalFurnaceBlockEntity findCoreNearby(Level level, BlockPos center) {
      for (BlockPos checkPos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
         if (level.getBlockEntity(checkPos) instanceof InfernalFurnaceBlockEntity furnace) {
            return furnace;
         }
      }

      return null;
   }
}
