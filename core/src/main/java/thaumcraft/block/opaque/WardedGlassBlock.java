package thaumcraft.block.opaque;

import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.Config;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class WardedGlassBlock extends BaseEntityBlock {
   public static final MapCodec<WardedGlassBlock> CODEC = simpleCodec(props -> new WardedGlassBlock());

   public WardedGlassBlock() {
      super(
         Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(Config.wardedStone ? -1.0F : 5.0F, 999.0F)
            .sound(SoundType.STONE)
            .noOcclusion()
            .isSuffocating((s, l, p) -> false)
            .isViewBlocking((s, l, p) -> false)
            .lightLevel(value -> 15)
      );
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @NotNull
   @OnlyIn(Dist.CLIENT)
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
      return adjacentState.getBlock() == this ? true : super.skipRendering(state, adjacentState, side);
   }

   public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
      return false;
   }

   public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
      consumer.accept(new IClientBlockExtensions() {
         public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine engine) {
            if (target.getType() == Type.BLOCK && target instanceof BlockHitResult blockTarget) {
               BlockPos pos = blockTarget.getBlockPos();
               float f = (float)(blockTarget.getLocation().x - pos.getX());
               float f1 = (float)(blockTarget.getLocation().y - pos.getY());
               float f2 = (float)(blockTarget.getLocation().z - pos.getZ());
               Thaumcraft.setup.blockWard(level, pos.getX(), pos.getY(), pos.getZ(), blockTarget.getDirection(), f, f1, f2);
            }

            return false;
         }
      });
   }

   public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
   }

   public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
      return false;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
      super.onPlace(state, level, pos, oldState, isMoving);
      markCtmDirty(level, pos);
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      markCtmDirty(level, pos);
      super.onRemove(state, level, pos, newState, isMoving);
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      super.neighborChanged(state, level, pos, block, fromPos, isMoving);
      markCtmDirty(level, pos);
   }

   static void markCtmDirty(Level level, BlockPos center) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               markOne(level, center.offset(dx, dy, dz));
            }
         }
      }
   }

   private static void markOne(Level level, BlockPos pos) {
      if (level.hasChunkAt(pos)) {
         BlockState st = level.getBlockState(pos);
         if (st.getBlock() instanceof WardedGlassBlock) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
               be.requestModelDataUpdate();
            }

            level.sendBlockUpdated(pos, st, st, 2);
         }
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new OwnedBlockEntity(pos, state);
   }
}
