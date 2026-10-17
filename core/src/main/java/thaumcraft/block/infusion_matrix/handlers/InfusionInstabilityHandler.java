package thaumcraft.block.infusion_matrix.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.block.arcane_pedestal.ArcanePedestalBlockEntity;
import thaumcraft.block.crystal.CrystalClusterBlock;
import thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModMobEffect;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXBlockZap;
import thaumcraft.network.playerdata.PacketSyncWarp;
import thaumcraft.network.playerdata.PacketWarpMessage;
import thaumcraft.research.PlayerKnowledge;

public class InfusionInstabilityHandler {
   private final RunicMatrixBlockEntity matrix;

   public InfusionInstabilityHandler(RunicMatrixBlockEntity matrix) {
      this.matrix = matrix;
   }

   // ------------------------------------------------------------------
   // Система стабильности (собственная, вместо дробной нестабильности TC4)
   // ------------------------------------------------------------------
   //
   // Игрок копит ОЧКИ СТАБИЛЬНОСТИ (S). Гол��я матрица даёт {@link #BASE_STABILITY}.
   // Каждый рецепт требует очки по своему классу сложности (см. {@link #tierCost}):
   // Negligible — 10, Minor — 20, Moderate — 40, High — 60, Very High — 80, Dangerous — 100.
   // Пока S ниже требуемого, матрица «нестабильна» на величину дефицита.
   //
   // Правила (все целые, кроме свечей — 2.5, это точная двоичная дробь):
   //   зеркальная пара  = сумма весов двух блоков (свеча 2.5, кристалл 10, череп 20);
   //   одиночка без пары = −{@link #SINGLE_PENALTY} (штраф за асимметрию);
   //   пьедестал без пары = −20 (−30 с предметом), зеркальная пара пьедесталов = 0;
   //   итог обрезан сверху {@link #MAX_STABILITY}, снизу не обрезается — минус возможен.
   // ------------------------------------------------------------------

   /** Стабильность голой матрицы — ровно закрывает рецепт класса Negligible. */
   public static final int BASE_STABILITY = 10;
   /** Потолок: перекрывает Dangerous с запасом (S бывает и «100 и выше»). */
   public static final int MAX_STABILITY = 150;
   /** Штраф за стабилизатор-одиночку (нет зеркального блока напротив). */
   public static final int SINGLE_PENALTY = 10;
   /** Штраф за пьедестал без зеркальной пары; с предметом — на 10 больше. */
   public static final int PEDESTAL_PENALTY = 20;
   /** Дополнительный штраф за пьедестал с предметом. */
   public static final int PEDESTAL_ITEM_EXTRA = 10;
   /** Ролл события нестабильности раз в 500 тиков на очко дефицита. */
   public static final int EVENT_ROLL = 500;
   /** How often the matrix rolls an instability event, in ticks. */
   public static final int EVENT_PERIOD_TICKS = 40;
   /**
    * Нижняя граница знаменателя для утечки эссенции: раз в 40 тиков, то есть утечка
    * не может идти чаще раза в две секунды. Без неё {@code max(1, …)} на большом
    * дефиците даёт {@code nextInt(1)} — безусловную утечку каждый тик.
    */
   public static final int ESSENTIA_LOSS_MIN_ROLL = 40;
   /** Вес свечи в паре (2.5 — точная двоичная дробь, без накопления ошибки). */
   public static final double CANDLE_WEIGHT = 2.5;
   /** Вес кластера кристалла в паре. */
   public static final int CRYSTAL_WEIGHT = 10;
   /** Вес черепа в паре — самый сильный и самый редкий. */
   public static final int SKULL_WEIGHT = 20;

   /** Сколько очков требует рецепт каждого класса; индекс массива = {@link #tierOf}. */
   private static final int[] TIER_COST = {10, 20, 40, 60, 80, 100};

