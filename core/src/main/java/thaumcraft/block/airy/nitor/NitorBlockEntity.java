package thaumcraft.block.airy.nitor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class NitorBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   public NitorBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.NITOR.get(), blockPos, blockState);
   }

   @Override
   @net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (!(level instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel)) {
         return;
      }

      int pc2 = Thaumcraft.setup.particleCount(2);
      int pc4 = Thaumcraft.setup.particleCount(4);
      int chanceA = Math.max(1, 9 - pc2);
      int chanceB = Math.max(1, 15 - pc4);
      double cx = this.worldPosition.getX() + 0.5;
      double cy = this.worldPosition.getY() + 0.5;
      double cz = this.worldPosition.getZ() + 0.5;
      float r = thaumcraft.block.airy.nitor.NitorBlock.NITOR_R;
      float g = thaumcraft.block.airy.nitor.NitorBlock.NITOR_G;
      float b = thaumcraft.block.airy.nitor.NitorBlock.NITOR_B;
      if (clientLevel.random.nextInt(chanceA) == 0) {
         thaumcraft.particles.fx.ParticleUtils.addEffect(new thaumcraft.particles.fx.particles.FXWisp(
            clientLevel,
            cx, cy, cz,
            this.worldPosition.getX() + 0.3 + clientLevel.random.nextFloat() * 0.4,
            this.worldPosition.getY() + 0.5,
            this.worldPosition.getZ() + 0.3 + clientLevel.random.nextFloat() * 0.4,
            0.5F, r, g, b
         ));
      }

      if (clientLevel.random.nextInt(chanceB) == 0) {
         thaumcraft.particles.fx.ParticleUtils.addEffect(new thaumcraft.particles.fx.particles.FXWisp(
            clientLevel,
            cx, cy, cz,
            this.worldPosition.getX() + 0.4 + clientLevel.random.nextFloat() * 0.2,
            this.worldPosition.getY() + 0.5,
            this.worldPosition.getZ() + 0.4 + clientLevel.random.nextFloat() * 0.2,
            0.25F, r, g, b
         ));
      }
   }
}
