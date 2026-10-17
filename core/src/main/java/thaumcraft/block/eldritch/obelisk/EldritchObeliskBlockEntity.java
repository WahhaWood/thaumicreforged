package thaumcraft.block.eldritch.obelisk;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.init.registry.ModBlockEntities;

public class EldritchObeliskBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   private int tickCount = 0;

   public EldritchObeliskBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.ELDRITCH_OBELISK.get(), pos, state);
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      BlockPos p = this.getBlockPos();
      return new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 5, p.getZ() + 1);
   }

   /**
    * Обелиск усиливает элдрич-мобов рядом — порт
    * {@code TileEldritchObelisk.func_145845_h} (1.7.10, строки 37-60).
    *
    * <p>Серверной части в порте не было вовсе: был только клиентский
    * {@code wispFX4}, то есть визуальные искры вокруг моба. Обелиски в
    * лабиринте и в боссовых залах стояли и выглядели, но ничего не делали.
    *
    * <p>Правила оригинала: раз в 20 тиков все {@code IEldritchMob} в радиусе 6
    * от центра блока получают силу и сопротивление урону на 40 тиков, если силы
    * ещё нет. Радиус считается от (x+0.5, y, z+0.5) — вертикально центр смещён
    * к низу блока, а не в его середину.
    *
    * <p>Эффекты расставлены по сверке с 1.12.2-референсом того же мода
    * ({@code TileEldritchObelisk}, строки 36-41), где имена читаемые:
    * {@code MobEffects.REGENERATION} + {@code MobEffects.RESISTANCE} с проверкой
    * «уже нет RESISTANCE». Структурно это совпадает с 1.7.10, где набор полей
    * {@code Potion.field_76420_g} + {@code field_76428_l} и проверка
    * {@code field_76428_l}. Значит в 1.7.10:
    * {@code field_76428_l} — сила, {@code field_76420_g} — сопротивление.
    * Подтверждения: {@code EntityThaumcraftBoss} (строки 236-238) даёт
    * {@code field_76428_l} с усилением {@code damage/15} и
    * {@code field_76420_g}/{@code field_76424_c} с {@code damage/40} — в 1.12.2
    * это STRENGTH, RESISTANCE и FIRE_RESISTANCE, то есть
    * {@code field_76424_c} — огнестойкость.
    *
    * <p>Репенсион между 1.7.10 и 1.12.2 в самом обелиске есть: 1.12.2 даёт
    * регенерацию вместо силы. Ориентируемся на 1.7.10 — по нему написан контракт
    * паритета.
    */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (level.isClientSide() || this.tickCount++ % 20 != 0) {
         return;
      }

      double cx = pos.getX() + 0.5D;
      double cy = pos.getY();
      double cz = pos.getZ() + 0.5D;
      AABB box = new AABB(cx - 6.0D, cy - 6.0D, cz - 6.0D, cx + 6.0D, cy + 6.0D, cz + 6.0D);

      for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, box)) {
         if (!(mob instanceof IEldritchMob)
            || mob.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST)) {
            continue;
         }

         mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false));
         mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 40, 0, true, false));
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      double cx = pos.getX() + 0.5;
      double cy = pos.getY();
      double cz = pos.getZ() + 0.5;
      AABB box = new AABB(cx - 6.0, cy - 6.0, cz - 6.0, cx + 6.0, cy + 6.0, cz + 6.0);
      List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box);
      if (!list.isEmpty()) {
         for (Entity e : list) {
            if (e instanceof IEldritchMob) {
               Thaumcraft.setup.wispFX4(level, cx, pos.getY() + 1 + level.random.nextFloat() * 3.0F, cz, e, 5, true, 1.0F);
            }
         }
      }
   }
}