   /**
    * Потолок класса рецепта: 5 — Dangerous. Выше рецепта не бывает, поэтому и
    * требование не уходит за 100 очков, сколько бы чар ни висело на предмете
    * ({@code InfusionEnchantmentRecipe#calcInstability}) и сколько бы рун ни
    * сидело на броне ({@code InfusionRunicAugmentRecipe#getInstability}).
    */
   public static final int MAX_TIER = TIER_COST.length - 1;

   /** Требуемая стабильность для рецепта с нестабильностью {@code recipeInstability}. */
   public static int requiredStability(int recipeInstability) {
      return TIER_COST[tierOf(recipeInstability)];
   }

   /** Класс сложности рецепта: 0 Negligible … {@link #MAX_TIER} Dangerous (канон TC4 {@code R/2}). */
   public static int tierOf(int recipeInstability) {
      return Math.min(MAX_TIER, Math.max(0, recipeInstability / 2));
   }

   /**
    * Mean time between instability events for a deficit: one roll every {@link #EVENT_PERIOD_TICKS}
    * with a {@code (deficit + 1) / EVENT_ROLL} chance.
    */
   public static double eventIntervalSeconds(int deficit) {
      return EVENT_PERIOD_TICKS / 20.0 * EVENT_ROLL / (deficit + 1);
   }

   /** Pedestals found by the last structure scan; the matrix draws ingredients and events pick targets from it. */
   private List<BlockPos> pedestals = List.of();

   public List<BlockPos> getPedestals() {
      return this.pedestals;
   }

   /**
    * The pedestals of this altar: within 8 blocks horizontally, from the matrix level down to 10 below,
    * the topmost one per column, the central pedestal excluded. One rule for the recipe, the draw,
    * the stability score and the events.
    */
   public List<BlockPos> findPedestals() {
      Level level = this.matrix.getLevel();
      List<BlockPos> found = new ArrayList<>();
      if (level == null) {
         return found;
      }

      BlockPos origin = this.matrix.getBlockPos();
      for (int xx = -8; xx <= 8; xx++) {
         for (int zz = -8; zz <= 8; zz++) {
            if (xx == 0 && zz == 0) {
               continue;
            }

            for (int yy = 0; yy <= 10; yy++) {
               BlockPos pos = origin.offset(xx, -yy, zz);
               if (level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity) {
                  found.add(pos);
                  break;
               }
            }
         }
      }

      return found;
   }

   /**
    * Полная стабильность структуры алтаря — целые очки вместо дробной нестабильности.
    * Обрезка сверху {@link #MAX_STABILITY}, снизу не режется (перекос в минус честен).
    */
   public int scanStability() {
      return this.scanStructureDetail().stability();
   }

   /**
    * Детальная разбивка скана — для калькулятора стабилизации и JEI.
    *
    * @param stability итоговые очки (с базой, потолком и штрафами)
    * @param pedestalPenalty штраф за асимметричные пьедесталы (≤ 0)
    * @param candlePairs пары свечей/кластеров кристалла
    * @param crystalPairs пары кристаллов
    * @param skullPairs пары черепов
    * @param singles одиночки без зеркальной пары (штраф −10 каждая)
    * @param pedestals сколько всего пьедесталов в зоне скана
    * @param pairs сколько из них имеют зеркальную пару
    */
   public record StructureScan(
      int stability,
      int pedestalPenalty,
      float candlePairs,
      float crystalPairs,
      float skullPairs,
      int singles,
      int pedestals,
      int pairs
   ) {
      /** Сколько очков дали стабилизаторы (без базы и без штрафов). */
      public float stabilizerGain() {
         return this.candlePairs + this.crystalPairs + this.skullPairs - this.singles * SINGLE_PENALTY;
      }

      /** Сырой счёт без базы и потолка — что реально настроено блоками. */
      public float raw() {
         return this.stabilizerGain() + this.pedestalPenalty;
      }
   }

