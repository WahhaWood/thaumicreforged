package thaumcraft.block.warded;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.Config;

public class WardedBlock extends Block implements EntityBlock {
   public WardedBlock() {
      super(Properties.of().strength(Config.wardedStone ? -1.0F : 5.0F, 999.0F).sound(SoundType.STONE).noLootTable());
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new WardedBlockEntity(pos, state);
   }

   private BlockState mimic(BlockGetter level, BlockPos pos) {
      return level.getBlockEntity(pos) instanceof WardedBlockEntity tw && tw.getMimic() != null ? tw.getMimic() : Blocks.STONE.defaultBlockState();
   }

   public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
      return level.getBlockEntity(pos) instanceof WardedBlockEntity tw ? tw.getLight() : 0;
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

   public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
      return Config.disableTransparentForWardedBlocks ? 255 : this.mimic(level, pos).getLightBlock(level, pos);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return this.mimic(level, pos).getShape(level, pos, ctx);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return this.mimic(level, pos).getCollisionShape(level, pos, ctx);
   }

   public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return this.mimic(level, pos).getVisualShape(level, pos, ctx);
   }

   public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
      return Config.disableTransparentForWardedBlocks ? false : this.mimic(level, pos).propagatesSkylightDown(level, pos);
   }

   public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
      return this.mimic(level, pos).getShadeBrightness(level, pos);
   }

   public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
      return this.mimic(level, pos).getSoundType(level, pos, entity);
   }

   public float getFriction(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
      return this.mimic(level, pos).getFriction(level, pos, entity);
   }

   public PushReaction getPistonPushReaction(BlockState state) {
      return PushReaction.BLOCK;
   }

   public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
      return false;
   }

   public boolean isPathfindable(BlockState state, PathComputationType type) {
      return false;
   }

   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
      BlockState mimic = this.mimic(level, pos);
      return mimic.getBlock().asItem().getDefaultInstance();
   }

   public List<ItemStack> getDrops(BlockState state, Builder builder) {
      return Collections.emptyList();
   }
}
