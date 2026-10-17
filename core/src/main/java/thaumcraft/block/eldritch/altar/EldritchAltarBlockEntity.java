package thaumcraft.block.eldritch.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.entities.smart.cultist.EntityCultist;
import thaumcraft.entities.smart.cultist.EntityCultistCleric;
import thaumcraft.entities.smart.cultist.EntityCultistKnight;
import thaumcraft.entities.smart.eldrich_guardian.EntityEldritchGuardian;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.dimension.outer_lands.maze.MazeHandler;
import thaumcraft.dimension.outer_lands.maze.MazeThread;
import thaumcraft.init.registry.ModEntities;

public class EldritchAltarBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   private static final int SPAWN_INTERVAL = 40;
   private static final int SPAWN_DELAY = 80;
   private static final double GUARD_SEARCH_RANGE = 24.0;
   private static final int MAX_CULTISTS = 8;
   // Лимит стражей у алтаря: раньше spawnGuardian не проверял количество —
   // алтарь клепал стража каждые 2 с бесконечно (все с persistence), и кольцо
   // заваливалось десятками мобов, стоило задержаться рядом.
   private static final int MAX_GUARDIANS = 4;
   private boolean spawner = false;
   private boolean open = false;
   private boolean spawnedClerics = false;
   private byte spawnType = 0;
   private byte eyes = 0;
   private int tickCount = 0;
   private boolean mazeRequested = false;
   private int mazeW = 0;
   private int mazeH = 0;
   private long mazeSeed = 0L;

   public EldritchAltarBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.ELDRITCH_ALTAR.get(), pos, state);
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.loadAdditional(tag, provider);
      this.spawnedClerics = tag.getBoolean("spawnedClerics");
      this.spawner = tag.getBoolean("spawner");
      this.spawnType = tag.getByte("spawntype");
      this.mazeRequested = tag.getBoolean("mazeRequested");
      this.mazeW = tag.getInt("mazeW");
      this.mazeH = tag.getInt("mazeH");
      this.mazeSeed = tag.getLong("mazeSeed");
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.saveAdditional(tag, provider);
      tag.putBoolean("spawnedClerics", this.spawnedClerics);
      tag.putBoolean("spawner", this.spawner);
      tag.putByte("spawntype", this.spawnType);
      tag.putBoolean("mazeRequested", this.mazeRequested);
      tag.putInt("mazeW", this.mazeW);
      tag.putInt("mazeH", this.mazeH);
      tag.putLong("mazeSeed", this.mazeSeed);
   }

   @Override
   public void loadCustom(CompoundTag tag) {
      this.setEyes(tag.getByte("eyes"));
      this.setOpen(tag.getBoolean("open"));
   }

   @Override
   public void saveCustom(CompoundTag tag) {
      tag.putByte("eyes", this.getEyes());
      tag.putBoolean("open", this.isOpen());
   }


   /** Спавнер алтаря: клирики → рыцари → страж (порт TileEldritchAltar из 1.7.10). */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (!this.spawner) {
         return;
      }

      if (this.tickCount++ < SPAWN_DELAY || this.tickCount % SPAWN_INTERVAL != 0) {
         return;
      }

      if (this.spawnType == 1) {
         this.spawnGuardian(level, pos);
      } else if (!this.spawnedClerics) {
         this.spawnClerics(level, pos);
      } else {
         this.spawnGuards(level, pos);
      }
   }

   private void spawnClerics(Level level, BlockPos pos) {
      int success = 0;
      int[][] offsets = new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};

      for (int[] offset : offsets) {
         BlockPos spawnPos = pos.offset(offset[0], 0, offset[1]);
         EntityCultistCleric cleric = ModEntities.CULTIST_CLERIC.get().create(level);
         if (cleric == null || !this.canSpawnAt(level, spawnPos, cleric)) {
            continue;
         }

         cleric.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
         cleric.setPersistenceRequired();
         cleric.setHomeArea(pos, 8);
         cleric.setIsRitualist(true);
         if (level.addFreshEntity(cleric)) {
            success++;
         }
      }

      if (success > 2) {
         this.spawnedClerics = true;
         this.setChanged();
      }
   }

   private void spawnGuards(Level level, BlockPos pos) {
      AABB area = new AABB(pos).inflate(GUARD_SEARCH_RANGE, 16.0, GUARD_SEARCH_RANGE);
      if (level.getEntitiesOfClass(EntityCultistCleric.class, area).isEmpty()) {
         this.spawner = false;
         this.setChanged();
         return;
      }

      if (level.getEntitiesOfClass(EntityCultist.class, area).size() >= MAX_CULTISTS) {
         return;
      }

      EntityCultistKnight knight = ModEntities.CULTIST_KNIGHT.get().create(level);
      if (knight == null) {
         return;
      }

      BlockPos spawnPos = this.randomSpawnPos(level, pos, 4, 10, 3);
      if (spawnPos != null && this.canSpawnAt(level, spawnPos, knight)) {
         knight.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
         knight.setPersistenceRequired();
         knight.restrictTo(pos, 16);
         level.addFreshEntity(knight);
      }
   }

   private void spawnGuardian(Level level, BlockPos pos) {
      AABB area = new AABB(pos).inflate(GUARD_SEARCH_RANGE, 16.0, GUARD_SEARCH_RANGE);
      if (level.getEntitiesOfClass(EntityEldritchGuardian.class, area).size() >= MAX_GUARDIANS) {
         return;
      }

      EntityEldritchGuardian guardian = ModEntities.ELDRITCH_GUARDIAN.get().create(level);
      if (guardian == null) {
         return;
      }

      BlockPos spawnPos = this.randomSpawnPos(level, pos, 4, 10, 3);
      if (spawnPos != null && this.canSpawnAt(level, spawnPos, guardian)) {
         guardian.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
         guardian.setPersistenceRequired();
         guardian.restrictTo(pos, 16);
         level.addFreshEntity(guardian);
      }
   }

   private BlockPos randomSpawnPos(Level level, BlockPos pos, int min, int max, int maxVertical) {
      int dx = this.randomSigned(level, min, max);
      int dy = this.randomSigned(level, 0, maxVertical);
      int dz = this.randomSigned(level, min, max);
      return pos.offset(dx, dy, dz);
   }

   private int randomSigned(Level level, int min, int max) {
      int value = min + level.random.nextInt(max - min + 1);
      return value * (level.random.nextBoolean() ? 1 : -1);
   }

   private boolean canSpawnAt(Level level, BlockPos spawnPos, Mob entity) {
      BlockPos floor = spawnPos.below();
      if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
         return false;
      }

      entity.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0F, 0.0F);
      return level.noCollision(entity) && !level.containsAnyLiquid(entity.getBoundingBox());
   }

   /** Тикер спавнера (вызывается блоком). */
   public byte getSpawnType() {
      return this.spawnType;
   }

   public void setSpawnType(byte spawnType) {
      this.spawnType = spawnType;
   }

   public byte getEyes() {
      return this.eyes;
   }

   public void setEyes(byte eyes) {
      this.eyes = eyes;
   }

   public boolean isOpen() {
      return this.open;
   }

   public void setOpen(boolean open) {
      this.open = open;
   }

   public boolean isSpawner() {
      return this.spawner;
   }

   public void setSpawner(boolean spawner) {
      this.spawner = spawner;
   }
   /**
    * Порт {@code TileEldritchAltar.checkForMaze}: гарантирует, что вокруг алтаря
    * есть лабиринт нужного размера, и возвращает {@code true}, если он уже готов.
    *
    * <p>Размер подбирается случайно один раз и запоминается вместе с сидом, чтобы
    * повторные попытки (игрок кликает по алтарю до готовности) не заказывали
    * каждый раз новый лабиринт. Пока генерация идёт, возвращается {@code false} —
    * именно на этом держится проверка варп-колеса: переход в Outer Lands
    * блокируется, пока лабиринт не построен.
    *
    * <p>В оригинале {@code w} и {@code h} вычисляются заново на каждом вызове,
    * из-за чего при нескольких кликах заказывалось несколько лабиринтов подряд;
    * здесь это исправлено полями {@code mazeW}/{@code mazeH}, которые в порте
    * уже были заведены под эти данные, но не использовались.
    */
   public boolean checkForMaze() {
      if (this.mazeRequested) {
         int w = this.mazeW;
         int h = this.mazeH;
         return MazeHandler.mazesInRange(this.getBlockPos().getX() >> 4, this.getBlockPos().getZ() >> 4, w, h);
      }

      if (this.getLevel() == null) {
         return false;
      }

      RandomSource random = this.getLevel().getRandom();
      int w = 15 + random.nextInt(8) * 2;
      int h = 15 + random.nextInt(8) * 2;
      long seed = random.nextLong();
      this.mazeW = w;
      this.mazeH = h;
      this.mazeSeed = seed;
      this.mazeRequested = true;
      this.setChanged();

      MazeThread.schedule(this.getBlockPos().getX() >> 4, this.getBlockPos().getZ() >> 4, w, h, seed);
      return false;
   }

   public int getMazeW() {
      return this.mazeW;
   }

   public int getMazeH() {
      return this.mazeH;
   }

   public long getMazeSeed() {
      return this.mazeSeed;
   }

   /** Сброс заказа лабиринта — нужно, если алтарь переставили. */
   public void resetMazeRequest() {
      this.mazeRequested = false;
      this.mazeW = 0;
      this.mazeH = 0;
      this.mazeSeed = 0L;
      this.setChanged();
   }

}