   public StructureScan scanStructureDetail() {
      Level level = this.matrix.getLevel();
      if (level == null) {
         return new StructureScan(BASE_STABILITY, 0, 0, 0, 0, 0, 0, 0);
      }

      BlockPos origin = this.matrix.getBlockPos();
      int originX = origin.getX();
      int originY = origin.getY();
      int originZ = origin.getZ();
      List<BlockPos> pedestals = this.findPedestals();
      this.pedestals = pedestals;
      List<BlockPos> stabilizers = new ArrayList<>();

      for (int xx = -12; xx <= 12; xx++) {
         for (int zz = -12; zz <= 12; zz++) {
            if (xx == 0 && zz == 0) {
               continue;
            }

            for (int yy = -5; yy <= 10; yy++) {
               BlockPos pos = new BlockPos(originX + xx, originY - yy, originZ + zz);
               if (isStabilizerBlock(level.getBlockState(pos))) {
                  stabilizers.add(pos);
               }
            }
         }
      }

      int pedestalPenalty = 0;
      int pairedPedestals = 0;
      for (BlockPos pedestalPos : pedestals) {
         boolean hasItem = false;
         if (level.getBlockEntity(pedestalPos) instanceof ArcanePedestalBlockEntity ped) {
            hasItem = !ped.getItem(0).isEmpty();
         }

         BlockPos mirror = mirrorOf(pedestalPos, originX, originZ);
         if (level.getBlockEntity(mirror) instanceof ArcanePedestalBlockEntity mirrorPed) {
            // Зеркальная пара: предмет должен быть на обоих, иначе перекос ингредиентов.
            // Обход задевает обе половины пары, поэтому считаем пару один раз — за ту
            // половину, которую отдаёт {@link #isOwnHalf}; иначе штраф за перекос списывался
            // бы дважды (−20 вместо −10) и равнялся бы паре одиночных пьедесталов.
            if (!isOwnHalf(pedestalPos, mirror)) {
               continue;
            }

            pairedPedestals++;
            if (hasItem != !mirrorPed.getItem(0).isEmpty()) {
               pedestalPenalty -= PEDESTAL_ITEM_EXTRA;
            }
         } else {
            pedestalPenalty -= PEDESTAL_PENALTY + (hasItem ? PEDESTAL_ITEM_EXTRA : 0);
         }
      }

      float candleGain = 0.0F;
      float crystalGain = 0.0F;
      float skullGain = 0.0F;
      int singles = 0;
      for (BlockPos stabilizerPos : stabilizers) {
         BlockPos mirror = mirrorOf(stabilizerPos, originX, originZ);
         BlockState mirrorState = level.getBlockState(mirror);
         if (isStabilizerBlock(mirrorState)) {
            // Пара засчитывается один раз: берём только «свою» половину, чтобы не удвоить
            // бонус (обе половины зеркальны друг другу). Сравнения только по X мало: пара,
            // лежащая на плоскости зеркала (север/юг от матрицы, x == originX), проходит
            // проверку обоими блоками и даёт двойной бонус — а это штатная раскладка
            // алтаря, то есть баг срабатывал на каждом кристалле и черепе.
            if (isOwnHalf(stabilizerPos, mirror)) {
               float gain = (float)(getStabilityValue(level.getBlockState(stabilizerPos)) + getStabilityValue(mirrorState));
               if (isSkull(level.getBlockState(stabilizerPos)) || isSkull(mirrorState)) {
                  skullGain += gain;
               } else if (isCrystalCluster(level.getBlockState(stabilizerPos)) || isCrystalCluster(mirrorState)) {
                  crystalGain += gain;
               } else {
                  candleGain += gain;
               }
            }
         } else {
            singles++;
         }
      }

      float total = BASE_STABILITY + candleGain + crystalGain + skullGain
         + pedestalPenalty - singles * SINGLE_PENALTY;
      int stability = Math.min(MAX_STABILITY, Math.round(total));
      return new StructureScan(
         stability, pedestalPenalty, candleGain, crystalGain, skullGain, singles, pedestals.size(), pairedPedestals
      );
   }

   /**
    * Зеркальная позиция относительно оси матрицы: {@code x → 2·originX − x},
    * {@code z → 2·originZ − z}. Y не отражается — блоки зеркальны по вертикали.
    */
   private static BlockPos mirrorOf(BlockPos pos, int originX, int originZ) {
      return new BlockPos(
         originX - (pos.getX() - originX), pos.getY(), originZ - (pos.getZ() - originZ)
      );
   }

