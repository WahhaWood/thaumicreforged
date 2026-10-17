package thaumcraft.block.eldritch.trap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ITickable;
import thaumcraft.init.registry.ModBlockEntities;

public class EldritchTrapBlockEntity extends BlockEntity implements ITickable {
   private int count = 20;

   public EldritchTrapBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.ELDRITCH_TRAP.get(), pos, state);
   }

   protected EldritchTrapBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }
}
