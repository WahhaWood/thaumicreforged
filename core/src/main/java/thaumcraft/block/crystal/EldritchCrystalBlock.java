package thaumcraft.block.crystal;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.particles.fx.particles.FXSpark;

public class EldritchCrystalBlock extends CrystalClusterBlock {
   @OnlyIn(Dist.CLIENT)
   public void animateTick(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull RandomSource random) {
      if (random.nextInt(17) == 0) {
         double x = blockPos.getX() + 0.3 + random.nextFloat() * 0.4F;
         double y = blockPos.getY() + 0.3 + random.nextFloat() * 0.4F;
         double z = blockPos.getZ() + 0.3 + random.nextFloat() * 0.4F;
         float param = 0.2F + random.nextFloat() * 0.1F;
         if (level instanceof ClientLevel clientLevel) {
            FXSpark ef = new FXSpark(clientLevel, x, y, z, param);
            Color c = new Color(this.getColor());
            ef.setColor(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F);
            Minecraft.getInstance().particleEngine.add(ef);
         }
      }
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new EldritchCrystalBlock.EldritchCrystalBlockEntity(blockPos, blockState);
   }

   public static class EldritchCrystalBlockEntity extends ThaumcraftBlockEntity {
      public EldritchCrystalBlockEntity(BlockPos blockPos, BlockState blockState) {
         super((BlockEntityType<?>)ModBlockEntities.STRANGE_CRYSTALS.get(), blockPos, blockState);
      }
   }
}
