package thaumcraft.api.aspects.obj;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;

public class BlockAspect {
   public final AspectList aspects;

   public BlockAspect(AspectList aspects) {
      this.aspects = aspects;
   }

   public boolean matches(BlockState state) {
      return false;
   }

   public int matchPriority(BlockState state) {
      return this.matches(state) ? 0 : Integer.MIN_VALUE;
   }

   public AspectList getAspects(BlockState state) {
      return this.aspects.copy();
   }

   @Nullable
   public String getScanKey(BlockState state) {
      return null;
   }
}
