package thaumcraft.block.magnet;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class MagnetBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   public MagnetBlockEntity(BlockPos pos, BlockState state) {
      this((BlockEntityType<?>)ModBlockEntities.MAGNET.get(), pos, state);
   }

   protected MagnetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      int signal = level.getBestNeighborSignal(pos);
      if (signal > 0 && level.random.nextInt(4) == 0) {
         boolean pulling = (Boolean)state.getValue(MagnetBlock.PULLING);
         Vec3 center = Vec3.atCenterOf(pos);
         double range = signal / 2.0;
         AABB area = new AABB(center.x - range, pos.getY(), center.z - range, center.x + range, center.y + range, center.z + range);

         for (Entity entity : level.getEntitiesOfClass(Entity.class, area, this.getEntitySelector())) {
            Thaumcraft.setup.sparkle((float)entity.getX(), (float)entity.getY(), (float)entity.getZ(), pulling ? 2 : 4);
         }
      }
   }

   protected Predicate<Entity> getEntitySelector() {
      return entity -> entity instanceof ItemEntity;
   }
}
