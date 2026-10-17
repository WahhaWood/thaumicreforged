package thaumcraft.item.wands.foci;

import java.util.HashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.Tags.Blocks;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusExcavation extends ItemFocusBasic {
   private static final AspectList cost = new AspectList().add(Aspect.EARTH, 15);
   private static AspectList costSilk = null;
   private static AspectList costDowsing = null;
   static HashMap<String, Long> soundDelay = new HashMap<>();
   static HashMap<String, Object> beam = new HashMap<>();
   static HashMap<String, Float> breakcount = new HashMap<>();
   static HashMap<String, BlockPos> last = new HashMap<>();
   // Время последнего тика по ключу игрока — для ленивой чистки зависших записей
   // (дисконнект/смерть без onPlayerStoppedUsingFocus). Долгая неактивность — удаляем.
   static HashMap<String, Long> lastSeen = new HashMap<>();
   private static final long STALE_TIMEOUT_MS = 30000L;
   private static int purgeCounter = 0;
   public static FocusUpgradeType dowsing = new FocusUpgradeType(
      20, Thaumcraft.location("textures/foci/dowsing.png"), "focus.upgrade.dowsing.name", "focus.upgrade.dowsing.text", new AspectList().add(Aspect.MINE, 1)
   );

   public ItemFocusExcavation() {
      super("excavation");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "BE" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getFocusColor(ItemStack itemstack) {
      return 409606;
   }

   @Override
   public AspectList getVisCost(ItemStack itemstack) {
      if (this.isUpgradedWith(itemstack, FocusUpgradeType.silktouch)) {
         if (costSilk == null) {
            costSilk = new AspectList()
               .add(Aspect.AIR, 1)
               .add(Aspect.FIRE, 1)
               .add(Aspect.EARTH, 1)
               .add(Aspect.WATER, 1)
               .add(Aspect.ORDER, 1)
               .add(Aspect.ENTROPY, 1);
            costSilk.add(cost);
         }

         return costSilk;
      } else if (this.isUpgradedWith(itemstack, dowsing)) {
         if (costDowsing == null) {
            costDowsing = new AspectList().add(Aspect.FIRE, 2).add(Aspect.ORDER, 2);
            costDowsing.add(cost);
         }

         return costDowsing;
      } else {
         return cost;
      }
   }

   @Override
   public boolean isVisCostPerTick(ItemStack itemstack) {
      return true;
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level world, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return wandStack;
   }

   @Override
   public void onUsingFocusTick(ItemStack wandStack, Player player, int count) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      ItemStack focusStack = wand.getFocusItem(wandStack);
      if (!wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), false, false)) {
         player.stopUsingItem();
      } else {
         String pp = keyFor(player);
         long now = System.currentTimeMillis();
         lastSeen.put(pp, now);
         // Ленивая чистка: записи игроков, давно не тикавших (вышли/умерли),
         // удаляем из всех карт, чтобы состояние не копилось вечно
         if (++purgeCounter >= 100) {
            purgeCounter = 0;
            purgeStale(now);
         }

         soundDelay.putIfAbsent(pp, 0L);
         breakcount.putIfAbsent(pp, 0.0F);
         last.putIfAbsent(pp, BlockPos.ZERO);
         Level level = player.level();
         HitResult hit = player.pick(10.0, 1.0F, false);
         BlockHitResult mop = hit.getType() == Type.BLOCK ? (BlockHitResult)hit : null;
         Vec3 target = player.getEyePosition().add(player.getViewVector(0.0F).scale(10.0));
         int impact = 0;
         if (mop != null) {
            target = mop.getLocation();
            impact = 5;
            if (!level.isClientSide && soundDelay.get(pp) < System.currentTimeMillis()) {
               level.playSound(player, target.x, target.y, target.z, (SoundEvent)ModSounds.RUMBLE.get(), SoundSource.MASTER, 0.3F, 1.0F);
               soundDelay.put(pp, System.currentTimeMillis() + 1200L);
            }
         } else {
            soundDelay.put(pp, 0L);
         }

         if (level.isClientSide) {
            beam.put(pp, Thaumcraft.setup.beamCont(level, player, target.x, target.y, target.z, 2, 166, false, impact > 0 ? 2.0F : 0.0F, beam.get(pp), impact));
         }

         if (mop != null && level.mayInteract(player, mop.getBlockPos())) {
            BlockPos blockPos = mop.getBlockPos();
            BlockState state = level.getBlockState(blockPos);
            float hardness = state.getDestroySpeed(level, blockPos);
            if (hardness >= 0.0F) {
               int pot = wand.getFocusPotency(wandStack);
               float speed = 0.05F + pot * 0.1F;
               if (state.is(Blocks.STONES) || state.is(Blocks.SANDS) || state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) {
                  speed = 0.25F + pot * 0.25F;
               }

               if (state.is(net.minecraft.world.level.block.Blocks.OBSIDIAN)) {
                  speed *= 3.0F;
               }

               if (last.get(pp).equals(blockPos)) {
                  float bc = breakcount.get(pp);
                  if (level.isClientSide && bc > 0.0F && !state.isAir()) {
                     int progress = (int)(bc / hardness * 9.0F);
                     Thaumcraft.setup.excavateFX(blockPos.getX(), blockPos.getY(), blockPos.getZ(), player, Block.getId(state), 0, progress);
                  }

                  if (level.isClientSide) {
                     if (bc >= hardness) {
                        breakcount.put(pp, 0.0F);
                     } else {
                        breakcount.put(pp, bc + speed);
                     }
                  } else if (bc >= hardness && wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), true, false)) {
                     ServerPlayer sp = (ServerPlayer)player;
                     if (sp.gameMode.destroyBlock(blockPos)) {
                        int enlarge = wand.getFocusUpgradeLevel(wandStack, FocusUpgradeType.enlarge);

                        for (int a = 0; a < enlarge; a++) {
                           if (wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), false, false) && this.breakNeighbour(sp, blockPos, state)) {
                              wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), true, false);
                           }
                        }
                     }

                     last.remove(pp);
                     breakcount.put(pp, 0.0F);
                  } else {
                     breakcount.put(pp, bc + speed);
                  }
               } else {
                  last.put(pp, blockPos);
                  breakcount.put(pp, 0.0F);
               }
            }
         } else {
            last.remove(pp);
            breakcount.put(pp, 0.0F);
         }
      }
   }

   boolean breakNeighbour(ServerPlayer player, BlockPos blockPos, BlockState blockState) {
      for (Direction dir : Util.shuffledCopy(Direction.values(), player.getRandom())) {
         BlockPos pos = blockPos.relative(dir);
         BlockState st = player.level().getBlockState(pos);
         if (st.equals(blockState) && player.gameMode.destroyBlock(pos)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void onPlayerStoppedUsingFocus(ItemStack wandStack, Level level, Player player, int count) {
      String pp = keyFor(player);
      soundDelay.remove(pp);
      breakcount.remove(pp);
      last.remove(pp);
      beam.remove(pp);
      lastSeen.remove(pp);
   }

   // Ключ по UUID, а не по нику: после смены ника старая запись иначе висит мёртвым грузом.
   // Префикс стороны оставляем — клиент и сервер ведут счётчики независимо, как раньше.
   private static String keyFor(Player player) {
      if (player.level().isClientSide) {
         return "R" + player.getUUID();
      } else {
         return "S" + player.getUUID();
      }
   }

   private static void purgeStale(long now) {
      lastSeen.entrySet().removeIf(entry -> {
         if (now - entry.getValue() > STALE_TIMEOUT_MS) {
            String pp = entry.getKey();
            soundDelay.remove(pp);
            breakcount.remove(pp);
            last.remove(pp);
            beam.remove(pp);
            return true;
         }

         return false;
      });
   }

   @Override
   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack itemstack) {
      return ItemFocusBasic.WandFocusAnimation.CHARGE;
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      return switch (rank) {
         case 1 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.treasure};
         case 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.enlarge};
         case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.treasure, dowsing};
         case 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.treasure, FocusUpgradeType.silktouch};
         default -> null;
      };
   }
}
