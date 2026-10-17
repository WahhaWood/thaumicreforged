package thaumcraft.item.wands.foci;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.block.hole.HoleBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusPortableHole extends ItemFocusBasic {
   private static final AspectList COST = new AspectList().add(Aspect.ENTROPY, 10).add(Aspect.AIR, 10);
   public static final ResourceLocation OUTER_DIM = ResourceLocation.fromNamespaceAndPath("thaumcraft", "outer_lands");

   public ItemFocusPortableHole() {
      super("portable_hole");
   }

   @Override
   public int getFocusColor(ItemStack itemstack) {
      return 594985;
   }

   @Override
   public AspectList getVisCost(ItemStack itemstack) {
      return COST.copy();
   }

   public static boolean createHole(Level level, int x, int y, int z, int side, byte count, int max) {
      return createHole(null, level, x, y, z, side, count, max);
   }

   public static boolean createHole(@Nullable Player player, Level level, int x, int y, int z, int side, byte count, int max) {
      BlockPos pos = new BlockPos(x, y, z);
      BlockState bi = level.getBlockState(pos);
      if (level.getBlockEntity(pos) == null
         && !ThaumcraftApi.portableHoleBlackList.contains(bi.getBlock())
         && !bi.is(Blocks.BEDROCK)
         && !bi.is((Block)ModBlocks.HOLE.get())
         && !bi.isAir()
         && bi.getDestroySpeed(level, pos) != -1.0F) {
         BlockState oldState = bi;
         level.setBlock(pos, ModBlocks.HOLE.get().defaultBlockState(), 3);
         if (level.getBlockEntity(pos) instanceof HoleBlockEntity hole) {
            hole.init(oldState, (short)max, count, (byte)side, player);
         }
         return true;
      }
      return false;
   }

   @Override
   public ItemStack onFocusBlockClick(ItemStack wandStack, Level level, Player player, InteractionHand hand, BlockPos blockPos, Direction direction) {
      if (!level.isClientSide && level.dimension().location().equals(OUTER_DIM)) {
         level.playSound(null, blockPos, (SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
         return wandStack;
      } else {
         ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
         ItemStack focusStack = wand.getFocusItem(wandStack);
         int ii = blockPos.getX();
         int jj = blockPos.getY();
         int kk = blockPos.getZ();
         int enlarge = this.getUpgradeLevel(focusStack, FocusUpgradeType.enlarge);
         int maxdis = 33 + enlarge * 8;
         int cx = ii;
         int cy = jj;
         int cz = kk;

         int len;
         for (len = 0; len < maxdis; len++) {
            BlockPos p = new BlockPos(cx, cy, cz);
            BlockState s = level.getBlockState(p);
            if (ThaumcraftApi.portableHoleBlackList.contains(s.getBlock())
               || s.is(Blocks.BEDROCK)
               || s.is((Block)ModBlocks.HOLE.get())
               || s.isAir()
               || s.getDestroySpeed(level, p) < 0.0F) {
               break;
            }

            switch (direction.get3DDataValue()) {
               case 0:
                  cy++;
                  break;
               case 1:
                  cy--;
                  break;
               case 2:
                  cz++;
                  break;
               case 3:
                  cz--;
                  break;
               case 4:
                  cx++;
                  break;
               case 5:
                  cx--;
            }
         }

         AspectList cost = this.getVisCost(focusStack);
         Aspect[] aspects = cost.getAspects();

         for (Aspect a : aspects) {
            cost.merge(a, cost.getAmount(a) * len);
         }

         if (wand.consumeAllVis(wandStack, player, cost, true, false)) {
            int extend = this.getUpgradeLevel(focusStack, FocusUpgradeType.extend);
            short max = (short)(120 + 60 * extend);
            // Счётчик норы в hole entity — byte, поэтому клампим длину, иначе длинные
            // норы обрежутся или уйдут в минус при касте к byte
            int clampedLen = Math.min(len, 126);
            createHole(player, level, ii, jj, kk, direction.get3DDataValue(), (byte)(clampedLen + 1), max);
            swing(player, wandStack);
            if (!level.isClientSide) {
               level.playSound(null, blockPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
         }

         return wandStack;
      }
   }

   private static void swing(Player player, ItemStack wandStack) {
      InteractionHand hand = player.getMainHandItem() == wandStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
      player.swing(hand, true);
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      return switch (rank) {
         case 1, 2, 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.extend};
         default -> null;
      };
   }
}
