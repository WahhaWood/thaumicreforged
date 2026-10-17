package thaumcraft.block.advanced_alchemy_furnace;

import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.ClientSetup;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.particles.fx.particles.FXSlimyBubble;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class AdvAlchemyFurnaceBlock extends TickableEntityBlock {
   public static final EnumProperty<AdvAlchemyFurnaceBlock.Element> ELEMENT = EnumProperty.create("element", AdvAlchemyFurnaceBlock.Element.class);
   public static final VoxelShape INSIDE = box(0.0, 0.0, 0.0, 16.0, 15.9F, 16.0);

   public AdvAlchemyFurnaceBlock() {
      super(Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.0F, 17.0F));
      this.registerDefaultState(
         (BlockState)((BlockState)this.defaultBlockState().setValue(ELEMENT, AdvAlchemyFurnaceBlock.Element.BASE))
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
      );
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return INSIDE;
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ELEMENT, BlockStateProperties.HORIZONTAL_FACING});
   }

   public int getLightEmission(BlockState state, BlockGetter getter, BlockPos pos) {
      AdvAlchemyFurnaceBlockEntity entity = this.getCore(state, getter, pos);
      return entity != null && entity.heat > 100 ? (int)((float)entity.heat / entity.maxPower * 12.0F) : super.getLightEmission(state, getter, pos);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return (BlockEntity)(switch ((AdvAlchemyFurnaceBlock.Element)blockState.getValue(ELEMENT)) {
         case BASE -> null;
         case VALVE -> new AdvAlchemyFurnaceNozzleBlockEntity(blockPos, blockState);
         case CENTER -> new AdvAlchemyFurnaceBlockEntity(blockPos, blockState);
      });
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
      if (level.getBlockEntity(pos) instanceof AdvAlchemyFurnaceBlockEntity tile && tile.vis > 0) {
         double x = pos.getX();
         double y = pos.getY();
         double z = pos.getZ();
         FXSlimyBubble ef = new FXSlimyBubble((ClientLevel)level, x + rand.nextFloat(), y + 1.0, z + rand.nextFloat(), 0.06F + rand.nextFloat() * 0.06F);
         ef.setAlpha(0.8F);
         ef.setColor(0.6F - rand.nextFloat() * 0.2F, 0.0F, 0.6F + rand.nextFloat() * 0.2F);
         ClientSetup.add(ef);
         if (rand.nextInt(50) == 0) {
            double var21 = x + rand.nextFloat();
            double var22 = y + 1.0;
            double var23 = z + rand.nextFloat();
            level.playLocalSound(
               var21, var22, var23, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + rand.nextFloat() * 0.1F, 0.9F + rand.nextFloat() * 0.15F, false
            );
         }

         int q = rand.nextInt(2);
         int w = rand.nextInt(2);
         FXSlimyBubble ef2 = new FXSlimyBubble(
            (ClientLevel)level, x - 0.6 + rand.nextFloat() * 0.2 + q * 2, y + 2.0, z - 0.6 + rand.nextFloat() * 0.2 + w * 2, 0.06F + rand.nextFloat() * 0.06F
         );
         ef2.setAlpha(0.8F);
         ef2.setColor(0.6F - rand.nextFloat() * 0.2F, 0.0F, 0.6F + rand.nextFloat() * 0.2F);
         ClientSetup.add(ef2);
      }

      super.animateTick(state, level, pos, rand);
   }

   @Override
   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState newState, boolean movedByPiston) {
      // Ядро вынули или заменили — печь тут же рассыпается. Условие на ALCHEMY_FURNACE
      // отсекает собственное восстановление из restoreParts: там ядро тоже снимается,
      // и без этой проверки разбор входил бы в себя рекурсивно.
      if (!state.is(newState.getBlock())
         && state.getValue(ELEMENT) == Element.CENTER
         && !newState.is(ModBlocks.ALCHEMY_FURNACE.get())
         && !level.isClientSide()) {
         restoreParts(level, blockPos);
      }

      super.onRemove(state, level, blockPos, newState, movedByPiston);
   }

   /**
    * Разборка конструкции.
    *
    * <p>В оригинале это {@code BlockAlchemyFurnace.func_149749_a} (onNeighborBlockChange), и там
    * ровно две ветки: у ядра ({@code md == 0}) — сразу восстановить детали; у любой другой
    * детали — найти ядро в 3×3×3 и поставить ему {@code tile.destroy = true}, чтобы разбор
    * завершился на следующем тике. Отсюда и асимметрия: над ядром стоят топки и баки, они с ним
    * не соседствуют гранями, поэтому сообщение о их поломке приходит деталям снизу.
    */
   @Override
   protected void neighborChanged(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
      @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston
   ) {
      if (level.isClientSide()) {
         return;
      }

      if (state.getValue(ELEMENT) == Element.CENTER) {
         checkIntegrity(level, pos);
         return;
      }

      // Остальные детали только сообщают ядру о поломке и ждут тика. Отложенная
      // проверка нужна ещё и потому, что над ядром стоят топки и баки: они с ним
      // гранями не соседствуют, и сломанную верхушку замечает только деталь под ней.
      BlockPos core = findCore(level, pos);

      if (core != null && level.getBlockEntity(core) instanceof AdvAlchemyFurnaceBlockEntity tile) {
         tile.requestDismantle();
      }
   }

   /**
    * Проверка целостности: любая недостающая деталь — разборка всей печи.
    *
    * <p>Проверять состав обязательно до разбора, а не разбирать сразу: жезл ставит
    * детали по одной, и каждая постановка будит соседей. Без проверки первая же
    * вставленная деталь видела бы «неполную» печь и рассыпала её прямо во время сборки.
    */
   static void checkIntegrity(@NotNull Level level, @NotNull BlockPos core) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = 0; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx == 0 && dz == 0) {
                  continue;
               }

               if (!isFurnacePart(level.getBlockState(core.offset(dx, dy, dz)))) {
                  restoreParts(level, core);
                  return;
               }
            }
         }
      }
   }

   /** Ядро печи в радиусе 1 от детали — оригинал сканирует те же 3×3×3. */
   @Nullable
   private static BlockPos findCore(@NotNull Level level, @NotNull BlockPos part) {
      for (BlockPos at : BlockPos.betweenClosed(part.offset(-1, -1, -1), part.offset(1, 1, 1))) {
         BlockState atState = level.getBlockState(at);

         if (atState.getBlock() instanceof AdvAlchemyFurnaceBlock && atState.getValue(ELEMENT) == Element.CENTER) {
            return at;
         }
      }

      return null;
   }

   /**
    * Возвращает печь в исходные блоки — ровно ту раскладку, которую
    * {@code WandManager.fitAdvancedAlchemicalFurnace} проверяет перед сборкой:
    * ядро → ALCHEMY_FURNACE, нижние детали → ADVANCED_ALCHEMICAL_CONSTRUCT,
    * топки → ALCHEMICAL_CONSTRUCT, баки → ARCANE_ALEMBIC.
    *
    * <p>Ядро убирается первым и сразу в печь: пока оно стоит, любая замена с
    * {@code UPDATE_ALL} снова разбудит проверку у соседей.
    */
   static void restoreParts(@NotNull Level level, @NotNull BlockPos center) {
      if (isFurnacePart(level.getBlockState(center))) {
         replaceWith(level, center, ModBlocks.ALCHEMY_FURNACE.get().defaultBlockState());
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = 0; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx == 0 && dz == 0) {
                  continue;
               }

               BlockPos at = center.offset(dx, dy, dz);
               BlockState restored = originalOf(level.getBlockState(at));

               if (restored != null) {
                  replaceWith(level, at, restored);
               }
            }
         }
      }
   }

   /** Блок, в который деталь возвращается при разборке; {@code null} — деталь не наша. */
   @Nullable
   private static BlockState originalOf(@NotNull BlockState state) {
      Block block = state.getBlock();

      if (block instanceof AdvAlchemyFurnaceBurnBlock) {
         return ModBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState();
      }

      if (block instanceof AdvAlchemyFurnaceTankBlock) {
         return ModBlocks.ARCANE_ALEMBIC.get().defaultBlockState();
      }

      if (block instanceof AdvAlchemyFurnaceBlock) {
         return switch ((Element)state.getValue(ELEMENT)) {
            case CENTER -> ModBlocks.ALCHEMY_FURNACE.get().defaultBlockState();
            case VALVE, BASE -> ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get().defaultBlockState();
         };
      }

      return null;
   }

   private static boolean isFurnacePart(@NotNull BlockState state) {
      Block block = state.getBlock();

      return block instanceof AdvAlchemyFurnaceBlock
         || block instanceof AdvAlchemyFurnaceBurnBlock
         || block instanceof AdvAlchemyFurnaceTankBlock;
   }

   private static void replaceWith(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState) {
      BlockState oldState = level.getBlockState(pos);
      level.setBlock(pos, newState, Block.UPDATE_ALL);
      level.sendBlockUpdated(pos, oldState, newState, Block.UPDATE_ALL);
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      return List.of(new ItemStack(switch ((AdvAlchemyFurnaceBlock.Element)state.getValue(ELEMENT)) {
         case BASE, VALVE -> (BlockItem)ModItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get();
         case CENTER -> (BlockItem)ModItems.ALCHEMY_FURNACE.get();
      }));
   }

   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
      return new ItemStack(switch ((AdvAlchemyFurnaceBlock.Element)state.getValue(ELEMENT)) {
         case BASE, VALVE -> (BlockItem)ModItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get();
         case CENTER -> (BlockItem)ModItems.ALCHEMY_FURNACE.get();
      });
   }

   private AdvAlchemyFurnaceBlockEntity getCore(BlockState state, BlockGetter getter, BlockPos pos) {
      Direction direction = (Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING);
      AdvAlchemyFurnaceBlock.Element element = (AdvAlchemyFurnaceBlock.Element)state.getValue(ELEMENT);
      if (element == AdvAlchemyFurnaceBlock.Element.BASE) {
         pos = pos.relative(direction).relative(direction.getCounterClockWise());
      } else if (element == AdvAlchemyFurnaceBlock.Element.VALVE) {
         pos = pos.relative(direction);
      }

      return getter.getBlockEntity(pos) instanceof AdvAlchemyFurnaceBlockEntity advanced ? advanced : null;
   }

   public static enum Element implements StringRepresentable {
      BASE,
      VALVE,
      CENTER;

      @NotNull
      public String getSerializedName() {
         return this.name().toLowerCase();
      }
   }
}
