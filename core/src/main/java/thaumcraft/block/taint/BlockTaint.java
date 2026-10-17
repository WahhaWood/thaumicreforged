package thaumcraft.block.taint;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import thaumcraft.Thaumcraft;
import thaumcraft.entities.brainless.falling_taint.EntityFallingTaint;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModMobEffect;
import thaumcraft.init.registry.ModSounds;

public class BlockTaint extends Block {
   public static final SoundType TAINT_SOUND = new SoundType(
      0.5F,
      0.8F,
      (SoundEvent)ModSounds.GORE.get(),
      (SoundEvent)ModSounds.GORE.get(),
      (SoundEvent)ModSounds.GORE.get(),
      (SoundEvent)ModSounds.GORE.get(),
      (SoundEvent)ModSounds.GORE.get()
   );
   private final BlockTaint.Type type;

   public BlockTaint(BlockTaint.Type type) {
      super(makeProps(type));
      this.type = type;
   }

   private static Properties makeProps(BlockTaint.Type type) {
      float hardness = switch (type) {
         case CRUST -> 1.75F;
         case SOIL -> 1.5F;
         case FLESH -> 0.2F;
      };
      return Properties.of().strength(hardness, 10.0F).sound(TAINT_SOUND).randomTicks();
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (this.type == BlockTaint.Type.CRUST && level.isEmptyBlock(pos.below()) && random.nextInt(10) == 0) {
         Thaumcraft.setup
            .dropletFX(
               level, pos.getX() + 0.1F + level.random.nextFloat() * 0.8F, pos.getY(), pos.getZ() + 0.1F + level.random.nextFloat() * 0.8F, 0.3F, 0.1F, 0.8F
            );
      }
   }

   public static boolean canFallBelow(Level level, BlockPos pos) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
               if (level.getBlockState(pos.offset(dx, dy, dz)).is(BlockTags.LOGS)) {
                  return false;
               }
            }
         }
      }

      BlockState below = level.getBlockState(pos);
      if (below.isAir()) {
         return true;
      } else {
         if (below.is((Block)ModBlocks.FLUX_GOO.get())) {
            FluidState fs = below.getFluidState();
            if (!fs.isEmpty() && fs.getAmount() >= 4) {
               return false;
            }
         }

         if (below.is(Blocks.FIRE)) {
            return true;
         } else {
            return below.getFluidState().is(FluidTags.WATER) || below.getFluidState().is(FluidTags.LAVA)
               ? true
               : below.is((Block)ModBlocks.FIBROUS_TAINT.get()) || below.is((Block)ModBlocks.TAINTED_PLANT.get()) || below.canBeReplaced();
         }
      }
   }

   public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
      if (this.type != BlockTaint.Type.FLESH
         && !level.isClientSide
         && entity instanceof LivingEntity living
         && living.getType().getCategory() != MobCategory.MONSTER) {
         RandomSource r = level.random;
         if (living instanceof Player) {
            if (r.nextInt(100) == 0) {
               living.addEffect(new MobEffectInstance(ModMobEffect.FLUX_TAINT, 80, 0, false, false));
            }
         } else if (r.nextInt(20) == 0) {
            living.addEffect(new MobEffectInstance(ModMobEffect.FLUX_TAINT, 160, 0, false, false));
         }
      }

      super.stepOn(level, pos, state, entity);
   }

   @Override
   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (this.type == BlockTaint.Type.FLESH) {
         return;
      }

      boolean inTaintBiome = level.getBiome(pos).is(thaumcraft.init.registry.ModBiomes.TAINT);

      if (inTaintBiome) {
         if (random.nextInt(200) == 0 && thaumcraft.world.biomes.TaintHelper.countAdjacentTaint(level, pos) >= 2) {
            int bx = pos.getX() + random.nextInt(3) - 1;
            int bz = pos.getZ() + random.nextInt(3) - 1;
            BlockPos bpos = new BlockPos(bx, pos.getY(), bz);
            if (!level.getBiome(bpos).is(thaumcraft.init.registry.ModBiomes.TAINT)) {
               thaumcraft.util.Utils.setBiomeAt(level, bpos, thaumcraft.init.registry.ModBiomes.TAINT);
            }
         }

         if (this.type == BlockTaint.Type.CRUST) {
            if (this.tryToFall(level, pos, state)) {
               return;
            }

            if (thaumcraft.init.registry.Config.spawnTaintSpore && level.isEmptyBlock(pos.above()) && random.nextInt(200) == 0) {
               List<thaumcraft.entities.smart.taint.spore.EntityTaintSporeSwarmer> targets = level.getEntitiesOfClass(
                  thaumcraft.entities.smart.taint.spore.EntityTaintSporeSwarmer.class,
                  new net.minecraft.world.phys.AABB(pos).inflate(16.0)
               );
               if (targets.isEmpty()) {
                  level.removeBlock(pos, false);
                  thaumcraft.entities.smart.taint.spore.EntityTaintSporeSwarmer spore = new thaumcraft.entities.smart.taint.spore.EntityTaintSporeSwarmer(level);
                  spore.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
                  level.addFreshEntity(spore);
                  level.playSound(null, pos, (SoundEvent)ModSounds.ROOTS.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.1F, 0.9F + random.nextFloat() * 0.2F);
                  return;
               }
            }

            boolean surrounded = level.getBlockState(pos.above()).is(this);
            if (surrounded) {
               for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                  if (!level.getBlockState(pos.relative(dir)).is(this)) {
                     surrounded = false;
                     break;
                  }
               }
            }
            if (surrounded) {
               level.setBlock(pos, ((Block)ModBlocks.FLUX_GOO.get()).defaultBlockState(), 3);
            }
         }
      } else {
         if (this.type == BlockTaint.Type.CRUST && random.nextInt(20) == 0) {
            level.setBlock(pos, ((Block)ModBlocks.FLUX_GOO.get()).defaultBlockState(), 3);
         } else if (this.type == BlockTaint.Type.SOIL && random.nextInt(10) == 0) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
         }
      }
   }

   private boolean tryToFall(ServerLevel level, BlockPos pos, BlockState state) {
      BlockPos below = pos.below();
      if (canFallBelow(level, below) && below.getY() >= level.getMinBuildHeight()) {
         EntityFallingTaint falling = new EntityFallingTaint(
            level,
            pos.getX() + 0.5,
            pos.getY(),
            pos.getZ() + 0.5,
            state,
            pos
         );
         level.removeBlock(pos, false);
         this.onStartFalling(falling);
         level.addFreshEntity(falling);
         return true;
      }
      return false;
   }

   protected void onStartFalling(EntityFallingTaint entity) {
   }

   public void onFinishFalling(ServerLevel level, BlockPos pos, BlockState placedState) {
   }

   public List<ItemStack> getDrops(BlockState state, Builder builder) {
      if (this.type == BlockTaint.Type.SOIL) {
         return List.of(new ItemStack(Blocks.DIRT));
      } else if (this.type != BlockTaint.Type.FLESH) {
         return List.of();
      } else {
         ItemStack tool = (ItemStack)builder.getOptionalParameter(LootContextParams.TOOL);
         Holder<Enchantment> silkTouchHolder = builder.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
         boolean silkTouch = tool != null && !tool.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder, tool) > 0;
         return silkTouch ? List.of(new ItemStack(this.asItem())) : List.of(new ItemStack(Items.ROTTEN_FLESH, 9));
      }
   }

   public static enum Type {
      CRUST,
      SOIL,
      FLESH;
   }
}
