package thaumcraft.block.jar.brain;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.block.jar.JarBaseBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModSounds;

public class BrainJarBlockEntity extends JarBaseBlockEntity implements ITickable {
   public static final int XP_MAX = 2000;
   public int xp = 0;
   public int xpMax = 2000;
   public int eatDelay = 0;
   private int clientTicks = 0;
   private long nextSighMs = System.currentTimeMillis() + 1500L;

   public BrainJarBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.BRAIN_JAR.get(), pos, state);
   }

   @Override
   protected void loadCustom(@NotNull CompoundTag tag) {
      if (tag.contains("XP")) {
         this.xp = tag.getInt("XP");
      } else {
         this.xp = tag.getInt("xp");
      }
   }

   @Override
   protected void saveCustom(@NotNull CompoundTag tag) {
      tag.putInt("XP", this.xp);
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.xp > this.xpMax) {
         this.xp = this.xpMax;
      }

      if (this.xp < this.xpMax && this.eatDelay == 0) {
         ExperienceOrb closest = getClosestOrb(level, new AABB(this.worldPosition).inflate(6.0));
         if (closest != null) {
            double var3x = (this.worldPosition.getX() + 0.5 - closest.getX()) / 7.0;
            double var5 = (this.worldPosition.getY() + 0.5 - closest.getY()) / 7.0;
            double var7 = (this.worldPosition.getZ() + 0.5 - closest.getZ()) / 7.0;
            double var9 = Math.sqrt(var3x * var3x + var5 * var5 + var7 * var7);
            double var11 = 1.0 - var9;
            if (var11 > 0.0 && var9 > 1.0E-6) {
               var11 *= var11;
               closest.setDeltaMovement(closest.getDeltaMovement().add(var3x / var9 * var11 * 0.15, var5 / var9 * var11 * 0.33, var7 / var9 * var11 * 0.15));
            }
         }
      }

      if (this.eatDelay > 0) {
         this.eatDelay--;
      } else if (this.xp < this.xpMax) {
         AABB eatBox = new AABB(this.worldPosition).inflate(0.1);
         List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, eatBox, orbx -> orbx != null && orbx.isAlive());
         if (!orbs.isEmpty()) {
            for (ExperienceOrb orb : orbs) {
               if (orb != null && orb.isAlive()) {
                  this.xp = this.xp + orb.getValue();
                  float pitch = (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F;
                  level.playSound(null, orb.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.1F, pitch);
                  orb.discard();
               }
            }

            this.markForUpdate();
         }
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.clientTicks++;
      this.rotYawPrev = this.rotYaw;
      Player playerTarget = null;
      ExperienceOrb orbTarget = null;
      if (this.level != null && this.xp < this.xpMax) {
         orbTarget = getClosestOrb(this.level, new AABB(this.worldPosition).inflate(6.0));
      }

      if (orbTarget == null && this.level != null) {
         playerTarget = getNearestPlayer(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 6.0);
      }

      Vec3 c = Vec3.atCenterOf(this.worldPosition);
      if (orbTarget != null) {
         double dx = orbTarget.getX() - c.x;
         double dz = orbTarget.getZ() - c.z;
         this.rotYawTarget = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI));
      } else if (playerTarget != null) {
         Vec3 dp = playerTarget.position();
         double dx = dp.x - c.x;
         double dz = dp.z - c.z;
         this.rotYawTarget = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI));
         long now = System.currentTimeMillis();
         if (now > this.nextSighMs) {
            float pitch = 0.8F + this.level.random.nextFloat() * 0.4F;
            this.level.playLocalSound(c.x, c.y, c.z, (SoundEvent)ModSounds.BRAIN.get(), SoundSource.BLOCKS, 0.15F, pitch, false);
            this.nextSighMs = now + 5000L + this.level.random.nextInt(25000);
         }
      } else {
         this.rotYawTarget = this.rotYaw + 0.57F;
      }

      float delta = Mth.wrapDegrees(this.rotYawTarget - this.rotYaw);
      this.rotYaw += delta * 0.04F;
      this.bob = (float)(Math.sin(this.clientTicks * 0.1) * 0.03);
   }

   @Nullable
   private static Player getNearestPlayer(@NotNull Level level, double x, double y, double z, double dist) {
      return level.getNearestPlayer(x, y, z, dist, false);
   }

   @Nullable
   private static ExperienceOrb getClosestOrb(@NotNull Level level, @NotNull AABB box) {
      List<ExperienceOrb> ents = level.getEntitiesOfClass(ExperienceOrb.class, box, orbx -> orbx != null && orbx.isAlive());
      if (ents.isEmpty()) {
         return null;
      } else {
         ExperienceOrb best = null;
         double bestDist = Double.MAX_VALUE;
         Vec3 center = box.getCenter();

         for (ExperienceOrb orb : ents) {
            double dx = orb.getX() - center.x;
            double dy = orb.getY() - center.y;
            double dz = orb.getZ() - center.z;
            double d = dx * dx + dy * dy + dz * dz;
            if (d < bestDist) {
               bestDist = d;
               best = orb;
            }
         }

         return best;
      }
   }
}
