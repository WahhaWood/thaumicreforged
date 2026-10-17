package thaumcraft.block.eldritch.crab_spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.init.registry.ModBlockEntities;

public class EldritchCrabSpawnerBlockEntity extends BlockEntity implements ITickable {
   private static final int MAX_ENTITIES = 5;
   private static final int SPAWN_RANGE = 32;
   private static final int ACTIVATION_RANGE = 16;
   public static final ResourceLocation SOUND_EXTINGUISH = ResourceLocation.fromNamespaceAndPath("minecraft", "block.fire.extinguish");
   public static final ResourceLocation SOUND_GORE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "gore");
   public int count = 150;
   private int venting = 0;

   public EldritchCrabSpawnerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.ELDRITCH_CRAB_SPAWNER.get(), pos, state);
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      return new AABB(this.worldPosition).inflate(1.0);
   }

   protected void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.count = tag.getInt("count");
   }

   protected void saveAdditional(CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      tag.putInt("count", this.count);
   }

   /** Серверная логика: спавн крабов при игроке рядом (порт TileEldritchCrabSpawner). */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      this.count--;
      // Дорогие сканы сущностей (игроки в 16, крабы в 32 блоках) — только когда
      // счётчик почти дотикал: раньше они гонялись каждый тик на каждом
      // спавнере лабиринта, хотя спавн возможен лишь в конце отсчёта.
      if (this.count > 16) {
         return;
      }

      if (!this.isActivated(level, pos) || this.maxEntitiesReached(level, pos)) {
         return;
      }

      if (this.count == 15) {
         level.blockEvent(pos, state.getBlock(), 1, 0);
         level.playSound(null, pos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.0F);
      } else if (this.count <= 0) {
         this.count = 150 + level.random.nextInt(100);
         this.spawnCrab(level, pos);
         level.playSound(null, pos, thaumcraft.init.registry.ModSounds.GORE.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.0F);
      }
   }

   private boolean isActivated(Level level, BlockPos pos) {
      return !level.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, new AABB(pos).inflate(16.0)).isEmpty();
   }

   private boolean maxEntitiesReached(Level level, BlockPos pos) {
      return level.getEntitiesOfClass(
         thaumcraft.entities.smart.edlritch_crab.EntityEldritchCrab.class, new AABB(pos).inflate(32.0)
      ).size() >= MAX_ENTITIES;
   }

   private void spawnCrab(Level level, BlockPos pos) {
      thaumcraft.entities.smart.edlritch_crab.EntityEldritchCrab crab =
         thaumcraft.init.registry.ModEntities.ELDRITCH_CRAB.get().create(level);
      if (crab == null) {
         return;
      }

      Direction dir = this.getFacingDirection(level.getBlockState(pos));
      double x = pos.getX() + 0.5 + dir.getStepX() * 1.2;
      double y = pos.getY() + 0.5 + dir.getStepY() * 1.2;
      double z = pos.getZ() + 0.5 + dir.getStepZ() * 1.2;
      crab.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
      level.addFreshEntity(crab);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (this.venting > 0) {
         this.venting--;

         for (int a = 0; a < 3; a++) {
            this.drawVent(level, pos, state);
         }
      } else if (level.getRandom().nextInt(20) == 0) {
         this.drawVent(level, pos, state);
      }
   }

   public boolean triggerEvent(int id, int type) {
      if (id == 1) {
         this.venting = 20;
         return true;
      } else {
         return super.triggerEvent(id, type);
      }
   }

   private void drawVent(Level level, BlockPos pos, BlockState state) {
      Direction dir = this.getFacingDirection(state);
      RandomSource rand = level.getRandom();
      double xBase = pos.getX() + 0.5 + dir.getStepX() * 0.55;
      double yBase = pos.getY() + 0.5 + dir.getStepY() * 0.55;
      double zBase = pos.getZ() + 0.5 + dir.getStepZ() * 0.55;
      float spread = 0.15F;
      Thaumcraft.setup
         .drawVentParticles(
            level,
            xBase + (rand.nextFloat() - 0.5) * spread,
            yBase + (rand.nextFloat() - 0.5) * spread,
            zBase + (rand.nextFloat() - 0.5) * spread,
            dir.getStepX() * 0.1F + rand.nextFloat() * 0.05F,
            dir.getStepY() * 0.1F + rand.nextFloat() * 0.05F,
            dir.getStepZ() * 0.1F + rand.nextFloat() * 0.05F,
            10061994,
            2.0F
         );
   }

   private Direction getFacingDirection(BlockState state) {
      return state.hasProperty(BlockStateProperties.FACING) ? (Direction)state.getValue(BlockStateProperties.FACING) : Direction.UP;
   }
}
