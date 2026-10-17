package thaumcraft.block.airy.nitor;

import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.particles.fx.ParticleUtils;
import thaumcraft.particles.fx.particles.FXSparkle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class NitorBlock extends TickableEntityBlock {
   /** Цвет нитора как в TC4: янтарный (245,180,32). */
   public static final float NITOR_R = 0.96F;
   public static final float NITOR_G = 0.71F;
   public static final float NITOR_B = 0.13F;
   private static final VoxelShape SMALL = box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);

   public NitorBlock() {
      super(Properties.of().sound(SoundType.WOOL).noCollission().noOcclusion().lightLevel(s -> 15));
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
      return SMALL;
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull Builder builder) {
      return List.of(new ItemStack(this));
   }


   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new NitorBlockEntity(pos, state);
   }
}
