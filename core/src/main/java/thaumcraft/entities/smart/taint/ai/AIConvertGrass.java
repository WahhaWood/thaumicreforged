package thaumcraft.entities.smart.taint.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.init.registry.ModBlocks;

/**
 * Овца-таинт заражает землю под собой — порт {@code AIConvertGrass}
 * (1.7.10, {@code thaumcraft/common/entities/ai/misc/AIConvertGrass}).
 *
 * <p>В оригинале результат всегда один: под ногами овцы появляются
 * {@code blockTaintFibres}, а биом становится тайновым. Почва под тайн-волокнами
 * портится уже сама — {@code BlockTaintFibres} в 1.7.10 превращает соседнюю
 * землю в {@code blockTaint} (метаданные 0-2: корка/почва/плоть). В порте
 * {@code FibrousTaintBlock.randomTick} делает ровно то же ({@code TAINT_SOIL} в
 * соседях), поэтому овце не нужно превращать землю под собой самой.
 *
 * <p>Три вещи были сделаны не по оригиналу и ломали поведение:
 *
 * <ul>
 *   <li>{@code setFlags(Flag.MOVE, JUMP, LOOK)}. В 1.7.10 флаги не заданы вовсе
 *       (в конструкторе только {@code func_75248_a(7)} — приоритет). В 1.21
 *       {@code GoalSelector} не даёт запустить цель, если она делит флаг с уже
 *       работающей целью, а у овцы почти всегда работает блуждание с
 *       {@code Flag.MOVE}. То есть цель физически не могла стартовать, и заражение
 *       земли не происходило никогда — это и было «известным флаком» регрессии.
 *   <li>Шанс 1/50 вместо 1/250 (в оригинале {@code nextInt(250) != 0}).
 *   <li>Ветка «земля под ногами» превращала блок <em>под</em> овцей в
 *       {@code TAINT_SOIL}, а на её месте ставила {@code TAINTED_GRASS} —
 *       ничего из этого в 1.7.10 не встречается.
 * </ul>
 *
 * <p>Условие в оригинале — трава в блоке овцы с метаданными 1 (мёртвый куст, он
 * же {@code dead_bush} в 1.21) либо {@code dirt} под ногами. Проверка метаданных
 * отброшена как устаревшая: в 1.21 трава отдельных метаданных не имеет, а
 * {@code DEAD_BUSH} — самостоятельный блок.
 */
public class AIConvertGrass extends Goal {
   /** 1.7.10:25 — {@code nextInt(250) != 0}, то есть примерно раз в 12 секунд. */
   private static final int CHANCE = 250;

   private final Mob entity;
   private int convertTimer;

   public AIConvertGrass(Mob entity) {
      this.entity = entity;
      // Флаги намеренно не задаются — см. javadoc: в 1.7.10 их нет, и без них
      // цель не конфликтует с блужданием овцы. Приоритет 7 (1.7.10:21) задаётся
      // при регистрации: в 1.21 у Goal нет своего сеттера.
   }

   @Override
   public boolean canUse() {
      if (this.entity.getRandom().nextInt(CHANCE) != 0) {
         return false;
      }

      Level level = this.entity.level();
      BlockPos pos = this.entity.blockPosition();
      BlockState standing = level.getBlockState(pos);
      BlockState below = level.getBlockState(pos.below());
      return isGrass(standing) || below.is(Blocks.DIRT);
   }

   @Override
   public boolean canContinueToUse() {
      return this.convertTimer > 0;
   }

   @Override
   public void start() {
      this.convertTimer = 40;
      this.entity.getNavigation().stop();
   }

   @Override
   public void stop() {
      this.convertTimer = 0;
   }

   @Override
   public void tick() {
      this.convertTimer = Math.max(0, this.convertTimer - 1);
      if (this.convertTimer != 4) {
         return;
      }

      Level level = this.entity.level();
      BlockPos pos = this.entity.blockPosition();
      boolean grass = isGrass(level.getBlockState(pos));
      boolean dirt = level.getBlockState(pos.below()).is(Blocks.DIRT);

      if (!grass && !dirt) {
         return;
      }

      if (grass) {
         level.destroyBlock(pos, false);
      }

      // 1.7.10:64 и :69 — blockTaintFibres ставится в блок самой овцы в обоих
      // случаях, биом меняется там же.
      level.setBlock(pos, ModBlocks.FIBROUS_TAINT.get().defaultBlockState(), 3);
      thaumcraft.util.Utils.setBiomeAt(level, pos, thaumcraft.init.registry.ModBiomes.TAINT);
      level.playSound(
         null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.4F, 0.8F + level.random.nextFloat() * 0.4F
      );

      // 1.7.10:66 и :71 — func_70615_aA, то есть eatGrassBonus: овца, поевшая
      // траву, отращивает снятую шерсть.
      if (this.entity instanceof net.minecraft.world.entity.animal.Sheep sheep && sheep.isSheared()) {
         sheep.setSheared(false);
      }
   }

   private static boolean isGrass(BlockState state) {
      return state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
         || state.is(Blocks.FERN) || state.is(Blocks.DEAD_BUSH);
   }
}