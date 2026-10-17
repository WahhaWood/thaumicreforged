package thaumcraft.api.aspects.obj;

import java.util.Objects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.AspectList;

public class DirectBlockAspect extends BlockAspect {
   private final Block block;

   public DirectBlockAspect(Block block, AspectList aspects) {
      super(aspects);
      this.block = block;
   }

   @Override
   public boolean matches(BlockState state) {
      return state != null && state.is(this.block);
   }

   @Override
   public int matchPriority(BlockState state) {
      return this.matches(state) ? 100 : Integer.MIN_VALUE;
   }

   @Override
   public String getScanKey(BlockState state) {
      return ObjectAspectUtils.getBlockKey(this.block);
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof DirectBlockAspect that ? this.block == that.block : super.equals(o);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.block);
   }
}
