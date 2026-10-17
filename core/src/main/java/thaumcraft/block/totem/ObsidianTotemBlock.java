package thaumcraft.block.totem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModBlockTags;

public class ObsidianTotemBlock extends Block {
   public static final IntegerProperty PHASE = IntegerProperty.create("phase", 0, 3);
   public static final EnumProperty<ObsidianTotemBlock.Style> STYLE = EnumProperty.create("style", ObsidianTotemBlock.Style.class);

   public ObsidianTotemBlock(Properties props) {
      super(props.strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.STONE));
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(PHASE, 0)).setValue(STYLE, ObsidianTotemBlock.Style.BASE)
      );
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{PHASE, STYLE});
   }

   public RenderShape getRenderShape(BlockState shape) {
      return RenderShape.MODEL;
   }

   public BlockState getStateForPlacement(@NotNull BlockPlaceContext ctx) {
      BlockPos pos = ctx.getClickedPos();
      int phase = computePhase(pos);
      ObsidianTotemBlock.Style style = computeStyle(ctx.getLevel(), pos);
      return (BlockState)((BlockState)this.defaultBlockState().setValue(PHASE, phase)).setValue(STYLE, style);
   }

   @NotNull
   public BlockState updateShape(
      @NotNull BlockState state,
      @NotNull Direction dir,
      @NotNull BlockState neighborState,
      @NotNull LevelAccessor level,
      @NotNull BlockPos pos,
      @NotNull BlockPos neighborPos
   ) {
      if (dir == Direction.UP || dir == Direction.DOWN) {
         ObsidianTotemBlock.Style newStyle = computeStyle(level, pos);
         if (newStyle != state.getValue(STYLE)) {
            return (BlockState)state.setValue(STYLE, newStyle);
         }
      }

      return state;
   }

   private static int computePhase(BlockPos pos) {
      return Math.floorMod(pos.getX() + pos.getY() + pos.getZ(), 4);
   }

   private static ObsidianTotemBlock.Style computeStyle(LevelAccessor level, BlockPos pos) {
      boolean up = isTotem(level.getBlockState(pos.above()));
      boolean down = isTotem(level.getBlockState(pos.below()));
      if (up) {
         return ObsidianTotemBlock.Style.SHADED;
      } else {
         return !down ? ObsidianTotemBlock.Style.BASE : ObsidianTotemBlock.Style.CARVED;
      }
   }

   private static boolean isTotem(BlockState state) {
      return state.is(ModBlockTags.OBSIDIAN_TOTEMS);
   }

   public static enum Style implements StringRepresentable {
      BASE("base"),
      SHADED("shaded"),
      CARVED("carved");

      private final String id;

      private Style(String id) {
         this.id = id;
      }

      public String getSerializedName() {
         return this.id;
      }
   }
}
