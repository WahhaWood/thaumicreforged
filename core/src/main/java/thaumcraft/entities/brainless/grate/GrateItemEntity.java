package thaumcraft.entities.brainless.grate;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModEntities;

public class GrateItemEntity extends ItemEntity {
   public GrateItemEntity(EntityType<? extends ItemEntity> entityType, Level level) {
      super(entityType, level);
   }

   public GrateItemEntity(Level level, Vec3 vec3, ItemStack stack) {
      super((EntityType)ModEntities.GRATE_ITEM.get(), level);
      this.setPos(vec3.x, vec3.y, vec3.z);
      this.setDeltaMovement(level.random.nextDouble() * 0.2 - 0.1, 0.2, level.random.nextDouble() * 0.2 - 0.1);
      this.setItem(stack);
      this.lifespan = stack.getEntityLifespan(level);
   }

   public boolean shouldRender(double x, double y, double z) {
      BlockPos blockPos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
      BlockState blockState = this.level().getBlockState(blockPos);
      return blockState.is((Block)ModBlocks.GRATE.get()) ? true : super.shouldRender(x, y, z);
   }
}
