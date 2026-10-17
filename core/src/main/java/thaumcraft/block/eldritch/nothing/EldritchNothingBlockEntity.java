package thaumcraft.block.eldritch.nothing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.init.registry.ModBlockEntities;

public class EldritchNothingBlockEntity extends BlockEntity {
   public EldritchNothingBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.ELDRITCH_NOTHING.get(), pos, state);
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      return new AABB(
         this.worldPosition.getX(),
         this.worldPosition.getY(),
         this.worldPosition.getZ(),
         this.worldPosition.getX() + 1.0,
         this.worldPosition.getY() + 1.0,
         this.worldPosition.getZ() + 1.0
      );
   }
}
