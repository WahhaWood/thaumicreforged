package thaumcraft.block.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import thaumcraft.particles.fx.particles.FXWisp;

public class BlockCustomPlant extends BushBlock {
   public static final MapCodec<BlockCustomPlant> CODEC = simpleCodec(props -> new BlockCustomPlant(BlockCustomPlant.PlantKind.SHIMMER_LEAF));
   private static final VoxelShape SHAPE = box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);
   private final BlockCustomPlant.PlantKind kind;

   public BlockCustomPlant(BlockCustomPlant.PlantKind kind) {
      super(Properties.of().mapColor(MapColor.PLANT).sound(SoundType.GRASS).noCollission().instabreak().lightLevel(s -> kind.light));
      this.kind = kind;
   }

   protected boolean mayPlaceOn(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
      return true;
   }

   protected MapCodec<? extends BushBlock> codec() {
      return CODEC;
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
      return SHAPE;
   }

   public void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
      if (this.kind == BlockCustomPlant.PlantKind.VISHROOM && entity instanceof LivingEntity le && !level.isClientSide) {
         boolean var10000 = false;
      }

      super.entityInside(state, level, pos, entity);
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
      if (level.isClientSide) {
         if (this.kind == BlockCustomPlant.PlantKind.CINDER_PEARL) {
            if (random.nextBoolean()) {
               double x = pos.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.1;
               double y = pos.getY() + 0.6 + (random.nextFloat() - random.nextFloat()) * 0.1;
               double z = pos.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.1;
               level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
               level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
            }
         } else if (this.kind == BlockCustomPlant.PlantKind.SHIMMER_LEAF) {
            if (random.nextInt(3) == 0) {
               float cr = 0.3F + random.nextFloat() * 0.3F;
               float cg = 0.7F + random.nextFloat() * 0.3F;
               float cb = 0.7F + random.nextFloat() * 0.3F;
               double x = pos.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.1;
               double y = pos.getY() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.15;
               double z = pos.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.1;
               FXWisp ef = new FXWisp((ClientLevel)level, x, y, z, 0.2F, cr, cg, cb);
               ef.tinkle = false;
               Minecraft.getInstance().particleEngine.add(ef);
            }
         } else {
            if (this.kind == BlockCustomPlant.PlantKind.VISHROOM && random.nextInt(3) == 0) {
               double x = pos.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.4;
               double y = pos.getY() + 0.3;
               double z = pos.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.4;
               FXWisp ef = new FXWisp((ClientLevel)level, x, y, z, 0.1F, 0.5F, 0.3F, 0.8F);
               ef.tinkle = false;
               ef.shrink = true;
               ef.setGravity(0.015F);
               Minecraft.getInstance().particleEngine.add(ef);
            }
         }
      }
   }

   public int getFlammability(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction face) {
      return 100;
   }

   public int getFireSpreadSpeed(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction face) {
      return 60;
   }

   public static enum PlantKind {
      SHIMMER_LEAF(0),
      CINDER_PEARL(0),
      ETHEREAL_BLOOM(15),
      VISHROOM(0);

      final int light;

      private PlantKind(int light) {
         this.light = light;
      }
   }
}
