package thaumcraft.block.candle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.api.render.ITIntColor;

public class CandleBlock extends TorchBlock implements IInfusionStabiliser, ITIntColor {
   protected static final VoxelShape INSIDE = Block.box(6.0, 0.0, 6.0, 10.0, 8.0, 10.0);
   private final DyeColor color;

   public CandleBlock(DyeColor color) {
      super(
         ParticleTypes.FLAME,
         Properties.of().mapColor(MapColor.WOOL).sound(SoundType.WOOL).strength(0.1F).lightLevel(value -> 14).pushReaction(PushReaction.DESTROY)
      );
      this.color = color;
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return INSIDE;
   }

   @Override
   public int getColor(int tint) {
      return tint == 0 ? this.color.getTextColor() : -1;
   }
}
