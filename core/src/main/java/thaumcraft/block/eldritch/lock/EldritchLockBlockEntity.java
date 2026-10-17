package thaumcraft.block.eldritch.lock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import thaumcraft.dimension.outer_lands.maze.CellLoc;
import thaumcraft.dimension.outer_lands.data.BossMapData;
import thaumcraft.dimension.outer_lands.maze.Cell;
import thaumcraft.dimension.outer_lands.maze.MazeHandler;
import thaumcraft.dimension.outer_lands.maze.GenCommon;
import thaumcraft.init.registry.ModEntities;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.init.registry.ModBlockEntities;

public class EldritchLockBlockEntity extends BlockEntity implements ITickable {
   public int count = -1;
   /**
    * Сторона механизма, заданная генератором лабиринта ({@code GenBossRoom}).
    * В 1.7.10 хранится как {@code byte facing} в TileEldritchLock; читается
    * только клиентской иконкой {@code BlockEldritch} ({@code insIcon[3]/[4]}),
    * но обязано переживать сохранение мира, иначе после перезапуска замок
    * теряет ориентацию. Значение — порядковый номер ForgeDirection,
    * где UNKNOWN = 6.
    */
   public byte facing;
   private static final Direction[][] PED_DIRECTIONS = new Direction[][]{
      {Direction.NORTH, Direction.NORTH, Direction.NORTH}, {Direction.WEST, null, Direction.EAST}, {Direction.SOUTH, Direction.SOUTH, Direction.SOUTH}
   };
   public static final ResourceLocation SND_PUMP = ResourceLocation.fromNamespaceAndPath("thaumcraft", "pump");
   public static final ResourceLocation SND_ICE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "ice");

   public EldritchLockBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.ELDRITCH_LOCK.get(), pos, state);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (this.count != -1) {
         this.count++;
      }
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.count != -1) {
         this.count++;
         if (this.count % 5 == 0) {
            this.playSoundAt(SND_PUMP);
         }
         if (this.count > 100) {
            this.playSoundAt(SND_ICE);
            for (int a = -2; a <= 2; a++) {
               for (int b = -2; b <= 2; b++) {
                  for (int c = -2; c <= 2; c++) {
                     BlockPos target = pos.offset(a, b, c);
                     BlockState bs = level.getBlockState(target);
                     if (bs.is((net.minecraft.world.level.block.Block)thaumcraft.init.registry.ModBlocks.IMPASSABLE.get())
                        || bs.is((net.minecraft.world.level.block.Block)thaumcraft.init.registry.ModBlocks.ELDRITCH_NOTHING.get())) {
                        level.removeBlock(target, false);
                     }
                  }
               }
            }
            this.spawnBoss(level, pos);
            level.removeBlock(pos, false);
         }
      }
   }

   /**
    * Пробуждение босса при вскрытии замка — порт {@code TileEldritchLock.doBossSpawn}
    * (1.7.10).
    *
    * <p>Все четыре варианта зала перенесены 1:1 (строки 94-106): 0 голем,
    * 1 смотритель, 2 культисты, 3 таинт. Раньше был только вариант 2, а 0, 1 и 3
    * проваливались в зал Смотрителя — то есть три зала из четырёх строились
    * неправильно, а {@code EntityEldritchGolem} и {@code EntityTantacleGiant}
    * были недостижимы: обе сущности были зарегистрированы со всеми атрибутами и
    * рендерерами, но не создавались нигде.
    */
   private void spawnBoss(Level level, BlockPos pos) {
      int cx = pos.getX() >> 4;
      int cz = pos.getZ() >> 4;
      int centerX = cx;
      int centerZ = cz;

      // Ищем центр боссовой ячейки (feature 2) в окрестности.
      for (int a = -2; a <= 2; a++) {
         for (int b = -2; b <= 2; b++) {
            Cell cell = MazeHandler.getFromHashMap(new CellLoc(cx + a, cz + b));
            if (cell != null && cell.feature == 2) {
               centerX = cx + a;
               centerZ = cz + b;
            }
         }
      }

      if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
         return;
      }

      // The hall variant follows the counter, as in the original (MapBossData):
      // 0 golem, 1 warden, 2 cultists, 3 taint (all four are ported, see D27).
      BossMapData bossData = BossMapData.get(serverLevel);
      bossData.bossCount++;
      if (serverLevel.random.nextFloat() < 0.25F) {
         bossData.bossCount++;
      }

      bossData.setDirty();

      // 1.7.10 (TileEldritchLock.doBossSpawn, строки 94-106) разбирает четыре
      // варианта по остатку счётчика: 0 голем, 1 смотритель, 2 культисты,
      // 3 таинт. Раньше тут был только вариант 2, а остальные три проваливались
      // в зал Смотрителя — то есть два боссовых зала из четырёх не существовало,
      // а EntityEldritchGolem и зал тайнта были недостижимы.
      switch (Math.floorMod(bossData.bossCount, 4)) {
         case 0 -> this.spawnGolemBoss(serverLevel, centerX, centerZ);
         case 1 -> this.spawnWardenBoss(serverLevel, centerX, centerZ);
         case 2 -> this.spawnCultistBoss(serverLevel, centerX, centerZ);
         default -> this.spawnTaintBoss(serverLevel, centerX, centerZ);
      }
   }

   /**
    * Зал голема — порт {@code spawnGolemBossRoom} (1.7.10, строки 217-289).
    *
    * <p>Три обелиска по углам, пьедестал под боссом и разбросанные по углам
    * сундуки/урны (15% на клетку). Голем {@link
    * thaumcraft.entities.smart.eldritch_golem.EntityEldritchGolem} был
    * зарегистрирован со всеми атрибутами и рендерером, но не создавался нигде —
    * теперь он появляется.
    */
   private void spawnGolemBoss(net.minecraft.server.level.ServerLevel level, int centerX, int centerZ) {
      int x = centerX * 16 + 16;
      int y = 50;
      int z = centerZ * 16 + 16;

      // Смещение пьедестала зависит от выхода из зала (признак feature 2..5).
      int exit = this.findExitFeature(centerX, centerZ);
      int dx = 0;
      int dz = 0;

      switch (exit) {
         case 2 -> {
            dx = 8;
            dz = 8;
         }
         case 3 -> {
            dx = -8;
            dz = 8;
         }
         case 4 -> {
            dx = 8;
            dz = -8;
         }
         case 5 -> {
            dx = -8;
            dz = -8;
         }
         default -> {
         }
      }

      GenCommon.genObelisk(level, x + dx, y + 4, z + dz);
      GenCommon.genObelisk(level, x - dx, y + 4, z + dz);
      GenCommon.genObelisk(level, x + dx, y + 4, z - dz);

      for (BlockPos p : new BlockPos[]{
         new BlockPos(x + dx, y + 2, z + dz),
         new BlockPos(x - dx, y + 2, z + dz),
         new BlockPos(x + dx, y + 2, z - dz)}) {
         level.setBlock(p, thaumcraft.init.registry.ModBlocks.OBSIDIAN_TILE.get().defaultBlockState(), 3);
      }

      this.placePedestal(level, x, y + 2, z);

      // Сундуки и урны по углам зала (1.7.10:264-288).
      for (int a = -10; a <= 10; a++) {
         for (int b = -10; b <= 10; b++) {
            boolean corner = (a < -2 && b < -2) || (a > 2 && b > 2)
               || (a < -2 && b > 2) || (a > 2 && b < -2);
            BlockPos p = new BlockPos(x + a, y + 2, z + b);

            if (corner && level.random.nextFloat() < 0.15F && level.isEmptyBlock(p)) {
               float rr = level.random.nextFloat();
               var block = level.random.nextFloat() < 0.3F
                  ? thaumcraft.init.registry.ModBlocks.LOOT_CRATE.get()
                  : GenCommon.lootUrn(rr < 0.05F ? 2 : (rr < 0.2F ? 1 : 0));
               level.setBlock(p, block.defaultBlockState(), 3);
            }
         }
      }

      var boss = (thaumcraft.entities.smart.eldritch_golem.EntityEldritchGolem)
         ModEntities.ELDRITCH_GOLEM.get().create(level);
      if (boss == null) {
         return;
      }

      this.faceBossToward(level, boss, x, y + 3, z, this.getBlockPos());
      boss.finalizeSpawn(level, level.getCurrentDifficultyAt(boss.blockPosition()),
         net.minecraft.world.entity.MobSpawnType.EVENT, null);
      boss.setPersistenceRequired();
      level.addFreshEntity(boss);
   }

   /**
    * Зал Смотрителя — порт {@code spawnWatcherBossRoom} (1.7.10, строки 133-216).
    *
    * <p>Два обелиска со смещением по выходу, между ними пьедесталы с урнами,
    * по углам — древний камень и пьедестал под боссом.
    */
   private void spawnWardenBoss(net.minecraft.server.level.ServerLevel level, int centerX, int centerZ) {
      int x = centerX * 16 + 16;
      int y = 50;
      int z = centerZ * 16 + 16;
      int exit = this.findExitFeature(centerX, centerZ);
      int x2 = x;
      int z2 = z;

      switch (exit) {
         case 2 -> {
            x2 = x + 8;
            z2 = z + 8;
         }
         case 3 -> {
            x2 = x - 8;
            z2 = z + 8;
         }
         case 4 -> {
            x2 = x + 8;
            z2 = z - 8;
         }
         case 5 -> {
            x2 = x - 8;
            z2 = z - 8;
         }
         default -> {
         }
      }

      GenCommon.genObelisk(level, x2, y + 4, z);
      GenCommon.genObelisk(level, x, y + 4, z2);

      for (BlockPos p : new BlockPos[]{
         new BlockPos(x2, y + 2, z),
         new BlockPos(x, y + 2, z2)}) {
         level.setBlock(p, thaumcraft.init.registry.ModBlocks.OBSIDIAN_TILE.get().defaultBlockState(), 3);
      }

      // Урны на пьедесталах (1.7.10:164-176): метаданные 2/1/0 — редкая,
      // необычная, обычная.
      for (BlockPos pedestal : new BlockPos[]{new BlockPos(x2, y + 2, z), new BlockPos(x, y + 2, z2)}) {
         for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
               if (a != 0 && b != 0 && level.random.nextFloat() < 0.9F) {
                  float rr = level.random.nextFloat();
                  level.setBlock(new BlockPos(pedestal.getX() + a, y + 2, pedestal.getZ() + b),
                     GenCommon.lootUrn(rr < 0.1F ? 2 : (rr < 0.3F ? 1 : 0)).defaultBlockState(), 3);
               }
            }
         }
      }

      for (int a = -2; a <= 2; a += 4) {
         for (int b = -2; b <= 2; b += 4) {
            level.setBlock(new BlockPos(x + a, y + 3, z + b),
               thaumcraft.init.registry.ModBlocks.ANCIENT_STONE.get().defaultBlockState(), 3);
            level.setBlock(new BlockPos(x + a, y + 2, z + b),
               thaumcraft.init.registry.ModBlocks.GLYPHED_STONE.get().defaultBlockState(), 3);
         }
      }

      this.placePedestal(level, x2, y + 2, z2);

      var boss = ModEntities.ELDRITCH_WARDEN.get().create(level);
      if (boss == null) {
         return;
      }

      this.faceBossToward(level, boss, x2, y + 3, z2, this.getBlockPos());
      boss.finalizeSpawn(level, level.getCurrentDifficultyAt(boss.blockPosition()),
         net.minecraft.world.entity.MobSpawnType.EVENT, null);
      boss.setPersistenceRequired();
      // Аналог func_110171_b(x, y + 2, z, 32) — «дом» босса.
      boss.restrictTo(new BlockPos(x, y + 2, z), 32);
      level.addFreshEntity(boss);
   }

   /**
    * Зал тайнта — порт {@code spawnTaintBossRoom} (1.7.10, строки 344-402).
    *
    * <p>Вся ячейка заливается тайном: волокна по стенкам и потолку, тайновая
    * почва в основании и тайновая корка сверху. Пробуждается пять
    * {@link thaumcraft.entities.smart.tentacle.EntityTantacle}, крупные
    * чередуются с мелкими, все становятся чемпионами ({@code
    * EntityUtils.makeChampion}). На лёгкой сложности крупных нет.
    *
    * <p>Заодно это единственный способ встретить {@code EntityTantacleGiant} —
    * он был зарегистрирован со всеми атрибутами, но нигде не создавался.
    */
   private void spawnTaintBoss(net.minecraft.server.level.ServerLevel level, int centerX, int centerZ) {
      int x = centerX * 16 + 16;
      int y = 50;
      int z = centerZ * 16 + 16;

      // Биом ячейки — тайновый, иначе боссы и тайн-биом не ведут себя как в
      // оригинале (1.7.10 звал Utils.setBiomeAt на всю площадь 25x25).
      for (int a = -12; a <= 12; a++) {
         for (int b = -12; b <= 12; b++) {
            thaumcraft.world.biomes.TaintHelper.trySetBiomeColumn(level,
               new BlockPos(x + b, y + 2, z + a), thaumcraft.init.registry.ModBiomes.TAINT);
         }
      }

      for (int a = -12; a <= 12; a++) {
         for (int b = -12; b <= 12; b++) {
            // Волокна: только у стен/пола/потолка, метаданные 0 или 1.
            for (int c = 0; c < 9; c++) {
               BlockPos p = new BlockPos(x + b, y + 2 + c, z + a);
               if (level.isEmptyBlock(p) && this.isAdjacentToSolid(level, p) && level.random.nextInt(3) != 0) {
                  level.setBlock(p, thaumcraft.init.registry.ModBlocks.FIBROUS_TAINT.get().defaultBlockState(), 3);
               }
            }

            // Тайновая корка сверху (метаданные 0 = корка в оригинале).
            if (level.random.nextFloat() < 0.15F) {
               level.setBlock(new BlockPos(x + b, y + 2, z + a),
                  thaumcraft.init.registry.ModBlocks.TAINT_CRUST.get().defaultBlockState(), 3);
               if (level.random.nextFloat() < 0.2F) {
                  level.setBlock(new BlockPos(x + b, y + 3, z + a),
                     thaumcraft.init.registry.ModBlocks.TAINT_CRUST.get().defaultBlockState(), 3);
               }
            }

            // Тайновая почва в основании (метаданные 1 в оригинале).
            if ((Math.abs(a) != 4 && Math.abs(b) != 4 || !level.random.nextBoolean())
               && (Math.abs(a) < 5 && Math.abs(b) < 5 || !(level.random.nextFloat() > 0.33F))
               && (Math.abs(a) < 7 && Math.abs(b) < 7 || !(level.random.nextFloat() > 0.25F))) {
               level.setBlock(new BlockPos(x + b, y + 1, z + a),
                  thaumcraft.init.registry.ModBlocks.TAINT_SOIL.get().defaultBlockState(), 3);
            }
         }
      }

      // Пять боссов: крупные чередуются с мелкими, на лёгкой все мелкие
      // (1.7.10:389-424). Крупные — это EntityTantacleGiant.
      var spawnAt = new Object[][]{
         {x + 0.5D, z + 0.5D, false},
         {x + 3.5D, z + 3.5D, level.random.nextBoolean()},
         {x - 2.5D, z + 3.5D, true},
         {x + 3.5D, z - 2.5D, level.random.nextBoolean()},
         {x - 2.5D, z - 2.5D, true}
      };
      var previousGiant = false;

      for (Object[] entry : spawnAt) {
         boolean giant = (boolean)entry[2];

         if (giant == previousGiant) {
            // В оригинале третий и пятый боссы всегда противоположны второму и
            // четвёртому: если предыдущий крупный, берём мелкого.
            giant = !giant;
         }

         previousGiant = giant;
         var boss = this.createTantacle(level, giant);
         if (boss == null) {
            continue;
         }

         boss.moveTo((double)entry[0], y + 3, (double)entry[1], 0.0F, 0.0F);
         boss.finalizeSpawn(level, level.getCurrentDifficultyAt(boss.blockPosition()),
            net.minecraft.world.entity.MobSpawnType.EVENT, null);
         thaumcraft.util.EntityUtils.makeChampion(boss, true);
         level.addFreshEntity(boss);
      }
   }

   private net.minecraft.world.entity.monster.Monster createTantacle(net.minecraft.server.level.ServerLevel level, boolean giant) {
      if (giant) {
         var type = ModEntities.TENTACLE_GIANT.get();
         return (net.minecraft.world.entity.monster.Monster)type.create(level);
      }

      var type = ModEntities.TENTACLE.get();
      return (net.minecraft.world.entity.monster.Monster)type.create(level);
   }

   /** Пьедестал 3x3 под боссом: пустые клетки — глифы, занятые — лестница. */
   private void placePedestal(net.minecraft.server.level.ServerLevel level, int x, int y, int z) {
      for (int a = 0; a < 3; a++) {
         for (int b = 0; b < 3; b++) {
            level.setBlock(new BlockPos(x - 1 + b, y, z - 1 + a),
               thaumcraft.init.registry.ModBlocks.GLYPHED_STONE.get().defaultBlockState(), 3);
         }
      }
   }

   /** Признак feature выхода из зала 2..5 в окрестности 5x5 (1.7.10:74-77). */
   private int findExitFeature(int cx, int cz) {
      for (int a = -2; a <= 2; a++) {
         for (int b = -2; b <= 2; b++) {
            Cell cell = MazeHandler.getFromHashMap(new CellLoc(cx + a, cz + b));
            if (cell != null && cell.feature >= 2 && cell.feature <= 5
               && (cell.north || cell.south || cell.east || cell.west)) {
               return cell.feature;
            }
         }
      }

      return 0;
   }

   /** Развернуть босса лицом к замку — 1.7.10 считает углы через atan2. */
   private void faceBossToward(
      net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.Mob boss,
      int bx, int by, int bz, BlockPos lock
   ) {
      double dx = lock.getX() - (bx + 0.5D);
      double dy = lock.getY() - (by + boss.getBbHeight() * 0.0F);
      double dz = lock.getZ() - (bz + 0.5D);
      double flat = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
      float pitch = -(float)(Math.atan2(dy, flat) * 180.0D / Math.PI);
      boss.moveTo(bx + 0.5D, by, bz + 0.5D, yaw, pitch);
      boss.setYHeadRot(yaw);
      boss.setYRot(yaw);
   }

   private boolean isAdjacentToSolid(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
      for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
         if (!level.getBlockState(pos.relative(d)).isAir()) {
            return true;
         }
      }

      return false;
   }

   /**
    * Зал культистов — порт {@code spawnCultistBossRoom} из 1.7.10.
    *
    * <p>Пол выкладывается глифированным камнем, по периметру ставится колоннада
    * из древнего камня и глифированного камня, в центре пробуждается портал
    * культистов: он и выпускает багровых магов, которых иначе в порте взять
    * было негде.
    */
   private void spawnCultistBoss(net.minecraft.server.level.ServerLevel level, int centerX, int centerZ) {
      int x = centerX * 16 + 16;
      int y = 50;
      int z = centerZ * 16 + 16;

      // Пол зала: рваные края по порогам из оригинала.
      for (int a = -4; a <= 4; a++) {
         for (int b = -4; b <= 4; b++) {
            if ((Math.abs(a) != 2 && Math.abs(b) != 2 || !level.random.nextBoolean())
               && (Math.abs(a) != 3 && Math.abs(b) != 3 || !(level.random.nextFloat() > 0.33F))
               && (Math.abs(a) != 4 && Math.abs(b) != 4 || !(level.random.nextFloat() > 0.25F))) {
               level.setBlock(new BlockPos(x + b, y + 1, z + a),
                  thaumcraft.init.registry.ModBlocks.GLYPHED_STONE.get().defaultBlockState(), 3);
            }
         }
      }

      // Колоннада по периметру.
      for (int a = 0; a < 5; a++) {
         for (int b = 0; b < 5; b++) {
            if (a == 0 || a == 4 || b == 0 || b == 4) {
               level.setBlock(new BlockPos(x - 8 + b * 4, y + 2, z - 8 + a * 4),
                  thaumcraft.init.registry.ModBlocks.ANCIENT_STONE.get().defaultBlockState(), 3);
               level.setBlock(new BlockPos(x - 8 + b * 4, y + 3, z - 8 + a * 4),
                  thaumcraft.init.registry.ModBlocks.GLYPHED_STONE.get().defaultBlockState(), 3);
               level.setBlock(new BlockPos(x - 8 + b * 4, y + 10, z - 8 + a * 4),
                  thaumcraft.init.registry.ModBlocks.ANCIENT_STONE.get().defaultBlockState(), 3);
               level.setBlock(new BlockPos(x - 8 + b * 4, y + 9, z - 8 + a * 4),
                  thaumcraft.init.registry.ModBlocks.GLYPHED_STONE.get().defaultBlockState(), 3);
            }
         }
      }

      var portalBoss = ModEntities.CULTIST_PORTAL.get().create(level);
      if (portalBoss != null) {
         portalBoss.moveTo(x + 0.5, y + 2, z + 0.5, 0.0F, 0.0F);
         portalBoss.setPersistenceRequired();
         level.addFreshEntity(portalBoss);
      }
   }

   private void playSoundAt(ResourceLocation id) {
      if (this.level != null) {
         SoundEvent se = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(id);
         if (se != null) {
            this.level.playSound(null, this.worldPosition, se, SoundSource.BLOCKS, 1.0F, 1.0F);
         }
      }
   }

   protected void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.count = tag.getInt("count");
      this.facing = tag.getByte("facing");
   }

   protected void saveAdditional(CompoundTag tag, Provider provider) {
      tag.putInt("count", this.count);
      tag.putByte("facing", this.facing);
      super.saveAdditional(tag, provider);
   }

   public byte getFacing() {
      return this.facing;
   }

   /**
    * Сторона механизма для рендера — порт {@code TileEldritchLock.getFacing} плюс
    * {@code ForgeDirection.getOrientation} из 1.7.10 (байткод рендерера, строки
    * 107-108 байткода {@code TileEldritchLockRenderer}).
    *
    * <p>Раньше рендер брал свойство блока {@code FACING}, а ориентацию пишет
    * генератор в {@link #facing}. Эти две вещи нигде не связаны, и свойство
    * оставалось дефолтным {@code NORTH} — то есть замок всегда рисовался лицом
    * на север независимо от того, с какой стороны ячейки был выход. Это и есть
    * жалоба «замок повёрнут неправильно».
    *
    * <p>Порядковые номера совпадают: в 1.7.10 это {@code ForgeDirection}
    * (DOWN=0, UP=1, NORTH=2, SOUTH=3, WEST=4, EAST=5, UNKNOWN=6), в 1.21.1 —
    * {@code Direction} в том же порядке. Значение 6 (UNKNOWN) и выход за
    * пределы массива отсекаются: рендерить нечего, и в оригинале
    * {@code getOrientation(6)} тоже вернул бы UNKNOWN с нулевыми смещениями.
    */
   @Nullable
   public Direction renderFacing() {
      int f = this.facing;
      Direction[] values = Direction.values();

      if (f < 0 || f >= values.length) {
         return null;
      }

      return values[f];
   }

   /** Порт {@code setFacing}: обновляет блок на клиенте и помечает изменённым. */
   public void setFacing(byte face) {
      this.facing = face;
      if (this.level != null) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
      this.setChanged();
   }

   public CompoundTag getUpdateTag(Provider provider) {
      return this.saveWithoutMetadata(provider);
   }

   @Nullable
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      return new AABB(
         this.worldPosition.getX() - 2.25,
         this.worldPosition.getY() - 2.25,
         this.worldPosition.getZ() - 2.25,
         this.worldPosition.getX() + 3.25,
         this.worldPosition.getY() + 3.25,
         this.worldPosition.getZ() + 3.25
      );
   }
}
