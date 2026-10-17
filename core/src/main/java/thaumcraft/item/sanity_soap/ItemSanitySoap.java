package thaumcraft.item.sanity_soap;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModSounds;

public class ItemSanitySoap extends Item {
   public ItemSanitySoap() {
      super(new Properties().durability(200));
   }

   @NotNull
   public UseAnim getUseAnimation(@NotNull ItemStack p_41452_) {
      return UseAnim.BRUSH;
   }

   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 200;
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      player.startUsingItem(hand);
      return InteractionResultHolder.consume(player.getItemInHand(hand));
   }

   public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int count) {
      int ticks = this.getUseDuration(stack, entity) - count;
      if (ticks > 195) {
         entity.stopUsingItem();
      }

      if (level.isClientSide) {
         if (level.random.nextFloat() < 0.2F) {
            level.playLocalSound(
               entity.getX(),
               entity.getY(),
               entity.getZ(),
               (SoundEvent)ModSounds.ROOTS.get(),
               SoundSource.PLAYERS,
               0.1F,
               1.5F + level.random.nextFloat() * 0.2F,
               false
            );
         }

         for (int unused = 0; unused < Thaumcraft.setup.particleCount(5); unused++) {
            Thaumcraft.setup
               .crucibleBubble(
                  Thaumcraft.setup.getClientWorld(),
                  (float)entity.getY() - 0.5F + level.random.nextFloat(),
                  (float)entity.getBoundingBox().minY + level.random.nextFloat() * entity.getBbHeight(),
                  (float)entity.getZ() - 0.5F + level.random.nextFloat(),
                  1.0F,
                  0.8F,
                  0.9F
               );
         }
      }
   }

   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int count) {
      int qq = this.getUseDuration(stack, entity) - count;
      if (qq > 195 && entity instanceof Player player) {
         stack.shrink(1);
         if (!level.isClientSide) {
            float chance = 0.33F;
            if (player.hasEffect(thaumcraft.init.registry.ModMobEffect.WARP_WARD)) {
               chance += 0.25F;
            }

            net.minecraft.core.BlockPos pos = player.blockPosition();
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
            if (state.is(thaumcraft.init.registry.ModBlocks.FLUID_PURE.get())) {
               chance += 0.25F;
            }

            thaumcraft.research.PlayerKnowledge knowledge = thaumcraft.research.PlayerKnowledge.of(player);
            if (level.random.nextFloat() < chance && knowledge.getWarpSticky() > 0) {
               Thaumcraft.addStickyWarpToPlayer(player, -1);
            }

            if (knowledge.getWarpTemp() > 0) {
               Thaumcraft.addWarpToPlayer(player, -knowledge.getWarpTemp(), true);
            }
         } else {
            level.playLocalSound(
               player.getX(),
               player.getY(),
               player.getZ(),
               (SoundEvent)ModSounds.CRAFT_START.get(),
               SoundSource.PLAYERS,
               0.25F,
               1.0F,
               false
            );

            for (int a = 0; a < Thaumcraft.setup.particleCount(20); a++) {
               Thaumcraft.setup
                  .crucibleBubble(
                     Thaumcraft.setup.getClientWorld(),
                     (float)player.getX() - 0.5F + level.random.nextFloat() * 1.5F,
                     (float)player.getBoundingBox().minY + level.random.nextFloat() * player.getBbHeight(),
                     (float)player.getZ() - 0.5F + level.random.nextFloat() * 1.5F,
                     1.0F,
                     0.7F,
                     0.9F
                  );
            }
         }
      }
   }
}
