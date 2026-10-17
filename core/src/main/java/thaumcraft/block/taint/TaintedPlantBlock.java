package thaumcraft.block.taint;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public class TaintedPlantBlock extends BushBlock {
   public static final MapCodec<TaintedPlantBlock> CODEC = simpleCodec(props -> new TaintedPlantBlock());
   public static final EnumProperty<TaintedPlantBlock.Variant> VARIANT = EnumProperty.create("variant", TaintedPlantBlock.Variant.class);
   public static final BooleanProperty HAS_SPORE = BooleanProperty.create("has_spore");

   public TaintedPlantBlock() {
      super(Properties.of().mapColor(Blocks.FERN.defaultMapColor()).sound(SoundType.GRASS).noCollission().randomTicks());
      this.registerDefaultState(
         (BlockState)((BlockState)this.defaultBlockState().setValue(VARIANT, TaintedPlantBlock.Variant.GRASS1)).setValue(HAS_SPORE, false)
      );
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> b) {
      b.add(new Property[]{VARIANT, HAS_SPORE});
   }

   protected MapCodec<? extends BushBlock> codec() {
      return CODEC;
   }

   public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
      TaintedPlantBlock.Variant v = (TaintedPlantBlock.Variant)state.getValue(VARIANT);
      if (v == TaintedPlantBlock.Variant.GRASS2) {
         return 8;
      } else {
         return v == TaintedPlantBlock.Variant.SPORE_STALK && state.getValue(HAS_SPORE) ? 10 : 0;
      }
   }

   private static Class<? extends Entity> entClass() {
      return Entity.class;
   }

   public static enum Variant implements StringRepresentable {
      GRASS1("grass1"),
      GRASS2("grass2"),
      SPORE_STALK("spore_stalk");

      private final String id;

      private Variant(String id) {
         this.id = id;
      }

      public String getSerializedName() {
         return this.id;
      }
   }
}