   /**
    * Эта половина пары — та, которой принадлежит право засчитать пару. Ровно одна из двух
    * половин проходит проверку, поэтому обход не удваивает бонус.
    *
    * <p>Сравнения по X не хватает: у пары «север/юг» обе половины имеют
    * {@code x == originX}, то есть {@code x == mirror.x}, и простое {@code x < mirror.x}
    * отсекало бы обоих. Дополнительное сравнение по Z как раз и выбирает северную половину.
    */
   private static boolean isOwnHalf(BlockPos pos, BlockPos mirror) {
      return pos.getX() < mirror.getX() || (pos.getX() == mirror.getX() && pos.getZ() <= mirror.getZ());
   }

   /** Череп — самый дорогой и самый сильный стабилизатор. */
   public static boolean isSkull(BlockState state) {
      return state.getBlock() instanceof AbstractSkullBlock;
   }

   /** Кластер кристалла — средний стабилизатор (10 за пару). */
   public static boolean isCrystalCluster(BlockState state) {
      return state.getBlock() instanceof CrystalClusterBlock;
   }

   /** Проверка, является ли блок стабилизатором алтаря наполнения. */
   public static boolean isStabilizerBlock(BlockState state) {
      Block block = state.getBlock();
      return block instanceof IInfusionStabiliser
         || block instanceof AbstractSkullBlock
         || block instanceof CandleBlock
         || state.is(BlockTags.CANDLES);
   }

   /** Вес блока в зеркальной паре: свеча 2.5, кристалл 10, череп 20. */
   public static double getStabilityValue(BlockState state) {
      Block block = state.getBlock();
      if (block instanceof AbstractSkullBlock) {
         return SKULL_WEIGHT;
      }
      if (block instanceof CrystalClusterBlock) {
         return CRYSTAL_WEIGHT;
      }
      if (block instanceof IInfusionStabiliser stabiliser) {
         return stabiliser.getStabilityValue();
      }
      return CANDLE_WEIGHT;
   }

   /** Cleaner rune: removes flux goo and gas in the stabilizer scan volume around the matrix. */
   public void cleanFlux(Level level) {
      BlockPos origin = this.matrix.getBlockPos();
      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-12, -10, -12), origin.offset(12, 5, 12))) {
         BlockState state = level.getBlockState(pos);
         if (state.is(ModBlocks.FLUX_GOO.get()) || state.is(ModBlocks.FLUX_GAS.get())) {
            level.removeBlock(pos, false);
         }
      }
   }

   /**
    * Обработка утечки эссенции: при нехватке стабильности матрица теряет эссенцию,
    * увеличивая требования. Частота считается от дефицита через {@link #EVENT_ROLL}.
    *
    * <p>Знаменатель нельзя доводить до нуля: при {@code max(1, 0)} бросок
    * {@code nextInt(1)} всегда даёт 0, то есть утечка становится безусловной и идёт
    * каждый тик. {@link #ESSENTIA_LOSS_MIN_ROLL} держит потолок примерно в одну
    * утечку за две секунды даже на полностью разобранном алтаре.
    */
   public void handleEssentiaLoss(Level level) {
      int deficit = this.matrix.getInstability();
      int roll = Math.max(ESSENTIA_LOSS_MIN_ROLL, EVENT_ROLL - deficit * 5);
      if (deficit > 0 && level.random.nextInt(roll) == 0) {
         AspectList essentia = this.matrix.getAspects();
         if (essentia != null && essentia.size() > 0) {
            Aspect[] aspects = essentia.getAspects();
            if (aspects.length > 0) {
               Aspect a = aspects[level.random.nextInt(aspects.length)];
               essentia.add(a, 1);
               this.matrix.queueSync(0);
            }
         }
      }
   }

   /**
    * Запуск события нестабильности (21 классический исход из TC4).
    */
   public void runInstabilityEvent(Level level) {
      int roll = level.random.nextInt(21);
      switch (roll) {
         case 0, 2, 10, 13 -> this.ejectPedestalItem(level, 0); // Выбросить предмет на землю
         case 6, 17 -> this.ejectPedestalItem(level, 1);        // Выбросить + разлить жидкий флюкс (Flux Goo)
         case 1, 11 -> this.ejectPedestalItem(level, 2);        // Выбросить + выпустить флюкс-газ (Flux Gas)
         case 19 -> this.ejectPedestalItem(level, 3);           // Уничтожить предмет + разлить жидкий флюкс
         case 7 -> this.ejectPedestalItem(level, 4);            // Уничтожить предмет + выпустить флюкс-газ
         case 4, 15 -> this.ejectPedestalItem(level, 5);        // Выбросить + взрыв на пьедестале
         case 3, 8, 14 -> this.zapNearby(level, false);         // Удар магической молнией по одной сущности
         case 12 -> this.zapNearby(level, true);                // Удар молнией по всем вокруг
         case 5, 16 -> this.harmNearby(level, false);           // Наложить порчу или истощение на одного
         case 18 -> this.harmNearby(level, true);               // Наложить порчу или истощение на всех
         case 9 -> this.matrixExplosion(level);                 // Малый взрыв у самой рунической матрицы
         case 20 -> this.warpCraftingPlayer(level);             // Наложить варп (искажение) на крафтящего игрока
      }
   }

   /** Выброс/уничтожение предмета с одного из пьедесталов. */
   private void ejectPedestalItem(Level level, int type) {
      BlockPos origin = this.matrix.getBlockPos();
      List<BlockPos> pedestals = new ArrayList<>();
      for (BlockPos pos : this.pedestals) {
         if (level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity ped && !ped.getItem(0).isEmpty()) {
            pedestals.add(pos);
         }
      }

      if (pedestals.isEmpty()) {
         return;
      }

      BlockPos target = pedestals.get(level.random.nextInt(pedestals.size()));
      if (level.getBlockEntity(target) instanceof ArcanePedestalBlockEntity ped) {
         ItemStack stack = ped.getItem(0).copy();

         if (type < 3 || type == 5) {
            ped.setItem(0, ItemStack.EMPTY);
            ped.setChanged();
            ItemEntity drop = new ItemEntity(
               level, target.getX() + 0.5, target.getY() + 1.2, target.getZ() + 0.5, stack
            );
            drop.setDeltaMovement(
               (level.random.nextDouble() - 0.5) * 0.3, 0.25, (level.random.nextDouble() - 0.5) * 0.3
            );
            level.addFreshEntity(drop);
         } else {
            // Уничтожение предмета (type 3 или 4)
            ped.setItem(0, ItemStack.EMPTY);
            ped.setChanged();
         }

         BlockPos above = target.above();
         boolean cleaner = this.matrix.hasRune(RunicMatrixBlockEntity.UPGRADE_CLEANER);
         if ((type == 1 || type == 3) && !cleaner) {
            if (level.getBlockState(above).isAir()) {
               level.setBlock(above, ModBlocks.FLUX_GOO.get().defaultBlockState(), 3);
            }
            level.playSound(null, target, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.4F, 1.0F);
         } else if ((type == 2 || type == 4) && !cleaner) {
            if (level.getBlockState(above).isAir()) {
               level.setBlock(above, ModBlocks.FLUX_GAS.get().defaultBlockState(), 3);
            }
            level.playSound(null, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 1.0F);
         } else if (type == 5) {
            level.explode(
               null, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
               1.0F, Level.ExplosionInteraction.NONE
            );
         }

         // Красные искры на пьедестале (порт blockEvent 11 из inEvEjectItem) и
         // молния от матрицы: FX уходит пакетом, потому что серверный
         // CommonSetup.nodeBolt — пустая заглушка (как TESR-вызовы в 1.7.10).
         level.blockEvent(target, level.getBlockState(target).getBlock(), 11, 0);
         PacketHandler.sendToAllAround(
            new PacketFXBlockZap(
               origin.getX() + 0.5F,
               origin.getY() + 0.5F,
               origin.getZ() + 0.5F,
               target.getX() + 0.5F,
               target.getY() + 1.5F,
               target.getZ() + 0.5F
            ),
            level,
            origin,
            32.0
         );
      }
   }

   /** Удар магической молнией по живым сущностям вокруг матрицы (порт inEvZap). */
   private void zapNearby(Level level, boolean all) {
      BlockPos origin = this.matrix.getBlockPos();
      List<LivingEntity> living = level.getEntitiesOfClass(
         LivingEntity.class, new AABB(origin).inflate(10.0)
      );

      for (LivingEntity entity : living) {
         entity.hurt(level.damageSources().magic(), 4.0F + level.random.nextInt(4));
         PacketHandler.sendToAllAround(
            new PacketFXBlockZap(
               origin.getX() + 0.5F,
               origin.getY() + 0.5F,
               origin.getZ() + 0.5F,
               (float)entity.getX(),
               (float)(entity.getY() + entity.getEyeHeight() / 2.0F),
               (float)entity.getZ()
            ),
            level,
            origin,
            32.0
         );
         if (!all) {
            break;
         }
      }
   }

   /** Наложение порчи или истощения на живых сущностей. */
   private void harmNearby(Level level, boolean all) {
      BlockPos origin = this.matrix.getBlockPos();
      List<LivingEntity> living = level.getEntitiesOfClass(
         LivingEntity.class, new AABB(origin).inflate(8.0)
      );

      for (LivingEntity entity : living) {
         if (level.random.nextBoolean() && ModMobEffect.FLUX_TAINT != null) {
            entity.addEffect(new MobEffectInstance(ModMobEffect.FLUX_TAINT, 120, 0, false, true));
         } else if (ModMobEffect.VIS_EXHAUST != null) {
            entity.addEffect(new MobEffectInstance(ModMobEffect.VIS_EXHAUST, 2400, 0, false, true));
         }
         level.playSound(null, entity.blockPosition(), SoundEvents.ZOMBIE_INFECT, SoundSource.BLOCKS, 0.4F, 1.2F);
         if (!all) {
            break;
         }
      }
   }

   /** Малый взрыв у рунической матрицы. */
   private void matrixExplosion(Level level) {
      BlockPos origin = this.matrix.getBlockPos();
      level.explode(
         null,
         origin.getX() + 0.5,
         origin.getY() + 0.5,
         origin.getZ() + 0.5,
         1.5F + level.random.nextFloat(),
         Level.ExplosionInteraction.NONE
      );
   }

   /** Наложение варпа (искажения) на игрока, запустившего крафт. */
   private void warpCraftingPlayer(Level level) {
      BlockPos origin = this.matrix.getBlockPos();
      Player targetPlayer = null;
      UUID playerUUID = this.matrix.getRecipePlayer();
      if (playerUUID != null) {
         targetPlayer = level.getPlayerByUUID(playerUUID);
      }
      if (targetPlayer == null) {
         List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(origin).inflate(10.0));
         if (!players.isEmpty()) {
            targetPlayer = players.get(level.random.nextInt(players.size()));
         }
      }

      if (targetPlayer instanceof ServerPlayer serverPlayer) {
         PlayerKnowledge pk = PlayerKnowledge.of(serverPlayer);
         if (level.random.nextFloat() < 0.25F) {
            pk.addWarpSticky(1);
            PacketHandler.sendTo(new PacketSyncWarp(serverPlayer, (byte)1), serverPlayer);
            PacketHandler.sendTo(new PacketWarpMessage((byte)1, 1), serverPlayer);
         } else {
            int temp = 1 + level.random.nextInt(5);
            pk.addWarpTemp(temp);
            PacketHandler.sendTo(new PacketSyncWarp(serverPlayer, (byte)2), serverPlayer);
            PacketHandler.sendTo(new PacketWarpMessage((byte)2, temp), serverPlayer);
         }
         serverPlayer.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.1")));
      }
   }
}
